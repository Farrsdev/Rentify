package com.example.rentify

import android.app.Application
import com.example.rentify.data.AppDatabase
import com.example.rentify.data.repository.ProductRepository
import com.example.rentify.data.repository.RentRepository
import com.example.rentify.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class RentifyApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    
    val userRepository by lazy { UserRepository(database.userDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val rentRepository by lazy { RentRepository(database.cartDao(), database.transactionDao()) }
}

