package com.example.voltstore.data

import android.content.Context
import com.example.voltstore.data.repository.LocalRepository

object FirebaseService {
    private var _repository: LocalRepository? = null
    
    val repository: LocalRepository
        get() = _repository ?: throw IllegalStateException("FirebaseService must be initialized with context first")

    fun init(context: Context) {
        if (_repository == null) {
            _repository = LocalRepository(context.applicationContext)
        }
    }
}
