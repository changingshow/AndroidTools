package com.permissionmonitor.data.repository

import android.content.Context
import com.permissionmonitor.data.model.AppInfo
import com.permissionmonitor.data.source.AppDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    
    private val appDataSource = AppDataSource(context)
    
    suspend fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            appDataSource.getInstalledApps(includeSystemApps)
        }
    }
    
    suspend fun getAppInfo(packageName: String): AppInfo? {
        return withContext(Dispatchers.IO) {
            appDataSource.getAppInfo(packageName)
        }
    }
}
