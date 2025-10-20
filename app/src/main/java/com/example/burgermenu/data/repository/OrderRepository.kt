package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.Order
import com.example.burgermenu.data.network.TursoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray

class OrderRepository {
    
    suspend fun getAllOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Intentando obtener pedidos de Turso...")
            
            val response = TursoClient.executeQuery(
                "SELECT id, user_id, user_name, user_email, user_phone, user_address, items, total FROM orders ORDER BY id DESC"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("OrderRepository", "Respuesta de Turso: ${tursoResponse.results.size} resultados")
                
                // Extraer datos del formato v2
                val (columns: List<String>, rows: List<JsonArray>) = TursoClient.extractDataFromV2Response(tursoResponse)
                Log.d("OrderRepository", "Columnas extraídas: $columns")
                Log.d("OrderRepository", "Pedidos encontrados: ${rows.size}")
                
                if (rows.isNotEmpty()) {
                    val orders = TursoClient.rowsToMaps(columns, rows).map { row ->
                        Log.d("OrderRepository", "Procesando pedido: $row")
                        Order(
                            id = row["id"] ?: "",
                            user_id = row["user_id"] ?: "",
                            user_name = row["user_name"] ?: "",
                            user_email = row["user_email"] ?: "",
                            user_phone = row["user_phone"] ?: "",
                            user_address = row["user_address"] ?: "",
                            items = row["items"] ?: "[]",
                            total = row["total"]?.toDoubleOrNull() ?: 0.0,
                            status = "pending" // Valor por defecto ya que no existe en la tabla
                        )
                    }
                    Log.d("OrderRepository", "Pedidos procesados exitosamente: ${orders.size}")
                    Result.success(orders)
                } else {
                    Log.w("OrderRepository", "No hay pedidos en la BD")
                    Result.success(emptyList())
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("OrderRepository", "Error en Turso: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido de Turso"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Excepción: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getOrdersByStatus(status: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Obteniendo pedidos por estado: $status")
            
            val response = TursoClient.executeQuery(
                "SELECT * FROM orders WHERE status = '$status' ORDER BY id DESC"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns: List<String>, rows: List<JsonArray>) = TursoClient.extractDataFromV2Response(tursoResponse)
                
                if (rows.isNotEmpty()) {
                    val orders = TursoClient.rowsToMaps(columns, rows).map { row ->
                        Order(
                            id = row["id"] ?: "",
                            user_id = row["user_id"] ?: "",
                            user_name = row["user_name"] ?: "",
                            user_email = row["user_email"] ?: "",
                            user_phone = row["user_phone"] ?: "",
                            user_address = row["user_address"] ?: "",
                            items = row["items"] ?: "[]",
                            total = row["total"]?.toDoubleOrNull() ?: 0.0,
                            status = row["status"] ?: "pending"
                        )
                    }
                    Result.success(orders)
                } else {
                    Log.w("OrderRepository", "No hay pedidos con estado $status en la BD")
                    Result.success(emptyList())
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("OrderRepository", "Error al obtener pedidos por estado: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido de Turso"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Excepción al obtener pedidos por estado: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(
        userId: String,
        userName: String,
        userEmail: String,
        userPhone: String,
        userAddress: String,
        items: String,
        total: Double
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Creando nuevo pedido para usuario: $userName")
            
            val orderId = "order_${System.currentTimeMillis()}"
            val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            
            // Escapar comillas simples
            val safeName = userName.replace("'", "''")
            val safeEmail = userEmail.replace("'", "''")
            val safePhone = userPhone.replace("'", "''")
            val safeAddress = userAddress.replace("'", "''")
            val safeItems = items.replace("'", "''")
            
            val sql = """
                INSERT INTO orders (id, user_id, user_name, user_email, user_phone, user_address, items, total, status, created_at, updated_at) 
                VALUES ('$orderId', '$userId', '$safeName', '$safeEmail', '$safePhone', '$safeAddress', '$safeItems', $total, 'pending', '$currentTime', '$currentTime')
            """.trimIndent()
            
            Log.d("OrderRepository", "SQL Query: $sql")
            
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                Log.d("OrderRepository", "Pedido creado exitosamente: $orderId")
                Result.success(orderId)
            } else {
                val error = response.exceptionOrNull()
                Log.e("OrderRepository", "Error al crear pedido: ${error?.message}")
                Result.failure(error ?: Exception("Error al crear pedido"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Excepción creando pedido: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Actualizando estado del pedido $orderId a $newStatus")
            
            val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            
            val sql = """
                UPDATE orders 
                SET status = '$newStatus', updated_at = '$currentTime' 
                WHERE id = '$orderId'
            """.trimIndent()
            
            Log.d("OrderRepository", "SQL Query: $sql")
            
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                Log.d("OrderRepository", "Estado del pedido actualizado exitosamente")
                Result.success(true)
            } else {
                val error = response.exceptionOrNull()
                Log.e("OrderRepository", "Error al actualizar estado: ${error?.message}")
                Result.failure(error ?: Exception("Error al actualizar estado"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Excepción actualizando estado: ${e.message}", e)
            Result.failure(e)
        }
    }
}