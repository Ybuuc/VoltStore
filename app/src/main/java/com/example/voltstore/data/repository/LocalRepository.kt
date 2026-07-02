package com.example.voltstore.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.voltstore.data.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class LocalAccount(val email: String, val pass: String, val uid: String)

class LocalRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("voltstore_local_db_v6", Context.MODE_PRIVATE)
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    private val KEY_PRODUCTS = "products"
    private val KEY_USERS = "users_list"
    private val KEY_ORDERS_PREFIX = "orders_v2_"
    private val KEY_CARTS_PREFIX = "carts_v2_"
    private val KEY_ACCOUNTS = "accounts_list"
    private val KEY_CURRENT_USER_ID = "active_user_id"

    // AUTH
    fun getCurrentUserId(): String? = prefs.getString(KEY_CURRENT_USER_ID, null)

    fun setCurrentUserId(uid: String?) {
        prefs.edit().putString(KEY_CURRENT_USER_ID, uid).commit()
    }

    private fun getAccounts(): List<LocalAccount> {
        val jsonStr = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    suspend fun registerLocal(email: String, pass: String): String? {
        val accounts = getAccounts().toMutableList()
        if (accounts.any { it.email == email }) return null
        val uid = java.util.UUID.randomUUID().toString()
        accounts.add(LocalAccount(email, pass, uid))
        prefs.edit().putString(KEY_ACCOUNTS, json.encodeToString(accounts)).commit()
        
        val newProfile = UserProfile(
            id = uid,
            name = email.split("@")[0].replaceFirstChar { it.uppercase() },
            email = email,
            address = "",
            role = "user"
        )
        saveUserProfile(uid, newProfile)
        return uid
    }

    fun loginLocal(email: String, pass: String): String? {
        if (email == "admin@voltstore.ru" && pass == "admin123") return "demo_admin_id"
        if (email == "guest@voltstore.ru" && pass == "guest123") return "demo_guest_id"
        return getAccounts().find { it.email == email && it.pass == pass }?.uid
    }

    // USERS
    suspend fun getAllUsers(): List<UserProfile> {
        val jsonStr = prefs.getString(KEY_USERS, null)
        if (jsonStr == null) return getInitialDemoUsers()
        
        return try {
            val list = json.decodeFromString<List<UserProfile>>(jsonStr)
            if (list.isEmpty()) getInitialDemoUsers() else list
        } catch (e: Exception) {
            getInitialDemoUsers()
        }
    }

    private fun getInitialDemoUsers(): List<UserProfile> {
        return listOf(
            UserProfile(id = "demo_admin_id", name = "Администратор", email = "admin@voltstore.ru", role = "admin", bonusPoints = 0, address = ""),
            UserProfile(id = "demo_guest_id", name = "Гость", email = "guest@voltstore.ru", role = "user", bonusPoints = 0, address = "")
        )
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        val all = getAllUsers()
        val found = all.find { it.id == userId }
        if (found != null) return found
        
        return when(userId) {
            "demo_admin_id" -> getInitialDemoUsers()[0]
            "demo_guest_id" -> getInitialDemoUsers()[1]
            else -> null
        }
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        val users = getAllUsers().toMutableList()
        val profileToSave = profile.copy(id = userId)
        val index = users.indexOfFirst { it.id == userId }
        if (index != -1) {
            users[index] = profileToSave
        } else {
            users.add(profileToSave)
        }
        prefs.edit().putString(KEY_USERS, json.encodeToString(users)).commit()
    }

    suspend fun deleteUser(userId: String) {
        val users = getAllUsers().filter { it.id != userId }
        prefs.edit().putString(KEY_USERS, json.encodeToString(users)).commit()
        val accounts = getAccounts().filter { it.uid != userId }
        prefs.edit().putString(KEY_ACCOUNTS, json.encodeToString(accounts)).commit()
    }

    // PRODUCTS
    suspend fun getProducts(): List<Product> {
        val jsonStr = prefs.getString(KEY_PRODUCTS, null) ?: return getInitialMockProducts()
        return try { json.decodeFromString(jsonStr) } catch (e: Exception) { getInitialMockProducts() }
    }

    suspend fun saveProduct(product: Product) {
        val products = getProducts().toMutableList()
        val index = products.indexOfFirst { it.id == product.id }
        if (index != -1) products[index] = product else products.add(product)
        prefs.edit().putString(KEY_PRODUCTS, json.encodeToString(products)).commit()
    }

    suspend fun deleteProduct(productId: String) {
        val products = getProducts().filter { it.id != productId }
        prefs.edit().putString(KEY_PRODUCTS, json.encodeToString(products)).commit()
    }

    // ORDERS
    suspend fun getOrders(userId: String): List<Order> {
        val jsonStr = prefs.getString(KEY_ORDERS_PREFIX + userId, null) ?: return emptyList()
        return try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    suspend fun saveOrder(userId: String, order: Order) {
        val orders = getOrders(userId).toMutableList()
        orders.add(0, order)
        prefs.edit().putString(KEY_ORDERS_PREFIX + userId, json.encodeToString(orders)).commit()
    }

    // CART
    suspend fun saveCart(userId: String, cartItems: List<CartItem>) {
        prefs.edit().putString(KEY_CARTS_PREFIX + userId, json.encodeToString(cartItems)).commit()
    }

    suspend fun getCart(userId: String): List<CartItem> {
        val jsonStr = prefs.getString(KEY_CARTS_PREFIX + userId, null) ?: return emptyList()
        return try { json.decodeFromString(jsonStr) } catch (e: Exception) { emptyList() }
    }

    private fun getInitialMockProducts(): List<Product> {
        val b = "https://loremflickr.com/400/400/"
        return listOf(
            Product("1", "PowerBank 20k", "Қуатты аккумулятор.", 24990.0, 4.8f, "Аксессуарлар", "${b}powerbank?lock=1"),
            Product("2", "Buds Pro", "Шу басатын құлаққаптар.", 45990.0, 4.9f, "Аудио", "${b}earbuds?lock=2"),
            Product("3", "GaN 65W", "Шағын зарядтау құрылғысы.", 15500.0, 4.7f, "Зарядтау", "${b}charger?lock=3"),
            Product("4", "Watch S1", "Смарт-сағаттар.", 89990.0, 4.6f, "Смарт-сағат", "${b}smartwatch?lock=4"),
            Product("5", "Cable 2m", "USB-C кабелі.", 4990.0, 4.5f, "Кабельдер", "${b}usb-cable?lock=5"),
            Product("6", "Stand", "Сымсыз зарядтау.", 12500.0, 4.7f, "Зарядтау", "${b}wireless-charger?lock=6"),
            Product("7", "Hub 7-in-1", "USB-C хабы.", 18990.0, 4.8f, "Аксессуарлар", "${b}usb-hub?lock=7"),
            Product("8", "Mouse", "Сымсыз тышқан.", 9990.0, 4.7f, "Аксессуарлар", "${b}mouse,computer?lock=8"),
            Product("9", "Speaker Go", "Колонка.", 21500.0, 4.9f, "Аудио", "${b}speaker?lock=9"),
            Product("10", "Headset Pro", "Құлаққаптар.", 35990.0, 4.8f, "Аудио", "${b}headset?lock=10"),
            Product("11", "Case Ultra", "Қапшық.", 3500.0, 4.4f, "Аксессуарлар", "${b}phone-case?lock=11"),
            Product("12", "Light Led", "Ақылды жарық.", 14990.0, 4.7f, "Гаджеттер", "${b}led-lamp?lock=12"),
            Product("13", "Keyboard K1", "Пернетақта.", 42000.0, 4.9f, "Гаджеттер", "${b}keyboard?lock=13"),
            Product("14", "Fan Mini", "Желдеткіш.", 2990.0, 4.3f, "Аксессуарлар", "${b}fan?lock=14"),
            Product("15", "Pad XL", "Төсеніш.", 7500.0, 4.8f, "Аксессуарлар", "${b}mousepad?lock=15"),
            Product("16", "Screen Pro", "Тазалау жинағы.", 3200.0, 4.6f, "Күтім", "${b}cleaning-kit?lock=16"),
            Product("17", "Tripod Go", "Штатив.", 12990.0, 4.5f, "Фото", "${b}tripod?lock=17"),
            Product("18", "Mic Stream", "Микрофон.", 55000.0, 4.9f, "Аудио", "${b}microphone?lock=18"),
            Product("19", "Bag Volt", "Сөмке.", 18500.0, 4.7f, "Күтім", "${b}backpack?lock=19"),
            Product("20", "Clip On", "Шам.", 4500.0, 4.4f, "Гаджеттер", "${b}clip-light?lock=20"),
            Product("21", "Plug Smart", "Розетка.", 8990.0, 4.8f, "Гаджеттер", "${b}smart-plug?lock=21"),
            Product("22", "Smart Phone", "Смартфон", 399900.0, 4.6f, "Гаджеттер", "${b}phone?lock=22")
        )
    }
}
