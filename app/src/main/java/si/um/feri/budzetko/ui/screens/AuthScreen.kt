package si.um.feri.budzetko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import si.um.feri.budzetko.viewmodel.AuthViewModel

private enum class AuthMode {
    LOGIN,
    REGISTER,
    RESET
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {

    val currentUser by authViewModel.currentUser.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var mode by remember {
        mutableStateOf(AuthMode.LOGIN)
    }

    var email by remember {
        mutableStateOf("")
    }

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F3E8))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(90.dp))

            Text(
                text = "Budžetko",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Prijavi se"
                    AuthMode.REGISTER -> "Registriraj se"
                    AuthMode.RESET -> "Ponastavi geslo"
                },

                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color.Black,

                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(18.dp))

            AuthField(
                value = email,
                onValueChange = {
                    email = it
                },
                label = "Email"
            )

            if (mode == AuthMode.REGISTER) {

                Spacer(modifier = Modifier.height(14.dp))

                AuthField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = "Uporabniško ime"
                )
            }

            if (mode != AuthMode.RESET) {

                Spacer(modifier = Modifier.height(14.dp))

                AuthField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = "Geslo",
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {

                    when (mode) {

                        AuthMode.LOGIN -> {

                            authViewModel.login(
                                email = email,
                                password = password
                            )
                        }

                        AuthMode.REGISTER -> {

                            if (
                                email.isNotBlank() &&
                                username.isNotBlank() &&
                                password.length >= 6
                            ) {

                                authViewModel.register(
                                    email = email,
                                    password = password
                                )
                            }
                        }

                        AuthMode.RESET -> {
                        }
                    }
                },

                enabled = !isLoading,

                modifier = Modifier
                    .width(160.dp)
                    .height(48.dp)
                    .align(Alignment.Start),

                shape = RoundedCornerShape(14.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                )
            ) {

                Text(
                    text =
                        if (isLoading) {
                            "Počakaj..."
                        } else {
                            when (mode) {
                                AuthMode.LOGIN -> "Prijavi"
                                AuthMode.REGISTER -> "Registriraj"
                                AuthMode.RESET -> "Pošlji"
                            }
                        },

                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (mode == AuthMode.LOGIN) {

                Text(
                    text = "Pozabljeno geslo",

                    fontSize = 12.sp,
                    color = Color.Gray,

                    modifier = Modifier
                        .align(Alignment.Start)
                        .clickable {
                            mode = AuthMode.RESET
                        }
                )
            }

            Spacer(modifier = Modifier.height(170.dp))

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Registriraj se"
                    AuthMode.REGISTER -> "Vpiši se"
                    AuthMode.RESET -> "Vpiši se"
                },

                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,

                modifier = Modifier.clickable {

                    mode = when (mode) {

                        AuthMode.LOGIN -> AuthMode.REGISTER

                        AuthMode.REGISTER -> AuthMode.LOGIN

                        AuthMode.RESET -> AuthMode.LOGIN
                    }
                }
            )

            error?.let {

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = it,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {

    OutlinedTextField(
        value = value,

        onValueChange = onValueChange,

        label = {
            Text(
                text = label,
                color = Color.Black
            )
        },

        singleLine = true,

        visualTransformation =
            if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },

        keyboardOptions =
            if (isPassword) {

                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrectEnabled = false
                )

            } else {

                KeyboardOptions.Default
            },

        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),

        shape = RoundedCornerShape(14.dp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black,
            focusedLabelColor = Color.Black,
            unfocusedLabelColor = Color.Black,
            cursorColor = Color.Black,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}