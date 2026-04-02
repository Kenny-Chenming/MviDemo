package com.mvi.kenny.feature.android17migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ============================================================
 * MigrationViewModel — Android 17 迁移助手状态管理
 * ============================================================
 * Inherits ViewModel, holds MigrationState and MigrationEffect.
 *
 * State Management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, for UI layer subscription (collectAsState)
 *
 * Effect Management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * Why Channel instead of StateFlow for effects?
 * —————————————————————————————————————————————————————
 * StateFlow remembers current value, new subscribers receive last value.
 * Channel only forwards new events, suitable for "one-time" events (navigation, toast).
 *
 * @see MigrationState Page state definition
 * @see MigrationIntent User intentions
 * @see MigrationEffect Side effects
 * @see MigrationDashboardScreen Main dashboard UI
 * @see IssueDetailScreen Issue detail UI
 * @see WizardScreen Wizard flow UI
 */
class MigrationViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Page state (StateFlow, UI read-only) / 页面状态 */
    private val _state = MutableStateFlow(MigrationState.Initial)
    val state: StateFlow<MigrationState> = _state.asStateFlow()

    /** Current state snapshot for lambda access / 当前状态快照 */
    val currentState: MigrationState get() = _state.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel / 副作用通道 */
    private val _effect = Channel<MigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Receive and process user intent / 接收并处理用户意图
     *
     * Entry point, UI layer calls via viewModel.sendIntent(intent).
     * Dispatches to corresponding handler based on intent type.
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: MigrationIntent) {
        when (intent) {
            is MigrationIntent.SelectProject -> selectProject(intent.path)
            is MigrationIntent.StartScan -> startScan()
            is MigrationIntent.CancelScan -> cancelScan()
            is MigrationIntent.FixIssue -> fixIssue(intent.issueId)
            is MigrationIntent.RunAutoFix -> runAutoFix()
            is MigrationIntent.IgnoreIssue -> ignoreIssue(intent.issueId)
            is MigrationIntent.UnignoreIssue -> unignoreIssue(intent.issueId)
            is MigrationIntent.ExportReport -> exportReport(intent.format)
            is MigrationIntent.SetSeverityFilter -> setFilter(intent.severity)
            is MigrationIntent.SelectIssue -> selectIssue(intent.issueId)
            is MigrationIntent.ClearSelectedIssue -> clearSelectedIssue()
            is MigrationIntent.NavigateToWizard -> navigateToWizard(intent.issueId)
            is MigrationIntent.NextWizardStep -> nextWizardStep(intent.currentStep)
            is MigrationIntent.PrevWizardStep -> prevWizardStep(intent.currentStep)
            is MigrationIntent.CompleteWizard -> completeWizard(intent.issueId)
            is MigrationIntent.DismissError -> dismissError()
        }
    }

    // =============================================================
    // Project Selection
    // =============================================================
    /**
     * Select project folder / 选择项目文件夹
     *
     * @param path Project root path / 项目根路径
     */
    private fun selectProject(path: String) {
        _state.value = _state.value.copy(
            selectedProjectPath = path,
            scanStatus = ScanStatus.IDLE,
            scanProgress = 0f,
            error = null
        )
    }

    // =============================================================
    // Scan
    // =============================================================
    /** Flag to cancel ongoing scan / 取消正在进行的扫描 */
    @Volatile
    private var isScanCancelled = false

    /**
     * Start project scan / 开始项目扫描
     *
     * Simulates scanning Android project for API compatibility issues.
     * Uses coroutines with Dispatchers.IO for background processing.
     *
     * Note: This is a simulation. Real implementation would:
     * 1. Use SAF (Storage Access Framework) to access project files
     * 2. Parse build.gradle.kts files to identify modules
     * 3. Use lint tool or reflection to detect deprecated APIs
     * 4. Report progress in real-time
     */
    private fun startScan() {
        isScanCancelled = false
        viewModelScope.launch {
            _state.value = _state.value.copy(
                scanStatus = ScanStatus.SCANNING,
                scanProgress = 0f,
                scannedFilesCount = 0,
                totalFilesCount = 1024,
                error = null
            )

            try {
                // Simulate file scanning with progress updates
                // 模拟文件扫描，逐步更新进度
                val totalFiles = 1024
                val allIssues = mutableListOf<MigrationIssue>()
                val severityDistribution = listOf(
                    Severity.CRITICAL to 3,
                    Severity.WARNING to 7,
                    Severity.INFO to 5
                )

                for (i in 0 until totalFiles) {
                    if (isScanCancelled) {
                        _state.value = _state.value.copy(
                            scanStatus = ScanStatus.IDLE,
                            scanProgress = 0f
                        )
                        return@launch
                    }

                    // Simulate work per file
                    delay(5)

                    // Generate issues at certain files
                    if (i > 0 && i % 80 == 0) {
                        for ((severity, count) in severityDistribution) {
                            repeat(count) { idx ->
                                allIssues.add(
                                    generateMockIssue(severity, i, idx)
                                )
                            }
                        }
                    }

                    val progress = (i + 1).toFloat() / totalFiles
                    _state.value = _state.value.copy(
                        scanProgress = progress,
                        scannedFilesCount = i + 1,
                        totalFilesCount = totalFiles,
                        totalIssues = allIssues.size,
                        criticalCount = allIssues.count { it.severity == Severity.CRITICAL },
                        warningCount = allIssues.count { it.severity == Severity.WARNING },
                        infoCount = allIssues.count { it.severity == Severity.INFO },
                        issues = allIssues,
                        filteredIssues = applyFilter(allIssues, _state.value.currentFilter)
                    )
                }

                // Scan complete
                _state.value = _state.value.copy(
                    scanStatus = ScanStatus.DONE,
                    scanProgress = 1f
                )
                _effect.send(MigrationEffect.ScanComplete)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    scanStatus = ScanStatus.ERROR,
                    error = "Scan failed: ${e.message}"
                )
                _effect.send(MigrationEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * Generate mock issue for simulation / 生成模拟问题用于演示
     */
    private fun generateMockIssue(severity: Severity, fileIndex: Int, idx: Int): MigrationIssue {
        val issueTemplates = when (severity) {
            Severity.CRITICAL -> listOf(
                Triple(
                    "Implicit Pending Intent Mutability",
                    "隐式 Pending Intent 可变性",
                    "Starting from Android 12, you must explicitly specify whether a PendingIntent should be mutable. Use FLAG_IMMUTABLE or FLAG_MUTABLE."
                )
            )
            Severity.WARNING -> listOf(
                Triple(
                    "Deprecated WebView Methods",
                    "废弃的 WebView 方法",
                    "WebViewClient.onReceivedError() is deprecated. Use onReceivedError() with WebResourceRequest, or onReceivedHttpError()."
                ),
                Triple(
                    "Toast Callback",
                    "Toast 回调",
                    "Toast.setCallback() is deprecated in Android 11+. Avoid relying on Toast completion callbacks."
                )
            )
            Severity.INFO -> listOf(
                Triple(
                    "Foreground Service Type",
                    "前台服务类型",
                    "Starting Android 14, foreground services require a specific type. Add android:foregroundServiceType to your manifest."
                ),
                Triple(
                    "Non-SDK Interface Restriction",
                    "非 SDK 接口限制",
                    "Some internal APIs are restricted. Consider using public SDK alternatives."
                )
            )
        }

        val template = issueTemplates[idx % issueTemplates.size]
        return MigrationIssue(
            id = "${severity.name.lowercase()}_${fileIndex}_$idx",
            title = template.first,
            titleZh = template.second,
            description = "This API behavior has changed in Android 17 (API 36). ${template.third}",
            descriptionZh = "此 API 行为在 Android 17（API 36）中已变更。${template.third}",
            severity = severity,
            affectedFiles = Random.nextInt(1, 5),
            affectedMethods = Random.nextInt(1, 12),
            filePaths = listOf(
                "app/src/main/java/com/example/app/MainActivity.kt",
                "app/src/main/java/com/example/app/ServiceImpl.kt"
            ).take(Random.nextInt(1, 3)),
            fixSuggestion = when (severity) {
                Severity.CRITICAL -> """
                    |// Fix: Explicitly set PendingIntent mutability
                    |val pendingIntent = PendingIntent.getActivity(
                    |    context,
                    |    REQUEST_CODE,
                    |    intent,
                    |    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    |)
                """.trimMargin()
                Severity.WARNING -> """
                    |// Fix: Use updated WebViewClient callback
                    |webView.webViewClient = object : WebViewClient() {
                    |    override fun onReceivedError(
                    |        request: WebResourceRequest,
                    |        error: WebResourceError
                    |    ) {
                    |        // Handle error properly
                    |        Log.e("WebView", "Error: ${'$'}{error.description}")
                    |    }
                    |}
                """.trimMargin()
                Severity.INFO -> """
                    |// Fix: Add foregroundServiceType to manifest
                    |<service
                    |    android:name=".MyService"
                    |    android:foregroundServiceType="location"
                    |    android:exported="false" />
                """.trimMargin()
            },
            wizardSteps = severity.ordinal + 1
        )
    }

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun cancelScan() {
        isScanCancelled = true
        _state.value = _state.value.copy(
            scanStatus = ScanStatus.IDLE,
            scanProgress = 0f
        )
    }

    // =============================================================
    // Issue Fix
    // =============================================================
    /**
     * Fix a specific issue / 修复指定问题
     *
     * @param issueId Issue ID to fix / 待修复问题ID
     */
    private fun fixIssue(issueId: String) {
        viewModelScope.launch {
            updateIssue(issueId) { it.copy(isFixed = true) }
            _state.value = _state.value.copy(
                fixedCount = _state.value.fixedCount + 1
            )
            _effect.send(MigrationEffect.ShowToast("Issue fixed successfully / 问题已修复"))
        }
    }

    /**
     * Run auto-fix for all applicable issues / 自动修复所有适用问题
     */
    private fun runAutoFix() {
        viewModelScope.launch {
            val state = _state.value
            var fixedCount = 0
            state.issues
                .filter { !it.isFixed && !it.isIgnored }
                .forEach { issue ->
                    // In real implementation, this would call lint fix or AST editor
                    updateIssue(issue.id) { it.copy(isFixed = true) }
                    fixedCount++
                    delay(100) // Simulate per-issue processing
                }
            _state.value = _state.value.copy(
                fixedCount = _state.value.fixedCount + fixedCount
            )
            _effect.send(MigrationEffect.ShowToast("Auto-fix complete: $fixedCount issues resolved / 自动修复完成：已解决 $fixedCount 个问题"))
        }
    }

    /**
     * Ignore a specific issue / 忽略指定问题
     */
    private fun ignoreIssue(issueId: String) {
        viewModelScope.launch {
            updateIssue(issueId) { it.copy(isIgnored = true) }
            _state.value = _state.value.copy(
                ignoredCount = _state.value.ignoredCount + 1
            )
            _effect.send(MigrationEffect.ShowToast("Issue ignored / 问题已标记为忽略"))
        }
    }

    /**
     * Un-ignore an issue / 取消忽略问题
     */
    private fun unignoreIssue(issueId: String) {
        viewModelScope.launch {
            val issue = _state.value.issues.find { it.id == issueId } ?: return@launch
            if (issue.isIgnored) {
                updateIssue(issueId) { it.copy(isIgnored = false) }
                _state.value = _state.value.copy(
                    ignoredCount = maxOf(0, _state.value.ignoredCount - 1)
                )
            }
        }
    }

    /**
     * Update issue in state / 更新状态中的问题
     */
    private fun updateIssue(issueId: String, update: (MigrationIssue) -> MigrationIssue) {
        val updatedIssues = _state.value.issues.map {
            if (it.id == issueId) update(it) else it
        }
        _state.value = _state.value.copy(
            issues = updatedIssues,
            filteredIssues = applyFilter(updatedIssues, _state.value.currentFilter)
        )
    }

    // =============================================================
    // Export Report
    // =============================================================
    /**
     * Export migration report / 导出迁移报告
     *
     * @param format Export format / 导出格式
     */
    private fun exportReport(format: ExportFormat) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                // Simulate report generation
                delay(1500)
                val filePath = "/storage/emulated/0/Download/migration_report.${format.name.lowercase()}"
                _state.value = _state.value.copy(isExporting = false)
                _effect.send(MigrationEffect.ShareFile(filePath))
                _effect.send(MigrationEffect.ShowToast("Report exported: $filePath"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false)
                _effect.send(MigrationEffect.ShowError("Export failed: ${e.message}"))
            }
        }
    }

    // =============================================================
    // Filtering
    // =============================================================
    /**
     * Set severity filter / 设置严重程度过滤
     */
    private fun setFilter(severity: Severity?) {
        _state.value = _state.value.copy(
            currentFilter = severity,
            filteredIssues = applyFilter(_state.value.issues, severity)
        )
    }

    /**
     * Apply severity filter to issues list / 对问题列表应用严重程度过滤
     */
    private fun applyFilter(issues: List<MigrationIssue>, severity: Severity?): List<MigrationIssue> {
        return if (severity == null) {
            issues.filter { !it.isIgnored }
        } else {
            issues.filter { it.severity == severity && !it.isIgnored }
        }
    }

    // =============================================================
    // Navigation
    // =============================================================
    /**
     * Select issue to view detail / 选择查看详情的问题
     */
    private fun selectIssue(issueId: String) {
        _state.value = _state.value.copy(selectedIssueId = issueId)
        viewModelScope.launch {
            _effect.send(MigrationEffect.NavigateToDetail(issueId))
        }
    }

    /**
     * Clear selected issue / 清除选中问题
     */
    private fun clearSelectedIssue() {
        _state.value = _state.value.copy(selectedIssueId = null)
    }

    /**
     * Navigate to wizard to fix issue / 导航到向导页
     */
    private fun navigateToWizard(issueId: String) {
        _state.value = _state.value.copy(
            selectedIssueId = issueId,
            currentWizardStep = 0
        )
        viewModelScope.launch {
            _effect.send(MigrationEffect.NavigateToWizard(issueId))
        }
    }

    // =============================================================
    // Wizard Steps
    // =============================================================
    /**
     * Move to next wizard step / 进入下一步向导
     */
    private fun nextWizardStep(currentStep: Int) {
        val issue = _state.value.issues.find { it.id == _state.value.selectedIssueId }
        val maxSteps = issue?.wizardSteps ?: 1
        if (currentStep < maxSteps - 1) {
            _state.value = _state.value.copy(currentWizardStep = currentStep + 1)
        }
    }

    /**
     * Move to previous wizard step / 返回上一步向导
     */
    private fun prevWizardStep(currentStep: Int) {
        if (currentStep > 0) {
            _state.value = _state.value.copy(currentWizardStep = currentStep - 1)
        }
    }

    /**
     * Complete wizard and apply fix / 完成向导并应用修复
     */
    private fun completeWizard(issueId: String) {
        viewModelScope.launch {
            fixIssue(issueId)
            _state.value = _state.value.copy(
                selectedIssueId = null,
                currentWizardStep = 0
            )
        }
    }

    // =============================================================
    // Error Handling
    // =============================================================
    /**
     * Dismiss error message / 关闭错误信息
     */
    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }
}
