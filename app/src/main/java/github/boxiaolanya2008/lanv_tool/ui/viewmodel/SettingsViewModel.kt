package github.boxiaolanya2008.lanv_tool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository
import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepository
import github.boxiaolanya2008.lanv_tool.domain.usecase.GetSettingsUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val autoBackup: Boolean,
        val confirmBeforeApply: Boolean,
        val predictiveBack: Boolean,
        val appVersion: String,
        val totalProperties: Int,
        val modifiedCount: Int
    ) : SettingsUiState
}

sealed interface SettingsAction {
    data class SetAutoBackup(val enabled: Boolean) : SettingsAction
    data class SetConfirmBeforeApply(val enabled: Boolean) : SettingsAction
    data class SetPredictiveBack(val enabled: Boolean) : SettingsAction
    data object ClearAllBackups : SettingsAction
}

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val propertyRepository: PropertyRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        getSettingsUseCase.observeAutoBackup(),
        getSettingsUseCase.observeConfirmBeforeApply(),
        getSettingsUseCase.observePredictiveBack(),
        propertyRepository.observeProperties()
    ) { autoBackup, confirmBeforeApply, predictiveBack, properties ->
        SettingsUiState.Success(
            autoBackup = autoBackup,
            confirmBeforeApply = confirmBeforeApply,
            predictiveBack = predictiveBack,
            appVersion = settingsRepository.let { getSettingsUseCase.getAppVersion() },
            totalProperties = properties.size,
            modifiedCount = properties.count { it.isModified }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState.Loading
    )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.SetAutoBackup -> setAutoBackup(action.enabled)
            is SettingsAction.SetConfirmBeforeApply -> setConfirmBeforeApply(action.enabled)
            is SettingsAction.SetPredictiveBack -> setPredictiveBack(action.enabled)
            SettingsAction.ClearAllBackups -> clearAllBackups()
        }
    }

    private fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setAutoBackup(enabled)
        }
    }

    private fun setConfirmBeforeApply(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setConfirmBeforeApply(enabled)
        }
    }

    private fun setPredictiveBack(enabled: Boolean) {
        viewModelScope.launch {
            updateSettingsUseCase.setPredictiveBack(enabled)
        }
    }

    private fun clearAllBackups() {
        viewModelScope.launch {
            propertyRepository.clearAllBackups()
        }
    }

    class Factory(
        private val getSettingsUseCase: GetSettingsUseCase,
        private val updateSettingsUseCase: UpdateSettingsUseCase,
        private val propertyRepository: PropertyRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                getSettingsUseCase = getSettingsUseCase,
                updateSettingsUseCase = updateSettingsUseCase,
                propertyRepository = propertyRepository,
                settingsRepository = settingsRepository
            ) as T
        }
    }
}
