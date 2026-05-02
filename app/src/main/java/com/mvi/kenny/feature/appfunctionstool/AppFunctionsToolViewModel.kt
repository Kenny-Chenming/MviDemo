package com.mvi.kenny.feature.appfunctionstool

// ================================================================
// AppFunctionsToolViewModel — Android AppFunctions 开发工具包 ViewModel
// ================================================================
// MVI ViewModel for Android AppFunctions Development Toolkit.
//
// PRD-214: Android AppFunctions 开发工具包
// Handles user intents, updates state, and emits one-time effects.
//
// Key responsibilities:
//   1. Process all user intents (scan, generate JSON, check compliance, etc.)
//   2. Update immutable State via copy()
//   3. Send one-time Effects (snackbar, clipboard) via Channel
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * ============================================================
 * AppFunctionsToolViewModel — MVI ViewModel
 * ============================================================
 *
 * @param initialState Initial page state
 * @see AppFunctionsToolState
 * @see AppFunctionsToolIntent
 * @see AppFunctionsToolEffect
 */
class AppFunctionsToolViewModel(
    private val initialState: AppFunctionsToolState = AppFunctionsToolState.Initial
) : ViewModel() {

    // ── State ──────────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<AppFunctionsToolState> = _state.asStateFlow()

    // ── Effects ─────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<AppFunctionsToolEffect>()
    val effect: MutableSharedFlow<AppFunctionsToolEffect> = _effect

    // ── Date formatter for debug logs ──────────────────────────────
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // ================================================================
    // sendIntent — Intent 分发入口
    // ================================================================
    /**
     * Process user intent and update state accordingly.
     * This is the single entry point for all user actions.
     *
     * @param intent The user intent to process
     * @see AppFunctionsToolIntent
     */
    fun sendIntent(intent: AppFunctionsToolIntent) {
        when (intent) {
            is AppFunctionsToolIntent.SelectTab -> handleSelectTab(intent.index)
            is AppFunctionsToolIntent.UpdateSourcePath -> handleUpdateSourcePath(intent.path)
            is AppFunctionsToolIntent.StartScan -> handleStartScan(intent.simulate)
            is AppFunctionsToolIntent.UpdateKotlinCode -> handleUpdateKotlinCode(intent.code)
            is AppFunctionsToolIntent.GenerateJson -> handleGenerateJson()
            is AppFunctionsToolIntent.UpdateGradleConfig -> handleUpdateGradleConfig(intent.config)
            is AppFunctionsToolIntent.CheckCompliance -> handleCheckCompliance(intent.simulate)
            is AppFunctionsToolIntent.SelectErrorType -> handleSelectErrorType(intent.type)
            is AppFunctionsToolIntent.GenerateTemplate -> handleGenerateTemplate()
            is AppFunctionsToolIntent.UpdateSimulatedParams -> handleUpdateSimulatedParams(intent.params)
            is AppFunctionsToolIntent.SimulateCall -> handleSimulateCall()
            is AppFunctionsToolIntent.CopyToClipboard -> handleCopyToClipboard(intent.text, intent.label)
            is AppFunctionsToolIntent.DismissSnackbar -> handleDismissSnackbar()
            is AppFunctionsToolIntent.ResetAll -> handleResetAll()
        }
    }

    // ================================================================
    // Intent Handlers — Tab 1: 策略扫描器
    // ================================================================

    private fun handleSelectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    private fun handleUpdateSourcePath(path: String) {
        _state.value = _state.value.copy(sourcePath = path)
    }

    /**
     * Start scanning source code for AppFunction exposure suggestions.
     * If simulate=true, uses pre-defined mock data.
     * Otherwise, simulates a 3-second analysis process.
     */
    private fun handleStartScan(simulate: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isScanning = true, scanProgress = 0f)

            if (simulate) {
                // Simulate scan progress
                for (i in 1..3) {
                    delay(1000)
                    _state.value = _state.value.copy(scanProgress = i / 3f)
                }
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = SIMULATED_EXPOSURE_SUGGESTIONS
                )
                _effect.emit(AppFunctionsToolEffect.ShowSnackbar("扫描完成，找到 ${SIMULATED_EXPOSURE_SUGGESTIONS.size} 个可暴露函数"))
            } else {
                // Real scan — in a production app, this would invoke a lint/analyze task
                // For now, use simulation with the provided path
                if (_state.value.sourcePath.isBlank()) {
                    _effect.emit(AppFunctionsToolEffect.ShowError("请输入源码路径"))
                    _state.value = _state.value.copy(isScanning = false)
                    return@launch
                }
                for (i in 1..3) {
                    delay(1000)
                    _state.value = _state.value.copy(scanProgress = i / 3f)
                }
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    scanResults = SIMULATED_EXPOSURE_SUGGESTIONS
                )
                _effect.emit(AppFunctionsToolEffect.ShowSnackbar("扫描完成，找到 ${SIMULATED_EXPOSURE_SUGGESTIONS.size} 个可暴露函数"))
            }
        }
    }

    // ================================================================
    // Intent Handlers — Tab 2: 能力描述器
    // ================================================================

    private fun handleUpdateKotlinCode(code: String) {
        _state.value = _state.value.copy(kotlinCode = code)
    }

    /**
     * Generate AppFunction self-describing JSON from Kotlin function signature.
     * Uses simple regex parsing — not a full Kotlin compiler.
     */
    private fun handleGenerateJson() {
        viewModelScope.launch {
            val code = _state.value.kotlinCode
            if (code.isBlank()) {
                _effect.emit(AppFunctionsToolEffect.ShowError("请输入 Kotlin 函数代码"))
                return@launch
            }

            _state.value = _state.value.copy(isGeneratingJson = true)
            delay(800) // Simulate analysis

            val json = generateAppFunctionJson(code)
            _state.value = _state.value.copy(
                isGeneratingJson = false,
                generatedJson = json
            )
            _effect.emit(AppFunctionsToolEffect.ShowSnackbar("JSON 生成成功"))
        }
    }

    /**
     * Parse Kotlin function signature and generate AppFunction JSON description.
     * Uses regex — not a full Kotlin compiler. Handles basic cases only.
     *
     * @param code Kotlin function code
     * @return AppFunction JSON description
     */
    private fun generateAppFunctionJson(code: String): String {
        // Basic regex patterns for Kotlin function parsing
        val functionNamePattern = Regex("""fun\s+(\w+)\s*\(""")
        val returnTypePattern = Regex("""\)\s*:\s*(\w+)""")

        val functionName = functionNamePattern.find(code)?.groupValues?.get(1) ?: "unknown"
        val returnType = returnTypePattern.find(code)?.groupValues?.get(1) ?: "Unit"

        // Extract parameters using a simple approach
        val paramsBlock = code.substringAfter("(", "").substringBefore(")", "")
        val params = paramsBlock.split(",").mapNotNull { param ->
            val parts = param.trim().split(Regex("""\s+"""))
            if (parts.size >= 2) {
                val name = parts.last()
                val type = parts.dropLast(1).joinToString(" ").replace("?", "")
                mapOf("name" to name, "type" to type)
            } else null
        }

        // Build JSON manually (no external JSON library)
        val paramsJson = params.joinToString(",\n      ") { param ->
            """{"name": "${param["name"]}", "type": "${param["type"]}", "description": ""}"""
        }

        return """
{
  "appFunction": {
    "name": "${functionName.lowercase()}",
    "description": "Auto-generated description for $functionName",
    "parameters": [
      $paramsJson
    ],
    "returnType": "$returnType",
    "actions": ["general"],
    "version": "1.0"
  }
}""".trimIndent()
    }

    // ================================================================
    // Intent Handlers — Tab 3: 权限合规检测
    // ================================================================

    private fun handleUpdateGradleConfig(config: String) {
        _state.value = _state.value.copy(gradleConfig = config)
    }

    /**
     * Check AppFunctions compliance in build.gradle.kts configuration.
     * Detects missing permissions, incorrect configurations, etc.
     */
    private fun handleCheckCompliance(simulate: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCheckingCompliance = true)

            delay(1500) // Simulate analysis

            val results = if (simulate || _state.value.gradleConfig.isBlank()) {
                SIMULATED_COMPLIANCE_RESULTS
            } else {
                analyzeGradleConfig(_state.value.gradleConfig)
            }

            _state.value = _state.value.copy(
                isCheckingCompliance = false,
                complianceResults = results
            )

            val passCount = results.count { it.status == ComplianceStatus.PASS }
            val warnCount = results.count { it.status == ComplianceStatus.WARNING }
            val failCount = results.count { it.status == ComplianceStatus.VIOLATION }
            _effect.emit(AppFunctionsToolEffect.ShowSnackbar("检测完成：通过$passCount / 警告$warnCount / 违规$failCount"))
        }
    }

    /**
     * Analyze Gradle configuration for AppFunctions compliance issues.
     *
     * @param config Gradle build script content
     * @return List of compliance results
     */
    private fun analyzeGradleConfig(config: String): List<ComplianceResult> {
        val results = mutableListOf<ComplianceResult>()

        // Check for EXECUTE_APP_FUNCTIONS permission
        if (!config.contains("EXECUTE_APP_FUNCTIONS")) {
            results.add(ComplianceResult(
                rule = "EXECUTE_APP_FUNCTIONS 权限声明",
                status = ComplianceStatus.VIOLATION,
                message = "AndroidManifest.xml 中缺少 EXECUTE_APP_FUNCTIONS 权限声明",
                fixSuggestion = """<uses-permission android:name="android.permission.EXECUTE_APP_FUNCTIONS" />"""
            ))
        } else {
            results.add(ComplianceResult(
                rule = "EXECUTE_APP_FUNCTIONS 权限声明",
                status = ComplianceStatus.PASS,
                message = "权限声明已找到"
            ))
        }

        // Check for appfunctions dependency
        if (!config.contains("appfunctions")) {
            results.add(ComplianceResult(
                rule = "androidx.appfunctions 依赖",
                status = ComplianceStatus.WARNING,
                message = "未找到 androidx.appfunctions 依赖声明",
                fixSuggestion = """// build.gradle.kts
dependencies {
    implementation("androidx.appfunctions:appfunctions:1.0.0-alpha01")
}"""
            ))
        } else {
            results.add(ComplianceResult(
                rule = "androidx.appfunctions 依赖",
                status = ComplianceStatus.PASS,
                message = "appfunctions 依赖已配置"
            ))
        }

        return results
    }

    // ================================================================
    // Intent Handlers — Tab 4: 错误处理模板
    // ================================================================

    private fun handleSelectErrorType(type: ErrorType) {
        _state.value = _state.value.copy(selectedErrorType = type)
        // Auto-generate template when error type changes
        handleGenerateTemplate()
    }

    private fun handleGenerateTemplate() {
        val type = _state.value.selectedErrorType
        val template = ERROR_TEMPLATE_MAP[type] ?: ""
        _state.value = _state.value.copy(generatedTemplate = template)
    }

    // ================================================================
    // Intent Handlers — Tab 5: 决策树 + 调试面板
    // ================================================================

    private fun handleUpdateSimulatedParams(params: String) {
        _state.value = _state.value.copy(simulatedCallParams = params)
    }

    /**
     * Simulate an Agent calling an AppFunction.
     * Generates a realistic debug log entry with random latency.
     */
    private fun handleSimulateCall() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSimulatingCall = true)

            val params = _state.value.simulatedCallParams.ifBlank {
                """{"function": "send_message", "recipientId": "user_123", "content": "Hello from Agent!"}"""
            }

            delay(Random.nextLong(500, 2000)) // Simulate network + execution latency

            val timestamp = timeFormatter.format(Date())
            val success = Random.nextFloat() > 0.2f // 80% success rate

            val logEntry = if (success) {
                DebugLogEntry(
                    timestamp = timestamp,
                    caller = "com.google.android.apps.search.agent (Gemini)",
                    params = params,
                    result = """{"status": "SUCCESS", "messageId": "msg_${System.currentTimeMillis()}"}""",
                    durationMs = Random.nextLong(200, 1500),
                    status = "SUCCESS"
                )
            } else {
                val errorTypes = listOf("TIMEOUT", "PERMISSION_DENIED", "INVALID_PARAMS", "SERVICE_UNAVAILABLE")
                DebugLogEntry(
                    timestamp = timestamp,
                    caller = "com.google.android.apps.search.agent (Gemini)",
                    params = params,
                    result = """{"status": "FAILURE", "error": "${errorTypes.random()}"}""",
                    durationMs = Random.nextLong(100, 500),
                    status = "FAILURE"
                )
            }

            val newLogs = listOf(logEntry) + _state.value.debugLogs.take(19) // Keep last 20
            _state.value = _state.value.copy(
                isSimulatingCall = false,
                debugLogs = newLogs
            )

            _effect.emit(AppFunctionsToolEffect.ShowSnackbar(
                if (success) "✅ 模拟调用成功" else "❌ 模拟调用失败"
            ))
        }
    }

    // ================================================================
    // Intent Handlers — 全局
    // ================================================================

    private fun handleCopyToClipboard(text: String, label: String) {
        viewModelScope.launch {
            _effect.emit(AppFunctionsToolEffect.CopyToClipboard(text, label))
            _effect.emit(AppFunctionsToolEffect.ShowSnackbar("已复制 $label 到剪贴板"))
        }
    }

    private fun handleDismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    private fun handleResetAll() {
        _state.value = AppFunctionsToolState.Initial
        viewModelScope.launch {
            _effect.emit(AppFunctionsToolEffect.ShowSnackbar("已重置所有状态"))
        }
    }
}
