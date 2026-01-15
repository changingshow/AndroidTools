package com.permissionmonitor.data.source

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.permissionmonitor.data.model.AppInfo
import com.permissionmonitor.data.model.PermissionDetail

class AppDataSource(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    
    @Suppress("DEPRECATION")
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val flags = PackageManager.GET_PERMISSIONS
        val packages = packageManager.getInstalledPackages(flags)
        
        return packages
            .filter { packageInfo ->
                val appInfo = packageInfo.applicationInfo
                if (appInfo == null) {
                    false
                } else if (includeSystemApps) {
                    true
                } else {
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
            }
            .filter { it.packageName != context.packageName }
            .mapNotNull { packageInfo -> createAppInfo(packageInfo) }
            .sortedByDescending { it.dangerousPermissions }
    }
    
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val flags = PackageManager.GET_PERMISSIONS
            val packageInfo = packageManager.getPackageInfo(packageName, flags)
            createAppInfo(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun createAppInfo(packageInfo: PackageInfo): AppInfo? {
        val applicationInfo = packageInfo.applicationInfo ?: return null
        val permissions = getPermissionDetails(packageInfo)
        
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
        
        return AppInfo(
            packageName = packageInfo.packageName,
            appName = applicationInfo.loadLabel(packageManager).toString(),
            icon = try { applicationInfo.loadIcon(packageManager) } catch (e: Exception) { null },
            versionName = packageInfo.versionName,
            versionCode = versionCode,
            isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            permissions = permissions,
            totalPermissions = permissions.size,
            grantedPermissions = permissions.count { it.isGranted },
            dangerousPermissions = permissions.count { 
                PermissionClassifier.isDangerousPermission(it.name) && it.isGranted 
            },
            installTime = packageInfo.firstInstallTime,
            updateTime = packageInfo.lastUpdateTime
        )
    }
    
    private fun getPermissionDetails(packageInfo: PackageInfo): List<PermissionDetail> {
        val requestedPermissions = packageInfo.requestedPermissions ?: return emptyList()
        val requestedPermissionsFlags = packageInfo.requestedPermissionsFlags
        
        return requestedPermissions.mapIndexed { index, permission ->
            val isGranted = if (requestedPermissionsFlags != null && index < requestedPermissionsFlags.size) {
                (requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            } else {
                false
            }
            
            PermissionDetail(
                name = permission,
                isGranted = isGranted,
                riskLevel = PermissionClassifier.classifyPermission(permission),
                group = PermissionClassifier.getPermissionGroup(permission),
                description = PermissionClassifier.getPermissionDescription(permission)
            )
        }.sortedByDescending { it.riskLevel.ordinal }
    }
}
