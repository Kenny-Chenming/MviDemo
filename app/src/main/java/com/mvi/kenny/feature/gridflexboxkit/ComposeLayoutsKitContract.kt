package com.mvi.kenny.feature.gridflexboxkit

// ============================================================
// ComposeLayoutsKitContract — ComposeLayoutsKit MVI 契约
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
// ============================================================
/**
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * MVI 三要素：
 * - Model (State): 页面状态的唯一真相来源，Immutable 数据类
 * - View: Composable 函数，消费 State，渲染 UI
 * - Intent: 用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see ComposeLayoutsKitViewModel
 * @see ComposeLayoutsKitScreen
 */

// ============================================================
// 模块信息 / Module Info
// ============================================================
/**
 * 子模块枚举，代表 ComposeLayoutsKit 中的各个工具模块。
 * Sub-module enumeration for ComposeLayoutsKit tools.
 *
 * @param titleCn 中文标题
 * @param titleEn 英文标题
 * @param description 模块描述 / Module description
 * @param priority 优先级标签 / Priority label
 */
enum class LayoutKitModule(
    val titleCn: String,
    val titleEn: String,
    val description: String,
    val priority: String,
    val titleEnFull: String = ""  // Optional full English title / 可选的完整英文标题
) {
    GridDecisionTree(
        titleCn = "Grid/FlexBox 决策树",
        titleEn = "Grid/FlexBox Decision Tree",
        description = "场景化最佳实践指南 — 何时用 Grid vs FlexBox vs LazyGrid vs Column/Row",
        priority = "P0"
    ),
    CssMigrationGuide(
        titleCn = "CSS 迁移手册",
        titleEn = "CSS → Compose Migration Guide",
        description = "Web 开发者平移知识，CSS Grid/Flexbox 到 Compose 的完整属性对照表",
        priority = "P0"
    ),
    PerformanceAnalysis(
        titleCn = "性能分析面板",
        titleEn = "Performance Analysis Panel",
        description = "Grid/FlexBox vs LazyGrid/Column/Row 的重组开销对比基准测试",
        priority = "P1"
    ),
    GridDebugPanel(
        titleCn = "Grid 调试面板",
        titleEn = "Grid Debug Visualization",
        description = "实时可视化 Grid lines/tracks/cells/gaps，辅助理解布局结构",
        priority = "P1"
    ),
    FlexBoxDebugPanel(
        titleCn = "FlexBox 调试面板",
        titleEn = "FlexBox Debug Visualization",
        description = "实时展示 flex grow/shrink/basis 热力图，直观理解 FlexBox 行为",
        priority = "P1"
    ),
    AdaptiveLayout(
        titleCn = "自适应布局模板",
        titleEn = "Adaptive Layout Templates",
        description = "Grid/FlexBox × Adaptive Layout — 小屏单列 / 大屏双列 / 折叠屏动态切换",
        priority = "P1"
    ),
    MigrationScanner(
        titleCn = "迁移扫描器",
        titleEn = "Migration Scanner",
        titleEnFull = "Old Layout → Grid/FlexBox Migration Scanner",
        description = "检测现有 Column/Row/LazyGrid 用法，输出适合迁移到 Grid/FlexBox 的位置和建议",
        priority = "P2"
    ),
    Playground(
        titleCn = "Playground",
        titleEn = "Interactive Playground",
        description = "交互式示例，开发者可在线编辑 Grid/FlexBox 代码并实时预览布局效果",
        priority = "P2"
    )
}

// ============================================================
// State — 页面状态 / Page State
// ============================================================
/**
 * 主页状态（HomeScreen State）
 * Main screen state holding the list of available sub-modules.
 *
 * @param modules 子模块列表 / List of available sub-modules
 * @param selectedModule 当前选中的子模块，null 表示在主页 / Currently selected sub-module, null means on home
 * @param isLoading 模块加载状态 / Module loading state
 */
data class ComposeLayoutsKitState(
    val modules: List<LayoutKitModule> = LayoutKitModule.entries,
    val selectedModule: LayoutKitModule? = null,
    val isLoading: Boolean = false,
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = ComposeLayoutsKitState()
    }
}

// ============================================================
// Intent — 用户意图 / User Intent
// ============================================================
/**
 * 主页用户意图 / Home screen user intent.
 * Every user action on the home screen corresponds to an Intent.
 *
 * @see ComposeLayoutsKitViewModel.sendIntent
 */
sealed interface ComposeLayoutsKitIntent {
    /** 选择子模块进入 / Select a sub-module to enter */
    data class SelectModule(val module: LayoutKitModule) : ComposeLayoutsKitIntent

    /** 从子模块返回主页 / Navigate back to home from sub-module */
    data object NavigateBack : ComposeLayoutsKitIntent
}

// ============================================================
// Effect — 副作用 / Side Effects
// ============================================================
/**
 * 一次性副作用 / One-time side effects.
 * Immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see ComposeLayoutsKitViewModel.effect
 */
sealed interface ComposeLayoutsKitEffect {
    /** 显示 Toast 消息 / Show a toast message */
    data class ShowToast(val message: String) : ComposeLayoutsKitEffect

    /** 导航到子模块（通过状态切换实现）Navigation to sub-module is handled by state update */
}

// ============================================================
// GridDecisionTree State & Intent & Effect
// ============================================================
/**
 * 布局类型枚举 / Layout type enumeration
 * Represents different Compose layout types for decision making.
 *
 * @param displayNameCn 中文显示名
 * @param displayNameEn 英文显示名
 * @param description 适用场景描述
 */
enum class LayoutType(
    val displayNameCn: String,
    val displayNameEn: String,
    val description: String
) {
    Grid(
        displayNameCn = "Grid（网格）",
        displayNameEn = "Grid",
        description = "二维精确控制，行列同时布局。适用于仪表盘/相册/棋盘式布局"
    ),
    FlexBox(
        displayNameCn = "FlexBox（弹性盒）",
        displayNameEn = "FlexBox",
        description = "单维高性能流动布局，支持 grow/shrink/basis。适用于标签栏/响应式列表/自适应表单"
    ),
    LazyGrid(
        displayNameCn = "LazyGrid（虚拟化网格）",
        displayNameEn = "LazyGrid",
        description = "虚拟化长列表网格，适合大数据集。适用于列表展示类布局"
    ),
    ColumnRow(
        displayNameCn = "Column/Row（线性布局）",
        displayNameEn = "Column/Row",
        description = "基础流式布局，适合简单线性排列"
    )
}

/**
 * 决策树节点 / Decision tree node
 *
 * @param id 节点 ID / Node ID
 * @param question 问题文本 / Question text
 * @param options 选项列表 / List of options
 */
data class DecisionNode(
    val id: String,
    val question: String,
    val options: List<DecisionOption>
)

/**
 * 决策选项 / Decision option
 *
 * @param label 选项标签 / Option label
 * @param nextNodeId 下一节点 ID，null 表示到达叶子节点 / Next node ID, null means leaf node
 * @param recommendedLayout 推荐的布局类型（叶子节点） / Recommended layout type (leaf node)
 */
data class DecisionOption(
    val label: String,
    val nextNodeId: String?,
    val recommendedLayout: LayoutType? = null
)

/**
 * 代码分析结果 / Code analysis result
 *
 * @param detectedLayout 当前检测到的布局类型 / Detected layout type
 * @param suggestion 迁移建议 / Migration suggestion
 * @param confidence 置信度 / Confidence score (0.0 ~ 1.0)
 */
data class CodeAnalysisResult(
    val detectedLayout: LayoutType,
    val suggestion: String,
    val confidence: Float
)

/**
 * GridDecisionTreeScreen 状态 / GridDecisionTree screen state
 *
 * @param currentPath 当前决策路径 / Current decision path
 * @param currentNodeId 当前节点 ID / Current node ID
 * @param recommendedLayout 推荐的布局类型 / Recommended layout type
 * @param analysisResult 代码反查结果 / Code analysis result
 * @param codeInput 用户输入的代码片段 / User input code snippet
 * @param selectedLayoutsForComparison 性能对比选中的布局类型 / Layouts selected for performance comparison
 */
data class GridDecisionTreeState(
    val currentPath: List<String> = emptyList(),
    val currentNodeId: String = "root",
    val recommendedLayout: LayoutType? = null,
    val analysisResult: CodeAnalysisResult? = null,
    val codeInput: String = "",
    val selectedLayoutsForComparison: Set<LayoutType> = emptySet(),
) {
    companion object {
        val Initial = GridDecisionTreeState()
    }
}

/** GridDecisionTree 用户意图 / GridDecisionTree user intent */
sealed interface GridDecisionTreeIntent {
    data class SelectOption(val option: DecisionOption) : GridDecisionTreeIntent
    data object ResetTree : GridDecisionTreeIntent
    data class AnalyzeCode(val code: String) : GridDecisionTreeIntent
    data object NavigateToPlayground : GridDecisionTreeIntent
}

/** GridDecisionTree 副作用 / GridDecisionTree side effects */
sealed interface GridDecisionTreeEffect {
    data class ShowRecommendation(val layout: LayoutType) : GridDecisionTreeEffect
    data class NavigateToPlayground(val templateId: String) : GridDecisionTreeEffect
}

// ============================================================
// Playground State & Intent & Effect
// ============================================================
/**
 * Grid 参数 / Grid parameters for playground
 *
 * @param columns 列数 / Number of columns
 * @param rows 行数 / Number of rows
 * @param columnGap 列间距 / Column gap
 * @param rowGap 行间距 / Row gap
 */
data class GridParams(
    val columns: Int = 3,
    val rows: Int = 2,
    val columnGap: Int = 8,
    val rowGap: Int = 8,
)

/**
 * FlexBox 参数 / FlexBox parameters for playground
 *
 * @param direction 方向：Row / Column / Row-reverse / Column-reverse
 * @param flexGrow flexGrow 值列表 / List of flexGrow values
 * @param flexShrink flexShrink 值列表 / List of flexShrink values
 * @param flexBasis flexBasis 值列表 / List of flexBasis values (Dp)
 * @param mainAxisSpacing 主轴间距 / Main axis spacing
 * @param crossAxisSpacing 交叉轴间距 / Cross axis spacing
 */
data class FlexBoxParams(
    val direction: FlexDirection = FlexDirection.Row,
    val flexGrow: List<Float> = listOf(1f, 1f, 1f),
    val flexShrink: List<Float> = listOf(1f, 1f, 1f),
    val flexBasis: List<Int> = listOf(0, 0, 0), // 0 means auto
    val mainAxisSpacing: Int = 8,
    val crossAxisSpacing: Int = 8,
)

/** FlexBox 方向 / FlexBox direction */
enum class FlexDirection(val displayName: String) {
    Row("Row (左→右)"),
    Column("Column (上→下)"),
    RowReverse("Row Reverse (右→左)"),
    ColumnReverse("Column Reverse (下→上)")
}

/**
 * Playground 预设模板 / Playground preset template
 *
 * @param id 模板 ID / Template ID
 * @param nameCn 中文名称
 * @param nameEn 英文名称
 * @param description 模板描述
 * @param gridParams Grid 参数 / Grid parameters
 * @param flexBoxParams FlexBox 参数 / FlexBox parameters
 * @param itemCount 子项数量 / Number of items
 */
data class PlaygroundTemplate(
    val id: String,
    val nameCn: String,
    val nameEn: String,
    val description: String,
    val gridParams: GridParams? = null,
    val flexBoxParams: FlexBoxParams? = null,
    val itemCount: Int = 6,
    val layoutType: LayoutType = LayoutType.Grid
)

/**
 * PlaygroundScreen 状态 / Playground screen state
 *
 * @param code 当前代码 / Current code
 * @param gridParams Grid 参数 / Grid parameters
 * @param flexBoxParams FlexBox 参数 / FlexBox parameters
 * @param selectedTemplate 当前选中的模板 / Currently selected template
 * @param templates 可用模板列表 / Available templates
 * @param previewScale 预览缩放比例 / Preview scale
 * @param isLivePreviewEnabled 是否启用实时预览 / Live preview enabled
 * @param previewError 预览错误信息 / Preview error message
 * @param activeTab 当前激活的 Tab：Grid / FlexBox
 */
data class PlaygroundState(
    val code: String = "",
    val gridParams: GridParams = GridParams(),
    val flexBoxParams: FlexBoxParams = FlexBoxParams(),
    val selectedTemplate: PlaygroundTemplate? = null,
    val templates: List<PlaygroundTemplate> = emptyList(),
    val previewScale: Float = 1f,
    val isLivePreviewEnabled: Boolean = true,
    val previewError: String? = null,
    val activeTab: PlaygroundTab = PlaygroundTab.Grid,
) {
    companion object {
        val Initial = PlaygroundState()
    }
}

/** Playground Tab / Playground Tab */
enum class PlaygroundTab(val displayName: String) {
    Grid("Grid"),
    FlexBox("FlexBox")
}

/** Playground 用户意图 / Playground user intent */
sealed interface PlaygroundIntent {
    data class UpdateCode(val code: String) : PlaygroundIntent
    data class UpdateGridParams(val params: GridParams) : PlaygroundIntent
    data class UpdateFlexParams(val params: FlexBoxParams) : PlaygroundIntent
    data class LoadTemplate(val templateId: String) : PlaygroundIntent
    data class SelectTab(val tab: PlaygroundTab) : PlaygroundIntent
    data class UpdatePreviewScale(val scale: Float) : PlaygroundIntent
    data object ToggleLivePreview : PlaygroundIntent
    data object ClearPreviewError : PlaygroundIntent
}

/** Playground 副作用 / Playground side effects */
sealed interface PlaygroundEffect {
    data class ShowPreviewError(val message: String) : PlaygroundEffect
    data object ShowSaveSuccess : PlaygroundEffect
}

// ============================================================
// PerformanceAnalysis State & Intent & Effect
// ============================================================
/**
 * 布局性能测试结果 / Layout performance test result
 *
 * @param layoutType 布局类型 / Layout type
 * @param recompositionCount 重组次数 / Recomposition count
 * @param renderTimeMs 渲染耗时（毫秒） / Render time in milliseconds
 * @param memoryUsageMb 内存占用（MB） / Memory usage in MB
 * @param deviceInfo 测试设备信息 / Test device info
 * @param timestamp 测试时间戳 / Test timestamp
 */
data class LayoutPerformanceResult(
    val layoutType: LayoutType,
    val recompositionCount: Int,
    val renderTimeMs: Long,
    val memoryUsageMb: Float,
    val deviceInfo: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * PerformanceAnalysisScreen 状态 / Performance analysis screen state
 *
 * @param testResults 测试结果列表 / Test results list
 * @param selectedLayouts 选中进行对比的布局类型 / Layouts selected for comparison
 * @param isRunning 是否正在运行测试 / Whether test is running
 * @param benchmarkProgress 测试进度（0.0 ~ 1.0） / Benchmark progress
 * @param selectedMetric 选中的性能指标 / Selected performance metric
 */
data class PerformanceAnalysisState(
    val testResults: List<LayoutPerformanceResult> = emptyList(),
    val selectedLayouts: Set<LayoutType> = setOf(LayoutType.Grid, LayoutType.FlexBox, LayoutType.LazyGrid, LayoutType.ColumnRow),
    val isRunning: Boolean = false,
    val benchmarkProgress: Float = 0f,
    val selectedMetric: PerformanceMetric = PerformanceMetric.RecompositionCount,
) {
    companion object {
        val Initial = PerformanceAnalysisState()
    }
}

/** 性能指标枚举 / Performance metric enumeration */
enum class PerformanceMetric(val displayNameCn: String, val displayNameEn: String) {
    RecompositionCount("重组次数", "Recomposition Count"),
    RenderTime("渲染耗时", "Render Time"),
    MemoryUsage("内存占用", "Memory Usage")
}

/** PerformanceAnalysis 用户意图 / PerformanceAnalysis user intent */
sealed interface PerformanceAnalysisIntent {
    data object RunBenchmark : PerformanceAnalysisIntent
    data class SelectLayouts(val layouts: Set<LayoutType>) : PerformanceAnalysisIntent
    data class SelectMetric(val metric: PerformanceMetric) : PerformanceAnalysisIntent
    data object ClearResults : PerformanceAnalysisIntent
}

/** PerformanceAnalysis 副作用 / PerformanceAnalysis side effects */
sealed interface PerformanceAnalysisEffect {
    data class ShowBenchmarkProgress(val progress: Float) : PerformanceAnalysisEffect
    data class ShowBenchmarkResult(val result: LayoutPerformanceResult) : PerformanceAnalysisEffect
}

// ============================================================
// MigrationScanner State & Intent & Effect
// ============================================================
/**
 * 迁移扫描结果 / Migration scan result
 *
 * @param filePath 文件路径 / File path
 * @param lineNumber 行号 / Line number
 * @param detectedLayout 检测到的布局类型 / Detected layout type
 * @param suggestedMigration 建议的迁移目标 / Suggested migration target
 * @param confidence 置信度 / Confidence
 * @param codeSnippet 代码片段 / Code snippet
 */
data class MigrationScanResult(
    val filePath: String,
    val lineNumber: Int,
    val detectedLayout: LayoutType,
    val suggestedMigration: LayoutType,
    val confidence: Float,
    val codeSnippet: String
)

/**
 * MigrationScannerScreen 状态 / Migration scanner screen state
 *
 * @param scanResults 扫描结果列表 / Scan results
 * @param isScanning 是否正在扫描 / Whether scanning
 * @param scanProgress 扫描进度 / Scan progress
 * @param targetDirectory 目标目录 / Target directory
 * @param filterLayoutType 按布局类型过滤 / Filter by layout type
 */
data class MigrationScannerState(
    val scanResults: List<MigrationScanResult> = emptyList(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val targetDirectory: String = "",
    val filterLayoutType: LayoutType? = null,
) {
    companion object {
        val Initial = MigrationScannerState()
    }
}

/** MigrationScanner 用户意图 / MigrationScanner user intent */
sealed interface MigrationScannerIntent {
    data class UpdateTargetDirectory(val path: String) : MigrationScannerIntent
    data object StartScan : MigrationScannerIntent
    data class FilterByLayoutType(val layoutType: LayoutType?) : MigrationScannerIntent
    data object ExportReport : MigrationScannerIntent
}

/** MigrationScanner 副作用 / MigrationScanner side effects */
sealed interface MigrationScannerEffect {
    data class ShowScanComplete(val count: Int) : MigrationScannerEffect
    data class ShowExportSuccess(val path: String) : MigrationScannerEffect
    data class ShowError(val message: String) : MigrationScannerEffect
}
