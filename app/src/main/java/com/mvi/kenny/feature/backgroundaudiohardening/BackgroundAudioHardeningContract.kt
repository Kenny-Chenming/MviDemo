package com.mvi.kenny.feature.backgroundaudiohardening

/**
 * ============================================================
 * BackgroundAudioHardeningContract — Android 17 Background Audio Hardening MVI 契约
 * ============================================================
 * MVI architecture contract for Background Audio Hardening compliance tool.
 *
 * Design Reference: memory/agency/designs/PRD-160-Android-17-Background-Audio-Hardening-后台音频加固迁移工具包.md
 * MVI 三要素 / Three pillars:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable function, consumes State, renders UI
 * - Intent: User intentions, ViewModel processes and updates State
 * Effect: One-time side effects (navigation, toast), delivered via Channel
 * —————————————————————————————————————————————————————
 */

/**
 * ============================================================
 * AuditStatus — 合规检测扫描状态枚举
 * ============================================================
 * Status of the background audio compliance audit scan.
 *
 * @param labelCN 中文显示名称
 */
enum class AuditStatus(val labelCN: String) {
    /** 初始空闲状态 */
    Idle("等待扫描"),
    /** 正在扫描项目 */
    Scanning("扫描中"),
    /** 扫描完成 */
    Completed("扫描完成"),
    /** 扫描出错 */
    Error("扫描失败")
}

/**
 * ============================================================
 * IssueSeverity — 问题严重级别枚举
 * ============================================================
 * Severity level for detected compliance issues.
 *
 * @param labelCN 中文显示名称
 * @param color 严重级别颜色（hex）
 */
enum class IssueSeverity(val labelCN: String, val color: String) {
    /** 必须修复的严重问题 */
    Critical("严重", "#DC3545"),
    /** 建议修复的警告 */
    Warning("警告", "#FD7E14"),
    /** 可选优化的建议 */
    Suggestion("建议", "#0D6EFD")
}

/**
 * ============================================================
 * ViolationType — 违规类型枚举
 * ============================================================
 * Type of background audio hardening violation detected.
 */
enum class ViolationType(val labelCN: String, val labelEN: String) {
    /** MediaPlayer.start() 在无前台生命周期时调用 */
    MediaPlayer_NoForeground("MediaPlayer.start() 无前台生命周期", "MediaPlayer.start() called without foreground lifecycle"),
    /** AudioManager.requestAudioFocus() 在后台调用 */
    AudioFocusRequest_InBackground("后台请求音频焦点", "AudioFocusRequest in background"),
    /** ExoPlayer 在无 MediaSession 关联的 Service 中播放 */
    ExoPlayer_NoMediaSession("ExoPlayer 无 MediaSession 关联", "ExoPlayer without MediaSession"),
    /** 后台 Service 缺少 mediaPlayback 类型声明 */
    ForegroundService_MissingMediaType("ForegroundService 缺少 mediaPlayback 类型", "ForegroundService missing mediaPlayback type"),
    /** AudioTrack.write() 在后台路径调用 */
    AudioTrack_InBackground("后台路径 AudioTrack.write()", "AudioTrack.write() in background path"),
    /** 播放服务未设置 SessionToken */
    MediaSession_NoToken("播放服务未设置 SessionToken", "PlaybackService without setSessionToken")
}

/**
 * ============================================================
 * ComplianceIssue — 合规问题数据模型
 * ============================================================
 * Represents a single background audio compliance issue.
 *
 * @param id 唯一标识符
 * @param type 违规类型
 * @param severity 严重级别
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param codeSnippet 违规代码片段
 * @param description 问题描述
 * @param fixSuggestion 修复建议
 * @param docsUrl Android 官方文档链接
 */
data class ComplianceIssue(
    val id: String,
    val type: ViolationType,
    val severity: IssueSeverity,
    val filePath: String,
    val lineNumber: Int,
    val codeSnippet: String,
    val description: String,
    val fixSuggestion: String,
    val docsUrl: String = "https://developer.android.com/about/versions/17/changes/bg-audio"
)

/**
 * ============================================================
 * MediaSessionTemplate — MediaSession 代码模板
 * ============================================================
 * Template for MediaSession integration code.
 *
 * @param name 模板名称
 * @param description 模板描述
 * @param code 模板代码内容
 * @param language 代码语言 (kotlin/java)
 */
data class MediaSessionTemplate(
    val name: String,
    val description: String,
    val code: String,
    val language: String = "kotlin"
)

/**
 * ============================================================
 * AuditResult — 扫描结果数据模型
 * ============================================================
 * Result of a background audio compliance audit scan.
 *
 * @param score 合规分数 (0-100)
 * @param totalIssues 总问题数
 * @param criticalCount 严重问题数
 * @param warningCount 警告问题数
 * @param suggestionCount 建议数
 * @param issues 问题列表
 * @param scanDurationMs 扫描耗时（毫秒）
 */
data class AuditResult(
    val score: Int,
    val totalIssues: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val suggestionCount: Int,
    val issues: List<ComplianceIssue>,
    val scanDurationMs: Long
)

/**
 * ============================================================
 * FGSConfig — Foreground Service 配置项
 * ============================================================
 * Foreground Service configuration for media playback.
 *
 * @param serviceClassName Service 类名
 * @param hasMediaPlaybackType 是否声明了 mediaPlayback 类型
 * @param hasWhileInUse 是否声明了 while-in-use capability
 * @param hasMediaSession 是否关联了 MediaSession
 */
data class FGSConfig(
    val serviceClassName: String,
    val hasMediaPlaybackType: Boolean,
    val hasWhileInUse: Boolean,
    val hasMediaSession: Boolean
)

/**
 * ============================================================
 * BackgroundAudioHardeningState — 页面状态
 * ============================================================
 * Main page state for Background Audio Hardening tool.
 *
 * @param activeTab 当前活跃的标签页
 * @param auditStatus 合规扫描状态
 * @param auditResult 最新扫描结果
 * @param selectedIssue 选中的问题详情
 * @param projectPath 项目路径（用于扫描）
 * @param targetSdk Target SDK 版本
 * @param scanProgress 扫描进度 (0-100)
 * @param isGeneratingReport 是否正在生成报告
 * @param generatedCode 生成器的代码输出
 * @param generatedTemplateType 生成的模板类型
 * @param ciConfig CI 配置参数
 * @param failOnError CI fail-on-error 模式
 *
 * @see AuditStatus Status enum
 * @see AuditResult Scan result
 */
data class BackgroundAudioHardeningState(
    val activeTab: AudioHardeningTab = AudioHardeningTab.Overview,
    val auditStatus: AuditStatus = AuditStatus.Idle,
    val auditResult: AuditResult? = null,
    val selectedIssue: ComplianceIssue? = null,
    val projectPath: String = "",
    val targetSdk: Int = 37,
    val scanProgress: Int = 0,
    val isGeneratingReport: Boolean = false,
    val generatedCode: String = "",
    val generatedTemplateType: TemplateType = TemplateType.MediaSessionInit,
    val ciConfig: String = "",
    val failOnError: Boolean = false
) {
    companion object {
        /** Initial/empty state */
        val Initial = BackgroundAudioHardeningState()
    }
}

/**
 * ============================================================
 * AudioHardeningTab — 功能标签页枚举
 * ============================================================
 * Tab definitions for the Background Audio Hardening tool.
 *
 * @param title Tab 显示标题
 * @param route Tab 路由标识
 */
enum class AudioHardeningTab(val title: String, val route: String) {
    Overview("概览", "overview"),
    Scanner("音频扫描器", "scanner"),
    MediaSession("MediaSession模板", "media_session"),
    FGSConfig("FGS配置", "fgs_config"),
    CICompliance("CI合规检测", "ci_compliance"),
    FallbackStrategy("降级策略", "fallback_strategy")
}

/**
 * ============================================================
 * TemplateType — 模板类型枚举
 * ============================================================
 * Type of MediaSession template to generate.
 */
enum class TemplateType(val labelCN: String, val labelEN: String) {
    MediaSessionInit("MediaSession 初始化", "MediaSession Initialization"),
    MediaSessionCallback("MediaSessionCallback 实现", "MediaSessionCallback Implementation"),
    PlaybackService("PlaybackService 完整模板", "PlaybackService Complete Template"),
    AudioFocusHandling("音频焦点处理", "Audio Focus Handling"),
    LifecycleSync("生命周期同步", "Lifecycle Synchronization")
}

/**
 * ============================================================
 * BackgroundAudioHardeningIntent — 用户意图
 * ============================================================
 * User intentions (actions) that drive state changes.
 * 用户意图/动作，驱动状态变更。
 */
sealed class BackgroundAudioHardeningIntent {
    /** 切换到指定标签页 */
    data class SwitchTab(val tab: AudioHardeningTab) : BackgroundAudioHardeningIntent()
    /** 开始合规扫描 */
    data class StartAudit(val projectPath: String, val targetSdk: Int) : BackgroundAudioHardeningIntent()
    /** 取消扫描 */
    object CancelAudit : BackgroundAudioHardeningIntent()
    /** 选中查看问题详情 */
    data class SelectIssue(val issue: ComplianceIssue?) : BackgroundAudioHardeningIntent()
    /** 生成 MediaSession 模板 */
    data class GenerateTemplate(val templateType: TemplateType) : BackgroundAudioHardeningIntent()
    /** 生成 FGS 配置 */
    data class GenerateFGSConfig(val serviceClassName: String) : BackgroundAudioHardeningIntent()
    /** 更新 CI 配置 */
    data class UpdateCIConfig(val config: String, val failOnError: Boolean) : BackgroundAudioHardeningIntent()
    /** 复制代码到剪贴板 */
    data class CopyCode(val code: String) : BackgroundAudioHardeningIntent()
    /** 导出 HTML 报告 */
    object ExportReport : BackgroundAudioHardeningIntent()
    /** 刷新状态 */
    object Refresh : BackgroundAudioHardeningIntent()
}

/**
 * ============================================================
 * BackgroundAudioHardeningEffect — 一次性副作用
 * ============================================================
 * One-time side effects delivered via Channel.
 * 一次性副作用，通过 Channel 传递给 UI。
 */
sealed class BackgroundAudioHardeningEffect {
    /** 显示 Toast 消息 */
    data class ShowToast(val message: String) : BackgroundAudioHardeningEffect()
    /** 复制到剪贴板成功 */
    data class CopyToClipboard(val text: String) : BackgroundAudioHardeningEffect()
    /** 打开外部链接 */
    data class OpenUrl(val url: String) : BackgroundAudioHardeningEffect()
    /** 报告导出成功 */
    data class ReportExported(val path: String) : BackgroundAudioHardeningEffect()
    /** 导航到问题所在文件 */
    data class NavigateToFile(val filePath: String, val lineNumber: Int) : BackgroundAudioHardeningEffect()
}
