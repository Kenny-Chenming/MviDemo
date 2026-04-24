package com.mvi.kenny.feature.navevent

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// =============================================================
// NavEventKitContract — Navigation Event KMP 迁移工具包 MVI 契约
// PRD-140: Navigation Event KMP 库迁移检测与 PredictiveBackHandler 废弃替代工具包
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see NavEventKitViewModel State management
// @see NavEventKitScreen Main screen

// =============================================================
// ScanPhase — 扫描阶段枚举
// =============================================================
/**
 * Scan phase status / 扫描阶段状态
 *
 * Represents the current phase of the migration scan process.
 */
sealed class ScanPhase {
    /** Idle state / 空闲状态 */
    data object Idle : ScanPhase()
    /** Scanning for PredictiveBackHandler usages / 扫描 PredictiveBackHandler 使用中 */
    data object Scanning : ScanPhase()
    /** Analyzing migration complexity / 分析迁移复杂度中 */
    data object Analyzing : ScanPhase()
    /** Generating migration diffs / 生成迁移差异中 */
    data object GeneratingDiffs : ScanPhase()
    /** Applying Gradle configuration / 应用 Gradle 配置中 */
    data class ApplyingConfig(val message: String = "") : ScanPhase()
    /** Scan complete / 扫描完成 */
    data object Done : ScanPhase()
    /** Error occurred / 发生错误 */
    data class Error(val message: String) : ScanPhase()
}

// =============================================================
// MigrationComplexity — 迁移复杂度评级
// =============================================================
/**
 * Migration complexity rating / 迁移复杂度评级
 *
 * @param label Display label / 显示标签
 * @param priority Priority number for sorting / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class MigrationComplexity(val label: String, val priority: Int, val color: Color) {
    /** Simple — direct 1:1 replacement / 简单 — 直接 1:1 替换 */
    SIMPLE("Simple / 简单", 0, Color(0xFF66BB6A)),
    /** Medium — requires some refactoring / 中等 — 需要一些重构 */
    MEDIUM("Medium / 中等", 1, Color(0xFFFFA726)),
    /** Complex — significant architectural changes needed / 复杂 — 需要重大架构更改 */
    COMPLEX("Complex / 复杂", 2, Color(0xFFEF5350)),
    /** Manual — requires manual review and intervention / 手动 — 需要手动审查和干预 */
    MANUAL("Manual / 手动", 3, Color(0xFF42A5F5))
}

// =============================================================
// TargetPlatform — 目标平台
// =============================================================
/**
 * Target platform for migration / 迁移目标平台
 */
enum class TargetPlatform(val label: String, val displayName: String) {
    ANDROID("Android", "Android"),
    IOS("iOS", "iOS"),
    DESKTOP("Desktop", "Desktop"),
    JS("JS/Web", "JavaScript/Web")
}

// =============================================================
// WizardStep — 迁移向导步骤
// =============================================================
/**
 * Migration wizard step / 迁移向导步骤
 */
enum class WizardStep {
    /** Select scan scope / 选择扫描范围 */
    SELECT_SCOPE,
    /** Select usages to migrate / 选择要迁移的用法 */
    SELECT_USAGES,
    /** Select target platforms / 选择目标平台 */
    SELECT_PLATFORMS,
    /** Preview migration diff / 预览迁移差异 */
    PREVIEW_DIFF,
    /** Confirm migration / 确认迁移 */
    CONFIRM,
    /** Applying migration / 应用迁移中 */
    APPLYING,
    /** Migration complete / 迁移完成 */
    DONE
}

// =============================================================
// PredictiveBackHandlerUsage — PredictiveBackHandler 使用点
// =============================================================
/**
 * PredictiveBackHandler usage location / PredictiveBackHandler 使用位置
 *
 * Represents a single location in the codebase where PredictiveBackHandler
 * is being used and needs migration.
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param methodName Method name containing the call / 包含该调用的方法名
 * @param className Class name containing the call / 包含该调用的类名
 * @param callChain Full call chain / 完整调用链
 * @param complexity Migration complexity rating / 迁移复杂度评级
 * @param migrationSuggestion Suggested migration approach / 迁移建议
 * @param isMigrated Whether this usage has been migrated / 是否已迁移
 */
data class PredictiveBackHandlerUsage(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val methodName: String,
    val className: String,
    val callChain: String,
    val complexity: MigrationComplexity,
    val migrationSuggestion: String,
    val isMigrated: Boolean = false
)

// =============================================================
// DispatcherNode — Dispatcher 树节点
// =============================================================
/**
 * Dispatcher tree node / Dispatcher 树节点
 *
 * Represents a node in the hierarchical NavigationEventDispatcher tree.
 *
 * @param id Unique identifier / 唯一标识符
 * @param name Display name / 显示名称
 * @param type Node type (Root/NavHost/Screen/Tab) / 节点类型
 * @param parentId Parent node ID / 父节点 ID
 * @param children Child nodes / 子节点
 * @param isActive Whether this dispatcher is currently active / 是否当前活跃
 * @param depth Tree depth level / 树深度级别
 */
data class DispatcherNode(
    val id: String,
    val name: String,
    val type: DispatcherType,
    val parentId: String? = null,
    val children: List<DispatcherNode> = emptyList(),
    val isActive: Boolean = false,
    val depth: Int = 0
)

/**
 * Dispatcher node type / Dispatcher 节点类型
 */
enum class DispatcherType(val label: String, val color: Color) {
    ROOT("RootDispatcher", Color(0xFF1565C0)),
    NAV_HOST("NavHostDispatcher", Color(0xFF7B1FA2)),
    SCREEN("ScreenDispatcher", Color(0xFF00897B)),
    TAB("TabDispatcher", Color(0xFFFFA726))
}

// =============================================================
// EventTrace — 事件追踪记录
// =============================================================
/**
 * Event trace record / 事件追踪记录
 *
 * Represents a single navigation event and its propagation path.
 *
 * @param eventId Unique event identifier / 唯一事件标识符
 * @param eventType Event type (BackGesture, PredictiveBack, etc.) / 事件类型
 * @param timestamp Event timestamp / 事件时间戳
 * @param path Propagation path through dispatchers / 传播路径
 */
data class EventTrace(
    val eventId: String,
    val eventType: String,
    val timestamp: Long,
    val path: List<String> // Dispatcher IDs in propagation order
)

// =============================================================
// ExportFormat — 导出格式
// =============================================================
/**
 * Export format for dispatcher tree / Dispatcher 树导出格式
 */
enum class ExportFormat(val label: String, val extension: String) {
    JSON("JSON", "json"),
    GRAPHML("GraphML", "graphml"),
    MARKDOWN("Markdown", "md")
}

// =============================================================
// PlatformBehavior — 各平台行为差异
// =============================================================
/**
 * Platform behavior difference / 各平台行为差异
 *
 * @param feature Feature name / 功能名称
 * @param androidBehavior Behavior on Android / Android 平台行为
 * @param iosBehavior Behavior on iOS / iOS 平台行为
 * @param desktopBehavior Behavior on Desktop / Desktop 平台行为
 * @param jsBehavior Behavior on JavaScript/Web / JS/Web 平台行为
 * @param hasDifference Whether behavior differs across platforms / 行为是否跨平台不同
 */
data class PlatformBehavior(
    val feature: String,
    val androidBehavior: String,
    val iosBehavior: String,
    val desktopBehavior: String,
    val jsBehavior: String,
    val hasDifference: Boolean
)

// =============================================================
// NavEventKitState — 页面状态
// =============================================================
/**
 * Navigation Event Kit State / Navigation Event 工具包页面状态
 *
 * Single source of truth for the entire Navigation Event Kit UI.
 *
 * @param activeTab Currently active tool tab / 当前活跃的工具 Tab
 * @param scanPhase Current scan phase / 当前扫描阶段
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scannedFiles Number of files scanned / 已扫描文件数
 * @param usages List of PredictiveBackHandler usages found / 发现的 PredictiveBackHandler 使用列表
 * @param complexityMatrix Complexity matrix by file / 按文件分的复杂度矩阵
 * @param selectedUsage Selected usage for detail view / 选中的使用详情
 * @param wizardStep Current wizard step / 当前向导步骤
 * @param selectedUsages Set of selected usage IDs for migration / 已选择的迁移用法 ID 集合
 * @param selectedPlatforms Set of selected target platforms / 已选择的目标平台集合
 * @param previewDiffs Map of file paths to their diff content / 文件路径到差异内容的映射
 * @param treeRoot Root of dispatcher tree / Dispatcher 树根节点
 * @param expandedNodes Set of expanded node IDs / 已展开的节点 ID 集合
 * @param selectedNode Currently selected dispatcher node / 当前选中的 dispatcher 节点
 * @param eventHistory List of event traces / 事件追踪历史
 * @param filterPlatform Platform filter for dispatcher tree / Dispatcher 树平台过滤器
 * @param isConnected Whether debugger is connected / 调试器是否连接
 * @param isApplying Whether migration is being applied / 是否正在应用迁移
 * @param error Error message if any / 错误信息
 */
data class NavEventKitState(
    val activeTab: NavEventTab = NavEventTab.MIGRATION_REPORT,
    val scanPhase: ScanPhase = ScanPhase.Idle,
    val scanProgress: Float = 0f,
    val scannedFiles: Int = 0,
    val usages: List<PredictiveBackHandlerUsage> = emptyList(),
    val complexityMatrix: Map<String, MigrationComplexity> = emptyMap(),
    val selectedUsage: PredictiveBackHandlerUsage? = null,
    val wizardStep: WizardStep = WizardStep.SELECT_SCOPE,
    val selectedUsages: Set<String> = emptySet(),
    val selectedPlatforms: Set<TargetPlatform> = setOf(TargetPlatform.ANDROID),
    val previewDiffs: Map<String, String> = emptyMap(),
    val treeRoot: DispatcherNode? = null,
    val expandedNodes: Set<String> = emptySet(),
    val selectedNode: DispatcherNode? = null,
    val eventHistory: List<EventTrace> = emptyList(),
    val filterPlatform: TargetPlatform? = null,
    val isConnected: Boolean = false,
    val isApplying: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = NavEventKitState()
    }

    /**
     * Sorted usages by complexity / 按复杂度排序的用法
     */
    val sortedUsages: List<PredictiveBackHandlerUsage>
        get() = usages.sortedBy { it.complexity.priority }

    /**
     * Count of usages by complexity / 按复杂度统计的用法数
     */
    val complexityCounts: Map<MigrationComplexity, Int>
        get() = usages.groupingBy { it.complexity }.eachCount()

    /**
     * Scan completion percentage / 扫描完成百分比
     */
    val scanPercentage: Int
        get() = (scanProgress * 100).toInt()

    /**
     * Selected usages count / 已选择用法数
     */
    val selectedCount: Int
        get() = selectedUsages.size

    /**
     * Has any usages requiring manual review / 是否有需要手动审查的用法
     */
    val hasManualReview: Boolean
        get() = usages.any { it.complexity == MigrationComplexity.MANUAL }
}

// =============================================================
// NavEventTab — 工具 Tab
// =============================================================
/**
 * Tool module tab / 功能模块 Tab
 */
enum class NavEventTab(
    val title: String,
    val description: String
) {
    /** Migration report tab / 迁移报告 Tab */
    MIGRATION_REPORT("Migration Report", "Scan & Report"),
    /** File diff tab / 文件差异 Tab */
    FILE_DIFF("File Diff", "View Diffs"),
    /** Dispatcher tree tab / Dispatcher 树 Tab */
    DISPATCHER_TREE("Dispatcher Tree", "Hierarchical View"),
    /** Platform behavior tab / 平台行为 Tab */
    PLATFORM_BEHAVIOR("Platform Behavior", "Cross-Platform")
}

// =============================================================
// NavEventKitIntent — 用户意图
// =============================================================
/**
 * Navigation Event Kit User Intents / Navigation Event 工具包用户意图
 */
sealed interface NavEventKitIntent {
    /** Select a tool tab / 选择工具 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: NavEventTab) : NavEventKitIntent

    /** Start migration scan / 开始迁移扫描
     * @param modulePath Module or project path to scan / 要扫描的模块或项目路径
     */
    data class StartScan(val modulePath: String) : NavEventKitIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : NavEventKitIntent

    /** Select a usage to view detail / 选择用法查看详情
     * @param usage Usage to select / 要选择的用法
     */
    data class SelectUsage(val usage: PredictiveBackHandlerUsage) : NavEventKitIntent

    /** Clear selected usage / 清除选中的用法 */
    data object ClearUsage : NavEventKitIntent

    /** Toggle usage selection for migration / 切换用法的迁移选择状态
     * @param usageId Usage ID to toggle / 要切换的用法 ID
     */
    data class ToggleUsageSelection(val usageId: String) : NavEventKitIntent

    /** Select all usages / 全选所有用法 */
    data object SelectAllUsages : NavEventKitIntent

    /** Deselect all usages / 取消所有用法选择 */
    data object DeselectAllUsages : NavEventKitIntent

    /** Toggle platform selection / 切换平台选择
     * @param platform Platform to toggle / 要切换的平台
     */
    data class TogglePlatform(val platform: TargetPlatform) : NavEventKitIntent

    /** Next wizard step / 下一步向导
     */
    data object NextWizardStep : NavEventKitIntent

    /** Previous wizard step / 上一步向导
     */
    data object PreviousWizardStep : NavEventKitIntent

    /** Apply migration / 应用迁移
     */
    data object ApplyMigration : NavEventKitIntent

    /** Toggle dispatcher tree node expansion / 切换 Dispatcher 树节点展开状态
     * @param nodeId Node ID to toggle / 要切换的节点 ID
     */
    data class ToggleNodeExpand(val nodeId: String) : NavEventKitIntent

    /** Select dispatcher tree node / 选择 Dispatcher 树节点
     * @param node Node to select / 要选择的节点
     */
    data class SelectDispatcherNode(val node: DispatcherNode) : NavEventKitIntent

    /** Connect to debugger / 连接到调试器
     */
    data object ConnectDebugger : NavEventKitIntent

    /** Disconnect from debugger / 断开调试器连接
     */
    data object DisconnectDebugger : NavEventKitIntent

    /** Export dispatcher tree / 导出 Dispatcher 树
     * @param format Export format / 导出格式
     */
    data class ExportTree(val format: ExportFormat) : NavEventKitIntent

    /** Set platform filter / 设置平台过滤器
     * @param platform Platform to filter by / 要过滤的平台
     */
    data class SetPlatformFilter(val platform: TargetPlatform?) : NavEventKitIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : NavEventKitIntent
}

// =============================================================
// NavEventKitEffect — 副作用
// =============================================================
/**
 * Navigation Event Kit Side Effects / Navigation Event 工具包副作用
 */
sealed interface NavEventKitEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : NavEventKitEffect

    /** Export success event / 导出成功事件
     * @param filePath Exported file path / 导出文件路径
     */
    data class ExportSuccess(val filePath: String) : NavEventKitEffect

    /** Scan complete event / 扫描完成事件 */
    data object ScanComplete : NavEventKitEffect

    /** Migration applied event / 迁移应用完成事件 */
    data object MigrationApplied : NavEventKitEffect

    /** Debugger connected event / 调试器连接事件 */
    data object DebuggerConnected : NavEventKitEffect

    /** Debugger disconnected event / 调试器断开连接事件 */
    data object DebuggerDisconnected : NavEventKitEffect
}
