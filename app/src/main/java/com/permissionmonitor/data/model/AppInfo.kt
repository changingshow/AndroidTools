package com.permissionmonitor.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val versionName: String?,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val permissions: List<PermissionDetail> = emptyList(),
    val totalPermissions: Int = 0,
    val grantedPermissions: Int = 0,
    val dangerousPermissions: Int = 0,
    val installTime: Long = 0,
    val updateTime: Long = 0
)
