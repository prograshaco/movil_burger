package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.Order
import com.example.burgermenu.data.network.TursoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class OrderRepository {

    suspend fun getAllOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Obteniendo todos los pedidos desde Turso")
            
            val sql = "SELECT * FROM orders ORDER BY created_at DESC"
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns, rows) = TursoClient.extractDataFromV2Response(tursoResponse)
                
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
                
                Log.d("OrderRepository", "✅ Pedidos obtenidos: ${orders.size}")
                Result.success(orders)
            } else {
                Log.e("OrderRepository", "❌ Error al obtener pedidos")
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener pedidos"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Exception: ${e.message}", e)
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
            val orderId = "order_${UUID.randomUUID()}"
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Escapar comillas simples en los strings
            val safeName = userName.replace("'", "''")
            val safeEmail = userEmail.replace("'", "''")
            val safePhone = userPhone.replace("'", "''")
            val safeAddress = userAddress.replace("'", "''")
            val safeItems = items.replace("'", "''")
            
            val sql = """
                INSERT INTO orders (id, user_id, user_name, user_email, user_phone, user_address, items, total, status, created_at, updated_at) 
                VALUES ('$orderId', '$userId', '$safeName', '$safeEmail', '$safePhone', '$safeAddress', '$safeItems', $total, 'pending', '$currentTime', '$currentTime')
            """.trimIndent()
            
            Log.d("OrderRepository", "Creando pedido: $orderId")
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                Log.d("OrderRepository", "✅ Pedido creado exitosamente")
                Result.success(orderId)
            } else {
                Log.e("OrderRepository", "❌ Error al crear pedido")
                Result.failure(response.exceptionOrNull() ?: Exception("Error al crear pedido"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            val sql = """
                UPDATE orders 
                SET status = '$newStatus', updated_at = '$currentTime' 
                WHERE id = '$orderId'
            """.trimIndent()
            
            Log.d("OrderRepository", "Actualizando estado del pedido $orderId a $newStatus")
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                Log.d("OrderRepository", "✅ Estado actualizado exitosamente")
                Result.success(true)
            } else {
                Log.e("OrderRepository", "❌ Error al actualizar estado")
                Result.failure(response.exceptionOrNull() ?: Exception("Error al actualizar estado"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getOrdersByStatus(status: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Obteniendo pedidos con estado: $status")
            
            val sql = "SELECT * FROM orders WHERE status = '$status' ORDER BY created_at DESC"
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns, rows) = TursoClient.extractDataFromV2Response(tursoResponse)
                
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
                
                Log.d("OrderRepository", "✅ Pedidos filtrados obtenidos: ${orders.size}")
                Result.success(orders)
            } else {
                Log.e("OrderRepository", "❌ Error al obtener pedidos por estado")
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener pedidos"))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}