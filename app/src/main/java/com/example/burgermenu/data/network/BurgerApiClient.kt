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
    val id: String? = null,
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

@Serializable
data class ApiUser(
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String = "",
    val name: String,
    val role: String = "customer",
    val phone: String = "",
    val address: String = ""
)

@Serializable
data class ApiOrder(
    val id: String? = null,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val userAddress: String,
    val items: String, // JSON string
    val total: Double,
    val status: String,
    val createdAt: String? = null
)

@Serializable
data class ApiCreateOrderRequest(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val userAddress: String,
    val items: String,
    val total: Double
)

object BurgerApiClient {
    
    // URL de producción (Render)
    private const val BASE_URL = "https://api-burger.onrender.com/api"
    
    private var authToken: String? = null

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

        // Interceptor para agregar el token a todas las peticiones
        install(io.ktor.client.plugins.DefaultRequest) {
            authToken?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    fun setToken(token: String) {
        authToken = token
    }

    suspend fun login(username: String, password: String): Result<String> {
        return try {
            Log.d("BurgerApiClient", "=== INICIANDO LOGIN ===")
            Log.d("BurgerApiClient", "Usuario: $username")
            Log.d("BurgerApiClient", "URL: $BASE_URL/auth/login")
            
            // Intentar con el endpoint de autenticación real
            try {
                val response: HttpResponse = httpClient.post("$BASE_URL/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("username" to username, "password" to password))
                }

                Log.d("BurgerApiClient", "Status de respuesta: ${response.status}")
                
                if (response.status.isSuccess()) {
                    try {
                        val responseBody = response.body<Map<String, String>>()
                        Log.d("BurgerApiClient", "Respuesta del servidor: $responseBody")
                        
                        val token = responseBody["accessToken"] ?: responseBody["token"] ?: responseBody["jwt"]
                        
                        if (token != null) {
                            authToken = token
                            Log.d("BurgerApiClient", "✅ LOGIN EXITOSO - Token JWT obtenido")
                            Log.d("BurgerApiClient", "Token (primeros 20 chars): ${token.take(20)}...")
                            return Result.success(token)
                        } else {
                            Log.e("BurgerApiClient", "❌ Token no encontrado en respuesta")
                            Log.e("BurgerApiClient", "Claves disponibles: ${responseBody.keys}")
                        }
                    } catch (e: Exception) {
                        Log.e("BurgerApiClient", "❌ Error parseando respuesta: ${e.message}")
                        val rawBody = response.bodyAsText()
                        Log.e("BurgerApiClient", "Respuesta raw: $rawBody")
                    }
                } else {
                    val errorText = response.bodyAsText()
                    Log.e("BurgerApiClient", "❌ Error en login: ${response.status}")
                    Log.e("BurgerApiClient", "Detalle: $errorText")
                    Log.d("BurgerApiClient", "Intentando método alternativo...")
                }
            } catch (e: Exception) {
                Log.e("BurgerApiClient", "❌ Excepción en /auth/login: ${e.message}", e)
                Log.d("BurgerApiClient", "Intentando método alternativo...")
            }
            
            // Si el endpoint de auth no existe, verificar credenciales contra la lista de usuarios
            // y generar un token simulado (esto NO funcionará para operaciones protegidas)
            val usersResult = getAllUsers()
            
            if (usersResult.isSuccess) {
                val users = usersResult.getOrThrow()
                val user = users.find { 
                    it.username == username && it.password == password 
                }
                
                if (user != null) {
                    // Login exitoso pero sin token real
                    val fakeToken = "fake_token_${System.currentTimeMillis()}"
                    authToken = fakeToken
                    Log.w("BurgerApiClient", "⚠️ Login exitoso pero sin token JWT real")
                    Log.w("BurgerApiClient", "⚠️ Las operaciones protegidas (eliminar, actualizar) pueden fallar")
                    Result.success(fakeToken)
                } else {
                    Log.e("BurgerApiClient", "❌ Credenciales inválidas")
                    Result.failure(Exception("Usuario o contraseña incorrectos"))
                }
            } else {
                Log.e("BurgerApiClient", "❌ Error al obtener usuarios")
                Result.failure(Exception("Error al verificar credenciales"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Excepción Login: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllProducts(): Result<List<ApiProduct>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo todos los productos de $BASE_URL/products")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/products")
            
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
            Log.d("BurgerApiClient", "=== UPDATE PRODUCT ===")
            Log.d("BurgerApiClient", "ID: $id")
            Log.d("BurgerApiClient", "Product: $product")
            Log.d("BurgerApiClient", "  - name: ${product.name}")
            Log.d("BurgerApiClient", "  - price: ${product.price}")
            Log.d("BurgerApiClient", "  - category: ${product.category}")
            Log.d("BurgerApiClient", "  - available: ${product.available}")
            Log.d("BurgerApiClient", "  - imageUrl: ${product.imageUrl}")
            
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
            Log.d("BurgerApiClient", "=== DELETE PRODUCT ===")
            Log.d("BurgerApiClient", "ID recibido: '$id'")
            Log.d("BurgerApiClient", "ID length: ${id.length}")
            Log.d("BurgerApiClient", "ID isEmpty: ${id.isEmpty()}")
            Log.d("BurgerApiClient", "ID isBlank: ${id.isBlank()}")
            
            if (id.isEmpty() || id.isBlank()) {
                Log.e("BurgerApiClient", "❌ ERROR: ID está vacío!")
                return Result.failure(Exception("ID del producto está vacío"))
            }
            
            val url = "$BASE_URL/products/$id"
            Log.d("BurgerApiClient", "URL completa: $url")
            Log.d("BurgerApiClient", "Token actual: ${authToken ?: "NO HAY TOKEN"}")
            
            val response: HttpResponse = httpClient.delete(url)
            
            Log.d("BurgerApiClient", "Status de respuesta: ${response.status}")
            
            if (response.status.isSuccess()) {
                Log.d("BurgerApiClient", "✅ Producto eliminado exitosamente")
                Result.success(true)
            } else {
                val errorText = response.bodyAsText()
                Log.e("BurgerApiClient", "❌ HTTP Error al eliminar: ${response.status}")
                Log.e("BurgerApiClient", "❌ Detalle del error: $errorText")
                
                val errorMsg = when (response.status.value) {
                    403 -> "No tienes permisos para eliminar productos (403 Forbidden)"
                    401 -> "No estás autenticado correctamente (401 Unauthorized)"
                    404 -> "Producto no encontrado (404 Not Found)"
                    else -> "Error ${response.status.value}: $errorText"
                }
                
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiClient", "❌ Exception al eliminar: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(category: String): Result<List<ApiProduct>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo productos de categoría: $category")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/products/category/$category")
            
            if (response.status.isSuccess()) {
                val products = response.body<List<ApiProduct>>()
                Log.d("BurgerApiClient", "✅ Productos de categoría obtenidos: ${products.size}")
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
    
    suspend fun getCategories(): Result<List<String>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo categorías")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/products/categories")
            
            if (response.status.isSuccess()) {
                val categories = response.body<List<String>>()
                Log.d("BurgerApiClient", "✅ Categorías obtenidas: ${categories.size}")
                Result.success(categories)
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

    // --- USER ENDPOINTS ---

    suspend fun getAllUsers(): Result<List<ApiUser>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo todos los usuarios de $BASE_URL/users")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/users")
            
            if (response.status.isSuccess()) {
                val users = response.body<List<ApiUser>>()
                Log.d("BurgerApiClient", "✅ Usuarios obtenidos: ${users.size}")
                Result.success(users)
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

    suspend fun getUserById(id: String): Result<ApiUser> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo usuario con ID: $id")
            
            val response: HttpResponse = httpClient.get("$BASE_URL/users/$id")
            
            if (response.status.isSuccess()) {
                val user = response.body<ApiUser>()
                Log.d("BurgerApiClient", "✅ Usuario obtenido: ${user.username}")
                Result.success(user)
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

    suspend fun createUser(user: ApiUser): Result<ApiUser> {
        return try {
            Log.d("BurgerApiClient", "Creando usuario: ${user.username}")
            
            val response: HttpResponse = httpClient.post("$BASE_URL/users") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            
            if (response.status.isSuccess()) {
                val createdUser = response.body<ApiUser>()
                Log.d("BurgerApiClient", "✅ Usuario creado: ${createdUser.username}")
                Result.success(createdUser)
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

    suspend fun updateUser(id: String, user: ApiUser): Result<ApiUser> {
        return try {
            Log.d("BurgerApiClient", "Actualizando usuario con ID: $id")
            
            val response: HttpResponse = httpClient.put("$BASE_URL/users/$id") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            
            if (response.status.isSuccess()) {
                val updatedUser = response.body<ApiUser>()
                Log.d("BurgerApiClient", "✅ Usuario actualizado: ${updatedUser.username}")
                Result.success(updatedUser)
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

    suspend fun deleteUser(id: String): Result<Boolean> {
        return try {
            Log.d("BurgerApiClient", "Eliminando usuario con ID: $id")
            
            val response: HttpResponse = httpClient.delete("$BASE_URL/users/$id")
            
            if (response.status.isSuccess()) {
                Log.d("BurgerApiClient", "✅ Usuario eliminado")
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

    // --- ORDER ENDPOINTS ---

    suspend fun getAllOrders(): Result<List<ApiOrder>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo todos los pedidos")
            val response: HttpResponse = httpClient.get("$BASE_URL/orders")
            if (response.status.isSuccess()) {
                val orders = response.body<List<ApiOrder>>()
                Log.d("BurgerApiClient", "✅ Pedidos obtenidos: ${orders.size}")
                Result.success(orders)
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

    suspend fun getOrdersByStatus(status: String): Result<List<ApiOrder>> {
        return try {
            Log.d("BurgerApiClient", "Obteniendo pedidos con estado: $status")
            val response: HttpResponse = httpClient.get("$BASE_URL/orders/status/$status")
            if (response.status.isSuccess()) {
                val orders = response.body<List<ApiOrder>>()
                Log.d("BurgerApiClient", "✅ Pedidos por estado obtenidos: ${orders.size}")
                Result.success(orders)
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

    suspend fun createOrder(order: ApiCreateOrderRequest): Result<ApiOrder> {
        return try {
            Log.d("BurgerApiClient", "Creando pedido para: ${order.userName}")
            val response: HttpResponse = httpClient.post("$BASE_URL/orders") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            if (response.status.isSuccess()) {
                val createdOrder = response.body<ApiOrder>()
                Log.d("BurgerApiClient", "✅ Pedido creado: ${createdOrder.id}")
                Result.success(createdOrder)
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

    suspend fun updateOrderStatus(id: String, status: String): Result<ApiOrder> {
        return try {
            Log.d("BurgerApiClient", "Actualizando estado pedido $id a $status")
            val response: HttpResponse = httpClient.patch("$BASE_URL/orders/$id/status") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("status" to status))
            }
            if (response.status.isSuccess()) {
                val updatedOrder = response.body<ApiOrder>()
                Log.d("BurgerApiClient", "✅ Estado actualizado a: ${updatedOrder.status}")
                Result.success(updatedOrder)
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
