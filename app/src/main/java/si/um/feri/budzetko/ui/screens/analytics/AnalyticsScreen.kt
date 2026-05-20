package si.um.feri.budzetko.ui.screens.analytics

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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.data.entity.ExpenseEntity
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
private val WarningColor = Color(0xFFFFB864)
private val DangerColor = Color(0xFFFF7D8A)

@Composable
fun AnalyticsScreen(
    viewModel: DashboardViewModel,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AnalyticsContent(
        uiState = uiState,
        onProfileClick = onProfileClick,
        onHomeClick = onHomeClick,
        onTransactionsClick = onTransactionsClick,
        onCategorySettingsClick = onCategorySettingsClick,
        onAddExpenseClick = onAddExpenseClick,
        onSettingsClick = onSettingsClick,
        modifier = modifier
    )
}

@Composable
private fun AnalyticsContent(
    uiState: DashboardUiState,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
                onAnalyticsClick = {},
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
                AnalyticsHeader(onProfileClick = onProfileClick)
            }
            item {
                AnalyticsSummaryRow(uiState = uiState)
            }
            item {
                CategoryDistributionCard(
                    categories = uiState.categorySpending,
                    onEditClick = onCategorySettingsClick
                )
            }
            item {
                AiRecommendationCard()
            }
            item {
                SpendingChartCard(categories = uiState.categorySpending)
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Analitika",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = "Pregled porabe po kategorijah",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
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
}

@Composable
private fun AnalyticsSummaryRow(uiState: DashboardUiState) {
    val budgetUsage = if (uiState.totalBudget > 0.0) {
        ((uiState.totalSpent / uiState.totalBudget) * 100.0).coerceAtLeast(0.0)
    } else {
        0.0
    }
    val warningCount = uiState.categorySpending.count { it.limitAmount != null && it.progress >= 0.8f }
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.ArrowOutward,
            title = "Poraba",
            value = "${uiState.totalSpent.formatMoney()}€",
            subtitle = "${budgetUsage.toInt()}% proračuna",
            iconBackground = Ink
        )
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.ErrorOutline,
            title = "Opozorila",
            value = warningCount.toString(),
            subtitle = "Preseženih limitov",
            iconBackground = if (warningCount > 0) DangerColor else SoftAccent,
            iconTint = if (warningCount > 0) Color.White else Ink
        )
    }
}

@Composable
private fun AnalyticsMetricCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    iconBackground: Color,
    iconTint: Color = Color.White
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Ink)
            }
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Ink)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MutedInk)
        }
    }
}

@Composable
private fun CategoryDistributionCard(
    categories: List<DashboardCategorySpending>,
    onEditClick: () -> Unit
) {
    val visibleCategories = categories.filter { it.limitAmount != null || it.spentAmount > 0.0 }.take(6)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Porazdelitev po kategorijah",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Uredi kategorije",
                    tint = Ink,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onEditClick)
                )
            }

            if (visibleCategories.isEmpty()) {
                EmptyText("Za analitiko najprej dodaj kategorije, budget in stroške.")
            } else {
                visibleCategories.forEach { category ->
                    AnalyticsCategoryRow(
                        category = category
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCategoryRow(
    category: DashboardCategorySpending
) {
    val limit = category.limitAmount ?: 0.0
    val progress = category.progress
    val categoryColor = analyticsCategoryColor(category)
    val progressColor = when {
        progress >= 1f -> DangerColor
        progress >= 0.8f -> WarningColor
        else -> categoryColor
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(progressColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = if (limit > 0.0) {
                    "${category.spentAmount.formatMoney()}€ / ${limit.formatMoney()}€"
                } else {
                    "${category.spentAmount.formatMoney()}€"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = progressColor,
            trackColor = SoftAccent
        )
        Text(
            text = when {
                limit <= 0.0 -> "Limit še ni nastavljen"
                progress >= 1f -> "Limit presežen"
                progress >= 0.8f -> "Blizu limita"
                else -> "${(progress * 100).toInt()}% porabljeno"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (progress >= 0.8f && limit > 0.0) progressColor else MutedInk
        )
    }
}

@Composable
private fun AiRecommendationCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 54.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LimeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Ink, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AI Priporočilo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = "Kliknite gumb, da generirate vaše poročilo in priporočila.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
            }
            Button(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = 24.dp)
                    .size(42.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SpendingChartCard(categories: List<DashboardCategorySpending>) {
    val bars = categories.filter { it.spentAmount > 0.0 }.take(6)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Graf porabe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            if (bars.isEmpty()) {
                EmptyText("Graf se prikaže, ko dodaš prve stroške.")
            } else {
                SpendingBarChart(
                    categories = bars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                ChartLabels(categories = bars)
            }
        }
    }
}

@Composable
private fun SpendingBarChart(
    categories: List<DashboardCategorySpending>,
    modifier: Modifier = Modifier
) {
    val maxSpent = categories.maxOfOrNull { it.spentAmount }?.coerceAtLeast(1.0) ?: 1.0
    Canvas(modifier = modifier) {
        val gap = 18.dp.toPx()
        val barWidth = ((size.width - gap * (categories.size - 1)) / categories.size).coerceAtLeast(10.dp.toPx())
        categories.forEachIndexed { index, category ->
            val barHeight = ((category.spentAmount / maxSpent) * size.height).toFloat().coerceAtLeast(8.dp.toPx())
            val x = index * (barWidth + gap)
            drawRoundRect(
                color = analyticsCategoryColor(category),
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }
    }
}

@Composable
private fun ChartLabels(categories: List<DashboardCategorySpending>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(analyticsCategoryColor(category))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${category.spentAmount.formatMoney()}€",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MutedInk,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MutedInk
    )
}

private fun analyticsCategoryColor(category: DashboardCategorySpending): Color {
    return budzetkoCategoryColor(
        categoryId = category.categoryId,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )
}

private fun Double.formatMoney(): String = "%.2f".format(this)

@Preview(showBackground = true)
@Composable
private fun AnalyticsContentPreview() {
    BudzetkoTheme {
        AnalyticsContent(
            uiState = DashboardUiState(
                month = 5,
                year = 2026,
                totalBudget = 2_000.0,
                totalSpent = 715.86,
                categorySpending = listOf(
                    DashboardCategorySpending(1, "Hrana", "🍓", 0, 217.0, 600.0),
                    DashboardCategorySpending(2, "Stroški", "🚨", 1, 180.0, 300.0),
                    DashboardCategorySpending(3, "Drugo", "🎁", 2, 314.0, 200.0)
                ),
                recentTransactions = listOf(
                    DashboardTransaction(
                        expense = ExpenseEntity(
                            amount = 12.60,
                            date = System.currentTimeMillis(),
                            description = "Kino",
                            userId = "demo-user",
                            categoryId = 3
                        ),
                        categoryName = "Zabava",
                        categoryEmoji = "🎁",
                        categoryColorIndex = 2
                    )
                )
            ),
            onProfileClick = {},
            onHomeClick = {},
            onTransactionsClick = {},
            onCategorySettingsClick = {},
            onAddExpenseClick = {},
            onSettingsClick = {}
        )
    }
}
