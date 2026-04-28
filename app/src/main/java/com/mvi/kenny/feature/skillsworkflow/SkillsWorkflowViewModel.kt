package com.mvi.kenny.feature.skillsworkflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvi.kenny.feature.skillsworkflow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// =============================================================
// SkillsWorkflowViewModel — MVI ViewModel for Skills Workflow Toolkit
// PRD-195: Android CLI Skills Workflow 自动化工具包
// =============================================================
/**
 * ViewModel implementing MVI pattern for Skills Workflow Toolkit.
 * Manages state for Skill Hub, Skill Detail, Workflow Editor, and Setup Wizard.
 * Each sub-feature has dedicated intent handling and state management.
 *
 * @see SkillsWorkflowContract For all state, intent, and effect definitions
 * @see SkillsWorkflowScreen For UI implementation
 */
class SkillsWorkflowViewModel : ViewModel() {

    // =============================================================
    // State Management
    // =============================================================
    private val _state = MutableStateFlow(SkillsWorkflowState.Initial)
    val state: StateFlow<SkillsWorkflowState> = _state.asStateFlow()

    // Effect channels for one-time events / 一次性事件的副作用通道
    private val _effect = MutableSharedFlow<SkillsWorkflowEffect>()
    val effect: SharedFlow<SkillsWorkflowEffect> = _effect.asSharedFlow()

    // =============================================================
    // Intent Processing
    // =============================================================
    /**
     * Process top-level user intents.
     * Routes to appropriate sub-feature handler or updates active tab.
     */
    fun handleIntent(intent: SkillsWorkflowIntent) {
        when (intent) {
            is SkillsWorkflowIntent.SelectTab -> handleSelectTab(intent.tab)
            is SkillsWorkflowIntent.ForwardToSkillHub -> handleSkillHubIntent(intent.intent)
            is SkillsWorkflowIntent.ForwardToSkillDetail -> handleSkillDetailIntent(intent.intent)
            is SkillsWorkflowIntent.ForwardToWorkflow -> handleWorkflowIntent(intent.intent)
            is SkillsWorkflowIntent.ForwardToWizard -> handleWizardIntent(intent.intent)
            SkillsWorkflowIntent.DismissError -> handleDismissError()
        }
    }

    // =============================================================
    // Tab Selection
    // =============================================================
    private fun handleSelectTab(tab: SkillsWorkflowTab) {
        _state.update { it.copy(activeTab = tab) }
        // Load data when switching to specific tabs / 切换到特定 Tab 时加载数据
        when (tab) {
            SkillsWorkflowTab.SKILL_HUB -> {
                if (_state.value.skillHubState.installedSkills.isEmpty()) {
                    handleSkillHubIntent(SkillHubIntent.LoadSkills)
                }
            }
            SkillsWorkflowTab.WIZARD -> {
                handleWizardIntent(WizardIntent.CheckCliStatus)
            }
            else -> { /* Detail/Workflow have their own loading */ }
        }
    }

    // =============================================================
    // Skill Hub Intent Handlers
    // =============================================================
    private fun handleSkillHubIntent(intent: SkillHubIntent) {
        when (intent) {
            is SkillHubIntent.LoadSkills -> loadSkills()
            is SkillHubIntent.SearchSkills -> searchSkills(intent.query)
            is SkillHubIntent.FilterByCategory -> filterByCategory(intent.category)
            is SkillHubIntent.InstallSkill -> installSkill(intent.skillId)
            is SkillHubIntent.UninstallSkill -> uninstallSkill(intent.skillId)
            is SkillHubIntent.OpenSkillDetail -> openSkillDetail(intent.skillId)
            SkillHubIntent.DismissError -> dismissHubError()
        }
    }

    /**
     * Load skills from local installation and market.
     * Simulates API call with demo data.
     */
    private fun loadSkills() {
        viewModelScope.launch {
            _state.update {
                it.copy(skillHubState = it.skillHubState.copy(isLoading = true, error = null))
            }
            try {
                // Simulate network delay / 模拟网络延迟
                delay(500)

                // Demo data for installed skills / 已安装 Skill 的演示数据
                val installedSkills = listOf(
                    SkillInfo(
                        id = "@android/migrate-to-compose",
                        name = "Migrate to Compose",
                        version = "v2.3.0",
                        author = "Google",
                        description = "XML to Jetpack Compose migration toolkit",
                        category = SkillCategory.MIGRATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 15420,
                        rating = 4.8f,
                        tags = listOf("compose", "migration", "xml"),
                        installCommand = "gh skill install android/migrate-to-compose"
                    ),
                    SkillInfo(
                        id = "@android/upgrade-agp9",
                        name = "Upgrade AGP 9",
                        version = "v1.5.0",
                        author = "Google",
                        description = "AGP 9.0 upgrade assistant and compliance checker",
                        category = SkillCategory.MIGRATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 8930,
                        rating = 4.6f,
                        tags = listOf("agp", "upgrade", "migration"),
                        installCommand = "gh skill install android/upgrade-agp9"
                    ),
                    SkillInfo(
                        id = "@microagents/navigation3-best-practices",
                        name = "Navigation 3 Best Practices",
                        version = "v1.0.0",
                        author = "MicroAgents",
                        description = "Navigation 3 migration and best practices guide",
                        category = SkillCategory.DOCUMENTATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 3240,
                        rating = 4.9f,
                        tags = listOf("navigation", "compose", "best-practices"),
                        installCommand = "gh skill install microagents/navigation3-best-practices"
                    )
                )

                // Demo data for market skills / 市场 Skill 的演示数据
                val marketSkills = listOf(
                    SkillInfo(
                        id = "@android/r8-config-audit",
                        name = "R8 Config Audit",
                        version = "v1.2.0",
                        author = "Google",
                        description = "R8/D8 configuration audit and optimization suggestions",
                        category = SkillCategory.AUDIT,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 12500,
                        rating = 4.5f,
                        tags = listOf("r8", "audit", "optimization"),
                        installCommand = "gh skill install android/r8-config-audit"
                    ),
                    SkillInfo(
                        id = "@android/upgrade-play-billing",
                        name = "Upgrade Play Billing",
                        version = "v3.0.0",
                        author = "Google",
                        description = "Play Billing Library upgrade assistant",
                        category = SkillCategory.MIGRATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 7800,
                        rating = 4.7f,
                        tags = listOf("play-billing", "upgrade", "payments"),
                        installCommand = "gh skill install android/upgrade-play-billing"
                    ),
                    SkillInfo(
                        id = "@android/edge-to-edge",
                        name = "Make App Edge-to-Edge",
                        version = "v2.0.0",
                        author = "Google",
                        description = "Convert app to edge-to-edge display",
                        category = SkillCategory.OPTIMIZATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 21000,
                        rating = 4.9f,
                        tags = listOf("edge-to-edge", "display", "ui"),
                        installCommand = "gh skill install android/edge-to-edge"
                    ),
                    SkillInfo(
                        id = "@community/kotlin-best-practices",
                        name = "Kotlin Best Practices",
                        version = "v3.1.0",
                        author = "JetBrains",
                        description = "Kotlin coding best practices and style guide",
                        category = SkillCategory.DOCUMENTATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 45000,
                        rating = 4.8f,
                        tags = listOf("kotlin", "best-practices", "style"),
                        installCommand = "gh skill install community/kotlin-best-practices"
                    ),
                    SkillInfo(
                        id = "@community/compose-perf",
                        name = "Compose Performance",
                        version = "v1.5.0",
                        author = "社区",
                        description = "Jetpack Compose performance optimization guide",
                        category = SkillCategory.OPTIMIZATION,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 18900,
                        rating = 4.6f,
                        tags = listOf("compose", "performance", "optimization"),
                        installCommand = "gh skill install community/compose-perf"
                    ),
                    SkillInfo(
                        id = "@community/ci-github-actions",
                        name = "GitHub Actions CI",
                        version = "v2.0.0",
                        author = "Community",
                        description = "GitHub Actions CI template for Android projects",
                        category = SkillCategory.CI_CD,
                        status = SkillStatus.PUBLISHED,
                        downloadCount = 9200,
                        rating = 4.4f,
                        tags = listOf("ci", "github-actions", "automation"),
                        installCommand = "gh skill install community/ci-github-actions"
                    )
                )

                _state.update {
                    it.copy(
                        skillHubState = it.skillHubState.copy(
                            installedSkills = installedSkills,
                            marketSkills = marketSkills,
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        skillHubState = it.skillHubState.copy(
                            isLoading = false,
                            error = "Failed to load skills: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun searchSkills(query: String) {
        _state.update {
            it.copy(skillHubState = it.skillHubState.copy(searchQuery = query))
        }
    }

    private fun filterByCategory(category: SkillCategory?) {
        _state.update {
            it.copy(skillHubState = it.skillHubState.copy(selectedCategory = category))
        }
    }

    private fun installSkill(skillId: String) {
        viewModelScope.launch {
            val skill = _state.value.skillHubState.marketSkills.find { it.id == skillId }
            if (skill != null) {
                // Simulate installation / 模拟安装
                delay(800)
                val updatedMarket = _state.value.skillHubState.marketSkills.filter { it.id != skillId }
                val updatedInstalled = _state.value.skillHubState.installedSkills + skill
                _state.update {
                    it.copy(
                        skillHubState = it.skillHubState.copy(
                            marketSkills = updatedMarket,
                            installedSkills = updatedInstalled
                        )
                    )
                }
                _effect.emit(SkillsWorkflowEffect.ShowSnackbar("✓ Installed: ${skill.name}", false))
            }
        }
    }

    private fun uninstallSkill(skillId: String) {
        viewModelScope.launch {
            val skill = _state.value.skillHubState.installedSkills.find { it.id == skillId }
            if (skill != null) {
                delay(400)
                val updatedInstalled = _state.value.skillHubState.installedSkills.filter { it.id != skillId }
                val updatedMarket = _state.value.skillHubState.marketSkills + skill
                _state.update {
                    it.copy(
                        skillHubState = it.skillHubState.copy(
                            installedSkills = updatedInstalled,
                            marketSkills = updatedMarket
                        )
                    )
                }
                _effect.emit(SkillsWorkflowEffect.ShowSnackbar("✓ Uninstalled: ${skill.name}", false))
            }
        }
    }

    private fun openSkillDetail(skillId: String) {
        _state.update { it.copy(activeTab = SkillsWorkflowTab.SKILL_DETAIL) }
        handleSkillDetailIntent(SkillDetailIntent.LoadSkillDetail(skillId))
    }

    private fun dismissHubError() {
        _state.update { it.copy(skillHubState = it.skillHubState.copy(error = null)) }
    }

    // =============================================================
    // Skill Detail Intent Handlers
    // =============================================================
    private fun handleSkillDetailIntent(intent: SkillDetailIntent) {
        when (intent) {
            is SkillDetailIntent.LoadSkillDetail -> loadSkillDetail(intent.skillId)
            is SkillDetailIntent.RunTest -> runSkillTest(intent.skillId)
            is SkillDetailIntent.PublishSkill -> publishSkill(intent.skillId)
            is SkillDetailIntent.CopyInstallCommand -> copyInstallCommand(intent.command)
            SkillDetailIntent.DismissError -> dismissDetailError()
        }
    }

    private fun loadSkillDetail(skillId: String) {
        viewModelScope.launch {
            _state.update { it.copy(skillDetailState = it.skillDetailState.copy(isLoading = true, error = null)) }
            try {
                delay(400)

                // Find in installed or market / 在已安装或市场中查找
                val skill = _state.value.skillHubState.installedSkills.find { it.id == skillId }
                    ?: _state.value.skillHubState.marketSkills.find { it.id == skillId }

                if (skill != null) {
                    // Demo readme / 演示 readme
                    val demoReadme = """
                        # ${skill.name}

                        ## Description / 描述
                        ${skill.description}

                        ## Installation / 安装
                        ```bash
                        ${skill.fullInstallCommand}
                        ```

                        ## Usage / 使用方法
                        1. Install the skill using the command above
                        2. Configure your project to use the skill
                        3. Run the skill with your AI Agent

                        ## Categories / 分类
                        ${skill.tags.joinToString(", ") { "``$it``" }}

                        ## Version History / 版本历史
                        - **${skill.version}** (current)
                        - v1.0.0 - Initial release
                    """.trimIndent()

                    val versionHistory = listOf("v${skill.version}", "v1.0.0")

                    _state.update {
                        it.copy(
                            skillDetailState = it.skillDetailState.copy(
                                skill = skill,
                                readme = demoReadme,
                                versionHistory = versionHistory,
                                isLoading = false
                            )
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            skillDetailState = it.skillDetailState.copy(
                                isLoading = false,
                                error = "Skill not found: $skillId"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        skillDetailState = it.skillDetailState.copy(
                            isLoading = false,
                            error = "Failed to load skill detail: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun runSkillTest(skillId: String) {
        viewModelScope.launch {
            _state.update { it.copy(skillDetailState = it.skillDetailState.copy(isRunningTest = true)) }
            try {
                delay(1500)

                // Demo test report / 演示测试报告
                val report = TestReport(
                    skillId = skillId,
                    passedTestCount = 8,
                    failedTestCount = 1,
                    totalTestCount = 9,
                    coverage = 85,
                    executionTimeMs = 1234,
                    testResults = listOf(
                        TestResult("test_trigger_conditions", true, "All triggers validated", 120),
                        TestResult("test_tools_available", true, "All tools accessible", 89),
                        TestResult("test_workflow_steps", true, "Workflow complete", 201),
                        TestResult("test_documentation", true, "SKILL.md complete", 156),
                        TestResult("test_examples", true, "All examples valid", 198),
                        TestResult("test_cli_integration", false, "CLI command not found in PATH", 245),
                        TestResult("test_permission_model", true, "Permission levels correct", 78),
                        TestResult("test_version_format", true, "SemVer compliant", 45),
                        TestResult("test_dependencies", true, "All dependencies resolvable", 102)
                    )
                )

                _state.update {
                    it.copy(skillDetailState = it.skillDetailState.copy(
                        testReport = report,
                        isRunningTest = false
                    ))
                }
                _effect.emit(SkillsWorkflowEffect.ShowSnackbar("⚠ Test completed: 8/9 passed", false))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        skillDetailState = it.skillDetailState.copy(
                            isRunningTest = false,
                            error = "Test failed: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    private fun publishSkill(skillId: String) {
        viewModelScope.launch {
            delay(1000)
            _effect.emit(SkillsWorkflowEffect.ShowSnackbar("✓ Published: $skillId", false))
        }
    }

    private fun copyInstallCommand(command: String) {
        viewModelScope.launch {
            _effect.emit(SkillsWorkflowEffect.ShowSnackbar("📋 Copied: $command", false))
        }
    }

    private fun dismissDetailError() {
        _state.update { it.copy(skillDetailState = it.skillDetailState.copy(error = null)) }
    }

    // =============================================================
    // Workflow Editor Intent Handlers
    // =============================================================
    private fun handleWorkflowIntent(intent: WorkflowIntent) {
        when (intent) {
            is WorkflowIntent.AddNode -> addWorkflowNode(intent.skillId)
            is WorkflowIntent.RemoveNode -> removeWorkflowNode(intent.nodeId)
            is WorkflowIntent.SelectNode -> selectWorkflowNode(intent.nodeId)
            is WorkflowIntent.MoveNode -> moveWorkflowNode(intent.nodeId, intent.deltaX, intent.deltaY)
            is WorkflowIntent.ConnectNodes -> connectNodes(intent.fromNodeId, intent.toNodeId, intent.condition)
            is WorkflowIntent.DisconnectNodes -> disconnectNodes(intent.edgeId)
            is WorkflowIntent.SetEntryPoint -> setEntryPoint(intent.nodeId)
            is WorkflowIntent.UpdateWorkflowMeta -> updateWorkflowMeta(intent.name, intent.description)
            is WorkflowIntent.ExecuteWorkflow -> executeWorkflow()
            is WorkflowIntent.SaveWorkflow -> saveWorkflow()
            is WorkflowIntent.LoadWorkflow -> loadWorkflow(intent.workflowId)
            is WorkflowIntent.ClearAll -> clearWorkflow()
            WorkflowIntent.DismissError -> dismissWorkflowError()
        }
    }

    private fun addWorkflowNode(skillId: String) {
        val newNode = WorkflowNode(
            id = UUID.randomUUID().toString(),
            skillId = skillId,
            skillName = skillId.removePrefix("@").replace("/", " / "),
            positionX = 100f + (_state.value.workflowState.nodes.size * 50f) % 300f,
            positionY = 100f + (_state.value.workflowState.nodes.size * 30f) % 200f,
            isEntryPoint = _state.value.workflowState.nodes.isEmpty()
        )
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                nodes = it.workflowState.nodes + newNode
            ))
        }
    }

    private fun removeWorkflowNode(nodeId: String) {
        val updatedNodes = _state.value.workflowState.nodes.filter { it.id != nodeId }
        val updatedEdges = _state.value.workflowState.edges.filter {
            it.fromNodeId != nodeId && it.toNodeId != nodeId
        }
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                nodes = updatedNodes,
                edges = updatedEdges,
                selectedNodeId = if (it.workflowState.selectedNodeId == nodeId) null else it.workflowState.selectedNodeId
            ))
        }
    }

    private fun selectWorkflowNode(nodeId: String?) {
        _state.update { it.copy(workflowState = it.workflowState.copy(selectedNodeId = nodeId)) }
    }

    private fun moveWorkflowNode(nodeId: String, deltaX: Float, deltaY: Float) {
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                nodes = it.workflowState.nodes.map { node ->
                    if (node.id == nodeId) {
                        node.copy(positionX = node.positionX + deltaX, positionY = node.positionY + deltaY)
                    } else node
                }
            ))
        }
    }

    private fun connectNodes(fromNodeId: String, toNodeId: String, condition: String) {
        // Prevent duplicate edges / 防止重复边
        val exists = _state.value.workflowState.edges.any {
            it.fromNodeId == fromNodeId && it.toNodeId == toNodeId
        }
        if (!exists && fromNodeId != toNodeId) {
            val newEdge = WorkflowEdge(
                id = UUID.randomUUID().toString(),
                fromNodeId = fromNodeId,
                toNodeId = toNodeId,
                condition = condition
            )
            _state.update {
                it.copy(workflowState = it.workflowState.copy(
                    edges = it.workflowState.edges + newEdge
                ))
            }
        }
    }

    private fun disconnectNodes(edgeId: String) {
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                edges = it.workflowState.edges.filter { it.id != edgeId }
            ))
        }
    }

    private fun setEntryPoint(nodeId: String) {
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                nodes = it.workflowState.nodes.map { node ->
                    node.copy(isEntryPoint = node.id == nodeId)
                }
            ))
        }
    }

    private fun updateWorkflowMeta(name: String, description: String) {
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                workflowName = name,
                workflowDescription = description
            ))
        }
    }

    private fun executeWorkflow() {
        viewModelScope.launch {
            _state.update { it.copy(workflowState = it.workflowState.copy(isExecuting = true, executionLog = emptyList())) }

            val entryNode = _state.value.workflowState.nodes.find { it.isEntryPoint }
            if (entryNode == null) {
                _state.update {
                    it.copy(workflowState = it.workflowState.copy(
                        isExecuting = false,
                        executionLog = listOf("❌ No entry point defined")
                    ))
                }
                return@launch
            }

            val logLines = mutableListOf<String>()
            logLines.add("🚀 Starting workflow execution...")
            logLines.add("📍 Entry node: ${entryNode.skillName}")

            // Simulate execution steps / 模拟执行步骤
            val sortedNodes = topologicalSort(entryNode.id)
            for ((index, node) in sortedNodes.withIndex()) {
                delay(400)
                logLines.add("  [$index] ✓ Executing: ${node.skillName}")
                _state.update {
                    it.copy(workflowState = it.workflowState.copy(executionLog = logLines.toList()))
                }
            }

            delay(200)
            logLines.add("✅ Workflow completed successfully!")
            _state.update {
                it.copy(workflowState = it.workflowState.copy(
                    isExecuting = false,
                    executionLog = logLines
                ))
            }
        }
    }

    /**
     * Simple topological sort for workflow execution order.
     * 简单的工作流执行顺序拓扑排序。
     */
    private fun topologicalSort(startNodeId: String): List<WorkflowNode> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<WorkflowNode>()
        val nodesMap = _state.value.workflowState.nodes.associateBy { it.id }

        fun dfs(nodeId: String) {
            if (nodeId in visited) return
            visited.add(nodeId)
            nodesMap[nodeId]?.let { result.add(it) }
            _state.value.workflowState.edges
                .filter { it.fromNodeId == nodeId }
                .forEach { dfs(it.toNodeId) }
        }

        dfs(startNodeId)
        return result
    }

    private fun saveWorkflow() {
        viewModelScope.launch {
            _state.update { it.copy(workflowState = it.workflowState.copy(isSaving = true)) }
            delay(600)
            _state.update {
                it.copy(workflowState = it.workflowState.copy(isSaving = false))
            }
            _effect.emit(SkillsWorkflowEffect.ShowSnackbar("✓ Workflow saved", false))
        }
    }

    private fun loadWorkflow(workflowId: String) {
        viewModelScope.launch {
            delay(500)
            _effect.emit(SkillsWorkflowEffect.ShowSnackbar("Loaded: $workflowId", false))
        }
    }

    private fun clearWorkflow() {
        _state.update {
            it.copy(workflowState = it.workflowState.copy(
                nodes = emptyList(),
                edges = emptyList(),
                selectedNodeId = null,
                workflowName = "",
                workflowDescription = "",
                executionLog = emptyList()
            ))
        }
    }

    private fun dismissWorkflowError() {
        _state.update { it.copy(workflowState = it.workflowState.copy(error = null)) }
    }

    // =============================================================
    // Setup Wizard Intent Handlers
    // =============================================================
    private fun handleWizardIntent(intent: WizardIntent) {
        when (intent) {
            is WizardIntent.NextStep -> nextWizardStep()
            is WizardIntent.PreviousStep -> previousWizardStep()
            is WizardIntent.GoToStep -> goToWizardStep(intent.stepIndex)
            is WizardIntent.CompleteCurrentStep -> completeCurrentWizardStep()
            is WizardIntent.InstallAndroidCli -> installAndroidCli()
            is WizardIntent.CheckCliStatus -> checkCliStatus()
            is WizardIntent.InstallFirstSkill -> installFirstSkill(intent.skillId)
            is WizardIntent.CreateFirstSkill -> createFirstSkill()
            is WizardIntent.RunVerification -> runVerification()
            is WizardIntent.ResetWizard -> resetWizard()
            WizardIntent.DismissError -> dismissWizardError()
        }
    }

    private fun nextWizardStep() {
        val currentStep = _state.value.wizardState.currentStep
        val maxSteps = _state.value.wizardState.steps.size
        if (currentStep < maxSteps - 1) {
            _state.update {
                it.copy(wizardState = it.wizardState.copy(currentStep = currentStep + 1))
            }
        }
    }

    private fun previousWizardStep() {
        val currentStep = _state.value.wizardState.currentStep
        if (currentStep > 0) {
            _state.update {
                it.copy(wizardState = it.wizardState.copy(currentStep = currentStep - 1))
            }
        }
    }

    private fun goToWizardStep(stepIndex: Int) {
        if (stepIndex in 0 until _state.value.wizardState.steps.size) {
            _state.update { it.copy(wizardState = it.wizardState.copy(currentStep = stepIndex)) }
        }
    }

    private fun completeCurrentWizardStep() {
        val currentStep = _state.value.wizardState.currentStep
        _state.update {
            it.copy(wizardState = it.wizardState.copy(
                steps = it.wizardState.steps.mapIndexed { index, step ->
                    if (index == currentStep) step.copy(isCompleted = true) else step
                }
            ))
        }
    }

    private fun installAndroidCli() {
        viewModelScope.launch {
            _state.update { it.copy(wizardState = it.wizardState.copy(isInstallingCli = true)) }
            try {
                delay(2000) // Simulate installation / 模拟安装
                _state.update {
                    it.copy(wizardState = it.wizardState.copy(
                        isInstallingCli = false,
                        androidCliInstalled = true,
                        cliVersion = "1.0.0"
                    ))
                }
                _effect.emit(SkillsWorkflowEffect.ShowSnackbar("✓ Android CLI installed!", false))
            } catch (e: Exception) {
                _state.update {
                    it.copy(wizardState = it.wizardState.copy(
                        isInstallingCli = false,
                        error = "CLI installation failed: ${e.message}"
                    ))
                }
            }
        }
    }

    private fun checkCliStatus() {
        viewModelScope.launch {
            delay(300)
            // Simulate CLI check / 模拟 CLI 检查
            _state.update {
                it.copy(wizardState = it.wizardState.copy(
                    androidCliInstalled = false,
                    cliVersion = ""
                ))
            }
        }
    }

    private fun installFirstSkill(skillId: String) {
        handleSkillHubIntent(SkillHubIntent.InstallSkill(skillId))
        _state.update {
            it.copy(wizardState = it.wizardState.copy(
                installedSkillsCount = it.wizardState.installedSkillsCount + 1
            ))
        }
    }

    private fun createFirstSkill() {
        viewModelScope.launch {
            _effect.emit(SkillsWorkflowEffect.ShowSnackbar("Opening Skill Creator...", false))
            _state.update {
                it.copy(wizardState = it.wizardState.copy(firstSkillCreated = true))
            }
        }
    }

    private fun runVerification() {
        viewModelScope.launch {
            delay(1000)
            val wizardState = _state.value.wizardState
            val isSuccess = wizardState.androidCliInstalled && wizardState.installedSkillsCount > 0
            val message = if (isSuccess) {
                "✅ All verifications passed!\n- CLI: ${wizardState.cliVersion}\n- Skills installed: ${wizardState.installedSkillsCount}"
            } else {
                "⚠️ Some verifications pending.\n- CLI installed: ${wizardState.androidCliInstalled}\n- Skills installed: ${wizardState.installedSkillsCount}"
            }
            _effect.emit(SkillsWorkflowEffect.ShowVerificationResult(isSuccess, message))
        }
    }

    private fun resetWizard() {
        _state.update {
            it.copy(wizardState = it.wizardState.copy(
                currentStep = 0,
                androidCliInstalled = false,
                cliVersion = "",
                installedSkillsCount = 0,
                firstSkillCreated = false,
                steps = it.wizardState.steps.map { step -> step.copy(isCompleted = false) }
            ))
        }
    }

    private fun dismissWizardError() {
        _state.update { it.copy(wizardState = it.wizardState.copy(error = null)) }
    }

    // =============================================================
    // Global Error Handling
    // =============================================================
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
    }
}
