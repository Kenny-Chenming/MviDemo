package com.mvi.kenny.feature.pqcmigration

// ============================================================
// PQCMigrationViewModel — PQC 迁移工具包主 ViewModel
// PRD-165 | Android 17 Post-Quantum Cryptography 迁移工具包
// ============================================================
/**
 * 继承 ViewModel，持有 PQCMigrationState 和各子模块 State，
 * 通过 sendIntent() 处理所有用户意图。
 *
 * 状态管理：
 * - _state: 私有 MutableStateFlow，ViewModel 内部写入
 * - state: 公开 StateFlow，供 UI 层订阅（collectAsState）
 *
 * 副作用管理：
 * - _effect: Channel（热流），缓冲区大小 BUFFERED
 * - effect: receiveAsFlow，UI 层通过 collect{} 监听
 *
 * @see PQCMigrationContract
 * @see PQCMigrationScreen
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PQC 迁移工具包主 ViewModel
 * PQC Migration Toolkit Main ViewModel.
 *
 * 管理全局状态和各子模块状态：
 * - 全局 OverviewState（Tab 导航、工具摘要）
 * - KeyGenState（密钥生成）
 * - KeyMigrationState（密钥迁移扫描）
 * - TLSComplianceState（TLS 合规检测）
 * - AppSigningState（App Signing PQ 合规）
 *
 * @see PQCMigrationState
 * @see PQCMigrationIntent
 * @see PQCMigrationEffect
 */
class PQCMigrationViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态
    // ============================================================

    /** 全局页面状态（StateFlow，UI 只读） / Global page state (StateFlow, UI read-only) */
    private val _state = MutableStateFlow(PQCMigrationState.Initial)
    val state: StateFlow<PQCMigrationState> = _state.asStateFlow()

    /** KeyGen 子模块状态 / KeyGen sub-module state */
    private val _keyGenState = MutableStateFlow(KeyGenState.Initial)
    val keyGenState: StateFlow<KeyGenState> = _keyGenState.asStateFlow()

    /** KeyMigration 子模块状态 / KeyMigration sub-module state */
    private val _keyMigrationState = MutableStateFlow(KeyMigrationState.Initial)
    val keyMigrationState: StateFlow<KeyMigrationState> = _keyMigrationState.asStateFlow()

    /** TLSCompliance 子模块状态 / TLSCompliance sub-module state */
    private val _tlsComplianceState = MutableStateFlow(TLSComplianceState.Initial)
    val tlsComplianceState: StateFlow<TLSComplianceState> = _tlsComplianceState.asStateFlow()

    /** AppSigning 子模块状态 / AppSigning sub-module state */
    private val _appSigningState = MutableStateFlow(AppSigningState.Initial)
    val appSigningState: StateFlow<AppSigningState> = _appSigningState.asStateFlow()

    // ============================================================
    // Effect — 副作用 Channel
    // ============================================================

    /** 副作用 Channel（热流） / Effect Channel (hot flow) */
    private val _effect = Channel<PQCMigrationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent 处理入口
    // ============================================================

    /**
     * 接收并处理用户意图 / Receive and handle user intent
     *
     * @param intent 用户意图 / User intent
     *
     * @see PQCMigrationIntent
     */
    fun sendIntent(intent: PQCMigrationIntent) {
        when (intent) {
            // —————————————————————————————————————————————
            // Tab Navigation
            // —————————————————————————————————————————————
            is PQCMigrationIntent.SelectTab -> handleSelectTab(intent.tab)

            // —————————————————————————————————————————————
            // KeyGen
            // —————————————————————————————————————————————
            is PQCMigrationIntent.SelectAlgorithm -> handleSelectAlgorithm(intent.algorithm)
            is PQCMigrationIntent.SetApiLevel -> handleSetApiLevel(intent.level)
            is PQCMigrationIntent.GenerateKeyGenCode -> handleGenerateKeyGenCode()
            is PQCMigrationIntent.CopyGeneratedCode -> handleCopyGeneratedCode()

            // —————————————————————————————————————————————
            // KeyMigration
            // —————————————————————————————————————————————
            is PQCMigrationIntent.SetProjectPath -> handleSetProjectPath(intent.path)
            is PQCMigrationIntent.StartKeyMigrationScan -> handleStartKeyMigrationScan()
            is PQCMigrationIntent.CancelScan -> handleCancelScan()
            is PQCMigrationIntent.SelectFinding -> handleSelectFinding(intent.finding)
            is PQCMigrationIntent.ExportMigrationReport -> handleExportMigrationReport()

            // —————————————————————————————————————————————
            // TLSCompliance
            // —————————————————————————————————————————————
            is PQCMigrationIntent.SetTLSInputPath -> handleSetTLSInputPath(intent.path)
            is PQCMigrationIntent.SetTLSInputMode -> handleSetTLSInputMode(intent.mode)
            is PQCMigrationIntent.StartTLSScan -> handleStartTLSScan()
            is PQCMigrationIntent.ExportTLSReport -> handleExportTLSReport()

            // —————————————————————————————————————————————
            // AppSigning
            // —————————————————————————————————————————————
            is PQCMigrationIntent.SetApkPath -> handleSetApkPath(intent.path)
            is PQCMigrationIntent.AnalyzeAppSigning -> handleAnalyzeAppSigning()
            is PQCMigrationIntent.ExportSigningReport -> handleExportSigningReport()

            // —————————————————————————————————————————————
            // Overview
            // —————————————————————————————————————————————
            is PQCMigrationIntent.RefreshOverview -> handleRefreshOverview()
            is PQCMigrationIntent.ScanTool -> handleScanTool(intent.module)
        }
    }

    // ============================================================
    // Tab Navigation Handlers
    // ============================================================

    /**
     * 处理 Tab 选择 / Handle tab selection
     *
     * @param tab 选中的 Tab / Selected tab
     */
    private fun handleSelectTab(tab: PQCToolModule) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // ============================================================
    // KeyGen Handlers
    // ============================================================

    /**
     * 处理算法选择 / Handle algorithm selection
     *
     * @param algorithm 选中的算法 / Selected algorithm
     */
    private fun handleSelectAlgorithm(algorithm: PqcAlgorithm) {
        _keyGenState.update { it.copy(selectedAlgorithm = algorithm, generatedCode = null) }
    }

    /**
     * 处理 API 级别设置 / Handle API level setting
     *
     * @param level API 级别 / API level
     */
    private fun handleSetApiLevel(level: Int) {
        _keyGenState.update { it.copy(apiLevel = level, generatedCode = null) }
    }

    /**
     * 处理密钥生成代码生成 / Handle key generation code generation
     *
     * 根据选中的算法和 API 级别，生成 KeyGenParameterSpec 完整代码。
     * Generates complete KeyGenParameterSpec code based on selected algorithm and API level.
     */
    private fun handleGenerateKeyGenCode() {
        viewModelScope.launch {
            _keyGenState.update { it.copy(isGenerating = true, error = null) }

            // Simulate code generation delay / 模拟代码生成延迟
            delay(800)

            val state = _keyGenState.value
            val code = generateKeyGenCode(state.selectedAlgorithm, state.apiLevel)

            _keyGenState.update {
                it.copy(
                    isGenerating = false,
                    generatedCode = code,
                    copiedToClipboard = false
                )
            }
        }
    }

    /**
     * 生成 KeyGenParameterSpec 代码 / Generate KeyGenParameterSpec code
     *
     * @param algorithm PQ 算法 / PQC algorithm
     * @param apiLevel API 级别 / API level
     * @return 生成的 Kotlin 代码 / Generated Kotlin code
     */
    private fun generateKeyGenCode(algorithm: PqcAlgorithm, apiLevel: Int): String {
        return when (algorithm) {
            PqcAlgorithm.ML_KEM_768 -> """
/**
 * Generate ML-KEM-768 Key via Android Keystore
 * ML-KEM-768 密钥生成 — Android Keystore
 *
 * Prerequisites / 前置条件:
 * - minSdkVersion must be >= 38 (Android 14)
 * - Algorithm: KeyProperties.KEY_ALGORITHM_EC (PQ hybrid via KeyGenParameterSpec)
 *
 * @param keyAlias Alias for the generated key / 密钥别名
 * @param attestationChallenge Optional challenge for attestation / 可选的认证挑战
 */
fun generateMLKEM768Key(
    keyAlias: String,
    attestationChallenge: ByteArray? = null
): KeyGenParameterSpec {
    val builder = KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        // Set algorithm to EC for PQ hybrid / 设置为 EC 算法（PQ 混合模式）
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        // ML-KEM-768 key size / ML-KEM-768 密钥大小
        .setKeySize(768)
        // Digest for signing / 签名摘要算法
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        // Validity period / 密钥有效期
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        // Android 14+ required for ML-KEM support / ML-KEM 支持需要 Android 14+
        .setMinApiLevel(38)

    // Attestation challenge if provided / 如果提供了认证挑战
    attestationChallenge?.let {
        builder.setAttestationChallenge(it)
    }

    return builder.build()
}
            """.trimIndent()

            PqcAlgorithm.ML_KEM_1024 -> """
/**
 * Generate ML-KEM-1024 Key via Android Keystore
 * ML-KEM-1024 密钥生成 — Android Keystore
 *
 * Prerequisites / 前置条件:
 * - minSdkVersion must be >= 38
 * - Higher security level than ML-KEM-768
 */
fun generateMLKEM1024Key(
    keyAlias: String,
    attestationChallenge: ByteArray? = null
): KeyGenParameterSpec {
    val builder = KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        .setKeySize(1024)  // ML-KEM-1024 security level / ML-KEM-1024 安全级别
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA384)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)

    attestationChallenge?.let { builder.setAttestationChallenge(it) }

    return builder.build()
}
            """.trimIndent()

            PqcAlgorithm.KYBER_512 -> """
/**
 * Generate Kyber-512 Key via Android Keystore
 * Kyber-512 密钥生成 — Android Keystore
 *
 * Note: Kyber-512 is the predecessor to ML-KEM-768 (same security level)
 * Kyber-512 是 ML-KEM-768 的前身（相同安全级别）
 */
fun generateKyber512Key(
    keyAlias: String,
    attestationChallenge: ByteArray? = null
): KeyGenParameterSpec {
    val builder = KeyGenParameterSpec.Builder(
        keyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        .setKeySize(512)  // Kyber-512 equivalent to ML-KEM-768 / Kyber-512 等价于 ML-KEM-768
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)

    attestationChallenge?.let { builder.setAttestationChallenge(it) }

    return builder.build()
}
            """.trimIndent()

            PqcAlgorithm.HYBRID_RSA_ML_KEM -> """
/**
 * Generate Hybrid RSA + ML-KEM Key via Android Keystore
 * 混合 RSA + ML-KEM 密钥生成 — Android Keystore
 *
 * Hybrid mode provides dual security: traditional RSA + post-quantum ML-KEM
 * 混合模式提供双重安全：传统 RSA + 后量子 ML-KEM
 *
 * Note: Android Keystore uses KeyGenParameterSpec for key generation.
 * For hybrid mode, both keys should be generated and combined.
 *
 * @param rsaKeyAlias RSA key alias / RSA 密钥别名
 * @param pqKeyAlias Post-quantum key alias / 后量子密钥别名
 */
fun generateHybridRSAKey(
    rsaKeyAlias: String,
    pqKeyAlias: String,
    keySize: Int = 2048  // RSA key size / RSA 密钥大小
): KeyGenParameterSpec {
    // RSA key generation spec / RSA 密钥生成规格
    return KeyGenParameterSpec.Builder(
        rsaKeyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
        .setKeySize(keySize)
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)
        .build()
}

// Separate function for ML-KEM component / ML-KEM 组件的独立函数
fun generateHybridMLKEMComponent(pqKeyAlias: String): KeyGenParameterSpec {
    return KeyGenParameterSpec.Builder(
        pqKeyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        .setKeySize(768)  // ML-KEM-768 / ML-KEM-768
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)
        .build()
}
            """.trimIndent()

            PqcAlgorithm.HYBRID_EC_ML_KEM -> """
/**
 * Generate Hybrid EC + ML-KEM Key via Android Keystore
 * 混合椭圆曲线 + ML-KEM 密钥生成 — Android Keystore
 *
 * Hybrid mode combines traditional elliptic curve cryptography with post-quantum ML-KEM
 * 混合模式结合传统椭圆曲线密码学与后量子 ML-KEM
 *
 * @param ecKeyAlias Elliptic curve key alias / 椭圆曲线密钥别名
 * @param pqKeyAlias Post-quantum key alias / 后量子密钥别名
 */
fun generateHybridECKey(
    ecKeyAlias: String,
    pqKeyAlias: String
): KeyGenParameterSpec {
    // EC key generation spec / EC 密钥生成规格
    return KeyGenParameterSpec.Builder(
        ecKeyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        .setKeySize(256)  // P-256 curve / P-256 曲线
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)
        .build()
}

// ML-KEM component / ML-KEM 组件
fun generateHybridMLKEMForEC(pqKeyAlias: String): KeyGenParameterSpec {
    return KeyGenParameterSpec.Builder(
        pqKeyAlias,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    )
        .setKeyType(KeyProperties.KEY_ALGORITHM_EC)
        .setKeySize(768)  // ML-KEM-768 / ML-KEM-768
        .setDigests(KeyProperties.SIGNATURE_PURPOSE, KeyProperties.DIGEST_SHA256)
        .setKeyValidityForOriginEnd(KeyEpochClf.NEVER)
        .setMinApiLevel(38)
        .build()
}
            """.trimIndent()
        }
    }

    /**
     * 处理复制生成代码到剪贴板 / Handle copying generated code to clipboard
     */
    private fun handleCopyGeneratedCode() {
        viewModelScope.launch {
            val code = _keyGenState.value.generatedCode ?: return@launch
            _effect.send(PQCMigrationEffect.CopyToClipboard(code))
            _keyGenState.update { it.copy(copiedToClipboard = true) }
            _effect.send(PQCMigrationEffect.ShowSuccess("代码已复制到剪贴板 / Code copied to clipboard"))
        }
    }

    // ============================================================
    // KeyMigration Handlers
    // ============================================================

    /**
     * 处理项目路径设置 / Handle project path setting
     *
     * @param path 项目路径 / Project path
     */
    private fun handleSetProjectPath(path: String) {
        _keyMigrationState.update { it.copy(projectPath = path, error = null) }
    }

    /**
     * 开始密钥迁移扫描 / Start key migration scan
     *
     * 模拟扫描流程：
     * Phase 1: Detecting → Phase 2: Analyzing → Phase 3: Generating Diff → Complete
     *
     * Simulated scan flow:
     * Phase 1: Detecting → Phase 2: Analyzing → Phase 3: Generating Diff → Complete
     */
    private fun handleStartKeyMigrationScan() {
        val path = _keyMigrationState.value.projectPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(PQCMigrationEffect.ShowError("请输入项目路径 / Please enter project path"))
            }
            return
        }

        viewModelScope.launch {
            _keyMigrationState.update {
                it.copy(
                    isScanning = true,
                    scanPhase = ScanPhase.DETECTING,
                    progress = 0f,
                    findings = emptyList(),
                    migrationDiff = null,
                    error = null
                )
            }

            // Phase 1: Detecting / 检测阶段
            for (i in 1..10) {
                delay(200)
                _keyMigrationState.update {
                    it.copy(progress = 0.25f * (i / 10f))
                }
            }
            _keyMigrationState.update { it.copy(scanPhase = ScanPhase.ANALYZING) }

            // Phase 2: Analyzing / 分析阶段
            for (i in 1..10) {
                delay(200)
                _keyMigrationState.update {
                    it.copy(progress = 0.25f + 0.25f * (i / 10f))
                }
            }

            // Generate mock findings / 生成模拟发现项
            val mockFindings = listOf(
                NonCompliantKeyFinding(
                    id = "FIND-001",
                    filePath = "app/src/main/java/com/example/app/CryptoManager.kt",
                    lineNumber = 47,
                    keyType = "RSA",
                    usageContext = "KeyPairGenerator.getInstance(\"RSA\") — 密钥生成",
                    suggestedReplacement = "ML-KEM-768 via Android Keystore",
                    severity = "HIGH"
                ),
                NonCompliantKeyFinding(
                    id = "FIND-002",
                    filePath = "app/src/main/java/com/example/app/SecureStorage.kt",
                    lineNumber = 89,
                    keyType = "EC",
                    usageContext = "KeyPairGenerator.getInstance(\"EC\") — 签名密钥",
                    suggestedReplacement = "Hybrid EC + ML-KEM",
                    severity = "MEDIUM"
                ),
                NonCompliantKeyFinding(
                    id = "FIND-003",
                    filePath = "app/src/main/java/com/example/app/NetworkClient.kt",
                    lineNumber = 112,
                    keyType = "RSA",
                    usageContext = "SSLSocketFactory using RSA-based keystore",
                    suggestedReplacement = "TLS 1.3 with PQ hybrid cipher suite",
                    severity = "HIGH"
                )
            )

            _keyMigrationState.update { it.copy(findings = mockFindings) }
            _keyMigrationState.update { it.copy(scanPhase = ScanPhase.GENERATING_DIFF) }

            // Phase 3: Generating Diff / 生成 Diff 阶段
            for (i in 1..10) {
                delay(200)
                _keyMigrationState.update {
                    it.copy(progress = 0.75f + 0.25f * (i / 10f))
                }
            }

            val mockDiff = generateMockMigrationDiff()
            _keyMigrationState.update {
                it.copy(
                    isScanning = false,
                    scanPhase = ScanPhase.COMPLETE,
                    progress = 1f,
                    migrationDiff = mockDiff
                )
            }
            _effect.send(PQCMigrationEffect.ScanComplete)
        }
    }

    /**
     * 生成模拟迁移 Diff / Generate mock migration diff
     *
     * @return Unified diff format string / Unified diff 格式字符串
     */
    private fun generateMockMigrationDiff(): String {
        return """
--- a/app/src/main/java/com/example/app/CryptoManager.kt
+++ b/app/src/main/java/com/example/app/CryptoManager.kt
@@ -44,9 +44,12 @@ class CryptoManager {
-    fun generateKeyPair(): KeyPair {
-        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
-        generator.initialize(2048)
-        return generator.generateKeyPair()
+    /**
+     * Generate PQC-compliant key pair using ML-KEM-768
+     * 使用 ML-KEM-768 生成 PQC 合规密钥对
+     */
+    fun generateKeyPair(): KeyPair {
+        val spec = generateMLKEM768Key("pqc_master_key")
+        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
+        generator.initialize(spec)
+        return generator.generateKeyPair()
     }

     companion object {
--- a/app/src/main/java/com/example/app/NetworkClient.kt
+++ b/app/src/main/java/com/example/app/NetworkClient.kt
@@ -109,11 +109,15 @@ class NetworkClient {
-    private fun createSSLSocketFactory(): SSLSocketFactory {
-        val keyManagerFactory = KeyManagerFactory.getInstance("X509")
-        keyManagerFactory.init(keyStore, null)
+    /**
+     * Create PQC-compliant SSL Socket Factory with Hybrid Mode
+     * 创建支持 PQ 混合模式的 PQC 合规 SSL Socket Factory
+     */
+    private fun createSSLSocketFactory(): SSLSocketFactory {
+        // Use PQ hybrid KeyManager / 使用 PQ 混合 KeyManager
+        val keyManagerFactory = KeyManagerFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
+        keyManagerFactory.init(keyStore, null)
         val trustManagerFactory = TrustManagerFactory.getInstance(TLSUtils.TRUST_MANAGER_FACTORY)
         trustManagerFactory.init(keyStore)

-        val sslContext = SSLContext.getInstance("TLSv1.2")
+        // TLS 1.3 with PQ hybrid cipher suite / TLS 1.3 + PQ 混合密码套件
+        val sslContext = SSLContext.getInstance("TLSv1.3")
         sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, null)
         return sslContext.socketFactory
     }
        """.trimIndent()
    }

    /**
     * 处理取消扫描 / Handle scan cancellation
     */
    private fun handleCancelScan() {
        _keyMigrationState.update {
            it.copy(
                isScanning = false,
                scanPhase = ScanPhase.IDLE,
                progress = 0f
            )
        }
    }

    /**
     * 处理选择发现项 / Handle finding selection
     *
     * @param finding 选中的发现项 / Selected finding
     */
    private fun handleSelectFinding(finding: NonCompliantKeyFinding) {
        viewModelScope.launch {
            _effect.send(PQCMigrationEffect.ShowSuccess("已选择: ${finding.filePath}:${finding.lineNumber}"))
        }
    }

    /**
     * 处理导出迁移报告 / Handle migration report export
     */
    private fun handleExportMigrationReport() {
        viewModelScope.launch {
            _effect.send(PQCMigrationEffect.ShowSuccess("迁移报告已导出 / Migration report exported"))
            _effect.send(PQCMigrationEffect.ReportExported("/tmp/pqc_migration_report.md"))
        }
    }

    // ============================================================
    // TLSCompliance Handlers
    // ============================================================

    /**
     * 处理 TLS 输入路径设置 / Handle TLS input path setting
     *
     * @param path 输入路径 / Input path
     */
    private fun handleSetTLSInputPath(path: String) {
        _tlsComplianceState.update { it.copy(inputPath = path, error = null) }
    }

    /**
     * 处理 TLS 输入模式设置 / Handle TLS input mode setting
     *
     * @param mode 输入模式 / Input mode (APK / SOURCE_PATH)
     */
    private fun handleSetTLSInputMode(mode: String) {
        _tlsComplianceState.update { it.copy(inputMode = mode) }
    }

    /**
     * 开始 TLS 合规扫描 / Start TLS compliance scan
     *
     * Simulates TLS configuration analysis:
     * - Check TLS version (1.3 required for PQ hybrid)
     * - Check cipher suites (PQ hybrid suites)
     * - Check NetworkSecurityConfig
     */
    private fun handleStartTLSScan() {
        val path = _tlsComplianceState.value.inputPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(PQCMigrationEffect.ShowError("请输入 APK 或源码路径 / Please enter APK or source path"))
            }
            return
        }

        viewModelScope.launch {
            _tlsComplianceState.update {
                it.copy(
                    isScanning = true,
                    complianceLevel = TLSComplianceLevel.UNKNOWN,
                    complianceScore = null,
                    findings = emptyList(),
                    error = null
                )
            }

            // Simulate TLS analysis / 模拟 TLS 分析
            delay(2000)

            // Mock TLS findings / 模拟 TLS 发现项
            val mockFindings = listOf(
                TLSFinding(
                    id = "TLS-001",
                    domain = "api.example.com",
                    issue = "TLS 1.2 detected — not PQ compliant",
                    currentConfig = "TLSv1.2 with RSA cipher suites",
                    suggestedFix = "Upgrade to TLS 1.3 with PQ hybrid cipher suite (TLS_AES_256_GCM_SHA384 + ECDHE)",
                    severity = "HIGH"
                ),
                TLSFinding(
                    id = "TLS-002",
                    domain = "cdn.example.com",
                    issue = "No PQ hybrid mode configured",
                    currentConfig = "Standard TLS 1.3 without PQ hybrid",
                    suggestedFix = "Enable hybrid PQ mode: configure X25519+ML-KEM hybrid key exchange",
                    severity = "MEDIUM"
                )
            )

            _tlsComplianceState.update {
                it.copy(
                    isScanning = false,
                    complianceLevel = TLSComplianceLevel.PARTIAL,
                    complianceScore = 62,
                    findings = mockFindings
                )
            }
            _effect.send(PQCMigrationEffect.ScanComplete)
        }
    }

    /**
     * 处理导出 TLS 报告 / Handle TLS report export
     */
    private fun handleExportTLSReport() {
        viewModelScope.launch {
            _effect.send(PQCMigrationEffect.ShowSuccess("TLS 合规报告已导出 / TLS compliance report exported"))
        }
    }

    // ============================================================
    // AppSigning Handlers
    // ============================================================

    /**
     * 处理 APK 路径设置 / Handle APK path setting
     *
     * @param path APK 路径 / APK path
     */
    private fun handleSetApkPath(path: String) {
        _appSigningState.update { it.copy(apkPath = path, error = null) }
    }

    /**
     * 开始 App Signing 分析 / Start App Signing analysis
     *
     * Analyzes APK signature scheme:
     * - v1 (JAR signing) — legacy, not PQ compliant
     * - v2 (APK signature scheme v2) — not PQ compliant
     * - v3 (APK signature scheme v3) — not PQ compliant
     * - v4 (APK signature scheme v4) — supports PQ signatures
     */
    private fun handleAnalyzeAppSigning() {
        val path = _appSigningState.value.apkPath
        if (path.isBlank()) {
            viewModelScope.launch {
                _effect.send(PQCMigrationEffect.ShowError("请输入 APK 路径 / Please enter APK path"))
            }
            return
        }

        viewModelScope.launch {
            _appSigningState.update {
                it.copy(
                    isAnalyzing = true,
                    isPQCCompliant = null,
                    signatureScheme = null,
                    complianceDetails = null,
                    error = null
                )
            }

            // Simulate signing analysis / 模拟签名分析
            delay(1500)

            _appSigningState.update {
                it.copy(
                    isAnalyzing = false,
                    signatureScheme = "APK Signature Scheme v3",
                    signatureVersion = 3,
                    isPQCCompliant = false,
                    complianceDetails = """
                        |当前签名方案: APK Signature Scheme v3
                        |PQ 合规状态: ❌ 不合规
                        |
                        |Google Play PQ 要求:
                        |• 2025 年起: 推荐使用 v4 签名
                        |• 2029 年起: 必须支持 PQ 签名格式
                        |
                        |建议行动:
                        |1. 使用 apksigner 重新签名，启用 v4 方案
                        |2. 在 Google Play Console 中启用 PQ 签名支持
                        |3. 使用: apksigner sign --ks key.pk8 --v4-signature-file... 
                    """.trimMargin()
                )
            }
            _effect.send(PQCMigrationEffect.ShowSuccess("App Signing 分析完成 / App Signing analysis complete"))
        }
    }

    /**
     * 处理导出签名报告 / Handle signing report export
     */
    private fun handleExportSigningReport() {
        viewModelScope.launch {
            _effect.send(PQCMigrationEffect.ShowSuccess("签名报告已导出 / Signing report exported"))
        }
    }

    // ============================================================
    // Overview Handlers
    // ============================================================

    /**
     * 处理刷新概览 / Handle overview refresh
     */
    private fun handleRefreshOverview() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(500)
            _state.update {
                it.copy(
                    isLoading = false,
                    toolSummaries = getDefaultToolSummaries()
                )
            }
        }
    }

    /**
     * 处理扫描工具 / Handle tool scan
     *
     * @param module 要扫描的工具模块 / Tool module to scan
     */
    private fun handleScanTool(module: PQCToolModule) {
        _state.update { it.copy(selectedTab = module) }

        viewModelScope.launch {
            when (module) {
                PQCToolModule.KeyGen -> {
                    // Navigate to KeyGen tab / 导航到 KeyGen 标签
                }
                PQCToolModule.KeyMigration -> {
                    handleStartKeyMigrationScan()
                }
                PQCToolModule.TLSCompliance -> {
                    handleStartTLSScan()
                }
                PQCToolModule.AppSigning -> {
                    handleAnalyzeAppSigning()
                }
                PQCToolModule.Overview -> {
                    handleRefreshOverview()
                }
            }
        }
    }

    /**
     * 获取默认工具摘要列表 / Get default tool summaries list
     *
     * @return 工具摘要列表 / List of tool summaries
     */
    private fun getDefaultToolSummaries(): List<ToolComplianceSummary> {
        return listOf(
            ToolComplianceSummary(
                module = PQCToolModule.KeyGen,
                complianceScore = null,
                lastScanTime = null,
                status = "就绪"
            ),
            ToolComplianceSummary(
                module = PQCToolModule.KeyMigration,
                complianceScore = null,
                lastScanTime = null,
                status = "待检测"
            ),
            ToolComplianceSummary(
                module = PQCToolModule.TLSCompliance,
                complianceScore = null,
                lastScanTime = null,
                status = "待检测"
            ),
            ToolComplianceSummary(
                module = PQCToolModule.AppSigning,
                complianceScore = null,
                lastScanTime = null,
                status = "待检测"
            )
        )
    }
}
