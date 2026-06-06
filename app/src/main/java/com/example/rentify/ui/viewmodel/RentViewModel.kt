package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.dao.CartItemWithProduct
import com.example.rentify.data.dao.TransactionWithDetails
import com.example.rentify.data.repository.RentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RentViewModel(private val rentRepository: RentRepository) : ViewModel() {

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun setUserId(userId: Int) {
        _currentUserId.value = userId
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val cartItems: StateFlow<List<CartItemWithProduct>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                rentRepository.getCartWithProducts(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTransactions: StateFlow<List<TransactionWithDetails>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId != null) {
                rentRepository.getTransactionsByUserId(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionWithDetails>> = rentRepository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addToCart(productId: Int, title: String) {
        val userId = _currentUserId.value ?: return
        viewModelScope.launch {
            try {
                rentRepository.addToCart(userId, productId, 1)
                _toastMessage.emit("'$title' berhasil dimasukkan ke keranjang!")
            } catch (e: Exception) {
                _toastMessage.emit("Gagal menambah ke keranjang: ${e.message}")
            }
        }
    }

    fun updateCartItemDays(cartId: Int, rentDays: Int) {
        if (rentDays <= 0) return
        viewModelScope.launch {
            try {
                rentRepository.updateCartItemDays(cartId, rentDays)
            } catch (e: Exception) {
                _toastMessage.emit("Gagal memperbarui durasi sewa: ${e.message}")
            }
        }
    }

    fun removeFromCart(cartId: Int) {
        viewModelScope.launch {
            try {
                rentRepository.removeFromCart(cartId)
                _toastMessage.emit("Item dihapus dari keranjang")
            } catch (e: Exception) {
                _toastMessage.emit("Gagal menghapus item: ${e.message}")
            }
        }
    }

    fun checkout(onSuccess: () -> Unit) {
        val userId = _currentUserId.value ?: return
        val currentCart = cartItems.value
        if (currentCart.isEmpty()) {
            viewModelScope.launch { _toastMessage.emit("Keranjang masih kosong!") }
            return
        }

        viewModelScope.launch {
            try {
                val success = rentRepository.checkout(userId, currentCart)
                if (success) {
                    _toastMessage.emit("Checkout berhasil! Permintaan peminjaman dikirim.")
                    onSuccess()
                } else {
                    _toastMessage.emit("Gagal melakukan checkout.")
                }
            } catch (e: Exception) {
                _toastMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun confirmRent(transactionId: Int, productTitle: String) {
        viewModelScope.launch {
            try {
                rentRepository.confirmTransaction(transactionId)
                _toastMessage.emit("Sewa untuk '$productTitle' berhasil dikonfirmasi!")
            } catch (e: Exception) {
                _toastMessage.emit("Gagal konfirmasi sewa: ${e.message}")
            }
        }
    }
}

