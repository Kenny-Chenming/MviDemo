package com.mvi.kenny.feature.android17memory

// ================================================================
// Android17MemoryContract — Android 17 内存限制适配工具包 MVI 契约
// ================================================================
// MVI Architecture Contract for Android 17 Memory Limits Toolkit.
//
// PRD-213: Android 17 设备 RAM 内存限制适配工具包
// Design Reference: memory/agency/designs/PRD-213-Android-17-内存限制适配工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import android.graphics.BitmapFactory

// =============================================================
// Color palette — 深色 Terminal 风格配色
// =============================================================
object MemoryColors {
    val Primary = android.graphics.Color.parseColor("#00E5CC")       // 青绿色，工具感
    val Secondary = android.graphics.Color.parseColor("#FFB347")      // 橙色，警告
    val Background = android.graphics.Color.parseColor("#0D1117")   // 深黑
    val Surface = android.graphics.Color.parseColor("#161B22")       // 卡片背景
    val SurfaceVariant = android.graphics.Color.parseColor("#21262D") // 输入框
    val OnSurface = android.graphics.Color.parseColor("#E6EDF3")     // 主文字
    val OnSurfaceVariant = android.graphics.Color.parseColor("#8B949E") // 次级文字
    val Error = android.graphics.Color.parseColor("#FF6B6B")         // CRITICAL
    val Warning = android.graphics.Color.parseColor("#FFB347")       // 高/中风险
    val Success = android.graphics.Color.parseColor("#00E5CC")       // 合规/低风险
    val MemoryHigh = android.graphics.Color.parseColor("#FF6B6B")    // 高内存占用
    val MemoryMedium = android.graphics.Color.parseColor("#FFB347")   // 中等内存
    val MemoryLow = android.graphics.Color.parseColor("#00E5CC")      // 低内存
}

// =============================================================
// MemoryTab — 功能 Tab 枚举（5 个主要 Tab）
// =============================================================
/**
 * 5 个主要功能 Tab，按开发者工作流排列。
 *
 * Tab 1: 📊 内存检测 — App 当前内存使用检测工具
 * Tab 2: 📋 设备换算 — 设备 RAM → 内存限制换算表
 * Tab 3: ⚠️ 风险评估 — 内存限制影响评估扫描器
 * Tab 4: 🔧 Profiling — ProfilingManager 内存限制查询工具
 * Tab 5: 🧪 CI 测试 — 低 RAM 环境模拟 + CI 合规检测
 *
 * @param title Display title / 显示标题
 * @param emoji Emoji representation
 */
enum class MemoryTab(val title: String, val emoji: String) {
    MEMORY_DETECTION("内存检测", "📊"),
    RAM_TABLE("设备换算", "📋"),
    RISK_SCAN("风险评估", "⚠️"),
    PROFILING("Profiling", "🔧"),
    CI_TEST("CI 测试", "🧪")
}

// =============================================================
// MemoryUsage — 当前内存使用数据
// =============================================================
/**
 * 当前 App 内存使用情况数据结构。
 *
 * @property heapUsedMb Heap 已使用内存（MB）
 * @property heapTotalMb Heap 总内存（MB）
 * @property dalvikUsedMb Dalvik/ART 已使用内存（MB）
 * @property nativeUsedMb Native 堆已使用内存（MB）
 * @property totalUsedMb 总已使用内存（MB）
 * @property timestamp 采样时间戳
 */
data class MemoryUsage(
    val heapUsedMb: Long = 0L,
    val heapTotalMb: Long = 0L,
    val dalvikUsedMb: Long = 0L,
    val nativeUsedMb: Long = 0L,
    val totalUsedMb: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Heap 使用率 / Heap usage percentage (0.0–1.0) */
    val heapUsageRatio: Float
        get() = if (heapTotalMb > 0) heapUsedMb.toFloat() / heapTotalMb else 0f

    /** Memory usage level / 内存使用级别 */
    val usageLevel: MemoryUsageLevel
        get() = when {
            heapUsageRatio >= 0.85f -> MemoryUsageLevel.HIGH
            heapUsageRatio >= 0.6f -> MemoryUsageLevel.MEDIUM
            else -> MemoryUsageLevel.LOW
        }
}

/**
 * 内存使用级别枚举。
 */
enum class MemoryUsageLevel {
    LOW,    // < 60% — 绿色
    MEDIUM, // 60–85% — 橙色
    HIGH    // > 85% — 红色
}

// =============================================================
// RamLimitEntry — RAM 换算表条目
// =============================================================
/**
 * 设备 RAM → App 内存上限换算表条目。
 *
 * Android 17 per-device memory limits: App memory limit = f(deviceTotalRAM)
 *
 * @property deviceRamGb 设备总 RAM（GB）
 * @property appMemoryLimitMb App 内存上限（MB）
 * @property isLargeHeap 是否支持 large heap
 * @property deviceCategory 设备分类：Low-end / Mid-range / High-end / Flagship
 */
data class RamLimitEntry(
    val deviceRamGb: Double,
    val appMemoryLimitMb: Long,
    val isLargeHeap: Boolean,
    val deviceCategory: String
)

// =============================================================
// RiskItem — 风险扫描结果项
// =============================================================
/**
 * 内存风险扫描结果项。
 *
 * @property codePath 出问题的代码路径/位置
 * @property riskType 风险类型
 * @property severity 严重程度
 * @property description 中文描述
 * @property suggestion 优化建议
 */
data class RiskItem(
    val codePath: String,
    val riskType: RiskType,
    val severity: Severity,
    val description: String,
    val suggestion: String
)

/**
 * 内存风险类型枚举。
 */
enum class RiskType(val displayName: String) {
    LARGE_BITMAP("大图加载"),
    VIDEO_FRAME("视频帧处理"),
    DEEP_COPY("深度复制"),
    CACHE("缓存未清理"),
    LARGE_ALLOC("大内存分配"),
    OTHER("其他")
}

/**
 * 风险严重程度枚举。
 */
enum class Severity(val displayName: String, val emoji: String) {
    CRITICAL("严重", "🔴"),
    HIGH("高", "🟠"),
    MEDIUM("中", "🟡"),
    LOW("低", "🟢")
}

// =============================================================
// ProfilingResult — ProfilingManager 查询结果
// =============================================================
/**
 * ProfilingManager 内存限制查询结果。
 *
 * Android 17 新增 ProfilingManager API，App 可查询自身内存限制。
 *
 * @property memoryLimitMb 当前设备对本 App 的内存上限（MB）
 * @property isLargeHeapEnabled 是否启用了 large heap
 * @property profilingManagerAvailable ProfilingManager 是否可用（模拟器可能不支持）
 */
data class ProfilingResult(
    val memoryLimitMb: Long = 0L,
    val isLargeHeapEnabled: Boolean = false,
    val profilingManagerAvailable: Boolean = false
)

// =============================================================
// EmulatorConfig — 低 RAM 模拟器配置
// =============================================================
/**
 * 低 RAM 环境模拟器配置。
 *
 * 用于 CI 中使用 `-memory` 参数启动模拟器。
 *
 * @property targetRamMb 目标 RAM 大小（MB）
 * @property avdName AVD 名称
 * @property iniConfig .ini 配置文件内容
 * @property startCommand 启动命令
 */
data class EmulatorConfig(
    val targetRamMb: Int,
    val avdName: String = "low_ram_test_device",
    val iniConfig: String = "",
    val startCommand: String = ""
)

// =============================================================
// CiComplianceStatus — CI 合规状态
// =============================================================
/**
 * CI 合规检测状态。
 */
enum class CiComplianceStatus(val displayName: String, val emoji: String) {
    UNKNOWN("未知", "⚪"),
    COMPLIANT("合规", "✅"),
    NON_COMPLIANT("不合规", "❌"),
    SCANNING("扫描中", "🔄")
}

// =============================================================
// MemoryChecklist — Android 17 内存限制检查清单
// =============================================================
/**
 * Android 17 内存限制完整检查清单。
 *
 * @property items 检查项列表
 * @property isExpanded 是否展开
 */
data class MemoryChecklist(
    val items: List<ChecklistItem> = emptyList(),
    val isExpanded: Boolean = false
)

/**
 * 检查清单条目。
 *
 * @property id 唯一 ID
 * @property title 检查项标题
 * @property description 检查项描述
 * @property isChecked 是否已检查
 * @property category 分类
 */
data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val category: String = ""
)

// =============================================================
// Android17MemoryState — MVI 状态
// =============================================================
/**
 * MVI 页面状态。
 *
 * @property currentTab 当前 Tab 索引
 * @property selectedPackage 选中的包名
 * @property installedApps 已安装 App 列表（包名 + 应用名）
 * @property currentMemoryUsage 当前内存使用情况
 * @property ramLimitTable RAM 换算表数据
 * @property riskScanResults 风险扫描结果列表
 * @property profilingResult ProfilingManager 查询结果
 * @property emulatorConfig 模拟器配置
 * @property ciComplianceStatus CI 合规状态
 * @property checklist 检查清单
 * @property isLoading 是否正在加载
 * @property snackbarMessage Snackbar 消息
 */
data class Android17MemoryState(
    val currentTab: Int = 0,
    val selectedPackage: String = "",
    val installedApps: List<Pair<String, String>> = emptyList(), // (packageName, appName)
    val currentMemoryUsage: MemoryUsage = MemoryUsage(),
    val ramLimitTable: List<RamLimitEntry> = emptyList(),
    val riskScanResults: List<RiskItem> = emptyList(),
    val profilingResult: ProfilingResult = ProfilingResult(),
    val emulatorConfig: EmulatorConfig? = null,
    val ciComplianceStatus: CiComplianceStatus = CiComplianceStatus.UNKNOWN,
    val checklist: MemoryChecklist = MemoryChecklist(),
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val selectedRamGb: Double = 4.0  // RAM table filter
)

// =============================================================
// Android17MemoryIntent — MVI 用户意图
// =============================================================
/**
 * MVI 用户意图（User Intents）。
 *
 * 所有用户操作都被封装为 Intent，由 ViewModel 处理。
 */
sealed class Android17MemoryIntent {
    /** 切换 Tab / Switch to a specific tab */
    data class SelectTab(val index: Int) : Android17MemoryIntent()

    /** 选择 App / Select an installed app */
    data class SelectApp(val packageName: String, val appName: String) : Android17MemoryIntent()

    /** 刷新内存使用数据 / Refresh current memory usage */
    data object RefreshMemoryUsage : Android17MemoryIntent()

    /** 加载设备换算表 / Generate RAM limit conversion table */
    data object LoadRamLimitTable : Android17MemoryIntent()

    /** 运行风险扫描 / Run risk scan */
    data object RunRiskScan : Android17MemoryIntent()

    /** 查询 ProfilingManager / Query ProfilingManager for memory limits */
    data object QueryProfilingManager : Android17MemoryIntent()

    /** 生成低 RAM 模拟器配置 / Generate low RAM emulator config */
    data class GenerateEmulatorConfig(val targetRamMb: Int) : Android17MemoryIntent()

    /** 设置 CI 合规检测 / Set up CI compliance check */
    data object SetupCiCompliance : Android17MemoryIntent()

    /** 导出检查清单为 Markdown / Export checklist as Markdown */
    data object ExportChecklist : Android17MemoryIntent()

    /** 切换检查清单展开状态 / Toggle checklist expansion */
    data object ToggleChecklist : Android17MemoryIntent()

    /** 关闭 Snackbar / Dismiss snackbar */
    data object DismissSnackbar : Android17MemoryIntent()

    /** 切换 Large Heap 过滤 / Toggle large heap filter in RAM table */
    data class FilterLargeHeap(val showLargeHeapOnly: Boolean) : Android17MemoryIntent()

    /** 加载已安装 App 列表 / Load list of installed apps */
    data object LoadInstalledApps : Android17MemoryIntent()
}

// =============================================================
// Android17MemoryEffect — MVI 副作用
// =============================================================
/**
 * MVI 副作用（One-time side effects）。
 *
 * 导航、Toast、剪贴板等一次性操作通过 Channel 发送。
 */
sealed class Android17MemoryEffect {
    /** 显示 Snackbar / Show a snackbar message */
    data class ShowSnackbar(val message: String) : Android17MemoryEffect()

    /** 复制到剪贴板 / Copy text to clipboard */
    data class CopyToClipboard(val content: String, val label: String = "Content") : Android17MemoryEffect()

    /** 分享检查清单 / Share checklist content */
    data class ShareChecklist(val content: String) : Android17MemoryEffect()
}
