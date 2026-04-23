package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * AAPMonitorViewModel.kt — 主 ViewModel
 * ============================================================
 */

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class AAPMonitorViewModel : ViewModel() {

    private val _state = MutableStateFlow(AAPMonitorState())
    val state: StateFlow<AAPMonitorState> = _state.asStateFlow()

    private val _effect = Channel<AAPMonitorEffect>(Channel.BUFFERED)
    val effect: Flow<AAPMonitorEffect> = _effect.receiveAsFlow()

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

    private fun startScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.SCANNING, scanProgress = 0f, error = null) }
            try {
                for (progress in 1..10) {
                    delay(200)
                    _state.update { it.copy(scanProgress = progress / 10f) }
                }
                val mockIssues = generateMockIssues()
                _state.update {
                    it.copy(scanStatus = ScanStatus.DONE, scanResults = mockIssues, scanProgress = 1f)
                }
                _effect.send(AAPMonitorEffect.ScanCompleted)
                _effect.send(AAPMonitorEffect.TriggerHapticFeedback)
            } catch (e: CancellationException) {
                _state.update { it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f) }
            } catch (e: Exception) {
                _state.update { it.copy(scanStatus = ScanStatus.ERROR, error = e.message ?: "Unknown error") }
                _effect.send(AAPMonitorEffect.ShowError(e.message ?: "Scan failed"))
            }
        }
    }

    private fun cancelScan() {
        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.IDLE, scanProgress = 0f) }
        }
    }

    private fun selectIssue(issue: AccessibilityIssue) {
        _state.update { it.copy(selectedIssue = issue) }
        viewModelScope.launch {
            _effect.send(AAPMonitorEffect.NavigateToDetail(issue))
        }
    }

    private fun setFilter(options: FilterOptions) {
        _state.update { it.copy(filterOptions = options) }
    }

    private fun refreshAapmStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                delay(500)
                val sdkVersion = Build.VERSION.SDK_INT
                val isAndroid17OrAbove = sdkVersion >= 35
                val aapmStatus = when {
                    !isAndroid17OrAbove -> AapmStatus.INACTIVE
                    kotlin.random.Random.nextBoolean() -> AapmStatus.ACTIVE
                    else -> AapmStatus.INACTIVE
                }
                val deviceInfo = DeviceAapmInfo(
                    sdkVersion = sdkVersion,
                    isRooted = false,
                    manufacturer = Build.MANUFACTURER,
                    model = Build.MODEL
                )
                _state.update {
                    it.copy(aapmStatus = aapmStatus, deviceInfo = deviceInfo, isLoading = false)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(aapmStatus = AapmStatus.UNKNOWN, isLoading = false, error = e.message)
                }
            }
        }
    }

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

    private fun setComplianceStep(step: Int) {
        if (step in 1..4) {
            _state.update { it.copy(complianceStep = step) }
        }
    }

    private fun exportReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                delay(1000)
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

    private fun setBottomSheetExpanded(expanded: Boolean) {
        _state.update { it.copy(isBottomSheetExpanded = expanded) }
    }

    private fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun loadKnowledgeBase() {
        _state.update { it.copy(knowledgeEntries = getMockKnowledgeEntries()) }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun generateMockIssues(): List<AccessibilityIssue> {
        return listOf(
            AccessibilityIssue(
                id = "issue_001",
                serviceName = "AutoFillPasswordService",
                filePath = "app/src/main/java/com/mvi/kenny/service/AutoFillPasswordService.kt",
                severity = Severity.P0,
                description = "使用了 AccessibilityService API 但未标记 isAccessibilityTool=true，在 Android 17 AAPM 模式下将被完全禁用",
                suggestedFix = "在 AndroidManifest.xml 中添加 android:accessibilityToolType=passwordManager",
                callChain = listOf("AutoFillPasswordService.onServiceConnected()", "AccessibilityService.getSystemService()")
            ),
            AccessibilityIssue(
                id = "issue_002",
                serviceName = "ScreenCaptureService",
                filePath = "app/src/main/java/com/mvi/kenny/service/ScreenCaptureService.kt",
                severity = Severity.P0,
                description = "截屏服务依赖 AccessibilityService，在 AAPM 模式下无法正常工作",
                suggestedFix = "迁移至 Screen Capture API (MediaProjection)",
                callChain = listOf("ScreenCaptureService.startCapture()", "AccessibilityService.performGlobalAction()")
            ),
            AccessibilityIssue(
                id = "issue_003",
                serviceName = "TaskAutomationService",
                filePath = "app/src/main/java/com/mvi/kenny/service/TaskAutomationService.kt",
                severity = Severity.P1,
                description = "自动化任务服务使用受限 API 调用",
                suggestedFix = "申请辅助工具认证或引导用户关闭 AAPM",
                callChain = listOf("TaskAutomationService.executeTask()", "AccessibilityService.findFocus()")
            ),
            AccessibilityIssue(
                id = "issue_004",
                serviceName = "MessageInterceptorService",
                filePath = "app/src/main/java/com/mvi/kenny/service/MessageInterceptorService.kt",
                severity = Severity.P1,
                description = "消息拦截服务依赖内容观察者模式",
                suggestedFix = "迁移至 NotificationListenerService",
                callChain = listOf("MessageInterceptorService.onAccessibilityEvent()")
            ),
            AccessibilityIssue(
                id = "issue_005",
                serviceName = "BankKeyboardService",
                filePath = "app/src/main/java/com/mvi/kenny/service/BankKeyboardService.kt",
                severity = Severity.P2,
                description = "安全键盘使用 AccessibilityService 实现按键监控",
                suggestedFix = "使用自定义软键盘 (InputMethodService) 替代",
                callChain = listOf("BankKeyboardService.onKeyEvent()", "AccessibilityService.onGesture()")
            ),
            AccessibilityIssue(
                id = "issue_006",
                serviceName = "ChatReadReceiptService",
                filePath = "app/src/main/java/com/mvi/kenny/service/ChatReadReceiptService.kt",
                severity = Severity.P2,
                description = "已读回执功能依赖窗口内容读取",
                suggestedFix = "改用 Conversation API (Android 14+)",
                callChain = listOf("ChatReadReceiptService.onAccessibilityEvent()", "AccessibilityNodeInfo.getText()")
            )
        )
    }

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
