package com.mvi.kenny.feature.pqcmigration

// ============================================================
// PQCMigrationContract — Android 17 PQC 迁移工具包 MVI 契约
// PRD-165 | Android 17 Post-Quantum Cryptography 迁移工具包
// ============================================================
/**
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * MVI 三要素：
 * - Model (State): 页面状态的唯一真相来源，Immutable 数据类
 * - View: Composable 函数，消费 State，渲染 UI
 * - Intent: 用户意图（用户操作），ViewModel 收到 Intent 后执行业务逻辑
 *
 * Effect: 一次性副作用（导航、Toast），通过 Channel 传递
 *
 * @see PQCMigrationViewModel
 * @see PQCMigrationScreen
 */

// ============================================================
// 模块信息 / Module Info
// ============================================================
/**
 * PQC 工具模块枚举，代表 PQC Migration Toolkit 中的各个工具模块。
 * PQC tool module enumeration for the Android 17 PQC Migration Toolkit.
 *
 * @param titleCn 中文标题
 * @param titleEn 英文标题
 * @param description 模块描述
 * @param iconEmoji 模块图标 emoji
 */
enum class PQCToolModule(
    val titleCn: String,
    val titleEn: String,
    val description: String,
    val iconEmoji: String
) {
    Overview(
        titleCn = "首页概览",
        titleEn = "Overview",
        description = "PQC 迁移状态总览与倒计时",
        iconEmoji = "🛡️"
    ),
    KeyGen(
        titleCn = "PQ 密钥生成",
        titleEn = "Keystore KeyGen",
        description = "Android Keystore ML-KEM 密钥生成指南与代码生成",
        iconEmoji = "🗝️"
    ),
    KeyMigration(
        titleCn = "密钥迁移扫描",
        titleEn = "Key Migration Scanner",
        description = "检测 App 中 RSA/EC 密钥用法，输出迁移 Diff",
        iconEmoji = "🔄"
    ),
    TLSCompliance(
        titleCn = "TLS 合规检测",
        titleEn = "TLS Hybrid Mode",
        description = "检测 TLS 配置是否启用 hybrid PQ 模式",
        iconEmoji = "🔒"
    ),
    AppSigning(
        titleCn = "App Signing PQ",
        titleEn = "App Signing PQ",
        description = "验证 App 签名是否满足 Google Play PQ 要求",
        iconEmoji = "📜"
    )
}

/**
 * PQ 算法枚举 / Post-Quantum Algorithm enumeration
 *
 * @param displayName 显示名称
 * @param apiLevelMin 最低支持 API 级别
 * @param description 算法描述
 */
enum class PqcAlgorithm(
    val displayName: String,
    val apiLevelMin: Int,
    val description: String
) {
    ML_KEM_768(
        displayName = "ML-KEM-768",
        apiLevelMin = 38,
        description = "NIST 标准，PQC 主流算法，768-bit 安全性"
    ),
    ML_KEM_1024(
        displayName = "ML-KEM-1024",
        apiLevelMin = 38,
        description = "NIST 标准，更高安全级别，1024-bit"
    ),
    KYBER_512(
        displayName = "Kyber-512",
        apiLevelMin = 38,
        description = "NIST PQC 初始标准，ML-KEM 前身，512-bit"
    ),
    HYBRID_RSA_ML_KEM(
        displayName = "Hybrid RSA + ML-KEM",
        apiLevelMin = 38,
        description = "混合模式，传统 RSA 与 ML-KEM 并行，双重安全"
    ),
    HYBRID_EC_ML_KEM(
        displayName = "Hybrid EC + ML-KEM",
        apiLevelMin = 38,
        description = "混合模式，椭圆曲线与 ML-KEM 并行"
    )
}

/**
 * 扫描阶段枚举 / Scan phase enumeration
 *
 * @param displayNameCn 中文显示名称
 * @param progress 阶段进度（0.0 - 1.0）
 */
enum class ScanPhase(
    val displayNameCn: String,
    val progress: Float
) {
    IDLE("空闲", 0f),
    DETECTING("检测密钥用法", 0.25f),
    ANALYZING("分析兼容性", 0.5f),
    GENERATING_DIFF("生成迁移 Diff", 0.75f),
    COMPLETE("完成", 1.0f)
}

/**
 * TLS 合规等级 / TLS Compliance level
 */
enum class TLSComplianceLevel(
    val label: String,
    val score: Int,
    val color: Long  // ARGB color
) {
    COMPLIANT("完全合规", 100, 0xFF4ADE80),
    PARTIAL("部分合规", 60, 0xFFFBBF24),
    NON_COMPLIANT("不合规", 20, 0xFFFF6B6B),
    UNKNOWN("未检测", 0, 0xFF6B7280)
}

// ============================================================
// State — 页面状态 / Page State
// ============================================================

/**
 * 主页面状态（Overview State）
 * Main page state for the PQC Migration Toolkit dashboard.
 *
 * @param selectedTab 当前选中的 Tab / Currently selected tab
 * @param countdownDays 距 2029 年 PQC 截止天数 / Days until 2029 PQC deadline
 * @param toolSummaries 各工具合规状态摘要 / Compliance summary for each tool
 * @param announcement 最新公告 / Latest announcement
 * @param isLoading 加载状态 / Loading state
 *
 * @see PQCToolModule
 * @see ToolComplianceSummary
 */
data class PQCMigrationState(
    val selectedTab: PQCToolModule = PQCToolModule.Overview,
    val countdownDays: Int = 1095,  // Default ~3 years from now to 2029
    val toolSummaries: List<ToolComplianceSummary> = emptyList(),
    val announcement: String? = "Android 17 PQC 迁移窗口已开启，Google 将 PQ 截止日期提前至 2029 年",
    val isLoading: Boolean = false
) {
    companion object {
        /** 初始状态 / Initial state */
        val Initial = PQCMigrationState()
    }
}

/**
 * 工具合规摘要 / Tool compliance summary
 *
 * @param module 工具模块 / Tool module
 * @param complianceScore 合规分数（null = 未检测）/ Compliance score (null = not scanned)
 * @param lastScanTime 最后扫描时间戳 / Last scan timestamp
 * @param status 状态标签 / Status label
 */
data class ToolComplianceSummary(
    val module: PQCToolModule,
    val complianceScore: Int?,
    val lastScanTime: Long?,
    val status: String  // "未检测" / "合规" / "不合规" / "部分合规"
)

// —————————————————————————————————————————————————————
// KeyGen State — PQ 密钥生成状态
// —————————————————————————————————————————————————————

/**
 * KeyGen 标签页状态 / KeyGen tab state
 *
 * @param selectedAlgorithm 选中的 PQ 算法 / Selected PQC algorithm
 * @param apiLevel API 级别 / API level
 * @param generatedCode 生成的代码 / Generated code
 * @param isGenerating 是否正在生成 / Whether generating
 * @param copiedToClipboard 是否已复制到剪贴板 / Whether copied to clipboard
 */
data class KeyGenState(
    val selectedAlgorithm: PqcAlgorithm = PqcAlgorithm.ML_KEM_768,
    val apiLevel: Int = 38,
    val generatedCode: String? = null,
    val isGenerating: Boolean = false,
    val copiedToClipboard: Boolean = false,
    val error: String? = null
) {
    companion object {
        val Initial = KeyGenState()
    }
}

// —————————————————————————————————————————————————————
// KeyMigration State — 密钥迁移扫描状态
// —————————————————————————————————————————————————————

/**
 * 发现的非合规密钥 / Non-compliant key finding
 *
 * @param filePath 文件路径 / File path
 * @param lineNumber 行号 / Line number
 * @param keyType 密钥类型（RSA/EC）/ Key type
 * @param usageContext 使用场景 / Usage context
 * @param suggestedReplacement 建议替换方案 / Suggested replacement
 * @param severity 严重程度 / Severity (HIGH/MEDIUM/LOW)
 */
data class NonCompliantKeyFinding(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val keyType: String,  // "RSA" / "EC" / "DH" / etc.
    val usageContext: String,
    val suggestedReplacement: String,
    val severity: String  // "HIGH" / "MEDIUM" / "LOW"
)

/**
 * KeyMigration 标签页状态 / KeyMigration tab state
 *
 * @param projectPath 项目路径 / Project path
 * @param scanPhase 扫描阶段 / Scan phase
 * @param progress 扫描进度（0.0 - 1.0）/ Scan progress
 * @param findings 发现的问题列表 / List of findings
 * @param migrationDiff 生成的迁移 Diff / Generated migration diff
 * @param error 错误信息 / Error message
 * @param isScanning 是否正在扫描 / Whether scanning
 */
data class KeyMigrationState(
    val projectPath: String = "",
    val scanPhase: ScanPhase = ScanPhase.IDLE,
    val progress: Float = 0f,
    val findings: List<NonCompliantKeyFinding> = emptyList(),
    val migrationDiff: String? = null,
    val error: String? = null,
    val isScanning: Boolean = false
) {
    companion object {
        val Initial = KeyMigrationState()
    }
}

// —————————————————————————————————————————————————————
// TLSCompliance State — TLS 合规检测状态
// —————————————————————————————————————————————————————

/**
 * TLS 发现项 / TLS finding
 *
 * @param domain 域名 / Domain
 * @param issue 问题描述 / Issue description
 * @param currentConfig 当前配置 / Current configuration
 * @param suggestedFix 建议修复方案 / Suggested fix
 * @param severity 严重程度 / Severity
 */
data class TLSFinding(
    val id: String,
    val domain: String,
    val issue: String,
    val currentConfig: String,
    val suggestedFix: String,
    val severity: String  // "HIGH" / "MEDIUM" / "LOW"
)

/**
 * TLSCompliance 标签页状态 / TLSCompliance tab state
 *
 * @param inputPath 输入路径（APK 或源码路径）/ Input path
 * @param inputMode 输入模式 / Input mode (APK / SOURCE_PATH)
 * @param complianceLevel 合规等级 / Compliance level
 * @param complianceScore 合规分数 / Compliance score
 * @param findings 发现的问题列表 / List of findings
 * @param isScanning 是否正在扫描 / Whether scanning
 * @param error 错误信息 / Error message
 */
data class TLSComplianceState(
    val inputPath: String = "",
    val inputMode: String = "SOURCE_PATH",  // "APK" / "SOURCE_PATH"
    val complianceLevel: TLSComplianceLevel = TLSComplianceLevel.UNKNOWN,
    val complianceScore: Int? = null,
    val findings: List<TLSFinding> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
) {
    companion object {
        val Initial = TLSComplianceState()
    }
}

// —————————————————————————————————————————————————————
// AppSigning State — App Signing PQ 合规状态
// —————————————————————————————————————————————————————

/**
 * AppSigning 标签页状态 / AppSigning tab state
 *
 * @param apkPath APK 路径 / APK path
 * @param signatureScheme 当前签名方案 / Current signature scheme
 * @param signatureVersion 签名版本 / Signature version
 * @param isPQCCompliant 是否 PQ 合规 / Whether PQC compliant
 * @param complianceDetails 合规详情 / Compliance details
 * @param reportPath 报告路径 / Report path
 * @param isAnalyzing 是否正在分析 / Whether analyzing
 * @param error 错误信息 / Error message
 */
data class AppSigningState(
    val apkPath: String = "",
    val signatureScheme: String? = null,
    val signatureVersion: Int? = null,
    val isPQCCompliant: Boolean? = null,
    val complianceDetails: String? = null,
    val reportPath: String? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null
) {
    companion object {
        val Initial = AppSigningState()
    }
}

// ============================================================
// Intent — 用户意图 / User Intent
// ============================================================

/**
 * 全局 Intent（由 ViewModel 处理）
 * Global intent handled by ViewModel.
 *
 * @see PQCMigrationViewModel
 */
sealed class PQCMigrationIntent {
    // Tab navigation / Tab 导航
    data class SelectTab(val tab: PQCToolModule) : PQCMigrationIntent()

    // KeyGen intents / KeyGen 意图
    data class SelectAlgorithm(val algorithm: PqcAlgorithm) : PQCMigrationIntent()
    data class SetApiLevel(val level: Int) : PQCMigrationIntent()
    data object GenerateKeyGenCode : PQCMigrationIntent()
    data object CopyGeneratedCode : PQCMigrationIntent()

    // KeyMigration intents / KeyMigration 意图
    data class SetProjectPath(val path: String) : PQCMigrationIntent()
    data object StartKeyMigrationScan : PQCMigrationIntent()
    data object CancelScan : PQCMigrationIntent()
    data class SelectFinding(val finding: NonCompliantKeyFinding) : PQCMigrationIntent()
    data object ExportMigrationReport : PQCMigrationIntent()

    // TLSCompliance intents / TLSCompliance 意图
    data class SetTLSInputPath(val path: String) : PQCMigrationIntent()
    data class SetTLSInputMode(val mode: String) : PQCMigrationIntent()
    data object StartTLSScan : PQCMigrationIntent()
    data object ExportTLSReport : PQCMigrationIntent()

    // AppSigning intents / AppSigning 意图
    data class SetApkPath(val path: String) : PQCMigrationIntent()
    data object AnalyzeAppSigning : PQCMigrationIntent()
    data object ExportSigningReport : PQCMigrationIntent()

    // Overview intents / Overview 意图
    data object RefreshOverview : PQCMigrationIntent()
    data class ScanTool(val module: PQCToolModule) : PQCMigrationIntent()
}

// ============================================================
// Effect — 副作用 / Side Effects
// ============================================================

/**
 * 全局副作用（一次性事件，通过 Channel 传递）
 * Global side effects (one-time events delivered via Channel).
 *
 * Use cases:
 * - Navigation: Navigate to sub-pages
 * - Toast/Snackbar: Show feedback messages
 * - Clipboard: Copy to clipboard
 * - File: Export reports
 */
sealed class PQCMigrationEffect {
    /** 扫描完成 / Scan completed */
    data object ScanComplete : PQCMigrationEffect()

    /** 复制到剪贴板 / Copied to clipboard */
    data class CopyToClipboard(val text: String) : PQCMigrationEffect()

    /** 显示错误信息 / Show error message */
    data class ShowError(val message: String) : PQCMigrationEffect()

    /** 显示成功消息 / Show success message */
    data class ShowSuccess(val message: String) : PQCMigrationEffect()

    /** 导出报告成功 / Report exported successfully */
    data class ReportExported(val filePath: String) : PQCMigrationEffect()

    /** 导航到子页面 / Navigate to sub-page */
    data class NavigateToModule(val module: PQCToolModule) : PQCMigrationEffect()
}

// ============================================================
// ViewModel 基类 — MVI ViewModel
// ============================================================

/**
 * PQC 迁移工具包 ViewModel 基类
 * Base ViewModel for PQC Migration Toolkit following MVI pattern.
 *
 * 架构说明：
 * - _state: MutableStateFlow，ViewModel 内部写入
 * - state: StateFlow 暴露给 UI 层只读访问
 * - _effect: Channel，热流，用于一次性副作用事件
 * - effect: receiveAsFlow，UI 层通过 collect{} 监听副作用
 *
 * @see PQCMigrationState
 * @see PQCMigrationIntent
 * @see PQCMigrationEffect
 */
