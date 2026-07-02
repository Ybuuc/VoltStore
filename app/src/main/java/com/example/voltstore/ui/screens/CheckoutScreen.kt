package com.example.voltstore.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voltstore.data.model.Order
import com.example.voltstore.viewmodel.CartViewModel
import com.example.voltstore.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    profileViewModel: ProfileViewModel,
    onOrderSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    var address by remember(userProfile.address) { mutableStateOf(userProfile.address) }
    var paymentMethod by remember { mutableStateOf("Картамен төлеу") }
    val totalPrice = cartViewModel.getTotalPrice()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тапсырысты рәсімдеу") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Button(
                    onClick = {
                        scope.launch {
                            // ПРИНУДИТЕЛЬНО сохраняем профиль и ЖДЕМ завершения записи
                            profileViewModel.updateProfileSync(
                                name = userProfile.name,
                                email = userProfile.email,
                                phone = userProfile.phone,
                                address = address
                            )
                            
                            // Создаем заказ
                            val newOrder = Order(
                                id = "VOLT-${Random().nextInt(9000) + 1000}",
                                date = "Бүгін",
                                products = cartItems.keys.toList(),
                                totalPrice = totalPrice,
                                status = "Өңделуде"
                            )
                            
                            profileViewModel.addOrder(newOrder)
                            cartViewModel.clearCart()
                            onOrderSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Тапсырысты растау (${totalPrice.toInt()} ₸)")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Жеткізу мәліметтері", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Text("Жеткізу мекенжайы", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        label = { Text("Адрес") }
                    )
                }
            }

            Text("Төлем түрі", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Төлем әдісі", style = MaterialTheme.typography.labelSmall)
                        Text(paymentMethod, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Text("Тапсырыс құрамы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    cartItems.forEach { (product, count) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${product.name} x$count", modifier = Modifier.weight(1f))
                            Text("${(product.price * count).toInt()} ₸")
                        }
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Барлығы:", fontWeight = FontWeight.Bold)
                        Text("${totalPrice.toInt()} ₸", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
