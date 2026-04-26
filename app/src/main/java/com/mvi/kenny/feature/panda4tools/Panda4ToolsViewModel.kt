package com.mvi.kenny.feature.panda4tools

// ================================================================
// Panda4ToolsViewModel — Panda 4 AI Agent 开发工具包 ViewModel
// ================================================================
// MVI architecture ViewModel for Panda 4 AI Agent Toolkit.
//
// PRD-158: Android Studio Panda 4 AI Agent 开发工具包
// Design Reference: memory/agency/designs/PRD-158-Android-Studio-Panda-4-AI-Agent-开发工具包.md
//
// 八大工具模块：
// 1. Planning Mode 计划评审
// 2. NEP 链式编辑验证
// 3. Planning Git 集成
// 4. Planning 审计日志
// 5. NEP 代码审查联动
// 6. Gemini API Starter CLI
// 7. Google One 配额监控
// 8. Panda 4 vs Panda 3 对比
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * ============================================================
 * Panda4ToolsViewModel — Panda 4 工具包 ViewModel
 * ================================================================
 * MVI architecture ViewModel for Panda 4 AI Agent Toolkit.
 *
 * Key behaviors:
 * 1. Manages tool module selection and switching
 * 2. Planning Mode review with complexity and risk analysis
 * 3. NEP sequence verification with syntax and reference checking
 * 4. Git integration for planning commits
 * 5. Audit log management and export
 * 6. Code review request generation from NEP sequences
 * 7. Gemini API Starter CLI execution
 * 8. Google One quota monitoring
 * 9. Panda 4 vs Panda 3 feature comparison
 * —————————————————————————————————————————————————————
 */
class Panda4ToolsViewModel : ViewModel() {

    // MVI State — single source of truth using StateFlow
    private val _state = MutableStateFlow(Panda4ToolsState())
    val state: StateFlow<Panda4ToolsState> = _state.asStateFlow()

    // MVI Effect — one-time side effects via Channel
    private val _effect = Channel<Panda4ToolsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Date formatter for timestamps
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * ============================================================
     * sendIntent — 处理用户意图
     * ================================================================
     * Entry point for all user intents. Maps Intent → Business Logic.
     *
     * @param intent User intent from the UI layer
     */
    fun sendIntent(intent: Panda4ToolsIntent) {
        when (intent) {
            // Module Selection / 模块选择
            is Panda4ToolsIntent.SelectModule -> selectModule(intent.module)

            // Planning Review / 计划评审
            is Panda4ToolsIntent.StartReview -> startReview(intent.planFilePath)
            is Panda4ToolsIntent.ClearReview -> clearReview()

            // NEP Verify / NEP 验证
            is Panda4ToolsIntent.LoadNepSequence -> loadNepSequence(intent.sequences)
            is Panda4ToolsIntent.StartVerification -> startVerification()
            is Panda4ToolsIntent.ClearVerification -> clearVerification()

            // Planning Git / Git 集成
            is Panda4ToolsIntent.ExecuteGitIntegration -> executeGitIntegration(intent.planFilePath, intent.branchName)

            // Planning Audit / 审计日志
            is Panda4ToolsIntent.LoadAuditLog -> loadAuditLog(intent.entries)
            is Panda4ToolsIntent.ExportAuditLog -> exportAuditLog(intent.format)

            // NEP Review / NEP 代码审查
            is Panda4ToolsIntent.CreateCodeReview -> createCodeReview(intent.sequences, intent.targetBranch)

            // Gemini Starter / Gemini CLI
            is Panda4ToolsIntent.StartGeminiCli -> startGeminiCli(intent.apiKey, intent.promptTemplate)

            // Quota Monitor / 配额监控
            is Panda4ToolsIntent.FetchQuota -> fetchQuota(intent.email)

            // Panda Compare / 版本对比
            is Panda4ToolsIntent.LoadFeatureComparison -> loadFeatureComparison()
        }
    }

    // ================================================================
    // Module Selection / 模块选择
    // ================================================================

    /**
     * Select a tool module.
     */
    private fun selectModule(module: ToolModule) {
        _state.value = _state.value.copy(selectedModule = module)
    }

    // ================================================================
    // Planning Review / 计划评审
    // ================================================================

    /**
     * Start a planning review.
     * Simulates async review process with mock data.
     */
    private fun startReview(planFilePath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                planFilePath = planFilePath,
                reviewStatus = ReviewStatus.Reviewing
            )

            // Simulate review process / 模拟评审过程
            delay(2000)

            // Generate mock review report / 生成模拟评审报告
            val report = PlanReview(
                complexityScore = Random.nextInt(4, 9),
                risks = listOf(
                    RiskPoint(
                        severity = "medium",
                        location = "app/src/main/java/com/example/MainActivity.kt",
                        description = "文件修改涉及 UI 层，可能影响现有布局",
                        suggestion = "建议在修改前创建备份分支"
                    ),
                    RiskPoint(
                        severity = "high",
                        location = "app/build.gradle.kts",
                        description = "依赖升级可能引入版本兼容性问题",
                        suggestion = "先在测试环境验证依赖兼容性"
                    )
                ),
                missedDependencies = listOf(
                    "ViewModel 依赖未声明",
                    "Lifecycle 运行时依赖缺失"
                ),
                testabilityScore = TestabilityScore(
                    score = Random.nextInt(5, 9),
                    analysis = "计划步骤清晰，模块间依赖明确，具备较好的可测试性基础",
                    missingTests = listOf(
                        "ViewModel 单元测试",
                        "Repository 集成测试"
                    )
                ),
                overallRecommendation = if (Random.nextBoolean()) "Approved" else "Needs Revision",
                generatedAt = dateFormat.format(Date())
            )

            _state.value = _state.value.copy(
                reviewStatus = ReviewStatus.Reviewed,
                reviewReport = report
            )

            _effect.send(Panda4ToolsEffect.ShowSuccess("评审完成"))
        }
    }

    /**
     * Clear review results.
     */
    private fun clearReview() {
        _state.value = _state.value.copy(
            planFilePath = "",
            reviewStatus = ReviewStatus.Idle,
            reviewReport = null
        )
    }

    // ================================================================
    // NEP Verify / NEP 验证
    // ================================================================

    /**
     * Load NEP prediction sequences.
     */
    private fun loadNepSequence(sequences: List<NepSequence>) {
        _state.value = _state.value.copy(
            nepSequences = sequences,
            verifyReport = null
        )
    }

    /**
     * Start NEP sequence verification.
     * Simulates verification process with syntax and reference checking.
     */
    private fun startVerification() {
        viewModelScope.launch {
            _state.value = _state.value.copy(verifyStatus = VerifyStatus.Verifying)

            // Simulate verification process / 模拟验证过程
            delay(2500)

            val sequences = _state.value.nepSequences
            val passedCount = sequences.size - Random.nextInt(0, 2)
            val hasErrors = passedCount < sequences.size

            val errors = if (hasErrors) {
                listOf(
                    VerifyError(
                        stepNumber = passedCount + 1,
                        errorType = "SyntaxError",
                        message = "缺少分号或括号不匹配",
                        filePath = sequences.getOrNull(passedCount)?.filePath ?: "unknown"
                    )
                )
            } else {
                emptyList()
            }

            val report = VerifyReport(
                overallStatus = if (hasErrors) VerifyStatus.Failed else VerifyStatus.Passed,
                passedSteps = passedCount,
                failedSteps = sequences.size - passedCount,
                errors = errors,
                warnings = listOf(
                    "建议为新增方法添加 @Composable 注解",
                    "Repository 方法建议添加异常处理"
                ),
                isCompilable = !hasErrors
            )

            _state.value = _state.value.copy(
                verifyStatus = if (hasErrors) VerifyStatus.Failed else VerifyStatus.Passed,
                verifyReport = report
            )

            if (hasErrors) {
                _effect.send(Panda4ToolsEffect.ShowError("验证发现 ${errors.size} 个错误"))
            } else {
                _effect.send(Panda4ToolsEffect.ShowSuccess("验证通过"))
            }
        }
    }

    /**
     * Clear verification results.
     */
    private fun clearVerification() {
        _state.value = _state.value.copy(
            nepSequences = emptyList(),
            verifyStatus = VerifyStatus.Idle,
            verifyReport = null
        )
    }

    // ================================================================
    // Planning Git / Git 集成
    // ================================================================

    /**
     * Execute Git integration.
     * Simulates branch creation and commit.
     */
    private fun executeGitIntegration(planFilePath: String, branchName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Simulate Git operation / 模拟 Git 操作
            delay(1500)

            val generatedBranch = if (branchName.isNotBlank()) {
                branchName
            } else {
                "feature/planning-${UUID.randomUUID().toString().take(8)}"
            }

            val integration = GitIntegration(
                success = Random.nextFloat() > 0.1f, // 90% success rate
                branchName = generatedBranch,
                commitHash = UUID.randomUUID().toString().take(7),
                planFilePath = planFilePath,
                message = if (Random.nextFloat() > 0.1f) {
                    "计划已提交到 $generatedBranch"
                } else {
                    "Git 操作失败，请检查分支名称是否合法"
                }
            )

            _state.value = _state.value.copy(
                isLoading = false,
                gitIntegration = integration
            )

            if (integration.success) {
                _effect.send(Panda4ToolsEffect.ShowSuccess("Git 集成成功"))
            } else {
                _effect.send(Panda4ToolsEffect.ShowError("Git 集成失败"))
            }
        }
    }

    // ================================================================
    // Planning Audit / 审计日志
    // ================================================================

    /**
     * Load audit log entries.
     */
    private fun loadAuditLog(entries: List<AuditEntry>) {
        _state.value = _state.value.copy(auditLog = entries)
    }

    /**
     * Export audit log.
     * Simulates export process.
     */
    private fun exportAuditLog(format: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExportingAudit = true)

            // Simulate export process / 模拟导出过程
            delay(1000)

            val exportPath = "./audit-log-${System.currentTimeMillis()}.$format"

            _state.value = _state.value.copy(isExportingAudit = false)

            _effect.send(Panda4ToolsEffect.ExportCompleted(format, exportPath))
        }
    }

    // ================================================================
    // NEP Review / NEP 代码审查
    // ================================================================

    /**
     * Create code review from NEP sequences.
     * Simulates review request creation.
     */
    private fun createCodeReview(sequences: List<NepSequence>, targetBranch: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreatingReview = true)

            // Simulate review creation / 模拟审查创建
            delay(2000)

            val request = CodeReviewRequest(
                id = UUID.randomUUID().toString().take(8),
                title = "NEP 代码审查请求 - ${dateFormat.format(Date())}",
                targetBranch = targetBranch,
                changes = sequences.map { "${it.operation}: ${it.filePath}" },
                reviewUrl = "https://github.com/example/pull/${Random.nextInt(100, 999)}"
            )

            _state.value = _state.value.copy(
                isCreatingReview = false,
                codeReviewRequest = request
            )

            _effect.send(Panda4ToolsEffect.ReviewCreated(request.reviewUrl))
        }
    }

    // ================================================================
    // Gemini Starter / Gemini CLI
    // ================================================================

    /**
     * Start Gemini CLI.
     * Simulates API call execution.
     */
    private fun startGeminiCli(apiKey: String, promptTemplate: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRunningCli = true)

            // Simulate CLI execution / 模拟 CLI 执行
            delay(3000)

            val response = GeminiResponse(
                content = """
                    # Gemini API 响应

                    根据提供的代码片段，这是一个标准的 Android ViewModel 实现。

                    **代码分析：**
                    - 使用了 Kotlin 的 `StateFlow` 进行状态管理
                    - 遵循了 MVVM 架构模式
                    - 建议添加错误处理机制

                    **改进建议：**
                    1. 添加 `catch` 块处理可能的异常
                    2. 考虑使用 `sealed class` 管理 UI 状态
                    3. 添加单元测试覆盖关键业务逻辑
                """.trimIndent(),
                usage = TokenUsage(
                    promptTokens = Random.nextInt(100, 500),
                    completionTokens = Random.nextInt(200, 800),
                    totalTokens = Random.nextInt(300, 1300)
                )
            )

            _state.value = _state.value.copy(
                isRunningCli = false,
                geminiResponse = response
            )

            _effect.send(Panda4ToolsEffect.ShowSuccess("Gemini CLI 执行完成"))
        }
    }

    // ================================================================
    // Quota Monitor / 配额监控
    // ================================================================

    /**
     * Fetch quota information.
     * Simulates Google One API quota fetch.
     */
    private fun fetchQuota(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isFetchingQuota = true)

            // Simulate API fetch / 模拟 API 获取
            delay(1500)

            val dailyUsed = Random.nextLong(500_000, 1_500_000)
            val dailyLimit = 1_500_000L
            val dailyLevel = when {
                dailyUsed >= dailyLimit -> QuotaLevel.Exhausted
                dailyUsed >= dailyLimit * 0.9 -> QuotaLevel.Critical
                dailyUsed >= dailyLimit * 0.7 -> QuotaLevel.Warning
                else -> QuotaLevel.Normal
            }

            val monthlyUsed = Random.nextLong(5_000_000, 20_000_000)
            val monthlyLimit = 200_000_000L
            val monthlyLevel = when {
                monthlyUsed >= monthlyLimit -> QuotaLevel.Exhausted
                monthlyUsed >= monthlyLimit * 0.9 -> QuotaLevel.Critical
                monthlyUsed >= monthlyLimit * 0.7 -> QuotaLevel.Warning
                else -> QuotaLevel.Normal
            }

            val rpmUsed = Random.nextLong(30, 80)
            val rpmLimit = 60L
            val rpmLevel = when {
                rpmUsed >= rpmLimit -> QuotaLevel.Exhausted
                rpmUsed >= rpmLimit * 0.9 -> QuotaLevel.Critical
                rpmUsed >= rpmLimit * 0.7 -> QuotaLevel.Warning
                else -> QuotaLevel.Normal
            }

            val alerts = mutableListOf<String>()
            if (dailyLevel == QuotaLevel.Warning || dailyLevel == QuotaLevel.Critical) {
                alerts.add("日配额使用已达 ${String.format("%.1f", dailyUsed.toFloat() / dailyLimit * 100)}%")
            }
            if (rpmLevel == QuotaLevel.Warning || rpmLevel == QuotaLevel.Critical) {
                alerts.add("请求速率接近限额，当前 ${rpmUsed}/min")
            }

            val quotaInfo = QuotaInfo(
                email = email,
                quotaType = "Gemini 1.5 Pro",
                dailyUsed = dailyUsed,
                dailyLimit = dailyLimit,
                dailyLevel = dailyLevel,
                monthlyUsed = monthlyUsed,
                monthlyLimit = monthlyLimit,
                monthlyLevel = monthlyLevel,
                rpmUsed = rpmUsed,
                rpmLimit = rpmLimit,
                rpmLevel = rpmLevel,
                alerts = alerts
            )

            _state.value = _state.value.copy(
                isFetchingQuota = false,
                quotaInfo = quotaInfo
            )

            if (alerts.isNotEmpty()) {
                _effect.send(Panda4ToolsEffect.ShowError("检测到 ${alerts.size} 个配额告警"))
            } else {
                _effect.send(Panda4ToolsEffect.ShowSuccess("配额查询完成"))
            }
        }
    }

    // ================================================================
    // Panda Compare / 版本对比
    // ================================================================

    /**
     * Load feature comparison data.
     * Populates Panda 4 vs Panda 3 feature comparison.
     */
    private fun loadFeatureComparison() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingComparison = true)

            // Simulate data load / 模拟数据加载
            delay(1000)

            val comparisons = listOf(
                FeatureComparison(
                    featureName = "Planning Mode",
                    panda4Description = "内置计划生成与评审，Agent 执行前先展示计划",
                    panda3Description = "Agent 直接执行，无计划评审环节",
                    isNewInPanda4 = true,
                    improvementPercentage = "全新功能"
                ),
                FeatureComparison(
                    featureName = "NEP (Next Edit Prediction)",
                    panda4Description = "多步编辑流预测，自动推理下一步编辑",
                    panda3Description = "单步补全，仅预测下一个 token",
                    isNewInPanda4 = true,
                    improvementPercentage = "3x 编辑效率"
                ),
                FeatureComparison(
                    featureName = "代码审查集成",
                    panda4Description = "AI 辅助代码审查，自动生成审查意见",
                    panda3Description = "基础语法检查，无智能审查",
                    isNewInPanda4 = true,
                    improvementPercentage = "50% 审查时间"
                ),
                FeatureComparison(
                    featureName = "上下文窗口",
                    panda4Description = "2M token 超大上下文",
                    panda3Description = "128K token 上下文",
                    isNewInPanda4 = false,
                    improvementPercentage = "15x 上下文提升"
                ),
                FeatureComparison(
                    featureName = "构建速度",
                    panda4Description = "增量编译优化，清洁构建提速 40%",
                    panda3Description = "标准 Gradle 编译",
                    isNewInPanda4 = false,
                    improvementPercentage = "40% 构建加速"
                ),
                FeatureComparison(
                    featureName = "AI 模型",
                    panda4Description = "Gemini 2.5 Pro + 轻量级本地模型",
                    panda3Description = "Gemini 1.5",
                    isNewInPanda4 = false,
                    improvementPercentage = "新一代模型"
                )
            )

            _state.value = _state.value.copy(
                isLoadingComparison = false,
                featureComparison = comparisons
            )

            _effect.send(Panda4ToolsEffect.ShowSuccess("对比数据加载完成"))
        }
    }
}
