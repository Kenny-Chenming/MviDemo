package com.mvi.kenny.feature.agentskillsdevkit

/**
 * ============================================================
 * AgentSkillsDevKitContract — MVI Contract
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 * Mobile Prototype — MVI 三要素定义
 *
 * 设计文档对应关系（Mapping to Design Doc）:
 * — Section 6.1: SkillEditorFeature State/Intent/Effect
 * — Section 6.2: PermissionDashboardFeature State/Intent/Effect
 * — Section 7.1: 视觉规范（颜色/字体/主题）
 *
 * MVI 三要素（Three pillars of MVI）:
 * — State: 页面状态的唯一真相来源，Immutable data class
 * — Intent: 用户意图，ViewModel receives and processes
 * — Effect: 一次性副作用，通过 Channel 投递
 *
 * @see AgentSkillsDevKitViewModel
 * @see AgentSkillsDevKitScreen
 * @see AgentSkillsDevKitModels
 * —————————————————————————————————————————————————————
 */

// ================================================================
// 6.1 SkillEditorFeature — Skill 编辑器
// ================================================================

/**
 * Skill 编辑器状态
 *
 * @param skillId 当前编辑的 Skill ID（null 表示新建）
 * @param name Skill 名称
 * @param description Skill 描述
 * @param dslContent DSL 内容（Kotlin DSL 格式）
 * @param version 版本号
 * @param versionNotes 版本变更说明
 * @param fileTypes 适用文件类型列表
 * @param validationErrors DSL 校验错误列表
 * @param isDirty 是否有未保存的更改
 * @param isSaving 是否正在保存
 * @param saveResult 上次保存结果
 * @param testResult 上次测试结果
 */
data class SkillEditorState(
    val skillId: String? = null,
    val name: String = "",
    val description: String = "",
    val dslContent: String = DEFAULT_DSL_TEMPLATE,
    val version: String = "1.0.0",
    val versionNotes: String = "",
    val fileTypes: List<String> = listOf("*.kt"),
    val validationErrors: List<ValidationError> = emptyList(),
    val isDirty: Boolean = false,
    val isSaving: Boolean = false,
    val saveResult: SaveResult? = null,
    val testResult: TestResult? = null
) {
    companion object {
        /** 默认 DSL 模板 */
        const val DEFAULT_DSL_TEMPLATE = """skill {
    name = "MySkill"
    description = "Describe what this skill does"
    version = "1.0.0"
    
    fileTypes = ["*.kt"]
    
    constraints {
        maxLineLength = 120
        requireContractPattern = true
    }
    
    permissions {
        allowRead = true
        allowWrite = false
        allowedPaths = ["src/main/kotlin"]
    }
}
"""
    }

    /** DSL 是否有效（无 ERROR 级别错误） */
    val isDslValid: Boolean get() = validationErrors.isEmpty()
}

/** Skill 编辑器用户意图 */
sealed interface SkillEditorIntent {
    data class UpdateDsl(val content: String) : SkillEditorIntent()
    data class UpdateName(val name: String) : SkillEditorIntent()
    data class UpdateDescription(val desc: String) : SkillEditorIntent()
    data class UpdateVersion(val version: String) : SkillEditorIntent()
    data class UpdateVersionNotes(val notes: String) : SkillEditorIntent()
    data class ValidateDsl(val content: String) : SkillEditorIntent()
    data object SaveSkill : SkillEditorIntent()
    data object RunLocalTest : SkillEditorIntent()
    data object DiscardChanges : SkillEditorIntent()
}

/** Skill 编辑器副作用 */
sealed interface SkillEditorEffect {
    data class ShowToast(val message: String) : SkillEditorEffect()
    data object DSLValid : SkillEditorEffect()
    data class DSLInvalid(val errors: List<ValidationError>) : SkillEditorEffect()
    data class SkillSaved(val skillId: String) : SkillEditorEffect()
}

// ================================================================
// 6.2 PermissionDashboardFeature — 权限策略仪表盘
// ================================================================

/**
 * 权限仪表盘状态
 *
 * @param rootNodes 权限树根节点列表
 * @param selectedNode 当前选中的节点
 * @param activeTemplate 当前激活的权限模板
 * @param isDirty 是否有未保存的更改
 * @param isSaving 是否正在保存
 */
data class PermissionDashboardState(
    val rootNodes: List<PermissionNode> = emptyList(),
    val selectedNode: PermissionNode? = null,
    val activeTemplate: String? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false
) {
    companion object {
        /** 生成模拟权限树数据 */
        fun createMockTree(): List<PermissionNode> = listOf(
            PermissionNode(
                path = "src/main/kotlin",
                name = "kotlin",
                isDirectory = true,
                mode = PermissionMode.ALLOW,
                children = listOf(
                    PermissionNode(
                        path = "src/main/kotlin/com",
                        name = "com",
                        isDirectory = true,
                        mode = PermissionMode.ALLOW,
                        children = listOf(
                            PermissionNode(
                                path = "src/main/kotlin/com/example",
                                name = "example",
                                isDirectory = true,
                                mode = PermissionMode.ALLOW,
                                children = listOf(
                                    PermissionNode(
                                        path = "src/main/kotlin/com/example/App.kt",
                                        name = "App.kt",
                                        isDirectory = false,
                                        mode = PermissionMode.ALLOW
                                    )
                                )
                            )
                        )
                    ),
                    PermissionNode(
                        path = "src/main/kotlin/util",
                        name = "util",
                        isDirectory = true,
                        mode = PermissionMode.ALLOW,
                        children = listOf(
                            PermissionNode(
                                path = "src/main/kotlin/util/Helper.kt",
                                name = "Helper.kt",
                                isDirectory = false,
                                mode = PermissionMode.ALLOW
                            )
                        )
                    )
                )
            ),
            PermissionNode(
                path = "src/main/res",
                name = "res",
                isDirectory = true,
                mode = PermissionMode.READ_ONLY,
                children = listOf(
                    PermissionNode(
                        path = "src/main/res/values",
                        name = "values",
                        isDirectory = true,
                        mode = PermissionMode.READ_ONLY,
                        children = listOf(
                            PermissionNode(
                                path = "src/main/res/values/strings.xml",
                                name = "strings.xml",
                                isDirectory = false,
                                mode = PermissionMode.READ_ONLY
                            ),
                            PermissionNode(
                                path = "src/main/res/values/colors.xml",
                                name = "colors.xml",
                                isDirectory = false,
                                mode = PermissionMode.READ_ONLY
                            )
                        )
                    ),
                    PermissionNode(
                        path = "src/main/res/values/secrets.xml",
                        name = "secrets.xml",
                        isDirectory = false,
                        mode = PermissionMode.DENY
                    )
                )
            ),
            PermissionNode(
                path = "build.gradle.kts",
                name = "build.gradle.kts",
                isDirectory = false,
                mode = PermissionMode.DENY
            )
        )
    }
}

/** 权限仪表盘用户意图 */
sealed interface PermissionDashboardIntent {
    data class SetNodePermission(val nodePath: String, val mode: PermissionMode) : PermissionDashboardIntent()
    data class ToggleNodeExpand(val nodePath: String) : PermissionDashboardIntent()
    data class SelectNode(val node: PermissionNode) : PermissionDashboardIntent()
    data class ApplyTemplate(val templateName: String) : PermissionDashboardIntent()
    data object SavePolicy : PermissionDashboardIntent()
}

/** 权限仪表盘副作用 */
sealed interface PermissionDashboardEffect {
    data class ShowToast(val message: String) : PermissionDashboardEffect()
    data object PolicySaved : PermissionDashboardEffect()
}

// ================================================================
// AuditFeature — 执行审计视图
// ================================================================

/**
 * 审计视图状态
 *
 * @param entries 审计日志条目列表
 * @param selectedEntry 当前选中的条目
 */
data class AuditState(
    val entries: List<AuditEntry> = emptyList(),
    val selectedEntry: AuditEntry? = null
) {
    companion object {
        /** 生成模拟审计数据 */
        fun createMockEntries(): List<AuditEntry> = listOf(
            AuditEntry(
                id = "a1",
                timestamp = "2026-04-09 16:30:22",
                skillName = "Kotlin Style Checker",
                agentName = "Gemini 3",
                action = "Read file",
                targetPath = "src/main/kotlin/com/example/App.kt",
                outcome = AuditOutcome.SUCCESS,
                durationMs = 120
            ),
            AuditEntry(
                id = "a2",
                timestamp = "2026-04-09 16:30:45",
                skillName = "Architecture Enforcer",
                agentName = "Gemini 3",
                action = "Validate pattern",
                targetPath = "src/main/kotlin/com/example/viewmodel",
                outcome = AuditOutcome.SUCCESS,
                durationMs = 85
            ),
            AuditEntry(
                id = "a3",
                timestamp = "2026-04-09 16:31:02",
                skillName = "Security Scanner",
                agentName = "Claude 3",
                action = "Read file",
                targetPath = "src/main/res/values/secrets.xml",
                outcome = AuditOutcome.DENIED,
                durationMs = 50
            ),
            AuditEntry(
                id = "a4",
                timestamp = "2026-04-09 15:45:11",
                skillName = "Kotlin Style Checker",
                agentName = "Gemini 3",
                action = "Read file",
                targetPath = "src/main/kotlin/util/Helper.kt",
                outcome = AuditOutcome.SUCCESS,
                durationMs = 95
            ),
            AuditEntry(
                id = "a5",
                timestamp = "2026-04-09 14:20:00",
                skillName = "Detekt Integration",
                agentName = "Claude 3",
                action = "Run check",
                targetPath = null,
                outcome = AuditOutcome.WARNING,
                durationMs = 3200
            ),
            AuditEntry(
                id = "a6",
                timestamp = "2026-04-09 12:15:33",
                skillName = "Architecture Enforcer",
                agentName = "Gemini 3",
                action = "Write file",
                targetPath = "src/main/kotlin/com/example/App.kt",
                outcome = AuditOutcome.SUCCESS,
                durationMs = 210
            ),
            AuditEntry(
                id = "a7",
                timestamp = "2026-04-08 18:00:01",
                skillName = "Security Scanner",
                agentName = "Gemini 3",
                action = "Scan project",
                targetPath = null,
                outcome = AuditOutcome.SUCCESS,
                durationMs = 5500
            )
        )
    }
}

/** 审计用户意图 */
sealed interface AuditIntent {
    data object LoadAudit : AuditIntent()
    data class SelectEntry(val entry: AuditEntry) : AuditIntent()
    data object DismissDetail : AuditIntent()
}

/** 审计副作用 */
sealed interface AuditEffect {
    data class ShowToast(val message: String) : AuditEffect()
}

// ================================================================
// AgentSkillsDevKitState — 父级状态（组合所有子 Feature 状态）
// ================================================================

/**
 * Agent Skills DevKit 主状态
 *
 * 组合所有子 Feature 状态：
 * — skillEditor: Skill 编辑器状态
 * — permissionDashboard: 权限仪表盘状态
 * — audit: 审计视图状态
 *
 * @param currentTab 当前活跃的 Tab 索引（0=编辑器, 1=权限, 2=审计）
 * @param skillEditor Skill 编辑器子状态
 * @param permissionDashboard 权限仪表盘子状态
 * @param audit 审计视图子状态
 */
data class AgentSkillsDevKitState(
    val currentTab: Int = 0,
    val skillEditor: SkillEditorState = SkillEditorState(),
    val permissionDashboard: PermissionDashboardState = PermissionDashboardState(),
    val audit: AuditState = AuditState()
) {
    companion object {
        val Initial = AgentSkillsDevKitState()
    }
}

/** 顶层用户意图 */
sealed interface AgentSkillsDevKitIntent {
    data class SwitchTab(val tab: Int) : AgentSkillsDevKitIntent()

    // Skill Editor Intents
    data class EditorIntent(val intent: SkillEditorIntent) : AgentSkillsDevKitIntent()

    // Permission Dashboard Intents
    data class PermissionIntent(val intent: PermissionDashboardIntent) : AgentSkillsDevKitIntent()

    // Audit Intents
    data class AuditIntent(val intent: AuditIntent) : AgentSkillsDevKitIntent()
}

/** 顶层副作用 */
sealed interface AgentSkillsDevKitEffect {
    data class ShowToast(val message: String) : AgentSkillsDevKitEffect()
}
