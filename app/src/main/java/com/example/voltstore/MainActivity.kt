package com.example.voltstore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.voltstore.ui.screens.*
import com.example.voltstore.ui.theme.VoltStoreTheme
import com.example.voltstore.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.voltstore.data.FirebaseService.init(this)
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
    val authViewModel: AuthViewModel = viewModel()
    val catalogViewModel: CatalogViewModel = viewModel()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()

    val screens = listOf(
        Screen.Catalog,
        Screen.Favorites,
        Screen.Cart,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            if (authState is AuthState.Authenticated) {
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
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = if (authState is AuthState.Authenticated) Screen.Catalog.route else Screen.Login.route
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(authViewModel, 
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onSuccess = { navController.navigate(Screen.Catalog.route) { popUpTo(0) } }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(authViewModel,
                        onNavigateToLogin = { navController.popBackStack() },
                        onSuccess = { navController.navigate(Screen.Catalog.route) { popUpTo(0) } }
                    )
                }
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
                    // Передаем userId из authState в ProfileViewModel, чтобы избежать рассинхрона
                    val userId = if (authState is AuthState.Authenticated) (authState as AuthState.Authenticated).userId else null
                    LaunchedEffect(userId) {
                        if (userId != null) {
                            profileViewModel.loadUserProfile(userId)
                        }
                    }

                    ProfileScreen(profileViewModel, authViewModel) { target ->
                        if (target == "ADMIN_PANEL") {
                            navController.navigate(Screen.AdminDashboard.route)
                        } else {
                            navController.navigate("order_details/$target")
                        }
                    }
                }
                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(adminViewModel) {
                        navController.popBackStack()
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
