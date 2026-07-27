package github.boxiaolanya2008.lanv_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.lanv_tool.app.LanVToolApp
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.ui.screens.CategoryDetailRoute
import github.boxiaolanya2008.lanv_tool.ui.screens.PropertyRoute
import github.boxiaolanya2008.lanv_tool.ui.screens.settings.SettingsRoute
import github.boxiaolanya2008.lanv_tool.ui.theme.LanVToolTheme
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyViewModel
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LanVToolApp.instance = this.application as LanVToolApp
        setContent {
            LanVToolTheme {
                AppNavigation()
            }
        }
    }
}

sealed interface Screen {
    data object CategoryList : Screen
    data class CategoryDetail(val categoryName: String) : Screen
    data object Settings : Screen

    val category: PropertyCategory?
        get() = when (this) {
            is CategoryDetail -> PropertyCategory.entries.find { it.name == categoryName }
            else -> null
        }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.CategoryList) }

    val app = LanVToolApp.instance
    val propertyViewModel: PropertyViewModel = viewModel(
        factory = PropertyViewModel.Factory(
            observePropertiesUseCase = app.observePropertiesUseCase,
            refreshPropertiesUseCase = app.refreshPropertiesUseCase,
            applyPropertyUseCase = app.applyPropertyUseCase,
            restorePropertyUseCase = app.restorePropertyUseCase,
            restoreAllUseCase = app.restoreAllUseCase,
            checkRootUseCase = app.checkRootUseCase
        )
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            getSettingsUseCase = app.getSettingsUseCase,
            updateSettingsUseCase = app.updateSettingsUseCase,
            propertyRepository = app.propertyRepository,
            settingsRepository = app.settingsRepository
        )
    )

    when (val screen = currentScreen) {
        Screen.CategoryList -> PropertyRoute(
            viewModel = propertyViewModel,
            onNavigateToCategory = { category ->
                currentScreen = Screen.CategoryDetail(category.name)
            },
            onNavigateToSettings = {
                currentScreen = Screen.Settings
            }
        )
        is Screen.CategoryDetail -> screen.category?.let { category ->
            CategoryDetailRoute(
                category = category,
                viewModel = propertyViewModel,
                onNavigateBack = {
                    currentScreen = Screen.CategoryList
                }
            )
        } ?: run {
            currentScreen = Screen.CategoryList
            PropertyRoute(
                viewModel = propertyViewModel,
                onNavigateToCategory = { category ->
                    currentScreen = Screen.CategoryDetail(category.name)
                },
                onNavigateToSettings = {
                    currentScreen = Screen.Settings
                }
            )
        }
        Screen.Settings -> SettingsRoute(
            viewModel = settingsViewModel,
            onNavigateBack = {
                currentScreen = Screen.CategoryList
            }
        )
    }
}
