package com.example.ru.yourname.fitcalculatormobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedIn: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        if (email.isBlank()) {
            setError("Введите email")
            return
        }

        if (password.isBlank()) {
            setError("Введите пароль")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isLoading = true
            )

            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                _uiState.value = AuthUiState(
                    isLoggedIn = true
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message
                        ?: "Не удалось выполнить вход"
                )
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (name.isBlank()) {
            setError("Введите имя")
            return
        }

        if (email.isBlank()) {
            setError("Введите email")
            return
        }

        if (password.isBlank()) {
            setError("Введите пароль")
            return
        }

        if (password != confirmPassword) {
            setError("Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isLoading = true
            )

            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password

                    data = buildJsonObject {
                        put("name", name)
                    }
                }

                _uiState.value = AuthUiState(
                    successMessage = "Регистрация выполнена"
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message
                        ?: "Не удалось создать аккаунт"
                )
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            setError("Введите email")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(
                isLoading = true
            )

            try {
                SupabaseClientProvider.client.auth.resetPasswordForEmail(
                    email = email
                )

                _uiState.value = AuthUiState(
                    successMessage = "Письмо для восстановления отправлено"
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message
                        ?: "Не удалось отправить письмо"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.signOut()

                _uiState.value = AuthUiState()

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    errorMessage = e.message
                        ?: "Не удалось выйти из аккаунта"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun setError(message: String) {
        _uiState.value = AuthUiState(
            errorMessage = message
        )
    }
}