package com.mvi.kenny.feature.locationpermission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ============================================================
 * LocationPermissionViewModel — 位置权限管控状态管理
 * ============================================================
 * Manages page state and business logic for the location permission dashboard.
 *
 * State management:
 * - _state: Private MutableStateFlow, written internally by ViewModel
 * - state: Public StateFlow, exposed to UI layer (collected via collectAsState)
 *
 * Effect management:
 * - _effect: Channel (hot flow), buffer size BUFFERED
 * - effect: receiveAsFlow, UI layer listens via collect{}
 *
 * Why Channel instead of StateFlow for Effects?
 * —————————————————————————————————————————————————————
 * StateFlow remembers the current value, new subscribers receive the last value.
 * Channel only passes new events, suitable for "one-time" events (navigation, toast).
 *
 * @see LocationPermissionState Immutable page state
 * @see LocationPermissionIntent User intentions
 * @see LocationPermissionEffect One-time side effects
 */
class LocationPermissionViewModel : ViewModel() {

    // ============================================================
    // State — 页面状态
    // ============================================================
    private val _state = MutableStateFlow(LocationPermissionState.Initial)
    val state: StateFlow<LocationPermissionState> = _state.asStateFlow()

    // ============================================================
    // Effect — 一次性副作用（通过 Channel 传递）
    // ============================================================
    private val _effect = Channel<LocationPermissionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        // 初始化时自动执行一次扫描 / Auto-scan on init
        sendIntent(LocationPermissionIntent.StartScan)
    }

    /**
     * ============================================================
     * sendIntent — 处理用户意图的核心方法
     * ============================================================
     * All user actions call this method to dispatch Intents.
     * ViewModel processes each Intent and updates State or sends Effects.
     *
     * @param intent User intention to process
     *
     * @see LocationPermissionIntent All available Intent types
     */
    fun sendIntent(intent: LocationPermissionIntent) {
        viewModelScope.launch {
            when (intent) {
                is LocationPermissionIntent.StartScan -> handleStartScan()
                is LocationPermissionIntent.RevokeAllActive -> handleRevokeAllActive()
                is LocationPermissionIntent.RevokeApp -> handleRevokeApp(intent.packageName)
                is LocationPermissionIntent.DowngradePermission -> handleDowngrade(intent.packageName)
                is LocationPermissionIntent.SelectApp -> handleSelectApp(intent.packageName)
                is LocationPermissionIntent.DismissAppDetail -> handleDismissAppDetail()
                is LocationPermissionIntent.SwitchTab -> handleSwitchTab(intent.tab)
                is LocationPermissionIntent.SetListFilter -> handleSetListFilter(intent.filter)
                is LocationPermissionIntent.SetHistoryFilter -> handleSetHistoryFilter(intent.filter)
                is LocationPermissionIntent.UpdateSettings -> handleUpdateSettings(intent.settings)
                is LocationPermissionIntent.ClearError -> handleClearError()
            }
        }
    }

    // ============================================================
    // Intent Handlers — 意图处理方法
    // ============================================================

    /**
     * 处理 StartScan 意图
     * —————————————————————————————————————————————————————
     * 模拟扫描：设置 isScanning = true，延迟后加载模拟数据。
     * 实际实现中，这里应调用 PackageManager + UsageStatsManager 获取真实数据。
     */
    private suspend fun handleStartScan() {
        _state.value = _state.value.copy(isScanning = true, errorMessage = null)
        try {
            // 模拟后台扫描延迟 / Simulate background scan delay
            delay(1500)

            // 生成模拟数据（实际实现中替换为真实 API 调用）
            // Generate mock data (replace with real API calls in production)
            val mockActiveApps = buildMockActiveApps()
            val mockRecentApps = buildMockRecentApps()
            val mockAllApps = mockActiveApps + mockRecentApps + buildMockNeverAccessedApps()
            val mockHistory = buildMockHistory()

            val threatLevel = computeThreatLevel(mockActiveApps, mockRecentApps)

            _state.value = _state.value.copy(
                isScanning = false,
                activeApps = mockActiveApps,
                recentApps = mockRecentApps,
                allApps = mockAllApps,
                historyByDay = mockHistory,
                threatLevel = threatLevel
            )
            _effect.send(LocationPermissionEffect.ScanComplete)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isScanning = false,
                errorMessage = "扫描失败: ${e.message}"
            )
        }
    }

    /**
     * 处理 RevokeAllActive 意图 — 撤销所有活跃 App 的精确位置权限
     * —————————————————————————————————————————————————————
     * 模拟实现：实际调用 PackageManager.setPermissionGrantState 或跳转系统设置。
     */
    private suspend fun handleRevokeAllActive() {
        val activeApps = _state.value.activeApps
        if (activeApps.isEmpty()) {
            _effect.send(LocationPermissionEffect.ShowToast("没有正在访问位置的 App"))
            return
        }
        // 模拟撤销操作：清除 activeApps
        val updatedAllApps = _state.value.allApps.map { app ->
            if (activeApps.any { it.packageName == app.packageName }) {
                app.copy(
                    permissionType = LocationPermissionType.COARSE,
                    isActive = false
                )
            } else app
        }
        _state.value = _state.value.copy(
            activeApps = emptyList(),
            allApps = updatedAllApps,
            threatLevel = ThreatLevel.NONE
        )
        _effect.send(LocationPermissionEffect.EmergencyBlockTriggered)
        _effect.send(LocationPermissionEffect.ShowToast("已阻断 ${activeApps.size} 个 App 的精确位置访问"))
    }

    /**
     * 处理 RevokeApp 意图 — 撤销单个 App 的精确位置权限
     */
    private suspend fun handleRevokeApp(packageName: String) {
        val app = _state.value.allApps.find { it.packageName == packageName } ?: return
        val updatedAllApps = _state.value.allApps.map {
            if (it.packageName == packageName) {
                it.copy(
                    permissionType = LocationPermissionType.NONE,
                    isActive = false
                )
            } else it
        }
        val updatedActive = _state.value.activeApps.filter { it.packageName != packageName }
        _state.value = _state.value.copy(
            allApps = updatedAllApps,
            activeApps = updatedActive,
            selectedApp = null,
            threatLevel = computeThreatLevel(updatedActive, _state.value.recentApps)
        )
        _effect.send(LocationPermissionEffect.PermissionRevoked(app.appName))
        _effect.send(LocationPermissionEffect.ShowToast("${app.appName} 的位置权限已撤销"))
    }

    /**
     * 处理 DowngradePermission 意图 — 将 App 降级为粗略位置
     */
    private suspend fun handleDowngrade(packageName: String) {
        val app = _state.value.allApps.find { it.packageName == packageName } ?: return
        val updatedAllApps = _state.value.allApps.map {
            if (it.packageName == packageName) {
                it.copy(
                    permissionType = LocationPermissionType.COARSE,
                    isActive = false
                )
            } else it
        }
        val updatedActive = _state.value.activeApps.filter { it.packageName != packageName }
        _state.value = _state.value.copy(
            allApps = updatedAllApps,
            activeApps = updatedActive,
            selectedApp = null,
            threatLevel = computeThreatLevel(updatedActive, _state.value.recentApps)
        )
        _effect.send(LocationPermissionEffect.ShowToast("${app.appName} 已降级为粗略位置"))
    }

    /**
     * 处理 SelectApp 意图 — 选中 App 打开详情
     */
    private fun handleSelectApp(packageName: String) {
        val app = _state.value.allApps.find { it.packageName == packageName }
        _state.value = _state.value.copy(selectedApp = app)
    }

    /**
     * 处理 DismissAppDetail 意图 — 关闭详情 BottomSheet
     */
    private fun handleDismissAppDetail() {
        _state.value = _state.value.copy(selectedApp = null)
    }

    /**
     * 处理 SwitchTab 意图 — 切换内部 Tab
     */
    private fun handleSwitchTab(tab: LocationTab) {
        _state.value = _state.value.copy(activeTab = tab)
    }

    /**
     * 处理 SetListFilter 意图 — 设置列表筛选条件
     */
    private fun handleSetListFilter(filter: AppListFilter) {
        _state.value = _state.value.copy(listTabFilter = filter)
    }

    /**
     * 处理 SetHistoryFilter 意图 — 设置历史筛选条件
     */
    private fun handleSetHistoryFilter(filter: HistoryFilter) {
        _state.value = _state.value.copy(
            historyByDay = if (filter.showOnlyPrecise) {
                _state.value.historyByDay.mapValues { (_, events) ->
                    events.filter { it.isPrecise }
                }
            } else _state.value.historyByDay
        )
    }

    /**
     * 处理 UpdateSettings 意图 — 更新设置
     */
    private fun handleUpdateSettings(settings: PermissionSettings) {
        _state.value = _state.value.copy(settings = settings)
        viewModelScope.launch {
            _effect.send(LocationPermissionEffect.ShowToast("设置已保存"))
        }
    }

    /**
     * 处理 ClearError 意图 — 清除错误信息
     */
    private fun handleClearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    // ============================================================
    // Threat Level Computation — 威胁等级计算
    // ============================================================

    /**
     * 根据活跃 App 和近期 App 计算威胁等级
     *
     * Threat level logic:
     * - HIGH: 3+ active precise location apps OR any app with FINE_ONE_TIME
     * - MEDIUM: 1-2 active precise location apps
     * - LOW: recent apps with location history
     * - NONE: no apps accessing location
     */
    private fun computeThreatLevel(
        activeApps: List<AppLocationInfo>,
        recentApps: List<AppLocationInfo>
    ): ThreatLevel {
        val preciseActive = activeApps.count {
            it.permissionType == LocationPermissionType.FINE ||
            it.permissionType == LocationPermissionType.FINE_ONE_TIME ||
            it.permissionType == LocationPermissionType.ALWAYS_FINE
        }
        val hasOneTime = activeApps.any { it.permissionType == LocationPermissionType.FINE_ONE_TIME }

        return when {
            preciseActive >= 3 || hasOneTime -> ThreatLevel.HIGH
            preciseActive >= 1 -> ThreatLevel.MEDIUM
            recentApps.isNotEmpty() -> ThreatLevel.LOW
            else -> ThreatLevel.NONE
        }
    }

    // ============================================================
    // Mock Data Builders — 模拟数据构建器
    // ============================================================
    // In production, replace these with real PackageManager / UsageStatsManager calls

    private fun buildMockActiveApps(): List<AppLocationInfo> = listOf(
        AppLocationInfo(
            packageName = "com.example.mapapp",
            appName = "地图导航",
            permissionType = LocationPermissionType.FINE,
            lastAccessTime = LocalDateTime.now().minusMinutes(2),
            accessCount = 47,
            permissionSource = PermissionSource.USER_GRANTED,
            isActive = true
        ),
        AppLocationInfo(
            packageName = "com.example.rideshare",
            appName = "出行打车",
            permissionType = LocationPermissionType.FINE_ONE_TIME,
            lastAccessTime = LocalDateTime.now().minusMinutes(8),
            accessCount = 3,
            permissionSource = PermissionSource.USER_GRANTED,
            isActive = true
        )
    )

    private fun buildMockRecentApps(): List<AppLocationInfo> = listOf(
        AppLocationInfo(
            packageName = "com.example.weather",
            appName = "天气预报",
            permissionType = LocationPermissionType.COARSE,
            lastAccessTime = LocalDateTime.now().minusHours(1),
            accessCount = 12,
            permissionSource = PermissionSource.SYSTEM_DEFAULT,
            isActive = false
        ),
        AppLocationInfo(
            packageName = "com.example.social",
            appName = "社交分享",
            permissionType = LocationPermissionType.FINE,
            lastAccessTime = LocalDateTime.now().minusDays(1),
            accessCount = 8,
            permissionSource = PermissionSource.USER_GRANTED,
            isActive = false
        ),
        AppLocationInfo(
            packageName = "com.example.fitness",
            appName = "健身运动",
            permissionType = LocationPermissionType.ALWAYS_FINE,
            lastAccessTime = LocalDateTime.now().minusDays(2),
            accessCount = 34,
            permissionSource = PermissionSource.USER_GRANTED,
            isActive = false
        )
    )

    private fun buildMockNeverAccessedApps(): List<AppLocationInfo> = listOf(
        AppLocationInfo(
            packageName = "com.example.calculator",
            appName = "计算器",
            permissionType = LocationPermissionType.NONE,
            lastAccessTime = null,
            accessCount = 0,
            permissionSource = PermissionSource.SYSTEM_DEFAULT,
            isActive = false
        ),
        AppLocationInfo(
            packageName = "com.example.notes",
            appName = "笔记",
            permissionType = LocationPermissionType.NONE,
            lastAccessTime = null,
            accessCount = 0,
            permissionSource = PermissionSource.SYSTEM_DEFAULT,
            isActive = false
        )
    )

    private fun buildMockHistory(): Map<LocalDate, List<LocationEvent>> {
        val today = LocalDate.now()
        return mapOf(
            today to listOf(
                LocationEvent(
                    id = 1,
                    packageName = "com.example.mapapp",
                    appName = "地图导航",
                    eventTime = LocalDateTime.now().minusMinutes(2),
                    permissionType = LocationPermissionType.FINE,
                    isPrecise = true
                ),
                LocationEvent(
                    id = 2,
                    packageName = "com.example.rideshare",
                    appName = "出行打车",
                    eventTime = LocalDateTime.now().minusMinutes(8),
                    permissionType = LocationPermissionType.FINE_ONE_TIME,
                    isPrecise = true
                ),
                LocationEvent(
                    id = 3,
                    packageName = "com.example.weather",
                    appName = "天气预报",
                    eventTime = LocalDateTime.now().minusHours(1),
                    permissionType = LocationPermissionType.COARSE,
                    isPrecise = false
                )
            ),
            today.minusDays(1) to listOf(
                LocationEvent(
                    id = 4,
                    packageName = "com.example.social",
                    appName = "社交分享",
                    eventTime = LocalDateTime.now().minusDays(1).withHour(14),
                    permissionType = LocationPermissionType.FINE,
                    isPrecise = true
                )
            )
        )
    }
}
