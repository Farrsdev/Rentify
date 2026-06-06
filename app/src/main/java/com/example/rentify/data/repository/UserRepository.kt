package com.example.rentify.data.repository

import com.example.rentify.data.dao.UserDao
import com.example.rentify.data.entity.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun registerUser(user: UserEntity): Boolean {
        val existing = userDao.getUserByUsername(user.username)
        if (existing != null) return false
        val id = userDao.insertUser(user)
        return id > 0
    }

    suspend fun initDefaultAdmin() {
        if (userDao.getUserCount() == 0) {
            userDao.insertUser(
                UserEntity(
                    username = "admin",
                    password = "admin",
                    role = "ADMIN"
                )
            )
        }
    }
}

