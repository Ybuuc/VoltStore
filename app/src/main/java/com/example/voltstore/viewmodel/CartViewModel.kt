package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voltstore.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cartItems: StateFlow<Map<Product, Int>> = _cartItems.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableMap()
        current[product] = current.getOrDefault(product, 0) + 1
        _cartItems.value = current
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
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.entries.sumOf { it.key.price * it.value }
    }
}
