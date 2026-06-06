package com.example.rentify.data.repository

import com.example.rentify.data.dao.CartDao
import com.example.rentify.data.dao.CartItemWithProduct
import com.example.rentify.data.dao.TransactionDao
import com.example.rentify.data.dao.TransactionWithDetails
import com.example.rentify.data.entity.CartEntity
import com.example.rentify.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class RentRepository(
    private val cartDao: CartDao,
    private val transactionDao: TransactionDao
) {
    fun getCartWithProducts(userId: Int): Flow<List<CartItemWithProduct>> {
        return cartDao.getCartWithProducts(userId)
    }

    suspend fun addToCart(userId: Int, productId: Int, rentDays: Int = 1) {
        val existing = cartDao.getCartItemByProduct(userId, productId)
        if (existing != null) {
            cartDao.updateRentDays(existing.id, existing.rentDays + rentDays)
        } else {
            cartDao.insertCartItem(CartEntity(userId = userId, productId = productId, rentDays = rentDays))
        }
    }

    suspend fun updateCartItemDays(cartId: Int, rentDays: Int) {
        cartDao.updateRentDays(cartId, rentDays)
    }

    suspend fun removeFromCart(cartId: Int) {
        cartDao.deleteCartItemById(cartId)
    }

    suspend fun checkout(userId: Int, cartItems: List<CartItemWithProduct>): Boolean {
        if (cartItems.isEmpty()) return false
        
        for (item in cartItems) {
            val totalPrice = item.productPricePerDay * item.rentDays
            transactionDao.insertTransaction(
                TransactionEntity(
                    userId = userId,
                    productId = item.productId,
                    rentDays = item.rentDays,
                    totalPrice = totalPrice,
                    status = "PENDING"
                )
            )
        }
        cartDao.clearCart(userId)
        return true
    }

    fun getAllTransactions(): Flow<List<TransactionWithDetails>> {
        return transactionDao.getAllTransactionsWithDetails()
    }

    fun getTransactionsByUserId(userId: Int): Flow<List<TransactionWithDetails>> {
        return transactionDao.getTransactionsByUserId(userId)
    }

    suspend fun confirmTransaction(transactionId: Int) {
        transactionDao.confirmTransaction(transactionId)
    }
}

