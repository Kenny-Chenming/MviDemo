package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsContract — Android 17 App Memory Limits MVI 契约
// ================================================================
// MVI architecture contract for Android 17 Memory Limits detection & optimization toolkit.
//
// PRD-151: Android 17 App Memory Limits 内存限制检测与调优开发工具包
// Design Reference: memory/agency/designs/PRD-151-Android-17-App-Memory-Limits-内存限制检测与调优开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/**
 * ============================================================
 * MonitoringState — 监控状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class MonitoringState(val displayName: String) {
    /** 初始空闲状态 */
    Idle("空闲"),
    /** 正在监控中 */
    Monitoring("监控中"),
    /** 监控已暂停 */
    Paused("已暂停"),
    /** 监控出错 */
    Error("错误")
}

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 * 内存风险等级，从安全到危险共 4 档。
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color 风险颜色
 */
enum class RiskLevel(val displayName: String, val emoji: String, val color: Color) {
    /** 安全 — 内存使用率 < 50% */
    SAFE("安全", "🟢", Color(0xFF4CAF50)),
    /** 警告 — 内存使用率 50-70% */
    WARNING("警告", "🟡", Color(0xFFFF9800)),
    /** 危险 — 内存使用率 70-85% */
    DANGER("危险", "🟠", Color(0xFFF44336)),
    /** 严重 — 内存使用率 > 85% */
    CRITICAL("严重", "🔴", Color(0xFFB71C1C));

    companion object {
        /** 根据内存使用百分比推断风险等级 */
        fun fromUsagePercent(percent: Float): RiskLevel = when {
            percent < 50f -> SAFE
            percent < 70f -> WARNING
            percent < 85f -> DANGER
            else -> CRITICAL
        }
    }
}

/**
 * ============================================================
 * ComplianceStatus — 合规状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 */
enum class ComplianceStatus(val displayName: String, val emoji: String) {
    PASS("通过", "✅"),
    FAIL("失败", "❌"),
    WARNING("警告", "⚠️")
}

/**
 * ============================================================
 * ReportFormat — 报告导出格式枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param extension 文件扩展名
 */
enum class ReportFormat(val displayName: String, val extension: String) {
    JSON("JSON", "json"),
    HTML("HTML", "html"),
    Markdown("Markdown", "md")
}

/**
 * ============================================================
 * ActiveTab — Dashboard 当前激活的 Tab
 * ============================================================
 *
 * @param title Tab 显示标题
 */
enum class DashboardTab(val title: String) {
    Overview("总览"),
    Events("事件"),
    Profiler("分析器")
}

/**
 * ============================================================
 * DeviceRamPreset — 预设设备 RAM 配置
 * ============================================================
 *
 * @param displayName 设备名称
 * @param ramBytes RAM 大小（字节）
 */
enum class DeviceRamPreset(val displayName: String, val ramBytes: Long) {
    Pixel8("Pixel 8 (8GB)", 8L * 1024 * 1024 * 1024),
    SamsungS24("Samsung S24 (12GB)", 12L * 1024 * 1024 * 1024),
    Pixel9ProXL("Pixel 9 Pro XL (16GB)", 16L * 1024 * 1024 * 1024),
    Custom("自定义", 0L)
}

/**
 * ============================================================
 * MemoryLimitsState — 内存限制工具页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param monitoringState Current monitoring state
 * @param memoryUsageBytes Current memory usage in bytes
 * @param memoryLimitBytes Memory limit for this app in bytes
 * @param memoryUsagePercent Current memory usage percentage (0-100)
 * @param limiterEvents List of MemoryLimiter kill events
 * @param riskLevel Current risk level
 * @param heapDumpResults List of heap dump analysis results
 * @param ciReport CI compliance report
 * @param isSimulatorRunning Whether simulator is running
 * @param simulatedDeviceRam Simulated device RAM in bytes
 * @param simulatorResult Simulator result if available
 * @param activeTab Currently active tab on Dashboard
 * @param isMonitoringPaused Whether monitoring is paused
 * @param errorMessage Error message if any
 * @param targetSdk Target SDK version
 *
 * @see MonitoringState
 * @see RiskLevel
 * @see DashboardTab
 */
data class MemoryLimitsState(
    val monitoringState: MonitoringState = MonitoringState.Idle,
    val memoryUsageBytes: Long = 0L,
    val memoryLimitBytes: Long = 0L,
    val memoryUsagePercent: Float = 0f,
    val limiterEvents: List<LimiterEvent> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.SAFE,
    val heapDumpResults: List<HeapDumpResult> = emptyList(),
    val ciReport: CiReport? = null,
    val isSimulatorRunning: Boolean = false,
    val simulatedDeviceRam: Long = 8L * 1024 * 1024 * 1024,
    val simulatorResult: SimulatorResult? = null,
    val activeTab: DashboardTab = DashboardTab.Overview,
    val isMonitoringPaused: Boolean = false,
    val errorMessage: String? = null,
    val targetSdk: Int = 35,
    // Memory usage history for curve chart (data points)
    val memoryHistory: List<MemoryDataPoint> = emptyList(),
    // Module memory breakdown
    val moduleMemoryBreakdown: List<ModuleMemory> = emptyList(),
    // Best practice items
    val bestPractices: List<BestPracticeItem> = defaultBestPractices,
    // Simulator selected device preset
    val selectedDevicePreset: DeviceRamPreset = DeviceRamPreset.Pixel8
) {
    companion object {
        /** Initial/empty state */
        val Initial = MemoryLimitsState()
    }
}

/**
 * ============================================================
 * MemoryLimitsIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see MemoryLimitsViewModel.sendIntent handles all Intents
 */
sealed interface MemoryLimitsIntent {

    /** 用户点击"开始监控"按钮 */
    data object StartMonitoring : MemoryLimitsIntent

    /** 用户点击"停止监控"按钮 */
    data object StopMonitoring : MemoryLimitsIntent

    /** 用户暂停/恢复监控 */
    data object ToggleMonitoring : MemoryLimitsIntent

    /** 用户手动触发 Heap Dump
     * @param reason 触发原因
     */
    data class TriggerHeapDump(val reason: String) : MemoryLimitsIntent

    /** 用户运行合规检查
     * @param targetRam 目标设备 RAM（字节）
     */
    data class RunComplianceCheck(val targetRam: Long) : MemoryLimitsIntent

    /** 用户运行模拟器
     * @param deviceRam 设备 RAM（字节）
     */
    data class RunSimulator(val deviceRam: Long) : MemoryLimitsIntent

    /** 用户切换 Dashboard Tab
     * @param tab Target tab
     */
    data class SwitchTab(val tab: DashboardTab) : MemoryLimitsIntent

    /** 用户加载历史事件
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     */
    data class LoadHistoricalEvents(val startTime: Long, val endTime: Long) : MemoryLimitsIntent

    /** 用户清除错误消息 */
    data object ClearError : MemoryLimitsIntent

    /** 用户选择模拟器设备预设
     * @param preset 设备预设
     */
    data class SelectDevicePreset(val preset: DeviceRamPreset) : MemoryLimitsIntent

    /** 用户导出 CI 报告
     * @param format 导出格式
     */
    data class ExportCiReport(val format: ReportFormat) : MemoryLimitsIntent

    /** 用户刷新内存数据 */
    data object RefreshMemoryData : MemoryLimitsIntent
}

/**
 * ============================================================
 * MemoryLimitsEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see MemoryLimitsViewModel _effect.send() sends Effects
 */
sealed interface MemoryLimitsEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : MemoryLimitsEffect

    /** 导航到 Heap Dump 详情
     * @param dumpId Dump ID
     */
    data class NavigateToHeapDump(val dumpId: String) : MemoryLimitsEffect

    /** 导出 CI 报告成功
     * @param filePath 导出文件路径
     */
    data class ExportCiReportSuccess(val filePath: String) : MemoryLimitsEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : MemoryLimitsEffect

    /** Heap Dump 采集完成
     * @param dumpId Dump ID
     */
    data class HeapDumpCollected(val dumpId: String) : MemoryLimitsEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * LimiterEvent — MemoryLimiter 杀死事件
 * ============================================================
 *
 * @param id Unique event ID
 * @param timestamp Event timestamp (milliseconds since epoch)
 * @param memoryUsageBytes Memory usage when killed (bytes)
 * @param memoryLimitBytes Memory limit at the time (bytes)
 * @param processName Process name
 * @param reasonDescription Reason description from ApplicationExitInfo
 * @param stackTrace Stack trace summary
 * @param packageName App package name
 */
data class LimiterEvent(
    val id: String,
    val timestamp: Long,
    val memoryUsageBytes: Long,
    val memoryLimitBytes: Long,
    val processName: String,
    val reasonDescription: String,
    val stackTrace: String,
    val packageName: String
)

/**
 * ============================================================
 * HeapDumpResult — Heap Dump 分析结果
 * ============================================================
 *
 * @param id Unique dump ID
 * @param timestamp Dump timestamp
 * @param filePath Dump file path
 * @param fileSizeBytes Dump file size in bytes
 * @param leakingObjects List of leaking object tree roots
 * @param suspiciousReferences List of suspicious reference chains
 * @param suggestions List of fix suggestions
 * @param totalLeakingBytes Total bytes in leaking objects
 */
data class HeapDumpResult(
    val id: String,
    val timestamp: Long,
    val filePath: String,
    val fileSizeBytes: Long,
    val leakingObjects: List<LeakingObject>,
    val suspiciousReferences: List<SuspiciousReference>,
    val suggestions: List<String>,
    val totalLeakingBytes: Long
)

/**
 * ============================================================
 * LeakingObject — 泄漏对象节点
 * ============================================================
 *
 * @param className Class name of leaking object
 * @param shallowSizeBytes Shallow size in bytes
 * @param retainedSizeBytes Retained size in bytes
 * @param instanceCount Number of instances
 * @param children Child objects in retention tree
 */
data class LeakingObject(
    val className: String,
    val shallowSizeBytes: Long,
    val retainedSizeBytes: Long,
    val instanceCount: Int,
    val children: List<LeakingObject> = emptyList()
)

/**
 * ============================================================
 * SuspiciousReference — 可疑引用链
 * ============================================================
 *
 * @param fromObject Source object of the reference
 * @param toObject Target object being held
 * @param referenceType Reference type (e.g. "static field", "inner class")
 * @param pathDescription Human-readable path description
 */
data class SuspiciousReference(
    val fromObject: String,
    val toObject: String,
    val referenceType: String,
    val pathDescription: String
)

/**
 * ============================================================
 * CiReport — CI 合规报告
 * ============================================================
 *
 * @param id Report ID
 * @param timestamp Report generation timestamp
 * @param overallStatus Overall compliance status
 * @param overallScore Overall compliance score (0-100)
 * @param moduleReports Per-module compliance reports
 * @param passedChecks Number of passed checks
 * @param failedChecks Number of failed checks
 * @param warningChecks Number of warning checks
 */
data class CiReport(
    val id: String,
    val timestamp: Long,
    val overallStatus: ComplianceStatus,
    val overallScore: Int,
    val moduleReports: List<ModuleComplianceReport>,
    val passedChecks: Int,
    val failedChecks: Int,
    val warningChecks: Int
)

/**
 * ============================================================
 * ModuleComplianceReport — 模块合规报告
 * ============================================================
 *
 * @param moduleName Module name
 * @param status Compliance status for this module
 * @param riskLevel Risk level for this module
 * @param memoryUsageBytes Memory usage by this module
 * @param memoryLimitBytes Applicable memory limit
 * @param suggestions List of suggestions for this module
 */
data class ModuleComplianceReport(
    val moduleName: String,
    val status: ComplianceStatus,
    val riskLevel: RiskLevel,
    val memoryUsageBytes: Long,
    val memoryLimitBytes: Long,
    val suggestions: List<String>
)

/**
 * ============================================================
 * SimulatorResult — 模拟器结果
 * ============================================================
 *
 * @param deviceRam Simulated device RAM
 * @param appMemoryLimit Calculated app memory limit
 * @param estimatedUsageBytes Estimated app memory usage
 * @param wouldTriggerOom Whether OOM would be triggered
 * @param riskLevel Predicted risk level
 * @param warnings List of warnings
 * @param suggestions List of optimization suggestions
 */
data class SimulatorResult(
    val deviceRam: Long,
    val appMemoryLimit: Long,
    val estimatedUsageBytes: Long,
    val wouldTriggerOom: Boolean,
    val riskLevel: RiskLevel,
    val warnings: List<String>,
    val suggestions: List<String>
)

/**
 * ============================================================
 * MemoryDataPoint — 内存曲线数据点
 * ============================================================
 *
 * @param timestamp Timestamp of this data point
 * @param usageBytes Memory usage in bytes at this point
 */
data class MemoryDataPoint(
    val timestamp: Long,
    val usageBytes: Long
)

/**
 * ============================================================
 * ModuleMemory — 模块内存占用
 * ============================================================
 *
 * @param moduleName Module name
 * @param usageBytes Memory usage in bytes
 * @param percentage Percentage of total app memory
 * @param riskLevel Risk level for this module
 */
data class ModuleMemory(
    val moduleName: String,
    val usageBytes: Long,
    val percentage: Float,
    val riskLevel: RiskLevel
)

/**
 * ============================================================
 * BestPracticeItem — 最佳实践条目
 * ============================================================
 *
 * @param id Unique ID
 * @param title Title of the practice
 * @param description Description
 * @param category Category (e.g. "Bitmap", "Leak", "Cache")
 * @param impact Impact level (HIGH/MEDIUM/LOW)
 * @param isImplemented Whether this practice is implemented
 */
data class BestPracticeItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val impact: String,
    val isImplemented: Boolean = false
)

// ================================================================
// 默认最佳实践数据 / Default Best Practice Data
// ================================================================

/**
 * ============================================================
 * defaultBestPractices — Android 17 内存最佳实践清单
 * ============================================================
 * Reference: developer.android.com/about/versions/17/behavior-changes
 */
val defaultBestPractices = listOf(
    BestPracticeItem(
        id = "bp_001",
        title = "Bitmap 内存优化",
        description = "使用 inSampleSize / inBitmap 减少图片内存占用，避免加载超大位图",
        category = "Bitmap",
        impact = "HIGH"
    ),
    BestPracticeItem(
        id = "bp_002",
        title = " LeakCanary 自动注入",
        description = "在 debug 构建中集成 LeakCanary，自动检测 Activity/Fragment 泄漏",
        category = "Leak",
        impact = "HIGH"
    ),
    BestPracticeItem(
        id = "bp_003",
        title = "对象池复用",
        description = "避免在 onDraw 等高频调用中创建新对象，使用对象池或预分配对象",
        category = "Performance",
        impact = "MEDIUM"
    ),
    BestPracticeItem(
        id = "bp_004",
        title = "WeakReference / SoftReference",
        description = "对可重建的缓存使用 WeakReference，对重要缓存使用 SoftReference",
        category = "Memory",
        impact = "MEDIUM"
    ),
    BestPracticeItem(
        id = "bp_005",
        title = "ProfilingManager ANOMALY 触发器",
        description = "配置 TRIGGER_TYPE_ANOMALY，在内存超限时自动采集 heap dump",
        category = "Debug",
        impact = "MEDIUM"
    ),
    BestPracticeItem(
        id = "bp_006",
        title = "LargeHeap 选项评估",
        description = "评估 android:largeHeap=true 的实际收益，权衡内存预留与 OOM 风险",
        category = "Config",
        impact = "MEDIUM"
    ),
    BestPracticeItem(
        id = "bp_007",
        title = "后台进程内存限制感知",
        description = "使用 ProcessLifecycleOwner 或 WorkManager 管理后台任务，避免在后台被 MemoryLimiter 杀死",
        category = "Lifecycle",
        impact = "HIGH"
    ),
    BestPracticeItem(
        id = "bp_008",
        title = "内存泄漏定期巡检",
        description = "将内存泄漏检测纳入 CI，在每次 PR 中检查新增泄漏",
        category = "CI",
        impact = "HIGH"
    )
)

// ================================================================
// 辅助函数 / Helper Functions
// ================================================================

/**
 * 格式化字节数为可读字符串
 *
 * @param bytes 字节数
 * @return 格式化后的字符串（如 "128.5 MB"）
 */
fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
    bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
}

/**
 * 计算内存使用百分比
 *
 * @param usageBytes 内存使用量
 * @param limitBytes 内存上限
 * @return 使用百分比 (0-100)
 */
fun calculateUsagePercent(usageBytes: Long, limitBytes: Long): Float {
    if (limitBytes <= 0) return 0f
    return (usageBytes.toFloat() / limitBytes.toFloat() * 100f).coerceIn(0f, 100f)
}
