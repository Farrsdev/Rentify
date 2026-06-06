package com.example.rentify.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rentify.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TransactionWithDetails(
    val id: Int,
    val userId: Int,
    val username: String,
    val productId: Int,
    val productTitle: String,
    val productCategory: String,
    val rentDays: Int,
    val totalPrice: Double,
    val status: String,
    val rentDate: Long
)

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.userId, u.username, t.productId, p.title as productTitle, 
               p.category as productCategory, t.rentDays, t.totalPrice, t.status, t.rentDate 
        FROM transactions t 
        INNER JOIN users u ON t.userId = u.id 
        INNER JOIN products p ON t.productId = p.id 
        ORDER BY t.rentDate DESC
    """)
    fun getAllTransactionsWithDetails(): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.id, t.userId, u.username, t.productId, p.title as productTitle, 
               p.category as productCategory, t.rentDays, t.totalPrice, t.status, t.rentDate 
        FROM transactions t 
        INNER JOIN users u ON t.userId = u.id 
        INNER JOIN products p ON t.productId = p.id 
        WHERE t.userId = :userId 
        ORDER BY t.rentDate DESC
    """)
    fun getTransactionsByUserId(userId: Int): Flow<List<TransactionWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = 'CONFIRMED' WHERE id = :transactionId")
    suspend fun confirmTransaction(transactionId: Int)
}

