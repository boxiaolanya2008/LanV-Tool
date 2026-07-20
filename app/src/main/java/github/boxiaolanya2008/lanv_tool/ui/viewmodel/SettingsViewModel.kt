package github.boxiaolanya2008.lanv_tool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository
import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val autoBackup: Boolean = true,
    val confirmBeforeApply: Boolean = true,
    val appVersion: String = "1.0",
    val totalProperties: Int = 0,
    val modifiedCount: Int = 0
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val propertyRepository: PropertyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val states = propertyRepository.getAllPropertyStates()
        _uiState.value = SettingsUiState(
            autoBackup = settingsRepository.autoBackup,
            confirmBeforeApply = settingsRepository.confirmBeforeApply,
            appVersion = settingsRepository.appVersion,
            totalProperties = states.size,
            modifiedCount = states.count { it.isModified }
        )
    }

    fun setAutoBackup(enabled: Boolean) {
        settingsRepository.autoBackup = enabled
        _uiState.value = _uiState.value.copy(autoBackup = enabled)
    }

    fun setConfirmBeforeApply(enabled: Boolean) {
        settingsRepository.confirmBeforeApply = enabled
        _uiState.value = _uiState.value.copy(confirmBeforeApply = enabled)
    }

    fun clearAllBackups(onDone: () -> Unit) {
        propertyRepository.clearAllBackups()
        loadSettings()
        onDone()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val propertyRepository: PropertyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(settingsRepository, propertyRepository) as T
        }
    }
}
