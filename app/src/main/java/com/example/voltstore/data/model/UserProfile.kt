package com.example.voltstore.data.model

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val avatarUrl: String,
    val bonusPoints: Int
)
