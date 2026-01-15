package com.permissionmonitor.data.source

import com.permissionmonitor.data.model.PermissionRisk

object PermissionClassifier {
    
    private val dangerousPermissions = setOf(
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",
        "android.permission.CAMERA",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.GET_ACCOUNTS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_PHONE_NUMBERS",
        "android.permission.CALL_PHONE",
        "android.permission.ANSWER_PHONE_CALLS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ADD_VOICEMAIL",
        "android.permission.USE_SIP",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.BODY_SENSORS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_WAP_PUSH",
        "android.permission.RECEIVE_MMS",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.BODY_SENSORS_BACKGROUND"
    )
    
    private val highRiskPermissions = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_CONTACTS",
        "android.permission.BODY_SENSORS_BACKGROUND"
    )
    
    private val mediumRiskPermissions = setOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_PHONE_STATE",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.GET_ACCOUNTS",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO"
    )
    
    private val permissionDescriptions = mapOf(
        "android.permission.CAMERA" to "允许应用访问摄像头拍照和录像",
        "android.permission.RECORD_AUDIO" to "允许应用录制音频",
        "android.permission.ACCESS_FINE_LOCATION" to "允许应用获取精确位置（GPS）",
        "android.permission.ACCESS_COARSE_LOCATION" to "允许应用获取大致位置（网络）",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "允许应用在后台获取位置",
        "android.permission.READ_CONTACTS" to "允许应用读取联系人",
        "android.permission.WRITE_CONTACTS" to "允许应用修改联系人",
        "android.permission.READ_SMS" to "允许应用读取短信",
        "android.permission.SEND_SMS" to "允许应用发送短信",
        "android.permission.RECEIVE_SMS" to "允许应用接收短信",
        "android.permission.READ_CALL_LOG" to "允许应用读取通话记录",
        "android.permission.WRITE_CALL_LOG" to "允许应用修改通话记录",
        "android.permission.CALL_PHONE" to "允许应用直接拨打电话",
        "android.permission.READ_PHONE_STATE" to "允许应用读取手机状态和身份",
        "android.permission.READ_EXTERNAL_STORAGE" to "允许应用读取存储空间",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "允许应用写入存储空间",
        "android.permission.READ_CALENDAR" to "允许应用读取日历",
        "android.permission.WRITE_CALENDAR" to "允许应用修改日历",
        "android.permission.BODY_SENSORS" to "允许应用访问身体传感器（如心率）",
        "android.permission.READ_MEDIA_IMAGES" to "允许应用读取图片",
        "android.permission.READ_MEDIA_VIDEO" to "允许应用读取视频",
        "android.permission.READ_MEDIA_AUDIO" to "允许应用读取音频文件",
        "android.permission.POST_NOTIFICATIONS" to "允许应用发送通知"
    )
    
    private val permissionGroups = mapOf(
        "android.permission.CAMERA" to "相机",
        "android.permission.RECORD_AUDIO" to "麦克风",
        "android.permission.ACCESS_FINE_LOCATION" to "位置",
        "android.permission.ACCESS_COARSE_LOCATION" to "位置",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "位置",
        "android.permission.READ_CONTACTS" to "通讯录",
        "android.permission.WRITE_CONTACTS" to "通讯录",
        "android.permission.GET_ACCOUNTS" to "通讯录",
        "android.permission.READ_SMS" to "短信",
        "android.permission.SEND_SMS" to "短信",
        "android.permission.RECEIVE_SMS" to "短信",
        "android.permission.RECEIVE_MMS" to "短信",
        "android.permission.RECEIVE_WAP_PUSH" to "短信",
        "android.permission.READ_CALL_LOG" to "通话",
        "android.permission.WRITE_CALL_LOG" to "通话",
        "android.permission.CALL_PHONE" to "通话",
        "android.permission.ANSWER_PHONE_CALLS" to "通话",
        "android.permission.READ_PHONE_STATE" to "电话",
        "android.permission.READ_PHONE_NUMBERS" to "电话",
        "android.permission.READ_EXTERNAL_STORAGE" to "存储",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "存储",
        "android.permission.READ_CALENDAR" to "日历",
        "android.permission.WRITE_CALENDAR" to "日历",
        "android.permission.BODY_SENSORS" to "传感器",
        "android.permission.READ_MEDIA_IMAGES" to "媒体",
        "android.permission.READ_MEDIA_VIDEO" to "媒体",
        "android.permission.READ_MEDIA_AUDIO" to "媒体",
        "android.permission.POST_NOTIFICATIONS" to "通知"
    )
    
    fun classifyPermission(permission: String): PermissionRisk {
        return when {
            permission in highRiskPermissions -> PermissionRisk.DANGEROUS
            permission in dangerousPermissions -> PermissionRisk.HIGH
            permission in mediumRiskPermissions -> PermissionRisk.MEDIUM
            else -> PermissionRisk.LOW
        }
    }
    
    fun isDangerousPermission(permission: String): Boolean {
        return permission in dangerousPermissions
    }
    
    fun getPermissionDescription(permission: String): String? {
        return permissionDescriptions[permission]
    }
    
    fun getPermissionGroup(permission: String): String? {
        return permissionGroups[permission]
    }
    
    fun getSimplePermissionName(permission: String): String {
        return permission.substringAfterLast(".")
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}
