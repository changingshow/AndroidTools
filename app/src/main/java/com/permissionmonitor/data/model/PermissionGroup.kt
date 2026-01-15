package com.permissionmonitor.data.model

data class PermissionGroup(
    val groupName: String,
    val displayName: String,
    val permissions: List<PermissionDetail>,
    val iconResName: String = "ic_permission"
)
