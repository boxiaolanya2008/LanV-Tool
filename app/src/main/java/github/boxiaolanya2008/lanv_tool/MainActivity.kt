package github.boxiaolanya2008.lanv_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.lanv_tool.app.LanVToolApp
import github.boxiaolanya2008.lanv_tool.ui.screens.MainScreen
import github.boxiaolanya2008.lanv_tool.ui.screens.settings.SettingsScreen
import github.boxiaolanya2008.lanv_tool.ui.theme.LanVToolTheme
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyViewModel
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LanVToolTheme {
                AppNavigation()
            }
        }
    }
}

enum class Screen {
    MAIN, SETTINGS
}

@Composable
fun AppNavigation() {
    val app = LanVToolApp.instance
    var currentScreen by rememberSaveable { mutableStateOf(Screen.MAIN) }

    val propertyViewModel: PropertyViewModel = viewModel(
        factory = PropertyViewModel.Factory(app.propertyRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(app.settingsRepository, app.propertyRepository)
    )

    when (currentScreen) {
        Screen.MAIN -> MainScreen(
            viewModel = propertyViewModel,
            onNavigateToSettings = { currentScreen = Screen.SETTINGS }
        )
        Screen.SETTINGS -> SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateBack = { currentScreen = Screen.MAIN }
        )
    }
}
