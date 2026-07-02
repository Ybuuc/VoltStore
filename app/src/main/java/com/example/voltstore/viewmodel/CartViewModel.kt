package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltstore.data.FirebaseService
import com.example.voltstore.data.model.CartItem
import com.example.voltstore.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = FirebaseService.repository
    
    private val _cartItems = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cartItems: StateFlow<Map<Product, Int>> = _cartItems.asStateFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        val userId = repository.getCurrentUserId() ?: "demo_guest_id"
        viewModelScope.launch {
            val localCart = repository.getCart(userId)
            if (localCart.isNotEmpty()) {
                _cartItems.value = localCart.associate { it.product to it.quantity }
            }
        }
    }

    private fun syncCart() {
        val userId = repository.getCurrentUserId() ?: "demo_guest_id"
        viewModelScope.launch {
            val items = _cartItems.value.map { CartItem(it.key, it.value) }
            repository.saveCart(userId, items)
        }
    }

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableMap()
        current[product] = current.getOrDefault(product, 0) + 1
        _cartItems.value = current
        syncCart()
    }

    fun removeFromCart(product: Product) {
        val current = _cartItems.value.toMutableMap()
        val count = current.getOrDefault(product, 0)
        if (count > 1) {
            current[product] = count - 1
        } else {
            current.remove(product)
        }
        _cartItems.value = current
        syncCart()
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        syncCart()
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.entries.sumOf { it.key.price * it.value }
    }
}
