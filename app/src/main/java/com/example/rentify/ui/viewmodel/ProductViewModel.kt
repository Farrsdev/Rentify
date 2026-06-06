package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.entity.ProductEntity
import com.example.rentify.data.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = _searchQuery
        .flatMapLatest { query ->
            productRepository.searchProducts(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(title: String, category: String, pricePerDay: Double, description: String, onSuccess: () -> Unit) {
        if (title.isBlank() || description.isBlank() || pricePerDay <= 0.0) {
            viewModelScope.launch { _toastMessage.emit("Data produk tidak valid atau belum lengkap") }
            return
        }

        viewModelScope.launch {
            try {
                val newProduct = ProductEntity(
                    title = title,
                    category = category,
                    pricePerDay = pricePerDay,
                    description = description
                )
                productRepository.addProduct(newProduct)
                _toastMessage.emit("Produk '$title' berhasil ditambahkan!")
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.emit("Gagal menambahkan produk: ${e.message}")
            }
        }
    }

    fun updateProduct(id: Int, title: String, category: String, pricePerDay: Double, description: String, onSuccess: () -> Unit) {
        if (title.isBlank() || description.isBlank() || pricePerDay <= 0.0) {
            viewModelScope.launch { _toastMessage.emit("Data produk tidak valid atau belum lengkap") }
            return
        }

        viewModelScope.launch {
            try {
                val updatedProduct = ProductEntity(
                    id = id,
                    title = title,
                    category = category,
                    pricePerDay = pricePerDay,
                    description = description
                )
                productRepository.updateProduct(updatedProduct)
                _toastMessage.emit("Produk '$title' berhasil diperbarui!")
                onSuccess()
            } catch (e: Exception) {
                _toastMessage.emit("Gagal memperbarui produk: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                _toastMessage.emit("Produk '${product.title}' berhasil dihapus!")
            } catch (e: Exception) {
                _toastMessage.emit("Gagal menghapus produk: ${e.message}")
            }
        }
    }
}

