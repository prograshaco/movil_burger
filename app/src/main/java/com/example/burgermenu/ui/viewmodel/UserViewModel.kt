package com.example.burgermenu.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgermenu.data.models.User
import com.example.burgermenu.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val users: List<User> = emptyList(),
    val showActiveOnly: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = if (_uiState.value.showActiveOnly) {
                repository.getActiveUsers()
            } else {
                repository.getAllUsers()
            }
            
            result
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(
                        users = users,
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
    
    fun toggleActiveFilter(showActiveOnly: Boolean) {
        _uiState.value = _uiState.value.copy(showActiveOnly = showActiveOnly)
        loadUsers()
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.deleteUser(userId)
                .onSuccess {
                    loadUsers()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al eliminar usuario: ${exception.message}"
                    )
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun retry() {
        loadUsers()
    }
}