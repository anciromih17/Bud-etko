package si.um.feri.budzetko.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.R
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoBackground
import si.um.feri.budzetko.ui.theme.BudzetkoBorder
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.SyncStatusUi
import si.um.feri.budzetko.viewmodel.SyncUiState
import si.um.feri.budzetko.viewmodel.SyncViewModel
import si.um.feri.budzetko.viewmodel.UserUiState
import si.um.feri.budzetko.viewmodel.UserViewModel

private val CardSurface = Color(0xFFFFFFFF)
private val PrimaryAccent = Color(0xFF6B4EFF)
private val SecondaryAccent = Color(0xFFF4F0FF)
private val LimeAccent = Color(0xFFD9FF6C)
private val SoftBorder = BudzetkoBorder
private val Ink = Color(0xFF050505)
private val MutedInk = Color(0xFF6D6774)

@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    syncViewModel: SyncViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncUiState by syncViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        syncViewModel.refreshLocalSyncStatus()
    }

    ProfileContent(
        uiState = uiState,
        syncUiState = syncUiState,
        onSyncClick = syncViewModel::syncNow,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onAnalyticsClick = onAnalyticsClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
private fun ProfileContent(
    uiState: UserUiState,
    syncUiState: SyncUiState,
    onSyncClick: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BudzetkoBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = onHomeClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BudzetkoBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProfileHeader(onBackClick = onBackClick)
            }
            item {
                ProfileHeroCard(uiState = uiState)
            }
            item {
                ProfileInfoCard(uiState = uiState)
            }
            item {
                ProfileSettingsCard(
                    syncUiState = syncUiState,
                    onSyncClick = onSyncClick
                )
            }
        }
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
private fun ProfileHeroCard(uiState: UserUiState) {
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
                Text(
                    text = uiState.username.firstOrNull()?.uppercase() ?: "A",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
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
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeAccent,
                    contentColor = Ink
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
                value = "Firebase Auth connected"
            )
        }
    }
}

@Composable
private fun ProfileSettingsCard(
    syncUiState: SyncUiState,
    onSyncClick: () -> Unit
) {
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
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.profile_language),
                value = stringResource(R.string.profile_language_value)
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
                        LimeAccent.copy(alpha = 0.45f)
                    } else {
                        SecondaryAccent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryAccent,
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
            onSyncClick = {},
            onBackClick = {},
            onHomeClick = {},
            onAnalyticsClick = {},
            onSettingsClick = {}
        )
    }
}
