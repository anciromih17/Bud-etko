package si.um.feri.budzetko.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel,
    expenseToEdit: ExpenseEntity?,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onAddCategoryClick: () -> Unit
) {
    val categoryState by categoryViewModel.uiState.collectAsState()
    val categories = categoryState.categories

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(LocalDate.now().format(DateFormatter)) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(expenseToEdit?.id, categories) {
        if (expenseToEdit == null) {
            amount = ""
            description = ""
            note = ""
            dateInput = LocalDate.now().format(DateFormatter)
            selectedCategoryId = selectedCategoryId ?: categories.firstOrNull()?.id
            errorMessage = null
        } else {
            amount = expenseToEdit.amount.toString()
            description = expenseToEdit.description
            note = ""
            dateInput = Instant.ofEpochMilli(expenseToEdit.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateFormatter)
            selectedCategoryId = expenseToEdit.categoryId
            errorMessage = null
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClose
                )
                .padding(horizontal = 20.dp, vertical = 28.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(30.dp),
                color = CardSurface,
                shadowElevation = 12.dp
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item { AddExpenseHeader(onClose = onClose) }
                    item {
                        AddExpenseFormCard(
                            isEditing = expenseToEdit != null,
                            amount = amount,
                            description = description,
                            note = note,
                            dateInput = dateInput,
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            errorMessage = errorMessage,
                            onAmountChange = {
                                amount = it.filter { char -> char.isDigit() || char == '.' || char == ',' }
                                errorMessage = null
                            },
                            onDescriptionChange = {
                                description = it
                                errorMessage = null
                            },
                            onNoteChange = { note = it },
                            onDateChange = {
                                dateInput = it
                                errorMessage = null
                            },
                            onDatePickerClick = {
                                isDatePickerOpen = true
                            },
                            onCategorySelected = {
                                selectedCategoryId = it
                                errorMessage = null
                            },
                            onAddCategoryClick = onAddCategoryClick,
                            onSaveClick = {
                                val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
                                val parsedDate = parseDate(dateInput)
                                val categoryId = selectedCategoryId

                                when {
                                    parsedAmount == null || parsedAmount <= 0.0 ->
                                        errorMessage = "Vnesi veljaven znesek."

                                    description.isBlank() ->
                                        errorMessage = "Vnesi opis stroška."

                                    categoryId == null ->
                                        errorMessage = "Izberi kategorijo."

                                    parsedDate == null ->
                                        errorMessage = "Datum vnesi v obliki dd.MM.yyyy."

                                    else -> {
                                        val dateMillis = parsedDate
                                            .atStartOfDay(ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                        val fullDescription = if (note.isBlank()) {
                                            description.trim()
                                        } else {
                                            "${description.trim()}\n${note.trim()}"
                                        }

                                        if (expenseToEdit == null) {
                                            expenseViewModel.addExpense(
                                                amount = parsedAmount,
                                                date = dateMillis,
                                                description = fullDescription,
                                                categoryId = categoryId
                                            )
                                        } else {
                                            expenseViewModel.updateExpense(
                                                expense = expenseToEdit,
                                                amount = parsedAmount,
                                                date = dateMillis,
                                                description = fullDescription,
                                                categoryId = categoryId
                                            )
                                        }
                                        onSaved()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isDatePickerOpen) {
        val selectedDateMillis = parseDate(dateInput)
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dateInput = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateFormatter)
                            errorMessage = null
                        }
                        isDatePickerOpen = false
                    }
                ) {
                    Text("Izberi")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Prekliči")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AddExpenseHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Dobrodošli!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Text(
                text = "Dodaj nov strošek",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MutedInk
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(CardSurface)
        ) {
            Icon(Icons.Outlined.Close, contentDescription = "Zapri", tint = Ink)
        }
    }
}

@Composable
private fun AddExpenseFormCard(
    isEditing: Boolean,
    amount: String,
    description: String,
    note: String,
    dateInput: String,
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    errorMessage: String?,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAddCategoryClick: () -> Unit,
    onSaveClick: () -> Unit
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
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Dodaj strošek",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Znesek (€)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Opis") },
                placeholder = { Text("Npr. Nakup v trgovini") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Kategorija",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(186.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(categories.take(5), key = { it.id }) { category ->
                    CategoryTile(
                        category = category,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
                item {
                    AddCategoryTile(onClick = onAddCategoryClick)
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = onDateChange,
                    label = { Text("Datum") },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDatePickerClick
                        )
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Opombe (opcijsko)") },
                placeholder = { Text("Dodatne informacije...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(text = it, color = Danger, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isEditing) "Shrani spremembe" else "Dodaj strošek", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Skeniraj račun", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = budzetkoCategoryColor(
        categoryId = category.id,
        colorIndex = category.colorIndex,
        hasEmoji = !category.emoji.isNullOrBlank()
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) categoryColor.copy(alpha = 0.58f) else SoftAccent
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (category.emoji.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Ink)
                    )
                }
            } else {
                Text(text = category.emoji, style = MaterialTheme.typography.titleLarge)
            }
            Text(text = category.name, style = MaterialTheme.typography.bodySmall, color = Ink)
        }
    }
}

@Composable
private fun AddCategoryTile(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = SoftAccent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Ink),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            }
            Text(text = "Dodaj", style = MaterialTheme.typography.bodySmall, color = Ink)
        }
    }
}

private fun parseDate(value: String): LocalDate? {
    return try {
        LocalDate.parse(value, DateFormatter)
    } catch (_: DateTimeParseException) {
        null
    }
}
