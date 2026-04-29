package com.mvi.kenny.feature.deliqueue

// ================================================================
// DeliQueueViewModel — Android 17 DeliQueue 迁移检测工具包 ViewModel
// ================================================================
// MVI ViewModel for Android 17 lock-free MessageQueue migration toolkit.
//
// PRD-198: Android 17 DeliQueue（Lock-free MessageQueue）迁移检测工具包
// Design Reference: memory/agency/designs/PRD-198-Android-17-DeliQueue-迁移检测工具包.md
//
// The ViewModel processes DeliQueueIntent and updates DeliQueueState.
// It simulates a scan using static analysis patterns (no actual Gradle task execution
// in this demo implementation — production would use KSP/ksp for AST parsing).
//
// DeliQueue (API 37+) breaks all reflection access to MessageQueue private fields:
//   - mMessages, mQuota, mBlocking, mThread, etc.
// Alternatives:
//   - mMessages → Looper.myQueue() (public API)
//   - mThread → Looper.getThread()
//   - mBlocking → Observe MessageQueue#quit() behavior
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

// ================================================================
// DeliQueueViewModel — Main ViewModel
// ================================================================
/**
 * ============================================================
 * DeliQueueViewModel — DeliQueue 工具 ViewModel
 * ============================================================
 * Processes user intents and updates state accordingly.
 *
 * The scan simulation generates realistic findings based on common
 * MessageQueue reflection patterns found in Android projects.
 */
class DeliQueueViewModel : ViewModel() {

    // ============================================================
    // State — MVI State (single source of truth)
    // ============================================================
    private val _state = MutableStateFlow(DeliQueueState())
    val state: StateFlow<DeliQueueState> = _state.asStateFlow()

    // ============================================================
    // Effect Channel — One-time side effects
    // ============================================================
    private val _effect = Channel<DeliQueueEffect>(Channel.BUFFERED)
    val effect: Flow<DeliQueueEffect> = _effect.receiveAsFlow()

    // ============================================================
    // Date format for scan logs
    // ============================================================
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    // ============================================================
    // sendIntent — Process user intent
    // ============================================================
    /**
     * Process user intent and update state accordingly.
     * 这是 MVI 的入口点 — 所有用户意图都通过这个方法处理。
     *
     * @param intent The user intent to process
     */
    fun sendIntent(intent: DeliQueueIntent) {
        when (intent) {
            is DeliQueueIntent.SetProjectPath -> setProjectPath(intent.path)
            is DeliQueueIntent.StartScan -> startScan()
            is DeliQueueIntent.CancelScan -> cancelScan()
            is DeliQueueIntent.ClearResults -> clearResults()
            is DeliQueueIntent.SwitchTab -> switchTab(intent.tabIndex)
            is DeliQueueIntent.SetImpactDimension -> setImpactDimension(intent.dimension)
            is DeliQueueIntent.SelectFinding -> selectFinding(intent.finding)
            is DeliQueueIntent.ClearFindingSelection -> clearFindingSelection()
            is DeliQueueIntent.NextStep -> nextStep()
            is DeliQueueIntent.PreviousStep -> previousStep()
            is DeliQueueIntent.ApplyFix -> applyFix(intent.finding)
            is DeliQueueIntent.RunCIVerification -> runCIVerification()
            is DeliQueueIntent.CopyToClipboard -> copyToClipboard(intent.text)
        }
    }

    // ============================================================
    // Intent Handlers — 具体意图处理方法
    // ============================================================

    private fun setProjectPath(path: String) {
        _state.update { it.copy(projectPath = path) }
    }

    private fun switchTab(tabIndex: Int) {
        _state.update { it.copy(currentTab = tabIndex) }
    }

    private fun setImpactDimension(dimension: ImpactDimension) {
        _state.update { it.copy(impactDimension = dimension) }
    }

    private fun selectFinding(finding: RiskFinding) {
        _state.update {
            it.copy(
                migrationWizard = it.migrationWizard.copy(
                    selectedFinding = finding,
                    generatedDiff = generateDiff(finding),
                    currentStep = MigrationStep.IDENTIFY
                ),
                currentTab = 2  // Switch to Migration Wizard tab
            )
        }
    }

    private fun clearFindingSelection() {
        _state.update {
            it.copy(
                migrationWizard = MigrationWizardState()
            )
        }
    }

    private fun nextStep() {
        val currentStep = _state.value.migrationWizard.currentStep
        val nextStep = when (currentStep) {
            MigrationStep.IDENTIFY -> MigrationStep.EVALUATE
            MigrationStep.EVALUATE -> MigrationStep.FIX
            MigrationStep.FIX -> MigrationStep.VERIFY
            MigrationStep.VERIFY -> MigrationStep.VERIFY
        }
        _state.update {
            it.copy(migrationWizard = it.migrationWizard.copy(currentStep = nextStep))
        }
    }

    private fun previousStep() {
        val currentStep = _state.value.migrationWizard.currentStep
        val prevStep = when (currentStep) {
            MigrationStep.IDENTIFY -> MigrationStep.IDENTIFY
            MigrationStep.EVALUATE -> MigrationStep.IDENTIFY
            MigrationStep.FIX -> MigrationStep.EVALUATE
            MigrationStep.VERIFY -> MigrationStep.FIX
        }
        _state.update {
            it.copy(migrationWizard = it.migrationWizard.copy(currentStep = prevStep))
        }
    }

    private fun applyFix(finding: RiskFinding) {
        viewModelScope.launch {
            _state.update {
                it.copy(migrationWizard = it.migrationWizard.copy(isApplyingFix = true))
            }

            // Simulate fix application delay
            delay(1500)

            _state.update {
                it.copy(
                    migrationWizard = it.migrationWizard.copy(
                        isApplyingFix = false,
                        fixAppliedSuccessfully = true
                    )
                )
            }

            _effect.send(DeliQueueEffect.ShowToast("✅ 修复已应用！请在 Step 4 验证。"))
        }
    }

    private fun runCIVerification() {
        viewModelScope.launch {
            // Simulate CI verification
            delay(2000)

            val passed = _state.value.scanState.findings.isEmpty() ||
                    _state.value.migrationWizard.fixAppliedSuccessfully

            _state.update {
                it.copy(
                    migrationWizard = it.migrationWizard.copy(ciVerificationPassed = passed)
                )
            }

            _effect.send(
                DeliQueueEffect.CIVerificationResult(
                    passed = passed,
                    message = if (passed) {
                        "✅ CI 合规检测通过！所有 MessageQueue 反射用法已修复。"
                    } else {
                        "❌ CI 合规检测失败，仍有 ${_state.value.scanState.findings.size} 个未修复。"
                    }
                )
            )
        }
    }

    private fun copyToClipboard(text: String) {
        viewModelScope.launch {
            _effect.send(DeliQueueEffect.CopiedToClipboard(text))
            _effect.send(DeliQueueEffect.ShowToast("📋 已复制到剪贴板"))
        }
    }

    private fun cancelScan() {
        _state.update {
            it.copy(scanState = DeliQueueScanState())
        }
    }

    private fun clearResults() {
        _state.update {
            it.copy(
                scanState = DeliQueueScanState(),
                migrationWizard = MigrationWizardState()
            )
        }
    }

    // ============================================================
    // Scan Logic — 扫描逻辑
    // ============================================================
    private fun startScan() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val logs = mutableListOf<ScanLog>()

            // Initialize scan state
            _state.update {
                it.copy(
                    scanState = DeliQueueScanState(
                        phase = ScanPhase.PARSING_PROJECT,
                        progress = 0,
                        logs = emptyList(),
                        startTimeMs = startTime
                    )
                )
            }

            // Phase 1: Parsing project structure
            addLog(logs, LogLevel.INFO, "开始解析项目结构...")
            delay(400)
            _state.update { s -> s.copy(scanState = s.scanState.copy(progress = 10, logs = logs.toList())) }

            addLog(logs, LogLevel.INFO, "定位源码目录: app/src/main/java")
            delay(300)
            _state.update { s -> s.copy(scanState = s.scanState.copy(progress = 15, logs = logs.toList())) }

            // Phase 2: Scanning code for reflection patterns
            addLog(logs, LogLevel.INFO, "开始扫描代码中的 MessageQueue 反射调用...")
            _state.update { s -> s.copy(scanState = s.scanState.copy(phase = ScanPhase.SCANNING_CODE, progress = 20, logs = logs.toList())) }
            delay(500)

            // Simulate finding files with MessageQueue reflection
            val findings = mutableListOf<RiskFinding>()
            val scanFiles = listOf(
                Triple("com/mvi/kenny/util/MessageQueueHelper.kt", 42, "queue.javaClass.getDeclaredField(\"mMessages\")"),
                Triple("com/mvi/kenny/network/OkHttpInterceptor.kt", 78, "val mQueue = messageQueue.javaClass.getField(\"mMessages\")"),
                Triple("com/mvi/kenny/core/HandlerExtension.kt", 23, "val mBlock = msgQueue.getDeclaredField(\"mBlocking\")"),
                Triple("com/mvi/kenny/legacy/LegacyQueueManager.kt", 115, "val mThread = queue.javaClass.getDeclaredField(\"mThread\")"),
                Triple("com/mvi/kenny/legacy/LegacyQueueManager.kt", 203, "val field = queue.javaClass.getDeclaredField(\"mQuota\")"),
            )

            var fileCount = 0
            for ((file, line, snippet) in scanFiles) {
                fileCount++
                addLog(logs, LogLevel.WARN, "⚠️ 检测到 MessageQueue 反射: $file:$line")
                delay(200)
                val progress = 20 + (fileCount * 12)
                _state.update { s -> s.copy(scanState = s.scanState.copy(progress = progress.coerceAtMost(75), logs = logs.toList())) }

                val target = when {
                    snippet.contains("mMessages") -> ReflectionTarget.M_MESSAGES
                    snippet.contains("mBlocking") -> ReflectionTarget.M_BLOCKING
                    snippet.contains("mThread") -> ReflectionTarget.M_THREAD
                    snippet.contains("mQuota") -> ReflectionTarget.M_QUOTA
                    else -> ReflectionTarget.OTHER
                }
                val riskLevel = when (target) {
                    ReflectionTarget.M_MESSAGES -> RiskLevel.P0
                    ReflectionTarget.M_BLOCKING -> RiskLevel.P1
                    ReflectionTarget.M_THREAD -> RiskLevel.P1
                    ReflectionTarget.M_QUOTA -> RiskLevel.P2
                    ReflectionTarget.OTHER -> RiskLevel.P2
                }

                findings.add(
                    RiskFinding(
                        filePath = file,
                        lineNumber = line,
                        codeSnippet = snippet,
                        target = target,
                        riskLevel = riskLevel,
                        alternativeApi = target.alternativeApi,
                        alternativeCode = generateAlternativeCode(target)
                    )
                )
            }

            // Phase 3: Analyzing third-party dependencies
            addLog(logs, LogLevel.INFO, "开始分析第三方库依赖...")
            _state.update { s -> s.copy(scanState = s.scanState.copy(phase = ScanPhase.ANALYZING_DEPS, progress = 80, logs = logs.toList())) }
            delay(500)

            val libraryFindings = listOf(
                LibraryFinding(
                    libraryName = "com.squareup.okhttp3:okhttp",
                    version = "4.12.0",
                    affectedClasses = listOf("RealConnection", "Http2Connection"),
                    impactDescription = "OkHttp 使用 MessageQueue 反射进行连接管理，API 37+ 可能受影响",
                    recommendation = LibraryRecommendation.UPGRADE
                ),
                LibraryFinding(
                    libraryName = "io.reactivex.rxjava3:rxjava",
                    version = "3.1.8",
                    affectedClasses = listOf("HandlerScheduler", "MessageQueue"),
                    impactDescription = "RxJava HandlerScheduler 使用 MessageQueue 反射调度",
                    recommendation = LibraryRecommendation.MONITOR
                )
            )

            for (lib in libraryFindings) {
                addLog(logs, LogLevel.WARN, "⚠️ 第三方库受影响: ${lib.libraryName} (${lib.affectedClasses.size} 个类)")
                delay(200)
            }

            // Phase 4: Calculating impact
            addLog(logs, LogLevel.INFO, "计算影响范围...")
            _state.update { s -> s.copy(scanState = s.scanState.copy(phase = ScanPhase.CALCULATING_IMPACT, progress = 90, logs = logs.toList())) }
            delay(400)

            val p0 = findings.count { it.riskLevel == RiskLevel.P0 }
            val p1 = findings.count { it.riskLevel == RiskLevel.P1 }
            val p2 = findings.count { it.riskLevel == RiskLevel.P2 }

            if (p0 > 0) {
                addLog(logs, LogLevel.ERROR, "🔴 检测到 $p0 个 P0 运行时崩溃风险！")
            }
            if (p1 > 0) {
                addLog(logs, LogLevel.WARN, "🟡 检测到 $p1 个 P1 行为异常风险")
            }
            if (p2 > 0) {
                addLog(logs, LogLevel.INFO, "🟢 检测到 $p2 个 P2 潜在风险")
            }

            // Phase 5: Generating report
            addLog(logs, LogLevel.INFO, "生成扫描报告...")
            _state.update { s -> s.copy(scanState = s.scanState.copy(phase = ScanPhase.GENERATING_REPORT, progress = 95, logs = logs.toList())) }
            delay(300)

            val endTime = System.currentTimeMillis()
            addLog(logs, LogLevel.SUCCESS, "✅ 扫描完成！发现 ${findings.size} 个反射调用，${libraryFindings.size} 个受影响第三方库")
            addLog(logs, LogLevel.INFO, "📊 扫描耗时: ${endTime - startTime}ms")

            // Final state update
            _state.update {
                it.copy(
                    scanState = DeliQueueScanState(
                        phase = ScanPhase.COMPLETED,
                        progress = 100,
                        logs = logs.toList(),
                        findings = findings,
                        libraryFindings = libraryFindings,
                        scannedFilesCount = 128,
                        startTimeMs = startTime,
                        endTimeMs = endTime
                    )
                )
            }

            // Send completion effect
            _effect.send(
                DeliQueueEffect.ScanCompleted(
                    findingCount = findings.size,
                    libraryCount = libraryFindings.size,
                    durationMs = endTime - startTime
                )
            )
        }
    }

    // ============================================================
    // Helper Methods — 辅助方法
    // ============================================================

    private fun addLog(logs: MutableList<ScanLog>, level: LogLevel, message: String) {
        logs.add(ScanLog(timestamp = timeFormat.format(Date()), level = level, message = message))
    }

    /**
     * Generate code diff for a given finding.
     * 生成修复代码的 Diff 视图。
     */
    private fun generateDiff(finding: RiskFinding): String {
        return buildString {
            appendLine("```kotlin")
            appendLine("// ❌ BEFORE (${finding.filePath}:${finding.lineNumber})")
            appendLine(finding.codeSnippet)
            appendLine()
            appendLine("// ✅ AFTER")
            appendLine(finding.alternativeCode)
            appendLine("```")
        }
    }

    /**
     * Generate alternative code for a given reflection target.
     * 根据反射目标生成替代代码。
     */
    private fun generateAlternativeCode(target: ReflectionTarget): String {
        return when (target) {
            ReflectionTarget.M_MESSAGES -> """
// ❌ Before: 反射访问 mMessages（DeliQueue 后将崩溃）
val field = messageQueue.javaClass.getDeclaredField("mMessages")
field.isAccessible = true
val mMessages = field.get(messageQueue)

// ✅ After: 使用公开 API Looper.myQueue()
val queue = Looper.myQueue()  // 公开 API，完全兼容 DeliQueue
""".trimIndent()

            ReflectionTarget.M_BLOCKING -> """
// ❌ Before: 反射访问 mBlocking
val mBlockingField = messageQueue.javaClass.getDeclaredField("mBlocking")
mBlockingField.isAccessible = true
val isBlocking = mBlockingField.getBoolean(messageQueue)

// ✅ After: 通过行为观察判断阻塞特性
// MessageQueue 不提供直接公开 API 获取阻塞状态
// 建议：观察 quit() 行为或使用 Handler(Looper) 的回调机制
""".trimIndent()

            ReflectionTarget.M_THREAD -> """
// ❌ Before: 反射访问 mThread
val mThreadField = messageQueue.javaClass.getDeclaredField("mThread")
mThreadField.isAccessible = true
val thread = mThreadField.get(messageQueue) as Thread

// ✅ After: 使用 Looper.getThread()
val looper = Looper.myLooper()
val associatedThread = looper.thread  // 公开 API
""".trimIndent()

            ReflectionTarget.M_QUOTA -> """
// ❌ Before: 反射访问 mQuota
val mQuotaField = messageQueue.javaClass.getDeclaredField("mQuota")
mQuotaField.isAccessible = true
val quota = mQuotaField.getInt(messageQueue)

// ✅ After: 通过 MessageQueue.quit() 行为观察间接判断
// DeliQueue 移除了 mQuota，暂无直接替代公开 API
// 建议：移除对此字段的依赖，或通过功能测试验证行为
""".trimIndent()

            ReflectionTarget.OTHER -> """
// ❌ Before: 反射访问 MessageQueue 私有字段
val field = messageQueue.javaClass.getDeclaredField("fieldName")
field.isAccessible = true
val value = field.get(messageQueue)

// ✅ After: 使用公开 API 或移除该依赖
// 请参考 DeliQueue 迁移指南选择合适的替代方案
""".trimIndent()
        }
    }
}
