package com.mvi.kenny.feature.pandatools

// ============================================================
// PandaToolsViewModel — Android Studio Panda 4 AI Tools ViewModel
// PRD-136 | Android Studio Panda 4 AI 编程助手开发工具包
// MVI: State → Intent → Effect
// ============================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PandaTools ViewModel — 继承 ViewModel，支持 MVI 架构。
 * PandaTools ViewModel — extends ViewModel, supports MVI architecture.
 *
 * @see PandaToolsContract for State, Intent, and Effect definitions
 * @see PandaToolsScreen for the Composable UI
 */
class PandaToolsViewModel : ViewModel() {

    // ============================================================
    // State — 状态流（唯一真相来源）
    // State — single source of truth for UI state
    // ============================================================
    private val _state = MutableStateFlow(PandaToolsState())
    val state: StateFlow<PandaToolsState> = _state.asStateFlow()

    // ============================================================
    // Effect — 副作用通道（一次性事件）
    // Effect — side-effect channel for one-time events
    // ============================================================
    private val _effect = Channel<PandaToolsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent 处理 — 业务逻辑入口
    // Intent Processing — business logic entry point
    // ============================================================

    /**
     * 处理用户意图。
     * Process user intent.
     *
     * @param intent 用户意图
     */
    fun processIntent(intent: PandaToolsIntent) {
        when (intent) {
            is PandaToolsIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is PandaToolsIntent.OpenProject -> handleOpenProject(intent.path)
            is PandaToolsIntent.SelectFile -> handleSelectFile(intent.file)
            is PandaToolsIntent.UpdateFileContent -> handleUpdateFileContent(intent.content)
            is PandaToolsIntent.SaveFile -> handleSaveFile()
            is PandaToolsIntent.ValidateFile -> handleValidateFile()
            is PandaToolsIntent.SelectPlan -> handleSelectPlan(intent.plan)
            is PandaToolsIntent.ApprovePlan -> handleApprovePlan(intent.planId)
            is PandaToolsIntent.RequestPlanRevision -> handleRequestPlanRevision(intent.planId, intent.feedback)
            is PandaToolsIntent.ComparePlanVersions -> handleComparePlanVersions(intent.v1, intent.v2)
            is PandaToolsIntent.RollbackPlan -> handleRollbackPlan(intent.planId, intent.targetVersion)
            is PandaToolsIntent.SelectPredictionRecord -> handleSelectPredictionRecord(intent.record)
            is PandaToolsIntent.MarkPredictionMisclassification -> handleMarkMisclassification(intent.recordId)
            is PandaToolsIntent.RunPermissionAudit -> handleRunPermissionAudit()
            is PandaToolsIntent.SortSkills -> handleSortSkills(intent.option)
            is PandaToolsIntent.FilterSkillsByCategory -> handleFilterSkills(intent.category)
            is PandaToolsIntent.SelectSkill -> handleSelectSkill(intent.skill)
        }
    }

    // ============================================================
    // Tab 切换处理
    // Tab Switching Handler
    // ============================================================

    private fun handleSwitchTab(tab: PandaTab) {
        _state.update { it.copy(currentTab = tab) }
        // 切换 Tab 时加载对应数据 / Load data when switching tabs
        when (tab) {
            PandaTab.SkillsEditor -> loadSkillsEditorData()
            PandaTab.PlanManagement -> loadPlansData()
            PandaTab.NepAnalysis -> loadNepData()
            PandaTab.PermissionAudit -> loadAuditData()
            PandaTab.QualityScore -> loadQualityScoreData()
        }
    }

    // ============================================================
    // Skills 编辑器处理
    // Skills Editor Handlers
    // ============================================================

    private fun handleOpenProject(path: String) {
        _state.update {
            it.copy(
                skillsEditorState = it.skillsEditorState.copy(
                    projectPath = path,
                    isLoading = true
                )
            )
        }
        viewModelScope.launch {
            try {
                // 模拟加载 Skill 文件列表 / Simulate loading skill file list
                val mockFiles = listOf(
                    SkillFile("1", "compose-navigation.yaml", "$path/compose-navigation.yaml", "Official"),
                    SkillFile("2", "gradle-config.yaml", "$path/gradle-config.yaml", "Official"),
                    SkillFile("3", "material-design.yaml", "$path/material-design.yaml", "Community"),
                    SkillFile("4", "custom-agent.yaml", "$path/custom-agent.yaml", "Community")
                )
                _state.update {
                    it.copy(
                        skillsEditorState = it.skillsEditorState.copy(
                            projectPath = path,
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                _effect.send(PandaToolsEffect.ShowError("加载项目失败: ${e.message}"))
            }
        }
    }

    private fun handleSelectFile(file: SkillFile) {
        _state.update {
            it.copy(
                skillsEditorState = it.skillsEditorState.copy(
                    selectedFile = file,
                    fileContent = getMockFileContent(file),
                    isDirty = false,
                    validationErrors = emptyList()
                )
            )
        }
    }

    private fun handleUpdateFileContent(content: String) {
        _state.update {
            it.copy(
                skillsEditorState = it.skillsEditorState.copy(
                    fileContent = content,
                    isDirty = true
                )
            )
        }
    }

    private fun handleSaveFile() {
        viewModelScope.launch {
            try {
                // 模拟保存文件 / Simulate saving file
                _state.update {
                    it.copy(
                        skillsEditorState = it.skillsEditorState.copy(isDirty = false)
                    )
                }
                _effect.send(PandaToolsEffect.FileSaved)
                _effect.send(PandaToolsEffect.ShowSnackbar("文件已保存 / File saved"))
            } catch (e: Exception) {
                _effect.send(PandaToolsEffect.ShowError("保存失败: ${e.message}"))
            }
        }
    }

    private fun handleValidateFile() {
        viewModelScope.launch {
            val content = _state.value.skillsEditorState.fileContent
            // 简单 YAML 语法验证 / Simple YAML syntax validation
            val errors = mutableListOf<ValidationError>()
            content.lines().forEachIndexed { index, line ->
                when {
                    line.contains("\t") && !line.startsWith("#") -> {
                        errors.add(ValidationError(index + 1, "Tab not allowed, use spaces", "Error"))
                    }
                    line.trim().endsWith(":") && line.contains("  ") && !line.startsWith(" ") -> {
                        errors.add(ValidationError(index + 1, "Potential indentation error", "Warning"))
                    }
                }
            }
            _state.update {
                it.copy(
                    skillsEditorState = it.skillsEditorState.copy(
                        validationErrors = errors,
                        validationOutput = if (errors.isEmpty()) "✅ YAML 验证通过 / YAML validation passed" else "❌ 发现 ${errors.size} 个问题 / ${errors.size} issues found"
                    )
                )
            }
            if (errors.isNotEmpty()) {
                _effect.send(PandaToolsEffect.ValidationFailed(errors))
            }
        }
    }

    // ============================================================
    // 计划管理处理
    // Plan Management Handlers
    // ============================================================

    private fun handleSelectPlan(plan: AgentPlan) {
        _state.update {
            it.copy(planManagementState = it.planManagementState.copy(selectedPlan = plan))
        }
    }

    private fun handleApprovePlan(planId: String) {
        viewModelScope.launch {
            _state.update {
                val updatedPlans = it.planManagementState.plans.map { plan ->
                    if (plan.id == planId) plan.copy(status = "Approved") else plan
                }
                val updatedSelected = it.planManagementState.selectedPlan?.let { plan ->
                    if (plan.id == planId) plan.copy(status = "Approved") else plan
                }
                it.copy(
                    planManagementState = it.planManagementState.copy(
                        plans = updatedPlans,
                        selectedPlan = updatedSelected
                    )
                )
            }
            _effect.send(PandaToolsEffect.PlanApproved(planId))
            _effect.send(PandaToolsEffect.ShowSnackbar("计划已批准 / Plan approved"))
        }
    }

    private fun handleRequestPlanRevision(planId: String, feedback: String) {
        viewModelScope.launch {
            _state.update {
                val updatedPlans = it.planManagementState.plans.map { plan ->
                    if (plan.id == planId) {
                        plan.copy(
                            status = "RequestChange",
                            developerComments = plan.developerComments + PlanComment(
                                id = System.currentTimeMillis().toString(),
                                author = "Developer",
                                content = feedback,
                                timestamp = java.time.Instant.now().toString(),
                                type = "RequestChange"
                            )
                        )
                    } else plan
                }
                it.copy(planManagementState = it.planManagementState.copy(plans = updatedPlans))
            }
            _effect.send(PandaToolsEffect.ShowSnackbar("已提交修改请求 / Revision requested"))
        }
    }

    private fun handleComparePlanVersions(v1: AgentPlan, v2: AgentPlan) {
        _state.update {
            it.copy(planManagementState = it.planManagementState.copy(diffVersions = Pair(v1, v2)))
        }
        viewModelScope.launch {
            _effect.send(PandaToolsEffect.ShowPlanDiff(v1, v2))
        }
    }

    private fun handleRollbackPlan(planId: String, targetVersion: Int) {
        viewModelScope.launch {
            _state.update {
                val updatedPlans = it.planManagementState.plans.map { plan ->
                    if (plan.id == planId) plan.copy(version = targetVersion, status = "Draft") else plan
                }
                it.copy(planManagementState = it.planManagementState.copy(plans = updatedPlans))
            }
            _effect.send(PandaToolsEffect.PlanRolledBack(planId, targetVersion))
            _effect.send(PandaToolsEffect.ShowSnackbar("已回滚至 v$targetVersion / Rolled back to v$targetVersion"))
        }
    }

    // ============================================================
    // NEP 分析处理
    // NEP Analysis Handlers
    // ============================================================

    private fun handleSelectPredictionRecord(record: PredictionRecord) {
        _state.update {
            it.copy(nepAnalysisState = it.nepAnalysisState.copy(selectedRecord = record))
        }
    }

    private fun handleMarkMisclassification(recordId: String) {
        viewModelScope.launch {
            _state.update {
                val updatedHistory = it.nepAnalysisState.predictionHistory.map { record ->
                    if (record.id == recordId) record.copy(isMatch = false) else record
                }
                it.copy(nepAnalysisState = it.nepAnalysisState.copy(predictionHistory = updatedHistory))
            }
            _effect.send(PandaToolsEffect.ShowSnackbar("已标记为误判 / Marked as misclassification"))
        }
    }

    // ============================================================
    // 权限审计处理
    // Permission Audit Handlers
    // ============================================================

    private fun handleRunPermissionAudit() {
        viewModelScope.launch {
            _state.update {
                it.copy(permissionAuditState = it.permissionAuditState.copy(isAuditing = true))
            }
            try {
                // 模拟审计过程 / Simulate audit process
                kotlinx.coroutines.delay(1500)
                val mockOverview = AuditOverview(
                    totalPermissions = 12,
                    coveredPermissions = 8,
                    overGrantedCount = 2,
                    riskLevel = "Medium",
                    coverageRate = 0.67f
                )
                val mockMatrix = listOf(
                    PermissionRow("FileRead", "Read", "None", "None", "Low"),
                    PermissionRow("FileWrite", "Write", "None", "None", "Medium"),
                    PermissionRow("NetworkRequest", "None", "Full", "None", "High"),
                    PermissionRow("CodeExecution", "None", "None", "Execute", "High")
                )
                val mockRiskItems = listOf(
                    RiskItem("r1", "过度文件授权", "Agent 具有完整的文件系统写权限", "High", "限制为只读目录"),
                    RiskItem("r2", "网络权限过宽", "Agent 可以访问任意网络端点", "Medium", "配置白名单域名")
                )
                _state.update {
                    it.copy(
                        permissionAuditState = it.permissionAuditState.copy(
                            isAuditing = false,
                            auditOverview = mockOverview,
                            permissionMatrix = mockMatrix,
                            riskItems = mockRiskItems
                        )
                    )
                }
                _effect.send(PandaToolsEffect.AuditComplete)
                _effect.send(PandaToolsEffect.ShowSnackbar("审计完成 / Audit complete"))
            } catch (e: Exception) {
                _effect.send(PandaToolsEffect.ShowError("审计失败: ${e.message}"))
            }
        }
    }

    // ============================================================
    // 质量评分处理
    // Quality Score Handlers
    // ============================================================

    private fun handleSortSkills(option: SkillSortOption) {
        _state.update {
            val sorted = when (option) {
                SkillSortOption.Rating -> it.qualityScoreState.skills.sortedByDescending { s -> s.rating }
                SkillSortOption.UsageCount -> it.qualityScoreState.skills.sortedByDescending { s -> s.usageCount }
                SkillSortOption.Latest -> it.qualityScoreState.skills.sortedByDescending { s -> s.id }
                SkillSortOption.Alphabetical -> it.qualityScoreState.skills.sortedBy { s -> s.name }
            }
            it.copy(qualityScoreState = it.qualityScoreState.copy(skills = sorted, sortBy = option))
        }
    }

    private fun handleFilterSkills(category: String?) {
        _state.update {
            it.copy(qualityScoreState = it.qualityScoreState.copy(filterCategory = category))
        }
    }

    private fun handleSelectSkill(skill: SkillCard) {
        _state.update {
            it.copy(qualityScoreState = it.qualityScoreState.copy(selectedSkill = skill))
        }
    }

    // ============================================================
    // 数据加载（模拟）
    // Data Loading (Mock)
    // ============================================================

    private fun loadSkillsEditorData() {
        // Skills 编辑器数据由用户打开项目时触发
    }

    private fun loadPlansData() {
        viewModelScope.launch {
            _state.update { it.copy(planManagementState = it.planManagementState.copy(isLoading = true)) }
            kotlinx.coroutines.delay(500)
            val mockPlans = listOf(
                AgentPlan(
                    id = "p1",
                    title = "迁移至 Navigation Event API",
                    createdAt = "2026-04-23T10:00:00Z",
                    updatedAt = "2026-04-23T14:30:00Z",
                    version = 2,
                    status = "Pending",
                    steps = listOf(
                        PlanStep("s1", "分析 PredictiveBackHandler 用法", "扫描项目中的 PredictiveBackHandler 调用", false, 1),
                        PlanStep("s2", "创建 NavigationEventDispatcher", "初始化 dispatcher 并建立父子连接", false, 2),
                        PlanStep("s3", "迁移回退处理逻辑", "将 PredictiveBackHandler 回调迁移至新 API", false, 3)
                    ),
                    developerComments = emptyList()
                ),
                AgentPlan(
                    id = "p2",
                    title = "Compose Grid/FlexBox 性能优化",
                    createdAt = "2026-04-22T09:00:00Z",
                    updatedAt = "2026-04-22T16:00:00Z",
                    version = 1,
                    status = "Approved",
                    steps = listOf(
                        PlanStep("s4", "性能基准测试", "对比 Grid vs LazyGrid 重组开销", true, 1),
                        PlanStep("s5", "优化 Grid 布局", "减少不必要的重组", true, 2)
                    ),
                    developerComments = listOf(
                        PlanComment("c1", "Dev", "LGTM, 可以执行", "2026-04-22T16:00:00Z", "Approve")
                    )
                )
            )
            _state.update {
                it.copy(planManagementState = it.planManagementState.copy(plans = mockPlans, isLoading = false))
            }
        }
    }

    private fun loadNepData() {
        viewModelScope.launch {
            val mockRecords = listOf(
                PredictionRecord("nep1", "fun onCreate(savedInstanceState: Bundle?)", "fun onCreate(savedInstanceState: Bundle?)", true, "2026-04-23T10:00:00Z", "MainActivity.kt", "Kotlin"),
                PredictionRecord("nep2", "setContentView(R.layout.main)", "binding = ActivityMainBinding.inflate(layoutInflater)", false, "2026-04-23T10:05:00Z", "MainActivity.kt", "Kotlin"),
                PredictionRecord("nep3", "viewModelScope.launch {", "viewModelScope.launch {", true, "2026-04-23T10:10:00Z", "MainViewModel.kt", "Kotlin"),
                PredictionRecord("nep4", "LazyColumn(items = list)", "LazyColumn(items = items, modifier = Modifier.fillMaxSize())", false, "2026-04-23T10:15:00Z", "ListScreen.kt", "Kotlin"),
                PredictionRecord("nep5", "@Composable fun Home()", "@Composable fun HomeScreen() {", false, "2026-04-23T10:20:00Z", "HomeScreen.kt", "Kotlin")
            )
            val accuracy = mockRecords.count { it.isMatch }.toFloat() / mockRecords.size
            val mockProfile = CodeStyleProfile(
                languagePreference = "Kotlin",
                namingPattern = "camelCase",
                refactoringPatterns = listOf("Extract Function", "Inline Variable", "Move to Scope"),
                avgLineLength = 80
            )
            _state.update {
                it.copy(
                    nepAnalysisState = it.nepAnalysisState.copy(
                        totalPredictions = mockRecords.size,
                        accuracyRate = accuracy,
                        codeStyleProfile = mockProfile,
                        predictionHistory = mockRecords
                    )
                )
            }
        }
    }

    private fun loadAuditData() {
        // 初始空数据，用户点击"开始审计"按钮时触发 handleRunPermissionAudit
    }

    private fun loadQualityScoreData() {
        viewModelScope.launch {
            val mockSkills = listOf(
                SkillCard("sk1", "Compose Navigation", "官方 Navigation 技能库", "Google", 4.8f, 12500, "Navigation", true, listOf("Navigation", "Compose")),
                SkillCard("sk2", "Gradle Config Helper", "Gradle 配置辅助技能", "AndroidDev", 4.5f, 8300, "Build", false, listOf("Gradle", "Build")),
                SkillCard("sk3", "Material Design 3", "Material3 组件使用指南", "Google", 4.9f, 15000, "UI", true, listOf("Material3", "Compose")),
                SkillCard("sk4", "Testing Toolkit", "单元测试与 UI 测试技能", "QA Team", 4.2f, 5200, "Testing", false, listOf("Testing", "JUnit")),
                SkillCard("sk5", "KMP Setup Wizard", "Kotlin Multiplatform 初始化向导", "JetBrains", 4.6f, 7800, "KMP", true, listOf("KMP", "Compose Multiplatform")),
                SkillCard("sk6", "Performance Profiler", "性能分析与优化技能", "AndroidDev", 4.3f, 4100, "Performance", false, listOf("Performance", "Benchmark"))
            )
            _state.update {
                it.copy(qualityScoreState = it.qualityScoreState.copy(skills = mockSkills))
            }
        }
    }

    // ============================================================
    // 辅助方法
    // Helper Methods
    // ============================================================

    /**
     * 获取模拟文件内容。
     * Get mock file content for demonstration.
     */
    private fun getMockFileContent(file: SkillFile): String {
        return when {
            file.name.contains("compose-navigation") -> """
# Compose Navigation Skill
# 官方 Skill 文件 / Official Skill File
name: compose-navigation
version: 1.0.0
description: |
  如何在 Compose 中正确使用 Navigation 组件。
  包括 NavHost / NavController 的初始化和路由管理。
provider: google
capabilities:
  - navigation_setup
  - route_management
  - deep_links
examples:
  - title: 基础导航设置
    code: |
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "home") {
          composable("home") { HomeScreen() }
          composable("detail/{id}") { backStackEntry ->
              DetailScreen(id = backStackEntry.arguments?.getString("id") ?: "")
          }
      }
            """.trimIndent()
            file.name.contains("gradle-config") -> """
# Gradle Configuration Skill
# 官方 Skill 文件 / Official Skill File
name: gradle-config
version: 1.2.0
description: |
  Android Gradle Plugin 配置最佳实践。
  包含凯撒、依赖管理、构建变体配置。
provider: google
capabilities:
  - dependency_management
  - build_variants
  - proguard
            """.trimIndent()
            else -> "# ${file.name}\n# Placeholder content\nname: ${file.name.removeSuffix(".yaml")}"
        }
    }
}
