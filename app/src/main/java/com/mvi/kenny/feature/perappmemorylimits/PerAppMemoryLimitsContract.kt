package com.mvi.kenny.feature.perappmemorylimits

// ================================================================
// PerAppMemoryLimitsContract — Android 17 Per-App Memory Limits MVI Contract
// ================================================================
// MVI architecture contract for Android 17 Per-App Memory Limits Detection & Optimization Toolkit.
//
// PRD-235: Android 17 Per-App 内存限制检测与优化工具包
// Design Reference: memory/agency/designs/PRD-235-Android-17-Per-App-内存限制检测与优化工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// This toolkit provides 5 developer tools:
//   1. Memory Calculator (输入设备RAM → App内存上限)
//   2. Leak Scanner (内存泄漏模式扫描)
//   3. Memory Monitor SDK (运行时内存监控)
//   4. Pressure Test (CI内存压力测试)
//   5. Gradle Plugin (CI合规检测)
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

// =============================================================
// Color Palette — 深色 Terminal CLI 风格配色（ANSI CYAN主题）
// =============================================================
object PerAppMemoryColors {
    val Primary = Color(0xFF00E5CC)         // CYAN — 标题/主色
    val Secondary = Color(0xFFFFB347)        // ORANGE — 警告
    val Background = Color(0xFF0D1117)       // 深黑背景
    val Surface = Color(0xFF161B22)         // 卡片背景
    val SurfaceVariant = Color(0xFF21262D)   // 输入框背景
    val OnSurface = Color(0xFFE6EDF3)        // 主文字
    val OnSurfaceVariant = Color(0xFF8B949E) // 次级文字
    val Pass = Color(0xFF3FB950)            // GREEN — 成功/PASS
    val Warn = Color(0xFFFFD700)            // YELLOW — 警告/WARN
    val Fail = Color(0xFFF85149)             // RED — 失败/FAIL
    val Info = Color(0xFF58A6FF)            // BLUE — 信息
    val Accent = Color(0xFF1A73E8)           // Android Blue — 强调色
}

// =============================================================
// ToolTab — 工具Tab枚举
// =============================================================
enum class ToolTab(val displayName: String, val emoji: String, val description: String) {
    CALCULATOR("Calculator", "🧮", "Calculate per-app memory limit by device RAM"),
    LEAK_SCANNER("Leak Scanner", "🔍", "Scan source code for memory leak patterns"),
    MONITOR_SDK("Monitor SDK", "📊", "Runtime memory monitoring SDK integration"),
    PRESSURE_TEST("Pressure Test", "💾", "CI memory pressure test on Android Emulator"),
    GRADLE_PLUGIN("Gradle Plugin", "🔧", "CI compliance check Gradle plugin")
}

// =============================================================
// DeviceRamTier — 设备RAM层级
// =============================================================
enum class DeviceRamTier(val ramGB: Int, val displayName: String, val appMemoryLimitMB: Long) {
    LOW_END(4, "4GB (Low-end)", 256L),
    MID_LOW(6, "6GB (Mid-low)", 384L),
    MID(8, "8GB (Mid-range)", 512L),
    MID_HIGH(12, "12GB (Mid-high)", 768L),
    HIGH_END(16, "16GB (High-end)", 1024L),
    CUSTOM(0, "Custom", 0L)
}

// =============================================================
// LeakSeverity — 内存泄漏风险等级
// =============================================================
enum class LeakSeverity(val displayName: String, val emoji: String, val color: Color, val priority: Int) {
    CRITICAL("Critical", "🔴", Color(0xFFF85149), 1),
    HIGH("High", "🟠", Color(0xFFFF7B72), 2),
    MEDIUM("Medium", "🟡", Color(0xFFFFD700), 3),
    LOW("Low", "🟢", Color(0xFF3FB950), 4),
    INFO("Info", "ℹ️", Color(0xFF58A6FF), 5)
}

// =============================================================
// LeakPattern — 内存泄漏模式类型
// =============================================================
enum class LeakPatternType(val displayName: String, val pattern: String, val severity: LeakSeverity) {
    BITMAP_NOT_RECYCLED("Bitmap not recycled", "Bitmap.createBitmap", LeakSeverity.HIGH),
    CONTEXT_IN_HANDLER("Context leak in Handler", "Handler.post", LeakSeverity.CRITICAL),
    STATIC_CONTEXT("Static Context reference", "static.*Context", LeakSeverity.CRITICAL),
    SINGLETON_ACTIVITY("Singleton holds Activity", "Singleton.*Activity", LeakSeverity.HIGH),
    BITMAP_CACHE_NO_REcycle("Bitmap cache without recycle", "LruCache.*Bitmap", LeakSeverity.MEDIUM),
    VIEW_COLLECTION_LEAK("View collected by collector", "ViewCollect", LeakSeverity.LOW),
    NON_STATIC_HANDLER("Non-static Handler class", "class.*Handler", LeakSeverity.MEDIUM),
    WEAK_REF_MISSING("Missing WeakReference", "SoftReference", LeakSeverity.INFO)
}

// =============================================================
// LeakFinding — 扫描发现的泄漏问题
// =============================================================
data class LeakFinding(
    val file: String,
    val line: Int,
    val pattern: LeakPatternType,
    val code: String,
    val suggestion: String
)

// =============================================================
// ScanPhase — 扫描阶段
// =============================================================
enum class ScanPhase {
    IDLE,
    SCANNING,
    ANALYZING,
    GENERATING_REPORT,
    COMPLETED,
    ERROR
}

// =============================================================
// MonitoringState — 运行时监控状态
// =============================================================
enum class MonitoringState(val displayName: String) {
    IDLE("Idle"),
    MONITORING("Monitoring"),
    PAUSED("Paused"),
    WARNING("Warning (80%+)"),
    CRITICAL("Critical (90%+)"),
    OOM("OOM"),
    ERROR("Error")
}

// =============================================================
// MemoryInfo — 内存信息数据类
// =============================================================
data class MemoryInfo(
    val totalMemoryMB: Long,
    val usedMemoryMB: Long,
    val availableMemoryMB: Long,
    val usedPercent: Float,
    val isLowMemory: Boolean,
    val thresholdMB: Long
)

// =============================================================
// PressureTestResult — 内存压力测试结果
// =============================================================
data class PressureTestResult(
    val deviceRAM: String,
    val appMemoryLimit: String,
    val testStatus: TestStatus,
    val peakMemoryMB: Long,
    val oomOccurred: Boolean,
    val recommendation: String
)

enum class TestStatus { NOT_RUN, PASS, DEGRADED, OOM, ERROR }

// =============================================================
// CalculatorState — 计算器状态（MVI State）
// =============================================================
data class CalculatorState(
    val selectedTier: DeviceRamTier = DeviceRamTier.MID,
    val customRamGB: Float = 8f,
    val calculatedLimitMB: Long = 512L,
    val calculatedHeapMB: Long = 256L,
    val calculatedNativeMB: Long = 256L,
    val isCalculating: Boolean = false,
    val showDetails: Boolean = false
)

// =============================================================
// LeakScannerState — 泄漏扫描器状态（MVI State）
// =============================================================
data class LeakScannerState(
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanPhase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val scannedFiles: Int = 0,
    val totalFiles: Int = 0,
    val findings: List<LeakFinding> = emptyList(),
    val summary: Map<LeakSeverity, Int> = emptyMap(),
    val lastScanTime: Long = 0L,
    val reportPath: String = "",
    val errorMessage: String = ""
)

// =============================================================
// MonitorSDKState — 运行时监控SDK状态（MVI State）
// =============================================================
data class MonitorSDKState(
    val monitoringState: MonitoringState = MonitoringState.IDLE,
    val memoryInfo: MemoryInfo = MemoryInfo(0L, 0L, 0L, 0f, false, 0L),
    val warningThreshold: Float = 0.8f,
    val criticalThreshold: Float = 0.9f,
    val updateIntervalMs: Long = 1000L,
    val monitoringStartTime: Long = 0L,
    val memoryHistory: List<Float> = emptyList(),
    val isSDKIntegrated: Boolean = false,
    val integrationSnippet: String = ""
)

// =============================================================
// PressureTestState — 内存压力测试状态（MVI State）
// =============================================================
data class PressureTestState(
    val selectedTier: DeviceRamTier = DeviceRamTier.MID,
    val isRunning: Boolean = false,
    val progress: Int = 0,
    val currentPhase: String = "Idle",
    val results: List<PressureTestResult> = emptyList(),
    val lastTestTime: Long = 0L,
    val reportPath: String = ""
)

// =============================================================
// GradlePluginState — Gradle插件状态（MVI State）
// =============================================================
data class GradlePluginState(
    val isChecking: Boolean = false,
    val isSDKIntegrated: Boolean = false,
    val isThresholdConfigured: Boolean = false,
    val isOOMHandlerRegistered: Boolean = false,
    val checkResult: CIComplianceResult = CIComplianceResult.NOT_CHECKED,
    val checkMessages: List<String> = emptyList(),
    val configurationSnippet: String = "",
    val errorMessage: String = ""
)

enum class CIComplianceResult { NOT_CHECKED, PASS, WARN, FAIL }

// =============================================================
// Main State — 聚合所有工具状态的根状态
// =============================================================
data class PerAppMemoryLimitsState(
    val selectedTab: ToolTab = ToolTab.CALCULATOR,
    val calculatorState: CalculatorState = CalculatorState(),
    val leakScannerState: LeakScannerState = LeakScannerState(),
    val monitorSDKState: MonitorSDKState = MonitorSDKState(),
    val pressureTestState: PressureTestState = PressureTestState(),
    val gradlePluginState: GradlePluginState = GradlePluginState()
)

// =============================================================
// Intent — 用户操作
// =============================================================
sealed class PerAppMemoryLimitsIntent {
    // Calculator intents
    data class SelectTier(val tier: DeviceRamTier) : PerAppMemoryLimitsIntent()
    data class UpdateCustomRam(val ramGB: Float) : PerAppMemoryLimitsIntent()
    data object Calculate : PerAppMemoryLimitsIntent()
    data object ToggleCalculatorDetails : PerAppMemoryLimitsIntent()

    // Leak Scanner intents
    data class UpdateProjectPath(val path: String) : PerAppMemoryLimitsIntent()
    data object StartScan : PerAppMemoryLimitsIntent()
    data object CancelScan : PerAppMemoryLimitsIntent()
    data object ClearFindings : PerAppMemoryLimitsIntent()

    // Monitor SDK intents
    data object StartMonitoring : PerAppMemoryLimitsIntent()
    data object StopMonitoring : PerAppMemoryLimitsIntent()
    data class UpdateWarningThreshold(val threshold: Float) : PerAppMemoryLimitsIntent()
    data class UpdateCriticalThreshold(val threshold: Float) : PerAppMemoryLimitsIntent()
    data object GenerateIntegrationSnippet : PerAppMemoryLimitsIntent()

    // Pressure Test intents
    data class SelectTestTier(val tier: DeviceRamTier) : PerAppMemoryLimitsIntent()
    data object RunPressureTest : PerAppMemoryLimitsIntent()
    data object StopPressureTest : PerAppMemoryLimitsIntent()
    data object ClearTestResults : PerAppMemoryLimitsIntent()

    // Gradle Plugin intents
    data object CheckCompliance : PerAppMemoryLimitsIntent()
    data object GenerateConfigSnippet : PerAppMemoryLimitsIntent()

    // Tab navigation
    data class SelectTab(val tab: ToolTab) : PerAppMemoryLimitsIntent()
}

// =============================================================
// Effect — 一次性副作用（通过 Channel）
// =============================================================
sealed class PerAppMemoryLimitsEffect {
    data class ShowToast(val message: String) : PerAppMemoryLimitsEffect()
    data class ShowError(val message: String) : PerAppMemoryLimitsEffect()
    data class OpenFile(val path: String) : PerAppMemoryLimitsEffect()
    data object ScanCompleted : PerAppMemoryLimitsEffect()
    data class MemoryWarning(val usedPercent: Float) : PerAppMemoryLimitsEffect()
    data object MemoryCritical : PerAppMemoryLimitsEffect()
    data object OOMOccurred : PerAppMemoryLimitsEffect()
    data class ReportGenerated(val path: String) : PerAppMemoryLimitsEffect()
}
