package com.mvi.kenny.feature.navevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// =============================================================
// NavEventKitViewModel — Navigation Event KMP 迁移工具包 ViewModel
// PRD-140: Navigation Event KMP 库迁移检测与 PredictiveBackHandler 废弃替代工具包
// =============================================================
/**
 * ViewModel for Navigation Event Kit / Navigation Event 工具包 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 *
 * Key responsibilities:
 * - Scan project for PredictiveBackHandler usages
 * - Generate migration diffs
 * - Manage hierarchical dispatcher tree visualization
 * - Handle platform behavior comparisons
 *
 * @see NavEventKitContract For State, Intent, Effect definitions
 * @see NavEventKitScreen For UI implementation
 */
class NavEventKitViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(NavEventKitState.Initial)
    val state: StateFlow<NavEventKitState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<NavEventKitEffect>()
    val effect: SharedFlow<NavEventKitEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ---------------------------------------------------------
    // Intent processing — process user actions
    // ---------------------------------------------------------
    /**
     * Process user intent / 处理用户意图
     */
    fun processIntent(intent: NavEventKitIntent) {
        when (intent) {
            is NavEventKitIntent.SelectTab -> handleSelectTab(intent.tab)
            is NavEventKitIntent.StartScan -> handleStartScan(intent.modulePath)
            is NavEventKitIntent.CancelScan -> handleCancelScan()
            is NavEventKitIntent.SelectUsage -> handleSelectUsage(intent.usage)
            is NavEventKitIntent.ClearUsage -> handleClearUsage()
            is NavEventKitIntent.ToggleUsageSelection -> handleToggleUsageSelection(intent.usageId)
            is NavEventKitIntent.SelectAllUsages -> handleSelectAllUsages()
            is NavEventKitIntent.DeselectAllUsages -> handleDeselectAllUsages()
            is NavEventKitIntent.TogglePlatform -> handleTogglePlatform(intent.platform)
            is NavEventKitIntent.NextWizardStep -> handleNextWizardStep()
            is NavEventKitIntent.PreviousWizardStep -> handlePreviousWizardStep()
            is NavEventKitIntent.ApplyMigration -> handleApplyMigration()
            is NavEventKitIntent.ToggleNodeExpand -> handleToggleNodeExpand(intent.nodeId)
            is NavEventKitIntent.SelectDispatcherNode -> handleSelectDispatcherNode(intent.node)
            is NavEventKitIntent.ConnectDebugger -> handleConnectDebugger()
            is NavEventKitIntent.DisconnectDebugger -> handleDisconnectDebugger()
            is NavEventKitIntent.ExportTree -> handleExportTree(intent.format)
            is NavEventKitIntent.SetPlatformFilter -> handleSetPlatformFilter(intent.platform)
            is NavEventKitIntent.DismissError -> handleDismissError()
        }
    }

    // ---------------------------------------------------------
    // Intent handlers
    // ---------------------------------------------------------

    /**
     * Handle tab selection / 处理 Tab 选择
     */
    private fun handleSelectTab(tab: NavEventTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    /**
     * Handle scan initiation / 处理扫描启动
     *
     * @param modulePath Path to scan / 扫描路径
     */
    private fun handleStartScan(modulePath: String) {
        scanJob?.cancel()

        _state.update {
            it.copy(
                scanPhase = ScanPhase.Scanning,
                scanProgress = 0f,
                usages = emptyList(),
                error = null,
                wizardStep = WizardStep.SELECT_SCOPE
            )
        }

        scanJob = viewModelScope.launch {
            try {
                // Phase 1: Scanning / 阶段 1: 扫描
                val usages = withContext(Dispatchers.Default) {
                    simulateScan(modulePath)
                }

                // Phase 2: Analyzing / 阶段 2: 分析
                _state.update { it.copy(scanPhase = ScanPhase.Analyzing, scanProgress = 0.3f) }
                delay(500)

                // Phase 3: Generating diffs / 阶段 3: 生成差异
                _state.update { it.copy(scanPhase = ScanPhase.GeneratingDiffs, scanProgress = 0.6f) }
                val diffs = generateDiffs(usages)
                delay(500)

                // Phase 4: Done / 阶段 4: 完成
                _state.update {
                    it.copy(
                        scanPhase = ScanPhase.Done,
                        scanProgress = 1f,
                        usages = usages,
                        previewDiffs = diffs,
                        wizardStep = WizardStep.SELECT_USAGES
                    )
                }
                _effect.emit(NavEventKitEffect.ScanComplete)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _state.update { it.copy(scanPhase = ScanPhase.Idle, scanProgress = 0f) }
                } else {
                    _state.update {
                        it.copy(
                            scanPhase = ScanPhase.Error(e.message ?: "Unknown error"),
                            error = e.message
                        )
                    }
                    _effect.emit(
                        NavEventKitEffect.ShowSnackbar(
                            "Scan failed: ${e.message}",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Simulate scan with demo results / 用演示结果模拟扫描
     *
     * In production, this would use Kotlin PSI or lint AST analysis.
     *
     * @param modulePath Module path being scanned / 正在扫描的模块路径
     * @return List of detected usages / 检测到的用法列表
     */
    private suspend fun simulateScan(modulePath: String): List<PredictiveBackHandlerUsage> {
        // Simulate scan progress / 模拟扫描进度
        val totalSteps = 10
        repeat(totalSteps) { step ->
            delay(200)
            _state.update {
                it.copy(
                    scanProgress = (step + 1).toFloat() / totalSteps * 0.3f,
                    scannedFiles = it.scannedFiles + 3
                )
            }
        }

        // Return simulated usages / 返回模拟的用法
        return listOf(
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/ui/screens/HomeScreen.kt",
                lineNumber = 42,
                methodName = "BackHandlerDemo()",
                className = "HomeScreen",
                callChain = "HomeScreen.BackHandlerDemo() → PredictiveBackHandler",
                complexity = MigrationComplexity.SIMPLE,
                migrationSuggestion = "Replace PredictiveBackHandler with NavigationEventDispatcher using DirectNavigationEventInput"
            ),
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/ui/screens/DetailScreen.kt",
                lineNumber = 78,
                methodName = "onSwipeBack()",
                className = "DetailScreen",
                callChain = "DetailScreen.onSwipeBack() → PredictiveBackHandler(scope, label, callback)",
                complexity = MigrationComplexity.MEDIUM,
                migrationSuggestion = "Map scope to NavigationEventDispatcherOwner context, wrap callback in OnBackInvokedInput"
            ),
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/ui/navigation/NavHost.kt",
                lineNumber = 156,
                methodName = "setupBackHandling()",
                className = "NavHost",
                callChain = "NavHost.setupBackHandling() → PredictiveBackHandler + rememberCoroutineScope",
                complexity = MigrationComplexity.COMPLEX,
                migrationSuggestion = "Refactor to use Hierarchical NavigationEventDispatcher pattern with proper parent-child relationships"
            ),
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/features/checkout/CheckoutViewModel.kt",
                lineNumber = 203,
                methodName = "handleCheckoutBack()",
                className = "CheckoutViewModel",
                callChain = "CheckoutViewModel.handleCheckoutBack() → PredictiveBackHandler with custom state",
                complexity = MigrationComplexity.MANUAL,
                migrationSuggestion = "Manual review required: Custom back handling state needs architectural redesign for NavigationEventDispatcher"
            ),
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/ui/components/SwipeBackContainer.kt",
                lineNumber = 89,
                methodName = "SwipeBackContent()",
                className = "SwipeBackContainer",
                callChain = "SwipeBackContainer.SwipeBackContent() → PredictiveBackHandler",
                complexity = MigrationComplexity.SIMPLE,
                migrationSuggestion = "Direct replacement with NavigationEventDispatcher.OnBackInvokedInput"
            ),
            PredictiveBackHandlerUsage(
                id = UUID.randomUUID().toString(),
                filePath = "$modulePath/androidMain/PlatformBackHandler.kt",
                lineNumber = 34,
                methodName = "setupBackCallback()",
                className = "PlatformBackHandler",
                callChain = "PlatformBackHandler.setupBackCallback() → PredictiveBackHandler(androidx.compose.ui)",
                complexity = MigrationComplexity.MEDIUM,
                migrationSuggestion = "Use platform-specific NavigationEventDispatcher with OnBackInvokedInput for Android"
            )
        )
    }

    /**
     * Generate migration diffs for usages / 为用法生成迁移差异
     */
    private fun generateDiffs(usages: List<PredictiveBackHandlerUsage>): Map<String, String> {
        return usages.associate { usage ->
            usage.filePath to generateDiffForUsage(usage)
        }
    }

    /**
     * Generate diff for a single usage / 为单个用法生成差异
     */
    private fun generateDiffForUsage(usage: PredictiveBackHandlerUsage): String {
        return when (usage.complexity) {
            MigrationComplexity.SIMPLE -> """
--- ${usage.filePath}
+++ ${usage.filePath} (Migrated)
@@ -${usage.lineNumber},1 +${usage.lineNumber},1 @@
-import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
-import androidx.compose.ui.input.nestedscroll.NestedScrollSource
-import androidx.compose.foundation.layout.Box
-import androidx.compose.material3.Text
-import androidx.compose.runtime.Composable
-import androidx.compose.ui.input.nestedscroll.rememberNestedScrollConnection
+import androidx.navigationevent.rememberNavigationEventDispatcher
+ import androidx.navigationevent.NavigationEventDispatcherOwner
+ import androidx.navigationevent.DirectNavigationEventInput
+ import androidx.navigationevent.OnBackInvokedInput
 
 // Before / 迁移前:
 PredictiveBackHandler(
     scope = rememberCoroutineScope(),
     onBackInvokedCallback = {
         // Handle back gesture / 处理返回手势
     }
 )
 
 // After / 迁移后:
 val dispatcher = NavigationEventDispatcherOwner.current.dispatcher
 val input = DirectNavigationEventInput { event ->
     // Handle navigation event / 处理导航事件
 }
 dispatcher?.registerInput(input)
            """.trimIndent()

            MigrationComplexity.MEDIUM -> """
--- ${usage.filePath}
+++ ${usage.filePath} (Migrated)
@@ -${usage.lineNumber},1 +${usage.lineNumber},1 @@
 // Migration requires:
 // 1. Add navigationevent dependency to build.gradle.kts
 // 2. Wrap composable with NavigationEventDispatcherOwner
 // 3. Register DirectNavigationEventInput or OnBackInvokedInput
 //
 // Before / 迁移前:
 PredictiveBackHandler(
     scope = ${usage.methodName}().coroutineScope,
     label = "${usage.className}",
     onBackInvokedCallback = { /* custom logic */ }
 )
 //
 // After / 迁移后:
 @Composable
 fun ${usage.className}() {
     val dispatcher = LocalNavigationEventDispatcher.current
     val backInput = OnBackInvokedInput(
         onBack = { /* custom logic */ }
     )
     LaunchedEffect(Unit) {
         dispatcher?.registerInput(backInput)
     }
     DisposableEffect(Unit) {
         onDispose {
             dispatcher?.unregisterInput(backInput)
         }
     }
 }
            """.trimIndent()

            MigrationComplexity.COMPLEX -> """
--- ${usage.filePath}
+++ ${usage.filePath} (Migrated)
@@ -${usage.lineNumber},1 +${usage.lineNumber},1 @@
 // ⚠️ Complex migration: Architectural refactoring required
 //
 // Hierarchical NavigationEventDispatcher pattern needed:
 //
 // 1. Set up parent-child dispatcher relationships
 // 2. Implement proper event propagation
 // 3. Handle platform-specific behavior differences
 //
 // Before / 迁移前:
 PredictiveBackHandler(scope = scope, label = "${usage.className}", onBackInvokedCallback = {...})
 //
 // After / 迁移后:
 // See: NavigationEventDispatcherOwner + HierarchicalDispatcher setup guide
 // File: docs/navigation-event-hierarchical-setup.md
            """.trimIndent()

            MigrationComplexity.MANUAL -> """
--- ${usage.filePath}
+++ ${usage.filePath}
@@ -${usage.lineNumber},1 +${usage.lineNumber},1 @@
 // ⚠️ MANUAL REVIEW REQUIRED / 需要手动审查
 //
 // This usage has custom back handling state that requires
 // careful architectural analysis before migration.
 //
 // Recommended steps:
 // 1. Analyze the back handling state machine
 // 2. Design new NavigationEventDispatcher architecture
 // 3. Create migration plan with rollback strategy
 // 4. Implement in isolation first
 // 5. Run full regression tests
 //
 // Location: ${usage.filePath}:${usage.lineNumber}
 // Method: ${usage.methodName}
 // Class: ${usage.className}
            """.trimIndent()
        }
    }

    /**
     * Handle scan cancellation / 处理扫描取消
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update {
            it.copy(
                scanPhase = ScanPhase.Idle,
                scanProgress = 0f,
                scannedFiles = 0
            )
        }
    }

    /**
     * Handle usage selection / 处理用法选择
     */
    private fun handleSelectUsage(usage: PredictiveBackHandlerUsage) {
        _state.update { it.copy(selectedUsage = usage) }
    }

    /**
     * Handle clearing selected usage / 处理清除选中的用法
     */
    private fun handleClearUsage() {
        _state.update { it.copy(selectedUsage = null) }
    }

    /**
     * Handle toggling usage selection / 处理切换用法选择状态
     */
    private fun handleToggleUsageSelection(usageId: String) {
        _state.update {
            val newSelected = if (usageId in it.selectedUsages) {
                it.selectedUsages - usageId
            } else {
                it.selectedUsages + usageId
            }
            it.copy(selectedUsages = newSelected)
        }
    }

    /**
     * Handle selecting all usages / 处理全选所有用法
     */
    private fun handleSelectAllUsages() {
        _state.update {
            it.copy(selectedUsages = it.usages.map { u -> u.id }.toSet())
        }
    }

    /**
     * Handle deselecting all usages / 处理取消所有用法选择
     */
    private fun handleDeselectAllUsages() {
        _state.update { it.copy(selectedUsages = emptySet()) }
    }

    /**
     * Handle toggling platform / 处理切换平台
     */
    private fun handleTogglePlatform(platform: TargetPlatform) {
        _state.update {
            val newPlatforms = if (platform in it.selectedPlatforms) {
                it.selectedPlatforms - platform
            } else {
                it.selectedPlatforms + platform
            }
            it.copy(selectedPlatforms = newPlatforms)
        }
    }

    /**
     * Handle next wizard step / 处理下一步向导
     */
    private fun handleNextWizardStep() {
        val currentStep = _state.value.wizardStep
        val nextStep = when (currentStep) {
            WizardStep.SELECT_SCOPE -> WizardStep.SELECT_USAGES
            WizardStep.SELECT_USAGES -> WizardStep.SELECT_PLATFORMS
            WizardStep.SELECT_PLATFORMS -> WizardStep.PREVIEW_DIFF
            WizardStep.PREVIEW_DIFF -> WizardStep.CONFIRM
            WizardStep.CONFIRM -> WizardStep.APPLYING
            WizardStep.APPLYING -> WizardStep.DONE
            WizardStep.DONE -> WizardStep.DONE
        }
        _state.update { it.copy(wizardStep = nextStep) }
    }

    /**
     * Handle previous wizard step / 处理上一步向导
     */
    private fun handlePreviousWizardStep() {
        val currentStep = _state.value.wizardStep
        val prevStep = when (currentStep) {
            WizardStep.SELECT_SCOPE -> WizardStep.SELECT_SCOPE
            WizardStep.SELECT_USAGES -> WizardStep.SELECT_SCOPE
            WizardStep.SELECT_PLATFORMS -> WizardStep.SELECT_USAGES
            WizardStep.PREVIEW_DIFF -> WizardStep.SELECT_PLATFORMS
            WizardStep.CONFIRM -> WizardStep.PREVIEW_DIFF
            WizardStep.APPLYING -> WizardStep.CONFIRM
            WizardStep.DONE -> WizardStep.DONE
        }
        _state.update { it.copy(wizardStep = prevStep) }
    }

    /**
     * Handle apply migration / 处理应用迁移
     */
    private fun handleApplyMigration() {
        viewModelScope.launch {
            _state.update { it.copy(isApplying = true, wizardStep = WizardStep.APPLYING) }

            try {
                // Simulate applying migration / 模拟应用迁移
                _state.update { it.copy(scanPhase = ScanPhase.ApplyingConfig("Applying Gradle changes...")) }
                delay(1000)
                _state.update { it.copy(scanPhase = ScanPhase.ApplyingConfig("Generating updated files...")) }
                delay(1000)
                _state.update { it.copy(scanPhase = ScanPhase.ApplyingConfig("Validating changes...")) }
                delay(500)

                _state.update {
                    it.copy(
                        isApplying = false,
                        wizardStep = WizardStep.DONE,
                        scanPhase = ScanPhase.Done
                    )
                }
                _effect.emit(NavEventKitEffect.MigrationApplied)
                _effect.emit(NavEventKitEffect.ShowSnackbar("Migration applied successfully"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isApplying = false,
                        error = "Migration failed: ${e.message}"
                    )
                }
                _effect.emit(
                    NavEventKitEffect.ShowSnackbar(
                        "Migration failed: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Handle toggling node expansion / 处理切换节点展开状态
     */
    private fun handleToggleNodeExpand(nodeId: String) {
        _state.update {
            val newExpanded = if (nodeId in it.expandedNodes) {
                it.expandedNodes - nodeId
            } else {
                it.expandedNodes + nodeId
            }
            it.copy(expandedNodes = newExpanded)
        }
    }

    /**
     * Handle selecting dispatcher node / 处理选择 dispatcher 节点
     */
    private fun handleSelectDispatcherNode(node: DispatcherNode) {
        _state.update { it.copy(selectedNode = node) }
    }

    /**
     * Handle connect debugger / 处理连接调试器
     */
    private fun handleConnectDebugger() {
        viewModelScope.launch {
            _state.update { it.copy(isConnected = false) }
            // Simulate connection / 模拟连接
            delay(1000)
            val demoTree = createDemoDispatcherTree()
            _state.update {
                it.copy(
                    isConnected = true,
                    treeRoot = demoTree,
                    expandedNodes = setOf("root", "navhost", "screen1", "screen2")
                )
            }
            _effect.emit(NavEventKitEffect.DebuggerConnected)
            _effect.emit(NavEventKitEffect.ShowSnackbar("Debugger connected"))
        }
    }

    /**
     * Create demo dispatcher tree / 创建演示用 dispatcher 树
     */
    private fun createDemoDispatcherTree(): DispatcherNode {
        return DispatcherNode(
            id = "root",
            name = "RootDispatcher (Activity)",
            type = DispatcherType.ROOT,
            isActive = true,
            depth = 0,
            children = listOf(
                DispatcherNode(
                    id = "navhost",
                    name = "NavHostDispatcher",
                    type = DispatcherType.NAV_HOST,
                    parentId = "root",
                    isActive = true,
                    depth = 1,
                    children = listOf(
                        DispatcherNode(
                            id = "screen1",
                            name = "HomeScreenDispatcher",
                            type = DispatcherType.SCREEN,
                            parentId = "navhost",
                            isActive = true,
                            depth = 2
                        ),
                        DispatcherNode(
                            id = "screen2",
                            name = "DetailScreenDispatcher",
                            type = DispatcherType.SCREEN,
                            parentId = "navhost",
                            isActive = false,
                            depth = 2
                        )
                    )
                ),
                DispatcherNode(
                    id = "tab",
                    name = "TabDispatcher",
                    type = DispatcherType.TAB,
                    parentId = "root",
                    isActive = false,
                    depth = 1,
                    children = listOf(
                        DispatcherNode(
                            id = "bottomnav",
                            name = "BottomNavDispatcher",
                            type = DispatcherType.SCREEN,
                            parentId = "tab",
                            isActive = false,
                            depth = 2
                        )
                    )
                )
            )
        )
    }

    /**
     * Handle disconnect debugger / 处理断开调试器
     */
    private fun handleDisconnectDebugger() {
        _state.update {
            it.copy(
                isConnected = false,
                treeRoot = null,
                selectedNode = null,
                eventHistory = emptyList()
            )
        }
        viewModelScope.launch {
            _effect.emit(NavEventKitEffect.DebuggerDisconnected)
            _effect.emit(NavEventKitEffect.ShowSnackbar("Debugger disconnected"))
        }
    }

    /**
     * Handle export tree / 处理导出树
     */
    private fun handleExportTree(format: ExportFormat) {
        viewModelScope.launch {
            try {
                val tree = _state.value.treeRoot ?: throw Exception("No tree to export")
                val content = when (format) {
                    ExportFormat.JSON -> generateJsonExport(tree)
                    ExportFormat.GRAPHML -> generateGraphMlExport(tree)
                    ExportFormat.MARKDOWN -> generateMarkdownExport(tree)
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "dispatcher_tree_$timestamp.${format.extension}"
                val filePath = "/tmp/$fileName"

                withContext(Dispatchers.IO) {
                    File(filePath).apply {
                        parentFile?.mkdirs()
                        writeText(content)
                    }
                }

                _effect.emit(NavEventKitEffect.ExportSuccess(filePath))
                _effect.emit(NavEventKitEffect.ShowSnackbar("Exported to: $filePath"))

            } catch (e: Exception) {
                _effect.emit(
                    NavEventKitEffect.ShowSnackbar(
                        "Export failed: ${e.message}",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Generate JSON export / 生成 JSON 导出
     */
    private fun generateJsonExport(tree: DispatcherNode): String {
        return buildString {
            appendLine("{")
            appendLine("  \"dispatcherTree\": {")
            appendLine("    \"id\": \"${tree.id}\",")
            appendLine("    \"name\": \"${tree.name}\",")
            appendLine("    \"type\": \"${tree.type.name}\",")
            appendLine("    \"isActive\": ${tree.isActive},")
            appendLine("    \"children\": [")
            tree.children.forEachIndexed { index, child ->
                appendLine("      {")
                appendLine("        \"id\": \"${child.id}\",")
                appendLine("        \"name\": \"${child.name}\",")
                appendLine("        \"type\": \"${child.type.name}\",")
                appendLine("        \"isActive\": ${child.isActive}")
                append("      }")
                if (index < tree.children.lastIndex) appendLine(",") else appendLine()
            }
            appendLine("    ]")
            appendLine("  }")
            append("}")
        }
    }

    /**
     * Generate GraphML export / 生成 GraphML 导出
     */
    private fun generateGraphMlExport(tree: DispatcherNode): String {
        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">")
            appendLine("  <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>")
            appendLine("  <key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>")
            appendLine("  <graph id=\"DispatcherTree\" edgedefault=\"directed\">")
            appendLine("    <!-- Nodes / 节点 -->")
            appendLine("    <node id=\"${tree.id}\">")
            appendLine("      <data key=\"label\">${tree.name}</data>")
            appendLine("      <data key=\"type\">${tree.type.name}</data>")
            appendLine("    </node>")
            tree.children.forEach { child ->
                appendLine("    <node id=\"${child.id}\">")
                appendLine("      <data key=\"label\">${child.name}</data>")
                appendLine("      <data key=\"type\">${child.type.name}</data>")
                appendLine("    </node>")
            }
            appendLine("    <!-- Edges / 边 -->")
            tree.children.forEach { child ->
                appendLine("    <edge source=\"${tree.id}\" target=\"${child.id}\"/>")
            }
            appendLine("  </graph>")
            append("</graphml>")
        }
    }

    /**
     * Generate Markdown export / 生成 Markdown 导出
     */
    private fun generateMarkdownExport(tree: DispatcherNode): String {
        return buildString {
            appendLine("# Dispatcher Tree / Dispatcher 树")
            appendLine()
            appendLine("## ${tree.name}")
            appendLine()
            appendLine("- **Type**: ${tree.type.label}")
            appendLine("- **Active**: ${tree.isActive}")
            appendLine()
            tree.children.forEach { child ->
                appendLine("### ${child.name}")
                appendLine()
                appendLine("- **Type**: ${child.type.label}")
                appendLine("- **Active**: ${child.isActive}")
                appendLine()
            }
        }
    }

    /**
     * Handle set platform filter / 处理设置平台过滤器
     */
    private fun handleSetPlatformFilter(platform: TargetPlatform?) {
        _state.update { it.copy(filterPlatform = platform) }
    }

    /**
     * Handle dismiss error / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    // ---------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
