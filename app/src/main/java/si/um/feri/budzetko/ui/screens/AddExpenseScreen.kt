package si.um.feri.budzetko.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel

@Composable
fun AddExpenseScreen(
    expenseViewModel: ExpenseViewModel,
    categoryViewModel: CategoryViewModel
) {
    val categoryState by categoryViewModel.uiState.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()

    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val firstCategory = categoryState.categories.firstOrNull()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Button(
            onClick = {
                val amountValue = amount.toDoubleOrNull()

                if (firstCategory == null) {
                    message = "Najprej dodaj kategorijo."
                    return@Button
                }

                if (amountValue == null || description.isBlank()) {
                    message = "Vnesi znesek in opis."
                    return@Button
                }

                val currentEditingExpense = editingExpense

                if (currentEditingExpense == null) {
                    expenseViewModel.addExpense(
                        amount = amountValue,
                        description = description,
                        categoryId = firstCategory.id
                    )
                    message = "Strošek je dodan."
                } else {
                    expenseViewModel.updateExpense(
                        expense = currentEditingExpense,
                        amount = amountValue,
                        description = description
                    )
                    editingExpense = null
                    message = "Strošek je posodobljen."
                }

                amount = ""
                description = ""
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                if (editingExpense == null) {
                    "Add Expense"
                } else {
                    "Update Expense"
                }
            )
        }

        if (editingExpense != null) {
            TextButton(
                onClick = {
                    editingExpense = null
                    amount = ""
                    description = ""
                    message = ""
                }
            ) {
                Text("Cancel edit")
            }
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Text(
            text = "Expenses",
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        LazyColumn {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = "Amount: ${expense.amount} €")
                        Text(text = "Description: ${expense.description}")

                        TextButton(
                            onClick = {
                                editingExpense = expense
                                amount = expense.amount.toString()
                                description = expense.description
                                message = ""
                            }
                        ) {
                            Text("Edit")
                        }

                        TextButton(
                            onClick = {
                                expenseViewModel.deleteExpense(expense)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}