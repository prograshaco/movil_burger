package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.network.TursoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProductRepository {
    
    // Función para probar conectividad básica
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "=== PROBANDO CONECTIVIDAD ===")
            
            // Probar consulta más básica posible
            val response = TursoClient.executeQuery("SELECT 1 as test")
            
            Log.d("ProductRepository", "Response success: ${response.isSuccess}")
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("ProductRepository", "TursoResponse: $tursoResponse")
                
                if (tursoResponse.results.isNotEmpty()) {
                    Log.d("ProductRepository", "✅ Conexión básica funciona")
                    
                    // Ahora probar listar tablas
                    val tablesResponse = TursoClient.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")
                    if (tablesResponse.isSuccess) {
                        val tablesResult = tablesResponse.getOrThrow()
                        Log.d("ProductRepository", "Tablas response: $tablesResult")
                        
                        if (tablesResult.results.isNotEmpty()) {
                            val result = tablesResult.results.first()
                            val tables = TursoClient.rowsToMaps(result.columns, result.rows)
                            Log.d("ProductRepository", "Tablas encontradas: $tables")
                            
                            // Probar contar productos
                            val countResponse = TursoClient.executeQuery("SELECT COUNT(*) as count FROM products")
                            if (countResponse.isSuccess) {
                                val countResult = countResponse.getOrThrow()
                                Log.d("ProductRepository", "Count response: $countResult")
                            }
                        }
                    }
                    
                    Result.success("Conexión exitosa")
                } else {
                    Log.e("ProductRepository", "❌ Conexión falla - results vacío")
                    Result.failure(Exception("Results vacío"))
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("ProductRepository", "❌ Error en response: ${error?.message}")
                Result.failure(error ?: Exception("Error de conexión"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "❌ Excepción: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Función de diagnóstico para ver qué tablas existen
    suspend fun diagnoseTables(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "=== DIAGNÓSTICO DE BASE DE DATOS ===")
            
            // 1. Ver qué tablas existen
            val tablesResponse = TursoClient.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table'"
            )
            
            val diagnosticInfo = StringBuilder()
            
            if (tablesResponse.isSuccess) {
                val tursoResponse = tablesResponse.getOrThrow()
                if (tursoResponse.results.isNotEmpty()) {
                    val result = tursoResponse.results.first()
                    val tables = TursoClient.rowsToMaps(result.columns, result.rows)
                    
                    diagnosticInfo.append("TABLAS ENCONTRADAS:\n")
                    tables.forEach { table ->
                        val tableName = table["name"]
                        diagnosticInfo.append("- $tableName\n")
                        Log.d("ProductRepository", "Tabla encontrada: $tableName")
                    }
                    
                    // 2. Para cada tabla, ver su estructura y algunos datos
                    tables.forEach { table ->
                        val tableName = table["name"] ?: ""
                        if (tableName.isNotEmpty() && !tableName.startsWith("sqlite_")) {
                            
                            // Ver estructura de la tabla
                            val schemaResponse = TursoClient.executeQuery("PRAGMA table_info($tableName)")
                            if (schemaResponse.isSuccess) {
                                val schemaResult = schemaResponse.getOrThrow()
                                if (schemaResult.results.isNotEmpty()) {
                                    val schema = schemaResult.results.first()
                                    val columns = TursoClient.rowsToMaps(schema.columns, schema.rows)
                                    
                                    diagnosticInfo.append("\nESTRUCTURA DE $tableName:\n")
                                    columns.forEach { col ->
                                        diagnosticInfo.append("  ${col["name"]} (${col["type"]})\n")
                                    }
                                }
                            }
                            
                            // Ver algunos datos de muestra
                            val dataResponse = TursoClient.executeQuery("SELECT * FROM $tableName LIMIT 3")
                            if (dataResponse.isSuccess) {
                                val dataResult = dataResponse.getOrThrow()
                                if (dataResult.results.isNotEmpty()) {
                                    val data = dataResult.results.first()
                                    val rows = TursoClient.rowsToMaps(data.columns, data.rows)
                                    
                                    diagnosticInfo.append("\nDATOS DE MUESTRA DE $tableName (${rows.size} filas):\n")
                                    rows.forEach { row ->
                                        diagnosticInfo.append("  $row\n")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            val finalDiagnostic = diagnosticInfo.toString()
            Log.d("ProductRepository", "DIAGNÓSTICO COMPLETO:\n$finalDiagnostic")
            Result.success(finalDiagnostic)
            
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error en diagnóstico: ${e.message}", e)
            Result.failure(e)
        }
    }

    
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Conectando a Turso para obtener productos...")
            
            // Consulta simple para obtener solo un producto
            Log.d("ProductRepository", "=== OBTENIENDO PRODUCTOS ===")
            val response = TursoClient.executeQuery(
                "SELECT * FROM products ORDER BY created_at DESC"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("ProductRepository", "Turso conectado. Resultados: ${tursoResponse.results.size}")
                Log.d("ProductRepository", "Respuesta completa: $tursoResponse")
                
                // Extraer datos del formato v2
                val (columns: List<String>, rows: List<kotlinx.serialization.json.JsonArray>) = TursoClient.extractDataFromV2Response(tursoResponse)
                Log.d("ProductRepository", "Columnas extraídas: $columns")
                Log.d("ProductRepository", "Filas extraídas: ${rows.size}")
                
                if (rows.isNotEmpty()) {
                    val products = TursoClient.rowsToMaps(columns, rows).map { row ->
                            Log.d("ProductRepository", "Procesando producto: $row")
                            val priceValue = row["price"]?.toDoubleOrNull() ?: 0.0
                            val priceInCents = (priceValue * 100).toInt()
                            
                            Product(
                                id = row["id"] ?: "",
                                name = row["name"] ?: "",
                                description = row["description"] ?: "",
                                price = priceInCents,
                                category = row["category"] ?: "",
                                image_url = row["image_url"] ?: "",
                                available = row["available"]?.toIntOrNull() ?: 1,
                                created_at = row["created_at"] ?: ""
                            )
                        }
                    Log.d("ProductRepository", "Productos procesados exitosamente: ${products.size}")
                    Result.success(products)
                } else {
                    Log.w("ProductRepository", "No hay productos disponibles en la BD")
                    Result.success(emptyList())
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("ProductRepository", "Error conectando a Turso: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido de Turso"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Excepción al conectar a Turso: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(category: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo productos por categoría: $category")
            
            val response = TursoClient.executeQuery(
                "SELECT * FROM products WHERE category = '$category' AND available = 1 ORDER BY name"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                if (tursoResponse.results.isNotEmpty()) {
                    val result = tursoResponse.results.first()
                    val products = TursoClient.rowsToMaps(result.columns, result.rows).map { row ->
                        Product(
                            id = row["id"] ?: "",
                            name = row["name"] ?: "",
                            description = row["description"] ?: "",
                            price = (row["price"]?.toDoubleOrNull()?.times(100))?.toInt() ?: 0,
                            category = row["category"] ?: "",
                            image_url = row["image_url"] ?: "",
                            available = row["available"]?.toIntOrNull() ?: 1,
                            created_at = row["created_at"] ?: ""
                        )
                    }
                    Result.success(products)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener productos por categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCategories(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo categorías de productos")
            
            val response = TursoClient.executeQuery(
                "SELECT DISTINCT category FROM products WHERE available = 1 ORDER BY category"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                if (tursoResponse.results.isNotEmpty()) {
                    val result = tursoResponse.results.first()
                    val categories = TursoClient.rowsToMaps(result.columns, result.rows).mapNotNull { row ->
                        row["category"]
                    }
                    Result.success(categories)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener categorías"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(productId: String): Result<Product?> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo producto por ID: $productId")
            
            val response = TursoClient.executeQuery(
                "SELECT * FROM products WHERE id = '$productId'"
            )
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                val (columns, rows) = TursoClient.extractDataFromV2Response(tursoResponse)
                
                if (rows.isNotEmpty()) {
                    val row = TursoClient.rowsToMaps(columns, rows).first()
                    val product = Product(
                        id = row["id"] ?: "",
                        name = row["name"] ?: "",
                        description = row["description"] ?: "",
                        price = ((row["price"]?.toDoubleOrNull() ?: 0.0) * 100).toInt(),
                        category = row["category"] ?: "",
                        image_url = row["image_url"] ?: "",
                        available = row["available"]?.toIntOrNull() ?: 1,
                        created_at = row["created_at"] ?: ""
                    )
                    Result.success(product)
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo producto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(name: String, description: String, price: Double, category: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Creando producto: $name con precio: $price")
            
            val productId = "prod_${java.util.UUID.randomUUID()}"
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Escapar comillas simples y caracteres especiales
            val safeName = name.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeDescription = description.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeCategory = category.replace("'", "''").replace("\n", " ").replace("\r", " ")
            
            val sql = """INSERT INTO products (id, name, description, price, category, image_url, available, created_at, updated_at) 
                        VALUES ('$productId', '$safeName', '$safeDescription', $price, '$safeCategory', '', 1, '$currentTime', '$currentTime')"""
            
            Log.d("ProductRepository", "SQL Query: $sql")
            
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("ProductRepository", "Respuesta de creación: $tursoResponse")
                
                // Verificar si la respuesta indica éxito
                if (tursoResponse.results.isNotEmpty()) {
                    Log.d("ProductRepository", "Producto creado exitosamente")
                    Result.success(true)
                } else {
                    Log.w("ProductRepository", "Respuesta vacía al crear producto")
                    Result.success(true) // Asumir éxito si no hay error
                }
            } else {
                val error = response.exceptionOrNull()
                Log.e("ProductRepository", "Error al crear producto: ${error?.message}")
                Result.failure(error ?: Exception("Error al crear producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Excepción creando producto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(productId: String, name: String, description: String, price: Double, category: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Actualizando producto: $productId con precio: $price")
            
            // Primero, intentar una actualización simple solo del precio para probar
            val simplePriceUpdate = "UPDATE products SET price = $price WHERE id = '$productId'"
            Log.d("ProductRepository", "Probando actualización simple de precio: $simplePriceUpdate")
            
            val testResponse = TursoClient.executeQuery(simplePriceUpdate)
            if (testResponse.isSuccess) {
                Log.d("ProductRepository", "Actualización de precio exitosa")
            } else {
                Log.e("ProductRepository", "Error en actualización de precio: ${testResponse.exceptionOrNull()?.message}")
            }
            
            // Actualización completa en una sola consulta
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Escapar caracteres especiales
            val safeName = name.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeDescription = description.replace("'", "''").replace("\n", " ").replace("\r", " ")
            val safeCategory = category.replace("'", "''").replace("\n", " ").replace("\r", " ")
            
            val sql = """UPDATE products 
                        SET name = '$safeName', description = '$safeDescription', price = $price, category = '$safeCategory', updated_at = '$currentTime'
                        WHERE id = '$productId'"""
            
            Log.d("ProductRepository", "SQL Update: $sql")
            val response = TursoClient.executeQuery(sql)
            
            if (response.isSuccess) {
                val tursoResponse = response.getOrThrow()
                Log.d("ProductRepository", "Respuesta de actualización: $tursoResponse")
                
                // Verificar el producto actualizado obteniendo los datos nuevos
                val verifyResponse = TursoClient.executeQuery("SELECT * FROM products WHERE id = '$productId'")
                if (verifyResponse.isSuccess) {
                    val verifyResult = verifyResponse.getOrThrow()
                    val (verifyCols, verifyRows) = TursoClient.extractDataFromV2Response(verifyResult)
                    if (verifyRows.isNotEmpty()) {
                        val updatedProduct = TursoClient.rowsToMaps(verifyCols, verifyRows).first()
                        Log.d("ProductRepository", "Producto después de actualizar: $updatedProduct")
                        Log.d("ProductRepository", "Precio en BD después de actualizar: ${updatedProduct["price"]}")
                    }
                }
                
                Log.d("ProductRepository", "Producto actualizado exitosamente")
                Result.success(true)
            } else {
                val error = response.exceptionOrNull()
                Log.e("ProductRepository", "Error al actualizar producto: ${error?.message}")
                Result.failure(error ?: Exception("Error al actualizar producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Excepción actualizando producto: ${e.message}", e)
            Result.failure(e)
        }
    }
}