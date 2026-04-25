package com.mvi.kenny.feature.onalaarmlistener

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
// OnAlarmViewModel — OnAlarmListener 工具 ViewModel
// =============================================================
/**
 * ViewModel for OnAlarmListener Battery Optimization Tool / OnAlarmListener 电池优化工具 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Scan project for WakeLock/AlarmManager/WorkManager usage
 * - Calculate battery compliance score
 * - Generate migration diffs (WakeLock → OnAlarmListener)
 * - Provide CI template downloads
 *
 * @see OnAlarmContract For State, Intent, Effect definitions
 * @see OnAlarmScreen For UI implementation
 */
class OnAlarmViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(OnAlarmState.Initial)
    val state: StateFlow<OnAlarmState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<OnAlarmEffect>()
    val effect: SharedFlow<OnAlarmEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Internal state
    // ---------------------------------------------------------
    private var scanJob: Job? = null

    // ============================================================
    // Intent Processing — process user actions
    // ============================================================
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Each when branch handles one Intent type.
     */
    fun processIntent(intent: OnAlarmIntent) {
        when (intent) {
            // Tab navigation / Tab 导航
            is OnAlarmIntent.SelectTab -> handleSelectTab(intent.tab)

            // Dashboard intents / 仪表盘意图
            is OnAlarmIntent.StartQuickScan -> handleStartQuickScan()
            is OnAlarmIntent.RefreshDashboard -> handleRefreshDashboard()
            is OnAlarmIntent.NavigateToTask -> handleNavigateToTask(intent.task)

            // Scanner intents / 扫描器意图
            is OnAlarmIntent.SelectProject -> handleSelectProject(intent.path)
            is OnAlarmIntent.NextStep -> handleNextStep()
            is OnAlarmIntent.PreviousStep -> handlePreviousStep()
            is OnAlarmIntent.StartScan -> handleStartScan()
            is OnAlarmIntent.GenerateDiff -> handleGenerateDiff()
            is OnAlarmIntent.ExportDiff -> handleExportDiff(intent.format)

            // Analysis intents / 分析意图
            is OnAlarmIntent.SelectTask -> handleSelectTask(intent.task)
            is OnAlarmIntent.ClearSelectedTask -> handleClearSelectedTask()
            is OnAlarmIntent.MarkAsMigrated -> handleMarkAsMigrated(intent.taskId)
            is OnAlarmIntent.ExportReport -> handleExportReport(intent.format)

            // Settings intents / 设置意图
            is OnAlarmIntent.UpdateWakeLockThreshold -> handleUpdateThreshold(intent.minutes)
            is OnAlarmIntent.UpdateCiTemplate -> handleUpdateCiTemplate(intent.template)
            is OnAlarmIntent.ToggleNotifications -> handleToggleNotifications()
            is OnAlarmIntent.DownloadCiTemplate -> handleDownloadCiTemplate()

            // Common intents / 通用意图
            is OnAlarmIntent.DismissError -> handleDismissError()
        }
    }

    // ============================================================
    // Intent Handlers
    // ============================================================

    // ─────────────────────────────────────────────────────────
    // Tab Navigation / Tab 导航
    // ─────────────────────────────────────────────────────────

    /**
     * Handle tab selection / 处理 Tab 选择
     *
     * Switches between 4 tool modules: Dashboard / Scanner / Analysis / Settings.
     */
    private fun handleSelectTab(tab: ToolTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    // ─────────────────────────────────────────────────────────
    // Dashboard Handlers / 仪表盘处理
    // ─────────────────────────────────────────────────────────

    /**
     * Handle quick scan initiation from dashboard / 处理从仪表盘启动快速扫描
     *
     * Switches to Scanner tab and starts scan from Step 1.
     */
    private fun handleStartQuickScan() {
        _state.update {
            it.copy(
                activeTab = ToolTab.SCANNER,
                scannerCurrentStep = ScanStep.PROJECT_SELECTION
            )
        }
        viewModelScope.launch {
            _effect.emit(OnAlarmEffect.NavigateToScanner)
        }
    }

    /**
     * Handle dashboard refresh / 处理仪表盘刷新
     *
     * Re-calculates compliance score from existing scan results.
     */
    private fun handleRefreshDashboard() {
        val currentTasks = _state.value.affectedTasks
        if (currentTasks.isEmpty()) {
            viewModelScope.launch {
                _effect.emit(OnAlarmEffect.ShowSnackbar("No scan data yet. Run a scan first. / 暂无扫描数据，请先运行扫描。"))
            }
            return
        }
        val newScore = calculateComplianceScore(currentTasks)
        val riskLevel = when {
            newScore >= 80 -> RiskLevel.GREEN
            newScore >= 50 -> RiskLevel.YELLOW
            else -> RiskLevel.RED
        }
        _state.update {
            it.copy(
                overallScore = newScore,
                batteryRiskLevel = riskLevel,
                lastScanTime = System.currentTimeMillis()
            )
        }
    }

    /**
     * Navigate to a specific task / 导航到指定任务
     *
     * Switches to Analysis tab and selects the target task.
     */
    private fun handleNavigateToTask(task: AffectedTask) {
        _state.update {
            it.copy(
                activeTab = ToolTab.ANALYSIS,
                selectedTask = task
            )
        }
        viewModelScope.launch {
            _effect.emit(OnAlarmEffect.NavigateToAnalysis)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Scanner Handlers / 扫描器处理
    // ─────────────────────────────────────────────────────────

    /**
     * Handle project path selection / 处理项目路径选择
     *
     * @param path Project or module path / 项目或模块路径
     */
    private fun handleSelectProject(path: String) {
        addScanLog("INFO", "Project selected: $path / 已选择项目：$path")
    }

    /**
     * Handle next step / 处理下一步
     *
     * Advances scanner to the next step in the guided flow.
     */
    private fun handleNextStep() {
        val currentStep = _state.value.scannerCurrentStep
        val nextStep = when (currentStep) {
            ScanStep.PROJECT_SELECTION -> ScanStep.WAKELOCK_SCAN
            ScanStep.WAKELOCK_SCAN -> ScanStep.ALARM_WORK_SCAN
            ScanStep.ALARM_WORK_SCAN -> ScanStep.COMPLIANCE_EVALUATION
            ScanStep.COMPLIANCE_EVALUATION -> ScanStep.DIFF_GENERATION
            ScanStep.DIFF_GENERATION -> ScanStep.DIFF_GENERATION
        }
        _state.update { it.copy(scannerCurrentStep = nextStep) }
        addScanLog("INFO", "Step ${nextStep.stepNumber}: ${nextStep.title} / 步骤 ${nextStep.stepNumber}：${nextStep.title}")
    }

    /**
     * Handle previous step / 处理上一步
     */
    private fun handlePreviousStep() {
        val currentStep = _state.value.scannerCurrentStep
        val prevStep = when (currentStep) {
            ScanStep.PROJECT_SELECTION -> ScanStep.PROJECT_SELECTION
            ScanStep.WAKELOCK_SCAN -> ScanStep.PROJECT_SELECTION
            ScanStep.ALARM_WORK_SCAN -> ScanStep.WAKELOCK_SCAN
            ScanStep.COMPLIANCE_EVALUATION -> ScanStep.ALARM_WORK_SCAN
            ScanStep.DIFF_GENERATION -> ScanStep.COMPLIANCE_EVALUATION
        }
        _state.update { it.copy(scannerCurrentStep = prevStep) }
        addScanLog("INFO", "Back to Step ${prevStep.stepNumber}: ${prevStep.title} / 返回步骤 ${prevStep.stepNumber}：${prevStep.title}")
    }

    /**
     * Handle scan start / 处理扫描开始
     *
     * Runs the full scan flow: WakeLock scan → Alarm/WorkManager scan → Compliance evaluation.
     * In production, this would use Kotlin PSI or lint AST analysis.
     */
    private fun handleStartScan() {
        scanJob?.cancel()

        _state.update {
            it.copy(
                isScanning = true,
                scanProgress = 0f,
                scanLogs = emptyList(),
                wakeLockResults = emptyList(),
                alarmResults = emptyList(),
                generatedDiff = null,
                error = null
            )
        }

        scanJob = viewModelScope.launch {
            try {
                // Step 1: Project selection (already done) / 项目选择（已完成）
                addScanLog("INFO", "Starting scan / 开始扫描...")
                delay(500)

                // Step 2: WakeLock scan / WakeLock 扫描
                _state.update { it.copy(scannerCurrentStep = ScanStep.WAKELOCK_SCAN) }
                addScanLog("INFO", "Scanning for WakeLock usage... / 正在扫描 WakeLock 使用情况...")
                val wakeLockResults = withContext(Dispatchers.Default) {
                    simulateWakeLockScan()
                }
                _state.update {
                    it.copy(
                        wakeLockResults = wakeLockResults,
                        scanProgress = 0.33f
                    )
                }
                addScanLog("SUCCESS", "Found ${wakeLockResults.size} WakeLock usages / 发现 ${wakeLockResults.size} 处 WakeLock 使用")

                delay(500)

                // Step 3: Alarm/WorkManager scan / Alarm/WorkManager 扫描
                _state.update { it.copy(scannerCurrentStep = ScanStep.ALARM_WORK_SCAN) }
                addScanLog("INFO", "Scanning for AlarmManager & WorkManager usage... / 正在扫描 AlarmManager & WorkManager 使用情况...")
                val alarmResults = withContext(Dispatchers.Default) {
                    simulateAlarmScan()
                }
                _state.update {
                    it.copy(
                        alarmResults = alarmResults,
                        scanProgress = 0.66f
                    )
                }
                addScanLog("SUCCESS", "Found ${alarmResults.size} Alarm/WorkManager usages / 发现 ${alarmResults.size} 处 Alarm/WorkManager 使用")

                delay(500)

                // Step 4: Compliance evaluation / 合规评估
                _state.update { it.copy(scannerCurrentStep = ScanStep.COMPLIANCE_EVALUATION) }
                addScanLog("INFO", "Calculating compliance score... / 正在计算合规评分...")

                val allTasks = buildAllTasks(wakeLockResults, alarmResults)
                val score = calculateComplianceScore(allTasks)
                val riskLevel = when {
                    score >= 80 -> RiskLevel.GREEN
                    score >= 50 -> RiskLevel.YELLOW
                    else -> RiskLevel.RED
                }

                _state.update {
                    it.copy(
                        affectedTasks = allTasks,
                        taskGroups = allTasks.groupBy { t -> t.taskType },
                        overallScore = score,
                        batteryRiskLevel = riskLevel,
                        electricityData = calculateElectricityData(allTasks),
                        scanProgress = 1f,
                        lastScanTime = System.currentTimeMillis()
                    )
                }
                addScanLog("SUCCESS", "Compliance score: $score / 合规评分：$score ($riskLevel)")

                // Step 5: Diff generation ready / Diff 生成就绪
                _state.update { it.copy(scannerCurrentStep = ScanStep.DIFF_GENERATION) }
                addScanLog("INFO", "Scan complete. Ready to generate diff. / 扫描完成，可以生成 Diff。")

                _state.update { it.copy(isScanning = false) }
                _effect.emit(OnAlarmEffect.ScanComplete(score))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    addScanLog("WARN", "Scan cancelled / 扫描已取消")
                    _state.update {
                        it.copy(isScanning = false, scanProgress = 0f)
                    }
                } else {
                    addScanLog("ERROR", "Scan failed: ${e.message} / 扫描失败：${e.message}")
                    _state.update {
                        it.copy(
                            isScanning = false,
                            error = "Scan failed: ${e.message}"
                        )
                    }
                    _effect.emit(
                        OnAlarmEffect.ShowSnackbar(
                            "Scan failed: ${e.message}",
                            isError = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Simulate WakeLock scan with demo results / 用演示结果模拟 WakeLock 扫描
     *
     * In production, use Kotlin PSI or lint to analyze source files.
     *
     * @return List of detected WakeLock findings / 检测到的 WakeLock 结果列表
     */
    private suspend fun simulateWakeLockScan(): List<WakeLockFinding> {
        repeat(5) { step ->
            delay(200)
            _state.update { it.copy(scanProgress = step / 15f) }
        }

        return listOf(
            WakeLockFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/chat/ChatSyncService.kt",
                lineNumber = 67,
                className = "ChatSyncService",
                methodName = "startSync()",
                callChain = "ChatSyncService.startSync() → PowerManager.newWakeLock(PARTIAL_WAKE_LOCK)",
                estimatedDurationMinutes = 45,
                suggestion = "Use OnAlarmListener + Expedited WorkRequest to replace continuous WakeLock"
            ),
            WakeLockFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/mail/MailPoller.kt",
                lineNumber = 34,
                className = "MailPoller",
                methodName = "pollMails()",
                callChain = "MailPoller.pollMails() → wakeLock.acquire(10 * 60 * 1000L)",
                estimatedDurationMinutes = 10,
                suggestion = "Replace with AlarmManager.setExactAndAllowWhileIdle() + WorkManager"
            ),
            WakeLockFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/news/NewsRefreshReceiver.kt",
                lineNumber = 52,
                className = "NewsRefreshReceiver",
                methodName = "onReceive()",
                callChain = "NewsRefreshReceiver.onReceive() → PowerManager.WakeLock",
                estimatedDurationMinutes = 5,
                suggestion = "Use WorkManager.enqueueUniquePeriodicWork() with expedited flag"
            ),
            WakeLockFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/cloud/CloudSyncWorker.kt",
                lineNumber = 88,
                className = "CloudSyncWorker",
                methodName = "doWork()",
                callChain = "CloudSyncWorker.doWork() → WakeLock for upload",
                estimatedDurationMinutes = 15,
                suggestion = "Use UserInitiatedDataTransfer API (Android 17) for background sync"
            )
        )
    }

    /**
     * Simulate Alarm/WorkManager scan with demo results / 用演示结果模拟 Alarm/WorkManager 扫描
     *
     * @return List of detected Alarm/WorkManager findings / 检测到的 Alarm/WorkManager 结果列表
     */
    private suspend fun simulateAlarmScan(): List<AlarmFinding> {
        repeat(5) { step ->
            delay(200)
            _state.update { it.copy(scanProgress = (5 + step) / 15f) }
        }

        return listOf(
            AlarmFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/chat/ChatAlarmScheduler.kt",
                lineNumber = 45,
                className = "ChatAlarmScheduler",
                methodName = "scheduleNextSync()",
                alarmType = "ELAPSED_REALTIME_WAKEUP",
                estimatedTriggerFrequency = 60,
                suggestion = "Use WorkManager.enqueueUniquePeriodicWork() with Intervals"
            ),
            AlarmFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/mail/MailAlarmReceiver.kt",
                lineNumber = 23,
                className = "MailAlarmReceiver",
                methodName = "scheduleMailCheck()",
                alarmType = "RTC_WAKEUP",
                estimatedTriggerFrequency = 30,
                suggestion = "Use WorkManager.setInitialDelay() + PeriodicWorkRequest"
            ),
            AlarmFinding(
                id = UUID.randomUUID().toString(),
                filePath = "app/src/main/java/com/example/backup/BackupWorker.kt",
                lineNumber = 67,
                className = "BackupWorker",
                methodName = "enqueueBackup()",
                alarmType = "WORK_MANAGER",
                estimatedTriggerFrequency = 4,
                suggestion = "Good candidate for Expedited WorkRequest if user-initiated"
            )
        )
    }

    /**
     * Build combined task list from scan results / 从扫描结果构建综合任务列表
     */
    private fun buildAllTasks(
        wakeLockFindings: List<WakeLockFinding>,
        alarmFindings: List<AlarmFinding>
    ): List<AffectedTask> {
        val wakeLockTasks = wakeLockFindings.map { finding ->
            AffectedTask(
                id = finding.id,
                taskName = "${finding.className}.${finding.methodName}",
                taskType = TaskType.WAKELOCK,
                filePath = finding.filePath,
                lineNumber = finding.lineNumber,
                methodName = finding.methodName,
                className = finding.className,
                estimatedDurationMinutes = finding.estimatedDurationMinutes,
                estimatedFrequency = 0,
                migrationSuggestion = finding.suggestion,
                isMigrated = false
            )
        }

        val alarmTasks = alarmFindings.map { finding ->
            AffectedTask(
                id = finding.id,
                taskName = "${finding.className}.${finding.methodName}",
                taskType = when (finding.alarmType) {
                    "WORK_MANAGER" -> TaskType.WORK_MANAGER
                    else -> TaskType.ALARM
                },
                filePath = finding.filePath,
                lineNumber = finding.lineNumber,
                methodName = finding.methodName,
                className = finding.className,
                estimatedDurationMinutes = 0,
                estimatedFrequency = finding.estimatedTriggerFrequency,
                migrationSuggestion = finding.suggestion,
                isMigrated = false
            )
        }

        return wakeLockTasks + alarmTasks
    }

    /**
     * Calculate compliance score / 计算合规评分
     *
     * Algorithm:
     * - Start with 100, subtract for each risk factor
     * - WakeLock duration > threshold: -10 per occurrence
     * - High frequency alarm: -5 per occurrence
     * - Missing Expedited/UIDT flag: -8 per WorkManager task
     *
     * @param tasks List of affected tasks / 受影响任务列表
     * @return Compliance score 0-100 / 合规评分 0-100
     */
    private fun calculateComplianceScore(tasks: List<AffectedTask>): Int {
        val threshold = _state.value.wakeLockThresholdMinutes
        var score = 100

        tasks.forEach { task ->
            when (task.taskType) {
                TaskType.WAKELOCK -> {
                    if (task.estimatedDurationMinutes > threshold) {
                        score -= 10
                    } else if (task.estimatedDurationMinutes > 0) {
                        score -= 3
                    }
                }
                TaskType.ALARM -> {
                    if (task.estimatedFrequency > 30) {
                        score -= 5
                    } else if (task.estimatedFrequency > 10) {
                        score -= 2
                    }
                }
                TaskType.WORK_MANAGER -> {
                    score -= 3
                }
                TaskType.EXPEDITED, TaskType.USER_INITIATED -> {
                    // These are good — no penalty / 这些是好的，不扣分
                }
            }
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Calculate electricity contribution data / 计算耗电贡献数据
     */
    private fun calculateElectricityData(tasks: List<AffectedTask>): List<ElectricityContribution> {
        val totalWeight = tasks.sumOf { task: AffectedTask ->
            when (task.taskType) {
                TaskType.WAKELOCK -> task.estimatedDurationMinutes * 2
                TaskType.ALARM -> task.estimatedFrequency
                TaskType.WORK_MANAGER -> 5
                TaskType.EXPEDITED, TaskType.USER_INITIATED -> 1
            }
        }.toFloat().coerceAtLeast(1f)

        return tasks.map { task ->
            val weight = when (task.taskType) {
                TaskType.WAKELOCK -> task.estimatedDurationMinutes * 2f
                TaskType.ALARM -> task.estimatedFrequency.toFloat()
                TaskType.WORK_MANAGER -> 5f
                TaskType.EXPEDITED, TaskType.USER_INITIATED -> 1f
            }
            val percent = (weight / totalWeight * 100)
            val mah = percent * 0.5f // Simplified model / 简化模型

            ElectricityContribution(
                taskName = task.taskName,
                taskType = task.taskType,
                contributionPercent = percent,
                estimatedMah = mah
            )
        }.sortedByDescending { it.contributionPercent }
    }

    /**
     * Add scan log entry / 添加扫描日志条目
     */
    private fun addScanLog(level: String, message: String) {
        val logLevel = when (level) {
            "INFO" -> LogLevel.INFO
            "WARN" -> LogLevel.WARN
            "ERROR" -> LogLevel.ERROR
            "SUCCESS" -> LogLevel.SUCCESS
            else -> LogLevel.INFO
        }
        val entry = ScanLogEntry(
            timestamp = System.currentTimeMillis(),
            level = logLevel,
            message = message
        )
        _state.update { it.copy(scanLogs = it.scanLogs + entry) }
    }

    /**
     * Handle diff generation / 处理 Diff 生成
     *
     * Generates migration diff for WakeLock → OnAlarmListener.
     */
    private fun handleGenerateDiff() {
        val wakeLockResults = _state.value.wakeLockResults
        if (wakeLockResults.isEmpty()) {
            viewModelScope.launch {
                _effect.emit(OnAlarmEffect.ShowSnackbar("No WakeLock findings to migrate. / 没有需要迁移的 WakeLock。", isError = true))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val diff = withContext(Dispatchers.Default) {
                    generateMigrationDiff(wakeLockResults)
                }

                _state.update {
                    it.copy(
                        generatedDiff = diff,
                        isLoading = false
                    )
                }
                _effect.emit(OnAlarmEffect.DiffGenerated(diff))
                _effect.emit(OnAlarmEffect.ShowSnackbar("Diff generated successfully. / Diff 生成成功。"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Diff generation failed: ${e.message}"
                    )
                }
                _effect.emit(OnAlarmEffect.ShowSnackbar("Diff generation failed: ${e.message}", isError = true))
            }
        }
    }

    /**
     * Generate migration diff / 生成迁移 Diff
     *
     * Creates a unified diff showing WakeLock → OnAlarmListener migration.
     *
     * @param findings WakeLock findings to migrate / 要迁移的 WakeLock 发现
     * @return Unified diff string / Unified Diff 字符串
     */
    private fun generateMigrationDiff(findings: List<WakeLockFinding>): String {
        val sb = StringBuilder()
        sb.appendLine("--- a/onalaarmlistener_migration.diff")
        sb.appendLine("+++ b/onalaarmlistener_migration.diff")
        sb.appendLine("@@ Migration: WakeLock → OnAlarmListener (Android 17) @@")
        sb.appendLine()

        findings.forEach { finding ->
            sb.appendLine("--- ${finding.filePath}")
            sb.appendLine("+++ ${finding.filePath} (migrated)")
            sb.appendLine("@@ Found WakeLock at line ${finding.lineNumber} @@")
            sb.appendLine()
            sb.appendLine("-// OLD: PowerManager WakeLock (battery drain)")
            sb.appendLine("-val wakeLock = powerManager.newWakeLock(")
            sb.appendLine("-    PowerManager.PARTIAL_WAKE_LOCK,")
            sb.appendLine("-    \"${finding.className}:${finding.methodName}\"")
            sb.appendLine("-)")
            sb.appendLine("-wakeLock.acquire(${finding.estimatedDurationMinutes * 60 * 1000L}L)")
            sb.appendLine()
            sb.appendLine("+// NEW: OnAlarmListener (Android 17, battery optimized)")
            sb.appendLine("+val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager")
            sb.appendLine("+val pendingIntent = PendingIntent.getService(")
            sb.appendLine("+    context,")
            sb.appendLine("+    REQUEST_CODE_${finding.lineNumber},")
            sb.appendLine("+    Intent(context, ${finding.className}::class.java),")
            sb.appendLine("+    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE")
            sb.appendLine("+)")
            sb.appendLine("+alarmManager.setExactAndAllowWhileIdle(")
            sb.appendLine("+    AlarmManager.ELAPSED_REALTIME_WAKEUP,")
            sb.appendLine("+    ${finding.estimatedDurationMinutes * 60 * 1000L}L,")
            sb.appendLine("+    pendingIntent")
            sb.appendLine("+)")
            sb.appendLine()
            sb.appendLine("+// Migration note: ${finding.suggestion}")
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Handle diff export / 处理 Diff 导出
     */
    private fun handleExportDiff(format: ExportFormat) {
        val diff = _state.value.generatedDiff ?: run {
            viewModelScope.launch {
                _effect.emit(OnAlarmEffect.ShowSnackbar("No diff to export. Generate diff first. / 没有可导出的 Diff，请先生成。", isError = true))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isExportingDiff = true) }

            try {
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val extension = when (format) {
                    ExportFormat.UNIFIED_DIFF -> "diff"
                    ExportFormat.GIT_PATCH -> "patch"
                    ExportFormat.JSON -> "json"
                }
                val fileName = "wakediff_migration_$dateStr.$extension"
                val filePath = "/tmp/$fileName"

                withContext(Dispatchers.IO) {
                    File(filePath).apply {
                        parentFile?.mkdirs()
                        writeText(diff)
                    }
                }

                _state.update { it.copy(isExportingDiff = false) }
                _effect.emit(OnAlarmEffect.ReportExported(filePath))
                _effect.emit(OnAlarmEffect.ShowSnackbar("Diff exported: $filePath / Diff 已导出：$filePath"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExportingDiff = false,
                        error = "Export failed: ${e.message}"
                    )
                }
                _effect.emit(OnAlarmEffect.ShowSnackbar("Export failed: ${e.message}", isError = true))
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Analysis Handlers / 分析处理
    // ─────────────────────────────────────────────────────────

    /**
     * Handle task selection / 处理任务选择
     */
    private fun handleSelectTask(task: AffectedTask) {
        _state.update { it.copy(selectedTask = task) }
    }

    /**
     * Handle clear selected task / 处理清除选中的任务
     */
    private fun handleClearSelectedTask() {
        _state.update { it.copy(selectedTask = null) }
    }

    /**
     * Handle mark as migrated / 处理标记为已迁移
     *
     * @param taskId ID of the task to mark as migrated / 要标记为已迁移的任务 ID
     */
    private fun handleMarkAsMigrated(taskId: String) {
        _state.update { state ->
            val updatedTasks = state.affectedTasks.map { task ->
                if (task.id == taskId) task.copy(isMigrated = true) else task
            }
            val updatedGroups = updatedTasks.groupBy { it.taskType }

            state.copy(
                affectedTasks = updatedTasks,
                taskGroups = updatedGroups,
                selectedTask = state.selectedTask?.let { if (it.id == taskId) it.copy(isMigrated = true) else it }
            )
        }

        // Recalculate score / 重新计算评分
        val newScore = calculateComplianceScore(_state.value.affectedTasks)
        val riskLevel = when {
            newScore >= 80 -> RiskLevel.GREEN
            newScore >= 50 -> RiskLevel.YELLOW
            else -> RiskLevel.RED
        }
        _state.update {
            it.copy(
                overallScore = newScore,
                batteryRiskLevel = riskLevel
            )
        }

        viewModelScope.launch {
            _effect.emit(OnAlarmEffect.TaskMarkedMigrated(taskId))
            _effect.emit(OnAlarmEffect.ShowSnackbar("Task marked as migrated. Score updated. / 任务已标记为迁移，评分已更新。"))
        }
    }

    /**
     * Handle report export / 处理报告导出
     */
    private fun handleExportReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val extension = format.name.lowercase()
                val fileName = "battery_compliance_report_$dateStr.$extension"
                val filePath = "/tmp/$fileName"

                val content = withContext(Dispatchers.Default) {
                    buildComplianceReportContent(format)
                }

                withContext(Dispatchers.IO) {
                    File(filePath).apply {
                        parentFile?.mkdirs()
                        writeText(content)
                    }
                }

                _state.update { it.copy(isLoading = false) }
                _effect.emit(OnAlarmEffect.ReportExported(filePath))
                _effect.emit(OnAlarmEffect.ShowSnackbar("Report exported: $filePath / 报告已导出：$filePath"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Report export failed: ${e.message}"
                    )
                }
                _effect.emit(OnAlarmEffect.ShowSnackbar("Report export failed: ${e.message}", isError = true))
            }
        }
    }

    /**
     * Build compliance report content / 构建合规报告内容
     */
    private fun buildComplianceReportContent(format: ReportFormat): String {
        val s = _state.value
        return when (format) {
            ReportFormat.MARKDOWN -> buildString {
                appendLine("# Android Battery Compliance Report / Android 电池合规报告")
                appendLine()
                appendLine("## Summary / 摘要")
                appendLine("- Compliance Score / 合规评分: **${s.overallScore}**")
                appendLine("- Risk Level / 风险等级: **${s.batteryRiskLevel.label}**")
                appendLine("- Total Tasks / 任务总数: ${s.affectedTasks.size}")
                appendLine("- Migrated / 已迁移: ${s.migratedCount}")
                appendLine("- Remaining / 剩余: ${s.affectedTasks.size - s.migratedCount}")
                appendLine()
                appendLine("## Affected Tasks / 受影响任务")
                s.affectedTasks.forEach { task ->
                    appendLine("- ${task.taskType.label}: ${task.taskName} (${task.filePath}:${task.lineNumber})")
                    appendLine("  - Suggestion / 建议: ${task.migrationSuggestion}")
                }
            }
            ReportFormat.JSON -> buildString {
                appendLine("{")
                appendLine("  \"complianceScore\": ${s.overallScore},")
                appendLine("  \"riskLevel\": \"${s.batteryRiskLevel.name}\",")
                appendLine("  \"totalTasks\": ${s.affectedTasks.size},")
                appendLine("  \"migratedCount\": ${s.migratedCount},")
                appendLine("  \"tasks\": [")
                s.affectedTasks.forEachIndexed { index, task ->
                    val comma = if (index < s.affectedTasks.size - 1) "," else ""
                    appendLine("    {")
                    appendLine("      \"id\": \"${task.id}\",")
                    appendLine("      \"name\": \"${task.taskName}\",")
                    appendLine("      \"type\": \"${task.taskType.name}\",")
                    appendLine("      \"file\": \"${task.filePath}\",")
                    appendLine("      \"line\": ${task.lineNumber},")
                    appendLine("      \"migrated\": ${task.isMigrated}")
                    appendLine("    }$comma")
                }
                appendLine("  ]")
                appendLine("}")
            }
            ReportFormat.PDF -> buildString {
                appendLine("BATTERY COMPLIANCE REPORT")
                appendLine("Score: ${s.overallScore}/100")
                appendLine("Risk: ${s.batteryRiskLevel.label}")
                appendLine("Tasks: ${s.affectedTasks.size} total, ${s.migratedCount} migrated")
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Settings Handlers / 设置处理
    // ─────────────────────────────────────────────────────────

    /**
     * Handle threshold update / 处理阈值更新
     */
    private fun handleUpdateThreshold(minutes: Int) {
        _state.update { it.copy(wakeLockThresholdMinutes = minutes) }
        // Recalculate score with new threshold / 用新阈值重新计算评分
        val newScore = calculateComplianceScore(_state.value.affectedTasks)
        _state.update { it.copy(overallScore = newScore) }
    }

    /**
     * Handle CI template update / 处理 CI 模板更新
     */
    private fun handleUpdateCiTemplate(template: CiTemplate) {
        _state.update { it.copy(ciTemplateType = template) }
    }

    /**
     * Handle notifications toggle / 处理通知切换
     */
    private fun handleToggleNotifications() {
        _state.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
        viewModelScope.launch {
            _effect.emit(
                OnAlarmEffect.ShowSnackbar(
                    if (_state.value.notificationsEnabled) "Notifications enabled / 通知已启用"
                    else "Notifications disabled / 通知已禁用"
                )
            )
        }
    }

    /**
     * Handle CI template download / 处理 CI 模板下载
     */
    private fun handleDownloadCiTemplate() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val template = withContext(Dispatchers.Default) {
                    generateCiTemplate(_state.value.ciTemplateType)
                }

                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val extension = when (_state.value.ciTemplateType) {
                    CiTemplate.GITHUB_ACTIONS -> "yml"
                    CiTemplate.GITLAB_CI -> "yaml"
                }
                val fileName = "battery_scan_workflow.$extension"
                val filePath = "/tmp/$fileName"

                withContext(Dispatchers.IO) {
                    File(filePath).apply {
                        parentFile?.mkdirs()
                        writeText(template)
                    }
                }

                _state.update { it.copy(isLoading = false) }
                _effect.emit(OnAlarmEffect.ReportExported(filePath))
                _effect.emit(OnAlarmEffect.ShowSnackbar("CI template downloaded: $filePath / CI 模板已下载：$filePath"))

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "CI template download failed: ${e.message}"
                    )
                }
                _effect.emit(OnAlarmEffect.ShowSnackbar("CI template download failed: ${e.message}", isError = true))
            }
        }
    }

    /**
     * Generate CI template / 生成 CI 模板
     */
    private fun generateCiTemplate(template: CiTemplate): String {
        return when (template) {
            CiTemplate.GITHUB_ACTIONS -> buildString {
                appendLine("name: Android Battery Compliance Scan")
                appendLine("on:")
                appendLine("  push:")
                appendLine("    branches: [main]")
                appendLine("  pull_request:")
                appendLine("    branches: [main]")
                appendLine("jobs:")
                appendLine("  battery-scan:")
                appendLine("    runs-on: ubuntu-latest")
                appendLine("    steps:")
                appendLine("      - uses: actions/checkout@v4")
                appendLine("      - uses: actions/setup-java@v4")
                appendLine("      - name: Setup Gradle")
                appendLine("        run: |")
                appendLine("          ./gradlew wrapper --gradle-version=8.5")
                appendLine("      - name: Run Battery Compliance Scan")
                appendLine("        run: |")
                appendLine("          ./gradlew batteryComplianceScan")
                appendLine("      - name: Upload SARIF report")
                appendLine("        uses: github/codeql-action/upload-sarif@v3")
                appendLine("        with:")
                appendLine("          sarif_file: build/reports/battery-scan.sarif")
            }
            CiTemplate.GITLAB_CI -> buildString {
                appendLine("stages:")
                appendLine("  - scan")
                appendLine("battery_compliance_scan:")
                appendLine("  stage: scan")
                appendLine("  image: gradle:8.5-jdk17")
                appendLine("  script:")
                appendLine("    - ./gradlew batteryComplianceScan")
                appendLine("  artifacts:")
                appendLine("    paths:")
                appendLine("      - build/reports/battery-scan.sarif")
                appendLine("    expire_in: 30 days")
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // Common Handlers / 通用处理
    // ─────────────────────────────────────────────────────────

    /**
     * Handle error dismissal / 处理错误关闭
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    // ============================================================
    // Lifecycle
    // ============================================================
    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
