package com.rfid.rfidreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rfid.rfidreader.data.SessionManager
import com.rfid.rfidreader.ui.LoginScreen
import com.rfid.rfidreader.ui.TryOnRoute
import com.rfid.rfidreader.ui.TryOnViewModel
import com.rfid.rfidreader.ui.theme.RFIDREADERTheme
import com.rfid.rfidreader.util.AppLogger

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<TryOnViewModel> {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                AppLogger.log("Creating TryOnViewModel in MainActivity")
                return TryOnViewModel(
                    repository = com.rfid.rfidreader.data.TryOnRepository.create(this@MainActivity),
                    sessionManager = SessionManager(this@MainActivity),
                    context = this@MainActivity
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.init(this)
        AppLogger.log("App onCreate - RFIDREADER started")
        
        val sessionManager = SessionManager(this)

        setContent {
            RFIDREADERTheme {
                var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn) }

                if (!isLoggedIn) {
                    AppLogger.log("User not logged in, showing LoginScreen")
                    LoginScreen {
                        AppLogger.log("Login successful callback received in MainActivity")
                        sessionManager.isLoggedIn = true
                        isLoggedIn = true
                        viewModel.retryNow() // Refresh data immediately after login
                    }
                } else {
                    AppLogger.log("User already logged in, showing TryOnRoute")
                    TryOnRoute(
                        viewModel = viewModel,
                        onLogout = {
                            AppLogger.log("Logout initiated")
                            sessionManager.logout()
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
}
