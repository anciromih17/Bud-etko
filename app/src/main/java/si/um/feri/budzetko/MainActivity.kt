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
import si.um.feri.budzetko.data.repository.AiSummaryRepository
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.domain.ai.AiRecommendationService
import si.um.feri.budzetko.domain.ai.GeminiAiRecommendationClient
import si.um.feri.budzetko.ui.screens.AddExpenseScreen
import si.um.feri.budzetko.ui.screens.TransactionsScreen
import si.um.feri.budzetko.ui.screens.analytics.AnalyticsScreen
import si.um.feri.budzetko.ui.screens.categories.CategoryScreen
import si.um.feri.budzetko.ui.screens.dashboard.DashboardScreen
import si.um.feri.budzetko.ui.screens.history.BudgetHistoryScreen
import si.um.feri.budzetko.ui.screens.profile.ProfileScreen
import si.um.feri.budzetko.ui.screens.settings.SettingsScreen
import si.um.feri.budzetko.viewmodel.BudgetHistoryViewModel
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.AnalyticsViewModel
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

    val budgetRepository = remember { BudgetRepository(database.budgetDao()) }
    val categoryRepository = remember { CategoryRepository(database.categoryDao()) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val expenseRepository = remember { ExpenseRepository(database.expenseDao()) }
    val aiSummaryRepository = remember { AiSummaryRepository(database.aiSummaryDao()) }
    val aiRecommendationService = remember {
        AiRecommendationService(
            geminiClient = GeminiAiRecommendationClient(apiKey = BuildConfig.GEMINI_API_KEY)
        )
    }

    var currentScreen by remember { mutableStateOf(BudzetkoScreen.Dashboard) }
    var isAddExpenseDialogOpen by remember { mutableStateOf(false) }
    var expenseBeingEdited by remember { mutableStateOf<ExpenseEntity?>(null) }
    var transactionsInitialMonth by remember { mutableStateOf<Int?>(null) }
    var transactionsInitialYear by remember { mutableStateOf<Int?>(null) }

    fun openTransactions(month: Int? = null, year: Int? = null) {
        transactionsInitialMonth = month
        transactionsInitialYear = year
        currentScreen = BudzetkoScreen.Transactions
    }

    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModel.Factory(
            categoryRepository = categoryRepository,
            budgetRepository = budgetRepository,
            userRepository = userRepository
        )
    )

    val budgetViewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            userRepository = userRepository
        )
    )
    val budgetHistoryViewModel: BudgetHistoryViewModel = viewModel(
        factory = BudgetHistoryViewModel.Factory(
            budgetRepository = budgetRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository
        )
    )
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository,
            aiRecommendationService = aiRecommendationService
        )
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository,
            aiRecommendationService = aiRecommendationService
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
        BudzetkoScreen.Dashboard -> DashboardScreen(
            viewModel = dashboardViewModel,
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onTransactionsClick = { openTransactions() },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )

        BudzetkoScreen.Analytics -> AnalyticsScreen(
            viewModel = analyticsViewModel,
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onTransactionsClick = { openTransactions() },
            onCategorySettingsClick = { currentScreen = BudzetkoScreen.Categories },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )

        BudzetkoScreen.Categories -> CategoryScreen(
            viewModel = categoryViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = { openTransactions() },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics }
        )

        BudzetkoScreen.Settings -> SettingsScreen(
            budgetViewModel = budgetViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onCategorySettingsClick = { currentScreen = BudzetkoScreen.Categories },
            onBudgetHistoryClick = { currentScreen = BudzetkoScreen.BudgetHistory },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onTransactionsClick = { openTransactions() },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics }
        )

        BudzetkoScreen.BudgetHistory -> BudgetHistoryScreen(
            viewModel = budgetHistoryViewModel,
            onBackClick = { currentScreen = BudzetkoScreen.Settings },
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onTransactionsClick = { openTransactions() },
            onMonthTransactionsClick = { month, year -> openTransactions(month, year) },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )

        BudzetkoScreen.Transactions -> TransactionsScreen(
            expenseViewModel = expenseViewModel,
            categoryViewModel = categoryViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onEditExpenseClick = {
                expenseBeingEdited = it
                isAddExpenseDialogOpen = true
            },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            initialMonth = transactionsInitialMonth,
            initialYear = transactionsInitialYear
        )

        BudzetkoScreen.Profile -> ProfileScreen(
            viewModel = userViewModel,
            onBackClick = { currentScreen = BudzetkoScreen.Dashboard },
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics },
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
                openTransactions()
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
    BudgetHistory,
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
