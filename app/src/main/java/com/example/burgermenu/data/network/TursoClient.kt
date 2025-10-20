package com.example.burgermenu.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class TursoResponse(
    val results: List<TursoResult> = emptyList()
)

@Serializable
data class TursoResult(
    val columns: List<String> = emptyList(),
    val rows: List<JsonArray> = emptyList(),
    val type: String? = null,
    val response: TursoResultResponse? = null
)

@Serializable
data class TursoResultResponse(
    val type: String? = null,
    val result: TursoQueryResult? = null
)

@Serializable
data class TursoQueryResult(
    val cols: List<TursoColumn> = emptyList(),
    val rows: List<JsonArray> = emptyList(),
    val affected_row_count: Int = 0,
    val last_insert_rowid: String? = null
)

@Serializable
data class TursoColumn(
    val name: String = "",
    val decltype: String? = null
)

// Clases para el request body
@Serializable
data class TursoRequest(
    val requests: List<TursoExecuteRequest>
)

@Serializable
data class TursoExecuteRequest(
    val type: String = "execute",
    val stmt: TursoStatement
)

@Serializable
data class TursoStatement(
    val sql: String
)

object TursoClient {
    
    private const val TURSO_DATABASE_URL = "https://restaurant-prograshaco.aws-us-west-2.turso.io"
    private const val TURSO_AUTH_TOKEN = "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhIjoicnciLCJpYXQiOjE3NTk5NjQ1NzIsImlkIjoiMjdjMDU0NGUtMTJlYi00ZmU5LTk1ZWQtYzVkNjRkOTQwZGI0IiwicmlkIjoiM2MxMGU0NmYtYzBlMi00ZTc0LTg0MGEtMTMxNTBmZDczMmU3In0.YhN_SBSWv6evTSSOayVcUJboRv_yBHvcdx6dmFQhfUjuBpaDPAd6MVWdqt2lPAlcCMk8jjFemjAUDWzSMX3GAQ"
    
    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
    
    suspend fun executeQuery(sql: String): Result<TursoResponse> {
        return try {
            android.util.Log.d("TursoClient", "Ejecutando SQL: $sql")
            
            // Crear el request body usando serialización para evitar problemas de escape
            val requestBody = TursoRequest(
                requests = listOf(
                    TursoExecuteRequest(
                        type = "execute",
                        stmt = TursoStatement(sql = sql)
                    )
                )
            )
            
            android.util.Log.d("TursoClient", "Request body object: $requestBody")
            
            val response: HttpResponse = httpClient.post("$TURSO_DATABASE_URL/v2/pipeline") {
                header("Authorization", "Bearer $TURSO_AUTH_TOKEN")
                header("Content-Type", "application/json")
                setBody(requestBody)
            }
            
            android.util.Log.d("TursoClient", "Response status: ${response.status}")
            val responseText = response.bodyAsText()
            android.util.Log.d("TursoClient", "Response body RAW: $responseText")
            
            if (response.status.isSuccess()) {
                try {
                    // Parsear el formato v2 de Turso
                    val tursoResponse = response.body<TursoResponse>()
                    android.util.Log.d("TursoClient", "✅ Parsed response: $tursoResponse")
                    Result.success(tursoResponse)
                } catch (e: Exception) {
                    android.util.Log.e("TursoClient", "❌ JSON Parse Error: ${e.message}")
                    android.util.Log.e("TursoClient", "❌ Raw response that failed: $responseText")
                    Result.failure(Exception("JSON Parse Error: ${e.message}"))
                }
            } else {
                android.util.Log.e("TursoClient", "❌ HTTP Error: ${response.status}")
                Result.failure(Exception("HTTP ${response.status}: $responseText"))
            }
            
        } catch (e: Exception) {
            android.util.Log.e("TursoClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Función helper para convertir filas a Map (maneja el formato v2 de Turso)
    fun rowsToMaps(columns: List<String>, rows: List<JsonArray>): List<Map<String, String>> {
        return rows.map { row ->
            columns.mapIndexed { index, column ->
                val value = if (index < row.size) {
                    val element = row[index]
                    android.util.Log.d("TursoClient", "Procesando columna '$column': $element")
                    
                    when (element) {
                        is JsonPrimitive -> element.content
                        is JsonObject -> {
                            // Formato Turso v2: {"type":"text","value":"valor_real"} o {"type":"null"}
                            val typeValue = element["type"]?.let { 
                                when (it) {
                                    is JsonPrimitive -> it.content
                                    else -> it.toString()
                                }
                            }
                            
                            val result = when (typeValue) {
                                "null" -> "" // Manejar valores null como string vacío
                                else -> {
                                    element["value"]?.let { valueElement ->
                                        when (valueElement) {
                                            is JsonPrimitive -> valueElement.content
                                            else -> valueElement.toString()
                                        }
                                    } ?: ""
                                }
                            }
                            
                            android.util.Log.d("TursoClient", "Columna '$column' -> tipo: '$typeValue', resultado: '$result'")
                            result
                        }
                        else -> {
                            android.util.Log.d("TursoClient", "Columna '$column' -> tipo desconocido: ${element::class.simpleName}")
                            element.toString()
                        }
                    }
                } else {
                    ""
                }
                column to value
            }.toMap()
        }
    }
    
    // Función helper para extraer datos del formato v2
    fun extractDataFromV2Response(tursoResponse: TursoResponse): Pair<List<String>, List<JsonArray>> {
        // Buscar en el formato v2
        tursoResponse.results.forEach { result ->
            result.response?.result?.let { queryResult ->
                val columns = queryResult.cols.map { it.name }
                val rows = queryResult.rows
                if (columns.isNotEmpty()) {
                    return Pair(columns, rows)
                }
            }
        }
        
        // Fallback al formato v1
        tursoResponse.results.firstOrNull()?.let { result ->
            if (result.columns.isNotEmpty()) {
                return Pair(result.columns, result.rows)
            }
        }
        
        return Pair(emptyList(), emptyList())
    }
}