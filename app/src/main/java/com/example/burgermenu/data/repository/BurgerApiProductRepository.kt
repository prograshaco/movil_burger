package com.example.burgermenu.data.repository

import android.util.Log
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.network.ApiProduct
import com.example.burgermenu.data.network.BurgerApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BurgerApiProductRepository {
    
    // Convertir ApiProduct a Product (modelo local)
    private fun ApiProduct.toProduct(): Product {
        return Product(
            id = id ?: "",
            name = name,
            description = description,
            price = (price * 100).toInt(), // Convertir a centavos
            category = category,
            image_url = imageUrl,
            available = available, // Ya es Int (0 o 1)
            created_at = ""
        )
    }
    
    // Convertir Product a ApiProduct
    private fun Product.toApiProduct(): ApiProduct {
        return ApiProduct(
            id = if (id.isNotEmpty()) id else null,
            name = name,
            description = description,
            price = price / 100.0, // Convertir de centavos a precio decimal
            category = category,
            imageUrl = image_url,
            available = available // Ya es Int (0 o 1)
        )
    }
    
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Obteniendo todos los productos de la API")
            
            val response = BurgerApiClient.getAllProducts()
            
            if (response.isSuccess) {
                val apiProducts = response.getOrThrow()
                val products = apiProducts.map { it.toProduct() }
                Log.d("BurgerApiProductRepository", "✅ Productos obtenidos: ${products.size}")
                Result.success(products)
            } else {
                val error = response.exceptionOrNull()
                Log.e("BurgerApiProductRepository", "❌ Error: ${error?.message}")
                Result.failure(error ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductsByCategory(category: String): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Obteniendo productos por categoría: $category")
            
            val response = BurgerApiClient.getAllProducts()
            
            if (response.isSuccess) {
                val apiProducts = response.getOrThrow()
                val products = apiProducts
                    .filter { it.category.equals(category, ignoreCase = true) && it.isAvailable }
                    .map { it.toProduct() }
                Log.d("BurgerApiProductRepository", "✅ Productos filtrados: ${products.size}")
                Result.success(products)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener productos"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCategories(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Obteniendo categorías")
            
            val response = BurgerApiClient.getAllProducts()
            
            if (response.isSuccess) {
                val apiProducts = response.getOrThrow()
                val categories = apiProducts
                    .filter { it.isAvailable }
                    .map { it.category }
                    .distinct()
                    .sorted()
                Log.d("BurgerApiProductRepository", "✅ Categorías obtenidas: ${categories.size}")
                Result.success(categories)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener categorías"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(productId: String): Result<Product?> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Obteniendo producto por ID: $productId")
            
            val response = BurgerApiClient.getProductById(productId)
            
            if (response.isSuccess) {
                val apiProduct = response.getOrThrow()
                val product = apiProduct.toProduct()
                Log.d("BurgerApiProductRepository", "✅ Producto obtenido: ${product.name}")
                Result.success(product)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al obtener producto"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(name: String, description: String, price: Double, category: String, imageUrl: String = ""): Result<Product> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Creando producto: $name")
            
            val apiProduct = ApiProduct(
                name = name,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                available = 1
            )
            
            val response = BurgerApiClient.createProduct(apiProduct)
            
            if (response.isSuccess) {
                val createdProduct = response.getOrThrow().toProduct()
                Log.d("BurgerApiProductRepository", "✅ Producto creado: ${createdProduct.name}")
                Result.success(createdProduct)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al crear producto"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(productId: String, name: String, description: String, price: Double, category: String, imageUrl: String = ""): Result<Product> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Actualizando producto: $productId")
            
            val apiProduct = ApiProduct(
                id = productId,
                name = name,
                description = description,
                price = price,
                category = category,
                imageUrl = imageUrl,
                available = 1
            )
            
            val response = BurgerApiClient.updateProduct(productId, apiProduct)
            
            if (response.isSuccess) {
                val updatedProduct = response.getOrThrow().toProduct()
                Log.d("BurgerApiProductRepository", "✅ Producto actualizado: ${updatedProduct.name}")
                Result.success(updatedProduct)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al actualizar producto"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("BurgerApiProductRepository", "Eliminando producto: $productId")
            
            val response = BurgerApiClient.deleteProduct(productId)
            
            if (response.isSuccess) {
                Log.d("BurgerApiProductRepository", "✅ Producto eliminado")
                Result.success(true)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Error al eliminar producto"))
            }
        } catch (e: Exception) {
            Log.e("BurgerApiProductRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}
