package si.um.feri.budzetko.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.currency.formatCurrencyAmount
import si.um.feri.budzetko.ui.theme.BudzetkoLime
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.budzetkoBackground
import si.um.feri.budzetko.ui.theme.budzetkoInk
import si.um.feri.budzetko.ui.theme.budzetkoMutedInk
import si.um.feri.budzetko.ui.theme.budzetkoSoftAccent
import si.um.feri.budzetko.ui.theme.budzetkoSurface
import si.um.feri.budzetko.viewmodel.BudgetHistoryItem
import si.um.feri.budzetko.viewmodel.BudgetHistoryViewModel
import si.um.feri.budzetko.viewmodel.monthName

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
private val WarningAccent = Color(0xFFFF7D8A)

@Composable
fun BudgetHistoryScreen(
    viewModel: BudgetHistoryViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onMonthTransactionsClick: (month: Int, year: Int) -> Unit,
    onAddExpenseClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
            contentPadding = PaddingValues(start = 22.dp, top = 28.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BudgetHistoryHeader(onBackClick = onBackClick)
            }
            item {
                BudgetHistorySearch(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchChange
                )
            }

            if (uiState.items.isEmpty()) {
                item { EmptyHistoryCard() }
            } else {
                uiState.items.groupBy { it.year }.forEach { (year, items) ->
                    item {
                        Text(
                            text = year.toString(),
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MutedInk
                        )
                    }
                    items(items, key = { it.monthKey }) { item ->
                        BudgetHistoryMonthCard(
                            item = item,
                            isExpanded = uiState.expandedMonthKey == item.monthKey,
                            onClick = { viewModel.toggleMonth(item.month, item.year) },
                            onMonthTransactionsClick = onMonthTransactionsClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetHistoryHeader(onBackClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardSurface)
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBackIosNew,
                contentDescription = "Nazaj",
                tint = Ink,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Zgodovina proračunov",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = "Pregled mesecev in AI povzetkov",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
    }
}

@Composable
private fun BudgetHistorySearch(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(32.dp), tint = Ink)
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Išči mesec ali AI povzetek...") },
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        IconButton(
            onClick = {},
            modifier = Modifier.size(42.dp)
        ) {
            Icon(Icons.Outlined.FilterList, contentDescription = "Filter", modifier = Modifier.size(28.dp), tint = Ink)
        }
    }
}

@Composable
private fun BudgetHistoryMonthCard(
    item: BudgetHistoryItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onMonthTransactionsClick: (month: Int, year: Int) -> Unit
) {
    val progressColor = when {
        item.progressPercent >= 100 -> WarningAccent
        item.progressPercent >= 85 -> Color(0xFFFFB864)
        else -> LimeAccent
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(progressColor.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(progressColor)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = monthName(item.month),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = "Porabljeno: ${formatCurrencyAmount(item.spent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
                Text(
                    text = "${item.progressPercent} %",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(18.dp)
                )
            }
            LinearProgressIndicator(
                progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = progressColor,
                trackColor = SoftAccent
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                BudgetHistoryDetails(
                    item = item,
                    onMonthTransactionsClick = onMonthTransactionsClick
                )
            }
        }
    }
}

@Composable
private fun BudgetHistoryDetails(
    item: BudgetHistoryItem,
    onMonthTransactionsClick: (month: Int, year: Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HistoryStatPill(
                modifier = Modifier.weight(1f),
                title = "Proračun",
                value = formatCurrencyAmount(item.income)
            )
            HistoryStatPill(
                modifier = Modifier.weight(1f),
                title = "Preostanek",
                value = formatCurrencyAmount(item.remaining)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = SoftAccent.copy(alpha = 0.72f)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(LimeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AI povzetek",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Text(
                        text = item.aiSummary ?: "Za ta mesec AI povzetek še ni shranjen.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
            }
        }
        Button(
            onClick = { onMonthTransactionsClick(item.month, item.year) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryAccent,
                contentColor = Color.White
            )
        ) {
            Text(text = "Prikaži stroške", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun HistoryStatPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SoftAccent
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MutedInk
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardSurface
    ) {
        Text(
            text = "Zgodovina je trenutno prazna. Ko shraniš mesečni proračun, se bo prikazal tukaj.",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MutedInk
        )
    }
}

private fun Double.formatMoney(): String = "%.2f".format(this)
