package si.um.feri.budzetko.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.firebase.auth.FirebaseAuth
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.currency.currentCurrencySymbol
import si.um.feri.budzetko.currency.formatCurrencyAmount
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoBorder
import si.um.feri.budzetko.ui.theme.budzetkoCategoryColor
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSoftAccent
import si.um.feri.budzetko.ui.theme.budzetkoSurface
import si.um.feri.budzetko.viewmodel.BudgetLimitDraft
import si.um.feri.budzetko.viewmodel.BudgetUiState
import si.um.feri.budzetko.viewmodel.BudgetViewModel
import si.um.feri.budzetko.viewmodel.DashboardUiState
import si.um.feri.budzetko.viewmodel.SyncStatusUi
import si.um.feri.budzetko.viewmodel.SyncUiState

private val CardSurface: Color
    @Composable get() = budzetkoSurface()
private val PrimaryAccent = BudzetkoPurple
private val SecondaryAccent: Color
    @Composable get() = budzetkoSoftAccent()
private val LimeAccent = BudzetkoLime
private val SoftBorder: Color
    @Composable get() = budzetkoBorder()
private val Ink: Color
    @Composable get() = budzetkoInk()
private val MutedInk: Color
    @Composable get() = budzetkoMutedInk()

@Composable
fun SettingsScreen(
    budgetViewModel: BudgetViewModel,
    dashboardUiState: DashboardUiState,
    syncUiState: SyncUiState,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onSystemSettingsClick: () -> Unit = onProfileClick,
    onLogoutClick: () -> Unit,
    onBudgetHistoryClick: () -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onTransactionsClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val budgetUiState by budgetViewModel.uiState.collectAsState()

    SettingsContent(
        budgetUiState = budgetUiState,
        dashboardUiState = dashboardUiState,
        syncUiState = syncUiState,
        onHomeClick = onHomeClick,
        onProfileClick = onProfileClick,
        onAddExpenseClick = onAddExpenseClick,
        onTransactionsClick = onTransactionsClick,
        onAnalyticsClick = onAnalyticsClick,
        onCategorySettingsClick = onCategorySettingsClick,
        onSystemSettingsClick = onSystemSettingsClick,
        onLogoutClick = onLogoutClick,
        onBudgetHistoryClick = onBudgetHistoryClick,
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
    dashboardUiState: DashboardUiState,
    syncUiState: SyncUiState,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onSystemSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBudgetHistoryClick: () -> Unit,
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
    var isNotificationsOpen by remember { mutableStateOf(false) }
    var readNotificationIds by remember { mutableStateOf(setOf<String>()) }
    val notificationItems = notificationItemsFor(dashboardUiState, syncUiState, readNotificationIds)
    val unreadNotificationCount = notificationItems.count { it.unread }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = budzetkoBackground(),
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
                .background(budzetkoBackground())
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { SettingsHeader() }
            item { ProfileCard(onClick = onProfileClick) }
            item {
                SettingsMenuCard(
                    onOpenBudget = onOpenBudget,
                    onBudgetHistoryClick = onBudgetHistoryClick,
                    onCategorySettingsClick = onCategorySettingsClick,
                    onNotificationsClick = { isNotificationsOpen = true },
                    notificationCount = unreadNotificationCount,
                    onSystemSettingsClick = onSystemSettingsClick
                )
            }
            item {
                LogoutButton(onLogoutClick = onLogoutClick)
            }
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

    if (isNotificationsOpen) {
        NotificationsDialog(
            items = notificationItems,
            onMarkRead = { notificationId ->
                readNotificationIds = readNotificationIds + notificationId
            },
            onDismiss = { isNotificationsOpen = false }
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
                    .background(LimeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color(0xFF050505),
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
                    text = FirebaseAuth.getInstance().currentUser?.email ?: "uporabnik@email.com",
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
    onBudgetHistoryClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationCount: Int,
    onSystemSettingsClick: () -> Unit
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
            SettingsMenuRow(Icons.Outlined.History, stringResource(R.string.settings_budget_history), onBudgetHistoryClick)
            SettingsMenuRow(Icons.Outlined.Category, stringResource(R.string.settings_category_settings), onCategorySettingsClick)
            SettingsMenuRow(
                Icons.Outlined.Notifications,
                stringResource(R.string.settings_notifications),
                onNotificationsClick,
                badgeCount = notificationCount
            )
            SettingsMenuRow(Icons.Outlined.Settings, stringResource(R.string.settings_system), onSystemSettingsClick)
        }
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Ink),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = CardSurface
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }
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

private data class NotificationItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val message: String,
    val date: String,
    val unread: Boolean = false
)

@Composable
private fun notificationItemsFor(
    dashboardUiState: DashboardUiState,
    syncUiState: SyncUiState,
    readNotificationIds: Set<String>
): List<NotificationItem> {
    val monthTitle = "${monthName(dashboardUiState.month)} ${dashboardUiState.year}"
    val spent = formatCurrencyAmount(dashboardUiState.totalSpent)
    val budgetPercent = if (dashboardUiState.totalBudget > 0.0) {
        ((dashboardUiState.totalSpent / dashboardUiState.totalBudget) * 100).toInt()
    } else {
        0
    }

    val items = mutableListOf(
        NotificationItem(
            id = "monthly-summary-${dashboardUiState.year}-${dashboardUiState.month}",
            icon = Icons.Outlined.QueryStats,
            title = stringResource(R.string.notification_monthly_summary),
            message = if (dashboardUiState.hasBudget) {
                stringResource(R.string.notification_monthly_summary_message, monthTitle, spent, budgetPercent)
            } else {
                stringResource(R.string.notification_missing_budget_message, monthTitle)
            },
            date = stringResource(R.string.notification_today),
            unread = "monthly-summary-${dashboardUiState.year}-${dashboardUiState.month}" !in readNotificationIds
        )
    )

    val limitAlert = dashboardUiState.categorySpending
        .filter { it.limitAmount != null && it.limitAmount > 0.0 && it.progress >= 0.8f }
        .maxByOrNull { it.progress }
    if (limitAlert != null) {
        val percent = (limitAlert.progress * 100).toInt()
        items += NotificationItem(
            id = "limit-${limitAlert.categoryId}-${dashboardUiState.year}-${dashboardUiState.month}",
            icon = Icons.Outlined.WarningAmber,
            title = if (limitAlert.progress >= 1f) {
                stringResource(R.string.notification_limit_exceeded)
            } else {
                stringResource(R.string.notification_limit_near)
            },
            message = stringResource(
                R.string.notification_limit_message,
                limitAlert.categoryName,
                percent
            ),
            date = stringResource(R.string.notification_today),
            unread = "limit-${limitAlert.categoryId}-${dashboardUiState.year}-${dashboardUiState.month}" !in readNotificationIds
        )
    }

    val aiMessage = dashboardUiState.aiSummary
    items += NotificationItem(
        id = "ai-${dashboardUiState.year}-${dashboardUiState.month}",
        icon = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.notification_ai_recommendation),
        message = aiMessage?.lineSequence()?.firstOrNull { it.isNotBlank() }?.take(150)
            ?: stringResource(R.string.notification_ai_recommendation_fallback),
        date = stringResource(R.string.notification_recent),
        unread = "ai-${dashboardUiState.year}-${dashboardUiState.month}" !in readNotificationIds
    )

    if (syncUiState.status == SyncStatusUi.ERROR || syncUiState.status == SyncStatusUi.WARNING) {
        items += NotificationItem(
            id = "sync-${syncUiState.status}",
            icon = Icons.Outlined.CloudOff,
            title = stringResource(R.string.notification_sync_attention),
            message = stringResource(syncUiState.messageResId, *syncUiState.messageArgs.toTypedArray()),
            date = stringResource(R.string.notification_recent),
            unread = "sync-${syncUiState.status}" !in readNotificationIds
        )
    }

    return items.take(4)
}

@Composable
private fun NotificationsDialog(
    items: List<NotificationItem>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showUnreadOnly by remember { mutableStateOf(false) }
    val unreadItems = items.filter { it.unread }
    val visibleItems = if (showUnreadOnly) unreadItems else items

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .padding(horizontal = 24.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 520.dp, max = 720.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(34.dp),
                color = CardSurface,
                shadowElevation = 16.dp
            ) {
                Column {
                    NotificationsHeader(onDismiss = onDismiss)
                    Text(
                        text = if (visibleItems.isEmpty()) {
                            stringResource(R.string.notifications_empty)
                        } else {
                            stringResource(R.string.notifications_status)
                        },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                    NotificationsTabs(
                        allCount = items.size,
                        unreadCount = unreadItems.size,
                        showUnreadOnly = showUnreadOnly,
                        onAllClick = { showUnreadOnly = false },
                        onUnreadClick = { showUnreadOnly = true }
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (visibleItems.isEmpty()) {
                            item {
                                EmptyNotificationsCard(
                                    message = if (showUnreadOnly) {
                                        stringResource(R.string.notifications_no_unread)
                                    } else {
                                        stringResource(R.string.notifications_empty)
                                    }
                                )
                            }
                        } else {
                            items(visibleItems) { item ->
                                NotificationCard(
                                    item = item,
                                    onClick = { onMarkRead(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 18.dp, end = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_notifications),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SecondaryAccent)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close), tint = Ink)
        }
    }
}

@Composable
private fun NotificationsTabs(
    allCount: Int,
    unreadCount: Int,
    showUnreadOnly: Boolean,
    onAllClick: () -> Unit,
    onUnreadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NotificationTabButton(
            text = stringResource(R.string.notifications_all, allCount),
            selected = !showUnreadOnly,
            onClick = onAllClick,
            modifier = Modifier.weight(1f)
        )
        NotificationTabButton(
            text = stringResource(R.string.notifications_unread, unreadCount),
            selected = showUnreadOnly,
            onClick = onUnreadClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NotificationTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PrimaryAccent else SecondaryAccent,
        border = BorderStroke(1.dp, if (selected) PrimaryAccent else SoftBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else Ink
            )
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = if (item.unread) SecondaryAccent else CardSurface,
        border = BorderStroke(1.dp, if (item.unread) PrimaryAccent.copy(alpha = 0.24f) else SoftBorder)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (item.icon == Icons.Outlined.AutoAwesome) LimeAccent else PrimaryAccent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.icon == Icons.Outlined.AutoAwesome) Color(0xFF050505) else PrimaryAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    if (item.unread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LimeAccent)
                        )
                    }
                }
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = SecondaryAccent,
        border = BorderStroke(1.dp, SoftBorder)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MutedInk
        )
    }
}

@Composable
private fun LogoutButton(
    onLogoutClick: () -> Unit
) {
    Button(
        onClick = onLogoutClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
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
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .padding(horizontal = 22.dp, vertical = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 760.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
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
                                suffix = { Text(text = currentCurrencySymbol()) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = onSuggestLimits,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = Color(0xFF050505))
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
                    item {
                        Text(
                            text = stringResource(R.string.suggested_limits),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink
                        )
                    }
                    if (uiState.proposedLimits.isNotEmpty()) {
                        items(uiState.proposedLimits, key = { it.category.id }) { draft ->
                            BudgetLimitCard(
                                draft = draft,
                                onEditLimit = onEditLimit,
                                onConfirmLimit = onConfirmLimit,
                                onLimitChange = onLimitChange
                            )
                        }
                    } else {
                        item {
                            MessageText(
                                message = "Ni še predlaganih limitov. Lahko shraniš samo mesečni proračun ali najprej dodaj kategorije.",
                                color = MutedInk
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = onSaveBudget,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
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
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(26.dp),
        color = budgetCardBackground(draft.category)
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
                        text = "Mesečni limit: ${formatCurrencyAmount(draft.limitAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                    draft.trendNote?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryAccent
                        )
                    }
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
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
    val accent = categoryAccent(category)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.45f)),
        contentAlignment = Alignment.Center
    ) {
        if (category.emoji.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        } else {
            Text(text = category.emoji.orEmpty())
        }
    }
}

private fun categoryAccent(category: CategoryEntity): Color {
    return budzetkoCategoryColor(
        categoryId = category.id,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )
}

private fun budgetCardBackground(category: CategoryEntity): Color {
    return categoryAccent(category).copy(alpha = 0.13f)
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
            dashboardUiState = DashboardUiState(month = 5, year = 2026),
            syncUiState = SyncUiState(),
            onHomeClick = {},
            onProfileClick = {},
            onAddExpenseClick = {},
            onTransactionsClick = {},
            onAnalyticsClick = {},
            onCategorySettingsClick = {},
            onSystemSettingsClick = {},
            onLogoutClick = {},
            onBudgetHistoryClick = {},
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
