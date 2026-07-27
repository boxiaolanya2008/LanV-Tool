package github.boxiaolanya2008.lanv_tool.data.repository

import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.model.SystemProperty
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {
    fun observeProperties(): Flow<List<PropertyState>>
    fun observeProperty(key: String): Flow<PropertyState?>
    suspend fun refreshProperties(): Result<List<PropertyState>>
    suspend fun applyProperty(key: String, value: String): Result<Unit>
    suspend fun restoreProperty(key: String): Result<Unit>
    suspend fun restoreAll(): Result<Map<String, Boolean>>
    suspend fun forceRefresh(): Result<List<PropertyState>>
    suspend fun clearAllBackups()
    suspend fun checkRoot(): Boolean
}

interface SettingsRepository {
    fun observeDarkMode(): Flow<Boolean>
    fun observeAutoBackup(): Flow<Boolean>
    fun observeConfirmBeforeApply(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setAutoBackup(enabled: Boolean)
    suspend fun setConfirmBeforeApply(enabled: Boolean)
    suspend fun getAppVersion(): String
    suspend fun getLastRestoreTime(): Long
    suspend fun setLastRestoreTime(time: Long)
}
