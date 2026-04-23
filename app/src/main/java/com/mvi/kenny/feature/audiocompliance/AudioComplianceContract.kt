package com.mvi.kenny.feature.audiocompliance

/**
 * ============================================================
 * AudioComplianceContract — 后台音频限制检测 MVI 契约
 * ============================================================
 * PRD-120 | Android 17 后台音频限制检测与合规迁移工具包
 *
 * MVI 三要素:
 * - State: 页面状态的唯一真相来源，Immutable data class
 * - Intent: 用户意图，ViewModel 收到后执行业务逻辑
 * - Effect: 一次性副作用，通过 Channel 传递
 *
 * @see AudioComplianceViewModel
 * @see AudioComplianceScreen
 */

// ================================================================
// Tab index constants / Tab 索引常量
// ================================================================
object AudioComplianceTab {
    const val DASHBOARD = 0
    const val SCANNER = 1
    const val SIMULATOR = 2
    const val REPORT = 3
    const val FGS_GUIDE = 4
    const val FALLBACKS = 5
    const val REGRESSION = 6
}

// ================================================================
// Severity levels / 严重程度级别
// ================================================================
/**
 * 音频 API 受影响严重程度
 *
 * BLOCKED: 直接阻断 — 后台调用 AudioTrack/MediaPlayer.start() 无 FGS 保护
 * DEGRADED: 行为降级 — AudioFocusRequest 声明了 AUDIOFOCUS_GAIN 但未处理焦点丢失恢复
 * SAFE: 无影响 — 在 Lifecycle.STARTED/RESUMED 状态下调用，或已有 FGS 保护
 */
enum class AudioSeverity {
    BLOCKED,  // 🔴 直接阻断
    DEGRADED, // 🟡 行为降级
    SAFE      // 🟢 无影响
}

// ================================================================
// Audio scenario / 音频场景
// ================================================================
/**
 * 音频场景预设
 *
 * @param id 场景 ID
 * @param name 场景名称
 * @param description 场景描述
 * @param affectedApis 受影响的 API 列表
 */
data class AudioScenario(
    val id: String,
    val name: String,
    val description: String,
    val affectedApis: List<String>
)

// ================================================================
// Audio API call / 受影响 API 调用
// ================================================================
/**
 * 受影响的音频 API 调用记录
 *
 * @param id 调用 ID
 * @param apiName API 名称
 * @param filePath 源文件路径
 * @param lineNumber 代码行号
 * @param severity 严重程度
 * @param description 场景描述
 * @param fixSuggestion 修复建议
 */
data class AudioApiCall(
    val id: Long,
    val apiName: String,
    val filePath: String,
    val lineNumber: Int,
    val severity: AudioSeverity,
    val description: String,
    val fixSuggestion: String
)

// ================================================================
// Simulation result / 模拟结果
// ================================================================
/**
 * 模拟结果
 *
 * @param scenarioId 场景 ID
 * @param scenarioName 场景名称
 * @param expectedBehavior 预期行为（NORMAL/BLOCKED/DEGRADED）
 * @param logLines 模拟日志行
 */
enum class SimulationBehavior { NORMAL, BLOCKED, DEGRADED }

data class SimulationResult(
    val scenarioId: String,
    val scenarioName: String,
    val behavior: SimulationBehavior,
    val detail: String
)

// ================================================================
// FGS config / 前台服务配置
// ================================================================
/**
 * FGS（前台服务）配置检测结果
 *
 * @param hasMediaPlaybackService 是否有媒体播放 FGS
 * @param hasMediaProcessingService 是否有媒体处理 FGS
 * @param hasOtherFgs 是否有其他 FGS
 * @param isCompliant 是否合规
 * @param missingTypes 缺失的 FGS 类型列表
 */
data class FgsConfig(
    val hasMediaPlaybackService: Boolean,
    val hasMediaProcessingService: Boolean,
    val hasOtherFgs: Boolean,
    val isCompliant: Boolean,
    val missingTypes: List<String>
)

// ================================================================
// Fallback solution / 降级方案
// ================================================================
/**
 * 音频焦点替代方案
 *
 * @param id 方案 ID
 * @param name 方案名称
 * @param适用场景 scenarios this applies to
 * @param implementationDifficulty 实现难度（EASY/MEDIUM/HARD）
 * @param effectDescription 效果描述
 * @param steps 实施步骤
 */
data class FallbackSolution(
    val id: String,
    val name: String,
    val applicableScenarios: List<String>,
    val implementationDifficulty: String, // EASY / MEDIUM / HARD
    val effectDescription: String,
    val steps: List<String>
)

// ================================================================
// Compliance report / 合规报告
// ================================================================
/**
 * 音频限制合规报告
 *
 * @param totalApis 总 API 调用数
 * @param blockedCount 阻断数量
 * @param degradedCount 降级数量
 * @param safeCount 安全数量
 * @param healthScore 健康度评分 (0-100)
 * @param summary 报告摘要
 * @param moduleBreakdown 按模块分组的详情
 */
data class AudioComplianceReport(
    val totalApis: Int,
    val blockedCount: Int,
    val degradedCount: Int,
    val safeCount: Int,
    val healthScore: Int,
    val summary: String,
    val moduleBreakdown: Map<String, List<AudioApiCall>>
)

// ================================================================
// Test case / 测试用例
// ================================================================
/**
 * 音频回归测试用例
 *
 * @param id 用例 ID
 * @param name 用例名称
 * @param scenario 测试场景
 * @param expectedBehavior 预期行为
 * @param verificationSteps 验证步骤
 * @param lastResult 上次结果（PASS/FAIL/null）
 */
enum class TestResultStatus { PASS, FAIL, PENDING }

data class AudioTestCase(
    val id: String,
    val name: String,
    val scenario: String,
    val expectedBehavior: String,
    val verificationSteps: List<String>,
    val lastResult: TestResultStatus = TestResultStatus.PENDING
)

data class TestResult(
    val caseId: String,
    val status: TestResultStatus,
    val executedAt: Long,
    val message: String
)

// ================================================================
// State / 页面状态
// ================================================================
/**
 * 页面状态 — 音频合规检测工具的全局状态容器
 *
 * @param selectedTab 当前 Tab 索引
 * @param healthScore 合规健康度评分 (0-100)
 * @param affectedApiCount 受影响 API 总数
 * @param blockedCount 阻断数量
 * @param degradedCount 降级数量
 * @param safeCount 安全数量
 * // T2 Scanner
 * @param scanProgress 扫描进度 (0f-1f)
 * @param isScanning 是否正在扫描
 * @param scanResults 扫描结果列表
 * @param severityFilter 当前严重程度过滤（null 表示全部）
 * // T3 Simulator
 * @param selectedScenario 选中的音频场景
 * @param simulationResults 模拟结果列表
 * @param simulationLogs 模拟日志
 * // T4 Report
 * @param reportData 合规报告数据
 * // T5 FGS Guide
 * @param currentFgsConfig 当前 FGS 配置
 * @param fgsRecommendation FGS 配置建议
 * @param generatedFgsTemplate 生成的 FGS 模板代码
 * // T6 Fallbacks
 * @param fallbackSolutions 替代方案列表
 * @param expandedFallbackId 展开的方案 ID
 * // T7 Regression
 * @param testCases 测试用例列表
 * @param testResults 测试结果列表
 * @param isRunningTests 是否正在运行测试
 * @param isAddTestDialogVisible 新增用例对话框是否可见
 */
data class AudioComplianceState(
    val selectedTab: Int = AudioComplianceTab.DASHBOARD,
    val healthScore: Int = 0,
    val affectedApiCount: Int = 0,
    val blockedCount: Int = 0,
    val degradedCount: Int = 0,
    val safeCount: Int = 0,
    // T2 Scanner
    val scanProgress: Float = 0f,
    val isScanning: Boolean = false,
    val scanResults: List<AudioApiCall> = emptyList(),
    val severityFilter: AudioSeverity? = null,
    // T3 Simulator
    val selectedScenario: AudioScenario? = null,
    val simulationResults: List<SimulationResult> = emptyList(),
    val simulationLogs: List<String> = emptyList(),
    // T4 Report
    val reportData: AudioComplianceReport? = null,
    // T5 FGS Guide
    val currentFgsConfig: FgsConfig? = null,
    val fgsRecommendation: String = "",
    val generatedFgsTemplate: String? = null,
    // T6 Fallbacks
    val fallbackSolutions: List<FallbackSolution> = emptyList(),
    val expandedFallbackId: String? = null,
    // T7 Regression
    val testCases: List<AudioTestCase> = emptyList(),
    val testResults: List<TestResult> = emptyList(),
    val isRunningTests: Boolean = false,
    val isAddTestDialogVisible: Boolean = false,
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = AudioComplianceState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================
/**
 * 用户意图 — 定义用户可以执行的操作
 *
 * @see AudioComplianceViewModel.sendIntent
 */
sealed interface AudioComplianceIntent {
    /** 切换 Tab / Select tab */
    data class SelectTab(val index: Int) : AudioComplianceIntent

    // T2 Scanner
    /** 开始扫描 / Start scan */
    data object StartScan : AudioComplianceIntent()

    /** 按严重程度过滤 / Filter by severity */
    data class FilterBySeverity(val severity: AudioSeverity?) : AudioComplianceIntent()

    /** 选中扫描结果 / Select scan result */
    data class SelectScanResult(val result: AudioApiCall) : AudioComplianceIntent()

    // T3 Simulator
    /** 选择音频场景 / Select audio scenario */
    data class SelectScenario(val scenario: AudioScenario) : AudioComplianceIntent()

    /** 运行模拟 / Run simulation */
    data object RunSimulation : AudioComplianceIntent()

    // T5 FGS Guide
    /** 检测 FGS 配置 / Detect FGS config */
    data object DetectFgsConfig : AudioComplianceIntent()

    /** 生成 FGS 模板 / Generate FGS template */
    data object GenerateFgsTemplate : AudioComplianceIntent()

    // T6 Fallbacks
    /** 展开/折叠降级方案 / Toggle fallback detail */
    data class ToggleFallbackDetail(val id: String) : AudioComplianceIntent()

    // T7 Regression
    /** 运行测试套件 / Run test suite */
    data object RunTestSuite : AudioComplianceIntent()

    /** 新增测试用例 / Add test case */
    data class AddTestCase(val testCase: AudioTestCase) : AudioComplianceIntent()

    /** 显示/隐藏新增用例对话框 / Toggle add test dialog */
    data class SetAddTestDialogVisible(val visible: Boolean) : AudioComplianceIntent()
}

// ================================================================
// Effect / 副作用
// ================================================================
/**
 * 副作用 — 一次性操作，通过 Channel 传递
 *
 * @see AudioComplianceViewModel
 */
sealed interface AudioComplianceEffect {
    /** 显示 Toast / Show toast */
    data class ShowToast(val message: String) : AudioComplianceEffect()

    /** 复制到剪贴板 / Copy to clipboard */
    data class CopyToClipboard(val text: String) : AudioComplianceEffect()

    /** 分享报告 / Share report */
    data class ShareReport(val content: String) : AudioComplianceEffect()
}
