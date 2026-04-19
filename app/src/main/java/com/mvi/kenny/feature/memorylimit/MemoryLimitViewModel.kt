package com.mvi.kenny.feature.memorylimit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ============================================================
 * MemoryLimitViewModel — 内存限制检测状态管理
 * ============================================================
 * PRD-123 | Android 17 App 内存限制检测与 LeakCanary Profiler 集成工具包
 *
 * @see MemoryLimitState 页面状态定义
 * @see MemoryLimitIntent 用户意图
 * @see MemoryLimitEffect 副作用
 */
class MemoryLimitViewModel : ViewModel() {

    // ================================================================
    // State / 状态
    // ================================================================
    private val _state = MutableStateFlow(MemoryLimitState.Initial)
    val state: StateFlow<MemoryLimitState> = _state.asStateFlow()

    val currentState: MemoryLimitState get() = _state.value

    // ================================================================
    // Effect / 副作用
    // ================================================================
    private val _effect = Channel<MemoryLimitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ================================================================
    // Available devices / 可用设备列表
    // ================================================================
    private val availableDevices = listOf(
        DeviceInfo(
            model = "Pixel 8 Pro",
            totalRamMb = 12288,
            androidVersion = 37,
            memoryLimitMb = 3072  // 12GB * 0.25 = 3GB
        ),
        DeviceInfo(
            model = "Samsung Galaxy S26",
            totalRamMb = 16384,
            androidVersion = 37,
            memoryLimitMb = 4096  // 16GB * 0.25 = 4GB
        ),
        DeviceInfo(
            model = "Pixel 7a",
            totalRamMb = 8192,
            androidVersion = 36,
            memoryLimitMb = 2048  // 8GB * 0.25 = 2GB
        ),
        DeviceInfo(
            model = "Xiaomi 15 Ultra",
            totalRamMb = 16384,
            androidVersion = 37,
            memoryLimitMb = 4096  // 16GB * 0.25 = 4GB
        ),
        DeviceInfo(
            model = "OnePlus 13",
            totalRamMb = 12288,
            androidVersion = 37,
            memoryLimitMb = 3072  // 12GB * 0.25 = 3GB
        )
    )

    init {
        // 初始化时加载默认设备数据
        loadMockData()
    }

    // ================================================================
    // Intent 处理入口 / Intent entry point
    // ================================================================
    fun sendIntent(intent: MemoryLimitIntent) {
        when (intent) {
            is MemoryLimitIntent.SelectTab -> selectTab(intent.index)
            is MemoryLimitIntent.SelectDevice -> selectDevice(intent.device)
            is MemoryLimitIntent.RefreshMemoryUsage -> refreshMemoryUsage()
            is MemoryLimitIntent.SetLeakSortOption -> setSortOption(intent.option)
            is MemoryLimitIntent.ToggleLeakDetail -> toggleLeakDetail(intent.signature)
            is MemoryLimitIntent.SetAnomalyEnabled -> setAnomalyEnabled(intent.enabled)
            is MemoryLimitIntent.GenerateCIConfig -> generateCIConfig()
            is MemoryLimitIntent.CopyScript -> copyScript(intent.script)
            is MemoryLimitIntent.ViewHeapDump -> viewHeapDump(intent.triggerId)
            is MemoryLimitIntent.ShareReport -> shareReport(intent.content)
        }
    }

    // ================================================================
    // Tab 切换 / Tab switching
    // ================================================================
    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(currentTab = index)
    }

    // ================================================================
    // 设备选择 / Device selection
    // ================================================================
    /**
     * 选择目标设备
     * 根据设备 RAM 计算内存限制并更新状态
     * @param device 设备信息
     */
    private fun selectDevice(device: DeviceInfo) {
        val limitMb = device.memoryLimitMb
        // 模拟当前使用量（设备限制的 30-90%）
        val currentMb = (limitMb * (0.3f + Random.nextFloat() * 0.6f))
        val percent = (currentMb / limitMb * 100f).coerceIn(0f, 100f)
        val risk = when {
            percent > 80 -> RiskLevel.HIGH
            percent > 60 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        // 生成 5 分钟趋势数据（300 个点）
        val trendData = generateTrendData(limitMb, currentMb.toInt())

        _state.value = _state.value.copy(
            selectedDevice = device,
            memoryLimitMb = limitMb.toFloat(),
            currentMemoryUsageMb = currentMb,
            memoryUsagePercent = percent,
            isNearLimit = percent > 80,
            riskLevel = risk,
            memoryTrendData = trendData
        )
    }

    // ================================================================
    // 刷新内存使用 / Refresh memory usage
    // ================================================================
    /**
     * 刷新内存使用数据
     * 模拟实时内存波动
     */
    private fun refreshMemoryUsage() {
        viewModelScope.launch {
            val device = _state.value.selectedDevice ?: return@launch
            val limitMb = device.memoryLimitMb
            val currentMb = (limitMb * (0.3f + Random.nextFloat() * 0.6f))
            val percent = (currentMb / limitMb * 100f).coerceIn(0f, 100f)
            val risk = when {
                percent > 80 -> RiskLevel.HIGH
                percent > 60 -> RiskLevel.MEDIUM
                else -> RiskLevel.LOW
            }

            val currentTrend = _state.value.memoryTrendData.toMutableList()
            currentTrend.add(currentMb)
            if (currentTrend.size > 300) currentTrend.removeAt(0)

            _state.value = _state.value.copy(
                currentMemoryUsageMb = currentMb,
                memoryUsagePercent = percent,
                isNearLimit = percent > 80,
                riskLevel = risk,
                memoryTrendData = currentTrend
            )
            _effect.send(MemoryLimitEffect.ShowSnackbar("内存数据已刷新"))
        }
    }

    // ================================================================
    // 排序选项 / Sort option
    // ================================================================
    private fun setSortOption(option: LeakSortOption) {
        _state.value = _state.value.copy(sortOption = option)
    }

    // ================================================================
    // 泄漏详情 / Leak detail
    // ================================================================
    private fun toggleLeakDetail(signature: String) {
        _state.value = _state.value.copy(
            expandedLeakSignature = if (_state.value.expandedLeakSignature == signature) null else signature
        )
    }

    // ================================================================
    // 异常触发配置 / Anomaly trigger config
    // ================================================================
    private fun setAnomalyEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isAnomalyEnabled = enabled)
    }

    // ================================================================
    // CI 配置生成 / CI config generation
    // ================================================================
    private fun generateCIConfig() {
        viewModelScope.launch {
            val yaml = """
                |# ============================================================
                |# Android 17 Memory Limiter CI/CD Pipeline
                |# PRD-123 | Memory Limit Detection & LeakCanary Profiler Toolkit
                |# ============================================================
                |
                |name: Android Memory Behavior Validation
                |
                |on:
                |  push:
                |    branches: [main, develop]
                |  pull_request:
                |    branches: [main]
                |
                |jobs:
                |  memory-test:
                |    runs-on: ubuntu-latest
                |
                |    steps:
                |      - name: Checkout code
                |        uses: actions/checkout@v4
                |
                |      - name: Setup Android SDK
                |        uses: android-actions/setup-android@v3
                |
                |      - name: Setup JDK 17
                |        uses: actions/setup-java@v4
                |        with:
                |          distribution: 'temurin'
                |          java-version: '17'
                |
                |      # Trigger memory limit anomaly test
                |      - name: Trigger Memory Limiter Anomaly
                |        run: |
                |          adb shell am dumpheap
                |            ${'$'}{ secrets.ANDROID_PACKAGE_NAME | 'com.mvi.kenny' }
                |            /sdcard/memory_anomaly.hprof
                |
                |      - name: Run LeakCanary Tests
                |        run: ./gradlew :app:testDebugUnitTest
                |          -PleakCanary.enabled=true
                |          -Pmemory.limit.mb=${_state.value.memoryLimitMb.toInt()}
                |
                |      - name: Upload Heap Dumps
                |        if: always()
                |        uses: actions/upload-artifact@v4
                |        with:
                |          name: heap-dumps-${'$'}{{ github.run_number }}
                |          path: app/build/outputs heap_dumps/
                |
                |      - name: Analyze Memory Report
                |        run: |
                |          echo "Memory Limit: ${_state.value.memoryLimitMb.toInt()} MB"
                |          echo "Current Usage: ${_state.value.currentMemoryUsageMb.toInt()} MB"
                |          echo "Usage Percent: ${_state.value.memoryUsagePercent.toInt()}%"
                |          echo "Risk Level: ${_state.value.riskLevel}"
            """.trimMargin()

            _state.value = _state.value.copy(ciConfigYaml = yaml)
            _effect.send(MemoryLimitEffect.ShowSnackbar("CI 配置已生成"))
        }
    }

    // ================================================================
    // 复制脚本 / Copy script
    // ================================================================
    private fun copyScript(script: String) {
        viewModelScope.launch {
            _effect.send(MemoryLimitEffect.CopyToClipboard(script))
            _effect.send(MemoryLimitEffect.ShowSnackbar("已复制到剪贴板"))
        }
    }

    // ================================================================
    // 查看堆转储 / View heap dump
    // ================================================================
    private fun viewHeapDump(triggerId: Long) {
        viewModelScope.launch {
            _effect.send(MemoryLimitEffect.ShowSnackbar("堆转储 #${triggerId} 已加载"))
        }
    }

    // ================================================================
    // 分享报告 / Share report
    // ================================================================
    private fun shareReport(content: String) {
        viewModelScope.launch {
            _effect.send(MemoryLimitEffect.ShareReport(content))
        }
    }

    // ================================================================
    // 生成趋势数据 / Generate trend data
    // ================================================================
    private fun generateTrendData(limitMb: Int, currentMb: Int): List<Float> {
        val points = 60  // 1 minute at 1 sample/sec for demo
        val trend = mutableListOf<Float>()
        var value = currentMb.toFloat()
        repeat(points) {
            value = (value + Random.nextFloat() * limitMb * 0.02f - limitMb * 0.01f)
                .coerceIn(limitMb * 0.1f, limitMb * 0.95f)
            trend.add(value)
        }
        return trend
    }

    // ================================================================
    // 加载模拟数据 / Load mock data
    // ================================================================
    private fun loadMockData() {
        val defaultDevice = availableDevices.first()
        val limitMb = defaultDevice.memoryLimitMb
        val currentMb = (limitMb * 0.65f)  // 65% usage for demo

        val leakResults = listOf(
            LeakResult(
                leakSignature = "androidx.recyclerview:RecyclerView.mAttachments",
                leakSizeKb = 256,
                leakPath = "ViewHolder.itemView -> RecyclerView.mAttachments -> LeakCanary.RootView",
                riskScore = 87,
                detectedAt = System.currentTimeMillis() - 3600000,
                suggestedFix = "在 onViewRecycled() 中清除 ViewHolder 的引用，避免长时间持有 Attachment"
            ),
            LeakResult(
                leakSignature = "com.example.app.MainActivity.mHandler",
                leakSizeKb = 512,
                leakPath = "MainActivity.handler -> Handler.mCallback -> Activity.mInstrumentation -> Leak",
                riskScore = 93,
                detectedAt = System.currentTimeMillis() - 7200000,
                suggestedFix = "使用 static Handler + WeakReference，或在 onDestroy() 中移除所有Callbacks"
            ),
            LeakResult(
                leakSignature = "android.graphics.Bitmap.mNativeBitmap",
                leakSizeKb = 4096,
                leakPath = "ImageView.drawable -> Bitmap.mNativeBitmap -> native native摩尔",
                riskScore = 78,
                detectedAt = System.currentTimeMillis() - 1800000,
                suggestedFix = "使用 glide/BitmapFactory 替代直接创建 Bitmap，及时 recycle()"
            ),
            LeakResult(
                leakSignature = "kotlinx.coroutines.MainScope.mDispatches",
                leakSizeKb = 128,
                leakPath = "ViewModel.viewModelScope -> MainScope -> CoroutineDispatcher",
                riskScore = 45,
                detectedAt = System.currentTimeMillis() - 900000,
                suggestedFix = "在 ViewModel.onCleared() 中调用 scope.cancel()"
            ),
            LeakResult(
                leakSignature = "android.content.res.Resources.mResourcesImpl",
                leakSizeKb = 1024,
                leakPath = "ContextThemeWrapper.resources -> Resources.mResourcesImpl -> Theme",
                riskScore = 62,
                detectedAt = System.currentTimeMillis() - 14400000,
                suggestedFix = "避免在 Application 中缓存 Resources 实例，使用 weak reference"
            )
        )

        val anomalyTriggers = listOf(
            AnomalyTrigger(
                id = 1,
                triggerTime = System.currentTimeMillis() - 3600000,
                triggerReason = "MemoryLimiter: limit exceeded by 256MB",
                heapDumpSizeKb = 18432,
                heapDumpPath = "/sdcard/heap_dump_001.hprof"
            ),
            AnomalyTrigger(
                id = 2,
                triggerTime = System.currentTimeMillis() - 7200000,
                triggerReason = "MemoryLimiter: GC pressure threshold reached",
                heapDumpSizeKb = 16384,
                heapDumpPath = "/sdcard/heap_dump_002.hprof"
            )
        )

        val audioPaths = listOf(
            AudioPath(
                apiName = "AudioManager.requestAudioFocus()",
                filePath = "app/src/main/java/com/mvi/kenny/feature/media/MediaPlayer.kt",
                lineNumber = 78,
                isAffected = true,
                description = "在后台 Activity 中请求音频焦点，Android 17 会静默拒绝"
            ),
            AudioPath(
                apiName = "AudioAttributes.USAGE_GAME",
                filePath = "app/src/main/java/com/mvi/kenny/feature/game/GameService.kt",
                lineNumber = 45,
                isAffected = true,
                description = "Game 类型音频属性在后台受限，Android 17 Beta 4 引入硬化"
            ),
            AudioPath(
                apiName = "AudioManager.adjustVolume()",
                filePath = "app/src/main/java/com/mvi/kenny/feature/audio/VolumeControl.kt",
                lineNumber = 112,
                isAffected = false,
                description = "音量调整在后台不受限，使用 MEDIA 类型音频属性"
            ),
            AudioPath(
                apiName = "MediaPlayer.start()",
                filePath = "app/src/main/java/com/mvi/kenny/feature/media/MusicService.kt",
                lineNumber = 234,
                isAffected = true,
                description = "后台音乐播放若无 Foreground Service 会触发音频硬化阻断"
            )
        )

        _state.value = _state.value.copy(
            selectedDevice = defaultDevice,
            memoryLimitMb = limitMb.toFloat(),
            currentMemoryUsageMb = currentMb,
            memoryUsagePercent = (currentMb / limitMb * 100f),
            isNearLimit = false,
            riskLevel = RiskLevel.MEDIUM,
            memoryTrendData = generateTrendData(limitMb, currentMb.toInt()),
            leakCanaryResults = leakResults,
            anomalyTriggers = anomalyTriggers,
            audioAffectedPaths = audioPaths
        )
    }
}
