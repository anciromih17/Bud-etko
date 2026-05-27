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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.R
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoBorder
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoSurface

private val CardSurface: Color
    @Composable get() = budzetkoSurface()
private val SoftBorder: Color
    @Composable get() = budzetkoBorder()
private val Ink: Color
    @Composable get() = budzetkoInk()
private val PrimaryAccent = BudzetkoPurple

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
            .background(budzetkoBackground())
            .padding(start = 24.dp, end = 24.dp, bottom = 18.dp, top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.06f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(CardSurface)
                .border(BorderStroke(1.dp, SoftBorder), RoundedCornerShape(28.dp))
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
                .size(60.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.24f)
                )
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
