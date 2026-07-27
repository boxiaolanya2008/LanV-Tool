package github.boxiaolanya2008.lanv_tool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.domain.usecase.ApplyPropertyUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.CheckRootUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.ObservePropertiesUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RefreshPropertiesUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RestoreAllUseCase
import github.boxiaolanya2008.lanv_tool.domain.usecase.RestorePropertyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PropertyUiState {
    data object Loading : PropertyUiState
    data class Success(
        val properties: List<PropertyState>,
        val categories: List<PropertyCategory>,
        val modifiedCount: Int,
        val applyingKey: String? = null
    ) : PropertyUiState
    data class Error(
        val message: String,
        val isNoRoot: Boolean = false
    ) : PropertyUiState
}

sealed interface PropertyAction {
    data object LoadProperties : PropertyAction
    data class ApplyValue(val key: String, val value: String) : PropertyAction
    data class Restore(val key: String) : PropertyAction
    data object RestoreAll : PropertyAction
    data object Retry : PropertyAction
    data object ClearMessage : PropertyAction
}

class PropertyViewModel(
    private val observePropertiesUseCase: ObservePropertiesUseCase,
    private val refreshPropertiesUseCase: RefreshPropertiesUseCase,
    private val applyPropertyUseCase: ApplyPropertyUseCase,
    private val restorePropertyUseCase: RestorePropertyUseCase,
    private val restoreAllUseCase: RestoreAllUseCase,
    private val checkRootUseCase: CheckRootUseCase
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PropertyUiState> = observePropertiesUseCase()
        .map { properties ->
            if (properties.isEmpty()) {
                val hasRoot = checkRootUseCase()
                if (!hasRoot) {
                    return@map PropertyUiState.Error(
                        message = "未获取到 Root 权限，请确认设备已 Root 并授予本应用权限",
                        isNoRoot = true
                    )
                }
            }
            val categories = properties.map { it.property.category }.distinct()
            val modifiedCount = properties.count { it.isModified }
            PropertyUiState.Success(
                properties = properties,
                categories = categories,
                modifiedCount = modifiedCount
            )
        }
        .catch { e ->
            emit(PropertyUiState.Error(message = e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PropertyUiState.Loading
        )

    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        onAction(PropertyAction.LoadProperties)
    }

    fun onAction(action: PropertyAction) {
        when (action) {
            PropertyAction.LoadProperties -> loadProperties()
            is PropertyAction.ApplyValue -> applyProperty(action.key, action.value)
            is PropertyAction.Restore -> restoreProperty(action.key)
            PropertyAction.RestoreAll -> restoreAll()
            PropertyAction.Retry -> retry()
            PropertyAction.ClearMessage -> _message.value = null
        }
    }

    private fun loadProperties() {
        viewModelScope.launch {
            val hasRoot = checkRootUseCase()
            if (!hasRoot) {
                _message.value = "未获取到 Root 权限，请确认设备已 Root 并授予本应用权限"
                return@launch
            }
            refreshPropertiesUseCase().onFailure { e ->
                _message.value = e.message ?: "加载失败"
            }
        }
    }

    private fun applyProperty(key: String, value: String) {
        viewModelScope.launch {
            applyPropertyUseCase(key, value)
                .onSuccess {
                    val displayValue = if (value.isEmpty()) "空值" else value
                    _message.value = "已应用: $key = $displayValue"
                }
                .onFailure { e ->
                    _message.value = "应用失败: $key - ${e.message}"
                }
        }
    }

    private fun restoreProperty(key: String) {
        viewModelScope.launch {
            restorePropertyUseCase(key)
                .onSuccess { _message.value = "已还原" }
                .onFailure { e -> _message.value = "还原失败: ${e.message}" }
        }
    }

    private fun restoreAll() {
        viewModelScope.launch {
            restoreAllUseCase()
                .onSuccess { results ->
                    val successCount = results.count { it.value }
                    _message.value = "还原完成: 成功 $successCount 项"
                }
                .onFailure { e ->
                    _message.value = "还原失败: ${e.message}"
                }
        }
    }

    private fun retry() {
        viewModelScope.launch {
            refreshPropertiesUseCase().onFailure { e ->
                _message.value = e.message ?: "重试失败"
            }
        }
    }

    class Factory(
        private val observePropertiesUseCase: ObservePropertiesUseCase,
        private val refreshPropertiesUseCase: RefreshPropertiesUseCase,
        private val applyPropertyUseCase: ApplyPropertyUseCase,
        private val restorePropertyUseCase: RestorePropertyUseCase,
        private val restoreAllUseCase: RestoreAllUseCase,
        private val checkRootUseCase: CheckRootUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PropertyViewModel(
                observePropertiesUseCase = observePropertiesUseCase,
                refreshPropertiesUseCase = refreshPropertiesUseCase,
                applyPropertyUseCase = applyPropertyUseCase,
                restorePropertyUseCase = restorePropertyUseCase,
                restoreAllUseCase = restoreAllUseCase,
                checkRootUseCase = checkRootUseCase
            ) as T
        }
    }
}
