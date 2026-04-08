package com.mvi.kenny.feature.wearos64bit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ============================================================
 * WearOs64BitContract — Wear OS 64位合规工具 MVI 契约
 * ============================================================
 * MVI Architecture Pattern / MVI 架构模式
 *
 * - Model (State): Immutable data class, single source of truth for UI
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions, ViewModel processes and updates State
 * - Effect: One-time side effects (navigation, toast, etc.)
 *
 * PRD-061 / Wear OS 64-Bit Compliance Tool
 * Google Deadline: 2026-09-15
 *
 * @see WearOs64BitViewModel State management
 * @see WearOs64BitScreen Main screen
 */

// =============================================================
// BottomTab — 底部导航 Tab
// =============================================================
/**
 * Bottom navigation tab / 底部导航 Tab
 *
 * @param title Display title / 显示标题
 * @param icon Tab icon / Tab 图标
 */
enum class BottomTab(val title: String, val icon: ImageVector? = null) {
    HOME("首页"),
    SDK_QUERY("SDK查询"),
    MIGRATION_TRACKER("进度追踪"),
    SETTINGS("设置")
}

// =============================================================
// ComplianceGrade — 合规等级
// =============================================================
/**
 * Overall compliance grade / 整体合规等级
 *
 * @param label Display label / 显示标签
 * @param color Grade color / 等级颜色
 * @param scoreMin Minimum score for this grade / 最低分数
 */
enum class ComplianceGrade(val label: String, val color: Color, val scoreMin: Int) {
    COMPLIANT("合规", Color(0xFF4CAF50), 90),
    PARTIAL("部分合规", Color(0xFFFF9800), 60),
    NON_COMPLIANT("不合规", Color(0xFFF44336), 0)
}

// =============================================================
// UploadStatus — APK 上传状态
// =============================================================
/**
 * APK upload and processing status / APK 上传和处理状态
 */
enum class UploadStatus {
    IDLE,
    UPLOADING,
    PROCESSING,
    DONE,
    ERROR
}

// =============================================================
// FixStrategy — 修复策略
// =============================================================
/**
 * Fix strategy for 64-bit migration / 64位迁移修复策略
 *
 * @param label Display label / 显示标签
 */
enum class FixStrategy(val label: String) {
    RECOMPILE("重新编译"),
    SUPPRESS("抑制警告"),
    REPLACE_SDK("替换 SDK"),
    CUSTOM("自定义")
}

// =============================================================
// Abis — 支持的 CPU 架构
// =============================================================
/**
 * ABI (Application Binary Interface) / CPU 架构
 *
 * @param label Display label / 显示标签
 * @param is64Bit Whether this is a 64-bit architecture / 是否为64位架构
 */
enum class Abis(val label: String, val is64Bit: Boolean) {
    ARM64_V8A("arm64-v8a", true),
    ARMEABI_V7A("armeabi-v7a", false),
    X86_64("x86_64", true),
    X86("x86", false),
    UNKNOWN("unknown", false)
}

// =============================================================
// SoLibrary — Native 库信息
// =============================================================
/**
 * Native library (.so) file information / Native 库文件信息
 *
 * Represents a single .so file found in the APK.
 *
 * @param name So file name (e.g., libnative-lib.so) / .so 文件名
 * @param source Which dependency introduced this .so / 引入该 .so 的依赖来源
 * @param supportedAbis List of ABIs this .so supports / 该 .so 支持的 ABI 列表
 * @param has64Bit Whether this .so includes any 64-bit variant / 是否包含64位变体
 * @param isFromThirdParty Whether this .so is from a third-party SDK / 是否来自第三方 SDK
 * @param size Size in bytes / 文件大小（字节）
 * @param recommendedFix Recommended fix for this .so / 推荐的修复方式
 */
data class SoLibrary(
    val name: String,
    val source: String,
    val supportedAbis: List<Abis>,
    val has64Bit: Boolean,
    val isFromThirdParty: Boolean,
    val size: Long,
    val recommendedFix: FixStrategy = FixStrategy.RECOMPILE
)

// =============================================================
// ApkInfo — APK 文件信息
// =============================================================
/**
 * APK file information / APK 文件信息
 *
 * @param packageName Package name (e.g., com.example.myapp) / 包名
 * @param versionName Version name (e.g., 1.0.0) / 版本名称
 * @param versionCode Version code / 版本号
 * @param minSdkVersion Minimum SDK version / 最低 SDK 版本
 * @param targetSdkVersion Target SDK version / 目标 SDK 版本
 * @param soCount Total number of .so files / .so 文件总数
 * @param fileSize APK file size in bytes / APK 文件大小（字节）
 * @param architectures List of architectures included in the APK / APK 包含的架构列表
 */
data class ApkInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val soCount: Int,
    val fileSize: Long,
    val architectures: List<Abis> = emptyList()
)

// =============================================================
// ComplianceResult — 合规判定结果
// =============================================================
/**
 * 64-bit compliance result for an APK / APK 的64位合规判定结果
 *
 * @param grade Overall compliance grade / 整体合规等级
 * @param score Compliance score (0-100) / 合规分数
 * @param pass Whether APK passes compliance / 是否通过合规
 * @param missing64BitAbis List of ABIs that are missing 64-bit support / 缺失64位支持的 ABI 列表
 * @param summary Human-readable summary / 摘要
 */
data class ComplianceResult(
    val grade: ComplianceGrade,
    val score: Int,
    val pass: Boolean,
    val missing64BitAbis: List<Abis>,
    val summary: String
)

// =============================================================
// SdkInfo — SDK 兼容性信息
// =============================================================
/**
 * Third-party SDK 64-bit compatibility information / 第三方 SDK 64位兼容性信息
 *
 * @param name SDK name / SDK 名称
 * @param vendor SDK vendor / SDK 供应商
 * @param status Support status / 支持状态
 * @param version Current version / 当前版本
 * @param compatibleVersion Version that supports 64-bit / 支持64位的版本
 * @param notes Additional notes / 附加说明
 */
data class SdkInfo(
    val name: String,
    val vendor: String,
    val status: SdkSupportStatus,
    val version: String,
    val compatibleVersion: String?,
    val notes: String
)

/**
 * SDK 64-bit support status / SDK 64位支持状态
 *
 * @param label Display label / 显示标签
 * @param color Status color / 状态颜色
 */
enum class SdkSupportStatus(val label: String, val color: Color) {
    SUPPORTED("✅ 已支持", Color(0xFF4CAF50)),
    PARTIAL("⚠️ 部分支持", Color(0xFFFF9800)),
    NOT_SUPPORTED("❌ 不支持", Color(0xFFF44336)),
    PENDING("🔍 待确认", Color(0xFF9E9E9E))
}

// =============================================================
// ScanRecord — 扫描记录
// =============================================================
/**
 * Historical scan record / 历史扫描记录
 *
 * @param id Unique record ID / 唯一记录 ID
 * @param apkName APK name or package name / APK 名称或包名
 * @param scanTime Timestamp of scan / 扫描时间
 * @param result Compliance result / 合规结果
 */
data class ScanRecord(
    val id: String,
    val apkName: String,
    val scanTime: Long,
    val result: ComplianceResult
)

// =============================================================
// ProjectModule — 项目模块信息
// =============================================================
/**
 * Android project module information / Android 项目模块信息
 *
 * @param name Module name (e.g., app, :library:core) / 模块名称
 * @param soLibraries List of .so files in this module / 该模块的 .so 文件列表
 * @param complianceRate Compliance rate for this module / 该模块的合规率
 * @param isSelected Whether this module is selected for scanning / 是否选中该模块进行扫描
 */
data class ProjectModule(
    val name: String,
    val soLibraries: List<SoLibrary>,
    val complianceRate: Float,
    val isSelected: Boolean = true
)

// =============================================================
// MigrationProject — 迁移项目进度
// =============================================================
/**
 * Migration project progress tracking / 迁移项目进度追踪
 *
 * @param name Project name / 项目名称
 * @param moduleCount Total number of modules / 模块总数
 * @param completedModules Number of completed modules / 已完成模块数
 * @param status Overall migration status / 整体迁移状态
 * @param lastScanTime Last scan timestamp / 最后扫描时间
 */
data class MigrationProject(
    val name: String,
    val moduleCount: Int,
    val completedModules: Int,
    val status: MigrationStatus,
    val lastScanTime: Long
)

/**
 * Migration status / 迁移状态
 */
enum class MigrationStatus(val label: String, val color: Color) {
    NOT_STARTED("未启动", Color(0xFF9E9E9E)),
    IN_PROGRESS("进行中", Color(0xFF2196F3)),
    COMPLETED("已完成", Color(0xFF4CAF50))
}

// =============================================================
// FixWizardStep — 修复向导步骤
// =============================================================
/**
 * Fix wizard step / 修复向导步骤
 *
 * @param index Step index (0-based) / 步骤索引
 * @param title Step title / 步骤标题
 */
enum class FixWizardStep(val index: Int, val title: String) {
    SELECT(0, "Step 1 — 选择待修复项"),
    STRATEGY(1, "Step 2 — 修复策略"),
    PREVIEW(2, "Step 3 — 预览变更"),
    EXECUTE(3, "Step 4 — 执行修复")
}

// =============================================================
// WearOs64BitState — 页面状态
// =============================================================
/**
 * Wear OS 64-Bit Compliance Tool State / Wear OS 64位合规工具页面状态
 *
 * Single source of truth for the entire tool UI.
 * All UI state is derived from this data class.
 *
 * @param activeTab Current bottom tab / 当前底部 Tab
 * @param activeHomeTab Current home sub-tab / 当前首页子 Tab
 * @param homeState Home tab state / 首页 Tab 状态
 * @param sdkQueryState SDK query tab state / SDK 查询 Tab 状态
 * @param trackerState Migration tracker tab state / 进度追踪 Tab 状态
 * @param isLoading Whether a background operation is in progress / 是否有后台操作进行中
 * @param error Error message if any / 错误信息
 */
data class WearOs64BitState(
    val activeTab: BottomTab = BottomTab.HOME,
    val activeHomeTab: HomeSubTab = HomeSubTab.APK_SCAN,
    val homeState: HomeState = HomeState(),
    val sdkQueryState: SdkQueryState = SdkQueryState(),
    val trackerState: TrackerState = TrackerState(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = WearOs64BitState()
    }
}

/**
 * Home sub-tab / 首页子 Tab
 */
enum class HomeSubTab(val title: String) {
    APK_SCAN("APK 扫描"),
    PROJECT_SCAN("项目扫描")
}

/**
 * Home tab state / 首页 Tab 状态
 *
 * @param overallScore Overall compliance score (0-100) / 整体合规分数
 * @param overallGrade Overall compliance grade / 整体合规等级
 * @param recentScans Recent scan history / 最近扫描记录
 * @param uploadStatus APK upload status / APK 上传状态
 * @param apkInfo Uploaded APK information / 已上传 APK 信息
 * @param soLibraries List of .so files found / 发现的 .so 文件列表
 * @param complianceResult Compliance analysis result / 合规分析结果
 * @param parseProgress APK parsing progress (0.0~1.0) / APK 解析进度
 * @param selectedSoDetail Selected .so for detail view / 选中的 .so 详情
 * @param projectModules Project scan modules / 项目扫描模块
 * @param projectScanActive Whether project scan mode is active / 项目扫描模式是否激活
 * @param wizardStep Current fix wizard step / 当前修复向导步骤
 * @param selectedLibrariesForFix Selected .so libraries for fix / 选中的待修复 .so 库
 * @param selectedFixStrategy Selected fix strategy / 选中的修复策略
 * @param wizardLog Wizard execution log / 向导执行日志
 * @param wizardComplete Whether wizard has completed / 向导是否已完成
 */
data class HomeState(
    val overallScore: Int = 0,
    val overallGrade: ComplianceGrade = ComplianceGrade.NON_COMPLIANT,
    val recentScans: List<ScanRecord> = emptyList(),
    val uploadStatus: UploadStatus = UploadStatus.IDLE,
    val apkInfo: ApkInfo? = null,
    val soLibraries: List<SoLibrary> = emptyList(),
    val complianceResult: ComplianceResult? = null,
    val parseProgress: Float = 0f,
    val selectedSoDetail: SoLibrary? = null,
    val projectModules: List<ProjectModule> = emptyList(),
    val projectScanActive: Boolean = false,
    val wizardStep: FixWizardStep = FixWizardStep.SELECT,
    val selectedLibrariesForFix: Set<String> = emptySet(),
    val selectedFixStrategy: FixStrategy = FixStrategy.RECOMPILE,
    val wizardLog: List<String> = emptyList(),
    val wizardComplete: Boolean = false
)

/**
 * SDK Query tab state / SDK 查询 Tab 状态
 *
 * @param searchQuery SDK search query / SDK 搜索查询
 * @param sdkList List of all SDKs / SDK 列表
 * @param filteredSdkList Filtered SDK list / 过滤后的 SDK 列表
 * @param isLoading Whether SDK list is loading / 是否正在加载
 */
data class SdkQueryState(
    val searchQuery: String = "",
    val sdkList: List<SdkInfo> = emptyList(),
    val filteredSdkList: List<SdkInfo> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * Migration tracker tab state / 进度追踪 Tab 状态
 *
 * @param projects List of migration projects / 迁移项目列表
 * @param filterFilter Current filter / 当前筛选
 */
data class TrackerState(
    val projects: List<MigrationProject> = emptyList(),
    val filter: TrackerFilter = TrackerFilter.ALL
)

/**
 * Tracker filter / 进度追踪筛选
 */
enum class TrackerFilter(val label: String) {
    ALL("全部"),
    COMPLIANT("合规"),
    IN_PROGRESS("进行中"),
    NOT_STARTED("未启动")
}

// =============================================================
// WearOs64BitIntent — 用户意图
// =============================================================
/**
 * Wear OS 64-Bit Compliance Tool User Intents / Wear OS 64位合规工具用户意图
 *
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 */
sealed interface WearOs64BitIntent {
    /** Switch bottom tab / 切换底部 Tab
     * @param tab Target tab / 目标 Tab
     */
    data class SwitchTab(val tab: BottomTab) : WearOs64BitIntent

    /** Switch home sub-tab / 切换首页子 Tab
     * @param subTab Target sub-tab / 目标子 Tab
     */
    data class SwitchHomeSubTab(val subTab: HomeSubTab) : WearOs64BitIntent

    /** Upload APK file / 上传 APK 文件
     * @param uri APK file URI / APK 文件 URI
     */
    data class UploadApk(val uri: android.net.Uri) : WearOs64BitIntent

    /** Start APK scan / 开始 APK 扫描
     * @param uri APK file URI / APK 文件 URI
     */
    data class StartApkScan(val uri: android.net.Uri) : WearOs64BitIntent

    /** Select .so to view detail / 选择查看 .so 详情
     * @param library .so library info / .so 库信息
     */
    data class SelectSoDetail(val library: SoLibrary) : WearOs64BitIntent

    /** Clear selected .so detail / 清除选中的 .so 详情 */
    data object ClearSoDetail : WearOs64BitIntent

    /** Search SDK compatibility / 搜索 SDK 兼容性
     * @param query Search query / 搜索查询
     */
    data class SearchSdk(val query: String) : WearOs64BitIntent

    /** Toggle module selection for project scan / 切换项目模块选择状态
     * @param moduleName Module name / 模块名称
     */
    data class ToggleModuleSelection(val moduleName: String) : WearOs64BitIntent

    /** Start project scan / 开始项目扫描
     * @param projectPath Project path / 项目路径
     */
    data class StartProjectScan(val projectPath: String) : WearOs64BitIntent

    /** Toggle .so selection in fix wizard / 在修复向导中切换 .so 选择状态
     * @param soName .so file name / .so 文件名
     */
    data class ToggleFixSoSelection(val soName: String) : WearOs64BitIntent

    /** Set fix strategy / 设置修复策略
     * @param strategy Fix strategy / 修复策略
     */
    data class SetFixStrategy(val strategy: FixStrategy) : WearOs64BitIntent

    /** Start fix wizard / 开始修复向导 */
    data object StartFixWizard : WearOs64BitIntent

    /** Next wizard step / 下一步
     */
    data object NextWizardStep : WearOs64BitIntent

    /** Previous wizard step / 上一步
     */
    data object PrevWizardStep : WearOs64BitIntent

    /** Execute fix / 执行修复
     */
    data object ExecuteFix : WearOs64BitIntent

    /** Reset wizard / 重置向导
     */
    data object ResetWizard : WearOs64BitIntent

    /** Filter tracker list / 筛选进度列表
     * @param filter Filter option / 筛选选项
     */
    data class FilterTracker(val filter: TrackerFilter) : WearOs64BitIntent

    /** Export report / 导出报告
     * @param isJson Whether export as JSON (false = PDF) / 是否导出为 JSON
     */
    data class ExportReport(val isJson: Boolean) : WearOs64BitIntent

    /** Dismiss error / 关闭错误信息
     */
    data object DismissError : WearOs64BitIntent
}

// =============================================================
// WearOs64BitEffect — 副作用
// =============================================================
/**
 * Wear OS 64-Bit Compliance Tool Side Effects / Wear OS 64位合规工具副作用
 *
 * One-time events, consumed only once by UI layer.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 */
sealed interface WearOs64BitEffect {
    /** Navigate to .so detail page / 导航到 .so 详情页
     * @param library .so library info / .so 库信息
     */
    data class NavigateToSoDetail(val library: SoLibrary) : WearOs64BitEffect

    /** Show export success message / 显示导出成功消息
     * @param filePath Exported file path / 导出文件路径
     */
    data class ShowExportSuccess(val filePath: String) : WearOs64BitEffect

    /** Show error message / 显示错误消息
     * @param message Error message / 错误信息
     */
    data class ShowError(val message: String) : WearOs64BitEffect

    /** Show toast message / 显示 Toast 消息
     * @param message Toast message / Toast 消息
     */
    data class ShowToast(val message: String) : WearOs64BitEffect

    /** Open file picker / 打开文件选择器
     */
    data object OpenFilePicker : WearOs64BitEffect

    /** Scroll to top / 滚动到顶部
     */
    data object ScrollToTop : WearOs64BitEffect
}
