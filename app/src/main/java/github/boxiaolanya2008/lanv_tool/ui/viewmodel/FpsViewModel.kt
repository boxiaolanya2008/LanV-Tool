package github.boxiaolanya2008.lanv_tool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.lanv_tool.data.model.AppFpsState
import github.boxiaolanya2008.lanv_tool.data.model.AppInfo
import github.boxiaolanya2008.lanv_tool.data.model.FpsLevel
import github.boxiaolanya2008.lanv_tool.data.repository.FpsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FpsUiState {
    data object Loading : FpsUiState
    data class Success(
        val appStates: List<AppFpsState>,
        val installedApps: List<AppInfo>
    ) : FpsUiState
    data class Error(val message: String) : FpsUiState
}

sealed interface FpsAction {
    data object LoadData : FpsAction
    data class SetFps(val packageName: String, val level: FpsLevel) : FpsAction
    data class RemoveApp(val packageName: String) : FpsAction
    data object ClearMessage : FpsAction
}

class FpsViewModel(
    private val repository: FpsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FpsUiState>(FpsUiState.Loading)
    val uiState: StateFlow<FpsUiState> = _uiState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        onAction(FpsAction.LoadData)
    }

    fun onAction(action: FpsAction) {
        when (action) {
            FpsAction.LoadData -> loadData()
            is FpsAction.SetFps -> setFps(action.packageName, action.level)
            is FpsAction.RemoveApp -> removeApp(action.packageName)
            FpsAction.ClearMessage -> _message.value = null
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = FpsUiState.Loading

            val statesResult = repository.getFpsStates()
            val appsResult = repository.getInstalledApps()

            val states = statesResult.getOrElse {
                _uiState.value = FpsUiState.Error(it.message ?: "读取配置失败")
                return@launch
            }

            val apps = appsResult.getOrElse {
                _uiState.value = FpsUiState.Error(it.message ?: "获取应用列表失败")
                return@launch
            }

            _uiState.value = FpsUiState.Success(states, apps)
        }
    }

    private fun setFps(packageName: String, level: FpsLevel) {
        viewModelScope.launch {
            repository.setFpsLevel(packageName, level)
                .onSuccess {
                    _message.value = "已设置 ${level.label}"
                    loadData()
                }
                .onFailure {
                    _message.value = "设置失败: ${it.message}"
                }
        }
    }

    private fun removeApp(packageName: String) {
        viewModelScope.launch {
            repository.removeApp(packageName)
                .onSuccess {
                    _message.value = "已移除"
                    loadData()
                }
                .onFailure {
                    _message.value = "移除失败: ${it.message}"
                }
        }
    }

    class Factory(
        private val repository: FpsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FpsViewModel(repository) as T
        }
    }
}
