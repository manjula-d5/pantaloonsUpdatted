package com.rfid.rfidreader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rfid.rfidreader.R
import com.rfid.rfidreader.ui.theme.RFIDREADERTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory(LocalContext.current)),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LoginContent(
        uiState = uiState,
        onLogin = viewModel::login,
        onLoginSuccess = onLoginSuccess,
        resetSuccess = viewModel::resetSuccess
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLogin: (String, String) -> Unit,
    onLoginSuccess: () -> Unit,
    resetSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf("") }
    
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage ?: localErrorMessage
    
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
            resetSuccess()
        }
    }

    // Detect screen configuration for responsive layout
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val cardVerticalPadding = if (isLandscape) 32.dp else 40.dp
    val cardHorizontalPadding = if (isLandscape) 40.dp else 36.dp
    val itemSpacing = if (isLandscape) 18.dp else 22.dp
    val logoHeight = if (isLandscape) 48.dp else 60.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1BB8B4)),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        Box(
            modifier = Modifier
                .size(700.dp)
                .offset(x = (-180).dp, y = (-220).dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 140.dp, y = (-300).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(720.dp)
                .offset(x = (-220).dp, y = 380.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )

        // Login card - responsive and properly proportioned across all screen resolutions
        Card(
            modifier = Modifier
                .fillMaxWidth(if (isLandscape) 0.48f else 0.86f)
                .widthIn(min = 340.dp, max = 560.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cardHorizontalPadding, vertical = cardVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                // Pantaloons Logo Image
                Image(
                    painter = painterResource(id = R.drawable.pantaloons1),
                    contentDescription = "Pantaloons Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.60f)
                        .height(logoHeight),
                    contentScale = ContentScale.Fit
                )

                // Welcome header text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Sign in to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        localErrorMessage = ""
                    },
                    label = { Text("Username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username",
                            tint = Color(0xFF1BB8B4)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1BB8B4),
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedLabelColor = Color(0xFF1BB8B4),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localErrorMessage = ""
                    },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = Color(0xFF1BB8B4)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible) android.R.drawable.ic_menu_view
                                    else android.R.drawable.ic_secure
                                ),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1BB8B4),
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedLabelColor = Color(0xFF1BB8B4),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            val cleanUser = username.trim()
                            val cleanPass = password.trim()
                            if (cleanUser.isNotBlank() && cleanPass.isNotBlank()) {
                                onLogin(cleanUser, cleanPass)
                            }
                        }
                    )
                )

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Login button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val cleanUser = username.trim()
                        val cleanPass = password.trim()
                        if (cleanUser.isBlank()) {
                            localErrorMessage = "Please enter username"
                        } else if (cleanPass.isBlank()) {
                            localErrorMessage = "Please enter password"
                        } else {
                            onLogin(cleanUser, cleanPass)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1BB8B4),
                        disabledContainerColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Footer text
                Text(
                    text = "For authorized personnel only",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    RFIDREADERTheme(dynamicColor = false) {
        LoginContent(
            uiState = LoginUiState(),
            onLogin = { _, _ -> },
            onLoginSuccess = {},
            resetSuccess = {}
        )
    }
}

