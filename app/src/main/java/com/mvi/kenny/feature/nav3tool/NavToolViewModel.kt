package com.mvi.kenny.feature.nav3tool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.nav3tool.NavToolState
import com.mvi.kenny.feature.nav3tool.NavToolIntent
import com.mvi.kenny.feature.nav3tool.NavToolEffect
import com.mvi.kenny.feature.nav3tool.NavToolContract
import com.mvi.kenny.feature.nav3tool.ScanProgress
import com.mvi.kenny.feature.nav3tool.ScanStatus
import com.mvi.kenny.feature.nav3tool.NavLifecycleEvent
import com.mvi.kenny.feature.nav3tool.BackstackSnapshot
import com.mvi.kenny.feature.nav3tool.BackstackItem
import com.mvi.kenny.feature.nav3tool.MigrationPreview
import com.mvi.kenny.feature.nav3tool.CodeChange
import com.mvi.kenny.feature.nav3tool.ChangeType
import com.mvi.kenny.feature.nav3tool.KmpPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * ============================================================
 * NavToolViewModel — Navigation 3 迁移工具 ViewModel
 * ============================================================
 * PRD-031 | Navigation 3 迁移与多平台工具套件
 *
 * 职责：
 * - 管理 NavToolState（UI 状态唯一真相来源）
 * - 接收 NavToolIntent（用户意图），执行业务逻辑，输出新状态
 * - 通过 NavToolEffect Channel 发送一次性副作用
 *
 * MVI 数据流：
 * Intent → ViewModel → State → Screen recomposition
 *              ↓
 *           Effect（Channel）
 *
 * @see NavToolContract MVI 契约定义
 * @see NavToolScreen UI 渲染层
 */
@OptIn(ExperimentalStdlibApi::class)
class NavToolViewModel : ViewModel() {

    // ============================================================
    // State Management / 状态管理
    // ============================================================

    /** 主状态流，UI 层 collect */
    private val _state = MutableStateFlow(NavToolState())
    val state: StateFlow<NavToolState> = _state.asStateFlow()

    // ============================================================
    // Effect Channel / 副作用通道
    // ============================================================

    /** 一次性副作用（Snackbar / 导航 / 文件操作） */
    private val _effect = Channel<NavToolEffect>(Channel.BUFFERED)
    val effect: Flow<NavToolEffect> = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing / 意图处理
    // ============================================================

    /**
     * 处理用户意图
     * 入口方法，由 Screen 层调用
     */
    fun processIntent(intent: NavToolIntent) {
        when (intent) {
            // ---------- Tab 切换 ----------
            is NavToolIntent.SwitchTab -> handleSwitchTab(intent.tab)

            // ---------- 检测报告 ----------
            is NavToolIntent.StartScan -> handleStartScan()
            is NavToolIntent.CancelScan -> handleCancelScan()
            is NavToolIntent.SelectIssue -> handleSelectIssue(intent.issue)
            is NavToolIntent.ExportScanReport -> handleExportScanReport()

            // ---------- 迁移引擎 ----------
            is NavToolIntent.ApplyMigration -> handleApplyMigration(intent.changes)
            is NavToolIntent.PreviewMigration -> handlePreviewMigration(intent.code)
            is NavToolIntent.ConfirmMigration -> handleConfirmMigration()
            is NavToolIntent.DiscardMigration -> handleDiscardMigration()

            // ---------- 可视化调试 ----------
            is NavToolIntent.RefreshBackstack -> handleRefreshBackstack()
            is NavToolIntent.SelectBackstackNode -> handleSelectBackstackNode(intent.nodeId)
            is NavToolIntent.CaptureSnapshot -> handleCaptureSnapshot()
            is NavToolIntent.PlayTransitionPreview -> handlePlayTransitionPreview()

            // ---------- 模板生成 ----------
            is NavToolIntent.UpdateRouteDef -> handleUpdateRouteDef(intent.yaml)
            is NavToolIntent.TogglePlatform -> handleTogglePlatform(intent.platform)
            is NavToolIntent.GenerateTemplate -> handleGenerateTemplate()
            is NavToolIntent.CopyTemplate -> handleCopyTemplate(intent.platform)

            // ---------- 快照回放 ----------
            is NavToolIntent.SelectSnapshot -> handleSelectSnapshot(intent.snapshotId)
            is NavToolIntent.PlaybackSnapshot -> handlePlaybackSnapshot()
            is NavToolIntent.PauseSnapshot -> handlePauseSnapshot()
            is NavToolIntent.StepForward -> handleStepForward()
            is NavToolIntent.StepBackward -> handleStepBackward()
            is NavToolIntent.ImportSnapshot -> handleImportSnapshot()
            is NavToolIntent.ExportSnapshot -> handleExportSnapshot()

            // ---------- 生命周期 ----------
            is NavToolIntent.StartLifecycleMonitor -> handleStartLifecycleMonitor()
            is NavToolIntent.StopLifecycleMonitor -> handleStopLifecycleMonitor()
            is NavToolIntent.ClearLifecycleLog -> handleClearLifecycleLog()
        }
    }

    // ============================================================
    // Tab Switch Handler / Tab 切换处理
    // ============================================================

    /**
     * 切换 Tab 时重置与当前 Tab 相关的状态
     * 保留跨 Tab 的通用状态（如 scanResults）
     */
    private fun handleSwitchTab(tab: NavToolTab) {
        _state.update { currentState ->
            currentState.copy(
                currentTab = tab,
                isLoading = false,
                error = null
                // migrationPreview / backstackSnapshot 等 Tab 专属状态按需保留
            )
        }
    }

    // ============================================================
    // Detection Tab Handlers / 检测报告模块
    // ============================================================

    /**
     * 启动 Nav 2 → Nav 3 扫描
     * 模拟扫描过程：更新 progress，显示 Snackbar 完成
     *
     * 注意：真实实现需要接入 Gradle 插件或直接解析项目文件。
     * 这里用模拟数据展示 UI 效果。
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanProgress = ScanProgress(status = ScanStatus.SCANNING, progress = 0f)) }

            // Simulate scanning progress
            for (i in 1..10) {
                if (_state.value.scanProgress.status == ScanStatus.IDLE) break
                delay(300)
                _state.update {
                    it.copy(
                        scanProgress = ScanProgress(
                            status = ScanStatus.SCANNING,
                            progress = i / 10f,
                            currentFile = "/path/to/project/src/main/java/com/example/MainActivity.kt"
                        )
                    )
                }
            }

            // Mock scan results / 模拟扫描结果
            val mockResults = listOf(
                NavFileIssue(
                    filePath = "/path/to/project/src/main/java/com/example/MainActivity.kt",
                    issueType = IssueType.NavHostFragment,
                    lineNumber = 42,
                    priority = MigrationPriority.P0,
                    estimatedMinutes = 30,
                    codeSnippet = "val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment"
                ),
                NavFileIssue(
                    filePath = "/path/to/project/src/main/res/navigation/nav_graph.xml",
                    issueType = IssueType.NavGraphXml,
                    lineNumber = 15,
                    priority = MigrationPriority.P0,
                    estimatedMinutes = 45,
                    codeSnippet = "<fragment android:id=\"@+id/nav_home\" android:name=\"com.example.HomeFragment\" />"
                ),
                NavFileIssue(
                    filePath = "/path/to/project/src/main/java/com/example/HomeFragmentArgs.kt",
                    issueType = IssueType.NavArgs,
                    lineNumber = 8,
                    priority = MigrationPriority.P1,
                    estimatedMinutes = 15,
                    codeSnippet = "data class HomeFragmentArgs(val userId: String)"
                ),
                NavFileIssue(
                    filePath = "/path/to/project/src/main/res/navigation/nav_graph.xml",
                    issueType = IssueType.NavDeepLink,
                    lineNumber = 30,
                    priority = MigrationPriority.P2,
                    estimatedMinutes = 10,
                    codeSnippet = "<deepLink android:id=\"@+id/deeplink_home\" app:uri=\"myapp://home/{userId}\" />"
                )
            )

            _state.update {
                it.copy(
                    scanProgress = ScanProgress(status = ScanStatus.COMPLETE, progress = 1f),
                    scanResults = mockResults
                )
            }

            _effect.send(NavToolEffect.ShowSnackbar("扫描完成，发现 ${mockResults.size} 个待迁移文件"))
        }
    }

    /**
     * 取消正在进行的扫描
     */
    private fun handleCancelScan() {
        _state.update {
            it.copy(scanProgress = ScanProgress(status = ScanStatus.IDLE, progress = 0f))
        }
    }

    /**
     * 选中扫描问题项，触发 IDE 跳转
     */
    private fun handleSelectIssue(issue: NavFileIssue) {
        viewModelScope.launch {
            _effect.send(NavToolEffect.OpenFile(issue.filePath, issue.lineNumber))
        }
    }

    /**
     * 导出扫描报告
     */
    private fun handleExportScanReport() {
        viewModelScope.launch {
            val report = buildString {
                appendLine("# Navigation 2 → 3 迁移检测报告")
                appendLine()
                _state.value.scanResults.forEach { issue ->
                    appendLine("## ${issue.filePath}:${issue.lineNumber}")
                    appendLine("- 类型: ${issue.issueType}")
                    appendLine("- 优先级: ${issue.priority.label}")
                    appendLine("- 预估耗时: ${issue.estimatedMinutes} 分钟")
                    appendLine("```")
                    appendLine(issue.codeSnippet)
                    appendLine("```")
                    appendLine()
                }
            }
            _effect.send(NavToolEffect.ExportReport("/tmp/nav3-migration-report.md"))
            _effect.send(NavToolEffect.CopyToClipboard(report, "迁移检测报告"))
        }
    }

    // ============================================================
    // Migration Tab Handlers / 迁移引擎模块
    // ============================================================

    /**
     * 预览迁移效果
     * 模拟 AI 迁移逻辑：将字符串路由转换为 @Serializable data class 路由
     */
    private fun handlePreviewMigration(code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(800) // Simulate AI processing

            // Mock migration transformation
            val migratedCode = code
                .replace("\"home\"", "HomeRoute")
                .replace("\"detail/{id}\"", "DetailRoute(id: String)")

            val preview = MigrationPreview(
                originalCode = code,
                migratedCode = migratedCode,
                changes = listOf(
                    CodeChange(
                        lineNumber = 1,
                        changeType = ChangeType.MODIFY,
                        originalLine = "destination = \"home\"",
                        newLine = "destination = HomeRoute",
                        explanation = "字符串路由 → @Serializable data class 路由（类型安全）"
                    )
                ),
                hasAiSuggestion = true,
                migrationNotes = "已将 1 处字符串路由替换为强类型路由定义，建议配合 kotlin.serialization 进行使用。"
            )

            _state.update { it.copy(isLoading = false, migrationPreview = preview) }
        }
    }

    /**
     * 应用迁移变更
     */
    private fun handleApplyMigration(changes: List<CodeChange>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)
            _state.update { it.copy(isLoading = false) }
            _effect.send(NavToolEffect.ShowSnackbar("已应用 ${changes.size} 处迁移变更"))
        }
    }

    /**
     * 确认迁移（提交）
     */
    private fun handleConfirmMigration() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("迁移已确认，代码已更新"))
            _state.update { it.copy(migrationPreview = null) }
        }
    }

    /**
     * 放弃迁移预览
     */
    private fun handleDiscardMigration() {
        _state.update { it.copy(migrationPreview = null) }
    }

    // ============================================================
    // Visualizer Tab Handlers / 可视化调试模块
    // ============================================================

    /**
     * 刷新 BackStack 可视化数据
     */
    private fun handleRefreshBackstack() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(400)

            val mockBackstack = listOf(
                BackstackItem("1", "NavHost", true, null, 0, "CREATE"),
                BackstackItem("2", "HomeRoute", true, "1", 1, "navigate"),
                BackstackItem("3", "DetailRoute(id=42)", false, "2", 2, "navigate"),
                BackstackItem("4", "ProfileRoute", false, "2", 2, "navigate")
            )

            val snapshot = BackstackSnapshot(
                snapshotId = "snapshot-${System.currentTimeMillis()}",
                timestamp = System.currentTimeMillis(),
                backstackItems = mockBackstack,
                currentRoute = "HomeRoute",
                navDisplayState = "Lifecycle.STATE_STARTED"
            )

            _state.update { it.copy(isLoading = false, backstackSnapshot = snapshot) }
        }
    }

    /**
     * 选中 BackStack 节点
     */
    private fun handleSelectBackstackNode(nodeId: String) {
        // Highlight the selected node in the visualizer
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("已选中节点: $nodeId"))
        }
    }

    /**
     * 捕获当前快照
     */
    private fun handleCaptureSnapshot() {
        viewModelScope.launch {
            handleRefreshBackstack()
            delay(500)
            _effect.send(NavToolEffect.ShowSnackbar("快照已捕获"))
        }
    }

    /**
     * 播放过渡动画预览
     */
    private fun handlePlayTransitionPreview() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("动画预览：HomeRoute → DetailRoute"))
        }
    }

    // ============================================================
    // Template Tab Handlers / 模板生成模块
    // ============================================================

    /**
     * 更新路由定义编辑器内容
     */
    private fun handleUpdateRouteDef(yaml: String) {
        _state.update { it.copy(routeDefEditor = yaml) }
    }

    /**
     * 切换目标平台勾选状态
     */
    private fun handleTogglePlatform(platform: KmpPlatform) {
        _state.update { currentState ->
            val newPlatforms = if (platform in currentState.selectedPlatforms) {
                currentState.selectedPlatforms - platform
            } else {
                currentState.selectedPlatforms + platform
            }
            currentState.copy(selectedPlatforms = newPlatforms)
        }
    }

    /**
     * 生成 KMP 路由模板代码
     */
    private fun handleGenerateTemplate() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(600)

            val platformTemplates = mutableMapOf<KmpPlatform, String>()

            KmpPlatform.entries.forEach { platform ->
                if (platform in _state.value.selectedPlatforms) {
                    platformTemplates[platform] = when (platform) {
                        KmpPlatform.ANDROID -> buildAndroidTemplate()
                        KmpPlatform.IOS -> buildIosTemplate()
                        KmpPlatform.DESKTOP -> buildDesktopTemplate()
                        KmpPlatform.WEB -> buildWebTemplate()
                    }
                }
            }

            _state.update { it.copy(isLoading = false, generatedTemplate = platformTemplates) }
            _effect.send(NavToolEffect.ShowSnackbar("模板生成完成，共 ${platformTemplates.size} 个平台"))
        }
    }

    private fun buildAndroidTemplate(): String = """
        |// Android — Navigation 3 + Kotlin Serialization
        |@Serializable
        |object HomeRoute
        |
        |@Serializable
        |data class DetailRoute(val id: String)
        |
        |val navGraph = NavHostGraph(
        |    startDestination = HomeRoute,
        |    routes = listOf(HomeRoute, DetailRoute)
        |)
    """.trimMargin()

    private fun buildIosTemplate(): String = """
        |// iOS — SwiftUI NavigationPath (Kotlin Multiplatform)
        |struct HomeRoute: Codable, Hashable {}
        |
        |struct DetailRoute: Codable, Hashable {
        |    let id: String
        |}
        |
        |typealias NavPath = NavigationPath
    """.trimMargin()

    private fun buildDesktopTemplate(): String = """
        |// Desktop — Default Compose Navigation
        |object HomeRoute
        |
        |data class DetailRoute(val id: String)
        |
        |// Desktop uses standard NavHost pattern
    """.trimMargin()

    private fun buildWebTemplate(): String = """
        |// Web — popbackstack() semantic
        |// Navigation 3 Web 支持 popbackstack() 语义
        |object HomeRoute
        |
        |data class DetailRoute(val id: String)
        |
        |// 建议使用 navigation-compose-web 适配器
    """.trimMargin()

    /**
     * 复制模板到剪贴板
     */
    private fun handleCopyTemplate(platform: KmpPlatform) {
        viewModelScope.launch {
            val template = _state.value.generatedTemplate[platform] ?: ""
            _effect.send(NavToolEffect.CopyToClipboard(template, "${platform.displayName} 路由模板"))
            _effect.send(NavToolEffect.ShowSnackbar("${platform.displayName} 模板已复制"))
        }
    }

    // ============================================================
    // Snapshot Tab Handlers / 快照回放模块
    // ============================================================

    /**
     * 选中历史快照
     */
    private fun handleSelectSnapshot(snapshotId: String) {
        _state.update { it.copy(selectedSnapshotId = snapshotId) }
    }

    /**
     * 开始快照回放
     */
    private fun handlePlaybackSnapshot() {
        _state.update { it.copy(isPlayingSnapshot = true) }
    }

    /**
     * 暂停快照回放
     */
    private fun handlePauseSnapshot() {
        _state.update { it.copy(isPlayingSnapshot = false) }
    }

    /**
     * 快照步进（前进）
     */
    private fun handleStepForward() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("回放步进 +1"))
        }
    }

    /**
     * 快照步进（后退）
     */
    private fun handleStepBackward() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("回放步进 -1"))
        }
    }

    /**
     * 导入快照
     */
    private fun handleImportSnapshot() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("快照导入功能开发中"))
        }
    }

    /**
     * 导出快照
     */
    private fun handleExportSnapshot() {
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("快照已导出至 /tmp/nav3-snapshot.json"))
        }
    }

    // ============================================================
    // Lifecycle Tab Handlers / 生命周期调试模块
    // ============================================================

    /**
     * 启动生命周期监控
     */
    private fun handleStartLifecycleMonitor() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Simulate lifecycle events
            val events = listOf(
                NavLifecycleEvent("evt-1", System.currentTimeMillis(), "STARTED", "STARTED", "navigate(home)"),
                NavLifecycleEvent("evt-2", System.currentTimeMillis() + 100, "RESUMED", "RESUMED", "HomeScreen onResume"),
                NavLifecycleEvent("evt-3", System.currentTimeMillis() + 500, "CREATED", "DESTROYED", "navigate(detail)"),
                NavLifecycleEvent("evt-4", System.currentTimeMillis() + 600, "STARTED", "STARTED", "DetailScreen onStart")
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    lifecycleEvents = events
                )
            }

            _effect.send(NavToolEffect.ShowSnackbar("生命周期监控已启动"))
        }
    }

    /**
     * 停止生命周期监控
     */
    private fun handleStopLifecycleMonitor() {
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            _effect.send(NavToolEffect.ShowSnackbar("生命周期监控已停止"))
        }
    }

    /**
     * 清空生命周期日志
     */
    private fun handleClearLifecycleLog() {
        _state.update { it.copy(lifecycleEvents = emptyList()) }
    }
}
