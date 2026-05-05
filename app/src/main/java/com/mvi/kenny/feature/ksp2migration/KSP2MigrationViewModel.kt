package com.mvi.kenny.feature.ksp2migration

// ================================================================
// KSP2MigrationViewModel — KSP1→KSP2 迁移工具包 ViewModel
// ================================================================
// MVI ViewModel for KSP2 Migration Toolkit.
//
// PRD-229: KSP1→KSP2 迁移工具包
// Design Reference: memory/agency/designs/PRD-229-KSP2-Migration-Toolkit.md
//
// Key Responsibilities:
//   1. Process user Intents and update State accordingly
//   2. Manage scan/compliance simulation logic
//   3. Send one-time Effects via Channel for UI side-effects
//
// Architecture: MVI (Model-View-Intent)
//   - State: single source of truth, immutable
//   - Intent: user actions, processed here
//   - Effect: one-time side effects (toast, clipboard, share)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * KSP2MigrationViewModel — 主 ViewModel
 * ============================================================
 * Manages all state and business logic for the KSP2 migration toolkit.
 *
 * @param initialState 初始状态，默认使用 KSP2MigrationState.Initial
 *
 * MVI Pattern:
 *   State is exposed as StateFlow (read-only, single source of truth)
 *   Intent is received via sendIntent() method
 *   Effect is sent via _effect Channel (one-time events)
 *
 * @see KSP2MigrationContract for all available State, Intent, Effect definitions
 */
class KSP2MigrationViewModel(
    private val initialState: KSP2MigrationState = KSP2MigrationState.Initial
) : ViewModel() {

    // ── State (M) ────────────────────────────────────────────────
    // Mutable state, single source of truth
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<KSP2MigrationState> = _state.asStateFlow()

    // ── Effect (E) ───────────────────────────────────────────────
    // One-time side effects channel
    private val _effect = MutableSharedFlow<KSP2MigrationEffect>()
    val effect = _effect as SharedFlow<KSP2MigrationEffect>

    // ── Intent Handler ────────────────────────────────────────────
    /**
     * Main entry point for user Intents.
     * All user actions are dispatched here and processed accordingly.
     *
     * @param intent The user action/intent to process
     */
    fun sendIntent(intent: KSP2MigrationIntent) {
        viewModelScope.launch {
            when (intent) {
                is KSP2MigrationIntent.SetTab -> handleSetTab(intent.index)
                is KSP2MigrationIntent.PasteGradleContent -> handlePasteGradle(intent.content)
                is KSP2MigrationIntent.StartScan -> handleStartScan()
                is KSP2MigrationIntent.FilterMatrix -> handleFilterMatrix(intent.query)
                is KSP2MigrationIntent.SelectApiCategory -> handleSelectApiCategory(intent.category)
                is KSP2MigrationIntent.ToggleStep -> handleToggleStep(intent.stepIndex)
                is KSP2MigrationIntent.RunComplianceCheck -> handleRunComplianceCheck()
                is KSP2MigrationIntent.ExportReport -> handleExportReport()
                is KSP2MigrationIntent.DismissSnackbar -> handleDismissSnackbar()
                is KSP2MigrationIntent.ResetAll -> handleResetAll()
            }
        }
    }

    // ── State Handlers ───────────────────────────────────────────

    /**
     * Handle tab switching.
     * Switches between the 5 main tabs: Scanner/Matrix/Migration/API/Compliance.
     *
     * @param index New tab index (0-4)
     */
    private fun handleSetTab(index: Int) {
        _state.value = _state.value.copy(currentTab = index)
    }

    /**
     * Handle Gradle file content paste (Tab1 Scanner).
     * Stores the raw content for scan processing.
     *
     * @param content Gradle file content pasted by user
     */
    private fun handlePasteGradle(content: String) {
        _state.value = _state.value.copy(gradleFileContent = content)
    }

    /**
     * Handle KSP scan trigger (Tab1 Scanner).
     * Parses the pasted Gradle content to detect ksp { } configurations.
     * Shows simulated results if no real content or for demo purposes.
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)
            _effect.emit(KSP2MigrationEffect.ShowSnackbar("开始扫描 KSP 配置..."))

            // Simulate scan progress animation
            for (i in 1..10) {
                delay(100)
                _state.value = _state.value.copy(scanProgress = i / 10f)
            }

            // Parse the pasted Gradle content to extract ksp configurations
            val content = _state.value.gradleFileContent
            val scanResults = parseKspConfigurations(content)

            // Extract detected versions
            val kotlinVersion = extractKotlinVersion(content)
            val kspVersion = extractKspVersion(content)

            _state.value = _state.value.copy(
                isScanning = false,
                scanProgress = 1f,
                scanResults = scanResults,
                scanDetectedKotlinVersion = kotlinVersion,
                scanDetectedKspVersion = kspVersion
            )

            if (scanResults.isNotEmpty()) {
                _effect.emit(KSP2MigrationEffect.ShowSnackbar("扫描完成，检测到 ${scanResults.size} 个 KSP processor"))
            } else {
                // Show simulated results for demo when no content or for demonstration
                _state.value = _state.value.copy(scanResults = SIMULATED_SCAN_RESULTS)
                _effect.emit(KSP2MigrationEffect.ShowSnackbar("扫描完成（演示模式），检测到 5 个 KSP processor"))
            }
        }
    }

    /**
     * Parse ksp { } configurations from Gradle content.
     * Extracts processor names, versions, and compatibility status.
     *
     * @param content Gradle file content
     * @return List of detected ProcessorScanResult
     */
    private fun parseKspConfigurations(content: String): List<ProcessorScanResult> {
        if (content.isBlank()) return emptyList()

        val results = mutableListOf<ProcessorScanResult>()
        val kspProcessorRegex = Regex("""ksp\s*\(\s*["']([^"']+)["']\s*(?:,\s*["']([^"']+)["'])?\s*\)""")

        kspProcessorRegex.findAll(content).forEach { match ->
            val artifact = match.groupValues[1]
            val version = match.groupValues.getOrNull(2) ?: "unknown"

            // Find compatibility info from built-in data
            val processorInfo = KSP2_PROCESSORS.find { artifact.contains(it.name, ignoreCase = true) }
            val compatibility = processorInfo?.compatibility ?: Compatibility.UNKNOWN

            results.add(
                ProcessorScanResult(
                    name = artifact.substringAfterLast(":").substringBefore("-"),
                    version = version,
                    compatibility = compatibility,
                    configSnippet = match.value,
                    ksp2CompatibleVersion = processorInfo?.ksp2CompatibleSince,
                    notes = when (compatibility) {
                        Compatibility.SUPPORTED -> "KSP2 兼容 ✅"
                        Compatibility.BETA -> "Beta 支持 KSP2 ⚠️"
                        Compatibility.UNSUPPORTED -> "不支持 KSP2 ❌"
                        Compatibility.UNKNOWN -> "兼容性未知"
                    }
                )
            )
        }

        return results
    }

    /**
     * Extract Kotlin version from Gradle content.
     *
     * @param content Gradle file content
     * @return Kotlin version string or null
     */
    private fun extractKotlinVersion(content: String): String? {
        val kotlinVersionRegex = Regex("""kotlin\s*\{[^}]*version\s*=\s*["']([^"']+)["']""", RegexOption.DOT_MATCHES_ALL)
        return kotlinVersionRegex.find(content)?.groupValues?.get(1)
    }

    /**
     * Extract KSP version from Gradle content.
     *
     * @param content Gradle file content
     * @return KSP version string or null
     */
    private fun extractKspVersion(content: String): String? {
        val kspVersionRegex = Regex("""ksp\s*\{[^}]*arg\s*\(\s*["']kspVersion["']\s*,\s*["']([^"']+)["']""", RegexOption.DOT_MATCHES_ALL)
        return kspVersionRegex.find(content)?.groupValues?.get(1)
    }

    /**
     * Handle matrix filter input (Tab2).
     *
     * @param query Filter/search query
     */
    private fun handleFilterMatrix(query: String) {
        _state.value = _state.value.copy(matrixFilter = query)
    }

    /**
     * Handle API category selection (Tab4).
     *
     * @param category Category name
     */
    private fun handleSelectApiCategory(category: String) {
        _state.value = _state.value.copy(selectedApiCategory = category)
    }

    /**
     * Handle migration step toggle (Tab3).
     * Marks a step as completed or reverts it to pending.
     *
     * @param stepIndex Step index to toggle
     */
    private fun handleToggleStep(stepIndex: Int) {
        val currentCompleted = _state.value.completedSteps.toMutableSet()
        if (currentCompleted.contains(stepIndex)) {
            currentCompleted.remove(stepIndex)
        } else {
            currentCompleted.add(stepIndex)
        }
        _state.value = _state.value.copy(completedSteps = currentCompleted)

        viewModelScope.launch {
            val step = _state.value.migrationSteps.find { it.stepNumber == stepIndex }
            val message = if (currentCompleted.contains(stepIndex)) {
                "✅ 步骤 ${step?.stepNumber}: ${step?.title} 已完成"
            } else {
                "⏳ 步骤 ${step?.stepNumber}: ${step?.title} 标记为待处理"
            }
            _effect.emit(KSP2MigrationEffect.ShowSnackbar(message))
        }
    }

    /**
     * Handle CI compliance check trigger (Tab5).
     * Simulates running compliance checks for KSP2 configuration.
     */
    private fun handleRunComplianceCheck() {
        viewModelScope.launch {
            _state.value = _state.value.copy(complianceState = ComplianceState.RUNNING)
            _effect.emit(KSP2MigrationEffect.ShowSnackbar("正在运行 CI 合规检测..."))

            // Simulate check progress
            delay(1500)

            // Use simulation data for demo purposes
            val results = SIMULATED_COMPLIANCE_RESULTS
            val passedCount = results.count { it.passed }
            val overallCompliance = when {
                passedCount == results.size -> ComplianceLevel.COMPLIANT
                passedCount > 0 -> ComplianceLevel.PARTIAL
                else -> ComplianceLevel.NON_COMPLIANT
            }

            _state.value = _state.value.copy(
                complianceState = ComplianceState.COMPLETED,
                complianceResults = results,
                overallCompliance = overallCompliance
            )

            _effect.emit(KSP2MigrationEffect.ShowSnackbar("合规检测完成：$passedCount/${results.size} 项通过"))
        }
    }

    /**
     * Handle compliance report export (Tab5).
     * Generates a text report and triggers share effect.
     */
    private fun handleExportReport() {
        viewModelScope.launch {
            val state = _state.value
            val report = buildComplianceReport(state)

            _state.value = _state.value.copy(exportedReport = report)
            _effect.emit(KSP2MigrationEffect.ShareReport(report))
        }
    }

    /**
     * Build compliance report text from current state.
     *
     * @param state Current state
     * @return Formatted compliance report string
     */
    private fun buildComplianceReport(state: KSP2MigrationState): String {
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("  KSP2 迁移合规检测报告")
        sb.appendLine("  KSP2 Migration Compliance Report")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        sb.appendLine("整体合规状态: ${state.overallCompliance.emoji} ${state.overallCompliance.displayName}")
        sb.appendLine("检测时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        sb.appendLine()
        sb.appendLine("───────────────────────────────────────")
        sb.appendLine("检测结果详情:")
        sb.appendLine()

        state.complianceResults.forEach { result ->
            sb.appendLine("${if (result.passed) "✅" else "❌"} ${result.checkName}")
            sb.appendLine("   ${result.message}")
            if (result.suggestion.isNotBlank()) {
                sb.appendLine("   💡 建议: ${result.suggestion}")
            }
            sb.appendLine()
        }

        sb.appendLine("───────────────────────────────────────")
        sb.appendLine("如有问题，请参考「迁移指南」Tab 逐步处理。")
        sb.appendLine()
        sb.appendLine("Generated by KSP2 Migration Toolkit (PRD-229)")

        return sb.toString()
    }

    /**
     * Handle dismiss snackbar action.
     */
    private fun handleDismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    /**
     * Handle full state reset.
     */
    private fun handleResetAll() {
        _state.value = KSP2MigrationState.Initial
        viewModelScope.launch {
            _effect.emit(KSP2MigrationEffect.ShowSnackbar("状态已重置"))
        }
    }

    // ── Initialization ────────────────────────────────────────────
    init {
        // Initialize with built-in data
        _state.value = _state.value.copy(
            processors = KSP2_PROCESSORS,
            migrationSteps = KSP2_MIGRATION_STEPS,
            apiChanges = KSP2_API_CHANGES
        )
    }
}
