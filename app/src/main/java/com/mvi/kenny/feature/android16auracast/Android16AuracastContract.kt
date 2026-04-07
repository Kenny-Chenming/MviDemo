package com.mvi.kenny.feature.android16auracast

import android.os.Build
import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Android16AuracastContract — Android 16 Auracast 开发工具包 MVI 契约
 * ============================================================
 * PRD-045 | Android 16 Beta 3 Platform Stability 迁移与 Auracast 开发工具包
 *
 * 功能模块（6个Tab）：
 * - AURACAST_BROADCAST → LE Audio 广播管理（发送端）
 * - AURACAST_RECEIVER  → LE Audio 接收管理（接收端）
 * - OUTLINE_TEXT       → Android 16 无障碍 Outline Text API 组件
 * - LOCAL_NETWORK      → Local Network 权限引导 UI
 * - BEHAVIOR_CHANGE    → Android 16 行为变更速查面板
 * - MIGRATION          → 迁移检测与 Gradle 报告面板
 *
 * 设计参考：memory/agency/designs/PRD-045-Android16-Beta3-Platform-Stability-迁移与Auracast开发工具包.md
 * @see Android16AuracastViewModel 状态管理逻辑
 * @see Android16AuracastScreen UI 渲染层
 */

// ============================================================
// Enums / 枚举定义
// ============================================================

/**
 * 工具模块 Tab 枚举
 *
 * @param title 中文标题
 * @param iconName Material 图标名称
 * @param description 模块功能描述
 */
enum class Android16Tab(
    val title: String,
    val iconName: String,
    val description: String
) {
    AURACAST_BROADCAST("广播发送", "radio", "通过 LE Audio Auracast 广播音频流"),
    AURACAST_RECEIVER("广播接收", "headphones", "发现并接收附近的 Auracast 音频广播"),
    OUTLINE_TEXT("Outline Text", "text_fields", "Android 16 无障碍 Outline Text API"),
    LOCAL_NETWORK("Local Network", "wifi", "Local Network 权限引导 UI"),
    BEHAVIOR_CHANGE("行为变更", "warning", "Android 16 行为变更速查面板"),
    MIGRATION("迁移检测", "move_to_inbox", "Android 16 迁移检测与 Gradle 报告")
}

/**
 * 广播会话状态
 *
 * @param IDLE        空闲状态
 * @param STARTING    正在启动广播
 * @param BROADCASTING 正在广播中
 * @param PAUSED      暂停广播
 * @param ERROR       错误状态
 */
enum class BroadcastStatus {
    IDLE,
    STARTING,
    BROADCASTING,
    PAUSED,
    ERROR
}

/**
 * 接收器状态
 *
 * @param IDLE        空闲，未在扫描
 * @param SCANNING    正在扫描附近的 Auracast 源
 * @param RECEIVING   正在接收音频
 * @param ERROR       错误状态
 */
enum class ReceiverStatus {
    IDLE,
    SCANNING,
    RECEIVING,
    ERROR
}

/**
 * 变更严重程度
 *
 * @param P0 严重错误（红色 #B3261E），必须处理
 * @param P1 警告（橙色 #E8A317），强烈建议处理
 * @param P2 信息（灰色 #625B71），可选处理
 */
enum class Severity(val color: Long) {
    P0(0xFFB3261E),
    P1(0xFFE8A317),
    P2(0xFF625B71)
}

/**
 * 行为变更类别
 *
 * @param PRIVACY        隐私权限类变更
 * @param BEHAVIOR       系统行为类变更
 * @param API            API 变更
 * @param DEPRECATION    废弃类变更
 */
enum class BehaviorCategory {
    PRIVACY,
    BEHAVIOR,
    API,
    DEPRECATION
}

/**
 * Outline Text 样式枚举
 *
 * @param Default    药丸形背景，文字下方（Android 16+ 默认样式）
 * @param Highlight  纯色高亮
 * @param Underline  下划线强调
 */
enum class OutlineTextStyle {
    Default,
    Highlight,
    Underline
}

/**
 * 权限状态
 *
 * @param NOT_DETERMINED  未确定（未申请过）
 * @param GRANTED         已授权
 * @param DENIED          被拒绝
 * @param DENIED_FOREVER  永久拒绝（需手动到设置开启）
 */
enum class PermissionState {
    NOT_DETERMINED,
    GRANTED,
    DENIED,
    DENIED_FOREVER
}

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 广播会话数据类
 * 代表一个正在进行的 Auracast 广播会话
 *
 * @param sessionId   会话唯一 ID
 * @param name        广播名称（用户友好名称）
 * @param broadcastId 底层广播 ID
 * @param startedAt   开始时间（毫秒时间戳）
 * @param status      广播状态
 */
data class BroadcastSession(
    val sessionId: String,
    val name: String,
    val broadcastId: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val status: BroadcastStatus = BroadcastStatus.IDLE
)

/**
 * Auracast 音频源数据类
 * 代表一个被发现的 Auracast 广播源
 *
 * @param sourceId    源唯一 ID
 * @param name        广播源名称
 * @param deviceName  设备名称
 * @param signalStrength 信号强度（dBm）
 * @param isConnected 是否已连接
 */
data class AuracastSource(
    val sourceId: String,
    val name: String,
    val deviceName: String,
    val signalStrength: Int = 0,
    val isConnected: Boolean = false
)

/**
 * Auracast 事件 sealed class
 * 接收端产生的各类事件通知
 *
 * @param SourceDiscovered   发现新广播源
 * @param SourceLost         广播源丢失
 * @param SourceConnected    已连接广播源
 * @param SourceDisconnected 断开广播源连接
 * @param AudioStarted       音频开始播放
 * @param AudioStopped       音频停止播放
 * @param Error              错误事件
 */
sealed class AuracastEvent {
    data class SourceDiscovered(val source: AuracastSource) : AuracastEvent()
    data class SourceLost(val sourceId: String) : AuracastEvent()
    data class SourceConnected(val source: AuracastSource) : AuracastEvent()
    data class SourceDisconnected(val sourceId: String) : AuracastEvent()
    data class AudioStarted(val sourceId: String) : AuracastEvent()
    data class AudioStopped(val sourceId: String) : AuracastEvent()
    data class Error(val message: String) : AuracastEvent()
}

/**
 * 行为变更数据类
 * 代表一条 Android 平台行为变更记录
 *
 * @param id           变更 ID
 * @param title        变更标题
 * @param description  变更描述
 * @param severity     严重程度
 * @param category     变更类别
 * @param version      引入版本（如 "Android 16"）
 * @param docUrl       官方文档链接
 * @param affectedFiles 受影响文件列表（Gradle 插件端使用）
 * @param migrationGuide 迁移指南
 */
data class BehaviorChange(
    val id: String,
    val title: String,
    val description: String,
    val severity: Severity,
    val category: BehaviorCategory,
    val version: String = "Android 16",
    val docUrl: String = "",
    val affectedFiles: List<String> = emptyList(),
    val migrationGuide: String = ""
)

// ============================================================
// MVI State / 状态
// ============================================================

/**
 * Auracast 广播状态
 *
 * @param isBroadcasting    是否正在广播
 * @param broadcastSession  当前广播会话（null 表示无活跃会话）
 * @param broadcastName     广播名称输入
 * @param status            广播状态
 * @param error             错误信息
 */
data class AuracastBroadcastState(
    val isBroadcasting: Boolean = false,
    val broadcastSession: BroadcastSession? = null,
    val broadcastName: String = "",
    val status: BroadcastStatus = BroadcastStatus.IDLE,
    val error: String? = null
)

/**
 * Auracast 接收状态
 *
 * @param status              接收器状态
 * @param availableSources    可用广播源列表
 * @param connectedSourceId    已连接的源 ID（null 表示未连接）
 * @param isScanning          是否正在扫描
 * @param error               错误信息
 */
data class AuracastReceiverState(
    val status: ReceiverStatus = ReceiverStatus.IDLE,
    val availableSources: List<AuracastSource> = emptyList(),
    val connectedSourceId: String? = null,
    val isScanning: Boolean = false,
    val error: String? = null
)

/**
 * Outline Text 状态
 *
 * @param currentStyle   当前选择的样式
 * @param textInput      用户输入的文本
 * @param autoAccessible 是否启用自动无障碍检测
 */
data class OutlineTextState(
    val currentStyle: OutlineTextStyle = OutlineTextStyle.Default,
    val textInput: String = "Hello Android 16",
    val autoAccessible: Boolean = true
)

/**
 * Local Network 权限状态
 *
 * @param permissionState       当前权限状态
 * @param showIntro             是否显示权限说明页
 * @param showDeniedPage        是否显示拒绝引导页
 */
data class LocalNetworkState(
    val permissionState: PermissionState = PermissionState.NOT_DETERMINED,
    val showIntro: Boolean = false,
    val showDeniedPage: Boolean = false
)

/**
 * 行为变更速查状态
 *
 * @param selectedVersionTab 当前选中的版本 Tab（Android 16/15/14）
 * @param behaviorChanges    当前版本的行为变更列表
 * @param searchQuery        搜索关键词
 * @param selectedSeverity   严重程度过滤（null 表示全部）
 * @param selectedCategory   类别过滤（null 表示全部）
 * @param expandedChangeId   展开的变更 ID（null 表示全部收起）
 */
data class BehaviorChangeState(
    val selectedVersionTab: String = "Android 16",
    val behaviorChanges: List<BehaviorChange> = emptyList(),
    val searchQuery: String = "",
    val selectedSeverity: Severity? = null,
    val selectedCategory: BehaviorCategory? = null,
    val expandedChangeId: String? = null
)

/**
 * 迁移检测状态
 *
 * @param projectPath       项目路径
 * @param targetVersion     目标版本
 * @param isScanning        是否正在扫描
 * @param scanProgress      扫描进度（0.0 ~ 1.0）
 * @param currentFile       正在扫描的当前文件
 * @param behaviorChanges   扫描发现的行为变更列表
 * @param error             错误信息
 */
data class MigrationState(
    val projectPath: String = "",
    val targetVersion: String = "Android 16",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentFile: String = "",
    val behaviorChanges: List<BehaviorChange> = emptyList(),
    val error: String? = null
) {
    companion object {
        val Initial = MigrationState()
    }
}

/**
 * 主页面完整状态
 *
 * @param currentTab              当前激活的 Tab
 * @param auracastBroadcastState 广播发送状态
 * @param auracastReceiverState   广播接收状态
 * @param outlineTextState        Outline Text 状态
 * @param localNetworkState       Local Network 权限状态
 * @param behaviorChangeState     行为变更速查状态
 * @param migrationState          迁移检测状态
 * @param isLoading               是否显示加载态
 * @param error                   错误信息
 */
data class Android16AuracastState(
    val currentTab: Android16Tab = Android16Tab.AURACAST_BROADCAST,
    val auracastBroadcastState: AuracastBroadcastState = AuracastBroadcastState(),
    val auracastReceiverState: AuracastReceiverState = AuracastReceiverState(),
    val outlineTextState: OutlineTextState = OutlineTextState(),
    val localNetworkState: LocalNetworkState = LocalNetworkState(),
    val behaviorChangeState: BehaviorChangeState = BehaviorChangeState(),
    val migrationState: MigrationState = MigrationState(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ============================================================
// MVI Intent / 用户意图
// ============================================================

/**
 * Android 16 Auracast 工具用户意图
 * ViewModel 处理 Intent，返回新状态或触发 Effect
 */
sealed class Android16AuracastIntent {

    // ---------- Tab 切换 ----------
    data class SwitchTab(val tab: Android16Tab) : Android16AuracastIntent()

    // ---------- Auracast 广播发送 ----------
    data class UpdateBroadcastName(val name: String) : Android16AuracastIntent()
    data object StartBroadcast : Android16AuracastIntent()
    data object StopBroadcast : Android16AuracastIntent()
    data object PauseBroadcast : Android16AuracastIntent()
    data object ResumeBroadcast : Android16AuracastIntent()

    // ---------- Auracast 广播接收 ----------
    data object StartScanning : Android16AuracastIntent()
    data object StopScanning : Android16AuracastIntent()
    data class ConnectToSource(val sourceId: String) : Android16AuracastIntent()
    data object DisconnectSource : Android16AuracastIntent()

    // ---------- Outline Text ----------
    data class UpdateOutlineText(val text: String) : Android16AuracastIntent()
    data class UpdateOutlineStyle(val style: OutlineTextStyle) : Android16AuracastIntent()
    data class ToggleAutoAccessible(val enabled: Boolean) : Android16AuracastIntent()

    // ---------- Local Network 权限 ----------
    data object RequestLocalNetworkPermission : Android16AuracastIntent()
    data object ShowPermissionIntro : Android16AuracastIntent()
    data object DismissPermissionIntro : Android16AuracastIntent()
    data object OpenAppSettings : Android16AuracastIntent()
    data class UpdatePermissionState(val state: PermissionState) : Android16AuracastIntent()

    // ---------- 行为变更速查 ----------
    data class SelectVersionTab(val version: String) : Android16AuracastIntent()
    data class UpdateSearchQuery(val query: String) : Android16AuracastIntent()
    data class FilterBySeverity(val severity: Severity?) : Android16AuracastIntent()
    data class FilterByCategory(val category: BehaviorCategory?) : Android16AuracastIntent()
    data class ExpandChange(val changeId: String) : Android16AuracastIntent()
    data object CollapseAllChanges : Android16AuracastIntent()

    // ---------- 迁移检测 ----------
    data class UpdateProjectPath(val path: String) : Android16AuracastIntent()
    data class UpdateTargetVersion(val version: String) : Android16AuracastIntent()
    data object StartMigrationScan : Android16AuracastIntent()
    data object CancelMigrationScan : Android16AuracastIntent()
    data object ExportMigrationReport : Android16AuracastIntent()
}

// ============================================================
// MVI Effect / 副作用
// ============================================================

/**
 * Android 16 Auracast 工具一次性副作用
 * 通过 Channel 传递，UI 层消费
 */
sealed class Android16AuracastEffect {

    /** 显示 Snackbar 提示 */
    data class ShowSnackbar(val message: String) : Android16AuracastEffect()

    /** 显示 Toast 提示 */
    data class ShowToast(val message: String) : Android16AuracastEffect()

    /** 导航到指定路由 */
    data class NavigateTo(val route: String) : Android16AuracastEffect()

    /** 跳转到系统设置页面 */
    data object OpenSystemSettings : Android16AuracastEffect()

    /** 导出报告到指定路径 */
    data class ExportReport(val reportPath: String) : Android16AuracastEffect()

    /** 显示错误对话框 */
    data class ShowError(val title: String, val message: String) : Android16AuracastEffect()

    /** Auracast 广播启动成功 */
    data class BroadcastStarted(val session: BroadcastSession) : Android16AuracastEffect()

    /** Auracast 音频源丢失通知 */
    data class SourceLost(val sourceId: String) : Android16AuracastEffect()
}

// ============================================================
// Companion Constants / 伴生常量
// ============================================================

/**
 * Android16AuracastContract 伴生对象
 * 定义主题颜色、间距等视觉规范常量
 */
object Android16AuracastContract {

    /** 视觉规范 — 颜色定义（来自设计文档） */
    object Colors {
        val P0Color = Color(0xFFB3261E)   // P0 严重错误
        val P1Color = Color(0xFFE8A317)   // P1 警告
        val P2Color = Color(0xFF625B71)   // P2 信息
        val Primary = Color(0xFF6750A4)   // Material 3 Primary
        val OutlineTextBg = Color(0xFFE8DEF8) // Outline Text 背景色
    }

    /** 视觉规范 — 间距定义 */
    object Spacing {
        const val PagePadding = 24
        const val CardPadding = 16
        const val ElementGapSmall = 8
        const val ElementGapMedium = 16
        const val CornerRadius = 12
    }

    /** Auracast 相关常量 */
    object Auracast {
        /** 最小信号强度阈值（dBm） */
        const val MIN_SIGNAL_STRENGTH = -80

        /** 扫描超时时间（毫秒） */
        const val SCAN_TIMEOUT_MS = 30_000L

        /** 最大广播源数量 */
        const val MAX_SOURCES = 10
    }

    /** Outline Text 版本守卫检查 */
    object OutlineText {
        /** 是否支持 Outline Text API（Android 16+） */
        val isSupported: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    }
}
