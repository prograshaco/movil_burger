package com.example.burgermenu.data.network

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val conversion_rates: Map<String, Double>
)

object CurrencyService {
    private const val BASE_URL = "https://api.exchangerate-api.com/v4/latest"
    private val json = Json { ignoreUnknownKeys = true }
    
    // Caché de tasas de cambio
    private var cachedRates: Map<String, Double>? = null
    private var lastUpdate: Long = 0
    private const val CACHE_DURATION = 3600000 // 1 hora en milisegundos
    
    suspend fun getExchangeRates(baseCurrency: String = "USD"): Result<Map<String, Double>> {
        return try {
            // Usar caché si está disponible y no ha expirado
            val now = System.currentTimeMillis()
            if (cachedRates != null && (now - lastUpdate) < CACHE_DURATION) {
                Log.d("CurrencyService", "Usando tasas de cambio en caché")
                return Result.success(cachedRates!!)
            }
            
            Log.d("CurrencyService", "Obteniendo tasas de cambio desde API...")
            Log.d("CurrencyService", "URL: $BASE_URL/$baseCurrency")
            
            val response: HttpResponse = BurgerApiClient.httpClient.get("$BASE_URL/$baseCurrency")
            
            Log.d("CurrencyService", "Status: ${response.status.value}")
            
            if (response.status.value == 200) {
                val body = response.bodyAsText()
                Log.d("CurrencyService", "Response body (primeros 200 chars): ${body.take(200)}")
                
                val data = json.decodeFromString<ExchangeRateResponse>(body)
                
                cachedRates = data.conversion_rates
                lastUpdate = now
                
                Log.d("CurrencyService", "✅ Tasas de cambio obtenidas: ${data.conversion_rates.size} monedas")
                Log.d("CurrencyService", "Tasa CLP: ${data.conversion_rates["CLP"]}")
                Log.d("CurrencyService", "Tasa EUR: ${data.conversion_rates["EUR"]}")
                Result.success(data.conversion_rates)
            } else {
                val errorBody = response.bodyAsText()
                Log.e("CurrencyService", "❌ Error HTTP: ${response.status}")
                Log.e("CurrencyService", "Error body: $errorBody")
                
                // Usar tasas fijas como fallback
                Log.d("CurrencyService", "Usando tasas fijas como fallback")
                val fallbackRates = mapOf(
                    "USD" to 1.0,
                    "EUR" to 0.92,
                    "CLP" to 950.0,
                    "MXN" to 17.0,
                    "ARS" to 350.0,
                    "COP" to 4000.0,
                    "BRL" to 5.0,
                    "GBP" to 0.79,
                    "JPY" to 150.0,
                    "CNY" to 7.2
                )
                cachedRates = fallbackRates
                lastUpdate = now
                Result.success(fallbackRates)
            }
        } catch (e: Exception) {
            Log.e("CurrencyService", "❌ Exception: ${e.message}", e)
            e.printStackTrace()
            
            // Usar tasas fijas como fallback
            if (cachedRates == null) {
                Log.d("CurrencyService", "Usando tasas fijas debido a excepción")
                val fallbackRates = mapOf(
                    "USD" to 1.0,
                    "EUR" to 0.92,
                    "CLP" to 950.0,
                    "MXN" to 17.0,
                    "ARS" to 350.0,
                    "COP" to 4000.0,
                    "BRL" to 5.0,
                    "GBP" to 0.79,
                    "JPY" to 150.0,
                    "CNY" to 7.2
                )
                cachedRates = fallbackRates
                lastUpdate = System.currentTimeMillis()
            }
            Result.success(cachedRates!!)
        }
    }
    
    suspend fun convertPrice(amount: Double, fromCurrency: String, toCurrency: String): Result<Double> {
        return try {
            Log.d("CurrencyService", "=== CONVERTIR PRECIO ===")
            Log.d("CurrencyService", "Monto: $amount")
            Log.d("CurrencyService", "De: $fromCurrency")
            Log.d("CurrencyService", "A: $toCurrency")
            
            if (fromCurrency == toCurrency) {
                Log.d("CurrencyService", "Misma moneda, retornando monto original")
                return Result.success(amount)
            }
            
            val rates = getExchangeRates(fromCurrency).getOrThrow()
            Log.d("CurrencyService", "Tasas obtenidas: ${rates.size} monedas")
            
            val rate = rates[toCurrency]
            if (rate == null) {
                Log.e("CurrencyService", "Moneda $toCurrency no encontrada en tasas")
                Log.d("CurrencyService", "Monedas disponibles: ${rates.keys.take(10)}")
                return Result.failure(Exception("Moneda no encontrada: $toCurrency"))
            }
            
            val converted = amount * rate
            Log.d("CurrencyService", "Tasa de cambio: $rate")
            Log.d("CurrencyService", "Resultado: $amount $fromCurrency = $converted $toCurrency")
            Result.success(converted)
        } catch (e: Exception) {
            Log.e("CurrencyService", "Error en conversión: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun clearCache() {
        cachedRates = null
        lastUpdate = 0
    }
}

// Monedas soportadas
enum class Currency(val code: String, val symbol: String, val displayName: String) {
    USD("USD", "$", "Dólar estadounidense"),
    EUR("EUR", "€", "Euro"),
    CLP("CLP", "$", "Peso chileno"),
    MXN("MXN", "$", "Peso mexicano"),
    ARS("ARS", "$", "Peso argentino"),
    COP("COP", "$", "Peso colombiano"),
    BRL("BRL", "R$", "Real brasileño"),
    GBP("GBP", "£", "Libra esterlina"),
    JPY("JPY", "¥", "Yen japonés"),
    CNY("CNY", "¥", "Yuan chino");
    
    companion object {
        fun fromCode(code: String): Currency? {
            return values().find { it.code == code }
        }
    }
}
