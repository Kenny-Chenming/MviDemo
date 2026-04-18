package com.mvi.kenny.feature.pqcsecurity

/**
 * ============================================================
 * PRD-120 | Android 17 Post-Quantum Cryptography App Signing
 * Migration Detection & Quantum Security Toolkit
 * ============================================================
 * MVI Contract — Defines State, Intent, and Effect
 *
 * Design Doc: memory/agency/designs/PRD-120-Android-17-Post-Quantum-Cryptography-应用签名迁移检测与量子安全工具包.md
 * Status: 设计完成，待移交开发 | Completed: 2026-04-17
 *
 * This toolkit helps Android developers:
 * 1. Analyze current APK/AAB signing algorithms
 * 2. Understand NIST PQC standards (ML-KEM/LMS/XMSS)
 * 3. Configure quantum-safe keys in Google Play App Signing
 * 4. Generate PQC compliance reports
 * 5. Detect device Keystore PQC capabilities
 * 6. Track migration progress via checklist
 *
 * 本工具包帮助 Android 开发者：
 * 1. 分析当前 APK/AAB 签名算法
 * 2. 理解 NIST PQC 标准（ML-KEM/LMS/XMSS）
 * 3. 在 Google Play App Signing 中配置量子安全密钥
 * 4. 生成 PQC 合规报告
 * 5. 检测设备 Keystore PQC 能力
 * 6. 通过清单追踪迁移进度
 */

// ============ Data Models ============
// ============ 数据模型 ============

/**
 * Overall scan status for APK signature analysis
 * APK 签名分析的整体扫描状态
 */
enum class ScanStatus {
    IDLE,       // Not started / 未开始
    SCANNING,   // In progress / 进行中
    SUCCESS,    // Completed successfully / 成功完成
    ERROR       // Failed with error / 失败
}

/**
 * Signature algorithm strength classification
 * 签名算法强度分类
 * - QUANTUM_SAFE: Algorithms resistant to quantum computer attacks (ML-KEM/LMS)
 * - TRANSITIONAL: ECDSA P-384 etc, quantum-vulnerable but widely used
 * - LEGACY: ECDSA P-256, V1/V2 signatures — quantum-vulnerable
 * - UNKNOWN: Cannot determine algorithm / 无法确定算法
 */
enum class SignatureStrength {
    QUANTUM_SAFE,  // 量子安全（ML-KEM/LMS）
    TRANSITIONAL,  // 过渡期（ECDSA P-384）
    LEGACY,        // 遗留（ECDSA P-256/V1/V2）
    UNKNOWN         // 未知
}

/**
 * APK/AAB signature scheme version
 * APK/AAB 签名方案版本
 */
enum class SignatureScheme(val label: String, val description: String) {
    JAR_SIG("JAR (V1)", "Original JAR signature scheme / 原始 JAR 签名方案"),
    V2("APK Signature Scheme V2", "V2 full-file signature / V2 整文件签名"),
    V3("APK Signature Scheme V3", "V3 with rotation support / V3 支持密钥轮换"),
    V4("APK Signature Scheme V4", "V4 merkle tree signature / V4 默克尔树签名"),
    UNKNOWN("Unknown", "Unable to determine / 无法确定")
}

/**
 * Risk level for a specific signature
 * 特定签名的风险级别
 */
enum class RiskLevel(val label: String, val labelZh: String) {
    HIGH("HIGH Risk", "高风险"),
    MEDIUM("MEDIUM Risk", "中等风险"),
    LOW("LOW Risk", "低风险"),
    NONE("No Risk", "无风险")
}

/**
 * APK signature information extracted from scan
 * 从扫描中提取的 APK 签名信息
 */
data class ApkSignatureInfo(
    val fileName: String,                    // File name / 文件名
    val filePath: String,                    // File path / 文件路径
    val fileSize: Long,                      // File size in bytes / 文件大小（字节）
    val packageName: String?,               // App package name / 应用包名
    val versionName: String?,               // App version / 应用版本
    val versionCode: Long?,                 // App version code / 版本号
    val signatureSchemes: List<SignatureScheme>, // Supported schemes / 支持的签名方案
    val algorithms: List<SignatureAlgorithm>,   // Signature algorithms / 签名算法
    val overallStrength: SignatureStrength,     // Overall strength / 整体强度
    val riskLevel: RiskLevel,                  // Risk assessment / 风险评估
    val quantumSafe: Boolean,                  // Is quantum-safe / 是否量子安全
    val migrationUrgency: MigrationUrgency      // Migration urgency / 迁移紧迫度
)

/**
 * Individual signature algorithm used in APK
 * APK 中使用的单个签名算法
 */
data class SignatureAlgorithm(
    val name: String,             // Algorithm name / 算法名称
    val keySize: Int?,           // Key size in bits / 密钥位数
    val scheme: SignatureScheme,  // Which scheme uses this / 所属签名方案
    val strength: SignatureStrength, // Strength classification / 强度分类
    val quantumResistant: Boolean  // Is quantum resistant / 是否抗量子
)

/**
 * Migration urgency level
 * 迁移紧迫度级别
 */
enum class MigrationUrgency(val label: String, val labelZh: String, val score: Int) {
    CRITICAL("CRITICAL", "紧急", 4),  // Immediate action required / 需立即行动
    HIGH("HIGH", "高", 3),            // Migrate soon / 尽快迁移
    MEDIUM("MEDIUM", "中", 2),        // Plan for migration / 计划迁移
    LOW("LOW", "低", 1),             // Monitor / 关注
    NONE("NONE", "无", 0)             // Already quantum-safe / 已量子安全
}

/**
 * NIST PQC Standard Algorithm
 * NIST 后量子密码学标准算法
 */
data class PQCAlgorithm(
    val id: String,
    val name: String,             // Display name / 显示名称
    val fullName: String,         // Full name / 全称
    val type: PQCType,           // Algorithm type / 算法类型
    val nistLevel: Int,           // NIST security level (1-5) / NIST 安全级别
    val keySize: String,          // Typical key size / 典型密钥大小
    val description: String,      // Brief description / 简要描述
    val androidSupport: AndroidPQCSupport, // Android support status / Android 支持状态
    val useCases: List<String>,    // Use case descriptions / 用例描述
    val pros: List<String>,       // Advantages / 优点
    val cons: List<String>        // Disadvantages / 缺点
)

/**
 * PQC algorithm type
 * PQC 算法类型
 */
enum class PQCType(val label: String) {
    KEY_ENCAPSULATION("Key Encapsulation / 密钥封装"),
    DIGITAL_SIGNATURE("Digital Signature / 数字签名"),
    HASH_BASED("Hash-Based / 基于哈希")
}

/**
 * Android PQC support status
 * Android PQC 支持状态
 */
enum class AndroidPQCSupport(val label: String, val labelZh: String) {
    ANDROID_17_PLUS("Android 17+", "Android 17+"),
    ANDROID_14_PLUS("Android 14+", "Android 14+"),
    PLANNED("Planned", "计划中"),
    NOT_SUPPORTED("Not Supported", "不支持")
}

/**
 * Keystore PQC capability of a device
 * 设备的 Keystore PQC 能力
 */
data class KeystoreCapability(
    val deviceModel: String,           // Device model / 设备型号
    val androidVersion: String,        // Android version / Android 版本
    val apiLevel: Int,                 // API level / API 级别
    val supportedAlgorithms: List<String>, // Supported algorithms / 支持的算法
    val isQuantumSafe: Boolean,         // Has any quantum-safe algorithm / 是否有量子安全算法
    val keymasterVersion: String,       // Keymaster version / Keymaster 版本
    val securityLevel: String           // Security level / 安全级别
)

/**
 * Migration step in the checklist
 * 清单中的迁移步骤
 */
data class MigrationStep(
    val id: String,
    val stepNumber: Int,              // Step number (1-5) / 步骤编号
    val title: String,                // Step title / 步骤标题
    val description: String,           // Detailed description / 详细描述
    val status: MigrationStepStatus,  // Current status / 当前状态
    val isBlocking: Boolean,          // Is this a blocking step / 是否为阻塞步骤
    val estimatedTime: String,        // Estimated time / 预计时间
    val referenceLink: String?        // Reference documentation / 参考文档
)

/**
 * Migration step completion status
 * 迁移步骤完成状态
 */
enum class MigrationStepStatus(val label: String, val labelZh: String) {
    PENDING("Pending", "待处理"),
    IN_PROGRESS("In Progress", "进行中"),
    COMPLETED("Completed", "已完成"),
    BLOCKED("Blocked", "阻塞中")
}

/**
 * Radar chart data for dashboard
 * 仪表盘雷达图数据
 */
data class RadarChartData(
    val signatureStrength: Int,     // 0-100 / 签名强度
    val algorithmType: Int,          // 0-100 / 算法类型
    val keyLength: Int,             // 0-100 / 密钥长度
    val keystoreSupport: Int,       // 0-100 / Keystore 支持
    val playSigningCompliance: Int, // 0-100 / Play 签名合规
    val migrationReadiness: Int      // 0-100 / 迁移就绪度
)

// Report format options
// 报告格式选项
enum class ReportFormat(val label: String) {
    PDF("PDF Report"),
    JSON("JSON Data")
}

// Config wizard step
// 配置向导步骤
enum class ConfigWizardStep(val step: Int, val title: String, val titleZh: String) {
    ASSESS_CURRENT(0, "Assess Current", "评估当前签名"),
    SELECT_ALGORITHM(1, "Select Algorithm", "选择量子安全算法"),
    GENERATE_KEYS(2, "Generate Keys", "在 Play Console 生成新密钥"),
    CONFIGURE_DUAL_SIGNING(3, "Configure Dual Signing", "配置双重签名"),
    VERIFY_MIGRATION(4, "Verify Migration", "验证迁移")
}

// Tab enum for the 7-tab interface
// 7标签页界面的 Tab 枚举
enum class PQCTab(val label: String, val labelZh: String, val icon: String) {
    DASHBOARD("Dashboard", "安全仪表盘", "dashboard"),
    SCANNER("Scanner", "签名检测器", "search"),
    KNOWLEDGE("Knowledge", "PQC知识库", "school"),
    CONFIG_WIZARD("Config", "配置引导", "build"),
    REPORT("Report", "合规报告", "description"),
    DEVICE("Device", "设备检测", "phone_android"),
    CHECKLIST("Checklist", "迁移清单", "checklist")
}

// ============ State ============
// ============ 状态 ============

/**
 * MVI State for PQC Security Toolkit
 * PQC 安全工具包的 MVI 状态
 */
data class PQCSecurityState(
    // Scan state / 扫描状态
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,                    // 0f ~ 1f
    val scanError: String? = null,

    // APK analysis results / APK 分析结果
    val scannedApks: List<ApkSignatureInfo> = emptyList(),
    val selectedApk: ApkSignatureInfo? = null,

    // Dashboard metrics / 仪表盘指标
    val overallHealthScore: Int = 0,                  // 0-100 overall health score / 0-100 整体健康分
    val radarChartData: RadarChartData = RadarChartData(
        signatureStrength = 0,
        algorithmType = 0,
        keyLength = 0,
        keystoreSupport = 0,
        playSigningCompliance = 0,
        migrationReadiness = 0
    ),
    val totalApksScanned: Int = 0,
    val quantumSafeCount: Int = 0,
    val atRiskCount: Int = 0,

    // PQC Knowledge base / PQC 知识库
    val pqcAlgorithms: List<PQCAlgorithm> = emptyList(),
    val selectedAlgorithm: PQCAlgorithm? = null,

    // Config wizard / 配置向导
    val configWizardStep: ConfigWizardStep = ConfigWizardStep.ASSESS_CURRENT,
    val selectedPQCForMigration: PQCAlgorithm? = null,

    // Compliance report / 合规报告
    val reportGenerating: Boolean = false,
    val reportGenerated: Boolean = false,
    val reportPath: String? = null,
    val selectedReportFormat: ReportFormat = ReportFormat.PDF,

    // Device detection / 设备检测
    val deviceCapabilities: List<KeystoreCapability> = emptyList(),
    val isDetectingDevices: Boolean = false,

    // Migration checklist / 迁移清单
    val migrationSteps: List<MigrationStep> = emptyList(),
    val migrationProgress: Float = 0f,                 // 0f ~ 1f

    // UI state / UI 状态
    val selectedTab: PQCTab = PQCTab.DASHBOARD,
    val errorMessage: String? = null
) : MviState

// Marker interface for MVI State
// MVI State 的标记接口
interface MviState

// ============ Intent ============
// ============ 用户意图 ============

/**
 * MVI Intent for user actions
 * MVI 用户意图（用户动作）
 */
sealed class PQCIntent : MviIntent {

    // Tab navigation / 标签页导航
    data class SelectTab(val tab: PQCTab) : PQCIntent()

    // APK Scanning / APK 扫描
    data class ScanAPK(val uri: android.net.Uri) : PQCIntent()
    data class SelectApkResult(val apk: ApkSignatureInfo?) : PQCIntent()
    object ClearScanResults : PQCIntent()

    // PQC Knowledge / PQC 知识库
    data class SelectAlgorithm(val algorithm: PQCAlgorithm?) : PQCIntent()

    // Config Wizard / 配置向导
    object NextWizardStep : PQCIntent()
    object PrevWizardStep : PQCIntent()
    data class SelectPQCForMigration(val algorithm: PQCAlgorithm) : PQCIntent()

    // Compliance Report / 合规报告
    data class GenerateReport(val format: ReportFormat) : PQCIntent()
    data class SelectReportFormat(val format: ReportFormat) : PQCIntent()

    // Device Detection / 设备检测
    object DetectDeviceCapabilities : PQCIntent()

    // Migration Checklist / 迁移清单
    data class ToggleMigrationStep(val stepId: String) : PQCIntent()
    object ResetChecklist : PQCIntent()
}

// Marker interface for MVI Intent
// MVI Intent 的标记接口
interface MviIntent

// ============ Effect ============
// ============ 副作用 ============

/**
 * MVI Effect — one-time side effects
 * MVI 副作用 — 一次性副作用
 */
sealed class PQCEffect : MviEffect {

    // Show snackbar message / 显示提示消息
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : PQCEffect()

    // Report generation complete / 报告生成完成
    data class ReportGenerated(val filePath: String, val format: ReportFormat) : PQCEffect()

    // Share file via system share sheet / 通过系统分享面板分享文件
    data class ShareFile(val filePath: String) : PQCEffect()

    // Navigate to algorithm detail / 导航到算法详情
    data class NavigateToAlgorithmDetail(val algorithmId: String) : PQCEffect()

    // Navigate to config wizard step / 导航到配置向导步骤
    data class NavigateToWizardStep(val step: ConfigWizardStep) : PQCEffect()

    // Scan complete notification / 扫描完成通知
    data class ScanComplete(val apkCount: Int, val quantumSafeCount: Int) : PQCEffect()

    // Generic error / 通用错误
    data class ShowError(val message: String) : PQCEffect()
}

// Marker interface for MVI Effect
// MVI Effect 的标记接口
interface MviEffect
