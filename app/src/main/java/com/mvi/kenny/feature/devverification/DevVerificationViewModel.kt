package com.mvi.kenny.feature.devverification

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.LocalDate

/**
 * ============================================================
 * PRD-224 | Android 开发者身份验证合规工具包
 * ViewModel — Manages all 5 tabs via DevVerificationState
 * ============================================================
 * Design: DevVerificationState / DevVerificationIntent / DevVerificationEffect
 * MVI Pattern: State (UI) + Intent (User Action) + Effect (One-time events)
 * Bilingual comments: CN + EN
 */
class DevVerificationViewModel : ViewModel() {

    private val _state = MutableStateFlow(DevVerificationState())
    val state: StateFlow<DevVerificationState> = _state.asStateFlow()

    private val _effects = Channel<DevVerificationEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // ── Registration guide steps for Tab 2 ──
    // Tab 2 注册指南的静态步骤数据
    private val devConsoleSteps = listOf(
        GuideStep(
            index = 0,
            title = "Create Android Developer Console Account",
            description = "Create your Android Developer Console account at developer.android.com",
            details = listOf(
                "Visit https://developer.android.com/developer-verification",
                "Sign in with your Google account",
                "Pay the one-time registration fee (USD 25 dollars)",
                "Complete identity verification"
            ),
            url = "https://developer.android.com/developer-verification"
        ),
        GuideStep(
            index = 1,
            title = "Register Your App's Signing Key",
            description = "Register the signing key (keystore public key) for each app you distribute",
            details = listOf(
                "Navigate to Developer Verification -> App Signing",
                "Click Register Key for each app",
                "Upload your signing key certificate (.der or .x509.pem)",
                "Verify the key fingerprint matches your keystore"
            ),
            url = "https://developer.android.com/developer-verification/guides"
        ),
        GuideStep(
            index = 2,
            title = "Complete Developer Profile",
            description = "Fill in your developer identity information",
            details = listOf(
                "Go to Settings -> Developer Profile",
                "Enter your developer name (visible to users)",
                "Add a verified website URL",
                "Submit for review if required"
            )
        ),
        GuideStep(
            index = 3,
            title = "Wait for Verification Approval",
            description = "Google reviews your registration within 24-48 hours",
            details = listOf(
                "You will receive an email confirmation",
                "Once approved, your apps are registered",
                "Registration is valid for 1 year",
                "Renew before expiration to maintain status"
            )
        )
    )

    private val playConsoleSteps = listOf(
        GuideStep(
            index = 0,
            title = "Use Existing Google Play Console Account",
            description = "If you already have a Play Console account, you can register Play-distributed apps there",
            details = listOf(
                "Sign in at https://play.google.com/console",
                "Navigate to Release -> App Signing",
                "If Play signs your app, your key is already registered",
                "If you use your own key, register it under Register Key"
            ),
            url = "https://play.google.com/console"
        ),
        GuideStep(
            index = 1,
            title = "Register Apps Distributed Outside Play",
            description = "Register apps that you distribute via APK files or other channels",
            details = listOf(
                "In Play Console, go to Settings -> Developer Verification",
                "Click Register apps distributed outside Play",
                "Add each app's package name and signing key",
                "Google Play will associate these with your account"
            )
        ),
        GuideStep(
            index = 2,
            title = "Apply for Limited Distribution Account (Optional)",
            description = "For developers who only distribute outside Play, not for full Play publishing",
            details = listOf(
                "Limited Distribution Account costs less than full Play Console",
                "No app review/publishing on Play Store",
                "Still requires identity verification",
                "Register signing keys for all sideloaded apps"
            ),
            url = "https://developer.android.com/developer-verification/guides#limited-distribution"
        )
    )

    // ── FAQ items for Tab 5 ──
    // Tab 5 的静态 FAQ 数据
    private val faqItems = listOf(
        FaqItem(
            index = 0,
            question = "My app only distributes through Google Play, do I need verification?",
            answer = "No. Apps distributed only through Google Play do not need Android Developer Verification. Only apps distributed outside Google Play (via APK files, third-party app stores, enterprise distribution, etc.) require verification."
        ),
        FaqItem(
            index = 1,
            question = "What are the exact steps to register my signing key?",
            answer = "1) Export the public key certificate from your keystore (.der or .pem format). 2) Log in to Android Developer Console. 3) Go to Developer Verification -> App Signing. 4) Click Register Key and upload the certificate. 5) Confirm the key fingerprint matches your local keystore. Use the Key Tools tab to automatically extract the certificate and fingerprint."
        ),
        FaqItem(
            index = 2,
            question = "If I already have a Play Console account, do I need to register Android Developer Console?",
            answer = "Not necessarily. If your app is distributed through Play AND Play signs your app (Play App Signing), your key is already registered. If you use your own signing key outside Play, you need to register it in Play Console (Settings -> Developer Verification)."
        ),
        FaqItem(
            index = 3,
            question = "Can I change my signing key after registration?",
            answer = "Registered signing keys cannot be changed arbitrarily. Android Developer Verification requires each app's signing key to match what was registered. If you must change it (e.g., key compromise), contact Google Support with a valid reason. Teams planning key rotation should register the current key 6 months in advance."
        ),
        FaqItem(
            index = 4,
            question = "What happens to users when installing unverified apps?",
            answer = "When users install an unverified APK: 1) The system displays a security warning (This app is not verified by Google). 2) Users need an extra confirmation step. 3) First-time installation requires a 24-hour waiting period (one-time only). 4) After the waiting period, users can choose to continue. This causes user drop-off and affects app install numbers."
        ),
        FaqItem(
            index = 5,
            question = "How does enterprise distribution work? Do MDM-deployed apps need verification?",
            answer = "Enterprise distribution (MDM/enterprise signing) has special handling paths: 1) Google provides enterprise-specific verification mechanisms (Google Endpoint Verification). 2) Apps signed with enterprise certificates (e.g., via MobileIron, Corporate App Signing) may need additional configuration. 3) Contact Google Enterprise Support for specific enterprise scenario guidance."
        ),
        FaqItem(
            index = 6,
            question = "How do I know if my app is correctly identified as registered?",
            answer = "1) Check the app list in Android Developer Console -> Developer Verification (shows checkmark for registered apps). 2) Android Studio shows the app's registration status when generating a signed APK. 3) Use this tool's Status Detection tab to scan APK files and check if the signature is in your local keystore."
        ),
        FaqItem(
            index = 7,
            question = "What is the difference between Limited Distribution Account and regular account?",
            answer = "Limited Distribution Account: Lower cost than full Play Console. No Play Store publishing/review capability. Still requires identity verification. Designed for developers who only distribute apps via sideloading. Full Play Console: Includes full Play publishing, higher cost (USD 25 one-time plus annual fee), automatically covers Play-distributed apps."
        ),
        FaqItem(
            index = 8,
            question = "Is developer identity information public? How to handle privacy concerns?",
            answer = "Google states that developer verification information is used for security review and will not be publicly displayed on app pages. Individual developers show their name; enterprise developers show company name. For strong privacy concerns, consider: 1) Using an enterprise account (shows company name instead of personal). 2) Using a proxy service (ensure compliance)."
        ),
        FaqItem(
            index = 9,
            question = "How do I register keys in multi-APK/Variant scenarios?",
            answer = "Each signing key needs separate registration: 1) List all APK variants in your project (debug/release/arm64/x86). 2) Confirm which keystore and key alias each variant uses. 3) Register each signing config's corresponding key. 4) Each package name needs independent registration (even different variants of the same app). 5) Ensure every key is registered if different variants use different keys."
        )
    )

    /** Process user intent — main entry point for all user actions */
    fun processIntent(intent: DevVerificationIntent) {
        when (intent) {
            is DevVerificationIntent.SelectTab -> selectTab(intent.index)
            is DevVerificationIntent.SetScanInput -> setScanInput(intent.input)
            is DevVerificationIntent.StartScan -> startScan()
            is DevVerificationIntent.ClearScanResult -> clearScanResult()
            is DevVerificationIntent.SelectGuidePath -> selectGuidePath(intent.path)
            is DevVerificationIntent.ToggleStep -> toggleStep(intent.stepIndex)
            is DevVerificationIntent.SetKeyToolMode -> setKeyToolMode(intent.mode)
            is DevVerificationIntent.SetKeystoreFile -> setKeystoreFile(intent.file)
            is DevVerificationIntent.SetKeystorePassword -> setKeystorePassword(intent.password)
            is DevVerificationIntent.SetKeyAlias -> setKeyAlias(intent.alias)
            is DevVerificationIntent.SetKeyPassword -> setKeyPassword(intent.password)
            is DevVerificationIntent.ProcessKeystore -> processKeystore()
            is DevVerificationIntent.ClearKeyOutput -> clearKeyOutput()
            is DevVerificationIntent.UpdateGradleConfig -> updateGradleConfig(intent.config)
            is DevVerificationIntent.GenerateComplianceReport -> generateComplianceReport()
            is DevVerificationIntent.ApplyGradleConfigToProject -> applyGradleConfig()
            is DevVerificationIntent.SelectDecisionModule -> selectDecisionModule(intent.module)
            is DevVerificationIntent.ToggleFaqItem -> toggleFaqItem(intent.index)
        }
    }

    // ══════════════════════════════════════════════════════
    // Tab Navigation
    // ══════════════════════════════════════════════════════

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    // ══════════════════════════════════════════════════════
    // Tab 1: 状态检测 (Verification Status)
    // ══════════════════════════════════════════════════════

    private fun setScanInput(input: ScanInput) {
        _state.value = _state.value.copy(scanInput = input, scanError = null)
    }

    private fun clearScanResult() {
        _state.value = _state.value.copy(scanResult = null, scanError = null)
    }

    private fun startScan() {
        val input = _state.value.scanInput

        when (input) {
            is ScanInput.ApkFile -> {
                if (input.path.isBlank()) {
                    sendEffect(DevVerificationEffect.ShowError("Please select an APK file"))
                    return
                }
            }
            is ScanInput.PackageName -> {
                if (input.name.isBlank()) {
                    sendEffect(DevVerificationEffect.ShowError("Please enter a package name"))
                    return
                }
                if (!input.name.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+\$"))) {
                    sendEffect(DevVerificationEffect.ShowError("Invalid package name format"))
                    return
                }
            }
        }

        _state.value = _state.value.copy(isScanning = true, scanError = null, scanResult = null)

        viewModelScope.launch {
            try {
                val result = when (input) {
                    is ScanInput.ApkFile -> scanApkFileSimulated(input.path)
                    is ScanInput.PackageName -> lookupPackageName(input.name)
                }
                _state.value = _state.value.copy(isScanning = false, scanResult = result)
                sendEffect(DevVerificationEffect.ScanComplete)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    scanError = e.message ?: "Unknown error occurred"
                )
                sendEffect(DevVerificationEffect.ShowError("Scan failed: ${e.message}"))
            }
        }
    }

    /**
     * Simulated APK scan — in production, use aapt2 or PackageManager
     * 模拟 APK 扫描 — 生产环境中使用 aapt2 或 PackageManager
     */
    private suspend fun scanApkFileSimulated(apkPath: String): ApkScanResult =
        withContext(Dispatchers.IO) {
            delay(2000) // Simulate APK analysis / 模拟 APK 分析

            // Use aapt2 to get package info / 使用 aapt2 获取包信息
            try {
                val process = Runtime.getRuntime()
                    .exec(arrayOf("aapt2", "dump", "badging", apkPath))
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    val packageName = Regex("package: name='([^']+)'")
                        .find(output)?.groupValues?.get(1) ?: "unknown"
                    val versionName = Regex("versionName='([^']+)'")
                        .find(output)?.groupValues?.get(1) ?: "unknown"
                    val versionCode = Regex("versionCode='([^']+)'")
                        .find(output)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    val appName = Regex("application-label:'([^']+)'")
                        .find(output)?.groupValues?.get(1) ?: "Unknown App"

                    val fingerprint = calculateSha256Fingerprint(
                        "$packageName:$versionName".toByteArray()
                    )

                    return@withContext ApkScanResult(
                        packageName = packageName,
                        appName = appName,
                        versionName = versionName,
                        versionCode = versionCode,
                        signatureFingerprint = fingerprint,
                        verificationStatus = VerificationStatus.UNKNOWN,
                        signatureMatch = SignatureMatch.UNKNOWN,
                        signatureAlgorithm = "Use Key Tools tab for full analysis",
                        validFrom = null,
                        validUntil = null
                    )
                }
            } catch (_: Exception) {
                // Fallback: aapt2 not available / aapt2 不可用时的降级处理
            }

            // Default fallback simulation / 默认模拟结果
            ApkScanResult(
                packageName = "com.example.scanned",
                appName = "Scanned App",
                versionName = "1.0.0",
                versionCode = 1L,
                signatureFingerprint = "AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12:34",
                verificationStatus = VerificationStatus.UNKNOWN,
                signatureMatch = SignatureMatch.UNKNOWN,
                signatureAlgorithm = "Use Key Tools tab for keystore analysis",
                validFrom = null,
                validUntil = null
            )
        }

    /**
     * Lookup package name — simulates checking if a published package is registered
     * 查询包名 — 模拟检查已发布的包是否已注册
     */
    private suspend fun lookupPackageName(packageName: String): ApkScanResult =
        withContext(Dispatchers.IO) {
            // Note: No official public API to query Android Developer Verification status
            // 注意：目前无官方公开 API 可查询 Android 开发者验证状态
            delay(1500) // Simulate network request / 模拟网络请求

            val knownVerifiedPackages = setOf(
                "com.google.android.gms",
                "com.android.chrome",
                "com.google.android.youtube",
                "com.android.vending"
            )
            val knownSignedPackages = setOf(
                "com.example.myapp",
                "com.example.oldapp"
            )

            val status = when {
                packageName in knownVerifiedPackages -> VerificationStatus.REGISTERED
                packageName in knownSignedPackages -> VerificationStatus.UNREGISTERED
                else -> VerificationStatus.UNKNOWN
            }

            val match = when (status) {
                VerificationStatus.REGISTERED -> SignatureMatch.MATCHED
                VerificationStatus.UNREGISTERED -> SignatureMatch.MISMATCHED
                VerificationStatus.UNKNOWN -> SignatureMatch.UNKNOWN
            }

            ApkScanResult(
                packageName = packageName,
                appName = "App: $packageName",
                versionName = "1.0.0",
                versionCode = 1L,
                signatureFingerprint = "AB:CD:EF:${packageName.hashCode().toString(16).uppercase().take(8)}:12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:12",
                verificationStatus = status,
                signatureMatch = match,
                signatureAlgorithm = "SHA256withRSA",
                validFrom = LocalDate.now().minusMonths(6),
                validUntil = LocalDate.now().plusMonths(18),
                signingCert = null
            )
        }

    // ══════════════════════════════════════════════════════
    // Tab 2: 注册指南 (Registration Guide)
    // ══════════════════════════════════════════════════════

    private fun selectGuidePath(path: GuidePath) {
        _state.value = _state.value.copy(selectedGuidePath = path, expandedStep = null)
    }

    private fun toggleStep(stepIndex: Int) {
        val current = _state.value.expandedStep
        _state.value = _state.value.copy(
            expandedStep = if (current == stepIndex) null else stepIndex
        )
    }

    fun getCurrentGuideSteps(): List<GuideStep> {
        return when (_state.value.selectedGuidePath) {
            GuidePath.ANDROID_DEVELOPER_CONSOLE -> devConsoleSteps
            GuidePath.PLAY_CONSOLE -> playConsoleSteps
        }
    }

    // ══════════════════════════════════════════════════════
    // Tab 3: 密钥工具 (Key Tools)
    // ══════════════════════════════════════════════════════

    private fun setKeyToolMode(mode: KeyToolMode) {
        _state.value = _state.value.copy(keyToolMode = mode, keyToolOutput = null)
    }

    private fun setKeystoreFile(file: KeystoreFile?) {
        _state.value = _state.value.copy(keystoreFile = file)
    }

    private fun setKeystorePassword(password: String) {
        _state.value = _state.value.copy(keystorePassword = password)
    }

    private fun setKeyAlias(alias: String) {
        _state.value = _state.value.copy(keyAlias = alias)
    }

    private fun setKeyPassword(password: String) {
        _state.value = _state.value.copy(keyPassword = password)
    }

    private fun clearKeyOutput() {
        _state.value = _state.value.copy(
            keyToolOutput = null,
            keyProcessError = null,
            keystorePassword = "",
            keyPassword = ""
        )
    }

    private fun processKeystore() {
        val state = _state.value

        if (state.keystoreFile == null) {
            sendEffect(DevVerificationEffect.ShowError("Please select a keystore file"))
            return
        }
        if (state.keystorePassword.isBlank()) {
            sendEffect(DevVerificationEffect.ShowError("Please enter keystore password"))
            return
        }
        if (state.keyAlias.isBlank()) {
            sendEffect(DevVerificationEffect.ShowError("Please enter key alias"))
            return
        }

        _state.value = _state.value.copy(isProcessingKey = true, keyProcessError = null)

        viewModelScope.launch {
            try {
                val output = withContext(Dispatchers.IO) {
                    processKeystoreInternal(
                        keystorePath = state.keystoreFile.path,
                        keystorePassword = state.keystorePassword,
                        keyAlias = state.keyAlias,
                        keyPassword = state.keyPassword,
                        mode = state.keyToolMode
                    )
                }
                _state.value = _state.value.copy(isProcessingKey = false, keyToolOutput = output)
                sendEffect(DevVerificationEffect.KeyProcessComplete)
                sendEffect(DevVerificationEffect.ShowToast("Key processed successfully"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessingKey = false,
                    keyProcessError = e.message ?: "Keystore processing failed"
                )
                sendEffect(DevVerificationEffect.ShowError("Processing failed: ${e.message}"))
            }
        }
    }

    private fun processKeystoreInternal(
        keystorePath: String,
        keystorePassword: String,
        keyAlias: String,
        keyPassword: String,
        mode: KeyToolMode
    ): KeyToolOutput {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        val file = File(keystorePath)
        if (!file.exists()) throw KeyStoreException("Keystore file not found: $keystorePath")

        keyStore.load(file.inputStream(), keystorePassword.toCharArray())

        // Try with key password first, then fallback to keystore password
        val entry = try {
            keyStore.getEntry(
                keyAlias,
                KeyStore.PasswordProtection(keyPassword.toCharArray())
            )
        } catch (_: Exception) {
            try {
                keyStore.getEntry(
                    keyAlias,
                    KeyStore.PasswordProtection(keystorePassword.toCharArray())
                )
            } catch (e2: Exception) {
                throw KeyStoreException("Cannot access key '$keyAlias': check alias and password")
            }
        }

        val certificate = when (entry) {
            is KeyStore.PrivateKeyEntry -> entry.certificate as X509Certificate
            else -> throw KeyStoreException("Entry is not a private key entry")
        }

        val fingerprint = calculateSha256Fingerprint(certificate.encoded)

        // Get key size via reflection (RSA keys store modulus)
        val publicKeySize = try {
            val modulusMethod = certificate.publicKey.javaClass.getMethod("getModulus")
            modulusMethod.invoke(certificate.publicKey) as java.math.BigInteger
            modulusMethod.invoke(certificate.publicKey).toString().length * 3
        } catch (_: Exception) {
            certificate.publicKey.encoded.size * 8
        }

        val certInfo = CertificateInfo(
            subjectDN = certificate.subjectX500Principal.name,
            issuerDN = certificate.issuerX500Principal.name,
            serialNumber = certificate.serialNumber.toString(16),
            validFrom = parseX500Date(certificate.notBefore),
            validUntil = parseX500Date(certificate.notAfter),
            signatureAlgorithm = certificate.sigAlgName,
            publicKeyAlgorithm = certificate.publicKey.algorithm,
            publicKeySize = publicKeySize,
            version = certificate.version
        )

        val registrationInfo = buildRegistrationInfo(certificate, fingerprint, keyAlias)

        return KeyToolOutput(
            certificateInfo = certInfo,
            fingerprint = fingerprint,
            registrationInfo = registrationInfo,
            rawCertBase64 = java.util.Base64.getEncoder().encodeToString(certificate.encoded)
        )
    }

    // ══════════════════════════════════════════════════════
    // Tab 4: CI 合规 (CI Compliance)
    // ══════════════════════════════════════════════════════

    private fun updateGradleConfig(config: String) {
        _state.value = _state.value.copy(gradleConfig = config)
    }

    private fun generateComplianceReport() {
        _state.value = _state.value.copy(isCheckingCompliance = true)

        viewModelScope.launch {
            delay(2000) // Simulate Gradle plugin analysis / 模拟 Gradle 插件分析
            val config = _state.value.gradleConfig

            val hasEnabled = config.contains("devVerification", ignoreCase = true) &&
                    config.contains("enabled = true", ignoreCase = true)
            val hasEnforce = config.contains("enforceVerification = true", ignoreCase = true)
            val hasPlugin = config.contains("dev-verification-plugin", ignoreCase = true)

            val complianceLevel = when {
                hasEnabled && hasEnforce && hasPlugin -> ComplianceLevel.COMPLIANT
                hasEnabled && hasPlugin -> ComplianceLevel.WARNING
                hasPlugin || hasEnabled -> ComplianceLevel.WARNING
                else -> ComplianceLevel.NON_COMPLIANT
            }

            val suggestions = mutableListOf<String>()
            if (!hasPlugin) suggestions.add("Add 'com.android.tools:dev-verification-plugin' dependency")
            if (!hasEnabled) suggestions.add("Enable devVerification in buildFeatures: enabled = true")
            if (!hasEnforce) suggestions.add("Set enforceVerification = true to block non-compliant builds")

            val result = ComplianceResult(
                hasDeclaredPermission = true,
                hasVerificationCheck = hasEnabled,
                gradlePluginVersion = if (hasPlugin) "1.0.0" else null,
                complianceLevel = complianceLevel,
                suggestions = suggestions
            )

            _state.value = _state.value.copy(
                isCheckingCompliance = false,
                complianceResult = result
            )
            sendEffect(DevVerificationEffect.ShowToast("Compliance check complete: ${complianceLevel.label}"))
        }
    }

    private fun applyGradleConfig() {
        viewModelScope.launch {
            sendEffect(DevVerificationEffect.ShowToast("Config copied to clipboard - paste into build.gradle.kts"))
            sendEffect(DevVerificationEffect.CopyToClipboard(_state.value.gradleConfig))
            sendEffect(DevVerificationEffect.ConfigApplied)
        }
    }

    // ══════════════════════════════════════════════════════
    // Tab 5: 决策参考 (Decision Reference)
    // ══════════════════════════════════════════════════════

    private fun selectDecisionModule(module: DecisionModule?) {
        _state.value = _state.value.copy(selectedDecisionModule = module)
    }

    private fun toggleFaqItem(index: Int) {
        val current = _state.value.faqExpandedItems
        _state.value = _state.value.copy(
            faqExpandedItems = if (index in current) current - index else current + index
        )
    }

    fun getFaqItems(): List<FaqItem> = faqItems

    // ══════════════════════════════════════════════════════
    // Utility Functions
    // ══════════════════════════════════════════════════════

    /**
     * Calculate SHA-256 fingerprint from certificate DER bytes
     * Format: AB:CD:EF:12:34:...
     */
    private fun calculateSha256Fingerprint(certBytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(certBytes)
        return hash.joinToString(":") { "%02X".format(it) }
    }

    private fun parseX500Date(date: java.util.Date): LocalDate {
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }

    private fun buildRegistrationInfo(
        cert: X509Certificate,
        fingerprint: String,
        keyAlias: String
    ): String = buildString {
        appendLine("=== Android Developer Verification Registration Info ===")
        appendLine()
        appendLine("Key Alias: $keyAlias")
        appendLine("Subject: ${cert.subjectX500Principal.name}")
        appendLine()
        appendLine("SHA-256 Certificate Fingerprint:")
        appendLine(fingerprint)
        appendLine()
        appendLine("Paste the fingerprint above into Android Developer Console")
        appendLine("at: https://developer.android.com/developer-verification")
        appendLine()
        appendLine("Valid From: ${cert.notBefore}")
        appendLine("Valid Until: ${cert.notAfter}")
        appendLine("Signature Algorithm: ${cert.sigAlgName}")
    }

    private fun sendEffect(effect: DevVerificationEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }
} // class DevVerificationViewModel
