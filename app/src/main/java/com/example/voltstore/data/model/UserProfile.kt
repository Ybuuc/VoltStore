package com.example.voltstore.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val bonusPoints: Int = 0,
    val role: String = "user"
)
