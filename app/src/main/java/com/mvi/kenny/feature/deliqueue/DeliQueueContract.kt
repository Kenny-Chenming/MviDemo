package com.mvi.kenny.feature.deliqueue

// ================================================================
// DeliQueueContract — Android 17 DeliQueue 迁移检测工具包 MVI Contract
// ================================================================
// MVI architecture contract for Android 17 lock-free MessageQueue migration toolkit.
//
// PRD-198: Android 17 DeliQueue（Lock-free MessageQueue）迁移检测工具包
// Design Reference: memory/agency/designs/PRD-198-Android-17-DeliQueue-迁移检测工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
//
// DeliQueue (API 37+) replaces synchronized priority queue with a Treibier
// stack + min-heap single-threaded architecture, eliminating UI thread lock
// contention. However, all reflection-based access to MessageQueue private
// fields (e.g., mMessages) will break.
//
// This toolkit provides 8 developer tools:
//   1. MessageQueue 反射扫描器（Gradle Task/CLI）
//   2. DeliQueue 迁移指南（从反射迁移到公开 API）
//   3. MessageQueue 反射 CI 合规检测
//   4. DeliQueue 性能基准工具
//   5. DeliQueue × Perfetto 分析指南
//   6. DeliQueue 对第三方库影响扫描
//   7. DeliQueue 开发者迁移检查清单
//   8. DeliQueue 回退策略指南
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// RiskLevel — 反射风险等级
// ================================================================
/**
 * ============================================================
 * RiskLevel — 反射风险等级枚举
 * ============================================================
 * Represents the risk level of a MessageQueue reflection usage.
 *
 * P0: Runtime crash — invoke() directly fails after DeliQueue
 * P1: Behavioral anomaly — returns null or throws unexpected exception
 * P2: Potential risk — static analysis inference, may not trigger
 *
 * @param displayName 中文显示名称
 * @param color UI color for this risk level
 * @param priority Numeric priority (1=highest risk)
 */
enum class RiskLevel(
    val displayName: String,
    val displayNameEn: String,
    val emoji: String,
    val color: Color,
    val priority: Int
) {
    P0("运行时崩溃", "Runtime Crash", "🔴", Color(0xFFF85149), 1),      // Red — direct crash
    P1("行为异常", "Behavioral Anomaly", "🟡", Color(0xFFD29922), 2),   // Amber — null/exception
    P2("潜在风险", "Potential Risk", "🟢", Color(0xFF3FB950), 3),     // Green — static inference
    UNKNOWN("未知", "Unknown", "❓", Color(0xFF8B949E), 4)             // Gray — unable to assess
}

// ================================================================
// ReflectionTarget — 反射访问目标
// ================================================================
/**
 * ============================================================
 * ReflectionTarget — MessageQueue 反射访问目标
 * ============================================================
 * Represents a specific MessageQueue private field that is being
 * accessed via reflection.
 *
 * @property fieldName The private field name accessed
 * @property alternativeApi Public API alternative to use instead
 * @property descriptionZh 中文描述
 * @property descriptionEn English description
 */
enum class ReflectionTarget(
    val fieldName: String,
    val alternativeApi: String,
    val descriptionZh: String,
    val descriptionEn: String
) {
    M_MESSAGES(
        "mMessages",
        "Looper.myQueue()",
        "消息队列链表头，存储第一条待处理消息",
        "Linked list head of pending messages"
    ),
    M_QUOTA(
        "mQuota",
        "MessageQueue.quit() 行为观察",
        "消息队列配额限制",
        "MessageQueue quota limit"
    ),
    M_BLOCKING(
        "mBlocking",
        "MessageQueue 是阻塞还是非阻塞模式",
        "阻塞模式标志",
        "Blocking mode flag"
    ),
    M_THREAD(
        "mThread",
        "Looper.getThread()",
        "关联的 Looper 线程",
        "Associated Looper thread"
    ),
    OTHER(
        "其他私有字段",
        "对应的公开 API",
        "其他私有字段访问",
        "Other private field access"
    )
}

// ================================================================
// ScanPhase — 扫描阶段
// ================================================================
/**
 * ============================================================
 * ScanPhase — 扫描阶段枚举
 * ============================================================
 * Represents the current phase of the DeliQueue reflection scan.
 */
enum class ScanPhase {
    IDLE,                   // 初始空闲状态
    PARSING_PROJECT,        // 解析项目结构
    SCANNING_CODE,          // 扫描代码中的反射调用
    ANALYZING_DEPS,         // 分析第三方库依赖
    CALCULATING_IMPACT,     // 计算影响范围
    GENERATING_REPORT,       // 生成报告
    COMPLETED,              // 扫描完成
    ERROR                   // 扫描出错
}

// ================================================================
// ScanLog — 扫描日志条目
// ================================================================
/**
 * ============================================================
 * ScanLog — 扫描日志条目
 * ============================================================
 * Represents a single log entry during the scan process.
 *
 * @property timestamp Log timestamp (HH:mm:ss.SSS format)
 * @property level Log level (INFO/WARN/ERROR/SUCCESS)
 * @property message Log message
 */
data class ScanLog(
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

/**
 * ============================================================
 * LogLevel — 日志级别
 * ============================================================
 */
enum class LogLevel(val color: Color, val prefix: String) {
    INFO(Color(0xFF79C0FF), "INFO"),
    WARN(Color(0xFFD29922), "WARN"),
    ERROR(Color(0xFFF85149), "ERROR"),
    SUCCESS(Color(0xFF3FB950), "SUCCESS")
}

// ================================================================
// RiskFinding — 风险发现
// ================================================================
/**
 * ============================================================
 * RiskFinding — 风险发现
 * ============================================================
 * Represents a single finding from the MessageQueue reflection scan.
 *
 * @property filePath Source file path
 * @property lineNumber Line number in source file
 * @property codeSnippet Affected code snippet
 * @property target What private field is being accessed
 * @property riskLevel Risk severity
 * @property alternativeApi Recommended public API alternative
 * @property alternativeCode Recommended replacement code
 */
data class RiskFinding(
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val target: ReflectionTarget,
    val riskLevel: RiskLevel,
    val alternativeApi: String,
    val alternativeCode: String
)

// ================================================================
// LibraryFinding — 第三方库风险发现
// ================================================================
/**
 * ============================================================
 * LibraryFinding — 第三方库风险发现
 * ============================================================
 * Represents a third-party library that uses MessageQueue reflection.
 *
 * @property libraryName Library name (e.g., "com.squareup.okhttp:okhttp")
 * @property version Library version
 * @property affectedClasses List of affected class names
 * @property impactDescription Impact description
 * @property recommendation Recommendation (upgrade/update/remove)
 */
data class LibraryFinding(
    val libraryName: String,
    val version: String,
    val affectedClasses: List<String>,
    val impactDescription: String,
    val recommendation: LibraryRecommendation
)

/**
 * ============================================================
 * LibraryRecommendation — 库处理建议
 * ============================================================
 */
enum class LibraryRecommendation(val displayName: String) {
    UPGRADE("升级版本"),
    UPDATE("更新库"),
    REMOVE("移除库"),
    MONITOR("持续监控"),
    REPLACE("替换库")
}

// ================================================================
// DeliQueueScanState — 扫描器状态（MVI State）
// ================================================================
/**
 * ============================================================
 * DeliQueueScanState — 扫描器状态（MVI State）
 * ============================================================
 * Immutable state representing the DeliQueue scanner's current status.
 *
 * @property phase Current scan phase
 * @property progress Overall progress percentage (0-100)
 * @property logs List of scan log entries
 * @property findings List of detected risk findings
 * @property libraryFindings List of third-party library findings
 * @property scannedFilesCount Number of files scanned
 * @property startTimeMs Scan start timestamp (milliseconds)
 * @property endTimeMs Scan end timestamp (milliseconds)
 * @property errorMessage Error message if phase is ERROR
 */
data class DeliQueueScanState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val progress: Int = 0,
    val logs: List<ScanLog> = emptyList(),
    val findings: List<RiskFinding> = emptyList(),
    val libraryFindings: List<LibraryFinding> = emptyList(),
    val scannedFilesCount: Int = 0,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L,
    val errorMessage: String? = null
) {
    /**
     * 是否正在扫描中
     * Whether a scan is currently in progress
     */
    val isScanning: Boolean
        get() = phase != ScanPhase.IDLE &&
                phase != ScanPhase.COMPLETED &&
                phase != ScanPhase.ERROR

    /**
     * 扫描总耗时（毫秒）
     * Total scan duration in milliseconds
     */
    val durationMs: Long
        get() = if (endTimeMs > 0 && startTimeMs > 0) endTimeMs - startTimeMs else 0L

    /**
     * P0 风险数量
     */
    val p0Count: Int
        get() = findings.count { it.riskLevel == RiskLevel.P0 }

    /**
     * P1 风险数量
     */
    val p1Count: Int
        get() = findings.count { it.riskLevel == RiskLevel.P1 }

    /**
     * P2 风险数量
     */
    val p2Count: Int
        get() = findings.count { it.riskLevel == RiskLevel.P2 }

    /**
     * 受影响的第三方库数量
     */
    val affectedLibraryCount: Int
        get() = libraryFindings.size

    /**
     * 按风险等级分组
     * Findings grouped by risk level
     */
    val findingsByRisk: Map<RiskLevel, List<RiskFinding>>
        get() = findings.groupBy { it.riskLevel }
}

// ================================================================
// ImpactDimension — 影响分析维度
// ================================================================
/**
 * ============================================================
 * ImpactDimension — 影响分析维度
 * ============================================================
 */
enum class ImpactDimension {
    FILE,    // 按文件维度展示
    LIBRARY  // 按第三方库维度展示
}

// ================================================================
// MigrationStep — 迁移向导步骤
// ================================================================
/**
 * ============================================================
 * MigrationStep — 迁移向导步骤
 * ============================================================
 * Represents the 4-step migration wizard steps.
 */
enum class MigrationStep(val stepNumber: Int, val title: String, val titleEn: String) {
    IDENTIFY(1, "识别", "Identify"),     // Step 1: Identify affected methods
    EVALUATE(2, "评估", "Evaluate"),     // Step 2: Evaluate alternatives
    FIX(3, "修复", "Fix"),               // Step 3: Apply the fix
    VERIFY(4, "验证", "Verify")          // Step 4: Run CI verification
}

// ================================================================
// MigrationWizardState — 迁移向导状态（MVI State）
// ================================================================
/**
 * ============================================================
 * MigrationWizardState — 迁移向导状态（MVI State）
 * ============================================================
 * State for the migration wizard (Tab 3 of the UI).
 *
 * @property currentStep Current wizard step (0-3)
 * @property selectedFinding Currently selected finding to fix
 * @property generatedDiff Generated code diff for the fix
 * @property isApplyingFix Whether a fix is being applied
 * @property fixAppliedSuccessfully Whether the fix was successfully applied
 */
data class MigrationWizardState(
    val currentStep: MigrationStep = MigrationStep.IDENTIFY,
    val selectedFinding: RiskFinding? = null,
    val generatedDiff: String = "",
    val isApplyingFix: Boolean = false,
    val fixAppliedSuccessfully: Boolean = false,
    val ciVerificationPassed: Boolean = false
) {
    /**
     * 当前步骤索引（0-based）
     */
    val currentStepIndex: Int
        get() = currentStep.stepNumber - 1

    /**
     * 是否为第一步
     */
    val isFirstStep: Boolean
        get() = currentStep == MigrationStep.IDENTIFY

    /**
     * 是否为最后一步
     */
    val isLastStep: Boolean
        get() = currentStep == MigrationStep.VERIFY
}

// ================================================================
// DeliQueueIntent — 用户意图（MVI Intent）
// ================================================================
/**
 * ============================================================
 * DeliQueueIntent — DeliQueue 工具用户意图（MVI Intent）
 * ============================================================
 * User intentions that the ViewModel processes.
 */
sealed class DeliQueueIntent {
    /**
     * 设置项目路径
     * Set the project path for scanning
     *
     * @property path Project root path
     */
    data class SetProjectPath(val path: String) : DeliQueueIntent()

    /**
     * 开始扫描
     * Start the MessageQueue reflection scan
     */
    data object StartScan : DeliQueueIntent()

    /**
     * 取消扫描
     * Cancel ongoing scan
     */
    data object CancelScan : DeliQueueIntent()

    /**
     * 清除扫描结果
     * Clear scan results
     */
    data object ClearResults : DeliQueueIntent()

    /**
     * 切换主 Tab
     * Switch main tab (0=Scanner, 1=Impact Analysis, 2=Migration Wizard)
     *
     * @property tabIndex Tab index
     */
    data class SwitchTab(val tabIndex: Int) : DeliQueueIntent()

    /**
     * 切换影响分析维度
     * Switch impact analysis dimension
     *
     * @property dimension FILE or LIBRARY
     */
    data class SetImpactDimension(val dimension: ImpactDimension) : DeliQueueIntent()

    /**
     * 选择风险发现项
     * Select a risk finding for migration
     *
     * @property finding The finding to select
     */
    data class SelectFinding(val finding: RiskFinding) : DeliQueueIntent()

    /**
     * 清除风险选择
     * Clear risk item selection
     */
    data object ClearFindingSelection : DeliQueueIntent()

    /**
     * 迁移向导下一步
     * Move to next step in migration wizard
     */
    data object NextStep : DeliQueueIntent()

    /**
     * 迁移向导上一步
     * Move to previous step in migration wizard
     */
    data object PreviousStep : DeliQueueIntent()

    /**
     * 应用修复
     * Apply the fix to selected finding
     *
     * @property finding The finding to fix
     */
    data class ApplyFix(val finding: RiskFinding) : DeliQueueIntent()

    /**
     * 运行 CI 验证
     * Run CI compliance verification
     */
    data object RunCIVerification : DeliQueueIntent()

    /**
     * 复制到剪贴板
     * Copy text to clipboard
     *
     * @property text Text to copy
     */
    data class CopyToClipboard(val text: String) : DeliQueueIntent()
}

// ================================================================
// DeliQueueEffect — 副作用（MVI Effect）
// ================================================================
/**
 * ============================================================
 * DeliQueueEffect — DeliQueue 工具副作用（MVI Effect）
 * ============================================================
 * One-time side effects emitted via Channel.
 */
sealed class DeliQueueEffect {
    /**
     * Toast 消息
     * Show toast message
     *
     * @property message Toast message text
     */
    data class ShowToast(val message: String) : DeliQueueEffect()

    /**
     * 复制到剪贴板
     * Copy text to clipboard
     *
     * @property text Text that was copied
     */
    data class CopiedToClipboard(val text: String) : DeliQueueEffect()

    /**
     * 扫描完成
     * Scan completed event
     *
     * @property findingCount Total number of findings
     * @property libraryCount Number of affected libraries
     * @property durationMs Scan duration in milliseconds
     */
    data class ScanCompleted(
        val findingCount: Int,
        val libraryCount: Int,
        val durationMs: Long
    ) : DeliQueueEffect()

    /**
     * 终端输出
     * Terminal colored output event
     *
     * @property message Output message
     * @property level Log level
     */
    data class TerminalOutput(
        val message: String,
        val level: LogLevel
    ) : DeliQueueEffect()

    /**
     * CI 验证结果
     * CI verification result
     *
     * @property passed Whether CI verification passed
     * @property message Result message
     */
    data class CIVerificationResult(
        val passed: Boolean,
        val message: String
    ) : DeliQueueEffect()

    /**
     * 错误事件
     * Error event
     *
     * @property message Error message
     * @property throwable Optional original exception
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : DeliQueueEffect()
}

// ================================================================
// DeliQueueState — 主状态（MVI State）
// ================================================================
/**
 * ============================================================
 * DeliQueueState — DeliQueue 工具主状态（MVI State）
 * ============================================================
 * Main state for the DeliQueue Migration Toolkit.
 *
 * @property currentTab Current tab (0=Scanner, 1=Impact Analysis, 2=Migration Wizard)
 * @property projectPath Project root path for scanning
 * @property scanState Current scan state
 * @property impactDimension Current impact analysis dimension
 * @property migrationWizard Migration wizard state
 */
data class DeliQueueState(
    val currentTab: Int = 0,                          // 0=Scanner, 1=Impact, 2=Migration
    val projectPath: String = "",
    val scanState: DeliQueueScanState = DeliQueueScanState(),
    val impactDimension: ImpactDimension = ImpactDimension.FILE,
    val migrationWizard: MigrationWizardState = MigrationWizardState()
) {
    /**
     * 总体风险描述
     * Overall risk description
     */
    val overallRiskDescription: String
        get() = when {
            scanState.p0Count > 0 -> "🔴 检测到 ${scanState.p0Count} 个 P0 运行时崩溃风险，必须立即修复！"
            scanState.p1Count > 0 -> "🟡 检测到 ${scanState.p1Count} 个 P1 行为异常风险，需要尽快处理。"
            scanState.p2Count > 0 -> "🟢 检测到 ${scanState.p2Count} 个 P2 潜在风险，建议关注。"
            scanState.findings.isEmpty() && scanState.phase == ScanPhase.COMPLETED ->
                "✅ 未检测到 MessageQueue 反射用法，项目已适配 DeliQueue！"
            else -> "🔍 点击「开始扫描」检测项目中的 MessageQueue 反射用法。"
        }

    /**
     * 是否显示扫描结果
     * Whether to show scan results
     */
    val showScanResults: Boolean
        get() = scanState.phase == ScanPhase.COMPLETED && scanState.findings.isNotEmpty()

    /**
     * 是否显示空状态（无风险）
     * Whether to show empty state (no risks)
     */
    val showEmptyState: Boolean
        get() = scanState.phase == ScanPhase.COMPLETED && scanState.findings.isEmpty()
}
