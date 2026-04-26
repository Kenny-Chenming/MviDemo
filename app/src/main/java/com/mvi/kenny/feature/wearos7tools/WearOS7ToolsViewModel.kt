package com.mvi.kenny.feature.wearos7tools

// ================================================================
// WearOS7ToolsViewModel — Wear OS 7 开发者工具箱 MVI ViewModel
// ================================================================
// ViewModel for Wear OS 7 developer toolkit.
//
// PRD-164: Wear OS 7 开发工具包
// Design Reference: memory/agency/designs/PRD-164-Wear-OS-7-开发工具包.md
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage tab/tool selection navigation
//   - Simulate compliance scan workflow
//   - Handle code copy feedback
//   - Manage watch form factor preview switching
//   - Emit one-time Effects (toast, navigation, share)
//
// Google I/O 2026 (May 19-20) — Wear OS 7 will be officially announced.
// Tool 1: Live Updates phone→watch migration guide
// Tool 2: Live Updates Watch UI templates (round/OLED/square)
// Tool 3: Health API × Health Connect synergy SDK
// Tool 4: Compose for Wear OS best practices
// Tool 5: Multi-screen CI adaptation detector (Gradle plugin)
// Tool 6: Health Services API Mock testing framework
// Tool 7: Wear OS 7 API compliance detector
// Tool 8: Wear OS × Android XR synergy templates
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ============================================================
 * WearOS7ToolsViewModel — Wear OS 7 工具箱 ViewModel
 * ============================================================
 * Manages the WearOS7ToolsState and processes WearOS7ToolsIntent.
 *
 * In a real implementation:
 *   - Tool 5 (Multi-screen CI) would use a Gradle plugin for actual layout detection
 *   - Tool 6 (Health Mock) would mock Health Services API sensor data
 *   - Tool 7 (Compliance) would use static analysis / lint checks
 *
 * Current implementation simulates scanning for demonstration purposes.
 *
 * @see WearOS7ToolsState
 * @see WearOS7ToolsIntent
 * @see WearOS7ToolsEffect
 */
class WearOS7ToolsViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(WearOS7ToolsState.Initial)
    val state: StateFlow<WearOS7ToolsState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via Channel
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<WearOS7ToolsEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // sendIntent — 路由所有用户意图
    // ─────────────────────────────────────────────────────────────
    /**
     * Route incoming Intents to their handlers.
     * Every user action calls this method with a corresponding Intent.
     *
     * @param intent The user intent to process
     */
    fun sendIntent(intent: WearOS7ToolsIntent) {
        when (intent) {
            is WearOS7ToolsIntent.SelectTab -> handleSelectTab(intent.tab)
            is WearOS7ToolsIntent.SelectTool -> handleSelectTool(intent.tool)
            is WearOS7ToolsIntent.BackToOverview -> handleBackToOverview()
            is WearOS7ToolsIntent.ChangeWatchFormFactor -> handleChangeWatchFormFactor(intent.formFactor)
            is WearOS7ToolsIntent.StartScan -> handleStartScan()
            is WearOS7ToolsIntent.CopyCode -> handleCopyCode(intent.codeBlockId)
            is WearOS7ToolsIntent.ExportReport -> handleExportReport()
            is WearOS7ToolsIntent.ClearError -> handleClearError()
            is WearOS7ToolsIntent.ToggleCodeBlock -> handleToggleCodeBlock(intent.codeBlockId)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers — 业务逻辑处理
    // ─────────────────────────────────────────────────────────────

    /**
     * Handle tab selection.
     * Switches the active tab in the bottom navigation.
     */
    private fun handleSelectTab(tab: WearOS7Tab) {
        _state.update { it.copy(selectedTab = tab, selectedTool = null) }
    }

    /**
     * Handle tool selection.
     * Opens the tool detail panel.
     */
    private fun handleSelectTool(tool: WearOS7Tool) {
        _state.update { it.copy(selectedTool = tool) }
    }

    /**
     * Handle back navigation.
     * Returns to the dashboard/overview.
     */
    private fun handleBackToOverview() {
        _state.update { it.copy(selectedTool = null) }
    }

    /**
     * Handle watch form factor change for UI preview.
     * Updates the preview mode for Live Updates Watch UI tool.
     */
    private fun handleChangeWatchFormFactor(formFactor: WatchFormFactor) {
        _state.update { it.copy(watchFormFactor = formFactor) }
    }

    /**
     * Handle scan start.
     * Simulates a compliance scan for Wear OS 7 API usage.
     * In production, this would invoke a Gradle plugin or lint check.
     */
    private fun handleStartScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f, scanResults = emptyList()) }

            try {
                // Simulate 5-step scan progress
                val steps = listOf(
                    "解析 Manifest" to 0.2f,
                    "扫描代码路径" to 0.4f,
                    "检测 Wear OS 7 API 调用" to 0.6f,
                    "合规性分析" to 0.8f,
                    "生成报告" to 1.0f
                )

                for ((description, progress) in steps) {
                    delay(400) // Simulate work
                    _state.update { it.copy(scanProgress = progress) }
                }

                // Generate simulated compliance results
                val results = generateSimulatedComplianceResults()
                _state.update {
                    it.copy(
                        isScanning = false,
                        scanProgress = 1.0f,
                        scanResults = results
                    )
                }

                _effect.emit(WearOS7ToolsEffect.ShowToast("扫描完成，共发现 ${results.size} 个问题"))
            } catch (e: Exception) {
                _state.update { it.copy(isScanning = false, errorMessage = e.message) }
                _effect.emit(WearOS7ToolsEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * Handle code copy action.
     * Shows a toast feedback and resets the codeCopied flag.
     */
    private fun handleCopyCode(codeBlockId: String) {
        viewModelScope.launch {
            _state.update { it.copy(codeCopied = true) }
            _effect.emit(WearOS7ToolsEffect.ShowToast("代码已复制到剪贴板"))
            delay(2000)
            _state.update { it.copy(codeCopied = false) }
        }
    }

    /**
     * Handle report export.
     * In production, this would generate a PDF or Markdown report.
     */
    private fun handleExportReport() {
        viewModelScope.launch {
            _effect.emit(WearOS7ToolsEffect.ShowToast("报告导出功能开发中"))
        }
    }

    /**
     * Handle error clearing.
     */
    private fun handleClearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    /**
     * Handle code block expand/collapse toggle.
     */
    private fun handleToggleCodeBlock(codeBlockId: String) {
        _state.update {
            it.copy(
                expandedCodeBlockId = if (it.expandedCodeBlockId == codeBlockId) null else codeBlockId
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 模拟数据生成 / Simulated Data Generation
    // ─────────────────────────────────────────────────────────────

    /**
     * Generate simulated compliance results for demonstration.
     * In production, these would come from actual lint/Gradle plugin analysis.
     */
    private fun generateSimulatedComplianceResults(): List<ComplianceResult> {
        return listOf(
            ComplianceResult(
                id = UUID.randomUUID().toString(),
                toolName = "Wear OS 7 新 API 合规检测",
                filePath = "app/src/main/java/com/example/MyWatchFace.kt",
                lineNumber = 42,
                issueDescription = "使用已废弃的 WatchFaceService API，应迁移到 Compose WatchFace API",
                severity = "P1",
                complianceLevel = ComplianceLevel.Warning,
                suggestedFix = "将 WatchFaceService 替换为 androidx.wear.watchface.composibility.WatchFaceHost"
            ),
            ComplianceResult(
                id = UUID.randomUUID().toString(),
                toolName = "Wear OS 7 新 API 合规检测",
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 18,
                issueDescription = "Live Updates 手表端权限声明缺失",
                severity = "P2",
                complianceLevel = ComplianceLevel.Warning,
                suggestedFix = "添加 android.permission.permission-group.LIVE_UPDATES"
            ),
            ComplianceResult(
                id = UUID.randomUUID().toString(),
                toolName = "多屏幕尺寸 CI 适配检测",
                filePath = "app/src/main/res/layout/watch_square.xml",
                lineNumber = null,
                issueDescription = "方形屏幕布局未适配圆形 Watch 裁剪区域",
                severity = "P1",
                complianceLevel = ComplianceLevel.Warning,
                suggestedFix = "使用 BoxWithConstraints 检测屏幕形态，动态调整布局"
            )
        )
    }
}
