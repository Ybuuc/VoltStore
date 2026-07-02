package com.example.voltstore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voltstore.ui.components.ProductCard
import com.example.voltstore.viewmodel.CartViewModel
import com.example.voltstore.viewmodel.CatalogViewModel
import com.example.voltstore.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    catalogViewModel: CatalogViewModel,
    favoritesViewModel: FavoritesViewModel,
    cartViewModel: CartViewModel
) {
    val products by catalogViewModel.products.collectAsState()
    val favoriteProducts by favoritesViewModel.favoriteProducts.collectAsState()

    LaunchedEffect(Unit) {
        catalogViewModel.loadProducts()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Volt Store",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 2.dp,
                end = 2.dp,
                top = paddingValues.calculateTopPadding() + 2.dp,
                bottom = 2.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { product ->
                val isFavorite = favoriteProducts.any { it.id == product.id }
                ProductCard(
                    product = product,
                    onBuyClick = { cartViewModel.addToCart(product) },
                    onFavoriteClick = { favoritesViewModel.toggleFavorite(product) },
                    isFavorite = isFavorite
                )
            }
        }
    }
}
