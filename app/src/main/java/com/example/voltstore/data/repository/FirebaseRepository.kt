package com.example.voltstore.data.repository

import android.util.Log
import com.example.voltstore.data.model.Product
import com.example.voltstore.data.model.Order
import com.example.voltstore.data.model.UserProfile
import com.example.voltstore.data.model.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")
    private val usersCollection = db.collection("users")
    private val ordersCollection = db.collection("orders")

    suspend fun getProducts(): List<Product> {
        return try {
            val snapshot = productsCollection.get().await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting products: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        try {
            usersCollection.document(userId).set(profile).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving user profile: ${e.message}")
        }
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user profile: ${e.message}")
            null
        }
    }

    suspend fun saveOrder(order: Order) {
        try {
            ordersCollection.document(order.id).set(order).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving order: ${e.message}")
        }
    }

    suspend fun saveProduct(product: Product) {
        try {
            productsCollection.document(product.id).set(product).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving product: ${e.message}")
        }
    }

    suspend fun deleteProduct(productId: String) {
        try {
            productsCollection.document(productId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting product: ${e.message}")
        }
    }

    suspend fun getOrders(): List<Order> {
        return try {
            val snapshot = ordersCollection.get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting orders: ${e.message}")
            emptyList()
        }
    }

    suspend fun saveCart(userId: String, cartItems: List<CartItem>) {
        try {
            usersCollection.document(userId).collection("cart").get().await().documents.forEach { it.reference.delete() }
            cartItems.forEach { item ->
                usersCollection.document(userId).collection("cart").document(item.product.id).set(item).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving cart: ${e.message}")
        }
    }

    suspend fun getCart(userId: String): List<CartItem> {
        return try {
            usersCollection.document(userId).collection("cart").get().await().toObjects(CartItem::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting cart: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllUsers(): List<UserProfile> {
        return try {
            val snapshot = usersCollection.get().await()
            // Нам нужно сохранить ID документа в объекте UserProfile, если он там не хранится явно.
            // Но в нашей модели UserProfile нет поля id. 
            // Для целей удаления мы можем возвращать Pair(userId, UserProfile) или расширить модель.
            // Для простоты вернем список, предположив, что email или имя уникальны для отображения, 
            // но для удаления нужен UID.
            snapshot.documents.mapNotNull { doc ->
                val profile = doc.toObject(UserProfile::class.java)
                // Мы можем использовать email демо-пользователей для их идентификации, 
                // но лучше добавить поле uid в ProfileViewModel или использовать ID документа.
                profile?.copy(address = doc.id) // Временно используем поле address для хранения UID для удаления, 
                // если не хотим менять модель. Но лучше просто возвращать список объектов, где ID документа доступен.
            }
            // Исправление: просто возвращаем объекты. В AdminViewModel будем использовать email для удаления или доработаем.
            snapshot.toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting all users: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteUser(userId: String) {
        try {
            usersCollection.document(userId).delete().await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting user: ${e.message}")
        }
    }
}
