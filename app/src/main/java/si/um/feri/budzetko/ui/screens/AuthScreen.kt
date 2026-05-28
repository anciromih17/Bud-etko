package si.um.feri.budzetko.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import si.um.feri.budzetko.R
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSurface
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
    val message by authViewModel.message.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(budzetkoBackground())
            .clipToBounds()
    ) {
        AuthBackgroundAccents(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 34.dp, vertical = 18.dp)
                .padding(bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = budzetkoInk()
            )

            Spacer(
                modifier = Modifier.height(
                    when (mode) {
                        AuthMode.LOGIN -> 108.dp
                        AuthMode.REGISTER -> 82.dp
                        AuthMode.RESET -> 126.dp
                    }
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = mode.title(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = budzetkoInk()
                )

                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.profile_email),
                    keyboardType = KeyboardType.Email
                )

                if (mode == AuthMode.REGISTER) {
                    AuthField(
                        value = username,
                        onValueChange = { username = it },
                        label = stringResource(R.string.profile_username)
                    )
                }

                if (mode != AuthMode.RESET) {
                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        isPassword = true,
                        keyboardType = KeyboardType.Password
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        when (mode) {
                            AuthMode.LOGIN -> authViewModel.login(email = email, password = password)
                            AuthMode.REGISTER -> authViewModel.register(
                                email = email,
                                username = username,
                                password = password
                            )

                            AuthMode.RESET -> authViewModel.resetPassword(email)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth(0.58f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BudzetkoPurple,
                        contentColor = Color.White,
                        disabledContainerColor = BudzetkoPurple.copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.78f)
                    )
                ) {
                    Text(
                        text = if (isLoading) stringResource(R.string.auth_wait) else mode.primaryAction(),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (mode == AuthMode.LOGIN) {
                    TextButton(
                        onClick = { mode = AuthMode.RESET },
                        colors = ButtonDefaults.textButtonColors(contentColor = budzetkoMutedInk())
                    ) {
                        Text(
                            text = stringResource(R.string.auth_forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                error?.let { AuthMessage(text = it, color = Color(0xFFB3261E)) }
                message?.let { AuthMessage(text = it, color = Color(0xFF167C72)) }
            }
        }

        Text(
            text = mode.secondaryAction(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = BudzetkoPurple,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 34.dp, bottom = 34.dp)
                .clickable {
                    mode = when (mode) {
                        AuthMode.LOGIN -> AuthMode.REGISTER
                        AuthMode.REGISTER -> AuthMode.LOGIN
                        AuthMode.RESET -> AuthMode.LOGIN
                    }
                }
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = budzetkoInk()
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                autoCorrectEnabled = false
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BudzetkoPurple,
                unfocusedBorderColor = budzetkoInk().copy(alpha = 0.80f),
                cursorColor = BudzetkoPurple,
                focusedTextColor = budzetkoInk(),
                unfocusedTextColor = budzetkoInk(),
                focusedContainerColor = budzetkoSurface().copy(alpha = 0.52f),
                unfocusedContainerColor = budzetkoSurface().copy(alpha = 0.38f)
            )
        )
    }
}

@Composable
private fun AuthMessage(
    text: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AuthBackgroundAccents(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawOval(
            color = BudzetkoPurple.copy(alpha = 0.07f),
            topLeft = Offset(size.width * 0.62f, size.height * 0.72f),
            size = Size(size.width * 0.52f, size.height * 0.24f)
        )
        drawOval(
            color = BudzetkoLime.copy(alpha = 0.18f),
            topLeft = Offset(size.width * -0.18f, size.height * 0.12f),
            size = Size(size.width * 0.34f, size.width * 0.34f)
        )
    }
}

@Composable
private fun AuthMode.title(): String {
    return when (this) {
        AuthMode.LOGIN -> stringResource(R.string.auth_login_title)
        AuthMode.REGISTER -> stringResource(R.string.auth_register_title)
        AuthMode.RESET -> stringResource(R.string.auth_reset_title)
    }
}

@Composable
private fun AuthMode.primaryAction(): String {
    return when (this) {
        AuthMode.LOGIN -> stringResource(R.string.auth_login_action)
        AuthMode.REGISTER -> stringResource(R.string.auth_register_action)
        AuthMode.RESET -> stringResource(R.string.auth_reset_action)
    }
}

@Composable
private fun AuthMode.secondaryAction(): String {
    return when (this) {
        AuthMode.LOGIN -> stringResource(R.string.auth_go_register)
        AuthMode.REGISTER -> stringResource(R.string.auth_go_login)
        AuthMode.RESET -> stringResource(R.string.auth_go_login)
    }
}
