package com.rfid.rfidreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.rfid.rfidreader.ui.LoginScreen
import com.rfid.rfidreader.ui.TryOnRoute
import com.rfid.rfidreader.ui.TryOnViewModel
import com.rfid.rfidreader.ui.theme.RFIDREADERTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<TryOnViewModel> {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TryOnViewModel(
                    repository = com.rfid.rfidreader.data.TryOnRepository.create(),
                    context = this@MainActivity
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RFIDREADERTheme {
                var isLoggedIn by remember { mutableStateOf(false) }

                if (!isLoggedIn) {
                    LoginScreen {
                        isLoggedIn = true
                    }
                } else {
                    TryOnRoute(viewModel = viewModel)
                }
            }
        }
    }
}
