package com.mvi.kenny.feature.exoplayermedia3migration

import androidx.compose.ui.graphics.Color

// =============================================================
// ExoPlayer2Media3MigrationToolContract — ExoPlayer 2 → Media3
// Migration Toolkit MVI Contract
// =============================================================
// MVI Architecture Pattern / MVI 架构模式
//
// - Model (State): Immutable data class, single source of truth for UI
// - View: Composable functions that consume State and render UI
// - Intent: User intentions, ViewModel processes and updates State
// - Effect: One-time side effects (navigation, toast, etc.)
//
// @see ExoPlayer2Media3MigrationToolViewModel State management
// @see ExoPlayer2Media3MigrationToolScreen Main screen

// =============================================================
// MigrationTab — 功能 Tab 枚举
// =============================================================
/**
 * Migration tool module tab / 迁移工具模块 Tab
 *
 * Four main sections: Scanner / Migration / Library / Report
 *
 * @param title Display title / 显示标题
 */
enum class MigrationTab(val title: String) {
    HOME("首页 / Home"),
    SCANNER("扫描器 / Scanner"),
    MIGRATION("迁移向导 / Migration"),
    LIBRARY("参考库 / Library"),
    REPORT("合规报告 / Report")
}

// =============================================================
// ScanStatus — 扫描状态枚举
// =============================================================
/**
 * Scan status / 扫描状态
 *
 * @param label Display label / 显示标签
 */
enum class ScanStatus(val label: String) {
    IDLE("Idle / 空闲"),
    SCANNING("Scanning / 扫描中"),
    COMPLETED("Completed / 完成"),
    ERROR("Error / 错误")
}

// =============================================================
// ScanResult — ExoPlayer 2 扫描结果项
// =============================================================
/**
 * ExoPlayer 2 usage scan result / ExoPlayer 2 使用扫描结果
 *
 * Represents a detected usage of ExoPlayer 2 in the codebase.
 *
 * @param id Unique identifier / 唯一标识符
 * @param filePath Source file path / 源文件路径
 * @param lineNumber Line number in source file / 源文件行号
 * @param className Class name containing the usage / 包含该使用的类名
 * @param methodName Method name containing the usage / 包含该使用的方法名
 * @param usageType Usage type (Player / TrackSelector / DataSource / MediaSource) / 使用类型
 * @param severity Severity level (Critical / Warning / Info) / 严重程度
 * @param codeSnippet Relevant code snippet / 相关代码片段
 * @param description Problem description / 问题描述
 * @param fixSuggestion Suggested fix / 修复建议
 * @param isMigrated Whether this item has been migrated / 是否已迁移
 */
data class ScanResult(
    val id: String,
    val filePath: String,
    val lineNumber: Int,
    val className: String,
    val methodName: String,
    val usageType: UsageType,
    val severity: IssueSeverity,
    val codeSnippet: String,
    val description: String,
    val fixSuggestion: String,
    val isMigrated: Boolean = false
)

/**
 * Usage type / 使用类型
 *
 * @param label Display label / 显示标签
 * @param apiCount Approximate number of ExoPlayer 2 APIs in this category / 该类别中 ExoPlayer 2 API 约数量
 */
enum class UsageType(val label: String, val apiCount: Int) {
    PLAYER("Player API / Player API", 45),
    TRACK_SELECTOR("TrackSelector / TrackSelector", 12),
    DATA_SOURCE("DataSource / DataSource", 8),
    MEDIA_SOURCE("MediaSource / MediaSource", 15),
    MEDIA_SESSION("MediaSession / MediaSession", 20),
    AUDIO_FOCUS("AudioFocus / AudioFocus", 6)
}

/**
 * Issue severity / 问题严重程度
 *
 * @param label Display label / 显示标签
 * @param priority Sort priority / 排序优先级
 * @param color Badge color / 徽章颜色
 */
enum class IssueSeverity(val label: String, val priority: Int, val color: Color) {
    CRITICAL("P0 — 必须迁移 / Must Migrate", 0, Color(0xFFD32F2F)),
    WARNING("P1 — 建议迁移 / Should Migrate", 1, Color(0xFFF57C00)),
    INFO("P2 — 可选优化 / Optional", 2, Color(0xFF1976D2))
}

// =============================================================
// MigrationStep — 迁移步骤
// =============================================================
/**
 * Migration step / 迁移步骤
 *
 * @param index Step index (0-4) / 步骤索引
 * @param title Step title / 步骤标题
 * @param description Step description / 步骤描述
 * @param isCompleted Whether this step has been completed / 是否已完成
 * @param diffContent Code diff preview content / 代码 Diff 预览内容
 * @param templateCode Template code for this step / 该步骤的模板代码
 */
data class MigrationStep(
    val index: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val diffContent: String? = null,
    val templateCode: String? = null
) {
    companion object {
        /**
         * Default migration steps / 默认迁移步骤
         */
        fun defaultSteps(): List<MigrationStep> = listOf(
            MigrationStep(
                index = 0,
                title = "1. 替换依赖 / Replace Dependencies",
                description = "将 build.gradle.kts 中的 ExoPlayer 2 依赖替换为 Media3",
                templateCode = """// build.gradle.kts
// Before / 之前
implementation("com.google.android.exoplayer:exoplayer-core:2.19.1")
implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")

// After / 之后
implementation("androidx.media3:media3-exoplayer:1.2.1")
implementation("androidx.media3:media3-ui:1.2.1")
implementation("androidx.media3:media3-session:1.2.1")"""
            ),
            MigrationStep(
                index = 1,
                title = "2. 迁移 AndroidManifest / Migrate AndroidManifest",
                description = "为 MediaSessionService 添加 foregroundServiceType=\"mediaPlayback\"",
                templateCode = """<!-- AndroidManifest.xml -->
<!-- Before / 之前 -->
<service android:name=".AudioPlaybackService" />

<!-- After / 之后 -->
<service
    android:name=".AudioPlaybackService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false">
    <intent-filter>
        <action android:name="androidx.media3.session.MediaSessionService" />
    </intent-filter>
</service>

<!-- Required permissions / 所需权限 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /> <!-- Android 13+ -->"""
            ),
            MigrationStep(
                index = 2,
                title = "3. 替换 Player API / Replace Player API",
                description = "将 ExoPlayer 2 Player 替换为 Media3 Player",
                templateCode = """// Before / 之前
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player

val player = ExoPlayer.Builder(context).build()
player.playWhenReady = true
player.seekTo(currentWindow, playbackPosition)
player.addListener(playerListener)

// After / 之后
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

val player = ExoPlayer.Builder(context).build()
player.playWhenReady = true
player.seekTo(currentWindow, playbackPosition)
player.addListener(playerListener)

val mediaItem = MediaItem.fromUri(uri)
player.setMediaItem(mediaItem)
player.prepare()"""
            ),
            MigrationStep(
                index = 3,
                title = "4. 配置 MediaSession / Configure MediaSession",
                description = "将 MediaSessionCompat 替换为 Media3 MediaSession",
                templateCode = """// Before / 之前
import com.google.android.exoplayer2.session.MediaSessionCompat

val mediaSession = MediaSessionCompat(context, "MusicService")
mediaSession.setActive(true)

// After / 之后
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class AudioPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}"""
            ),
            MigrationStep(
                index = 4,
                title = "5. 处理音频焦点 / Handle Audio Focus",
                description = "迁移到 Media3 内置音频焦点管理",
                templateCode = """// Before / 之前
import com.google.android.exoplayer2.audio.AudioFocusManager

val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> player.play()
            AudioManager.AUDIOFOCUS_LOSS -> player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> player.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.volume = 0.3f
        }
    }
    .build()

// After / 之后
// Media3 handles audio focus automatically via AudioFocusMember
// Media3 通过 AudioFocusMember 自动处理音频焦点
import androidx.media3.common.util.UnstableApi

@UnstableApi
class AudioPlaybackService : MediaSessionService() {
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                Player.AudioAttributes.Builder()
                    .setContentType(C.USAGE_MEDIA)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // handleAudioFocus / 处理音频焦点
            )
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
    }
}"""
            )
        )
    }
}

// =============================================================
// ApiComparisonItem — ExoPlayer 2 vs Media3 API 对照项
// =============================================================
/**
 * API comparison item / API 对照项
 *
 * @param category API category / API 类别
 * @param exoPlayer2Api ExoPlayer 2 API signature / ExoPlayer 2 API 签名
 * @param media3Api Media3 API signature / Media3 API 签名
 * @param notes Migration notes / 迁移备注
 */
data class ApiComparisonItem(
    val category: ApiCategory,
    val exoPlayer2Api: String,
    val media3Api: String,
    val notes: String
)

/**
 * API category / API 类别
 */
enum class ApiCategory(val label: String) {
    PLAYER("Player / Player"),
    SESSION("MediaSession / MediaSession"),
    AUDIO_FOCUS("AudioFocus / AudioFocus"),
    NOTIFICATION("Notification / Notification")
}

// =============================================================
// ComplianceReport — 合规报告
// =============================================================
/**
 * Android 17 background audio compliance report / Android 17 背景音频合规报告
 *
 * @param score Overall compliance score 0-100 / 总合规评分 0-100
 * @param level Compliance level (Compliant / Partial / Non-compliant) / 合规等级
 * @param totalIssues Total number of issues found / 发现的问题总数
 * @param criticalCount Number of P0 critical issues / P0 严重问题数量
 * @param warningCount Number of P1 warning issues / P1 警告问题数量
 * @param infoCount Number of P2 info issues / P2 信息问题数量
 * @param migratedCount Number of issues already migrated / 已迁移问题数量
 * @param remainingIssues List of remaining issues / 剩余问题列表
 * @param generatedAt Report generation timestamp / 报告生成时间戳
 * @param lastScanTime Last scan timestamp / 最后扫描时间戳
 */
data class ComplianceReport(
    val score: Int,
    val level: ComplianceLevel,
    val totalIssues: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val infoCount: Int,
    val migratedCount: Int,
    val remainingIssues: List<ScanResult>,
    val generatedAt: Long,
    val lastScanTime: Long
)

/**
 * Compliance level / 合规等级
 *
 * @param label Display label / 显示标签
 * @param description Level description / 等级描述
 */
enum class ComplianceLevel(val label: String, val description: String) {
    COMPLIANT("✅ 完全合规 / Fully Compliant", "App 符合 Android 17 背景音频要求"),
    PARTIAL("⚠️ 部分合规 / Partially Compliant", "存在可迁移问题，建议修复"),
    NON_COMPLIANT("❌ 不合规 / Non-Compliant", "App 在 Android 17 上将无法正常后台播放音频")
}

// =============================================================
// LibraryTab — 参考库 Tab
// =============================================================
/**
 * Library sub-tab / 参考库子 Tab
 */
enum class LibraryTab(val title: String) {
    API_TABLE("API 对照表 / API Table"),
    TEMPLATES("配置模板 / Templates"),
    FAQ("常见问题 / FAQ")
}

// =============================================================
// ExoPlayer2Media3MigrationToolState — 页面状态
// =============================================================
/**
 * ExoPlayer 2 → Media3 Migration Tool State / ExoPlayer 2 → Media3 迁移工具页面状态
 *
 * Single source of truth for the entire Migration Tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Currently active tool tab / 当前活跃的工具 Tab
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scanResults List of ExoPlayer 2 usage results found / 发现的 ExoPlayer 2 使用结果列表
 * @param selectedScanResult Selected scan result for detail view / 选中的扫描结果详情
 * @param migrationSteps List of migration steps / 迁移步骤列表
 * @param currentStep Current migration step index / 当前迁移步骤索引
 * @param selectedStep Selected migration step for detail / 选中的迁移步骤详情
 * @param libraryTab Current library sub-tab / 当前参考库子 Tab
 * @param selectedApiCategory Selected API category filter / 选中的 API 类别过滤器
 * @param apiComparisons List of API comparison items / API 对照项列表
 * @param complianceReport Generated compliance report / 生成的合规报告
 * @param isGeneratingReport Whether report is being generated / 是否正在生成报告
 * @param isApplyingStep Whether a migration step is being applied / 是否正在应用迁移步骤
 * @param detectedPlayerInstances Number of ExoPlayer 2 instances detected / 检测到的 ExoPlayer 2 实例数量
 * @param migrationProgress Overall migration progress 0.0~1.0 / 总体迁移进度
 * @param lastScanTime Last scan timestamp / 最后扫描时间戳
 * @param isCompliant Whether the app is currently compliant / App 当前是否合规
 * @param isLoading Whether any background operation is in progress / 是否有任何后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class ExoPlayer2Media3MigrationToolState(
    val activeTab: MigrationTab = MigrationTab.HOME,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanResults: List<ScanResult> = emptyList(),
    val selectedScanResult: ScanResult? = null,
    val migrationSteps: List<MigrationStep> = MigrationStep.defaultSteps(),
    val currentStep: Int = 0,
    val selectedStep: MigrationStep? = null,
    val libraryTab: LibraryTab = LibraryTab.API_TABLE,
    val selectedApiCategory: ApiCategory? = null,
    val apiComparisons: List<ApiComparisonItem> = emptyList(),
    val complianceReport: ComplianceReport? = null,
    val isGeneratingReport: Boolean = false,
    val isApplyingStep: Boolean = false,
    val detectedPlayerInstances: Int = 0,
    val migrationProgress: Float = 0f,
    val lastScanTime: Long? = null,
    val isCompliant: Boolean? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = ExoPlayer2Media3MigrationToolState()
    }

    /**
     * Sorted scan results by severity / 按严重程度排序的扫描结果
     * Critical (P0) first / P0 严重问题优先
     */
    val sortedScanResults: List<ScanResult>
        get() = scanResults.sortedBy { it.severity.priority }

    /**
     * Critical (P0) issue count / P0 严重问题数量
     */
    val criticalCount: Int
        get() = scanResults.count { it.severity == IssueSeverity.CRITICAL }

    /**
     * Warning (P1) issue count / P1 警告问题数量
     */
    val warningCount: Int
        get() = scanResults.count { it.severity == IssueSeverity.WARNING }

    /**
     * Info (P2) issue count / P2 信息问题数量
     */
    val infoCount: Int
        get() = scanResults.count { it.severity == IssueSeverity.INFO }

    /**
     * Number of already migrated items / 已迁移项目数量
     */
    val migratedCount: Int
        get() = scanResults.count { it.isMigrated }

    /**
     * Scan completion percentage / 扫描完成百分比
     */
    val scanPercentage: Int
        get() = (scanProgress * 100).toInt()

    /**
     * Compliance status text / 合规状态文本
     */
    val complianceStatusText: String
        get() = when {
            isCompliant == true -> "✅ App 已符合 Android 17 背景音频要求"
            isCompliant == false -> "❌ App 不符合 Android 17 背景音频要求"
            else -> "⚠️ 请先运行扫描检测"
        }

    /**
     * Number of completed migration steps / 已完成的迁移步骤数量
     */
    val completedStepsCount: Int
        get() = migrationSteps.count { it.isCompleted }
}

// =============================================================
// ExoPlayer2Media3MigrationToolIntent — 用户意图
// =============================================================
/**
 * ExoPlayer 2 → Media3 Migration Tool User Intents / ExoPlayer 2 → Media3 迁移工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface ExoPlayer2Media3MigrationToolIntent {
    /** Select a tool tab / 选择工具 Tab
     * @param tab Tab to select / 要选择的 Tab
     */
    data class SelectTab(val tab: MigrationTab) : ExoPlayer2Media3MigrationToolIntent

    /** Start ExoPlayer 2 usage scan / 开始 ExoPlayer 2 使用扫描
     * @param modulePath Module or project path to scan / 要扫描的模块或项目路径
     */
    data class StartScan(val modulePath: String) : ExoPlayer2Media3MigrationToolIntent

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : ExoPlayer2Media3MigrationToolIntent

    /** Select a scan result to view detail / 选择扫描结果查看详情
     * @param result Scan result to select / 要选择的扫描结果
     */
    data class SelectScanResult(val result: ScanResult) : ExoPlayer2Media3MigrationToolIntent

    /** Clear selected scan result / 清除选中的扫描结果 */
    data object ClearScanResult : ExoPlayer2Media3MigrationToolIntent

    /** Select a migration step / 选择迁移步骤
     * @param step Step to select / 要选择的步骤
     */
    data class SelectStep(val step: MigrationStep) : ExoPlayer2Media3MigrationToolIntent

    /** Apply a migration step / 应用迁移步骤
     * @param stepIndex Step index to apply / 要应用的步骤索引
     */
    data class ApplyStep(val stepIndex: Int) : ExoPlayer2Media3MigrationToolIntent

    /** Navigate to next migration step / 导航到下一步迁移
     */
    data object NextStep : ExoPlayer2Media3MigrationToolIntent

    /** Navigate to previous migration step / 导航到上一步迁移
     */
    data object PreviousStep : ExoPlayer2Media3MigrationToolIntent

    /** Select a library sub-tab / 选择参考库子 Tab
     * @param tab Library tab to select / 要选择的参考库 Tab
     */
    data class SelectLibraryTab(val tab: LibraryTab) : ExoPlayer2Media3MigrationToolIntent

    /** Filter API comparisons by category / 按类别过滤 API 对照
     * @param category Category to filter by, null for all / 要过滤的类别，null 表示全部
     */
    data class FilterApiCategory(val category: ApiCategory?) : ExoPlayer2Media3MigrationToolIntent

    /** Generate compliance report / 生成合规报告
     */
    data object GenerateReport : ExoPlayer2Media3MigrationToolIntent

    /** Navigate to migration wizard / 导航到迁移向导
     */
    data object NavigateToMigration : ExoPlayer2Media3MigrationToolIntent

    /** Navigate to report screen / 导航到报告页面
     */
    data object NavigateToReport : ExoPlayer2Media3MigrationToolIntent

    /** Quick scan from home / 从首页快速扫描
     */
    data object StartQuickScan : ExoPlayer2Media3MigrationToolIntent

    /** Dismiss error message / 关闭错误信息 */
    data object DismissError : ExoPlayer2Media3MigrationToolIntent

    /** Copy code to clipboard / 复制代码到剪贴板
     * @param code Code to copy / 要复制的代码
     */
    data class CopyCode(val code: String) : ExoPlayer2Media3MigrationToolIntent
}

// =============================================================
// ExoPlayer2Media3MigrationToolEffect — 副作用
// =============================================================
/**
 * ExoPlayer 2 → Media3 Migration Tool Side Effects / ExoPlayer 2 → Media3 迁移工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface ExoPlayer2Media3MigrationToolEffect {
    /** Show snackbar message / 显示 Snackbar 消息
     * @param message Message to display / 要显示的消息
     * @param isError Whether this is an error message / 是否为错误消息
     */
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : ExoPlayer2Media3MigrationToolEffect

    /** Copy to clipboard success / 复制到剪贴板成功
     * @param message Confirmation message / 确认消息
     */
    data class CopyToClipboard(val message: String) : ExoPlayer2Media3MigrationToolEffect

    /** Scan complete event / 扫描完成事件 */
    data object ScanComplete : ExoPlayer2Media3MigrationToolEffect

    /** Navigate to tab / 导航到指定 Tab
     * @param tab Tab to navigate to / 要导航到的 Tab
     */
    data class NavigateToTab(val tab: MigrationTab) : ExoPlayer2Media3MigrationToolEffect

    /** Migration step applied successfully / 迁移步骤应用成功
     * @param stepIndex Applied step index / 已应用的步骤索引
     */
    data class StepApplied(val stepIndex: Int) : ExoPlayer2Media3MigrationToolEffect

    /** Report generated successfully / 报告生成成功
     * @param score Compliance score / 合规评分
     */
    data class ReportGenerated(val score: Int) : ExoPlayer2Media3MigrationToolEffect
}
