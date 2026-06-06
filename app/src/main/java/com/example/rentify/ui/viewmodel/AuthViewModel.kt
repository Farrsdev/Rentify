package com.example.rentify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentify.data.entity.UserEntity
import com.example.rentify.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: UserEntity) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    fun login(username: String, password: String, onNavigate: (String) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            viewModelScope.launch { _toastMessage.emit("Username dan password tidak boleh kosong") }
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = userRepository.getUserByUsername(username)
                if (user != null && user.password == password) {
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                    _toastMessage.emit("Login berhasil sebagai ${user.role}!")
                    onNavigate(user.role)
                } else {
                    _uiState.value = AuthUiState.Error("Username atau password salah")
                    _toastMessage.emit("Username atau password salah")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Terjadi kesalahan")
                _toastMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun register(username: String, password: String, onRegisterSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            viewModelScope.launch { _toastMessage.emit("Username dan password tidak boleh kosong") }
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val newUser = UserEntity(username = username, password = password, role = "BUYER")
                val success = userRepository.registerUser(newUser)
                if (success) {
                    _uiState.value = AuthUiState.Idle
                    _toastMessage.emit("Registrasi berhasil! Silakan login.")
                    onRegisterSuccess()
                } else {
                    _uiState.value = AuthUiState.Error("Username sudah terdaftar")
                    _toastMessage.emit("Username sudah terdaftar")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Terjadi kesalahan")
                _toastMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun logout(onNavigateToLogin: () -> Unit) {
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
        onNavigateToLogin()
    }
}

