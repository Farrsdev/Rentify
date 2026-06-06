package com.example.rentify.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val productId: Int,
    val rentDays: Int,
    val totalPrice: Double,
    val status: String, // "PENDING" or "CONFIRMED"
    val rentDate: Long = System.currentTimeMillis()
)

