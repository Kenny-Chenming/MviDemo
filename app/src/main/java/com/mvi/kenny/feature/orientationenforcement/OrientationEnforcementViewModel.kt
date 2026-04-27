package com.mvi.kenny.feature.orientationenforcement

// ================================================================
// OrientationEnforcementViewModel — Android 17 大屏方向锁定合规检测工具 ViewModel
// ================================================================
// MVI ViewModel for Android 17 Large Screen Orientation Enforcement compliance toolkit.
//
// PRD-183: Android 17 Large Screen Resizability & Orientation Enforcement 合规检测工具包
// Design Reference: memory/agency/designs/PRD-183-Android-17-Large-Screen-Resizability-Orientation-Enforcement.md
//
// MVI 架构 / MVI Architecture:
//   State  — 页面状态，通过 StateFlow 暴露给 UI
//   Intent — 用户意图，通过 sendIntent() 发送给 ViewModel
//   Effect — 一次性副作用，通过 Channel 发送给 UI（如 Toast、导航）
//
// 协程处理 / Coroutine Handling:
//   使用 viewModelScope.launch 处理所有异步操作
//   使用 Dispatchers.IO 进行 Manifest/代码解析
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mvi.kenny.feature.orientationenforcement.ComplianceStatus as Status
import com.mvi.kenny.feature.orientationenforcement.ScanPhase as Phase
import com.mvi.kenny.feature.orientationenforcement.ToolId as TId
import com.mvi.kenny.feature.orientationenforcement.ViolationType as VType
import com.mvi.kenny.feature.orientationenforcement.ViolationSeverity as VSeverity

/**
 * ============================================================
 * OrientationEnforcementViewModel — MVI ViewModel
 * ============================================================
 */
class OrientationEnforcementViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — 页面状态（single source of truth）
    // ─────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(OrientationEnforcementState.Initial)
    val state: StateFlow<OrientationEnforcementState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — 一次性副作用（Toast、导航、剪贴板等）
    // ─────────────────────────────────────────────────────────────

    private val _effect = MutableStateFlow<OrientationEnforcementEffect?>(null)
    val effect: StateFlow<OrientationEnforcementEffect?> = _effect.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state — 内部扫描状态
    // ─────────────────────────────────────────────────────────────

    private var scanJob: Job? = null

    // ============================================================
    // sendIntent — 接收用户意图（MVI Intent 处理入口）
    // ============================================================
    // All user intents are routed through this single entry point.
    //
    // @param intent User intent (sealed interface)
    // ============================================================

    fun sendIntent(intent: OrientationEnforcementIntent) {
        when (intent) {
            is OrientationEnforcementIntent.SelectApp -> handleSelectApp(intent.app)
            is OrientationEnforcementIntent.StartScan -> handleStartScan()
            is OrientationEnforcementIntent.CancelScan -> handleCancelScan()
            is OrientationEnforcementIntent.SelectViolation -> handleSelectViolation(intent.violation)
            is OrientationEnforcementIntent.GenerateDiff -> handleGenerateDiff(intent.violation)
            is OrientationEnforcementIntent.ApplyDiff -> handleApplyDiff(intent.diff)
            is OrientationEnforcementIntent.SetMigrationStep -> handleSetMigrationStep(intent.step)
            is OrientationEnforcementIntent.SetWindowSizeClass -> handleSetWindowSizeClass(intent.wsc)
            is OrientationEnforcementIntent.SelectTool -> handleSelectTool(intent.toolId)
            is OrientationEnforcementIntent.BackToDashboard -> handleBackToDashboard()
            is OrientationEnforcementIntent.ExportReport -> handleExportReport(intent.format)
            is OrientationEnforcementIntent.ClearError -> handleClearError()
        }
    }

    /**
     * consumeEffect — 消费副作用（UI 调用后清除）
     */
    fun consumeEffect() {
        _effect.value = null
    }

    // ============================================================
    // Intent Handlers — 意图处理器
    // ============================================================

    /**
     * Handle SelectApp intent.
     * @param app Selected app info
     */
    private fun handleSelectApp(app: AppInfo) {
        _state.update { it.copy(selectedApp = app) }
    }

    /**
     * Handle StartScan intent.
     * Initiates the orientation/resizability scan simulation.
     * 启动扫描流程：Manifest 解析 → 代码扫描 → 生成报告
     */
    private fun handleStartScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _state.update { it.copy(scanPhase = Phase.Scanning, progress = 0f, error = null) }

            try {
                // ─── Phase 1: Manifest 扫描（0-30%）──────────────────
                _state.update { it.copy(progress = 0.1f) }
                delay(300) // Simulate IO work / 模拟 IO 操作

                val manifestViolations = simulateManifestScan()
                _state.update { it.copy(progress = 0.3f) }
                delay(200)

                // ─── Phase 2: 代码扫描（30-60%）─────────────────────
                val codeViolations = simulateCodeScan()
                _state.update { it.copy(progress = 0.6f) }
                delay(200)

                // ─── Phase 3: 状态验证（60-80%）─────────────────────
                val configViolations = simulateConfigChangeScan()
                _state.update { it.copy(progress = 0.8f) }
                delay(200)

                // ─── Phase 4: 生成报告（80-100%）────────────────────
                val allViolations = manifestViolations + codeViolations + configViolations
                val warnings = allViolations.count { it.severity == VSeverity.MEDIUM }
                val passes = 8 - allViolations.size.coerceAtLeast(0)

                val scanResult = ScanResult(
                    totalViolations = allViolations.size,
                    warningCount = warnings,
                    passCount = passes.coerceAtLeast(0),
                    manifestViolations = manifestViolations,
                    codeViolations = codeViolations,
                    scanDurationMs = System.currentTimeMillis() - (System.currentTimeMillis() - 1200L)
                )

                _state.update { it.copy(progress = 1f, scanPhase = Phase.Completed, scanResult = scanResult) }

                // Send completion effect
                _effect.value = OrientationEnforcementEffect.ShowToast("扫描完成，发现 ${allViolations.size} 个问题")

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        scanPhase = Phase.Error,
                        error = e.message ?: "扫描失败：未知错误"
                    )
                }
            }
        }
    }

    /**
     * Handle CancelScan intent.
     * Cancels the ongoing scan.
     */
    private fun handleCancelScan() {
        scanJob?.cancel()
        _state.update { it.copy(scanPhase = Phase.Idle, progress = 0f) }
        _effect.value = OrientationEnforcementEffect.ShowToast("扫描已取消")
    }

    /**
     * Handle SelectViolation intent.
     * @param violation Selected violation item
     */
    private fun handleSelectViolation(violation: ViolationItem) {
        _state.update { it.copy(selectedViolation = violation) }
        _effect.value = OrientationEnforcementEffect.NavigateToViolation(violation.id)
    }

    /**
     * Handle GenerateDiff intent.
     * Generates a fix diff for the given violation.
     * @param violation Violation to generate diff for
     */
    private fun handleGenerateDiff(violation: ViolationItem) {
        viewModelScope.launch {
            val diff = withContext(Dispatchers.Default) {
                generateDiffForViolation(violation)
            }
            _state.update { it.copy(generatedDiff = diff) }
            _effect.value = OrientationEnforcementEffect.CopyToClipboard(diff.after)
            _effect.value = OrientationEnforcementEffect.ShowToast("修复代码已复制到剪贴板")
        }
    }

    /**
     * Handle ApplyDiff intent.
     * @param diff Diff to apply (simulated: just show toast)
     */
    private fun handleApplyDiff(diff: DiffResult) {
        viewModelScope.launch {
            // Simulate applying diff / 模拟应用 Diff
            delay(500)
            _effect.value = OrientationEnforcementEffect.ShowToast("Diff 已应用（请手动在项目中修改）")
        }
    }

    /**
     * Handle SetMigrationStep intent.
     * @param step Migration step to set
     */
    private fun handleSetMigrationStep(step: MigrationStep) {
        _state.update { it.copy(migrationStep = step) }
    }

    /**
     * Handle SetWindowSizeClass intent.
     * @param wsc Window size class to set
     */
    private fun handleSetWindowSizeClass(wsc: WindowSizeClassOption) {
        _state.update { it.copy(windowSizeClass = wsc) }
    }

    /**
     * Handle SelectTool intent.
     * @param toolId Selected tool ID
     */
    private fun handleSelectTool(toolId: String) {
        _state.update { it.copy(currentToolId = toolId) }
    }

    /**
     * Handle BackToDashboard intent.
     */
    private fun handleBackToDashboard() {
        _state.update {
            it.copy(
                currentToolId = null,
                selectedViolation = null,
                generatedDiff = null
            )
        }
    }

    /**
     * Handle ExportReport intent.
     * @param format Export format (markdown/json)
     */
    private fun handleExportReport(format: String) {
        viewModelScope.launch {
            val report = buildExportReport(format)
            _effect.value = OrientationEnforcementEffect.ShareReport(report, "Orientation Enforcement Report")
        }
    }

    /**
     * Handle ClearError intent.
     */
    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }

    // ============================================================
    // 模拟扫描逻辑 / Simulation Scan Logic
    // ============================================================
    // In a real implementation, these would parse the actual
    // Android project files using PackageManager, AST, etc.

    /**
     * Simulate Manifest scan for orientation locks.
     * 模拟 Manifest 扫描：检测 screenOrientation 和 resizeableActivity 违规
     */
    private fun simulateManifestScan(): List<ViolationItem> {
        return listOf(
            ViolationItem(
                id = "v-manifest-001",
                type = VType.MANIFEST_ORIENTATION_LOCK,
                severity = VSeverity.HIGH,
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 42,
                currentValue = "portrait",
                expectedValue = "unspecified (default)",
                description = "Activity 锁定了 portrait 方向，在 Android 17 大屏设备（sw≥600dp）上会强制崩溃或布局扭曲",
                fixSuggestion = "移除 android:screenOrientation=\"portrait\" 属性，或改为 \"unspecified\""
            ),
            ViolationItem(
                id = "v-manifest-002",
                type = VType.MANIFEST_ORIENTATION_LOCK,
                severity = VSeverity.HIGH,
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 58,
                currentValue = "landscape",
                expectedValue = "unspecified (default)",
                description = "Activity 锁定了 landscape 方向，在 Android 17 大屏设备上不支持锁定",
                fixSuggestion = "移除 android:screenOrientation 属性"
            ),
            ViolationItem(
                id = "v-manifest-003",
                type = VType.RESIZABLE_ACTIVITY_FALSE,
                severity = VSeverity.HIGH,
                filePath = "app/src/main/AndroidManifest.xml",
                lineNumber = 100,
                currentValue = "false",
                expectedValue = "true",
                description = "resizeableActivity=\"false\" 会导致 App 在可调整窗口中被系统杀死（Android 17 强制执行）",
                fixSuggestion = "将 android:resizeableActivity=\"false\" 改为 \"true\"，并确保 Activity 支持双向布局"
            )
        )
    }

    /**
     * Simulate code scan for setRequestedOrientation calls.
     * 模拟代码扫描：检测 setRequestedOrientation() 调用
     */
    private fun simulateCodeScan(): List<ViolationItem> {
        return listOf(
            ViolationItem(
                id = "v-code-001",
                type = VType.CODE_SET_ORIENTATION,
                severity = VSeverity.HIGH,
                filePath = "app/src/main/java/com/example/MainActivity.kt",
                lineNumber = 88,
                currentValue = "ActivityInfo.SCREEN_ORIENTATION_PORTRAIT",
                expectedValue = "ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED",
                description = "代码中调用 setRequestedOrientation 锁定为 portrait，Android 17 大屏设备上会失效",
                fixSuggestion = "移除此行代码，或改为 SCREEN_ORIENTATION_UNSPECIFIED"
            ),
            ViolationItem(
                id = "v-code-002",
                type = VType.CODE_SET_ORIENTATION,
                severity = VSeverity.MEDIUM,
                filePath = "app/src/main/java/com/example/DetailActivity.kt",
                lineNumber = 120,
                currentValue = "ActivityInfo.SCREEN_ORIENTATION_LOCKED",
                expectedValue = "ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED",
                description = "使用了 SCREEN_ORIENTATION_LOCKED，在 Android 17 大屏设备上需要特殊处理",
                fixSuggestion = "移除此行代码，或添加 sw≥600dp 的特殊分支处理"
            )
        )
    }

    /**
     * Simulate configuration change handling scan.
     * 模拟配置变更扫描：检测 onSaveInstanceState 使用情况
     */
    private fun simulateConfigChangeScan(): List<ViolationItem> {
        return listOf(
            ViolationItem(
                id = "v-config-001",
                type = VType.CONFIG_CHANGE_UNHANDLED,
                severity = VSeverity.MEDIUM,
                filePath = "app/src/main/java/com/example/EditorActivity.kt",
                lineNumber = 55,
                currentValue = "onSaveInstanceState 未调用",
                expectedValue = "应正确调用 onSaveInstanceState 保存编辑状态",
                description = "EditorActivity 未实现 onSaveInstanceState，旋转或窗口调整时用户编辑内容会丢失",
                fixSuggestion = "重写 onSaveInstanceState(outState: Bundle)，在其中保存编辑内容：outState.putString(\"content\", editorContent)"
            )
        )
    }

    /**
     * Generate fix diff for a violation.
     * @param violation Violation to generate diff for
     * @return DiffResult with before/after code
     */
    private fun generateDiffForViolation(violation: ViolationItem): DiffResult {
        return when (violation.type) {
            VType.MANIFEST_ORIENTATION_LOCK -> DiffResult(
                before = """<!-- Before: screenOrientation locked -->
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize" />""",
                after = """<!-- After: screenOrientation removed for Android 17 compliance -->
<activity
    android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize" />""",
                language = "xml",
                description = "移除 screenOrientation 属性，改为通过 configChanges 声明处理配置变更"
            )
            VType.RESIZABLE_ACTIVITY_FALSE -> DiffResult(
                before = """<!-- Before: resizeableActivity=false (will be killed on Android 17) -->
<activity
    android:name=".MainActivity"
    android:resizeableActivity="false" />""",
                after = """<!-- After: resizeableActivity=true (compliant with Android 17) -->
<activity
    android:name=".MainActivity"
    android:resizeableActivity="true" />""",
                language = "xml",
                description = "将 resizeableActivity 设为 true，确保 App 在可调整窗口中正常运行"
            )
            VType.CODE_SET_ORIENTATION -> DiffResult(
                before = """// Before: Lock orientation in code
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);""",
                after = """// After: Remove orientation lock
// setRequestedOrientation removed for Android 17 compliance
// If you must lock orientation, use a sw≥600dp resource qualifier instead
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);""",
                language = "kotlin",
                description = "移除 setRequestedOrientation 调用，改为 SCREEN_ORIENTATION_UNSPECIFIED"
            )
            VType.CONFIG_CHANGE_UNHANDLED -> DiffResult(
                before = """// Before: No state save on configuration change
class EditorActivity : AppCompatActivity() {
    // Missing: onSaveInstanceState override
}""",
                after = """// After: Proper state save on configuration change
class EditorActivity : AppCompatActivity() {
    private var editorContent: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore saved content on recreation
        editorContent = savedInstanceState?.getString("content") ?: ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save editor content before activity is destroyed
        outState.putString("content", editorContent)
    }
}""",
                language = "kotlin",
                description = "添加 onSaveInstanceState 实现，保存编辑器内容"
            )
        }
    }

    /**
     * Build export report in the specified format.
     * @param format Export format (markdown/json)
     * @return Formatted report string
     */
    private fun buildExportReport(format: String): String {
        val result = _state.value.scanResult ?: return ""
        val builder = StringBuilder()

        if (format == "markdown") {
            builder.appendLine("# Android 17 大屏方向锁定合规检测报告")
            builder.appendLine()
            builder.appendLine("## 扫描概览")
            builder.appendLine("- 违规项：${result.totalViolations}")
            builder.appendLine("- 警告项：${result.warningCount}")
            builder.appendLine("- 合规项：${result.passCount}")
            builder.appendLine()
            builder.appendLine("## Manifest 违规")
            result.manifestViolations.forEach { v ->
                builder.appendLine("- **${v.filePath}:${v.lineNumber}** [${v.severity.displayName}] ${v.description}")
            }
            builder.appendLine()
            builder.appendLine("## 代码违规")
            result.codeViolations.forEach { v ->
                builder.appendLine("- **${v.filePath}:${v.lineNumber}** [${v.severity.displayName}] ${v.description}")
            }
        }

        return builder.toString()
    }
}
