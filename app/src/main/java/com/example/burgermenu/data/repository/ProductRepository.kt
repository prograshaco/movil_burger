package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.network.ApiProduct
import com.example.burgermenu.data.network.BurgerApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo productos desde API...")
            val result = BurgerApiClient.getAllProducts()
            
            if (result.isSuccess) {
                val apiProducts = result.getOrThrow()
                Log.d("ProductRepository", "API Products recibidos: ${apiProducts.size}")
                apiProducts.forEachIndexed { index, apiProduct ->
                    Log.d("ProductRepository", "  [$index] id='${apiProduct.id}' name='${apiProduct.name}'")
                }
                
                val products = apiProducts.map { it.toDomain() }
                Log.d("ProductRepository", "Productos mapeados: ${products.size}")
                products.forEachIndexed { index, product ->
                    Log.d("ProductRepository", "  [$index] id='${product.id}' name='${product.name}'")
                }
                
                Result.success(products)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo productos: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(productId: String): Result<Product?> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo producto $productId desde API...")
            val result = BurgerApiClient.getProductById(productId)
            
            if (result.isSuccess) {
                val apiProduct = result.getOrThrow()
                Result.success(apiProduct.toDomain())
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo producto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(name: String, description: String, price: Double, category: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Creando producto en API...")
            val apiProduct = ApiProduct(
                name = name,
                description = description,
                price = price, // La API espera Double
                category = category,
                imageUrl = "",
                available = 1
            )
            
            val result = BurgerApiClient.createProduct(apiProduct)
            
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error creando producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error creando producto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(productId: String, name: String, description: String, price: Double, category: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Actualizando producto $productId en API...")
            val apiProduct = ApiProduct(
                id = productId,
                name = name,
                description = description,
                price = price,
                category = category,
                imageUrl = "",
                available = 1
            )
            
            val result = BurgerApiClient.updateProduct(productId, apiProduct)
            
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error actualizando producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error actualizando producto: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getProductsByCategory(category: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo productos de categoría $category desde API...")
            val result = BurgerApiClient.getProductsByCategory(category)
            
            if (result.isSuccess) {
                val apiProducts = result.getOrThrow()
                val products = apiProducts.map { it.toDomain() }
                Result.success(products)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo productos por categoría: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCategories(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Obteniendo categorías desde API...")
            val result = BurgerApiClient.getCategories()
            
            if (result.isSuccess) {
                Result.success(result.getOrThrow())
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error obteniendo categorías: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("ProductRepository", "Eliminando producto $productId desde API...")
            val result = BurgerApiClient.deleteProduct(productId)
            
            if (result.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error eliminando producto"))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error eliminando producto: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Método de extensión para convertir de ApiProduct a Product (Dominio)
    private fun ApiProduct.toDomain(): Product {
        return Product(
            id = this.id ?: "",
            name = this.name,
            description = this.description,
            price = (this.price * 100).toInt(), // Convertir a centavos para el dominio
            category = this.category,
            image_url = this.imageUrl,
            available = this.available,
            created_at = "" 
        )
    }
}