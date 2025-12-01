package com.example.burgermenu.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.*
import com.example.burgermenu.data.repository.OrderRepository
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class OrderMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = OrderRepository()
    private val notificationService = NotificationService(applicationContext)
    
    companion object {
        private const val PREFS_NAME = "order_monitor_prefs"
        private const val KEY_LAST_ORDER_IDS = "last_order_ids"
        private const val KEY_ORDER_STATUSES = "order_statuses"
        
        fun schedulePeriodicCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val periodicWorkRequest = PeriodicWorkRequestBuilder<OrderMonitorWorker>(
                15, TimeUnit.MINUTES // Cada 15 minutos
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES) // Esperar 1 minuto antes de empezar
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "order_monitor",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
            
            Log.d("OrderMonitorWorker", "Monitoreo de pedidos programado")
        }
        
        fun cancelPeriodicCheck(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("order_monitor")
            Log.d("OrderMonitorWorker", "Monitoreo de pedidos cancelado")
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderMonitorWorker", "Verificando nuevos pedidos...")
            
            val result = repository.getAllOrders()
            
            if (result.isSuccess) {
                val currentOrders = result.getOrThrow()
                val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                
                // Obtener IDs de pedidos anteriores
                val lastOrderIds = prefs.getStringSet(KEY_LAST_ORDER_IDS, emptySet()) ?: emptySet()
                val lastOrderStatuses = prefs.getString(KEY_ORDER_STATUSES, "") ?: ""
                
                // Parsear estados anteriores
                val previousStatuses = mutableMapOf<String, String>()
                lastOrderStatuses.split(";").forEach { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        previousStatuses[parts[0]] = parts[1]
                    }
                }
                
                // Detectar nuevos pedidos
                val currentOrderIds = currentOrders.map { it.id }.toSet()
                val newOrderIds = currentOrderIds - lastOrderIds
                
                newOrderIds.forEach { newOrderId ->
                    val order = currentOrders.find { it.id == newOrderId }
                    order?.let {
                        Log.d("OrderMonitorWorker", "¡Nuevo pedido detectado! ID: ${it.id}")
                        notificationService.showNewOrderNotification(
                            it.id,
                            it.user_name,
                            it.total
                        )
                    }
                }
                
                // Detectar cambios de estado
                currentOrders.forEach { order ->
                    val previousStatus = previousStatuses[order.id]
                    if (previousStatus != null && previousStatus != order.status) {
                        Log.d("OrderMonitorWorker", "Estado cambiado: ${order.id} de $previousStatus a ${order.status}")
                        notificationService.showOrderStatusUpdateNotification(
                            order.id,
                            order.status,
                            order.user_name
                        )
                    }
                }
                
                // Guardar estado actual
                val newStatuses = currentOrders.joinToString(";") { "${it.id}:${it.status}" }
                prefs.edit()
                    .putStringSet(KEY_LAST_ORDER_IDS, currentOrderIds)
                    .putString(KEY_ORDER_STATUSES, newStatuses)
                    .apply()
                
                Log.d("OrderMonitorWorker", "Verificación completada. Pedidos actuales: ${currentOrders.size}")
                Result.success()
            } else {
                Log.e("OrderMonitorWorker", "Error al obtener pedidos: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("OrderMonitorWorker", "Error en monitoreo: ${e.message}", e)
            Result.retry()
        }
    }
}
