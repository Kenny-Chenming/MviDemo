package com.mvi.kenny.feature.compose111

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
 * Compose111ViewModel — Compose 1.11 破坏性变更检测 ViewModel
 * ============================================================
 * PRD-130 | Compose 1.11.0-rc01 破坏性变更检测与迁移工具包
 *
 * 职责：
 * 1. 管理 Compose111State（UI 状态唯一来源）
 * 2. 处理 Compose111Intent（用户意图）
 * 3. 触发 Compose111Effect（一次性副作用）
 *
 * 健康度计算权重：
 * - Text padding: 60%（影响最广）
 * - DrawLayer: 25%
 * - SwipeToReveal: 15%
 *
 * @see Compose111Contract
 * @see Compose111Screen
 */
class Compose111ViewModel : ViewModel() {

    private val _state = MutableStateFlow(Compose111State())
    val state: StateFlow<Compose111State> = _state.asStateFlow()

    private val _effect = Channel<Compose111Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 自动触发首次扫描 / Auto-trigger first scan on init
        processIntent(Compose111Intent.StartScan)
    }

    /**
     * 处理用户意图 / Process user intent
     *
     * @param intent 用户意图
     */
    fun processIntent(intent: Compose111Intent) {
        when (intent) {
            is Compose111Intent.StartScan -> startScan()
            is Compose111Intent.RefreshScan -> startScan()
            is Compose111Intent.FixTextPadding -> fixTextPadding(intent.index)
            is Compose111Intent.FixDrawLayer -> fixDrawLayer(intent.index)
            is Compose111Intent.MigrateSwipeToReveal -> migrateSwipeToReveal(intent.index)
            is Compose111Intent.GenerateReport -> generateReport()
            is Compose111Intent.GenerateCiConfig -> generateCiConfig()
            is Compose111Intent.SelectTab -> selectTab(intent.index)
            is Compose111Intent.SelectCiPlatform -> selectCiPlatform(intent.platform)
            is Compose111Intent.FilterBySeverity -> filterBySeverity(intent.severity)
            is Compose111Intent.TogglePreviewMode -> togglePreviewMode()
            is Compose111Intent.CopyToClipboard -> Unit // Handled in Screen
        }
    }

    // ================================================================
    // Scan Logic / 扫描逻辑
    // ================================================================

    /**
     * 启动全量扫描 / Start full scan
     *
     * 模拟 Gradle 插件静态分析：
     * - Text padding: 扫描 Text() 调用中的 padding/lineHeight 参数
     * - DrawLayer: 正则匹配 outlineShape / clipToOutline 字符串
     * - SwipeToReveal: 识别旧版 SwipeToReveal 的 XML/Compose 用法
     *
     * 由于是静态分析工具包演示，使用模拟数据 / Demo data for illustration
     */
    private fun startScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, scanProgress = 0f) }

            // 模拟扫描进度 / Simulate scan progress
            val steps = listOf(
                0.15f to "Scanning Text padding issues...",
                0.40f to "Scanning DrawLayer API usage...",
                0.65f to "Scanning SwipeToReveal usage...",
                0.85f to "Calculating health scores...",
                1.00f to "Scan complete"
            )

            for ((progress, _) in steps) {
                delay(300)
                _state.update { it.copy(scanProgress = progress) }
            }

            // 生成模拟扫描结果 / Generate demo scan results
            val textIssues = generateDemoTextPaddingIssues()
            val drawLayerIssues = generateDemoDrawLayerIssues()
            val swipeToRevealIssues = generateDemoSwipeToRevealIssues()

            // 计算健康度 / Calculate health scores
            val textScore = calculateTextScore(textIssues)
            val drawLayerScore = calculateDrawLayerScore(drawLayerIssues)
            val swipeScore = calculateSwipeScore(swipeToRevealIssues)

            // 综合健康度 = 加权平均 / Overall = weighted average
            val overall = textScore * 0.60f + drawLayerScore * 0.25f + swipeScore * 0.15f

            val critical = textIssues.count { it.severity == Compose111Severity.CRITICAL } +
                    drawLayerIssues.size +
                    swipeToRevealIssues.size
            val warning = textIssues.count { it.severity == Compose111Severity.WARNING }
            val pass = listOf(textIssues.isEmpty(), drawLayerIssues.isEmpty(), swipeToRevealIssues.isEmpty())
                .count { it }

            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    textPaddingIssues = textIssues,
                    drawLayerIssues = drawLayerIssues,
                    swipeToRevealIssues = swipeToRevealIssues,
                    textPaddingScore = textScore,
                    drawLayerScore = drawLayerScore,
                    swipeToRevealScore = swipeScore,
                    overallHealthScore = overall,
                    criticalCount = critical,
                    warningCount = warning,
                    passCount = pass
                )
            }

            _effect.send(Compose111Effect.ScanComplete)
        }
    }

    // ================================================================
    // Demo data generators / 模拟数据生成器
    // ================================================================

    /**
     * 生成演示用 Text Padding 问题列表
     * Generate demo Text padding issues
     *
     * 模拟真实场景中 Text extra padding 被移除后受影响的位置
     */
    private fun generateDemoTextPaddingIssues(): List<TextPaddingIssue> = listOf(
        TextPaddingIssue(
            filePath = "app/src/main/java/com/example/news/ui/components/ArticleText.kt",
            lineNumber = 42,
            codeSnippet = "Text(\n    text = content,\n    style = MaterialTheme.typography.bodyLarge\n)",
            severity = Compose111Severity.CRITICAL,
            suggestedFix = "使用 Modifier.heightIn(min = ... ) 恢复原有视觉高度，或调整 lineHeight"
        ),
        TextPaddingIssue(
            filePath = "app/src/main/java/com/example/chat/ui/MessageBubble.kt",
            lineNumber = 78,
            codeSnippet = "Text(\n    text = message.text,\n    modifier = Modifier.padding(vertical = 4.dp)\n)",
            severity = Compose111Severity.WARNING,
            suggestedFix = "额外 vertical padding 不受影响，但高度计算需重新验证"
        ),
        TextPaddingIssue(
            filePath = "app/src/main/java/com/example/reader/ui/PageView.kt",
            lineNumber = 115,
            codeSnippet = "Text(\n    text = pageContent,\n    lineHeight = 24.sp\n)",
            severity = Compose111Severity.CRITICAL,
            suggestedFix = "lineHeight 未变但 baseline 计算变了，建议用 Modifier.firstBaselineToTop() 对齐"
        ),
        TextPaddingIssue(
            filePath = "app/src/main/java/com/example/social/ui/PostCard.kt",
            lineNumber = 203,
            codeSnippet = "Text(\n    text = username,\n    style = MaterialTheme.typography.titleMedium\n)",
            severity = Compose111Severity.PASS,
            suggestedFix = "未使用 padding 相关参数，不受影响"
        )
    )

    /**
     * 生成演示用 DrawLayer API 问题列表
     * Generate demo DrawLayer API issues
     */
    private fun generateDemoDrawLayerIssues(): List<DrawLayerIssue> = listOf(
        DrawLayerIssue(
            filePath = "app/src/main/java/com/example/ui/Avatar.kt",
            lineNumber = 35,
            oldApi = "outlineShape",
            newApi = "shape",
            codeSnippet = "Modifier.drawBehind {\n    outlineShape(CircleShape)\n}"
        ),
        DrawLayerIssue(
            filePath = "app/src/main/java/com/example/ui/BackgroundCard.kt",
            lineNumber = 67,
            oldApi = "clipToOutline",
            newApi = "clip",
            codeSnippet = "Modifier.graphicsLayer {\n    clipToOutline = true\n}"
        ),
        DrawLayerIssue(
            filePath = "app/src/main/java/com/example/ui/ShadowBox.kt",
            lineNumber = 90,
            oldApi = "outlineShape",
            newApi = "shape",
            codeSnippet = "Modifier.drawWithContent {\n    outlineShape(RoundedCornerShape(8.dp))\n}"
        )
    )

    /**
     * 生成演示用 SwipeToReveal 问题列表
     * Generate demo SwipeToReveal issues
     */
    private fun generateDemoSwipeToRevealIssues(): List<SwipeToRevealIssue> = listOf(
        SwipeToRevealIssue(
            filePath = "app/src/main/java/com/example/ui/SwipeToDelete.kt",
            lineNumber = 44,
            oldUsage = "SwipeToDismiss(\n    state = state,\n    background = { ... },\n    dismissContent = { ... }\n)",
            newUsage = "SwipeToDismissBox(\n    state = state,\n    backgroundContent = { ... },\n    content = { ... }\n)",
            migrationStep = 1
        ),
        SwipeToRevealIssue(
            filePath = "app/src/main/java/com/example/ui/SwipeToReply.kt",
            lineNumber = 88,
            oldUsage = "SwipeToDismiss(\n    dismissDirection = DismissDirection.EndToStart\n)",
            newUsage = "SwipeToDismissBox(\n    swipeDirection = SwipeToDismissBoxState.Direction.EndToStart\n)",
            migrationStep = 2
        )
    )

    // ================================================================
    // Health score calculation / 健康度计算
    // ================================================================

    /**
     * 计算 Text padding 健康度
     * Text padding health score = 100 - (critical * 30) - (warning * 10)
     */
    private fun calculateTextScore(issues: List<TextPaddingIssue>): Float {
        val critical = issues.count { it.severity == Compose111Severity.CRITICAL }
        val warning = issues.count { it.severity == Compose111Severity.WARNING }
        return (100f - critical * 30f - warning * 10f).coerceIn(0f, 100f)
    }

    /**
     * 计算 DrawLayer 健康度
     * DrawLayer health score = 100 - (issues * 25)
     */
    private fun calculateDrawLayerScore(issues: List<DrawLayerIssue>): Float {
        return (100f - issues.size * 25f).coerceIn(0f, 100f)
    }

    /**
     * 计算 SwipeToReveal 健康度
     * SwipeToReveal health score = 100 - (issues * 30)
     */
    private fun calculateSwipeScore(issues: List<SwipeToRevealIssue>): Float {
        return (100f - issues.size * 30f).coerceIn(0f, 100f)
    }

    // ================================================================
    // Fix & Migration actions / 修复与迁移操作
    // ================================================================

    /** 标记 Text Padding 问题已修复 / Mark Text padding issue as fixed */
    private fun fixTextPadding(index: Int) {
        _state.update { s ->
            val issues = s.textPaddingIssues.toMutableList()
            if (index in issues.indices) {
                issues[index] = issues[index].copy(isFixed = true)
            }
            // 重新计算健康度
            val critical = issues.count { it.severity == Compose111Severity.CRITICAL && !it.isFixed }
            val textScore = (100f - critical * 30f).coerceIn(0f, 100f)
            val overall = textScore * 0.60f + s.drawLayerScore * 0.25f + s.swipeToRevealScore * 0.15f
            s.copy(
                textPaddingIssues = issues,
                textPaddingScore = textScore,
                overallHealthScore = overall,
                criticalCount = s.criticalCount - 1
            )
        }
        viewModelScope.launch {
            _effect.send(Compose111Effect.ShowFixSuggestion("Text padding issue marked as fixed"))
        }
    }

    /** 标记 DrawLayer 问题已修复 / Mark DrawLayer issue as fixed */
    private fun fixDrawLayer(index: Int) {
        _state.update { s ->
            val issues = s.drawLayerIssues.toMutableList()
            if (index in issues.indices) {
                issues[index] = issues[index].copy(isFixed = true)
            }
            val drawLayerScore = (100f - issues.count { !it.isFixed } * 25f).coerceIn(0f, 100f)
            val overall = s.textPaddingScore * 0.60f + drawLayerScore * 0.25f + s.swipeToRevealScore * 0.15f
            s.copy(
                drawLayerIssues = issues,
                drawLayerScore = drawLayerScore,
                overallHealthScore = overall,
                criticalCount = (s.criticalCount - 1).coerceAtLeast(0)
            )
        }
    }

    /** 标记 SwipeToReveal 已迁移 / Mark SwipeToReveal as migrated */
    private fun migrateSwipeToReveal(index: Int) {
        _state.update { s ->
            val issues = s.swipeToRevealIssues.toMutableList()
            if (index in issues.indices) {
                val current = issues[index]
                if (current.migrationStep < 3) {
                    issues[index] = current.copy(migrationStep = current.migrationStep + 1)
                } else {
                    issues[index] = current.copy(isMigrated = true)
                }
            }
            val swipeScore = (100f - issues.count { !it.isMigrated } * 30f).coerceIn(0f, 100f)
            val overall = s.textPaddingScore * 0.60f + s.drawLayerScore * 0.25f + swipeScore * 0.15f
            s.copy(
                swipeToRevealIssues = issues,
                swipeToRevealScore = swipeScore,
                overallHealthScore = overall,
                criticalCount = (s.criticalCount - 1).coerceAtLeast(0)
            )
        }
    }

    // ================================================================
    // Report generation / 报告生成
    // ================================================================

    /** 生成 Markdown 合规报告 / Generate Markdown compliance report */
    private fun generateReport() {
        val s = _state.value
        val sb = StringBuilder()
        sb.appendLine("# Compose 1.11.0-rc01 破坏性变更合规报告")
        sb.appendLine()
        sb.appendLine("**生成时间**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}")
        sb.appendLine("**综合健康度**: ${s.overallHealthScore.toInt()}/100")
        sb.appendLine()
        sb.appendLine("## 📊 健康度总览")
        sb.appendLine()
        sb.appendLine("| 变更项 | 健康度 | Critical | Warning |")
        sb.appendLine("|--------|--------|----------|---------|")
        sb.appendLine("| Text Extra Padding | ${s.textPaddingScore.toInt()} | ${s.textPaddingIssues.count { it.severity == Compose111Severity.CRITICAL && !it.isFixed }} | ${s.textPaddingIssues.count { it.severity == Compose111Severity.WARNING }} |")
        sb.appendLine("| DrawLayer API | ${s.drawLayerScore.toInt()} | ${s.drawLayerIssues.count { !it.isFixed }} | 0 |")
        sb.appendLine("| SwipeToReveal | ${s.swipeToRevealScore.toInt()} | ${s.swipeToRevealIssues.count { !it.isMigrated }} | 0 |")
        sb.appendLine()
        sb.appendLine("## 🔴 Critical 问题")
        s.textPaddingIssues.filter { it.severity == Compose111Severity.CRITICAL && !it.isFixed }.forEach { issue ->
            sb.appendLine("- **${issue.filePath}:${issue.lineNumber}**")
            sb.appendLine("  ```kotlin")
            sb.appendLine("  ${issue.codeSnippet}")
            sb.appendLine("  ```")
            sb.appendLine("  建议: ${issue.suggestedFix}")
            sb.appendLine()
        }
        sb.appendLine("## 🟡 Warning 问题")
        s.textPaddingIssues.filter { it.severity == Compose111Severity.WARNING && !it.isFixed }.forEach { issue ->
            sb.appendLine("- **${issue.filePath}:${issue.lineNumber}**")
            sb.appendLine("  建议: ${issue.suggestedFix}")
            sb.appendLine()
        }
        sb.appendLine("## ✅ 已修复 / 已迁移")
        val fixed = s.textPaddingIssues.count { it.isFixed } +
                s.drawLayerIssues.count { it.isFixed } +
                s.swipeToRevealIssues.count { it.isMigrated }
        sb.appendLine("共 $fixed 项已修复或迁移")

        val report = sb.toString()
        _state.update { it.copy(reportMarkdown = report) }
    }

    /** 生成 CI YAML 配置 / Generate CI YAML configuration */
    private fun generateCiConfig() {
        val s = _state.value
        val yaml = when (s.selectedCiPlatform) {
            CiPlatform.GITHUB_ACTIONS -> buildGithubActionsYaml()
            CiPlatform.GITLAB_CI -> buildGitlabCiYaml()
        }
        _state.update { it.copy(ciConfigYaml = yaml) }
    }

    private fun buildGithubActionsYaml(): String = """
name: Compose 1.11 Compliance Check
on: [push, pull_request]

jobs:
  compose-111-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Run Compose 1.11 compliance scan
        run: |
          echo "Scanning for Compose 1.11 breaking changes..."
          # Gradle task to scan Text padding
          ./gradlew :app:checkTextPadding || true
          # Gradle task to scan DrawLayer API
          ./gradlew :app:checkDrawLayerApi || true
      - name: Upload compliance report
        uses: actions/upload-artifact@v4
        with:
          name: compose-111-report
          path: build/reports/compose-111/
    """.trimIndent()

    private fun buildGitlabCiYaml(): String = """
compose-111-check:
  image: ubuntu:latest
  stage: test
  script:
    - echo "Scanning for Compose 1.11 breaking changes..."
    - apt-get update && apt-get install -y openjdk-17-jdk
    - ./gradlew :app:checkTextPadding || true
    - ./gradlew :app:checkDrawLayerApi || true
  artifacts:
    reports:
      text: build/reports/compose-111/
    expire_in: 1 week
    """.trimIndent()

    // ================================================================
    // Tab & filter actions / Tab 和过滤操作
    // ================================================================

    /** 切换 Tab / Switch tab */
    private fun selectTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    /** 选择 CI 平台 / Select CI platform */
    private fun selectCiPlatform(platform: CiPlatform) {
        _state.update { it.copy(selectedCiPlatform = platform, ciConfigYaml = "") }
    }

    /** 按严重性过滤 / Filter by severity */
    private fun filterBySeverity(severity: Compose111Severity?) {
        _state.update { it.copy(selectedSeverityFilter = severity) }
    }

    /** 切换预览模式 / Toggle preview mode */
    private fun togglePreviewMode() {
        _state.update { it.copy(previewFixMode = !it.previewFixMode) }
    }
}
