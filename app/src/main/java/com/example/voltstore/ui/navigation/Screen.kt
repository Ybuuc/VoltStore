package com.example.voltstore.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Catalog : Screen("catalog", "Каталог", Icons.Default.Storefront)
    object Favorites : Screen("favorites", "Таңдаулылар", Icons.Default.Favorite)
    object Cart : Screen("cart", "Себет", Icons.Default.ShoppingCart)
    object Profile : Screen("profile", "Профиль", Icons.Default.Person)
    object OrderDetails : Screen("order_details/{orderId}", "Тапсырыс мәліметтері", Icons.Default.History)
    object Checkout : Screen("checkout", "Тапсырысты рәсімдеу", Icons.Default.ShoppingCart)
}
