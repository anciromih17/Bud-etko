package si.um.feri.budzetko.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.R

private val ScreenBackground = Color(0xFFF7F4EE)
private val CardSurface = Color(0xFFFFFFFF)
private val PrimaryAccent = Color(0xFF156C6A)
private val SoftBorder = Color(0xFFE3DDD3)
private val Ink = Color(0xFF191B1F)

@Composable
fun BudzetkoBottomBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ScreenBackground)
            .padding(start = 24.dp, end = 24.dp, bottom = 18.dp, top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CardSurface)
                .border(BorderStroke(1.dp, SoftBorder), RoundedCornerShape(18.dp))
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarIcon(Icons.Filled.Home, stringResource(R.string.nav_home), onHomeClick)
            BottomBarIcon(Icons.Outlined.AccountBalanceWallet, stringResource(R.string.nav_budget), onBudgetClick)
            Spacer(modifier = Modifier.width(50.dp))
            BottomBarIcon(Icons.Outlined.Analytics, stringResource(R.string.nav_analytics), onAnalyticsClick)
            BottomBarIcon(Icons.Outlined.Settings, stringResource(R.string.nav_settings), onSettingsClick)
        }

        IconButton(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(PrimaryAccent)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.nav_add_expense),
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun BottomBarIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(38.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Ink,
            modifier = Modifier.size(24.dp)
        )
    }
}
