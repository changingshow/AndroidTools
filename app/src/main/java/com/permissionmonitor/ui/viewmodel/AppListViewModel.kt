package com.permissionmonitor.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
    val error: String? = null,
    val needsPermission: Boolean = false,
    val totalApps: Int = 0,
    val safeApps: Int = 0,
    val riskyApps: Int = 0
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
                
                // 如果获取到的应用太少（少于5个），可能是没有权限
                val needsPermission = allApps.size < 5 && !_uiState.value.includeSystemApps
                
                // 计算统计数据
                val safeApps = allApps.count { it.dangerousPermissions == 0 }
                val riskyApps = allApps.count { it.dangerousPermissions > 0 }
                
                _uiState.value = _uiState.value.copy(
                    needsPermission = needsPermission,
                    totalApps = allApps.size,
                    safeApps = safeApps,
                    riskyApps = riskyApps
                )
                
                filterApps()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载失败: ${e.message}"
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
    
    fun openAppSettings() {
        val context = getApplication<Application>()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
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
