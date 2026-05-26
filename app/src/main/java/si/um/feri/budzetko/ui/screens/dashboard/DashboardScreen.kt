package si.um.feri.budzetko.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.SouthWest
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.SyncStatus
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoBackground
import si.um.feri.budzetko.ui.theme.BudzetkoInk
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoSurface
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.ui.theme.budzetkoCategoryColor
import si.um.feri.budzetko.viewmodel.DashboardCategorySpending
import si.um.feri.budzetko.viewmodel.DashboardTransaction
import si.um.feri.budzetko.viewmodel.DashboardUiState
import si.um.feri.budzetko.viewmodel.DashboardViewModel

private val CardSurface = BudzetkoSurface
private val Ink = BudzetkoInk
private val MutedInk = Color(0xFF6D6774)
private val PrimaryAccent = BudzetkoPurple
private val LimeAccent = BudzetkoLime
private val SoftAccent = Color(0xFFF4F0FF)
private val DateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onProfileClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedMonth: Int,
    selectedYear: Int,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.setMonth(selectedMonth, selectedYear)
    }

    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onProfileClick = onProfileClick,
        onTransactionsClick = onTransactionsClick,
        onAddExpenseClick = onAddExpenseClick,
        onAnalyticsClick = onAnalyticsClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onProfileClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BudzetkoBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = {},
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
                .background(BudzetkoBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, top = 34.dp, end = 24.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SummaryCard(
                    uiState = uiState,
                    onProfileClick = onProfileClick
                )
            }
            item {
                SpendingByCategoryCard(
                    month = uiState.month,
                    year = uiState.year,
                    categorySpending = uiState.categorySpending
                )
            }
            item {
                RecentTransactionsCard(
                    transactions = uiState.recentTransactions,
                    onSeeAllClick = onTransactionsClick
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    uiState: DashboardUiState,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(34.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dobrodošli!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "Skupni mesečni proračun",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                    Text(
                        text = "${uiState.totalBudget.formatMoney()}€",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = monthName(uiState.month) + " " + uiState.year,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MutedInk
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SoftAccent)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profil", tint = Ink, modifier = Modifier.size(30.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    iconColor = Ink,
                    icon = Icons.Outlined.SouthWest,
                    label = "Porabljeno",
                    value = "${uiState.totalSpent.formatMoney()}€"
                )
                SummaryMetricCard(
                    modifier = Modifier.weight(1f),
                    iconColor = Ink,
                    icon = Icons.Outlined.ArrowOutward,
                    label = "Na voljo",
                    value = "${uiState.available.formatMoney()}€"
                )
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(
    modifier: Modifier,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFBFAF7)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Ink)
        }
    }
}

@Composable
private fun SpendingByCategoryCard(
    month: Int,
    year: Int,
    categorySpending: List<DashboardCategorySpending>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Poraba po kategorijah",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Text(
                    text = monthName(month) + " " + year,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk
                )
            }

            val visibleCategories = categorySpending.filter { it.spentAmount > 0.0 }.take(5)
            if (visibleCategories.isEmpty()) {
                EmptyCardText("Ta mesec še ni porabe po kategorijah.")
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                CategoryDonutChart(
                    categories = visibleCategories,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(148.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleCategories.forEach { category ->
                        CategoryProgressRow(
                            category = category
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDonutChart(
    categories: List<DashboardCategorySpending>,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.spentAmount }.coerceAtLeast(1.0)
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 26.dp.toPx(), cap = StrokeCap.Butt)
        val size = Size(this.size.minDimension, this.size.minDimension)
        var startAngle = -90f
        categories.forEach { category ->
            val sweep = ((category.spentAmount / total) * 360.0).toFloat()
            drawArc(
                color = dashboardCategoryColor(category),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                size = size,
                style = stroke
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryProgressRow(
    category: DashboardCategorySpending
) {
    val progress = category.progress.coerceIn(0f, 1f)
    val iconColor = dashboardCategoryColor(category)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.62f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji ?: "●")
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(
                    text = category.categoryName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "${category.spentAmount.formatMoney()}€",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = iconColor,
                trackColor = SoftAccent
            )
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    transactions: List<DashboardTransaction>,
    onSeeAllClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nedavne transakcije",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Text(
                    text = "Vse",
                    modifier = Modifier.clickable(onClick = onSeeAllClick),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
            }

            if (transactions.isEmpty()) {
                EmptyCardText("Ni še dodanih transakcij.")
            } else {
                transactions.forEach { transaction ->
                    RecentTransactionRow(transaction)
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionRow(transaction: DashboardTransaction) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    budzetkoCategoryColor(
                        categoryId = transaction.expense.categoryId,
                        colorIndex = transaction.categoryColorIndex,
                        hasEmoji = !transaction.categoryEmoji.isNullOrBlank()
                    ).copy(alpha = 0.62f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = transaction.categoryEmoji ?: "●")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.expense.description.lineSequence().firstOrNull().orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = transaction.categoryName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "-${transaction.expense.amount.formatMoney()}€",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = transaction.expense.dateLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
        }
    }
}

@Composable
private fun EmptyCardText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MutedInk
    )
}

private fun dashboardCategoryColor(category: DashboardCategorySpending): Color {
    return budzetkoCategoryColor(
        categoryId = category.categoryId,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )
}

private fun ExpenseEntity.dateLabel(): String {
    return Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateFormatter)
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
private fun DashboardContentPreview() {
    BudzetkoTheme {
        DashboardContent(
            uiState = DashboardUiState(
                month = 5,
                year = 2026,
                totalBudget = 2_000.0,
                totalSpent = 715.86,
                categorySpending = listOf(
                    DashboardCategorySpending(1, "Hrana", "🍓", 0, 320.0, 600.0),
                    DashboardCategorySpending(2, "Stroški", "🚨", 2, 210.0, 420.0),
                    DashboardCategorySpending(3, "Zabava", "🎁", 6, 185.86, 240.0)
                ),
                recentTransactions = listOf(
                    DashboardTransaction(
                        expense = ExpenseEntity(
                            amount = 12.60,
                            date = System.currentTimeMillis(),
                            description = "Kino",
                            userId = "demo-user",
                            categoryId = 3,
                            syncStatus = SyncStatus.SYNCED
                        ),
                        categoryName = "Zabava",
                        categoryEmoji = "🎁",
                        categoryColorIndex = 6
                    )
                )
            ),
            onProfileClick = {},
            onTransactionsClick = {},
            onAddExpenseClick = {},
            onAnalyticsClick = {},
            onSettingsClick = {}
        )
    }
}
