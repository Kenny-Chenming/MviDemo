package com.mvi.kenny.feature.otpdelay

// ================================================================
// OtpDelayContract — Android 17 SMS OTP Delay 合规检测与迁移工具包 MVI Contract
// ================================================================
// MVI architecture contract for Android 17 SMS OTP Delay compliance toolkit.
//
// PRD-186: Android 17 SMS OTP Delay 合规检测与迁移工具包
// Design Reference: memory/agency/designs/PRD-186-SMS-OTP-Delay-合规检测与迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// This toolkit provides 8 developer tools:
//   1. OTP Delay Impact Scanner (Gradle Task)
//   2. SMS Retriever Integration Template
//   3. SMS User Consent Integration Template
//   4. OTP Delay CI Compliance Detection Tool (Gradle Plugin)
//   5. OTP Delay Fallback Strategy Template
//   6. SMS Retriever Hash String Generator Tool (CLI)
//   7. OTP Delay Scenario Simulator
//   8. OTP Delay × Privacy Compliance Report
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * OtpSeverity — OTP 影响等级枚举
 * ================================================================
 * Represents the severity level of an OTP-related API usage.
 * HIGH: Direct OTP reading without Retriever API — most critical
 * MEDIUM: SMS permission declared but usage unclear
 * LOW: Legacy code with no active OTP flow
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color Terminal output color
 * @param priority Processing priority (1=highest)
 */
enum class OtpSeverity(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val priority: Int
) {
    HIGH("高风险", "🔴", Color(0xFFF85149), 1),
    MEDIUM("中风险", "🟡", Color(0xFFFFD700), 2),
    LOW("低风险", "🟢", Color(0xFF3FB950), 3),
    INFO("信息", "ℹ️", Color(0xFF58A6FF), 4)
}

/**
 * ============================================================
 * OtpFinding — OTP 扫描发现问题
 * ================================================================
 * Represents a single finding from the OTP Delay impact scan.
 *
 * @property file File path of the finding
 * @property line Line number in the file
 * @property severity Risk severity level
 * @property api API or pattern detected
 * @property description Human-readable description
 * @property suggestion Fix suggestion
 */
data class OtpFinding(
    val file: String,
    val line: Int,
    val severity: OtpSeverity,
    val api: String,
    val description: String,
    val suggestion: String
)

/**
 * ============================================================
 * ScanPhase — 扫描阶段
 * ================================================================
 * Represents the current phase of the OTP impact scan.
 */
enum class ScanPhase {
    IDLE,           // 初始空闲状态
    SCANNING_KOTLIN, // 扫描 Kotlin 源文件
    SCANNING_JAVA,   // 扫描 Java 源文件
    ANALYZING,       // 分析结果
    GENERATING_REPORT, // 生成报告
    COMPLETED,       // 扫描完成
    ERROR            // 扫描出错
}

/**
 * ============================================================
 * OtpScanState — OTP 扫描器状态（MVI State）
 * ================================================================
 * Immutable state representing the OTP impact scanner's current status.
 *
 * @property phase Current scan phase
 * @property progress Overall progress percentage (0-100)
 * @property findings List of detected OTP findings
 * @property scannedFilesCount Number of files scanned
 * @property errorMessage Error message if phase is ERROR
 * @property startTime Scan start timestamp
 * @property endTime Scan end timestamp
 */
data class OtpScanState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val findings: List<OtpFinding> = emptyList(),
    val scannedFilesCount: Int = 0,
    val errorMessage: String? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L
) {
    /**
     * 是否正在扫描中
     */
    val isScanning: Boolean
        get() = phase != ScanPhase.IDLE &&
                phase != ScanPhase.COMPLETED &&
                phase != ScanPhase.ERROR

    /**
     * 扫描总耗时（毫秒）
     */
    val durationMs: Long
        get() = if (endTime > 0 && startTime > 0) endTime - startTime else 0L

    /**
     * 按严重等级分组的发现
     */
    val findingsBySeverity: Map<OtpSeverity, List<OtpFinding>>
        get() = findings.groupBy { it.severity }

    /**
     * HIGH 风险发现数量
     */
    val highSeverityCount: Int
        get() = findings.count { it.severity == OtpSeverity.HIGH }

    /**
     * MEDIUM 风险发现数量
     */
    val mediumSeverityCount: Int
        get() = findings.count { it.severity == OtpSeverity.MEDIUM }

    /**
     * LOW 风险发现数量
     */
    val lowSeverityCount: Int
        get() = findings.count { it.severity == OtpSeverity.LOW }
}

/**
 * ============================================================
 * OtpScanIntent — OTP 扫描器用户意图（MVI Intent）
 * ================================================================
 * User intentions that the ViewModel processes.
 */
sealed class OtpScanIntent {
    /**
     * 开始扫描指定目录
     * @property sourceDir 项目源码目录
     * @property packageName 应用包名
     */
    data class StartScan(
        val sourceDir: String,
        val packageName: String
    ) : OtpScanIntent()

    /**
     * 取消正在进行的扫描
     */
    data object CancelScan : OtpScanIntent()

    /**
     * 清除扫描结果
     */
    data object ClearResults : OtpScanIntent()

    /**
     * 导出报告
     * @property format 报告格式（html/markdown/json）
     * @property outputPath 输出文件路径
     */
    data class ExportReport(
        val format: ReportFormat,
        val outputPath: String
    ) : OtpScanIntent()
}

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

/**
 * ============================================================
 * OtpScanEffect — OTP 扫描器副作用（MVI Effect）
 * ================================================================
 * One-time side effects emitted via Channel.
 */
sealed class OtpScanEffect {
    /**
     * 扫描完成事件
     * @property results 扫描结果摘要
     */
    data class ScanCompleted(
        val totalFindings: Int,
        val highSeverity: Int,
        val mediumSeverity: Int,
        val lowSeverity: Int,
        val durationMs: Long
    ) : OtpScanEffect()

    /**
     * 终端彩色输出事件
     * @property message 输出消息
     * @property severity 消息严重等级（用于着色）
     */
    data class TerminalOutput(
        val message: String,
        val severity: OtpSeverity = OtpSeverity.INFO
    ) : OtpScanEffect()

    /**
     * 报告生成完成事件
     * @property filePath 报告文件路径
     * @property format 报告格式
     */
    data class ReportGenerated(
        val filePath: String,
        val format: ReportFormat
    ) : OtpScanEffect()

    /**
     * 错误事件
     * @property message 错误消息
     * @property throwable 原始异常（可选）
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : OtpScanEffect()

    /**
     * 导航事件
     * @property route 目标路由
     */
    data class Navigate(val route: String) : OtpScanEffect()
}

// ================================================================
// Compliance Check State — CI 合规检测状态
// ================================================================

/**
 * ============================================================
 * ComplianceLevel — 合规等级
 * ================================================================
 */
enum class ComplianceLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    COMPLIANT("合规", "✅", Color(0xFF3FB950)),
    NON_COMPLIANT("不合规", "❌", Color(0xFFF85149)),
    WARNING("警告", "⚠️", Color(0xFFFFD700)),
    NOT_CHECKED("未检测", "❓", Color(0xFF8B949E))
}

/**
 * ============================================================
 * ComplianceIssue — CI 合规问题
 * ================================================================
 */
data class ComplianceIssue(
    val file: String,
    val line: Int,
    val ruleId: String,
    val description: String,
    val fixSuggestion: String,
    val level: ComplianceLevel
)

/**
 * ============================================================
 * OtpComplianceState — CI 合规检测状态（MVI State）
 * ================================================================
 */
data class OtpComplianceState(
    val isChecking: Boolean = false,
    val complianceLevel: ComplianceLevel = ComplianceLevel.NOT_CHECKED,
    val issues: List<ComplianceIssue> = emptyList(),
    val lastCheckTime: Long = 0L,
    val strictMode: Boolean = true,  // strict=true 时 fail build
    val reportPath: String? = null
)

// ================================================================
// Hash Generator State — Hash 生成器状态
// ================================================================

/**
 * ============================================================
 * HashGeneratorState — Hash 生成器状态（MVI State）
 * ================================================================
 */
data class HashGeneratorState(
    val packageName: String = "",
    val keystorePath: String = "",
    val keyAlias: String = "",
    val isGenerating: Boolean = false,
    val generatedHash: String? = null,
    val error: String? = null
)

/**
 * ============================================================
 * HashGeneratorIntent — Hash 生成器用户意图
 * ================================================================
 */
sealed class HashGeneratorIntent {
    data class UpdatePackageName(val packageName: String) : HashGeneratorIntent()
    data class UpdateKeystorePath(val keystorePath: String) : HashGeneratorIntent()
    data class UpdateKeyAlias(val keyAlias: String) : HashGeneratorIntent()
    data object GenerateHash : HashGeneratorIntent()
    data object Clear : HashGeneratorIntent()
}

// ================================================================
// Fallback Strategy State — 降级策略状态
// ================================================================

/**
 * ============================================================
 * FallbackStrategy — 降级策略类型
 * ================================================================
 */
enum class FallbackStrategy(
    val displayName: String,
    val displayNameEn: String,
    val description: String,
    val userExperience: String
) {
    SMS_RETRIEVER(
        "SMS Retriever API（推荐）",
        "SMS Retriever API (Recommended)",
        "Google 官方推荐的 OTP 读取方案，用户无感知",
        "用户收到 OTP 后自动读取，无需任何操作"
    ),
    USER_CONSENT(
        "SMS User Consent API",
        "SMS User Consent API",
        "用户点击确认按钮后读取 OTP",
        "用户收到 OTP 后弹出确认对话框，点击确认后读取"
    ),
    DEEP_LINK(
        "深度链接重发",
        "Deep Link Resend",
        "通过应用内链接或推送接收 OTP",
        "点击链接或在 App 内打开，OTP 直接显示"
    ),
    BACKUP_CODE(
        "备用码方案",
        "Backup Code",
        "预先生成备用码作为二次验证",
        "用户输入预先设置的备用码完成验证"
    )
}

/**
 * ============================================================
 * OtpDelayPolicy — OTP 延迟策略
 * ================================================================
 */
data class OtpDelayPolicy(
    val isEnabled: Boolean = true,
    val delayHours: Int = 3,
    val exemptApps: List<String> = emptyList(),  // 默认 SMS App 豁免
    val recommendedStrategy: FallbackStrategy = FallbackStrategy.SMS_RETRIEVER
)
