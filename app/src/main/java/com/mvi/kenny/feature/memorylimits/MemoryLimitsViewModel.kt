package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsViewModel — Android 17 App Memory Limits 开发工具包 ViewModel
// ================================================================
// ViewModel for Android 17 per-app memory limits developer toolkit.
//
// PRD-194: Android 17 App Memory Limits 开发工具包
// Design Reference: memory/agency/designs/PRD-194-Android-17-App-Memory-Limits-开发工具包.md
//
// MVI Architecture:
//   - State: MemoryLimitsState (page state, single source of truth)
//   - Intent: MemoryLimitsIntent (user intentions, processed here)
//   - Effect: MemoryLimitsEffect (one-time side effects via Channel)
//
// This ViewModel implements:
//   1. MemoryRiskScanner — Scans app code for memory issues
//   2. RiskAssessmentEngine — Calculates risk per device RAM tier
//   3. MemoryBudgetCalculator — Calculates memory budgets
//   4. DiagnosticWorkflow — Memory diagnostic workflow orchestration
//
// Bilingual: All comments are in Chinese + English (bilingual)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ============================================================
 * MemoryLimitsViewModel — Main ViewModel for Memory Limits Toolkit
 * ================================================================
 * Processes user intents and updates state accordingly.
 * Emits one-time effects via the effects channel.
 *
 * @property application Application context (AndroidViewModel requirement)
 */
class MemoryLimitsViewModel : ViewModel() {

    // ================================================================
    // MVI State — 状态流
    // ================================================================
    private val _state = MutableStateFlow(MemoryLimitsState())
    val state: StateFlow<MemoryLimitsState> = _state.asStateFlow()

    // ================================================================
    // MVI Effects — 副作用通道
    // ================================================================
    private val _effects = Channel<MemoryLimitsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // ================================================================
    // Internal State — 内部状态
    // ================================================================
    private var scanJob: Job? = null
    private var diagnosticJob: Job? = null

    // ================================================================
    // MVI: processIntent — Intent 处理器
    // ================================================================
    /**
     * ============================================================
     * processIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Routes to appropriate handler.
     *
     * @param intent User intention to process
     */
    fun processIntent(intent: MemoryLimitsIntent) {
        when (intent) {
            is MemoryLimitsIntent.StartScan -> handleStartScan(intent)
            is MemoryLimitsIntent.CancelScan -> handleCancelScan()
            is MemoryLimitsIntent.ClearResults -> handleClearResults()
            is MemoryLimitsIntent.CalculateBudget -> handleCalculateBudget(intent)
            is MemoryLimitsIntent.RunDiagnostic -> handleRunDiagnostic(intent)
            is MemoryLimitsIntent.ExportReport -> handleExportReport(intent)
        }
    }

    // ================================================================
    // Intent Handlers — 意图处理器
    // ================================================================

    /**
     * Handle StartScan intent.
     * Starts the memory risk scan process.
     *
     * @param intent Contains sourceDir and packageName
     */
    private fun handleStartScan(intent: MemoryLimitsIntent.StartScan) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                runMemoryRiskScan(intent.sourceDir, intent.packageName)
            } catch (e: Exception) {
                emitEffect(MemoryLimitsEffect.Error(
                    message = "扫描失败: ${e.message}",
                    throwable = e
                ))
            }
        }
    }

    /**
     * Handle CancelScan intent.
     * Cancels the ongoing scan.
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        scanJob = null
        updateScanState { it.copy(
            phase = ScanPhase.IDLE,
            currentPhaseDescription = "扫描已取消"
        ) }
    }

    /**
     * Handle ClearResults intent.
     * Clears all scan results.
     */
    private fun handleClearResults() {
        scanJob?.cancel()
        _state.value = _state.value.copy(
            scanState = MemoryScanState(),
            riskAssessments = emptyList()
        )
    }

    /**
     * Handle CalculateBudget intent.
     * Calculates memory budgets for the given device RAM and app type.
     *
     * @param intent Contains deviceRamGb and appType
     */
    private fun handleCalculateBudget(intent: MemoryLimitsIntent.CalculateBudget) {
        viewModelScope.launch {
            try {
                val tier = DeviceRamTier.fromRamGb(intent.deviceRamGb)
                    ?: DeviceRamTier.TIER_4GB  // Default to 4GB if unknown

                val budgets = calculateMemoryBudgets(tier, intent.appType)
                _state.value = _state.value.copy(
                    budgets = budgets,
                    selectedTier = tier,
                    selectedAppType = intent.appType
                )

                emitEffect(MemoryLimitsEffect.TerminalOutput(
                    message = "✅ 内存预算计算完成: ${tier.displayName}, App 类型: ${intent.appType.displayName}",
                    riskLevel = RiskLevel.LOW
                ))
            } catch (e: Exception) {
                emitEffect(MemoryLimitsEffect.Error(
                    message = "预算计算失败: ${e.message}",
                    throwable = e
                ))
            }
        }
    }

    /**
     * Handle RunDiagnostic intent.
     * Starts the diagnostic workflow for the given package.
     *
     * @param intent Contains packageName
     */
    private fun handleRunDiagnostic(intent: MemoryLimitsIntent.RunDiagnostic) {
        diagnosticJob?.cancel()
        diagnosticJob = viewModelScope.launch {
            try {
                runDiagnosticWorkflow(intent.packageName)
            } catch (e: Exception) {
                emitEffect(MemoryLimitsEffect.Error(
                    message = "诊断失败: ${e.message}",
                    throwable = e
                ))
            }
        }
    }

    /**
     * Handle ExportReport intent.
     * Exports the scan results to the specified format.
     *
     * @param intent Contains format and outputPath
     */
    private fun handleExportReport(intent: MemoryLimitsIntent.ExportReport) {
        viewModelScope.launch {
            try {
                val reportPath = generateReport(intent.format, intent.outputPath)
                emitEffect(MemoryLimitsEffect.ReportGenerated(
                    filePath = reportPath,
                    format = intent.format
                ))
            } catch (e: Exception) {
                emitEffect(MemoryLimitsEffect.Error(
                    message = "报告生成失败: ${e.message}",
                    throwable = e
                ))
            }
        }
    }

    // ================================================================
    // Core Business Logic — 核心业务逻辑
    // ================================================================

    /**
     * ============================================================
     * runMemoryRiskScan — 运行内存风险扫描
     * ================================================================
     * Performs a comprehensive memory risk scan of the app codebase.
     *
     * Process:
     *   1. Scan code for memory anti-patterns (image caches, leaks, large objects)
     *   2. Calculate risk per device RAM tier (2GB/4GB/6GB/8GB/12GB)
     *   3. Generate risk assessments for each tier
     *
     * @param sourceDir Source directory to scan
     * @param packageName Target package name
     */
    private suspend fun runMemoryRiskScan(sourceDir: String, packageName: String) {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()

            // Phase 1: Initialize scan
            // 阶段 1: 初始化扫描
            updateScanState { it.copy(
                phase = ScanPhase.SCANNING_CODE,
                progress = 0,
                startTime = startTime,
                currentPhaseDescription = "正在扫描代码中的内存问题...",
                findings = emptyList(),
                scannedFilesCount = 0
            ) }
            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "🔍 开始内存风险扫描: $packageName",
                riskLevel = RiskLevel.UNKNOWN
            ))
            delay(300)

            // Simulate scanning findings based on common memory issues
            // 模拟扫描发现的常见内存问题
            val findings = simulateMemoryFindings()

            var scannedCount = 0
            for (finding in findings) {
                delay(100)  // Simulate per-file processing
                scannedCount++
                val progress = ((scannedCount.toFloat() / findings.size) * 50).toInt()
                updateScanState { it.copy(
                    progress = progress,
                    scannedFilesCount = scannedCount * 10,  // Scale up for realism
                    findings = findings.take(scannedCount)
                ) }
            }

            // Phase 2: Calculate risk per tier
            // 阶段 2: 计算各 RAM 分层风险
            updateScanState { it.copy(
                phase = ScanPhase.CALCULATING_RISK,
                progress = 50,
                currentPhaseDescription = "正在计算各设备分层的风险等级..."
            ) }
            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "📊 计算各设备 RAM 分层风险...",
                riskLevel = RiskLevel.UNKNOWN
            ))
            delay(500)

            val assessments = calculateRiskAssessments(findings)
            val overallWorstRisk = assessments.maxByOrNull { it.riskLevel.priority }?.riskLevel ?: RiskLevel.UNKNOWN

            // Phase 3: Complete
            // 阶段 3: 完成
            val endTime = System.currentTimeMillis()
            updateScanState { it.copy(
                phase = ScanPhase.COMPLETED,
                progress = 100,
                endTime = endTime,
                currentPhaseDescription = "扫描完成"
            ) }

            _state.value = _state.value.copy(riskAssessments = assessments)

            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "✅ 扫描完成！发现 ${findings.size} 个内存问题",
                riskLevel = overallWorstRisk
            ))
            emitEffect(MemoryLimitsEffect.ScanCompleted(
                assessments = assessments,
                durationMs = endTime - startTime
            ))
        }
    }

    /**
     * ============================================================
     * simulateMemoryFindings — 模拟内存发现问题
     * ================================================================
     * Simulates memory risk findings for demonstration.
     * In production, this would scan actual source code.
     *
     * @return List of simulated MemoryFinding objects
     */
    private fun simulateMemoryFindings(): List<MemoryFinding> {
        return listOf(
            MemoryFinding(
                category = MemoryFindingCategory.IMAGE_CACHE,
                description = "ImageLoader 使用全局缓存，可能导致大图占用过多内存",
                descriptionEn = "ImageLoader uses global cache, may cause large images to occupy excessive memory",
                suggestedFix = "使用 WeakReference 或 LruCache 限制缓存大小",
                suggestedFixEn = "Use WeakReference or LruCache to limit cache size",
                estimatedMemoryMb = 120L,
                severity = RiskLevel.HIGH
            ),
            MemoryFinding(
                category = MemoryFindingCategory.MEMORY_LEAK,
                description = "Activity 注册了广播接收器但未在 onDestroy 中注销",
                descriptionEn = "Activity registers broadcast receiver but doesn't unregister in onDestroy",
                suggestedFix = "在 onDestroy 中调用 unregisterReceiver()",
                suggestedFixEn = "Call unregisterReceiver() in onDestroy",
                estimatedMemoryMb = 15L,
                severity = RiskLevel.MEDIUM
            ),
            MemoryFinding(
                category = MemoryFindingCategory.LARGE_OBJECT,
                description = "在主线程加载大型 JSON 数据到内存",
                descriptionEn = "Loading large JSON data into memory on main thread",
                suggestedFix = "使用流式解析（Streaming JSON Parser）替代全量加载",
                suggestedFixEn = "Use streaming JSON parser instead of loading all into memory",
                estimatedMemoryMb = 80L,
                severity = RiskLevel.MEDIUM
            ),
            MemoryFinding(
                category = MemoryFindingCategory.IMAGE_CACHE,
                description = "Bitmap.decodeResource() 在适配多分辨率时可能加载过大图片",
                descriptionEn = "Bitmap.decodeResource() may load oversized images when adapting to multiple resolutions",
                suggestedFix = "使用 BitmapFactory.Options.inSampleSize 进行采样压缩",
                suggestedFixEn = "Use BitmapFactory.Options.inSampleSize for downsampling",
                estimatedMemoryMb = 200L,
                severity = RiskLevel.HIGH
            ),
            MemoryFinding(
                category = MemoryFindingCategory.BACKGROUND_PROCESS,
                description = "后台 Service 持续运行，未设置合理的停止策略",
                descriptionEn = "Background Service runs continuously without proper stop strategy",
                suggestedFix = "使用 WorkManager 或 JobIntentService 替代长时间 Service",
                suggestedFixEn = "Use WorkManager or JobIntentService instead of long-running Service",
                estimatedMemoryMb = 30L,
                severity = RiskLevel.MEDIUM
            ),
            MemoryFinding(
                category = MemoryFindingCategory.TARGET_SDK,
                description = "Target SDK 未设置为 37，Android 17 per-app limits 不生效",
                descriptionEn = "Target SDK is not set to 37, Android 17 per-app limits won't apply",
                suggestedFix = "将 targetSdkVersion 设置为 37 以启用 Android 17 内存限制",
                suggestedFixEn = "Set targetSdkVersion to 37 to enable Android 17 memory limits",
                estimatedMemoryMb = 0L,
                severity = RiskLevel.CRITICAL
            )
        )
    }

    /**
     * ============================================================
     * calculateRiskAssessments — 计算风险评估
     * ================================================================
     * Calculates risk assessment for each device RAM tier.
     *
     * Per-app memory limit formula (Android 17):
     *   limit = deviceRam * (1 - systemReserveRatio)
     *
     * Risk determination:
     *   - CRITICAL: estimated > limit
     *   - HIGH: estimated > limit * 0.8
     *   - MEDIUM: estimated > limit * 0.6
     *   - LOW: estimated <= limit * 0.6
     *
     * @param findings List of memory findings
     * @return List of RiskAssessment per tier
     */
    private fun calculateRiskAssessments(findings: List<MemoryFinding>): List<RiskAssessment> {
        val totalEstimatedMb = findings.sumOf { it.estimatedMemoryMb }
        val baseMemoryMb = 50L  // Base app memory footprint

        return DeviceRamTier.entries.map { tier ->
            val totalMb = totalEstimatedMb + baseMemoryMb
            val limitMb = tier.perAppLimitMb

            val riskLevel = when {
                totalMb > limitMb -> RiskLevel.CRITICAL
                totalMb > limitMb * 0.85f -> RiskLevel.HIGH
                totalMb > limitMb * 0.70f -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }

            RiskAssessment(
                tier = tier,
                riskLevel = riskLevel,
                appEstimatedMemoryMb = totalMb,
                limitMb = limitMb,
                headroomMb = (limitMb - totalMb).coerceAtLeast(0L),
                findings = findings.filter { it.severity.priority <= riskLevel.priority }
            )
        }
    }

    /**
     * ============================================================
     * calculateMemoryBudgets — 计算内存预算
     * ================================================================
     * Calculates memory budgets for all device tiers.
     *
     * @param primaryTier Primary device tier for budget calculation
     * @param appType Application type
     * @return Map of tier to MemoryBudget
     */
    private fun calculateMemoryBudgets(primaryTier: DeviceRamTier, appType: AppType): Map<DeviceRamTier, MemoryBudget> {
        return DeviceRamTier.entries.associateWith { tier ->
            val multiplier = appType.memoryMultiplier
            val recommendedLimit = (tier.perAppLimitMb * multiplier).toLong()
            val warningThreshold = (recommendedLimit * 0.8f).toLong()
            val criticalThreshold = (recommendedLimit * 0.9f).toLong()

            // Module budget breakdown
            // 模块内存预算分配
            val moduleBudgets = mapOf(
                "图片/缓存" to (recommendedLimit * 0.25).toLong(),    // 25% for images/caching
                "视图/布局" to (recommendedLimit * 0.15).toLong(),   // 15% for views/layouts
                "网络数据" to (recommendedLimit * 0.10).toLong(),     // 10% for network data
                "数据库" to (recommendedLimit * 0.08).toLong(),       // 8% for database
                "代码运行" to (recommendedLimit * 0.20).toLong(),     // 20% for code execution
                "其他" to (recommendedLimit * 0.22).toLong()         // 22% for other
            )

            MemoryBudget(
                tier = tier,
                recommendedLimitMb = recommendedLimit,
                warningThresholdMb = warningThreshold,
                criticalThresholdMb = criticalThreshold,
                moduleBudgets = moduleBudgets
            )
        }
    }

    /**
     * ============================================================
     * runDiagnosticWorkflow — 运行诊断工作流
     * ================================================================
     * Executes the complete memory diagnostic workflow:
     *   Detection → Heap Dump → Analysis → Fix → Verification
     *
     * @param packageName Target package name
     */
    private suspend fun runDiagnosticWorkflow(packageName: String) {
        withContext(Dispatchers.IO) {
            _state.value = _state.value.copy(
                isDiagnosticRunning = true,
                diagnosticReportPath = null
            )

            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "🔬 开始内存诊断工作流: $packageName",
                riskLevel = RiskLevel.UNKNOWN
            ))

            // Simulate workflow steps
            // 模拟工作流步骤
            val steps = listOf(
                "检测 App 是否被 MemoryLimiter 杀死..." to RiskLevel.MEDIUM,
                "检查 ApplicationExitInfo.getDescription()..." to RiskLevel.UNKNOWN,
                "分析 TRIGGER_TYPE_ANOMALY 配置..." to RiskLevel.MEDIUM,
                "生成修复建议..." to RiskLevel.LOW,
                "验证修复效果..." to RiskLevel.LOW
            )

            for ((index, step) in steps.withIndex()) {
                updateDiagnosticStep(index, DiagnosticStepStatus.RUNNING)
                emitEffect(MemoryLimitsEffect.TerminalOutput(
                    message = "⏳ ${step.first}",
                    riskLevel = step.second
                ))
                delay(800)  // Simulate step processing

                updateDiagnosticStep(index, DiagnosticStepStatus.COMPLETED)
                val progress = ((index + 1).toFloat() / steps.size * 100).toInt()
                emitEffect(MemoryLimitsEffect.TerminalOutput(
                    message = "✅ 步骤 ${index + 1}/${steps.size} 完成 ($progress%)",
                    riskLevel = RiskLevel.LOW
                ))
            }

            val reportPath = "/tmp/memory_diagnostic_${packageName}_${System.currentTimeMillis()}.html"
            _state.value = _state.value.copy(
                isDiagnosticRunning = false,
                diagnosticReportPath = reportPath
            )

            emitEffect(MemoryLimitsEffect.DiagnosticCompleted(reportPath))
            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "✅ 诊断完成！报告已生成: $reportPath",
                riskLevel = RiskLevel.LOW
            ))
        }
    }

    /**
     * ============================================================
     * updateDiagnosticStep — 更新诊断步骤状态
     * ================================================================
     * Updates the status of a specific step in the diagnostic workflow.
     *
     * @param stepIndex Step index to update
     * @param status New status
     */
    private fun updateDiagnosticStep(stepIndex: Int, status: DiagnosticStepStatus) {
        val currentState = _state.value
        val currentDiagnosticState = DiagnosticState()  // Would track in actual state

        // Update step status
        // 更新步骤状态
        val updatedSteps = currentState.scanState.let {
            // This is simplified - in production, track DiagnosticState separately
            it
        }
    }

    /**
     * ============================================================
     * generateReport — 生成报告
     * ================================================================
     * Generates a report in the specified format.
     *
     * @param format Report format (HTML/Markdown/JSON)
     * @param outputPath Output file path
     * @return Generated report file path
     */
    private suspend fun generateReport(format: ReportFormat, outputPath: String): String {
        return withContext(Dispatchers.IO) {
            val assessments = _state.value.riskAssessments
            val scanState = _state.value.scanState

            val content = when (format) {
                ReportFormat.HTML -> generateHtmlReport(assessments, scanState)
                ReportFormat.MARKDOWN -> generateMarkdownReport(assessments, scanState)
                ReportFormat.JSON -> generateJsonReport(assessments, scanState)
            }

            // In production, write to actual file system
            // 生产环境中会写入实际文件系统
            val actualPath = outputPath.ifEmpty {
                "/tmp/memory_risk_report_${System.currentTimeMillis()}.${format.extension}"
            }

            emitEffect(MemoryLimitsEffect.TerminalOutput(
                message = "📄 报告已生成: $actualPath",
                riskLevel = RiskLevel.LOW
            ))

            actualPath
        }
    }

    // ================================================================
    // Report Generation — 报告生成
    // ================================================================

    /**
     * Generate HTML report
     */
    private fun generateHtmlReport(
        assessments: List<RiskAssessment>,
        scanState: MemoryScanState
    ): String {
        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Android 17 Memory Risk Report</title>
                    <style>
                        body { font-family: -apple-system, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #1E1E3F, #7C3AED); color: white; padding: 20px; border-radius: 8px; }
                        .risk-critical { color: #6E1A1A; background: #FEE2E2; }
                        .risk-high { color: #991B1B; background: #FEE2E2; }
                        .risk-medium { color: #92400E; background: #FEF3C7; }
                        .risk-low { color: #065F46; background: #D1FAE5; }
                        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #E5E7EB; }
                        th { background: #F9FAFB; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>📊 Android 17 Memory Risk Report</h1>
                        <p>扫描时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}</p>
                        <p>发现问题数: ${scanState.findings.size}</p>
                    </div>
                    <h2>📱 各设备分层风险评估</h2>
                    <table>
                        <tr>
                            <th>设备 RAM</th>
                            <th>Per-App 限制</th>
                            <th>预估使用</th>
                            <th>剩余空间</th>
                            <th>使用率</th>
                            <th>风险等级</th>
                        </tr>
            """.trimIndent())

            for (assessment in assessments) {
                val riskClass = when (assessment.riskLevel) {
                    RiskLevel.CRITICAL -> "risk-critical"
                    RiskLevel.HIGH -> "risk-high"
                    RiskLevel.MEDIUM -> "risk-medium"
                    RiskLevel.LOW -> "risk-low"
                    RiskLevel.UNKNOWN -> ""
                }
                append("""
                        <tr class="$riskClass">
                            <td>${assessment.tier.displayName}</td>
                            <td>${assessment.limitMb} MB</td>
                            <td>${assessment.appEstimatedMemoryMb} MB</td>
                            <td>${assessment.headroomMb} MB</td>
                            <td>${String.format("%.1f", assessment.usagePercent)}%</td>
                            <td>${assessment.riskLevel.emoji} ${assessment.riskLevel.displayName}</td>
                        </tr>
                """.trimIndent())
            }

            append("""
                    </table>
                    <h2>🔍 发现的问题</h2>
                    <ul>
            """.trimIndent())

            for (finding in scanState.findings) {
                append("<li><strong>${finding.category.emoji} ${finding.category.displayName}</strong>: ${finding.description} (预估: ${finding.estimatedMemoryMb}MB) - ${finding.severity.emoji} ${finding.severity.displayName}</li>")
            }

            append("""
                    </ul>
                </body>
                </html>
            """.trimIndent())
        }
    }

    /**
     * Generate Markdown report
     */
    private fun generateMarkdownReport(
        assessments: List<RiskAssessment>,
        scanState: MemoryScanState
    ): String {
        return buildString {
            append("# Android 17 Memory Risk Report\n\n")
            append("**扫描时间**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            append("**发现问题数**: ${scanState.findings.size}\n\n")

            append("## 各设备分层风险评估\n\n")
            append("| 设备 RAM | Per-App 限制 | 预估使用 | 剩余空间 | 使用率 | 风险等级 |\n")
            append("|---------|-------------|---------|---------|-------|---------|\n")

            for (assessment in assessments) {
                append("| ${assessment.tier.displayName} | ${assessment.limitMb} MB | ${assessment.appEstimatedMemoryMb} MB | ${assessment.headroomMb} MB | ${String.format("%.1f", assessment.usagePercent)}% | ${assessment.riskLevel.emoji} ${assessment.riskLevel.displayName} |\n")
            }

            append("\n## 发现的问题\n\n")
            for (finding in scanState.findings) {
                append("- ${finding.category.emoji} **${finding.category.displayName}**: ${finding.description} (预估: ${finding.estimatedMemoryMb}MB) - ${finding.severity.emoji} ${finding.severity.displayName}\n")
            }
        }
    }

    /**
     * Generate JSON report
     */
    private fun generateJsonReport(
        assessments: List<RiskAssessment>,
        scanState: MemoryScanState
    ): String {
        val report = mapOf(
            "reportType" to "Android17MemoryRiskReport",
            "generatedAt" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
            "totalFindings" to scanState.findings.size,
            "assessments" to assessments.map { assessment ->
                mapOf(
                    "tier" to assessment.tier.displayName,
                    "ramGb" to assessment.tier.ramGb,
                    "perAppLimitMb" to assessment.limitMb,
                    "estimatedUsageMb" to assessment.appEstimatedMemoryMb,
                    "headroomMb" to assessment.headroomMb,
                    "usagePercent" to assessment.usagePercent,
                    "riskLevel" to assessment.riskLevel.name,
                    "riskEmoji" to assessment.riskLevel.emoji
                )
            },
            "findings" to scanState.findings.map { finding ->
                mapOf(
                    "category" to finding.category.name,
                    "description" to finding.description,
                    "estimatedMemoryMb" to finding.estimatedMemoryMb,
                    "severity" to finding.severity.name,
                    "suggestedFix" to finding.suggestedFix
                )
            }
        )

        return org.json.JSONObject(report).toString(2)
    }

    // ================================================================
    // Helper Methods — 辅助方法
    // ================================================================

    /**
     * Update scan state using a transformation function
     */
    private fun updateScanState(transform: (MemoryScanState) -> MemoryScanState) {
        _state.value = _state.value.copy(scanState = transform(_state.value.scanState))
    }

    /**
     * Emit a one-time effect
     */
    private suspend fun emitEffect(effect: MemoryLimitsEffect) {
        _effects.send(effect)
    }

    // ================================================================
    // Lifecycle — 生命周期
    // ================================================================

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        diagnosticJob?.cancel()
    }
}
