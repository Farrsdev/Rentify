package com.example.rentify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rentify.data.dao.CartDao
import com.example.rentify.data.dao.ProductDao
import com.example.rentify.data.dao.TransactionDao
import com.example.rentify.data.dao.UserDao
import com.example.rentify.data.entity.CartEntity
import com.example.rentify.data.entity.ProductEntity
import com.example.rentify.data.entity.TransactionEntity
import com.example.rentify.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, ProductEntity::class, CartEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Rentify_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.userDao(), database.productDao())
                }
            }
        }

        suspend fun populateDatabase(userDao: UserDao, productDao: ProductDao) {
            // Seed Admin account
            userDao.insertUser(
                UserEntity(
                    username = "admin",
                    password = "admin", // simple password for testing/local
                    role = "ADMIN"
                )
            )
            // Seed Buyer account
            userDao.insertUser(
                UserEntity(
                    username = "buyer",
                    password = "buyer",
                    role = "BUYER"
                )
            )

            // Seed initial products
            productDao.insertProduct(
                ProductEntity(
                    title = "Adobe Photoshop 2026",
                    category = "APP",
                    pricePerDay = 15000.0,
                    description = "Professional photo and image design software for creative designers."
                )
            )
            productDao.insertProduct(
                ProductEntity(
                    title = "Microsoft Office 365",
                    category = "APP",
                    pricePerDay = 10000.0,
                    description = "Suite of productivity applications including Word, Excel, PowerPoint."
                )
            )
            productDao.insertProduct(
                ProductEntity(
                    title = "Cyberpunk 2077: Phantom Liberty",
                    category = "GAME",
                    pricePerDay = 25000.0,
                    description = "Action-adventure RPG set in the dark future of Night City."
                )
            )
            productDao.insertProduct(
                ProductEntity(
                    title = "FIFA 26 / FC 26",
                    category = "GAME",
                    pricePerDay = 20000.0,
                    description = "Ultimate association football simulation game by EA Sports."
                )
            )
        }
    }
}

