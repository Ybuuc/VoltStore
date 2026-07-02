package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltstore.data.FirebaseService
import com.example.voltstore.data.model.Product
import com.example.voltstore.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AdminViewModel : ViewModel() {
    private val repository = FirebaseService.repository

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    init {
        loadProducts()
        loadUsers()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = repository.getProducts()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getAllUsers()
        }
    }

    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            val finalProduct = if (product.id.isEmpty()) {
                product.copy(id = UUID.randomUUID().toString())
            } else {
                product
            }
            repository.saveProduct(finalProduct)
            loadProducts()
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            loadProducts()
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            loadUsers()
        }
    }
}
