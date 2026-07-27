package github.boxiaolanya2008.lanv_tool.data.repository

import android.content.Context
import android.content.SharedPreferences
import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.model.PropertyRegistry
import github.boxiaolanya2008.lanv_tool.data.shell.PropManager
import github.boxiaolanya2008.lanv_tool.data.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

internal class PropertyRepositoryImpl(
    private val context: Context
) : PropertyRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lanv_tool_props", Context.MODE_PRIVATE)

    private val keyOriginalPrefix = "original_"
    private val keyCurrentPrefix = "current_"
    private val keyAvailablePrefix = "available_"
    private val keyFirstBootChecked = "first_boot_checked"

    private val _propertiesFlow = MutableStateFlow<List<PropertyState>>(emptyList())
    private val _rootState = MutableStateFlow<Boolean?>(null)

    override fun observeProperties(): Flow<List<PropertyState>> = _propertiesFlow.asStateFlow()

    override fun observeProperty(key: String): Flow<PropertyState?> =
        MutableStateFlow(_propertiesFlow.value.find { it.property.key == key }).asStateFlow()

    override suspend fun refreshProperties(): Result<List<PropertyState>> = withContext(Dispatchers.IO) {
        runCatching {
            if (!ShellExecutor.checkRoot()) {
                _rootState.value = false
                return@runCatching emptyList()
            }
            _rootState.value = true

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

            val states = buildPropertyStates()
            _propertiesFlow.value = states
            states
        }
    }

    override suspend fun applyProperty(key: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val success = PropManager.setProp(key, value)
            if (!success) throw IllegalStateException("setprop failed for $key")

            prefs.edit()
                .putString("$keyCurrentPrefix$key", value)
                .putBoolean("$keyAvailablePrefix$key", true)
                .apply()

            val states = buildPropertyStates()
            _propertiesFlow.value = states
        }
    }

    override suspend fun restoreProperty(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val original = getOriginalValue(key)
                ?: throw IllegalStateException("No original value for $key")

            val success = PropManager.setProp(key, original)
            if (!success) throw IllegalStateException("setprop failed for $key")

            prefs.edit()
                .putString("$keyCurrentPrefix$key", original)
                .apply()

            val states = buildPropertyStates()
            _propertiesFlow.value = states
        }
    }

    override suspend fun restoreAll(): Result<Map<String, Boolean>> = withContext(Dispatchers.IO) {
        runCatching {
            val results = mutableMapOf<String, Boolean>()
            for (prop in PropertyRegistry.properties) {
                if (isPropertyAvailable(prop.key)) {
                    val original = getOriginalValue(prop.key)
                    if (original != null) {
                        val success = PropManager.setProp(prop.key, original)
                        results[prop.key] = success
                        if (success) {
                            prefs.edit().putString("$keyCurrentPrefix${prop.key}", original).apply()
                        }
                    }
                }
            }
            val states = buildPropertyStates()
            _propertiesFlow.value = states
            results
        }
    }

    override suspend fun forceRefresh(): Result<List<PropertyState>> = withContext(Dispatchers.IO) {
        runCatching {
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

            val states = buildPropertyStates()
            _propertiesFlow.value = states
            states
        }
    }

    override suspend fun clearAllBackups() {
        withContext(Dispatchers.IO) {
            prefs.edit().clear().apply()
            _propertiesFlow.value = emptyList()
        }
    }

    override suspend fun checkRoot(): Boolean = withContext(Dispatchers.IO) {
        val result = ShellExecutor.checkRoot()
        _rootState.value = result
        result
    }

    private fun isFirstBoot(): Boolean = !prefs.getBoolean(keyFirstBootChecked, false)

    private fun getOriginalValue(key: String): String? =
        prefs.getString("$keyOriginalPrefix$key", null)

    private fun getSavedCurrentValue(key: String): String? =
        prefs.getString("$keyCurrentPrefix$key", null)

    private fun isPropertyAvailable(key: String): Boolean =
        prefs.getBoolean("$keyAvailablePrefix$key", false)

    private fun buildPropertyStates(): List<PropertyState> {
        return PropertyRegistry.properties.map { prop ->
            val available = isPropertyAvailable(prop.key)
            val original = getOriginalValue(prop.key)
            val current = getSavedCurrentValue(prop.key)

            val displayCurrent = current ?: ""
            val isModified = original != null && original != displayCurrent

            PropertyState(
                property = prop,
                isAvailable = available,
                originalValue = original,
                currentValue = displayCurrent,
                isModified = isModified
            )
        }
    }
}
