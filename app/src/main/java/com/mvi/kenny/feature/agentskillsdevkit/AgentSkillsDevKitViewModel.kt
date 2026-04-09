package com.mvi.kenny.feature.agentskillsdevkit

/**
 * ============================================================
 * AgentSkillsDevKitViewModel — MVI ViewModel
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 * Mobile Prototype — ViewModel 实现
 *
 * 状态管理（State Management）:
 * — _state: 私有 MutableStateFlow，ViewModel 内部写入（唯一真相来源）
 * — state: 公开 StateFlow，UI 层只读订阅
 *
 * 副作用管理（Effect Management）:
 * — _effect: Channel（热流，缓冲区 BUFFERED），一次性事件
 * — effect: receiveAsFlow，UI 层通过 collect{} 监听并处理
 *
 * Intent 路由（Intent Routing）:
 * — handleIntent() 接收顶层 Intent
 * — 子 Intent 分发到对应的处理函数
 *
 * @see AgentSkillsDevKitContract
 * @see AgentSkillsDevKitScreen
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

/**
 * Agent Skills DevKit ViewModel
 * 实现 MVI 架构，处理 SkillEditor、PermissionDashboard、Audit 三个子 Feature
 */
class AgentSkillsDevKitViewModel : ViewModel() {

    // ================================================================
    // State & Effect
    // ================================================================

    /** 页面状态 */
    private val _state = MutableStateFlow(AgentSkillsDevKitState.Initial)
    val state: StateFlow<AgentSkillsDevKitState> = _state.asStateFlow()

    /** 当前状态快照（用于 lambda 访问） */
    val currentState: AgentSkillsDevKitState get() = _state.value

    /** 副作用 Channel */
    private val _effect = Channel<AgentSkillsDevKitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ================================================================
    // Initialization
    // ================================================================

    init {
        // 初始化权限树
        _state.value = _state.value.copy(
            permissionDashboard = PermissionDashboardState(
                rootNodes = PermissionDashboardState.createMockTree()
            )
        )
        // 初始化审计数据
        _state.value = _state.value.copy(
            audit = AuditState(entries = AuditState.createMockEntries())
        )
    }

    // ================================================================
    // Intent Routing — 意图路由入口
    // ================================================================

    /**
     * 处理顶层 Intent
     * 根据 Intent 类型路由到对应的子 Feature 处理函数
     */
    fun handleIntent(intent: AgentSkillsDevKitIntent) {
        when (intent) {
            is AgentSkillsDevKitIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is AgentSkillsDevKitIntent.EditorIntent -> handleEditorIntent(intent.intent)
            is AgentSkillsDevKitIntent.PermissionIntent -> handlePermissionIntent(intent.intent)
            is AgentSkillsDevKitIntent.AuditIntent -> handleAuditIntent(intent.intent)
        }
    }

    // ================================================================
    // Tab Switching
    // ================================================================

    private fun handleSwitchTab(tab: Int) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    // ================================================================
    // SkillEditor — DSL 编辑器逻辑
    // ================================================================

    private fun handleEditorIntent(intent: SkillEditorIntent) {
        when (intent) {
            is SkillEditorIntent.UpdateDsl -> handleUpdateDsl(intent.content)
            is SkillEditorIntent.UpdateName -> handleUpdateName(intent.name)
            is SkillEditorIntent.UpdateDescription -> handleUpdateDescription(intent.desc)
            is SkillEditorIntent.UpdateVersion -> handleUpdateVersion(intent.version)
            is SkillEditorIntent.UpdateVersionNotes -> handleUpdateVersionNotes(intent.notes)
            is SkillEditorIntent.ValidateDsl -> handleValidateDsl(intent.content)
            is SkillEditorIntent.SaveSkill -> handleSaveSkill()
            is SkillEditorIntent.RunLocalTest -> handleRunLocalTest()
            is SkillEditorIntent.DiscardChanges -> handleDiscardChanges()
        }
    }

    /** 更新 DSL 内容并实时校验 */
    private fun handleUpdateDsl(content: String) {
        val errors = validateDsl(content)
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                dslContent = content,
                validationErrors = errors,
                isDirty = content != SkillEditorState.DEFAULT_DSL_TEMPLATE,
                testResult = null
            )
        )
    }

    /** 更新 Skill 名称 */
    private fun handleUpdateName(name: String) {
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                name = name,
                isDirty = true
            )
        )
    }

    /** 更新 Skill 描述 */
    private fun handleUpdateDescription(desc: String) {
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                description = desc,
                isDirty = true
            )
        )
    }

    /** 更新版本号 */
    private fun handleUpdateVersion(version: String) {
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                version = version,
                isDirty = true
            )
        )
    }

    /** 更新版本变更说明 */
    private fun handleUpdateVersionNotes(notes: String) {
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                versionNotes = notes,
                isDirty = true
            )
        )
    }

    /**
     * DSL 校验逻辑
     * 检查：括号平衡、必填字段
     */
    private fun handleValidateDsl(content: String) {
        val errors = validateDsl(content)
        _state.value = _state.value.copy(
            skillEditor = _state.value.skillEditor.copy(
                validationErrors = errors
            )
        )
        viewModelScope.launch {
            if (errors.isEmpty()) {
                _effect.send(AgentSkillsDevKitEffect.ShowToast("DSL 校验通过"))
            }
        }
    }

    /**
     * DSL 校验实现
     * 检查括号平衡、必填字段（name、description、version）
     */
    private fun validateDsl(content: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val lines = content.lines()

        // 检查括号平衡
        var braceCount = 0
        var parenCount = 0
        lines.forEachIndexed { index, line ->
            braceCount += line.count { it == '{' } - line.count { it == '}' }
            parenCount += line.count { it == '(' } - line.count { it == ')' }
            if (braceCount < 0) {
                errors.add(ValidationError(index + 1, "多余的右花括号 }"))
                braceCount = 0
            }
            if (parenCount < 0) {
                errors.add(ValidationError(index + 1, "多余的右圆括号 )"))
                parenCount = 0
            }
        }
        if (braceCount > 0) {
            errors.add(ValidationError(lines.size, "缺少 ${braceCount} 个右花括号 }"))
        }
        if (parenCount > 0) {
            errors.add(ValidationError(lines.size, "缺少 ${parenCount} 个右圆括号 )"))
        }

        // 检查必填字段
        if (!content.contains("name =")) {
            errors.add(ValidationError(1, "缺少必填字段: name"))
        }
        if (!content.contains("description =")) {
            errors.add(ValidationError(1, "缺少必填字段: description"))
        }
        if (!content.contains("version =")) {
            errors.add(ValidationError(1, "缺少必填字段: version"))
        }

        return errors
    }

    /**
     * 保存 Skill
     * Mock 实现：1 秒延迟后返回成功
     */
    private fun handleSaveSkill() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                skillEditor = _state.value.skillEditor.copy(isSaving = true)
            )

            delay(1000) // Mock 1s 网络延迟

            val skillId = "skill_${System.currentTimeMillis()}"
            _state.value = _state.value.copy(
                skillEditor = _state.value.skillEditor.copy(
                    isSaving = false,
                    isDirty = false,
                    skillId = skillId,
                    saveResult = SaveResult(success = true, message = "Skill 保存成功")
                )
            )
            _effect.send(AgentSkillsDevKitEffect.ShowToast("Skill 保存成功 ($skillId)"))
        }
    }

    /**
     * 运行本地测试
     * Mock 实现：返回模拟测试日志
     */
    private fun handleRunLocalTest() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                skillEditor = _state.value.skillEditor.copy(isSaving = true)
            )

            delay(1500) // Mock 1.5s 测试执行

            val logs = listOf(
                "[INFO] 加载 Skill DSL...",
                "[INFO] 解析约束规则...",
                "[INFO] 初始化 Mock Agent 环境...",
                "[INFO] 执行测试用例 #1: 读取 kotlin 文件",
                "[INFO]   ✓ src/main/kotlin/com/example/App.kt - 通过",
                "[INFO] 执行测试用例 #2: 验证代码风格",
                "[INFO]   ✓ maxLineLength 检查 - 通过 (行 45: 118 chars)",
                "[INFO] 执行测试用例 #3: 架构模式检查",
                "[INFO]   ✓ Contract Pattern 检查 - 通过",
                "[WARN] 执行测试用例 #4: 尝试读取 secrets.xml",
                "[INFO]   ✓ 权限拒绝正确触发 - 通过",
                "[INFO] 执行测试用例 #5: 尝试写入 build.gradle.kts",
                "[INFO]   ✓ 权限拒绝正确触发 - 通过",
                "[SUCCESS] 5/5 测试用例通过，0 失败",
            )
            val passed = true

            _state.value = _state.value.copy(
                skillEditor = _state.value.skillEditor.copy(
                    isSaving = false,
                    testResult = TestResult(passed = passed, logs = logs)
                )
            )
            _effect.send(AgentSkillsDevKitEffect.ShowToast(if (passed) "测试通过 (5/5)" else "测试失败"))
        }
    }

    /** 放弃更改，恢复默认模板 */
    private fun handleDiscardChanges() {
        _state.value = _state.value.copy(
            skillEditor = SkillEditorState()
        )
        viewModelScope.launch {
            _effect.send(AgentSkillsDevKitEffect.ShowToast("已放弃更改"))
        }
    }

    // ================================================================
    // PermissionDashboard — 权限策略逻辑
    // ================================================================

    private fun handlePermissionIntent(intent: PermissionDashboardIntent) {
        when (intent) {
            is PermissionDashboardIntent.SetNodePermission -> handleSetNodePermission(intent.nodePath, intent.mode)
            is PermissionDashboardIntent.ToggleNodeExpand -> handleToggleNodeExpand(intent.nodePath)
            is PermissionDashboardIntent.SelectNode -> handleSelectNode(intent.node)
            is PermissionDashboardIntent.ApplyTemplate -> handleApplyTemplate(intent.templateName)
            is PermissionDashboardIntent.SavePolicy -> handleSavePolicy()
        }
    }

    /** 设置节点权限 */
    private fun handleSetNodePermission(nodePath: String, mode: PermissionMode) {
        val updatedNodes = updateNodePermission(_state.value.permissionDashboard.rootNodes, nodePath, mode)
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                rootNodes = updatedNodes,
                isDirty = true
            )
        )
    }

    /** 递归更新节点权限 */
    private fun updateNodePermission(
        nodes: List<PermissionNode>,
        targetPath: String,
        mode: PermissionMode
    ): List<PermissionNode> {
        return nodes.map { node ->
            if (node.path == targetPath) {
                node.copy(mode = mode)
            } else {
                node.copy(children = updateNodePermission(node.children, targetPath, mode))
            }
        }
    }

    /** 展开/收起节点 */
    private fun handleToggleNodeExpand(nodePath: String) {
        val updatedNodes = toggleNodeExpand(_state.value.permissionDashboard.rootNodes, nodePath)
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                rootNodes = updatedNodes
            )
        )
    }

    /** 递归切换节点展开状态 */
    private fun toggleNodeExpand(nodes: List<PermissionNode>, targetPath: String): List<PermissionNode> {
        return nodes.map { node ->
            if (node.path == targetPath) {
                node.copy(isExpanded = !node.isExpanded)
            } else {
                node.copy(children = toggleNodeExpand(node.children, targetPath))
            }
        }
    }

    /** 选中节点 */
    private fun handleSelectNode(node: PermissionNode) {
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                selectedNode = node
            )
        )
    }

    /**
     * 应用权限模板
     * 预设三种模板：Read-Only / Standard Dev / Admin
     */
    private fun handleApplyTemplate(templateName: String) {
        val updatedNodes = when (templateName) {
            "Read-Only" -> applyReadOnlyTemplate(_state.value.permissionDashboard.rootNodes)
            "Standard Dev" -> applyStandardDevTemplate(_state.value.permissionDashboard.rootNodes)
            "Admin" -> applyAdminTemplate(_state.value.permissionDashboard.rootNodes)
            else -> return
        }
        _state.value = _state.value.copy(
            permissionDashboard = _state.value.permissionDashboard.copy(
                rootNodes = updatedNodes,
                activeTemplate = templateName,
                isDirty = true
            )
        )
        viewModelScope.launch {
            _effect.send(AgentSkillsDevKitEffect.ShowToast("已应用模板: $templateName"))
        }
    }

    private fun applyReadOnlyTemplate(nodes: List<PermissionNode>): List<PermissionNode> =
        nodes.map { node ->
            val newMode = when {
                node.isDirectory -> PermissionMode.READ_ONLY
                node.path.endsWith(".kt") -> PermissionMode.READ_ONLY
                else -> PermissionMode.DENY
            }
            node.copy(mode = newMode, children = applyReadOnlyTemplate(node.children))
        }

    private fun applyStandardDevTemplate(nodes: List<PermissionNode>): List<PermissionNode> =
        nodes.map { node ->
            val newMode = when {
                node.path.contains("build.gradle") -> PermissionMode.DENY
                node.path.contains("secrets.xml") -> PermissionMode.DENY
                node.path.contains("src/main/kotlin") -> PermissionMode.ALLOW
                node.path.contains("src/main/res") -> PermissionMode.READ_ONLY
                else -> PermissionMode.UNSET
            }
            node.copy(mode = newMode, children = applyStandardDevTemplate(node.children))
        }

    private fun applyAdminTemplate(nodes: List<PermissionNode>): List<PermissionNode> =
        nodes.map { node ->
            node.copy(mode = PermissionMode.ALLOW, children = applyAdminTemplate(node.children))
        }

    /**
     * 保存权限策略
     * Mock 实现：1 秒延迟后返回成功
     */
    private fun handleSavePolicy() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                permissionDashboard = _state.value.permissionDashboard.copy(isSaving = true)
            )

            delay(1000) // Mock 1s 网络延迟

            _state.value = _state.value.copy(
                permissionDashboard = _state.value.permissionDashboard.copy(
                    isSaving = false,
                    isDirty = false
                )
            )
            _effect.send(AgentSkillsDevKitEffect.ShowToast("权限策略已保存"))
        }
    }

    // ================================================================
    // Audit — 审计视图逻辑
    // ================================================================

    private fun handleAuditIntent(intent: AuditIntent) {
        when (intent) {
            is AuditIntent.LoadAudit -> handleLoadAudit()
            is AuditIntent.SelectEntry -> handleSelectEntry(intent.entry)
            is AuditIntent.DismissDetail -> handleDismissDetail()
        }
    }

    /** 加载审计数据（Mock） */
    private fun handleLoadAudit() {
        _state.value = _state.value.copy(
            audit = AuditState(entries = AuditState.createMockEntries())
        )
    }

    /** 选中审计条目 */
    private fun handleSelectEntry(entry: AuditEntry) {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(selectedEntry = entry)
        )
    }

    /** 关闭详情 */
    private fun handleDismissDetail() {
        _state.value = _state.value.copy(
            audit = _state.value.audit.copy(selectedEntry = null)
        )
    }
}
