package com.example.voltstore.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val rating: Float = 0.0f,
    val category: String = "",
    val imageUrl: String = "",
    val isFavorite: Boolean = false
)
