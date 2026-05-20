package si.um.feri.budzetko.ui.screens.settings

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoBackground
import si.um.feri.budzetko.ui.theme.BudzetkoBorder
import si.um.feri.budzetko.ui.theme.BudzetkoInk
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.BudgetLimitDraft
import si.um.feri.budzetko.viewmodel.BudgetUiState
import si.um.feri.budzetko.viewmodel.BudgetViewModel

private val CardSurface = Color(0xFFFFFFFF)
private val PrimaryAccent = BudzetkoPurple
private val SecondaryAccent = Color(0xFFF4F0FF)
private val LimeAccent = BudzetkoLime
private val SoftBorder = BudzetkoBorder
private val Ink = BudzetkoInk
private val MutedInk = Color(0xFF6D6774)

@Composable
fun SettingsScreen(
    budgetViewModel: BudgetViewModel,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val budgetUiState by budgetViewModel.uiState.collectAsState()

    SettingsContent(
        budgetUiState = budgetUiState,
        onHomeClick = onHomeClick,
        onProfileClick = onProfileClick,
        onAddExpenseClick = onAddExpenseClick,
        onTransactionsClick = onTransactionsClick,
        onAnalyticsClick = onAnalyticsClick,
        onCategorySettingsClick = onCategorySettingsClick,
        onOpenBudget = budgetViewModel::openBudgetDialog,
        onCloseBudget = budgetViewModel::closeBudgetDialog,
        onIncomeChange = budgetViewModel::onIncomeChange,
        onPreviousMonth = budgetViewModel::previousMonth,
        onNextMonth = budgetViewModel::nextMonth,
        onSuggestLimits = { budgetViewModel.proposeLimits(budgetUiState.categories) },
        onEditLimit = budgetViewModel::startEditingLimit,
        onConfirmLimit = budgetViewModel::confirmEditingLimit,
        onLimitChange = budgetViewModel::updateLimitPercent,
        onSaveBudget = budgetViewModel::saveBudget,
        modifier = modifier
    )
}

@Composable
private fun SettingsContent(
    budgetUiState: BudgetUiState,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onOpenBudget: () -> Unit,
    onCloseBudget: () -> Unit,
    onIncomeChange: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSuggestLimits: () -> Unit,
    onEditLimit: (Long) -> Unit,
    onConfirmLimit: (Long) -> Unit,
    onLimitChange: (Long, Float) -> Unit,
    onSaveBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BudzetkoBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = onHomeClick,
                onBudgetClick = onTransactionsClick,
                onAddExpenseClick = onAddExpenseClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = {}
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BudzetkoBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { SettingsHeader() }
            item { ProfileCard(onClick = onProfileClick) }
            item {
                SettingsMenuCard(
                    onOpenBudget = onOpenBudget,
                    onCategorySettingsClick = onCategorySettingsClick
                )
            }
            item { LogoutButton() }
        }
    }

    if (budgetUiState.isBudgetDialogOpen) {
        BudgetDialog(
            uiState = budgetUiState,
            onDismiss = onCloseBudget,
            onIncomeChange = onIncomeChange,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onSuggestLimits = onSuggestLimits,
            onEditLimit = onEditLimit,
            onConfirmLimit = onConfirmLimit,
            onLimitChange = onLimitChange,
            onSaveBudget = onSaveBudget
        )
    }
}

@Composable
private fun SettingsHeader() {
    Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MutedInk
        )
    }
}

@Composable
private fun ProfileCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LimeAccent.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.settings_user),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Text(
                    text = "ana@budzetko.local",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    onOpenBudget: () -> Unit,
    onCategorySettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SettingsMenuRow(Icons.Outlined.EventNote, stringResource(R.string.settings_monthly_budget), onOpenBudget)
            SettingsMenuRow(Icons.Outlined.History, stringResource(R.string.settings_budget_history), {})
            SettingsMenuRow(Icons.Outlined.Category, stringResource(R.string.settings_category_settings), onCategorySettingsClick)
            SettingsMenuRow(Icons.Outlined.Notifications, stringResource(R.string.settings_notifications), {})
            SettingsMenuRow(Icons.Outlined.Settings, stringResource(R.string.settings_system), {})
        }
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SecondaryAccent)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryAccent)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = title,
                tint = Ink,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LogoutButton() {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
    ) {
        Icon(imageVector = Icons.Outlined.Logout, contentDescription = null)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(R.string.logout), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BudgetDialog(
    uiState: BudgetUiState,
    onDismiss: () -> Unit,
    onIncomeChange: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSuggestLimits: () -> Unit,
    onEditLimit: (Long) -> Unit,
    onConfirmLimit: (Long) -> Unit,
    onLimitChange: (Long, Float) -> Unit,
    onSaveBudget: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .padding(horizontal = 22.dp, vertical = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp),
                shape = RoundedCornerShape(30.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, SoftBorder),
                shadowElevation = 12.dp
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item { BudgetDialogHeader(onDismiss = onDismiss) }
                    item {
                        MonthSelector(
                            month = uiState.month,
                            year = uiState.year,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.monthly_income),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            OutlinedTextField(
                                value = uiState.incomeInput,
                                onValueChange = onIncomeChange,
                                suffix = { Text(text = "€") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = onSuggestLimits,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Outlined.TipsAndUpdates, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.suggest_limits), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (uiState.errorMessage != null) {
                        item { MessageText(uiState.errorMessage, Color(0xFFB3261E)) }
                    }
                    if (uiState.successMessage != null) {
                        item { MessageText(uiState.successMessage, PrimaryAccent) }
                    }
                    if (uiState.proposedLimits.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.suggested_limits),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink
                            )
                        }
                        items(uiState.proposedLimits, key = { it.category.id }) { draft ->
                            BudgetLimitCard(
                                draft = draft,
                                onEditLimit = onEditLimit,
                                onConfirmLimit = onConfirmLimit,
                                onLimitChange = onLimitChange
                            )
                        }
                        item {
                            Button(
                                onClick = onSaveBudget,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                            ) {
                                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.save), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetDialogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.monthly_budget),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(36.dp)
                .border(BorderStroke(1.dp, SoftBorder), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.close),
                tint = Ink
            )
        }
    }
}

@Composable
private fun MonthSelector(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.budget_month_previous))
        }
        Text(
            text = "${monthName(month)} $year",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Outlined.ArrowForwardIos, contentDescription = stringResource(R.string.budget_month_next))
        }
    }
}

@Composable
private fun BudgetLimitCard(
    draft: BudgetLimitDraft,
    onEditLimit: (Long) -> Unit,
    onConfirmLimit: (Long) -> Unit,
    onLimitChange: (Long, Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryMarker(draft.category)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = draft.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = "Mesečni limit: ${draft.limitAmount.formatMoney()}€",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }
                Text(
                    text = "${draft.percent} %",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryAccent
                )
                IconButton(
                    onClick = {
                        if (draft.isEditing) onConfirmLimit(draft.category.id) else onEditLimit(draft.category.id)
                    }
                ) {
                    Icon(
                        imageVector = if (draft.isEditing) Icons.Outlined.CheckCircle else Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = PrimaryAccent
                    )
                }
            }
            if (draft.isEditing) {
                Slider(
                    value = draft.percent.toFloat(),
                    onValueChange = { onLimitChange(draft.category.id, it) },
                    valueRange = 0f..100f
                )
                Button(
                    onClick = { onConfirmLimit(draft.category.id) },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Composable
private fun CategoryMarker(category: CategoryEntity) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(SecondaryAccent),
        contentAlignment = Alignment.Center
    ) {
        if (category.emoji.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(LimeAccent)
            )
        } else {
            Text(text = category.emoji.orEmpty())
        }
    }
}

@Composable
private fun MessageText(message: String, color: Color) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = color
    )
}

private fun monthName(month: Int): String {
    return listOf(
        "Januar", "Februar", "Marec", "April", "Maj", "Junij",
        "Julij", "Avgust", "September", "Oktober", "November", "December"
    )[month - 1]
}

private fun Double.formatMoney(): String = "%.2f".format(this)

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    BudzetkoTheme {
        SettingsContent(
            budgetUiState = BudgetUiState(),
            onHomeClick = {},
            onProfileClick = {},
            onAddExpenseClick = {},
            onTransactionsClick = {},
            onAnalyticsClick = {},
            onCategorySettingsClick = {},
            onOpenBudget = {},
            onCloseBudget = {},
            onIncomeChange = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onSuggestLimits = {},
            onEditLimit = {},
            onConfirmLimit = {},
            onLimitChange = { _, _ -> },
            onSaveBudget = {}
        )
    }
}
