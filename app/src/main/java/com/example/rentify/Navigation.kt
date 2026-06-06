package com.example.rentify

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.rentify.ui.screen.*
import com.example.rentify.ui.viewmodel.AuthViewModel
import com.example.rentify.ui.viewmodel.ProductViewModel
import com.example.rentify.ui.viewmodel.RentViewModel

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val app = context.applicationContext as RentifyApplication

  // Instantiate ViewModels with Factories linking to Application dependencies
  val authViewModel: AuthViewModel = viewModel { AuthViewModel(app.userRepository) }
  val productViewModel: ProductViewModel = viewModel { ProductViewModel(app.productRepository) }
  val rentViewModel: RentViewModel = viewModel { RentViewModel(app.rentRepository) }

  // Handle toast notifications globally from ViewModels
  LaunchedEffect(Unit) {
    authViewModel.toastMessage.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
  }
  LaunchedEffect(Unit) {
    productViewModel.toastMessage.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
  }
  LaunchedEffect(Unit) {
    rentViewModel.toastMessage.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
  }

  val backStack = rememberNavBackStack(Login)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(
            authViewModel = authViewModel,
            onNavigateToRegister = { backStack.add(Register) },
            onLoginSuccess = { role ->
              val currentUser = authViewModel.currentUser.value
              if (currentUser != null) {
                rentViewModel.setUserId(currentUser.id)
              }
              if (role == "ADMIN") {
                backStack.add(AdminDashboard)
              } else {
                backStack.add(BuyerDashboard)
              }
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<Register> {
          RegisterScreen(
            authViewModel = authViewModel,
            onNavigateToLogin = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<AdminDashboard> {
          AdminDashboardScreen(
            productViewModel = productViewModel,
            authViewModel = authViewModel,
            onNavigateToConfirmRents = { backStack.add(AdminConfirmRents) },
            onLogout = {
              backStack.clear()
              backStack.add(Login)
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<AdminConfirmRents> {
          AdminConfirmRentScreen(
            rentViewModel = rentViewModel,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<BuyerDashboard> {
          BuyerDashboardScreen(
            productViewModel = productViewModel,
            rentViewModel = rentViewModel,
            authViewModel = authViewModel,
            onNavigateToCart = { backStack.add(Cart) },
            onNavigateToHistory = { backStack.add(RentHistory) },
            onLogout = {
              backStack.clear()
              backStack.add(Login)
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<Cart> {
          CartScreen(
            rentViewModel = rentViewModel,
            onBack = { backStack.removeLastOrNull() },
            onCheckoutSuccess = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<RentHistory> {
          RentHistoryScreen(
            rentViewModel = rentViewModel,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
      },
  )
}


