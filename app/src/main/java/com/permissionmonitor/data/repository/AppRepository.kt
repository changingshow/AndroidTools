package com.permissionmonitor.data.repository

import android.content.Context
import com.permissionmonitor.data.model.AppInfo
import com.permissionmonitor.data.source.AppDataSource

class AppRepository(context: Context) {
    
    private val appDataSource = AppDataSource(context)
    
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        return appDataSource.getInstalledApps(includeSystemApps)
    }
    
    fun getAppInfo(packageName: String): AppInfo? {
        return appDataSource.getAppInfo(packageName)
    }
}
