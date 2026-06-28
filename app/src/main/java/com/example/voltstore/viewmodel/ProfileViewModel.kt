package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voltstore.data.model.Order
import com.example.voltstore.data.model.Product
import com.example.voltstore.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Дмитрий Волков",
            email = "dmitry@voltstore.ru",
            phone = "+7 (707) 123-45-67",
            address = "Алматы қ., Абай даңғылы, 10 үй",
            avatarUrl = "https://images.unsplash.com/photo-1633332755192-727a05c4013d?q=80&w=200&auto=format&fit=crop",
            bonusPoints = 1250
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateProfile(name: String, email: String, phone: String, address: String) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            email = email,
            phone = phone,
            address = address
        )
    }

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    fun addOrder(order: Order) {
        val current = _orderHistory.value.toMutableList()
        current.add(0, order)
        _orderHistory.value = current
    }

    init {
        loadMockOrders()
    }

    private fun loadMockOrders() {
        _orderHistory.value = listOf(
            Order(
                id = "VOLT-9821",
                date = "12 Июня 2024",
                products = listOf(
                    Product(1, "Volt Buds Pro", "", 7500.0, 4.9f, "Аудио", "")
                ),
                totalPrice = 7500.0,
                status = "Доставлено"
            ),
            Order(
                id = "VOLT-7740",
                date = "05 Мая 2024",
                products = listOf(
                    Product(3, "Volt Charger GaN 65W", "", 3200.0, 4.7f, "Зарядки", "")
                ),
                totalPrice = 3200.0,
                status = "Доставлено"
            )
        )
    }
}
