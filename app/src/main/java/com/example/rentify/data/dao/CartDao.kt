package com.example.rentify.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rentify.data.entity.CartEntity
import com.example.rentify.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class CartItemWithProduct(
    val cartId: Int,
    val userId: Int,
    val productId: Int,
    val rentDays: Int,
    val productTitle: String,
    val productCategory: String,
    val productPricePerDay: Double,
    val productDescription: String
)

@Dao
interface CartDao {
    @Query("""
        SELECT c.id as cartId, c.userId, c.productId, c.rentDays, 
               p.title as productTitle, p.category as productCategory, 
               p.pricePerDay as productPricePerDay, p.description as productDescription 
        FROM cart_items c 
        INNER JOIN products p ON c.productId = p.id 
        WHERE c.userId = :userId
    """)
    fun getCartWithProducts(userId: Int): Flow<List<CartItemWithProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    @Update
    suspend fun updateCartItem(cartItem: CartEntity)

    @Query("UPDATE cart_items SET rentDays = :rentDays WHERE id = :cartId")
    suspend fun updateRentDays(cartId: Int, rentDays: Int)

    @Delete
    suspend fun deleteCartItem(cartItem: CartEntity)

    @Query("DELETE FROM cart_items WHERE id = :cartId")
    suspend fun deleteCartItemById(cartId: Int)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Int)

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItemByProduct(userId: Int, productId: Int): CartEntity?
}

