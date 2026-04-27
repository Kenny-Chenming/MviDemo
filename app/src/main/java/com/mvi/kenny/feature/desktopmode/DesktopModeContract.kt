package com.mvi.kenny.feature.desktopmode

// ================================================================
// DesktopModeContract — Android 17 Desktop Mode MVI Contract
// ================================================================
// MVI architecture contract for Android 17 Desktop Mode development toolkit.
//
// PRD-170: Android 17 Desktop Mode 开发工具包
// Design Reference: memory/agency/designs/PRD-170-Android-17-Desktop-Mode-开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * ============================================================
 * DesktopTab — Desktop Mode 工具 Tab 枚举
 * ============================================================
 *
 * @param title Tab 显示标题
 */
enum class DesktopTab(val title: String) {
    OVERVIEW("总览"),
    QUARTZ_API("Quartz API"),
    AI_REFLECT("AI Reflect"),
    CI_SCAN("CI 检测"),
    ZRAM_TEST("zRAM 测试"),
    DECISION_GUIDE("适配决策"),
    FLOATING_WINDOWS("浮动窗口"),
    QUALITY_SCORE("质量评分")
}

/**
 * ============================================================
 * ReflectStatus — AI Reflect Layer 适配状态
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 */
enum class ReflectStatus(val displayName: String, val emoji: String) {
    UNKNOWN("未知", "❓"),
    REFLECTING("反射中", "🔄"),
    ADAPTED("已适配", "✅"),
    FAILED("适配失败", "❌")
}

/**
 * ============================================================
 * RadarScores — 四维雷达评分
 * ============================================================
 *
 * @param multiWindow 多窗口支持得分 (0-100)
 * @param stateRecovery 状态恢复得分 (0-100)
 * @param inputAdaptation 输入适配得分 (0-100)
 * @param visualLayout 视觉布局得分 (0-100)
 */
data class RadarScores(
    val multiWindow: Int = 0,
    val stateRecovery: Int = 0,
    val inputAdaptation: Int = 0,
    val visualLayout: Int = 0
) {
    val total: Int get() = (multiWindow + stateRecovery + inputAdaptation + visualLayout) / 4
}

/**
 * ============================================================
 * CIResult — CI 检测结果
 * ============================================================
 *
 * @param id Unique result ID
 * @param issueType Issue type (LAYOUT_TRUNCATION, OVERFLOW, CRASH, etc.)
 * @param severity Severity level (ERROR, WARNING, INFO)
 * @param description Issue description
 * @param filePath Source file path
 * @param lineNumber Line number in source
 * @param suggestion Fix suggestion
 */
data class CIResult(
    val id: String,
    val issueType: String,
    val severity: String,
    val description: String,
    val filePath: String,
    val lineNumber: Int,
    val suggestion: String
)

/**
 * ============================================================
 * ZRAMTestResult — zRAM 休眠/唤醒测试结果
 * ============================================================
 *
 * @param id Unique result ID
 * @param timestamp Test timestamp
 * @param hibernationDurationMs Hibernation duration in milliseconds
 * @param wakeDurationMs Wake duration in milliseconds
 * @param stateConsistent Whether app state is consistent after wake
 * @param memoryRestoredBytes Memory restored after wake
 * @param issues List of issues found
 */
data class ZRAMTestResult(
    val id: String,
    val timestamp: Long,
    val hibernationDurationMs: Long,
    val wakeDurationMs: Long,
    val stateConsistent: Boolean,
    val memoryRestoredBytes: Long,
    val issues: List<String>
)

/**
 * ============================================================
 * WindowLayoutIssue — 浮动窗口布局问题
 * ============================================================
 *
 * @param id Unique issue ID
 * @param windowSize Window size (widthDp x heightDp)
 * @param issueType Issue type (TRUNCATION, OVERFLOW, CRASH, etc.)
 * @param description Issue description
 * @param beforeImagePath Before fix screenshot path
 * @param afterImagePath After fix screenshot path
 */
data class WindowLayoutIssue(
    val id: String,
    val windowSize: Pair<Int, Int>, // (widthDp, heightDp)
    val issueType: String,
    val description: String,
    val beforeImagePath: String? = null,
    val afterImagePath: String? = null
)

/**
 * ============================================================
 * DecisionResult — Desktop Mode vs 大屏适配决策结果
 * ============================================================
 *
 * @param recommendedApproach Recommended approach
 * @param priority Priority level (HIGH, MEDIUM, LOW)
 * @param reasoning Decision reasoning
 * @param effortEstimate Effort estimate (MAN_HOURS)
 */
data class DecisionResult(
    val recommendedApproach: String,
    val priority: String,
    val reasoning: String,
    val effortEstimate: String
)

/**
 * ============================================================
 * CheckItem — 检查清单条目
 * ============================================================
 *
 * @param id Unique ID
 * @param title Check item title
 * @param description Description
 * @param isChecked Whether the item is checked
 * @param category Category (WindowAPI, Reflect, zRAM, Input, etc.)
 */
data class CheckItem(
    val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val category: String
)

/**
 * ============================================================
 * DesktopModeState — Desktop Mode 工具页面状态（MVI State）
 * ============================================================
 *
 * @param qualityScore Overall quality score (0-100)
 * @param radarScores Four-dimensional radar scores
 * @param activeTab Currently active tab
 * @param isScanning Whether CI scan is running
 * @param scanProgress CI scan progress (0f-1f)
 * @param reflectStatus AI Reflect Layer adaptation status
 * @param ciResults CI scan results
 * @param zramTestResult zRAM test result
 * @param deviceRAM Simulated device RAM in MB
 * @param decisionResult Desktop Mode vs Large Screen decision result
 * @param checklist Checklist items
 * @param isReflectChecking Whether Reflect check is running
 * @param isZramTesting Whether zRAM test is running
 * @param errorMessage Error message if any
 */
data class DesktopModeState(
    val qualityScore: Int = 0,
    val radarScores: RadarScores = RadarScores(),
    val activeTab: DesktopTab = DesktopTab.OVERVIEW,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val reflectStatus: ReflectStatus = ReflectStatus.UNKNOWN,
    val ciResults: List<CIResult> = emptyList(),
    val zramTestResult: ZRAMTestResult? = null,
    val deviceRAM: Int = 8192,
    val decisionResult: DecisionResult? = null,
    val checklist: List<CheckItem> = emptyList(),
    val isReflectChecking: Boolean = false,
    val isZramTesting: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial/empty state */
        val Initial = DesktopModeState()
    }
}

/**
 * ============================================================
 * DesktopModeIntent — 用户意图（User Intent）
 * ============================================================
 *
 * @see DesktopModeViewModel.sendIntent handles all Intents
 */
sealed interface DesktopModeIntent {

    /** 用户点击"开始 CI 扫描"按钮 */
    data object StartCIScan : DesktopModeIntent

    /** 用户点击"开始 Reflect 检测"按钮 */
    data object StartReflectCheck : DesktopModeIntent

    /** 用户点击"开始 zRAM 测试"按钮 */
    data object TestZRAMHibernation : DesktopModeIntent

    /** 用户调整设备 RAM 滑块
     * @param ramMB RAM 大小（MB）
     */
    data class SetDeviceRAM(val ramMB: Int) : DesktopModeIntent

    /** 用户切换 Tab
     * @param tab Target tab
     */
    data class SelectTab(val tab: DesktopTab) : DesktopModeIntent

    /** 用户导出报告 */
    data object ExportReport : DesktopModeIntent

    /** 用户切换 Checklist 条目
     * @param itemId Checklist 条目 ID
     */
    data class ToggleCheckItem(val itemId: String) : DesktopModeIntent

    /** 用户清除错误消息 */
    data object ClearError : DesktopModeIntent

    /** 用户运行适配决策分析 */
    data object RunDecisionAnalysis : DesktopModeIntent
}

/**
 * ============================================================
 * DesktopModeEffect — 一次性副作用（Effect）
 * ============================================================
 *
 * @see DesktopModeViewModel _effect.send() sends Effects
 */
sealed interface DesktopModeEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : DesktopModeEffect

    /** 报告导出成功
     * @param path 导出文件路径
     */
    data class ReportReady(val path: String) : DesktopModeEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : DesktopModeEffect
}

// ================================================================
// 默认数据 / Default Data
// ================================================================

/**
 * ============================================================
 * defaultChecklist — Desktop Mode 适配检查清单
 * ============================================================
 */
val defaultChecklist = listOf(
    // Quartz Compositor API
    CheckItem("ck_001", "使用 Quartz WindowManager API", "使用 android.window.WindowManager 而非传统 ActivityManager", category = "WindowAPI"),
    CheckItem("ck_002", "处理 onConfigurationChanged", "浮动窗口尺寸变化时正确处理配置变更", category = "WindowAPI"),
    CheckItem("ck_003", "最小窗口尺寸支持", "确保布局支持 220dp × 275dp 最小窗口", category = "WindowAPI"),
    CheckItem("ck_004", "多窗口生命周期管理", "正确处理多窗口 enter/exit 生命周期", category = "WindowAPI"),
    // AI Reflect Layer
    CheckItem("ck_005", "避免固定像素尺寸", "使用 dp/sp 而非 px，避免 AI Reflect 截断", category = "Reflect"),
    CheckItem("ck_006", "自适应布局", "使用 ConstraintLayout/链式布局替代固定位置", category = "Reflect"),
    CheckItem("ck_007", "测试不同窗口尺寸", "在 220dp 到全屏范围内测试布局", category = "Reflect"),
    // zRAM Hibernation
    CheckItem("ck_008", "状态保存/恢复", "正确实现 onSaveInstanceState/onRestoreInstanceState", category = "zRAM"),
    CheckItem("ck_009", "后台进程保活策略", "使用 WorkManager 而非后台 Service", category = "zRAM"),
    CheckItem("ck_010", "内存释放响应式", "监听 ComponentCallbacks2.onTrimMemory", category = "zRAM"),
    // Input Adaptation
    CheckItem("ck_011", "键鼠交互支持", "添加键盘快捷键和鼠标 hover 状态", category = "Input"),
    CheckItem("ck_012", "滚动容器支持", "所有列表使用 RecyclerView/LazyColumn 而非 ScrollView", category = "Input"),
    CheckItem("ck_013", "焦点管理", "为可交互元素设置 contentDescription 和 focusable", category = "Input"),
    // Visual Layout
    CheckItem("ck_014", "深色模式适配", "验证 Desktop Mode 深色主题正确渲染", category = "Layout"),
    CheckItem("ck_015", "横竖屏布局", "支持横屏 16:9 和竖屏 9:16 多种比例", category = "Layout")
)

/**
 * ============================================================
 * defaultRadarScores — 默认雷达评分（演示数据）
 * ============================================================
 */
fun defaultRadarScores() = RadarScores(
    multiWindow = 65,
    stateRecovery = 70,
    inputAdaptation = 55,
    visualLayout = 60
)
