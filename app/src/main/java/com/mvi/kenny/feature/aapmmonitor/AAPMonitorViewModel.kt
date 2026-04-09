package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorViewModel.kt — 主 ViewModel
 * AAPMonitorViewModel.kt — Main ViewModel
 * ============================================================
 */

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * AAPMonitor 主 ViewModel
 * Main ViewModel for AAPMonitor feature
 *
 * 处理所有用户交互逻辑，生成模拟数据用于演示
 * Handles all user interaction logic, generates mock data for demo
 */
class AAPMonitorViewModel : ViewModel() {

    // ============================================================
    // State — 状态流
    // State — State Flow
    // ============================================================

    private val _state = MutableStateFlow(AAPMonitorState())
    val state: StateFlow<AAPMonitorState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道（一次性事件）
    // Effect — Side Effect Channel (One-time events)
    // ============================================================

    private val _effect = Channel<AAPMonitorEffect>(Channel.BUFFERED)
    val effect: Flow<AAPMonitorEffect> = _effect.receiveAsFlow()

    // ============================================================
    // Process Intent — 处理用户意图
    // Process Intent — Handle User Intents
    // ============================================================

    /**
     * 处理用户意图
     * Process user intent
     *
     * @param intent 用户意图
     */
    fun processIntent(intent: AAPMonitorIntent) {
        when (intent) {
            is AAPMonitorIntent.StartScan -> startScan()
            is AAPMonitorIntent.CancelScan -> cancelScan()
            is AAPMonitorIntent.SelectIssue -> selectIssue(intent.issue)
            is AAPMonitorIntent.SetFilter -> setFilter(intent.options)
            is AAPMonitorIntent.RefreshAapmStatus -> refreshAapmStatus()
            is AAPMonitorIntent.MarkIssueResolved -> markIssueResolved(intent.issue)
            is AAPMonitorIntent.SetComplianceStep -> setComplianceStep(intent.step)
            is AAPMonitorIntent.ExportReport -> exportReport()
            is AAPMonitorIntent.SetBottomSheetExpanded -> setBottomSheetExpanded(intent.expanded)
            is AAPMonitorIntent.SetSearchQuery -> setSearchQuery(intent.query)
            is AAPMonitorIntent.LoadKnowledgeBase -> loadKnowledgeBase()
            is AAPMonitorIntent.DismissError -> dismissError()
        }
    }

    // ============================================================
    // Intent Handlers — 意图处理器
    // Intent Handlers — Intent Processors
    // ============================================================

    /**
     * 开始扫描
     * Start scanning
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.SCANNING, scanProgress = 0f, error = null) }

            try {
                // 模拟扫描进度 0 → 1
                // Simulate scan progress 0 → 1
                for (progress in 1..10) {
                    delay(200) // 每 200ms 更新一次
                    _state.update { it.copy(scanProgress = progress / 10f) }
                }

                // 生成模拟问题列表
                // Generate mock issue list
                val mockIssues = generateMockIssues()
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.DONE,
                        scanResults = mockIssues,
                        scanProgress = 1f
                    )
                }

                _effect.send(AAPMonitorEffect.ScanCompleted)
                _effect.send(AAPMonitorEffect.TriggerHapticFeedback)

            } catch (e: CancellationException) {
                _state.update { it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.ERROR,
                        error = e.message ?: "Unknown error"
                    )
                }
                _effect.send(AAPMonitorEffect.ShowError(e.message ?: "Scan failed"))
            }
        }
    }

    /**
     * 取消扫描
     * Cancel scanning
     */
    private fun cancelScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f) }
        }
    }

    /**
     * 选中问题
     * Select an issue
     */
    private fun selectIssue(issue: AccessibilityIssue) {
        _state.update { it.copy(selectedIssue = issue) }
        viewModelScope.launch {
            _effect.send(AAPMonitorEffect.NavigateToDetail(issue))
        }
    }

    /**
     * 设置筛选选项
     * Set filter options
     */
    private fun setFilter(options: FilterOptions) {
        _state.update { it.copy(filterOptions = options) }
    }

    /**
     * 刷新 AAPM 状态
     * Refresh AAPM status
     *
     * 检测当前设备的 Android SDK 版本，判断 AAPM 可用性
     * Checks current device's Android SDK version to determine AAPM availability
     */
    private fun refreshAapmStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // 模拟检查 AAPM 状态
                // Mock check AAPM status
                delay(500)

                val sdkVersion = Build.VERSION.SDK_INT
                val isAndroid17OrAbove = sdkVersion >= Build.VERSION_CODES.Android17

                // 模拟数据：实际项目中应调用 AdvancedProtectionManager
                // Mock data: In real project should call AdvancedProtectionManager
                val aapmStatus = when {
                    !isAndroid17OrAbove -> AapmStatus.INACTIVE
                    kotlin.random.Random.nextBoolean() -> AapmStatus.ACTIVE
                    else -> AapmStatus.INACTIVE
                }

                val deviceInfo = DeviceAapmInfo(
                    sdkVersion = sdkVersion,
                    isRooted = false, // 实际应检测 root 状态
                    manufacturer = Build.MANUFACTURER,
                    model = Build.MODEL
                )

                _state.update {
                    it.copy(
                        aapmStatus = aapmStatus,
                        deviceInfo = deviceInfo,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        aapmStatus = AapmStatus.UNKNOWN,
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * 标记问题已解决
     * Mark issue as resolved
     */
    private fun markIssueResolved(issue: AccessibilityIssue) {
        _state.update { state ->
            state.copy(
                scanResults = state.scanResults.map {
                    if (it.id == issue.id) it.copy(isResolved = true) else it
                },
                selectedIssue = if (state.selectedIssue?.id == issue.id) {
                    state.selectedIssue.copy(isResolved = true)
                } else {
                    state.selectedIssue
                }
            )
        }
        viewModelScope.launch {
            _effect.send(AAPMonitorEffect.ShowToast("Issue marked as resolved"))
        }
    }

    /**
     * 设置合规步骤
     * Set compliance step
     */
    private fun setComplianceStep(step: Int) {
        if (step in 1..4) {
            _state.update { it.copy(complianceStep = step) }
        }
    }

    /**
     * 导出报告
     * Export report
     */
    private fun exportReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // 模拟生成 JSON 报告
                // Mock generate JSON report
                delay(1000)

                val report = generateMockReport()
                val reportJson = """
                    {
                        "generatedAt": ${report.generatedAt},
                        "totalIssues": ${report.totalIssues},
                        "p0Count": ${report.p0Count},
                        "p1Count": ${report.p1Count},
                        "p2Count": ${report.p2Count},
                        "issues": [
                            ${report.issues.joinToString(",\n                            ") { issue ->
                                """
                                {
                                    "id": "${issue.id}",
                                    "serviceName": "${issue.serviceName}",
                                    "filePath": "${issue.filePath}",
                                    "severity": "${issue.severity}",
                                    "description": "${issue.description}",
                                    "suggestedFix": "${issue.suggestedFix}"
                                }
                                """.trimIndent()
                            }}
                        ]
                    }
                """.trimIndent()

                // 模拟保存路径
                // Mock save path
                val savePath = "/Downloads/AAPM_ScanReport_${System.currentTimeMillis()}.json"

                _state.update { it.copy(isLoading = false) }
                _effect.send(AAPMonitorEffect.ShareReport(savePath))
                _effect.send(AAPMonitorEffect.ShowToast("Report exported to $savePath"))

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effect.send(AAPMonitorEffect.ShowError("Failed to export report: ${e.message}"))
            }
        }
    }

    /**
     * 设置 BottomSheet 展开状态
     * Set BottomSheet expanded state
     */
    private fun setBottomSheetExpanded(expanded: Boolean) {
        _state.update { it.copy(isBottomSheetExpanded = expanded) }
    }

    /**
     * 设置搜索关键词
     * Set search query
     */
    private fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    /**
     * 加载知识库
     * Load knowledge base
     */
    private fun loadKnowledgeBase() {
        _state.update { it.copy(knowledgeEntries = getMockKnowledgeEntries()) }
    }

    /**
     * 关闭错误提示
     * Dismiss error
     */
    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    // ============================================================
    // Mock Data Generators — 模拟数据生成器
    // Mock Data Generators — For Demo Purpose
    // ============================================================

    /**
     * 生成模拟问题列表
     * Generate mock issue list
     */
    private fun generateMockIssues(): List<AccessibilityIssue> {
        return listOf(
            AccessibilityIssue(
                id = "issue_001",
                serviceName = "AutoFillPasswordService",
                filePath = "app/src/main/java/com/mvi/kenny/service/AutoFillPasswordService.kt",
                severity = Severity.P0,
                description = "使用了 AccessibilityService API 但未标记 isAccessibilityTool=\"true\"，在 Android 17 AAPM 模式下将被完全禁用",
                suggestedFix = "在 AndroidManifest.xml 中添加 android:accessibilityToolType=\"passwordManager\"，或申请辅助工具认证",
                callChain = listOf(
                    "AutoFillPasswordService.onServiceConnected()",
                    "AccessibilityService.getSystemService(Context.ACCESSIBILITY_SERVICE)",
                    "android.accessibilityservice.AccessibilityService"
                )
            ),
            AccessibilityIssue(
                id = "issue_002",
                serviceName = "ScreenCaptureService",
                filePath = "app/src/main/java/com/mvi/kenny/service/ScreenCaptureService.kt",
                severity = Severity.P0,
                description = "截屏服务依赖 AccessibilityService，在 AAPM 模式下无法正常工作",
                suggestedFix = "迁移至 Screen Capture API (MediaProjection)，无需 AccessibilityService 权限",
                callChain = listOf(
                    "ScreenCaptureService.startCapture()",
                    "AccessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)"
                )
            ),
            AccessibilityIssue(
                id = "issue_003",
                serviceName = "TaskAutomationService",
                filePath = "app/src/main/java/com/mvi/kenny/service/TaskAutomationService.kt",
                severity = Severity.P1,
                description = "自动化任务服务使用受限 API 调用，在 Android 17 下面临功能失效",
                suggestedFix = "申请辅助工具认证 (Accessibility Tool Certification) 或引导用户关闭 AAPM",
                callChain = listOf(
                    "TaskAutomationService.executeTask()",
                    "AccessibilityService.findFocus()",
                    "AccessibilityNodeInfo.performAction()"
                )
            ),
            AccessibilityIssue(
                id = "issue_004",
                serviceName = "MessageInterceptorService",
                filePath = "app/src/main/java/com/mvi/kenny/service/MessageInterceptorService.kt",
                severity = Severity.P1,
                description = "消息拦截服务依赖内容观察者模式，存在隐私合规风险",
                suggestedFix = "迁移至 NotificationListenerService，仅处理用户可见通知",
                callChain = listOf(
                    "MessageInterceptorService.onAccessibilityEvent()",
                    "AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED"
                )
            ),
            AccessibilityIssue(
                id = "issue_005",
                serviceName = "BankKeyboardService",
                filePath = "app/src/main/java/com/mvi/kenny/service/BankKeyboardService.kt",
                severity = Severity.P2,
                description = "安全键盘使用 AccessibilityService 实现按键监控，存在安全漏洞",
                suggestedFix = "使用自定义软键盘 (InputMethodService) 替代，Android 17 推荐使用 InAppKeyboard API",
                callChain = listOf(
                    "BankKeyboardService.onKeyEvent()",
                    "AccessibilityService.onGesture()"
                )
            ),
            AccessibilityIssue(
                id = "issue_006",
                serviceName = "ChatReadReceiptService",
                filePath = "app/src/main/java/com/mvi/kenny/service/ChatReadReceiptService.kt",
                severity = Severity.P2,
                description = "已读回执功能依赖窗口内容读取，可能被 AAPM 拦截",
                suggestedFix = "改用 Conversation API (Android 14+) 或引导用户授予特定权限",
                callChain = listOf(
                    "ChatReadReceiptService.onAccessibilityEvent()",
                    "AccessibilityNodeInfo.getText()"
                )
            )
        )
    }

    /**
     * 生成模拟报告
     * Generate mock report
     */
    private fun generateMockReport(): ScanReport {
        val issues = _state.value.scanResults
        return ScanReport(
            generatedAt = System.currentTimeMillis(),
            totalIssues = issues.size,
            p0Count = issues.count { it.severity == Severity.P0 },
            p1Count = issues.count { it.severity == Severity.P1 },
            p2Count = issues.count { it.severity == Severity.P2 },
            issues = issues
        )
    }

    /**
     * 获取模拟知识库条目
     * Get mock knowledge base entries
     */
    private fun getMockKnowledgeEntries(): List<ScopedApiEntry> {
        return listOf(
            ScopedApiEntry(
                name = "Autofill API",
                description = "自动填充服务 API，用于替代密码管理器的 AccessibilityService",
                alternative = "android.view.autofill.AutofillService",
                applicableScenario = "密码管理器、自动登录应用"
            ),
            ScopedApiEntry(
                name = "Screen Capture API",
                description = "屏幕录制/截图 API，替代 AccessibilityService 的截屏功能",
                alternative = "android.media.projection.MediaProjectionManager",
                applicableScenario = "屏幕录制、截图、直播应用"
            ),
            ScopedApiEntry(
                name = "NotificationListenerService",
                description = "通知监听服务，替代消息拦截功能",
                alternative = "android.service.notification.NotificationListenerService",
                applicableScenario = "消息通知类应用、已读回执"
            ),
            ScopedApiEntry(
                name = "InputMethodService",
                description = "自定义输入法服务，替代安全键盘的按键监控",
                alternative = "android.inputmethodservice.InputMethodService",
                applicableScenario = "安全键盘、特殊输入法"
            ),
            ScopedApiEntry(
                name = "InAppKeyboard API",
                description = "Android 17 引入的应用内键盘 API",
                alternative = "android.view.inputmethod.InAppKeyboard",
                applicableScenario = "银行类应用的安全键盘"
            ),
            ScopedApiEntry(
                name = "Conversation API",
                description = "对话 API，用于处理应用对话和消息",
                alternative = "android.app.ConversationInfo",
                applicableScenario = "聊天应用、消息管理"
            )
        )
    }
}

// ============================================================
// ComplianceGuideViewModel — 合规引导步骤 ViewModel
// ComplianceGuideViewModel — Compliance Guide Step ViewModel
// ============================================================

/**
 * 合规引导步骤 ViewModel
 * ViewModel for compliance guide stepper
 *
 * 管理 4 步合规流程的状态
 * Manages the 4-step compliance flow state
 */
class ComplianceGuideViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _completedSteps = MutableStateFlow(setOf<Int>())
    val completedSteps: StateFlow<Set<Int>> = _completedSteps.asStateFlow()

    private val _stepData = MutableStateFlow(getDefaultStepData())
    val stepData: StateFlow<List<ComplianceStep>> = _stepData.asStateFlow()

    /**
     * 进入下一步
     * Go to next step
     */
    fun nextStep() {
        val current = _currentStep.value
        if (current < 4) {
            _completedSteps.update { it + current }
            _currentStep.value = current + 1
        }
    }

    /**
     * 返回上一步
     * Go to previous step
     */
    fun previousStep() {
        val current = _currentStep.value
        if (current > 1) {
            _currentStep.value = current - 1
        }
    }

    /**
     * 跳转到指定步骤
     * Jump to specified step
     */
    fun goToStep(step: Int) {
        if (step in 1..4) {
            _currentStep.value = step
        }
    }

    /**
     * 完成所有步骤
     * Complete all steps
     */
    fun completeAll() {
        _completedSteps.value = setOf(1, 2, 3, 4)
        _currentStep.value = 4
    }

    /**
     * 重置流程
     * Reset flow
     */
    fun reset() {
        _currentStep.value = 1
        _completedSteps.value = emptySet()
        _stepData.value = getDefaultStepData()
    }

    /**
     * 获取默认步骤数据
     * Get default step data
     */
    private fun getDefaultStepData(): List<ComplianceStep> {
        return listOf(
            ComplianceStep(
                step = 1,
                title = "理解要求",
                description = "了解 Android 17 AAPM 安全模式的影响范围和合规要求",
                isCompleted = false
            ),
            ComplianceStep(
                step = 2,
                title = "检查资格",
                description = "确认您的应用是否符合辅助工具认证条件",
                isCompleted = false
            ),
            ComplianceStep(
                step = 3,
                title = "设置标志",
                description = "在 AndroidManifest.xml 中设置 isAccessibilityTool 属性",
                isCompleted = false
            ),
            ComplianceStep(
                step = 4,
                title = "申请认证",
                description = "提交 Google 辅助工具认证申请",
                isCompleted = false
            )
        )
    }
}
