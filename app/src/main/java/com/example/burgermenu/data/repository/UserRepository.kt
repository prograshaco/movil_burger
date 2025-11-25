package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.User
import com.example.burgermenu.data.network.ApiUser
import com.example.burgermenu.data.network.BurgerApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    
    private fun ApiUser.toUser(): User {
        return User(
            id = id ?: "",
            username = username,
            email = email,
            password = password,
            name = name,
            role = role,
            phone = phone,
            address = address
        )
    }

    suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Obteniendo todos los usuarios de la API")
            val response = BurgerApiClient.getAllUsers()
            
            if (response.isSuccess) {
                val apiUsers = response.getOrThrow()
                val users = apiUsers.map { it.toUser() }
                Log.d("UserRepository", "✅ Usuarios obtenidos: ${users.size}")
                Result.success(users)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener usuarios"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getActiveUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        // Por ahora, la API no tiene endpoint específico para activos, filtramos localmente o usamos getAllUsers
        // Idealmente el backend debería soportar filtrado
        getAllUsers().map { users -> 
            users.filter { it.role != "inactive" }
        }
    }
    
    suspend fun getUserById(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Obteniendo usuario por ID: $userId")
            val response = BurgerApiClient.getUserById(userId)
            
            if (response.isSuccess) {
                val apiUser = response.getOrThrow()
                Result.success(apiUser.toUser())
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createUser(username: String, email: String, name: String, phone: String, address: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Creando usuario: $username")
            
            val apiUser = ApiUser(
                username = username,
                email = email,
                name = name,
                phone = phone,
                address = address,
                password = "password123" // Default password
            )
            
            val response = BurgerApiClient.createUser(apiUser)
            
            if (response.isSuccess) {
                Log.d("UserRepository", "✅ Usuario creado")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: String, username: String, email: String, name: String, phone: String, address: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Actualizando usuario: $userId")
            
            val apiUser = ApiUser(
                id = userId,
                username = username,
                email = email,
                name = name,
                phone = phone,
                address = address
            )
            
            val response = BurgerApiClient.updateUser(userId, apiUser)
            
            if (response.isSuccess) {
                Log.d("UserRepository", "✅ Usuario actualizado")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al actualizar usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Eliminando usuario: $userId")
            val response = BurgerApiClient.deleteUser(userId)
            
            if (response.isSuccess) {
                Log.d("UserRepository", "✅ Usuario eliminado")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al eliminar usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}