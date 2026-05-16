package com.mvi.kenny.feature.telecom

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =============================================================
// TelecomViewModel — Jetpack Telecom v1.1.0 VoIP Native Visibility
// 集成工具包 ViewModel
// =============================================================
// PRD-254 | Jetpack Telecom v1.1.0 VoIP Native Visibility Integration Toolkit
//
// ViewModel manages state and processes user intents.
// All state mutations happen here. UI layer is purely declarative.
//
// Key responsibilities:
// - Process user intents and update state accordingly
// - Emit one-time side effects via effect channel
// - Simulate telecom API behaviors (full demo implementation)
//
// @see TelecomContract For all state/intent/effect definitions
// @see TelecomScreen For the UI layer

class TelecomViewModel : ViewModel() {

    // ============================================================
    // State — single source of truth for UI
    // ============================================================
    private val _state = MutableStateFlow(TelecomState.Initial)
    val state: StateFlow<TelecomState> = _state.asStateFlow()

    // ============================================================
    // Effect — one-time side effects channel
    // ============================================================
    private val _effect = Channel<TelecomEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Initialize with sample call log entries and check telecom support
        loadInitialData()
    }

    // ============================================================
    // Intent processing
    // ============================================================

    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer via:
     * ```kotlin
     * viewModel.sendIntent(TelecomIntent.SelectTab(...))
     * ```
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: TelecomIntent) {
        when (intent) {
            is TelecomIntent.SelectTab -> handleSelectTab(intent.tab)
            is TelecomIntent.ToggleTheme -> handleToggleTheme()
            is TelecomIntent.CopyCodeBlock -> handleCopyCodeBlock(intent.codeBlockId, intent.code)
            is TelecomIntent.SelectCallLogEntry -> handleSelectCallLogEntry(intent.entry)
            is TelecomIntent.ClearCallLogEntry -> handleClearCallLogEntry()
            is TelecomIntent.ToggleCallLogExclusion -> handleToggleCallLogExclusion(intent.entryId)
            is TelecomIntent.ToggleCallLogIntegrationGuide -> handleToggleCallLogIntegrationGuide(intent.sectionId)
            is TelecomIntent.ToggleCallbackGuide -> handleToggleCallbackGuide(intent.sectionId)
            is TelecomIntent.ToggleExclusionGuide -> handleToggleExclusionGuide(intent.sectionId)
            is TelecomIntent.UpdateAllowlistApplication -> handleUpdateAllowlistApplication(intent.application)
            is TelecomIntent.SubmitAllowlistApplication -> handleSubmitAllowlistApplication()
            is TelecomIntent.RunCIValidation -> handleRunCIValidation()
            is TelecomIntent.CheckTelecomSupport -> handleCheckTelecomSupport()
            is TelecomIntent.DismissError -> handleDismissError()
            is TelecomIntent.ClearCIReport -> handleClearCIReport()
        }
    }

    // ============================================================
    // Tab Navigation
    // ============================================================

    private fun handleSelectTab(tab: TelecomTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // ============================================================
    // Theme
    // ============================================================

    private fun handleToggleTheme() {
        _state.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    // ============================================================
    // Code Copy
    // ============================================================

    private fun handleCopyCodeBlock(codeBlockId: String, code: String) {
        _state.update { it.copy(copiedCodeBlock = codeBlockId) }
        viewModelScope.launch {
            _effect.send(TelecomEffect.CodeCopied(code))
            // Reset copied state after animation duration
            delay(2000)
            _state.update { current ->
                if (current.copiedCodeBlock == codeBlockId) {
                    current.copy(copiedCodeBlock = null)
                } else {
                    current
                }
            }
        }
    }

    // ============================================================
    // Call Log Entry
    // ============================================================

    private fun handleSelectCallLogEntry(entry: CallLogEntry) {
        _state.update { it.copy(selectedCallLogEntry = entry) }
    }

    private fun handleClearCallLogEntry() {
        _state.update { it.copy(selectedCallLogEntry = null) }
    }

    private fun handleToggleCallLogExclusion(entryId: String) {
        _state.update { current ->
            current.copy(
                callLogEntries = current.callLogEntries.map { entry ->
                    if (entry.id == entryId) {
                        entry.copy(isExcluded = !entry.isExcluded)
                    } else {
                        entry
                    }
                }
            )
        }
    }

    // ============================================================
    // Guide Expansion
    // ============================================================

    private fun handleToggleCallLogIntegrationGuide(sectionId: String) {
        _state.update { current ->
            val expanded = current.callLogIntegrationGuideExpanded
            current.copy(
                callLogIntegrationGuideExpanded = if (expanded.contains(sectionId)) {
                    expanded - sectionId
                } else {
                    expanded + sectionId
                }
            )
        }
    }

    private fun handleToggleCallbackGuide(sectionId: String) {
        _state.update { current ->
            val expanded = current.callbackGuideExpanded
            current.copy(
                callbackGuideExpanded = if (expanded.contains(sectionId)) {
                    expanded - sectionId
                } else {
                    expanded + sectionId
                }
            )
        }
    }

    private fun handleToggleExclusionGuide(sectionId: String) {
        _state.update { current ->
            val expanded = current.exclusionGuideExpanded
            current.copy(
                exclusionGuideExpanded = if (expanded.contains(sectionId)) {
                    expanded - sectionId
                } else {
                    expanded + sectionId
                }
            )
        }
    }

    // ============================================================
    // Allowlist Application
    // ============================================================

    private fun handleUpdateAllowlistApplication(application: AllowlistApplication) {
        _state.update { it.copy(allowlistApplication = application) }
    }

    private fun handleSubmitAllowlistApplication() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Simulate submission delay (3 seconds)
            delay(3000)

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    allowlistApplication = current.allowlistApplication.copy(
                        status = AllowlistStatus.IN_REVIEW,
                        submittedAt = System.currentTimeMillis()
                    )
                )
            }

            _effect.send(
                TelecomEffect.AllowlistSubmitted(
                    _state.value.allowlistApplication.appName
                )
            )
        }
    }

    // ============================================================
    // CI Validation
    // ============================================================

    private fun handleRunCIValidation() {
        viewModelScope.launch {
            _state.update { it.copy(isRunningCIValidation = true) }

            // Simulate CI validation process (5 seconds)
            delay(5000)

            // Generate simulated CI validation results
            val results = listOf(
                CIValidationResult(
                    checkName = "ConnectionService Declaration",
                    passed = true,
                    message = "ConnectionService properly declared in AndroidManifest.xml with BIND_TELECOM_CONNECTION_SERVICE permission",
                    filePath = "app/src/main/AndroidManifest.xml",
                    lineNumber = 42
                ),
                CIValidationResult(
                    checkName = "CallLogIntegration",
                    passed = true,
                    message = "CallLogIntegration implemented via ConnectionService",
                    filePath = "app/src/main/java/com/example/voip/VoipConnectionService.kt",
                    lineNumber = 1
                ),
                CIValidationResult(
                    checkName = "android:permission=\"android.permission.BIND_TELECOM_CONNECTION_SERVICE\"",
                    passed = true,
                    message = "Required permission found in service declaration",
                    filePath = "app/src/main/AndroidManifest.xml",
                    lineNumber = 43
                ),
                CIValidationResult(
                    checkName = "Callback Capability",
                    passed = false,
                    message = "WARNING: Callback from Dialer requires Secure Package Allowlist approval. Not yet approved.",
                    filePath = null,
                    lineNumber = null
                ),
                CIValidationResult(
                    checkName = "SDK Version Check",
                    passed = true,
                    message = "TelecomManager.isSupported() check present in code",
                    filePath = "app/src/main/java/com/example/voip/VoipManager.kt",
                    lineNumber = 15
                ),
                CIValidationResult(
                    checkName = "android.handoff category",
                    passed = false,
                    message = "Activity does not declare android.handoff category in AndroidManifest.xml",
                    filePath = "app/src/main/AndroidManifest.xml",
                    lineNumber = 10
                )
            )

            val passedCount = results.count { it.passed }
            val failedCount = results.count { !it.passed }

            val report = CIValidationReport(
                totalChecks = results.size,
                passedChecks = passedCount,
                failedChecks = failedCount,
                warningChecks = results.count { !it.passed && it.message.contains("WARNING") },
                results = results,
                overallPassed = failedCount == 0,
                reportContent = buildString {
                    appendLine("# Telecom CI Validation Report")
                    appendLine()
                    appendLine("| Check | Status | Message |")
                    appendLine("|-------|--------|--------|")
                    results.forEach { result ->
                        val status = if (result.passed) "✅ PASS" else "❌ FAIL"
                        appendLine("| ${result.checkName} | $status | ${result.message} |")
                    }
                    appendLine()
                    appendLine("**Summary**: $passedCount passed, $failedCount failed out of ${results.size} checks")
                    appendLine()
                    appendLine("**Overall**: ${if (failedCount == 0) "✅ ALL CHECKS PASSED" else "❌ VALIDATION FAILED"}")
                }
            )

            _state.update { it.copy(isRunningCIValidation = false, ciValidationReport = report) }
            _effect.send(TelecomEffect.CIValidationComplete(report.overallPassed))
        }
    }

    // ============================================================
    // Telecom Support Check
    // ============================================================

    private fun handleCheckTelecomSupport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Simulate telecom support check
            delay(1000)

            val isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // API 31+
            val supportsCallback = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE // API 34+

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    connectionServiceConfig = current.connectionServiceConfig.copy(
                        isSupported = isSupported,
                        supportsCallback = supportsCallback,
                        serviceClassName = "com.example.voip.voipService",
                        label = "Example VoIP",
                        iconUri = "android.resource://com.example.voip/drawable/ic_voip"
                    )
                )
            }

            if (!isSupported) {
                _effect.send(
                    TelecomEffect.ShowSnackbar(
                        "TelecomManager not supported on this device (Android ${Build.VERSION.SDK_INT})",
                        isError = true
                    )
                )
            }
        }
    }

    // ============================================================
    // Error Handling
    // ============================================================

    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun handleClearCIReport() {
        _state.update { it.copy(ciValidationReport = null) }
    }

    // ============================================================
    // Initial Data Loading
    // ============================================================

    private fun loadInitialData() {
        // Load sample call log entries / 加载示例通话记录
        val sampleCallLogs = listOf(
            CallLogEntry(
                id = "call-001",
                contactName = "张三 Zhang",
                phoneNumber = "+86 138-0000-0001",
                timestamp = System.currentTimeMillis() - 3600_000, // 1 hour ago
                duration = 300, // 5 minutes
                callType = CallType.OUTGOING,
                isExcluded = false,
                accountHandle = "voip-account-001"
            ),
            CallLogEntry(
                id = "call-002",
                contactName = "李四 Li",
                phoneNumber = "+86 139-0000-0002",
                timestamp = System.currentTimeMillis() - 7200_000, // 2 hours ago
                duration = 0, // missed
                callType = CallType.MISSED,
                isExcluded = false,
                accountHandle = "voip-account-001"
            ),
            CallLogEntry(
                id = "call-003",
                contactName = "王五 Wang",
                phoneNumber = "+86 136-0000-0003",
                timestamp = System.currentTimeMillis() - 86400_000, // 1 day ago
                duration = 600, // 10 minutes
                callType = CallType.INCOMING,
                isExcluded = false,
                accountHandle = "voip-account-001"
            ),
            CallLogEntry(
                id = "call-004",
                contactName = "赵六 Zhao",
                phoneNumber = "+86 137-0000-0004",
                timestamp = System.currentTimeMillis() - 172800_000, // 2 days ago
                duration = 120, // 2 minutes
                callType = CallType.OUTGOING,
                isExcluded = true, // Excluded — private call
                accountHandle = "voip-account-001"
            ),
            CallLogEntry(
                id = "call-005",
                contactName = "孙七 Sun",
                phoneNumber = "+86 135-0000-0005",
                timestamp = System.currentTimeMillis() - 259200_000, // 3 days ago
                duration = 1800, // 30 minutes
                callType = CallType.INCOMING,
                isExcluded = false,
                accountHandle = "voip-account-001"
            )
        )

        _state.update { it.copy(callLogEntries = sampleCallLogs) }

        // Check telecom support on load / 加载时检查 Telecom 支持
        handleCheckTelecomSupport()
    }
}
