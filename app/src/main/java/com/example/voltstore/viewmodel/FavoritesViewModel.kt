package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voltstore.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesViewModel : ViewModel() {
    private val _favoriteProducts = MutableStateFlow<List<Product>>(emptyList())
    val favoriteProducts: StateFlow<List<Product>> = _favoriteProducts.asStateFlow()

    fun toggleFavorite(product: Product) {
        val current = _favoriteProducts.value.toMutableList()
        if (current.any { it.id == product.id }) {
            current.removeAll { it.id == product.id }
        } else {
            current.add(product.copy(isFavorite = true))
        }
        _favoriteProducts.value = current
    }

    fun isFavorite(productId: Int): Boolean {
        return _favoriteProducts.value.any { it.id == productId }
    }
}
