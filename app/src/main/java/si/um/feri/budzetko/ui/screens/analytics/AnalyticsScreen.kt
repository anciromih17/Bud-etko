package si.um.feri.budzetko.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.currency.formatCurrencyAmount
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoCategoryColor
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSoftAccent
import si.um.feri.budzetko.ui.theme.budzetkoSurface
import si.um.feri.budzetko.viewmodel.DashboardCategorySpending
import si.um.feri.budzetko.viewmodel.DashboardTransaction
import si.um.feri.budzetko.viewmodel.DashboardUiState
import si.um.feri.budzetko.viewmodel.AnalyticsViewModel

private val CardSurface: Color
    @Composable get() = budzetkoSurface()
private val Ink: Color
    @Composable get() = budzetkoInk()
private val MutedInk: Color
    @Composable get() = budzetkoMutedInk()
private val PrimaryAccent = BudzetkoPurple
private val LimeAccent = BudzetkoLime
private val SoftAccent: Color
    @Composable get() = budzetkoSoftAccent()
private val WarningColor = Color(0xFFFFB864)
private val DangerColor = Color(0xFFFF7D8A)

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onCategorySettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (month: Int, year: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.setMonth(selectedMonth, selectedYear)
    }

    val uiState by viewModel.uiState.collectAsState()
    var isAiRecommendationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.aiSummary) {
        if (uiState.aiSummary != null) {
            isAiRecommendationVisible = true
        }
    }

    AnalyticsContent(
        uiState = uiState,
        aiRecommendation = uiState.aiSummary,
        isAiRecommendationVisible = isAiRecommendationVisible,
        onGenerateAiRecommendation = {
            viewModel.generateAndSaveAiSummary(uiState)
            isAiRecommendationVisible = true
        },
        onDismissAiRecommendation = {
            isAiRecommendationVisible = false
        },
        onPreviousMonthClick = {
            val previous = previousMonth(uiState.month, uiState.year)
            onMonthChange(previous.first, previous.second)
        },
        onNextMonthClick = {
            val next = nextMonth(uiState.month, uiState.year)
            onMonthChange(next.first, next.second)
        },
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
    aiRecommendation: String?,
    isAiRecommendationVisible: Boolean,
    onGenerateAiRecommendation: () -> Unit,
    onDismissAiRecommendation: () -> Unit,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
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
        containerColor = budzetkoBackground(),
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
                .background(budzetkoBackground())
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, top = 34.dp, end = 24.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                AnalyticsHeader(onProfileClick = onProfileClick)
            }
            item {
                AnalyticsMonthSelector(
                    month = uiState.month,
                    year = uiState.year,
                    onPreviousClick = onPreviousMonthClick,
                    onNextClick = onNextMonthClick
                )
            }
            item {
                AnalyticsSummaryRow(uiState = uiState)
            }
            item {
                LimitAlertsCard(categories = uiState.categorySpending)
            }
            item {
                CategoryDistributionCard(
                    categories = uiState.categorySpending,
                    onEditClick = onCategorySettingsClick
                )
            }
            item {
                AiRecommendationCard(
                    recommendation = aiRecommendation,
                    recommendationSource = uiState.aiSummarySource,
                    isRecommendationVisible = isAiRecommendationVisible,
                    isGenerating = uiState.isAiSummaryGenerating,
                    onGenerateClick = onGenerateAiRecommendation,
                    onDismissClick = onDismissAiRecommendation
                )
            }
            item {
                SpendingChartCard(categories = uiState.categorySpending)
            }
        }
    }
}

@Composable
private fun AnalyticsMonthSelector(
    month: Int,
    year: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = CardSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(budzetkoBackground())
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = stringResource(R.string.budget_month_previous),
                    tint = Ink
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = monthYearLabel(month, year),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Text(
                    text = stringResource(R.string.analytics_selected_month),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MutedInk
                )
            }
            IconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(budzetkoBackground())
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.budget_month_next),
                    tint = Ink
                )
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
                text = stringResource(R.string.analytics_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = stringResource(R.string.analytics_subtitle),
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
            Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.profile), tint = Ink, modifier = Modifier.size(30.dp))
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
    val overLimitCount = uiState.categorySpending.count { it.hasLimit && it.progress >= 1f }
    val nearLimitCount = uiState.categorySpending.count { it.hasLimit && it.progress >= 0.8f && it.progress < 1f }
    val warningCount = overLimitCount + nearLimitCount
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.ArrowOutward,
            title = stringResource(R.string.analytics_spending),
            value = formatCurrencyAmount(uiState.totalSpent),
            subtitle = stringResource(R.string.analytics_budget_percent, budgetUsage.toInt()),
            iconBackground = PrimaryAccent
        )
        AnalyticsMetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.ErrorOutline,
            title = stringResource(R.string.analytics_alerts),
            value = warningCount.toString(),
            subtitle = stringResource(R.string.analytics_alert_counts, overLimitCount, nearLimitCount),
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
private fun LimitAlertsCard(categories: List<DashboardCategorySpending>) {
    val overLimitCategories = categories.filter { it.hasLimit && it.progress >= 1f }
    val nearLimitCategories = categories.filter { it.hasLimit && it.progress >= 0.8f && it.progress < 1f }
    if (overLimitCategories.isEmpty() && nearLimitCategories.isEmpty()) {
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (overLimitCategories.isNotEmpty()) DangerColor else WarningColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = alertTitle(overLimitCategories.size, nearLimitCategories.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = stringResource(R.string.analytics_review_limits),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
            }

            overLimitCategories.take(2).forEach { category ->
                AlertCategoryLine(
                    category = category,
                    color = DangerColor,
                    message = stringResource(R.string.analytics_exceeded_by, formatCurrencyAmount(category.spentAmount - (category.limitAmount ?: 0.0)))
                )
            }
            nearLimitCategories.take(2).forEach { category ->
                AlertCategoryLine(
                    category = category,
                    color = WarningColor,
                    message = stringResource(R.string.analytics_available_left, formatCurrencyAmount(((category.limitAmount ?: 0.0) - category.spentAmount).coerceAtLeast(0.0)))
                )
            }
        }
    }
}

@Composable
private fun AlertCategoryLine(
    category: DashboardCategorySpending,
    color: Color,
    message: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.categoryName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
                    text = stringResource(R.string.analytics_distribution),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.settings_category_settings),
                    tint = Ink,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onEditClick)
                )
            }

            if (visibleCategories.isEmpty()) {
                EmptyText(stringResource(R.string.analytics_empty_distribution))
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
                    "${formatCurrencyAmount(category.spentAmount)} / ${formatCurrencyAmount(limit)}"
                } else {
                    formatCurrencyAmount(category.spentAmount)
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
                limit <= 0.0 -> stringResource(R.string.analytics_limit_not_set)
                progress >= 1f -> stringResource(R.string.analytics_limit_exceeded_by, formatCurrencyAmount(category.spentAmount - limit))
                progress >= 0.8f -> stringResource(R.string.analytics_near_limit_left, formatCurrencyAmount((limit - category.spentAmount).coerceAtLeast(0.0)))
                else -> stringResource(R.string.analytics_percent_spent, (progress * 100).toInt())
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (progress >= 0.8f && limit > 0.0) progressColor else MutedInk
        )
    }
}

@Composable
private fun AiRecommendationCard(
    recommendation: String?,
    recommendationSource: AiSummarySource?,
    isRecommendationVisible: Boolean,
    isGenerating: Boolean,
    onGenerateClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(34.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LimeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF050505),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification_ai_recommendation),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = stringResource(R.string.analytics_ai_hint),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (recommendation != null && isRecommendationVisible) {
                    IconButton(
                        onClick = onDismissClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoftAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.analytics_hide_recommendation),
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onGenerateClick,
                    enabled = !isGenerating,
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent, contentColor = Color.White)
                ) {
                    if (isGenerating) {
                        Text(text = "...", fontWeight = FontWeight.ExtraBold)
                    } else {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                    }
                }
            }
            AnimatedVisibility(
                visible = recommendation != null && isRecommendationVisible,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AiRecommendationSourceLabel(source = recommendationSource)
                    AiRecommendationList(text = recommendation.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun AiRecommendationSourceLabel(source: AiSummarySource?) {
    val text = when (source) {
        AiSummarySource.GEMINI -> stringResource(R.string.ai_source_gemini)
        AiSummarySource.FALLBACK -> stringResource(R.string.ai_source_fallback)
        null -> return
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (source == AiSummarySource.GEMINI) LimeAccent.copy(alpha = 0.55f) else SoftAccent
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
    }
}

@Composable
private fun AiRecommendationList(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        text.lines()
            .filter { it.isNotBlank() }
            .forEach { line ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(LimeAccent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = line.highlightImportantParts(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Ink
                    )
                }
            }
    }
}

private fun String.highlightImportantParts() = buildAnnotatedString {
    append(this@highlightImportantParts)
    val boldStyle = SpanStyle(fontWeight = FontWeight.ExtraBold)
    listOf(
        Regex("""(?:€|\$|£)?\d+(?:\.\d+)?(?:€|\$|£)?"""),
        Regex("""\d+%"""),
        Regex("""kategoriji\s+([^:]+)"""),
        Regex("""Kategorija\s+(.+?)\s+(?:je|ima)""")
    ).forEach { regex ->
        regex.findAll(this@highlightImportantParts).forEach { match ->
            if (match.groups.size > 1 && match.groups[1] != null) {
                val group = match.groups[1]!!
                addStyle(boldStyle, group.range.first, group.range.last + 1)
            } else {
                addStyle(boldStyle, match.range.first, match.range.last + 1)
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
                text = stringResource(R.string.analytics_spending_chart),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            if (bars.isEmpty()) {
                EmptyText(stringResource(R.string.analytics_empty_chart))
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
                    text = formatCurrencyAmount(category.spentAmount),
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

private val DashboardCategorySpending.hasLimit: Boolean
    get() = limitAmount != null && limitAmount > 0.0

@Composable
private fun alertTitle(overLimitCount: Int, nearLimitCount: Int): String {
    return when {
        overLimitCount > 0 && nearLimitCount > 0 ->
            stringResource(R.string.analytics_alert_title_both, overLimitCount, nearLimitCount)
        overLimitCount > 0 ->
            stringResource(R.string.analytics_alert_title_over, overLimitCount)
        else ->
            stringResource(R.string.analytics_alert_title_near, nearLimitCount)
    }
}

private fun Double.formatMoney(): String = "%.2f".format(this)

private fun previousMonth(month: Int, year: Int): Pair<Int, Int> {
    return if (month == 1) 12 to year - 1 else month - 1 to year
}

private fun nextMonth(month: Int, year: Int): Pair<Int, Int> {
    return if (month == 12) 1 to year + 1 else month + 1 to year
}

@Composable
private fun monthYearLabel(month: Int, year: Int): String {
    val monthName = when (month) {
        1 -> stringResource(R.string.month_january)
        2 -> stringResource(R.string.month_february)
        3 -> stringResource(R.string.month_march)
        4 -> stringResource(R.string.month_april)
        5 -> stringResource(R.string.month_may)
        6 -> stringResource(R.string.month_june)
        7 -> stringResource(R.string.month_july)
        8 -> stringResource(R.string.month_august)
        9 -> stringResource(R.string.month_september)
        10 -> stringResource(R.string.month_october)
        11 -> stringResource(R.string.month_november)
        12 -> stringResource(R.string.month_december)
        else -> ""
    }
    return "$monthName $year"
}

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
            aiRecommendation = null,
            isAiRecommendationVisible = false,
            onGenerateAiRecommendation = {},
            onDismissAiRecommendation = {},
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onProfileClick = {},
            onHomeClick = {},
            onTransactionsClick = {},
            onCategorySettingsClick = {},
            onAddExpenseClick = {},
            onSettingsClick = {}
        )
    }
}
