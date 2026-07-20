package github.boxiaolanya2008.lanv_tool.data.repository

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lanv_tool_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_CONFIRM_BEFORE_APPLY = "confirm_before_apply"
        private const val KEY_LAST_RESTORE_TIME = "last_restore_time"
        private const val KEY_APP_VERSION = "app_version"
    }

    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var autoBackup: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BACKUP, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BACKUP, value).apply()

    var confirmBeforeApply: Boolean
        get() = prefs.getBoolean(KEY_CONFIRM_BEFORE_APPLY, true)
        set(value) = prefs.edit().putBoolean(KEY_CONFIRM_BEFORE_APPLY, value).apply()

    var lastRestoreTime: Long
        get() = prefs.getLong(KEY_LAST_RESTORE_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_RESTORE_TIME, value).apply()

    var appVersion: String
        get() = prefs.getString(KEY_APP_VERSION, "1.0") ?: "1.0"
        set(value) = prefs.edit().putString(KEY_APP_VERSION, value).apply()
}
