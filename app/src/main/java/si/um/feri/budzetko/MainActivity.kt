package si.um.feri.budzetko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import si.um.feri.budzetko.data.database.AppDatabase
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.ui.screens.AddExpenseScreen
import si.um.feri.budzetko.ui.screens.AuthScreen
import si.um.feri.budzetko.ui.screens.TransactionsScreen
import si.um.feri.budzetko.ui.screens.analytics.AnalyticsScreen
import si.um.feri.budzetko.ui.screens.categories.CategoryScreen
import si.um.feri.budzetko.ui.screens.dashboard.DashboardScreen
import si.um.feri.budzetko.ui.screens.profile.ProfileScreen
import si.um.feri.budzetko.ui.screens.settings.SettingsScreen
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.AuthViewModel
import si.um.feri.budzetko.viewmodel.BudgetViewModel
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.DashboardViewModel
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

    val budgetRepository = remember {
        BudgetRepository(database.budgetDao())
    }

    val categoryRepository = remember {
        CategoryRepository(database.categoryDao())
    }

    val userRepository = remember {
        UserRepository(database.userDao())
    }

    val expenseRepository = remember {
        ExpenseRepository(database.expenseDao())
    }

    val authViewModel: AuthViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {

        AuthScreen(
            authViewModel = authViewModel,
            onAuthSuccess = {}
        )

        return
    }

    val userId = currentUser!!.uid

    var currentScreen by remember {
        mutableStateOf(BudzetkoScreen.Dashboard)
    }

    var isAddExpenseDialogOpen by remember {
        mutableStateOf(false)
    }

    var expenseBeingEdited by remember {
        mutableStateOf<ExpenseEntity?>(null)
    }

    val categoryViewModel: CategoryViewModel = viewModel(
        key = "category_$userId",
        factory = CategoryViewModel.Factory(
            categoryRepository = categoryRepository,
            budgetRepository = budgetRepository,
            userRepository = userRepository
        )
    )

    val budgetViewModel: BudgetViewModel = viewModel(
        key = "budget_$userId",
        factory = BudgetViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            userRepository = userRepository
        )
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        key = "dashboard_$userId",
        factory = DashboardViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository
        )
    )

    val userViewModel: UserViewModel = viewModel(
        key = "user_$userId",
        factory = UserViewModel.Factory(
            userRepository
        )
    )

    val expenseViewModel: ExpenseViewModel = viewModel(
        key = "expense_$userId",
        factory = ExpenseViewModel.Factory(
            repository = expenseRepository
        )
    )

    when (currentScreen) {

        BudzetkoScreen.Dashboard -> DashboardScreen(
            viewModel = dashboardViewModel,
            onProfileClick = {
                currentScreen = BudzetkoScreen.Profile
            },
            onTransactionsClick = {
                currentScreen = BudzetkoScreen.Transactions
            },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onAnalyticsClick = {
                currentScreen = BudzetkoScreen.Analytics
            },
            onSettingsClick = {
                currentScreen = BudzetkoScreen.Settings
            }
        )

        BudzetkoScreen.Analytics -> AnalyticsScreen(
            viewModel = dashboardViewModel,
            onProfileClick = {
                currentScreen = BudzetkoScreen.Profile
            },
            onHomeClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onTransactionsClick = {
                currentScreen = BudzetkoScreen.Transactions
            },
            onCategorySettingsClick = {
                currentScreen = BudzetkoScreen.Categories
            },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onSettingsClick = {
                currentScreen = BudzetkoScreen.Settings
            }
        )

        BudzetkoScreen.Categories -> CategoryScreen(
            viewModel = categoryViewModel,
            onHomeClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onSettingsClick = {
                currentScreen = BudzetkoScreen.Settings
            },
            onProfileClick = {
                currentScreen = BudzetkoScreen.Profile
            },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = {
                currentScreen = BudzetkoScreen.Transactions
            },
            onAnalyticsClick = {
                currentScreen = BudzetkoScreen.Analytics
            }
        )

        BudzetkoScreen.Settings -> SettingsScreen(
            budgetViewModel = budgetViewModel,
            onHomeClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onProfileClick = {
                currentScreen = BudzetkoScreen.Profile
            },
            onCategorySettingsClick = {
                currentScreen = BudzetkoScreen.Categories
            },
            onLogoutClick = {
                authViewModel.logout()
            },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = {
                currentScreen = BudzetkoScreen.Transactions
            },
            onAnalyticsClick = {
                currentScreen = BudzetkoScreen.Analytics
            }
        )

        BudzetkoScreen.Transactions -> TransactionsScreen(
            expenseViewModel = expenseViewModel,
            categoryViewModel = categoryViewModel,
            onHomeClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onEditExpenseClick = {
                expenseBeingEdited = it
                isAddExpenseDialogOpen = true
            },
            onProfileClick = {
                currentScreen = BudzetkoScreen.Profile
            },
            onAnalyticsClick = {
                currentScreen = BudzetkoScreen.Analytics
            },
            onSettingsClick = {
                currentScreen = BudzetkoScreen.Settings
            }
        )

        BudzetkoScreen.Profile -> ProfileScreen(
            viewModel = userViewModel,
            onBackClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onHomeClick = {
                currentScreen = BudzetkoScreen.Dashboard
            },
            onAnalyticsClick = {
                currentScreen = BudzetkoScreen.Analytics
            },
            onSettingsClick = {
                currentScreen = BudzetkoScreen.Settings
            }
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
    Dashboard,
    Analytics,
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