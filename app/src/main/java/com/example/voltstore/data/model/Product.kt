package com.example.voltstore.data.model

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val rating: Float,
    val category: String,
    val imageUrl: String,
    val isFavorite: Boolean = false
)
