package com.example.voltstore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.voltstore.data.model.Product
import com.example.voltstore.data.model.UserProfile
import com.example.voltstore.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: AdminViewModel, onBack: () -> Unit) {
    val products by viewModel.products.collectAsState()
    val users by viewModel.users.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Обновляем данные при каждом открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
        viewModel.loadUsers()
    }
    
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Админ панель") },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text("Артқа") }
                    },
                    actions = {
                        if (selectedTab == 0) {
                            IconButton(onClick = { 
                                editingProduct = null
                                showDialog = true 
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить")
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Тауарлар") },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Пайдаланушылар") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(products) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = { Text("${product.price} ₸") },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = { 
                                            editingProduct = product
                                            showDialog = true 
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Изменить")
                                        }
                                        IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
                1 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(users) { user ->
                            ListItem(
                                headlineContent = { Text(if (user.name.isEmpty()) "Аты жоқ" else user.name) },
                                supportingContent = { Text(user.email) },
                                overlineContent = { Text("Рөл: ${user.role}") },
                                trailingContent = {
                                    if (user.role != "admin") { // Защита от удаления админа самим собой
                                        IconButton(onClick = { viewModel.deleteUser(user.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProductEditDialog(
            product = editingProduct,
            onDismiss = { showDialog = false },
            onConfirm = { product ->
                viewModel.addOrUpdateProduct(product)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductEditDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onConfirm: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Жаңа тауар" else "Өңдеу") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Атауы") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Бағасы") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Сипаттамасы") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категория") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Сурет URL") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    (product ?: Product()).copy(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        description = description,
                        category = category,
                        imageUrl = imageUrl
                    )
                )
            }) { Text("Сақтау") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Болдырмау") }
        }
    )
}
