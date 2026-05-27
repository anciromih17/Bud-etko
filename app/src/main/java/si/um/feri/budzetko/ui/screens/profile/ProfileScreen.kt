package si.um.feri.budzetko.ui.screens.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.AppCurrency
import si.um.feri.budzetko.AppLanguage
import si.um.feri.budzetko.AppThemeMode
import si.um.feri.budzetko.R
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoBorder
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSoftAccent
import si.um.feri.budzetko.ui.theme.budzetkoSurface
import si.um.feri.budzetko.viewmodel.SyncStatusUi
import si.um.feri.budzetko.viewmodel.SyncUiState
import si.um.feri.budzetko.viewmodel.SyncViewModel
import si.um.feri.budzetko.viewmodel.ProfileEditState
import si.um.feri.budzetko.viewmodel.UserUiState
import si.um.feri.budzetko.viewmodel.UserViewModel

private val CardSurface: Color
    @Composable get() = budzetkoSurface()
private val PrimaryAccent = Color(0xFF6B4EFF)
private val SecondaryAccent: Color
    @Composable get() = budzetkoSoftAccent()
private val LimeAccent = Color(0xFFD9FF6C)
private val SoftBorder: Color
    @Composable get() = budzetkoBorder()
private val Ink: Color
    @Composable get() = budzetkoInk()
private val MutedInk: Color
    @Composable get() = budzetkoMutedInk()

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    syncViewModel: SyncViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    appLanguage: AppLanguage = AppLanguage.SLOVENIAN,
    onLanguageChange: (AppLanguage) -> Unit = {},
    appCurrency: AppCurrency = AppCurrency.EUR,
    onCurrencyChange: (AppCurrency) -> Unit = {},
    appThemeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeChange: (AppThemeMode) -> Unit = {},
    onDeleteAccountClick: (String) -> Unit = {},
    onPickProfileImage: (((String) -> Unit) -> Unit) = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncUiState by syncViewModel.uiState.collectAsState()
    val profileEditState by viewModel.profileEditState.collectAsState()

    LaunchedEffect(Unit) {
        syncViewModel.refreshLocalSyncStatus()
    }

    ProfileContent(
        uiState = uiState,
        syncUiState = syncUiState,
        profileEditState = profileEditState,
        onSyncClick = syncViewModel::syncNow,
        onUpdateProfile = viewModel::updateProfile,
        onClearProfileEditMessage = viewModel::clearProfileEditMessage,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onTransactionsClick = onTransactionsClick,
        onAddExpenseClick = onAddExpenseClick,
        onAnalyticsClick = onAnalyticsClick,
        onSettingsClick = onSettingsClick,
        appLanguage = appLanguage,
        onLanguageChange = onLanguageChange,
        appCurrency = appCurrency,
        onCurrencyChange = onCurrencyChange,
        appThemeMode = appThemeMode,
        onThemeChange = onThemeChange,
        onDeleteAccountClick = onDeleteAccountClick,
        onPickProfileImage = onPickProfileImage,
        modifier = modifier
    )
}

@Composable
private fun ProfileContent(
    uiState: UserUiState,
    syncUiState: SyncUiState,
    profileEditState: ProfileEditState,
    onSyncClick: () -> Unit,
    onUpdateProfile: (username: String, email: String, currentPassword: String, newPassword: String) -> Unit,
    onClearProfileEditMessage: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    appLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    appCurrency: AppCurrency,
    onCurrencyChange: (AppCurrency) -> Unit,
    appThemeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    onDeleteAccountClick: (String) -> Unit,
    onPickProfileImage: (((String) -> Unit) -> Unit),
    modifier: Modifier = Modifier
) {
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("budzetko_profile", 0) }
    var profileImageUri by remember { mutableStateOf(profilePrefs.getString("profile_image_uri", null)) }
    LaunchedEffect(profileEditState.successMessage) {
        if (profileEditState.successMessage != null && isEditDialogOpen) {
            isEditDialogOpen = false
            onClearProfileEditMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = budzetkoBackground(),
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = onHomeClick,
                onBudgetClick = onTransactionsClick,
                onAddExpenseClick = onAddExpenseClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(budzetkoBackground())
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProfileHeader(onBackClick = onBackClick)
            }
            item {
                ProfileHeroCard(
                    uiState = uiState,
                    profileImageUri = profileImageUri,
                    onEditClick = {
                        onClearProfileEditMessage()
                        isEditDialogOpen = true
                    }
                )
            }
            item {
                ProfileInfoCard(uiState = uiState)
            }
            item {
                ProfileSettingsCard(
                    syncUiState = syncUiState,
                    onSyncClick = onSyncClick,
                    appLanguage = appLanguage,
                    onLanguageChange = onLanguageChange,
                    appCurrency = appCurrency,
                    onCurrencyChange = onCurrencyChange,
                    appThemeMode = appThemeMode,
                    onThemeChange = onThemeChange
                )
            }
            item {
                DeleteAccountButton(onClick = { isDeleteDialogOpen = true })
            }
        }
    }

    if (isDeleteDialogOpen) {
        DeleteAccountDialog(
            onConfirm = { password ->
                isDeleteDialogOpen = false
                onDeleteAccountClick(password)
            },
            onDismiss = { isDeleteDialogOpen = false }
        )
    }

    if (isEditDialogOpen) {
        EditProfileDialog(
            uiState = uiState,
            profileEditState = profileEditState,
            profileImageUri = profileImageUri,
            onPickImage = {
                onPickProfileImage { uriText ->
                    profileImageUri = uriText
                    profilePrefs.edit().putString("profile_image_uri", uriText).apply()
                }
            },
            onRemoveImage = {
                profileImageUri = null
                profilePrefs.edit().remove("profile_image_uri").apply()
            },
            onSave = onUpdateProfile,
            onDismiss = {
                isEditDialogOpen = false
                onClearProfileEditMessage()
            }
        )
    }
}

@Composable
private fun ProfileAvatar(
    username: String,
    profileImageUri: String?,
    sizeDp: Int
) {
    val context = LocalContext.current
    val imageBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, profileImageUri) {
        value = profileImageUri?.let { uriText ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uriText)).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = stringResource(R.string.profile_photo),
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Text(
            text = username.firstOrNull()?.uppercase() ?: "A",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF050505)
        )
    }
}

@Composable
private fun ProfileHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(CardSurface)
                .border(BorderStroke(1.dp, SoftBorder), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = stringResource(R.string.back),
                tint = Ink,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
    }
}

@Composable
private fun ProfileHeroCard(
    uiState: UserUiState,
    profileImageUri: String?,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = PrimaryAccent.copy(alpha = 0.12f),
                spotColor = PrimaryAccent.copy(alpha = 0.18f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF050505), Color(0xFF1B1624), Color(0xFF2D2244))
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(LimeAccent),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatar(
                    username = uiState.username,
                    profileImageUri = profileImageUri,
                    sizeDp = 92
                )
            }
            Text(
                text = uiState.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.74f)
            )
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeAccent,
                    contentColor = Color(0xFF050505)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.profile_edit),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(uiState: UserUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = PrimaryAccent.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileInfoRow(
                icon = Icons.Outlined.Badge,
                title = stringResource(R.string.profile_username),
                value = uiState.username
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Person,
                title = stringResource(R.string.profile_email),
                value = uiState.email
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.profile_auth),
                value = stringResource(R.string.profile_auth_connected)
            )
        }
    }
}

@Composable
private fun ProfileSettingsCard(
    syncUiState: SyncUiState,
    onSyncClick: () -> Unit,
    appLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    appCurrency: AppCurrency,
    onCurrencyChange: (AppCurrency) -> Unit,
    appThemeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit
) {
    var isLanguageDialogOpen by remember { mutableStateOf(false) }
    var isCurrencyDialogOpen by remember { mutableStateOf(false) }
    var isThemeDialogOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = PrimaryAccent.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileInfoRow(
                icon = Icons.Outlined.Paid,
                title = stringResource(R.string.profile_currency),
                value = stringResource(appCurrency.labelRes()),
                onClick = { isCurrencyDialogOpen = true }
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.profile_language),
                value = stringResource(appLanguage.labelRes()),
                onClick = { isLanguageDialogOpen = true }
            )
            ProfileInfoRow(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.profile_theme),
                value = stringResource(appThemeMode.labelRes()),
                onClick = { isThemeDialogOpen = true }
            )
            ProfileInfoRow(
                icon = Icons.Outlined.CloudSync,
                title = stringResource(R.string.profile_sync),
                value = syncUiState.message,
                onClick = onSyncClick,
                trailing = {
                    if (syncUiState.status == SyncStatusUi.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryAccent
                        )
                    }
                }
            )
        }
    }

    if (isCurrencyDialogOpen) {
        CurrencyDialog(
            selectedCurrency = appCurrency,
            onCurrencySelected = { currency ->
                onCurrencyChange(currency)
                isCurrencyDialogOpen = false
            },
            onDismiss = { isCurrencyDialogOpen = false }
        )
    }

    if (isLanguageDialogOpen) {
        LanguageDialog(
            selectedLanguage = appLanguage,
            onLanguageSelected = { language ->
                onLanguageChange(language)
                isLanguageDialogOpen = false
            },
            onDismiss = { isLanguageDialogOpen = false }
        )
    }

    if (isThemeDialogOpen) {
        ThemeDialog(
            selectedTheme = appThemeMode,
            onThemeSelected = { theme ->
                onThemeChange(theme)
                isThemeDialogOpen = false
            },
            onDismiss = { isThemeDialogOpen = false }
        )
    }
}

@Composable
private fun CurrencyDialog(
    selectedCurrency: AppCurrency,
    onCurrencySelected: (AppCurrency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            DialogTitle(text = stringResource(R.string.profile_currency))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppCurrency.entries.forEach { currency ->
                    SettingsOption(
                        text = stringResource(currency.labelRes()),
                        isSelected = currency == selectedCurrency,
                        onClick = { onCurrencySelected(currency) }
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun LanguageDialog(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            DialogTitle(text = stringResource(R.string.profile_language))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsOption(
                    text = stringResource(AppLanguage.SLOVENIAN.labelRes()),
                    isSelected = selectedLanguage == AppLanguage.SLOVENIAN,
                    onClick = { onLanguageSelected(AppLanguage.SLOVENIAN) }
                )
                SettingsOption(
                    text = stringResource(AppLanguage.ENGLISH.labelRes()),
                    isSelected = selectedLanguage == AppLanguage.ENGLISH,
                    onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
                )
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ThemeDialog(
    selectedTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            DialogTitle(text = stringResource(R.string.profile_theme))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppThemeMode.entries.forEach { theme ->
                    SettingsOption(
                        text = stringResource(theme.labelRes()),
                        isSelected = theme == selectedTheme,
                        onClick = { onThemeSelected(theme) }
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.ExtraBold,
        color = Ink
    )
}

@Composable
private fun SettingsOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) LimeAccent.copy(alpha = 0.45f) else SecondaryAccent,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) PrimaryAccent else SoftBorder
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
    }
}

@Composable
private fun EditProfileDialog(
    uiState: UserUiState,
    profileEditState: ProfileEditState,
    profileImageUri: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onSave: (username: String, email: String, currentPassword: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember(uiState.username) { mutableStateOf(uiState.username) }
    var email by remember(uiState.email) { mutableStateOf(uiState.email) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            DialogTitle(text = stringResource(R.string.profile_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(LimeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatar(
                            username = username,
                            profileImageUri = profileImageUri,
                            sizeDp = 68
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPickImage,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LimeAccent,
                                contentColor = Color(0xFF050505)
                            )
                        ) {
                            Text(text = stringResource(R.string.profile_choose_photo), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onRemoveImage,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryAccent,
                                contentColor = Ink
                            )
                        ) {
                            Text(text = stringResource(R.string.profile_remove_photo), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.profile_username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.profile_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(stringResource(R.string.current_password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(stringResource(R.string.new_password_optional)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.profile_edit_auth_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
                profileEditState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, email, currentPassword, newPassword) },
                enabled = !profileEditState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccent,
                    contentColor = Color.White
                )
            ) {
                if (profileEditState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(text = stringResource(R.string.save))
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !profileEditState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryAccent,
                    contentColor = Ink
                )
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeleteAccountButton(onClick: () -> Unit) {
    val danger = Color(0xFFE53935)
    val background = MaterialTheme.colorScheme.background
    val isDark = (background.red * 0.299f + background.green * 0.587f + background.blue * 0.114f) < 0.5f
    val dangerSurface = if (isDark) Color(0xFF2A1518) else Color(0xFFFFF3F2)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = danger.copy(alpha = 0.08f),
                spotColor = danger.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = dangerSurface,
            contentColor = danger
        ),
        border = BorderStroke(1.dp, danger.copy(alpha = 0.42f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(danger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = stringResource(R.string.delete_account), fontWeight = FontWeight.ExtraBold)
                Text(
                    text = stringResource(R.string.delete_account_hint),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = danger.copy(alpha = 0.76f)
                )
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            DialogTitle(text = stringResource(R.string.delete_account_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.delete_account_message),
                    color = MutedInk,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E),
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SecondaryAccent,
                    contentColor = Ink
                )
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

private fun AppCurrency.labelRes(): Int {
    return when (this) {
        AppCurrency.EUR -> R.string.currency_eur
        AppCurrency.USD -> R.string.currency_usd
        AppCurrency.GBP -> R.string.currency_gbp
    }
}

private fun AppLanguage.labelRes(): Int {
    return when (this) {
        AppLanguage.SLOVENIAN -> R.string.language_slovenian
        AppLanguage.ENGLISH -> R.string.language_english
    }
}

private fun AppThemeMode.labelRes(): Int {
    return when (this) {
        AppThemeMode.LIGHT -> R.string.theme_light
        AppThemeMode.DARK -> R.string.theme_dark
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    if (title == stringResource(R.string.profile_language)) {
                        LimeAccent
                    } else {
                        SecondaryAccent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (title == stringResource(R.string.profile_language)) Color(0xFF050505) else PrimaryAccent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
        trailing?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    BudzetkoTheme {
        ProfileContent(
            uiState = UserUiState(),
            syncUiState = SyncUiState(),
            profileEditState = ProfileEditState(),
            onSyncClick = {},
            onUpdateProfile = { _, _, _, _ -> },
            onClearProfileEditMessage = {},
            onBackClick = {},
            onHomeClick = {},
            onTransactionsClick = {},
            onAddExpenseClick = {},
            onAnalyticsClick = {},
            onSettingsClick = {},
            appLanguage = AppLanguage.SLOVENIAN,
            onLanguageChange = {},
            appCurrency = AppCurrency.EUR,
            onCurrencyChange = {},
            appThemeMode = AppThemeMode.LIGHT,
            onThemeChange = {},
            onDeleteAccountClick = {},
            onPickProfileImage = {}
        )
    }
}
