package si.um.feri.budzetko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import si.um.feri.budzetko.data.database.AppDatabase
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.ui.screens.AddExpenseScreen
import si.um.feri.budzetko.ui.screens.TransactionsScreen
import si.um.feri.budzetko.ui.screens.categories.CategoryScreen
import si.um.feri.budzetko.ui.screens.profile.ProfileScreen
import si.um.feri.budzetko.ui.screens.settings.SettingsScreen
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.BudgetViewModel
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel
import si.um.feri.budzetko.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudzetkoTheme {
                BudzetkoApp()
            }
        }
    }
}

@Composable
fun BudzetkoApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)

    val categoryRepository = remember { CategoryRepository(database.categoryDao()) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val expenseRepository = remember { ExpenseRepository(database.expenseDao()) }

    var currentScreen by remember { mutableStateOf(BudzetkoScreen.Categories) }
    var isAddExpenseDialogOpen by remember { mutableStateOf(false) }
    var expenseBeingEdited by remember { mutableStateOf<ExpenseEntity?>(null) }

    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModel.Factory(
            categoryRepository = categoryRepository,
            budgetRepository = BudgetRepository(database.budgetDao()),
            userRepository = userRepository
        )
    )

    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(
            budgetRepository = BudgetRepository(database.budgetDao()),
            categoryRepository = categoryRepository,
            userRepository = userRepository
        )
    )
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(userRepository)
    )

    val expenseViewModel: ExpenseViewModel = viewModel(
        factory = ExpenseViewModel.Factory(
            repository = expenseRepository
        )
    )

    when (currentScreen) {
        BudzetkoScreen.Categories -> CategoryScreen(
            viewModel = categoryViewModel,
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = { currentScreen = BudzetkoScreen.Transactions }
        )

        BudzetkoScreen.Settings -> SettingsScreen(
            budgetViewModel = budgetViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Categories },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onCategorySettingsClick = { currentScreen = BudzetkoScreen.Categories },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = { currentScreen = BudzetkoScreen.Transactions }
        )

        BudzetkoScreen.Transactions -> TransactionsScreen(
            expenseViewModel = expenseViewModel,
            categoryViewModel = categoryViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Categories },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onEditExpenseClick = {
                expenseBeingEdited = it
                isAddExpenseDialogOpen = true
            },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )

        BudzetkoScreen.Profile -> ProfileScreen(
            viewModel = userViewModel,
            onBackClick = { currentScreen = BudzetkoScreen.Categories },
            onHomeClick = { currentScreen = BudzetkoScreen.Categories },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )
    }

    if (isAddExpenseDialogOpen) {
        AddExpenseScreen(
            expenseViewModel = expenseViewModel,
            categoryViewModel = categoryViewModel,
            expenseToEdit = expenseBeingEdited,
            onClose = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = false
            },
            onSaved = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = false
                currentScreen = BudzetkoScreen.Transactions
            },
            onAddCategoryClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = false
                categoryViewModel.openCreateDialog()
                currentScreen = BudzetkoScreen.Categories
            }
        )
    }
}

private enum class BudzetkoScreen {
    Categories,
    Settings,
    Transactions,
    Profile
}

@Preview(showBackground = true)
@Composable
fun BudzetkoAppPreview() {
    BudzetkoTheme {
        BudzetkoApp()
    }
}
