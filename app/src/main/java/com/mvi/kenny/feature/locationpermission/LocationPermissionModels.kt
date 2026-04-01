package com.mvi.kenny.feature.locationpermission

import java.time.LocalDateTime

/**
 * ============================================================
 * AppLocationInfo — App 位置权限信息数据模型
 * ============================================================
 * Describes a single app's location permission status.
 *
 * @param packageName App package name, unique identifier
 * @param appName App display name
 * @param permissionType Permission type: precise/coarse/one-time/always
 * @param lastAccessTime Last location access timestamp
 * @param accessCount Cumulative access count
 * @param permissionSource Permission source: user-granted/system-default/app-requested
 * @param isActive Whether the app is actively accessing location right now
 *
 * @see LocationPermissionType Permission type enum
 * @see PermissionSource Permission source enum
 */
data class AppLocationInfo(
    val packageName: String,
    val appName: String,
    val permissionType: LocationPermissionType,
    val lastAccessTime: LocalDateTime?,
    val accessCount: Int = 0,
    val permissionSource: PermissionSource = PermissionSource.USER_GRANTED,
    val isActive: Boolean = false
)

/**
 * ============================================================
 * LocationPermissionType — 位置权限类型枚举
 * ============================================================
 * Android 17 introduces new fine location modes.
 *
 * @param label Chinese label for UI display
 * @param androidApiLevel Corresponding Android API level
 */
enum class LocationPermissionType(val label: String, val androidApiLevel: Int) {
    /** Precise location (full address) */
    FINE("精确", 36),

    /** Coarse location (city-level) */
    COARSE("粗略", 36),

    /** One-time precise location (Android 17 new API) */
    FINE_ONE_TIME("一次性精确", 36),

    /** Foreground-only precise location */
    FOREGROUND_ONLY("仅前台", 36),

    /** Always precise location (high risk) */
    ALWAYS_FINE("始终精确", 36),

    /** No location permission */
    NONE("无权限", 0)
}

/**
 * ============================================================
 * PermissionSource — 权限来源枚举
 * ============================================================
 *
 * @param label Chinese label for display
 */
enum class PermissionSource(val label: String) {
    /** User actively granted */
    USER_GRANTED("用户授予"),

    /** System default permission */
    SYSTEM_DEFAULT("系统默认"),

    /** App self-requested */
    APP_REQUESTED("App申请")
}

/**
 * ============================================================
 * LocationEvent — 位置访问历史事件
 * ============================================================
 * Records a single location access event for history timeline display.
 *
 * @param id Unique event ID
 * @param packageName App package name
 * @param appName App display name
 * @param eventTime Event timestamp
 * @param permissionType Permission type at the time of access
 * @param isPrecise Whether it was precise location
 */
data class LocationEvent(
    val id: Long,
    val packageName: String,
    val appName: String,
    val eventTime: LocalDateTime,
    val permissionType: LocationPermissionType,
    val isPrecise: Boolean
)

/**
 * ============================================================
 * ThreatLevel — 威胁等级枚举
 * ============================================================
 *
 * @param level Threat level value (higher = more dangerous)
 * @param label Chinese label
 */
enum class ThreatLevel(val level: Int, val label: String) {
    NONE(0, "安全"),
    LOW(1, "低危"),
    MEDIUM(2, "中危"),
    HIGH(3, "高危")
}

/**
 * ============================================================
 * HistoryFilter — 历史记录筛选条件
 * ============================================================
 *
 * @param days Filter range: 1=today, 7=last 7 days, 30=last 30 days
 * @param showOnlyPrecise Whether to show only precise location events
 */
data class HistoryFilter(
    val days: Int = 1,
    val showOnlyPrecise: Boolean = false
) {
    companion object {
        val TODAY = HistoryFilter(days = 1)
        val WEEK = HistoryFilter(days = 7)
        val MONTH = HistoryFilter(days = 30)
    }
}

/**
 * ============================================================
 * PermissionSettings — 权限管控设置
 * ============================================================
 * Application-level settings for notification preferences and data retention.
 *
 * @param notifyOnFirstRequest Notify when a new app first requests precise location
 * @param notifyOnLongAccess Notify when location is accessed continuously for > 5 minutes
 * @param dataRetentionDays Data retention period: 7/30/90 days
 * @param android17ApisEnabled Whether to enable Android 17 new APIs
 */
data class PermissionSettings(
    val notifyOnFirstRequest: Boolean = true,
    val notifyOnLongAccess: Boolean = true,
    val dataRetentionDays: Int = 30,
    val android17ApisEnabled: Boolean = true
)
