package com.mvi.kenny.feature.androidagenttoolkit

/**
 * ============================================================
 * AndroidAgentToolkitContract — Android CLI/Skills/KB AI Agent 工具链 MVI 契约
 * ============================================================
 * PRD-126 | Android CLI + Android Skills + Android Knowledge Base AI Agent 开发工具包
 *
 * MVI 三要素 / Three pillars of MVI:
 * — State: 页面状态的唯一真相来源，Immutable data class
 * — Intent: 用户意图，ViewModel 收到后执行业务逻辑
 * — Effect: 一次性副作用，通过 Channel 传递（Toast / 导航 / 剪贴板等）
 *
 * @see AndroidAgentToolkitViewModel
 * @see AndroidAgentToolkitScreen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object AndroidAgentToolkitTab {
    const val DASHBOARD = 0
    const val CLI = 1
    const val SKILLS = 2
    const val KNOWLEDGE_BASE = 3
    const val CICD = 4
    const val SETTINGS = 5
}

// ================================================================
// Module status / 模块状态
// ================================================================
/**
 * 工具链模块状态 / Toolchain module status
 *
 * INSTALLED: 已安装并可用
 * NOT_INSTALLED: 未安装
 * PENDING_CONFIG: 已安装但待配置
 */
enum class ModuleState {
    INSTALLED, NOT_INSTALLED, PENDING_CONFIG
}

/**
 * 模块状态信息 / Module status info
 *
 * @param name 模块名称 / Module name
 * @param description 模块描述 / Module description
 * @param state 模块状态 / Module state
 * @param version 当前版本 / Current version
 */
data class ModuleStatus(
    val name: String,
    val description: String,
    val state: ModuleState,
    val version: String
)

// ================================================================
// Token saving stats / Token 节省统计
// ================================================================
/**
 * Token 节省统计数据 / Token saving statistics
 *
 * @param totalTokens 总 Token 数 / Total tokens
 * @param savedTokens 节省 Token 数 / Saved tokens
 * @param savingPercent 节省百分比 / Saving percentage
 * @param projectCount 项目数 / Project count
 */
data class TokenSavingStats(
    val totalTokens: Int,
    val savedTokens: Int,
    val savingPercent: Int,
    val projectCount: Int
)

// ================================================================
// CLI Tab types / CLI Tab 类型
// ================================================================
/**
 * 命令执行状态 / Command execution status
 *
 * SUCCESS: 执行成功 / Execution succeeded
 * FAILED: 执行失败 / Execution failed
 * RUNNING: 执行中 / Running
 */
enum class CommandStatus {
    SUCCESS, FAILED, RUNNING
}

/**
 * CLI 命令执行记录 / CLI command execution record
 *
 * @param id 记录 ID / Record ID
 * @param command 命令内容 / Command content
 * @param timestamp 执行时间戳 / Execution timestamp
 * @param status 执行状态 / Execution status
 * @param output 标准输出 / Standard output
 * @param errorOutput 错误输出 / Error output
 */
data class CommandRecord(
    val id: String,
    val command: String,
    val timestamp: Long,
    val status: CommandStatus,
    val output: String = "",
    val errorOutput: String = ""
)

// ================================================================
// Skills Tab types / Skills Tab 类型
// ================================================================
/**
 * Skill 信息 / Skill information
 *
 * @param id Skill ID
 * @param name Skill 名称 / Skill name
 * @param version 版本号 / Version number
 * @param description 功能描述 / Description
 * @param scenarios 适用场景标签 / Applicable scenario tags
 * @param cliVersion 兼容 CLI 版本 / Compatible CLI version
 * @param agpVersion 兼容 AGP 版本 / Compatible AGP version
 * @param isInstalled 是否已安装 / Is installed
 */
data class SkillInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val scenarios: List<String>,
    val cliVersion: String,
    val agpVersion: String,
    val isInstalled: Boolean
)

/**
 * 兼容性矩阵单元格 / Compatibility matrix cell
 *
 * @param skillVersion Skill 版本 / Skill version
 * @param cliVersion CLI 版本 / CLI version
 * @param agpVersion AGP 版本 / AGP version
 * @param isCompatible 是否兼容 / Is compatible
 */
data class CompatibilityCell(
    val skillVersion: String,
    val cliVersion: String,
    val agpVersion: String,
    val isCompatible: Boolean
)

/**
 * 兼容性矩阵 / Compatibility matrix
 *
 * @param skillVersions Skill 版本列表 / Skill version list
 * @param cliVersions CLI 版本列表 / CLI version list
 * @param agpVersions AGP 版本列表 / AGP version list
 * @param cells 矩阵单元格 / Matrix cells
 */
data class CompatibilityMatrix(
    val skillVersions: List<String>,
    val cliVersions: List<String>,
    val agpVersions: List<String>,
    val cells: List<CompatibilityCell>
)

// ================================================================
// Knowledge Base Tab types / Knowledge Base Tab 类型
// ================================================================
/**
 * 同步状态 / Sync state
 *
 * IDLE: 空闲 / Idle
 * SYNCING: 同步中 / Syncing
 * ERROR: 错误 / Error
 * OFFLINE: 离线 / Offline
 */
enum class SyncState {
    IDLE, SYNCING, ERROR, OFFLINE
}

/**
 * 知识库同步状态 / Knowledge base sync status
 *
 * @param state 同步状态 / Sync state
 * @param progress 进度 (0.0-1.0) / Progress
 * @param errorMessage 错误信息 / Error message
 */
data class SyncStatus(
    val state: SyncState,
    val progress: Float = 0f,
    val errorMessage: String? = null
)

/**
 * 知识库条目 / Knowledge base entry
 *
 * @param id 条目 ID / Entry ID
 * @param title 标题 / Title
 * @param category 分类 / Category
 * @param androidVersion Android 版本 / Android version
 * @param snippet 内容摘要 / Content snippet
 * @param relevanceScore 相关性评分 / Relevance score
 */
data class KBEntry(
    val id: String,
    val title: String,
    val category: String,
    val androidVersion: String,
    val snippet: String,
    val relevanceScore: Float
)

// ================================================================
// CI/CD Tab types / CI/CD Tab 类型
// ================================================================
/**
 * 流水线模板类型 / Pipeline template type
 *
 * GITHUB_ACTIONS: GitHub Actions
 * GITLAB_CI: GitLab CI
 * JENKINS: Jenkins
 */
enum class PipelineTemplate {
    GITHUB_ACTIONS, GITLAB_CI, JENKINS
}

/**
 * 流水线问题 / Pipeline issue
 *
 * @param id 问题 ID / Issue ID
 * @param severity 严重程度 (error/warning/info) / Severity
 * @param message 问题描述 / Issue message
 * @param lineNumber 所在行号 / Line number
 */
data class PipelineIssue(
    val id: String,
    val severity: String,
    val message: String,
    val lineNumber: Int? = null
)

// ================================================================
// Settings types / 设置类型
// ================================================================
/**
 * AI Provider 类型 / AI Provider type
 *
 * GEMINI: Google Gemini
 * CLAUDE: Anthropic Claude
 * GPT: OpenAI GPT
 * LOCAL: 本地模型 / Local model
 */
enum class AIProvider {
    GEMINI, CLAUDE, GPT, LOCAL
}

/**
 * 输出格式 / Output format
 *
 * HUMAN_READABLE: 人类可读 / Human readable
 * JSON: JSON 格式 / JSON format
 * MARKDOWN: Markdown 格式 / Markdown format
 */
enum class OutputFormat {
    HUMAN_READABLE, JSON, MARKDOWN
}

/**
 * 日志级别 / Log level
 *
 * DEBUG: 调试 / Debug
 * INFO: 信息 / Info
 * WARNING: 警告 / Warning
 * ERROR: 错误 / Error
 */
enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR
}

// ================================================================
// State / 页面状态
// ================================================================
/**
 * 页面状态 / Page state
 *
 * @param selectedTab 当前 Tab 索引 / Current tab index
 * // CLI Tab / CLI Tab
 * @param commandInput 命令输入 / Command input
 * @param commandHistory 命令历史 / Command history
 * @param isExecuting 是否正在执行 / Is executing
 * @param availableCommands 可用命令列表 / Available commands
 * // Skills Tab / Skills Tab
 * @param installedSkills 已安装 Skill 列表 / Installed skills
 * @param skillSearchQuery Skill 搜索关键词 / Skill search query
 * @param skillMarketplace Skill 市场列表 / Skill marketplace
 * @param selectedSkill 选中的 Skill / Selected skill
 * @param skillCompatibilityMatrix 兼容性矩阵 / Compatibility matrix
 * // KB Tab / KB Tab
 * @param kbSyncStatus 知识库同步状态 / KB sync status
 * @param kbLastSyncTime 上次同步时间 / Last sync time
 * @param kbCacheSize 缓存大小 / Cache size
 * @param kbQueryInput 知识库查询输入 / KB query input
 * @param kbSearchResults 搜索结果 / Search results
 * @param kbCategories 知识库分类 / KB categories
 * // CI/CD Tab / CI/CD Tab
 * @param selectedPipelineTemplate 选中的流水线模板 / Selected pipeline template
 * @param pipelineYaml 流水线 YAML 配置 / Pipeline YAML config
 * @param pipelineDiagnostics 流水线诊断问题列表 / Pipeline diagnostics
 * @param envVariables 环境变量 Map / Environment variables
 * // Settings / Settings
 * @param aiProvider AI Provider 配置 / AI Provider config
 * @param outputFormat 输出格式 / Output format
 * @param logLevel 日志级别 / Log level
 * // Dashboard / Dashboard
 * @param moduleStatuses 模块状态列表 / Module statuses
 * @param tokenSavingStats Token 节省统计 / Token saving stats
 * @param errorMessage 错误信息 / Error message
 */
data class AndroidAgentToolkitState(
    val selectedTab: Int = AndroidAgentToolkitTab.DASHBOARD,
    // CLI Tab
    val commandInput: String = "",
    val commandHistory: List<CommandRecord> = emptyList(),
    val isExecuting: Boolean = false,
    val availableCommands: List<String> = listOf(
        "sdkmanager", "avdmanager", "apksigner", "d8", "lint", "gradle"
    ),
    // Skills Tab
    val installedSkills: List<SkillInfo> = emptyList(),
    val skillSearchQuery: String = "",
    val skillMarketplace: List<SkillInfo> = emptyList(),
    val selectedSkill: SkillInfo? = null,
    val skillCompatibilityMatrix: CompatibilityMatrix? = null,
    // KB Tab
    val kbSyncStatus: SyncStatus = SyncStatus(SyncState.IDLE),
    val kbLastSyncTime: String = "2026-04-17 08:00",
    val kbCacheSize: String = "128 MB",
    val kbQueryInput: String = "",
    val kbSearchResults: List<KBEntry> = emptyList(),
    val kbCategories: List<String> = listOf(
        "Compose", "Navigation", "Performance", "Security", "KMP", "Tools"
    ),
    // CI/CD Tab
    val selectedPipelineTemplate: PipelineTemplate = PipelineTemplate.GITHUB_ACTIONS,
    val pipelineYaml: String = "",
    val pipelineDiagnostics: List<PipelineIssue> = emptyList(),
    val envVariables: Map<String, String> = emptyMap(),
    // Settings
    val aiProvider: AIProvider = AIProvider.GEMINI,
    val outputFormat: OutputFormat = OutputFormat.HUMAN_READABLE,
    val logLevel: LogLevel = LogLevel.INFO,
    // Dashboard
    val moduleStatuses: List<ModuleStatus> = emptyList(),
    val tokenSavingStats: TokenSavingStats? = null,
    val errorMessage: String? = null
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = AndroidAgentToolkitState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================
/**
 * 用户意图 / User intent
 *
 * @see AndroidAgentToolkitViewModel.sendIntent
 */
sealed interface AndroidAgentToolkitIntent {
    // Tab Navigation / Tab 导航
    /** 切换 Tab / Select tab */
    data class SelectTab(val index: Int) : AndroidAgentToolkitIntent

    // CLI / CLI
    /** 更新命令输入 / Update command input */
    data class UpdateCommandInput(val input: String) : AndroidAgentToolkitIntent
    /** 执行命令 / Execute command */
    data object ExecuteCommand : AndroidAgentToolkitIntent
    /** 取消命令执行 / Cancel command execution */
    data object CancelCommand : AndroidAgentToolkitIntent
    /** 清空命令历史 / Clear command history */
    data object ClearCommandHistory : AndroidAgentToolkitIntent

    // Skills / Skills
    /** 更新 Skill 搜索 / Update skill search */
    data class UpdateSkillSearch(val query: String) : AndroidAgentToolkitIntent
    /** 选中 Skill / Select skill */
    data class SelectSkill(val skill: SkillInfo) : AndroidAgentToolkitIntent
    /** 安装选中 Skill / Install selected skill */
    data object InstallSelectedSkill : AndroidAgentToolkitIntent
    /** 卸载选中 Skill / Uninstall selected skill */
    data object UninstallSelectedSkill : AndroidAgentToolkitIntent
    /** 创建新 Skill / Create new skill */
    data object CreateNewSkill : AndroidAgentToolkitIntent

    // Knowledge Base / 知识库
    /** 更新 KB 查询 / Update KB query */
    data class UpdateKBQuery(val query: String) : AndroidAgentToolkitIntent
    /** 同步知识库 / Sync knowledge base */
    data object SyncKnowledgeBase : AndroidAgentToolkitIntent
    /** 清空知识库缓存 / Clear KB cache */
    data object ClearKBCache : AndroidAgentToolkitIntent
    /** 下载 KB 子集 / Download KB subset */
    data class DownloadKBSubset(val categories: List<String>) : AndroidAgentToolkitIntent

    // CI/CD
    /** 选择流水线模板 / Select pipeline template */
    data class SelectPipelineTemplate(val template: PipelineTemplate) : AndroidAgentToolkitIntent
    /** 更新流水线 YAML / Update pipeline YAML */
    data class UpdatePipelineYaml(val yaml: String) : AndroidAgentToolkitIntent
    /** 更新环境变量 / Update environment variable */
    data class UpdateEnvVariable(val key: String, val value: String) : AndroidAgentToolkitIntent
    /** 运行流水线诊断 / Run pipeline diagnostics */
    data object RunPipelineDiagnostics : AndroidAgentToolkitIntent
    /** 导出流水线配置 / Export pipeline config */
    data object ExportPipelineConfig : AndroidAgentToolkitIntent

    // Settings / 设置
    /** 更新 AI Provider / Update AI provider */
    data class UpdateAIProvider(val provider: AIProvider) : AndroidAgentToolkitIntent
    /** 更新输出格式 / Update output format */
    data class UpdateOutputFormat(val format: OutputFormat) : AndroidAgentToolkitIntent
    /** 更新日志级别 / Update log level */
    data class UpdateLogLevel(val level: LogLevel) : AndroidAgentToolkitIntent

    // Dashboard / 仪表盘
    /** 刷新仪表盘 / Refresh dashboard */
    data object RefreshDashboard : AndroidAgentToolkitIntent
}

// ================================================================
// Effect / 副作用
// ================================================================
/**
 * 副作用 / Side effects
 * 一次性事件，通过 Channel 传递 / One-time events, delivered via Channel
 *
 * @see AndroidAgentToolkitViewModel
 */
sealed interface AndroidAgentToolkitEffect {
    /** 显示 Toast 提示 / Show toast message */
    data class ShowToast(val message: String) : AndroidAgentToolkitEffect

    /** 显示错误对话框 / Show error dialog */
    data class ShowError(val title: String, val message: String) : AndroidAgentToolkitEffect

    /** 复制到剪贴板 / Copy to clipboard */
    data class CopyToClipboard(val text: String) : AndroidAgentToolkitEffect

    /** 触发流水线导出 / Trigger pipeline export */
    data class TriggerPipelineExport(val yaml: String) : AndroidAgentToolkitEffect

    /** 记录 Agent 命令日志 / Log agent command */
    data class LogAgentCommand(val command: String, val output: String) : AndroidAgentToolkitEffect
}
