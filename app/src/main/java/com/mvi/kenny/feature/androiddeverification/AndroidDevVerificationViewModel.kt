package com.mvi.kenny.feature.androiddeverification

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

/**
 * ============================================================
 * AndroidDevVerificationViewModel — Android 开发者验证合规与 CI 集成工具包 ViewModel
 * ============================================================
 *
 * MVI Architecture: ViewModel handles Intent → executes business logic → updates State.
 *
 * State management:
 * - _state: MutableStateFlow holding current UI state
 * - state: Public immutable StateFlow exposed to UI layer
 *
 * Effect management:
 * - _effect: Channel for one-time side effects (Toast, Clipboard, URL)
 * - effect: Public receiveAsFlow for UI to collect
 *
 * APK Scan Simulation:
 * - Simulates APK scanning process without actual APK parsing
 * - Uses preset sample data to demonstrate verification status
 * - Risk level calculated from developerVerified + distributionType + complianceStatus
 *
 * @see AndroidDevVerificationState
 * @see AndroidDevVerificationIntent
 * @see AndroidDevVerificationEffect
 */
class AndroidDevVerificationViewModel : ViewModel() {

    // ============================================================
    // State — UI 状态
    // ============================================================

    private val _state = MutableStateFlow(AndroidDevVerificationState.Initial)
    val state: StateFlow<AndroidDevVerificationState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道
    // ============================================================

    private val _effect = Channel<AndroidDevVerificationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Processing — 意图处理
    // ============================================================

    /**
     * Process user intention
     * 处理用户意图
     *
     * Entry point for all user interactions. Called from UI layer via:
     *   viewModel.sendIntent(AndroidDevVerificationIntent.xxx)
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: AndroidDevVerificationIntent) {
        when (intent) {
            is AndroidDevVerificationIntent.SelectTab -> handleSelectTab(intent.index)
            is AndroidDevVerificationIntent.ScanApk -> handleScanApk(intent.apkPath)
            is AndroidDevVerificationIntent.UpdateApkPath -> handleUpdateApkPath(intent.path)
            is AndroidDevVerificationIntent.GenerateReport -> handleGenerateReport()
            is AndroidDevVerificationIntent.CopySnippet -> handleCopySnippet(intent.snippetId, intent.content)
            is AndroidDevVerificationIntent.ToggleSnippet -> handleToggleSnippet(intent.snippetId)
            is AndroidDevVerificationIntent.OpenUrl -> handleOpenUrl(intent.url)
            is AndroidDevVerificationIntent.DismissReport -> handleDismissReport()
        }
    }

    // ============================================================
    // Tab Navigation / Tab 切换
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 切换
     *
     * @param index Selected tab index / 选中的 Tab 索引 (0-4)
     */
    private fun handleSelectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    // ============================================================
    // APK Path Update / APK 路径更新
    // ============================================================

    /**
     * Handle APK path input update
     * 处理 APK 路径输入更新
     *
     * @param path APK file path / APK 文件路径
     */
    private fun handleUpdateApkPath(path: String) {
        _state.update { it.copy(apkPath = path) }
    }

    // ============================================================
    // APK Scan / APK 扫描（模拟）
    // ============================================================

    /**
     * Handle APK scan request (simulated)
     * 处理 APK 扫描请求（模拟）
     *
     * This is a simulation — does NOT actually parse APK files.
     * Uses preset sample data to demonstrate verification detection.
     *
     * Risk Level Calculation:
     * - SAFE: developerVerified=true AND distributionType in (GooglePlay, Limited)
     * - PENDING: developerVerified=false AND distributionType=Limited
     * - NON_COMPLIANT: developerVerified=false AND distributionType=Direct
     * - UNKNOWN: distributionType=Unknown
     *
     * @param apkPath Path to APK file / APK 文件路径
     */
    private fun handleScanApk(apkPath: String) {
        viewModelScope.launch {
            // Start scanning / 开始扫描
            _state.update { it.copy(isScanning = true, apkPath = apkPath) }

            // Simulate scan delay (1.5 seconds) / 模拟扫描延迟
            delay(1500)

            // Generate simulated result based on APK path hash / 基于 APK 路径哈希生成模拟结果
            val result = simulateScanResult(apkPath)
            val riskLevel = calculateRiskLevel(result)

            _state.update {
                it.copy(
                    isScanning = false,
                    scanResult = result,
                    riskLevel = riskLevel
                )
            }

            // Show toast notification / 显示 Toast 通知
            _effect.send(
                AndroidDevVerificationEffect.ShowToast(
                    when (riskLevel) {
                        RiskLevel.SAFE -> "验证状态: 安全 / Verification: Safe"
                        RiskLevel.PENDING -> "验证状态: 待确认 / Verification: Pending"
                        RiskLevel.NON_COMPLIANT -> "验证状态: 未合规 / Verification: Non-compliant"
                        RiskLevel.UNKNOWN -> "验证状态: 未知 / Verification: Unknown"
                    }
                )
            )
        }
    }

    /**
     * Simulate APK scan result
     * 模拟 APK 扫描结果
     *
     * @param apkPath APK file path used to generate deterministic sample data / APK 文件路径
     * @return Simulated VerificationResult / 模拟的验证结果
     */
    private fun simulateScanResult(apkPath: String): VerificationResult {
        // Use path hash to deterministically select sample data
        // 使用路径哈希确定性地选择示例数据
        val hash = apkPath.hashCode().let { if (it < 0) -it else it }
        val samples = listOf(
            VerificationResult(
                packageName = "com.example.myapp",
                developerVerified = true,
                verificationDate = "2025-03-15T10:30:00Z",
                distributionType = "Google Play",
                complianceStatus = "COMPLIANT"
            ),
            VerificationResult(
                packageName = "com.example.limiteddist",
                developerVerified = true,
                verificationDate = "2025-06-20T14:00:00Z",
                distributionType = "Limited Distribution",
                complianceStatus = "COMPLIANT"
            ),
            VerificationResult(
                packageName = "com.example.enterprise",
                developerVerified = false,
                verificationDate = null,
                distributionType = "Limited Distribution",
                complianceStatus = "PENDING_VERIFICATION"
            ),
            VerificationResult(
                packageName = "com.example.directdist",
                developerVerified = false,
                verificationDate = null,
                distributionType = "Direct Distribution",
                complianceStatus = "NON_COMPLIANT"
            ),
            VerificationResult(
                packageName = "com.example.unknown",
                developerVerified = false,
                verificationDate = null,
                distributionType = "Unknown",
                complianceStatus = "UNKNOWN"
            ),
        )
        return samples[hash % samples.size]
    }

    /**
     * Calculate risk level based on verification result
     * 根据验证结果计算风险等级
     *
     * @param result Verification result / 验证结果
     * @return Calculated risk level / 计算后的风险等级
     */
    private fun calculateRiskLevel(result: VerificationResult): RiskLevel {
        return when {
            result.developerVerified && result.distributionType in listOf("Google Play", "Limited Distribution") -> RiskLevel.SAFE
            !result.developerVerified && result.distributionType == "Limited Distribution" -> RiskLevel.PENDING
            !result.developerVerified && result.distributionType == "Direct Distribution" -> RiskLevel.NON_COMPLIANT
            else -> RiskLevel.UNKNOWN
        }
    }

    // ============================================================
    // Report Generation / 报告生成
    // ============================================================

    /**
     * Handle generate compliance report request
     * 处理生成合规报告请求
     *
     * Generates a simulated JSON compliance report and shows it in BottomSheet.
     * 生成模拟 JSON 格式合规报告，并在 BottomSheet 中展示。
     */
    private fun handleGenerateReport() {
        viewModelScope.launch {
            val result = _state.value.scanResult
            val riskLevel = _state.value.riskLevel

            // Generate JSON compliance report / 生成 JSON 合规报告
            val reportJson = buildString {
                appendLine("{")
                appendLine("  \"reportId\": \"RPT-${System.currentTimeMillis()}\",")
                appendLine("  \"generatedAt\": \"${java.time.Instant.now()}\",")
                appendLine("  \"toolVersion\": \"1.0.0\",")
                appendLine("  \"developerVerification\": {")
                appendLine("    \"packageName\": \"${result?.packageName ?: "N/A"}\",")
                appendLine("    \"developerVerified\": ${result?.developerVerified ?: false},")
                appendLine("    \"verificationDate\": \"${result?.verificationDate ?: "N/A"}\",")
                appendLine("    \"distributionType\": \"${result?.distributionType ?: "Unknown"}\",")
                appendLine("    \"complianceStatus\": \"${result?.complianceStatus ?: "UNKNOWN"}\"")
                appendLine("  },")
                appendLine("  \"riskAssessment\": {")
                appendLine("    \"level\": \"${riskLevel.name}\",")
                appendLine("    \"label\": \"${riskLevel.label}\",")
                appendLine("    \"emoji\": \"${riskLevel.emoji}\",")
                appendLine("    \"colorHex\": \"#${java.lang.Long.toHexString(riskLevel.colorHex)}\"")
                appendLine("  },")
                appendLine("  \"recommendations\": [")

                val recommendations = getRecommendations(riskLevel, result?.distributionType ?: "Unknown")
                recommendations.forEachIndexed { index, rec ->
                    appendLine("    { \"priority\": ${rec.first}, \"action\": \"${rec.second}\" }${if (index < recommendations.size - 1) "," else ""}")
                }

                appendLine("  ],")
                appendLine("  \"enforcementTimeline\": {")
                appendLine("    \"brazil\": \"2026-09-01\",")
                appendLine("    \"indonesia\": \"2026-09-01\",")
                appendLine("    \"singapore\": \"2026-09-01\",")
                appendLine("    \"thailand\": \"2026-09-01\",")
                appendLine("    \"global\": \"2027-01-01\"")
                appendLine("  },")
                appendLine("  \"apkPath\": \"${_state.value.apkPath}\"")
                appendLine("}")
            }

            _state.update {
                it.copy(
                    isReportVisible = true,
                    reportContent = reportJson
                )
            }
        }
    }

    /**
     * Get remediation recommendations based on risk level and distribution type
     * 根据风险等级和分发类型获取修复建议
     *
     * @param riskLevel Current risk level / 当前风险等级
     * @param distributionType Distribution type / 分发类型
     * @return List of (priority, action) pairs / (优先级, 操作) 对列表
     */
    private fun getRecommendations(riskLevel: RiskLevel, distributionType: String): List<Pair<Int, String>> {
        return when (riskLevel) {
            RiskLevel.SAFE -> listOf(
                Pair(1, "Maintain verification status / 保持验证状态"),
                Pair(2, "Monitor policy updates / 关注政策更新"),
                Pair(3, "Review distribution compliance / 审查分发合规性")
            )
            RiskLevel.PENDING -> listOf(
                Pair(1, "Complete developer verification immediately / 立即完成开发者验证"),
                Pair(2, "Review Limited Distribution account requirements / 审查有限分发账号要求"),
                Pair(3, "Prepare compliance documentation / 准备合规文档")
            )
            RiskLevel.NON_COMPLIANT -> listOf(
                Pair(1, "URGENT: Complete developer verification / 紧急：完成开发者验证"),
                Pair(2, "Evaluate alternative distribution strategies / 评估替代分发策略"),
                Pair(3, "Consider App Claim process / 考虑 App Claim 流程"),
                Pair(4, "Review Google Play as primary distribution channel / 审查 Google Play 作为主要分发渠道")
            )
            RiskLevel.UNKNOWN -> listOf(
                Pair(1, "Determine distribution type / 确定分发类型"),
                Pair(2, "Check verification status / 检查验证状态"),
                Pair(3, "Consult compliance documentation / 咨询合规文档")
            )
        }
    }

    /**
     * Handle dismiss report BottomSheet
     * 处理关闭报告 BottomSheet
     */
    private fun handleDismissReport() {
        _state.update { it.copy(isReportVisible = false) }
    }

    // ============================================================
    // Clipboard / 剪贴板
    // ============================================================

    /**
     * Handle copy snippet to clipboard
     * 处理复制代码片段到剪贴板
     *
     * @param snippetId Snippet unique identifier / 片段唯一 ID
     * @param content Content to copy / 要复制的内容
     */
    private fun handleCopySnippet(snippetId: String, content: String) {
        viewModelScope.launch {
            _effect.send(AndroidDevVerificationEffect.CopyToClipboard(content))
            _effect.send(AndroidDevVerificationEffect.ShowToast("已复制到剪贴板 / Copied to clipboard"))
            _state.update { it.copy(copiedSnippetId = snippetId) }
        }
    }

    // ============================================================
    // Snippet Expansion / 代码片段展开/收起
    // ============================================================

    /**
     * Toggle code snippet expansion state
     * 切换代码片段展开/收起状态
     *
     * @param snippetId Snippet identifier / 片段标识
     */
    private fun handleToggleSnippet(snippetId: String) {
        _state.update { currentState ->
            val newExpanded = if (snippetId in currentState.expandedSnippets) {
                currentState.expandedSnippets - snippetId
            } else {
                currentState.expandedSnippets + snippetId
            }
            currentState.copy(expandedSnippets = newExpanded)
        }
    }

    // ============================================================
    // URL Opening / 打开外部链接
    // ============================================================

    /**
     * Handle open URL request
     * 处理打开外部链接请求
     *
     * @param url Target URL / 目标 URL
     */
    private fun handleOpenUrl(url: String) {
        viewModelScope.launch {
            _effect.send(AndroidDevVerificationEffect.OpenUrl(url))
        }
    }
}
