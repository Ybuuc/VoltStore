package com.example.voltstore.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val products: List<Product> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = ""
)
