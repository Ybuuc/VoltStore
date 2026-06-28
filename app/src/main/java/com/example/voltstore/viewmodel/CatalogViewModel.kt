package com.example.voltstore.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voltstore.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CatalogViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadMockProducts()
    }

    private fun loadMockProducts() {
        // Используем сервис Lorem Flick для максимально стабильных тематических фото
        val baseUrl = "https://loremflickr.com/320/240/"
        
        _products.value = listOf(
            Product(1, "PowerBank 20k", "Қуатты сыртқы аккумулятор.", 24990.0, 4.8f, "Аксессуарлар", "${baseUrl}powerbank?lock=1"),
            Product(2, "Buds Pro", "Шуды басатын құлаққаптар.", 45990.0, 4.9f, "Аудио", "${baseUrl}earbuds?lock=2"),
            Product(3, "GaN 65W", "Шағын зарядтау құрылғысы.", 15500.0, 4.7f, "Зарядтау", "${baseUrl}charger?lock=3"),
            Product(4, "Watch S1", "Стильді смарт-сағаттар.", 89990.0, 4.6f, "Смарт-сағат", "${baseUrl}smartwatch?lock=4"),
            Product(5, "Cable 2m", "Берік USB-C кабелі.", 4990.0, 4.5f, "Кабельдер", "${baseUrl}usb-cable?lock=5"),
            Product(6, "Stand", "Сымсыз зарядтау тіреуіші.", 12500.0, 4.7f, "Зарядтау", "${baseUrl}wireless-charger?lock=6"),
            Product(7, "Hub 7-in-1", "USB-C хабы.", 18990.0, 4.8f, "Аксессуарлар", "${baseUrl}usb-hub?lock=7"),
            Product(8, "Mouse", "Сымсыз тышқан.", 9990.0, 4.7f, "Аксессуарлар", "${baseUrl}mouse,computer?lock=8"),
            Product(9, "Speaker Go", "Портативті колонка.", 21500.0, 4.9f, "Аудио", "${baseUrl}speaker,bluetooth?lock=9"),
            Product(10, "Headset Pro", "Кәсіби құлаққаптар.", 35990.0, 4.8f, "Аудио", "${baseUrl}headphones?lock=10"),
            Product(11, "Case Ultra", "Қорғаныс қапшығы.", 3500.0, 4.4f, "Аксессуарлар", "${baseUrl}phone-case?lock=11"),
            Product(12, "Light Led", "Ақылды жарық.", 14990.0, 4.7f, "Гаджеттер", "${baseUrl}led-lamp?lock=12"),
            Product(13, "Keyboard K1", "Механикалық пернетақта.", 42000.0, 4.9f, "Гаджеттер", "${baseUrl}keyboard,gaming?lock=13"),
            Product(14, "Fan Mini", "Портативті желдеткіш.", 2990.0, 4.3f, "Аксессуарлар", "${baseUrl}mini-fan?lock=14"),
            Product(15, "Pad XL", "Ойын төсеніші.", 7500.0, 4.8f, "Аксессуарлар", "${baseUrl}mousepad?lock=15"),
            Product(16, "Screen Pro", "Тазалау жинағы.", 3200.0, 4.6f, "Күтім", "${baseUrl}cleaning-kit?lock=16"),
            Product(17, "Tripod Go", "Икемді штатив.", 12990.0, 4.5f, "Фото", "${baseUrl}tripod?lock=17"),
            Product(18, "Mic Stream", "Стримингке арналған микрофон.", 55000.0, 4.9f, "Аудио", "${baseUrl}microphone?lock=18"),
            Product(19, "Bag Volt", "Гаджеттерге арналған сөмке.", 18500.0, 4.7f, "Күтім", "${baseUrl}tech-bag?lock=19"),
            Product(20, "Clip On", "Оқуға арналған шам.", 4500.0, 4.4f, "Гаджеттер", "${baseUrl}clip-light?lock=20"),
            Product(21, "Plug Smart", "Ақылды розетка.", 8990.0, 4.8f, "Гаджеттер", "${baseUrl}smart-plug?lock=21")
        )
    }
}
