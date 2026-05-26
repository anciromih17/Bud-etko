package si.um.feri.budzetko.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import si.um.feri.budzetko.R
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.ui.theme.BudzetkoBackground
import si.um.feri.budzetko.ui.theme.BudzetkoInk
import si.um.feri.budzetko.ui.theme.BudzetkoPurple
import si.um.feri.budzetko.ui.theme.BudzetkoSurface
import si.um.feri.budzetko.ui.theme.budzetkoCategoryColor
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel

private val ScreenBackground = BudzetkoBackground
private val CardSurface = BudzetkoSurface
private val PrimaryAccent = BudzetkoPurple
private val SoftAccent = Color(0xFFF4F0FF)
private val Ink = BudzetkoInk
private val MutedInk = Color(0xFF71706A)
private val Danger = Color(0xFFB3261E)
private val DateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private enum class ExpenseSortMode(val label: String) {
    NEWEST("Najnovejše"),
    OLDEST("Najstarejše"),
    AMOUNT_HIGH("Najvišji znesek"),
    AMOUNT_LOW("Najnižji znesek");

    fun next(): ExpenseSortMode {
        return entries[(ordinal + 1) % entries.size]
    }
}

@Composable
fun TransactionsScreen(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel,
    onHomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onProfileClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    initialMonth: Int? = null,
    initialYear: Int? = null,
    selectedMonthFilter: Int? = initialMonth,
    selectedYearFilter: Int? = initialYear,
    onMonthFilterChange: (month: Int?, year: Int?) -> Unit = { _, _ -> }
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val categoryState by categoryViewModel.uiState.collectAsState()
    val categories = categoryState.categories.sortedBy { it.name.lowercase() }
    val categoriesById = categories.associateBy { it.id }
    var search by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(selectedMonthFilter) }
    var selectedYear by remember { mutableStateOf<Int?>(selectedYearFilter) }
    var sortMode by remember { mutableStateOf(ExpenseSortMode.NEWEST) }
    var areFiltersExpanded by remember { mutableStateOf(false) }
    var expensePendingDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    LaunchedEffect(selectedMonthFilter, selectedYearFilter) {
        selectedMonth = selectedMonthFilter
        selectedYear = selectedYearFilter
    }

    val availableMonths = expenses
        .map { it.monthYear() }
        .distinct()
        .sortedWith(compareByDescending<MonthYear> { it.year }.thenByDescending { it.month })

    val filteredExpenses = expenses.filter { expense ->
        val category = categoriesById[expense.categoryId]
        val matchesSearch = search.isBlank() ||
            expense.description.contains(search, ignoreCase = true) ||
            category?.name?.contains(search, ignoreCase = true) == true
        val matchesCategory = selectedCategoryId == null || expense.categoryId == selectedCategoryId
        val matchesMonth = selectedMonth == null ||
            (expense.monthYear().month == selectedMonth && expense.monthYear().year == selectedYear)
        matchesSearch && matchesCategory && matchesMonth
    }.sortedWith(sortMode.comparator())
    val totalSpent = filteredExpenses.sumOf { it.amount }
    val groupedExpenses = filteredExpenses.groupBy { it.dateLabel() }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = onHomeClick,
                onBudgetClick = {},
                onAddExpenseClick = onAddExpenseClick,
                onAnalyticsClick = onAnalyticsClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 22.dp, top = 30.dp, end = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TransactionsHeader(onProfileClick = onProfileClick) }
            item { TotalSpentCard(totalSpent = totalSpent) }
            item {
                TransactionControls(
                    search = search,
                    sortMode = sortMode,
                    areFiltersExpanded = areFiltersExpanded,
                    availableMonths = availableMonths,
                    categories = categories,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    selectedCategoryId = selectedCategoryId,
                    selectedCategory = selectedCategoryId?.let { categoriesById[it] },
                    onSearchChange = { search = it },
                    onSortClick = { sortMode = sortMode.next() },
                    onFilterClick = { areFiltersExpanded = !areFiltersExpanded },
                    onClearMonth = {
                        selectedMonth = null
                        selectedYear = null
                        onMonthFilterChange(null, null)
                    },
                    onClearCategory = { selectedCategoryId = null },
                    onMonthSelected = { monthYear ->
                        selectedMonth = monthYear?.month
                        selectedYear = monthYear?.year
                        onMonthFilterChange(monthYear?.month, monthYear?.year)
                    },
                    onCategorySelected = { selectedCategoryId = it }
                )
            }
            if (filteredExpenses.isEmpty()) {
                item { EmptyTransactionsCard() }
            } else {
                groupedExpenses.forEach { (date, dateExpenses) ->
                    item {
                        Text(
                            text = date,
                            modifier = Modifier.padding(start = 18.dp, top = 4.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MutedInk
                        )
                    }
                    item {
                        TransactionsGroupCard(
                            expenses = dateExpenses,
                            categoriesById = categoriesById,
                            onEditExpenseClick = onEditExpenseClick,
                            onDeleteExpenseClick = { expensePendingDelete = it }
                        )
                    }
                }
            }
        }
    }

    expensePendingDelete?.let { expense ->
        DeleteExpenseDialog(
            expenseDescription = expense.description.lineSequence().firstOrNull().orEmpty(),
            onConfirm = {
                expenseViewModel.deleteExpense(expense)
                expensePendingDelete = null
            },
            onDismiss = {
                expensePendingDelete = null
            }
        )
    }
}

@Composable
private fun TransactionsHeader(onProfileClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Transakcije",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = "Pregled vseh stroškov",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardSurface)
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Ink)
        }
    }
}

@Composable
private fun TotalSpentCard(totalSpent: Double) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
            Text(
                text = "Skupaj porabljeno",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MutedInk
            )
            Text(
                text = "${totalSpent.formatMoney()}€",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }
    }
}

@Composable
private fun TransactionControls(
    search: String,
    sortMode: ExpenseSortMode,
    areFiltersExpanded: Boolean,
    availableMonths: List<MonthYear>,
    categories: List<CategoryEntity>,
    selectedMonth: Int?,
    selectedYear: Int?,
    selectedCategoryId: Long?,
    selectedCategory: CategoryEntity?,
    onSearchChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearMonth: () -> Unit,
    onClearCategory: () -> Unit,
    onMonthSelected: (MonthYear?) -> Unit,
    onCategorySelected: (Long?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SearchRow(
            search = search,
            sortMode = sortMode,
            areFiltersExpanded = areFiltersExpanded,
            onSearchChange = onSearchChange,
            onSortClick = onSortClick,
            onFilterClick = onFilterClick
        )
        ActiveFilterRow(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            selectedCategory = selectedCategory,
            sortMode = sortMode,
            onClearMonth = onClearMonth,
            onClearCategory = onClearCategory
        )
        AnimatedVisibility(
            visible = areFiltersExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            FilterPanel(
                availableMonths = availableMonths,
                categories = categories,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                selectedCategoryId = selectedCategoryId,
                onMonthSelected = onMonthSelected,
                onCategorySelected = onCategorySelected
            )
        }
    }
}

@Composable
private fun SearchRow(
    search: String,
    sortMode: ExpenseSortMode,
    areFiltersExpanded: Boolean,
    onSearchChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(32.dp), tint = Ink)
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            placeholder = { Text("Išči...") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onFilterClick, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Outlined.FilterList,
                contentDescription = "Filtri",
                modifier = Modifier.size(26.dp),
                tint = if (areFiltersExpanded) PrimaryAccent else Ink
            )
        }
        IconButton(onClick = onSortClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.SwapVert, contentDescription = "Razvrsti: ${sortMode.label}", modifier = Modifier.size(26.dp), tint = Ink)
        }
    }
}

@Composable
private fun ActiveFilterRow(
    selectedMonth: Int?,
    selectedYear: Int?,
    selectedCategory: CategoryEntity?,
    sortMode: ExpenseSortMode,
    onClearMonth: () -> Unit,
    onClearCategory: () -> Unit
) {
    val hasMonth = selectedMonth != null && selectedYear != null
    val hasCategory = selectedCategory != null
    if (!hasMonth && !hasCategory && sortMode == ExpenseSortMode.NEWEST) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        if (hasMonth) {
            item {
                CompactInfoChip(
                    text = "${monthName(selectedMonth!!)} $selectedYear",
                    onClick = onClearMonth
                )
            }
        }
        if (hasCategory) {
            item {
                CompactInfoChip(
                    text = selectedCategory!!.name,
                    color = transactionIconBackground(selectedCategory),
                    onClick = onClearCategory
                )
            }
        }
        if (sortMode != ExpenseSortMode.NEWEST) {
            item {
                CompactInfoChip(text = sortMode.label)
            }
        }
    }
}

@Composable
private fun CompactInfoChip(
    text: String,
    color: Color = PrimaryAccent.copy(alpha = 0.28f),
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.65f)
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
private fun FilterPanel(
    availableMonths: List<MonthYear>,
    categories: List<CategoryEntity>,
    selectedMonth: Int?,
    selectedYear: Int?,
    selectedCategoryId: Long?,
    onMonthSelected: (MonthYear?) -> Unit,
    onCategorySelected: (Long?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MonthFilterRow(
                availableMonths = availableMonths,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthSelected = onMonthSelected
            )
            CategoryFilterRow(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected
            )
        }
    }
}

@Composable
private fun MonthFilterRow(
    availableMonths: List<MonthYear>,
    selectedMonth: Int?,
    selectedYear: Int?,
    onMonthSelected: (MonthYear?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedMonth == null,
                onClick = { onMonthSelected(null) },
                label = { Text(text = "Vsi meseci") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Ink,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(availableMonths, key = { "${it.year}-${it.month}" }) { monthYear ->
            FilterChip(
                selected = selectedMonth == monthYear.month && selectedYear == monthYear.year,
                onClick = { onMonthSelected(monthYear) },
                label = { Text(text = "${monthName(monthYear.month)} ${monthYear.year}") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryAccent.copy(alpha = 0.18f),
                    selectedLabelColor = Ink
                )
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
                label = { Text(text = "Vse") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Ink,
                    selectedLabelColor = Color.White
                )
            )
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(text = category.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(transactionIconBackground(category))
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryAccent.copy(alpha = 0.18f),
                    selectedLabelColor = Ink
                )
            )
        }
    }
}

@Composable
private fun TransactionsGroupCard(
    expenses: List<ExpenseEntity>,
    categoriesById: Map<Long, CategoryEntity>,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(30.dp),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            expenses.forEach { expense ->
                TransactionRow(
                    expense = expense,
                    category = categoriesById[expense.categoryId],
                    iconBackground = transactionIconBackground(categoriesById[expense.categoryId]),
                    onEditExpenseClick = onEditExpenseClick,
                    onDeleteExpenseClick = onDeleteExpenseClick
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    expense: ExpenseEntity,
    category: CategoryEntity?,
    iconBackground: Color,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBackground.copy(alpha = 0.70f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category?.emoji ?: "●")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = expense.description.lineSequence().firstOrNull().orEmpty(), fontWeight = FontWeight.Bold, color = Ink)
            Text(text = category?.name ?: "Brez kategorije", style = MaterialTheme.typography.bodySmall, color = MutedInk)
        }
        Text(text = "-${expense.amount.formatMoney()}€", fontWeight = FontWeight.ExtraBold, color = Ink)
        IconButton(onClick = { onEditExpenseClick(expense) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Edit, contentDescription = "Uredi", tint = PrimaryAccent, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { onDeleteExpenseClick(expense) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Delete, contentDescription = "Izbriši", tint = Danger, modifier = Modifier.size(18.dp))
        }
    }
}

private fun transactionIconBackground(category: CategoryEntity?): Color {
    if (category == null) return SoftAccent
    return budzetkoCategoryColor(
        categoryId = category.id,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )
}

private fun ExpenseSortMode.comparator(): Comparator<ExpenseEntity> {
    return when (this) {
        ExpenseSortMode.NEWEST -> compareByDescending<ExpenseEntity> { it.date }
            .thenByDescending { it.updatedAt }
        ExpenseSortMode.OLDEST -> compareBy<ExpenseEntity> { it.date }
            .thenBy { it.updatedAt }
        ExpenseSortMode.AMOUNT_HIGH -> compareByDescending<ExpenseEntity> { it.amount }
            .thenByDescending { it.date }
        ExpenseSortMode.AMOUNT_LOW -> compareBy<ExpenseEntity> { it.amount }
            .thenByDescending { it.date }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = CardSurface) {
        Text(
            text = "Ni še dodanih stroškov.",
            modifier = Modifier.padding(20.dp),
            color = MutedInk
        )
    }
}

@Composable
private fun DeleteExpenseDialog(
    expenseDescription: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(
                text = stringResource(R.string.delete_expense_title),
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_expense_message,
                    expenseDescription.ifBlank { stringResource(R.string.expense_without_description) }
                ),
                color = MutedInk
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Danger,
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

private fun ExpenseEntity.dateLabel(): String {
    return Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateFormatter)
}

private fun ExpenseEntity.monthYear(): MonthYear {
    val localDate = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return MonthYear(month = localDate.monthValue, year = localDate.year)
}

private data class MonthYear(
    val month: Int,
    val year: Int
)

private fun monthName(month: Int): String {
    return when (month) {
        1 -> "Januar"
        2 -> "Februar"
        3 -> "Marec"
        4 -> "April"
        5 -> "Maj"
        6 -> "Junij"
        7 -> "Julij"
        8 -> "Avgust"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "December"
        else -> month.toString()
    }
}

private fun Double.formatMoney(): String = "%.2f".format(this)
