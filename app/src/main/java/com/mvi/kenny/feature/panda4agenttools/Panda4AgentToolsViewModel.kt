package com.mvi.kenny.feature.panda4agenttools

// ================================================================
// Panda4AgentToolsViewModel — Android Studio Panda 4 AI Agent 增强工具包 ViewModel
// ================================================================
// MVI architecture ViewModel for Panda 4 AI Agent Enhancement Toolkit.
//
// PRD-240: Android Studio Panda 4 AI Agent 增强工具包
// Design Reference: memory/agency/designs/PRD-240-Android-Studio-Panda-4-AI-Agent-增强工具包.md
//
// Five feature modules:
// 1. Planning Mode — plan templates + readability tool + × Agent Skills integration
// 2. Next Edit Prediction — adoption analytics + custom prediction rules
// 3. Agent Web Search — result quality scoring + enterprise KB integration
// 4. Ask Mode — knowledge base configuration
// 5. Dev Verification — CI integration + audit log export
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

// ================================================================
// Sample Data / 示例数据 — 填充 UI 演示
// ================================================================

/** 生成示例计划模板列表 / Generate sample plan templates */
private fun generateSampleTemplates(scenario: PlanScenario): List<PlanTemplate> {
    val templates = mutableListOf<PlanTemplate>()
    when (scenario) {
        PlanScenario.Refactoring -> {
            templates.add(PlanTemplate(
                id = "tmpl-refactor-1",
                scenario = PlanScenario.Refactoring,
                title = "MVP → MVVM 重构计划",
                description = "将现有 MVP 架构项目重构为 MVVM，使用 Jetpack ViewModel + LiveData/StateFlow",
                content = """# MVP → MVVM 重构计划

## 1. 现状分析
- 识别所有 Presenter 类
- 统计 View 接口依赖数量
- 评估测试覆盖率现状

## 2. 迁移策略
- 保留 View 接口（转为 Compose UI）
- 将 Presenter → ViewModel
- LiveData/StateFlow 替代 Callback

## 3. 分阶段实施
### Phase 1: 单模块试点
- 选择 `UserModule` 作为试点
- 完成数据流重连
- 编写单元测试

### Phase 2: 批量迁移
- 剩余模块按依赖顺序迁移
- 每日集成测试

### Phase 3: 清理
- 移除废弃 MVP 基础设施
- 更新文档
""",
                tags = "MVVM,Jetpack,Refactor"
            ))
            templates.add(PlanTemplate(
                id = "tmpl-refactor-2",
                scenario = PlanScenario.Refactoring,
                title = "模块化重构计划",
                description = "将单体 App 拆分为多个 Feature Module，降低耦合",
                content = """# 模块化重构计划

## 目标
- 每个 Feature 独立 Module
- 共享代码抽至 `:core`
- 支持按需编译

## 实施步骤
1. 定义模块边界
2. 创建 Module 骨架
3. 迁移代码
4. 验证编译
""",
                tags = "MultiModule,Architecture"
            ))
        }
        PlanScenario.NewFeature -> {
            templates.add(PlanTemplate(
                id = "tmpl-newfeature-1",
                scenario = PlanScenario.NewFeature,
                title = "新功能开发计划模板",
                description = "标准新功能开发流程：需求分析 → 设计 → 开发 → 测试 → 上线",
                content = """# 新功能开发计划

## 功能概述
- 功能名称：
- 目标用户：
- 核心价值：

## 详细设计
### UI/UX
### 数据流
### API 接口

## 开发任务
- [ ] 任务 1
- [ ] 任务 2

## 测试计划
- 单元测试
- UI 测试
- 集成测试

## 上线检查
- 监控告警
- 回滚方案
""",
                tags = "Template,Standard"
            ))
        }
        PlanScenario.BugFix -> {
            templates.add(PlanTemplate(
                id = "tmpl-bugfix-1",
                scenario = PlanScenario.BugFix,
                title = "Bug 修复计划模板",
                description = "系统性 Bug 修复流程：复现 → 定位 → 修复 → 验证",
                content = """# Bug 修复计划

## Bug 信息
- Bug ID:
- 严重级别:
- 影响版本:

## 复现步骤
1.
2.
3.

## 根因分析
- 直接原因：
- 深层原因：

## 修复方案
- 修复代码：
- 影响范围：

## 验证
- [ ] 本地验证
- [ ] 回归测试
""",
                tags = "BugFix,Template"
            ))
        }
        PlanScenario.PerformanceOptimization -> {
            templates.add(PlanTemplate(
                id = "tmpl-perf-1",
                scenario = PlanScenario.PerformanceOptimization,
                title = "性能优化计划",
                description = "App 性能优化系统性方法：测量 → 分析 → 优化 → 验证",
                content = """# 性能优化计划

## 优化目标
- 冷启动时间: < 2s
- 帧率: 60 FPS
- 内存: < 150MB

## 测量基线
- 启动时间基线:
- 内存基线:

## 优化项
### 1. 启动优化
### 2. 内存优化
### 3. 渲染优化

## 验证方法
- Android Profiler
- Systrace
""",
                tags = "Performance,Optimization"
            ))
        }
        PlanScenario.ArchitectureUpgrade -> {
            templates.add(PlanTemplate(
                id = "tmpl-arch-1",
                scenario = PlanScenario.ArchitectureUpgrade,
                title = "架构升级计划",
                description = "大型架构变更的系统性规划，如 Compose 迁移、KMP 引入等",
                content = """# 架构升级计划

## 升级目标
- 从 XML → Compose
- 支持多平台 (KMP)

## 现状评估
- 代码行数:
- 测试覆盖率:
- 团队熟悉度:

## 分阶段路线图
### Phase 1 (Week 1-2): 调研与 POC
### Phase 2 (Week 3-8): 试点模块迁移
### Phase 3 (Week 9-16): 全面迁移
### Phase 4 (Week 17-20): 稳定与优化

## 风险评估
- 风险 1: 团队学习曲线
- 风险 2: 第三方库兼容性

## 回滚方案
""",
                tags = "Architecture,KMP,Compose"
            ))
        }
    }
    return templates
}

/** 生成示例预测规则 / Generate sample prediction rules */
private fun generateSampleRules(): List<PredictionRule> = listOf(
    PredictionRule(
        id = "rule-1",
        name = "测试文件变更后建议更新 Mock",
        pattern = "**/test/**.kt:modify:test_mock_*.kt",
        isEnabled = true,
        description = "当测试文件被修改时，建议同时更新对应的 mock 文件",
        adoptionCount = 12
    ),
    PredictionRule(
        id = "rule-2",
        name = "API 接口变更后建议更新 Model",
        pattern = "**/api/**.kt:modify:*Dto.kt",
        isEnabled = true,
        description = "API 接口文件修改后，建议检查并更新 DTO 模型",
        adoptionCount = 8
    ),
    PredictionRule(
        id = "rule-3",
        name = "Compose Screen 修改后检查 Preview",
        pattern = "**/*Screen.kt:modify:*Preview.kt",
        isEnabled = false,
        description = "Screen 文件变更后建议检查对应的 Preview 文件",
        adoptionCount = 5
    )
)

/** 生成示例审计日志 / Generate sample audit logs */
private fun generateSampleAuditLogs(): List<AuditLog> {
    val now = System.currentTimeMillis()
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return listOf(
        AuditLog(
            id = "audit-1",
            timestamp = df.format(Date(now - 3600_000 * 5)),
            action = "PlanCreated",
            description = "创建重构计划：MVP → MVVM",
            plannerOutput = "# MVP→MVVM 重构计划...",
            agentAction = "计划已保存至plandrafts/mvp-mvvm-2026-05-09.md",
            deviationDegree = 0f,
            executor = "Kenny"
        ),
        AuditLog(
            id = "audit-2",
            timestamp = df.format(Date(now - 3600_000 * 4)),
            action = "PlanReviewed",
            description = "计划评审通过：风险评级 Medium",
            plannerOutput = "",
            agentAction = "",
            deviationDegree = 0.1f,
            executor = "Agent"
        ),
        AuditLog(
            id = "audit-3",
            timestamp = df.format(Date(now - 3600_000 * 2)),
            action = "TaskExecuted",
            description = "执行：迁移 UserModule Presenter → ViewModel",
            plannerOutput = "UserModule Presenter → ViewModel",
            agentAction = "已迁移 UserPresenter.kt → UserViewModel.kt",
            deviationDegree = 0.05f,
            executor = "Agent (Planning Mode)"
        ),
        AuditLog(
            id = "audit-4",
            timestamp = df.format(Date(now - 3600_000)),
            action = "PlanApproved",
            description = "架构升级计划评审通过",
            plannerOutput = "# 架构升级计划...",
            agentAction = "计划已创建并提交",
            deviationDegree = 0f,
            executor = "Kenny"
        )
    )
}

// ================================================================
// ViewModel Implementation / ViewModel 实现
// ================================================================

/**
 * ============================================================
 * Panda4AgentToolsViewModel — 工具包 ViewModel
 * ================================================================
 * MVI architecture ViewModel — manages all 5 tabs' state.
 *
 * Key design principles:
 * - State is immutable (data class copy semantics)
 * - Intent processing is synchronous in viewModelScope.launch
 * - Effects are delivered via Channel (at-most-once)
 * - Tab state is preserved across tab switches
 */
class Panda4AgentToolsViewModel : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(Panda4AgentToolsState())
    val state: StateFlow<Panda4AgentToolsState> = _state.asStateFlow()

    // ── Effect ─────────────────────────────────────────────────────────────────

    private val _effect = Channel<Panda4AgentToolsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // Load initial data for all tabs
        loadInitialData()
    }

    // ── Initial Data Loading ────────────────────────────────────────────────────

    private fun loadInitialData() {
        viewModelScope.launch {
            val templates = generateSampleTemplates(PlanScenario.Refactoring)
            val rules = generateSampleRules()
            val auditLogs = generateSampleAuditLogs()
            // Simulate NEP adoption trend (7 days)
            val trend = List(7) { Random.nextFloat() * 0.4f + 0.5f }

            _state.value = _state.value.copy(
                planTemplates = templates,
                predictionRules = rules,
                adoptionRate = trend.last(),
                adoptionTrend = trend,
                auditLogs = auditLogs
            )
        }
    }

    // ── Intent Handler ─────────────────────────────────────────────────────────

    /**
     * ============================================================
     * sendIntent — 意图处理入口
     * ================================================================
     * All user intents route through this single function.
     */
    fun sendIntent(intent: Panda4AgentToolsIntent) {
        when (intent) {
            is Panda4AgentToolsIntent.SelectTab -> handleSelectTab(intent.index)
            is Panda4AgentToolsIntent.SelectScenario -> handleSelectScenario(intent.scenario)
            is Panda4AgentToolsIntent.PreviewTemplate -> handlePreviewTemplate(intent.template)
            is Panda4AgentToolsIntent.ClosePreview -> handleClosePreview()
            is Panda4AgentToolsIntent.FormatPlan -> handleFormatPlan(intent.rawText)
            is Panda4AgentToolsIntent.UpdateRawPlanInput -> handleUpdateRawPlanInput(intent.text)
            is Panda4AgentToolsIntent.AddPredictionRule -> handleAddRule(intent.rule)
            is Panda4AgentToolsIntent.DeletePredictionRule -> handleDeleteRule(intent.id)
            is Panda4AgentToolsIntent.ToggleRuleEnabled -> handleToggleRule(intent.id)
            is Panda4AgentToolsIntent.AnalyzeAdoptionRate -> handleAnalyzeAdoption()
            is Panda4AgentToolsIntent.AnalyzeSearchUrl -> handleAnalyzeUrl(intent.url)
            is Panda4AgentToolsIntent.UpdateSearchUrlInput -> handleUpdateSearchUrlInput(intent.text)
            is Panda4AgentToolsIntent.AddKnowledgeBase -> handleAddKb(intent.kb)
            is Panda4AgentToolsIntent.DeleteKnowledgeBase -> handleDeleteKb(intent.id)
            is Panda4AgentToolsIntent.UpdateKbInput -> handleUpdateKbInput(intent.title, intent.content, intent.type)
            is Panda4AgentToolsIntent.PreviewAskOutput -> handlePreviewAsk(intent.query)
            is Panda4AgentToolsIntent.UpdateAskQuery -> handleUpdateAskQuery(intent.query)
            is Panda4AgentToolsIntent.UpdateCiConfig -> handleUpdateCiConfig(intent.config)
            is Panda4AgentToolsIntent.LoadAuditLogs -> handleLoadAuditLogs()
            is Panda4AgentToolsIntent.ExportAuditLogs -> handleExportAuditLogs(intent.format)
            is Panda4AgentToolsIntent.ClearSnackbar -> handleClearSnackbar()
        }
    }

    // ── Tab Navigation ──────────────────────────────────────────────────────────

    private fun handleSelectTab(index: Int) {
        _state.value = _state.value.copy(currentTabIndex = index)
    }

    // ── Planning Mode ───────────────────────────────────────────────────────────

    private fun handleSelectScenario(scenario: PlanScenario) {
        viewModelScope.launch {
            val templates = generateSampleTemplates(scenario)
            _state.value = _state.value.copy(
                selectedScenario = scenario,
                planTemplates = templates,
                previewTemplate = null
            )
        }
    }

    private fun handlePreviewTemplate(template: PlanTemplate) {
        _state.value = _state.value.copy(previewTemplate = template)
    }

    private fun handleClosePreview() {
        _state.value = _state.value.copy(previewTemplate = null)
    }

    private fun handleUpdateRawPlanInput(text: String) {
        _state.value = _state.value.copy(rawPlanInput = text)
    }

    private fun handleFormatPlan(rawText: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isFormattingPlan = true)

            // Simulate AI formatting delay
            delay(800)

            // Basic markdown formatting: add headers, structure
            val formatted = buildString {
                appendLine("# Implementation Plan")
                appendLine()
                rawText.lines().forEachIndexed { index, line ->
                    when {
                        line.startsWith("- [ ]") || line.startsWith("- [x]") -> appendLine(line)
                        line.isNotBlank() && !line.startsWith("#") -> appendLine("## ${line.replaceFirstChar { it.uppercase() }}")
                        else -> appendLine(line)
                    }
                }
                appendLine()
                appendLine("---")
                appendLine("*Formatted by Planning Mode • ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}*")
            }

            _state.value = _state.value.copy(
                isFormattingPlan = false,
                formattedPlanOutput = formatted
            )
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("计划格式化完成"))
        }
    }

    // ── NEP ────────────────────────────────────────────────────────────────────

    private fun handleAddRule(rule: PredictionRule) {
        val current = _state.value.predictionRules.toMutableList()
        current.add(rule)
        _state.value = _state.value.copy(predictionRules = current)
        viewModelScope.launch {
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("规则已添加: ${rule.name}"))
        }
    }

    private fun handleDeleteRule(id: String) {
        val current = _state.value.predictionRules.filter { it.id != id }
        _state.value = _state.value.copy(predictionRules = current)
        viewModelScope.launch {
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("规则已删除"))
        }
    }

    private fun handleToggleRule(id: String) {
        val current = _state.value.predictionRules.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        _state.value = _state.value.copy(predictionRules = current)
    }

    private fun handleAnalyzeAdoption() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAnalyzingRules = true)
            delay(1200)

            // Simulate refreshed adoption data
            val newTrend = _state.value.adoptionTrend.toMutableList().apply {
                if (size >= 7) removeAt(0)
                add(Random.nextFloat() * 0.3f + 0.6f)
            }
            val newRate = newTrend.last()

            _state.value = _state.value.copy(
                isAnalyzingRules = false,
                adoptionRate = newRate,
                adoptionTrend = newTrend
            )
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("采纳率已更新: ${(newRate * 100).toInt()}%"))
        }
    }

    // ── Agent Web Search ────────────────────────────────────────────────────────

    private fun handleAnalyzeUrl(url: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAnalyzingUrl = true, searchUrlInput = url)
            delay(1500)

            // Simulate quality scoring
            val relevance = Random.nextInt(60, 100)
            val freshness = Random.nextInt(50, 100)
            val authority = Random.nextInt(40, 100)
            val overall = (relevance * 0.4 + freshness * 0.3 + authority * 0.3).toInt()

            val result = SearchQualityResult(
                url = url,
                relevanceScore = relevance,
                freshnessScore = freshness,
                authorityScore = authority,
                overallScore = overall,
                summary = when {
                    overall >= 80 -> "高质量资源，建议使用"
                    overall >= 60 -> "可用资源，建议补充"
                    else -> "质量一般，建议寻找更权威来源"
                }
            )

            _state.value = _state.value.copy(
                isAnalyzingUrl = false,
                searchResult = result
            )
        }
    }

    private fun handleUpdateSearchUrlInput(text: String) {
        _state.value = _state.value.copy(searchUrlInput = text)
    }

    private fun handleAddKb(kb: KnowledgeBase) {
        val current = _state.value.knowledgeBases.toMutableList()
        current.add(kb)
        _state.value = _state.value.copy(
            knowledgeBases = current,
            newKbTitleInput = "",
            newKbContentInput = ""
        )
        viewModelScope.launch {
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("知识库条目已添加: ${kb.title}"))
        }
    }

    private fun handleDeleteKb(id: String) {
        val current = _state.value.knowledgeBases.filter { it.id != id }
        _state.value = _state.value.copy(knowledgeBases = current)
        viewModelScope.launch {
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("知识库条目已删除"))
        }
    }

    private fun handleUpdateKbInput(title: String, content: String, type: KbType) {
        _state.value = _state.value.copy(
            newKbTitleInput = title,
            newKbContentInput = content,
            newKbType = type
        )
    }

    // ── Ask Mode ───────────────────────────────────────────────────────────────

    private fun handleUpdateAskQuery(query: String) {
        _state.value = _state.value.copy(askQueryInput = query)
    }

    private fun handlePreviewAsk(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isPreviewingAsk = true)
            delay(1000)

            val output = buildString {
                appendLine("## Ask Mode 预览结果")
                appendLine()
                appendLine("**Query:** $query")
                appendLine()
                appendLine("**Answer:**")
                appendLine("根据知识库配置，回答内容将在此显示...")
                appendLine()
                appendLine("**Sources:**")
                _state.value.askKnowledgeItems.take(3).forEach {
                    appendLine("- ${it.title}")
                }
            }

            _state.value = _state.value.copy(
                isPreviewingAsk = false,
                askPreviewOutput = output
            )
        }
    }

    // ── Dev Verification ───────────────────────────────────────────────────────

    private fun handleUpdateCiConfig(config: CiConfig) {
        _state.value = _state.value.copy(ciConfig = config)
        viewModelScope.launch {
            _effect.send(Panda4AgentToolsEffect.ShowSnackbar("CI 配置已更新"))
        }
    }

    private fun handleLoadAuditLogs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingAudit = true)
            delay(800)
            _state.value = _state.value.copy(
                isLoadingAudit = false,
                auditLogs = generateSampleAuditLogs()
            )
        }
    }

    private fun handleExportAuditLogs(format: ExportFormat) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, exportProgress = 0f)

            // Simulate export progress
            for (i in 1..10) {
                delay(150)
                _state.value = _state.value.copy(exportProgress = i / 10f)
            }

            _state.value = _state.value.copy(isExporting = false, exportProgress = 0f)
            val path = "downloads/audit_logs_${System.currentTimeMillis()}.${format.name.lowercase()}"
            _effect.send(Panda4AgentToolsEffect.DownloadFile(format, path))
        }
    }

    private fun handleClearSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }
}
