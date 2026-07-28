package github.boxiaolanya2008.lanv_tool.domain.usecase

import github.boxiaolanya2008.lanv_tool.data.repository.SettingsRepository

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    fun observeDarkMode() = repository.observeDarkMode()
    fun observeAutoBackup() = repository.observeAutoBackup()
    fun observeConfirmBeforeApply() = repository.observeConfirmBeforeApply()
    fun observePredictiveBack() = repository.observePredictiveBack()

    suspend fun getAppVersion(): String = repository.getAppVersion()
}

class UpdateSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend fun setDarkMode(enabled: Boolean) = repository.setDarkMode(enabled)
    suspend fun setAutoBackup(enabled: Boolean) = repository.setAutoBackup(enabled)
    suspend fun setConfirmBeforeApply(enabled: Boolean) = repository.setConfirmBeforeApply(enabled)
    suspend fun setPredictiveBack(enabled: Boolean) = repository.setPredictiveBack(enabled)
}
