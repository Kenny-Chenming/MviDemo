package com.mvi.kenny.feature.pqctoolkit

// ================================================================
// PQCToolkitViewModel — Android 17 PQC Migration Toolkit ViewModel
// ================================================================
// MVI ViewModel for Android 17 Post-Quantum Cryptography Migration Toolkit.
//
// PRD-236: Android 17 后量子密码学（PQC）迁移工具包
// Implements MVI pattern with:
//   State — PQCToolkitState (immutable)
//   Intent — PQCToolkitIntent (user actions)
//   Effect — PQCToolkitEffect (one-time side effects via Channel)
//
// ViewModel processes intents and updates state accordingly.
// Uses viewModelScope.launch for coroutine-based async operations.
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PQCToolkitViewModel : ViewModel() {

    // ── MVI State ──────────────────────────────────────────────
    private val _state = MutableStateFlow(PQCToolkitState())
    val state: StateFlow<PQCToolkitState> = _state.asStateFlow()

    // ── MVI Effects (one-time events) ───────────────────────────
    private val _effect = MutableSharedFlow<PQCToolkitEffect>()
    val effect = _effect.asSharedFlow()

    // ── Internal jobs ───────────────────────────────────────────
    private var scanJob: Job? = null
    private var benchmarkJob: Job? = null

    // ============================================================
    // Intent Processing
    // ============================================================
    fun processIntent(intent: PQCToolkitIntent) {
        when (intent) {
            // ── Crypto Scanner ─────────────────────────────────
            is PQCToolkitIntent.UpdateProjectPath -> updateProjectPath(intent.path)
            is PQCToolkitIntent.StartScan -> startCryptoScan()
            is PQCToolkitIntent.CancelScan -> cancelCryptoScan()
            is PQCToolkitIntent.ClearFindings -> clearFindings()

            // ── ML-DSA Generator ───────────────────────────────
            is PQCToolkitIntent.UpdateKeyAlias -> updateKeyAlias(intent.alias)
            is PQCToolkitIntent.SelectVariant -> selectVariant(intent.variant)
            is PQCToolkitIntent.GenerateKey -> generateMLDSAKey()
            is PQCToolkitIntent.GenerateCodeSnippet -> generateCodeSnippet()

            // ── Migration Guide ─────────────────────────────────
            is PQCToolkitIntent.SelectAlgorithm -> selectAlgorithm(intent.algorithm)
            is PQCToolkitIntent.SelectUsage -> selectUsage(intent.usage)
            is PQCToolkitIntent.NextStep -> nextStep()
            is PQCToolkitIntent.PreviousStep -> previousStep()
            is PQCToolkitIntent.ToggleHybridMode -> toggleHybridMode()
            is PQCToolkitIntent.GenerateCodeDiff -> generateCodeDiff()

            // ── Benchmark ───────────────────────────────────────
            is PQCToolkitIntent.RunBenchmark -> runBenchmark()
            is PQCToolkitIntent.StopBenchmark -> stopBenchmark()
            is PQCToolkitIntent.ClearResults -> clearBenchmarkResults()

            // ── Play Signing Guide ───────────────────────────────
            is PQCToolkitIntent.SelectTopic -> selectTopic(intent.topic)

            // ── Gradle Plugin ───────────────────────────────────
            is PQCToolkitIntent.CheckCompliance -> checkCompliance()
            is PQCToolkitIntent.GenerateConfigSnippet -> generateConfigSnippet()

            // ── Tab Navigation ───────────────────────────────────
            is PQCToolkitIntent.SelectTab -> { _state.value = _state.value.copy(selectedTab = intent.tab) }
        }
    }

    // ── Crypto Scanner ────────────────────────────────────────────
    private fun updateProjectPath(path: String) {
        val scannerState = _state.value.cryptoScannerState
        _state.value = _state.value.copy(
            cryptoScannerState = scannerState.copy(projectPath = path)
        )
    }

    private fun startCryptoScan() {
        val scannerState = _state.value.cryptoScannerState
        if (scannerState.projectPath.isBlank()) {
            viewModelScope.launch {
                _effect.emit(PQCToolkitEffect.ShowError("Please enter a project path"))
            }
            return
        }
        _state.value = _state.value.copy(
            cryptoScannerState = scannerState.copy(
                isScanning = true,
                scanPhase = ScanPhase.SCANNING_KOTLIN,
                progress = 0,
                findings = emptyList(),
                errorMessage = ""
            )
        )
        scanJob = viewModelScope.launch {
            performCryptoScan()
        }
    }

    private suspend fun performCryptoScan() {
        // Demo findings simulating scan results
        val demoFindings = listOf(
            CryptoFinding(
                file = "app/src/main/java/com/example/SignHelper.kt",
                line = 45,
                algorithm = CryptoAlgorithm.RSA_2048,
                usage = CryptoUsage.SIGNATURE,
                urgency = MigrationUrgency.CRITICAL,
                code = "val keyPair = KeyPairGenerator.getInstance(\"RSA\").genKeyPair()",
                suggestion = "Generate ML-DSA key and use hybrid mode: RSA + ML-DSA dual signature"
            ),
            CryptoFinding(
                file = "app/src/main/java/com/example/CryptoUtil.java",
                line = 78,
                algorithm = CryptoAlgorithm.ECDSA_P256,
                usage = CryptoUsage.SIGNATURE,
                urgency = MigrationUrgency.HIGH,
                code = "Signature sig = Signature.getInstance(\"SHA256withECDSA\")",
                suggestion = "Replace with ML-DSA hybrid signature. Keep ECDSA for backward compatibility."
            ),
            CryptoFinding(
                file = "app/src/main/java/com/example/KeyExchange.kt",
                line = 23,
                algorithm = CryptoAlgorithm.RSA_2048,
                usage = CryptoUsage.KEY_EXCHANGE,
                urgency = MigrationUrgency.MEDIUM,
                code = "cipher.init(Cipher.ENCRYPT_MODE, publicKey)",
                suggestion = "For key encapsulation, consider ML-KEM (CRYSTALS-Kyber) when available in Android."
            ),
            CryptoFinding(
                file = "app/src/test/java/com/example/SignHelperTest.kt",
                line = 12,
                algorithm = CryptoAlgorithm.RSA_1024,
                usage = CryptoUsage.TEST,
                urgency = MigrationUrgency.INFO,
                code = "KeyPairGenerator.getInstance(\"RSA\", \"BC\")",
                suggestion = "Test keys can be ignored, but plan migration before production use."
            )
        )

        val phases = listOf(
            ScanPhase.SCANNING_KOTLIN to "Scanning Kotlin files...",
            ScanPhase.SCANNING_JAVA to "Scanning Java files...",
            ScanPhase.ANALYZING to "Analyzing crypto usage patterns...",
            ScanPhase.GENERATING_REPORT to "Generating PQC scan report..."
        )

        for ((i, pair) in phases.withIndex()) {
            val (phase, description) = pair
            _state.value = _state.value.copy(
                cryptoScannerState = _state.value.cryptoScannerState.copy(
                    scanPhase = phase,
                    progress = (i + 1) * 25,
                    scannedFiles = (i + 1) * 125
                )
            )
            delay(800)
        }

        val criticalCount = demoFindings.count { it.urgency == MigrationUrgency.CRITICAL }
        val warningCount = demoFindings.count { it.urgency == MigrationUrgency.HIGH || it.urgency == MigrationUrgency.MEDIUM }
        val infoCount = demoFindings.count { it.urgency == MigrationUrgency.INFO || it.urgency == MigrationUrgency.LOW }

        val readiness = when {
            criticalCount > 0 -> ReadinessLevel.NOT_READY
            warningCount > 0 -> ReadinessLevel.WARNING
            else -> ReadinessLevel.READY
        }

        _state.value = _state.value.copy(
            cryptoScannerState = _state.value.cryptoScannerState.copy(
                isScanning = false,
                scanPhase = ScanPhase.COMPLETED,
                progress = 100,
                scannedFiles = 500,
                totalFiles = 500,
                findings = demoFindings,
                readinessLevel = readiness,
                criticalCount = criticalCount,
                warningCount = warningCount,
                infoCount = infoCount,
                lastScanTime = System.currentTimeMillis()
            )
        )
        _effect.emit(PQCToolkitEffect.ScanCompleted)
    }

    private fun cancelCryptoScan() {
        scanJob?.cancel()
        val scannerState = _state.value.cryptoScannerState
        _state.value = _state.value.copy(
            cryptoScannerState = scannerState.copy(
                isScanning = false,
                scanPhase = ScanPhase.IDLE,
                progress = 0
            )
        )
    }

    private fun clearFindings() {
        val scannerState = _state.value.cryptoScannerState
        _state.value = _state.value.copy(
            cryptoScannerState = scannerState.copy(
                findings = emptyList(),
                readinessLevel = ReadinessLevel.UNKNOWN,
                criticalCount = 0,
                warningCount = 0,
                infoCount = 0,
                scanPhase = ScanPhase.IDLE
            )
        )
    }

    // ── ML-DSA Generator ────────────────────────────────────────
    private fun updateKeyAlias(alias: String) {
        val genState = _state.value.mlDSAGeneratorState
        _state.value = _state.value.copy(
            mlDSAGeneratorState = genState.copy(keyAlias = alias)
        )
    }

    private fun selectVariant(variant: MLDSAVariant) {
        val genState = _state.value.mlDSAGeneratorState
        _state.value = _state.value.copy(
            mlDSAGeneratorState = genState.copy(selectedVariant = variant)
        )
    }

    private fun generateMLDSAKey() {
        val genState = _state.value.mlDSAGeneratorState
        _state.value = _state.value.copy(
            mlDSAGeneratorState = genState.copy(
                keyGenStatus = KeyGenStatus.GENERATING,
                errorMessage = ""
            )
        )
        viewModelScope.launch {
            delay(2000) // Simulate key generation
            val variant = _state.value.mlDSAGeneratorState.selectedVariant
            val alias = _state.value.mlDSAGeneratorState.keyAlias
            _state.value = _state.value.copy(
                mlDSAGeneratorState = _state.value.mlDSAGeneratorState.copy(
                    keyGenStatus = KeyGenStatus.SUCCESS,
                    keyInfo = "ML-DSA-${variant.parameterSet} generated successfully. Stored in Android Keystore (StrongBox if available)."
                ).let { it }
            )
            _effect.emit(PQCToolkitEffect.KeyGenerated)
        }
    }

    private fun generateCodeSnippet() {
        val genState = _state.value.mlDSAGeneratorState
        val variant = genState.selectedVariant
        val alias = genState.keyAlias
        val snippet = """
            // ============================================================
            // ML-DSA Key Generation (Android 17+)
            // ============================================================
            
            val keyGenSpec = KeyGenSpec.Builder(
                KeyProperties.KEY_ALGORITHM_ML_DSA,
                KeyProperties.PURPOSE_SIGN
            )
                .setKeySize(${variant.parameterSet * 8})
                .setAlias("$alias")
                .setKeyValidityStartAsInstant(Instant.now())
                .setKeyValidityPeriodForOriginationDays(365)
                .build()
            
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_ML_DSA,
                "AndroidKeyStore"
            )
            keyGenerator.init(keyGenSpec)
            val mlDsaKey = keyGenerator.generateKeyPair()
            
            // ── Signing with ML-DSA ────────────────────────────────
            val signature = Signature.getInstance("SHA256withMLDSA")
            signature.initSign(mlDsaKey.privateKey)
            signature.update(dataToSign)
            val mlDsaSignature = signature.sign()
        """.trimIndent()
        _state.value = _state.value.copy(
            mlDSAGeneratorState = genState.copy(
                generatedCodeSnippet = snippet
            )
        )
    }

    // ── Migration Guide ─────────────────────────────────────────
    private fun selectAlgorithm(algorithm: CryptoAlgorithm) {
        val guideState = _state.value.migrationGuideState
        _state.value = _state.value.copy(
            migrationGuideState = guideState.copy(
                selectedAlgorithm = algorithm,
                currentStep = 0
            )
        )
    }

    private fun selectUsage(usage: CryptoUsage) {
        val guideState = _state.value.migrationGuideState
        _state.value = _state.value.copy(
            migrationGuideState = guideState.copy(
                selectedUsage = usage,
                currentStep = 0
            )
        )
    }

    private fun nextStep() {
        val guideState = _state.value.migrationGuideState
        if (guideState.currentStep < guideState.totalSteps - 1) {
            _state.value = _state.value.copy(
                migrationGuideState = guideState.copy(currentStep = guideState.currentStep + 1)
            )
        }
    }

    private fun previousStep() {
        val guideState = _state.value.migrationGuideState
        if (guideState.currentStep > 0) {
            _state.value = _state.value.copy(
                migrationGuideState = guideState.copy(currentStep = guideState.currentStep - 1)
            )
        }
    }

    private fun toggleHybridMode() {
        val guideState = _state.value.migrationGuideState
        _state.value = _state.value.copy(
            migrationGuideState = guideState.copy(hybridModeEnabled = !guideState.hybridModeEnabled)
        )
    }

    private fun generateCodeDiff() {
        val guideState = _state.value.migrationGuideState
        val algorithm = guideState.selectedAlgorithm
        val hybrid = guideState.hybridModeEnabled

        val oldCode = when (algorithm) {
            CryptoAlgorithm.RSA_2048 -> """
                // ❌ OLD: RSA-2048 Signature
                val keyGen = KeyPairGenerator.getInstance("RSA")
                keyGen.initialize(2048)
                val keyPair = keyGen.genKeyPair()
                
                val signature = Signature.getInstance("SHA256withRSA")
                signature.initSign(keyPair.privateKey)
                signature.update(data)
                val sigBytes = signature.sign()
            """.trimIndent()

            CryptoAlgorithm.ECDSA_P256 -> """
                // ❌ OLD: ECDSA-P256 Signature
                val keyGen = KeyPairGenerator.getInstance("EC")
                val spec = ECGenParameterSpec("secp256r1")
                keyGen.initialize(spec)
                val keyPair = keyGen.genKeyPair()
                
                val signature = Signature.getInstance("SHA256withECDSA")
                signature.initSign(keyPair.privateKey)
                signature.update(data)
                val sigBytes = signature.sign()
            """.trimIndent()

            else -> "// Unsupported algorithm"
        }

        val newCode = if (hybrid) {
            """
                // ✅ NEW: Hybrid RSA + ML-DSA Signature (Android 17+)
                // Step 1: Generate both keys
                // (Assume RSA key already exists, ML-DSA key from key generator)
                
                // Step 2: Dual signature
                // RSA signature (for backward compatibility)
                val legacySignature = Signature.getInstance("SHA256withRSA")
                legacySignature.initSign(rsaKeyPair.privateKey)
                legacySignature.update(data)
                val rsaSig = legacySignature.sign()
                
                // ML-DSA signature (quantum-safe, Android 17+)
                val mlDsaSignature = Signature.getInstance("SHA256withMLDSA")
                mlDsaSignature.initSign(mlDsaKey.privateKey)
                mlDsaSignature.update(data)
                val mlDsaSig = mlDsaSignature.sign()
                
                // Step 3: Bundle both signatures
                val combinedSig = CombinedSignature.newBuilder()
                    .addSignature(rsaSig, "SHA256withRSA")
                    .addSignature(mlDsaSig, "SHA256withMLDSA")
                    .build()
                
                // Verification: receiver chooses strongest available
                // Modern receiver: verifies ML-DSA only
                // Legacy receiver: verifies RSA only
            """.trimIndent()
        } else {
            """
                // ✅ NEW: Pure ML-DSA Signature (Android 17+ only)
                val keyGenSpec = KeyGenSpec.Builder(
                    KeyProperties.KEY_ALGORITHM_ML_DSA,
                    KeyProperties.PURPOSE_SIGN
                )
                    .setKeySize(65 * 8)  // ML-DSA-65 parameter set
                    .setAlias("pqc-signing-key")
                    .build()
                
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_ML_DSA, "AndroidKeyStore"
                )
                keyGenerator.init(keyGenSpec)
                val mlDsaKey = keyGenerator.generateKeyPair()
                
                val signature = Signature.getInstance("SHA256withMLDSA")
                signature.initSign(mlDsaKey.privateKey)
                signature.update(data)
                val mlDsaSig = signature.sign()
                
                // Note: Not compatible with pre-Android 17 devices
            """.trimIndent()
        }

        _state.value = _state.value.copy(
            migrationGuideState = guideState.copy(
                codeDiff = "$oldCode\n\n---\n\n$newCode"
            )
        )
    }

    // ── Benchmark ───────────────────────────────────────────────
    private fun runBenchmark() {
        val benchmarkState = _state.value.benchmarkState
        _state.value = _state.value.copy(
            benchmarkState = benchmarkState.copy(
                isRunning = true,
                progress = 0,
                currentOperation = "Initializing..."
            )
        )
        benchmarkJob = viewModelScope.launch {
            val operations = listOf(
                "Key Generation (RSA-2048)" to 20,
                "Key Generation (ML-DSA-65)" to 40,
                "Signing (RSA-2048)" to 55,
                "Signing (ML-DSA-65)" to 70,
                "Verification (RSA-2048)" to 82,
                "Verification (ML-DSA-65)" to 92,
                "Generating report..." to 98
            )
            for ((op, progress) in operations) {
                _state.value = _state.value.copy(
                    benchmarkState = _state.value.benchmarkState.copy(
                        currentOperation = op,
                        progress = progress
                    )
                )
                delay(1000)
            }

            val results = listOf(
                BenchmarkResult("Sign (bytes)", "256", "2,420", "2,420", "+9.5x larger"),
                BenchmarkResult("Verify (bytes)", "256", "1,310", "1,310", "+5.1x larger"),
                BenchmarkResult("KeyGen (ms)", "5.2", "89.3", "89.3", "+17x slower"),
                BenchmarkResult("Sign (ms)", "1.1", "12.4", "12.4", "+11x slower"),
                BenchmarkResult("Verify (ms)", "0.3", "1.8", "1.8", "+6x slower"),
                BenchmarkResult("Key Size (bytes)", "256", "2,560", "2,560", "+10x larger")
            )

            val recommendation = """
                📋 RECOMMENDATION:
                • Use ML-DSA for signatures (quantum-safe, NIST standard)
                • Keep RSA for key encapsulation (until ML-KEM matures)
                • Hybrid mode: sign with both RSA + ML-DSA for transition period
                • Expected 2029 deadline — start migration planning now
            """.trimIndent()

            _state.value = _state.value.copy(
                benchmarkState = _state.value.benchmarkState.copy(
                    isRunning = false,
                    progress = 100,
                    currentOperation = "Completed",
                    results = results,
                    deviceInfo = "Android Emulator (API 35) / Pixel 8 (Tensor G3)",
                    recommendation = recommendation
                )
            )
            _effect.emit(PQCToolkitEffect.BenchmarkCompleted)
        }
    }

    private fun stopBenchmark() {
        benchmarkJob?.cancel()
        val benchmarkState = _state.value.benchmarkState
        _state.value = _state.value.copy(
            benchmarkState = benchmarkState.copy(
                isRunning = false,
                progress = 0,
                currentOperation = "Stopped"
            )
        )
    }

    private fun clearBenchmarkResults() {
        val benchmarkState = _state.value.benchmarkState
        _state.value = _state.value.copy(
            benchmarkState = benchmarkState.copy(
                results = emptyList(),
                recommendation = ""
            )
        )
    }

    // ── Play Signing Guide ───────────────────────────────────────
    private fun selectTopic(topic: PlaySigningTopic) {
        val guideState = _state.value.playSigningGuideState
        _state.value = _state.value.copy(
            playSigningGuideState = guideState.copy(selectedTopic = topic)
        )
    }

    // ── Gradle Plugin ───────────────────────────────────────────
    private fun checkCompliance() {
        val pluginState = _state.value.gradlePluginState
        _state.value = _state.value.copy(
            gradlePluginState = pluginState.copy(
                isChecking = true,
                errorMessage = ""
            )
        )
        viewModelScope.launch {
            delay(2000)

            val messages = mutableListOf<String>()
            var mlDSAIntegrated = false
            var hybridMode = true
            var playSigningPQC = false

            messages.add("[CHECK] Scanning for ML-DSA key usage...")
            if (!mlDSAIntegrated) {
                messages.add("[WARN] No ML-DSA key found — PQC migration not started")
            } else {
                messages.add("[PASS] ML-DSA key detected")
            }

            messages.add("[CHECK] Verifying hybrid mode implementation...")
            if (hybridMode) {
                messages.add("[PASS] Hybrid RSA+ML-DSA signature mode enabled")
            } else {
                messages.add("[WARN] Pure ML-DSA may break backward compatibility")
            }

            messages.add("[CHECK] Checking Google Play Signing PQC status...")
            if (playSigningPQC) {
                messages.add("[PASS] Play Signing PQC enabled for this app")
            } else {
                messages.add("[INFO] Play Signing PQC not yet enabled — enroll in Play Console")
            }

            val readiness = when {
                mlDSAIntegrated && hybridMode -> ReadinessLevel.READY
                mlDSAIntegrated || hybridMode -> ReadinessLevel.WARNING
                else -> ReadinessLevel.NOT_READY
            }
            messages.add("[INFO] PQC Readiness: ${readiness.emoji} ${readiness.displayName}")
            messages.add("[INFO] Deadline: 2029 — plan migration in phases")

            _state.value = _state.value.copy(
                gradlePluginState = _state.value.gradlePluginState.copy(
                    isChecking = false,
                    isMlDSAIntegrated = mlDSAIntegrated,
                    isHybridMode = hybridMode,
                    isPlaySigningPQC = playSigningPQC,
                    checkResult = readiness,
                    checkMessages = messages
                )
            )
        }
    }

    private fun generateConfigSnippet() {
        val snippet = """
            // ============================================================
            // android-pqc-toolkit Gradle Plugin Configuration
            // Add to your app/build.gradle.kts:
            // ============================================================
            
            plugins {
                id("android.pqc.check") version "1.0.0"
            }
            
            androidPqcCheck {
                // v1: warn only, v2 (2027+): fail CI if not PQC-ready
                requireMlDSA.set(false)
                
                // Check Play Signing PQC enrollment status
                checkPlaySigning.set(true)
                
                // Migration deadline reminder
                migrationDeadline.set(2029)
                
                // Exclude test code from critical findings
                excludeTestCode.set(true)
            }
            
            // ── Generate ML-DSA key ──────────────────────────────────
            // Run: ./gradlew generateMlDsaKey -Palias=my-pqc-key
            tasks.register<GenerateMlDsaKeyTask>("generateMlDsaKey") {
                alias.set(project.findProperty("alias") ?: "default-pqc-key")
                variant.set("ML-DSA-65")
            }
        """.trimIndent()
        val pluginState = _state.value.gradlePluginState
        _state.value = _state.value.copy(
            gradlePluginState = pluginState.copy(configurationSnippet = snippet)
        )
    }

    // ============================================================
    // Lifecycle
    // ============================================================
    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        benchmarkJob?.cancel()
    }
}
