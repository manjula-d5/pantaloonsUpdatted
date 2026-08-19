package com.rfid.rfidreader.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rfid.rfidreader.data.SessionManager
import com.rfid.rfidreader.data.TryOnRepository
import com.rfid.rfidreader.data.api.LoginResponse
import com.rfid.rfidreader.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(
    private val repository: TryOnRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(usernameOrEmail: String, password: String) {
        AppLogger.log("Login attempt for user: $usernameOrEmail")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val response = repository.login(usernameOrEmail, password)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == 200 && body.data != null) {
                        AppLogger.log("Login successful for: $usernameOrEmail")
                        sessionManager?.let {
                            it.authToken = body.data.accessToken
                            it.storeId = body.data.user?.storeId
                            it.trialRoomId = body.data.user?.trialRoomId ?: -1
                            it.trialRoomName = body.data.user?.trialRoomName
                            it.isLoggedIn = true
                        }
                        _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                    } else {
                        AppLogger.log("Login failed: ${body?.message}")
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                errorMessage = body?.message ?: "Login failed"
                            ) 
                        }
                    }
                } else {
                    // Extract message from error body (e.g. 401 Unauthorized)
                    val errorJson = response.errorBody()?.string()
                    val errorResponse = runCatching { 
                        Gson().fromJson(errorJson, LoginResponse::class.java) 
                    }.getOrNull()
                    
                    AppLogger.log("Login error (HTTP ${response.code()}): ${errorResponse?.message}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = errorResponse?.message ?: "Invalid username or password"
                        ) 
                    }
                }
            } catch (e: Exception) {
                AppLogger.logError("Login exception", e)
                val friendlyMessage = when (e) {
                    is java.net.ConnectException,
                    is java.net.SocketTimeoutException,
                    is java.net.UnknownHostException -> "SERVER IS SLOW"
                    else -> "Network error. Please check your connection."
                }
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = friendlyMessage
                    ) 
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(
                repository = TryOnRepository.create(context),
                sessionManager = SessionManager(context)
            ) as T
        }
    }
}
