package com.mvi.kenny.feature.handoff

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * HandoffModels — Android 17 Cross-Device Handoff 数据模型
 * ============================================================
 */

// HandoffScreen — 所有子页面路由枚举
enum class HandoffScreen(val title: String, val description: String) {
    Dashboard("Handoff Dashboard", "跨设备 Handoff 工具台"),
    ApiLibrary("API Library", "Handoff API 封装库"),
    Analyzer("Analyzer", "适用性分析器"),
    Serialization("Serialization", "序列化框架"),
    Pairing("Pairing", "设备发现与配对"),
    UXGuide("UX Guide", "UX 设计规范"),
    Debug("Debug Panel", "调试面板")
}

// PairedDevice — 已配对设备数据类
data class PairedDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val lastHandoffTime: Long = 0L,
    val isOnline: Boolean = false
)

enum class DeviceType(val displayName: String, val emoji: String) {
    PHONE("手机", "📱"),
    TABLET("平板", "📱"),
    WATCH("手表", "⌚"),
    CAR("车载", "🚗"),
    PC("电脑", "💻"),
    TV("电视", "📺"),
    UNKNOWN("未知设备", "❓")
}

// HandoffRecord — Handoff 历史记录
data class HandoffRecord(
    val id: String,
    val appPackage: String,
    val activityName: String,
    val targetDeviceId: String,
    val timestamp: Long,
    val status: HandoffStatus,
    val dataSizeBytes: Int = 0
)

enum class HandoffStatus(val color: Color, val displayName: String) {
    SUCCESS(Color(0xFF4CAF50), "成功"),
    FAILED(Color(0xFFB3261E), "失败"),
    CANCELLED(Color(0xFF9E9E9E), "已取消"),
    PENDING(Color(0xFF625B71), "等待中")
}

// HandoffFeature — 功能入口卡片数据
data class HandoffFeature(
    val screen: HandoffScreen,
    val title: String,
    val titleEn: String,
    val description: String,
    val iconName: String,
    val isNew: Boolean = false
)

// ApiExample — API 示例数据类
data class ApiExample(
    val title: String,
    val code: String,
    val description: String,
    val tags: List<String> = emptyList()
)

// HandoffConfig — Handoff 配置数据类
data class HandoffConfig(
    val autoResume: Boolean = true,
    val priority: HandoffPriority = HandoffPriority.NORMAL,
    val expirationMillis: Long = 5 * 60 * 1000L,
    val webFallbackUrl: String = ""
)

enum class HandoffPriority(val displayName: String) {
    HIGH("高优先级"),
    NORMAL("普通"),
    LOW("低优先级")
}

// ActivityHandoffScore — Activity Handoff 适配评分
data class ActivityHandoffScore(
    val activityName: String,
    val simpleName: String,
    val score: Int,
    val recommendation: String,
    val isHandoffable: Boolean,
    val reasons: List<String>,
    val dependencies: List<String> = emptyList()
)

// ActivityDependencyTree — Activity 依赖树
data class ActivityDependencyTree(
    val activityName: String,
    val children: List<ActivityDependencyTree> = emptyList(),
    val isRoot: Boolean = false,
    val handoffScore: Int? = null
)

enum class ScanStatus(val displayName: String) {
    IDLE("等待扫描"),
    SCANNING("扫描中"),
    DONE("扫描完成"),
    ERROR("扫描失败")
}

enum class SerializationStrategy(val displayName: String, val description: String) {
    LWW("LWW", "Last-Write-Wins — 时间戳最新的数据胜出"),
    MERGE("Merge", "合并 — 尝试合并两边状态"),
    MANUAL_CONFIRM("Manual", "手动确认 — 用户选择保留哪个版本")
}

enum class ApiLibraryTab(val title: String) {
    QUICK_START("快速接入"),
    API_USAGE("API 用法"),
    CONFIG("参数配置")
}

data class SecurityFilter(
    val name: String,
    val fieldPattern: String,
    val description: String,
    val enabled: Boolean = true
)

data class ConflictData(
    val fieldName: String,
    val localValue: String,
    val remoteValue: String,
    val resolution: String = ""
)

enum class SerializationTemplate(val title: String, val description: String) {
    BASIC_STATE("基础状态", "仅传输 UI 状态：文本输入、光标位置、滚动偏移"),
    DOCUMENT_STATE("文档状态", "传输文档内容：文本、图片引用、光标范围"),
    FORM_STATE("表单状态", "传输表单数据：字段值、验证状态、填写进度"),
    MEDIA_STATE("媒体状态", "传输媒体上下文：播放位置、队列、偏好设置")
}
