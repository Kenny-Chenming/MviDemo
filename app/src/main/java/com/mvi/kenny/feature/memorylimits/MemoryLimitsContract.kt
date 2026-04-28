package com.mvi.kenny.feature.memorylimits

// ================================================================
// MemoryLimitsContract — Android 17 App Memory Limits 开发工具包 MVI Contract
// ================================================================
// MVI architecture contract for Android 17 per-app memory limits toolkit.
//
// PRD-194: Android 17 App Memory Limits 开发工具包
// Design Reference: memory/agency/designs/PRD-194-Android-17-App-Memory-Limits-开发工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// This toolkit provides 8 developer tools:
//   1. MemoryLimiter Impact Scanner (Gradle Task)
//   2. MemoryLimiter CI Simulator (GitHub Actions template)
//   3. Trigger-based Profiling Integration Guide (TRIGGER_TYPE_ANOMALY)
//   4. App Memory Budget Calculator (2GB/4GB/6GB/8GB/12GB device mapping)
//   5. Memory Over-limit Diagnostic Workflow (Detection → Heap Dump → Analysis → Fix)
//   6. Android 17 Memory Optimization Checklist
//   7. Memory-sensitive CI Performance Benchmark Tool
//   8. MemoryLimiter vs OOM Differentiation Guide
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// RiskLevel — 内存风险等级
// ================================================================
/**
 * ============================================================
 * RiskLevel — 内存风险等级枚举
 * ================================================================
 * Represents the memory risk level for an app on a given device RAM tier.
 * CRITICAL: App almost certainly will be killed by MemoryLimiter
 * HIGH: High probability of being killed under normal usage
 * MEDIUM: May be killed under memory pressure
 * LOW: Should survive on this device tier
 * UNKNOWN: Unable to assess (missing data)
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation for terminal output
 * @param color UI color for this risk level
 * @param priority Numeric priority (1=highest risk)
 */
enum class RiskLevel(
    val displayName: String,
    val displayNameEn: String,
    val emoji: String,
    val color: Color,
    val priority: Int
) {
    CRITICAL("极高风险", "Critical", "⚫", Color(0xFF6E1A1A), 1),   // 深红，黑底
    HIGH("高风险", "High", "🔴", Color(0xFFEF4444), 2),             // 红色
    MEDIUM("中风险", "Medium", "🟡", Color(0xFFF59E0B), 3),          // 黄色
    LOW("低风险", "Low", "🟢", Color(0xFF3FB950), 4),               // 绿色
    UNKNOWN("未知", "Unknown", "❓", Color(0xFF8B949E), 5)          // 灰色
}

// ================================================================
// DeviceRamTier — 设备 RAM 分层
// ================================================================
/**
 * ============================================================
 * DeviceRamTier — 设备 RAM 分层
 * ================================================================
 * Represents device RAM tiers for per-app memory limit calculation.
 * Each tier has a system-reserved amount and a per-app usable limit.
 *
 * Per-app memory limit formula (Android 17):
 *   limit = deviceRam * (1 - systemReserveRatio)
 *   systemReserveRatio varies by tier (smaller devices reserve more %)
 *
 * @param ramGb Total RAM in GB
 * @param perAppLimitMb Per-app memory limit in MB
 * @param systemReserveRatio Ratio of RAM reserved for system
 */
enum class DeviceRamTier(
    val ramGb: Int,
    val perAppLimitMb: Long,
    val systemReserveRatio: Float,
    val displayName: String
) {
    TIER_2GB(2, 512, 0.74f, "2GB 设备"),
    TIER_4GB(4, 1024, 0.75f, "4GB 设备"),
    TIER_6GB(6, 1536, 0.74f, "6GB 设备"),
    TIER_8GB(8, 2048, 0.74f, "8GB 设备"),
    TIER_12GB(12, 3072, 0.74f, "12GB 设备");

    /**
     * 获取该分层的系统预留内存（MB）
     */
    val systemReserveMb: Long
        get() = (ramGb * 1024L) - perAppLimitMb

    companion object {
        /**
         * 根据 RAM 大小获取分层
         * @param ramGb RAM 大小（GB）
         * @return 最接近的 DeviceRamTier，不在范围内则返回 null
         */
        fun fromRamGb(ramGb: Int): DeviceRamTier? {
            return entries.minByOrNull { kotlin.math.abs(it.ramGb - ramGb) }
                ?.takeIf { kotlin.math.abs(it.ramGb - ramGb) <= 1 }
        }
    }
}

// ================================================================
// MemoryFinding — 内存风险发现
// ================================================================
/**
 * ============================================================
 * MemoryFinding — 内存风险发现
 * ================================================================
 * Represents a single finding from the memory risk scan.
 *
 * @property category 问题类别
 * @property description 问题描述
 * @property suggestedFix 建议修复方案
 * @property estimatedMemoryMb 预估占用内存（MB）
 * @property severity 严重程度
 */
data class MemoryFinding(
    val category: MemoryFindingCategory,
    val description: String,
    val descriptionEn: String,
    val suggestedFix: String,
    val suggestedFixEn: String,
    val estimatedMemoryMb: Long = 0L,
    val severity: RiskLevel = RiskLevel.UNKNOWN
)

/**
 * ============================================================
 * MemoryFindingCategory — 内存问题类别
 * ================================================================
 */
enum class MemoryFindingCategory(
    val displayName: String,
    val displayNameEn: String,
    val emoji: String
) {
    IMAGE_CACHE("图片缓存", "Image Cache", "🖼️"),
    MEMORY_LEAK("内存泄漏", "Memory Leak", "💧"),
    LARGE_OBJECT("大对象", "Large Object", "📦"),
    BACKGROUND_PROCESS("后台进程", "Background Process", "🔄"),
    UNSAFE_API("不安全 API", "Unsafe API Usage", "⚠️"),
    TARGET_SDK("Target SDK", "Target SDK", "🎯"),
    OTHER("其他", "Other", "📋")
}

// ================================================================
// ScanPhase — 扫描阶段
// ================================================================
/**
 * ============================================================
 * ScanPhase — 扫描阶段枚举
 * ================================================================
 * Represents the current phase of the memory risk scan.
 */
enum class ScanPhase {
    IDLE,                    // 初始空闲状态
    SCANNING_CODE,           // 扫描代码中的内存问题
    CALCULATING_RISK,        // 计算各 RAM 分层风险
    ANALYZING_HEAP,          // 分析堆内存使用
    GENERATING_REPORT,       // 生成报告
    COMPLETED,               // 扫描完成
    ERROR                    // 扫描出错
}

// ================================================================
// MemoryScanState — 内存扫描器状态（MVI State）
// ================================================================
/**
 * ============================================================
 * MemoryScanState — 内存扫描器状态（MVI State）
 * ================================================================
 * Immutable state representing the memory risk scanner's current status.
 *
 * @property phase Current scan phase
 * @property progress Overall progress percentage (0-100)
 * @property findings List of detected memory findings
 * @property scannedFilesCount Number of files scanned
 * @property currentPhaseDescription Current phase description
 * @property errorMessage Error message if phase is ERROR
 * @property startTime Scan start timestamp
 * @property endTime Scan end timestamp
 */
data class MemoryScanState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val findings: List<MemoryFinding> = emptyList(),
    val scannedFilesCount: Int = 0,
    val currentPhaseDescription: String = "就绪",
    val errorMessage: String? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L
) {
    /**
     * 是否正在扫描中
     * Whether a scan is currently in progress
     */
    val isScanning: Boolean
        get() = phase != ScanPhase.IDLE &&
                phase != ScanPhase.COMPLETED &&
                phase != ScanPhase.ERROR

    /**
     * 扫描总耗时（毫秒）
     * Total scan duration in milliseconds
     */
    val durationMs: Long
        get() = if (endTime > 0 && startTime > 0) endTime - startTime else 0L

    /**
     * 按类别分组的发现
     * Findings grouped by category
     */
    val findingsByCategory: Map<MemoryFindingCategory, List<MemoryFinding>>
        get() = findings.groupBy { it.category }

    /**
     * 按风险等级分组的发现
     * Findings grouped by risk level
     */
    val findingsByRisk: Map<RiskLevel, List<MemoryFinding>>
        get() = findings.groupBy { it.severity }

    /**
     * CRITICAL 风险发现数量
     */
    val criticalCount: Int
        get() = findings.count { it.severity == RiskLevel.CRITICAL }

    /**
     * HIGH 风险发现数量
     */
    val highCount: Int
        get() = findings.count { it.severity == RiskLevel.HIGH }

    /**
     * MEDIUM 风险发现数量
     */
    val mediumCount: Int
        get() = findings.count { it.severity == RiskLevel.MEDIUM }

    /**
     * LOW 风险发现数量
     */
    val lowCount: Int
        get() = findings.count { it.severity == RiskLevel.LOW }
}

// ================================================================
// RiskAssessment — 风险评估结果
// ================================================================
/**
 * ============================================================
 * RiskAssessment — 风险评估结果
 * ================================================================
 * Risk assessment result for a specific device RAM tier.
 *
 * @property tier Device RAM tier
 * @property riskLevel Overall risk level for this tier
 * @property appEstimatedMemoryMb 预估 App 内存占用（MB）
 * @property limitMb 该分层 Per-app 内存上限（MB）
 * @property headroomMb 剩余空间（MB）
 * @property findings 导致该风险等级的发现列表
 */
data class RiskAssessment(
    val tier: DeviceRamTier,
    val riskLevel: RiskLevel,
    val appEstimatedMemoryMb: Long,
    val limitMb: Long,
    val headroomMb: Long,
    val findings: List<MemoryFinding> = emptyList()
) {
    /**
     * 内存使用率（%）
     * Memory usage percentage
     */
    val usagePercent: Float
        get() = if (limitMb > 0) (appEstimatedMemoryMb.toFloat() / limitMb * 100f) else 0f

    /**
     * 是否超出限制
     * Whether the app exceeds the memory limit
     */
    val isOverLimit: Boolean
        get() = appEstimatedMemoryMb > limitMb
}

// ================================================================
// MemoryBudget — 内存预算
// ================================================================
/**
 * ============================================================
 * MemoryBudget — 内存预算
 * ================================================================
 * Represents a memory budget recommendation for a device tier.
 *
 * @property tier Device RAM tier
 * @property recommendedLimitMb 推荐内存上限（MB）
 * @property warningThresholdMb 警告阈值（MB）
 * @property criticalThresholdMb 危险阈值（MB）
 * @property moduleBudgets 各模块内存预算分配
 */
data class MemoryBudget(
    val tier: DeviceRamTier,
    val recommendedLimitMb: Long,
    val warningThresholdMb: Long,
    val criticalThresholdMb: Long,
    val moduleBudgets: Map<String, Long> = emptyMap()  // 模块名 -> 预算（MB）
)

// ================================================================
// MemoryLimitsIntent — 用户意图（MVI Intent）
// ================================================================
/**
 * ============================================================
 * MemoryLimitsIntent — 内存限制工具用户意图（MVI Intent）
 * ================================================================
 * User intentions that the ViewModel processes.
 */
sealed class MemoryLimitsIntent {
    /**
     * 开始内存风险扫描
     * Start memory risk scan
     *
     * @property sourceDir 项目源码目录
     * @property packageName 应用包名
     */
    data class StartScan(
        val sourceDir: String,
        val packageName: String
    ) : MemoryLimitsIntent()

    /**
     * 取消正在进行的扫描
     * Cancel ongoing scan
     */
    data object CancelScan : MemoryLimitsIntent()

    /**
     * 清除扫描结果
     * Clear scan results
     */
    data object ClearResults : MemoryLimitsIntent()

    /**
     * 计算内存预算
     * Calculate memory budget
     *
     * @property deviceRamGb 设备 RAM 大小（GB）
     * @property appType App 类型
     */
    data class CalculateBudget(
        val deviceRamGb: Int,
        val appType: AppType
    ) : MemoryLimitsIntent()

    /**
     * 运行诊断工作流
     * Run diagnostic workflow
     *
     * @property packageName 应用包名
     */
    data class RunDiagnostic(
        val packageName: String
    ) : MemoryLimitsIntent()

    /**
     * 导出报告
     * Export report
     *
     * @property format 报告格式
     * @property outputPath 输出文件路径
     */
    data class ExportReport(
        val format: ReportFormat,
        val outputPath: String
    ) : MemoryLimitsIntent()
}

// ================================================================
// AppType — App 类型
// ================================================================
/**
 * ============================================================
 * AppType — App 类型枚举
 * ================================================================
 * Represents the type of application for memory budget calculation.
 *
 * @property displayName 中文显示名称
 * @property memoryMultiplier Memory budget multiplier (higher for media-heavy apps)
 */
enum class AppType(
    val displayName: String,
    val memoryMultiplier: Float
) {
    GENERAL("通用 App", 0.70f),           // 70% of per-app limit
    MEDIA("媒体/图片 App", 0.60f),         // 60% (images, video, camera)
    GAME("游戏 App", 0.55f),              // 55% (graphics-intensive)
    SOCIAL("社交 App", 0.65f),            // 65% (mix of content types)
    ECOMMERCE("电商 App", 0.65f),        // 65% (images + content)
    NEWS("新闻阅读 App", 0.70f);         // 70% (text + some images)
}

// ================================================================
// ReportFormat — 报告格式
// ================================================================
/**
 * ============================================================
 * ReportFormat — 报告格式枚举
 * ================================================================
 */
enum class ReportFormat(val extension: String, val mimeType: String) {
    HTML("html", "text/html"),
    MARKDOWN("md", "text/markdown"),
    JSON("json", "application/json")
}

// ================================================================
// MemoryLimitsEffect — 副作用（MVI Effect）
// ================================================================
/**
 * ============================================================
 * MemoryLimitsEffect — 内存限制工具副作用（MVI Effect）
 * ================================================================
 * One-time side effects emitted via Channel.
 */
sealed class MemoryLimitsEffect {
    /**
     * 扫描完成事件
     * Scan completed event
     *
     * @property assessments 各 RAM 分层风险评估结果
     * @property durationMs 扫描耗时
     */
    data class ScanCompleted(
        val assessments: List<RiskAssessment>,
        val durationMs: Long
    ) : MemoryLimitsEffect()

    /**
     * 终端彩色输出事件
     * Terminal colored output event
     *
     * @property message 输出消息
     * @property riskLevel 消息风险等级（用于着色）
     */
    data class TerminalOutput(
        val message: String,
        val riskLevel: RiskLevel = RiskLevel.UNKNOWN
    ) : MemoryLimitsEffect()

    /**
     * 报告生成完成事件
     * Report generated event
     *
     * @property filePath 报告文件路径
     * @property format 报告格式
     */
    data class ReportGenerated(
        val filePath: String,
        val format: ReportFormat
    ) : MemoryLimitsEffect()

    /**
     * 诊断工作流完成事件
     * Diagnostic workflow completed event
     *
     * @property reportPath 诊断报告路径
     */
    data class DiagnosticCompleted(
        val reportPath: String
    ) : MemoryLimitsEffect()

    /**
     * 错误事件
     * Error event
     *
     * @property message 错误消息
     * @property throwable 原始异常（可选）
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : MemoryLimitsEffect()

    /**
     * 导航事件
     * Navigation event
     *
     * @property route 目标路由
     */
    data class Navigate(val route: String) : MemoryLimitsEffect()
}

// ================================================================
// MemoryLimitsState — 主状态（MVI State）
// ================================================================
/**
 * ============================================================
 * MemoryLimitsState — 内存限制工具主状态（MVI State）
 * ================================================================
 * Main state for the Memory Limits toolkit.
 *
 * @property scanState Current scan state
 * @property riskAssessments Per-tier risk assessments
 * @property selectedTier Currently selected device RAM tier
 * @property budgets Calculated memory budgets per tier
 * @property selectedAppType Selected app type for budget calculation
 * @property isDiagnosticRunning Whether diagnostic workflow is running
 * @property diagnosticReportPath Path to generated diagnostic report
 */
data class MemoryLimitsState(
    val scanState: MemoryScanState = MemoryScanState(),
    val riskAssessments: List<RiskAssessment> = emptyList(),
    val selectedTier: DeviceRamTier = DeviceRamTier.TIER_4GB,
    val budgets: Map<DeviceRamTier, MemoryBudget> = emptyMap(),
    val selectedAppType: AppType = AppType.GENERAL,
    val isDiagnosticRunning: Boolean = false,
    val diagnosticReportPath: String? = null
) {
    /**
     * 最高风险等级
     * Highest risk level across all tiers
     */
    val overallRiskLevel: RiskLevel
        get() = riskAssessments.maxByOrNull { it.riskLevel.priority }?.riskLevel ?: RiskLevel.UNKNOWN

    /**
     * 总体风险描述
     * Overall risk description
     */
    val overallRiskDescription: String
        get() = when (overallRiskLevel) {
            RiskLevel.CRITICAL -> "App 在多个设备分层上面临极高风险，必须立即优化"
            RiskLevel.HIGH -> "App 在多个设备分层上面临高风险，需要尽快优化"
            RiskLevel.MEDIUM -> "App 在部分设备分层上存在中风险，建议优化"
            RiskLevel.LOW -> "App 在所有设备分层上风险较低"
            RiskLevel.UNKNOWN -> "尚未进行风险评估"
        }
}

// ================================================================
// DiagnosticWorkflowStep — 诊断工作流步骤
// ================================================================
/**
 * ============================================================
 * DiagnosticWorkflowStep — 诊断工作流步骤
 * ================================================================
 * Represents a step in the memory diagnostic workflow.
 *
 * @property stepNumber Step number (1-based)
 * @property title Step title
 * @property description Step description
 * @property status Current status of this step
 * @property outputFilePath Output file path if this step generates a file
 */
data class DiagnosticWorkflowStep(
    val stepNumber: Int,
    val title: String,
    val titleEn: String,
    val description: String,
    val status: DiagnosticStepStatus,
    val outputFilePath: String? = null
)

/**
 * ============================================================
 * DiagnosticStepStatus — 诊断步骤状态
 * ================================================================
 */
enum class DiagnosticStepStatus {
    PENDING,    // 等待执行
    RUNNING,    // 执行中
    COMPLETED,  // 完成
    FAILED,     // 失败
    SKIPPED     // 跳过
}

// ================================================================
// DiagnosticState — 诊断工作流状态
// ================================================================
/**
 * ============================================================
 * DiagnosticState — 诊断工作流状态（MVI State）
 * ================================================================
 * State for the memory diagnostic workflow.
 *
 * @property steps Workflow steps
 * @property currentStepIndex Current step index
 * @property isRunning Whether workflow is running
 * @property packageName Target package name
 * @property reportPath Final report path
 */
data class DiagnosticState(
    val steps: List<DiagnosticWorkflowStep> = listOf(
        DiagnosticWorkflowStep(1, "检测", "Detection", "检测 App 是否被 MemoryLimiter 杀死", DiagnosticStepStatus.PENDING),
        DiagnosticWorkflowStep(2, "Heap Dump", "Heap Dump", "在触发条件满足时收集堆转储", DiagnosticStepStatus.PENDING),
        DiagnosticWorkflowStep(3, "分析", "Analysis", "分析 heap dump 识别内存问题", DiagnosticStepStatus.PENDING),
        DiagnosticWorkflowStep(4, "修复", "Fix", "提供修复建议并指导实施", DiagnosticStepStatus.PENDING),
        DiagnosticWorkflowStep(5, "验证", "Verification", "验证修复效果", DiagnosticStepStatus.PENDING)
    ),
    val currentStepIndex: Int = 0,
    val isRunning: Boolean = false,
    val packageName: String = "",
    val reportPath: String? = null
) {
    /**
     * 当前步骤
     * Current workflow step
     */
    val currentStep: DiagnosticWorkflowStep?
        get() = steps.getOrNull(currentStepIndex)

    /**
     * 完成进度（%）
     * Completion progress percentage
     */
    val progressPercent: Int
        get() = ((currentStepIndex.toFloat() / steps.size) * 100).toInt()

    /**
     * 是否全部完成
     * Whether all steps are completed
     */
    val isCompleted: Boolean
        get() = steps.all { it.status == DiagnosticStepStatus.COMPLETED || it.status == DiagnosticStepStatus.SKIPPED }
}
