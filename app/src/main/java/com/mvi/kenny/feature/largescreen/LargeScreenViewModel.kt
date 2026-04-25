package com.mvi.kenny.feature.largescreen

// ================================================================
// LargeScreenViewModel — Android 17 大屏强制适配 MVI ViewModel
// ================================================================
// ViewModel for Android 17 Large Screen adaptation toolkit.
//
// PRD-155: Android 17 大屏强制适配与 Continuous Canary Release 开发工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Manage 7-tool navigation and state
//   - Simulate compliance detection workflow
//   - Simulate multi-window screenshot testing
//   - Manage API change feed and migration tasks
//   - Handle Play Store pre-check simulation
//   - Emit one-time Effects (toast, diff, export)
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
 * LargeScreenViewModel — 大屏适配工具 ViewModel
 * ============================================================
 * Manages the LargeScreenState and processes LargeScreenIntent.
 * In a real implementation, this would use APK lint/manifest analysis
 * and screenshot automation tools (e.g., UIAutomator, Roborazzi).
 *
 * @see LargeScreenState
 * @see LargeScreenIntent
 * @see LargeScreenEffect
 */
class LargeScreenViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(LargeScreenState.Initial)
    val state: StateFlow<LargeScreenState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via Channel
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<LargeScreenEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Default data — initialized before init block
    // ─────────────────────────────────────────────────────────────

    companion object {
        /** Default foldable/freeform adaptation checklist items */
        private val defaultChecklistItems = listOf(
            ChecklistItem(
                id = "ck_window_layouts",
                category = "WindowManager",
                title = "WindowManager 依赖已添加",
                description = "添加 Jetpack WindowManager 1.3+ 依赖：androidx.window:window",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/reference/androidx/window/java/androidx/window",
                order = 1
            ),
            ChecklistItem(
                id = "ck_activity_recreate",
                category = "WindowManager",
                title = "Activity 可在配置变更时正确重建",
                description = "移除 android:configChanges 中的 orientation|screenSize，避免手动处理重建",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/guide/topics/manifest/activity-element",
                order = 2
            ),
            ChecklistItem(
                id = "ck_split_screen",
                category = "SplitScreen",
                title = "分屏模式支持检测",
                description = "App 需支持 split-screen 模式，确保 UI 在分屏场景下不会崩溃",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/guide/topics/manifest/activity-element#resizeableActivity",
                order = 3
            ),
            ChecklistItem(
                id = "ck_resizeable_true",
                category = "MultiWindow",
                title = "resizeableActivity=true 或未声明",
                description = "Android 17 targeting API 37+ 的 App 必须支持多窗口模式",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/guide/topics/manifest/activity-element#resizeableActivity",
                order = 4
            ),
            ChecklistItem(
                id = "ck_sw600dp_layouts",
                category = "Layout",
                title = "sw=600dp 布局资源已提供",
                description = "确保 res/layout-sw600dp/ 目录下有平板适配的布局文件",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/training/multiscreen/screensizes",
                order = 5
            ),
            ChecklistItem(
                id = "ck_sw840dp_layouts",
                category = "Layout",
                title = "sw=840dp 布局资源已提供",
                description = "确保 res/layout-sw840dp/ 目录下有大屏平板适配的布局文件",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/training/multiscreen/screensizes",
                order = 6
            ),
            ChecklistItem(
                id = "ck_freeform_support",
                category = "Freeform",
                title = "自由窗口模式支持",
                description = "Android 17+ 可能支持自由窗口，App 需处理 min/max/reset 场景",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/reference/android/view/WindowManager.LayoutParams#FLAG_RESIZE_MODE",
                order = 7
            ),
            ChecklistItem(
                id = "ck_aspect_ratio",
                category = "Display",
                title = "最大宽高比已正确配置",
                description = "android:maxAspectRatio 应设为较大值（如 2.4）或移除该声明",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/guide/topics/manifest/activity-element#maxAspectRatio",
                order = 8
            ),
            ChecklistItem(
                id = "ck_target_sdk_37",
                category = "SDK",
                title = "targetSdk >= 37 或已规划升级路径",
                description = "Android 17 要求 targeting API 37+，需提前规划升级",
                status = CheckStatus.UNCHECKED,
                referenceDoc = "https://developer.android.com/about/versions/17",
                order = 9
            )
        )

        /** Sample Android API changes for Continuous Canary workflow */
        private val sampleAPIChanges = listOf(
            APIChange(
                id = "api_37_001",
                version = 37,
                category = "Windowing",
                title = "大屏强制适配 (resizeableActivity)",
                description = "所有 targeting API 37+ 且 sw>=600dp 的 App 必须支持多窗口模式",
                isBreakingChange = true,
                migrationGuide = "https://developer.android.com/about/versions/17#large-screen"
            ),
            APIChange(
                id = "api_37_002",
                version = 37,
                category = "Permissions",
                title = "后台位置权限更严格",
                description = "ACCESS_BACKGROUND_LOCATION 需要额外的前台权限链",
                isBreakingChange = false,
                migrationGuide = "https://developer.android.com/about/versions/17#background-location"
            ),
            APIChange(
                id = "api_36_001",
                version = 36,
                category = "Health",
                title = "BODY_SENSORS 细粒度化",
                description = "BODY_SENSORS 拆分为 6 个细粒度权限（心率/血糖/血压等）",
                isBreakingChange = true,
                migrationGuide = "https://developer.android.com/about/versions/16#health-permissions"
            ),
            APIChange(
                id = "api_37_003",
                version = 37,
                category = "Battery",
                title = "AlarmListener 电池优化",
                description = "OnAlarmListener 受到更严格的后台调度限制",
                isBreakingChange = false,
                migrationGuide = "https://developer.android.com/about/versions/17#alarm-scheduling"
            )
        )
    }

    init {
        // Initialize checklist items with default foldable/freeform checks
        _state.update { it.copy(checklistItems = defaultChecklistItems) }
        // Initialize with some sample API changes
        _state.update { it.copy(apiChanges = sampleAPIChanges) }
    }

    // ─────────────────────────────────────────────────────────────
    // sendIntent — Entry point for all user intents
    // ─────────────────────────────────────────────────────────────
    fun sendIntent(intent: LargeScreenIntent) {
        when (intent) {
            is LargeScreenIntent.SelectTool -> handleSelectTool(intent.tool)
            is LargeScreenIntent.SelectSubTab -> handleSelectSubTab(intent.subTab)
            is LargeScreenIntent.RunComplianceCheck -> handleRunComplianceCheck(intent.target)
            is LargeScreenIntent.CaptureScreens -> handleCaptureScreens(intent.windowSizes)
            is LargeScreenIntent.GenerateFixDiff -> handleGenerateFixDiff(intent.violation)
            is LargeScreenIntent.UpdateChecklistItem -> handleUpdateChecklistItem(intent.item, intent.status)
            is LargeScreenIntent.RunPlayStorePreCheck -> handleRunPlayStorePreCheck(intent.apkPath)
            is LargeScreenIntent.SubscribeAPIChanges -> handleSubscribeAPIChanges(intent.androidVersions)
            is LargeScreenIntent.ExportReport -> handleExportReport(intent.format)
            is LargeScreenIntent.ClearError -> handleClearError()
            is LargeScreenIntent.DismissViolationDetail -> handleDismissViolationDetail()
            is LargeScreenIntent.SelectViolation -> handleSelectViolation(intent.violation)
            is LargeScreenIntent.PreviewWindowSize -> { /* handled in UI */ }
            is LargeScreenIntent.UpdateCheckTarget -> handleUpdateCheckTarget(intent.path)
            is LargeScreenIntent.AddMigrationTask -> handleAddMigrationTask(intent.task)
            is LargeScreenIntent.UpdateMigrationTaskStatus -> handleUpdateMigrationTaskStatus(intent.taskId, intent.status)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Intent Handlers
    // ─────────────────────────────────────────────────────────────

    private fun handleSelectTool(tool: LargeScreenTool) {
        _state.update { it.copy(currentTool = tool, activeSubTab = LargeScreenSubTab.OVERVIEW) }
    }

    private fun handleSelectSubTab(subTab: LargeScreenSubTab) {
        _state.update { it.copy(activeSubTab = subTab) }
    }

    private fun handleSelectViolation(violation: ComplianceViolation) {
        _state.update { it.copy(selectedViolation = violation) }
    }

    private fun handleDismissViolationDetail() {
        _state.update { it.copy(selectedViolation = null) }
    }

    private fun handleUpdateCheckTarget(path: String) {
        _state.update { it.copy(checkTargetPath = path) }
    }

    private fun handleClearError() {
        _state.update { it.copy(error = null) }
    }

    /**
     * Run compliance check simulation.
     * Real implementation would analyze AndroidManifest.xml and build.gradle
     * for resizeableActivity, configChanges, targetSdk settings.
     */
    private fun handleRunComplianceCheck(target: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, checkTargetPath = target) }
            delay(1500) // Simulate scanning

            // Simulate compliance violations based on target
            val violations = if (target.isNotBlank()) {
                listOf(
                    ComplianceViolation(
                        id = UUID.randomUUID().toString(),
                        file = "$target/app/src/main/AndroidManifest.xml",
                        line = 24,
                        config = "android:resizeableActivity=\"false\"",
                        issue = "App targeting API 37+ 且屏幕宽度 ≥ 600dp，必须支持多窗口模式",
                        fixSuggestion = "删除 android:resizeableActivity=\"false\" 或将其设为 true",
                        severity = ViolationSeverity.ERROR
                    ),
                    ComplianceViolation(
                        id = UUID.randomUUID().toString(),
                        file = "$target/app/src/main/AndroidManifest.xml",
                        line = 24,
                        config = "android:configChanges=\"orientation|screenSize\"",
                        issue = "声明 configChanges 会阻止系统重建 Activity，大屏适配时应避免",
                        fixSuggestion = "移除 orientation|screenSize，依赖系统自动处理配置变更",
                        severity = ViolationSeverity.WARNING
                    ),
                    ComplianceViolation(
                        id = UUID.randomUUID().toString(),
                        file = "$target/app/build.gradle.kts",
                        line = 12,
                        config = "targetSdk = 35",
                        issue = "targetSdk = 35 即将过期，建议升级到 36 并完成大屏适配",
                        fixSuggestion = "升级到 targetSdk = 36，运行大屏适配检测并修复所有违规项",
                        severity = ViolationSeverity.INFO
                    )
                )
            } else {
                emptyList()
            }

            _state.update { it.copy(complianceViolations = violations, isLoading = false) }
            _effect.emit(LargeScreenEffect.ShowSnackbar("检测完成，发现 ${violations.size} 个问题"))
        }
    }

    /**
     * Simulate multi-window screenshot test.
     * Real implementation would use UIAutomator or screenshot automation.
     */
    private fun handleCaptureScreens(windowSizes: List<WindowSize>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, selectedWindowSizes = windowSizes) }
            delay(2000) // Simulate capture

            val results = windowSizes.associateWith { size ->
                ScreenCaptureResult(
                    windowSize = size,
                    capturePath = "/mock/capture_${size.widthDp}x${size.heightDp}.png",
                    status = when {
                        size.type == WindowType.PHONE -> ScreenCaptureStatus.PASS
                        size.type == WindowType.TABLET && size.widthDp >= 840 -> ScreenCaptureStatus.FAIL
                        else -> ScreenCaptureStatus.PASS
                    },
                    diffRegions = if (size.type == WindowType.TABLET && size.widthDp >= 840) {
                        listOf(
                            DiffRegion(
                                regionId = "content_overflow",
                                description = "内容溢出：右侧边栏在 sw≥840dp 时超出边界",
                                severity = ViolationSeverity.WARNING
                            )
                        )
                    } else emptyList(),
                    errorMessage = if (size.type == WindowType.TABLET && size.widthDp >= 840) {
                        "LayoutConstraint: RIGHT sidebar overflows by 48dp at sw=840dp"
                    } else null
                )
            }

            _state.update { it.copy(screenCaptures = results, isLoading = false) }
            _effect.emit(LargeScreenEffect.ShowSnackbar("截图测试完成"))
        }
    }

    /**
     * Generate fix diff for a compliance violation.
     * Real implementation would generate precise AndroidManifest.xml diffs.
     */
    private fun handleGenerateFixDiff(violation: ComplianceViolation) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(800) // Simulate diff generation

            val diff = buildString {
                appendLine("--- a/${violation.file}")
                appendLine("+++ b/${violation.file}")
                appendLine("@@ -${violation.line},${violation.line} @@")
                when {
                    violation.config.contains("resizeableActivity") -> {
                        appendLine("-    android:resizeableActivity=\"false\"")
                        appendLine("+    <!-- android:resizeableActivity removed for Android 17 compliance -->")
                    }
                    violation.config.contains("configChanges") -> {
                        appendLine("-    android:configChanges=\"orientation|screenSize\"")
                        appendLine("+    android:configChanges=\"keyboard|keyboardHidden\"")
                    }
                    else -> {
                        appendLine("+    // TODO: Fix for Android 17 large screen compliance")
                        appendLine("+    ${violation.fixSuggestion}")
                    }
                }
            }

            _state.update { it.copy(generatedDiff = diff, isLoading = false) }
            _effect.emit(LargeScreenEffect.DiffGenerated(diff))
        }
    }

    private fun handleUpdateChecklistItem(item: ChecklistItem, status: CheckStatus) {
        _state.update { state ->
            state.copy(
                checklistItems = state.checklistItems.map {
                    if (it.id == item.id) it.copy(status = status) else it
                }
            )
        }
    }

    private fun handleRunPlayStorePreCheck(apkPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            delay(2000) // Simulate Play Store review simulation

            val result = PlayStorePreCheckResult(
                willBeRejected = true,
                riskLevel = RiskLevel.HIGH,
                rejectionReasons = listOf(
                    "App 声明 android:resizeableActivity=\"false\"，不符合大屏强制适配要求",
                    "targetSdk=35 即将在 Play Store 强制要求后不满足最低标准",
                    "未检测到 sw=600dp 及 sw=840dp 的窗口尺寸测试数据"
                ),
                warnings = listOf(
                    "建议添加 Android 17 兼容性声明",
                    "建议添加 Jetpack WindowManager 1.3+ 依赖"
                ),
                passedChecks = listOf(
                    "targetSdk >= 24 (最低版本要求)",
                    "非游戏类应用，无需额外屏幕宽高比声明"
                ),
                checkedAt = System.currentTimeMillis()
            )

            _state.update { it.copy(preCheckResult = result, isLoading = false) }
            _effect.emit(LargeScreenEffect.PlayStoreRejectionRisk(result.riskLevel, result.rejectionReasons))
        }
    }

    private fun handleSubscribeAPIChanges(androidVersions: List<Int>) {
        _state.update { state ->
            state.copy(
                apiChanges = state.apiChanges.map { change ->
                    if (change.version in androidVersions) change.copy(isSubscribed = true)
                    else change
                }
            )
        }
        viewModelScope.launch {
            _effect.emit(LargeScreenEffect.ShowSnackbar("已订阅 Android ${androidVersions.joinToString(", ")} API 变更通知"))
        }
    }

    private fun handleExportReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000) // Simulate export

            val filePath = "/mock/compliance_report_${System.currentTimeMillis()}.${format.extension}"
            _state.update { it.copy(exportedFilePath = filePath, isLoading = false) }
            _effect.emit(LargeScreenEffect.ReportExported(filePath))
        }
    }

    private fun handleAddMigrationTask(task: MigrationTask) {
        _state.update { state ->
            state.copy(migrationTasks = state.migrationTasks + task)
        }
    }

    private fun handleUpdateMigrationTaskStatus(taskId: String, status: MigrationTaskStatus) {
        _state.update { state ->
            state.copy(
                migrationTasks = state.migrationTasks.map { task ->
                    if (task.id == taskId) task.copy(status = status, updatedAt = System.currentTimeMillis())
                    else task
                }
            )
        }
    }
}
