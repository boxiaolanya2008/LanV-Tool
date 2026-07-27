package github.boxiaolanya2008.lanv_tool.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

internal class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lanv_tool_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_CONFIRM_BEFORE_APPLY = "confirm_before_apply"
        private const val KEY_LAST_RESTORE_TIME = "last_restore_time"
        private const val KEY_APP_VERSION = "app_version"
    }

    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    private val _autoBackup = MutableStateFlow(prefs.getBoolean(KEY_AUTO_BACKUP, true))
    private val _confirmBeforeApply = MutableStateFlow(prefs.getBoolean(KEY_CONFIRM_BEFORE_APPLY, true))

    override fun observeDarkMode(): Flow<Boolean> = _darkMode.asStateFlow()

    override fun observeAutoBackup(): Flow<Boolean> = _autoBackup.asStateFlow()

    override fun observeConfirmBeforeApply(): Flow<Boolean> = _confirmBeforeApply.asStateFlow()

    override suspend fun setDarkMode(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
            _darkMode.value = enabled
        }
    }

    override suspend fun setAutoBackup(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
            _autoBackup.value = enabled
        }
    }

    override suspend fun setConfirmBeforeApply(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit().putBoolean(KEY_CONFIRM_BEFORE_APPLY, enabled).apply()
            _confirmBeforeApply.value = enabled
        }
    }

    override suspend fun getAppVersion(): String {
        return withContext(Dispatchers.IO) {
            prefs.getString(KEY_APP_VERSION, "1.0") ?: "1.0"
        }
    }

    override suspend fun getLastRestoreTime(): Long {
        return withContext(Dispatchers.IO) {
            prefs.getLong(KEY_LAST_RESTORE_TIME, 0L)
        }
    }

    override suspend fun setLastRestoreTime(time: Long) {
        withContext(Dispatchers.IO) {
            prefs.edit().putLong(KEY_LAST_RESTORE_TIME, time).apply()
        }
    }
}
