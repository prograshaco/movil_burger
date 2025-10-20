package com.example.burgermenu.data.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "", // ID es string en Turso
    val username: String,
    val email: String,
    val password: String = "",
    val name: String,
    val role: String = "customer", // customer, admin, staff
    val phone: String = "",
    val address: String = ""
) {
    val isActive: Boolean get() = role != "inactive"
}

@Serializable
data class Product(
    val id: String = "", // ID es string en Turso
    val name: String,
    val description: String = "",
    val price: Int, // Precio en centavos para evitar problemas de punto flotante
    val category: String,
    val image_url: String = "",
    val available: Int = 1, // SQLite usa 0/1 para boolean
    val created_at: String = ""
) {
    val isAvailable: Boolean get() = available == 1
    val imageUrl: String get() = image_url
    val createdAt: String get() = created_at
}

@Serializable
data class Order(
    val id: String = "", // ID es string en Turso
    val user_id: String = "", // user_id también es string
    val user_name: String,
    val user_email: String = "",
    val user_phone: String = "",
    val user_address: String = "",
    val items: String = "[]", // JSON string en la base de datos
    val total: Double = 0.0, // Total como double (viene como float de Turso)
    val status: String = "pending"
) {
    val userId: String get() = user_id
    val userName: String get() = user_name
    val userEmail: String get() = user_email
    val userPhone: String get() = user_phone
    val userAddress: String get() = user_address
    val totalInCents: Int get() = (total * 100).toInt() // Convertir a centavos para mostrar
}

@Serializable
data class OrderItem(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val price: Int // Precio en centavos
)

@Serializable
data class Review(
    val id: Int = 0,
    val user_id: Int,
    val user_name: String,
    val rating: Int, // 1-5
    val comment: String,
    val approved: Int = 0, // SQLite usa 0/1 para boolean
    val created_at: String = "",
    val user: String = ""
) {
    val userId: Int get() = user_id
    val userName: String get() = user_name
    val isApproved: Boolean get() = approved == 1
    val createdAt: String get() = created_at
}

@Serializable
data class ActivityLog(
    val id: Int = 0,
    val action: String,
    val description: String,
    val userId: Int? = null,
    val timestamp: String,
    val user: String = ""
)

// DTOs para requests
@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val name: String,
    val phone: String = "",
    val address: String = ""
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageUrl: String = ""
)

@Serializable
data class CreateOrderRequest(
    val userId: Int,
    val items: List<OrderItemRequest>,
    val userEmail: String = "",
    val userPhone: String = "",
    val userAddress: String = ""
)

@Serializable
data class OrderItemRequest(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class CreateReviewRequest(
    val userId: Int,
    val rating: Int,
    val comment: String
)

// Response wrappers
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = ""
)

@Serializable
data class PaginatedResponse<T>(
    val success: Boolean,
    val data: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val message: String = ""
)