package com.mvi.kenny.feature.gemma4

// ================================================================
// Gemma4Contract — Gemma 4 Agent Mode Toolkit MVI 契约
// ================================================================
// MVI architecture contract for Gemma 4 × Android Studio Agent Mode toolkit.
//
// PRD-098: Gemma 4 × Android Studio Agent Mode 本地编码 Agent 工具链
// Design Reference: memory/agency/designs/PRD-098-Gemma-4-Agent-Mode工具链.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

/**
 * ============================================================
 * ConnectionStatus — Gemma 4 连接状态枚举
 * ================================================================
 * Represents the connection state between Android Studio and Gemma 4 agent.
 *
 * @param displayName 中文显示名称
 */
enum class ConnectionStatus(val displayName: String) {
    /** 未连接（初始状态） */
    Disconnected("未连接"),
    /** 连接中 */
    Connecting("连接中"),
    /** 已连接 */
    Connected("已连接")
}

/**
 * ============================================================
 * GemmaVariant — Gemma 4 模型变体枚举
 * ================================================================
 * Gemma 4 model variants with different parameter counts.
 *
 * @param displayName 中文显示名称
 * @param paramCount 参数量（单位：B）
 * @param recommendedFor 推荐使用场景
 */
enum class GemmaVariant(
    val displayName: String,
    val paramCount: String,
    val recommendedFor: String,
    val vramRequirement: String,   // 显存需求 / VRAM requirement
    val speedRating: String       // 推理速度评级 / Speed rating
) {
    Gemma2B("2B 轻量版", "2B", "轻量级项目 / 快速原型", "~2GB VRAM", "★★★★★"),
    Gemma7B("7B 均衡版", "7B", "中等规模项目 / 常规开发", "~6GB VRAM", "★★★☆☆"),
    Gemma9B("9B 全性能", "9B", "大型项目 / 深度分析", "~8GB VRAM", "★★☆☆☆")
}

/**
 * ============================================================
 * FallbackMode — 降级策略模式枚举
 * ================================================================
 * Controls how the system handles Gemma 4 → Gemini API fallback.
 *
 * @param displayName 中文显示名称
 */
enum class FallbackMode(val displayName: String) {
    /** 仅使用本地 Gemma 4，不允许降级 */
    LocalOnly("仅本地 Gemma"),
    /** 自动降级，当条件触发时自动切换到 Gemini API */
    AutoSwitch("自动降级"),
    /** 手动模式，触发降级时提示用户手动选择 */
    ManualOnly("手动选择")
}

/**
 * ============================================================
 * ContextType — 上下文注入类型枚举
 * ================================================================
 * Types of project context that can be injected into the agent.
 *
 * @param displayName 中文显示名称
 * @param description 描述
 */
enum class ContextType(val displayName: String, val description: String) {
    KotlinVersion("Kotlin 版本", "注入 Kotlin 编译器版本和语言版本信息"),
    AGPVersion("AGP 版本", "注入 Android Gradle Plugin 版本信息"),
    DependencyTree("依赖树", "解析并注入 build.gradle 依赖结构"),
    ProjectSpec("项目规范", "注入编码规范文件路径和规则"),
    GitHistory("Git 历史", "注入最近提交记录作为上下文"),
    CICDConfig("CI/CD 配置", "注入 GitHub Actions 工作流配置")
}

/**
 * ============================================================
 * ToolCallStatus — 工具调用状态枚举
 * ================================================================
 * Represents the execution status of a tool call.
 */
enum class ToolCallStatus {
    Running,  // 执行中
    Success,  // 成功
    Failed    // 失败
}

/**
 * ============================================================
 * ToolType — 工具类型枚举
 * ================================================================
 * Types of tools that Gemma 4 can invoke.
 *
 * @param iconName Material icon name for display
 */
enum class ToolType(val displayName: String, val iconName: String) {
    ReadFile("读取文件", "description"),
    EditFile("编辑文件", "edit"),
    WriteFile("写入文件", "create"),
    ExecuteCommand("执行命令", "terminal"),
    SearchCode("搜索代码", "search"),
    Navigate("导航", "navigation"),
    BuildProject("构建项目", "build"),
    RunTests("运行测试", "fact_check"),
    Unknown("未知", "help")
}

/**
 * ============================================================
 * BenchmarkDimension — 质量基准测试维度枚举
 * ================================================================
 * Evaluation dimensions for code quality benchmarking.
 */
enum class BenchmarkDimension(val displayName: String, val weight: Float) {
    SyntaxCorrectness("语法正确性", 0.25f),
    LogicCompleteness("逻辑完整性", 0.30f),
    CodeReadability("代码可读性", 0.20f),
    KotlinIdioms("Kotlin 惯用法", 0.25f)
}

/**
 * ============================================================
 * ScanStatus — 扫描状态枚举
 * ================================================================
 * Represents the current scanning/processing status.
 */
enum class ScanStatus {
    Idle,
    Scanning,
    Completed,
    Error
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ToolCallEntry — 单次工具调用记录
 * ================================================================
 * Represents a single tool invocation by the Gemma 4 agent.
 *
 * @param id Unique identifier for this call
 * @param timestamp Execution timestamp (HH:mm:ss)
 * @param toolType Type of tool invoked
 * @param toolName Human-readable tool name
 * @param params Tool invocation parameters (JSON string)
 * @param result Execution result or error message
 * @param status Execution status (Running / Success / Failed)
 * @param durationMs Execution duration in milliseconds
 */
data class ToolCallEntry(
    val id: String,
    val timestamp: String,
    val toolType: ToolType,
    val toolName: String,
    val params: String,
    val result: String = "",
    val status: ToolCallStatus = ToolCallStatus.Running,
    val durationMs: Long = 0L
)

/**
 * ============================================================
 * AgentSession — Agent 会话信息
 * ================================================================
 * Represents an active Gemma 4 agent session.
 *
 * @param sessionId Session unique identifier
 * @param startTime Session start timestamp
 * @param totalTokenUsage Total tokens consumed in this session
 * @param toolCallCount Number of tool calls made
 * @param successCount Number of successful tool calls
 * @param failureCount Number of failed tool calls
 */
data class AgentSession(
    val sessionId: String,
    val startTime: String,
    val totalTokenUsage: Int = 0,
    val toolCallCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0
)

/**
 * ============================================================
 * ContextConfig — 上下文注入配置
 * ================================================================
 * Configuration for which context types are injected into the agent.
 *
 * @param enabledContexts Map of ContextType → enabled status
 * @param customContextPaths List of custom file paths to inject
 */
data class ContextConfig(
    val enabledContexts: Map<ContextType, Boolean> = ContextType.entries.associateWith { it in listOf(
        ContextType.KotlinVersion, ContextType.AGPVersion, ContextType.DependencyTree
    )},
    val customContextPaths: List<String> = emptyList()
)

/**
 * ============================================================
 * VariantRecommendation — 变体推荐结果
 * ================================================================
 * Represents a Gemma 4 variant recommendation based on project profile.
 *
 * @param variant The Gemma variant being recommended
 * @param confidenceScore Recommendation confidence (0.0–1.0)
 * @param reasons List of reasons for this recommendation
 * @param estimatedTime Estimated processing time for typical tasks
 */
data class VariantRecommendation(
    val variant: GemmaVariant,
    val confidenceScore: Float,
    val reasons: List<String>,
    val estimatedTime: String
)

/**
 * ============================================================
 * ProjectProfile — 项目特征
 * ================================================================
 * Project characteristics used for variant recommendation.
 *
 * @param codeLineRange Estimated lines of code range
 * @param moduleCount Number of modules in the project
 * @param hasKMP Whether the project uses Kotlin Multiplatform
 * @param hasNativeCode Whether the project contains native code
 * @param totalMemoryGB Total available RAM in GB
 * @param hasDiscreteGPU Whether the machine has a discrete GPU
 */
data class ProjectProfile(
    val codeLineRange: String = "10K-50K",
    val moduleCount: Int = 1,
    val hasKMP: Boolean = false,
    val hasNativeCode: Boolean = false,
    val totalMemoryGB: Int = 16,
    val hasDiscreteGPU: Boolean = false
)

/**
 * ============================================================
 * BenchmarkScore — 单次基准测试评分
 * ================================================================
 * Represents a benchmark score for a specific dimension.
 *
 * @param dimension The evaluation dimension
 * @param score Score value (0–100)
 */
data class BenchmarkScore(
    val dimension: BenchmarkDimension,
    val score: Int  // 0-100
)

/**
 * ============================================================
 * BenchmarkResult — 基准测试结果
 * ================================================================
 * Represents a complete benchmark test result.
 *
 * @param id Unique identifier
 * @param taskDescription Description of the benchmark task
 * @param promptA Description of prompt/configuration A
 * @param promptB Description of prompt/configuration B
 * @param scoresA Scores for configuration A
 * @param scoresB Scores for configuration B
 * @param overallScoreA Overall score for A
 * @param overallScoreB Overall score for B
 * @param timestamp Test completion timestamp
 * @param winner Which configuration won ("A", "B", or "Tie")
 */
data class BenchmarkResult(
    val id: String,
    val taskDescription: String,
    val promptA: String,
    val promptB: String,
    val scoresA: List<BenchmarkScore>,
    val scoresB: List<BenchmarkScore>,
    val overallScoreA: Int,
    val overallScoreB: Int,
    val timestamp: String,
    val winner: String  // "A" / "B" / "Tie"
)

/**
 * ============================================================
 * FallbackTrigger — 降级触发条件
 * ================================================================
 * Configuration for when to trigger fallback to Gemini API.
 *
 * @param tokenThreshold Token usage percentage threshold (0–100)
 * @param failureThreshold Consecutive failure count threshold
 * @param timeoutSeconds Inference timeout threshold in seconds
 */
data class FallbackTrigger(
    val tokenThreshold: Int = 80,    // 80% 上下文窗口
    val failureThreshold: Int = 3,     // 连续失败 3 次
    val timeoutSeconds: Int = 60      // 推理超时 60s
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * Gemma4State — Gemma 4 工具包页面状态
 * ================================================================
 * Immutable UI state — single source of truth for the entire toolkit.
 *
 * @param connectionStatus Current Gemma 4 connection status
 * @param currentModel Currently selected Gemma variant
 * @param activeSession Current agent session (null if not connected)
 * @param toolCalls List of tool call entries (chronological)
 * @param contextConfig Context injection configuration
 * @param fallbackMode Current fallback strategy mode
 * @param fallbackTrigger Current fallback trigger thresholds
 * @param benchmarkHistory Historical benchmark results
 * @param scanStatus Current scan/processing status
 * @param projectProfile Current project profile for variant recommendation
 * @param recommendations Variant recommendations based on project profile
 * @param isRunningBenchmark Whether a benchmark is currently executing
 * @param currentBenchmarkTask Current benchmark task description
 */
data class Gemma4State(
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val currentModel: GemmaVariant = GemmaVariant.Gemma7B,
    val activeSession: AgentSession? = null,
    val toolCalls: List<ToolCallEntry> = emptyList(),
    val contextConfig: ContextConfig = ContextConfig(),
    val fallbackMode: FallbackMode = FallbackMode.AutoSwitch,
    val fallbackTrigger: FallbackTrigger = FallbackTrigger(),
    val benchmarkHistory: List<BenchmarkResult> = emptyList(),
    val scanStatus: ScanStatus = ScanStatus.Idle,
    val projectProfile: ProjectProfile = ProjectProfile(),
    val recommendations: List<VariantRecommendation> = emptyList(),
    val isRunningBenchmark: Boolean = false,
    val currentBenchmarkTask: String = "",
    val isRealTimeMode: Boolean = true  // true = live, false = replay
) {
    /** Total tool calls count / 工具调用总次数 */
    val totalCalls: Int get() = toolCalls.size

    /** Successful tool calls count / 成功调用次数 */
    val successCalls: Int get() = toolCalls.count { it.status == ToolCallStatus.Success }

    /** Failed tool calls count / 失败调用次数 */
    val failedCalls: Int get() = toolCalls.count { it.status == ToolCallStatus.Failed }

    /** Current Gemma 4 ↔ Gemini API mode / 当前降级模式 */
    val currentModeDisplay: String
        get() = if (connectionStatus == ConnectionStatus.Connected) {
            "Gemma 4 ${currentModel.displayName}"
        } else {
            "未连接"
        }
}

// ================================================================
// Intent / 意图
// ================================================================

/**
 * ============================================================
 * Gemma4Intent — 用户操作意图
 * ================================================================
 * Sealed class representing all possible user intentions.
 * ViewModel receives intents via sendIntent() and processes them.
 */
sealed class Gemma4Intent {
    // ─────────────────────────────────────────────────────────
    // Connection / 连接管理
    // ─────────────────────────────────────────────────────────
    /** 连接 Gemma 4 Agent */
    data object ConnectAgent : Gemma4Intent()

    /** 断开 Gemma 4 Agent */
    data object DisconnectAgent : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Variant Selection / 变体选择
    // ─────────────────────────────────────────────────────────
    /** 选择 Gemma 变体 */
    data class SelectVariant(val variant: GemmaVariant) : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Context Injection / 上下文注入
    // ─────────────────────────────────────────────────────────
    /** 切换上下文注入类型的启用状态 */
    data class ToggleContextInjection(
        val contextType: ContextType,
        val enabled: Boolean
    ) : Gemma4Intent()

    /** 添加自定义上下文文件路径 */
    data class AddCustomContextPath(val path: String) : Gemma4Intent()

    /** 移除自定义上下文文件路径 */
    data class RemoveCustomContextPath(val path: String) : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Benchmark / 基准测试
    // ─────────────────────────────────────────────────────────
    /** 运行代码质量基准测试 */
    data class RunBenchmark(
        val task: String,
        val promptA: String,
        val promptB: String
    ) : Gemma4Intent()

    /** 加载历史基准测试记录 */
    data object LoadBenchmarkHistory : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Fallback Strategy / 降级策略
    // ─────────────────────────────────────────────────────────
    /** 配置降级策略模式 */
    data class ConfigureFallbackMode(val mode: FallbackMode) : Gemma4Intent()

    /** 更新降级触发条件阈值 */
    data class UpdateFallbackTrigger(val trigger: FallbackTrigger) : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Tool Call Visualization / 工具调用可视化
    // ─────────────────────────────────────────────────────────
    /** 切换实时/回放模式 */
    data class SetRealTimeMode(val enabled: Boolean) : Gemma4Intent()

    /** 清空工具调用历史 */
    data object ClearToolCalls : Gemma4Intent()

    /** 展开/收起工具调用详情 */
    data class ToggleToolCallDetail(val callId: String) : Gemma4Intent()

    // ─────────────────────────────────────────────────────────
    // Variant Advisor / 变体选型顾问
    // ─────────────────────────────────────────────────────────
    /** 更新项目特征 */
    data class UpdateProjectProfile(val profile: ProjectProfile) : Gemma4Intent()

    /** 请求变体推荐分析 */
    data object RequestVariantRecommendation : Gemma4Intent()
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * Gemma4Effect — 一次性副作用
 * ================================================================
 * One-time side effects triggered by ViewModel.
 * Delivered via Channel<UiEffect> and consumed by the UI layer.
 */
sealed class Gemma4Effect {
    /** 显示降级通知 (from → to) */
    data class ShowFallbackNotification(
        val from: String,
        val to: String
    ) : Gemma4Effect()

    /** 显示错误消息 */
    data class ShowError(val message: String) : Gemma4Effect()

    /** 基准测试完成通知 */
    data class BenchmarkCompleted(val result: BenchmarkResult) : Gemma4Effect()

    /** 连接成功通知 */
    data object ConnectionSuccess : Gemma4Effect()

    /** 连接失败通知 */
    data class ConnectionFailed(val reason: String) : Gemma4Effect()

    /** 推荐生成完成通知 */
    data class RecommendationReady(val count: Int) : Gemma4Effect()
}
