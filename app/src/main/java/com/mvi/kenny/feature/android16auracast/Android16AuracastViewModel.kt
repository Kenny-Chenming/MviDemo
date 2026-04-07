package com.mvi.kenny.feature.android16auracast

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ============================================================
 * Android16AuracastViewModel — Android 16 Auracast 开发工具包 ViewModel
 * ============================================================
 * PRD-045 | Android 16 Beta 3 Platform Stability 迁移与 Auracast 开发工具包
 *
 * 职责：
 * - 管理所有 Tab 的状态（State）
 * - 处理用户操作（Intent）
 * - 触发一次性副作用（Effect）
 *
 * ⚠️ 注意：Auracast LE Audio 功能需要物理设备测试
 * ⚠️ 注意：Outline Text API 仅在 Android 16+（VanillaIceCream）可用
 *
 * @see Android16AuracastContract MVI 契约定义
 * @see Android16AuracastScreen UI 渲染层
 */
class Android16AuracastViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — 页面状态的唯一真相来源
    // ─────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(Android16AuracastState())
    val state: StateFlow<Android16AuracastState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect Channel — 一次性副作用（Snackbar、导航等）
    // ─────────────────────────────────────────────────────────────

    private val _effect = Channel<Android16AuracastEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal State — 各模块独立状态（委托给主 State）
    // ─────────────────────────────────────────────────────────────

    private val _auracastBroadcastState = MutableStateFlow(AuracastBroadcastState())
    private val _auracastReceiverState = MutableStateFlow(AuracastReceiverState())
    private val _outlineTextState = MutableStateFlow(OutlineTextState())
    private val _localNetworkState = MutableStateFlow(LocalNetworkState())
    private val _behaviorChangeState = MutableStateFlow(BehaviorChangeState())
    private val _migrationState = MutableStateFlow(MigrationState.Initial)

    // ─────────────────────────────────────────────────────────────
    // Internal Jobs — 后台任务引用
    // ─────────────────────────────────────────────────────────────

    private var broadcastJob: Job? = null
    private var scanJob: Job? = null
    private var migrationScanJob: Job? = null

    // ─────────────────────────────────────────────────────────────
    // Intent 处理入口
    // ─────────────────────────────────────────────────────────────

    fun processIntent(intent: Android16AuracastIntent) {
        when (intent) {
            // Tab 切换
            is Android16AuracastIntent.SwitchTab -> switchTab(intent.tab)

            // Auracast 广播发送
            is Android16AuracastIntent.UpdateBroadcastName -> updateBroadcastName(intent.name)
            is Android16AuracastIntent.StartBroadcast -> startBroadcast()
            is Android16AuracastIntent.StopBroadcast -> stopBroadcast()
            is Android16AuracastIntent.PauseBroadcast -> pauseBroadcast()
            is Android16AuracastIntent.ResumeBroadcast -> resumeBroadcast()

            // Auracast 广播接收
            is Android16AuracastIntent.StartScanning -> startScanning()
            is Android16AuracastIntent.StopScanning -> stopScanning()
            is Android16AuracastIntent.ConnectToSource -> connectToSource(intent.sourceId)
            is Android16AuracastIntent.DisconnectSource -> disconnectSource()

            // Outline Text
            is Android16AuracastIntent.UpdateOutlineText -> updateOutlineText(intent.text)
            is Android16AuracastIntent.UpdateOutlineStyle -> updateOutlineStyle(intent.style)
            is Android16AuracastIntent.ToggleAutoAccessible -> toggleAutoAccessible(intent.enabled)

            // Local Network 权限
            is Android16AuracastIntent.RequestLocalNetworkPermission -> requestLocalNetworkPermission()
            is Android16AuracastIntent.ShowPermissionIntro -> showPermissionIntro()
            is Android16AuracastIntent.DismissPermissionIntro -> dismissPermissionIntro()
            is Android16AuracastIntent.OpenAppSettings -> openAppSettings()
            is Android16AuracastIntent.UpdatePermissionState -> updatePermissionState(intent.state)

            // 行为变更速查
            is Android16AuracastIntent.SelectVersionTab -> selectVersionTab(intent.version)
            is Android16AuracastIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is Android16AuracastIntent.FilterBySeverity -> filterBySeverity(intent.severity)
            is Android16AuracastIntent.FilterByCategory -> filterByCategory(intent.category)
            is Android16AuracastIntent.ExpandChange -> expandChange(intent.changeId)
            is Android16AuracastIntent.CollapseAllChanges -> collapseAllChanges()

            // 迁移检测
            is Android16AuracastIntent.UpdateProjectPath -> updateProjectPath(intent.path)
            is Android16AuracastIntent.UpdateTargetVersion -> updateTargetVersion(intent.version)
            is Android16AuracastIntent.StartMigrationScan -> startMigrationScan()
            is Android16AuracastIntent.CancelMigrationScan -> cancelMigrationScan()
            is Android16AuracastIntent.ExportMigrationReport -> exportMigrationReport()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Tab 切换
    // ─────────────────────────────────────────────────────────────

    private fun switchTab(tab: Android16Tab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    // ═══════════════════════════════════════════════════════════════
    // Auracast 广播发送模块
    // ═══════════════════════════════════════════════════════════════

    private fun updateBroadcastName(name: String) {
        val current = _auracastBroadcastState.value
        _auracastBroadcastState.value = current.copy(broadcastName = name, error = null)
        syncAuracastBroadcastState()
    }

    /**
     * 启动 Auracast 广播
     *
     * 实际需要 BluetoothAdapter + LE Audio AudioBroadcastService。
     * 这里用模拟实现，真实场景需要：
     * 1. 检查 BLUETOOTH_ADVERTISE 权限
     * 2. 获取 BluetoothLeAdvertiser
     * 3. 构建 AudioBroadcastServiceInfo
     * 4. 调用 startLeAudioBroadcast()
     */
    private fun startBroadcast() {
        val name = _auracastBroadcastState.value.broadcastName.ifBlank { "My Auracast" }
        val sessionId = UUID.randomUUID().toString()

        _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
            status = BroadcastStatus.STARTING,
            error = null
        )
        syncAuracastBroadcastState()

        broadcastJob = viewModelScope.launch {
            try {
                // 模拟广播启动延迟（真实场景：调用 LE Audio API）
                delay(1500)

                val session = BroadcastSession(
                    sessionId = sessionId,
                    name = name,
                    broadcastId = "auracast_${sessionId.take(8)}",
                    status = BroadcastStatus.BROADCASTING
                )

                _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
                    isBroadcasting = true,
                    broadcastSession = session,
                    status = BroadcastStatus.BROADCASTING
                )
                syncAuracastBroadcastState()

                _effect.send(Android16AuracastEffect.BroadcastStarted(session))
                _effect.send(Android16AuracastEffect.ShowSnackbar("广播已启动：$name"))

            } catch (e: Exception) {
                _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
                    status = BroadcastStatus.ERROR,
                    error = e.message ?: "启动广播失败"
                )
                syncAuracastBroadcastState()
            }
        }
    }

    /** 停止广播 */
    private fun stopBroadcast() {
        broadcastJob?.cancel()
        _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
            isBroadcasting = false,
            broadcastSession = null,
            status = BroadcastStatus.IDLE,
            error = null
        )
        syncAuracastBroadcastState()
        viewModelScope.launch {
            _effect.send(Android16AuracastEffect.ShowSnackbar("广播已停止"))
        }
    }

    /** 暂停广播（模拟） */
    private fun pauseBroadcast() {
        val session = _auracastBroadcastState.value.broadcastSession ?: return
        _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
            broadcastSession = session.copy(status = BroadcastStatus.PAUSED),
            status = BroadcastStatus.PAUSED
        )
        syncAuracastBroadcastState()
    }

    /** 恢复广播（模拟） */
    private fun resumeBroadcast() {
        val session = _auracastBroadcastState.value.broadcastSession ?: return
        _auracastBroadcastState.value = _auracastBroadcastState.value.copy(
            broadcastSession = session.copy(status = BroadcastStatus.BROADCASTING),
            status = BroadcastStatus.BROADCASTING
        )
        syncAuracastBroadcastState()
    }

    // ═══════════════════════════════════════════════════════════════
    // Auracast 广播接收模块
    // ═══════════════════════════════════════════════════════════════

    /**
     * 开始扫描附近的 Auracast 广播源
     *
     * 实际需要：
     * 1. BLUETOOTH_SCAN + NEARBY_WIFI_DEVICES 权限
     * 2. AudioBroadcastClient.startScan()
     * 3. 监听 AUDIO_BROADCAST_PROGRESS / AUDIO_BROADCAST_FOUND 广播
     */
    private fun startScanning() {
        _auracastReceiverState.value = _auracastReceiverState.value.copy(
            status = ReceiverStatus.SCANNING,
            isScanning = true,
            availableSources = emptyList(),
            error = null
        )
        syncAuracastReceiverState()

        scanJob = viewModelScope.launch {
            try {
                // 模拟发现源（真实场景：注册 BroadcastClientCallback）
                delay(800)
                val mockSources = listOf(
                    AuracastSource(
                        sourceId = "source_1",
                        name = "Living Room Speaker",
                        deviceName = "Google Nest Audio",
                        signalStrength = -45,
                        isConnected = false
                    ),
                    AuracastSource(
                        sourceId = "source_2",
                        name = "Office Headphones",
                        deviceName = "Sony WH-1000XM5",
                        signalStrength = -62,
                        isConnected = false
                    ),
                    AuracastSource(
                        sourceId = "source_3",
                        name = "Kitchen Audio",
                        deviceName = "Sonos Era 100",
                        signalStrength = -71,
                        isConnected = false
                    )
                )

                _auracastReceiverState.value = _auracastReceiverState.value.copy(
                    availableSources = mockSources,
                    status = ReceiverStatus.SCANNING
                )
                syncAuracastReceiverState()

                _effect.send(Android16AuracastEffect.ShowToast("发现 ${mockSources.size} 个 Auracast 源"))

            } catch (e: Exception) {
                _auracastReceiverState.value = _auracastReceiverState.value.copy(
                    status = ReceiverStatus.ERROR,
                    isScanning = false,
                    error = e.message ?: "扫描失败"
                )
                syncAuracastReceiverState()
            }
        }
    }

    /** 停止扫描 */
    private fun stopScanning() {
        scanJob?.cancel()
        _auracastReceiverState.value = _auracastReceiverState.value.copy(
            status = ReceiverStatus.IDLE,
            isScanning = false
        )
        syncAuracastReceiverState()
    }

    /** 连接到指定广播源 */
    private fun connectToSource(sourceId: String) {
        val source = _auracastReceiverState.value.availableSources.find { it.sourceId == sourceId }
            ?: return

        _auracastReceiverState.value = _auracastReceiverState.value.copy(
            status = ReceiverStatus.RECEIVING,
            connectedSourceId = sourceId,
            availableSources = _auracastReceiverState.value.availableSources.map {
                it.copy(isConnected = it.sourceId == sourceId)
            }
        )
        syncAuracastReceiverState()

        viewModelScope.launch {
            _effect.send(Android16AuracastEffect.ShowSnackbar("已连接到：${source.name}"))
        }
    }

    /** 断开连接 */
    private fun disconnectSource() {
        _auracastReceiverState.value = _auracastReceiverState.value.copy(
            status = ReceiverStatus.SCANNING,
            connectedSourceId = null,
            availableSources = _auracastReceiverState.value.availableSources.map {
                it.copy(isConnected = false)
            }
        )
        syncAuracastReceiverState()
    }

    // ═══════════════════════════════════════════════════════════════
    // Outline Text 模块
    // ═══════════════════════════════════════════════════════════════

    private fun updateOutlineText(text: String) {
        _outlineTextState.value = _outlineTextState.value.copy(textInput = text)
        syncOutlineTextState()
    }

    private fun updateOutlineStyle(style: OutlineTextStyle) {
        _outlineTextState.value = _outlineTextState.value.copy(currentStyle = style)
        syncOutlineTextState()
    }

    private fun toggleAutoAccessible(enabled: Boolean) {
        _outlineTextState.value = _outlineTextState.value.copy(autoAccessible = enabled)
        syncOutlineTextState()
    }

    // ═══════════════════════════════════════════════════════════════
    // Local Network 权限模块
    // ═══════════════════════════════════════════════════════════════

    private fun showPermissionIntro() {
        _localNetworkState.value = _localNetworkState.value.copy(showIntro = true)
        syncLocalNetworkState()
    }

    private fun dismissPermissionIntro() {
        _localNetworkState.value = _localNetworkState.value.copy(showIntro = false)
        syncLocalNetworkState()
    }

    private fun requestLocalNetworkPermission() {
        _localNetworkState.value = _localNetworkState.value.copy(
            showIntro = false,
            permissionState = PermissionState.GRANTED // 模拟授权成功
        )
        syncLocalNetworkState()
        viewModelScope.launch {
            _effect.send(Android16AuracastEffect.ShowSnackbar("Local Network 权限已授权"))
        }
    }

    private fun openAppSettings() {
        viewModelScope.launch {
            _effect.send(Android16AuracastEffect.OpenSystemSettings)
        }
    }

    private fun updatePermissionState(state: PermissionState) {
        _localNetworkState.value = _localNetworkState.value.copy(
            permissionState = state,
            showDeniedPage = state == PermissionState.DENIED || state == PermissionState.DENIED_FOREVER
        )
        syncLocalNetworkState()
    }

    // ═══════════════════════════════════════════════════════════════
    // 行为变更速查模块
    // ═══════════════════════════════════════════════════════════════

    private fun selectVersionTab(version: String) {
        val changes = loadBehaviorChangesForVersion(version)
        _behaviorChangeState.value = _behaviorChangeState.value.copy(
            selectedVersionTab = version,
            behaviorChanges = changes,
            searchQuery = "",
            selectedSeverity = null,
            selectedCategory = null
        )
        syncBehaviorChangeState()
    }

    private fun updateSearchQuery(query: String) {
        _behaviorChangeState.value = _behaviorChangeState.value.copy(searchQuery = query)
        syncBehaviorChangeState()
    }

    private fun filterBySeverity(severity: Severity?) {
        _behaviorChangeState.value = _behaviorChangeState.value.copy(selectedSeverity = severity)
        syncBehaviorChangeState()
    }

    private fun filterByCategory(category: BehaviorCategory?) {
        _behaviorChangeState.value = _behaviorChangeState.value.copy(selectedCategory = category)
        syncBehaviorChangeState()
    }

    private fun expandChange(changeId: String) {
        _behaviorChangeState.value = _behaviorChangeState.value.copy(
            expandedChangeId = if (_behaviorChangeState.value.expandedChangeId == changeId) null else changeId
        )
        syncBehaviorChangeState()
    }

    private fun collapseAllChanges() {
        _behaviorChangeState.value = _behaviorChangeState.value.copy(expandedChangeId = null)
        syncBehaviorChangeState()
    }

    // ═══════════════════════════════════════════════════════════════
    // 迁移检测模块
    // ═══════════════════════════════════════════════════════════════

    private fun updateProjectPath(path: String) {
        _migrationState.value = _migrationState.value.copy(projectPath = path, error = null)
        syncMigrationState()
    }

    private fun updateTargetVersion(version: String) {
        _migrationState.value = _migrationState.value.copy(targetVersion = version, error = null)
        syncMigrationState()
    }

    /**
     * 启动迁移检测扫描
     *
     * 实际 Gradle 插件端需要：
     * 1. 在 configure<AndroidComponentsExtension> 中注册 Variant 级扫描
     * 2. 解析 behavior change 的 AST
     * 3. 对接 Google 官方 API Surface 文档
     * 此处用模拟实现
     */
    private fun startMigrationScan() {
        val projectPath = _migrationState.value.projectPath
        if (projectPath.isBlank()) {
            viewModelScope.launch {
                _effect.send(Android16AuracastEffect.ShowError("错误", "请输入项目路径"))
            }
            return
        }

        _migrationState.value = _migrationState.value.copy(
            isScanning = true,
            scanProgress = 0f,
            currentFile = "",
            behaviorChanges = emptyList(),
            error = null
        )
        syncMigrationState()

        migrationScanJob = viewModelScope.launch {
            try {
                val mockChanges = generateMockBehaviorChanges()

                for ((index, change) in mockChanges.withIndex()) {
                    delay(400) // 模拟逐文件扫描
                    _migrationState.value = _migrationState.value.copy(
                        scanProgress = (index + 1).toFloat() / mockChanges.size,
                        currentFile = change.affectedFiles.firstOrNull() ?: "Unknown",
                        behaviorChanges = mockChanges.take(index + 1)
                    )
                    syncMigrationState()
                }

                _migrationState.value = _migrationState.value.copy(
                    isScanning = false,
                    scanProgress = 1f,
                    currentFile = ""
                )
                syncMigrationState()

                _effect.send(Android16AuracastEffect.ShowSnackbar("扫描完成，发现 ${mockChanges.size} 条行为变更"))

            } catch (e: Exception) {
                _migrationState.value = _migrationState.value.copy(
                    isScanning = false,
                    error = e.message ?: "扫描失败"
                )
                syncMigrationState()
            }
        }
    }

    private fun cancelMigrationScan() {
        migrationScanJob?.cancel()
        _migrationState.value = _migrationState.value.copy(
            isScanning = false,
            scanProgress = 0f,
            currentFile = ""
        )
        syncMigrationState()
    }

    private fun exportMigrationReport() {
        viewModelScope.launch {
            val reportPath = "/storage/emulated/0/Download/android16_migration_report_${System.currentTimeMillis()}.json"
            _effect.send(Android16AuracastEffect.ExportReport(reportPath))
            _effect.send(Android16AuracastEffect.ShowSnackbar("报告已导出到：$reportPath"))
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // State 同步 — 将各模块状态合并到主 State
    // ═══════════════════════════════════════════════════════════════

    private fun syncAuracastBroadcastState() {
        _state.value = _state.value.copy(auracastBroadcastState = _auracastBroadcastState.value)
    }

    private fun syncAuracastReceiverState() {
        _state.value = _state.value.copy(auracastReceiverState = _auracastReceiverState.value)
    }

    private fun syncOutlineTextState() {
        _state.value = _state.value.copy(outlineTextState = _outlineTextState.value)
    }

    private fun syncLocalNetworkState() {
        _state.value = _state.value.copy(localNetworkState = _localNetworkState.value)
    }

    private fun syncBehaviorChangeState() {
        _state.value = _state.value.copy(behaviorChangeState = _behaviorChangeState.value)
    }

    private fun syncMigrationState() {
        _state.value = _state.value.copy(migrationState = _migrationState.value)
    }

    // ═══════════════════════════════════════════════════════════════
    // Mock Data — 模拟数据（实际应从 Gradle 插件或 API Surface 文档读取）
    // ═══════════════════════════════════════════════════════════════

    /**
     * 加载指定版本的行为变更数据
     * 真实场景：从 Gradle 插件生成的报告文件读取，或请求 API Surface 文档
     */
    private fun loadBehaviorChangesForVersion(version: String): List<BehaviorChange> {
        return when (version) {
            "Android 16" -> getAndroid16BehaviorChanges()
            "Android 15" -> getAndroid15BehaviorChanges()
            "Android 14" -> getAndroid14BehaviorChanges()
            else -> emptyList()
        }
    }

    /** 生成模拟行为变更数据 */
    private fun generateMockBehaviorChanges(): List<BehaviorChange> {
        return getAndroid16BehaviorChanges()
    }

    /** Android 16 行为变更（来自 Google 官方 API-diff 文档） */
    private fun getAndroid16BehaviorChanges(): List<BehaviorChange> = listOf(
        BehaviorChange(
            id = "android16-001",
            title = "Local Network 权限强制执行",
            description = "Android 16 正式对 Local Network 上的所有网络访问强制执行 ACCESS_FINE_LOCATION 权限要求。应用在 Local Network 上进行任何网络活动前必须声明并获取该权限。",
            severity = Severity.P0,
            category = BehaviorCategory.PRIVACY,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#network-local-network",
            affectedFiles = listOf("AndroidManifest.xml", "MainActivity.kt"),
            migrationGuide = "在 AndroidManifest.xml 中添加 ACCESS_FINE_LOCATION 权限，并在运行时请求授权。"
        ),
        BehaviorChange(
            id = "android16-002",
            title = "LE Audio Auracast 广播权限变更",
            description = "发送或接收 Auracast 广播需要新的 NEARBY_WIFI_DEVICES 权限（安装时权限），并要求 BLUETOOTH_ADVERTISE / BLUETOOTH_SCAN 权限。",
            severity = Severity.P0,
            category = BehaviorCategory.PRIVACY,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#le-audio-auracast",
            affectedFiles = listOf("AndroidManifest.xml", "AuracastManager.kt"),
            migrationGuide = "添加 NEARBY_WIFI_DEVICES 权限，并更新音频广播/接收逻辑。"
        ),
        BehaviorChange(
            id = "android16-003",
            title = "前台服务类型 FOREGROUND_SERVICE_TYPE_LOCATION 废弃",
            description = "前台位置服务类型在 Android 16 中废弃，应用应迁移到 useInlineResponse 或 JobScheduler。",
            severity = Severity.P1,
            category = BehaviorCategory.DEPRECATION,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#foreground-service-location",
            affectedFiles = listOf("AndroidManifest.xml", "LocationService.kt"),
            migrationGuide = "将前台位置服务迁移为 WorkManager 或 JobScheduler。"
        ),
        BehaviorChange(
            id = "android16-004",
            title = "Photo Picker 强制执行单选模式限制",
            description = "Photo Picker 的 selectSingleResource() API 现在强制执行单选，任何尝试选择多个资源的应用将收到 IllegalArgumentException。",
            severity = Severity.P1,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#photo-picker-single",
            affectedFiles = listOf("PhotoPickerHelper.kt"),
            migrationGuide = "使用 selectSingleResource() 替代 multiSelect() 进行单选场景。"
        ),
        BehaviorChange(
            id = "android16-005",
            title = "Predictive Back Gesture 必需支持",
            description = "所有targetSDK >= Android 16 的应用必须实现 Predictive Back Gesture，否则可能无法通过 Play Store 审核。",
            severity = Severity.P1,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#predictive-back",
            affectedFiles = listOf("MainActivity.kt", "NavigationGraph.xml"),
            migrationGuide = "使用 androidx.activity.compose# PredictiveBackHandler 实现预测性返回动画。"
        ),
        BehaviorChange(
            id = "android16-006",
            title = "SQLite 数据库版本检查强化",
            description = "Android 16 加强了 SQLite 版本检查，打开数据库时的 version 字段类型必须为整数，否则抛出 SQLiteException。",
            severity = Severity.P2,
            category = BehaviorCategory.API,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#sqlite",
            affectedFiles = listOf("DatabaseHelper.kt"),
            migrationGuide = "确保所有 SQLiteOpenHelper 的 version 参数为正整数。"
        ),
        BehaviorChange(
            id = "android16-007",
            title = "Edge-to-Edge 强制全屏内容区",
            description = "所有 targetSDK >= Android 16 的应用默认启用 Edge-to-Edge，应用必须正确处理 WindowInsets 以避免内容被系统栏遮挡。",
            severity = Severity.P1,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#edge-to-edge",
            affectedFiles = listOf("MainActivity.kt", "Theme.xml"),
            migrationGuide = "使用 enableEdgeToEdge() 并正确设置 WindowCompat.setDecorFitsSystemWindows()。"
        ),
        BehaviorChange(
            id = "android16-008",
            title = "Bouncycastle JCA Provider 移除",
            description = "Android 16 移除了内置的 Bouncycastle JCA Provider，应用如使用 Bouncycastle 进行加密需自行打包库。",
            severity = Severity.P2,
            category = BehaviorCategory.DEPRECATION,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#bouncycastle",
            affectedFiles = listOf("CryptoUtil.kt"),
            migrationGuide = "在 build.gradle 中添加 Bouncycastle 依赖：implementation 'org.bouncycastle:bcprov-jdk18on:...'。"
        ),
        BehaviorChange(
            id = "android16-009",
            title = "Accessibility Focus 顺序变更",
            description = "Android 16 改变了无障碍服务焦点遍历顺序，基于屏幕 Z 轴顺序而非 X/Y 坐标。",
            severity = Severity.P2,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#accessibility-focus",
            affectedFiles = listOf("AccessibilityService.kt", "CustomView.kt"),
            migrationGuide = "使用 androidx.core.view.accessibility 明确设置无障碍遍历顺序。"
        ),
        BehaviorChange(
            id = "android16-010",
            title = "Outline Text API 正式引入",
            description = "Android 16 正式引入 Outline Text API，支持药丸形背景 + 文字下方样式的无障碍高亮文字，提升低视力用户的阅读体验。",
            severity = Severity.P2,
            category = BehaviorCategory.API,
            version = "Android 16",
            docUrl = "https://developer.android.com/about/versions/16/behavior-changes-all#outline-text",
            affectedFiles = listOf("OutlineTextDemoPage.kt"),
            migrationGuide = "使用 Compose OutlineText 组件，设置 Build.VERSION.SDK_INT >= VANILLA_ICE_CREAM 版本守卫。"
        )
    )

    /** Android 15 行为变更（简化示例） */
    private fun getAndroid15BehaviorChanges(): List<BehaviorChange> = listOf(
        BehaviorChange(
            id = "android15-001",
            title = "受限外部存储访问",
            description = "Android 15 进一步限制应用访问其他应用的外部存储目录，需使用 MediaStore 或 SAF。",
            severity = Severity.P1,
            category = BehaviorCategory.PRIVACY,
            version = "Android 15",
            docUrl = "https://developer.android.com/about/versions/15/behavior-changes-all#scoped-storage",
            migrationGuide = "迁移到 MediaStore API 或 Storage Access Framework。"
        ),
        BehaviorChange(
            id = "android15-002",
            title = "Photo Picker 成为默认",
            description = "Photo Picker 成为所有 targetSDK 35+ 应用的默认图片选择方式，不再支持直接访问 MediaStore。",
            severity = Severity.P1,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 15",
            docUrl = "https://developer.android.com/about/versions/15/behavior-changes-all#photo-picker",
            migrationGuide = "使用 PhotoPicker API 替代 Intent.ACTION_PICK。"
        )
    )

    /** Android 14 行为变更（简化示例） */
    private fun getAndroid14BehaviorChanges(): List<BehaviorChange> = listOf(
        BehaviorChange(
            id = "android14-001",
            title = "前台服务类型必需声明",
            description = "Android 14 要求所有前台服务必须声明特定类型，否则启动时抛出 IllegalStateException。",
            severity = Severity.P0,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 14",
            docUrl = "https://developer.android.com/about/versions/14/behavior-changes-all#foreground-service-types",
            migrationGuide = "在 AndroidManifest 中声明 FOREGROUND_SERVICE_TYPE 权限。"
        ),
        BehaviorChange(
            id = "android14-002",
            title = "隐式 Intent 过滤限制",
            description = "Android 14 禁止应用通过隐式 Intent 启动内部组件，exported=true 的组件除外。",
            severity = Severity.P1,
            category = BehaviorCategory.BEHAVIOR,
            version = "Android 14",
            docUrl = "https://developer.android.com/about/versions/14/behavior-changes-all#intent-filter-accuracy",
            migrationGuide = "使用显式 Intent 启动内部组件。"
        )
    )

    init {
        // 初始化行为变更数据
        _behaviorChangeState.value = _behaviorChangeState.value.copy(
            behaviorChanges = getAndroid16BehaviorChanges()
        )
        syncBehaviorChangeState()
    }

    // ═══════════════════════════════════════════════════════════════
    // 计算属性 — 从 State 派生
    // ═══════════════════════════════════════════════════════════════

    /** 过滤后的行为变更列表（支持搜索 + 严重程度 + 类别过滤） */
    val filteredBehaviorChanges: List<BehaviorChange>
        get() {
            val s = _behaviorChangeState.value
            return s.behaviorChanges.filter { change ->
                val matchSearch = s.searchQuery.isBlank() ||
                        change.title.contains(s.searchQuery, ignoreCase = true) ||
                        change.description.contains(s.searchQuery, ignoreCase = true)
                val matchSeverity = s.selectedSeverity == null || change.severity == s.selectedSeverity
                val matchCategory = s.selectedCategory == null || change.category == s.selectedCategory
                matchSearch && matchSeverity && matchCategory
            }
        }

    /** 是否支持 Outline Text API */
    val isOutlineTextSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}
