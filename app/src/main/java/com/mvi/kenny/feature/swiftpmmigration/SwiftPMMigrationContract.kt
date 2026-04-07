package com.mvi.kenny.feature.swiftpmmigration

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * SwiftPMMigrationContract — KMP SwiftPM 迁移助手 MVI 契约
 * ============================================================
 * PRD-025 | KMP SwiftPM 迁移助手
 *
 * 功能模块（三大 Feature）：
 * - ScannerFeature    → CocoaPods 依赖扫描与过滤排序
 * - ConversionWizardFeature → 转换向导（SELECT → CONFIRM → DIFF → EXECUTE）
 * - ProgressTrackerFeature → 迁移进度追踪与报告导出
 *
 * 颜色语义（设计规范）：
 * - 成功 / 直接迁移：#81C784
 * - 警告 / 需要调整：#FFB74D
 * - 错误 / 不支持：  #E57373
 * - 紧迫 / Deadline：#F06292
 * - 信息：            #64B5F6
 *
 * MVI 三要素：
 * - State：页面状态的唯一真相来源，Immutable 数据类
 * - Intent：用户意图，ViewModel 收到 Intent 后执行业务逻辑
 * - Effect：一次性副作用（Toast、导航、文件导出）
 *
 * @see SwiftPMMigrationViewModel 状态管理逻辑
 * @see SwiftPMMigrationScreen UI 渲染层
 */

// ============================================================
// Color Palette / 颜色调色板
// ============================================================

/** 迁移助手专用颜色语义 */
object MigrationColors {
    val Success = Color(0xFF81C784)       // 直接迁移 / 成功
    val Warning = Color(0xFFFFB74D)       // 需要调整 / 警告
    val Error   = Color(0xFFE57373)       // 不支持 / 错误
    val Urgent  = Color(0xFFF06292)       // Deadline 紧迫
    val Info    = Color(0xFF64B5F6)       // 信息提示
    val Background = Color(0xFF1E1E1E)    // 暗色主题背景
    val Surface = Color(0xFF252526)       // 面板背景
    val Border  = Color(0xFF3C3C3C)       // 边框
    val TextPrimary = Color(0xFFCCCCCC)   // 主要文字
    val TextSecondary = Color(0xFF808080)  // 辅助文字
}

// ============================================================
// Enums / 枚举定义
// ============================================================

/**
 * 主导航 Tab（三栏左侧 NavigationRail）
 *
 * @param title 中文标题
 * @param iconName Material 图标名称
 */
enum class MigrationTab(val title: String, val iconName: String) {
    SCANNER("依赖扫描", "radar"),
    WIZARD("转换向导", "wizard"),
    PROGRESS("进度追踪", "timeline")
}

/** 扫描状态 */
enum class ScanStatus { IDLE, SCANNING, COMPLETED, ERROR }

/** 过滤模式 */
enum class FilterMode(val label: String) {
    ALL("全部"),
    DIRECT("直接迁移"),
    NEEDS_ADJUSTMENT("需要调整"),
    UNSUPPORTED("不支持")
}

/** 排序模式 */
enum class SortMode(val label: String) {
    BY_EMERGENCY("按紧急度"),
    BY_NAME("按名称"),
    BY_COMPLEXITY("按复杂度")
}

/** 迁移路径类型 */
enum class MigrationPath(val label: String, val color: Color) {
    DIRECT("直接迁移", MigrationColors.Success),
    NEEDS_ADJUSTMENT("需要调整", MigrationColors.Warning),
    UNSUPPORTED("不支持", MigrationColors.Error)
}

/** 转换向导步骤 */
enum class WizardStep(val label: String, val stepNumber: Int) {
    SELECT("选择依赖", 1),
    CONFIRM("确认方案", 2),
    DIFF("预览 Diff", 3),
    EXECUTE("执行转换", 4)
}

/** Deadline 紧迫度等级 */
enum class UrgencyLevel(val label: String, val color: Color, val days: Int) {
    GRACE("#90天以上", Color(0xFF9E9E9E), 90),
    ATTENTION("#60-90天", MigrationColors.Info, 60),
    SOON("#30-60天", MigrationColors.Warning, 30),
    URGENT("#30天内", MigrationColors.Urgent, 0),
    EXPIRED("已过期", MigrationColors.Error, -1)
}

/** 报告格式 */
enum class ReportFormat(val label: String, val extension: String) {
    MARKDOWN("Markdown", "md"),
    JSON("JSON", "json")
}

/** 向导导航方向 */
enum class WizardDirection { FORWARD, BACKWARD }

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * 依赖项信息
 *
 * @param id 唯一标识符
 * @param name 依赖名称
 * @param version 当前版本
 * @param migrationPath 迁移路径
 * @param isSelected 是否选中
 * @param daysUntilSunset 距 Sunset Deadline 天数
 * @param swiftpmProduct SwiftPM 对应 Product（若有）
 * @param alternativeLibrary 替代库（若不支持）
 * @param complexity 迁移复杂度（1-5）
 * @param podspecContent Podspec 内容摘要
 */
data class DependencyInfo(
    val id: String,
    val name: String,
    val version: String,
    val migrationPath: MigrationPath,
    val isSelected: Boolean = false,
    val daysUntilSunset: Int,
    val swiftpmProduct: String? = null,
    val alternativeLibrary: String? = null,
    val complexity: Int = 1,
    val podspecContent: String = "",
    val estimatedMinutes: Int = 0,
)

/**
 * 转换方案
 *
 * @param dependencyId 关联的依赖 ID
 * @param recommendedAction 推荐操作描述
 * @param newPodspecContent 新的 Podspec 内容（转换后）
 * @param warnings 警告信息列表
 * @param breakingChanges 破坏性变更列表
 */
data class ConversionPlan(
    val dependencyId: String,
    val recommendedAction: String,
    val newPodspecContent: String = "",
    val warnings: List<String> = emptyList(),
    val breakingChanges: List<String> = emptyList(),
)

/**
 * Diff 结果
 *
 * @param dependencyId 关联的依赖 ID
 * @param originalContent 原始内容
 * @param convertedContent 转换后内容
 * @param diffLines Diff 行列表
 * @param hasConflicts 是否有冲突
 */
data class DiffResult(
    val dependencyId: String,
    val originalContent: String,
    val convertedContent: String,
    val diffLines: List<DiffLine> = emptyList(),
    val hasConflicts: Boolean = false,
)

/**
 * Diff 单行
 *
 * @param type 行类型：CONTEXT / ADDED / REMOVED
 * @param content 行内容
 */
data class DiffLine(
    val type: DiffLineType,
    val content: String,
)

enum class DiffLineType { CONTEXT, ADDED, REMOVED }

/**
 * 模块进度
 *
 * @param moduleName 模块名称
 * @param totalDependencies 总依赖数
 * @param migratedCount 已迁移数
 * @param blockedCount 被阻塞数
 */
data class ModuleProgress(
    val moduleName: String,
    val totalDependencies: Int,
    val migratedCount: Int,
    val blockedCount: Int,
) {
    val progress: Float get() = if (totalDependencies > 0) migratedCount.toFloat() / totalDependencies else 0f
}

/**
 * 迁移记录
 *
 * @param id 记录 ID
 * @param dependencyName 依赖名称
 * @param migratedAt 迁移时间戳
 * @param status 迁移状态
 * @param moduleName 所属模块
 */
data class MigrationRecord(
    val id: String,
    val dependencyName: String,
    val migratedAt: Long,
    val status: MigrationStatus,
    val moduleName: String,
)

enum class MigrationStatus { SUCCESS, PARTIAL, FAILED, ROLLED_BACK }

// ============================================================
// MVI State / 页面状态
// ============================================================

/**
 * 主状态 — 三栏布局状态容器
 *
 * @param activeTab 当前活跃的导航 Tab
 * @param scannerState 扫描器状态
 * @param wizardState 转换向导状态
 * @param progressState 进度追踪状态
 * @param selectedDependencyId 右侧详情面板选中的依赖 ID
 * @param isDetailPanelExpanded 右侧详情面板是否展开
 */
data class SwiftPMMigrationState(
    val activeTab: MigrationTab = MigrationTab.SCANNER,
    val scannerState: ScannerState = ScannerState(),
    val wizardState: ConversionWizardState = ConversionWizardState(),
    val progressState: ProgressTrackerState = ProgressTrackerState(),
    val selectedDependencyId: String? = null,
    val isDetailPanelExpanded: Boolean = false,
)

/**
 * ScannerFeature 状态
 *
 * @param scanStatus 扫描状态
 * @param scanProgress 扫描进度 0-100
 * @param scannedCount 已扫描数
 * @param totalCount 总数
 * @param dependencies 依赖列表
 * @param filterMode 当前过滤模式
 * @param sortMode 当前排序模式
 * @param daysUntilSunset 距 Sunset Deadline 天数
 * @param errorMessage 错误信息（若有）
 */
data class ScannerState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Int = 0,
    val scannedCount: Int = 0,
    val totalCount: Int = 0,
    val dependencies: List<DependencyInfo> = emptyList(),
    val filterMode: FilterMode = FilterMode.ALL,
    val sortMode: SortMode = SortMode.BY_EMERGENCY,
    val daysUntilSunset: Int = 365,
    val errorMessage: String? = null,
) {
    /** 根据过滤和排序模式获取有效依赖列表 */
    val filteredDependencies: List<DependencyInfo>
        get() {
            val filtered = when (filterMode) {
                FilterMode.ALL -> dependencies
                FilterMode.DIRECT -> dependencies.filter { it.migrationPath == MigrationPath.DIRECT }
                FilterMode.NEEDS_ADJUSTMENT -> dependencies.filter { it.migrationPath == MigrationPath.NEEDS_ADJUSTMENT }
                FilterMode.UNSUPPORTED -> dependencies.filter { it.migrationPath == MigrationPath.UNSUPPORTED }
            }
            return when (sortMode) {
                SortMode.BY_EMERGENCY -> filtered.sortedBy { it.daysUntilSunset }
                SortMode.BY_NAME -> filtered.sortedBy { it.name }
                SortMode.BY_COMPLEXITY -> filtered.sortedBy { it.complexity }
            }
        }

    /** 获取紧迫度等级 */
    val urgencyLevel: UrgencyLevel
        get() = when {
            daysUntilSunset < 0 -> UrgencyLevel.EXPIRED
            daysUntilSunset < 30 -> UrgencyLevel.URGENT
            daysUntilSunset < 60 -> UrgencyLevel.SOON
            daysUntilSunset < 90 -> UrgencyLevel.ATTENTION
            else -> UrgencyLevel.GRACE
        }

    /** 是否显示 Deadline Banner */
    val showDeadlineBanner: Boolean get() = daysUntilSunset < 90

    /** 可迁移依赖总数（直接迁移 + 需要调整） */
    val migratableCount: Int
        get() = dependencies.count { it.migrationPath != MigrationPath.UNSUPPORTED }
}

/**
 * ConversionWizardFeature 状态
 *
 * @param currentStep 当前向导步骤
 * @param selectedDependencies 已选中的依赖 ID 集合
 * @param conversionPlans 转换方案 Map（dependencyId → ConversionPlan）
 * @param diffResults Diff 结果 Map（dependencyId → DiffResult）
 * @param isExecuting 是否正在执行转换
 * @param executionProgress 执行进度
 * @param executionTotal 执行总任务数
 * @param executedCount 已执行数量
 * @param rollbackAvailable 是否有可用的回滚
 * @param rollbackSnapshotId 回滚快照 ID
 */
data class ConversionWizardState(
    val currentStep: WizardStep = WizardStep.SELECT,
    val selectedDependencies: Set<String> = emptySet(),
    val conversionPlans: Map<String, ConversionPlan> = emptyMap(),
    val diffResults: Map<String, DiffResult> = emptyMap(),
    val isExecuting: Boolean = false,
    val executionProgress: Int = 0,
    val executionTotal: Int = 0,
    val executedCount: Int = 0,
    val rollbackAvailable: Boolean = false,
    val rollbackSnapshotId: String? = null,
) {
    /** 当前步骤是否可以前进 */
    val canGoForward: Boolean
        get() = when (currentStep) {
            WizardStep.SELECT -> selectedDependencies.isNotEmpty()
            WizardStep.CONFIRM -> conversionPlans.isNotEmpty()
            WizardStep.DIFF -> diffResults.isNotEmpty()
            WizardStep.EXECUTE -> !isExecuting
        }

    /** 当前步骤是否可以后退 */
    val canGoBackward: Boolean
        get() = currentStep.stepNumber > 1

    /** 执行进度百分比 */
    val executionPercent: Float
        get() = if (executionTotal > 0) executionProgress.toFloat() / executionTotal else 0f
}

/**
 * ProgressTrackerFeature 状态
 *
 * @param overallProgress 总体进度 0.0-1.0
 * @param daysUntilSunset 距 Deadline 天数
 * @param modules 模块进度列表
 * @param recentMigrations 最近迁移记录
 * @param exportedReportPath 导出的报告路径（若有）
 * @param selectedModuleName 当前选中的模块名
 */
data class ProgressTrackerState(
    val overallProgress: Float = 0f,
    val daysUntilSunset: Int = 365,
    val modules: List<ModuleProgress> = emptyList(),
    val recentMigrations: List<MigrationRecord> = emptyList(),
    val exportedReportPath: String? = null,
    val selectedModuleName: String? = null,
) {
    /** 已迁移依赖总数 */
    val totalMigrated: Int get() = modules.sumOf { it.migratedCount }

    /** 总依赖数 */
    val totalDependencies: Int get() = modules.sumOf { it.totalDependencies }

    /** 剩余依赖数 */
    val remainingDependencies: Int get() = totalDependencies - totalMigrated
}

// ============================================================
// MVI Intent / 用户意图
// ============================================================

/**
 * 主 Intent — 跨 Feature 导航意图
 */
sealed class SwiftPMMigrationIntent {
    data class SwitchTab(val tab: MigrationTab) : SwiftPMMigrationIntent()
    data class SelectDependencyForDetail(val dependencyId: String?) : SwiftPMMigrationIntent()
    data object ToggleDetailPanel : SwiftPMMigrationIntent()
}

/**
 * ScannerFeature Intent — 扫描器用户意图
 */
sealed class ScannerIntent {
    data object StartScan : ScannerIntent()
    data object CancelScan : ScannerIntent()
    data class SetFilter(val mode: FilterMode) : ScannerIntent()
    data class SetSort(val mode: SortMode) : ScannerIntent()
    data class SelectDependency(val id: String, val selected: Boolean) : ScannerIntent()
    data object SelectAllMigratable : ScannerIntent()
    data object DeselectAll : ScannerIntent()
}

/**
 * ConversionWizardFeature Intent — 转换向导用户意图
 */
sealed class ConversionWizardIntent {
    data class SelectDependencies(val ids: Set<String>) : ConversionWizardIntent()
    data object NextStep : ConversionWizardIntent()
    data object PreviousStep : ConversionWizardIntent()
    data object ExecuteConversion : ConversionWizardIntent()
    data object RollbackLast : ConversionWizardIntent()
    data object ConfirmAndClose : ConversionWizardIntent()
}

/**
 * ProgressTrackerFeature Intent — 进度追踪用户意图
 */
sealed class ProgressTrackerIntent {
    data object RefreshProgress : ProgressTrackerIntent()
    data class ExportReport(val format: ReportFormat) : ProgressTrackerIntent()
    data class SelectModule(val moduleName: String) : ProgressTrackerIntent()
}

// ============================================================
// MVI Effect / 一次性副作用
// ============================================================

/**
 * 主 Effect — 跨 Feature 副作用
 */
sealed class SwiftPMMigrationEffect {
    data class ShowToast(val message: String) : SwiftPMMigrationEffect()
    data class NavigateToTab(val tab: MigrationTab) : SwiftPMMigrationEffect()
}

/**
 * ScannerFeature Effect — 扫描器副作用
 */
sealed class ScannerEffect {
    data class ShowToast(val message: String) : ScannerEffect()
    data object ScanCompleted : ScannerEffect()
    data class NavigateToDependencyDetail(val dependencyId: String) : ScannerEffect()
    data class ShowError(val message: String) : ScannerEffect()
}

/**
 * ConversionWizardFeature Effect — 转换向导副作用
 */
sealed class ConversionWizardEffect {
    data class ShowDiff(val dependencyId: String, val diff: DiffResult) : ConversionWizardEffect()
    data object ConversionCompleted : ConversionWizardEffect()
    data object RollbackCompleted : ConversionWizardEffect()
    data class ShowError(val message: String) : ConversionWizardEffect()
    data class ShowConfirmation(val message: String, val onConfirm: () -> Unit) : ConversionWizardEffect()
}

/**
 * ProgressTrackerFeature Effect — 进度追踪副作用
 */
sealed class ProgressTrackerEffect {
    data class ReportExported(val path: String) : ProgressTrackerEffect()
    data class ShowModuleDetail(val module: ModuleProgress) : ProgressTrackerEffect()
}
