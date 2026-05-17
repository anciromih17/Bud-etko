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
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.ui.screens.categories.CategoryScreen
import si.um.feri.budzetko.ui.screens.settings.SettingsScreen
import si.um.feri.budzetko.ui.theme.BudzetkoTheme
import si.um.feri.budzetko.viewmodel.BudgetViewModel
import si.um.feri.budzetko.viewmodel.CategoryViewModel

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
    var currentScreen by remember { mutableStateOf(BudzetkoScreen.Categories) }

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

    when (currentScreen) {
        BudzetkoScreen.Categories -> CategoryScreen(
            viewModel = categoryViewModel,
            onSettingsClick = { currentScreen = BudzetkoScreen.Settings }
        )

        BudzetkoScreen.Settings -> SettingsScreen(
            budgetViewModel = budgetViewModel,
            onHomeClick = { currentScreen = BudzetkoScreen.Categories },
            onCategorySettingsClick = { currentScreen = BudzetkoScreen.Categories }
        )
    }
}

private enum class BudzetkoScreen {
    Categories,
    Settings
}

@Preview(showBackground = true)
@Composable
fun BudzetkoAppPreview() {
    BudzetkoTheme {
        BudzetkoApp()
    }
}
