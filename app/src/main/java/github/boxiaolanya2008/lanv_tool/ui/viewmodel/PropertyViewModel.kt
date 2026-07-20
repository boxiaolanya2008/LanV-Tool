package github.boxiaolanya2008.lanv_tool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.repository.PropertyRepository
import github.boxiaolanya2008.lanv_tool.data.shell.PropManager
import github.boxiaolanya2008.lanv_tool.data.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PropertyUiState(
    val isLoading: Boolean = true,
    val hasRoot: Boolean = false,
    val rootChecked: Boolean = false,
    val properties: List<PropertyState> = emptyList(),
    val categories: List<PropertyCategory> = emptyList(),
    val applyingKey: String? = null,
    val message: String? = null
)

class PropertyViewModel(
    private val repository: PropertyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PropertyUiState())
    val uiState: StateFlow<PropertyUiState> = _uiState.asStateFlow()

    init {
        checkRootAndLoad()
    }

    private fun checkRootAndLoad() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, rootChecked = false)
            val hasRoot = withContext(Dispatchers.IO) { ShellExecutor.checkRoot() }
            if (!hasRoot) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasRoot = false,
                    rootChecked = true,
                    message = "未获取到 Root 权限，请确认设备已 Root 并授予本应用权限"
                )
                return@launch
            }
            _uiState.value = _uiState.value.copy(hasRoot = true)
            loadProperties()
        }
    }

    fun loadProperties() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val states = withContext(Dispatchers.IO) {
                repository.refreshProperties()
            }
            val availableStates = states.filter { it.isAvailable || it.originalValue != null }
            val categories = availableStates.map { it.property.category }.distinct()

            val message = if (availableStates.isEmpty()) {
                "未获取到任何属性值，请确认是否为 Vivo/OriginOS 设备"
            } else {
                null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasRoot = true,
                rootChecked = true,
                properties = availableStates,
                categories = categories,
                message = message
            )
        }
    }

    fun retryCheck() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.clearAllBackups()
            }
            checkRootAndLoad()
        }
    }

    fun applyProperty(key: String, value: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(applyingKey = key)
            val success = withContext(Dispatchers.IO) {
                val setResult = PropManager.setProp(key, value)
                if (setResult) {
                    repository.saveCurrentValue(key, value)
                }
                setResult
            }
            val displayValue = if (value.isEmpty()) "空值" else value
            val message = if (success) "已应用: $key = $displayValue" else "应用失败: $key"
            val states = withContext(Dispatchers.IO) { repository.refreshProperties() }
            val availableStates = states.filter { it.isAvailable || it.originalValue != null }
            val categories = availableStates.map { it.property.category }.distinct()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasRoot = true,
                properties = availableStates,
                categories = categories,
                applyingKey = null,
                message = message
            )
        }
    }

    fun restoreProperty(key: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(applyingKey = key)
            val success = withContext(Dispatchers.IO) {
                repository.restoreOriginal(key)
            }
            val message = if (success) "已还原" else "还原失败"
            val states = withContext(Dispatchers.IO) { repository.refreshProperties() }
            val availableStates = states.filter { it.isAvailable || it.originalValue != null }
            val categories = availableStates.map { it.property.category }.distinct()
            _uiState.value = _uiState.value.copy(
                properties = availableStates,
                categories = categories,
                applyingKey = null,
                message = message
            )
        }
    }

    fun restoreAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val results = withContext(Dispatchers.IO) {
                repository.restoreAll()
            }
            val successCount = results.count { it.value }
            val states = withContext(Dispatchers.IO) { repository.refreshProperties() }
            val availableStates = states.filter { it.isAvailable || it.originalValue != null }
            val categories = availableStates.map { it.property.category }.distinct()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                properties = availableStates,
                categories = categories,
                message = "还原完成: 成功 $successCount 项"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(private val repository: PropertyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PropertyViewModel(repository) as T
        }
    }
}
