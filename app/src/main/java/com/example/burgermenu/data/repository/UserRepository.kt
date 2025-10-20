package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.User
import com.example.burgermenu.data.network.TursoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray

class UserRepository {
    
    suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Conectando a Turso para obtener usuarios...")
            
            val response = TursoClient.executeQuery(
                "SELECT id, username, email, name, role, phone, address FROM users ORDER BY name"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("UserRepository", "Turso conectado. Resultados: ${tursoResponse.results.size}")
                Log.d("UserRepository", "Respuesta completa: $tursoResponse")
                
                // Extraer datos del formato v2
                val (columns: List<String>, rows: List<JsonArray>) = TursoClient.extractDataFromV2Response(tursoResponse)
                Log.d("UserRepository", "Columnas extraídas: $columns")
                Log.d("UserRepository", "Filas extraídas: ${rows.size}")
                
                if (rows.isNotEmpty()) {
                    val users = TursoClient.rowsToMaps(columns, rows).map { row ->
                        Log.d("UserRepository", "Procesando usuario: $row")
                        User(
                            id = row["id"] ?: "",
                            username = row["username"] ?: "",
                            email = row["email"] ?: "",
                            password = "", // No devolvemos la contraseña por seguridad
                            name = row["name"] ?: "",
                            role = row["role"] ?: "customer",
                            phone = row["phone"] ?: "",
                            address = row["address"] ?: ""
                        )
                    }
                    Log.d("UserRepository", "Usuarios procesados exitosamente: ${users.size}")
                    Result.success(users)
                } else {
                    Log.w("UserRepository", "No hay usuarios en la BD")
                    Result.success(emptyList())
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("UserRepository", "Error conectando a Turso: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido de Turso"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Excepción al conectar a Turso: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getActiveUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Obteniendo usuarios activos...")
            
            val response = TursoClient.executeQuery(
                "SELECT id, username, email, name, role, phone, address FROM users WHERE role != 'inactive' ORDER BY name"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns: List<String>, rows: List<JsonArray>) = TursoClient.extractDataFromV2Response(tursoResponse)
                
                if (rows.isNotEmpty()) {
                    val users = TursoClient.rowsToMaps(columns, rows).map { row ->
                        User(
                            id = row["id"] ?: "",
                            username = row["username"] ?: "",
                            email = row["email"] ?: "",
                            password = "", // No devolvemos la contraseña por seguridad
                            name = row["name"] ?: "",
                            role = row["role"] ?: "customer",
                            phone = row["phone"] ?: "",
                            address = row["address"] ?: ""
                        )
                    }
                    Result.success(users)
                } else {
                    Log.w("UserRepository", "No hay usuarios activos en la BD")
                    Result.success(emptyList())
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("UserRepository", "Error al obtener usuarios activos: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido de Turso"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Excepción al obtener usuarios activos: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Obteniendo usuario por ID: $userId")
            
            val response = TursoClient.executeQuery(
                "SELECT id, username, email, name, role, phone, address FROM users WHERE id = '$userId'"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns, rows) = TursoClient.extractDataFromV2Response(tursoResponse)
                
                if (rows.isNotEmpty()) {
                    val row = TursoClient.rowsToMaps(columns, rows).first()
                    val user = User(
                        id = row["id"] ?: "",
                        username = row["username"] ?: "",
                        email = row["email"] ?: "",
                        password = "",
                        name = row["name"] ?: "",
                        role = row["role"] ?: "customer",
                        phone = row["phone"] ?: "",
                        address = row["address"] ?: ""
                    )
                    Result.success(user)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error obteniendo usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createUser(username: String, email: String, name: String, phone: String, address: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Creando usuario: $username")
            
            val userId = "user_${java.util.UUID.randomUUID()}"
            val defaultPassword = "password123" // En producción, esto debería ser hasheado
            
            val response = TursoClient.executeQuery(
                """INSERT INTO users (id, username, email, password, name, role, phone, address) 
                   VALUES ('$userId', '$username', '$email', '$defaultPassword', '$name', 'customer', '$phone', '$address')"""
            )
            
            if (response.isSuccess) {
                Log.d("UserRepository", "Usuario creado exitosamente")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creando usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(userId: String, username: String, email: String, name: String, phone: String, address: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Actualizando usuario: $userId")
            
            val response = TursoClient.executeQuery(
                """UPDATE users 
                   SET username = '$username', email = '$email', name = '$name', phone = '$phone', address = '$address'
                   WHERE id = '$userId'"""
            )
            
            if (response.isSuccess) {
                Log.d("UserRepository", "Usuario actualizado exitosamente")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al actualizar usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error actualizando usuario: ${e.message}", e)
            Result.failure(e)
        }
    }
}