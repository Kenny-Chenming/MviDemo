package com.mvi.kenny.feature.ksp2migration

// ================================================================
// KSP2MigrationContract — KSP1→KSP2 迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for KSP2 Migration Toolkit.
//
// PRD-229: KSP1→KSP2 迁移工具包
// Design Reference: memory/agency/designs/PRD-229-KSP2-Migration-Toolkit.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * Compatibility — KSP2 兼容性状态枚举
 * ============================================================
 * 各 Annotation Processor 对 KSP2 的兼容状态。
 *
 * @param displayNameCn 中文显示名称
 * @param displayNameEn 英文显示名称
 * @param emoji Emoji representation
 * @param color 状态颜色
 */
enum class Compatibility(
    val displayNameCn: String,
    val displayNameEn: String,
    val emoji: String,
    val color: Color
) {
    /** KSP2 完全支持 */
    SUPPORTED("已支持", "Supported", "✅", Color(0xFF3FB950)),
    /** KSP2 Beta 支持 */
    BETA("Beta 支持", "Beta", "⚠️", Color(0xFFD29922)),
    /** 暂不支持 KSP2 */
    UNSUPPORTED("不支持", "Unsupported", "❌", Color(0xFFF85149)),
    /** 未知兼容性 */
    UNKNOWN("未知", "Unknown", "❓", Color(0xFF8B949E))
}

/**
 * ============================================================
 * ComplianceLevel — CI 合规等级
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color 合规等级颜色
 */
enum class ComplianceLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    COMPLIANT("合规", "✅", Color(0xFF3FB950)),
    PARTIAL("部分合规", "⚠️", Color(0xFFD29922)),
    NON_COMPLIANT("不合规", "❌", Color(0xFFF85149)),
    UNKNOWN("未检测", "⚪", Color(0xFF8B949E))
}

/**
 * ============================================================
 * StepStatus — 迁移步骤状态
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 */
enum class StepStatus(val displayName: String, val emoji: String) {
    PENDING("待处理", "⏳"),
    COMPLETED("已完成", "✅"),
    FAILED("失败", "❌")
}

/**
 * ============================================================
 * KSP2MigrationState — KSP2 迁移工具页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param currentTab 当前 Tab 索引 (0-4)
 *   0: KSP2 扫描器 (KSPScannerTab)
 *   1: 兼容性矩阵 (CompatibilityMatrixTab)
 *   2: 迁移指南 (MigrationGuideTab)
 *   3: API 变化参考 (APIChangesTab)
 *   4: CI 合规检测 (CIComplianceTab)
 * @param scanState 扫描状态
 * @param scanResults 扫描结果
 * @param processors 处理器列表（兼容性矩阵数据）
 * @param matrixFilter 矩阵过滤关键词
 * @param migrationSteps 迁移步骤列表
 * @param completedSteps 已完成步骤集合
 * @param apiChanges API 变化列表
 * @param complianceState CI 合规检测状态
 * @param complianceResults CI 合规检测结果列表
 * @param overallCompliance 整体合规等级
 * @param snackbarMessage Snackbar 消息
 */
data class KSP2MigrationState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val currentTab: Int = 0,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val snackbarMessage: String? = null,

    // ── Tab 1: KSP2 扫描器 ─────────────────────────────────────
    val gradleFileContent: String = "",
    val scanResults: List<ProcessorScanResult> = emptyList(),
    val scanDetectedKspVersion: String? = null,
    val scanDetectedKotlinVersion: String? = null,

    // ── Tab 2: 兼容性矩阵 ────────────────────────────────────────
    val processors: List<ProcessorInfo> = emptyList(),
    val matrixFilter: String = "",

    // ── Tab 3: 迁移指南 ─────────────────────────────────────────
    val migrationSteps: List<MigrationStep> = emptyList(),
    val completedSteps: Set<Int> = emptySet(),

    // ── Tab 4: API 变化参考 ─────────────────────────────────────
    val apiChanges: List<APIChangeItem> = emptyList(),
    val selectedApiCategory: String = "ALL",

    // ── Tab 5: CI 合规检测 ───────────────────────────────────────
    val complianceState: ComplianceState = ComplianceState.IDLE,
    val complianceResults: List<ComplianceResult> = emptyList(),
    val overallCompliance: ComplianceLevel = ComplianceLevel.UNKNOWN,
    val exportedReport: String? = null
) {
    companion object {
        /** Initial/empty state */
        val Initial = KSP2MigrationState()
    }
}

/**
 * ============================================================
 * ComplianceState — CI 合规检测运行状态
 * ============================================================
 */
enum class ComplianceState {
    IDLE,
    RUNNING,
    COMPLETED
}

/**
 * ============================================================
 * KSP2MigrationIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see KSP2MigrationViewModel.sendIntent handles all Intents
 */
sealed interface KSP2MigrationIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SetTab(val index: Int) : KSP2MigrationIntent

    /** 用户粘贴 Gradle 文件内容（Tab1 扫描器）
     * @param content 文件内容
     */
    data class PasteGradleContent(val content: String) : KSP2MigrationIntent

    /** 用户触发 KSP 扫描（Tab1）
     * 扫描 build.gradle.kts 中的 ksp { } 配置，检测 processor 和版本
     */
    data object StartScan : KSP2MigrationIntent

    /** 用户过滤兼容性矩阵（Tab2）
     * @param query 过滤关键词
     */
    data class FilterMatrix(val query: String) : KSP2MigrationIntent

    /** 用户切换 API 变化分类（Tab4）
     * @param category 分类名称
     */
    data class SelectApiCategory(val category: String) : KSP2MigrationIntent

    /** 用户标记迁移步骤完成/未完成（Tab3）
     * @param stepIndex 步骤索引
     */
    data class ToggleStep(val stepIndex: Int) : KSP2MigrationIntent

    /** 用户运行 CI 合规检测（Tab5）
     * 检测 ksp.useKSP2=true、Kotlin 版本、processor 兼容性
     */
    data object RunComplianceCheck : KSP2MigrationIntent

    /** 用户导出合规报告（Tab5）
     * 生成合规检测报告文本
     */
    data object ExportReport : KSP2MigrationIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : KSP2MigrationIntent

    /** 用户重置所有状态 */
    data object ResetAll : KSP2MigrationIntent
}

/**
 * ============================================================
 * KSP2MigrationEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see KSP2MigrationViewModel _effect.send() sends Effects
 */
sealed interface KSP2MigrationEffect {

    /** 显示 Snackbar 消息
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : KSP2MigrationEffect

    /** 复制到剪贴板
     * @param label 标签
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val label: String, val content: String) : KSP2MigrationEffect

    /** 分享报告
     * @param content 报告内容
     */
    data class ShareReport(val content: String) : KSP2MigrationEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : KSP2MigrationEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ProcessorScanResult — KSP Processor 扫描结果
 * ============================================================
 * Tab1 扫描器输出的单个 processor 检测结果。
 *
 * @param name Processor 名称（如 Room、Dagger-Hilt）
 * @param version 检测到的版本号
 * @param compatibility KSP2 兼容性状态
 * @param configSnippet 在 build.gradle.kts 中的配置代码片段
 * @param ksp2CompatibleVersion 达到 KSP2 兼容所需的最低版本
 * @param notes 备注信息
 */
data class ProcessorScanResult(
    val name: String,
    val version: String,
    val compatibility: Compatibility,
    val configSnippet: String,
    val ksp2CompatibleVersion: String? = null,
    val notes: String = ""
)

/**
 * ============================================================
 * ProcessorInfo — Processor 兼容性信息
 * ============================================================
 * Tab2 兼容性矩阵中的 processor 详细信息。
 *
 * @param name Processor 名称
 * @param latestVersion 最新稳定版
 * @param ksp2CompatibleSince 首个支持 KSP2 的版本
 * @param compatibility 当前兼容性状态
 * @param category 分类（Database/DI/Serialization/Other）
 * @param description 简要描述
 * @param migrationGuide 迁移指南摘要
 */
data class ProcessorInfo(
    val name: String,
    val latestVersion: String,
    val ksp2CompatibleSince: String,
    val compatibility: Compatibility,
    val category: String,
    val description: String,
    val migrationGuide: String
)

/**
 * ============================================================
 * MigrationStep — 迁移步骤
 * ============================================================
 * Tab3 迁移指南中的步骤定义。
 *
 * @param stepNumber 步骤编号（从 1 开始）
 * @param title 步骤标题
 * @param description 步骤详细说明
 * @param codeBefore 迁移前代码（可为 null）
 * @param codeAfter 迁移后代码（可为 null）
 * @param critical 是否关键步骤（高亮显示）
 * @param estimatedMinutes 预估耗时（分钟）
 */
data class MigrationStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val codeBefore: String? = null,
    val codeAfter: String? = null,
    val critical: Boolean = false,
    val estimatedMinutes: Int = 0
)

/**
 * ============================================================
 * APIChangeItem — API 变化条目
 * ============================================================
 * Tab4 API 变化参考中的条目。
 *
 * @param apiName API 名称
 * @param category 分类（Resolver/Symbol/Diagnostic/Other）
 * @param changeType 变化类型（REMOVED/DEPRECATED/MODIFIED/NEW）
 * @param description 变化描述
 * @param beforeCode 旧 API 使用示例
 * @param afterCode 新 API 使用示例
 * @param migrationNote 迁移注意事项
 */
data class APIChangeItem(
    val apiName: String,
    val category: String,
    val changeType: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String,
    val migrationNote: String
)

/**
 * ============================================================
 * ComplianceResult — CI 合规检测结果条目
 * ============================================================
 * Tab5 CI 合规检测的单个检查项结果。
 *
 * @param checkName 检查项名称
 * @param passed 是否通过
 * @param message 检查结果消息
 * @param suggestion 如未通过，修复建议
 * @param severity 严重程度（P0/P1/P2）
 */
data class ComplianceResult(
    val checkName: String,
    val passed: Boolean,
    val message: String,
    val suggestion: String = "",
    val severity: String = "P1"
)

// ================================================================
// 内置 Processor 兼容性数据（静态数据，方便后续更新）
// Reference: GitHub KSP Issues milestone 2.0/2.1, KSP2 docs
// ================================================================

/**
 * ============================================================
 * KSP2_PROCESSORS — KSP2 兼容性矩阵数据
 * ============================================================
 * 各主流 Annotation Processor 的 KSP2 兼容性状态。
 * 数据来源：GitHub KSP 官方文档、KSP2 release notes、各 processor GitHub
 */
val KSP2_PROCESSORS = listOf(
    ProcessorInfo(
        name = "Room",
        latestVersion = "2.6.1",
        ksp2CompatibleSince = "2.6.0-alpha05",
        compatibility = Compatibility.SUPPORTED,
        category = "Database",
        description = "Android Jetpack 官方 ORM 库",
        migrationGuide = "升级到 2.6.0-alpha05+ 即可支持 KSP2，Room 3.0 将原生支持 KSP2"
    ),
    ProcessorInfo(
        name = "Hilt",
        latestVersion = "2.51.1",
        ksp2CompatibleSince = "2.51-beta01",
        compatibility = Compatibility.BETA,
        category = "DI",
        description = "Dagger-based 依赖注入库",
        migrationGuide = "使用 2.51-beta01+ 版本，KSP2 支持仍处于 Beta，部分高级场景可能有问题"
    ),
    ProcessorInfo(
        name = "Dagger",
        latestVersion = "2.51.1",
        ksp2CompatibleSince = "2.51-beta",
        compatibility = Compatibility.BETA,
        category = "DI",
        description = "Java/Kotlin 依赖注入框架",
        migrationGuide = "2.51-beta 开始支持 KSP2，建议在简单项目先行试用"
    ),
    ProcessorInfo(
        name = "Moshi",
        latestVersion = "1.15.1",
        ksp2CompatibleSince = "1.15.1",
        compatibility = Compatibility.SUPPORTED,
        category = "Serialization",
        description = "JSON 序列化/反序列化库",
        migrationGuide = "Moshi 1.15.0+ 完全支持 KSP2，建议升级到最新稳定版"
    ),
    ProcessorInfo(
        name = "Kotlinx Serialization",
        latestVersion = "1.6.3",
        ksp2CompatibleSince = "1.6.0",
        compatibility = Compatibility.SUPPORTED,
        category = "Serialization",
        description = "Kotlin 官方序列化库",
        migrationGuide = "Kotlinx Serialization 1.6.0+ 完全支持 KSP2，与 KSP2 同步发展"
    ),
    ProcessorInfo(
        name = "Koin",
        latestVersion = "3.5.6",
        ksp2CompatibleSince = "3.5.6",
        compatibility = Compatibility.SUPPORTED,
        category = "DI",
        description = "轻量级 Kotlin 依赖注入框架",
        migrationGuide = "Koin 3.5.6+ 支持 KSP2，Koin 团队积极维护 KSP2 兼容性"
    ),
    ProcessorInfo(
        name = "Airbnb Epoxy",
        latestVersion = "5.1.0",
        ksp2CompatibleSince = "5.1.0",
        compatibility = Compatibility.SUPPORTED,
        category = "Other",
        description = "Airbnb 的 RecyclerView 抽象库",
        migrationGuide = "Epoxy 5.1.0 完全支持 KSP2，支持 Model EpoxyController"
    ),
    ProcessorInfo(
        name = "Glide",
        latestVersion = "4.16.0",
        ksp2CompatibleSince = "4.16.0",
        compatibility = Compatibility.SUPPORTED,
        category = "Other",
        description = "图片加载库",
        migrationGuide = "Glide 4.16.0+ 支持 KSP2，支持 Generated API"
    ),
    ProcessorInfo(
        name = "Parceler",
        latestVersion = "1.1.1",
        ksp2CompatibleSince = "未发布",
        compatibility = Compatibility.UNSUPPORTED,
        category = "Serialization",
        description = "Android Parcelable 注解处理器",
        migrationGuide = "Parceler 尚未发布 KSP2 支持，建议使用 Android Jetpack 的 kotlin-parcelize plugin 作为替代"
    ),
    ProcessorInfo(
        name = "DataStore",
        latestVersion = "1.1.1",
        ksp2CompatibleSince = "1.1.0",
        compatibility = Compatibility.SUPPORTED,
        category = "Storage",
        description = "Jetpack DataStore 偏好存储",
        migrationGuide = "DataStore 1.1.0+ 通过 androidx.datastore 支持 KSP2"
    ),
    ProcessorInfo(
        name = "Requery",
        latestVersion = "4.9.0",
        ksp2CompatibleSince = "未发布",
        compatibility = Compatibility.UNSUPPORTED,
        category = "Database",
        description = "SQLite 访问库（Kotlin First）",
        migrationGuide = "Requery 尚未发布 KSP2 支持，建议评估迁移到 Room KSP"
    ),
    ProcessorInfo(
        name = "KotlinX DateTime",
        latestVersion = "0.5.0",
        ksp2CompatibleSince = "0.5.0",
        compatibility = Compatibility.SUPPORTED,
        category = "Other",
        description = "Kotlin 多平台日期时间库",
        migrationGuide = "kotlinx-datetime 0.5.0+ 完全支持 KSP2"
    )
)

/**
 * ============================================================
 * KSP2_MIGRATION_STEPS — KSP1→KSP2 迁移步骤
 * ============================================================
 * 标准 KSP1→KSP2 迁移步骤清单。
 */
val KSP2_MIGRATION_STEPS = listOf(
    MigrationStep(
        stepNumber = 1,
        title = "确认 Kotlin 版本 ≥ 2.0",
        description = "KSP2 要求 Kotlin ≥ 2.0。KSP1 不再支持 Kotlin 2.2+，升级 KSP2 前必须先确认 Kotlin 版本。",
        codeBefore = """
// Kotlin 1.x（不支持 KSP2）
kotlin {
    version = "1.9.24"  // ❌ KSP2 需要 Kotlin ≥ 2.0
}
""",
        codeAfter = """// build.gradle.kts
kotlin {
    version = "2.0.0"  // ✅ KSP2 要求 Kotlin ≥ 2.0
}""",
        critical = true,
        estimatedMinutes = 15
    ),
    MigrationStep(
        stepNumber = 2,
        title = "启用 KSP2（gradle.properties）",
        description = "在 gradle.properties 中添加 ksp.useKSP2=true。这是 KSP2 的核心开关。",
        codeBefore = """# gradle.properties ❌
# 无 KSP2 配置（默认使用 KSP1）
ksp.useKSP2=false""",
        codeAfter = """# gradle.properties ✅
# 启用 KSP2（Beta）
ksp.useKSP2=true""",
        critical = true,
        estimatedMinutes = 5
    ),
    MigrationStep(
        stepNumber = 3,
        title = "更新 Processor 版本到 KSP2 兼容版",
        description = "将各 annotation processor 升级到支持 KSP2 的版本。参考「兼容性矩阵」Tab。",
        codeBefore = """// build.gradle.kts ❌
dependencies {
    ksp("androidx.room:room-compiler:2.6.1")  // Room 2.6.1 支持 KSP2
    ksp("com.google.dagger:hilt-compiler:2.51")  // ❌ Hilt 2.51 不支持 KSP2
}""",
        codeAfter = """// build.gradle.kts ✅
dependencies {
    ksp("androidx.room:room-compiler:2.6.1")  // ✅ 2.6.1 支持 KSP2
    ksp("com.google.dagger:hilt-compiler:2.51.1")  // ✅ 2.51.1 Beta 支持 KSP2
}""",
        critical = true,
        estimatedMinutes = 30
    ),
    MigrationStep(
        stepNumber = 4,
        title = "处理 KAPT Fallback（如有必要）",
        description = "如遇暂不支持 KSP2 的 processor，可暂时保留 kapt 配置作为 fallback，但建议尽快迁移。",
        codeBefore = """// build.gradle.kts ❌
plugins {
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")  // ❌ 同时使用 KAPT 和 KSP
}
dependencies {
    ksp("com.example:processor:1.0.0")  // KSP
    kapt("com.example:legacy-processor:0.9.0")  // KAPT fallback
}""",
        codeAfter = """// build.gradle.kts ✅
// 阶段一：先全部迁移到 KSP（推荐）
plugins {
    id("com.google.devtools.ksp")  // ✅ 只保留 KSP
}
// 如必须保留 KAPT fallback：
plugins {
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt") apply false  // apply false：声明但不激活
}""",
        critical = false,
        estimatedMinutes = 45
    ),
    MigrationStep(
        stepNumber = 5,
        title = "验证编译结果",
        description = "运行 ./gradlew clean assembleDebug 验证编译通过。重点检查 ksp 生成的文件是否正确。",
        codeBefore = null,
        codeAfter = """# 验证命令
./gradlew clean assembleDebug

# 查看 KSP 生成的代码
ls app/build/generated/ksp/

# 如遇问题，开启 KSP2 详细日志
# 在 gradle.properties 中添加：
ksp.incremental.log=true""",
        critical = true,
        estimatedMinutes = 20
    ),
    MigrationStep(
        stepNumber = 6,
        title = "运行 KSP2 CI 合规检测",
        description = "在 CI 流程中加入 KSP2 合规检测，确保团队所有成员都正确配置了 KSP2。",
        codeBefore = null,
        codeAfter = """# 在 CI 的 build 步骤中加入：
./gradlew kspCheckKotlin  # 检测 KSP 配置
./gradlew dependencies --configuration ksp  # 检查 KSP 依赖

# 推荐：使用 Gradle plugin 检测合规性
# 参考「CI 合规检测」Tab""",
        critical = false,
        estimatedMinutes = 15
    )
)

/**
 * ============================================================
 * KSP2_API_CHANGES — KSP1→KSP2 API 变化清单
 * ============================================================
 * KSP2 新架构带来的 API 变化。
 * Reference: GitHub KSP Issues milestone 2.0/2.1
 */
val KSP2_API_CHANGES = listOf(
    APIChangeItem(
        apiName = "Resolver.getJvmWildcard",
        category = "Resolver",
        changeType = "MODIFIED",
        description = "KSP2 中 getJvmWildcard 返回 NonEmptyList<KrClassKind> 而非单个 TypeProjection。",
        beforeCode = """// KSP1
val wildcard = resolver.getJvmWildcard(type)

// KSP1 返回: TypeProjection?
// ⚠️ KSP2 不再支持 nullable，需使用 NonEmptyList""",
        afterCode = """// KSP2
val classKinds = resolver.getJvmWildcard(type)
// KSP2 返回: NonEmptyList<KrClassKind>
// ✅ 直接使用 classKinds.first() 获取主要类型

if (classKinds.contains(KrClassKind.CLASS)) {
    // 处理 CLASS 类型的通配符
}""",
        migrationNote = "KSP2 返回 NonEmptyList 而非 nullable TypeProjection，需调整空值处理逻辑"
    ),
    APIChangeItem(
        apiName = "Resolver.mapToJvmSignature",
        category = "Resolver",
        changeType = "MODIFIED",
        description = "mapToJvmSignature 在 KSP2 中行为变化，Map<String, NonExistentType> 返回 error type 而非 null。",
        beforeCode = """// KSP1
val jvmSig = resolver.mapToJvmSignature(type)
// KSP1: 如果类型无效可能返回 null
if (jvmSig == null) {
    // 处理无效类型
}""",
        afterCode = """// KSP2
val jvmSig = resolver.mapToJvmSignature(type)
// KSP2: 如果类型无效返回 ErrorType，不再返回 null
// ✅ 直接使用 jvmSig，需要判断是否为 ErrorType

if (jvmSig is ErrorType) {
    // 处理错误类型，而非判断 null
}""",
        migrationNote = "KSP2 中 error type 使用 ErrorType 类而非 null，需改用类型判断"
    ),
    APIChangeItem(
        apiName = "Symbol Visibility",
        category = "Symbol",
        changeType = "REMOVED",
        description = "KSP2 中某些 visibility modifier 处理方式变化，INTERNAL 符号处理逻辑调整。",
        beforeCode = """// KSP1 — 检查符号可见性
val visibility = sym.visibility
if (visibility == Visibility.INTERNAL) {
    // 处理 INTERNAL 符号
}""",
        afterCode = """// KSP2 — INTERNAL 符号默认不暴露
// KSP2 基于 Kotlin compiler 的统一可见性模型
// ✅ INTERNAL 符号默认不在 symbol过程中暴露
// 如需处理，使用 resolver.getClassDeclarationByName() 显式获取""",
        migrationNote = "KSP2 的可见性模型与 Kotlin compiler APIs 对齐，INTERNAL 默认不暴露"
    ),
    APIChangeItem(
        apiName = "SynthesisKind for Java getters/setters",
        category = "Symbol",
        changeType = "MODIFIED",
        description = "KSP2 中合成 Java getter/setter 的 SynthesisKind 行为变化（KSP2 milestone 2.0）。",
        beforeCode = """// KSP1 — 检测 Java getter/setter
val kind = sym.synthesisKind
if (kind == SynthesisKind.JAVA_ACC_SUPER) {
    // KSP1 处理方式
}""",
        afterCode = """// KSP2 — 统一到 Kotlin compiler APIs
// KSP2 不再使用 JAVA_ACC_SUPER
// ✅ 使用 Kotlin 语义处理 getter/setter
if (sym is KSPropertyDeclaration) {
    // 检查是否为 property
    val getter = sym.getter
    if (getter != null) {
        // 处理 getter
    }
}""",
        migrationNote = "KSP2 使用 Kotlin compiler 统一的 symbol 模型，不再区分 JAVA_ 前缀"
    ),
    APIChangeItem(
        apiName = "KSNode.containingFile",
        category = "Symbol",
        changeType = "MODIFIED",
        description = "KSP2 中 containingFile 对某些合成节点返回 null 的行为更一致。",
        beforeCode = """// KSP1
val file = sym.containingFile
// ⚠️ KSP1 对某些节点可能返回非预期值""",
        afterCode = """// KSP2
val file = sym.containingFile
// ✅ KSP2 对合成节点统一返回 null
// 使用 containingFile 时需显式判断 null

file?.let {
    // 安全处理文件引用
} ?: run {
    // 处理无文件引用的情况（如内联合成节点）
}""",
        migrationNote = "KSP2 containingFile 行为更可预测，需显式处理 null 情况"
    ),
    APIChangeItem(
        apiName = "Diagnostic code format",
        category = "Diagnostic",
        changeType = "NEW",
        description = "KSP2 引入了新的 Diagnostic code 格式，与 Kotlin compiler 2.0 保持一致。",
        beforeCode = """// KSP1 — 旧版 Diagnostic
val code = diagnostic.code
// KSP1 使用自定义 code 格式""",
        afterCode = """// KSP2 — 新版 Diagnostic code
val code = diagnostic.code
// KSP2 使用 Kotlin compiler 2.0 统一格式
// 格式: KSP2-NNNN（如 KSP2-0001）

// ✅ KSP2 新增的错误码
when (code) {
    "KSP2-0001" -> // KSP2 特有错误：符号冲突
    "KSP2-0002" -> // KSP2 特有错误：循环依赖
    else -> // 使用通用 Kotlin compiler 错误码处理
}""",
        migrationNote = "KSP2 引入新的错误码体系，部分 KSP1 错误码已废弃"
    ),
    APIChangeItem(
        apiName = "Incremental processing",
        category = "Processor",
        changeType = "MODIFIED",
        description = "KSP2 的增量处理模型基于 Kotlin compiler 的新 API，与 KSP1 的缓存机制不同。",
        beforeCode = """// KSP1 — 基于文件级缓存
// KSP1 使用 own cache + Kotlin compiler incremental cache
val changes = resolver.getChanges_since(lastRound)
// 依赖文件级变化检测""",
        afterCode = """// KSP2 — 基于 Kotlin compiler APIs
// KSP2 使用 Kotlin 2.0 compiler 的统一增量模型
// ✅ 增量处理更准确，减少不必要的重新编译
val affectedSymbols = resolver.getAffectedSymbols(changedFiles)
// 直接获取受影响符号，无需手动追踪文件变化

// 启用详细日志：
// gradle.properties: ksp.incremental.log=true""",
        migrationNote = "KSP2 增量处理更精准，但缓存机制变化，首次编译可能稍慢"
    ),
    APIChangeItem(
        apiName = "ROUND_COUNT handling",
        category = "Processor",
        changeType = "MODIFIED",
        description = "KSP2 中多轮处理的 round 计数逻辑与 KSP1 不同。",
        beforeCode = """// KSP1 — 手动处理多轮
while (resolver.processFinal round 未完成) {
    resolver.process(...)
    round++
}
// KSP1 需要手动管理 round 循环""",
        afterCode = """// KSP2 — Kotlin compiler 管理 round
// KSP2 由 Kotlin compiler APIs 统一管理 round
// ✅ processor 不再需要手动管理 round 循环
resolver.process(annotations)
// KSP2 的 resolver 自动处理多轮，直到无新符号生成
// 如需检测是否为最后一轮：
val isLastRound = resolver.isLastRound""",
        migrationNote = "KSP2 round 管理由 compiler 接管，processor 逻辑更简洁"
    )
)

/**
 * ============================================================
 * SIMULATED_COMPLIANCE_RESULTS — 模拟 CI 合规检测结果
 * ============================================================
 */
val SIMULATED_COMPLIANCE_RESULTS = listOf(
    ComplianceResult(
        checkName = "ksp.useKSP2 配置检测",
        passed = true,
        message = "gradle.properties 中已配置 ksp.useKSP2=true",
        suggestion = "",
        severity = "P0"
    ),
    ComplianceResult(
        checkName = "Kotlin 版本检测",
        passed = true,
        message = "Kotlin 版本为 2.0.0，满足 KSP2 要求（≥ 2.0）",
        suggestion = "",
        severity = "P0"
    ),
    ComplianceResult(
        checkName = "Room KSP2 兼容性",
        passed = true,
        message = "Room 2.6.1 已支持 KSP2",
        suggestion = "",
        severity = "P1"
    ),
    ComplianceResult(
        checkName = "Hilt KSP2 兼容性",
        passed = false,
        message = "Hilt 2.51 不支持 KSP2（需要 2.51.1-beta01+）",
        suggestion = "在 build.gradle.kts 中将 com.google.dagger:hilt-compiler 升级到 2.51.1 或更高版本",
        severity = "P1"
    ),
    ComplianceResult(
        checkName = "Processor 版本合规性",
        passed = false,
        message = "存在 1 个 processor 尚未升级到 KSP2 兼容版本",
        suggestion = "运行 ./gradlew kspCheckKotlin 查看所有不合规 processor，并逐个升级",
        severity = "P1"
    ),
    ComplianceResult(
        checkName = "Gradle Daemon JVM 配置",
        passed = true,
        message = "Gradle Daemon JVM 参数已配置（KSP2 推荐 -Xmx4g）",
        suggestion = "",
        severity = "P2"
    )
)

/**
 * ============================================================
 * 扫描模拟数据 — 用于 Tab1 扫描器演示
 * ============================================================
 */
val SIMULATED_SCAN_RESULTS = listOf(
    ProcessorScanResult(
        name = "Room",
        version = "2.6.1",
        compatibility = Compatibility.SUPPORTED,
        configSnippet = """ksp("androidx.room:room-compiler:2.6.1")""",
        ksp2CompatibleVersion = "2.6.0-alpha05",
        notes = "Room 2.6.1 完全支持 KSP2 ✅"
    ),
    ProcessorScanResult(
        name = "Hilt",
        version = "2.51",
        compatibility = Compatibility.BETA,
        configSnippet = """ksp("com.google.dagger:hilt-compiler:2.51")""",
        ksp2CompatibleVersion = "2.51.1-beta01",
        notes = "Hilt 2.51 不支持 KSP2，需要升级到 2.51.1-beta01+ ⚠️"
    ),
    ProcessorScanResult(
        name = "Moshi",
        version = "1.15.1",
        compatibility = Compatibility.SUPPORTED,
        configSnippet = """ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")""",
        ksp2CompatibleVersion = "1.15.1",
        notes = "Moshi 1.15.1 完全支持 KSP2 ✅"
    ),
    ProcessorScanResult(
        name = "Kotlinx Serialization",
        version = "1.6.3",
        compatibility = Compatibility.SUPPORTED,
        configSnippet = """ksp("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.serialization.compiler:1.6.3")""",
        ksp2CompatibleVersion = "1.6.0",
        notes = "kotlinx-serialization 1.6.0+ 完全支持 KSP2 ✅"
    ),
    ProcessorScanResult(
        name = "Koin",
        version = "3.5.3",
        compatibility = Compatibility.SUPPORTED,
        configSnippet = """ksp("io.insert-koin:koin-ksp-compiler:3.5.3")""",
        ksp2CompatibleVersion = "3.5.6",
        notes = "Koin 3.5.3 支持 KSP2，建议升级到 3.5.6+ ✅"
    )
)
