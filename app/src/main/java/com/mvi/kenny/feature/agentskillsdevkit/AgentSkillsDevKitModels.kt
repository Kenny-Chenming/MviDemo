package com.mvi.kenny.feature.agentskillsdevkit

/**
 * ============================================================
 * AgentSkillsDevKitModels — Supporting Data Classes
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 * Mobile Prototype — Supporting data models
 *
 * @see AgentSkillsDevKitContract
 * @see AgentSkillsDevKitViewModel
 * @see AgentSkillsDevKitScreen
 * —————————————————————————————————————————————————————
 */

// ================================================================
// ValidationError — DSL 校验错误
// ================================================================

/**
 * DSL 校验错误
 * @param line 错误所在行号（1-indexed）
 * @param message 错误描述信息
 */
data class ValidationError(
    val line: Int,
    val message: String
)

// ================================================================
// SaveResult — 保存操作结果
// ================================================================

/**
 * 保存操作结果
 * @param success 是否保存成功
 * @param message 结果描述信息
 */
data class SaveResult(
    val success: Boolean,
    val message: String
)

// ================================================================
// TestResult — 本地测试结果
// ================================================================

/**
 * 本地测试结果
 * @param passed 测试是否通过
 * @param logs 测试日志列表
 */
data class TestResult(
    val passed: Boolean,
    val logs: List<String>
)

// ================================================================
// PermissionMode — 权限模式枚举
// ================================================================

/**
 * 权限模式
 * @param symbol 显示符号
 * @param colorArgb ARGB 颜色值
 */
enum class PermissionMode(val symbol: String, val colorArgb: Long) {
    ALLOW(    "✓", 0xFF81C784), // 绿色：完全允许
    READ_ONLY("R",  0xFFFFB74D), // 黄色：只读
    DENY(     "✕", 0xFFE57373), // 红色：拒绝
    UNSET(    "—", 0xFF9E9E9E)  // 灰色：未设置
}

// ================================================================
// PermissionNode — 权限树节点
// ================================================================

/**
 * 权限树节点
 * @param path 节点路径
 * @param name 显示名称
 * @param isDirectory 是否为目录
 * @param mode 当前权限模式
 * @param children 子节点列表
 * @param isExpanded 是否展开（UI 状态）
 */
data class PermissionNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val mode: PermissionMode = PermissionMode.UNSET,
    val children: List<PermissionNode> = emptyList(),
    val isExpanded: Boolean = false
)

// ================================================================
// PermissionTemplate — 权限模板
// ================================================================

/**
 * 权限模板
 * @param name 模板名称
 * @param nodes 模板包含的权限节点列表
 */
data class PermissionTemplate(
    val name: String,
    val nodes: List<PermissionNode>
)

// ================================================================
// AuditEntry — 审计日志条目
// ================================================================

/**
 * 审计结果
 * @param displayName 显示名称
 * @param colorArgb ARGB 颜色值
 */
enum class AuditOutcome(val displayName: String, val colorArgb: Long) {
    SUCCESS("成功", 0xFF81C784),
    DENIED("拒绝", 0xFFE57373),
    WARNING("警告", 0xFFFFB74D),
    ERROR("错误", 0xFFEF5350),
}

/**
 * 审计日志条目
 * @param id 唯一标识
 * @param timestamp 时间戳
 * @param skillName Skill 名称
 * @param agentName Agent 名称
 * @param action 操作描述
 * @param targetPath 目标路径
 * @param outcome 执行结果
 * @param durationMs 耗时（毫秒）
 */
data class AuditEntry(
    val id: String,
    val timestamp: String,
    val skillName: String,
    val agentName: String,
    val action: String,
    val targetPath: String?,
    val outcome: AuditOutcome,
    val durationMs: Long
)
