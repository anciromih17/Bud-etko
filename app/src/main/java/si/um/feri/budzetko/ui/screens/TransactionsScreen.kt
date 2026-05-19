package si.um.feri.budzetko.ui.screens

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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.ui.components.BudzetkoBottomBar
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel

private val ScreenBackground = Color(0xFFF7F4EE)
private val CardSurface = Color(0xFFFFFFFF)
private val PrimaryAccent = Color(0xFF156C6A)
private val SoftAccent = Color(0xFFE9F3F2)
private val Ink = Color(0xFF191B1F)
private val MutedInk = Color(0xFF71706A)
private val Danger = Color(0xFFB3261E)
private val DateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
fun TransactionsScreen(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel,
    onHomeClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onSettingsClick: () -> Unit
) {
    val expenses by expenseViewModel.expenses.collectAsState()
    val categoryState by categoryViewModel.uiState.collectAsState()
    val categoriesById = categoryState.categories.associateBy { it.id }
    var search by remember { mutableStateOf("") }

    val filteredExpenses = expenses.filter { expense ->
        val category = categoriesById[expense.categoryId]
        search.isBlank() ||
            expense.description.contains(search, ignoreCase = true) ||
            category?.name?.contains(search, ignoreCase = true) == true
    }
    val totalSpent = filteredExpenses.sumOf { it.amount }
    val groupedExpenses = filteredExpenses.groupBy { it.dateLabel() }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            BudzetkoBottomBar(
                onHomeClick = onHomeClick,
                onBudgetClick = {},
                onAddExpenseClick = onAddExpenseClick,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TransactionsHeader() }
            item { TotalSpentCard(totalSpent = totalSpent) }
            item {
                SearchRow(
                    search = search,
                    onSearchChange = { search = it }
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
                            onDeleteExpenseClick = expenseViewModel::deleteExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsHeader() {
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
            onClick = {},
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
        modifier = Modifier.fillMaxWidth(),
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
private fun SearchRow(
    search: String,
    onSearchChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(34.dp), tint = Ink)
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedTextField(
            value = search,
            onValueChange = onSearchChange,
            placeholder = { Text("Išči...") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(32.dp), tint = Ink)
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            expenses.forEach { expense ->
                TransactionRow(
                    expense = expense,
                    category = categoriesById[expense.categoryId],
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
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(SoftAccent),
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

private fun ExpenseEntity.dateLabel(): String {
    return Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateFormatter)
}

private fun Double.formatMoney(): String = "%.2f".format(this)
