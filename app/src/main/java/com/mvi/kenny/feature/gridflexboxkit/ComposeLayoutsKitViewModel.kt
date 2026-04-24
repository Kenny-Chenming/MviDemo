package com.mvi.kenny.feature.gridflexboxkit

// ============================================================
// ComposeLayoutsKitViewModel — 主模块 ViewModel
// PRD-141 | Compose Grid + FlexBox 双布局 API 开发工具包
// ============================================================
/**
 * 继承 ViewModel，持有 ComposeLayoutsKitState（页面状态）和 ComposeLayoutsKitEffect（副作用）。
 *
 * 状态管理：
 * - _state: 私有 MutableStateFlow，ViewModel 内部写入
 * - state: 公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * - _effect: Channel（热流），缓冲区大小 BUFFERED
 * - effect: receiveAsFlow，UI 层通过 collect{} 监听
 *
 * @see ComposeLayoutsKitState
 * @see ComposeLayoutsKitIntent
 * @see ComposeLayoutsKitEffect
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ComposeLayoutsKitViewModel : ViewModel() {

    /** 页面状态（StateFlow，UI 只读） / Page state (StateFlow, UI read-only) */
    private val _state = MutableStateFlow(ComposeLayoutsKitState.Initial)
    val state: StateFlow<ComposeLayoutsKitState> = _state.asStateFlow()

    /** 当前状态快照 / Current state snapshot */
    val currentState: ComposeLayoutsKitState get() = _state.value

    /** 副作用 Channel / Effect Channel */
    private val _effect = Channel<ComposeLayoutsKitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * 接收并处理用户意图 / Receive and handle user intent
     *
     * @param intent 用户意图 / User intent
     */
    fun sendIntent(intent: ComposeLayoutsKitIntent) {
        when (intent) {
            is ComposeLayoutsKitIntent.SelectModule -> selectModule(intent.module)
            is ComposeLayoutsKitIntent.NavigateBack -> navigateBack()
        }
    }

    /**
     * 选择子模块 / Select a sub-module
     *
     * @param module 选中的子模块 / Selected module
     */
    private fun selectModule(module: LayoutKitModule) {
        _state.value = _state.value.copy(selectedModule = module, isLoading = false)
        viewModelScope.launch {
            _effect.send(ComposeLayoutsKitEffect.ShowToast("进入 ${module.titleCn}"))
        }
    }

    /**
     * 返回主页 / Navigate back to home
     */
    private fun navigateBack() {
        _state.value = _state.value.copy(selectedModule = null)
    }
}

// ============================================================
// GridDecisionTreeViewModel — 决策树 ViewModel
// ============================================================
/**
 * Grid/FlexBox 决策树 ViewModel
 * Manages the decision tree for choosing between Grid/FlexBox/LazyGrid/Column/Row layouts.
 *
 * Decision tree structure:
 * Root → [静态/虚拟化] → [1D/2D] → [对齐需求] → [空间分配] → LayoutType
 *
 * @see GridDecisionTreeState
 * @see GridDecisionTreeIntent
 * @see GridDecisionTreeEffect
 */
class GridDecisionTreeViewModel : ViewModel() {

    private val _state = MutableStateFlow(GridDecisionTreeState.Initial)
    val state: StateFlow<GridDecisionTreeState> = _state.asStateFlow()

    private val _effect = Channel<GridDecisionTreeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    /**
     * 决策树节点映射 / Decision tree node map
     * Maps node ID to DecisionNode.
     *
     * 决策树结构：
     * root → scale → dimension → alignment → spaceAllocation → LayoutType
     */
    private val decisionTree: Map<String, DecisionNode> = mapOf(
        "root" to DecisionNode(
            id = "root",
            question = "📊 数据规模是多少？/ What is the data scale?",
            options = listOf(
                DecisionOption("📋 静态小规模（< 100 项）", nextNodeId = "scale_small"),
                DecisionOption("📜 虚拟化大规模（> 100 项）", nextNodeId = "scale_large"),
            )
        ),
        "scale_small" to DecisionNode(
            id = "scale_small",
            question = "🔢 需要几维布局？/ How many dimensions?",
            options = listOf(
                DecisionOption("1️⃣ 单维（仅行或仅列）", nextNodeId = "dimension_1d"),
                DecisionOption("2️⃣ 二维（行 + 列同时控制）", nextNodeId = "dimension_2d"),
            )
        ),
        "scale_large" to DecisionNode(
            id = "scale_large",
            question = "🔢 需要几维布局？/ How many dimensions?",
            options = listOf(
                DecisionOption("1️⃣ 单维（仅行或仅列）", nextNodeId = "dimension_1d_lazy"),
                DecisionOption("2️⃣ 二维（行 + 列同时控制）", nextNodeId = "dimension_2d_lazy"),
            )
        ),
        "dimension_1d" to DecisionNode(
            id = "dimension_1d",
            question = "📐 空间分配需求是什么？/ What is the space allocation need?",
            options = listOf(
                DecisionOption("📏 固定尺寸（不伸缩）", nextNodeId = "space_fixed"),
                DecisionOption("↔️ 等分伸缩（flex-grow）", nextNodeId = "space_grow"),
                DecisionOption("↔️ 按比例伸缩（flex-grow/shrink）", nextNodeId = "space_flex"),
                DecisionOption("🔄 自动换行（flex-wrap）", nextNodeId = "space_wrap"),
            )
        ),
        "dimension_2d" to DecisionNode(
            id = "dimension_2d",
            question = "🎯 对齐需求是什么？/ What is the alignment need?",
            options = listOf(
                DecisionOption("🎯 精确对齐（跨行跨列）", nextNodeId = "alignment_exact", recommendedLayout = LayoutType.Grid),
                DecisionOption("📐 简单网格（无跨行跨列）", nextNodeId = "alignment_simple", recommendedLayout = LayoutType.Grid),
            )
        ),
        "dimension_1d_lazy" to DecisionNode(
            id = "dimension_1d_lazy",
            question = "📐 空间分配需求是什么？/ What is the space allocation need?",
            options = listOf(
                DecisionOption("📏 固定尺寸", nextNodeId = "space_fixed", recommendedLayout = LayoutType.LazyGrid),
                DecisionOption("↔️ 等分伸缩", nextNodeId = "space_grow", recommendedLayout = LayoutType.LazyGrid),
                DecisionOption("🔄 自动换行", nextNodeId = "space_wrap", recommendedLayout = LayoutType.LazyGrid),
            )
        ),
        "dimension_2d_lazy" to DecisionNode(
            id = "dimension_2d_lazy",
            question = "🎯 对齐需求是什么？/ What is the alignment need?",
            options = listOf(
                DecisionOption("🎯 精确对齐（跨行跨列）", nextNodeId = "alignment_exact", recommendedLayout = LayoutType.LazyGrid),
                DecisionOption("📐 简单网格", nextNodeId = "alignment_simple", recommendedLayout = LayoutType.LazyGrid),
            )
        ),
        "space_fixed" to DecisionNode(
            id = "space_fixed",
            question = "🎯 是否需要主轴/交叉轴对齐？/ Need axis alignment?",
            options = listOf(
                DecisionOption("✅ 是（justifyContent/alignItems）", nextNodeId = "alignment_yes", recommendedLayout = LayoutType.FlexBox),
                DecisionOption("❌ 否（简单线性排列）", nextNodeId = "alignment_no", recommendedLayout = LayoutType.ColumnRow),
            )
        ),
        "space_grow" to DecisionNode(
            id = "space_grow",
            question = "🎯 是否需要多行自动折行？/ Need multi-line wrap?",
            options = listOf(
                DecisionOption("✅ 是（flex-wrap: wrap）", nextNodeId = "alignment_yes", recommendedLayout = LayoutType.FlexBox),
                DecisionOption("❌ 否（单行等分）", nextNodeId = "alignment_yes", recommendedLayout = LayoutType.FlexBox),
            )
        ),
        "space_flex" to DecisionNode(
            id = "space_flex",
            question = "✅ 推荐 FlexBox / Recommended: FlexBox",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.FlexBox),
            )
        ),
        "space_wrap" to DecisionNode(
            id = "space_wrap",
            question = "✅ 推荐 FlexBox（带 flex-wrap）/ Recommended: FlexBox (with flex-wrap)",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.FlexBox),
            )
        ),
        "alignment_yes" to DecisionNode(
            id = "alignment_yes",
            question = "✅ 推荐 FlexBox / Recommended: FlexBox",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.FlexBox),
            )
        ),
        "alignment_no" to DecisionNode(
            id = "alignment_no",
            question = "✅ 推荐 Column/Row / Recommended: Column/Row",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.ColumnRow),
            )
        ),
        "alignment_exact" to DecisionNode(
            id = "alignment_exact",
            question = "✅ 推荐 Grid / Recommended: Grid",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.Grid),
            )
        ),
        "alignment_simple" to DecisionNode(
            id = "alignment_simple",
            question = "✅ 推荐 Grid / Recommended: Grid",
            options = listOf(
                DecisionOption("🔄 重新选择", nextNodeId = "root", recommendedLayout = LayoutType.Grid),
            )
        ),
    )

    fun sendIntent(intent: GridDecisionTreeIntent) {
        when (intent) {
            is GridDecisionTreeIntent.SelectOption -> selectOption(intent.option)
            is GridDecisionTreeIntent.ResetTree -> resetTree()
            is GridDecisionTreeIntent.AnalyzeCode -> analyzeCode(intent.code)
            is GridDecisionTreeIntent.NavigateToPlayground -> navigateToPlayground()
        }
    }

    /**
     * 选择决策选项 / Select a decision option
     *
     * @param option 选中的选项 / Selected option
     */
    private fun selectOption(option: DecisionOption) {
        val currentPath = _state.value.currentPath.toMutableList()
        currentPath.add(option.label)

        if (option.recommendedLayout != null) {
            // 叶子节点 - 到达推荐结果
            _state.value = _state.value.copy(
                currentPath = currentPath,
                recommendedLayout = option.recommendedLayout,
            )
            viewModelScope.launch {
                _effect.send(GridDecisionTreeEffect.ShowRecommendation(option.recommendedLayout))
            }
        } else if (option.nextNodeId != null) {
            // 中间节点 - 继续决策
            _state.value = _state.value.copy(
                currentPath = currentPath,
                currentNodeId = option.nextNodeId,
                recommendedLayout = null,
            )
        }
    }

    /** 重置决策树 / Reset the decision tree */
    private fun resetTree() {
        _state.value = GridDecisionTreeState.Initial
    }

    /**
     * 分析代码片段，反查应使用的布局类型 / Analyze code snippet to detect layout type
     *
     * @param code 代码片段 / Code snippet
     */
    private fun analyzeCode(code: String) {
        _state.value = _state.value.copy(codeInput = code)

        viewModelScope.launch {
            // 模拟代码分析延迟 / Simulate analysis delay
            delay(300)

            val result = when {
                code.contains("LazyVerticalGrid") || code.contains("LazyHorizontalGrid") -> CodeAnalysisResult(
                    detectedLayout = LayoutType.LazyGrid,
                    suggestion = "LazyGrid detected. Consider migrating to Grid if data is small (< 100 items).",
                    confidence = 0.95f
                )
                code.contains("Grid") && !code.contains("Lazy") -> CodeAnalysisResult(
                    detectedLayout = LayoutType.Grid,
                    suggestion = "Grid API detected. Ensure compileSdk 35+ and OptIn(ExperimentalLayoutApi::class).",
                    confidence = 0.90f
                )
                code.contains("flex") || code.contains("FlexBox") || code.contains("flexGrow") -> CodeAnalysisResult(
                    detectedLayout = LayoutType.FlexBox,
                    suggestion = "FlexBox API detected. flexGrow/flexShrink/flexBasis are the key properties.",
                    confidence = 0.85f
                )
                code.contains("Column") && !code.contains("Lazy") -> CodeAnalysisResult(
                    detectedLayout = LayoutType.ColumnRow,
                    suggestion = "Column detected. Consider FlexBox if you need grow/shrink/wrap capabilities.",
                    confidence = 0.80f
                )
                code.contains("Row") && !code.contains("Lazy") -> CodeAnalysisResult(
                    detectedLayout = LayoutType.ColumnRow,
                    suggestion = "Row detected. Consider FlexBox if you need grow/shrink/wrap capabilities.",
                    confidence = 0.80f
                )
                else -> CodeAnalysisResult(
                    detectedLayout = LayoutType.ColumnRow,
                    suggestion = "No specific layout detected. Consider using the decision tree above.",
                    confidence = 0.50f
                )
            }

            _state.value = _state.value.copy(analysisResult = result)
        }
    }

    /** 导航到 Playground / Navigate to Playground */
    private fun navigateToPlayground() {
        viewModelScope.launch {
            _effect.send(GridDecisionTreeEffect.NavigateToPlayground("default"))
        }
    }

    /** 获取当前节点 / Get current node */
    fun getCurrentNode(): DecisionNode? = decisionTree[_state.value.currentNodeId]
}

// ============================================================
// PlaygroundViewModel — Playground ViewModel
// ============================================================
/**
 * Playground ViewModel
 * Manages the interactive playground for Grid/FlexBox code editing and live preview.
 *
 * @see PlaygroundState
 * @see PlaygroundIntent
 * @see PlaygroundEffect
 */
class PlaygroundViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlaygroundState.Initial)
    val state: StateFlow<PlaygroundState> = _state.asStateFlow()

    private val _effect = Channel<PlaygroundEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化预设模板 / Initialize preset templates
        _state.value = _state.value.copy(templates = getDefaultTemplates())
    }

    fun sendIntent(intent: PlaygroundIntent) {
        when (intent) {
            is PlaygroundIntent.UpdateCode -> updateCode(intent.code)
            is PlaygroundIntent.UpdateGridParams -> updateGridParams(intent.params)
            is PlaygroundIntent.UpdateFlexParams -> updateFlexParams(intent.params)
            is PlaygroundIntent.LoadTemplate -> loadTemplate(intent.templateId)
            is PlaygroundIntent.SelectTab -> selectTab(intent.tab)
            is PlaygroundIntent.UpdatePreviewScale -> updatePreviewScale(intent.scale)
            is PlaygroundIntent.ToggleLivePreview -> toggleLivePreview()
            is PlaygroundIntent.ClearPreviewError -> clearPreviewError()
        }
    }

    /**
     * 更新代码 / Update code
     * 如果启用实时预览，延迟 500ms 后触发预览刷新。
     *
     * @param code 新的代码 / New code
     */
    private fun updateCode(code: String) {
        _state.value = _state.value.copy(code = code, previewError = null)
    }

    /**
     * 更新 Grid 参数 / Update Grid parameters
     *
     * @param params 新的 Grid 参数 / New Grid parameters
     */
    private fun updateGridParams(params: GridParams) {
        _state.value = _state.value.copy(gridParams = params)
    }

    /**
     * 更新 FlexBox 参数 / Update FlexBox parameters
     *
     * @param params 新的 FlexBox 参数 / New FlexBox parameters
     */
    private fun updateFlexParams(params: FlexBoxParams) {
        _state.value = _state.value.copy(flexBoxParams = params)
    }

    /**
     * 加载预设模板 / Load a preset template
     *
     * @param templateId 模板 ID / Template ID
     */
    private fun loadTemplate(templateId: String) {
        val template = _state.value.templates.find { it.id == templateId }
        if (template != null) {
            _state.value = _state.value.copy(
                selectedTemplate = template,
                gridParams = template.gridParams ?: _state.value.gridParams,
                flexBoxParams = template.flexBoxParams ?: _state.value.flexBoxParams,
                activeTab = if (template.layoutType == LayoutType.Grid) PlaygroundTab.Grid else PlaygroundTab.FlexBox,
            )
        }
    }

    private fun selectTab(tab: PlaygroundTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    private fun updatePreviewScale(scale: Float) {
        _state.value = _state.value.copy(previewScale = scale.coerceIn(0.25f, 2f))
    }

    private fun toggleLivePreview() {
        _state.value = _state.value.copy(isLivePreviewEnabled = !_state.value.isLivePreviewEnabled)
    }

    private fun clearPreviewError() {
        _state.value = _state.value.copy(previewError = null)
    }

    /**
     * 获取默认模板列表 / Get default template list
     * 8 个预设模板覆盖典型使用场景。
     */
    private fun getDefaultTemplates(): List<PlaygroundTemplate> = listOf(
        PlaygroundTemplate(
            id = "dashboard",
            nameCn = "仪表盘",
            nameEn = "Dashboard",
            description = "仪表盘布局，使用 Grid 实现跨行跨列的卡片网格",
            gridParams = GridParams(columns = 3, rows = 2, columnGap = 12, rowGap = 12),
            itemCount = 6,
            layoutType = LayoutType.Grid
        ),
        PlaygroundTemplate(
            id = "photo_gallery",
            nameCn = "相册",
            nameEn = "Photo Gallery",
            description = "相册网格布局，使用 Grid 实现统一尺寸的照片墙",
            gridParams = GridParams(columns = 4, rows = 3, columnGap = 4, rowGap = 4),
            itemCount = 12,
            layoutType = LayoutType.Grid
        ),
        PlaygroundTemplate(
            id = "form",
            nameCn = "表单",
            nameEn = "Form Layout",
            description = "自适应表单布局，使用 FlexBox 实现标签和输入框对齐",
            flexBoxParams = FlexBoxParams(
                direction = FlexDirection.Column,
                flexGrow = listOf(0f, 0f, 0f, 1f),
                crossAxisSpacing = 8
            ),
            itemCount = 4,
            layoutType = LayoutType.FlexBox
        ),
        PlaygroundTemplate(
            id = "tab_bar",
            nameCn = "标签栏",
            nameEn = "Tab Bar",
            description = "底部标签栏，使用 FlexBox 实现等宽标签自动换行",
            flexBoxParams = FlexBoxParams(
                direction = FlexDirection.Row,
                flexGrow = listOf(1f, 1f, 1f, 1f, 1f),
                mainAxisSpacing = 0,
                crossAxisSpacing = 0
            ),
            itemCount = 5,
            layoutType = LayoutType.FlexBox
        ),
        PlaygroundTemplate(
            id = "responsive_list",
            nameCn = "响应式列表",
            nameEn = "Responsive List",
            description = "响应式列表项，FlexBox 实现左侧固定右侧自适应的布局",
            flexBoxParams = FlexBoxParams(
                direction = FlexDirection.Row,
                flexGrow = listOf(0f, 1f),
                flexBasis = listOf(80, 0),
                mainAxisSpacing = 12
            ),
            itemCount = 6,
            layoutType = LayoutType.FlexBox
        ),
        PlaygroundTemplate(
            id = "chat_bubble",
            nameCn = "聊天气泡",
            nameEn = "Chat Bubbles",
            description = "聊天消息列表，Grid 实现左右对齐的消息气泡",
            gridParams = GridParams(columns = 2, rows = 10, columnGap = 8, rowGap = 4),
            itemCount = 10,
            layoutType = LayoutType.Grid
        ),
        PlaygroundTemplate(
            id = "pricing_cards",
            nameCn = "价格卡片",
            nameEn = "Pricing Cards",
            description = "定价页面三卡片布局，Grid 实现统一对齐",
            gridParams = GridParams(columns = 3, rows = 1, columnGap = 16, rowGap = 0),
            itemCount = 3,
            layoutType = LayoutType.Grid
        ),
        PlaygroundTemplate(
            id = "chip_group",
            nameCn = "标签组",
            nameEn = "Chip Group",
            description = "标签组，FlexBox 实现自动换行的标签流式布局",
            flexBoxParams = FlexBoxParams(
                direction = FlexDirection.Row,
                flexGrow = listOf(0f, 0f, 0f, 0f, 0f, 0f),
                mainAxisSpacing = 8,
                crossAxisSpacing = 8
            ),
            itemCount = 8,
            layoutType = LayoutType.FlexBox
        ),
    )
}

// ============================================================
// PerformanceAnalysisViewModel — 性能分析 ViewModel
// ============================================================
/**
 * PerformanceAnalysis ViewModel
 * Manages layout performance benchmark testing.
 *
 * @see PerformanceAnalysisState
 * @see PerformanceAnalysisIntent
 * @see PerformanceAnalysisEffect
 */
class PerformanceAnalysisViewModel : ViewModel() {

    private val _state = MutableStateFlow(PerformanceAnalysisState.Initial)
    val state: StateFlow<PerformanceAnalysisState> = _state.asStateFlow()

    private val _effect = Channel<PerformanceAnalysisEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: PerformanceAnalysisIntent) {
        when (intent) {
            is PerformanceAnalysisIntent.RunBenchmark -> runBenchmark()
            is PerformanceAnalysisIntent.SelectLayouts -> selectLayouts(intent.layouts)
            is PerformanceAnalysisIntent.SelectMetric -> selectMetric(intent.metric)
            is PerformanceAnalysisIntent.ClearResults -> clearResults()
        }
    }

    /**
     * 运行性能基准测试 / Run performance benchmark
     * 注意：重组计数仅在 debug build 中可靠。
     */
    private fun runBenchmark() {
        if (_state.value.isRunning) return

        _state.value = _state.value.copy(isRunning = true, benchmarkProgress = 0f)

        viewModelScope.launch {
            val results = mutableListOf<LayoutPerformanceResult>()
            val layouts = _state.value.selectedLayouts.toList()
            val deviceInfo = android.os.Build.MODEL + " (API " + android.os.Build.VERSION.SDK_INT + ")"

            layouts.forEachIndexed { index, layoutType ->
                // 模拟基准测试延迟 / Simulate benchmark delay
                delay(500)

                val result = LayoutPerformanceResult(
                    layoutType = layoutType,
                    recompositionCount = (10..50).random(),
                    renderTimeMs = (5..50).random().toLong(),
                    memoryUsageMb = (10..100).random() / 10f,
                    deviceInfo = deviceInfo
                )
                results.add(result)

                val progress = (index + 1).toFloat() / layouts.size
                _state.value = _state.value.copy(benchmarkProgress = progress)
                _effect.send(PerformanceAnalysisEffect.ShowBenchmarkProgress(progress))
                _effect.send(PerformanceAnalysisEffect.ShowBenchmarkResult(result))
            }

            _state.value = _state.value.copy(
                isRunning = false,
                testResults = results,
                benchmarkProgress = 1f
            )
        }
    }

    private fun selectLayouts(layouts: Set<LayoutType>) {
        _state.value = _state.value.copy(selectedLayouts = layouts)
    }

    private fun selectMetric(metric: PerformanceMetric) {
        _state.value = _state.value.copy(selectedMetric = metric)
    }

    private fun clearResults() {
        _state.value = _state.value.copy(testResults = emptyList(), benchmarkProgress = 0f)
    }
}

// ============================================================
// MigrationScannerViewModel — 迁移扫描器 ViewModel
// ============================================================
/**
 * MigrationScanner ViewModel
 * Scans code for Column/Row/LazyGrid usages and suggests Grid/FlexBox migrations.
 *
 * @see MigrationScannerState
 * @see MigrationScannerIntent
 * @see MigrationScannerEffect
 */
class MigrationScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow(MigrationScannerState.Initial)
    val state: StateFlow<MigrationScannerState> = _state.asStateFlow()

    private val _effect = Channel<MigrationScannerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: MigrationScannerIntent) {
        when (intent) {
            is MigrationScannerIntent.UpdateTargetDirectory -> updateTargetDirectory(intent.path)
            is MigrationScannerIntent.StartScan -> startScan()
            is MigrationScannerIntent.FilterByLayoutType -> filterByLayoutType(intent.layoutType)
            is MigrationScannerIntent.ExportReport -> exportReport()
        }
    }

    private fun updateTargetDirectory(path: String) {
        _state.value = _state.value.copy(targetDirectory = path)
    }

    /**
     * 开始扫描 / Start scanning
     * 模拟扫描过程，实际场景中应使用 AST 解析或 Ktlint 规则。
     */
    private fun startScan() {
        if (_state.value.isScanning) return

        _state.value = _state.value.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList())

        viewModelScope.launch {
            // 模拟扫描延迟 / Simulate scan delay
            delay(1000)

            // 模拟扫描结果 / Simulate scan results
            val mockResults = listOf(
                MigrationScanResult(
                    filePath = "feature/dashboard/DashboardScreen.kt",
                    lineNumber = 45,
                    detectedLayout = LayoutType.ColumnRow,
                    suggestedMigration = LayoutType.FlexBox,
                    confidence = 0.85f,
                    codeSnippet = "Column(modifier = Modifier.fillMaxWidth()) { ... }"
                ),
                MigrationScanResult(
                    filePath = "feature/list/ItemListScreen.kt",
                    lineNumber = 78,
                    detectedLayout = LayoutType.LazyGrid,
                    suggestedMigration = LayoutType.Grid,
                    confidence = 0.70f,
                    codeSnippet = "LazyVerticalGrid(columns = 4) { items(20) { ... } }"
                ),
                MigrationScanResult(
                    filePath = "feature/form/FormScreen.kt",
                    lineNumber = 112,
                    detectedLayout = LayoutType.ColumnRow,
                    suggestedMigration = LayoutType.FlexBox,
                    confidence = 0.90f,
                    codeSnippet = "Row(horizontalArrangement = Arrangement.SpaceEvenly) { ... }"
                ),
            )

            _state.value = _state.value.copy(
                isScanning = false,
                scanProgress = 1f,
                scanResults = mockResults
            )
            _effect.send(MigrationScannerEffect.ShowScanComplete(mockResults.size))
        }
    }

    private fun filterByLayoutType(layoutType: LayoutType?) {
        _state.value = _state.value.copy(filterLayoutType = layoutType)
    }

    private fun exportReport() {
        viewModelScope.launch {
            _effect.send(MigrationScannerEffect.ShowExportSuccess("/tmp/migration_report.md"))
        }
    }
}
