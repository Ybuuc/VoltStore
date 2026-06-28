package com.example.voltstore.data.model

data class Order(
    val id: String,
    val date: String,
    val products: List<Product>,
    val totalPrice: Double,
    val status: String
)
