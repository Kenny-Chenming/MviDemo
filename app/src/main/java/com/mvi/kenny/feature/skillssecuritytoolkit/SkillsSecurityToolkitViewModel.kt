package com.mvi.kenny.feature.skillssecuritytoolkit

// ================================================================
// SkillsSecurityToolkitViewModel — Android Skills 安全扫描工具包 MVI ViewModel
// ================================================================
// ViewModel for Android Skills Security Scanner.
//
// PRD-280: Android Skills 安全扫描工具包
// Design Reference: memory/agency/designs/PRD-280-Android-Skills-安全扫描工具包.md
//
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Dashboard: display security score, recent scan history
//   - Scanner: scan SKILL.md for malicious patterns, vulnerabilities
//   - Report: browse scan history, view detailed attack chain
//   - Reference: searchable reference library (malicious patterns / vulnerabilities / fixes)
//
// Architecture:
//   - State: immutable data class, single source of truth
//   - Intent: user actions, processed by sendIntent()
//   - Effect: one-time side effects via SharedFlow (toast, share, navigate)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ============================================================
 * SkillsSecurityToolkitViewModel — 安全扫描工具包 ViewModel
 * ============================================================
 * Manages the SkillsSecurityToolkitState and processes SkillsSecurityToolkitIntent.
 *
 * @see SkillsSecurityToolkitState
 * @see SkillsSecurityToolkitIntent
 * @see SkillsSecurityToolkitEffect
 */
class SkillsSecurityToolkitViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(SkillsSecurityToolkitState.Initial)
    val state: StateFlow<SkillsSecurityToolkitState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<SkillsSecurityToolkitEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Public API for UI to send intents
    // ─────────────────────────────────────────────────────────────
    /**
     * Send user intent to ViewModel for processing.
     *
     * @param intent User intent action
     */
    fun sendIntent(intent: SkillsSecurityToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is SkillsSecurityToolkitIntent.SelectTab -> handleSelectTab(intent.tab)
                is SkillsSecurityToolkitIntent.QuickScan -> handleQuickScan()
                is SkillsSecurityToolkitIntent.LoadDashboard -> handleLoadDashboard()
                is SkillsSecurityToolkitIntent.SetInputMode -> handleSetInputMode(intent.mode)
                is SkillsSecurityToolkitIntent.UpdatePath -> handleUpdatePath(intent.path)
                is SkillsSecurityToolkitIntent.UpdateUrl -> handleUpdateUrl(intent.url)
                is SkillsSecurityToolkitIntent.UpdateText -> handleUpdateText(intent.text)
                is SkillsSecurityToolkitIntent.StartScan -> handleStartScan()
                is SkillsSecurityToolkitIntent.DismissError -> handleDismissError()
                is SkillsSecurityToolkitIntent.LoadReports -> handleLoadReports()
                is SkillsSecurityToolkitIntent.SelectReport -> handleSelectReport(intent.reportId)
                is SkillsSecurityToolkitIntent.ExportReport -> handleExportReport(intent.reportId, intent.format)
                is SkillsSecurityToolkitIntent.SwitchReferenceTab -> handleSwitchReferenceTab(intent.tab)
                is SkillsSecurityToolkitIntent.SearchReference -> handleSearchReference(intent.query)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    /**
     * Handle tab selection — switch bottom navigation Tab.
     *
     * @param tab Target tab to switch to
     */
    private fun handleSelectTab(tab: SecurityTab) {
        _state.update { it.copy(selectedTab = tab) }
        // Load data for the selected tab if needed
        when (tab) {
            SecurityTab.DASHBOARD -> handleLoadDashboard()
            SecurityTab.REPORT -> handleLoadReports()
            SecurityTab.REFERENCE -> loadReferenceItems()
            SecurityTab.SCANNER -> { /* No pre-load needed */ }
        }
    }

    /**
     * Handle dashboard load — load recent scan history.
     */
    private fun handleLoadDashboard() {
        _state.update { it.copy(dashboardState = it.dashboardState.copy(isLoading = true)) }
        viewModelScope.launch {
            delay(300) // Simulate network/db load
            _state.update {
                it.copy(
                    dashboardState = SIMULATED_DASHBOARD_STATE.copy(isLoading = false)
                )
            }
        }
    }

    /**
     * Handle quick scan trigger from dashboard.
     */
    private fun handleQuickScan() {
        _state.update {
            it.copy(
                selectedTab = SecurityTab.SCANNER,
                scannerState = ScannerState(inputMode = InputMode.TEXT, inputText = "# 示例 SKILL.md\n这是一个示例技能文件")
            )
        }
        viewModelScope.launch {
            handleStartScan()
        }
    }

    /**
     * Handle input mode change.
     *
     * @param mode New input mode
     */
    private fun handleSetInputMode(mode: InputMode) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(inputMode = mode))
        }
    }

    /**
     * Handle local path update.
     *
     * @param path Updated file path
     */
    private fun handleUpdatePath(path: String) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(inputPath = path))
        }
    }

    /**
     * Handle URL update.
     *
     * @param url Updated ClawHub URL
     */
    private fun handleUpdateUrl(url: String) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(inputUrl = url))
        }
    }

    /**
     * Handle text update.
     *
     * @param text Updated SKILL.md text content
     */
    private fun handleUpdateText(text: String) {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(inputText = text))
        }
    }

    /**
     * Handle scan trigger — analyze SKILL.md for security threats.
     * In production, this would parse actual SKILL.md files and check against
     * the malicious pattern database (SkillSpector rules).
     *
     * Scan phases:
     *   PARSING → DETECTING → ANALYZING → GENERATING → DONE
     */
    private suspend fun handleStartScan() {
        _state.update {
            it.copy(
                scannerState = it.scannerState.copy(
                    scanPhase = ScanPhase.PARSING,
                    scanProgress = 0f,
                    scanResult = null,
                    errorMessage = null
                )
            )
        }

        withContext(Dispatchers.Default) {
            val phases = listOf(
                ScanPhase.PARSING to 0.2f,
                ScanPhase.DETECTING to 0.5f,
                ScanPhase.ANALYZING to 0.75f,
                ScanPhase.GENERATING to 0.95f
            )

            for ((phase, progress) in phases) {
                delay(500)
                _state.update {
                    it.copy(
                        scannerState = it.scannerState.copy(
                            scanPhase = phase,
                            scanProgress = progress
                        )
                    )
                }
            }

            // Use simulated result (in production: run actual SKILL.md analysis)
            val scanResult = simulateSecurityScan()

            delay(300)
            _state.update {
                it.copy(
                    scannerState = it.scannerState.copy(
                        scanPhase = ScanPhase.DONE,
                        scanProgress = 1.0f,
                        scanResult = scanResult
                    )
                )
            }

            _effect.emit(
                SkillsSecurityToolkitEffect.ScanComplete(
                    score = scanResult.securityScore,
                    threatLevel = scanResult.threatLevel
                )
            )
        }
    }

    /**
     * Simulate a security scan — returns demo result for preview/testing.
     * In production, this would:
     *   1. Parse SKILL.md markdown structure (use Kotlin regex + manual state machine)
     *   2. Detect malicious patterns (prompt injection, memory poisoning, etc.)
     *   3. Analyze skill permissions (ADB/SDK/Gradle declarations)
     *   4. Check Gradle dependencies for suspicious coordinates
     *   5. Generate SARIF-formatted report
     */
    private fun simulateSecurityScan(): ScanResult {
        val inputText = _state.value.scannerState.inputText
        val hasMaliciousContent = inputText.contains("hidden", ignoreCase = true) ||
                inputText.contains("curl http", ignoreCase = true) ||
                inputText.contains("wget http", ignoreCase = true) ||
                inputText.contains("rm -rf", ignoreCase = true)

        return if (hasMaliciousContent || _state.value.scannerState.inputMode == InputMode.TEXT) {
            SIMULATED_SCAN_RESULT.copy(
                id = UUID.randomUUID().toString(),
                scanTimestamp = System.currentTimeMillis(),
                target = when (_state.value.scannerState.inputMode) {
                    InputMode.LOCAL -> _state.value.scannerState.inputPath
                    InputMode.URL -> _state.value.scannerState.inputUrl
                    InputMode.TEXT -> "Manual Text Input"
                }
            )
        } else {
            ScanResult(
                id = UUID.randomUUID().toString(),
                target = when (_state.value.scannerState.inputMode) {
                    InputMode.LOCAL -> _state.value.scannerState.inputPath
                    InputMode.URL -> _state.value.scannerState.inputUrl
                    InputMode.TEXT -> "Manual Text Input"
                },
                scanTimestamp = System.currentTimeMillis(),
                securityScore = 100,
                threatLevel = ThreatLevel.SAFE,
                vulnerabilities = emptyList(),
                maliciousPatterns = emptyList(),
                skillPermissions = emptyList(),
                gradleDependencies = emptyList(),
                memoryPoisoningDetected = false,
                scanPhase = ScanPhase.DONE
            )
        }
    }

    /**
     * Handle error dismiss.
     */
    private fun handleDismissError() {
        _state.update {
            it.copy(scannerState = it.scannerState.copy(errorMessage = null))
        }
    }

    /**
     * Handle report history load.
     */
    private fun handleLoadReports() {
        _state.update { it.copy(reportState = it.reportState.copy(isLoading = true)) }
        viewModelScope.launch {
            delay(400)
            val reports = listOf(
                ReportSummary(
                    id = "report-001",
                    target = "ClawHub/android-utility-skill-v2",
                    timestamp = System.currentTimeMillis(),
                    securityScore = 38,
                    vulnerabilityCount = 5,
                    threatLevel = ThreatLevel.DANGER
                ),
                ReportSummary(
                    id = "report-002",
                    target = "/Users/kenny/.openclaw/skills/productivity-kit",
                    timestamp = System.currentTimeMillis() - 86400000,
                    securityScore = 91,
                    vulnerabilityCount = 0,
                    threatLevel = ThreatLevel.SAFE
                )
            )
            _state.update {
                it.copy(
                    reportState = it.reportState.copy(
                        reports = reports,
                        isLoading = false
                    )
                )
            }
        }
    }

    /**
     * Handle report selection — load full report detail.
     *
     * @param reportId Selected report ID
     */
    private suspend fun handleSelectReport(reportId: String) {
        _state.update { it.copy(reportState = it.reportState.copy(isLoading = true)) }
        delay(300)

        val detail = SIMULATED_REPORT_DETAIL.copy(
            summary = SIMULATED_REPORT_DETAIL.summary.copy(id = reportId)
        )

        _state.update {
            it.copy(
                reportState = it.reportState.copy(
                    selectedReport = detail,
                    isLoading = false
                )
            )
        }
    }

    /**
     * Handle report export.
     *
     * @param reportId Report ID to export
     * @param format Export format
     */
    private suspend fun handleExportReport(reportId: String, format: ExportFormat) {
        try {
            val content = generateExportContent(reportId, format)
            _effect.emit(
                SkillsSecurityToolkitEffect.ExportSuccess("Exported to: /tmp/scan-report-${reportId}.${format.extension}")
            )
            _effect.emit(
                SkillsSecurityToolkitEffect.ShowSnackbar("✅ 报告已导出为 ${format.label} 格式")
            )
        } catch (e: Exception) {
            _effect.emit(
                SkillsSecurityToolkitEffect.ExportError("导出失败: ${e.message}")
            )
        }
    }

    /**
     * Generate export content in the specified format.
     *
     * @param reportId Report ID
     * @param format Export format
     * @return Exported content string
     */
    private fun generateExportContent(reportId: String, format: ExportFormat): String {
        val report = _state.value.reportState.selectedReport ?: SIMULATED_REPORT_DETAIL
        return when (format) {
            ExportFormat.SARIF -> generateSarifReport(report)
            ExportFormat.JSON -> generateJsonReport(report)
            ExportFormat.PDF -> "# PDF Export\nPDF export requires Android Print Framework integration"
        }
    }

    /**
     * Generate SARIF-formatted security report.
     * SARIF = Static Analysis Results Interchange Format
     * Compatible with GitHub Security tab.
     */
    private fun generateSarifReport(report: ReportDetail): String {
        val vulnerabilities = report.vulnerabilities
        val results = vulnerabilities.map { vuln ->
            """
            {
              "ruleId": "${vuln.id}",
              "ruleName": "${vuln.type}",
              "level": "${when (vuln.severity) {
                VulnerabilitySeverity.P0 -> "error"
                VulnerabilitySeverity.P1 -> "warning"
                else -> "note"
              }}",
              "message": "${vuln.description.replace("\"", "\\\"")}",
              "locations": [{
                "physicalLocation": {
                  "artifactLocation": { "uri": "${vuln.location}" },
                  "region": { "snippet": { "text": "${vuln.codeSnippet.replace("\"", "\\\"").take(200)}" } }
                }
              }]
            }
            """.trimIndent()
        }.joinToString(",\n")

        return """
        {
          "version": "2.1.0",
          "${'$'}schema": "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
          "runs": [{
            "tool": {
              "driver": {
                "name": "AndroidSkillsSecurityScanner",
                "version": "1.0.0",
                "informationUri": "https://github.com/android/skills-security"
              }
            },
            "results": [${results}]
          }]
        }
        """.trimIndent()
    }

    /**
     * Generate JSON-formatted security report.
     */
    private fun generateJsonReport(report: ReportDetail): String {
        val vulnList = report.vulnerabilities.map { vuln ->
            """
            {
              "id": "${vuln.id}",
              "type": "${vuln.type}",
              "severity": "${vuln.severity.label}",
              "description": "${vuln.description}",
              "location": "${vuln.location}",
              "fixSuggestion": "${vuln.fixSuggestion}"
            }
            """.trimIndent()
        }.joinToString(",\n")

        return """
        {
          "reportId": "${report.summary.id}",
          "target": "${report.summary.target}",
          "timestamp": ${report.summary.timestamp},
          "securityScore": ${report.summary.securityScore},
          "threatLevel": "${report.summary.threatLevel.label}",
          "vulnerabilities": [${vulnList}],
          "maliciousPatterns": [${report.maliciousPatterns.map { "\"$it\"" }.joinToString(", ")}],
          "memoryPoisoningDetected": ${report.memoryPoisoning.isNotEmpty()}
        }
        """.trimIndent()
    }

    /**
     * Handle reference tab switch.
     *
     * @param tab New reference sub-tab
     */
    private fun handleSwitchReferenceTab(tab: ReferenceTab) {
        _state.update {
            it.copy(referenceState = it.referenceState.copy(activeTab = tab, searchQuery = ""))
        }
        loadReferenceItems()
    }

    /**
     * Handle reference library search.
     *
     * @param query Search query
     */
    private fun handleSearchReference(query: String) {
        _state.update {
            it.copy(referenceState = it.referenceState.copy(searchQuery = query))
        }
        loadReferenceItems()
    }

    /**
     * Load reference library items based on current tab and search query.
     * Uses simulated data in preview/testing mode.
     */
    private fun loadReferenceItems() {
        val currentTab = _state.value.referenceState.activeTab
        val query = _state.value.referenceState.searchQuery

        val items = when (currentTab) {
            ReferenceTab.MALICIOUS_PATTERNS -> SIMULATED_MALICIOUS_PATTERNS
            ReferenceTab.VULNERABILITY_TYPES -> SIMULATED_VULNERABILITY_TYPES
            ReferenceTab.FIX_SUGGESTIONS -> SIMULATED_FIX_SUGGESTIONS
        }

        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        _state.update {
            it.copy(referenceState = it.referenceState.copy(items = filtered))
        }
    }
}
