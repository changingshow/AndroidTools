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
    
    fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.GET_PERMISSIONS or PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_PERMISSIONS or PackageManager.GET_UNINSTALLED_PACKAGES
        }
        
        val packages = packageManager.getInstalledPackages(flags)
        
        return packages
            .filter { packageInfo ->
                if (includeSystemApps) {
                    true
                } else {
                    (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
            }
            .filter { it.packageName != context.packageName }
            .map { packageInfo -> createAppInfo(packageInfo) }
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
    
    private fun createAppInfo(packageInfo: PackageInfo): AppInfo {
        val applicationInfo = packageInfo.applicationInfo
        val permissions = getPermissionDetails(packageInfo)
        
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        
        return AppInfo(
            packageName = packageInfo.packageName,
            appName = applicationInfo.loadLabel(packageManager).toString(),
            icon = applicationInfo.loadIcon(packageManager),
            versionName = packageInfo.versionName,
            versionCode = versionCode,
            isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            permissions = permissions,
            totalPermissions = permissions.size,
            grantedPermissions = permissions.count { it.isGranted },
            dangerousPermissions = permissions.count { 
                PermissionClassifier.isDangerousPermission(it.name) && it.isGranted 
            }
        )
    }
    
    private fun getPermissionDetails(packageInfo: PackageInfo): List<PermissionDetail> {
        val requestedPermissions = packageInfo.requestedPermissions ?: return emptyList()
        val requestedPermissionsFlags = packageInfo.requestedPermissionsFlags ?: return emptyList()
        
        return requestedPermissions.mapIndexed { index, permission ->
            val isGranted = if (index < requestedPermissionsFlags.size) {
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
