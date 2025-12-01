package com.example.burgermenu.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgermenu.data.models.Order
import com.example.burgermenu.data.repository.OrderRepository
import com.example.burgermenu.services.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val selectedStatus: String = "Todos",
    val availableStatuses: List<String> = listOf("Todos", "pending", "confirmed", "preparing", "ready", "delivered", "cancelled"),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OrderViewModel(
    private val repository: OrderRepository = OrderRepository(),
    private val context: Context? = null
) : ViewModel() {
    
    private val notificationService: NotificationService? = context?.let { NotificationService(it) }
    
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    private var previousOrderIds = setOf<String>()
    private var previousOrderStatuses = mapOf<String, String>()
    private var monitoringJob: kotlinx.coroutines.Job? = null
    
    init {
        loadOrders()
    }
    
    fun startMonitoring() {
        if (monitoringJob?.isActive == true) return
        
        monitoringJob = viewModelScope.launch {
            android.util.Log.d("OrderViewModel", "Iniciando monitoreo de pedidos...")
            while (true) {
                kotlinx.coroutines.delay(5000) // Revisar cada 5 segundos
                checkForNewOrdersAndUpdates()
            }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        android.util.Log.d("OrderViewModel", "Monitoreo de pedidos detenido")
    }
    
    private suspend fun checkForNewOrdersAndUpdates() {
        try {
            val result = repository.getAllOrders()
            
            if (result.isSuccess) {
                val currentOrders = result.getOrThrow()
                val currentOrderIds = currentOrders.map { it.id }.toSet()
                
                // Detectar nuevos pedidos
                if (previousOrderIds.isNotEmpty()) {
                    val newOrderIds = currentOrderIds - previousOrderIds
                    
                    if (newOrderIds.isNotEmpty()) {
                        android.util.Log.d("OrderViewModel", "¡${newOrderIds.size} nuevo(s) pedido(s) detectado(s)!")
                    }
                    
                    newOrderIds.forEach { newOrderId ->
                        val order = currentOrders.find { it.id == newOrderId }
                        order?.let {
                            android.util.Log.d("OrderViewModel", "Nuevo pedido: ${it.id} - ${it.user_name}")
                            notificationService?.showNewOrderNotification(
                                it.id,
                                it.user_name,
                                it.total
                            )
                        }
                    }
                    
                    // Detectar cambios de estado
                    currentOrders.forEach { order ->
                        val previousStatus = previousOrderStatuses[order.id]
                        if (previousStatus != null && previousStatus != order.status) {
                            android.util.Log.d("OrderViewModel", "Estado cambiado: ${order.id} de $previousStatus a ${order.status}")
                            notificationService?.showOrderStatusUpdateNotification(
                                order.id,
                                order.status,
                                order.user_name
                            )
                        }
                    }
                }
                
                // Actualizar estado anterior
                previousOrderIds = currentOrderIds
                previousOrderStatuses = currentOrders.associate { it.id to it.status }
                
                // Actualizar UI si hay cambios
                if (_uiState.value.orders != currentOrders) {
                    _uiState.value = _uiState.value.copy(orders = currentOrders)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OrderViewModel", "Error en monitoreo: ${e.message}")
        }
    }
    
    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = if (_uiState.value.selectedStatus == "Todos") {
                repository.getAllOrders()
            } else {
                repository.getOrdersByStatus(_uiState.value.selectedStatus)
            }
            
            result
                .onSuccess { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
        }
    }
    
    fun filterByStatus(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadOrders()
    }
    
    fun createOrder(
        userId: String,
        userName: String,
        userEmail: String,
        userPhone: String,
        userAddress: String,
        items: String,
        total: Double,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.createOrder(userId, userName, userEmail, userPhone, userAddress, items, total)
                .onSuccess { orderId ->
                    // Mostrar notificación de nuevo pedido
                    notificationService?.showNewOrderNotification(orderId, userName, total)
                    
                    onSuccess(orderId)
                    loadOrders() // Recargar la lista
                }
                .onFailure { exception ->
                    onError(exception.message ?: "Error al crear pedido")
                }
        }
    }
    
    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            // Obtener el pedido actual para la notificación
            val currentOrder = _uiState.value.orders.find { it.id == orderId }
            
            repository.updateOrderStatus(orderId, newStatus)
                .onSuccess {
                    // Mostrar notificación de cambio de estado
                    currentOrder?.let { order ->
                        notificationService?.showOrderStatusUpdateNotification(
                            orderId,
                            newStatus,
                            order.user_name
                        )
                    }
                    
                    loadOrders() // Recargar la lista para mostrar el cambio
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Error al actualizar estado: ${exception.message}"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retry() {
        loadOrders()
    }
    
    fun refreshOrders() {
        loadOrders()
    }
    
    // Función helper para obtener el nombre en español del estado
    fun getStatusDisplayName(status: String): String {
        return when (status) {
            "pending" -> "Pendiente"
            "confirmed" -> "Confirmado"
            "preparing" -> "Preparando"
            "ready" -> "Listo"
            "delivered" -> "Entregado"
            "cancelled" -> "Cancelado"
            else -> status
        }
    }
    
    // Función para obtener el siguiente estado lógico
    fun getNextStatus(currentStatus: String): String? {
        return when (currentStatus) {
            "pending" -> "confirmed"
            "confirmed" -> "preparing"
            "preparing" -> "ready"
            "ready" -> "delivered"
            else -> null // No hay siguiente estado para delivered o cancelled
        }
    }
    
    // Función para obtener todos los estados posibles para un pedido
    fun getAvailableStatusesForOrder(currentStatus: String): List<String> {
        return when (currentStatus) {
            "pending" -> listOf("confirmed", "cancelled")
            "confirmed" -> listOf("preparing", "cancelled")
            "preparing" -> listOf("ready", "cancelled")
            "ready" -> listOf("delivered")
            else -> emptyList()
        }
    }
}

// Factory para crear OrderViewModel con contexto
class OrderViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
