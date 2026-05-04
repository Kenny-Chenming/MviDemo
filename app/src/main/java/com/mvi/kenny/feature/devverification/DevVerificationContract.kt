package com.mvi.kenny.feature.devverification

import android.graphics.drawable.Drawable
import java.security.cert.X509Certificate
import java.time.LocalDate

/**
 * ============================================================
 * PRD-224 | Android 开发者身份验证合规工具包
 * MVI Contract — State / Intent / Effect
 * ============================================================
 * Design: designs/PRD-224-Android-开发者验证合规工具包.md
 * Status: 设计完成，待移交开发 | 2026-05-04
 *
 * 5-Tab Architecture:
 *   Tab 0: 状态检测 (Verification Status)
 *   Tab 1: 注册指南 (Registration Guide)
 *   Tab 2: 密钥工具 (Key Tools)
 *   Tab 3: CI 合规 (CI Compliance)
 *   Tab 4: 决策参考 (Decision Reference)
 */

// ============ Shared Models ============
// ============ 共享数据模型 ============

/** Verification registration status for an app */
enum class VerificationStatus {
    REGISTERED,    // 已注册（合规）
    UNREGISTERED,  // 未注册（不合规）
    UNKNOWN        // 未知（无法自动检测）
}

/** Signature match result when comparing APK signature to registration records */
enum class SignatureMatch {
    MATCHED,       // 签名匹配（已在 Google 注册）
    MISMATCHED,    // 签名不匹配（密钥与注册信息不一致）
    NO_SIGNATURE,  // 无法获取签名
    UNKNOWN        // 未知
}

/**
 * APK scan input mode — either APK file or package name
 * APK 扫描输入模式 — APK文件 或 包名
 */
sealed class ScanInput {
    data class ApkFile(val path: String, val fileName: String) : ScanInput()
    data class PackageName(val name: String) : ScanInput()
}

/**
 * APK scan result data
 * APK 扫描结果数据
 */
data class ApkScanResult(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val signatureFingerprint: String,  // SHA-256 fingerprint / SHA-256 指纹
    val verificationStatus: VerificationStatus,
    val signatureMatch: SignatureMatch,
    val signatureAlgorithm: String,    // e.g. "SHA256withRSA"
    val validFrom: LocalDate?,        // 证书有效期起
    val validUntil: LocalDate?,       // 证书有效期止
    val signingCert: X509Certificate? = null  // Full cert for key tool / 完整证书（密钥工具用）
)

// ============ Tab 1: Status Detection Models ============
// ============ Tab 1: 状态检测数据模型 ============

/** Guide path: Android Developer Console vs Play Console */
enum class GuidePath(val displayName: String, val url: String) {
    ANDROID_DEVELOPER_CONSOLE(
        displayName = "Android Developer Console",
        url = "https://developer.android.com/developer-verification"
    ),
    PLAY_CONSOLE(
        displayName = "Google Play Console",
        url = "https://play.google.com/console"
    )
}

// ============ Tab 2: Registration Guide Models ============
// ============ Tab 2: 注册指南数据模型 ============

/**
 * Registration guide step
 * 注册指南步骤
 */
data class GuideStep(
    val index: Int,
    val title: String,
    val description: String,
    val details: List<String>,
    val url: String? = null
)

// ============ Tab 3: Key Tool Models ============
// ============ Tab 3: 密钥工具数据模型 ============

/** Key tool operation mode */
enum class KeyToolMode(val displayName: String) {
    EXTRACT_CERT("Extract Certificate"),
    CALCULATE_FINGERPRINT("Calculate Fingerprint"),
    GENERATE_REG_INFO("Generate Registration Info")
}

/** Keystore file selection result */
data class KeystoreFile(
    val path: String,
    val fileName: String,
    val fileSize: Long  // bytes
)

/**
 * Output from key tool operations
 * 密钥工具操作输出
 */
data class KeyToolOutput(
    val certificateInfo: CertificateInfo?,
    val fingerprint: String?,         // SHA-256 fingerprint / SHA-256 指纹
    val registrationInfo: String?,   // Ready-to-paste registration text / 可直接粘贴的注册文本
    val rawCertBase64: String?        // Base64 encoded DER certificate
)

/**
 * Parsed X.509 certificate information
 * 解析后的 X.509 证书信息
 */
data class CertificateInfo(
    val subjectDN: String,            // e.g. "CN=Android Debug, O=Android, C=US"
    val issuerDN: String,
    val serialNumber: String,
    val validFrom: LocalDate,
    val validUntil: LocalDate,
    val signatureAlgorithm: String,
    val publicKeyAlgorithm: String,
    val publicKeySize: Int,          // bits, e.g. 2048
    val version: Int                  // X.509 version, e.g. 3
)

// ============ Tab 4: CI Compliance Models ============
// ============ Tab 4: CI 合规数据模型 ============

/**
 * CI compliance check result
 * CI 合规检查结果
 */
data class ComplianceResult(
    val hasDeclaredPermission: Boolean,    // ACCESS_LOCAL_NETWORK declared / 已声明权限
    val hasVerificationCheck: Boolean,     // Has dev verification check in build / 构建中有验证检查
    val gradlePluginVersion: String?,      // Detected plugin version / 检测到的插件版本
    val complianceLevel: ComplianceLevel,
    val suggestions: List<String>
)

enum class ComplianceLevel(val label: String) {
    COMPLIANT("Compliant"),
    WARNING("Warning"),
    NON_COMPLIANT("Non-compliant"),
    UNKNOWN("Unknown")
}

// ============ Tab 5: Decision Reference Models ============
// ============ Tab 5: 决策参考数据模型 ============

/** Decision reference sub-module */
enum class DecisionModule(val displayName: String, val description: String) {
    KEY_ROTATION("Key Rotation", "签名密钥轮换规则与合规路径"),
    ENTERPRISE_DISTRIBUTION("Enterprise Distribution", "企业内部分发验证要求"),
    MULTI_APK("Multi-APK Strategy", "多 APK/Variant 密钥注册策略")
}

/**
 * FAQ item for decision reference
 * 决策参考 FAQ 条目
 */
data class FaqItem(
    val index: Int,
    val question: String,
    val answer: String
)

// ============ MVI State ============
// ============ MVI 状态 ============

/**
 * Top-level UI state —PRD-224 Dev Verification Toolkit
 * 主状态 — PRD-224 开发者验证合规工具包
 */
data class DevVerificationState(
    // Navigation / 导航
    val selectedTab: Int = 0,   // 0-4 corresponding to 5 main tabs / 0-4 对应5个主Tab

    // ─────────────────────────────────────────────────────────
    // Tab 1: 状态检测 (Verification Status)
    // ─────────────────────────────────────────────────────────
    val scanInput: ScanInput = ScanInput.PackageName(""),
    val scanResult: ApkScanResult? = null,
    val isScanning: Boolean = false,
    val scanError: String? = null,

    // ─────────────────────────────────────────────────────────
    // Tab 2: 注册指南 (Registration Guide)
    // ─────────────────────────────────────────────────────────
    val selectedGuidePath: GuidePath = GuidePath.ANDROID_DEVELOPER_CONSOLE,
    val expandedStep: Int? = null,   // Accordion expanded step index / 折叠面板展开的步骤索引

    // ─────────────────────────────────────────────────────────
    // Tab 3: 密钥工具 (Key Tools)
    // ─────────────────────────────────────────────────────────
    val keyToolMode: KeyToolMode = KeyToolMode.EXTRACT_CERT,
    val keystoreFile: KeystoreFile? = null,
    val keystorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = "",
    val keyToolOutput: KeyToolOutput? = null,
    val isProcessingKey: Boolean = false,
    val keyProcessError: String? = null,

    // ─────────────────────────────────────────────────────────
    // Tab 4: CI 合规 (CI Compliance)
    // ─────────────────────────────────────────────────────────
    val gradleConfig: String = DEFAULT_GRADLE_CONFIG,
    val complianceResult: ComplianceResult? = null,
    val isCheckingCompliance: Boolean = false,

    // ─────────────────────────────────────────────────────────
    // Tab 5: 决策参考 (Decision Reference)
    // ─────────────────────────────────────────────────────────
    val selectedDecisionModule: DecisionModule? = null,
    val faqExpandedItems: Set<Int> = emptySet()
)

// Default Gradle plugin config shown in CI Compliance tab
// CI 合规 Tab 中显示的默认 Gradle 插件配置
val DEFAULT_GRADLE_CONFIG: String = """
android {
    buildFeatures {
        // Enable Android Developer Verification check
        // 启用 Android 开发者验证检查
        devVerification {
            enabled = true
            // Block build if app is not verified
            // 如果 App 未验证则阻塞构建
            enforceVerification = true
            // Allowed unverified package prefixes (regex)
            // 允许未验证的包名前缀（正则）
            allowedUnverifiedPrefixes = ["com.mycompany.internal."]
        }
    }
}

dependencies {
    implementation("com.android.tools:dev-verification-plugin:1.0.0")
}
""".trimIndent()

// ============ MVI Intent ============
// ============ MVI 用户意图 ============

/**
 * All user intents for PRD-224
 * PRD-224 所有用户意图
 */
sealed class DevVerificationIntent {

    // ── Tab Navigation ──
    data class SelectTab(val index: Int) : DevVerificationIntent()

    // ── Tab 1: 状态检测 ──
    data class SetScanInput(val input: ScanInput) : DevVerificationIntent()
    data object StartScan : DevVerificationIntent()
    data object ClearScanResult : DevVerificationIntent()

    // ── Tab 2: 注册指南 ──
    data class SelectGuidePath(val path: GuidePath) : DevVerificationIntent()
    data class ToggleStep(val stepIndex: Int) : DevVerificationIntent()

    // ── Tab 3: 密钥工具 ──
    data class SetKeyToolMode(val mode: KeyToolMode) : DevVerificationIntent()
    data class SetKeystoreFile(val file: KeystoreFile?) : DevVerificationIntent()
    data class SetKeystorePassword(val password: String) : DevVerificationIntent()
    data class SetKeyAlias(val alias: String) : DevVerificationIntent()
    data class SetKeyPassword(val password: String) : DevVerificationIntent()
    data object ProcessKeystore : DevVerificationIntent()
    data object ClearKeyOutput : DevVerificationIntent()

    // ── Tab 4: CI 合规 ──
    data class UpdateGradleConfig(val config: String) : DevVerificationIntent()
    data object GenerateComplianceReport : DevVerificationIntent()
    data object ApplyGradleConfigToProject : DevVerificationIntent()

    // ── Tab 5: 决策参考 ──
    data class SelectDecisionModule(val module: DecisionModule?) : DevVerificationIntent()
    data class ToggleFaqItem(val index: Int) : DevVerificationIntent()
}

// ============ MVI Effect ============
// ============ MVI 副作用（一次性事件）===========

/**
 * One-time side effects for PRD-224
 * PRD-224 一次性副作用
 */
sealed class DevVerificationEffect {
    data class ShowToast(val message: String) : DevVerificationEffect()
    data object ScanComplete : DevVerificationEffect()
    data object KeyProcessComplete : DevVerificationEffect()
    data class CopyToClipboard(val text: String) : DevVerificationEffect()
    data class OpenExternalUrl(val url: String) : DevVerificationEffect()
    data class ShowError(val message: String) : DevVerificationEffect()
    data object ConfigApplied : DevVerificationEffect()
}
