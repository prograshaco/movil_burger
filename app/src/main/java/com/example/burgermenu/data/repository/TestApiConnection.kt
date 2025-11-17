package com.example.burgermenu.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Clase de utilidad para probar la conexión con la API de productos
 * Úsala desde tu Activity o ViewModel para verificar que todo funciona
 */
object TestApiConnection {
    
    private const val TAG = "TestApiConnection"
    
    /**
     * Prueba completa de todas las operaciones CRUD
     */
    fun runFullTest() {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = BurgerApiProductRepository()
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "INICIANDO PRUEBA DE CONEXIÓN CON LA API")
            Log.d(TAG, "========================================")
            
            // 1. Obtener todos los productos
            Log.d(TAG, "\n1. Obteniendo todos los productos...")
            repository.getAllProducts()
                .onSuccess { products ->
                    Log.d(TAG, "✅ Productos obtenidos: ${products.size}")
                    products.take(3).forEach { product ->
                        Log.d(TAG, "   - ${product.name} ($${product.price / 100.0})")
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error obteniendo productos: ${error.message}")
                }
            
            // 2. Obtener categorías
            Log.d(TAG, "\n2. Obteniendo categorías...")
            repository.getCategories()
                .onSuccess { categories ->
                    Log.d(TAG, "✅ Categorías obtenidas: ${categories.joinToString(", ")}")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error obteniendo categorías: ${error.message}")
                }
            
            // 3. Crear un producto de prueba
            Log.d(TAG, "\n3. Creando producto de prueba...")
            repository.createProduct(
                name = "Hamburguesa de Prueba",
                description = "Producto creado desde la app Android",
                price = 12.99,
                category = "Hamburguesas",
                imageUrl = ""
            )
                .onSuccess { product ->
                    Log.d(TAG, "✅ Producto creado: ${product.name} (ID: ${product.id})")
                    
                    // 4. Actualizar el producto creado
                    Log.d(TAG, "\n4. Actualizando producto...")
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.updateProduct(
                            productId = product.id,
                            name = "Hamburguesa de Prueba ACTUALIZADA",
                            description = "Descripción actualizada",
                            price = 15.99,
                            category = "Hamburguesas",
                            imageUrl = ""
                        )
                            .onSuccess { updated ->
                                Log.d(TAG, "✅ Producto actualizado: ${updated.name} ($${updated.price / 100.0})")
                                
                                // 5. Eliminar el producto
                                Log.d(TAG, "\n5. Eliminando producto de prueba...")
                                CoroutineScope(Dispatchers.IO).launch {
                                    repository.deleteProduct(product.id)
                                        .onSuccess {
                                            Log.d(TAG, "✅ Producto eliminado correctamente")
                                            Log.d(TAG, "\n========================================")
                                            Log.d(TAG, "PRUEBA COMPLETADA EXITOSAMENTE")
                                            Log.d(TAG, "========================================")
                                        }
                                        .onFailure { error ->
                                            Log.e(TAG, "❌ Error eliminando producto: ${error.message}")
                                        }
                                }
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ Error actualizando producto: ${error.message}")
                            }
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error creando producto: ${error.message}")
                }
        }
    }
    
    /**
     * Prueba simple solo para verificar conectividad
     */
    fun testConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = BurgerApiProductRepository()
            
            Log.d(TAG, "Probando conexión con la API...")
            
            repository.getAllProducts()
                .onSuccess { products ->
                    Log.d(TAG, "✅ Conexión exitosa! Productos disponibles: ${products.size}")
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Error de conexión: ${error.message}")
                }
        }
    }
}
