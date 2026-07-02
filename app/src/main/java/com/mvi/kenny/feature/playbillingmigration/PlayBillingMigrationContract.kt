package com.mvi.kenny.feature.playbillingmigration

// ================================================================
// PlayBillingMigrationContract — Google Play Billing Library 9 企业级迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for PBL 9 Migration Toolkit.
//
// PRD-S: Google Play Billing Library 9 企业级迁移工具包
// Design: memory/agency/designs/PRD-S-Play-Billing-Library-9-Migration-Toolkit.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (clipboard, toast), via Channel
// ================================================================
//
// Tab Structure:
//   Tab 0: 首页仪表板 — Dashboard overview of all scanned projects
//   Tab 1: 扫描结果 — PBL version detection and API call point analysis
//   Tab 2: 迁移向导 — Step-by-step migration wizard (In-App Messaging, Error Handling, Subscription Logic)
//   Tab 3: CI/CD 配置 — GitHub Actions / GitLab CI YAML configuration generator
//   Tab 4: 合规报告 — Export compliance report (PDF/JSON)
// ================================================================

import android.net.Uri
import androidx.compose.ui.graphics.Color

// ================================================================
// Risk Level & Status Enums / 风险等级 & 状态枚举
// ================================================================

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 */
enum class RiskLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    P0_CRITICAL("P0 严重", "🔴", Color(0xFFF85149)),
    P1_HIGH("P1 高风险", "🟠", Color(0xFFD29922)),
    P2_MEDIUM("P2 中风险", "🟡", Color(0xFF3FB950)),
    P3_LOW("P3 低风险", "🟢", Color(0xFF6BCF7F)),
    UNKNOWN("未知", "⚪", Color(0xFF8B949E))
}

/**
 * ============================================================
 * MigrationStepStatus — 迁移步骤状态枚举
 * ============================================================
 */
enum class MigrationStepStatus(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    PENDING("待处理", "⏳", Color(0xFF8B949E)),
    IN_PROGRESS("进行中", "🔄", Color(0xFF58A6FF)),
    COMPLETED("已完成", "✅", Color(0xFF3FB950)),
    FAILED("失败", "❌", Color(0xFFF85149)),
    SKIPPED("已跳过", "⏭️", Color(0xFFD29922))
}

/**
 * ============================================================
 * CICDProvider — CI/CD 提供商枚举
 * ============================================================
 */
enum class CICDProvider(val displayName: String, val icon: String) {
    GITHUB_ACTIONS("GitHub Actions", "🐙"),
    GITLAB_CI("GitLab CI", "🦊"),
    JENKINS("Jenkins", "🔧"),
    BITRISE("Bitrise", "🔵")
}

/**
 * ============================================================
 * ReportFormat — 报告导出格式枚举
 * ============================================================
 */
enum class ReportFormat(val displayName: String, val extension: String) {
    PDF("PDF 文档", "pdf"),
    JSON("JSON 数据", "json"),
    MARKDOWN("Markdown", "md")
}

/**
 * ============================================================
 * PBLVersion — Play Billing Library 版本枚举
 * ============================================================
 */
enum class PBLVersion(val version: String, val displayName: String) {
    V7("7.0.0", "PBL v7 (即将废弃)"),
    V8("8.0.0", "PBL v8 (即将废弃)"),
    V9("9.0.0", "PBL v9 (当前)"),
    UNKNOWN("?", "未知版本")
}

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * PlayBillingMigrationState — PBL 9 迁移工具页面状态（MVI State）
 * ============================================================
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scanProgressText 扫描进度文本
 *
 * Tab 0 — 首页仪表板
 * @param scannedProjects 已扫描项目列表
 *
 * Tab 1 — 扫描结果
 * @param scanResults API 调用点扫描结果列表
 * @param detectedPBLVersion 检测到的 PBL 版本
 * @param overallRiskLevel 整体风险等级
 *
 * Tab 2 — 迁移向导
 * @param migrationSteps 迁移步骤列表
 * @param currentStepIndex 当前步骤索引
 *
 * Tab 3 — CI/CD 配置
 * @param selectedProvider CI/CD 提供商选择
 * @param cicdYamlConfig 生成的 CI/CD YAML 配置
 * @param repositoryUrl 仓库 URL
 * @param branchName 分支名
 *
 * Tab 4 — 合规报告
 * @param reportUri 报告文件 URI
 * @param selectedReportFormat 选定的报告格式
 * @param deadlineDays 延期截止天数
 *
 * @param snackbarMessage Snackbar 消息
 */
data class PlayBillingMigrationState(
    // ── 全局状态 ───────────────────────────────────────────────
    val selectedTab: Int = 0,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanProgressText: String = "",

    // ── Tab 0: 首页仪表板 ──────────────────────────────────────
    val scannedProjects: List<ScannedProject> = emptyList(),

    // ── Tab 1: 扫描结果 ────────────────────────────────────────
    val scanResults: List<ApiCallResult> = emptyList(),
    val detectedPBLVersion: PBLVersion = PBLVersion.UNKNOWN,
    val overallRiskLevel: RiskLevel = RiskLevel.UNKNOWN,

    // ── Tab 2: 迁移向导 ────────────────────────────────────────
    val migrationSteps: List<MigrationStep> = emptyList(),
    val currentStepIndex: Int = 0,

    // ── Tab 3: CI/CD 配置 ──────────────────────────────────────
    val selectedProvider: CICDProvider = CICDProvider.GITHUB_ACTIONS,
    val cicdYamlConfig: String = "",
    val repositoryUrl: String = "",
    val branchName: String = "main",

    // ── Tab 4: 合规报告 ────────────────────────────────────────
    val reportUri: Uri? = null,
    val selectedReportFormat: ReportFormat = ReportFormat.PDF,
    val deadlineDays: Int = 122, // 2026-11-01 截止，当前 2026-07-02

    // ── 全局 ──────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = PlayBillingMigrationState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================

/**
 * ============================================================
 * PlayBillingMigrationIntent — 用户意图（User Intent）
 * ============================================================
 */
sealed interface PlayBillingMigrationIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : PlayBillingMigrationIntent

    /** 用户触发项目扫描
     * @param projectPath 项目路径（当前为模拟数据）
     */
    data class ScanProject(val projectPath: String) : PlayBillingMigrationIntent

    /** 用户选择扫描结果项目
     * @param project 项目
     */
    data class SelectProject(val project: ScannedProject) : PlayBillingMigrationIntent

    /** 用户进入下一步迁移
     */
    data object NextMigrationStep : PlayBillingMigrationIntent

    /** 用户返回上一步迁移
     */
    data object PrevMigrationStep : PlayBillingMigrationIntent

    /** 用户开始特定迁移步骤
     * @param stepId 步骤 ID
     */
    data class StartMigrationStep(val stepId: String) : PlayBillingMigrationIntent

    /** 用户标记步骤完成
     * @param stepId 步骤 ID
     */
    data class CompleteMigrationStep(val stepId: String) : PlayBillingMigrationIntent

    /** 用户选择 CI/CD 提供商
     * @param provider 提供商
     */
    data class SelectCICDProvider(val provider: CICDProvider) : PlayBillingMigrationIntent

    /** 用户更新仓库 URL
     * @param url 仓库 URL
     */
    data class UpdateRepositoryUrl(val url: String) : PlayBillingMigrationIntent

    /** 用户更新分支名
     * @param branch 分支名
     */
    data class UpdateBranchName(val branch: String) : PlayBillingMigrationIntent

    /** 用户生成 CI/CD 配置
     */
    data object GenerateCICDConfig : PlayBillingMigrationIntent

    /** 用户复制 CI/CD YAML
     * @param yaml YAML 配置内容
     */
    data class CopyCICDYaml(val yaml: String) : PlayBillingMigrationIntent

    /** 用户选择报告格式
     * @param format 报告格式
     */
    data class SelectReportFormat(val format: ReportFormat) : PlayBillingMigrationIntent

    /** 用户导出报告
     */
    data object ExportReport : PlayBillingMigrationIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : PlayBillingMigrationIntent

    /** 用户重置所有状态 */
    data object ResetAll : PlayBillingMigrationIntent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * PlayBillingMigrationEffect — 一次性副作用（Effect）
 * ============================================================
 */
sealed interface PlayBillingMigrationEffect {

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : PlayBillingMigrationEffect

    /** 显示 Snackbar
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : PlayBillingMigrationEffect

    /** 报告已生成
     * @param uri 报告文件 URI
     */
    data class ReportGenerated(val uri: Uri) : PlayBillingMigrationEffect
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ScannedProject — 已扫描项目
 * ============================================================
 *
 * @param id 唯一 ID
 * @param name 项目名称
 * @param currentPBLVersion 当前 PBL 版本
 * @param riskLevel 风险等级
 * @param pendingMigrations 待迁移项数
 * @param deadlineDays 距截止天数
 * @param lastScanned 最后扫描时间
 */
data class ScannedProject(
    val id: String,
    val name: String,
    val currentPBLVersion: PBLVersion,
    val riskLevel: RiskLevel,
    val pendingMigrations: Int,
    val deadlineDays: Int,
    val lastScanned: String = ""
)

/**
 * ============================================================
 * ApiCallResult — API 调用点扫描结果
 * ============================================================
 *
 * @param id 唯一 ID
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param apiType API 类型（BillingClient/PurchasesUpdatedListener/BillingFlowParams）
 * @param description 问题描述
 * @param codeSnippet 问题代码片段
 * @param fixSnippet 修复后代码片段
 * @param riskLevel 风险等级
 * @param migrationStepId 关联的迁移步骤 ID
 */
data class ApiCallResult(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val apiType: String,
    val description: String,
    val codeSnippet: String,
    val fixSnippet: String,
    val riskLevel: RiskLevel,
    val migrationStepId: String
)

/**
 * ============================================================
 * MigrationStep — 迁移步骤
 * ============================================================
 *
 * @param id 步骤 ID
 * @param stepNumber 步骤编号
 * @param title 标题
 * @param titleEn 英文标题
 * @param description 描述
 * @param affectedFiles 影响文件数
 * @param status 步骤状态
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 * @param riskLevel 风险等级
 */
data class MigrationStep(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val titleEn: String,
    val description: String,
    val affectedFiles: Int,
    val status: MigrationStepStatus = MigrationStepStatus.PENDING,
    val beforeCode: String = "",
    val afterCode: String = "",
    val riskLevel: RiskLevel = RiskLevel.P1_HIGH
)

// ================================================================
// Simulated Data / 模拟数据
// ================================================================

/**
 * ============================================================
 * SIMULATED_PROJECTS — 模拟已扫描项目
 * ============================================================
 */
val SIMULATED_PROJECTS = listOf(
    ScannedProject(
        id = "proj-001",
        name = "MyShoppingApp",
        currentPBLVersion = PBLVersion.V7,
        riskLevel = RiskLevel.P0_CRITICAL,
        pendingMigrations = 12,
        deadlineDays = 122,
        lastScanned = "2026-07-02 07:40"
    ),
    ScannedProject(
        id = "proj-002",
        name = "EnterpriseCRM",
        currentPBLVersion = PBLVersion.V8,
        riskLevel = RiskLevel.P1_HIGH,
        pendingMigrations = 7,
        deadlineDays = 122,
        lastScanned = "2026-07-02 07:41"
    ),
    ScannedProject(
        id = "proj-003",
        name = "MobileGamePro",
        currentPBLVersion = PBLVersion.V9,
        riskLevel = RiskLevel.P3_LOW,
        pendingMigrations = 2,
        deadlineDays = 122,
        lastScanned = "2026-07-02 07:42"
    )
)

/**
 * ============================================================
 * SIMULATED_API_CALL_RESULTS — 模拟 API 调用点扫描结果
 * ============================================================
 */
val SIMULATED_API_CALL_RESULTS = listOf(
    ApiCallResult(
        id = "api-001",
        filePath = "app/src/main/java/com/example/app/billing/BillingManager.kt",
        lineNumber = 87,
        apiType = "BillingFlowParams",
        description = "选择式涨价（opt-in price increase）应用内消息未实现",
        codeSnippet = """// ❌ PBL v8 写法 — 缺少应用内消息
val flowParams = BillingFlowParams.newBuilder()
    .setProductDetailsParamsList(
        listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
    )
    .build()
billingClient.launchBillingFlow(activity, flowParams)""",
        fixSnippet = """// ✅ PBL v9 写法 — 包含应用内消息
val flowParams = BillingFlowParams.newBuilder()
    .setProductDetailsParamsList(
        listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
    )
    .setIsOfferInfoPresentated(
        BillingFlowParams.IsOfferInfoPresented(presentOfferCodes = true)
    )
    .build()
billingClient.launchBillingFlow(activity, flowParams)""",
        riskLevel = RiskLevel.P0_CRITICAL,
        migrationStepId = "step-001"
    ),
    ApiCallResult(
        id = "api-002",
        filePath = "app/src/main/java/com/example/app/billing/BillingManager.kt",
        lineNumber = 124,
        apiType = "BillingClient",
        description = "BILLING_UNAVAILABLE 错误处理需更新 — v9 抛出异常而非静默失败",
        codeSnippet = """// ❌ PBL v8 写法 — BILLING_UNAVAILABLE 静默处理
billingClient.startConnection(object : BillingClientStateListener {
    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            // v8: 不合法状态静默忽略
        }
    }
})""",
        fixSnippet = """// ✅ PBL v9 写法 — BILLING_UNAVAILABLE 显式处理
billingClient.startConnection(object : BillingClientStateListener {
    override fun onBillingSetupFinished(result: BillingResult) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> { /* 正常流程 */ }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                // v9: 被封锁商店返回 BILLING_UNAVAILABLE，需提示用户
                showError("当前应用商店版本过低，请更新 Google Play")
            }
            else -> showError("Billing setup failed: ${'$'}{result.debugMessage}")
        }
    }
})""",
        riskLevel = RiskLevel.P0_CRITICAL,
        migrationStepId = "step-002"
    ),
    ApiCallResult(
        id = "api-003",
        filePath = "app/src/main/java/com/example/app/billing/SubscriptionHelper.kt",
        lineNumber = 45,
        apiType = "PurchasesUpdatedListener",
        description = "PurchasesUpdatedListener 回调需处理新错误码",
        codeSnippet = """// ❌ PBL v8 写法 — 旧错误码处理
override fun onPurchasesUpdated(
    result: BillingResult,
    purchases: List<Purchase>?
) {
    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
        purchases?.let { processPurchases(it) }
    }
    // v8: 其他错误码静默忽略
}""",
        fixSnippet = """// ✅ PBL v9 写法 — 完整错误码处理
override fun onPurchasesUpdated(
    result: BillingResult,
    purchases: List<Purchase>?
) {
    when (result.responseCode) {
        BillingClient.BillingResponseCode.OK -> {
            purchases?.let { processPurchases(it) }
        }
        BillingClient.BillingResponseCode.USER_CANCELED -> {
            // 用户取消，无需处理
        }
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
            // 已购买，走恢复流程
            billingClient.queryPurchasesAsync(...)
        }
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
            // v9: 封锁商店抛出异常
            showError("应用商店不可用，请更新 Google Play")
        }
        else -> showError("Purchase failed: ${'$'}{result.debugMessage}")
    }
}""",
        riskLevel = RiskLevel.P1_HIGH,
        migrationStepId = "step-002"
    ),
    ApiCallResult(
        id = "api-004",
        filePath = "app/src/main/java/com/example/app/billing/PriceChangeHandler.kt",
        lineNumber = 12,
        apiType = "PriceChangeConfirmationParams",
        description = "价格变更确认流程需使用新 API",
        codeSnippet = """// ❌ PBL v8 写法 — 价格变更旧 API
private fun handlePriceChange(productId: String) {
    // v8: 无应用内消息支持，价格变更直接生效
    launchPriceChangeFlow(activity, productId)
}""",
        fixSnippet = """// ✅ PBL v9 写法 — 价格变更 + 应用内消息
private fun handlePriceChange(
    productId: String,
    productDetails: ProductDetails
) {
    // v9: 价格变更必须经过应用内消息确认
    val params = PriceChangeConfirmationParams.newBuilder()
        .setProductDetails(productDetails)
        .build()

    billingClient.launchPriceChangeConfirmationFlow(
        activity,
        params,
        { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                // 用户确认价格变更
                processPriceChangeConfirmed(productId)
            }
        }
    )
}""",
        riskLevel = RiskLevel.P1_HIGH,
        migrationStepId = "step-001"
    ),
    ApiCallResult(
        id = "api-005",
        filePath = "app/build.gradle",
        lineNumber = 28,
        apiType = "build.gradle",
        description = "PBL 版本检测 — 当前使用 v7，需升级到 v9",
        codeSnippet = """// ❌ PBL v7
implementation 'com.android.billingclient:billing:7.0.0'""",
        fixSnippet = """// ✅ PBL v9
implementation 'com.android.billingclient:billing:9.0.0'""",
        riskLevel = RiskLevel.P0_CRITICAL,
        migrationStepId = "step-003"
    )
)

/**
 * ============================================================
 * SIMULATED_MIGRATION_STEPS — 模拟迁移步骤
 * ============================================================
 */
val SIMULATED_MIGRATION_STEPS = listOf(
    MigrationStep(
        id = "step-001",
        stepNumber = 1,
        title = "应用内消息（In-App Messaging）迁移",
        titleEn = "In-App Messaging Migration",
        description = "PBL 9 要求选择式涨价（opt-in price increase）必须经过应用内消息确认。更新 BillingFlowParams 和价格变更确认流程。",
        affectedFiles = 3,
        status = MigrationStepStatus.PENDING,
        beforeCode = """// ❌ PBL v8 — 缺少应用内消息
val flowParams = BillingFlowParams.newBuilder()
    .setProductDetailsParamsList(listOf(...))
    .build()""",
        afterCode = """// ✅ PBL v9 — 包含应用内消息
val flowParams = BillingFlowParams.newBuilder()
    .setProductDetailsParamsList(listOf(...))
    .setIsOfferInfoPresentated(
        BillingFlowParams.IsOfferInfoPresented(presentOfferCodes = true)
    )
    .build()""",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    MigrationStep(
        id = "step-002",
        stepNumber = 2,
        title = "错误处理适配（BILLING_UNAVAILABLE）",
        titleEn = "Error Handling: BILLING_UNAVAILABLE",
        description = "PBL 9 行为变更：被封锁商店返回 BILLING_UNAVAILABLE 而非静默失败。所有 BillingClient 调用处需增加错误处理。",
        affectedFiles = 5,
        status = MigrationStepStatus.PENDING,
        beforeCode = """// ❌ PBL v8 — 静默处理
if (result.responseCode == BillingClient.BillingResponseCode.OK) {
    // 处理成功
}
// v8: 其他错误静默忽略""",
        afterCode = """// ✅ PBL v9 — 显式错误处理
when (result.responseCode) {
    BillingClient.BillingResponseCode.OK -> { /* 成功 */ }
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
        showError("Google Play 版本过低，请更新")
    }
    BillingClient.BillingResponseCode.USER_CANCELED -> { /* 取消 */ }
    else -> showError("Error: ${'$'}{result.debugMessage}")
}""",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    MigrationStep(
        id = "step-003",
        stepNumber = 3,
        title = "PBL 版本升级（v7/v8 → v9）",
        titleEn = "PBL Version Upgrade (v7/v8 → v9)",
        description = "将 build.gradle 中的 billing library 版本从 v7/v8 升级到 v9。延期申请截止 2026-11-01（122天后）。",
        affectedFiles = 1,
        status = MigrationStepStatus.PENDING,
        beforeCode = """// PBL v7/v8
implementation 'com.android.billingclient:billing:7.0.0'
// 或
implementation 'com.android.billingclient:billing:8.0.0'""",
        afterCode = """// PBL v9
implementation 'com.android.billingclient:billing:9.0.0'""",
        riskLevel = RiskLevel.P1_HIGH
    ),
    MigrationStep(
        id = "step-004",
        stepNumber = 4,
        title = "订阅逻辑更新（延期申请辅助）",
        titleEn = "Subscription Logic & Deferral Assistance",
        description = "PBL 9 对订阅逻辑无重大破坏性变更，但需配合应用内消息流程。提供延期申请材料生成功能。",
        affectedFiles = 2,
        status = MigrationStepStatus.PENDING,
        beforeCode = """// 订阅处理 — v8 行为
billingClient.queryPurchasesAsync(
    BillingClient.SkuType.SUBS
) { result, purchases -> ... }""",
        afterCode = """// 订阅处理 — v9 行为（基本不变）
// v9 新增：订阅变更也需应用内消息确认
billingClient.queryPurchasesAsync(
    BillingClient.SkuType.SUBS
) { result, purchases -> ... }""",
        riskLevel = RiskLevel.P2_MEDIUM
    ),
    MigrationStep(
        id = "step-005",
        stepNumber = 5,
        title = "CI/CD 合规卡点集成",
        titleEn = "CI/CD Compliance Gate Integration",
        description = "在 CI/CD 流水线中集成 PBL 9 合规检测，确保所有合入代码符合 PBL 9 要求。",
        affectedFiles = 0,
        status = MigrationStepStatus.PENDING,
        beforeCode = """# GitHub Actions — 无 PBL 检测
jobs:
  build:
    steps:
      - run: ./gradlew assembleDebug""",
        afterCode = """# GitHub Actions — 含 PBL v9 合规卡点
jobs:
  pbl-compliance:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run PBL v9 Compliance Check
        run: |
          ./gradlew pbl9ComplianceCheck --project-path=${'$'}{{ env.PROJECT_PATH }}
      - name: Upload Compliance Report
        uses: actions/upload-artifact@v4
        with:
          name: pbl-compliance-report
          path: build/reports/pbl-compliance.json

  build:
    needs: pbl-compliance
    steps:
      - run: ./gradlew assembleDebug""",
        riskLevel = RiskLevel.P2_MEDIUM
    )
)
