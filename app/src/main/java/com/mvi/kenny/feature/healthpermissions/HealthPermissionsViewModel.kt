package com.mvi.kenny.feature.healthpermissions

// ================================================================
// HealthPermissionsViewModel — Android 16 健康权限 MVI ViewModel
// ================================================================
// ViewModel for Android 16 Body Sensors migration toolkit.
//
// PRD-106: Android 16 细粒度健康权限迁移检测与合规工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage scan workflow (5-step progress simulation)
//   - Maintain scan history
//   - Filter permission mappings by search query
//   - Handle settings persistence
//   - Emit one-time Effects (navigation, toast, errors)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ============================================================
 * HealthPermissionsViewModel — 健康权限 ViewModel
 * ============================================================
 * Manages the HealthPermissionsState and processes HealthPermissionsIntent.
 * In a real implementation, this would use KSP/Lint for actual code scanning.
 * Current implementation simulates scanning for demonstration purposes.
 *
 * @see HealthPermissionsState
 * @see HealthPermissionsIntent
 * @see HealthPermissionsEffect
 */
class HealthPermissionsViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(HealthPermissionsState.Initial)
    val state: StateFlow<HealthPermissionsState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via Channel
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<HealthPermissionsEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state
    // ─────────────────────────────────────────────────────────────
    private var scanJob: Job? = null

    init {
        // Load mock scan history on initialization
        _update { copy(scanHistory = createMockScanHistory()) }
    }

    /**
     * ============================================================
     * sendIntent — Intent 处理入口
     * ============================================================
     * Entry point for all user intents. Called from UI layer.
     *
     * @param intent User intent
     * @see HealthPermissionsIntent
     */
    fun sendIntent(intent: HealthPermissionsIntent) {
        when (intent) {
            is HealthPermissionsIntent.StartScan -> startScan()
            is HealthPermissionsIntent.CancelScan -> cancelScan()
            is HealthPermissionsIntent.LoadScanDetail -> loadScanDetail(intent.recordId)
            is HealthPermissionsIntent.SwitchTab -> switchTab(intent.tab)
            is HealthPermissionsIntent.MarkIssueFixed -> markIssueFixed(intent.issueId)
            is HealthPermissionsIntent.DismissIssue -> dismissIssue(intent.issueId)
            is HealthPermissionsIntent.SearchMappings -> searchMappings(intent.query)
            is HealthPermissionsIntent.UpdateSettings -> updateSettings(intent.settings)
            is HealthPermissionsIntent.ExportReport -> exportReport(intent.format)
            is HealthPermissionsIntent.ClearError -> clearError()
            is HealthPermissionsIntent.RefreshScan -> startScan()
        }
    }

    // ================================================================
    // Intent Handlers
    // ================================================================

    /**
     * StartScan — 开始模拟扫描流程
     *
     * Simulates a 5-step scan process:
     *   1. Parsing Manifest (0-20%)
     *   2. Scanning Code (20-45%)
     *   3. Analyzing Dependencies (45-65%)
     *   4. Health Connect Conflict Detection (65-85%)
     *   5. Generating Report (85-100%)
     *
     * In a real implementation, this would use:
     *   - XmlPullParser for AndroidManifest.xml parsing
     *   - KSP/Lint for code analysis
     *   - Gradle task introspection for dependency analysis
     */
    private fun startScan() {
        // Cancel any existing scan
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _update {
                copy(
                    scanState = ScanState.Scanning,
                    scanProgress = 0f,
                    currentStepNumber = 0,
                    scanProgressStepDescription = "准备开始...",
                    errorMessage = null
                )
            }

            try {
                // ── Step 1: Parse Manifest (0-20%) ────────────────────
                _update {
                    copy(
                        currentStepNumber = 1,
                        scanProgressStepDescription = ScanProgressStep.ParsingManifest.description
                    )
                }
                simulateProgress(0f, 0.2f, steps = 10)

                // ── Step 2: Scan Code (20-45%) ────────────────────────
                _update {
                    copy(
                        currentStepNumber = 2,
                        scanProgressStepDescription = ScanProgressStep.ScanningCode.description
                    )
                }
                simulateProgress(0.2f, 0.45f, steps = 12)

                // ── Step 3: Analyze Dependencies (45-65%) ──────────────
                _update {
                    copy(
                        currentStepNumber = 3,
                        scanProgressStepDescription = ScanProgressStep.AnalyzingDependencies.description
                    )
                }
                simulateProgress(0.45f, 0.65f, steps = 8)

                // ── Step 4: Health Connect Conflict Detection (65-85%) ──
                _update {
                    copy(
                        currentStepNumber = 4,
                        scanProgressStepDescription = ScanProgressStep.DetectingConflicts.description
                    )
                }
                simulateProgress(0.65f, 0.85f, steps = 10)

                // ── Step 5: Generate Report (85-100%) ──────────────────
                _update {
                    copy(
                        currentStepNumber = 5,
                        scanProgressStepDescription = ScanProgressStep.GeneratingReport.description
                    )
                }
                simulateProgress(0.85f, 1.0f, steps = 6)

                // ── Scan Complete ──────────────────────────────────────
                val scanResult = createMockScanResult()
                val scanRecord = ScanRecord(
                    id = scanResult.id,
                    timestamp = scanResult.timestamp,
                    score = scanResult.score,
                    issueCounts = scanResult.issueCounts,
                    isNew = true
                )

                _update {
                    copy(
                        scanState = ScanState.Success,
                        scanProgress = 1.0f,
                        lastScanResult = scanResult,
                        scanHistory = listOf(scanRecord) + scanHistory.map { it.copy(isNew = false) }
                    )
                }

                _effect.emit(HealthPermissionsEffect.ScanComplete(scanResult.id))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update {
                    copy(
                        scanState = ScanState.Error,
                        errorMessage = e.message ?: "扫描过程发生未知错误"
                    )
                }
                _effect.emit(HealthPermissionsEffect.ShowError(e.message ?: "扫描失败"))
            }
        }
    }

    /**
     * CancelScan — 取消正在进行的扫描
     */
    private fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
        _update {
            copy(
                scanState = ScanState.Idle,
                scanProgress = 0f,
                currentStepNumber = 0,
                scanProgressStepDescription = ""
            )
        }
    }

    /**
     * LoadScanDetail — 加载扫描详情
     *
     * @param recordId Scan record ID to load
     */
    private fun loadScanDetail(recordId: String) {
        _update { copy(selectedScanRecordId = recordId) }
        viewModelScope.launch {
            _effect.emit(HealthPermissionsEffect.NavigateToScanDetail(recordId))
        }
    }

    /**
     * SwitchTab — 切换 Tab
     *
     * @param tab Target tab
     */
    private fun switchTab(tab: ActiveTab) {
        _update { copy(activeTab = tab) }
    }

    /**
     * MarkIssueFixed — 标记问题为已修复
     *
     * @param issueId Issue ID
     */
    private fun markIssueFixed(issueId: String) {
        _update {
            val updatedResult = lastScanResult?.let { result ->
                result.copy(
                    codePaths = result.codePaths.map { path ->
                        if (path.id == issueId) path.copy(isFixed = true) else path
                    }
                )
            }
            copy(lastScanResult = updatedResult)
        }
        viewModelScope.launch {
            _effect.emit(HealthPermissionsEffect.ShowSnackbar("已将问题标记为已修复 ✓"))
        }
    }

    /**
     * DismissIssue — 忽略问题
     *
     * @param issueId Issue ID
     */
    private fun dismissIssue(issueId: String) {
        _update {
            val updatedResult = lastScanResult?.let { result ->
                result.copy(
                    codePaths = result.codePaths.map { path ->
                        if (path.id == issueId) path.copy(isDismissed = true) else path
                    }
                )
            }
            copy(lastScanResult = updatedResult)
        }
        viewModelScope.launch {
            _effect.emit(HealthPermissionsEffect.ShowSnackbar("已忽略该问题"))
        }
    }

    /**
     * SearchMappings — 搜索权限映射
     *
     * @param query Search query
     */
    private fun searchMappings(query: String) {
        _update {
            val filtered = if (query.isBlank()) {
                defaultPermissionMappings
            } else {
                defaultPermissionMappings.filter { mapping ->
                    mapping.oldPermission.contains(query, ignoreCase = true) ||
                    mapping.newPermissions.any { it.contains(query, ignoreCase = true) } ||
                    mapping.description.contains(query, ignoreCase = true)
                }
            }
            copy(
                mappingSearchQuery = query,
                permissionMappings = filtered
            )
        }
    }

    /**
     * UpdateSettings — 更新设置
     *
     * @param settings Updated settings
     */
    private fun updateSettings(settings: HealthPermissionsSettings) {
        _update { copy(settings = settings) }
    }

    /**
     * ExportReport — 导出报告
     *
     * @param format Export format
     */
    private fun exportReport(format: ReportFormat) {
        viewModelScope.launch {
            try {
                // Simulate export (in real impl, would write to file)
                delay(500)
                val filePath = "/tmp/health_permissions_report.${format.extension}"
                _effect.emit(HealthPermissionsEffect.ExportSuccess(filePath))
                _effect.emit(HealthPermissionsEffect.ShowSnackbar("报告已导出至 $filePath"))
            } catch (e: Exception) {
                _effect.emit(HealthPermissionsEffect.ShowError("导出失败: ${e.message}"))
            }
        }
    }

    /**
     * ClearError — 清除错误消息
     */
    private fun clearError() {
        _update { copy(errorMessage = null) }
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * update — State update helper using immutable copy
     *
     * @param update Lambda receiving current state, returning updated state
     */
    private inline fun _update(update: HealthPermissionsState.() -> HealthPermissionsState) {
        _state.update { it.update() }
    }

    /**
     * simulateProgress — 模拟进度条动画
     *
     * @param start Start progress (0.0 ~ 1.0)
     * @param end End progress (0.0 ~ 1.0)
     * @param steps Number of progress updates
     */
    private suspend fun simulateProgress(start: Float, end: Float, steps: Int) {
        withContext(Dispatchers.Main) {
            val stepSize = (end - start) / steps
            repeat(steps) { i ->
                delay((60..120).random().toLong())
                _update { copy(scanProgress = start + stepSize * (i + 1)) }
            }
        }
    }

    /**
     * createMockScanResult — 创建模拟扫描结果（演示用）
     *
     * In a real implementation, this data would come from:
     *   - XmlPullParser parsing AndroidManifest.xml
     *   - KSP/Lint analyzing source code
     *   - Gradle dependency graph introspection
     */
    private fun createMockScanResult(): ScanResult {
        val timestamp = System.currentTimeMillis()
        val id = "scan_${timestamp}"
        return ScanResult(
            id = id,
            timestamp = timestamp,
            score = 62,
            issueCounts = IssueCounts(p0Count = 1, p1Count = 3, p2Count = 2),
            declarations = listOf(
                PermissionDeclaration(
                    permission = "android.permission.BODY_SENSORS",
                    filePath = "app/src/main/AndroidManifest.xml",
                    lineNumber = 42,
                    suggestedNewPermissions = listOf(
                        "android.permission.health.HEALTH_HEART_RATE",
                        "android.permission.health.HEALTH_OXYGEN_SATURATION"
                    ),
                    severity = Severity.P0
                ),
                PermissionDeclaration(
                    permission = "android.permission.BODY_SENSORS_BACKGROUND",
                    filePath = "app/src/main/AndroidManifest.xml",
                    lineNumber = 43,
                    suggestedNewPermissions = listOf(
                        "android.permission.health.HEALTH_HEART_RATE_BACKGROUND"
                    ),
                    severity = Severity.P0
                )
            ),
            codePaths = listOf(
                CodePathIssue(
                    id = "issue_001",
                    permission = "android.permission.BODY_SENSORS",
                    filePath = "app/src/main/java/com/example/healthapp/HeartRateMonitor.kt",
                    lineNumber = 28,
                    codeSnippet = "if (checkSelfPermission(BODY_SENSORS) == PERMISSION_GRANTED) {\n    // read heart rate data\n}",
                    affectedComponent = "HeartRateMonitorFragment",
                    suggestedFix = "替换为 android.permission.health.HEALTH_HEART_RATE",
                    severity = Severity.P1
                ),
                CodePathIssue(
                    id = "issue_002",
                    permission = "android.permission.BODY_SENSORS",
                    filePath = "app/src/main/java/com/example/healthapp/StepsTracker.kt",
                    lineNumber = 15,
                    codeSnippet = "requestPermissions(arrayOf(BODY_SENSORS), REQUEST_CODE)",
                    affectedComponent = "StepsTrackerActivity",
                    suggestedFix = "BODY_SENSORS → HEALTH_STEPS (Android 16 新增独立权限)",
                    severity = Severity.P1
                ),
                CodePathIssue(
                    id = "issue_003",
                    permission = "android.permission.BODY_SENSORS_BACKGROUND",
                    filePath = "app/src/main/java/com/example/healthapp/BackgroundSensorService.kt",
                    lineNumber = 67,
                    codeSnippet = "ContextCompat.checkSelfPermission(context, BODY_SENSORS_BACKGROUND)",
                    affectedComponent = "BackgroundSensorService",
                    suggestedFix = "替换为 android.permission.health.HEALTH_HEART_RATE_BACKGROUND",
                    severity = Severity.P1
                )
            ),
            conflicts = listOf(
                HealthConnectConflict(
                    dataType = "Heart Rate",
                    nativePermission = "android.permission.BODY_SENSORS",
                    healthConnectDataType = "HEALTH_DATA_TYPE_HEART_RATE_BPM",
                    suggestion = "建议统一使用 Health Connect API 获取心率数据，移除原生 BODY_SENSORS 权限"
                )
            ),
            sdkReport = SdkCompatibilityReport(
                googleFitVersion = "21.0.1",
                samsungHealthVersion = "6.22.0.043",
                isGoogleFitCompatible = false,
                isSamsungHealthCompatible = true,
                googleFitNote = "Google Fit SDK 21.0.1 仍依赖 BODY_SENSORS，建议升级到最新版本",
                samsungHealthNote = "三星健康 SDK 6.22 暂未使用废弃权限"
            )
        )
    }

    /**
     * createMockScanHistory — 创建模拟扫描历史
     */
    private fun createMockScanHistory(): List<ScanRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            ScanRecord(
                id = "scan_${now - 86400000 * 2}",
                timestamp = now - 86400000 * 2,
                score = 55,
                issueCounts = IssueCounts(p0Count = 2, p1Count = 2, p2Count = 3)
            ),
            ScanRecord(
                id = "scan_${now - 86400000 * 5}",
                timestamp = now - 86400000 * 5,
                score = 48,
                issueCounts = IssueCounts(p0Count = 3, p1Count = 1, p2Count = 2)
            )
        )
    }
}
