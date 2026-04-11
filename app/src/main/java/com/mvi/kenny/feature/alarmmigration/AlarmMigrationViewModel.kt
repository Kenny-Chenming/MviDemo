package com.mvi.kenny.feature.alarmmigration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

/**
 * ============================================================
 * AlarmMigrationViewModel — AlarmManager Listener Mode 迁移工具状态管理
 * ============================================================
 * Inherits ViewModel, holds AlarmScanState, AlarmMigrationState, AlarmReportState.
 *
 * State Management:
 * - _scanState: Scan state (StateFlow) / 扫描状态
 * - _migrationState: Migration state (StateFlow) / 迁移状态
 * - _reportState: Report state (StateFlow) / 报告状态
 * - _effect: Channel (hot flow) for one-time side effects / 一次性副作用通道
 *
 * Core Components:
 * - AlarmUsageScanner: AST-based AlarmManager PendingIntent usage detector
 * - PendingIntentToListenerConverter: PendingIntent → OnAlarmListener code converter
 * - RegressionTestGenerator: Test case generator for migrated code
 * - HtmlReportGenerator: HTML report generator for scan/migration results
 *
 * @see AlarmMigrationContract MVI contract definitions
 * @see AlarmMigrationScreen Main screen UI
 * @see HtmlReportPreview HTML report preview component
 */
class AlarmMigrationViewModel : ViewModel() {

    // =============================================================
    // State
    // =============================================================
    /** Scan state (StateFlow, UI read-only) / 扫描状态 */
    private val _scanState = MutableStateFlow(AlarmScanState.Initial)
    val scanState: StateFlow<AlarmScanState> = _scanState.asStateFlow()

    /** Migration state (StateFlow, UI read-only) / 迁移状态 */
    private val _migrationState = MutableStateFlow(AlarmMigrationState.Initial)
    val migrationState: StateFlow<AlarmMigrationState> = _migrationState.asStateFlow()

    /** Report state (StateFlow, UI read-only) / 报告状态 */
    private val _reportState = MutableStateFlow(AlarmReportState())
    val reportState: StateFlow<AlarmReportState> = _reportState.asStateFlow()

    /** Current scan state snapshot / 当前扫描状态快照 */
    val currentScanState: AlarmScanState get() = _scanState.value

    /** Current migration state snapshot / 当前迁移状态快照 */
    val currentMigrationState: AlarmMigrationState get() = _migrationState.value

    // =============================================================
    // Effect
    // =============================================================
    /** Effect Channel (hot flow, buffered) / 副作用通道 */
    private val _effect = Channel<AlarmMigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Receive and process user intent / 接收并处理用户意图
     *
     * Entry point, UI layer calls via viewModel.sendIntent(intent).
     * 根据意图类型分发到对应的处理函数。
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: AlarmMigrationIntent) {
        when (intent) {
            is AlarmMigrationIntent.SetModulePath -> setModulePath(intent.path)
            is AlarmMigrationIntent.StartScan -> startScan()
            is AlarmMigrationIntent.CancelScan -> cancelScan()
            is AlarmMigrationIntent.SetSeverityFilter -> setSeverityFilter(intent.severity)
            is AlarmMigrationIntent.SetFilePathFilter -> setFilePathFilter(intent.filter)
            is AlarmMigrationIntent.ToggleShowFixed -> toggleShowFixed(intent.show)
            is AlarmMigrationIntent.ToggleShowIgnored -> toggleShowIgnored(intent.show)
            is AlarmMigrationIntent.GeneratePreview -> generatePreview(intent.issues)
            is AlarmMigrationIntent.ExecuteMigration -> executeMigration(intent.changes)
            is AlarmMigrationIntent.Rollback -> rollback()
            is AlarmMigrationIntent.IgnoreIssue -> ignoreIssue(intent.issueId)
            is AlarmMigrationIntent.UnignoreIssue -> unignoreIssue(intent.issueId)
            is AlarmMigrationIntent.GenerateReport -> generateReport(intent.scanState)
            is AlarmMigrationIntent.OpenReport -> openReport(intent.reportPath)
            is AlarmMigrationIntent.DiffScans -> diffScans(intent.beforePath, intent.afterPath)
            is AlarmMigrationIntent.DismissError -> dismissError()
        }
    }

    // =============================================================
    // Module Path
    // =============================================================
    /**
     * Set module path for scan / 设置扫描模块路径
     *
     * @param path Module root path / 模块根路径
     */
    private fun setModulePath(path: String) {
        _scanState.value = _scanState.value.copy(
            modulePath = path,
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
     * Start alarm usage scan / 开始告警使用扫描
     *
     * Simulates AST-based AlarmManager PendingIntent usage scanning.
     * 模拟基于 AST 的 AlarmManager PendingIntent 用法扫描。
     *
     * In real implementation, this would:
     * 1. Traverse all Kotlin/Java source files in the module
     * 2. Parse AST to detect AlarmManager API calls
     * 3. Detect PendingIntent.getService/getBroadcast/getActivity usage
     * 4. Classify by severity (CRITICAL/WARNING/INFO)
     * 5. Generate fix recommendations
     *
     * 真实实现会：
     * 1. 遍历模块中的所有 Kotlin/Java 源文件
     * 2. 解析 AST 检测 AlarmManager API 调用
     * 3. 检测 PendingIntent.getService/getBroadcast/getActivity 用法
     * 4. 按严重程度分类（CRITICAL/WARNING/INFO）
     * 5. 生成修复建议
     */
    private fun startScan() {
        isScanCancelled = false
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _scanState.value = _scanState.value.copy(
                scanStatus = AlarmScanStatus.SCANNING,
                scanProgress = 0f,
                error = null,
                results = emptyList()
            )

            try {
                // Simulate file scanning with progress updates
                // 模拟文件扫描，逐步更新进度
                val totalFiles = 256
                val allIssues = mutableListOf<AlarmUsageIssue>()

                // Severity distribution for simulation
                // 模拟用的严重程度分布
                val issueDistribution = listOf(
                    Triple(AlarmSeverity.CRITICAL, 2, listOf(AlarmApiType.SET_EXACT, AlarmApiType.SET_REPEATING)),
                    Triple(AlarmSeverity.WARNING, 4, listOf(AlarmApiType.SET, AlarmApiType.SET_AND_WHILE_IDLE)),
                    Triple(AlarmSeverity.INFO, 3, listOf(AlarmApiType.SET_EXACT))
                )

                for (i in 0 until totalFiles) {
                    if (isScanCancelled) {
                        _scanState.value = _scanState.value.copy(
                            scanStatus = AlarmScanStatus.IDLE,
                            scanProgress = 0f,
                            results = emptyList()
                        )
                        return@launch
                    }

                    // Simulate work per file
                    delay(8)

                    // Generate issues at certain file intervals
                    // 按一定间隔生成问题
                    if (i > 0 && i % 32 == 0) {
                        for ((severity, count, apiTypes) in issueDistribution) {
                            repeat(count) { idx ->
                                val apiType = apiTypes[idx % apiTypes.size]
                                allIssues.add(
                                    generateMockIssue(severity, apiType, i, idx)
                                )
                            }
                        }
                    }

                    val progress = (i + 1).toFloat() / totalFiles
                    _scanState.value = _scanState.value.copy(
                        scanProgress = progress,
                        results = allIssues,
                        summary = ScanSummary(
                            total = allIssues.size,
                            critical = allIssues.count { it.severity == AlarmSeverity.CRITICAL },
                            warning = allIssues.count { it.severity == AlarmSeverity.WARNING },
                            info = allIssues.count { it.severity == AlarmSeverity.INFO },
                            fixed = allIssues.count { it.isFixed },
                            ignored = allIssues.count { it.isIgnored },
                            filesScanned = i + 1,
                            scanDurationMs = System.currentTimeMillis() - startTime
                        )
                    )
                }

                // Scan complete
                val duration = System.currentTimeMillis() - startTime
                _scanState.value = _scanState.value.copy(
                    scanStatus = AlarmScanStatus.COMPLETED,
                    scanProgress = 1f,
                    summary = _scanState.value.summary.copy(scanDurationMs = duration)
                )
                _effect.send(AlarmMigrationEffect.ScanComplete)

            } catch (e: Exception) {
                _scanState.value = _scanState.value.copy(
                    scanStatus = AlarmScanStatus.ERROR,
                    error = "Scan failed: ${e.message}"
                )
                _effect.send(AlarmMigrationEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * Generate mock issue for simulation / 生成模拟问题用于演示
     *
     * Creates a realistic-looking AlarmManager issue based on severity and API type.
     * 根据严重程度和 API 类型生成看起来真实的 AlarmManager 问题。
     */
    private fun generateMockIssue(
        severity: AlarmSeverity,
        apiType: AlarmApiType,
        fileIndex: Int,
        idx: Int
    ): AlarmUsageIssue {
        val pendingIntentTypes = listOf("getService", "getBroadcast", "getActivity")
        val piType = pendingIntentTypes[idx % pendingIntentTypes.size]

        val methodNames = listOf(
            "scheduleAlarm", "setReminder", "startBackgroundTask",
            "scheduleNotification", "triggerBackup", "initAlarm"
        )

        val sampleCode = when (apiType) {
            AlarmApiType.SET_EXACT -> """
                |alarmManager.setExact(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    PendingIntent.get$piType(
                |        context, REQUEST_CODE, intent,
                |        PendingIntent.FLAG_UPDATE_CURRENT
                |    )
                |)
            """.trimMargin()

            AlarmApiType.SET_REPEATING -> """
                |alarmManager.setRepeating(
                |    AlarmManager.RTC_WAKEUP,
                |    firstTrigger,
                |    INTERVAL_HOUR,
                |    PendingIntent.get$piType(
                |        context, REQUEST_CODE, intent,
                |        PendingIntent.FLAG_UPDATE_CURRENT
                |    )
                |)
            """.trimMargin()

            AlarmApiType.SET -> """
                |alarmManager.set(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    PendingIntent.get$piType(
                |        context, REQUEST_CODE, intent,
                |        PendingIntent.FLAG_NO_CREATE
                |    )
                |)
            """.trimMargin()

            AlarmApiType.SET_AND_WHILE_IDLE -> """
                |alarmManager.setAndWhileIdleUntil(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    PendingIntent.get$piType(
                |        context, REQUEST_CODE, intent,
                |        PendingIntent.FLAG_UPDATE_CURRENT
                |    )
                |)
            """.trimMargin()
        }

        val recommendedFix = when (apiType) {
            AlarmApiType.SET_EXACT -> """
                |// Migrated to OnAlarmListener (Android 17+)
                |val listener = object : OnAlarmListener() {
                |    override fun onAlarm() {
                |        // Handle alarm trigger
                |        intent?.let { ctx.startService(it) }
                |    }
                |}
                |alarmManager.setExactAndUntilWhileIdle(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    listener
                |)
            """.trimMargin()

            AlarmApiType.SET_REPEATING -> """
                |// Recommended: Migrate to WorkManager (setRepeating is heavily restricted since Android 12)
                |val workRequest = PeriodicWorkRequestBuilder<MyWorker>(
                |    INTERVAL_HOUR, TimeUnit.MILLISECONDS
                |).build()
                |WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                |    "alarm_work",
                |    ExistingPeriodicWorkPolicy.KEEP,
                |    workRequest
                |)
            """.trimMargin()

            AlarmApiType.SET -> """
                |// Migrated to OnAlarmListener (Android 17+)
                |val listener = object : OnAlarmListener() {
                |    override fun onAlarm() {
                |        intent?.let { ctx.startService(it) }
                |    }
                |}
                |alarmManager.setAndUntilIdle(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    listener
                |)
            """.trimMargin()

            AlarmApiType.SET_AND_WHILE_IDLE -> """
                |// Already using setAndWhileIdleUntil — migrate PendingIntent to OnAlarmListener
                |val listener = object : OnAlarmListener() {
                |    override fun onAlarm() {
                |        intent?.let { ctx.startService(it) }
                |    }
                |}
                |alarmManager.setAndUntilIdle(
                |    AlarmManager.RTC_WAKEUP,
                |    triggerTime,
                |    listener
                |)
            """.trimMargin()
        }

        return AlarmUsageIssue(
            id = "alarm_${fileIndex}_${idx}",
            filePath = "app/src/main/java/com/example/app/${methodNames[idx % methodNames.size]}.kt",
            lineNumber = 20 + (idx * 5),
            methodName = methodNames[idx % methodNames.size],
            apiType = apiType,
            severity = severity,
            pendingIntentType = piType,
            codeSnippet = sampleCode,
            recommendedFix = recommendedFix
        )
    }

    /**
     * Cancel ongoing scan / 取消正在进行的扫描
     */
    private fun cancelScan() {
        isScanCancelled = true
        _scanState.value = _scanState.value.copy(
            scanStatus = AlarmScanStatus.IDLE,
            scanProgress = 0f
        )
    }

    // =============================================================
    // Filtering
    // =============================================================
    /**
     * Set severity filter / 设置严重程度过滤
     */
    private fun setSeverityFilter(severity: AlarmSeverity?) {
        _scanState.value = _scanState.value.copy(
            filterCriteria = _scanState.value.filterCriteria.copy(severity = severity)
        )
    }

    /**
     * Set file path filter / 设置文件路径过滤
     */
    private fun setFilePathFilter(filter: String?) {
        _scanState.value = _scanState.value.copy(
            filterCriteria = _scanState.value.filterCriteria.copy(filePathFilter = filter)
        )
    }

    /**
     * Toggle show fixed issues / 切换显示已修复问题
     */
    private fun toggleShowFixed(show: Boolean) {
        _scanState.value = _scanState.value.copy(
            filterCriteria = _scanState.value.filterCriteria.copy(showFixed = show)
        )
    }

    /**
     * Toggle show ignored issues / 切换显示已忽略问题
     */
    private fun toggleShowIgnored(show: Boolean) {
        _scanState.value = _scanState.value.copy(
            filterCriteria = _scanState.value.filterCriteria.copy(showIgnored = show)
        )
    }

    // =============================================================
    // Preview & Migration
    // =============================================================
    /**
     * Generate preview of pending migration changes (dry-run).
     * 生成待迁移变更预览（dry-run 预览模式）
     *
     * Shows all changes that will be made without actually applying them.
     * 显示将要进行的所有更改，但不实际应用。
     *
     * @param issues Issues to migrate / 要迁移的问题列表
     */
    private fun generatePreview(issues: List<AlarmUsageIssue>) {
        viewModelScope.launch {
            val pendingChanges = issues
                .filter { !it.isFixed && !it.isIgnored }
                .map { issue ->
                    PendingChange(
                        issueId = issue.id,
                        filePath = issue.filePath,
                        originalCode = issue.codeSnippet,
                        migratedCode = issue.recommendedFix,
                        backupComment = "// KAIFU_BACKUP: ${issue.codeSnippet.take(50)}..."
                    )
                }

            _migrationState.value = _migrationState.value.copy(
                migrationStatus = AlarmMigrationStatus.PREVIEW,
                pendingChanges = pendingChanges,
                blockedFiles = emptyList(),
                error = null
            )
        }
    }

    /**
     * Execute migration (after preview confirmation).
     * 执行迁移（预览确认后）
     *
     * Applies all pending changes, creates backup comments, and tracks rollback info.
     * 应用所有待处理更改，创建备份注释，并跟踪回滚信息。
     *
     * @param changes Changes to apply / 要应用的变更列表
     */
    private fun executeMigration(changes: List<PendingChange>) {
        viewModelScope.launch {
            _migrationState.value = _migrationState.value.copy(
                migrationStatus = AlarmMigrationStatus.MIGRATING,
                migrationProgress = 0f
            )

            try {
                val backupPaths = mutableMapOf<String, String>()
                val completedFiles = mutableListOf<String>()
                val blockedFiles = mutableListOf<BlockedFile>()

                changes.forEachIndexed { index, change ->
                    // Simulate file write operation
                    // 模拟文件写入操作
                    delay(100)

                    // In real implementation:
                    // 1. Read original file content
                    // 2. Create backup with KAIFU_BACKUP comment
                    // 3. Replace originalCode with migratedCode
                    // 4. Write back to file

                    // Simulate success for most files
                    // 模拟大部分文件成功
                    val success = Random.nextFloat() > 0.05f // 5% failure rate

                    if (success) {
                        backupPaths[change.filePath] = change.originalCode
                        completedFiles.add(change.filePath)
                    } else {
                        blockedFiles.add(
                            BlockedFile(
                                filePath = change.filePath,
                                reason = "File is read-only or locked by another process",
                                suggestedAction = "Check file permissions or close other editors"
                            )
                        )
                    }

                    _migrationState.value = _migrationState.value.copy(
                        migrationProgress = (index + 1).toFloat() / changes.size,
                        completedFiles = completedFiles.toList(),
                        blockedFiles = blockedFiles.toList()
                    )
                }

                // Mark all issues as fixed
                // 将所有问题标记为已修复
                val fixedIds = changes.map { it.issueId }.toSet()
                val updatedResults = _scanState.value.results.map { issue ->
                    if (issue.id in fixedIds) issue.copy(isFixed = true) else issue
                }

                _scanState.value = _scanState.value.copy(
                    results = updatedResults,
                    summary = _scanState.value.summary.copy(
                        fixed = _scanState.value.summary.fixed + completedFiles.size
                    )
                )

                _migrationState.value = _migrationState.value.copy(
                    migrationStatus = AlarmMigrationStatus.COMPLETED,
                    pendingChanges = emptyList(),
                    rollbackAvailable = backupPaths.isNotEmpty(),
                    lastBackupPaths = backupPaths,
                    migrationProgress = 1f
                )

                _effect.send(AlarmMigrationEffect.MigrationComplete)
                _effect.send(
                    AlarmMigrationEffect.ShowToast(
                        "Migration complete: ${completedFiles.size} files migrated, ${blockedFiles.size} blocked"
                    )
                )

            } catch (e: Exception) {
                _migrationState.value = _migrationState.value.copy(
                    migrationStatus = AlarmMigrationStatus.ERROR,
                    error = "Migration failed: ${e.message}"
                )
                _effect.send(AlarmMigrationEffect.ShowError("迁移失败: ${e.message}"))
            }
        }
    }

    /**
     * Rollback last migration / 回滚上次迁移
     *
     * Restores original code from backup comments.
     * 从备份注释恢复原始代码。
     */
    private fun rollback() {
        viewModelScope.launch {
            val backupPaths = _migrationState.value.lastBackupPaths
            if (backupPaths.isEmpty()) {
                _effect.send(AlarmMigrationEffect.ShowError("No rollback available / 无可回滚内容"))
                return@launch
            }

            _migrationState.value = _migrationState.value.copy(
                migrationStatus = AlarmMigrationStatus.MIGRATING
            )

            try {
                // In real implementation, restore original code from backup
                // 真实实现：从备份恢复原始代码
                delay(500)

                // Mark issues as not fixed
                // 将问题标记为未修复
                val rolledBackIds = backupPaths.keys.map { "alarm_${it.hashCode()}" }
                val updatedResults = _scanState.value.results.map { issue ->
                    if (rolledBackIds.any { id -> issue.id.startsWith(id.take(10)) }) {
                        issue.copy(isFixed = false)
                    } else issue
                }

                _scanState.value = _scanState.value.copy(
                    results = updatedResults,
                    summary = _scanState.value.summary.copy(
                        fixed = maxOf(0, _scanState.value.summary.fixed - backupPaths.size)
                    )
                )

                _migrationState.value = _migrationState.value.copy(
                    migrationStatus = AlarmMigrationStatus.ROLLED_BACK,
                    rollbackAvailable = false,
                    lastBackupPaths = emptyMap(),
                    completedFiles = emptyList()
                )

                _effect.send(AlarmMigrationEffect.RollbackComplete)
                _effect.send(AlarmMigrationEffect.ShowToast("Rollback complete / 回滚完成"))

            } catch (e: Exception) {
                _migrationState.value = _migrationState.value.copy(
                    migrationStatus = AlarmMigrationStatus.ERROR,
                    error = "Rollback failed: ${e.message}"
                )
                _effect.send(AlarmMigrationEffect.ShowError("回滚失败: ${e.message}"))
            }
        }
    }

    // =============================================================
    // Issue Management
    // =============================================================
    /**
     * Ignore a specific issue / 忽略指定问题
     */
    private fun ignoreIssue(issueId: String) {
        viewModelScope.launch {
            updateIssue(issueId) { it.copy(isIgnored = true) }
            _scanState.value = _scanState.value.copy(
                summary = _scanState.value.summary.copy(
                    ignored = _scanState.value.summary.ignored + 1
                )
            )
            _effect.send(AlarmMigrationEffect.ShowToast("Issue ignored / 问题已标记为忽略"))
        }
    }

    /**
     * Un-ignore an issue / 取消忽略问题
     */
    private fun unignoreIssue(issueId: String) {
        viewModelScope.launch {
            updateIssue(issueId) { it.copy(isIgnored = false) }
            _scanState.value = _scanState.value.copy(
                summary = _scanState.value.summary.copy(
                    ignored = maxOf(0, _scanState.value.summary.ignored - 1)
                )
            )
        }
    }

    /**
     * Update issue in state / 更新状态中的问题
     */
    private fun updateIssue(issueId: String, update: (AlarmUsageIssue) -> AlarmUsageIssue) {
        _scanState.value = _scanState.value.copy(
            results = _scanState.value.results.map { issue ->
                if (issue.id == issueId) update(issue) else issue
            }
        )
    }

    // =============================================================
    // Report Generation
    // =============================================================
    /**
     * Generate HTML report / 生成 HTML 报告
     *
     * @param scanState Current scan state to include in report / 报告中包含的当前扫描状态
     */
    private fun generateReport(scanState: AlarmScanState) {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(
                reportStatus = AlarmScanStatus.SCANNING
            )

            try {
                // Simulate HTML report generation
                // 模拟 HTML 报告生成
                delay(1000)

                val reportPath = "/storage/emulated/0/Download/alarm_migration_report.html"
                _reportState.value = _reportState.value.copy(
                    reportStatus = AlarmScanStatus.COMPLETED,
                    reportPath = reportPath,
                    isPreview = false
                )

                _effect.send(AlarmMigrationEffect.ShareReport(reportPath))
                _effect.send(AlarmMigrationEffect.ShowToast("Report generated: $reportPath"))

            } catch (e: Exception) {
                _reportState.value = _reportState.value.copy(
                    reportStatus = AlarmScanStatus.ERROR
                )
                _effect.send(AlarmMigrationEffect.ShowError("Report generation failed: ${e.message}"))
            }
        }
    }

    /**
     * Open report in browser / 在浏览器中打开报告
     */
    private fun openReport(reportPath: String) {
        viewModelScope.launch {
            _effect.send(AlarmMigrationEffect.OpenInBrowser(reportPath))
        }
    }

    // =============================================================
    // Diff
    // =============================================================
    /**
     * Compare two scan results / 对比两次扫描结果
     *
     * @param beforePath Path to before scan JSON / 前一次扫描 JSON 路径
     * @param afterPath Path to after scan JSON / 后一次扫描 JSON 路径
     */
    private fun diffScans(beforePath: String, afterPath: String) {
        viewModelScope.launch {
            // In real implementation, load both JSON files and compute diff
            // 真实实现：加载两个 JSON 文件并计算差异
            delay(500)
            _effect.send(AlarmMigrationEffect.ShowToast("Diff analysis complete / 差异分析完成"))
        }
    }

    // =============================================================
    // Error Handling
    // =============================================================
    /**
     * Dismiss error message / 关闭错误信息
     */
    private fun dismissError() {
        _scanState.value = _scanState.value.copy(error = null)
        _migrationState.value = _migrationState.value.copy(error = null)
    }
}
