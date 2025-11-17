package com.example.burgermenu.data.network

import android.util.Log
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

@Serializable
data class ApiProduct(
    val _id: String? = null,
    val name: String,
    val description: String = "",
    val price: Double,
    val category: String,
    val imageUrl: String = "",
    val available: Int = 1 // La API devuelve 0 o 1, no boolean
) {
    val isAvailable: Boolean get() = available == 1
}

@Serializable
data class ApiProductResponse(
    val success: Boolean = true,
    val data: ApiProduct? = null,
    val message: String = ""
)

@Serializable
data class ApiProductsResponse(
    val success: Boolean = true,
    val data: List<ApiProduct> = emptyList(),
    val message: String = ""
)

object BurgerApiClient {
    
    private const val BASE_URL = "https://api-burger.onrender.com/api"
    
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
    
    suspend fun getAllProducts(): Result<List<ApiProduct>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo todos los productos de $BASE_URL/products")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/products")
            
            Log.d("BurgerApiClient", "Response status: ${response.status}")
            
            if (response.status.isSuccess()) {
                val products = response.body<List<ApiProduct>>()
                Log.d("BurgerApiClient", "✅ Productos obtenidos: ${products.size}")
                Result.success(products)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error: ${response.status} - $errorText")
                Result.failure(Exception("HTTP ${response.status}: $errorText"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(id: String): Result<ApiProduct> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo producto con ID: $id")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/products/$id")
            
            if (response.status.isSuccess()) {
                val product = response.body<ApiProduct>()
                Log.d("BurgerApiClient", "✅ Producto obtenido: ${product.name}")
                Result.success(product)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error: ${response.status} - $errorText")
                Result.failure(Exception("HTTP ${response.status}: $errorText"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(product: ApiProduct): Result<ApiProduct> {
        return try {
            Log.d("BurgerApiClient", "Creando producto: ${product.name}")
            
            val response: HttpResponse = httpClient.post("$BASE_URL/products") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            
            if (response.status.isSuccess()) {
                val createdProduct = response.body<ApiProduct>()
                Log.d("BurgerApiClient", "✅ Producto creado: ${createdProduct.name}")
                Result.success(createdProduct)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error: ${response.status} - $errorText")
                Result.failure(Exception("HTTP ${response.status}: $errorText"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(id: String, product: ApiProduct): Result<ApiProduct> {
        return try {
            Log.d("BurgerApiClient", "Actualizando producto con ID: $id")
            
            val response: HttpResponse = httpClient.put("$BASE_URL/products/$id") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            
            if (response.status.isSuccess()) {
                val updatedProduct = response.body<ApiProduct>()
                Log.d("BurgerApiClient", "✅ Producto actualizado: ${updatedProduct.name}")
                Result.success(updatedProduct)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error: ${response.status} - $errorText")
                Result.failure(Exception("HTTP ${response.status}: $errorText"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(id: String): Result<Boolean> {
        return try {
            Log.d("BurgerApiClient", "Eliminando producto con ID: $id")
            
            val response: HttpResponse = httpClient.delete("$BASE_URL/products/$id")
            
            if (response.status.isSuccess()) {
                Log.d("BurgerApiClient", "✅ Producto eliminado")
                Result.success(true)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error: ${response.status} - $errorText")
                Result.failure(Exception("HTTP ${response.status}: $errorText"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
