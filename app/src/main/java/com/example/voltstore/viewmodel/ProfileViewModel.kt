package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltstore.data.FirebaseService
import com.example.voltstore.data.model.Order
import com.example.voltstore.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = FirebaseService.repository

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val currentUserId = repository.getCurrentUserId() ?: "demo_guest_id"
        loadUserProfile(currentUserId)
        loadOrders(currentUserId)
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile(userId)
                if (profile != null) {
                    _userProfile.value = profile
                } else {
                    // Если профиль совсем не найден (даже в демо), создаем новый с этим ID
                    val newProfile = UserProfile(id = userId, name = "Пайдаланушы", address = "")
                    _userProfile.value = newProfile
                    repository.saveUserProfile(userId, newProfile)
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun loadOrders(userId: String) {
        viewModelScope.launch {
            _orderHistory.value = repository.getOrders(userId)
        }
    }

    suspend fun updateProfileSync(name: String, email: String, phone: String, address: String) {
        val current = _userProfile.value
        val userId = if (current.id.isNotEmpty()) current.id else (repository.getCurrentUserId() ?: "demo_guest_id")
        
        val updatedProfile = current.copy(
            id = userId,
            name = name,
            email = email,
            phone = phone,
            address = address
        )
        repository.saveUserProfile(userId, updatedProfile)
        _userProfile.value = updatedProfile
    }

    fun updateProfile(name: String, email: String, phone: String, address: String) {
        viewModelScope.launch {
            updateProfileSync(name, email, phone, address)
        }
    }

    fun addOrder(order: Order) {
        viewModelScope.launch {
            val userId = _userProfile.value.id.ifEmpty { repository.getCurrentUserId() ?: "demo_guest_id" }
            repository.saveOrder(userId, order)
            _orderHistory.value = repository.getOrders(userId)
        }
    }
}
