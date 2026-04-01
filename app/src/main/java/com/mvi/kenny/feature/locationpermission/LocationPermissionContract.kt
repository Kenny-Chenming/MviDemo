package com.mvi.kenny.feature.locationpermission

/**
 * ============================================================
 * LocationPermissionContract — 位置权限管控 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) architecture contract.
 *
 * MVI 三要素 / Three pillars:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable function, consumes State, renders UI
 * - Intent: User intentions (user actions), ViewModel processes and updates State
 *
 * Effect: One-time side effects (navigation, toast), delivered via Channel
 * —————————————————————————————————————————————————————
 */

/**
 * ============================================================
 * LocationPermissionState — 位置权限管控页面状态
 * ============================================================
 *
 * @param isScanning Whether a location permission scan is in progress
 * @param activeApps Apps that are currently accessing location
 * @param recentApps Apps that accessed location in the past 7 days
 * @param allApps All apps (including those never accessed location)
 * @param historyByDay Location events grouped by day
 * @param selectedApp Currently selected app (for detail bottom sheet)
 * @param threatLevel Current overall threat level
 * @param settings User's permission management settings
 * @param activeTab Currently active inner tab: dashboard / list / history / settings
 * @param listTabFilter Filter for the app list tab
 * @param errorMessage Error message if any, null means no error
 *
 * @see AppLocationInfo App location info data model
 * @see ThreatLevel Threat level enum
 * @see PermissionSettings Settings data model
 */
data class LocationPermissionState(
    val isScanning: Boolean = false,
    val activeApps: List<AppLocationInfo> = emptyList(),
    val recentApps: List<AppLocationInfo> = emptyList(),
    val allApps: List<AppLocationInfo> = emptyList(),
    val historyByDay: Map<java.time.LocalDate, List<LocationEvent>> = emptyMap(),
    val selectedApp: AppLocationInfo? = null,
    val threatLevel: ThreatLevel = ThreatLevel.NONE,
    val settings: PermissionSettings = PermissionSettings(),
    val activeTab: LocationTab = LocationTab.DASHBOARD,
    val listTabFilter: AppListFilter = AppListFilter.ALL,
    val errorMessage: String? = null
) {
    companion object {
        /** Initial/empty state */
        val Initial = LocationPermissionState()
    }
}

/**
 * ============================================================
 * LocationTab — 位置权限管控内部 Tab 枚举
 * ============================================================
 *
 * @param title Chinese title for tab display
 */
enum class LocationTab(val title: String) {
    /** 仪表盘首页 */
    DASHBOARD("仪表盘"),
    /** App 权限列表 */
    APP_LIST("权限列表"),
    /** 位置访问历史 */
    HISTORY("访问历史"),
    /** 设置页 */
    SETTINGS("设置")
}

/**
 * ============================================================
 * AppListFilter — App 列表筛选条件枚举
 * ============================================================
 *
 * @param label Chinese label for filter display
 */
enum class AppListFilter(val label: String) {
    /** 全部 App */
    ALL("全部"),
    /** 正在访问的 App */
    ACTIVE("正在访问"),
    /** 曾访问过的 App */
    RECENT("曾访问"),
    /** 从未访问过的 App */
    NEVER("从未访问")
}

/**
 * ============================================================
 * LocationPermissionIntent — 用户意图（User Intent）
 * —————————————————————————————————————————————————————
 * Every user action on the page corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see LocationPermissionViewModel.sendIntent handles all Intents
 */
sealed interface LocationPermissionIntent {

    /** 开始扫描位置权限（触发后台扫描） */
    data object StartScan : LocationPermissionIntent

    /** 撤销所有正在访问精确位置的 App */
    data object RevokeAllActive : LocationPermissionIntent

    /** 撤销指定 App 的精确位置权限
     * @param packageName App 包名
     */
    data class RevokeApp(val packageName: String) : LocationPermissionIntent

    /** 将指定 App 降级为粗略位置
     * @param packageName App 包名
     */
    data class DowngradePermission(val packageName: String) : LocationPermissionIntent

    /** 选择 App 查看详情（打开 BottomSheet）
     * @param packageName App 包名
     */
    data class SelectApp(val packageName: String) : LocationPermissionIntent

    /** 关闭 App 详情 BottomSheet */
    data object DismissAppDetail : LocationPermissionIntent

    /** 切换内部 Tab
     * @param tab Target tab
     */
    data class SwitchTab(val tab: LocationTab) : LocationPermissionIntent

    /** 设置 App 列表筛选条件
     * @param filter Filter type
     */
    data class SetListFilter(val filter: AppListFilter) : LocationPermissionIntent

    /** 设置历史记录筛选条件
     * @param filter Filter criteria
     */
    data class SetHistoryFilter(val filter: HistoryFilter) : LocationPermissionIntent

    /** 更新设置
     * @param settings New settings
     */
    data class UpdateSettings(val settings: PermissionSettings) : LocationPermissionIntent

    /** 清除错误信息 */
    data object ClearError : LocationPermissionIntent
}

/**
 * ============================================================
 * LocationPermissionEffect — 一次性副作用（Effect）
 * —————————————————————————————————————————————————————
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see LocationPermissionViewModel _effect.send() sends Effects
 */
sealed interface LocationPermissionEffect {

    /** 显示 Toast 消息
     * @param message Toast text content
     */
    data class ShowToast(val message: String) : LocationPermissionEffect

    /** 扫描完成通知 */
    data object ScanComplete : LocationPermissionEffect

    /** 权限已撤销通知
     * @param appName 被撤销权限的 App 名称
     */
    data class PermissionRevoked(val appName: String) : LocationPermissionEffect

    /** 紧急阻断已触发（所有活跃 App 位置权限被撤销） */
    data object EmergencyBlockTriggered : LocationPermissionEffect

    /** 跳转系统设置页（引导用户手动授权）
     * @param packageName App 包名
     */
    data class NavigateToSystemSettings(val packageName: String) : LocationPermissionEffect

    /** 导航到 App 详情页
     * @param packageName App 包名
     */
    data class NavigateToAppDetail(val packageName: String) : LocationPermissionEffect
}
