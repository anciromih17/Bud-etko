package si.um.feri.budzetko

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.YearMonth
import si.um.feri.budzetko.data.database.AppDatabase
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.repository.AiSummaryRepository
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.data.repository.SyncRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.domain.ai.AiRecommendationService
import si.um.feri.budzetko.domain.ai.GeminiAiRecommendationClient
import si.um.feri.budzetko.currency.LocalAppCurrency
import si.um.feri.budzetko.ui.screens.AddExpenseScreen
import si.um.feri.budzetko.ui.screens.AuthScreen
import si.um.feri.budzetko.ui.screens.TransactionsScreen
import si.um.feri.budzetko.ui.screens.analytics.AnalyticsScreen
import si.um.feri.budzetko.ui.screens.categories.CategoryScreen
import si.um.feri.budzetko.ui.screens.dashboard.DashboardScreen
import si.um.feri.budzetko.ui.screens.history.BudgetHistoryScreen
import si.um.feri.budzetko.ui.screens.profile.ProfileScreen
import si.um.feri.budzetko.ui.screens.settings.SettingsScreen
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.AnalyticsViewModel
import si.um.feri.budzetko.viewmodel.AuthViewModel
import si.um.feri.budzetko.viewmodel.BudgetHistoryViewModel
import si.um.feri.budzetko.viewmodel.BudgetViewModel
import si.um.feri.budzetko.viewmodel.CategoryViewModel
import si.um.feri.budzetko.viewmodel.DashboardViewModel
import si.um.feri.budzetko.viewmodel.ExpenseViewModel
import si.um.feri.budzetko.viewmodel.SyncViewModel
import si.um.feri.budzetko.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    private var onProfileImageSelected: ((String) -> Unit)? = null
    private val profileImagePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onProfileImageSelected?.invoke(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val baseContext = LocalContext.current
            var appLanguage by remember { mutableStateOf(AppLanguageStore.load(baseContext)) }
            var appCurrency by remember { mutableStateOf(AppLanguageStore.loadCurrency(baseContext)) }
            var appThemeMode by remember { mutableStateOf(AppLanguageStore.loadTheme(baseContext)) }
            val localizedContext = remember(baseContext, appLanguage) {
                AppLanguageStore.localizedContext(baseContext, appLanguage)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalAppCurrency provides appCurrency
            ) {
                BudzetkoTheme(darkTheme = appThemeMode == AppThemeMode.DARK) {
                    BudzetkoApp(
                        appLanguage = appLanguage,
                        onLanguageChange = { language ->
                            AppLanguageStore.save(baseContext, language)
                            appLanguage = language
                        },
                        appCurrency = appCurrency,
                        onCurrencyChange = { currency ->
                            AppLanguageStore.saveCurrency(baseContext, currency)
                            appCurrency = currency
                        },
                        appThemeMode = appThemeMode,
                        onThemeChange = { themeMode ->
                            AppLanguageStore.saveTheme(baseContext, themeMode)
                            appThemeMode = themeMode
                        },
                        onPickProfileImage = { onSelected ->
                            onProfileImageSelected = onSelected
                            profileImagePicker.launch(arrayOf("image/*"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BudzetkoApp(
    appLanguage: AppLanguage = AppLanguage.SLOVENIAN,
    onLanguageChange: (AppLanguage) -> Unit = {},
    appCurrency: AppCurrency = AppCurrency.EUR,
    onCurrencyChange: (AppCurrency) -> Unit = {},
    appThemeMode: AppThemeMode = AppThemeMode.LIGHT,
    onThemeChange: (AppThemeMode) -> Unit = {},
    onPickProfileImage: (((String) -> Unit) -> Unit) = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = AppDatabase.getDatabase(context)

    val budgetRepository = remember { BudgetRepository(database.budgetDao()) }
    val categoryRepository = remember { CategoryRepository(database.categoryDao()) }
    val userRepository = remember {
        UserRepository(
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao(),
            aiSummaryDao = database.aiSummaryDao()
        )
    }
    val expenseRepository = remember { ExpenseRepository(database.expenseDao()) }
    val aiSummaryRepository = remember { AiSummaryRepository(database.aiSummaryDao()) }
    val syncRepository = remember {
        SyncRepository(
            userDao = database.userDao(),
            categoryDao = database.categoryDao(),
            expenseDao = database.expenseDao(),
            budgetDao = database.budgetDao(),
            aiSummaryDao = database.aiSummaryDao()
        )
    }
    val aiRecommendationService = remember(appCurrency) {
        AiRecommendationService(
            geminiClient = GeminiAiRecommendationClient(apiKey = BuildConfig.GEMINI_API_KEY),
            currency = appCurrency
        )
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            userRepository = userRepository
        )
    )
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthScreen(
            authViewModel = authViewModel,
            onAuthSuccess = {}
        )
        return
    }

    val userId = currentUser!!.uid

    var currentScreen by remember { mutableStateOf(BudzetkoScreen.Dashboard) }
    var isAddExpenseDialogOpen by remember { mutableStateOf(false) }
    var expenseBeingEdited by remember { mutableStateOf<ExpenseEntity?>(null) }
    var selectedAppMonth by remember { mutableStateOf(YearMonth.now()) }
    var transactionsMonthFilter by remember { mutableStateOf<Int?>(null) }
    var transactionsYearFilter by remember { mutableStateOf<Int?>(null) }

    fun openTransactions(
        month: Int? = transactionsMonthFilter,
        year: Int? = transactionsYearFilter
    ) {
        transactionsMonthFilter = month
        transactionsYearFilter = year
        if (month != null && year != null) {
            selectedAppMonth = YearMonth.of(year, month)
        }
        currentScreen = BudzetkoScreen.Transactions
    }

    fun setAnalyticsMonth(month: Int, year: Int) {
        val selectedMonth = YearMonth.of(year, month)
        selectedAppMonth = selectedMonth

        if (selectedMonth == YearMonth.now()) {
            transactionsMonthFilter = null
            transactionsYearFilter = null
        } else {
            transactionsMonthFilter = month
            transactionsYearFilter = year
        }
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

    val budgetHistoryViewModel: BudgetHistoryViewModel = viewModel(
        key = "budget_history_$userId",
        factory = BudgetHistoryViewModel.Factory(
            budgetRepository = budgetRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository
        )
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        key = "dashboard_$userId",
        factory = DashboardViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository,
            aiRecommendationService = aiRecommendationService
        )
    )

    val analyticsViewModel: AnalyticsViewModel = viewModel(
        key = "analytics_$userId",
        factory = AnalyticsViewModel.Factory(
            budgetRepository = budgetRepository,
            categoryRepository = categoryRepository,
            expenseRepository = expenseRepository,
            aiSummaryRepository = aiSummaryRepository,
            aiRecommendationService = aiRecommendationService
        )
    )

    val userViewModel: UserViewModel = viewModel(
        key = "user_$userId",
        factory = UserViewModel.Factory(userRepository)
    )

    val expenseViewModel: ExpenseViewModel = viewModel(
        key = "expense_$userId",
        factory = ExpenseViewModel.Factory(repository = expenseRepository)
    )

    val syncViewModel: SyncViewModel = viewModel(
        key = "sync_$userId",
        factory = SyncViewModel.Factory(syncRepository)
    )
    val dashboardUiState by dashboardViewModel.uiState.collectAsState()
    val syncUiState by syncViewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        syncViewModel.syncOnStartup(userId)
    }

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
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            selectedMonth = selectedAppMonth.monthValue,
            selectedYear = selectedAppMonth.year
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
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            selectedMonth = selectedAppMonth.monthValue,
            selectedYear = selectedAppMonth.year,
            onMonthChange = { month, year ->
                setAnalyticsMonth(month, year)
            }
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
            dashboardUiState = dashboardUiState,
            syncUiState = syncUiState,
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onProfileClick = { currentScreen = BudzetkoScreen.Profile },
            onCategorySettingsClick = { currentScreen = BudzetkoScreen.Categories },
            onSystemSettingsClick = { currentScreen = BudzetkoScreen.Profile },
            onBudgetHistoryClick = { currentScreen = BudzetkoScreen.BudgetHistory },
            onLogoutClick = { authViewModel.logout() },
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
            initialMonth = transactionsMonthFilter,
            initialYear = transactionsYearFilter,
            selectedMonthFilter = transactionsMonthFilter,
            selectedYearFilter = transactionsYearFilter,
            onMonthFilterChange = { month, year ->
                transactionsMonthFilter = month
                transactionsYearFilter = year
                if (month != null && year != null) {
                    selectedAppMonth = YearMonth.of(year, month)
                }
            }
        )

        BudzetkoScreen.Profile -> ProfileScreen(
            viewModel = userViewModel,
            syncViewModel = syncViewModel,
            onBackClick = { currentScreen = BudzetkoScreen.Dashboard },
            onHomeClick = { currentScreen = BudzetkoScreen.Dashboard },
            onTransactionsClick = { openTransactions() },
            onAddExpenseClick = {
                expenseBeingEdited = null
                isAddExpenseDialogOpen = true
            },
            onAnalyticsClick = { currentScreen = BudzetkoScreen.Analytics },
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings },
            appLanguage = appLanguage,
            onLanguageChange = onLanguageChange,
            appCurrency = appCurrency,
            onCurrencyChange = onCurrencyChange,
            appThemeMode = appThemeMode,
            onThemeChange = onThemeChange,
            onDeleteAccountClick = { password -> authViewModel.deleteAccount(password) },
            onPickProfileImage = onPickProfileImage
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
