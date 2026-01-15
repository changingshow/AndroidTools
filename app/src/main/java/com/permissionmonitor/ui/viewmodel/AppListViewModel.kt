package com.permissionmonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.permissionmonitor.data.model.AppInfo
import com.permissionmonitor.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppListUiState(
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = true,
    val includeSystemApps: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppRepository(application)
    
    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()
    
    private var allApps: List<AppInfo> = emptyList()
    
    init {
        loadApps()
    }
    
    fun loadApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                allApps = withContext(Dispatchers.IO) {
                    repository.getInstalledApps(_uiState.value.includeSystemApps)
                }
                filterApps()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载应用列表失败: ${e.message}"
                )
            }
        }
    }
    
    fun toggleSystemApps() {
        _uiState.value = _uiState.value.copy(
            includeSystemApps = !_uiState.value.includeSystemApps
        )
        loadApps()
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterApps()
    }
    
    private fun filterApps() {
        val query = _uiState.value.searchQuery.lowercase()
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter { app ->
                app.appName.lowercase().contains(query) ||
                app.packageName.lowercase().contains(query)
            }
        }
        _uiState.value = _uiState.value.copy(
            apps = filtered,
            isLoading = false
        )
    }
}
