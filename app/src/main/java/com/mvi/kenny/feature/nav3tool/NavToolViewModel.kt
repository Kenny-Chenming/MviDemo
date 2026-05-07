package com.mvi.kenny.feature.nav3tool

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
// NavToolViewModel — Navigation 3 迁移工具 ViewModel
// =============================================================
/**
 * ViewModel for Navigation 3 Migration Tool / Navigation 3 迁移工具 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Simulate Nav2 API scanning with realistic sample data
 * - Provide API mapping data for the reference guide
 * - Manage BottomNav migration diff display
 * - Provide code templates for Nav3 implementation
 * - Implement decision tree wizard logic
 *
 * @see NavToolContract For State, Intent, Effect definitions
 * @see NavToolScreen For UI implementation
 */
class NavToolViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(NavToolState.Initial)
    val state: StateFlow<NavToolState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<NavToolEffect>()
    val effect: SharedFlow<NavToolEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ---------------------------------------------------------
    // Intent processing — process user actions
    // ---------------------------------------------------------
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Each when branch handles one Intent type.
     */
    fun processIntent(intent: NavToolIntent) {
        when (intent) {
            is NavToolIntent.SelectTool -> handleSelectTool(intent.tool)
            is NavToolIntent.NavigateHome -> handleNavigateHome()
            is NavToolIntent.StartScan -> handleStartScan(intent.projectPath)
            is NavToolIntent.CancelScan -> handleCancelScan()
            is NavToolIntent.SelectIssue -> handleSelectIssue(intent.issue)
            is NavToolIntent.ClearIssue -> handleClearIssue()
            is NavToolIntent.FilterApiMappings -> handleFilterApiMappings(intent.category)
            is NavToolIntent.CopyTemplate -> handleCopyTemplate(intent.template)
            is NavToolIntent.AnswerDecisionTree -> handleAnswerDecisionTree(intent.answer, intent.nextNodeId)
            is NavToolIntent.ResetDecisionTree -> handleResetDecisionTree()
            is NavToolIntent.ExportScanReport -> handleExportScanReport(intent.format)
            is NavToolIntent.DismissError -> handleDismissError()
        }
    }

    // =============================================================
    // Tool Selection / 工具选择
    // =============================================================
    /**
     * Handle tool selection / 处理工具选择
     * Switches to the selected tool's detail view.
     *
     * @param tool Selected tool / 选中的工具
     */
    private fun handleSelectTool(tool: ToolId) {
        _state.update { it.copy(activeTool = tool, isHome = false) }
    }

    /**
     * Handle home navigation / 处理首页导航
     * Returns to the tool grid home view.
     */
    private fun handleNavigateHome() {
        _state.update { it.copy(isHome = true, selectedIssue = null, decisionTreeResult = null, decisionTreeCurrentNodeId = "root", decisionTreeAnswers = emptyList()) }
    }

    // =============================================================
    // Scan / 扫描
    // =============================================================
    /**
     * Start Nav2 API scan / 开始 Nav2 API 扫描
     *
     * Simulates scanning a project for Nav2 API usages.
     * In production, this would integrate with KSP or AST analysis.
     *
     * @param projectPath Project path to scan / 要扫描的项目路径
     */
    private fun handleStartScan(projectPath: String) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.SCANNING, scanProgress = 0f, scanResult = null) }

            // Simulate scanning progress / 模拟扫描进度
            val sampleIssues = generateSampleScanResult()
            val totalSteps = 20

            for (step in 1..totalSteps) {
                delay(150) // Simulate file processing time / 模拟文件处理时间
                _state.update { it.copy(scanProgress = step.toFloat() / totalSteps) }
            }

            // Build final scan result / 构建最终扫描结果
            val result = ScanResult(
                totalFilesScanned = 47,
                totalIssuesFound = sampleIssues.size,
                issuesBySeverity = mapOf(
                    Severity.P0_BLOCKER to sampleIssues.count { it.severity == Severity.P0_BLOCKER },
                    Severity.P1_HIGH to sampleIssues.count { it.severity == Severity.P1_HIGH },
                    Severity.P2_MEDIUM to sampleIssues.count { it.severity == Severity.P2_MEDIUM },
                    Severity.P3_LOW to sampleIssues.count { it.severity == Severity.P3_LOW }
                ),
                affectedFiles = sampleIssues.map { it.filePath }.distinct(),
                estimatedTotalMinutes = sampleIssues.sumOf { it.estimatedMinutes },
                nav2Version = "2.8.7",
                recommendation = getMigrationRecommendation(sampleIssues)
            )

            _state.update { it.copy(scanStatus = ScanStatus.COMPLETED, scanProgress = 1f, scanResult = result) }
            _effect.emit(NavToolEffect.ShowSnackbar("扫描完成，发现 ${sampleIssues.size} 处 Nav2 API 使用"))
        }
    }

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update { it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f) }
    }

    /**
     * Generate sample scan result for demonstration / 生成示例扫描结果
     */
    private fun generateSampleScanResult(): List<Nav2ApiUsage> = listOf(
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/home/HomeFragment.kt",
            lineNumber = 34,
            apiName = "NavHostFragment",
            codeSnippet = "val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment",
            severity = Severity.P0_BLOCKER,
            migrationHint = "将 NavHostFragment 替换为 Compose NavDisplay",
            estimatedMinutes = 30
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/home/HomeFragment.kt",
            lineNumber = 45,
            apiName = "navController.navigate(route)",
            codeSnippet = "navController.navigate(R.id.action_home_to_detail, bundleOf(\"id\" to itemId))",
            severity = Severity.P0_BLOCKER,
            migrationHint = "改用 navigator.navigateTo(DetailKey(itemId))",
            estimatedMinutes = 15
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/navigation/NavGraph.kt",
            lineNumber = 12,
            apiName = "navGraph.xml",
            codeSnippet = "<navigation xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:id=\"@+id/nav_graph\">",
            severity = Severity.P0_BLOCKER,
            migrationHint = "将 XML NavGraph 重写为 Kotlin DSL (entryProvider)",
            estimatedMinutes = 60
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/main/MainActivity.kt",
            lineNumber = 67,
            apiName = "NavBackStackEntry",
            codeSnippet = "navController.currentBackStackEntry.observe(this) { entry ->\n    updateBadge(entry.destination.id)\n}",
            severity = Severity.P1_HIGH,
            migrationHint = "改用 navigationState.currentEntryProvider.collectAsState()",
            estimatedMinutes = 20
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/main/MainActivity.kt",
            lineNumber = 89,
            apiName = "BottomNavigation + NavController",
            codeSnippet = "bottomNav.setOnItemSelectedListener { item ->\n    navController.navigate(item.itemId) { launchSingleTop = true }\n}",
            severity = Severity.P1_HIGH,
            migrationHint = "统一使用 navigator，BottomNavigation 不再持有独立 NavController",
            estimatedMinutes = 45
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/main/MainActivity.kt",
            lineNumber = 112,
            apiName = "navArgument + Safe Args",
            codeSnippet = "navArgument(name = \"userId\") { type = NavType.IntType }",
            severity = Severity.P1_HIGH,
            migrationHint = "改用 sealed class RouteKey 替代 Safe Args KSP 生成",
            estimatedMinutes = 30
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/navigation/NavGraph.kt",
            lineNumber = 56,
            apiName = "navArgument uriPatterns",
            codeSnippet = "uriPattern = listOf(\"https://myapp.com/user/{userId}\")",
            severity = Severity.P2_MEDIUM,
            migrationHint = "改用 Nav3 的 uris 参数格式",
            estimatedMinutes = 10
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/main/MainActivity.kt",
            lineNumber = 134,
            apiName = "popEnterTransition",
            codeSnippet = "popEnterTransition = fadeIn(animation.fastOutSlowIn)",
            severity = Severity.P2_MEDIUM,
            migrationHint = "使用 Nav3 SpatialTransitions API 重写",
            estimatedMinutes = 15
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/src/main/java/com/example/ui/detail/DetailFragment.kt",
            lineNumber = 23,
            apiName = "navController.getBackStackEntry(route)",
            codeSnippet = "val savedStateHandle = navController.getBackStackEntry(\"home\").savedStateHandle",
            severity = Severity.P2_MEDIUM,
            migrationHint = "改用 navigationState.snapshotEntry(HomeKey).savedStateHandle",
            estimatedMinutes = 15
        ),
        Nav2ApiUsage(
            id = UUID.randomUUID().toString(),
            filePath = "app/build.gradle.kts",
            lineNumber = 18,
            apiName = "Safe Args Plugin",
            codeSnippet = "id(\"androidx.navigation.safeargs.kotlin\")",
            severity = Severity.P3_LOW,
            migrationHint = "Nav3 无需 Safe Args，可移除该插件",
            estimatedMinutes = 5
        )
    )

    /**
     * Get migration recommendation based on scan results / 根据扫描结果给出迁移建议
     */
    private fun getMigrationRecommendation(issues: List<Nav2ApiUsage>): String {
        val p0Count = issues.count { it.severity == Severity.P0_BLOCKER }
        val p1Count = issues.count { it.severity == Severity.P1_HIGH }
        val totalMinutes = issues.sumOf { it.estimatedMinutes }

        return when {
            p0Count >= 5 -> "建议立即规划 Nav3 迁移。检测到 $p0Count 个阻塞级问题，迁移复杂度高，建议预留 ${(totalMinutes / 60).coerceAtLeast(1)} 周完整迁移时间。"
            p0Count >= 2 -> "建议在下一个版本中启动 Nav3 迁移。检测到 $p0Count 个阻塞级问题，可分阶段逐步迁移 BottomNavigation 和 NavHostFragment。"
            else -> "Nav3 迁移优先级中等。建议优先迁移 P0/P1 问题（$p0Count 个阻塞级 + $p1Count 个高优先级），总耗时约 ${totalMinutes} 分钟。"
        }
    }

    // =============================================================
    // Issue Selection / 问题选择
    // =============================================================
    /**
     * Handle issue selection / 处理问题选中
     *
     * @param issue Selected issue / 选中的问题
     */
    private fun handleSelectIssue(issue: Nav2ApiUsage) {
        _state.update { it.copy(selectedIssue = issue) }
    }

    /**
     * Handle issue clear / 处理问题清除
     */
    private fun handleClearIssue() {
        _state.update { it.copy(selectedIssue = null) }
    }

    // =============================================================
    // API Mapping / API 对照
    // =============================================================
    /**
     * Filter API mappings by category / 按分类过滤 API 对照表
     *
     * @param category Category to filter by / 要过滤的分类
     */
    private fun handleFilterApiMappings(category: String) {
        _state.update { it.copy(apiMappingFilter = category) }
    }

    // =============================================================
    // Template / 模板
    // =============================================================
    /**
     * Handle template copy / 处理模板复制
     *
     * @param template Template to copy / 要复制的模板
     */
    private fun handleCopyTemplate(template: CodeTemplate) {
        viewModelScope.launch {
            _effect.emit(NavToolEffect.CopyToClipboard(template.code, template.title))
            _effect.emit(NavToolEffect.ShowSnackbar("已复制: ${template.title}"))
        }
    }

    // =============================================================
    // Decision Tree / 决策树
    // =============================================================
    /**
     * Handle decision tree answer / 处理决策树答案
     *
     * @param answer Selected answer / 选中的答案
     * @param nextNodeId Next node ID / 下一节点 ID
     */
    private fun handleAnswerDecisionTree(answer: String, nextNodeId: String) {
        _state.update {
            it.copy(
                decisionTreeCurrentNodeId = nextNodeId,
                decisionTreeAnswers = it.decisionTreeAnswers + answer
            )
        }
    }

    /**
     * Reset decision tree / 重置决策树
     */
    private fun handleResetDecisionTree() {
        _state.update {
            it.copy(
                decisionTreeCurrentNodeId = "root",
                decisionTreeAnswers = emptyList(),
                decisionTreeResult = null
            )
        }
    }

    // =============================================================
    // Report Export / 报告导出
    // =============================================================
    /**
     * Handle scan report export / 处理扫描报告导出
     *
     * @param format Export format (json/markdown) / 导出格式
     */
    private fun handleExportScanReport(format: String) {
        viewModelScope.launch {
            val result = _state.value.scanResult ?: return@launch
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())

            val content = buildString {
                appendLine("# Nav2 → Nav3 迁移扫描报告")
                appendLine("> 生成时间: $timestamp")
                appendLine()
                appendLine("## 概览")
                appendLine("- 扫描文件数: ${result.totalFilesScanned}")
                appendLine("- 发现 Nav2 API 使用: ${result.totalIssuesFound} 处")
                appendLine("- navigation-compose 版本: ${result.nav2Version ?: "未知"}")
                appendLine()
                appendLine("### 严重程度分布")
                result.issuesBySeverity.forEach { (severity, count) ->
                    appendLine("- ${severity.label}: $count 处")
                }
                appendLine()
                appendLine("### 受影响文件")
                result.affectedFiles.forEach { file ->
                    appendLine("- $file")
                }
                appendLine()
                appendLine("## 迁移建议")
                appendLine(result.recommendation)
            }

            val fileName = "nav3-migration-report-$timestamp.${format}"
            val reportsDir = File(System.getProperty("user.home"), "Downloads").also { it.mkdirs() }
            val file = File(reportsDir, fileName)
            withContext(Dispatchers.IO) {
                file.writeText(content)
            }

            _effect.emit(NavToolEffect.ExportReport(file.absolutePath))
            _effect.emit(NavToolEffect.ShowSnackbar("报告已导出: $fileName"))
        }
    }

    // =============================================================
    // Error Handling / 错误处理
    // =============================================================
    /**
     * Handle dismiss error / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    // =============================================================
    // Lifecycle / 生命周期
    // =============================================================
    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
