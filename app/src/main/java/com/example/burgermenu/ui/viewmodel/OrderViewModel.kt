package com.example.burgermenu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgermenu.data.models.Order
import com.example.burgermenu.data.repository.OrderRepository
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
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()
    
    init {
        loadOrders()
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
            repository.updateOrderStatus(orderId, newStatus)
                .onSuccess {
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