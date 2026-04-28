package com.mvi.kenny.feature.androidskills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// AndroidSkillsViewModel — Android CLI & Android Skills 工具包 ViewModel
// PRD-184: Android CLI & Android Skills 工具包
// =============================================================
/**
 * ViewModel for Android Skills Toolkit / Android Skills 工具包 ViewModel
 *
 * Manages all state transitions following MVI pattern.
 * ViewModelScope is used for all coroutine operations.
 *
 * Key responsibilities:
 * - Skill creation workflow management
 * - Skill quality audit & validation
 * - Skill publishing simulation
 * - SKILL.md generation
 *
 * @see AndroidSkillsContract For State, Intent, Effect definitions
 * @see AndroidSkillsScreen For UI implementation
 */
class AndroidSkillsViewModel : ViewModel() {

    // ---------------------------------------------------------
    // State — single source of truth for UI
    // ---------------------------------------------------------
    private val _state = MutableStateFlow(AndroidSkillsState.Initial)
    val state: StateFlow<AndroidSkillsState> = _state.asStateFlow()

    // ---------------------------------------------------------
    // Effect — one-time events for UI
    // ---------------------------------------------------------
    private val _effect = MutableSharedFlow<AndroidSkillsEffect>()
    val effect: SharedFlow<AndroidSkillsEffect> = _effect.asSharedFlow()

    // ---------------------------------------------------------
    // Skill Creator dedicated effect channel
    // ---------------------------------------------------------
    private val _skillCreatorEffect = MutableSharedFlow<SkillCreatorEffect>()
    val skillCreatorEffect: SharedFlow<SkillCreatorEffect> = _skillCreatorEffect.asSharedFlow()

    // ---------------------------------------------------------
    // Intent processing — process user actions
    // ---------------------------------------------------------
    /**
     * Process user intent / 处理用户意图
     *
     * Called from UI layer when user performs an action.
     * Each when branch handles one Intent type.
     */
    fun processIntent(intent: AndroidSkillsIntent) {
        when (intent) {
            is AndroidSkillsIntent.SelectTab -> handleSelectTab(intent.tab)
            is AndroidSkillsIntent.ForwardToSkillCreator -> handleForwardToSkillCreator(intent.intent)
            is AndroidSkillsIntent.DismissError -> handleDismissError()
        }
    }

    /**
     * Process Skill Creator intent / 处理 Skill Creator 意图
     *
     * Delegates to [handleSkillCreatorIntent] for Skill Creator specific intents.
     */
    private fun handleForwardToSkillCreator(intent: SkillCreatorIntent) {
        viewModelScope.launch {
            handleSkillCreatorIntent(intent)
        }
    }

    // ---------------------------------------------------------
    // Tab selection handler
    // ---------------------------------------------------------
    /**
     * Handle tab selection / 处理 Tab 选择
     *
     * @param tab Tab to select / 要选择的 Tab
     */
    private fun handleSelectTab(tab: AndroidSkillsTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    // ---------------------------------------------------------
    // Skill Creator Intent handlers
    // ---------------------------------------------------------
    /**
     * Handle Skill Creator intent / 处理 Skill Creator 意图
     *
     * @param intent Skill Creator intent / Skill Creator 意图
     */
    private suspend fun handleSkillCreatorIntent(intent: SkillCreatorIntent) {
        when (intent) {
            is SkillCreatorIntent.SelectSkillType -> handleSelectSkillType(intent.type)
            is SkillCreatorIntent.UpdateSkillName -> handleUpdateSkillName(intent.name)
            is SkillCreatorIntent.UpdateSkillDescription -> handleUpdateSkillDescription(intent.description)
            is SkillCreatorIntent.AddTriggerCondition -> handleAddTriggerCondition(intent.condition)
            is SkillCreatorIntent.RemoveTriggerCondition -> handleRemoveTriggerCondition(intent.condition)
            is SkillCreatorIntent.AddTool -> handleAddTool(intent.tool)
            is SkillCreatorIntent.RemoveTool -> handleRemoveTool(intent.tool)
            is SkillCreatorIntent.AddWorkflowStep -> handleAddWorkflowStep(intent.step)
            is SkillCreatorIntent.RemoveWorkflowStep -> handleRemoveWorkflowStep(intent.stepIndex)
            is SkillCreatorIntent.RunAudit -> handleRunAudit()
            is SkillCreatorIntent.Publish -> handlePublish()
            is SkillCreatorIntent.SelectTemplate -> handleSelectTemplate(intent.template)
            is SkillCreatorIntent.GenerateSkillMd -> handleGenerateSkillMd()
            is SkillCreatorIntent.CopyToClipboard -> handleCopyToClipboard()
            is SkillCreatorIntent.ExportSkill -> handleExportSkill(intent.path)
            is SkillCreatorIntent.ClearAll -> handleClearAll()
            is SkillCreatorIntent.DismissError -> handleDismissError()
        }
    }

    /**
     * Handle skill type selection / 处理 Skill 类型选择
     *
     * @param type Selected skill type / 选中的 Skill 类型
     */
    private fun handleSelectSkillType(type: SkillType) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    skillType = type,
                    // Auto-populate tools based on type / 根据类型自动填充工具
                    tools = getDefaultToolsForType(type),
                    // Auto-populate workflow based on type / 根据类型自动填充工作流
                    workflow = getDefaultWorkflowForType(type)
                )
            )
        }
    }

    /**
     * Handle skill name update / 处理 Skill 名称更新
     */
    private fun handleUpdateSkillName(name: String) {
        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(skillName = name))
        }
    }

    /**
     * Handle skill description update / 处理 Skill 描述更新
     */
    private fun handleUpdateSkillDescription(description: String) {
        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(skillDescription = description))
        }
    }

    /**
     * Handle add trigger condition / 处理添加触发条件
     */
    private fun handleAddTriggerCondition(condition: String) {
        if (condition.isBlank()) return
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    triggerConditions = it.skillCreatorState.triggerConditions + condition.trim()
                )
            )
        }
    }

    /**
     * Handle remove trigger condition / 处理移除触发条件
     */
    private fun handleRemoveTriggerCondition(condition: String) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    triggerConditions = it.skillCreatorState.triggerConditions.filter { it != condition }
                )
            )
        }
    }

    /**
     * Handle add tool / 处理添加工具
     */
    private fun handleAddTool(tool: SkillTool) {
        if (tool in _state.value.skillCreatorState.tools) return
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    tools = it.skillCreatorState.tools + tool
                )
            )
        }
    }

    /**
     * Handle remove tool / 处理移除工具
     */
    private fun handleRemoveTool(tool: SkillTool) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    tools = it.skillCreatorState.tools.filter { it.id != tool.id }
                )
            )
        }
    }

    /**
     * Handle add workflow step / 处理添加工作流步骤
     */
    private fun handleAddWorkflowStep(step: WorkflowStep) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    workflow = it.skillCreatorState.workflow + step
                )
            )
        }
    }

    /**
     * Handle remove workflow step / 处理移除工作流步骤
     */
    private fun handleRemoveWorkflowStep(stepIndex: Int) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    workflow = it.skillCreatorState.workflow.filterIndexed { index, _ -> index != stepIndex }
                )
            )
        }
    }

    /**
     * Handle run audit / 处理运行审核
     *
     * Triggers quality scoring and validation checks.
     * Simulates audit process with demo results.
     */
    private suspend fun handleRunAudit() {
        val currentState = _state.value.skillCreatorState

        // Validation check first / 首先进行验证检查
        if (currentState.validationErrors.isNotEmpty()) {
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowAuditWarning(currentState.validationErrors.first())
            )
            return
        }

        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(isRunningAudit = true))
        }

        try {
            val result = withContext(Dispatchers.Default) {
                simulateAudit(currentState)
            }

            val qualityScore = result.score ?: calculateQualityScore(currentState)

            _state.update {
                it.copy(
                    skillCreatorState = it.skillCreatorState.copy(
                        isRunningAudit = false,
                        auditResult = result,
                        qualityScore = qualityScore
                    )
                )
            }

            _skillCreatorEffect.emit(SkillCreatorEffect.ShowQualityScore(qualityScore))

            if (!result.isPassed) {
                result.warnings.firstOrNull()?.let {
                    _skillCreatorEffect.emit(SkillCreatorEffect.ShowAuditWarning(it))
                }
            }

        } catch (e: Exception) {
            _state.update {
                it.copy(
                    skillCreatorState = it.skillCreatorState.copy(
                        isRunningAudit = false,
                        error = "Audit failed: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Handle publish / 处理发布
     */
    private suspend fun handlePublish() {
        val currentState = _state.value.skillCreatorState

        if (!currentState.canPublish) {
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowSnackbar(
                    "Please complete all required fields before publishing / 请完善所有必填项后再发布",
                    isError = true
                )
            )
            return
        }

        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(isPublishing = true))
        }

        try {
            // Simulate publishing process / 模拟发布过程
            delay(2000)

            val repoUrl = "https://github.com/example/android-skill-${currentState.skillName.lowercase().replace(" ", "-")}"

            _state.update {
                it.copy(
                    skillCreatorState = it.skillCreatorState.copy(isPublishing = false)
                )
            }

            _skillCreatorEffect.emit(SkillCreatorEffect.PublishSuccess(repoUrl))
            _skillCreatorEffect.emit(SkillCreatorEffect.NavigateToPublish(repoUrl))

        } catch (e: Exception) {
            _state.update {
                it.copy(
                    skillCreatorState = it.skillCreatorState.copy(
                        isPublishing = false,
                        error = "Publish failed: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Handle select template / 处理选择模板
     */
    private fun handleSelectTemplate(template: SkillTemplate) {
        _state.update {
            it.copy(
                skillCreatorState = it.skillCreatorState.copy(
                    selectedTemplate = template,
                    skillType = template.category,
                    skillName = template.name,
                    skillDescription = template.description
                )
            )
        }
    }

    /**
     * Handle generate SKILL.md / 处理生成 SKILL.md
     */
    private suspend fun handleGenerateSkillMd() {
        val currentState = _state.value.skillCreatorState

        if (currentState.skillType == null || currentState.skillName.isBlank()) {
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowSnackbar(
                    "Please select a skill type and name first / 请先选择 Skill 类型和名称",
                    isError = true
                )
            )
            return
        }

        val content = generateSkillMdContent(currentState)

        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(generatedSkillMd = content))
        }

        _skillCreatorEffect.emit(
            SkillCreatorEffect.ShowSnackbar("SKILL.md generated / SKILL.md 已生成")
        )
    }

    /**
     * Handle copy to clipboard / 处理复制到剪贴板
     */
    private suspend fun handleCopyToClipboard() {
        val content = _state.value.skillCreatorState.generatedSkillMd

        if (content.isBlank()) {
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowSnackbar(
                    "Generate SKILL.md first / 请先生成 SKILL.md",
                    isError = true
                )
            )
            return
        }

        _skillCreatorEffect.emit(SkillCreatorEffect.CopySuccess(content))
        _skillCreatorEffect.emit(
            SkillCreatorEffect.ShowSnackbar("Copied to clipboard / 已复制到剪贴板")
        )
    }

    /**
     * Handle export skill / 处理导出 Skill
     *
     * @param path Target directory path / 目标目录路径
     */
    private suspend fun handleExportSkill(path: String) {
        val currentState = _state.value.skillCreatorState

        if (currentState.generatedSkillMd.isBlank()) {
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowSnackbar(
                    "Generate SKILL.md first / 请先生成 SKILL.md",
                    isError = true
                )
            )
            return
        }

        _state.update { it.copy(isLoading = true) }

        try {
            withContext(Dispatchers.IO) {
                val file = File("$path/SKILL.md")
                file.parentFile?.mkdirs()
                file.writeText(currentState.generatedSkillMd)
            }

            _state.update { it.copy(isLoading = false) }
            _skillCreatorEffect.emit(SkillCreatorEffect.ExportSuccess("$path/SKILL.md"))
            _skillCreatorEffect.emit(
                SkillCreatorEffect.ShowSnackbar("Exported to: $path/SKILL.md")
            )

        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Handle clear all / 处理清除所有
     */
    private fun handleClearAll() {
        _state.update {
            it.copy(skillCreatorState = SkillCreatorState.Initial)
        }
    }

    /**
     * Handle dismiss error / 处理关闭错误
     */
    private fun handleDismissError() {
        _state.update { it.copy(error = null) }
        _state.update {
            it.copy(skillCreatorState = it.skillCreatorState.copy(error = null))
        }
    }

    // ---------------------------------------------------------
    // Helper functions
    // ---------------------------------------------------------

    /**
     * Get default tools for skill type / 获取 Skill 类型的默认工具
     */
    private fun getDefaultToolsForType(type: SkillType): List<SkillTool> {
        return when (type) {
            SkillType.MIGRATION -> listOf(
                SkillTool("file_reader", "File Reader", "Read source files / 读取源文件"),
                SkillTool("file_writer", "File Writer", "Write modified files / 写入修改文件", PermissionLevel.WRITE),
                SkillTool("grep", "Grep Search", "Search code patterns / 搜索代码模式"),
                SkillTool("lint_check", "Lint Check", "Run Android lint / 运行 Android lint")
            )
            SkillType.OPTIMIZATION -> listOf(
                SkillTool("perf_profiler", "Performance Profiler", "Profile app performance / 分析应用性能"),
                SkillTool("memory_analyzer", "Memory Analyzer", "Analyze memory usage / 分析内存使用"),
                SkillTool("trace_viewer", "Trace Viewer", "View execution traces / 查看执行追踪")
            )
            SkillType.DEBUGGING -> listOf(
                SkillTool("log_reader", "Log Reader", "Read system logs / 读取系统日志"),
                SkillTool("r8_analyzer", "R8 Analyzer", "Analyze R8/D8 output / 分析 R8/D8 输出"),
                SkillTool("stack_trace", "Stack Trace Parser", "Parse exception stack traces / 解析异常堆栈")
            )
            SkillType.CI_CONFIG -> listOf(
                SkillTool("file_writer", "File Writer", "Write CI configuration / 写入 CI 配置", PermissionLevel.WRITE),
                SkillTool("gh_cli", "GitHub CLI", "Interact with GitHub / 与 GitHub 交互", PermissionLevel.EXECUTE),
                SkillTool("yaml_editor", "YAML Editor", "Edit YAML files / 编辑 YAML 文件", PermissionLevel.WRITE)
            )
            SkillType.DOCUMENTATION -> listOf(
                SkillTool("file_writer", "File Writer", "Write documentation / 写入文档", PermissionLevel.WRITE),
                SkillTool("code_reader", "Code Reader", "Read source code / 读取源代码"),
                SkillTool("markdown_renderer", "Markdown Renderer", "Render markdown preview / 渲染 Markdown 预览")
            )
            SkillType.AUDIT -> listOf(
                SkillTool("file_reader", "File Reader", "Read SKILL.md files / 读取 SKILL.md 文件"),
                SkillTool("quality_scorer", "Quality Scorer", "Calculate quality scores / 计算质量评分"),
                SkillTool("validator", "Validator", "Validate SKILL.md format / 验证 SKILL.md 格式")
            )
            SkillType.PUBLISH -> listOf(
                SkillTool("gh_cli", "GitHub CLI", "GitHub operations / GitHub 操作", PermissionLevel.EXECUTE),
                SkillTool("git_cli", "Git CLI", "Git operations / Git 操作", PermissionLevel.EXECUTE),
                SkillTool("file_writer", "File Writer", "Write release files / 写入发布文件", PermissionLevel.WRITE)
            )
            SkillType.CLI_GUIDE -> listOf(
                SkillTool("shell_exec", "Shell Executor", "Execute shell commands / 执行 Shell 命令", PermissionLevel.EXECUTE),
                SkillTool("file_reader", "File Reader", "Read CLI output / 读取 CLI 输出"),
                SkillTool("android_cli", "Android CLI", "Run android CLI commands / 运行 android CLI 命令")
            )
            SkillType.MCP_INTEGRATION -> listOf(
                SkillTool("mcp_client", "MCP Client", "MCP protocol client / MCP 协议客户端"),
                SkillTool("json_rpc", "JSON-RPC", "JSON-RPC communication / JSON-RPC 通信"),
                SkillTool("file_reader", "File Reader", "Read config files / 读取配置文件")
            )
        }
    }

    /**
     * Get default workflow for skill type / 获取 Skill 类型的默认工作流
     */
    private fun getDefaultWorkflowForType(type: SkillType): List<WorkflowStep> {
        return when (type) {
            SkillType.MIGRATION -> listOf(
                WorkflowStep(1, "Analyze Source", "Analyze existing codebase / 分析现有代码库", 10),
                WorkflowStep(2, "Identify Patterns", "Identify migration patterns / 识别迁移模式", 15),
                WorkflowStep(3, "Generate Migration", "Generate migrated code / 生成迁移代码", 20),
                WorkflowStep(4, "Validate", "Validate migrated code / 验证迁移代码", 10),
                WorkflowStep(5, "Report", "Generate migration report / 生成迁移报告", 5)
            )
            SkillType.OPTIMIZATION -> listOf(
                WorkflowStep(1, "Profile", "Profile current performance / 分析当前性能", 15),
                WorkflowStep(2, "Identify Bottlenecks", "Identify performance bottlenecks / 识别性能瓶颈", 10),
                WorkflowStep(3, "Apply Optimizations", "Apply optimizations / 应用优化", 20),
                WorkflowStep(4, "Verify", "Verify improvements / 验证改进", 10)
            )
            SkillType.DEBUGGING -> listOf(
                WorkflowStep(1, "Collect Logs", "Collect relevant logs / 收集相关日志", 5),
                WorkflowStep(2, "Analyze Traces", "Analyze stack traces / 分析堆栈跟踪", 10),
                WorkflowStep(3, "Identify Root Cause", "Identify root cause / 定位根本原因", 15),
                WorkflowStep(4, "Generate Fix", "Generate fix suggestions / 生成修复建议", 10)
            )
            SkillType.CI_CONFIG -> listOf(
                WorkflowStep(1, "Analyze Project", "Analyze project structure / 分析项目结构", 5),
                WorkflowStep(2, "Generate Config", "Generate CI configuration / 生成 CI 配置", 15),
                WorkflowStep(3, "Validate", "Validate configuration / 验证配置", 5),
                WorkflowStep(4, "Commit & Push", "Commit and push changes / 提交并推送更改", 5)
            )
            SkillType.DOCUMENTATION -> listOf(
                WorkflowStep(1, "Analyze Context", "Analyze skill context / 分析 Skill 上下文", 10),
                WorkflowStep(2, "Write SKILL.md", "Write SKILL.md content / 编写 SKILL.md 内容", 20),
                WorkflowStep(3, "Review", "Review documentation / 审查文档", 10),
                WorkflowStep(4, "Publish", "Publish to repository / 发布到仓库", 5)
            )
            SkillType.AUDIT -> listOf(
                WorkflowStep(1, "Read SKILL.md", "Read SKILL.md file / 读取 SKILL.md 文件", 5),
                WorkflowStep(2, "Validate Format", "Validate format compliance / 验证格式合规性", 10),
                WorkflowStep(3, "Score Quality", "Calculate quality scores / 计算质量评分", 10),
                WorkflowStep(4, "Generate Report", "Generate audit report / 生成审核报告", 5)
            )
            SkillType.PUBLISH -> listOf(
                WorkflowStep(1, "Validate Skill", "Validate skill content / 验证 Skill 内容", 10),
                WorkflowStep(2, "Create Release", "Create GitHub release / 创建 GitHub 发布", 10),
                WorkflowStep(3, "Tag Version", "Tag with version / 打版本标签", 5),
                WorkflowStep(4, "Publish", "Publish to GitHub / 发布到 GitHub", 10)
            )
            SkillType.CLI_GUIDE -> listOf(
                WorkflowStep(1, "Define CLI Interface", "Define CLI interface / 定义 CLI 接口", 10),
                WorkflowStep(2, "Implement Wrapper", "Implement CLI wrapper / 实现 CLI 封装", 20),
                WorkflowStep(3, "Test Commands", "Test CLI commands / 测试 CLI 命令", 15),
                WorkflowStep(4, "Document", "Document usage / 编写使用文档", 10)
            )
            SkillType.MCP_INTEGRATION -> listOf(
                WorkflowStep(1, "Analyze MCP Server", "Analyze MCP server capabilities / 分析 MCP 服务端能力", 15),
                WorkflowStep(2, "Define Interface", "Define skill-tool interface / 定义 Skill-Tool 接口", 15),
                WorkflowStep(3, "Implement Bridge", "Implement MCP bridge / 实现 MCP 桥接", 20),
                WorkflowStep(4, "Test Integration", "Test integration / 测试集成", 15)
            )
        }
    }

    /**
     * Simulate audit process / 模拟审核过程
     *
     * In production, this would perform actual SKILL.md validation.
     *
     * @param currentState Current skill creator state / 当前 Skill Creator 状态
     * @return Audit result / 审核结果
     */
    private suspend fun simulateAudit(currentState: SkillCreatorState): AuditResult {
        // Simulate processing time / 模拟处理时间
        delay(2000)

        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // Check required fields / 检查必填字段
        if (currentState.skillName.isBlank()) {
            errors.add("Skill name is required / Skill 名称不能为空")
        }

        if (currentState.triggerConditions.isEmpty()) {
            errors.add("At least one trigger condition is required / 至少需要一个触发条件")
        }

        if (currentState.tools.isEmpty()) {
            warnings.add("No tools defined. Consider adding tools for better usability. / 未定义工具，建议添加工具以提高可用性。")
        }

        if (currentState.workflow.isEmpty()) {
            warnings.add("No workflow defined. Consider adding workflow steps. / 未定义工作流，建议添加工作流步骤。")
        }

        // Check naming convention / 检查命名规范
        if (currentState.skillName.isNotBlank() && !currentState.skillName.matches(Regex("^[A-Za-z0-9][A-Za-z0-9 -]*$"))) {
            errors.add("Skill name should start with letter/number and contain only letters, numbers, spaces, and hyphens / Skill 名称应以字母/数字开头，只能包含字母、数字、空格和连字符")
        }

        // Generate suggestions / 生成建议
        if (currentState.triggerConditions.size < 3) {
            suggestions.add("Consider adding more specific trigger conditions for better accuracy / 建议添加更具体的触发条件以提高准确性")
        }

        if (currentState.skillDescription.isBlank()) {
            suggestions.add("Add a detailed description to help users understand the skill / 添加详细描述以帮助用户理解 Skill")
        }

        val isPassed = errors.isEmpty()

        return AuditResult(
            isPassed = isPassed,
            warnings = warnings,
            errors = errors,
            suggestions = suggestions
        )
    }

    /**
     * Calculate quality score / 计算质量评分
     */
    private fun calculateQualityScore(currentState: SkillCreatorState): QualityScore {
        val coverage = calculateCoverage(currentState)
        val accuracy = calculateAccuracy(currentState)
        val maintainability = calculateMaintainability(currentState)
        val documentation = calculateDocumentation(currentState)

        return QualityScore(coverage, accuracy, maintainability, documentation)
    }

    private fun calculateCoverage(state: SkillCreatorState): Int {
        var score = 40 // Base score / 基础分
        if (state.skillType != null) score += 15
        if (state.triggerConditions.isNotEmpty()) score += 15
        if (state.tools.isNotEmpty()) score += 15
        if (state.workflow.size >= 3) score += 15
        return score.coerceAtMost(100)
    }

    private fun calculateAccuracy(state: SkillCreatorState): Int {
        var score = 50 // Base score / 基础分
        if (state.skillName.isNotBlank()) score += 20
        if (state.skillDescription.isNotBlank()) score += 15
        if (state.triggerConditions.size >= 2) score += 15
        return score.coerceAtMost(100)
    }

    private fun calculateMaintainability(state: SkillCreatorState): Int {
        var score = 60 // Base score / 基础分
        if (state.workflow.isNotEmpty()) score += 20
        if (state.tools.isNotEmpty()) score += 20
        return score.coerceAtMost(100)
    }

    private fun calculateDocumentation(state: SkillCreatorState): Int {
        var score = 30 // Base score / 基础分
        if (state.skillDescription.isNotBlank()) score += 25
        if (state.triggerConditions.isNotEmpty()) score += 25
        if (state.workflow.isNotEmpty()) score += 20
        return score.coerceAtMost(100)
    }

    /**
     * Generate SKILL.md content / 生成 SKILL.md 内容
     *
     * @param state Current skill creator state / 当前 Skill Creator 状态
     * @return Generated SKILL.md content / 生成的 SKILL.md 内容
     */
    private fun generateSkillMdContent(state: SkillCreatorState): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        return buildString {
            appendLine("# ${state.skillName}")
            appendLine()
            if (state.skillDescription.isNotBlank()) {
                appendLine(state.skillDescription)
                appendLine()
            }
            appendLine("## 触发条件 / Trigger Conditions")
            appendLine()
            state.triggerConditions.forEach { condition ->
                appendLine("- $condition")
            }
            appendLine()
            appendLine("## 工具 / Tools")
            appendLine()
            if (state.tools.isEmpty()) {
                appendLine("_No tools defined / 未定义工具_")
            } else {
                state.tools.forEach { tool ->
                    appendLine("- **${tool.name}**")
                    appendLine("  - ${tool.description}")
                    appendLine("  - Permission: ${tool.permissionLevel.label}")
                }
            }
            appendLine()
            appendLine("## 工作流 / Workflow")
            appendLine()
            if (state.workflow.isEmpty()) {
                appendLine("_No workflow defined / 未定义工作流_")
            } else {
                state.workflow.forEach { step ->
                    val optional = if (step.isOptional) " _(Optional)_" else ""
                    appendLine("${step.stepNumber}. **${step.title}**$optional")
                    appendLine("   - ${step.description}")
                    appendLine("   - Estimated time: ~${step.estimatedMinutes} min")
                }
            }
            appendLine()
            appendLine("## 约束 / Constraints")
            appendLine()
            appendLine("- Follow Android development best practices / 遵循 Android 开发最佳实践")
            appendLine("- Use least-privilege principle for tool permissions / 工具权限遵循最小权限原则")
            appendLine("- Ensure backward compatibility where applicable / 在适用的情况下确保向后兼容")
            appendLine()
            appendLine("## 输出格式 / Output Format")
            appendLine()
            appendLine("```markdown")
            appendLine("## Result")
            appendLine("- Status: [success/failure]")
            appendLine("- Summary: [brief description]")
            appendLine("- Details: [relevant outputs or findings]")
            appendLine("```")
            appendLine()
            appendLine("---")
            appendLine("*Generated by Android Skills Toolkit on $dateStr*")
        }
    }

    // ---------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
    }
}
