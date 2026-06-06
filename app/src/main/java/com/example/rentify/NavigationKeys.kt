package com.example.rentify

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Register : NavKey
@Serializable data object AdminDashboard : NavKey
@Serializable data object AdminConfirmRents : NavKey
@Serializable data object BuyerDashboard : NavKey
@Serializable data object Cart : NavKey
@Serializable data object RentHistory : NavKey


