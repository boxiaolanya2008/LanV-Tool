package github.boxiaolanya2008.lanv_tool.data.repository

import android.content.Context
import android.content.SharedPreferences
import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.model.PropertyRegistry
import github.boxiaolanya2008.lanv_tool.data.shell.PropManager

class PropertyRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lanv_tool_props", Context.MODE_PRIVATE)

    private val keyOriginalPrefix = "original_"
    private val keyCurrentPrefix = "current_"
    private val keyAvailablePrefix = "available_"
    private val keyFirstBootChecked = "first_boot_checked"

    fun isFirstBoot(): Boolean = !prefs.getBoolean(keyFirstBootChecked, false)

    fun getOriginalValue(key: String): String? =
        prefs.getString("$keyOriginalPrefix$key", null)

    fun getSavedCurrentValue(key: String): String? =
        prefs.getString("$keyCurrentPrefix$key", null)

    fun isPropertyAvailable(key: String): Boolean =
        prefs.getBoolean("$keyAvailablePrefix$key", false)

    fun getAllPropertyStates(): List<PropertyState> {
        return PropertyRegistry.properties.mapNotNull { prop ->
            val available = isPropertyAvailable(prop.key)
            val original = getOriginalValue(prop.key)
            val current = getSavedCurrentValue(prop.key)

            if (original == null && !available) {
                return@mapNotNull null
            }

            val displayCurrent = current ?: ""
            val isModified = original != null && original != displayCurrent

            PropertyState(
                property = prop,
                isAvailable = true,
                originalValue = original,
                currentValue = displayCurrent,
                isModified = isModified
            )
        }
    }

    fun refreshProperties(): List<PropertyState> {
        val editor = prefs.edit()

        for (prop in PropertyRegistry.properties) {
            val currentValue = PropManager.getProp(prop.key)
            val available = currentValue != null

            editor.putBoolean("$keyAvailablePrefix${prop.key}", available)

            if (available) {
                val existingOriginal = getOriginalValue(prop.key)
                if (existingOriginal == null) {
                    editor.putString("$keyOriginalPrefix${prop.key}", currentValue)
                    editor.putString("$keyCurrentPrefix${prop.key}", currentValue)
                } else {
                    val savedCurrent = getSavedCurrentValue(prop.key)
                    if (savedCurrent == null) {
                        editor.putString("$keyCurrentPrefix${prop.key}", currentValue)
                    }
                }
            }
        }

        editor.putBoolean(keyFirstBootChecked, true)
        editor.apply()

        return getAllPropertyStates()
    }

    fun forceRefresh(): List<PropertyState> {
        val editor = prefs.edit()

        for (prop in PropertyRegistry.properties) {
            val currentValue = PropManager.getProp(prop.key)
            val available = currentValue != null

            editor.putBoolean("$keyAvailablePrefix${prop.key}", available)

            if (available) {
                val existingOriginal = getOriginalValue(prop.key)
                if (existingOriginal == null) {
                    editor.putString("$keyOriginalPrefix${prop.key}", currentValue)
                }
                editor.putString("$keyCurrentPrefix${prop.key}", currentValue)
            }
        }

        editor.apply()
        return getAllPropertyStates()
    }

    fun saveCurrentValue(key: String, value: String) {
        prefs.edit()
            .putString("$keyCurrentPrefix$key", value)
            .putBoolean("$keyAvailablePrefix$key", true)
            .apply()
    }

    fun restoreOriginal(key: String): Boolean {
        val original = getOriginalValue(key) ?: return false
        val success = PropManager.setProp(key, original)
        if (success) {
            saveCurrentValue(key, original)
        }
        return success
    }

    fun restoreAll(): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        for (prop in PropertyRegistry.properties) {
            if (isPropertyAvailable(prop.key)) {
                results[prop.key] = restoreOriginal(prop.key)
            }
        }
        return results
    }

    fun clearAllBackups() {
        prefs.edit().clear().apply()
    }
}
