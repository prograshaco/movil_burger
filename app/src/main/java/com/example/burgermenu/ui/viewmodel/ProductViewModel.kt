package com.example.burgermenu.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgermenu.data.models.Product
import com.example.burgermenu.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Todos",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
        loadCategories()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            Log.d("ProductViewModel", "Iniciando carga de productos...")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getAllProducts()
                .onSuccess { products ->
                    Log.d("ProductViewModel", "Productos cargados exitosamente: ${products.size}")
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    Log.e("ProductViewModel", "Error cargando productos: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Error desconocido",
                        isLoading = false
                    )
                }
        }
    }
    
    fun loadProductsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                selectedCategory = category
            )
            
            val result = if (category == "Todos") {
                repository.getAllProducts()
            } else {
                repository.getProductsByCategory(category)
            }
            
            result
                .onSuccess { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
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
    
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = listOf("Todos") + categories
                    )
                }
                .onFailure { exception ->
                    // Si falla cargar categorías, usar las por defecto
                    _uiState.value = _uiState.value.copy(
                        categories = listOf("Todos", "Hamburguesas", "Bebidas", "Acompañamientos", "Pollo", "Ensaladas")
                    )
                }
        }
    }
    
    fun selectCategory(category: String) {
        if (category != _uiState.value.selectedCategory) {
            loadProductsByCategory(category)
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.deleteProduct(productId)
                .onSuccess {
                    refreshProducts()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al eliminar: ${exception.message}"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retry() {
        if (_uiState.value.selectedCategory == "Todos") {
            loadProducts()
        } else {
            loadProductsByCategory(_uiState.value.selectedCategory)
        }
    }
    
    fun refreshProducts() {
        Log.d("ProductViewModel", "Refrescando productos...")
        loadProducts()
        loadCategories()
    }
}