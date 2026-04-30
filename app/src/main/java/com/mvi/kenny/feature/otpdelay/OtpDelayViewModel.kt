package com.mvi.kenny.feature.otpdelay

// ================================================================
// OtpDelayViewModel — Android 17 SMS OTP Delay 合规检测与迁移工具包 ViewModel
// ================================================================
// MVI ViewModel for OTP Delay compliance toolkit.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// Responsibilities:
//   - OTP Impact Scanner: Scan source code for READ_SMS/SmsManager OTP usage
//   - Compliance Detector: Check if App uses SMS Retriever/User Consent API
//   - Hash Generator: Generate SMS Retriever hash strings
//   - Fallback Strategy: Provide migration guidance
//
// @see OtpDelayContract for State/Intent/Effect definitions
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ============================================================
 * OtpDelayViewModel — MVI ViewModel
 * ================================================================
 * Main ViewModel for the OTP Delay compliance toolkit.
 * Handles OTP impact scanning, compliance checking, and hash generation.
 */
class OtpDelayViewModel : ViewModel() {

    // ================================================================
    // MVI State
    // ================================================================

    private val _scanState = MutableStateFlow(OtpScanState())
    val scanState: StateFlow<OtpScanState> = _scanState.asStateFlow()

    private val _complianceState = MutableStateFlow(OtpComplianceState())
    val complianceState: StateFlow<OtpComplianceState> = _complianceState.asStateFlow()

    private val _hashGeneratorState = MutableStateFlow(HashGeneratorState())
    val hashGeneratorState: StateFlow<HashGeneratorState> = _hashGeneratorState.asStateFlow()

    // ================================================================
    // MVI Effects (one-time side effects via Channel)
    // ================================================================

    private val _scanEffects = Channel<OtpScanEffect>(Channel.BUFFERED)
    val scanEffects = _scanEffects.receiveAsFlow()

    // ================================================================
    // OTP Pattern Definitions
    // ================================================================
    // Patterns for detecting OTP-related API usage in source code.
    // 这些模式用于识别需要迁移到 SMS Retriever API 的代码。

    private data class OtpPattern(
        val pattern: Regex,
        val severity: OtpSeverity,
        val api: String,
        val description: String,
        val suggestion: String
    )

    private val otpPatterns = listOf(
        // HIGH severity: Direct OTP reading
        OtpPattern(
            pattern = Regex("""READ_SMS"""),
            severity = OtpSeverity.HIGH,
            api = "READ_SMS",
            description = "READ_SMS 权限声明 — Android 17 会对非默认 SMS App 的 OTP SMS 读取施加 3 小时延迟",
            suggestion = "迁移到 SMS Retriever API（推荐）或 SMS User Consent API"
        ),
        OtpPattern(
            pattern = Regex("""SmsManager\.getDefault\(\)\.sendTextMessage"""),
            severity = OtpSeverity.HIGH,
            api = "SmsManager.sendTextMessage",
            description = "SmsManager.sendTextMessage 调用 — 涉及 SMS 发送，需确认 OTP 处理逻辑",
            suggestion = "检查是否同时有 OTP 读取逻辑，确保已迁移到 Retriever API"
        ),
        OtpPattern(
            pattern = Regex("""SmsManager\.getSmsManagerForSubscriptionId"""),
            severity = OtpSeverity.HIGH,
            api = "SmsManager.getSmsManagerForSubscriptionId",
            description = "多卡设备的 SmsManager 调用 — 需确保所有卡槽的 OTP 处理都迁移到 Retriever API",
            suggestion = "为每个卡槽分别配置 SMS Retriever"
        ),
        OtpPattern(
            pattern = Regex("""Telephony.Sms.Intents\.READ_SMS_ACTION"""),
            severity = OtpSeverity.HIGH,
            api = "READ_SMS_ACTION BroadcastReceiver",
            description = "SMS_READ BroadcastReceiver 注册 — 监听 SMS 读取意图的组件会受 3 小时延迟影响",
            suggestion = "迁移到 SmsRetrieverClient 或 SmsConsentReceiver"
        ),
        OtpPattern(
            pattern = Regex("""android\.provider\.Telephony\.SMS_RECEIVED"""),
            severity = OtpSeverity.HIGH,
            api = "SMS_RECEIVED BroadcastReceiver",
            description = "系统 SMS_RECEIVED广播监听 — Android 17 会对 OTP SMS 读取施加 3 小时延迟",
            suggestion = "使用 SmsRetrieverClient.startSmsRetriever() 替代直接监听广播"
        ),
        OtpPattern(
            pattern = Regex("""ContentResolver\.query.*SMS"""),
            severity = OtpSeverity.MEDIUM,
            api = "ContentResolver.query(SMS)",
            description = "通过 ContentResolver 查询 SMS — 可能涉及 OTP 读取",
            suggestion = "检查查询目的，必要时迁移到 SMS Retriever API"
        ),
        OtpPattern(
            pattern = Regex("""Uri\.parse.*sms"""),
            severity = OtpSeverity.MEDIUM,
            api = "SMS Uri parse",
            description = "SMS URI 解析操作 — 需确认是否用于 OTP 读取场景",
            suggestion = "确认是否涉及 OTP，若是则迁移到 Retriever API"
        ),
        OtpPattern(
            pattern = Regex("""SmsRetriever\.CONFIGURATION_ERROR"""),
            severity = OtpSeverity.LOW,
            api = "SmsRetriever.CONFIGURATION_ERROR",
            description = "SmsRetriever 配置错误常量 — 代码中已有 Retriever 相关逻辑",
            suggestion = "检查配置是否正确，确保 hash string 有效"
        )
    )

    // ================================================================
    // MVI Intent Processing
    // ================================================================

    /**
     * Process OTP scan intent
     * 处理扫描意图，启动源码扫描
     */
    fun processIntent(intent: OtpScanIntent) {
        when (intent) {
            is OtpScanIntent.StartScan -> startScan(intent.sourceDir, intent.packageName)
            is OtpScanIntent.CancelScan -> viewModelScope.launch { cancelScan() }
            is OtpScanIntent.ClearResults -> viewModelScope.launch { clearResults() }
            is OtpScanIntent.ExportReport -> exportReport(intent.format, intent.outputPath)
        }
    }

    /**
     * Process hash generator intent
     * 处理 Hash 生成意图
     */
    fun processHashIntent(intent: HashGeneratorIntent) {
        when (intent) {
            is HashGeneratorIntent.UpdatePackageName -> updatePackageName(intent.packageName)
            is HashGeneratorIntent.UpdateKeystorePath -> updateKeystorePath(intent.keystorePath)
            is HashGeneratorIntent.UpdateKeyAlias -> updateKeyAlias(intent.keyAlias)
            is HashGeneratorIntent.GenerateHash -> generateHash()
            is HashGeneratorIntent.Clear -> clearHashGenerator()
        }
    }

    // ================================================================
    // Scan Implementation — 扫描实现
    // ================================================================

    /**
     * Start OTP impact scan
     * 启动 OTP 影响扫描，遍历源码目录检测相关 API 使用
     */
    private fun startScan(sourceDir: String, packageName: String) {
        viewModelScope.launch {
            _scanState.value = OtpScanState(
                phase = ScanPhase.SCANNING_KOTLIN,
                progress = 0,
                findings = emptyList(),
                startTime = System.currentTimeMillis()
            )

            emitEffect(OtpScanEffect.TerminalOutput("🔍 开始 OTP 影响扫描...", OtpSeverity.INFO))
            emitEffect(OtpScanEffect.TerminalOutput("📁 源码目录: $sourceDir", OtpSeverity.INFO))
            emitEffect(OtpScanEffect.TerminalOutput("📦 包名: $packageName\n", OtpSeverity.INFO))

            try {
                val findings = withContext(Dispatchers.IO) {
                    scanSourceDirectory(sourceDir)
                }

                val endTime = System.currentTimeMillis()
                val scanState = _scanState.value.copy(
                    phase = ScanPhase.COMPLETED,
                    progress = 100,
                    findings = findings,
                    scannedFilesCount = findings.size,
                    endTime = endTime
                )
                _scanState.value = scanState

                // Emit completion effect
                emitEffect(
                    OtpScanEffect.ScanCompleted(
                        totalFindings = findings.size,
                        highSeverity = findings.count { it.severity == OtpSeverity.HIGH },
                        mediumSeverity = findings.count { it.severity == OtpSeverity.MEDIUM },
                        lowSeverity = findings.count { it.severity == OtpSeverity.LOW },
                        durationMs = scanState.durationMs
                    )
                )

                // Emit terminal summary
                emitEffect(OtpScanEffect.TerminalOutput("\n📊 扫描结果摘要:", OtpSeverity.INFO))
                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "   🔴 HIGH:    ${findings.count { it.severity == OtpSeverity.HIGH }}",
                        OtpSeverity.HIGH
                    )
                )
                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "   🟡 MEDIUM: ${findings.count { it.severity == OtpSeverity.MEDIUM }}",
                        OtpSeverity.MEDIUM
                    )
                )
                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "   🟢 LOW:    ${findings.count { it.severity == OtpSeverity.LOW }}",
                        OtpSeverity.LOW
                    )
                )
                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "   ⏱️  耗时: ${scanState.durationMs}ms",
                        OtpSeverity.INFO
                    )
                )

            } catch (e: Exception) {
                _scanState.value = _scanState.value.copy(
                    phase = ScanPhase.ERROR,
                    errorMessage = e.message ?: "Unknown error"
                )
                emitEffect(
                    OtpScanEffect.Error(
                        message = "扫描失败: ${e.message}",
                        throwable = e
                    )
                )
            }
        }
    }

    /**
     * Scan source directory for OTP-related patterns
     * 扫描源码目录，检测 OTP 相关 API 使用模式
     */
    private suspend fun scanSourceDirectory(sourceDir: String): List<OtpFinding> {
        val findings = mutableListOf<OtpFinding>()
        val sourcePath = File(sourceDir)

        if (!sourcePath.exists()) {
            emitEffect(
                OtpScanEffect.TerminalOutput(
                    "⚠️  目录不存在: $sourceDir",
                    OtpSeverity.MEDIUM
                )
            )
            return emptyList()
        }

        // Find all Kotlin and Java source files
        // 查找所有 Kotlin 和 Java 源文件
        val kotlinFiles = sourcePath.walkTopDown()
            .filter { it.isFile && it.extension in listOf("kt", "kotlin") }
            .filter { !it.path.contains("/build/") && !it.path.contains("/.gradle/") }
            .toList()

        val javaFiles = sourcePath.walkTopDown()
            .filter { it.isFile && it.extension == "java" }
            .filter { !it.path.contains("/build/") && !it.path.contains("/.gradle/") }
            .toList()

        var totalFiles = 0

        // Scan Kotlin files
        // 扫描 Kotlin 文件
        _scanState.value = _scanState.value.copy(phase = ScanPhase.SCANNING_KOTLIN)
        emitEffect(OtpScanEffect.TerminalOutput("📝 扫描 Kotlin 源文件...", OtpSeverity.INFO))

        for (file in kotlinFiles) {
            totalFiles++
            emitEffect(
                OtpScanEffect.TerminalOutput(
                    "   扫描: ${file.name}",
                    OtpSeverity.INFO
                )
            )

            findings.addAll(scanFile(file, "kotlin"))
            _scanState.value = _scanState.value.copy(
                progress = (totalFiles * 100 / (totalFiles + 1)).coerceAtMost(99)
            )
        }

        // Scan Java files
        // 扫描 Java 文件
        _scanState.value = _scanState.value.copy(phase = ScanPhase.SCANNING_JAVA)
        emitEffect(OtpScanEffect.TerminalOutput("\n📝 扫描 Java 源文件...", OtpSeverity.INFO))

        for (file in javaFiles) {
            totalFiles++
            emitEffect(
                OtpScanEffect.TerminalOutput(
                    "   扫描: ${file.name}",
                    OtpSeverity.INFO
                )
            )

            findings.addAll(scanFile(file, "java"))
            _scanState.value = _scanState.value.copy(
                progress = (totalFiles * 100 / (totalFiles + 1)).coerceAtMost(99)
            )
        }

        _scanState.value = _scanState.value.copy(
            phase = ScanPhase.ANALYZING,
            scannedFilesCount = totalFiles
        )

        // Sort findings by severity priority
        // 按严重等级优先级排序发现
        return findings.sortedBy { it.severity.priority }
    }

    /**
     * Scan a single file for OTP patterns
     * 扫描单个文件中的 OTP 模式
     */
    private suspend fun scanFile(file: File, language: String): List<OtpFinding> {
        val findings = mutableListOf<OtpFinding>()
        val content = try {
            file.readText()
        } catch (e: Exception) {
            return emptyList()
        }

        val lines = content.lines()

        for ((index, line) in lines.withIndex()) {
            for (pattern in otpPatterns) {
                if (pattern.pattern.containsMatchIn(line)) {
                    val finding = OtpFinding(
                        file = file.path,
                        line = index + 1,  // 1-indexed for human readability
                        severity = pattern.severity,
                        api = pattern.api,
                        description = pattern.description,
                        suggestion = pattern.suggestion
                    )
                    findings.add(finding)

                    // Emit finding to terminal
                    emitEffect(
                        OtpScanEffect.TerminalOutput(
                            "⚠️  [${pattern.severity.name}] ${file.name}:${index + 1} — ${pattern.api}",
                            pattern.severity
                        )
                    )
                }
            }
        }

        return findings
    }

    private suspend fun cancelScan() {
        _scanState.value = _scanState.value.copy(
            phase = ScanPhase.IDLE,
            progress = 0
        )
        emitEffect(OtpScanEffect.TerminalOutput("🛑 扫描已取消", OtpSeverity.INFO))
    }

    private suspend fun clearResults() {
        _scanState.value = OtpScanState()
        emitEffect(OtpScanEffect.TerminalOutput("🧹 扫描结果已清除", OtpSeverity.INFO))
    }

    private fun exportReport(format: ReportFormat, outputPath: String) {
        viewModelScope.launch {
            try {
                val report = generateReport(format)
                withContext(Dispatchers.IO) {
                    File(outputPath).writeText(report)
                }
                emitEffect(OtpScanEffect.ReportGenerated(outputPath, format))
            } catch (e: Exception) {
                emitEffect(OtpScanEffect.Error("报告生成失败: ${e.message}"))
            }
        }
    }

    private fun generateReport(format: ReportFormat): String {
        val state = _scanState.value
        return when (format) {
            ReportFormat.HTML -> generateHtmlReport(state)
            ReportFormat.MARKDOWN -> generateMarkdownReport(state)
            ReportFormat.JSON -> generateJsonReport(state)
        }
    }

    private fun generateHtmlReport(state: OtpScanState): String {
        return buildString {
            append("""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>OTP Delay Impact Scan Report</title>
    <style>
        body { font-family: -apple-system, sans-serif; margin: 40px; }
        h1 { color: #333; }
        .summary { background: #f5f5f5; padding: 20px; border-radius: 8px; }
        .HIGH { color: #f85149; }
        .MEDIUM { color: #ffd700; }
        .LOW { color: #3fb950; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background: #333; color: white; }
    </style>
</head>
<body>
    <h1>🔍 OTP Delay Impact Scan Report</h1>
    <div class="summary">
        <p><strong>扫描文件数:</strong> ${state.scannedFilesCount}</p>
        <p><strong>总发现数:</strong> ${state.findings.size}</p>
        <p><strong>🔴 HIGH:</strong> ${state.highSeverityCount}</p>
        <p><strong>🟡 MEDIUM:</strong> ${state.mediumSeverityCount}</p>
        <p><strong>🟢 LOW:</strong> ${state.lowSeverityCount}</p>
        <p><strong>耗时:</strong> ${state.durationMs}ms</p>
    </div>
    <table>
        <tr>
            <th>Severity</th>
            <th>File</th>
            <th>Line</th>
            <th>API</th>
            <th>Description</th>
            <th>Suggestion</th>
        </tr>
""")
            state.findings.forEach { finding ->
                append("""        <tr>
            <td class="${finding.severity.name}">${finding.severity.emoji} ${finding.severity.name}</td>
            <td>${finding.file}</td>
            <td>${finding.line}</td>
            <td>${finding.api}</td>
            <td>${finding.description}</td>
            <td>${finding.suggestion}</td>
        </tr>
""")
            }
            append("""    </table>
</body>
</html>""")
        }
    }

    private fun generateMarkdownReport(state: OtpScanState): String {
        return buildString {
            append("# OTP Delay Impact Scan Report\n\n")
            append("**扫描文件数:** ${state.scannedFilesCount}\n")
            append("**总发现数:** ${state.findings.size}\n")
            append("**🔴 HIGH:** ${state.highSeverityCount}\n")
            append("**🟡 MEDIUM:** ${state.mediumSeverityCount}\n")
            append("**🟢 LOW:** ${state.lowSeverityCount}\n")
            append("**耗时:** ${state.durationMs}ms\n\n")
            append("## Findings\n\n")
            append("| Severity | File | Line | API | Description | Suggestion |\n")
            append("|----------|------|------|-----|-------------|------------|\n")
            state.findings.forEach { finding ->
                append("| ${finding.severity.emoji} ${finding.severity.name} | ${finding.file} | ${finding.line} | ${finding.api} | ${finding.description} | ${finding.suggestion} |\n")
            }
        }
    }

    private fun generateJsonReport(state: OtpScanState): String {
        return buildString {
            append("""{
  "scanResult": {
    "scannedFiles": ${state.scannedFilesCount},
    "totalFindings": ${state.findings.size},
    "highSeverity": ${state.highSeverityCount},
    "mediumSeverity": ${state.mediumSeverityCount},
    "lowSeverity": ${state.lowSeverityCount},
    "durationMs": ${state.durationMs}
  },
  "findings": [
""")
            state.findings.forEachIndexed { index, finding ->
                append("""    {
      "file": "${finding.file}",
      "line": ${finding.line},
      "severity": "${finding.severity.name}",
      "api": "${finding.api}",
      "description": "${finding.description}",
      "suggestion": "${finding.suggestion}"
    }""")
                if (index < state.findings.size - 1) append(",")
                append("\n")
            }
            append("""  ]
}""")
        }
    }

    // ================================================================
    // Hash Generator Implementation — Hash 生成器实现
    // ================================================================

    private fun updatePackageName(packageName: String) {
        _hashGeneratorState.value = _hashGeneratorState.value.copy(packageName = packageName)
    }

    private fun updateKeystorePath(keystorePath: String) {
        _hashGeneratorState.value = _hashGeneratorState.value.copy(keystorePath = keystorePath)
    }

    private fun updateKeyAlias(keyAlias: String) {
        _hashGeneratorState.value = _hashGeneratorState.value.copy(keyAlias = keyAlias)
    }

    /**
     * Generate SMS Retriever hash string
     * 生成 SMS Retriever 所需的 hash 字符串
     *
     * The hash is computed as:
     * 1. Get the signing certificate's public key
     * 2. Remove the first byte (algorithm identifier)
     * 3. Append the package name bytes
     * 4. Compute SHA-256 hash
     * 5. Take the first 8 bytes and base64 encode
     *
     * Note: This is a simplified implementation. For production use,
     * consider using the official Google Play Services or
     * running the official Python script from Google.
     */
    private fun generateHash() {
        viewModelScope.launch {
            val state = _hashGeneratorState.value

            if (state.packageName.isBlank()) {
                _hashGeneratorState.value = state.copy(error = "Package name is required")
                return@launch
            }

            _hashGeneratorState.value = state.copy(isGenerating = true, error = null)

            try {
                // Simulate hash generation with delay
                // 实际实现需要读取 keystore 并计算 hash，这里简化处理
                delay(500)

                // In production, this would:
                // 1. Load the keystore: KeyStore.getInstance("JKS")
                // 2. Get the signing certificate
                // 3. Extract public key and compute hash
                // For now, emit a placeholder with instructions
                val simulatedHash = generateSimulatedHash(state.packageName)

                _hashGeneratorState.value = _hashGeneratorState.value.copy(
                    isGenerating = false,
                    generatedHash = simulatedHash
                )

                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "✅ Hash 生成成功: $simulatedHash",
                        OtpSeverity.INFO
                    )
                )
                emitEffect(
                    OtpScanEffect.TerminalOutput(
                        "💡 请将上述 hash 添加到 SMS Retriever 后端服务",
                        OtpSeverity.INFO
                    )
                )

            } catch (e: Exception) {
                _hashGeneratorState.value = _hashGeneratorState.value.copy(
                    isGenerating = false,
                    error = e.message ?: "Hash generation failed"
                )
            }
        }
    }

    /**
     * Generate simulated hash for demonstration
     * 生成模拟 hash（演示用）
     *
     * In production, use the official Google method:
     * - Python: tools-scripts-java/sms_retriever_hash_v2.py (from Google Play Services)
     * - Or use the Play Console's App Signing page to generate automatically
     */
    private fun generateSimulatedHash(packageName: String): String {
        // Simulated hash based on package name
        // 实际生产环境应使用官方方法计算真实 hash
        val hashInput = packageName.toByteArray()
        val simulatedBytes = hashInput.take(8).map { (it.toInt() xor 0x42).toByte() }
        val simulatedHash = simulatedBytes.joinToString("") { "%02X".format(it) }
        return "${packageName.substringBefore('.')}:${simulatedHash}"
    }

    private fun clearHashGenerator() {
        _hashGeneratorState.value = HashGeneratorState()
    }

    // ================================================================
    // Utility Functions — 工具函数
    // ================================================================

    /**
     * Emit a scan effect to the channel
     * 向 Channel 发送扫描副作用
     */
    private suspend fun emitEffect(effect: OtpScanEffect) {
        _scanEffects.send(effect)
    }
}

// ================================================================
// Constants for Gradle Plugin — Gradle 插件常量
// ================================================================

/**
 * OTP Delay Gradle Plugin Constants
 * Gradle 插件配置常量
 */
object OtpDelayConstants {
    const val PLUGIN_ID = "com.mvi.kenny.otpdelay"
    const val TASK_GROUP = "otpdelay"
    const val SCAN_TASK_NAME = "scanOtpDelayImpact"
    const val COMPLIANCE_TASK_NAME = "checkOtpCompliance"
    const val REPORT_DIR = "otp-delay-reports"
}
