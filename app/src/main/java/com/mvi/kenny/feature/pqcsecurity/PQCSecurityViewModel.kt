package com.mvi.kenny.feature.pqcsecurity

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * ============================================================
 * PRD-120 | Android 17 Post-Quantum Cryptography App Signing
 * Migration Detection & Quantum Security Toolkit
 * ============================================================
 * ViewModel — Implements MVI pattern
 *
 * Design: memory/agency/designs/PRD-120-Android-17-Post-Quantum-Cryptography-...
 * State: Single source of truth managed via MutableStateFlow
 * Intent: User actions processed via processIntent()
 * Effect: One-time side effects delivered via Channel
 *
 * MVI 架构 — State 通过 MutableStateFlow 管理, Intent 通过 processIntent() 处理,
 * Effect 通过 Channel 传递一次性副作用
 *
 * This toolkit helps Android developers analyze APK/AAB signatures,
 * understand PQC standards, configure quantum-safe Play App Signing,
 * and track migration progress.
 * 本工具包帮助 Android 开发者分析 APK/AAB 签名、理解 PQC 标准、
 * 配置量子安全的 Play App Signing 并追踪迁移进度。
 */
class PQCSecurityViewModel : ViewModel() {

    private val _state = MutableStateFlow(PQCSecurityState())
    val state: StateFlow<PQCSecurityState> = _state.asStateFlow()

    private val _effect = Channel<PQCEffect>(Channel.BUFFERED)
    val effect: Flow<PQCEffect> = _effect.receiveAsFlow()

    // Simulated PQC algorithm knowledge base
    // 模拟的 PQC 算法知识库
    private val pqcKnowledgeBase = listOf(
        // Key Encapsulation Mechanisms (KEM) — for key exchange
        // 密钥封装机制 (KEM) — 用于密钥交换
        PQCAlgorithm(
            id = "ml-kem",
            name = "ML-KEM",
            fullName = "Module-Lattice-Based Key-Encapsulation Mechanism",
            type = PQCType.KEY_ENCAPSULATION,
            nistLevel = 5,
            keySize = "1,184 bytes (ML-KEM-768)",
            description = "NIST's primary post-quantum KEM standard based on Module-LWE. " +
                    "Android 17 adopts this for app signing key protection. / " +
                    "NIST 的主要后量子 KEM 标准，基于 Module-LWE。Android 17 采用此标准保护应用签名密钥。",
            androidSupport = AndroidPQCSupport.ANDROID_17_PLUS,
            useCases = listOf(
                "App Signing key protection in Google Play / Google Play 中的应用签名密钥保护",
                "Key exchange for quantum-safe communication / 量子安全通信的密钥交换",
                "Digital envelope for sensitive data / 敏感数据的数字信封"
            ),
            pros = listOf(
                "NIST standardized, maximum security assurance / NIST 标准化的，最大安全保障",
                "Relatively small key sizes / 相对较小的密钥大小",
                "Well-studied mathematical foundation (Lattice) / 被充分研究的数学基础（格）"
            ),
            cons = listOf(
                "Larger ciphertext/signature sizes than ECDSA / 密文/签名比 ECDSA 大",
                "Requires Android 17+ for full support / 需要 Android 17+ 才能完全支持",
                "Relatively new — long-term stability TBD / 相对较新，长期稳定性待定"
            )
        ),
        PQCAlgorithm(
            id = "lms",
            name = "LMS",
            fullName = "Leighton-Micali Hash-Based Signatures",
            type = PQCType.DIGITAL_SIGNATURE,
            nistLevel = 5,
            keySize = "~64 bytes (state) + M bytes (Merkle tree)",
            description = "Stateless hash-based signature scheme. Very fast signing, " +
                    "large signatures. Suitable for high-frequency signing scenarios. / " +
                    "无状态基于哈希的签名方案。签名速度快，签名较大。适合高频签名场景。",
            androidSupport = AndroidPQCSupport.ANDROID_17_PLUS,
            useCases = listOf(
                "Code signing for frequent OTA updates / 频繁 OTA 更新的代码签名",
                "Document signing with frequent updates / 频繁更新的文档签名",
                "Long-term signature archival / 长期签名存档"
            ),
            pros = listOf(
                "Extremely fast signing operations / 极快的签名操作",
                "Simple mathematical foundation — hash functions / 简单的数学基础（哈希函数）",
                "No known vulnerabilities to quantum algorithms / 对量子算法无已知漏洞"
            ),
            cons = listOf(
                "Large signature sizes (several KB) / 签名较大（数 KB）",
                "State management required for performance / 为性能需要状态管理",
                "Limited key reuse scenarios / 密钥复用场景有限"
            )
        ),
        PQCAlgorithm(
            id = "xmss",
            name = "XMSS",
            fullName = "eXtended Merkle Signature Scheme",
            type = PQCType.HASH_BASED,
            nistLevel = 5,
            keySize = "~64 bytes + large Merkle tree",
            description = "eXtended Merkle Signature Scheme. State-based (not stateless), " +
                    "supports many signings from same key. / " +
                    "扩展的 Merkle 签名方案。有状态的，支持从同一密钥进行多次签名。",
            androidSupport = AndroidPQCSupport.PLANNED,
            useCases = listOf(
                "Long-term document signing / 长期文档签名",
                "Systems requiring many signatures per key / 需要每个密钥多次签名的系统",
                "Compliance-sensitive archives / 合规敏感的存档"
            ),
            pros = listOf(
                "Strong security proofs based on hash functions / 基于哈希函数的强安全证明",
                "Supports many signatures / 支持多次签名",
                "Mature academic background / 成熟的学术背景"
            ),
            cons = listOf(
                "Stateful — must track usage to avoid reuse / 有状态的，必须追踪使用以避免重用",
                "Android support not yet available / Android 支持尚未提供",
                "Complex implementation / 复杂的实现"
            )
        ),
        PQCAlgorithm(
            id = "dilithium",
            name = "Dilithium",
            fullName = "CRYSTALS-Dilithium",
            type = PQCType.DIGITAL_SIGNATURE,
            nistLevel = 3,
            keySize = "1,312 bytes (public) / 4,000 bytes (Dilithium3)",
            description = "Lattice-based digital signature algorithm. Well-balanced " +
                    "between key size, signature size, and security. / " +
                    "基于格的数字签名算法。在密钥大小、签名大小和安全性之间平衡良好。",
            androidSupport = AndroidPQCSupport.ANDROID_14_PLUS,
            useCases = listOf(
                "User authentication tokens / 用户认证令牌",
                "Software update signing / 软件更新签名",
                "Document and contract signing / 文档和合同签名"
            ),
            pros = listOf(
                "Good balance of key and signature sizes / 密钥和签名大小平衡良好",
                "Based on well-studied lattice problems / 基于充分研究的格问题",
                "Android Keystore support in Android 14+ / Android 14+ 的 Keystore 支持"
            ),
            cons = listOf(
                "Larger signatures than ECDSA / 签名比 ECDSA 大",
                "Security level 3 (not level 5 like ML-KEM) / 安全级别 3（非 ML-KEM 的 5 级）",
                "Relatively high computational cost / 计算成本相对较高"
            )
        ),
        PQCAlgorithm(
            id = "falcon",
            name = "Falcon",
            fullName = "Fast Fourier Lattice-Based Compact Signatures over NTRU",
            type = PQCType.DIGITAL_SIGNATURE,
            nistLevel = 5,
            keySize = "897 bytes (public) / 666 bytes (private)",
            description = "Compact lattice-based signatures using NTRU lattices " +
                    "and Gaussian sampling. Produces the most compact signatures " +
                    "among NIST PQC standards. / " +
                    "使用 NTRU 格和高斯采样的紧凑基于格的签名。在 NIST PQC 标准中产生最紧凑的签名。",
            androidSupport = AndroidPQCSupport.PLANNED,
            useCases = listOf(
                "Blockchain and cryptocurrency applications / 区块链和加密货币应用",
                "Certificates and PKI / 证书和 PKI",
                "Space-constrained signing / 空间受限的签名"
            ),
            pros = listOf(
                "Most compact signatures among PQC standards / PQC 标准中最紧凑的签名",
                "NIST level 5 security / NIST 5 级安全性",
                "Fast verification / 快速验证"
            ),
            cons = listOf(
                "Complex implementation (Gaussian sampling) / 复杂的实现（高斯采样）",
                "No Android support yet / 尚未有 Android 支持",
                "Slow signing operation / 签名操作较慢"
            )
        )
    )

    // Simulated migration checklist steps
    // 模拟的迁移清单步骤
    private val defaultMigrationSteps = listOf(
        MigrationStep(
            id = "step-1",
            stepNumber = 1,
            title = "Assess Current Signing Configuration",
            description = "Analyze all APKs/AABs in your project. Determine current signature algorithms (ECDSA P-256/P-384), scheme versions (V1/V2/V3/V4), and identify which apps need migration. / 分析项目中所有 APK/AAB。确定当前签名算法（ECDSA P-256/P-384）、方案版本（V1/V2/V3/V4），并识别哪些应用需要迁移。",
            status = MigrationStepStatus.PENDING,
            isBlocking = false,
            estimatedTime = "1-2 days / 1-2天",
            referenceLink = "https://developer.android.com/about/versions/17#post-quantum"
        ),
        MigrationStep(
            id = "step-2",
            stepNumber = 2,
            title = "Select Quantum-Safe Algorithm",
            description = "Choose between ML-KEM-768 (for key encapsulation) and LMS (for signing). Consider: ML-KEM for new projects, LMS for high-frequency signing. / 在 ML-KEM-768（用于密钥封装）和 LMS（用于签名）之间选择。考虑：新项目用 ML-KEM，高频签名用 LMS。",
            status = MigrationStepStatus.PENDING,
            isBlocking = false,
            estimatedTime = "1 week / 1周",
            referenceLink = "https://csrc.nist.gov/projects/post-quantum-cryptography"
        ),
        MigrationStep(
            id = "step-3",
            stepNumber = 3,
            title = "Generate New Quantum-Safe Keys in Play Console",
            description = "In Google Play Console → App Signing → Request new quantum-safe key. Google will generate ML-KEM-768 keys in their HSM. Note: This is a one-way process. / 在 Google Play Console → App Signing → 请求新的量子安全密钥。Google 将在其 HSM 中生成 ML-KEM-768 密钥。注意：这是单向流程。",
            status = MigrationStepStatus.PENDING,
            isBlocking = true,
            estimatedTime = "3-5 days (Google processing) / 3-5天（Google 处理）",
            referenceLink = "https://play.google.com/console/app-signing"
        ),
        MigrationStep(
            id = "step-4",
            stepNumber = 4,
            title = "Configure Dual-Signing (Transition Period)",
            description = "Configure both ECDSA (current) and ML-KEM/LMS (new) signatures during transition. This ensures backward compatibility while building PQC trust. Duration: recommended 6-12 months. / 在过渡期同时配置 ECDSA（当前）和 ML-KEM/LMS（新）签名。这确保在建立 PQC 信任的同时保持向后兼容。建议持续时间：6-12 个月。",
            status = MigrationStepStatus.PENDING,
            isBlocking = true,
            estimatedTime = "1-2 days / 1-2天",
            referenceLink = null
        ),
        MigrationStep(
            id = "step-5",
            stepNumber = 5,
            title = "Verify Migration and Decommission Old Keys",
            description = "After dual-signing period, verify all apps work with PQC keys. Test on Android 17+ devices. Then decommission ECDSA keys per Google Play guidelines. / 双重签名期后，验证所有应用使用 PQC 密钥正常工作。在 Android 17+ 设备上测试。然后按照 Google Play 指南废弃 ECDSA 密钥。",
            status = MigrationStepStatus.PENDING,
            isBlocking = false,
            estimatedTime = "1-2 weeks / 1-2周",
            referenceLink = null
        )
    )

    init {
        // Initialize PQC knowledge base / 初始化 PQC 知识库
        _state.update {
            it.copy(
                pqcAlgorithms = pqcKnowledgeBase,
                migrationSteps = defaultMigrationSteps
            )
        }
    }

    /**
     * Process user intent — main entry point for all user actions
     * 处理用户意图 — 所有用户动作的主入口点
     */
    fun processIntent(intent: PQCIntent) {
        when (intent) {
            is PQCIntent.SelectTab -> selectTab(intent.tab)
            is PQCIntent.ScanAPK -> scanAPK(intent.uri)
            is PQCIntent.SelectApkResult -> selectApk(intent.apk)
            is PQCIntent.ClearScanResults -> clearScanResults()
            is PQCIntent.SelectAlgorithm -> selectAlgorithm(intent.algorithm)
            is PQCIntent.NextWizardStep -> nextWizardStep()
            is PQCIntent.PrevWizardStep -> prevWizardStep()
            is PQCIntent.SelectPQCForMigration -> selectPQCForMigration(intent.algorithm)
            is PQCIntent.GenerateReport -> generateReport(intent.format)
            is PQCIntent.SelectReportFormat -> selectReportFormat(intent.format)
            is PQCIntent.DetectDeviceCapabilities -> detectDeviceCapabilities()
            is PQCIntent.ToggleMigrationStep -> toggleMigrationStep(intent.stepId)
            is PQCIntent.ResetChecklist -> resetChecklist()
        }
    }

    // ============ Tab Navigation ============
    // ============ 标签页导航 ============

    private fun selectTab(tab: PQCTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    // ============ APK Scanning ============
    // ============ APK 扫描 ============

    /**
     * Scan APK/AAB file for signature information
     * In production, this would use android.content.pm.PackageManager and
     * java.security.Signature to extract actual signature data.
     * 扫描 APK/AAB 文件的签名信息
     * 实际生产中会使用 android.content.pm.PackageManager 和
     * java.security.Signature 提取实际签名数据。
     */
    private fun scanAPK(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(scanStatus = ScanStatus.SCANNING, scanProgress = 0f, scanError = null) }

            try {
                // Simulate multi-stage scanning process / 模拟多阶段扫描过程
                // Stage 1: Parse file / 阶段 1: 解析文件
                delay(300)
                _state.update { it.copy(scanProgress = 0.2f) }

                // Stage 2: Extract signatures / 阶段 2: 提取签名
                delay(400)
                _state.update { it.copy(scanProgress = 0.5f) }

                // Stage 3: Analyze algorithms / 阶段 3: 分析算法
                delay(300)
                _state.update { it.copy(scanProgress = 0.8f) }

                // Generate simulated APK result based on URI / 基于 URI 生成模拟 APK 结果
                val simulatedApk = generateSimulatedApkResult(uri)
                val currentApks = _state.value.scannedApks + simulatedApk

                // Recalculate dashboard metrics / 重新计算仪表盘指标
                val (healthScore, radarData, quantumSafeCount, atRiskCount) =
                    calculateDashboardMetrics(currentApks)

                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.SUCCESS,
                        scanProgress = 1f,
                        scannedApks = currentApks,
                        selectedApk = simulatedApk,
                        overallHealthScore = healthScore,
                        radarChartData = radarData,
                        totalApksScanned = currentApks.size,
                        quantumSafeCount = quantumSafeCount,
                        atRiskCount = atRiskCount
                    )
                }

                _effect.send(
                    PQCEffect.ScanComplete(
                        apkCount = currentApks.size,
                        quantumSafeCount = quantumSafeCount
                    )
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        scanStatus = ScanStatus.ERROR,
                        scanError = e.message ?: "Unknown error / 未知错误"
                    )
                }
                _effect.send(PQCEffect.ShowError(e.message ?: "Scan failed / 扫描失败"))
            }
        }
    }

    /**
     * Generate simulated APK signature result for demo
     * In production, this would parse the actual APK file.
     * 生成模拟的 APK 签名结果用于演示
     * 实际生产中会解析实际的 APK 文件。
     */
    private fun generateSimulatedApkResult(uri: Uri): ApkSignatureInfo {
        val fileName = uri.lastPathSegment ?: "unknown.apk"

        // Simulate different signature scenarios / 模拟不同的签名场景
        val scenario = Random.nextInt(5)

        return when (scenario) {
            0 -> ApkSignatureInfo(
                fileName = fileName,
                filePath = uri.toString(),
                fileSize = Random.nextLong(10_000_000L, 100_000_000L),
                packageName = "com.example.legacyapp",
                versionName = "1.0.0",
                versionCode = 1L,
                signatureSchemes = listOf(SignatureScheme.JAR_SIG, SignatureScheme.V2),
                algorithms = listOf(
                    SignatureAlgorithm(
                        name = "ECDSA with SHA-256",
                        keySize = 256,
                        scheme = SignatureScheme.V2,
                        strength = SignatureStrength.LEGACY,
                        quantumResistant = false
                    )
                ),
                overallStrength = SignatureStrength.LEGACY,
                riskLevel = RiskLevel.HIGH,
                quantumSafe = false,
                migrationUrgency = MigrationUrgency.CRITICAL
            )
            1 -> ApkSignatureInfo(
                fileName = fileName,
                filePath = uri.toString(),
                fileSize = Random.nextLong(10_000_000L, 100_000_000L),
                packageName = "com.example.mixedapp",
                versionName = "2.1.0",
                versionCode = 21L,
                signatureSchemes = listOf(SignatureScheme.V2, SignatureScheme.V3),
                algorithms = listOf(
                    SignatureAlgorithm(
                        name = "ECDSA with SHA-384",
                        keySize = 384,
                        scheme = SignatureScheme.V3,
                        strength = SignatureStrength.TRANSITIONAL,
                        quantumResistant = false
                    )
                ),
                overallStrength = SignatureStrength.TRANSITIONAL,
                riskLevel = RiskLevel.MEDIUM,
                quantumSafe = false,
                migrationUrgency = MigrationUrgency.HIGH
            )
            2 -> ApkSignatureInfo(
                fileName = fileName,
                filePath = uri.toString(),
                fileSize = Random.nextLong(10_000_000L, 100_000_000L),
                packageName = "com.example.quantumready",
                versionName = "3.0.0",
                versionCode = 30L,
                signatureSchemes = listOf(SignatureScheme.V3, SignatureScheme.V4),
                algorithms = listOf(
                    SignatureAlgorithm(
                        name = "ML-KEM-768",
                        keySize = 768,
                        scheme = SignatureScheme.V4,
                        strength = SignatureStrength.QUANTUM_SAFE,
                        quantumResistant = true
                    ),
                    SignatureAlgorithm(
                        name = "ECDSA P-384",
                        keySize = 384,
                        scheme = SignatureScheme.V3,
                        strength = SignatureStrength.TRANSITIONAL,
                        quantumResistant = false
                    )
                ),
                overallStrength = SignatureStrength.QUANTUM_SAFE,
                riskLevel = RiskLevel.LOW,
                quantumSafe = true,
                migrationUrgency = MigrationUrgency.NONE
            )
            3 -> ApkSignatureInfo(
                fileName = fileName,
                filePath = uri.toString(),
                fileSize = Random.nextLong(10_000_000L, 100_000_000L),
                packageName = "com.example.v3only",
                versionName = "1.5.0",
                versionCode = 15L,
                signatureSchemes = listOf(SignatureScheme.V3),
                algorithms = listOf(
                    SignatureAlgorithm(
                        name = "ECDSA with SHA-256",
                        keySize = 256,
                        scheme = SignatureScheme.V3,
                        strength = SignatureStrength.LEGACY,
                        quantumResistant = false
                    )
                ),
                overallStrength = SignatureStrength.LEGACY,
                riskLevel = RiskLevel.HIGH,
                quantumSafe = false,
                migrationUrgency = MigrationUrgency.CRITICAL
            )
            else -> ApkSignatureInfo(
                fileName = fileName,
                filePath = uri.toString(),
                fileSize = Random.nextLong(10_000_000L, 100_000_000L),
                packageName = "com.example.lmsready",
                versionName = "2.0.0",
                versionCode = 20L,
                signatureSchemes = listOf(SignatureScheme.V3, SignatureScheme.V4),
                algorithms = listOf(
                    SignatureAlgorithm(
                        name = "LMS-SHA256-20",
                        keySize = 256,
                        scheme = SignatureScheme.V4,
                        strength = SignatureStrength.QUANTUM_SAFE,
                        quantumResistant = true
                    )
                ),
                overallStrength = SignatureStrength.QUANTUM_SAFE,
                riskLevel = RiskLevel.LOW,
                quantumSafe = true,
                migrationUrgency = MigrationUrgency.NONE
            )
        }
    }

    private fun selectApk(apk: ApkSignatureInfo?) {
        _state.update { it.copy(selectedApk = apk) }
    }

    private fun clearScanResults() {
        _state.update {
            it.copy(
                scanStatus = ScanStatus.IDLE,
                scanProgress = 0f,
                scannedApks = emptyList(),
                selectedApk = null,
                scanError = null
            )
        }
    }

    // ============ Dashboard Metrics Calculation ============
    // ============ 仪表盘指标计算 ============

    /**
     * Calculate dashboard metrics from scanned APKs
     * 根据扫描的 APK 计算仪表盘指标
     */
    private fun calculateDashboardMetrics(
        apks: List<ApkSignatureInfo>
    ): Quartet<Int, RadarChartData, Int, Int> {
        if (apks.isEmpty()) {
            return Quartet(0, RadarChartData(0, 0, 0, 0, 0, 0), 0, 0)
        }

        val quantumSafeCount = apks.count { it.quantumSafe }
        val atRiskCount = apks.count { !it.quantumSafe }

        // Calculate overall health score / 计算整体健康分
        val healthScore = if (apks.isEmpty()) 0
        else (apks.sumOf { apk ->
            when (apk.overallStrength) {
                SignatureStrength.QUANTUM_SAFE -> 100
                SignatureStrength.TRANSITIONAL -> 60
                SignatureStrength.LEGACY -> 20
                SignatureStrength.UNKNOWN -> 40
            }
        } / apks.size)

        // Radar chart dimensions / 雷达图维度
        val avgAlgorithmScore = apks.map {
            when (it.overallStrength) {
                SignatureStrength.QUANTUM_SAFE -> 100
                SignatureStrength.TRANSITIONAL -> 60
                SignatureStrength.LEGACY -> 20
                SignatureStrength.UNKNOWN -> 40
            }
        }.average().toInt()

        val keystoreSupportScore = if (quantumSafeCount > 0) 80 else 40
        val playSigningComplianceScore = if (quantumSafeCount == apks.size) 100
        else if (quantumSafeCount > 0) 60 else 20

        val migrationReadinessScore = if (apks.isEmpty()) 0
        else (100 - (atRiskCount * 100 / apks.size))

        val radarData = RadarChartData(
            signatureStrength = healthScore,
            algorithmType = avgAlgorithmScore,
            keyLength = 60,
            keystoreSupport = keystoreSupportScore,
            playSigningCompliance = playSigningComplianceScore,
            migrationReadiness = migrationReadinessScore
        )

        return Quartet(healthScore, radarData, quantumSafeCount, atRiskCount)
    }

    // ============ PQC Knowledge Base ============
    // ============ PQC 知识库 ============

    private fun selectAlgorithm(algorithm: PQCAlgorithm?) {
        _state.update { it.copy(selectedAlgorithm = algorithm) }
        algorithm?.let {
            viewModelScope.launch {
                _effect.send(PQCEffect.NavigateToAlgorithmDetail(it.id))
            }
        }
    }

    // ============ Config Wizard ============
    // ============ 配置向导 ============

    private fun nextWizardStep() {
        val current = _state.value.configWizardStep
        val nextStep = ConfigWizardStep.entries.getOrNull(current.step + 1)
        if (nextStep != null) {
            _state.update { it.copy(configWizardStep = nextStep) }
            viewModelScope.launch {
                _effect.send(PQCEffect.NavigateToWizardStep(nextStep))
            }
        }
    }

    private fun prevWizardStep() {
        val current = _state.value.configWizardStep
        val prevStep = ConfigWizardStep.entries.getOrNull(current.step - 1)
        if (prevStep != null) {
            _state.update { it.copy(configWizardStep = prevStep) }
        }
    }

    private fun selectPQCForMigration(algorithm: PQCAlgorithm) {
        _state.update { it.copy(selectedPQCForMigration = algorithm) }
    }

    // ============ Compliance Report ============
    // ============ 合规报告 ============

    private fun generateReport(format: ReportFormat) {
        viewModelScope.launch {
            _state.update { it.copy(reportGenerating = true) }

            // Simulate report generation / 模拟报告生成
            delay(1500)

            val reportPath = "/storage/emulated/0/Download/PQC_Compliance_Report_${System.currentTimeMillis()}.${format.name.lowercase()}"

            _state.update {
                it.copy(
                    reportGenerating = false,
                    reportGenerated = true,
                    reportPath = reportPath
                )
            }

            _effect.send(PQCEffect.ReportGenerated(reportPath, format))
            _effect.send(
                PQCEffect.ShowSnackbar(
                    "Report generated: $reportPath / 报告已生成：$reportPath"
                )
            )
        }
    }

    private fun selectReportFormat(format: ReportFormat) {
        _state.update { it.copy(selectedReportFormat = format) }
    }

    // ============ Device Detection ============
    // ============ 设备检测 ============

    /**
     * Detect device Keystore PQC capabilities
     * In production, this would use android.security.keystore.KeyInfo
     * and android.os.Build to get actual device capabilities.
     * 检测设备 Keystore PQC 能力
     * 实际生产中使用 android.security.keystore.KeyInfo 和 android.os.Build 获取实际设备能力。
     */
    private fun detectDeviceCapabilities() {
        viewModelScope.launch {
            _state.update { it.copy(isDetectingDevices = true) }

            // Simulate detection delay / 模拟检测延迟
            delay(1000)

            // Generate simulated device capabilities / 生成模拟设备能力
            val simulatedDevices = listOf(
                KeystoreCapability(
                    deviceModel = "Pixel 9 Pro",
                    androidVersion = "Android 17",
                    apiLevel = 37,
                    supportedAlgorithms = listOf(
                        "EC/GCP-P-256/SHA-256",
                        "EC/GCP-P-384/SHA-384",
                        "ML-KEM-768",
                        "RSASSA-PSS-2048/SHA-256",
                        "LMS-SHA256-20"
                    ),
                    isQuantumSafe = true,
                    keymasterVersion = "Keymaster 6.0",
                    securityLevel = "StrongBox"
                ),
                KeystoreCapability(
                    deviceModel = "Samsung Galaxy S26 Ultra",
                    androidVersion = "Android 17",
                    apiLevel = 37,
                    supportedAlgorithms = listOf(
                        "EC/GCP-P-256/SHA-256",
                        "EC/GCP-P-384/SHA-384",
                        "ML-KEM-768",
                        "RSASSA-PSS-3072/SHA-256"
                    ),
                    isQuantumSafe = true,
                    keymasterVersion = "Keymaster 5.0",
                    securityLevel = "TEE"
                ),
                KeystoreCapability(
                    deviceModel = "Xiaomi 16 Pro",
                    androidVersion = "Android 16",
                    apiLevel = 36,
                    supportedAlgorithms = listOf(
                        "EC/GCP-P-256/SHA-256",
                        "EC/GCP-P-384/SHA-384",
                        "RSASSA-PSS-2048/SHA-256"
                    ),
                    isQuantumSafe = false,
                    keymasterVersion = "Keymaster 5.0",
                    securityLevel = "TEE"
                ),
                KeystoreCapability(
                    deviceModel = "Pixel 7",
                    androidVersion = "Android 15",
                    apiLevel = 35,
                    supportedAlgorithms = listOf(
                        "EC/GCP-P-256/SHA-256",
                        "RSASSA-PSS-2048/SHA-256",
                        "Dilithium2"
                    ),
                    isQuantumSafe = true,
                    keymasterVersion = "Keymaster 5.0",
                    securityLevel = "StrongBox"
                )
            )

            _state.update {
                it.copy(
                    isDetectingDevices = false,
                    deviceCapabilities = simulatedDevices
                )
            }

            _effect.send(PQCEffect.ShowSnackbar("Detected ${simulatedDevices.size} devices / 已检测 ${simulatedDevices.size} 台设备"))
        }
    }

    // ============ Migration Checklist ============
    // ============ 迁移清单 ============

    private fun toggleMigrationStep(stepId: String) {
        val currentSteps = _state.value.migrationSteps.toMutableList()
        val index = currentSteps.indexOfFirst { it.id == stepId }
        if (index >= 0) {
            val step = currentSteps[index]
            val newStatus = when (step.status) {
                MigrationStepStatus.PENDING -> MigrationStepStatus.IN_PROGRESS
                MigrationStepStatus.IN_PROGRESS -> MigrationStepStatus.COMPLETED
                MigrationStepStatus.COMPLETED -> MigrationStepStatus.PENDING
                MigrationStepStatus.BLOCKED -> step.status // Cannot toggle blocked steps / 不能切换阻塞步骤
            }
            currentSteps[index] = step.copy(status = newStatus)

            // Recalculate migration progress / 重新计算迁移进度
            val completedCount = currentSteps.count { it.status == MigrationStepStatus.COMPLETED }
            val totalNonBlocked = currentSteps.count { !it.isBlocking }
            val progress = if (totalNonBlocked > 0) completedCount.toFloat() / totalNonBlocked else 0f

            _state.update {
                it.copy(
                    migrationSteps = currentSteps,
                    migrationProgress = progress.coerceIn(0f, 1f)
                )
            }
        }
    }

    private fun resetChecklist() {
        _state.update {
            it.copy(
                migrationSteps = defaultMigrationSteps,
                migrationProgress = 0f
            )
        }
    }
}

/**
 * Simple Quartet data class for returning 4 values
 * 用于返回 4 个值的简单 Quartet 数据类
 */
data class Quartet<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
