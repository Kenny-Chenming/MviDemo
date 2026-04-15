package com.mvi.kenny.feature.paging35

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * Paging35Contract — Paging 3.5 asState 操作符开发工具包 MVI 契约
 * ============================================================
 * MVI Architecture Pattern / MVI 架构模式
 *
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions, ViewModel processes and updates State
 * - Effect: One-time side effects (navigation, toast, etc.)
 *
 * PRD-093: Paging 3.5 asState 操作符开发工具包
 *
 * @see Paging35ViewModel State management
 * @see Paging35Screen Main screen
 */

// =============================================================
// PagingTool — 工具类型枚举
// =============================================================
/**
 * Paging Tool Type / Paging 工具类型
 *
 * Represents the different tools available in the Paging 3.5 toolkit.
 *
 * @param title Display title / 显示标题
 * @param description Tool description / 工具描述
 * @param icon Tool icon / 工具图标
 */
enum class PagingTool(
    val title: String,
    val titleZh: String,
    val description: String,
    val descriptionZh: String
) {
    AS_STATE_TEMPLATE("asState Template Generator", "asState 模板生成器", "Generate production-ready asState code templates", "生成生产级 asState 代码模板"),
    MIGRATION_REPORT("Migration Report", "迁移检测报告", "Detect collectAsLazyPagingItems() and suggest asState migration", "检测 collectAsLazyPagingItems() 并建议 asState 迁移"),
    PAGING_DEBUG("PagingSource Debug Panel", "PagingSource 调试面板", "Real-time PagingData flow visualization and simulation", "实时 PagingData 流向可视化和模拟"),
    TROUBLESHOOTING("Troubleshooting Tool", "踩坑排查工具", "Common Paging 3 pitfalls diagnosis and solutions", "Paging 3 常见踩坑诊断和解决方案")
}

// =============================================================
// PagingScenario — asState 模板场景
// =============================================================
/**
 * Paging Scenario / 分页场景
 *
 * Represents different use cases for asState templates.
 *
 * @param label Display label / 显示标签
 * @param labelZh Chinese label / 中文标签
 */
enum class PagingScenario(val label: String, val labelZh: String) {
    BASIC_LIST("Basic List", "基础列表"),
    HEADER_FOOTER("Header/Footer List", "带 Header/Footer 的列表"),
    GRID_LIST("Grid List", "Grid 列表"),
    MULTI_TYPE("Multi-type Items", "多类型 Item 列表"),
    ROOM_INTEGRATION("Room Integration", "与 Room 集成的列表")
}

// =============================================================
// LoadState — 分页加载状态
// =============================================================
/**
 * Load State / 加载状态
 *
 * @param label Display label / 显示标签
 * @param color State color / 状态颜色
 */
enum class LoadState(val label: String, val color: Color) {
    LOADING("Loading", Color(0xFF6200EE)),
    ERROR("Error", Color(0xFFF44336)),
    SUCCESS("Success", Color(0xFF4CAF50)),
    IDLE("Idle", Color(0xFF757575))
}

// =============================================================
// CompatibilityLevel — 兼容性等级
// =============================================================
/**
 * Migration Compatibility Level / 迁移兼容性等级
 *
 * @param label Display label / 显示标签
 * @param labelZh Chinese label / 中文标签
 * @param color Status color / 状态颜色
 * @param emoji Status emoji / 状态 Emoji
 */
enum class CompatibilityLevel(
    val label: String,
    val labelZh: String,
    val color: Color,
    val emoji: String
) {
    DIRECT_REPLACE("Direct Replace", "直接替换", Color(0xFF4CAF50), "🟢"),
    NEEDS_ADJUSTMENT("Needs Adjustment", "需调整", Color(0xFFFF9800), "🟡"),
    NEEDS_REFACTOR("Needs Refactor", "需重构", Color(0xFFF44336), "🔴")
}

// =============================================================
// MigrationFile — 迁移文件信息
// =============================================================
/**
 * Migration File Information / 迁移文件信息
 *
 * Represents a file that needs migration from collectAsLazyPagingItems to asState.
 *
 * @param filePath File path / 文件路径
 * @param currentApi Current API used / 当前使用的 API
 * @param suggestedApi Suggested API to migrate to / 建议迁移到的 API
 * @param compatibilityLevel Migration compatibility level / 迁移兼容性等级
 * @param isIgnored Whether this file is ignored / 是否被忽略
 * @param issues Found issues in this file / 在此文件中发现的问题
 */
data class MigrationFile(
    val filePath: String,
    val currentApi: String,
    val suggestedApi: String,
    val compatibilityLevel: CompatibilityLevel,
    val isIgnored: Boolean = false,
    val issues: List<String> = emptyList()
)

// =============================================================
// LoadStateEvent — LoadState 变化事件
// =============================================================
/**
 * Load State Event / LoadState 变化事件
 *
 * Represents a single LoadState change event for timeline visualization.
 *
 * @param timestamp Event timestamp / 事件时间戳
 * @param loadState New load state / 新的加载状态
 * @param page Page number / 页码
 * @param itemCount Items loaded so far / 已加载的 Item 数量
 */
data class LoadStateEvent(
    val timestamp: Long,
    val loadState: LoadState,
    val page: Int,
    val itemCount: Int
)

// =============================================================
// TroubleshootingProblem — 踩坑问题
// =============================================================
/**
 * Troubleshooting Problem / 踩坑问题类型
 *
 * @param title Problem title / 问题标题
 * @param titleZh Chinese title / 中文标题
 * @param symptoms List of symptoms / 症状列表
 * @param symptomsZh Chinese symptoms / 中文症状
 * @param possibleCauses Possible causes / 可能原因
 * @param possibleCausesZh Chinese possible causes / 中文可能原因
 * @param solutions Solution suggestions / 解决方案建议
 * @param solutionsZh Chinese solutions / 中文解决方案
 */
data class TroubleshootingProblem(
    val id: String,
    val title: String,
    val titleZh: String,
    val symptoms: List<String>,
    val symptomsZh: List<String>,
    val possibleCauses: List<String>,
    val possibleCausesZh: List<String>,
    val solutions: List<String>,
    val solutionsZh: List<String>
)

// =============================================================
// Paging35DashboardState — 主仪表盘状态
// =============================================================
/**
 * Paging Health Dashboard State / Paging 健康仪表盘状态
 *
 * Single source of truth for the Paging health dashboard.
 *
 * @param pagingVersion Current Paging version / 当前 Paging 版本
 * @param healthScore Health score 0-100 / 健康度评分 0-100
 * @param totalPagingSources Total detected PagingSource count / 检测到的 PagingSource 总数
 * @param asStateUsageRatio asState usage ratio 0.0-1.0 / asState 使用比例 0.0-1.0
 * @param collectAsLazyCount collectAsLazyPagingItems count / collectAsLazyPagingItems 数量
 * @param isScanning Whether scanning is in progress / 是否正在扫描
 * @param scanProgress Scan progress 0.0-1.0 / 扫描进度 0.0-1.0
 * @param migrationFiles Files that need migration / 需要迁移的文件
 * @param selectedTool Currently selected tool / 当前选中的工具
 */
data class Paging35DashboardState(
    val pagingVersion: String = "3.5.0-beta01",
    val healthScore: Int = 0,
    val totalPagingSources: Int = 0,
    val asStateUsageRatio: Float = 0f,
    val collectAsLazyCount: Int = 0,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val migrationFiles: List<MigrationFile> = emptyList(),
    val selectedTool: PagingTool? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = Paging35DashboardState()
    }

    /**
     * Direct replace count / 可直接替换的数量
     */
    val directReplaceCount: Int
        get() = migrationFiles.count { it.compatibilityLevel == CompatibilityLevel.DIRECT_REPLACE && !it.isIgnored }

    /**
     * Needs adjustment count / 需要调整的数量
     */
    val needsAdjustmentCount: Int
        get() = migrationFiles.count { it.compatibilityLevel == CompatibilityLevel.NEEDS_ADJUSTMENT && !it.isIgnored }

    /**
     * Needs refactor count / 需要重构的数量
     */
    val needsRefactorCount: Int
        get() = migrationFiles.count { it.compatibilityLevel == CompatibilityLevel.NEEDS_REFACTOR && !it.isIgnored }
}

// =============================================================
// Paging35DashboardIntent — 主仪表盘用户意图
// =============================================================
/**
 * Paging Health Dashboard User Intents / Paging 健康仪表盘用户意图
 *
 * Every user action corresponds to an Intent.
 */
sealed interface Paging35DashboardIntent {
    /** Start scanning project for Paging usage / 开始扫描项目中的 Paging 使用情况
     */
    data object StartScan : Paging35DashboardIntent

    /** Refresh health score / 刷新健康度评分
     */
    data object RefreshHealth : Paging35DashboardIntent

    /** Navigate to a specific tool / 导航到特定工具
     * @param tool Tool to navigate to / 要导航到的工具
     */
    data class NavigateToTool(val tool: PagingTool) : Paging35DashboardIntent

    /** Select a tool / 选择工具
     * @param tool Tool to select / 要选择的工具
     */
    data class SelectTool(val tool: PagingTool) : Paging35DashboardIntent

    /** Go back to dashboard / 返回仪表盘
     */
    data object BackToDashboard : Paging35DashboardIntent
}

// =============================================================
// Paging35DashboardEffect — 主仪表盘副作用
// =============================================================
/**
 * Paging Health Dashboard Side Effects / Paging 健康仪表盘副作用
 *
 * One-time events, consumed only once by UI layer.
 */
sealed interface Paging35DashboardEffect {
    /** Navigate to tool / 导航到工具
     * @param tool Tool to navigate to / 要导航到的工具
     */
    data class NavigateToTool(val tool: PagingTool) : Paging35DashboardEffect

    /** Show scan complete notification / 显示扫描完成通知
     * @param issuesFound Number of issues found / 发现的问题数量
     */
    data class ShowScanComplete(val issuesFound: Int) : Paging35DashboardEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : Paging35DashboardEffect
}

// =============================================================
// AsStateTemplateState — asState 模板生成器状态
// =============================================================
/**
 * asState Template Generator State / asState 模板生成器状态
 *
 * @param selectedScenario Selected paging scenario / 选中的分页场景
 * @param pageSize Page size for paging / 分页大小
 * @param prefetchEnabled Whether prefetch is enabled / 是否启用预取
 * @param generatedCode Generated code / 生成的代码
 * @param isGenerating Whether code is being generated / 是否正在生成代码
 */
data class AsStateTemplateState(
    val selectedScenario: PagingScenario = PagingScenario.BASIC_LIST,
    val pageSize: Int = 20,
    val prefetchEnabled: Boolean = true,
    val generatedCode: String = "",
    val isGenerating: Boolean = false
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AsStateTemplateState()
    }
}

// =============================================================
// AsStateTemplateIntent — asState 模板生成器用户意图
// =============================================================
/**
 * asState Template Generator User Intents / asState 模板生成器用户意图
 */
sealed interface AsStateTemplateIntent {
    /** Select scenario / 选择场景
     * @param scenario Scenario to select / 要选择的场景
     */
    data class SelectScenario(val scenario: PagingScenario) : AsStateTemplateIntent

    /** Update page size / 更新分页大小
     * @param size New page size / 新的分页大小
     */
    data class UpdatePageSize(val size: Int) : AsStateTemplateIntent

    /** Toggle prefetch / 切换预取
     * @param enabled Whether prefetch is enabled / 是否启用预取
     */
    data class TogglePrefetch(val enabled: Boolean) : AsStateTemplateIntent

    /** Generate code / 生成代码
     */
    data object GenerateCode : AsStateTemplateIntent

    /** Copy code to clipboard / 复制代码到剪贴板
     */
    data object CopyCode : AsStateTemplateIntent
}

// =============================================================
// AsStateTemplateEffect — asState 模板生成器副作用
// =============================================================
/**
 * asState Template Generator Side Effects / asState 模板生成器副作用
 */
sealed interface AsStateTemplateEffect {
    /** Code copied to clipboard / 代码已复制到剪贴板
     */
    data object CodeCopied : AsStateTemplateEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : AsStateTemplateEffect
}

// =============================================================
// MigrationReportState — 迁移报告状态
// =============================================================
/**
 * Migration Report State / 迁移报告状态
 *
 * @param filesToMigrate Files that need migration / 需要迁移的文件
 * @param totalFiles Total number of files / 总文件数
 * @param directReplaceCount Direct replace count / 直接替换数量
 * @param needsAdjustmentCount Needs adjustment count / 需要调整数量
 * @param needsRefactorCount Needs refactor count / 需要重构数量
 * @param isGeneratingReport Whether report is being generated / 是否正在生成报告
 * @param selectedFile Selected file for detail view / 选中的文件（用于详情视图）
 * @param filterCompatibility Filter by compatibility level / 按兼容性等级过滤
 */
data class MigrationReportState(
    val filesToMigrate: List<MigrationFile> = emptyList(),
    val totalFiles: Int = 0,
    val directReplaceCount: Int = 0,
    val needsAdjustmentCount: Int = 0,
    val needsRefactorCount: Int = 0,
    val isGeneratingReport: Boolean = false,
    val selectedFile: MigrationFile? = null,
    val filterCompatibility: CompatibilityLevel? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = MigrationReportState()
    }

    /**
     * Filtered files based on compatibility filter / 根据兼容性过滤的文件
     */
    val filteredFiles: List<MigrationFile>
        get() = if (filterCompatibility == null) {
            filesToMigrate.filter { !it.isIgnored }
        } else {
            filesToMigrate.filter { !it.isIgnored && it.compatibilityLevel == filterCompatibility }
        }

    /**
     * Workload estimation / 工作量估算
     */
    val workloadEstimate: String
        get() = when {
            needsRefactorCount > 3 -> "High / 高"
            needsAdjustmentCount > 5 -> "Medium / 中"
            else -> "Low / 低"
        }
}

// =============================================================
// MigrationReportIntent — 迁移报告用户意图
// =============================================================
/**
 * Migration Report User Intents / 迁移报告用户意图
 */
sealed interface MigrationReportIntent {
    /** Generate report / 生成报告
     */
    data object GenerateReport : MigrationReportIntent

    /** Ignore file / 忽略文件
     * @param filePath File path to ignore / 要忽略的文件路径
     */
    data class IgnoreFile(val filePath: String) : MigrationReportIntent

    /** Export as Markdown / 导出为 Markdown
     */
    data object ExportMarkdown : MigrationReportIntent

    /** Select file for detail view / 选择文件查看详情
     * @param file File to select / 要选择的文件
     */
    data class SelectFile(val file: MigrationFile) : MigrationReportIntent

    /** Clear selected file / 清除选中的文件
     */
    data object ClearSelectedFile : MigrationReportIntent

    /** Filter by compatibility level / 按兼容性等级过滤
     * @param level Compatibility level to filter by / 要过滤的兼容性等级
     */
    data class FilterByCompatibility(val level: CompatibilityLevel?) : MigrationReportIntent
}

// =============================================================
// MigrationReportEffect — 迁移报告副作用
// =============================================================
/**
 * Migration Report Side Effects / 迁移报告副作用
 */
sealed interface MigrationReportEffect {
    /** Export complete / 导出完成
     * @param filePath Exported file path / 导出文件路径
     */
    data class ExportComplete(val filePath: String) : MigrationReportEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : MigrationReportEffect
}

// =============================================================
// PagingDebugPanelState — PagingSource 调试面板状态
// =============================================================
/**
 * PagingSource Debug Panel State / PagingSource 调试面板状态
 *
 * @param loadState Current load state / 当前加载状态
 * @param currentPage Current page number / 当前页码
 * @param totalPages Total pages / 总页数
 * @param totalItems Total loaded items / 已加载的 Item 总数
 * @param loadStateHistory Load state change history / LoadState 变化历史
 * @param isSimulating Whether simulation is running / 是否正在模拟
 * @param refreshTriggerTimes Number of times refresh was triggered / 刷新触发次数
 */
data class PagingDebugPanelState(
    val loadState: LoadState = LoadState.IDLE,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val totalItems: Int = 0,
    val loadStateHistory: List<LoadStateEvent> = emptyList(),
    val isSimulating: Boolean = false,
    val refreshTriggerTimes: Int = 0
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = PagingDebugPanelState()
    }

    /**
     * Get color for current load state / 获取当前加载状态的颜色
     */
    val currentLoadStateColor: Color
        get() = loadState.color

    /**
     * Progress percentage / 进度百分比
     */
    val progressPercentage: Float
        get() = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
}

// =============================================================
// PagingDebugPanelIntent — PagingSource 调试面板用户意图
// =============================================================
/**
 * PagingSource Debug Panel User Intents / PagingSource 调试面板用户意图
 */
sealed interface PagingDebugPanelIntent {
    /** Simulate loading next page / 模拟加载下一页
     */
    data object SimulateLoadNextPage : PagingDebugPanelIntent

    /** Simulate load error / 模拟加载失败
     */
    data object SimulateLoadError : PagingDebugPanelIntent

    /** Simulate refresh / 模拟刷新
     */
    data object SimulateRefresh : PagingDebugPanelIntent

    /** Reset state / 重置状态
     */
    data object ResetState : PagingDebugPanelIntent

    /** Record load state change / 记录加载状态变化
     * @param state New load state / 新的加载状态
     */
    data class RecordLoadStateChange(val state: LoadState) : PagingDebugPanelIntent
}

// =============================================================
// PagingDebugPanelEffect — PagingSource 调试面板副作用
// =============================================================
/**
 * PagingSource Debug Panel Side Effects / PagingSource 调试面板副作用
 */
sealed interface PagingDebugPanelEffect {
    /** Show pulse animation / 显示脉冲动画
     * @param color Animation color / 动画颜色
     */
    data class ShowPulseAnimation(val color: Color) : PagingDebugPanelEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : PagingDebugPanelEffect
}

// =============================================================
// PagingTroubleshootingState — 踩坑排查工具状态
// =============================================================
/**
 * Paging Troubleshooting Tool State / Paging 踩坑排查工具状态
 *
 * @param selectedTabIndex Selected tab index / 选中的 Tab 索引
 * @param selectedProblem Selected problem / 选中的问题
 * @param isAutoScanEnabled Whether auto scan is enabled / 是否启用自动扫描
 * @param scanProgress Scan progress 0.0-1.0 / 扫描进度 0.0-1.0
 * @param isScanning Whether scanning is in progress / 是否正在扫描
 * @param scanResults Scan results / 扫描结果
 * @param expandedCardId Expanded card ID / 展开的卡片 ID
 */
data class PagingTroubleshootingState(
    val selectedTabIndex: Int = 0,
    val selectedProblem: TroubleshootingProblem? = null,
    val isAutoScanEnabled: Boolean = false,
    val scanProgress: Float = 0f,
    val isScanning: Boolean = false,
    val scanResults: List<ScanResult> = emptyList(),
    val expandedCardId: String? = null
) {
    /** Tab titles / Tab 标题 */
    val tabTitles = listOf(
        "重复加载 / Duplicate Load",
        "状态丢失 / State Lost",
        "边界条件 / Boundary",
        "Room 协同 / Room",
        "PullToRefresh 协同 / PullToRefresh"
    )

    /**
     * Problems for current tab / 当前 Tab 的问题列表
     */
    val currentTabProblems: List<TroubleshootingProblem>
        get() = when (selectedTabIndex) {
            0 -> duplicateLoadProblems
            1 -> stateLostProblems
            2 -> boundaryProblems
            3 -> roomProblems
            4 -> pullToRefreshProblems
            else -> emptyList()
        }

    companion object {
        /** Initial state / 初始状态 */
        val Initial = PagingTroubleshootingState()

        /** Duplicate load problems / 重复加载问题 */
        val duplicateLoadProblems = listOf(
            TroubleshootingProblem(
                id = "dup_1",
                title = "Duplicate page load",
                titleZh = "重复加载同一页",
                symptoms = listOf("Same page loaded multiple times", "Network requests duplicated", "Items appear twice"),
                symptomsZh = listOf("同一页被多次加载", "网络请求重复", "Item 出现两次"),
                possibleCauses = listOf(
                    "PagingSource returning same key",
                    "Key not properly set in PagingSource",
                    "Collector receiving same PagingData multiple times"
                ),
                possibleCausesZh = listOf(
                    "PagingSource 返回相同的 key",
                    "PagingSource 中的 key 设置不正确",
                    "Collector 多次接收相同的 PagingData"
                ),
                solutions = listOf(
                    "Ensure PagingSource returns correct key for each page",
                    "Use LoadParams.key properly in paging source",
                    "Don't share PagingData instance between collectors"
                ),
                solutionsZh = listOf(
                    "确保 PagingSource 为每个页面返回正确的 key",
                    "在 paging source 中正确使用 LoadParams.key",
                    "不要在 collectors 之间共享 PagingData 实例"
                )
            )
        )

        /** State lost problems / 状态丢失问题 */
        val stateLostProblems = listOf(
            TroubleshootingProblem(
                id = "state_1",
                title = "State lost after configuration change",
                titleZh = "Configuration Change 后状态丢失",
                symptoms = listOf("Data disappears after rotation", "State reset to initial values", "Scroll position lost"),
                symptomsZh = listOf("旋转后数据消失", "状态重置为初始值", "滚动位置丢失"),
                possibleCauses = listOf(
                    "asState Flow is not properly remembered",
                    "ViewModel not saving paging state",
                    "Cold flow resets on configuration change"
                ),
                possibleCausesZh = listOf(
                    "asState Flow 未正确使用 remember",
                    "ViewModel 未保存分页状态",
                    "Cold flow 在 configuration change 时重置"
                ),
                solutions = listOf(
                    "Use remember + rememberUpdatedState for asState collection",
                    "Save and restore paging state in ViewModel",
                    "Consider using SavedStateHandle with Paging"
                ),
                solutionsZh = listOf(
                    "使用 remember + rememberUpdatedState 来收集 asState",
                    "在 ViewModel 中保存和恢复分页状态",
                    "考虑使用 SavedStateHandle 配合 Paging"
                )
            )
        )

        /** Boundary problems / 边界条件问题 */
        val boundaryProblems = listOf(
            TroubleshootingProblem(
                id = "boundary_1",
                title = "Incorrect boundary condition handling",
                titleZh = "边界条件判断错误",
                symptoms = listOf("Extra empty page at end", "Can't load more items", "LoadState stuck at Loading"),
                symptomsZh = listOf("末尾出现多余的空页", "无法加载更多 Item", "LoadState 卡在 Loading"),
                possibleCauses = listOf(
                    "Incorrect getRefreshKey() implementation",
                    "Not checking for last page properly",
                    "Missing append null check"
                ),
                possibleCausesZh = listOf(
                    "getRefreshKey() 实现不正确",
                    "没有正确检查是否最后一页",
                    "缺少 append null 检查"
                ),
                solutions = listOf(
                    "Implement getRefreshKey() correctly based on LoadParams",
                    "Check for last page and return null to stop loading",
                    "Handle append/retry LoadState properly"
                ),
                solutionsZh = listOf(
                    "根据 LoadParams 正确实现 getRefreshKey()",
                    "检查是否最后一页，返回 null 停止加载",
                    "正确处理 append/retry LoadState"
                )
            )
        )

        /** Room integration problems / Room 协同问题 */
        val roomProblems = listOf(
            TroubleshootingProblem(
                id = "room_1",
                title = "Room + Paging 3 integration issues",
                titleZh = "Room + Paging 3 协同问题",
                symptoms = listOf("Room data not updating", "PagingSource invalidation not working", "Stale data displayed"),
                symptomsZh = listOf("Room 数据不更新", "PagingSource 失效不工作", "显示过期数据"),
                possibleCauses = listOf(
                    "PagingSourceFactory not properly invalidating",
                    "Room transactions not on correct thread",
                    "Cache invalidation not triggering"
                ),
                possibleCausesZh = listOf(
                    "PagingSourceFactory 未正确失效",
                    "Room 事务未在正确线程执行",
                    "缓存失效未触发"
                ),
                solutions = listOf(
                    "Use suspend functions and proper coroutine dispatchers",
                    "Implement correct invalidation in PagingSourceFactory",
                    "Ensure Room database runs on IO dispatcher"
                ),
                solutionsZh = listOf(
                    "使用 suspend 函数和正确的协程调度器",
                    "在 PagingSourceFactory 中实现正确的失效逻辑",
                    "确保 Room 数据库在 IO dispatcher 上运行"
                )
            )
        )

        /** PullToRefresh problems / PullToRefresh 协同问题 */
        val pullToRefreshProblems = listOf(
            TroubleshootingProblem(
                id = "pr_1",
                title = "PullToRefresh + Paging conflicts",
                titleZh = "PullToRefresh 与 Paging 冲突",
                symptoms = listOf("PullToRefresh not working", "Refresh indicator stuck", "Conflicting scroll states"),
                symptomsZh = listOf("PullToRefresh 不工作", "刷新指示器卡住", "滚动状态冲突"),
                possibleCauses = listOf(
                    "asState and PullToRefresh state management conflict",
                    "LazyColumn scroll state interfering",
                    "RefreshIndicator state not properly handled"
                ),
                possibleCausesZh = listOf(
                    "asState 和 PullToRefresh 状态管理冲突",
                    "LazyColumn 滚动状态干扰",
                    "RefreshIndicator 状态未正确处理"
                ),
                solutions = listOf(
                    "Use PullToRefreshBox from Material 3",
                    "Properly handle refreshLoadState with asState",
                    "Separate refresh state from paging LoadState"
                ),
                solutionsZh = listOf(
                    "使用 Material 3 的 PullToRefreshBox",
                    "正确处理 asState 的 refreshLoadState",
                    "将刷新状态与分页 LoadState 分离"
                )
            )
        )
    }
}

// =============================================================
// ScanResult — 扫描结果
// =============================================================
/**
 * Scan Result / 扫描结果
 *
 * @param filePath File path where issue was found / 发现问题的文件路径
 * @param lineNumber Line number / 行号
 * @param issueDescription Issue description / 问题描述
 * @param issueDescriptionZh Chinese issue description / 中文问题描述
 * @param severity Severity level / 严重程度
 */
data class ScanResult(
    val filePath: String,
    val lineNumber: Int,
    val issueDescription: String,
    val issueDescriptionZh: String,
    val severity: CompatibilityLevel
)

// =============================================================
// PagingTroubleshootingIntent — 踩坑排查工具用户意图
// =============================================================
/**
 * Paging Troubleshooting Tool User Intents / Paging 踩坑排查工具用户意图
 */
sealed interface PagingTroubleshootingIntent {
    /** Select tab / 选择 Tab
     * @param index Tab index / Tab 索引
     */
    data class SelectTab(val index: Int) : PagingTroubleshootingIntent

    /** Toggle auto scan / 切换自动扫描
     * @param enabled Whether auto scan is enabled / 是否启用自动扫描
     */
    data class ToggleAutoScan(val enabled: Boolean) : PagingTroubleshootingIntent

    /** Start scan / 开始扫描
     */
    data object StartScan : PagingTroubleshootingIntent

    /** Expand card / 展开卡片
     * @param cardId Card ID to expand / 要展开的卡片 ID
     */
    data class ExpandCard(val cardId: String) : PagingTroubleshootingIntent

    /** Collapse card / 收起卡片
     */
    data object CollapseCard : PagingTroubleshootingIntent

    /** Select problem / 选择问题
     * @param problem Problem to select / 要选择的问题
     */
    data class SelectProblem(val problem: TroubleshootingProblem) : PagingTroubleshootingIntent
}

// =============================================================
// PagingTroubleshootingEffect — 踩坑排查工具副作用
// =============================================================
/**
 * Paging Troubleshooting Tool Side Effects / Paging 踩坑排查工具副作用
 */
sealed interface PagingTroubleshootingEffect {
    /** Show scan complete bottom sheet / 显示扫描完成 BottomSheet
     * @param results Scan results / 扫描结果
     */
    data class ShowScanCompleteBottomSheet(val results: List<ScanResult>) : PagingTroubleshootingEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : PagingTroubleshootingEffect
}
