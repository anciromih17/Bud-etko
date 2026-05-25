package si.um.feri.budzetko.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import si.um.feri.budzetko.data.entity.BudgetCategoryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveExpense(expense: ExpenseEntity) {
        db.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document(expense.id.toString())
            .set(expense)
            .await()
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        db.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document(expense.id.toString())
            .delete()
            .await()
    }

    suspend fun saveCategory(category: CategoryEntity) {
        db.collection("users")
            .document(category.userId)
            .collection("categories")
            .document(category.id.toString())
            .set(category)
            .await()
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        db.collection("users")
            .document(category.userId)
            .collection("categories")
            .document(category.id.toString())
            .delete()
            .await()
    }

    suspend fun saveBudget(
        budget: BudgetEntity,
        limits: List<BudgetCategoryEntity>
    ) {
        val budgetDocId = "${budget.year}_${budget.month}"

        db.collection("users")
            .document(budget.userId)
            .collection("budgets")
            .document(budgetDocId)
            .set(budget)
            .await()

        limits.forEach { limit ->
            db.collection("users")
                .document(budget.userId)
                .collection("budgets")
                .document(budgetDocId)
                .collection("limits")
                .document(limit.categoryId.toString())
                .set(limit)
                .await()
        }
    }
}