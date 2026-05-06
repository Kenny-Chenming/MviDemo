package com.mvi.kenny.feature.pqctoolkit

// ================================================================
// PQCToolkitContract — Android 17 PQC Migration Toolkit MVI Contract
// ================================================================
// MVI architecture contract for Android 17 Post-Quantum Cryptography Migration Toolkit.
//
// PRD-236: Android 17 后量子密码学（PQC）迁移工具包
// Design Reference: memory/agency/designs/PRD-236-Android-17-PQC-后量子密码学迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// Six tool tabs:
//   1. Crypto Scanner — Detect RSA/ECDSA usage in source code
//   2. ML-DSA Generator — Generate quantum-safe ML-DSA keys
//   3. Migration Guide — RSA/ECDSA → ML-DSA migration steps
//   4. Benchmark — ML-DSA vs RSA/ECDSA performance comparison
//   5. Play Signing Guide — Google Play Signing PQC guide
//   6. Gradle Plugin — CI PQC compliance check
// ================================================================

import androidx.compose.ui.graphics.Color

// =============================================================
// Color Palette — Dark Terminal CLI Style (MAGENTA Security Theme)
// =============================================================
object PQCColors {
    val Primary = Color(0xFF9C27B0)         // MAGENTA/PURPLE — Security theme
    val Secondary = Color(0xFFCE93D8)        // Light purple
    val Background = Color(0xFF0D1117)        // Deep dark
    val Surface = Color(0xFF161B22)          // Card background
    val SurfaceVariant = Color(0xFF21262D)    // Input background
    val OnSurface = Color(0xFFE6EDF3)         // Primary text
    val OnSurfaceVariant = Color(0xFF8B949E)  // Secondary text
    val Pass = Color(0xFF3FB950)             // GREEN — Success/PASS
    val Warn = Color(0xFFFFA000)             // AMBER — Warning
    val Fail = Color(0xFFEA4335)              // RED — Failure
    val Critical = Color(0xFFD32F2F)          // CRITICAL — Dark red
    val Info = Color(0xFF58A6FF)            // BLUE — Info
    val Accent = Color(0xFFBB86FC)           // Purple accent
}

// =============================================================
// ToolTab — Tool tab enumeration
// =============================================================
enum class PQCToolTab(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    CRYPTO_SCANNER("Crypto Scanner", "🔐", "Scan source code for RSA/ECDSA usage"),
    ML_DSA_GENERATOR("ML-DSA Gen", "🔑", "Generate quantum-safe ML-DSA keys"),
    MIGRATION_GUIDE("Migration", "📋", "RSA/ECDSA → ML-DSA migration guide"),
    BENCHMARK("Benchmark", "⚡", "ML-DSA vs RSA/ECDSA performance"),
    PLAY_SIGNING("Play Signing", "🎮", "Google Play Signing PQC guide"),
    GRADLE_PLUGIN("Gradle Plugin", "🔧", "CI PQC compliance check")
}

// =============================================================
// ReadinessLevel — PQC readiness level
// =============================================================
enum class ReadinessLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    READY("Ready", "✅", Color(0xFF3FB950)),
    WARNING("Warning", "⚠️", Color(0xFFFFA000)),
    NOT_READY("Not Ready", "⛔", Color(0xFFEA4335)),
    UNKNOWN("Unknown", "❓", Color(0xFF8B949E))
}

// =============================================================
// MigrationUrgency — Migration urgency level
// =============================================================
enum class MigrationUrgency(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val priority: Int
) {
    CRITICAL("Critical", "🔴", Color(0xFFD32F2F), 1),
    HIGH("High", "🟠", Color(0xFFFF7B72), 2),
    MEDIUM("Medium", "🟡", Color(0xFFFFD700), 3),
    LOW("Low", "🟢", Color(0xFF3FB950), 4),
    INFO("Info", "ℹ️", Color(0xFF58A6FF), 5)
}

// =============================================================
// CryptoAlgorithm — Detected crypto algorithm type
// =============================================================
enum class CryptoAlgorithm(
    val displayName: String,
    val keySize: Int,
    val quantumSafe: Boolean
) {
    RSA_1024("RSA-1024", 1024, false),
    RSA_2048("RSA-2048", 2048, false),
    RSA_3072("RSA-3072", 3072, false),
    RSA_4096("RSA-4096", 4096, false),
    ECDSA_P256("ECDSA-P256", 256, false),
    ECDSA_P384("ECDSA-P384", 384, false),
    ECDSA_P521("ECDSA-P521", 521, false),
    ML_DSA_65("ML-DSA-65", 65 * 8, true),
    ML_DSA_87("ML-DSA-87", 87 * 8, true),
    HYBRID("Hybrid", 0, true)
}

// =============================================================
// CryptoUsage — Usage context of crypto algorithm
// =============================================================
enum class CryptoUsage(val displayName: String) {
    SIGNATURE("Digital Signature"),
    KEY_EXCHANGE("Key Exchange"),
    ENCRYPTION("Encryption"),
    CERTIFICATE("Certificate Validation"),
    TEST("Test Code"),
    UNKNOWN("Unknown")
}

// =============================================================
// CryptoFinding — Scanning finding
// =============================================================
data class CryptoFinding(
    val file: String,
    val line: Int,
    val algorithm: CryptoAlgorithm,
    val usage: CryptoUsage,
    val urgency: MigrationUrgency,
    val code: String,
    val suggestion: String
)

// =============================================================
// ScanPhase — Scanning phase
// =============================================================
enum class ScanPhase {
    IDLE,
    SCANNING_KOTLIN,
    SCANNING_JAVA,
    ANALYZING,
    GENERATING_REPORT,
    COMPLETED,
    ERROR
}

// =============================================================
// KeyGenState — Key generation state
// =============================================================
enum class KeyGenStatus {
    IDLE,
    GENERATING,
    SUCCESS,
    ERROR
}

// =============================================================
// BenchmarkResult — Single benchmark comparison result
// =============================================================
data class BenchmarkResult(
    val operation: String,
    val rsa2048Value: String,
    val ecdsaP256Value: String,
    val mlDsa65Value: String,
    val delta: String
)

// =============================================================
// TestStatus — Test run status
// =============================================================
enum class TestStatus { NOT_RUN, PASS, WARN, FAIL, ERROR }

// =============================================================
// CryptoScannerState — Crypto scanner state (MVI State)
// =============================================================
data class CryptoScannerState(
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanPhase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val scannedFiles: Int = 0,
    val totalFiles: Int = 0,
    val findings: List<CryptoFinding> = emptyList(),
    val readinessLevel: ReadinessLevel = ReadinessLevel.UNKNOWN,
    val criticalCount: Int = 0,
    val warningCount: Int = 0,
    val infoCount: Int = 0,
    val lastScanTime: Long = 0L,
    val reportPath: String = "",
    val errorMessage: String = ""
)

// =============================================================
// MLDSAGeneratorState — ML-DSA key generator state (MVI State)
// =============================================================
data class MLDSAGeneratorState(
    val keyAlias: String = "my-pqc-key",
    val selectedVariant: MLDSAVariant = MLDSAVariant.ML_DSA_65,
    val keyGenStatus: KeyGenStatus = KeyGenStatus.IDLE,
    val generatedCodeSnippet: String = "",
    val keyInfo: String = "",
    val errorMessage: String = ""
)

enum class MLDSAVariant(val displayName: String, val parameterSet: Int) {
    ML_DSA_44("ML-DSA-44 (Speed)", 44),
    ML_DSA_65("ML-DSA-65 (Balanced)", 65),
    ML_DSA_87("ML-DSA-87 (Security)", 87)
}

// =============================================================
// MigrationGuideState — Migration guide state (MVI State)
// =============================================================
data class MigrationGuideState(
    val selectedAlgorithm: CryptoAlgorithm = CryptoAlgorithm.RSA_2048,
    val selectedUsage: CryptoUsage = CryptoUsage.SIGNATURE,
    val currentStep: Int = 0,
    val totalSteps: Int = 4,
    val codeDiff: String = "",
    val hybridModeEnabled: Boolean = true,
    val showFullDiff: Boolean = false
)

// =============================================================
// BenchmarkState — Benchmark state (MVI State)
// =============================================================
data class BenchmarkState(
    val isRunning: Boolean = false,
    val progress: Int = 0,
    val currentOperation: String = "Idle",
    val results: List<BenchmarkResult> = emptyList(),
    val deviceInfo: String = "Android Emulator / Physical Device",
    val recommendation: String = ""
)

// =============================================================
// PlaySigningGuideState — Play Signing guide state (MVI State)
// =============================================================
data class PlaySigningGuideState(
    val selectedTopic: PlaySigningTopic = PlaySigningTopic.OVERVIEW,
    val isExpanded: Boolean = false
)

enum class PlaySigningTopic(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    OVERVIEW("Overview", "📖", "Google Play Signing PQC overview"),
    KEY_MIGRATION("Key Migration", "🔑", "How to migrate your signing keys"),
    TIMELINE("Timeline", "⏰", "2029 deadline and milestones"),
    FAQ("FAQ", "❓", "Frequently asked questions")
}

// =============================================================
// GradlePluginState — Gradle plugin state (MVI State)
// =============================================================
data class GradlePluginState(
    val isChecking: Boolean = false,
    val isMlDSAIntegrated: Boolean = false,
    val isHybridMode: Boolean = false,
    val isPlaySigningPQC: Boolean = false,
    val checkResult: ReadinessLevel = ReadinessLevel.UNKNOWN,
    val checkMessages: List<String> = emptyList(),
    val configurationSnippet: String = "",
    val errorMessage: String = ""
)

// =============================================================
// Main State — Aggregate state
// =============================================================
data class PQCToolkitState(
    val selectedTab: PQCToolTab = PQCToolTab.CRYPTO_SCANNER,
    val cryptoScannerState: CryptoScannerState = CryptoScannerState(),
    val mlDSAGeneratorState: MLDSAGeneratorState = MLDSAGeneratorState(),
    val migrationGuideState: MigrationGuideState = MigrationGuideState(),
    val benchmarkState: BenchmarkState = BenchmarkState(),
    val playSigningGuideState: PlaySigningGuideState = PlaySigningGuideState(),
    val gradlePluginState: GradlePluginState = GradlePluginState()
)

// =============================================================
// Intent — User actions
// =============================================================
sealed class PQCToolkitIntent {
    // Crypto Scanner intents
    data class UpdateProjectPath(val path: String) : PQCToolkitIntent()
    data object StartScan : PQCToolkitIntent()
    data object CancelScan : PQCToolkitIntent()
    data object ClearFindings : PQCToolkitIntent()

    // ML-DSA Generator intents
    data class UpdateKeyAlias(val alias: String) : PQCToolkitIntent()
    data class SelectVariant(val variant: MLDSAVariant) : PQCToolkitIntent()
    data object GenerateKey : PQCToolkitIntent()
    data object GenerateCodeSnippet : PQCToolkitIntent()

    // Migration Guide intents
    data class SelectAlgorithm(val algorithm: CryptoAlgorithm) : PQCToolkitIntent()
    data class SelectUsage(val usage: CryptoUsage) : PQCToolkitIntent()
    data object NextStep : PQCToolkitIntent()
    data object PreviousStep : PQCToolkitIntent()
    data object ToggleHybridMode : PQCToolkitIntent()
    data object GenerateCodeDiff : PQCToolkitIntent()

    // Benchmark intents
    data object RunBenchmark : PQCToolkitIntent()
    data object StopBenchmark : PQCToolkitIntent()
    data object ClearResults : PQCToolkitIntent()

    // Play Signing Guide intents
    data class SelectTopic(val topic: PlaySigningTopic) : PQCToolkitIntent()

    // Gradle Plugin intents
    data object CheckCompliance : PQCToolkitIntent()
    data object GenerateConfigSnippet : PQCToolkitIntent()

    // Tab navigation
    data class SelectTab(val tab: PQCToolTab) : PQCToolkitIntent()
}

// =============================================================
// Effect — One-time side effects
// =============================================================
sealed class PQCToolkitEffect {
    data class ShowToast(val message: String) : PQCToolkitEffect()
    data class ShowError(val message: String) : PQCToolkitEffect()
    data class ReportGenerated(val path: String) : PQCToolkitEffect()
    data object ScanCompleted : PQCToolkitEffect()
    data object KeyGenerated : PQCToolkitEffect()
    data object BenchmarkCompleted : PQCToolkitEffect()
    data class ReadinessUpdate(val level: ReadinessLevel) : PQCToolkitEffect()
}
