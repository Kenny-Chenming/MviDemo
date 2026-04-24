package com.mvi.kenny.feature.audiohardening

// ================================================================
// AudioHardeningContract — Android 17 Background Audio Hardening MVI 契约
// ================================================================
// MVI architecture contract for Android 17 Background Audio Hardening toolkit.
//
// PRD-145: Android 17 Background Audio Hardening 合规检测与 Foreground Service 迁移工具包
// Design Reference: memory/agency/designs/PRD-145-Android-17-Background-Audio-Hardening-合规检测与-Foreground-Service-迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * ToolId — 工具唯一标识枚举
 * ============================================================
 *
 * @param titleCn 中文名称
 * @param titleEn 英文名称
 * @param icon Material 图标
 * @param description 工具描述
 */
enum class AudioToolId(
    val titleCn: String,
    val titleEn: String,
    val description: String
) {
    Scanner("自动化检测器", "AudioComplianceScanner", "Gradle 插件 + CLI 自动化检测 Background Audio Hardening 合规问题"),
    Monitor("静默失败监控", "SilentFailureMonitor", "实时监控后台音频 API 静默失败事件"),
    FGSGenerator("FGS 配置生成器", "FGSConfigGenerator", "生成 while-in-use Foreground Service 配置"),
    MediaSessionBinder("MediaSession 绑定", "MediaSessionBinder", "分步骤引导绑定 MediaSession 到后台音频"),
    AudioComplianceCI("合规 CI 检测", "AudioComplianceCI", "CI 流水线合规检测，输出 GitHub/GitLab Action"),
    AudioFocusDegradation("Audio Focus 降级", "AudioFocusDegradation", "音频焦点降级策略模板"),
    AudioRegressionTest("回归测试框架", "AudioRegressionTest", "Android 16→17 音频行为回归测试"),
    AudioDebugPanel("可视化调试工具", "AudioHardeningDebugPanel", "API 调用链路节点图可视化调试"),
    AudioFallbackPath("降级路径检测", "AudioFallbackPathDetector", "检测音频降级路径 fallback 逻辑是否存在")
}

/**
 * ============================================================
 * ToolStatus — 工具状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji 标识
 * @param colorHex 颜色值
 */
enum class ToolStatus(val displayName: String, val emoji: String, val colorHex: Long) {
    Pass("通过", "✅", 0xFF4CAF50),
    Warning("风险", "⚠️", 0xFFFFC107),
    NotChecked("待检测", "🔍", 0xFF9E9E9E),
    Scanning("扫描中", "⏳", 0xFF2196F3)
}

/**
 * ============================================================
 * ScanState — 扫描状态枚举
 * ============================================================
 */
enum class ScanState(val displayName: String) {
    Idle("空闲"),
    Scanning("扫描中"),
    Success("已完成"),
    Error("错误")
}

/**
 * ============================================================
 * RiskLevel — 风险等级
 * ============================================================
 */
enum class RiskLevel(val displayName: String, val emoji: String, val colorHex: Long) {
    P0("P0 阻断", "🔴", 0xFFF44336),
    P1("P1 警告", "🟡", 0xFFFFC107),
    P2("P2 建议", "🟢", 0xFF4CAF50)
}

/**
 * ============================================================
 * ScanScope — 扫描范围
 * ============================================================
 */
sealed class ScanScope {
    object All : ScanScope()
    data class Module(val moduleName: String) : ScanScope()
    object AffectedOnly : ScanScope()
}

/**
 * ============================================================
 * FGSUseCase — FGS 使用场景
 * ============================================================
 */
enum class FGSUseCase(val titleCn: String, val titleEn: String) {
    MusicPlayer("音乐播放", "Music Player"),
    Navigation("导航播报", "Navigation"),
    Audiobook("有声书", "Audiobook"),
    Fitness("健身语音", "Fitness Voice"),
    AIAgent("AI 语音助手", "AI Voice Assistant"),
    Custom("自定义", "Custom")
}

/**
 * ============================================================
 * AudioFocusStrategy — 音频焦点降级策略
 * ============================================================
 */
enum class AudioFocusStrategy(val titleCn: String, val description: String) {
    PauseAndWait("暂停播放", "暂停播放，等待焦点恢复后继续"),
    DuckAndRestore("降低音量", "降低音量（duck），恢复后复原音量"),
    DuckAndPause("混合模式", "降低音量后暂停，焦点恢复时播放")
}

/**
 * ============================================================
 * DeviceConnectionState — 设备连接状态
 * ============================================================
 */
enum class DeviceConnectionState(val titleCn: String, val emoji: String) {
    WiredHeadphones("有线耳机", "🎧"),
    BluetoothA2DP("蓝牙 A2DP", "📶"),
    Speaker("扬声器", "🔊"),
    Unknown("未知", "❓")
}

/**
 * ============================================================
 * AudioHardeningDashboardState — Dashboard 状态（MVI State）
 * ============================================================
 *
 * @param deviceInfo 设备信息
 * @param complianceSummary 合规摘要
 * @param toolStatuses 工具状态 Map
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度
 * @param selectedTool 当前选中的工具（导航用）
 * @param errorMessage 错误信息
 */
data class AudioHardeningDashboardState(
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val complianceSummary: ComplianceSummary = ComplianceSummary(),
    val toolStatuses: Map<AudioToolId, ToolStatus> = emptyMap(),
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val selectedTool: AudioToolId? = null,
    val errorMessage: String? = null
) {
    companion object {
        val Initial = AudioHardeningDashboardState()
    }
}

/**
 * ============================================================
 * AudioHardeningDashboardIntent — Dashboard 用户意图
 * ============================================================
 */
sealed interface AudioHardeningDashboardIntent {
    data object RunFullScan : AudioHardeningDashboardIntent
    data class OpenTool(val toolId: AudioToolId) : AudioHardeningDashboardIntent
    data object ExportReport : AudioHardeningDashboardIntent
    data object DismissError : AudioHardeningDashboardIntent
    data class SelectTool(val toolId: AudioToolId) : AudioHardeningDashboardIntent
}

/**
 * ============================================================
 * AudioHardeningDashboardEffect — Dashboard 副作用
 * ============================================================
 */
sealed interface AudioHardeningDashboardEffect {
    data class NavigateToTool(val toolId: AudioToolId) : AudioHardeningDashboardEffect
    data class ShowSnackbar(val message: String) : AudioHardeningDashboardEffect
    data class ShareReport(val filePath: String) : AudioHardeningDashboardEffect
}

// ================================================================
// 工具详情页 State / Tool Detail States
// ================================================================

/**
 * ============================================================
 * AudioComplianceScannerState — 自动化检测器状态
 * ============================================================
 */
data class AudioComplianceScannerState(
    val scanScope: ScanScope = ScanScope.All,
    val scanResults: List<ScanResult> = emptyList(),
    val isScanning: Boolean = false,
    val progress: Float = 0f,
    val selectedFilter: RiskLevel? = null,
    val expandedResultId: String? = null,
    val scanState: ScanState = ScanState.Idle
)

sealed interface AudioComplianceScannerIntent {
    data class SetScanScope(val scope: ScanScope) : AudioComplianceScannerIntent
    data object StartScan : AudioComplianceScannerIntent
    data object CancelScan : AudioComplianceScannerIntent
    data class ToggleResultExpand(val resultId: String) : AudioComplianceScannerIntent
    data class SetRiskFilter(val riskLevel: RiskLevel?) : AudioComplianceScannerIntent
    data object GenerateFixDiff : AudioComplianceScannerIntent
}

sealed interface AudioComplianceScannerEffect {
    data class ShowSnackbar(val message: String) : AudioComplianceScannerEffect
    data class ShareDiff(val diffContent: String) : AudioComplianceScannerEffect
}

/**
 * ============================================================
 * SilentFailureMonitorState — 静默失败监控状态
 * ============================================================
 */
data class SilentFailureMonitorState(
    val isMonitoring: Boolean = false,
    val events: List<AudioAPIEvent> = emptyList(),
    val selectedFilter: String? = null,
    val filterOptions: List<String> = listOf("MediaPlayer", "AudioManager", "AudioFocusRequest", "ExoPlayer"),
    val statistics: Map<String, Int> = emptyMap()
)

sealed interface SilentFailureMonitorIntent {
    data object ToggleMonitoring : SilentFailureMonitorIntent
    data class SetApiFilter(val apiType: String?) : SilentFailureMonitorIntent
    data object ClearEvents : SilentFailureMonitorIntent
}

sealed interface SilentFailureMonitorEffect {
    data class ShowSnackbar(val message: String) : SilentFailureMonitorEffect
}

/**
 * ============================================================
 * FGSConfigGeneratorState — FGS 配置生成器状态
 * ============================================================
 */
data class FGSConfigGeneratorState(
    val selectedUseCase: FGSUseCase = FGSUseCase.MusicPlayer,
    val generatedManifest: String = "",
    val generatedPermissions: List<String> = emptyList(),
    val generatedCapabilities: List<String> = emptyList(),
    val bestPracticeTips: List<String> = emptyList()
)

sealed interface FGSConfigGeneratorIntent {
    data class SelectUseCase(val useCase: FGSUseCase) : FGSConfigGeneratorIntent
    data object CopyManifest : FGSConfigGeneratorIntent
    data object ApplyToProject : FGSConfigGeneratorIntent
}

sealed interface FGSConfigGeneratorEffect {
    data class ShowSnackbar(val message: String) : FGSConfigGeneratorEffect
}

/**
 * ============================================================
 * MediaSessionBinderState — MediaSession 绑定状态
 * ============================================================
 */
data class MediaSessionBinderState(
    val currentStep: Int = 0,
    val totalSteps: Int = 3,
    val stepTitles: List<String> = listOf(
        "创建 MediaSessionCompat.Callback 子类",
        "在 Service 中初始化 MediaSession",
        "绑定音频播放到 MediaSession"
    ),
    val codeTemplates: List<String> = emptyList()
)

sealed interface MediaSessionBinderIntent {
    data object NextStep : MediaSessionBinderIntent
    data object PreviousStep : MediaSessionBinderIntent
    data object CopyAllCode : MediaSessionBinderIntent
}

sealed interface MediaSessionBinderEffect {
    data class ShowSnackbar(val message: String) : MediaSessionBinderEffect
}

/**
 * ============================================================
 * AudioComplianceCIState — 合规 CI 检测状态
 * ============================================================
 */
data class AudioComplianceCIState(
    val selectedTargetSdk: Int = 37,
    val targetSdkOptions: List<Int> = listOf(36, 37),
    val complianceItems: List<CIComplianceItem> = emptyList(),
    val warningItems: List<CIComplianceItem> = emptyList(),
    val violationItems: List<CIComplianceItem> = emptyList(),
    val isGenerating: Boolean = false
)

data class CIComplianceItem(
    val id: String,
    val title: String,
    val description: String,
    val status: ToolStatus
)

sealed interface AudioComplianceCIIntent {
    data class SelectTargetSdk(val version: Int) : AudioComplianceCIIntent
    data object GenerateGitHubAction : AudioComplianceCIIntent
    data object GenerateGitLabCI : AudioComplianceCIIntent
    data class ExportReport(val format: String) : AudioComplianceCIIntent
}

sealed interface AudioComplianceCIEffect {
    data class ShowSnackbar(val message: String) : AudioComplianceCIEffect
    data class ShareYAML(val yamlContent: String, val filename: String) : AudioComplianceCIEffect
}

/**
 * ============================================================
 * AudioFocusDegradationState — Audio Focus 降级策略状态
 * ============================================================
 */
data class AudioFocusDegradationState(
    val selectedStrategy: AudioFocusStrategy = AudioFocusStrategy.PauseAndWait,
    val customStrategyCode: String = "",
    val previewBehaviors: Map<String, String> = emptyMap()
)

sealed interface AudioFocusDegradationIntent {
    data class SelectStrategy(val strategy: AudioFocusStrategy) : AudioFocusDegradationIntent
    data class UpdateCustomCode(val code: String) : AudioFocusDegradationIntent
    data object CopyCode : AudioFocusDegradationIntent
}

sealed interface AudioFocusDegradationEffect {
    data class ShowSnackbar(val message: String) : AudioFocusDegradationEffect
}

/**
 * ============================================================
 * AudioRegressionTestState — 回归测试框架状态
 * ============================================================
 */
data class AudioRegressionTestState(
    val selectedVersions: Set<Int> = setOf(16, 17),
    val selectedScenarios: Set<RegressionScenario> = setOf(RegressionScenario.BackgroundPlayback),
    val testResults: List<TestResult> = emptyList(),
    val isRunning: Boolean = false,
    val ciIntegrationHint: String = ""
)

enum class RegressionScenario(val titleCn: String, val titleEn: String) {
    BackgroundPlayback("后台音频播放", "Background Audio Playback"),
    AudioFocusRequest("音频焦点请求", "Audio Focus Request"),
    VolumeChange("音量变更", "Volume Change")
}

data class TestResult(
    val scenario: RegressionScenario,
    val version: Int,
    val passed: Boolean,
    val log: String = ""
)

sealed interface AudioRegressionTestIntent {
    data class ToggleVersion(val version: Int) : AudioRegressionTestIntent
    data class ToggleScenario(val scenario: RegressionScenario) : AudioRegressionTestIntent
    data object RunTests : AudioRegressionTestIntent
    data object ViewCIIntegration : AudioRegressionTestIntent
}

sealed interface AudioRegressionTestEffect {
    data class ShowSnackbar(val message: String) : AudioRegressionTestEffect
}

/**
 * ============================================================
 * AudioHardeningDebugPanelState — 可视化调试工具状态
 * ============================================================
 */
data class AudioHardeningDebugPanelState(
    val apiNodes: List<APINode> = emptyList(),
    val selectedFilter: NodeFilter = NodeFilter.All,
    val timelineEvents: List<TimelineEvent> = emptyList()
)

enum class NodeFilter(val displayName: String) {
    All("全部"),
    Success("成功"),
    Failed("失败"),
    Pending("待调用")
}

data class APINode(
    val id: String,
    val methodName: String,
    val parameters: String,
    val returnValue: String,
    val timestamp: Long,
    val status: NodeStatus
)

enum class NodeStatus { Success, Failed, Pending }

data class TimelineEvent(
    val id: String,
    val methodName: String,
    val timestamp: Long,
    val duration: Long,
    val status: NodeStatus
)

sealed interface AudioHardeningDebugPanelIntent {
    data class SetNodeFilter(val filter: NodeFilter) : AudioHardeningDebugPanelIntent
    data class SelectNode(val nodeId: String) : AudioHardeningDebugPanelIntent
    data object ExportAsHAR : AudioHardeningDebugPanelIntent
    data object ExportAsJSON : AudioHardeningDebugPanelIntent
}

sealed interface AudioHardeningDebugPanelEffect {
    data class ShowSnackbar(val message: String) : AudioHardeningDebugPanelEffect
    data class ShareFile(val content: String, val filename: String) : AudioHardeningDebugPanelEffect
}

/**
 * ============================================================
 * AudioFallbackPathDetectorState — 降级路径检测状态
 * ============================================================
 */
data class AudioFallbackPathDetectorState(
    val deviceState: DeviceConnectionState = DeviceConnectionState.Unknown,
    val detectedPaths: List<FallbackPath> = emptyList(),
    val suggestedEnhancements: List<String> = emptyList()
)

data class FallbackPath(
    val id: String,
    val fromState: String,
    val toState: String,
    val hasFallbackLogic: Boolean,
    val codeSnippet: String = "",
    val suggestion: String = ""
)

sealed interface AudioFallbackPathDetectorIntent {
    data class SetDeviceState(val state: DeviceConnectionState) : AudioFallbackPathDetectorIntent
    data object RunDetection : AudioFallbackPathDetectorIntent
    data object ApplyEnhancements : AudioFallbackPathDetectorIntent
}

sealed interface AudioFallbackPathDetectorEffect {
    data class ShowSnackbar(val message: String) : AudioFallbackPathDetectorEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * DeviceInfo — 设备信息
 * ============================================================
 */
data class DeviceInfo(
    val androidVersion: Int = 0,
    val isBackgroundAudioHardeningSupported: Boolean = false,
    val deviceModel: String = ""
)

/**
 * ============================================================
 * ComplianceSummary — 合规摘要
 * ============================================================
 */
data class ComplianceSummary(
    val passCount: Int = 0,
    val warningCount: Int = 0,
    val notCheckedCount: Int = 9,
    val overallStatus: ToolStatus = ToolStatus.NotChecked
)

/**
 * ============================================================
 * ScanResult — 扫描结果
 * ============================================================
 */
data class ScanResult(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val riskLevel: RiskLevel,
    val description: String,
    val suggestedFix: String
)

/**
 * ============================================================
 * AudioAPIEvent — 音频 API 事件
 * ============================================================
 */
data class AudioAPIEvent(
    val id: String,
    val timestamp: Long,
    val apiType: String,
    val callingThread: String,
    val errorCode: Int,
    val stackTrace: String
)
