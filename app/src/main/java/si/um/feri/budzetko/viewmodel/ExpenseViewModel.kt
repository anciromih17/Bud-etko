package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.repository.ExpenseRepository

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val currentUserId: String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user"

    val expenses = repository
        .observeExpenses(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpense(
        amount: Double,
        date: Long,
        description: String,
        categoryId: Long,
        receiptImagePath: String? = null
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                amount = amount,
                date = date,
                description = description,
                userId = currentUserId,
                categoryId = categoryId,
                receiptImagePath = receiptImagePath
            )
        }
    }

    fun updateExpense(
        expense: ExpenseEntity,
        amount: Double,
        date: Long,
        description: String,
        categoryId: Long,
        receiptImagePath: String? = expense.receiptImagePath
    ) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    amount = amount,
                    date = date,
                    description = description,
                    categoryId = categoryId,
                    receiptImagePath = receiptImagePath,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    class Factory(
        private val repository: ExpenseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(repository) as T
        }
    }
}
