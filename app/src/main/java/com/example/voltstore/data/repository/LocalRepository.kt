package com.example.voltstore.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.voltstore.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class LocalAccount(val email: String, val pass: String, val uid: String)

class LocalRepository(context: Context) {
    private val appContext = context.applicationContext
    
    // Версия v17 с максимально стабильными ссылками через прокси
    private val prefs: SharedPreferences = appContext.getSharedPreferences("voltstore_local_db_v17", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val KEY_PRODUCTS = "products"
    private val KEY_USERS = "users"
    private val KEY_ORDERS_PREFIX = "orders_v3_"
    private val KEY_ACCOUNTS = "accounts"
    private val KEY_CURRENT_USER_ID = "active_user_id"
    private val KEY_CARTS_PREFIX = "carts_v3_"

    fun getCurrentUserId(): String? = prefs.getString(KEY_CURRENT_USER_ID, null)
    fun setCurrentUserId(uid: String?) { prefs.edit().putString(KEY_CURRENT_USER_ID, uid).apply() }

    private fun getAccounts(): List<LocalAccount> {
        val jsonStr = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    suspend fun registerLocal(email: String, pass: String): String? = withContext(Dispatchers.IO) {
        val accounts = getAccounts().toMutableList()
        if (accounts.any { it.email == email }) return@withContext null
        val uid = java.util.UUID.randomUUID().toString()
        accounts.add(LocalAccount(email, pass, uid))
        prefs.edit().putString(KEY_ACCOUNTS, json.encodeToString(accounts)).commit()
        val newProfile = UserProfile(id = uid, name = email.split("@")[0], email = email, address = "", role = "user")
        saveUserProfile(uid, newProfile)
        return@withContext uid
    }

    fun loginLocal(email: String, pass: String): String? {
        if (email == "admin@voltstore.ru" && pass == "admin123") return "demo_admin_id"
        if (email == "guest@voltstore.ru" && pass == "guest123") return "demo_guest_id"
        return getAccounts().find { it.email == email && it.pass == pass }?.uid
    }

    suspend fun getAllUsers(): List<UserProfile> = withContext(Dispatchers.IO) {
        val jsonStr = prefs.getString(KEY_USERS, null) ?: return@withContext getInitialDemoUsers()
        return@withContext try { json.decodeFromString<List<UserProfile>>(jsonStr).ifEmpty { getInitialDemoUsers() } } catch (e: Exception) { getInitialDemoUsers() }
    }

    private fun getInitialDemoUsers() = listOf(
        UserProfile(id = "demo_admin_id", name = "Администратор", email = "admin@voltstore.ru", role = "admin", address = "Достық даңғылы, 50"),
        UserProfile(id = "demo_guest_id", name = "Гость", email = "guest@voltstore.ru", role = "user", address = "Сәтпаев көшесі, 15")
    )

    suspend fun getUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        getAllUsers().find { it.id == userId } ?: when(userId) {
            "demo_admin_id" -> getInitialDemoUsers()[0]
            "demo_guest_id" -> getInitialDemoUsers()[1]
            else -> null
        }
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfile) = withContext(Dispatchers.IO) {
        val users = getAllUsers().toMutableList()
        val index = users.indexOfFirst { it.id == userId }
        if (index != -1) users[index] = profile.copy(id = userId) else users.add(profile.copy(id = userId))
        prefs.edit().putString(KEY_USERS, json.encodeToString(users)).commit()
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        val users = getAllUsers().filter { it.id != userId }
        prefs.edit().putString(KEY_USERS, json.encodeToString(users)).commit()
        val accounts = getAccounts().filter { it.uid != userId }
        prefs.edit().putString(KEY_ACCOUNTS, json.encodeToString(accounts)).commit()
    }

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        val jsonStr = prefs.getString(KEY_PRODUCTS, null) ?: return@withContext getInitialMockProducts()
        return@withContext try { json.decodeFromString(jsonStr) } catch (e: Exception) { getInitialMockProducts() }
    }

    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        val products = getProducts().toMutableList()
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) products[index] = product else products.add(product)
        prefs.edit().putString(KEY_PRODUCTS, json.encodeToString(products)).commit()
    }

    suspend fun deleteProduct(productId: String) = withContext(Dispatchers.IO) {
        val products = getProducts().filter { it.id != productId }
        prefs.edit().putString(KEY_PRODUCTS, json.encodeToString(products)).commit()
    }

    suspend fun getOrders(userId: String): List<Order> = withContext(Dispatchers.IO) {
        val jsonStr = prefs.getString(KEY_ORDERS_PREFIX + userId, null) ?: return@withContext emptyList()
        return@withContext try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    suspend fun getAllOrders(): List<Order> = withContext(Dispatchers.IO) {
        val allIds = getAccounts().map { it.uid } + listOf("demo_admin_id", "demo_guest_id")
        return@withContext allIds.flatMap { getOrders(it) }
    }

    suspend fun saveOrder(userId: String, order: Order) = withContext(Dispatchers.IO) {
        val orders = getOrders(userId).toMutableList()
        orders.add(0, order.copy(userId = userId))
        prefs.edit().putString(KEY_ORDERS_PREFIX + userId, json.encodeToString(orders)).commit()
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        val allIds = getAccounts().map { it.uid } + listOf("demo_admin_id", "demo_guest_id")
        for (uid in allIds) {
            val orders = getOrders(uid).toMutableList()
            val index = orders.indexOfFirst { it.id == orderId }
            if (index != -1) {
                orders[index] = orders[index].copy(status = status)
                prefs.edit().putString(KEY_ORDERS_PREFIX + uid, json.encodeToString(orders)).commit()
                return@withContext
            }
        }
    }

    suspend fun saveCart(userId: String, cartItems: List<CartItem>) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_CARTS_PREFIX + userId, json.encodeToString(cartItems)).apply()
    }

    suspend fun getCart(userId: String): List<CartItem> = withContext(Dispatchers.IO) {
        val jsonStr = prefs.getString(KEY_CARTS_PREFIX + userId, null) ?: return@withContext emptyList()
        return@withContext try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    private fun getInitialMockProducts(): List<Product> {
        val p = "https://images.weserv.nl/?url=https://images.unsplash.com/photo-"
        val q = "&w=300&h=300&fit=cover"
        return listOf(
            Product("1", "PowerBank 20k", "Қуатты аккумулятор.", 24990.0, 4.8f, "Аксессуарлар", "${p}1609091839311-d536819fe228$q"),
            Product("2", "Buds Pro", "Шу басатын құлаққаптар.", 45990.0, 4.9f, "Аудио", "${p}1590658268037-d12d90610332$q"),
            Product("3", "GaN 65W", "Зарядтау құрылғысы.", 15500.0, 4.7f, "Зарядтау", "${p}1560067174-e553b3647693$q"),
            Product("4", "Watch S1", "Смарт-сағаттар.", 89990.0, 4.6f, "Смарт-сағат", "${p}1508685096489-77a4ad2ba521$q"),
            Product("5", "Cable 2m", "Берік USB-C кабелі.", 4990.0, 4.5f, "Кабельдер", "${p}1612450518210-256fa076639d$q"),
            Product("6", "Stand", "Сымсыз зарядтау.", 12500.0, 4.7f, "Зарядтау", "${p}1605333550881-8b3687391942$q"),
            Product("7", "Hub 7-in-1", "USB-C хабы.", 18990.0, 4.8f, "Аксессуарлар", "${p}1563914098-b8089278453c$q"),
            Product("8", "Mouse", "Сымсыз тышқан.", 9990.0, 4.7f, "Аксессуарлар", "${p}1527866959224-340375a0246a$q"),
            Product("9", "Speaker Go", "Колонка.", 21500.0, 4.9f, "Аудио", "${p}1608043105843-df5814471b73$q"),
            Product("10", "Headset Pro", "Құлаққаптар.", 35990.0, 4.8f, "Аудио", "${p}1505740487315-996406538c6c$q"),
            Product("11", "Case Ultra", "Қапшық.", 3500.0, 4.4f, "Аксессуарлар", "${p}1601783515431-7e02165e5a3c$q"),
            Product("12", "Light Led", "Ақылды жарық.", 14990.0, 4.7f, "Гаджеттер", "${p}1534073864703-9e489b5bcd1b$q"),
            Product("13", "Keyboard K1", "Пернетақта.", 42000.0, 4.9f, "Гаджеттер", "${p}1587847702226-c2f82992e1e3$q"),
            Product("14", "Fan Mini", "Желдеткіш.", 2990.0, 4.3f, "Аксессуарлар", "${p}1591144738590-cef23723a657$q"),
            Product("15", "Pad XL", "Төсеніш.", 7500.0, 4.8f, "Аксессуарлар", "${p}1612450518210-256fa076639d$q"),
            Product("16", "Screen Pro", "Тазалау жинағы.", 3200.0, 4.6f, "Күтім", "${p}1605333550881-8b3687391942$q"),
            Product("17", "Tripod Go", "Штатив.", 12990.0, 4.5f, "Фото", "${p}1591144883938-7b608d1e56a2$q"),
            Product("18", "Mic Stream", "Микрофон.", 55000.0, 4.9f, "Аудио", "${p}1584674407370-9551615a6579$q"),
            Product("19", "Bag Volt", "Сөмке.", 18500.0, 4.7f, "Күтім", "${p}1534073864703-9e489b5bcd1b$q"),
            Product("20", "Clip On", "Шам.", 4500.0, 4.4f, "Гаджеттер", "${p}1534073864703-9e489b5bcd1b$q"),
            Product("21", "Plug Smart", "Розетка.", 8990.0, 4.8f, "Гаджеттер", "${p}1583863788412-2a246d953935$q"),
            Product("22", "Smart Phone", "Смартфон", 399900.0, 4.6f, "Гаджеттер", "${p}1511706680500-b1d447262873$q")
        )
    }
}
