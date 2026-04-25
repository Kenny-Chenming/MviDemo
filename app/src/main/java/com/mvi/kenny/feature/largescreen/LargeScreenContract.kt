package com.mvi.kenny.feature.largescreen

// ================================================================
// LargeScreenContract — Android 17 大屏强制适配与 Continuous Canary Release MVI 契约
// ================================================================
// MVI architecture contract for Android 17 Large Screen adaptation toolkit.
//
// PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
// Design Reference: memory/agency/designs/PRD-155-Android-17-大屏强制适配与-Continuous-Canary-Release-开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color

// ================================================================
// 工具枚举 / Tool Enums
// ================================================================

/**
 * ============================================================
 * LargeScreenTool — 大屏工具枚举
 * ============================================================
 * 七大工具的枚举表示。
 *
 * @param title 中文显示名称
 * @param description 工具描述
 * @param iconMaterialIcon 图标名称
 */
enum class LargeScreenTool(
    val title: String,
    val description: String,
    val iconMaterialIcon: String
) {
    /** 工具1：多窗口合规性自动化检测器 */
    COMPLIANCE_DETECTOR("合规检测器", "检测 resizeableActivity=false 等不合规配置", "Shield"),
    /** 工具2：大屏形态 UI 自动化截图测试工具 */
    SCREEN_CAPTURE_TEST("截图测试", "多窗口尺寸下的 UI 截图对比测试", "Screenshot"),
    /** 工具3：Continuous Canary 适配工作流 */
    CANARY_WORKFLOW("Canary 工作流", "持续跟踪 Android API 变化并管理适配任务", "Flow"),
    /** 工具4：多窗口降级策略检测器 */
    FALLBACK_DETECTOR("降级检测器", "检测 App 的 fallback 路径和降级策略", "Fallback"),
    /** 工具5：折叠屏/自由窗口适配检查清单 */
    ADAPTIVE_CHECKLIST("适配检查清单", "分步骤检查折叠屏/自由窗口适配状态", "Checklist")
}

/**
 * ============================================================
 * ViolationSeverity — 违规严重程度枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji 表示
 * @param colorHex Compose Color 值
 */
enum class ViolationSeverity(val displayName: String, val emoji: String, val colorHex: Long) {
    ERROR("错误", "🔴", 0xFFB3261E),
    WARNING("警告", "🟡", 0xFFF5A623),
    INFO("提示", "🟢", 0xFF146B3A)
}

/**
 * ============================================================
 * WindowType — 窗口类型枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param minWidthDp 最小宽度 dp
 */
enum class WindowType(val displayName: String, val minWidthDp: Int) {
    PHONE("手机", 0),
    TABLET("平板", 600),
    FOLDABLE("折叠屏", 600),
    FREEFORM("自由窗口", 0)
}

/**
 * ============================================================
 * CheckStatus — 检查项状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class CheckStatus(val displayName: String) {
    UNCHECKED("未检查"),
    PASSED("通过"),
    FAILED("未通过"),
    NOT_APPLICABLE("不适用")
}

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param colorHex 颜色值
 */
enum class RiskLevel(val displayName: String, val colorHex: Long) {
    LOW("低风险", 0xFF146B3A),
    MEDIUM("中风险", 0xFFF5A623),
    HIGH("高风险", 0xFFB3261E)
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
    PDF("PDF", "pdf"),
    JSON("JSON", "json"),
    MARKDOWN("Markdown", "md")
}

/**
 * ============================================================
 * MigrationTaskStatus — 适配任务状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class MigrationTaskStatus(val displayName: String) {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    DONE("已完成"),
    BLOCKED("已阻塞")
}

// ================================================================
// State / Model
// ================================================================

/**
 * ============================================================
 * LargeScreenState — 大屏适配工具页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param currentTool 当前选中的工具 Tab
 * @param activeSubTab 当前工具内的子 Tab（如有）
 * @param complianceViolations 合规违规列表
 * @param screenCaptures 多窗口尺寸截图映射
 * @param apiChanges API 变更日志列表
 * @param checklistItems 适配检查清单列表
 * @param preCheckResult Play Store 预检结果
 * @param complianceHistory 合规历史趋势数据
 * @param migrationTasks 适配任务看板数据
 * @param isLoading 是否正在加载
 * @param error 错误消息
 * @param selectedViolation 选中的违规项（详情 sheet 用）
 * @param checkTargetPath 检查目标路径（APK 路径或代码路径）
 * @param selectedWindowSizes 选中的窗口尺寸列表
 *
 * @see LargeScreenTool
 * @see ComplianceViolation
 * @see WindowSize
 * @see APIChange
 * @see ChecklistItem
 * @see PlayStorePreCheckResult
 * @see ComplianceSnapshot
 * @see MigrationTask
 */
data class LargeScreenState(
    val currentTool: LargeScreenTool = LargeScreenTool.COMPLIANCE_DETECTOR,
    val activeSubTab: LargeScreenSubTab = LargeScreenSubTab.OVERVIEW,
    val complianceViolations: List<ComplianceViolation> = emptyList(),
    val screenCaptures: Map<WindowSize, ScreenCaptureResult> = emptyMap(),
    val apiChanges: List<APIChange> = emptyList(),
    val checklistItems: List<ChecklistItem> = emptyList(),
    val preCheckResult: PlayStorePreCheckResult? = null,
    val complianceHistory: List<ComplianceSnapshot> = emptyList(),
    val migrationTasks: List<MigrationTask> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedViolation: ComplianceViolation? = null,
    val checkTargetPath: String = "",
    val selectedWindowSizes: List<WindowSize> = listOf(
        WindowSize(360, 800, WindowType.PHONE),
        WindowSize(600, 840, WindowType.TABLET),
        WindowSize(840, 1200, WindowType.TABLET)
    ),
    val generatedDiff: String? = null,
    val exportedFilePath: String? = null
) {
    companion object {
        /** 初始状态 */
        val Initial = LargeScreenState()
    }
}

/**
 * ============================================================
 * LargeScreenSubTab — 子 Tab 枚举
 * ============================================================
 * Each tool may have internal sub-tabs.
 *
 * @param title Tab 显示标题
 */
enum class LargeScreenSubTab(val title: String) {
    OVERVIEW("概览"),
    RESULTS("结果"),
    HISTORY("历史"),
    SETTINGS("设置")
}

// ================================================================
// Intent / User Actions
// ================================================================

/**
 * ============================================================
 * LargeScreenIntent — 大屏适配工具用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see LargeScreenViewModel.sendIntent handles all Intents
 */
sealed interface LargeScreenIntent {

    /** 用户切换工具 Tab */
    data class SelectTool(val tool: LargeScreenTool) : LargeScreenIntent

    /** 用户切换子 Tab */
    data class SelectSubTab(val subTab: LargeScreenSubTab) : LargeScreenIntent

    /** 运行合规检测
     * @param target APK 路径或代码目录路径
     */
    data class RunComplianceCheck(val target: String) : LargeScreenIntent

    /** 执行多窗口截图测试
     * @param windowSizes 窗口尺寸列表
     */
    data class CaptureScreens(val windowSizes: List<WindowSize>) : LargeScreenIntent

    /** 生成修复 Diff
     * @param violation 违规项
     */
    data class GenerateFixDiff(val violation: ComplianceViolation) : LargeScreenIntent

    /** 更新检查清单项状态
     * @param item 检查项
     * @param status 新状态
     */
    data class UpdateChecklistItem(
        val item: ChecklistItem,
        val status: CheckStatus
    ) : LargeScreenIntent

    /** 运行 Play Store 预检
     * @param apkPath APK 路径
     */
    data class RunPlayStorePreCheck(val apkPath: String) : LargeScreenIntent

    /** 订阅 API 变更
     * @param androidVersions 要订阅的 Android 版本列表
     */
    data class SubscribeAPIChanges(val androidVersions: List<Int>) : LargeScreenIntent

    /** 导出合规报告
     * @param format 导出格式
     */
    data class ExportReport(val format: ReportFormat) : LargeScreenIntent

    /** 清除错误消息 */
    data object ClearError : LargeScreenIntent

    /** 关闭违规详情 Sheet */
    data object DismissViolationDetail : LargeScreenIntent

    /** 选择某个违规项查看详情 */
    data class SelectViolation(val violation: ComplianceViolation) : LargeScreenIntent

    /** 查看大屏预览 */
    data class PreviewWindowSize(val windowSize: WindowSize) : LargeScreenIntent

    /** 更新检查目标路径 */
    data class UpdateCheckTarget(val path: String) : LargeScreenIntent

    /** 添加适配任务 */
    data class AddMigrationTask(val task: MigrationTask) : LargeScreenIntent

    /** 更新适配任务状态 */
    data class UpdateMigrationTaskStatus(
        val taskId: String,
        val status: MigrationTaskStatus
    ) : LargeScreenIntent
}

// ================================================================
// Effect / One-time Side Effects
// ================================================================

/**
 * ============================================================
 * LargeScreenEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see LargeScreenViewModel _effect.send() sends Effects
 */
sealed interface LargeScreenEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : LargeScreenEffect

    /** Diff 生成完成
     * @param diff 生成的 diff 内容
     */
    data class DiffGenerated(val diff: String) : LargeScreenEffect

    /** 报告导出成功
     * @param filePath 导出文件路径
     */
    data class ReportExported(val filePath: String) : LargeScreenEffect

    /** Play Store 拒绝风险通知
     * @param riskLevel 风险等级
     * @param reasons 拒绝原因列表
     */
    data class PlayStoreRejectionRisk(
        val riskLevel: RiskLevel,
        val reasons: List<String>
    ) : LargeScreenEffect

    /** API 变更通知
     * @param change API 变更项
     */
    data class APIChangeNotification(val change: APIChange) : LargeScreenEffect

    /** 显示 Snackbar 消息
     * @param message Snackbar 文本
     */
    data class ShowSnackbar(val message: String) : LargeScreenEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ComplianceViolation — 合规违规项
 * ================================================================
 *
 * @param id 唯一 ID
 * @param file 文件路径
 * @param line 行号
 * @param config 不合规配置项（如 android:resizeableActivity="false"）
 * @param issue 问题描述
 * @param fixSuggestion 修复建议
 * @param severity 严重程度
 */
data class ComplianceViolation(
    val id: String,
    val file: String,
    val line: Int,
    val config: String,
    val issue: String,
    val fixSuggestion: String,
    val severity: ViolationSeverity
)

/**
 * ============================================================
 * WindowSize — 窗口尺寸数据类
 * ================================================================
 *
 * @param widthDp 宽度 dp
 * @param heightDp 高度 dp
 * @param type 窗口类型
 */
data class WindowSize(
    val widthDp: Int,
    val heightDp: Int,
    val type: WindowType
) {
    val breakpointLabel: String
        get() = when {
            widthDp >= 960 -> "sw=960dp（大屏平板）"
            widthDp >= 840 -> "sw=840dp（标准平板）"
            widthDp >= 600 -> "sw=600dp（中等平板）"
            else -> "sw=${widthDp}dp（手机/折叠屏）"
        }
}

/**
 * ============================================================
 * ScreenCaptureResult — 截图测试结果
 * ================================================================
 *
 * @param windowSize 窗口尺寸
 * @param capturePath 截图文件路径
 * @param status 测试状态：PASS/FAIL/CRASH
 * @param diffRegions 差异区域列表（用于回归对比）
 * @param errorMessage 错误信息（如崩溃日志）
 */
data class ScreenCaptureResult(
    val windowSize: WindowSize,
    val capturePath: String?,
    val status: ScreenCaptureStatus,
    val diffRegions: List<DiffRegion> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ============================================================
 * ScreenCaptureStatus — 截图测试状态
 * ============================================================
 */
enum class ScreenCaptureStatus { PASS, FAIL, CRASH, PENDING }

/**
 * ============================================================
 * DiffRegion — 布局差异区域
 * ================================================================
 *
 * @param regionId 区域 ID
 * @param description 差异描述
 * @param severity 严重程度
 */
data class DiffRegion(
    val regionId: String,
    val description: String,
    val severity: ViolationSeverity
)

/**
 * ============================================================
 * APIChange — Android API 变更记录
 * ================================================================
 *
 * @param id 唯一 ID
 * @param version Android 版本（如 36, 37）
 * @param category 变更类别（Permissions / Windowing / Battery 等）
 * @param title 变更标题
 * @param description 变更描述
 * @param isBreakingChange 是否为破坏性变更
 * @param migrationGuide 迁移指南链接
 * @param isSubscribed 是否已订阅
 */
data class APIChange(
    val id: String,
    val version: Int,
    val category: String,
    val title: String,
    val description: String,
    val isBreakingChange: Boolean,
    val migrationGuide: String = "",
    val isSubscribed: Boolean = false
)

/**
 * ============================================================
 * ChecklistItem — 适配检查清单项
 * ================================================================
 *
 * @param id 唯一 ID
 * @param category 检查类别（WindowManager / SplitScreen / Freeform 等）
 * @param title 检查项标题
 * @param description 检查项描述
 * @param status 当前检查状态
 * @param referenceDoc 参考文档链接
 * @param order 排序序号
 */
data class ChecklistItem(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val status: CheckStatus = CheckStatus.UNCHECKED,
    val referenceDoc: String = "",
    val order: Int = 0
)

/**
 * ============================================================
 * PlayStorePreCheckResult — Play Store 预检结果
 * ================================================================
 *
 * @param willBeRejected 是否会被拒绝
 * @param riskLevel 风险等级
 * @param rejectionReasons 拒绝原因列表
 * @param warnings 警告信息列表
 * @param passedChecks 通过的检查项列表
 * @param checkedAt 检查时间戳
 */
data class PlayStorePreCheckResult(
    val willBeRejected: Boolean,
    val riskLevel: RiskLevel,
    val rejectionReasons: List<String>,
    val warnings: List<String>,
    val passedChecks: List<String>,
    val checkedAt: Long
)

/**
 * ============================================================
 * ComplianceSnapshot — 合规历史快照
 * ================================================================
 *
 * @param timestamp 快照时间戳
 * @param score 合规评分（0-100）
 * @param violationCount 总违规数
 * @param errorCount 错误数
 * @param warningCount 警告数
 */
data class ComplianceSnapshot(
    val timestamp: Long,
    val score: Int,
    val violationCount: Int,
    val errorCount: Int,
    val warningCount: Int
)

/**
 * ============================================================
 * MigrationTask — 适配任务
 * ================================================================
 *
 * @param id 任务 ID
 * @param title 任务标题
 * @param description 任务描述
 * @param apiChangeId 关联的 API 变更 ID
 * @param status 任务状态
 * @param priority 优先级（1=高，2=中，3=低）
 * @param assignee 负责人
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
data class MigrationTask(
    val id: String,
    val title: String,
    val description: String,
    val apiChangeId: String? = null,
    val status: MigrationTaskStatus = MigrationTaskStatus.TODO,
    val priority: Int = 2,
    val assignee: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * ============================================================
 * FallbackPath — 降级路径
 * ================================================================
 *
 * @param id 路径 ID
 * @param condition 触发条件描述
 * @param behavior 降级行为描述
 * @param affectedConfigurations 受影响的配置列表
 */
data class FallbackPath(
    val id: String,
    val condition: String,
    val behavior: String,
    val affectedConfigurations: List<String>
)
