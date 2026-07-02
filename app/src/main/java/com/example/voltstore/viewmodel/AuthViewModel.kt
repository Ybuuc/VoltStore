package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltstore.data.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = FirebaseService.repository

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val userId = repository.getCurrentUserId()
        _authState.value = if (userId != null) {
            _isDemoMode.value = userId.startsWith("demo_")
            AuthState.Authenticated(userId)
        } else {
            _isDemoMode.value = false
            AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()

            val userId = repository.loginLocal(trimmedEmail, trimmedPass)
            if (userId != null) {
                repository.setCurrentUserId(userId)
                _isDemoMode.value = userId.startsWith("demo_")
                _authState.value = AuthState.Authenticated(userId)
                onResult(true)
            } else {
                _authState.value = AuthState.Error("Қате: Мәліметтерді тексеріңіз")
                onResult(false)
            }
        }
    }

    fun register(email: String, pass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val trimmedEmail = email.trim()
            val trimmedPass = pass.trim()
            
            val userId = repository.registerLocal(trimmedEmail, trimmedPass)
            if (userId != null) {
                repository.setCurrentUserId(userId)
                _isDemoMode.value = false
                _authState.value = AuthState.Authenticated(userId)
                onResult(true)
            } else {
                _authState.value = AuthState.Error("Қате: Бұл email бос емес")
                onResult(false)
            }
        }
    }

    fun logout() {
        repository.setCurrentUserId(null)
        _isDemoMode.value = false
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
