package com.example.voltstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.voltstore.ui.navigation.Screen
import com.example.voltstore.ui.screens.CartScreen
import com.example.voltstore.ui.screens.CatalogScreen
import com.example.voltstore.ui.screens.CheckoutScreen
import com.example.voltstore.ui.screens.FavoritesScreen
import com.example.voltstore.ui.screens.OrderDetailsScreen
import com.example.voltstore.ui.screens.ProfileScreen
import com.example.voltstore.ui.theme.VoltStoreTheme
import com.example.voltstore.viewmodel.CartViewModel
import com.example.voltstore.viewmodel.CatalogViewModel
import com.example.voltstore.viewmodel.FavoritesViewModel
import com.example.voltstore.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoltStoreTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val catalogViewModel: CatalogViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val screens = listOf(
        Screen.Catalog,
        Screen.Favorites,
        Screen.Cart,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (currentDestination?.route != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Catalog.route
            ) {
                composable(Screen.Catalog.route) {
                    CatalogScreen(catalogViewModel, favoritesViewModel, cartViewModel)
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(favoritesViewModel, cartViewModel)
                }
                composable(Screen.Cart.route) {
                    CartScreen(cartViewModel) {
                        navController.navigate(Screen.Checkout.route)
                    }
                }
                composable(Screen.Checkout.route) {
                    CheckoutScreen(cartViewModel, profileViewModel, onOrderSuccess = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }, onBackClick = {
                        navController.popBackStack()
                    })
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(profileViewModel) { orderId ->
                        navController.navigate("order_details/$orderId")
                    }
                }
                composable(Screen.OrderDetails.route) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderDetailsScreen(orderId, profileViewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
