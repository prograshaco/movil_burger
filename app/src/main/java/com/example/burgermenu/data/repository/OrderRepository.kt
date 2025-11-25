package com.example.burgermenu.data.repository

import com.example.burgermenu.data.models.Order
import com.example.burgermenu.data.network.ApiCreateOrderRequest
import com.example.burgermenu.data.network.BurgerApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository {

    suspend fun getAllOrders(): Result<List<Order>> = withContext(Dispatchers.IO) {
        val result = BurgerApiClient.getAllOrders()
        
        result.map { apiOrders ->
            apiOrders.map { apiOrder ->
                Order(
                    id = apiOrder.id ?: "",
                    user_id = apiOrder.userId,
                    user_name = apiOrder.userName,
                    user_email = apiOrder.userEmail,
                    user_phone = apiOrder.userPhone,
                    user_address = apiOrder.userAddress,
                    items = apiOrder.items,
                    total = apiOrder.total,
                    status = apiOrder.status
                )
            }
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
        val request = ApiCreateOrderRequest(
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            userAddress = userAddress,
            items = items,
            total = total
        )
        
        val result = BurgerApiClient.createOrder(request)
        result.map { it.id ?: "" }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val result = BurgerApiClient.updateOrderStatus(orderId, newStatus)
        result.map { true }
    }
    
    suspend fun getOrdersByStatus(status: String): Result<List<Order>> = withContext(Dispatchers.IO) {
        val result = BurgerApiClient.getOrdersByStatus(status)
        
        result.map { apiOrders ->
            apiOrders.map { apiOrder ->
                Order(
                    id = apiOrder.id ?: "",
                    user_id = apiOrder.userId,
                    user_name = apiOrder.userName,
                    user_email = apiOrder.userEmail,
                    user_phone = apiOrder.userPhone,
                    user_address = apiOrder.userAddress,
                    items = apiOrder.items,
                    total = apiOrder.total,
                    status = apiOrder.status
                )
            }
        }
    }
}