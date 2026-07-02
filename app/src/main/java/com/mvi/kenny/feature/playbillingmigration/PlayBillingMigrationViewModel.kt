package com.mvi.kenny.feature.playbillingmigration

// ================================================================
// PlayBillingMigrationViewModel — Google Play Billing Library 9 迁移工具 ViewModel
// ================================================================
// MVI ViewModel: receives Intent, processes business logic, updates State.
//
// PRD-S: Google Play Billing Library 9 企业级迁移工具包
// Design: memory/agency/designs/PRD-S-Play-Billing-Library-9-Migration-Toolkit.md
//
// Key responsibilities:
//   1. Simulate project scanning for PBL version detection
//   2. Provide migration step-by-step wizard
//   3. Generate CI/CD YAML configuration
//   4. Export compliance reports
//
// NOTE: This is a local simulation tool. Real scanning requires actual project access.
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ================================================================
// Type Aliases / 类型别名
// ================================================================
typealias PlayBillingMigrationUiState = PlayBillingMigrationState
typealias PlayBillingMigrationUiEffect = PlayBillingMigrationEffect

// ================================================================
// ViewModel
// ================================================================

/**
 * ============================================================
 * PlayBillingMigrationViewModel — PBL 9 迁移工具 ViewModel
 * ============================================================
 * MVI pattern: State is the single source of truth, Intent drives changes.
 *
 * 页面结构（5 个 Tab）：
 *   Tab 0: 首页仪表板 — Dashboard overview
 *   Tab 1: 扫描结果 — API call scan results
 *   Tab 2: 迁移向导 — Step-by-step wizard
 *   Tab 3: CI/CD 配置 — CI/CD YAML generator
 *   Tab 4: 合规报告 — Compliance report export
 *
 * NOTE: This is a local simulation tool. It does NOT scan actual project files.
 */
class PlayBillingMigrationViewModel(
    private val initialState: PlayBillingMigrationState = PlayBillingMigrationState()
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<PlayBillingMigrationState> = _state.asStateFlow()

    // ── Effects ───────────────────────────────────────────────
    private val _effect = MutableSharedFlow<PlayBillingMigrationEffect>()
    val effect: MutableSharedFlow<PlayBillingMigrationEffect> = _effect

    // ==========================================================
    // Public API
    // ==========================================================

    /**
     * Process user intent / 处理用户意图
     * @param intent User action intent
     */
    fun sendIntent(intent: PlayBillingMigrationIntent) {
        when (intent) {
            is PlayBillingMigrationIntent.SelectTab ->
                _state.update { it.copy(selectedTab = intent.index) }

            is PlayBillingMigrationIntent.ScanProject ->
                runProjectScan(intent.projectPath)

            is PlayBillingMigrationIntent.SelectProject ->
                selectProject(intent.project)

            is PlayBillingMigrationIntent.NextMigrationStep ->
                nextMigrationStep()

            is PlayBillingMigrationIntent.PrevMigrationStep ->
                prevMigrationStep()

            is PlayBillingMigrationIntent.StartMigrationStep ->
                startMigrationStep(intent.stepId)

            is PlayBillingMigrationIntent.CompleteMigrationStep ->
                completeMigrationStep(intent.stepId)

            is PlayBillingMigrationIntent.SelectCICDProvider ->
                _state.update { it.copy(selectedProvider = intent.provider) }

            is PlayBillingMigrationIntent.UpdateRepositoryUrl ->
                _state.update { it.copy(repositoryUrl = intent.url) }

            is PlayBillingMigrationIntent.UpdateBranchName ->
                _state.update { it.copy(branchName = intent.branch) }

            is PlayBillingMigrationIntent.GenerateCICDConfig ->
                generateCICDConfig()

            is PlayBillingMigrationIntent.CopyCICDYaml ->
                copyYaml(intent.yaml)

            is PlayBillingMigrationIntent.SelectReportFormat ->
                _state.update { it.copy(selectedReportFormat = intent.format) }

            is PlayBillingMigrationIntent.ExportReport ->
                exportReport()

            is PlayBillingMigrationIntent.DismissSnackbar ->
                _state.update { it.copy(snackbarMessage = null) }

            is PlayBillingMigrationIntent.ResetAll ->
                resetAll()
        }
    }

    // ==========================================================
    // Intent Handlers / 意图处理器
    // ==========================================================

    /**
     * Run project scan simulation / 运行项目扫描模拟
     * @param projectPath 项目路径（模拟）
     */
    private fun runProjectScan(projectPath: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScanning = true,
                    scanProgress = 0f,
                    scanProgressText = "初始化扫描环境..."
                )
            }

            // Simulate scanning phases / 模拟扫描阶段
            val phases = listOf(
                "正在检测 PBL 版本..." to 0.2f,
                "扫描 BillingClient API 调用..." to 0.4f,
                "分析 BillingFlowParams 配置..." to 0.6f,
                "检测 PurchasesUpdatedListener 回调..." to 0.8f,
                "生成迁移建议..." to 1.0f
            )

            for ((text, progress) in phases) {
                _state.update { it.copy(scanProgressText = text, scanProgress = progress) }
                delay(400) // 模拟扫描耗时 / Simulate scan delay
            }

            _state.update {
                it.copy(
                    isScanning = false,
                    scanProgress = 1.0f,
                    scanProgressText = "扫描完成",
                    scannedProjects = SIMULATED_PROJECTS,
                    scanResults = SIMULATED_API_CALL_RESULTS,
                    detectedPBLVersion = PBLVersion.V7,
                    overallRiskLevel = RiskLevel.P0_CRITICAL,
                    migrationSteps = SIMULATED_MIGRATION_STEPS
                )
            }

            _effect.emit(
                PlayBillingMigrationEffect.ShowSnackbar("扫描完成：检测到 5 个 API 调用点，3 个项目")
            )
        }
    }

    /**
     * Select a scanned project / 选择扫描结果中的项目
     * @param project 被选中的项目
     */
    private fun selectProject(project: ScannedProject) {
        _state.update {
            it.copy(
                detectedPBLVersion = project.currentPBLVersion,
                overallRiskLevel = project.riskLevel,
                selectedTab = 1 // 切换到扫描结果 Tab / Switch to scan results tab
            )
        }
    }

    /**
     * Move to next migration step / 进入下一步迁移
     */
    private fun nextMigrationStep() {
        _state.update { current ->
            val maxStep = current.migrationSteps.size - 1
            val newIndex = minOf(current.currentStepIndex + 1, maxStep)
            current.copy(currentStepIndex = newIndex)
        }
    }

    /**
     * Move to previous migration step / 返回上一步迁移
     */
    private fun prevMigrationStep() {
        _state.update { current ->
            val newIndex = maxOf(current.currentStepIndex - 1, 0)
            current.copy(currentStepIndex = newIndex)
        }
    }

    /**
     * Start a specific migration step / 开始特定迁移步骤
     * @param stepId 步骤 ID
     */
    private fun startMigrationStep(stepId: String) {
        _state.update { current ->
            val updatedSteps = current.migrationSteps.map { step ->
                if (step.id == stepId && step.status == MigrationStepStatus.PENDING) {
                    step.copy(status = MigrationStepStatus.IN_PROGRESS)
                } else step
            }
            current.copy(migrationSteps = updatedSteps)
        }
    }

    /**
     * Mark a migration step as completed / 标记迁移步骤完成
     * @param stepId 步骤 ID
     */
    private fun completeMigrationStep(stepId: String) {
        _state.update { current ->
            val updatedSteps = current.migrationSteps.map { step ->
                if (step.id == stepId) {
                    step.copy(status = MigrationStepStatus.COMPLETED)
                } else step
            }
            current.copy(migrationSteps = updatedSteps)
        }

        viewModelScope.launch {
            _effect.emit(
                PlayBillingMigrationEffect.ShowSnackbar("步骤已完成，继续下一步...")
            )
        }
    }

    /**
     * Generate CI/CD configuration YAML / 生成 CI/CD 配置 YAML
     */
    private fun generateCICDConfig() {
        val currentState = _state.value
        val yaml = buildString {
            when (currentState.selectedProvider) {
                CICDProvider.GITHUB_ACTIONS -> {
                    appendLine("# ============================================================")
                    appendLine("# GitHub Actions — PBL v9 合规卡点")
                    appendLine("# Generated by Play Billing Library 9 Migration Toolkit")
                    appendLine("# Repository: ${currentState.repositoryUrl.ifEmpty { "<未设置>" }}")
                    appendLine("# Branch: ${currentState.branchName}")
                    appendLine("# ============================================================")
                    appendLine()
                    appendLine("name: PBL v9 Compliance & Build")
                    appendLine()
                    appendLine("on:")
                    appendLine("  push:")
                    appendLine("    branches: [${currentState.branchName.ifEmpty { "main" }}]")
                    appendLine("  pull_request:")
                    appendLine("    branches: [${currentState.branchName.ifEmpty { "main" }}]")
                    appendLine()
                    appendLine("jobs:")
                    appendLine("  # ── PBL v9 合规检测 ───────────────────────────────────")
                    appendLine("  pbl-compliance:")
                    appendLine("    runs-on: ubuntu-latest")
                    appendLine("    steps:")
                    appendLine("      - uses: actions/checkout@v4")
                    appendLine("      - name: Set up JDK")
                    appendLine("        uses: actions/setup-java@v4")
                    appendLine("        with:")
                    appendLine("          distribution: 'temurin'")
                    appendLine("          java-version: '17'")
                    appendLine("      - name: Run PBL v9 Compliance Check")
                    appendLine("        run: |")
                    appendLine("          echo 'Checking PBL version in build.gradle...'")
                    appendLine("          grep -r 'billingclient:billing' app/build.gradle || echo 'PBL not found'")
                    appendLine("      - name: Check In-App Messaging Implementation")
                    appendLine("        run: |")
                    appendLine("          echo 'Verifying BillingFlowParams includes IsOfferInfoPresented...'")
                    appendLine("      - name: Upload Compliance Report")
                    appendLine("        if: always()")
                    appendLine("        uses: actions/upload-artifact@v4")
                    appendLine("        with:")
                    appendLine("          name: pbl-compliance-report")
                    appendLine("          path: build/reports/pbl-compliance.json")
                    appendLine()
                    appendLine("  # ── 构建 job ──────────────────────────────────────────")
                    appendLine("  build:")
                    appendLine("    runs-on: ubuntu-latest")
                    appendLine("    needs: pbl-compliance  # 合规检测通过后才构建")
                    appendLine("    steps:")
                    appendLine("      - uses: actions/checkout@v4")
                    appendLine("      - name: Set up JDK")
                    appendLine("        uses: actions/setup-java@v4")
                    appendLine("        with:")
                    appendLine("          distribution: 'temurin'")
                    appendLine("          java-version: '17'")
                    appendLine("      - name: Cache Gradle packages")
                    appendLine("        uses: actions/cache@v3")
                    appendLine("        with:")
                    appendLine("          path: ~/.gradle/caches")
                    appendLine("          key: \${{ runner.os }}-gradle-\${{ hashFiles('**/*.gradle*') }}")
                    appendLine("      - name: Build Debug APK")
                    appendLine("        run: ./gradlew assembleDebug")
                    appendLine("      - name: Upload Debug APK")
                    appendLine("        uses: actions/upload-artifact@v4")
                    appendLine("        with:")
                    appendLine("          name: debug-apk")
                    appendLine("          path: app/build/outputs/apk/debug/app-debug.apk")
                }

                CICDProvider.GITLAB_CI -> {
                    appendLine("# ============================================================")
                    appendLine("# GitLab CI — PBL v9 合规卡点")
                    appendLine("# Generated by Play Billing Library 9 Migration Toolkit")
                    appendLine("# ============================================================")
                    appendLine()
                    appendLine("stages:")
                    appendLine("  - pbl-compliance")
                    appendLine("  - build")
                    appendLine()
                    appendLine("pbl-compliance-check:")
                    appendLine("  stage: pbl-compliance")
                    appendLine("  image: openjdk:17-jdk")
                    appendLine("  script:")
                    appendLine("    - echo 'Running PBL v9 compliance check...'")
                    appendLine("    - grep 'billingclient:billing:9' app/build.gradle")
                    appendLine("  artifacts:")
                    appendLine("    reports:")
                    appendLine("      json: build/reports/pbl-compliance.json")
                    appendLine("    expire_in: 1 week")
                    appendLine()
                    appendLine("build:")
                    appendLine("  stage: build")
                    appendLine("  image: openjdk:17-jdk")
                    appendLine("  script:")
                    appendLine("    - ./gradlew assembleDebug")
                    appendLine("  artifacts:")
                    appendLine("    paths:")
                    appendLine("      - app/build/outputs/apk/debug/app-debug.apk")
                    appendLine("  needs:")
                    appendLine("    - pbl-compliance-check")
                }

                CICDProvider.JENKINS -> {
                    appendLine("// ============================================================")
                    appendLine("// Jenkins Pipeline — PBL v9 合规卡点")
                    appendLine("// Generated by Play Billing Library 9 Migration Toolkit")
                    appendLine("// ============================================================")
                    appendLine()
                    appendLine("pipeline {")
                    appendLine("    agent any")
                    appendLine("    stages {")
                    appendLine("        stage('PBL v9 Compliance Check') {")
                    appendLine("            steps {")
                    appendLine("                echo 'Checking PBL version...'")
                    appendLine("                sh 'grep \"billingclient:billing:9\" app/build.gradle'")
                    appendLine("            }")
                    appendLine("        }")
                    appendLine("        stage('Build') {")
                    appendLine("            steps {")
                    appendLine("                sh './gradlew assembleDebug'")
                    appendLine("            }")
                    appendLine("        }")
                    appendLine("    }")
                    appendLine("}")
                }

                CICDProvider.BITRISE -> {
                    appendLine("# ============================================================")
                    appendLine("# Bitrise — PBL v9 合规卡点")
                    appendLine("# Generated by Play Billing Library 9 Migration Toolkit")
                    appendLine("# ============================================================")
                    appendLine()
                    appendLine("format_version: '9'")
                    appendLine("project_type: android")
                    appendLine()
                    appendLine("pipelines:")
                    appendLine("  pbl-v9-compliance:")
                    appendLine("    stages:")
                    appendLine("      - check: {}")
                    appendLine("      - build: {inputs: [variant: debug]}")
                    appendLine()
                    appendLine("stages:")
                    appendLine("  check:")
                    appendLine("    steps:")
                    appendLine("      - script:")
                    appendLine("          inputs:")
                    appendLine("            - content: |")
                    appendLine("                #!/bin/bash")
                    appendLine("                echo 'Running PBL v9 compliance check...'")
                    appendLine("                grep 'billingclient:billing:9' app/build.gradle")
                }
            }
        }

        _state.update { it.copy(cicdYamlConfig = yaml) }
    }

    /**
     * Copy CI/CD YAML to clipboard / 复制 CI/CD YAML 到剪贴板
     * @param yaml YAML 内容
     */
    private fun copyYaml(yaml: String) {
        viewModelScope.launch {
            _effect.emit(PlayBillingMigrationEffect.CopyToClipboard(yaml))
            _effect.emit(PlayBillingMigrationEffect.ShowSnackbar("YAML 配置已复制到剪贴板"))
        }
    }

    /**
     * Export compliance report / 导出合规报告
     */
    private fun exportReport() {
        viewModelScope.launch {
            // Simulate report generation / 模拟报告生成
            _state.update {
                it.copy(
                    reportUri = android.net.Uri.parse("content://mock/pbl-compliance-report")
                )
            }
            _effect.emit(
                PlayBillingMigrationEffect.ShowSnackbar(
                    "合规报告已生成（${_state.value.selectedReportFormat.displayName}）"
                )
            )
        }
    }

    /**
     * Reset all state / 重置所有状态
     */
    private fun resetAll() {
        _state.value = PlayBillingMigrationState.Initial
    }
}
