package com.mvi.kenny.feature.agp90migration

// ================================================================
// AGP90MigrationContract — AGP 9.0 破坏性变更迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for AGP 9.0 Breaking Changes Migration Toolkit.
//
// PRD-226: AGP 9.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-226-AGP-9-0-破坏性变更迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (clipboard, toast), via Channel
// ================================================================
//
// Tab Structure:
//   Tab 0: 密度Split检测 — Detect splits.density config, output App Bundle alternative
//   Tab 1: DSL稳定化 — incubating→stable API changes by AGP version
//   Tab 2: BuildConfig & NDK — BuildConfig field changes + NDK compliance check
//   Tab 3: Flutter兼容性 [P0] — Flutter AGP 9.0 incompatibility workaround + official issue tracking
//   Tab 4: 升级路径 — Gradle 9.0 + AGP 9.0 upgrade path + CI compliance + full checklist
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Risk Level & Status Enums / 风险等级 & 状态枚举
// ================================================================

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 * AGP 9.0 迁移风险等级。
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color 风险颜色
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
 * MigrationStatus — 迁移状态枚举
 * ============================================================
 */
enum class MigrationStatus(val displayName: String, val emoji: String) {
    PENDING("待处理", "⏳"),
    IN_PROGRESS("进行中", "🔄"),
    COMPLETED("已完成", "✅"),
    FAILED("失败", "❌"),
    NOT_APPLICABLE("不适用", "➖")
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * SplitsDensityResult — Tab0: splits.density 扫描结果
 * ============================================================
 *
 * @param filePath 包含 splits.density 配置的文件路径
 * @param currentConfig 当前的 splits.density 配置内容
 * @param alternative 推荐的 App Bundle 替代方案
 * @param riskLevel 风险等级
 */
data class SplitsDensityResult(
    val filePath: String,
    val currentConfig: String,
    val alternative: String,
    val riskLevel: RiskLevel,
    val recommendation: String  // "立即修复 / Fix immediately"
)

/**
 * ============================================================
 * DSLChange — Tab1: DSL 稳定化变更项
 * ============================================================
 *
 * @param agpVersion AGP 版本 (4.1/4.2/7.0)
 * @param apiName API 名称
 * @param oldPackage 旧的 incubating 包路径
 * @param newPackage 新的 stable 包路径
 * @param changeType 变更类型
 * @param riskLevel 风险等级
 * @param beforeCode 变更前代码示例
 * @param afterCode 变更后代码示例
 */
data class DSLChange(
    val agpVersion: String,
    val apiName: String,
    val oldPackage: String,
    val newPackage: String,
    val changeType: String,
    val riskLevel: RiskLevel,
    val beforeCode: String,
    val afterCode: String
)

/**
 * ============================================================
 * BuildConfigAnalysis — Tab2: BuildConfig 变更分析
 * ============================================================
 *
 * @param fieldName 字段名
 * @param oldBehavior 旧行为描述
 * @param newBehavior 新行为描述
 * @param affectedCode 受影响的代码位置
 * @param migrationStep 迁移步骤
 * @param riskLevel 风险等级
 */
data class BuildConfigAnalysis(
    val fieldName: String,
    val oldBehavior: String,
    val newBehavior: String,
    val affectedCode: String,
    val migrationStep: String,
    val riskLevel: RiskLevel
)

/**
 * ============================================================
 * NDKComplianceResult — Tab2: NDK 合规检测结果
 * ============================================================
 *
 * @param currentVersion 当前 NDK 版本
 * @param minRequiredVersion AGP 9.0 最低要求版本
 * @param isCompliant 是否合规
 * @param currentVersionProblems 当前版本问题
 * @param upgradePath 升级路径建议
 * @param riskLevel 风险等级
 */
data class NDKComplianceResult(
    val currentVersion: String,
    val minRequiredVersion: String,
    val isCompliant: Boolean,
    val currentVersionProblems: List<String>,
    val upgradePath: String,
    val riskLevel: RiskLevel
)

/**
 * ============================================================
 * FlutterCompatInfo — Tab3: Flutter 兼容性信息 [P0 BLOCKER]
 * ==============================================================
 *
 * @param flutterVersion Flutter 版本
 * @param dartVersion Dart 版本
 * @param isCompatible 是否兼容
 * @param officialIssueUrl 官方 Issue 链接
 * @param workarounds Workaround 步骤列表
 * @param estimatedFixVersion 预计修复版本
 * @param currentStatus 当前状态
 */
data class FlutterCompatInfo(
    val flutterVersion: String,
    val dartVersion: String,
    val isCompatible: Boolean,
    val officialIssueUrl: String,
    val workarounds: List<FlutterWorkaround>,
    val estimatedFixVersion: String,
    val currentStatus: String  // "Flutter官方明确警告不兼容 / Flutter officially warns incompatible"
)

/**
 * ============================================================
 * FlutterWorkaround — Flutter Workaround 步骤
 * ============================================================
 */
data class FlutterWorkaround(
    val step: Int,
    val title: String,
    val description: String,
    val code: String? = null
)

/**
 * ============================================================
 * MigrationPath — Tab4: 迁移路径信息
 * ============================================================
 *
 * @param currentGradleVersion 当前 Gradle 版本
 * @param targetGradleVersion 目标 Gradle 版本
 * @param currentAGPVersion 当前 AGP 版本
 * @param targetAGPVersion 目标 AGP 版本
 * @param upgradeOrder 升级顺序说明
 * @param compatibilityMatrix 版本兼容矩阵
 * @param riskLevel 风险等级
 */
data class MigrationPath(
    val currentGradleVersion: String,
    val targetGradleVersion: String,
    val currentAGPVersion: String,
    val targetAGPVersion: String,
    val upgradeOrder: String,
    val compatibilityMatrix: List<VersionCompatibilityItem>,
    val riskLevel: RiskLevel
)

/**
 * ============================================================
 * VersionCompatibilityItem — 版本兼容矩阵项
 * ============================================================
 */
data class VersionCompatibilityItem(
    val gradleVersion: String,
    val agpVersion: String,
    val isCompatible: Boolean
)

/**
 * ============================================================
 * CIComplianceStatus — Tab4: CI 合规状态
 * ============================================================
 *
 * @param agpVersionCheck AGP 版本检查结果
 * @param gradleVersionCheck Gradle 版本检查结果
 * @param overallPassed 是否全部通过
 * @param blockingIssues 阻塞性问题列表
 * @param recommendations 建议列表
 */
data class CIComplianceStatus(
    val agpVersionCheck: String,
    val gradleVersionCheck: String,
    val overallPassed: Boolean,
    val blockingIssues: List<String>,
    val recommendations: List<String>
)

/**
 * ============================================================
 * ChecklistItem — Tab4: 完整检查清单项
 * ============================================================
 *
 * @param id 唯一标识
 * @param category 分类
 * @param title 标题
 * @param description 描述
 * @param isChecked 是否已勾选
 * @param priority 优先级 P0/P1/P2
 */
data class ChecklistItem(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val isChecked: Boolean = false,
    val priority: RiskLevel
)

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * AGP90MigrationState — AGP 9.0 迁移工具页面状态（MVI State）
 * ============================================================
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 * @param projectPath 用户输入的项目路径
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scanProgressText 扫描进度文本
 *
 * Tab 0 — 密度Split检测
 * @param splitsDensityResults splits.density 扫描结果列表
 *
 * Tab 1 — DSL稳定化
 * @param dslChanges DSL 稳定化变更列表
 *
 * Tab 2 — BuildConfig & NDK
 * @param buildConfigAnalysis BuildConfig 变更分析
 * @param ndkComplianceResult NDK 合规检测结果
 *
 * Tab 3 — Flutter兼容性 [P0]
 * @param flutterCompatInfo Flutter 兼容性信息
 *
 * Tab 4 — 升级路径
 * @param migrationPath 迁移路径信息
 * @param ciComplianceStatus CI 合规状态
 * @param checklistItems 检查清单列表
 * @param completedChecklistCount 已完成检查项数量
 *
 * @param snackbarMessage Snackbar 消息
 */
data class AGP90MigrationState(
    // ── 全局状态 ───────────────────────────────────────────────
    val selectedTab: Int = 0,
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanProgressText: String = "",

    // ── Tab 0: 密度Split检测 ──────────────────────────────────
    val splitsDensityResults: List<SplitsDensityResult> = emptyList(),

    // ── Tab 1: DSL稳定化 ──────────────────────────────────────
    val dslChanges: List<DSLChange> = emptyList(),

    // ── Tab 2: BuildConfig & NDK ───────────────────────────────
    val buildConfigAnalysis: BuildConfigAnalysis? = null,
    val ndkComplianceResult: NDKComplianceResult? = null,

    // ── Tab 3: Flutter兼容性 [P0] ──────────────────────────────
    val flutterCompatInfo: FlutterCompatInfo? = null,

    // ── Tab 4: 升级路径 ───────────────────────────────────────
    val migrationPath: MigrationPath? = null,
    val ciComplianceStatus: CIComplianceStatus? = null,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val completedChecklistCount: Int = 0,

    // ── 全局 ─────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = AGP90MigrationState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================

/**
 * ============================================================
 * AGP90MigrationIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 */
sealed interface AGP90MigrationIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : AGP90MigrationIntent

    /** 用户输入项目路径
     * @param path 项目路径
     */
    data class SetProjectPath(val path: String) : AGP90MigrationIntent

    /** 用户触发 splits.density 扫描
     * @param simulate 是否使用模拟数据（跳过实际文件扫描）
     */
    data class ScanSplitsDensity(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户加载 DSL 稳定化变更
     * @param simulate 是否使用模拟数据
     */
    data class LoadDSLChanges(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户触发 BuildConfig 分析
     * @param simulate 是否使用模拟数据
     */
    data class AnalyzeBuildConfig(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户触发 NDK 合规检测
     * @param simulate 是否使用模拟数据
     */
    data class CheckNDKCompliance(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户加载 Flutter 兼容性信息
     * @param simulate 是否使用模拟数据
     */
    data class LoadFlutterCompat(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户生成迁移路径
     * @param simulate 是否使用模拟数据
     */
    data class GenerateMigrationPath(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户运行 CI 合规检测
     * @param simulate 是否使用模拟数据
     */
    data class RunCIComplianceCheck(val simulate: Boolean = false) : AGP90MigrationIntent

    /** 用户切换 Checklist 条目状态
     * @param itemId 条目 ID
     * @param checked 是否勾选
     */
    data class ToggleChecklist(val itemId: String, val checked: Boolean) : AGP90MigrationIntent

    /** 用户复制代码
     * @param code 要复制的代码
     */
    data class CopyCode(val code: String) : AGP90MigrationIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : AGP90MigrationIntent

    /** 用户重置所有状态 */
    data object ResetAll : AGP90MigrationIntent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * AGP90MigrationEffect — 副作用（一次性事件）
 * ============================================================
 * One-time side effects dispatched via Channel.
 */
sealed interface AGP90MigrationEffect {

    /** 显示 Snackbar 消息
     * @param message 消息内容
     * @param isError 是否为错误消息
     */
    data class ShowSnackbar(
        val message: String,
        val isError: Boolean = false
    ) : AGP90MigrationEffect

    /** 复制到剪贴板成功
     * @param label 内容标签
     */
    data class CopyToClipboard(val label: String) : AGP90MigrationEffect

    /** 打开外部链接
     * @param url URL 地址
     */
    data class OpenUrl(val url: String) : AGP90MigrationEffect
}
