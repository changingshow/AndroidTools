package com.permissionmonitor.data.source

import android.Manifest
import com.permissionmonitor.data.model.PermissionRisk

object PermissionClassifier {
    
    private val dangerousPermissions = setOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.POST_NOTIFICATIONS",
        "android.permission.NEARBY_WIFI_DEVICES",
        "android.permission.BODY_SENSORS_BACKGROUND"
    )
    
    private val highRiskPermissions = setOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_CONTACTS,
        "android.permission.BODY_SENSORS_BACKGROUND"
    )
    
    private val mediumRiskPermissions = setOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.GET_ACCOUNTS,
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO"
    )
    
    private val permissionDescriptions = mapOf(
        Manifest.permission.CAMERA to "允许应用访问摄像头拍照和录像",
        Manifest.permission.RECORD_AUDIO to "允许应用录制音频",
        Manifest.permission.ACCESS_FINE_LOCATION to "允许应用获取精确位置（GPS）",
        Manifest.permission.ACCESS_COARSE_LOCATION to "允许应用获取大致位置（网络）",
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to "允许应用在后台获取位置",
        Manifest.permission.READ_CONTACTS to "允许应用读取联系人",
        Manifest.permission.WRITE_CONTACTS to "允许应用修改联系人",
        Manifest.permission.READ_SMS to "允许应用读取短信",
        Manifest.permission.SEND_SMS to "允许应用发送短信",
        Manifest.permission.RECEIVE_SMS to "允许应用接收短信",
        Manifest.permission.READ_CALL_LOG to "允许应用读取通话记录",
        Manifest.permission.WRITE_CALL_LOG to "允许应用修改通话记录",
        Manifest.permission.CALL_PHONE to "允许应用直接拨打电话",
        Manifest.permission.READ_PHONE_STATE to "允许应用读取手机状态和身份",
        Manifest.permission.READ_EXTERNAL_STORAGE to "允许应用读取存储空间",
        Manifest.permission.WRITE_EXTERNAL_STORAGE to "允许应用写入存储空间",
        Manifest.permission.READ_CALENDAR to "允许应用读取日历",
        Manifest.permission.WRITE_CALENDAR to "允许应用修改日历",
        Manifest.permission.BODY_SENSORS to "允许应用访问身体传感器（如心率）",
        "android.permission.READ_MEDIA_IMAGES" to "允许应用读取图片",
        "android.permission.READ_MEDIA_VIDEO" to "允许应用读取视频",
        "android.permission.READ_MEDIA_AUDIO" to "允许应用读取音频文件",
        "android.permission.POST_NOTIFICATIONS" to "允许应用发送通知"
    )
    
    private val permissionGroups = mapOf(
        Manifest.permission.CAMERA to "相机",
        Manifest.permission.RECORD_AUDIO to "麦克风",
        Manifest.permission.ACCESS_FINE_LOCATION to "位置",
        Manifest.permission.ACCESS_COARSE_LOCATION to "位置",
        Manifest.permission.ACCESS_BACKGROUND_LOCATION to "位置",
        Manifest.permission.READ_CONTACTS to "通讯录",
        Manifest.permission.WRITE_CONTACTS to "通讯录",
        Manifest.permission.GET_ACCOUNTS to "通讯录",
        Manifest.permission.READ_SMS to "短信",
        Manifest.permission.SEND_SMS to "短信",
        Manifest.permission.RECEIVE_SMS to "短信",
        Manifest.permission.RECEIVE_MMS to "短信",
        Manifest.permission.RECEIVE_WAP_PUSH to "短信",
        Manifest.permission.READ_CALL_LOG to "通话",
        Manifest.permission.WRITE_CALL_LOG to "通话",
        Manifest.permission.CALL_PHONE to "通话",
        Manifest.permission.ANSWER_PHONE_CALLS to "通话",
        Manifest.permission.READ_PHONE_STATE to "电话",
        Manifest.permission.READ_PHONE_NUMBERS to "电话",
        Manifest.permission.READ_EXTERNAL_STORAGE to "存储",
        Manifest.permission.WRITE_EXTERNAL_STORAGE to "存储",
        Manifest.permission.READ_CALENDAR to "日历",
        Manifest.permission.WRITE_CALENDAR to "日历",
        Manifest.permission.BODY_SENSORS to "传感器",
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
