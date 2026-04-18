package com.mvi.kenny.feature.pqcsecurity

import android.net.Uri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ===== MVI Marker Interfaces =====
// ===== MVI 标记接口 =====

/**
 * Marker interface for MVI State
 * MVI 状态的标记接口
 */
interface MviState

/**
 * Marker interface for MVI Intent
 * MVI 用户意图的标记接口
 */
interface MviIntent

/**
 * Marker interface for MVI Effect
 * MVI 副作用的标记接口
 */
interface MviEffect

// ===== Data Models =====
// ===== 数据模型 =====

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
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val packageName: String?,
    val versionName: String?,
    val versionCode: Long?,
    val signatureSchemes: List<SignatureScheme>,
    val algorithms: List<SignatureAlgorithm>,
    val overallStrength: SignatureStrength,
    val riskLevel: RiskLevel,
    val quantumSafe: Boolean,
    val migrationUrgency: MigrationUrgency
)

/**
 * Individual signature algorithm used in APK
 * APK 中使用的单个签名算法
 */
data class SignatureAlgorithm(
    val name: String,
    val keySize: Int?,
    val scheme: SignatureScheme,
    val strength: SignatureStrength,
    val quantumResistant: Boolean
)

/**
 * Migration urgency level
 * 迁移紧迫度级别
 */
enum class MigrationUrgency(val label: String, val labelZh: String, val score: Int) {
    CRITICAL("CRITICAL", "紧急", 4),
    HIGH("HIGH", "高", 3),
    MEDIUM("MEDIUM", "中", 2),
    LOW("LOW", "低", 1),
    NONE("NONE", "无", 0)
}

/**
 * NIST PQC Standard Algorithm
 * NIST 后量子密码学标准算法
 */
data class PQCAlgorithm(
    val id: String,
    val name: String,
    val fullName: String,
    val type: PQCType,
    val nistLevel: Int,
    val keySize: String,
    val description: String,
    val androidSupport: AndroidPQCSupport,
    val useCases: List<String>,
    val pros: List<String>,
    val cons: List<String>
)

enum class PQCType(val label: String) {
    KEY_ENCAPSULATION("Key Encapsulation / 密钥封装"),
    DIGITAL_SIGNATURE("Digital Signature / 数字签名"),
    HASH_BASED("Hash-Based / 基于哈希")
}

enum class AndroidPQCSupport(val label: String, val labelZh: String) {
    ANDROID_17_PLUS("Android 17+", "Android 17+"),
    ANDROID_14_PLUS("Android 14+", "Android 14+"),
    PLANNED("Planned", "计划中"),
    NOT_SUPPORTED("Not Supported", "不支持")
}

data class KeystoreCapability(
    val deviceModel: String,
    val androidVersion: String,
    val apiLevel: Int,
    val supportedAlgorithms: List<String>,
    val isQuantumSafe: Boolean,
    val keymasterVersion: String,
    val securityLevel: String
)

data class MigrationStep(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val status: MigrationStepStatus,
    val isBlocking: Boolean,
    val estimatedTime: String,
    val referenceLink: String?
)

enum class MigrationStepStatus(val label: String, val labelZh: String) {
    PENDING("Pending", "待处理"),
    IN_PROGRESS("In Progress", "进行中"),
    COMPLETED("Completed", "已完成"),
    BLOCKED("Blocked", "阻塞中")
}

data class RadarChartData(
    val signatureStrength: Int,
    val algorithmType: Int,
    val keyLength: Int,
    val keystoreSupport: Int,
    val playSigningCompliance: Int,
    val migrationReadiness: Int
)

enum class ReportFormat(val label: String) {
    PDF("PDF Report"),
    JSON("JSON Data")
}

enum class ConfigWizardStep(val step: Int, val title: String, val titleZh: String) {
    ASSESS_CURRENT(0, "Assess Current", "评估当前签名"),
    SELECT_ALGORITHM(1, "Select Algorithm", "选择量子安全算法"),
    GENERATE_KEYS(2, "Generate Keys", "在 Play Console 生成新密钥"),
    CONFIGURE_DUAL_SIGNING(3, "Configure Dual Signing", "配置双重签名"),
    VERIFY_MIGRATION(4, "Verify Migration", "验证迁移")
}

enum class PQCTab(val label: String, val labelZh: String, val icon: String) {
    DASHBOARD("Dashboard", "安全仪表盘", "dashboard"),
    SCANNER("Scanner", "签名检测器", "search"),
    KNOWLEDGE("Knowledge", "PQC知识库", "school"),
    CONFIG_WIZARD("Config", "配置引导", "build"),
    REPORT("Report", "合规报告", "description"),
    DEVICE("Device", "设备检测", "phone_android"),
    CHECKLIST("Checklist", "迁移清单", "checklist")
}

// ===== State =====
// ===== 状态 =====

/**
 * MVI State for PQC Security Toolkit
 * PQC 安全工具包的 MVI 状态
 */
data class PQCSecurityState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanError: String? = null,
    val scannedApks: List<ApkSignatureInfo> = emptyList(),
    val selectedApk: ApkSignatureInfo? = null,
    val overallHealthScore: Int = 0,
    val radarChartData: RadarChartData = RadarChartData(0, 0, 0, 0, 0, 0),
    val totalApksScanned: Int = 0,
    val quantumSafeCount: Int = 0,
    val atRiskCount: Int = 0,
    val pqcAlgorithms: List<PQCAlgorithm> = emptyList(),
    val selectedAlgorithm: PQCAlgorithm? = null,
    val configWizardStep: ConfigWizardStep = ConfigWizardStep.ASSESS_CURRENT,
    val selectedPQCForMigration: PQCAlgorithm? = null,
    val reportGenerating: Boolean = false,
    val reportGenerated: Boolean = false,
    val reportPath: String? = null,
    val selectedReportFormat: ReportFormat = ReportFormat.PDF,
    val deviceCapabilities: List<KeystoreCapability> = emptyList(),
    val isDetectingDevices: Boolean = false,
    val migrationSteps: List<MigrationStep> = emptyList(),
    val migrationProgress: Float = 0f,
    val selectedTab: PQCTab = PQCTab.DASHBOARD,
    val errorMessage: String? = null
) : MviState

// ===== Intent =====
// ===== 用户意图 =====

sealed class PQCIntent : MviIntent {
    data class SelectTab(val tab: PQCTab) : PQCIntent()
    data class ScanAPK(val uri: Uri) : PQCIntent()
    data class SelectApkResult(val apk: ApkSignatureInfo?) : PQCIntent()
    object ClearScanResults : PQCIntent()
    data class SelectAlgorithm(val algorithm: PQCAlgorithm?) : PQCIntent()
    object NextWizardStep : PQCIntent()
    object PrevWizardStep : PQCIntent()
    data class SelectPQCForMigration(val algorithm: PQCAlgorithm) : PQCIntent()
    data class GenerateReport(val format: ReportFormat) : PQCIntent()
    data class SelectReportFormat(val format: ReportFormat) : PQCIntent()
    object DetectDeviceCapabilities : PQCIntent()
    data class ToggleMigrationStep(val stepId: String) : PQCIntent()
    object ResetChecklist : PQCIntent()
}

// ===== Effect =====
// ===== 副作用 =====

sealed class PQCEffect : MviEffect {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : PQCEffect()
    data class ReportGenerated(val filePath: String, val format: ReportFormat) : PQCEffect()
    data class ShareFile(val filePath: String) : PQCEffect()
    data class NavigateToAlgorithmDetail(val algorithmId: String) : PQCEffect()
    data class NavigateToWizardStep(val step: ConfigWizardStep) : PQCEffect()
    data class ScanComplete(val apkCount: Int, val quantumSafeCount: Int) : PQCEffect()
    data class ShowError(val message: String) : PQCEffect()
}
