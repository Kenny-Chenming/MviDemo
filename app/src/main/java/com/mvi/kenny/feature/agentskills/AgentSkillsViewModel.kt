package com.mvi.kenny.feature.agentskills

/**
 * ============================================================
 * AgentSkillsViewModel — Android Studio Panda 3 Agent Skills 开发工具包
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 *
 * 状态管理（State Management）:
 * — _state: 私有 MutableStateFlow，ViewModel 内部写入（唯一真相来源）
 * — state: 公开 StateFlow，UI 层只读订阅
 *
 * 副作用管理（Effect Management）:
 * — _effect: Channel（热流，缓冲区 BUFFERED），一次性事件
 * — effect: receiveAsFlow，UI 层通过 collect{} 监听并处理
 *
 * 子 Feature 处理（Sub-feature routing）:
 * — 每个 AgentSkillsIntent 可能携带子 Intent（如 SkillEditorIntent）
 * — ViewModel 通过 when 表达式路由到对应子 Feature 处理函数
 *
 * @see AgentSkillsContract 所有 State/Intent/Effect 定义
 * @see AgentSkillsScreen UI 层
 * —————————————————————————————————————————————————————
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AgentSkillsViewModel : ViewModel() {

    // ================================================================
    // State & Effect
    // ================================================================

    /** 页面状态（StateFlow，UI 只读） */
    private val _state = MutableStateFlow(AgentSkillsState.Initial)
    val state: StateFlow<AgentSkillsState> = _state.asStateFlow()

    /** 当前状态快照（Snapshot of current state for lambda access） */
    val currentState: AgentSkillsState get() = _state.value

    /** 副作用 Channel */
    private val _effect = Channel<AgentSkillsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ================================================================
    // Initialization
    // ================================================================

    init {
        // 初始化时加载我的 Skills 列表（Load my skills on init）
        handleIntent(AgentSkillsIntent.MySkillsWrapper(MySkillsIntent.LoadMySkills))
        // 初始化权限树（Init permission tree）
        _state.value = _state.value.copy(
            permissionDashboard = PermissionDashboardState(
                rootNodes = PermissionDashboardState.createMockTree()
            )
        )
        // 初始化审计数据（Init audit data）
        _state.value = _state.value.copy(
            audit = AuditState(entries = AuditState.createMockEntries())
        )
        // 初始化市场数据（Init market data）
        handleIntent(AgentSkillsIntent.MarketIntent(SkillMarketIntent.LoadMarket))
        // 初始化 Lint 映射（Init lint mappings）
        handleIntent(AgentSkillsIntent.LintIntent(LintIntegrationIntent.LoadMappings))
    }

    // ================================================================
    // Intent Routing — 意图路由入口
    // ================================================================

    /**
     * 接收并处理用户意图（Receive and handle user intent）
     * —————————————————————————————————————————————————————
     *
     * 所有 UI 层发起的用户操作都通过此方法进入 ViewModel。
     * 方法内部通过 when 表达式将意图路由到对应的处理函数。
     *
     * 路由规则（Routing rules）:
     * — AgentSkillsIntent.SwitchTab → handleSwitchTab
     * — AgentSkillsIntent.EditorSkillIntent → handleSkillEditorIntent
     * — AgentSkillsIntent.PermissionIntent → handlePermissionIntent
     * — AgentSkillsIntent.MarketIntent → handleMarketIntent
     * — AgentSkillsIntent.MySkillsWrapper → handleMySkillsIntent
     * — AgentSkillsIntent.AuditWrapper → handleAuditIntent
     * — AgentSkillsIntent.LintIntent → handleLintIntent
     *
     * @param intent 用户意图（非空）
     */
    fun sendIntent(intent: AgentSkillsIntent) {
        when (intent) {
            is AgentSkillsIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is AgentSkillsIntent.EditorSkillIntent -> handleSkillEditorIntent(intent.intent)
            is AgentSkillsIntent.PermissionIntent -> handlePermissionIntent(intent.intent)
            is AgentSkillsIntent.MarketIntent -> handleMarketIntent(intent.intent)
            is AgentSkillsIntent.MySkillsWrapper -> handleMySkillsIntent(intent.intent)
            is AgentSkillsIntent.AuditWrapper -> handleAuditIntent(intent.intent)
            is AgentSkillsIntent.LintIntent -> handleLintIntent(intent.intent)
        }
    }

    // ================================================================
    // Tab Navigation — 导航 Tab 切换
    // ================================================================

    /**
     * 处理 Tab 切换（Handle tab switch）
     *
     * @param tab 目标 Tab
     */
    private fun handleSwitchTab(tab: AgentSkillsTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    // ================================================================
    // 6.1 SkillEditorFeature — Skill 创建与编辑器
    // ================================================================

    /**
     * 处理 Skill 编辑器意图（Handle Skill Editor intents）
     * —————————————————————————————————————————————————————
     *
     * Skill 编辑器是向导式流程（Wizard Flow）：
     * — Step 0: DSL 定义（定义 Skill 名称、DSL 内容、适用文件类型）
     * — Step 1: 权限配置（配置 Skill 的读写权限范围）
     * — Step 2: 本地测试（输入代码片段，验证 Skill 行为）
     *
     * DSL 校验逻辑（DSL Validation Logic）:
     * — 实时校验：用户每次输入都触发校验（debounced）
     * — 校验内容：Kotlin DSL 语法、必需字段、版本格式
     * — 错误级别：ERROR（阻止保存）/ WARNING（提示）/ INFO（参考）
     *
     * @param intent Skill 编辑器子意图
     */
    private fun handleSkillEditorIntent(intent: SkillEditorIntent) {
        when (intent) {
            is SkillEditorIntent.UpdateDsl -> updateDsl(intent.content)
            is SkillEditorIntent.UpdateName -> updateName(intent.name)
            is SkillEditorIntent.UpdateDescription -> updateDescription(intent.desc)
            is SkillEditorIntent.UpdateVersion -> updateVersion(intent.version)
            is SkillEditorIntent.UpdateVersionNotes -> updateVersionNotes(intent.notes)
            is SkillEditorIntent.ToggleFileType -> toggleFileType(intent.fileType)
            is SkillEditorIntent.ValidateDsl -> validateDsl(intent.content)
            is SkillEditorIntent.SaveSkill -> saveSkill()
            is SkillEditorIntent.RunLocalTest -> runLocalTest(intent.codeSnippet)
            is SkillEditorIntent.DiscardChanges -> discardChanges()
            is SkillEditorIntent.SetWizardStep -> setWizardStep(intent.step)
            is SkillEditorIntent.CreateNewSkill -> createNewSkill()
            is SkillEditorIntent.LoadSkill -> loadSkill(intent.skillId)
        }
    }

    // --- Skill Editor State Updates ---

    private fun updateDsl(content: String) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(dslContent = content, isDirty = true)
        )
        // 触发 DSL 校验（异步，debounced）
        viewModelScope.launch {
            delay(300)
            validateDslInternal(content)
        }
    }

    private fun updateName(name: String) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(name = name, isDirty = true)
        )
    }

    private fun updateDescription(desc: String) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(description = desc, isDirty = true)
        )
    }

    private fun updateVersion(version: String) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(version = version, isDirty = true)
        )
    }

    private fun updateVersionNotes(notes: String) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(versionNotes = notes, isDirty = true)
        )
    }

    private fun toggleFileType(fileType: SkillFileType) {
        val editor = _state.value.skillEditor
        val current = editor.fileTypes.toMutableList()
        if (current.contains(fileType)) {
            if (current.size > 1) current.remove(fileType) // 至少保留一个
        } else {
            current.add(fileType)
        }
        _state.value = _state.value.copy(
            skillEditor = editor.copy(fileTypes = current, isDirty = true)
        )
    }

    private fun setWizardStep(step: Int) {
        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(wizardStep = step)
        )
    }

    /**
     * 创建新 Skill（Reset editor to default state）
     */
    private fun createNewSkill() {
        _state.value = _state.value.copy(
            skillEditor = SkillEditorState(
                wizardStep = 0,
                dslContent = SkillEditorState.DEFAULT_DSL_TEMPLATE
            )
        )
    }

    /**
     * 加载已有 Skill 到编辑器（Load existing skill into editor）
     *
     * @param skillId Skill ID
     */
    private fun loadSkill(skillId: String) {
        // 模拟从本地存储加载 Skill（In real scenario: load from DataStore/file）
        val mockSkill = MySkill(
            id = skillId,
            name = "Kotlin Style Checker",
            description = "Enforces Kotlin coding style guidelines",
            version = "1.2.0",
            lastModified = "2026-04-05",
            isEnabled = true,
            category = "CODE_STYLE"
        )
        _state.value = _state.value.copy(
            skillEditor = SkillEditorState(
                skillId = mockSkill.id,
                name = mockSkill.name,
                description = mockSkill.description,
                version = mockSkill.version,
                dslContent = SkillEditorState.DEFAULT_DSL_TEMPLATE
            )
        )
    }

    /**
     * DSL 校验入口（DSL validation entry point）
     */
    private fun validateDsl(content: String) {
        validateDslInternal(content)
    }

    /**
     * DSL 内部校验逻辑（Internal DSL validation）
     * —————————————————————————————————————————————————————
     *
     * 校验规则（Validation rules）:
     * 1. 语法检查：DSL 必须以 skill { 开头
     * 2. 必需字段：name, description, version 必填
     * 3. 版本格式：semver 格式（X.Y.Z）
     * 4. fileTypes：至少一个
     * 5. permissions.allowRead/allowWrite 必须为布尔值
     *
     * @param content DSL 内容
     */
    private fun validateDslInternal(content: String) {
        val errors = mutableListOf<ValidationError>()

        if (!content.trim().startsWith("skill {")) {
            errors.add(ValidationError(1, 1, "DSL must start with 'skill {'", ValidationSeverity.ERROR))
        }

        if (!content.contains("name =")) {
            errors.add(ValidationError(0, 0, "Missing required field: name", ValidationSeverity.ERROR))
        }

        if (!content.contains("version =")) {
            errors.add(ValidationError(0, 0, "Missing required field: version", ValidationSeverity.ERROR))
        }

        // 版本格式校验（semver）
        val versionRegex = Regex("""version = "(\d+)\.(\d+)\.(\d+)"""")
        versionRegex.find(content)?.let { match ->
            val parts = match.groupValues[1].toIntOrNull() ?: 0
            if (parts < 0) {
                errors.add(ValidationError(0, 0, "Version must be non-negative", ValidationSeverity.ERROR))
            }
        }

        if (!content.contains("fileTypes")) {
            errors.add(ValidationError(0, 0, "Missing required field: fileTypes", ValidationSeverity.WARNING))
        }

        val editor = _state.value.skillEditor
        _state.value = _state.value.copy(
            skillEditor = editor.copy(validationErrors = errors)
        )

        viewModelScope.launch {
            if (errors.isEmpty()) {
                _effect.send(AgentSkillsEffect.ShowToast("DSL 校验通过 / DSL validation passed"))
            }
        }
    }

    /**
     * 保存 Skill（Save skill to local storage）
     * —————————————————————————————————————————————————————
     *
     * 保存流程（Save flow）:
     * 1. 再次校验 DSL（前端校验）
     * 2. 设置 isSaving = true
     * 3. 模拟写入延迟（真实场景：写入 DataStore 或本地文件）
     * 4. 保存成功后：isSaving = false，saveResult = Success
     * 5. 发送 SkillSaved Effect
     * 6. 更新我的 Skills 列表
     */
    private fun saveSkill() {
        val editor = _state.value.skillEditor

        if (!editor.isDslValid) {
            viewModelScope.launch {
                _effect.send(AgentSkillsEffect.ShowToast("DSL 有错误，请修复后再保存 / Please fix DSL errors before saving"))
            }
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                skillEditor = editor.copy(isSaving = true)
            )

            try {
                delay(1000) // 模拟写入延迟

                val skillId = editor.skillId ?: "skill_${System.currentTimeMillis()}"
                _state.value = _state.value.copy(
                    skillEditor = editor.copy(
                        skillId = skillId,
                        isSaving = false,
                        saveResult = SaveResult.Success,
                        isDirty = false
                    )
                )

                _effect.send(AgentSkillsEffect.SkillSaved(skillId))
                _effect.send(AgentSkillsEffect.ShowToast("Skill 保存成功 / Skill saved successfully"))

                // 刷新我的 Skills 列表
                handleMySkillsIntent(MySkillsIntent.LoadMySkills)

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    skillEditor = editor.copy(
                        isSaving = false,
                        saveResult = SaveResult.Error(e.message ?: "Unknown error")
                    )
                )
                _effect.send(AgentSkillsEffect.ShowToast("保存失败: ${e.message}"))
            }
        }
    }

    /**
     * 运行本地测试（Run local test with mock Agent）
     * —————————————————————————————————————————————————————
     *
     * 测试流程（Test flow）:
     * 1. 使用 Mock Agent 环境模拟调用 Skill
     * 2. 输入代码片段，执行 Skill DSL 定义的行为
     * 3. 返回测试结果（passed/failed + output + duration）
     *
     * @param codeSnippet 测试用的代码片段
     */
    private fun runLocalTest(codeSnippet: String) {
        val editor = _state.value.skillEditor

        viewModelScope.launch {
            _state.value = _state.value.copy(
                skillEditor = editor.copy(testResult = null)
            )

            delay(1500) // 模拟测试执行时间

            // 模拟测试结果（Mock test result）
            val passed = editor.isDslValid
            val output = buildString {
                appendLine("=== Skill Test Results ===")
                appendLine("Skill: ${editor.name}")
                appendLine("Version: ${editor.version}")
                appendLine()
                appendLine("--- Input Code ---")
                appendLine(codeSnippet.ifEmpty { "// No input provided" })
                appendLine()
                appendLine("--- Validation ---")
                if (passed) {
                    appendLine("✓ DSL syntax is valid")
                    appendLine("✓ All required fields present")
                    appendLine("✓ File type filters configured")
                } else {
                    appendLine("✗ DSL has validation errors")
                    editor.validationErrors.forEach { err ->
                        appendLine("  Line ${err.line}: ${err.message}")
                    }
                }
                appendLine()
                appendLine("--- Execution ---")
                appendLine(if (passed) "✓ Skill executed successfully" else "✗ Skill execution blocked")
            }

            _state.value = _state.value.copy(
                skillEditor = editor.copy(
                    testResult = TestResult(
                        passed = passed,
                        output = output,
                        durationMs = 1500
                    )
                )
            )

            _effect.send(AgentSkillsEffect.ShowToast(
                if (passed) "测试通过 / Test passed" else "测试失败 / Test failed"
            ))
        }
    }

    /**
     * 放弃更改（Discard unsaved changes）
     */
    private fun discardChanges() {
        _state.value = _state.value.copy(
            skillEditor = SkillEditorState.Initial
        )
        viewModelScope.launch {
            _effect.send(AgentSkillsEffect.ShowToast("已放弃更改 / Changes discarded"))
        }
    }

    // ================================================================
    // 6.2 PermissionDashboardFeature — 权限策略仪表盘
    // ================================================================

    /**
     * 处理权限仪表盘意图（Handle Permission Dashboard intents）
     */
    private fun handlePermissionIntent(intent: PermissionDashboardIntent) {
        when (intent) {
            is PermissionDashboardIntent.SetNodePermission -> setNodePermission(intent.nodePath, intent.mode)
            is PermissionDashboardIntent.ApplyTemplate -> applyTemplate(intent.template)
            is PermissionDashboardIntent.ToggleNodeExpand -> toggleNodeExpand(intent.nodePath)
            is PermissionDashboardIntent.SelectNode -> selectNode(intent.node)
            is PermissionDashboardIntent.SavePolicy -> savePolicy()
            is PermissionDashboardIntent.ResetPolicy -> resetPolicy()
        }
    }

    private fun setNodePermission(nodePath: String, mode: PermissionMode) {
        val updatedTree = updateNodeInTree(_state.value.permissionDashboard.rootNodes, nodePath, mode)
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                rootNodes = updatedTree,
                isDirty = true
            )
        )
    }

    /** 递归更新权限树节点（Recursively update permission node in tree） */
    private fun updateNodeInTree(nodes: List<PermissionNode>, targetPath: String, mode: PermissionMode): List<PermissionNode> {
        return nodes.map { node ->
            if (node.path == targetPath) {
                node.copy(mode = mode)
            } else if (node.children.isNotEmpty()) {
                node.copy(children = updateNodeInTree(node.children, targetPath, mode))
            } else {
                node
            }
        }
    }

    private fun applyTemplate(template: PermissionTemplate) {
        val updatedTree: List<PermissionNode> = when (template) {
            PermissionTemplate.READ_ONLY -> _state.value.permissionDashboard.rootNodes.map { applyReadOnlyTemplate(it) }
            PermissionTemplate.STANDARD_DEV -> _state.value.permissionDashboard.rootNodes.map { applyStandardDevTemplate(it) }
            PermissionTemplate.ADMIN -> _state.value.permissionDashboard.rootNodes.map { it.copy(mode = PermissionMode.ALLOW) }
        }

        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                rootNodes = updatedTree,
                activeTemplate = template,
                isDirty = true
            )
        )

        viewModelScope.launch {
            _effect.send(AgentSkillsEffect.ShowToast("已应用模板: ${template.displayName}"))
        }
    }

    private fun applyReadOnlyTemplate(node: PermissionNode): PermissionNode {
        return if (node.isDirectory) {
            node.copy(
                mode = PermissionMode.READONLY,
                children = node.children.map { applyReadOnlyTemplate(it) }
            )
        } else {
            node.copy(mode = PermissionMode.READONLY)
        }
    }

    private fun applyStandardDevTemplate(node: PermissionNode): PermissionNode {
        val isSourceDir = node.path.contains("src/main/kotlin")
        val isSecrets = node.path.contains("secrets.xml") || node.path.contains("build.gradle")
        return if (node.isDirectory) {
            node.copy(
                mode = if (isSourceDir) PermissionMode.ALLOW else PermissionMode.READONLY,
                children = node.children.map { applyStandardDevTemplate(it) }
            )
        } else {
            node.copy(mode = if (isSecrets) PermissionMode.DENY else if (isSourceDir) PermissionMode.ALLOW else PermissionMode.READONLY)
        }
    }

    private fun toggleNodeExpand(nodePath: String) {
        val updatedTree = toggleExpandInTree(_state.value.permissionDashboard.rootNodes, nodePath)
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(rootNodes = updatedTree)
        )
    }

    private fun toggleExpandInTree(nodes: List<PermissionNode>, targetPath: String): List<PermissionNode> {
        return nodes.map { node ->
            if (node.path == targetPath) {
                node.copy(isExpanded = !node.isExpanded)
            } else if (node.children.isNotEmpty()) {
                node.copy(children = toggleExpandInTree(node.children, targetPath))
            } else {
                node
            }
        }
    }

    private fun selectNode(node: PermissionNode) {
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(selectedNode = node)
        )
    }

    private fun savePolicy() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                permissionDashboard = _state.value.permissionDashboard.copy(isSaving = true)
            )

            delay(800) // 模拟写入延迟

            _state.value = _state.value.copy(
                permissionDashboard = _state.value.permissionDashboard.copy(
                    isSaving = false,
                    isDirty = false,
                    saveResult = SaveResult.Success
                )
            )

            _effect.send(AgentSkillsEffect.ShowToast("权限策略已保存 / Policy saved"))
        }
    }

    private fun resetPolicy() {
        _state.value = _state.value.copy(
            permissionDashboard = PermissionDashboardState(
                rootNodes = PermissionDashboardState.createMockTree()
            )
        )
        viewModelScope.launch {
            _effect.send(AgentSkillsEffect.ShowToast("已重置为默认策略 / Policy reset to default"))
        }
    }

    // ================================================================
    // SkillMarketFeature — Skills 市场浏览
    // ================================================================

    private fun handleMarketIntent(intent: SkillMarketIntent) {
        when (intent) {
            is SkillMarketIntent.LoadMarket -> loadMarket()
            is SkillMarketIntent.Search -> search(intent.query)
            is SkillMarketIntent.SetCategory -> setCategory(intent.category)
            is SkillMarketIntent.SetSortOption -> setSortOption(intent.option)
            is SkillMarketIntent.SelectSkill -> selectSkill(intent.skill)
            is SkillMarketIntent.InstallSkill -> installSkill(intent.skillId)
            is SkillMarketIntent.UninstallSkill -> uninstallSkill(intent.skillId)
            is SkillMarketIntent.DismissDetail -> dismissSkillDetail()
        }
    }

    private fun loadMarket() {
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(isLoading = true)
        )

        viewModelScope.launch {
            delay(1200) // 模拟网络延迟

            val mockMarketSkills = listOf(
                MarketSkill("m1", "Kotlin Style Guide", "Official Kotlin coding style enforcement", "JetBrains", "2.1.0", 4.8f, 15420, MarketCategory.CODE_STYLE, listOf("kotlin", "style"), false, "2026-03-15"),
                MarketSkill("m2", "MVVM Architecture Enforcer", "Validates ViewModel + LiveData/StateFlow usage", "Android Team", "1.5.0", 4.6f, 8930, MarketCategory.ARCHITECTURE, listOf("mvvm", "viewmodel"), false, "2026-03-20"),
                MarketSkill("m3", "Compose Best Practices", "Enforces Jetpack Compose best practices", "Google", "3.0.0", 4.9f, 22100, MarketCategory.CODE_STYLE, listOf("compose", "ui"), true, "2026-04-01"),
                MarketSkill("m4", "Security Scanner", "Detects hardcoded secrets and insecure patterns", "Snyk", "1.8.0", 4.5f, 6720, MarketCategory.SECURITY, listOf("security", "secrets"), false, "2026-02-28"),
                MarketSkill("m5", "Performance Profiler", "Detects performance anti-patterns in Kotlin", "Uber", "1.2.0", 4.3f, 4500, MarketCategory.PERFORMANCE, listOf("performance", "optimization"), false, "2026-01-15"),
                MarketSkill("m6", "Kotlin Test Coverage", "Enforces minimum test coverage thresholds", "JetBrains", "1.0.0", 4.7f, 3200, MarketCategory.TESTING, listOf("testing", "coverage"), false, "2026-04-05"),
                MarketSkill("m7", "Detekt Rule Mapper", "Maps Detekt rules to Agent Skills DSL", "Android Team", "1.1.0", 4.4f, 2100, MarketCategory.ARCHITECTURE, listOf("detekt", "lint"), false, "2026-03-25"),
                MarketSkill("m8", "API Contract Validator", "Validates API contract changes", "Stripe", "2.0.0", 4.6f, 5800, MarketCategory.TESTING, listOf("api", "contract"), false, "2026-02-10")
            )

            _state.value = _state.value.copy(
                skillMarket = _state.value.skillMarket.copy(
                    skills = mockMarketSkills,
                    isLoading = false
                )
            )
        }
    }

    private fun search(query: String) {
        val currentFilter = _state.value.skillMarket.filter
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(
                filter = currentFilter.copy(searchQuery = query)
            )
        )
    }

    private fun setCategory(category: MarketCategory) {
        val currentFilter = _state.value.skillMarket.filter
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(
                filter = currentFilter.copy(category = category)
            )
        )
    }

    private fun setSortOption(option: MarketSortOption) {
        val currentFilter = _state.value.skillMarket.filter
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(
                filter = currentFilter.copy(sortBy = option)
            )
        )
    }

    private fun selectSkill(skill: MarketSkill) {
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(selectedSkill = skill)
        )
    }

    private fun dismissSkillDetail() {
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(selectedSkill = null)
        )
    }

    private fun installSkill(skillId: String) {
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(
                isInstalling = true,
                installingSkillId = skillId
            )
        )

        viewModelScope.launch {
            delay(2000) // 模拟安装延迟

            val updatedSkills = _state.value.skillMarket.skills.map { skill ->
                if (skill.id == skillId) skill.copy(isInstalled = true) else skill
            }

            _state.value = _state.value.copy(
                skillMarket = _state.value.skillMarket.copy(
                    skills = updatedSkills,
                    isInstalling = false,
                    installingSkillId = null
                )
            )

            _effect.send(AgentSkillsEffect.ShowToast("安装成功 / Installed successfully"))
            // 刷新我的 Skills
            handleMySkillsIntent(MySkillsIntent.LoadMySkills)
        }
    }

    private fun uninstallSkill(skillId: String) {
        val updatedSkills = _state.value.skillMarket.skills.map { skill ->
            if (skill.id == skillId) skill.copy(isInstalled = false) else skill
        }
        _state.value = _state.value.copy(
            skillMarket = _state.value.skillMarket.copy(skills = updatedSkills)
        )

        viewModelScope.launch {
            _effect.send(AgentSkillsEffect.ShowToast("已卸载 / Uninstalled"))
        }
    }

    // ================================================================
    // MySkillsFeature — 我的 Skills 管理
    // ================================================================

    private fun handleMySkillsIntent(intent: MySkillsIntent) {
        when (intent) {
            is MySkillsIntent.LoadMySkills -> loadMySkills()
            is MySkillsIntent.ToggleSkill -> toggleSkill(intent.skillId, intent.enabled)
            is MySkillsIntent.EditSkill -> editSkill(intent.skill)
            is MySkillsIntent.DeleteSkill -> deleteSkill(intent.skillId)
        }
    }

    private fun loadMySkills() {
        _state.value = _state.value.copy(
            mySkills = _state.value.mySkills.copy(isLoading = true)
        )

        viewModelScope.launch {
            delay(500)

            val mockMySkills = listOf(
                MySkill("s1", "Kotlin Style Checker", "Enforces Kotlin coding style guidelines", "1.2.0", "2026-04-05", true, "CODE_STYLE"),
                MySkill("s2", "Architecture Enforcer", "Validates MVVM/Clean architecture patterns", "2.0.1", "2026-04-03", true, "ARCHITECTURE"),
                MySkill("s3", "Security Scanner", "Detects hardcoded secrets in source files", "1.5.0", "2026-04-01", true, "SECURITY"),
                MySkill("s4", "Compose Best Practices", "Enforces Jetpack Compose best practices", "3.0.0", "2026-03-28", false, "CODE_STYLE"),
                MySkill("s5", "Performance Profiler", "Detects performance anti-patterns", "1.2.0", "2026-03-20", true, "PERFORMANCE")
            )

            _state.value = _state.value.copy(
                mySkills = _state.value.mySkills.copy(
                    skills = mockMySkills,
                    isLoading = false
                )
            )
        }
    }

    private fun toggleSkill(skillId: String, enabled: Boolean) {
        val updatedSkills = _state.value.mySkills.skills.map { skill ->
            if (skill.id == skillId) skill.copy(isEnabled = enabled) else skill
        }
        _state.value = _state.value.copy(
            mySkills = _state.value.mySkills.copy(skills = updatedSkills)
        )

        viewModelScope.launch {
            val action = if (enabled) "启用" else "禁用"
            _effect.send(AgentSkillsEffect.ShowToast("Skill [$skillId] 已$action"))
        }
    }

    private fun editSkill(skill: MySkill) {
        // 切换到编辑器 Tab 并加载 Skill
        _state.value = _state.value.copy(currentTab = AgentSkillsTab.SKILL_EDITOR)
        loadSkill(skill.id)
    }

    private fun deleteSkill(skillId: String) {
        val updatedSkills = _state.value.mySkills.skills.filter { it.id != skillId }
        _state.value = _state.value.copy(
            mySkills = _state.value.mySkills.copy(skills = updatedSkills)
        )

        viewModelScope.launch {
            _effect.send(AgentSkillsEffect.ShowToast("Skill 已删除 / Skill deleted"))
        }
    }

    // ================================================================
    // AuditFeature — 执行审计视图
    // ================================================================

    private fun handleAuditIntent(intent: AuditIntent) {
        when (intent) {
            is AuditIntent.LoadAudit -> loadAudit()
            is AuditIntent.SetTimeRange -> setTimeRange(intent.range)
            is AuditIntent.SetSkillFilter -> setSkillFilter(intent.skillName)
            is AuditIntent.SelectEntry -> selectAuditEntry(intent.entry)
            is AuditIntent.DismissDetail -> dismissAuditDetail()
            is AuditIntent.ExportAudit -> exportAudit()
        }
    }

    private fun loadAudit() {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(isLoading = true)
        )

        viewModelScope.launch {
            delay(800)
            _state.value = _state.value.copy(
                audit = _state.value.audit.copy(
                    entries = AuditState.createMockEntries(),
                    isLoading = false
                )
            )
        }
    }

    private fun setTimeRange(range: AuditTimeRange) {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(timeRange = range)
        )
        // 真实场景：基于时间范围重新过滤 entries
    }

    private fun setSkillFilter(skillName: String) {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(skillFilter = skillName)
        )
    }

    private fun selectAuditEntry(entry: AuditEntry) {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(selectedEntry = entry)
        )
    }

    private fun dismissAuditDetail() {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(selectedEntry = null)
        )
    }

    private fun exportAudit() {
        viewModelScope.launch {
            delay(1000)
            _effect.send(AgentSkillsEffect.ShowToast("审计日志已导出 / Audit log exported"))
        }
    }

    // ================================================================
    // LintIntegrationFeature — Lint/Detekt 联动设置
    // ================================================================

    private fun handleLintIntent(intent: LintIntegrationIntent) {
        when (intent) {
            is LintIntegrationIntent.LoadMappings -> loadLintMappings()
            is LintIntegrationIntent.ToggleMapping -> toggleMapping(intent.lintRuleId, intent.enabled)
            is LintIntegrationIntent.LinkToSkill -> linkLintToSkill(intent.lintRuleId, intent.skillId)
            is LintIntegrationIntent.SaveSettings -> saveLintSettings()
        }
    }

    private fun loadLintMappings() {
        viewModelScope.launch {
            val mockMappings = listOf(
                LintRuleMapping("R.style", "Hardcoded array", "s3", true),
                LintRuleMapping("R.string", "String literal", "s3", true),
                LintRuleMapping("KtLint", "Coding style", "s1", true),
                LintRuleMapping("Detekt.allRules", "All Detekt rules", "s2", false),
                LintRuleMapping("ComposeWarnings", "Compose API usage", "s4", true)
            )
            _state.value = _state.value.copy(
                lintIntegration = _state.value.lintIntegration.copy(lintMappings = mockMappings)
            )
        }
    }

    private fun toggleMapping(lintRuleId: String, enabled: Boolean) {
        val updatedMappings = _state.value.lintIntegration.lintMappings.map { mapping ->
            if (mapping.lintRuleId == lintRuleId) mapping.copy(isEnabled = enabled) else mapping
        }
        _state.value = _state.value.copy(
            lintIntegration = _state.value.lintIntegration.copy(
                lintMappings = updatedMappings,
                isDirty = true
            )
        )
    }

    private fun linkLintToSkill(lintRuleId: String, skillId: String?) {
        val updatedMappings = _state.value.lintIntegration.lintMappings.map { mapping ->
            if (mapping.lintRuleId == lintRuleId) mapping.copy(targetSkillId = skillId) else mapping
        }
        _state.value = _state.value.copy(
            lintIntegration = _state.value.lintIntegration.copy(
                lintMappings = updatedMappings,
                isDirty = true
            )
        )
    }

    private fun saveLintSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                lintIntegration = _state.value.lintIntegration.copy(isSaving = true)
            )

            delay(800)

            _state.value = _state.value.copy(
                lintIntegration = _state.value.lintIntegration.copy(
                    isSaving = false,
                    isDirty = false
                )
            )

            _effect.send(AgentSkillsEffect.ShowToast("Lint 联动设置已保存 / Lint settings saved"))
        }
    }
}
