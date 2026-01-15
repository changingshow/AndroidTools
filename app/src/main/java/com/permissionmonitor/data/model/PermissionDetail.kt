package com.permissionmonitor.data.model

data class PermissionDetail(
    val name: String,
    val isGranted: Boolean,
    val riskLevel: PermissionRisk,
    val group: String?,
    val description: String?
)

enum class PermissionRisk(val displayName: String, val color: Long) {
    LOW("低风险", 0xFF4CAF50),
    MEDIUM("中风险", 0xFFFF9800),
    HIGH("高风险", 0xFFFF5722),
    DANGEROUS("危险", 0xFFF44336)
}
