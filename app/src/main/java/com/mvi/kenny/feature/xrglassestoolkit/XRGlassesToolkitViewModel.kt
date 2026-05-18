package com.mvi.kenny.feature.xrglassestoolkit

/**
 * ============================================================
 * XRGlassesToolkitViewModel — Android XR AI Glasses 开发工具包 ViewModel
 * XRGlassesToolkitViewModel — Android XR AI Glasses Dev Toolkit ViewModel
 * ============================================================
 *
 * PRD-258 | Android XR AI Glasses 开发工具包
 * Ref: memory/agency/designs/PRD-258-Android-XR-AI-Glasses开发工具包.md
 *
 * MVI Architecture:
 * - Receives XRGlassesToolkitIntent from UI layer
 * - Executes business logic, updates XRGlassesToolkitState
 * - Emits XRGlassesToolkitEffect for one-time side effects
 *
 * @see XRGlassesToolkitContract for State/Intent/Effect definitions
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * XR Glasses Toolkit ViewModel
 * Android XR AI Glasses 开发工具包 ViewModel
 *
 * @see XRGlassesToolkitState
 * @see XRGlassesToolkitIntent
 * @see XRGlassesToolkitEffect
 */
class XRGlassesToolkitViewModel : ViewModel() {

    // ============================================================
    // State / 页面状态
    // ============================================================

    private val _state = MutableStateFlow(XRGlassesToolkitState.Initial)
    val state: StateFlow<XRGlassesToolkitState> = _state.asStateFlow()

    // ============================================================
    // Effect / 副作用 Channel
    // ============================================================

    private val _effect = Channel<XRGlassesToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Intent Handler / 意图处理
    // ============================================================

    /**
     * Process user intent
     * 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: XRGlassesToolkitIntent) {
        viewModelScope.launch {
            when (intent) {
                is XRGlassesToolkitIntent.SelectTab -> handleSelectTab(intent.index)
                is XRGlassesToolkitIntent.SelectDevice -> handleSelectDevice(intent.type)
                is XRGlassesToolkitIntent.ToggleStep -> handleToggleStep(intent.index)
                is XRGlassesToolkitIntent.SelectApi -> handleSelectApi(intent.api)
                is XRGlassesToolkitIntent.CopyApiCode -> handleCopyApiCode(intent.apiId)
                is XRGlassesToolkitIntent.DownloadApiCode -> handleDownloadApiCode(intent.apiId)
                is XRGlassesToolkitIntent.DismissBottomSheet -> handleDismissBottomSheet()
                is XRGlassesToolkitIntent.OpenFullScreenPreview -> handleOpenFullScreenPreview(intent.component)
                is XRGlassesToolkitIntent.DismissFullScreenPreview -> handleDismissFullScreenPreview()
                is XRGlassesToolkitIntent.SwitchGlimmerTab -> handleSwitchGlimmerTab(intent.tab)
                is XRGlassesToolkitIntent.UpdateSimulatorInput -> handleUpdateSimulatorInput(intent.input)
                is XRGlassesToolkitIntent.LaunchEmulator -> handleLaunchEmulator(intent.avdName)
                is XRGlassesToolkitIntent.NextStep -> handleNextStep()
                is XRGlassesToolkitIntent.ToggleChecklistItem -> handleToggleChecklistItem(intent.itemId)
                is XRGlassesToolkitIntent.FilterChecklist -> handleFilterChecklist(intent.filter)
                is XRGlassesToolkitIntent.GeneratePrivacyReport -> handleGeneratePrivacyReport()
                is XRGlassesToolkitIntent.LoadData -> handleLoadData()
            }
        }
    }

    // ============================================================
    // Tab 0: Getting Started Handlers
    // ============================================================

    /**
     * Handle tab selection
     * 处理 Tab 选择
     *
     * @param index Selected tab index / 选中的 Tab 索引
     */
    private fun handleSelectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    /**
     * Handle device type selection
     * 处理设备类型选择
     *
     * @param type Selected device type / 选中的设备类型
     */
    private fun handleSelectDevice(type: DeviceType) {
        _state.value = _state.value.copy(deviceType = type)
    }

    /**
     * Handle step card toggle (Tab 0)
     * 处理步骤卡片切换
     *
     * @param index Step card index / 步骤卡片索引
     */
    private fun handleToggleStep(index: Int) {
        val current = _state.value.expandedStepIndex
        _state.value = _state.value.copy(
            expandedStepIndex = if (current == index) null else index
        )
    }

    // ============================================================
    // Tab 1: Projected API Handlers
    // ============================================================

    /**
     * Handle API selection, open bottom sheet (Tab 1)
     * 处理 API 选择，打开底部 Sheet
     *
     * @param api Selected API / 选中的 API
     */
    private fun handleSelectApi(api: ProjectedApi) {
        _state.value = _state.value.copy(
            selectedApi = api,
            isBottomSheetVisible = true
        )
    }

    /**
     * Handle API code copy (Tab 1)
     * 处理 API 代码复制
     *
     * @param apiId API identifier / API 标识符
     */
    private fun handleCopyApiCode(apiId: Int) {
        val api = _state.value.apiList.find { it.id == apiId } ?: return
        viewModelScope.launch {
            _effect.send(XRGlassesToolkitEffect.CopyToClipboard(api.fullCode))
            _effect.send(XRGlassesToolkitEffect.ShowSnackbar("已复制: ${api.name}"))
        }
    }

    /**
     * Handle API code download (Tab 1)
     * 处理 API 代码下载
     *
     * @param apiId API identifier / API 标识符
     */
    private fun handleDownloadApiCode(apiId: Int) {
        val api = _state.value.apiList.find { it.id == apiId } ?: return
        val fileName = "ProjectedApi_${api.name}.kt"
        viewModelScope.launch {
            _effect.send(XRGlassesToolkitEffect.DownloadFile(fileName, api.fullCode))
            _effect.send(XRGlassesToolkitEffect.ShowSnackbar("下载: $fileName"))
        }
    }

    /**
     * Handle bottom sheet dismiss (Tab 1)
     * 处理底部 Sheet 关闭
     */
    private fun handleDismissBottomSheet() {
        _state.value = _state.value.copy(isBottomSheetVisible = false)
    }

    // ============================================================
    // Tab 2: Compose Glimmer Handlers
    // ============================================================

    /**
     * Handle full-screen preview open (Tab 2)
     * 处理全屏预览打开
     *
     * @param component Component to preview / 要预览的组件
     */
    private fun handleOpenFullScreenPreview(component: GlimmerComponent) {
        _state.value = _state.value.copy(fullScreenPreviewComponent = component)
    }

    /**
     * Handle full-screen preview dismiss (Tab 2)
     * 处理全屏预览关闭
     */
    private fun handleDismissFullScreenPreview() {
        _state.value = _state.value.copy(fullScreenPreviewComponent = null)
    }

    /**
     * Handle Glimmer sub-tab switch (Tab 2)
     * 处理 Glimmer 子 Tab 切换
     *
     * @param tab Target Glimmer sub-tab / 目标 Glimmer 子 Tab
     */
    private fun handleSwitchGlimmerTab(tab: GlimmerTab) {
        _state.value = _state.value.copy(glimmerTab = tab)
    }

    // ============================================================
    // Tab 3: Emulator Workflow Handlers
    // ============================================================

    /**
     * Handle simulator touchpad input update (Tab 3)
     * 处理模拟器触摸板输入更新
     *
     * @param input JSON touchpad event sequence / JSON 触摸板事件序列
     */
    private fun handleUpdateSimulatorInput(input: String) {
        _state.value = _state.value.copy(simulatorInput = input)
    }

    /**
     * Handle emulator launch (Tab 3)
     * 处理模拟器启动
     *
     * @param avdName AVD device name / AVD 设备名称
     */
    private fun handleLaunchEmulator(avdName: String) {
        _state.value = _state.value.copy(emulatorStatus = EmulatorStatus.STARTING)
        viewModelScope.launch {
            _effect.send(
                XRGlassesToolkitEffect.ExecuteCommand("adb emu avd name=$avdName")
            )
            // Simulate status update after delay / 延迟后模拟状态更新
            kotlinx.coroutines.delay(3000)
            _state.value = _state.value.copy(emulatorStatus = EmulatorStatus.RUNNING)
        }
    }

    /**
     * Handle next step advance (Tab 3)
     * 处理下一步推进
     */
    private fun handleNextStep() {
        val current = _state.value.currentStep
        if (current < 4) { // 5 steps total (0-4) / 共 5 步 (0-4)
            _state.value = _state.value.copy(currentStep = current + 1)
        }
    }

    // ============================================================
    // Tab 4: Privacy Compliance Handlers
    // ============================================================

    /**
     * Handle checklist item toggle (Tab 4)
     * 处理检查清单条目切换
     *
     * @param itemId Item unique identifier / 条目唯一标识
     */
    private fun handleToggleChecklistItem(itemId: Int) {
        val updated = _state.value.checklist.map { item ->
            if (item.id == itemId) {
                // Cycle: null -> true -> false -> null
                item.copy(isCompliant = when (item.isCompliant) {
                    null -> true
                    true -> false
                    false -> null
                })
            } else item
        }
        _state.value = _state.value.copy(checklist = updated)
    }

    /**
     * Handle checklist filter change (Tab 4)
     * 处理检查清单筛选变化
     *
     * @param filter Target filter / 目标过滤器
     */
    private fun handleFilterChecklist(filter: ChecklistFilter) {
        _state.value = _state.value.copy(checklistFilter = filter)
    }

    /**
     * Handle privacy report generation (Tab 4)
     * 处理隐私报告生成
     */
    private fun handleGeneratePrivacyReport() {
        viewModelScope.launch {
            val compliant = _state.value.checklist.count { it.isCompliant == true }
            val nonCompliant = _state.value.checklist.count { it.isCompliant == false }
            val notChecked = _state.value.checklist.count { it.isCompliant == null }
            val total = _state.value.checklist.size
            val report = buildString {
                appendLine("# Android XR AI Glasses 隐私合规报告")
                appendLine("## 概览")
                appendLine("- 总检查项: $total")
                appendLine("- 合规: $compliant")
                appendLine("- 不合规: $nonCompliant")
                appendLine("- 未检测: $notChecked")
                appendLine()
                appendLine("## 合规清单")
                _state.value.checklist.forEach { item ->
                    val status = when (item.isCompliant) {
                        true -> "✅ 合规"
                        false -> "❌ 不合规"
                        null -> "⏳ 未检测"
                    }
                    appendLine("- ${item.title}: $status")
                }
            }
            _effect.send(XRGlassesToolkitEffect.ShareFile(report))
            _effect.send(XRGlassesToolkitEffect.ShowSnackbar("隐私合规报告已生成"))
        }
    }

    // ============================================================
    // Data Loading / 数据加载
    // ============================================================

    /**
     * Load initial data for all tabs
     * 加载所有 Tab 的初始数据
     */
    private fun handleLoadData() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            // Load Projected APIs (Tab 1)
            val apis = getSampleApis()
            // Load Glimmer Components (Tab 2)
            val components = getSampleComponents()
            // Load Privacy Checklist (Tab 4)
            val checklist = getSampleChecklist()

            _state.value = _state.value.copy(
                apiList = apis,
                componentGrid = components,
                checklist = checklist,
                isLoading = false
            )
        }
    }

    // ============================================================
    // Sample Data / 示例数据
    // ============================================================

    /**
     * Get sample Projected API list (Tab 1)
     * 获取示例 Projected API 列表
     */
    private fun getSampleApis(): List<ProjectedApi> = listOf(
        ProjectedApi(
            id = 1,
            name = "ProjectedSession",
            description = "建立手机与 AI Glasses 的 Projected 会话，管理设备间连接生命周期。",
            codePreview = "val session = ProjectedSession(context)",
            fullCode = """// ProjectedSession.kt
// ProjectedSession — 建立手机与 AI Glasses 的 Projected 会话
// ProjectedSession — Establishes a Projected session between phone and AI Glasses

import android.content.Context
import androidx.projected.library.core.ProjectedSession

/**
 * Create a new Projected session with AI Glasses
 * 创建与 AI Glasses 的 Projected 会话
 *
 * @param context Application context / 应用上下文
 * @param config Session configuration / 会话配置
 * @return ProjectedSession instance / ProjectedSession 实例
 */
fun createProjectedSession(
    context: Context,
    config: ProjectedSession.Config = ProjectedSession.Config.DEFAULT
): ProjectedSession {
    return ProjectedSession(context, config).apply {
        // Register callback for connection state changes
        // 注册连接状态变化回调
        setCallback(object : ProjectedSession.Callback {
            override fun onConnected(deviceId: String) {
                // AI Glasses connected / AI 眼镜已连接
            }

            override fun onDisconnected(deviceId: String) {
                // AI Glasses disconnected / AI 眼镜已断开
            }

            override fun onError(error: Throwable) {
                // Connection error / 连接错误
            }
        })
    }
}"""
        ),
        ProjectedApi(
            id = 2,
            name = "SensorBridge",
            description = "桥接 AI Glasses 传感器数据（加速度计/陀螺仪/摄像头）到手机端处理。",
            codePreview = "val bridge = SensorBridge(session)",
            fullCode = """// SensorBridge.kt
// SensorBridge — AI Glasses 传感器数据桥接
// SensorBridge — AI Glasses sensor data bridging to phone

import androidx.projected.library.core.SensorData
import kotlinx.coroutines.flow.Flow

/**
 * Bridge sensor data stream from AI Glasses to phone
 * 桥接 AI Glasses 传感器数据流到手机端
 *
 * @param session Active ProjectedSession / 活跃的 ProjectedSession
 * @return Flow of sensor data / 传感器数据流
 */
class SensorBridge(private val session: ProjectedSession) {

    /**
     * Accelerometer data stream from AI Glasses
     * AI Glasses 加速度计数据流
     */
    fun getAccelerometerStream(): Flow<SensorData> = session.getSensorStream(
        SensorData.Type.ACCELEROMETER
    )

    /**
     * Gyroscope data stream from AI Glasses
     * AI Glasses 陀螺仪数据流
     */
    fun getGyroscopeStream(): Flow<SensorData> = session.getSensorStream(
        SensorData.Type.GYROSCOPE
    )

    /**
     * Camera frame stream from AI Glasses
     * AI Glasses 摄像头帧数据流
     */
    fun getCameraStream(): Flow<ByteArray> = session.getCameraStream(
        CameraConfig(resolution = Resolution.HD)
    )
}"""
        ),
        ProjectedApi(
            id = 3,
            name = "DisplayController",
            description = "控制 AI Glasses 显示内容，支持文本/图像/AR 叠加层渲染。",
            codePreview = "val display = DisplayController(session)",
            fullCode = """// DisplayController.kt
// DisplayController — AI Glasses 显示内容控制器
// DisplayController — Controls AI Glasses display content

import androidx.compose.ui.graphics.Color

/**
 * Controller for AI Glasses display output
 * AI Glasses 显示输出控制器
 *
 * @param session Active ProjectedSession / 活跃的 ProjectedSession
 */
class DisplayController(private val session: ProjectedSession) {

    /**
     * Show text content on AI Glasses display
     * 在 AI Glasses 显示上展示文本内容
     *
     * @param text Text to display / 要显示的文本
     * @param textColor Text color / 文本颜色
     */
    fun showText(text: String, textColor: Color = Color.White) {
        session.render(
            DisplayContent.Text(
                content = text,
                color = textColor,
                fontSize = FontSize.MEDIUM
            )
        )
    }

    /**
     * Show AR overlay on real-world view
     * 在现实视图上显示 AR 叠加层
     *
     * @param overlay AR overlay content / AR 叠加内容
     */
    fun showAROverlay(overlay: AROverlay) {
        session.render(DisplayContent.AROverlay(overlay))
    }

    /**
     * Clear display content
     * 清除显示内容
     */
    fun clear() {
        session.render(DisplayContent.Empty)
    }
}"""
        ),
        ProjectedApi(
            id = 4,
            name = "VoiceInputManager",
            description = "管理 AI Glasses 语音输入，将语音转换为文字并触发对应操作。",
            codePreview = "val voice = VoiceInputManager(session)",
            fullCode = """// VoiceInputManager.kt
// VoiceInputManager — AI Glasses 语音输入管理器
// VoiceInputManager — Manages AI Glasses voice input

import kotlinx.coroutines.flow.Flow

/**
 * Manages voice input from AI Glasses microphone
 * 管理 AI Glasses 麦克风的语音输入
 *
 * @param session Active ProjectedSession / 活跃的 ProjectedSession
 */
class VoiceInputManager(private val session: ProjectedSession) {

    /**
     * Live transcription flow from AI Glasses microphone
     * AI Glasses 麦克风的实时转录流
     */
    fun getTranscriptionFlow(): Flow<String> = session.getVoiceStream()

    /**
     * Start voice recognition with trigger phrase
     * 启动带有触发词的语音识别
     *
     * @param triggerPhrases List of trigger phrases / 触发词列表
     * @param onRecognized Callback when voice is recognized / 识别到语音时的回调
     */
    fun startListening(
        triggerPhrases: List<String> = listOf("Hey Glasses"),
        onRecognized: (String) -> Unit
    ) {
        session.startVoiceRecognition(
            VoiceConfig(triggerPhrases = triggerPhrases),
            onRecognized = onRecognized
        )
    }

    /**
     * Stop voice recognition
     * 停止语音识别
     */
    fun stopListening() {
        session.stopVoiceRecognition()
    }
}"""
        )
    )

    /**
     * Get sample Glimmer component list (Tab 2)
     * 获取示例 Glimmer 组件列表
     */
    private fun getSampleComponents(): List<GlimmerComponent> = listOf(
        GlimmerComponent(1, "GlimmerText", "AI Glasses 专用文本组件，支持高对比度/大字号", "基础组件"),
        GlimmerComponent(2, "GlimmerCard", "轻量级卡片组件，适用于信息展示", "容器组件"),
        GlimmerComponent(3, "GlimmerButton", "大触控区域按钮组件，适合眼镜端操作", "交互组件"),
        GlimmerComponent(4, "GlimmerIcon", "高对比度图标组件，支持动态颜色", "基础组件"),
        GlimmerComponent(5, "GlimmerList", "垂直列表组件，支持懒加载", "列表组件"),
        GlimmerComponent(6, "GlimmerImage", "图像展示组件，支持渐进加载", "媒体组件"),
        GlimmerComponent(7, "GlimmerBadge", "通知徽章组件，适用于状态提醒", "装饰组件"),
        GlimmerComponent(8, "GlimmerChip", "标签选择组件，支持单选/多选", "选择组件")
    )

    /**
     * Get sample privacy checklist (Tab 4)
     * 获取示例隐私检查清单
     */
    private fun getSampleChecklist(): List<PrivacyCheckItem> = listOf(
        PrivacyCheckItem(
            id = 1,
            title = "摄像头使用提示",
            description = "App 使用 AI Glasses 摄像头前必须显示明确的使用提示，获取用户同意。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 2,
            title = "麦克风使用提示",
            description = "语音输入功能启动前必须显示麦克风使用提示。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 3,
            title = "数据加密传输",
            description = "手机与 AI Glasses 之间的所有数据传输必须使用 TLS 加密。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 4,
            title = "本地处理优先",
            description = "敏感数据（如摄像头帧）应优先在眼镜端处理，必要时才传回手机。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 5,
            title = "隐私政策披露",
            description = "App 必须在其隐私政策中明确披露 AI Glasses 数据收集行为。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 6,
            title = "用户删除权",
            description = "用户必须能够删除其 AI Glasses 使用数据，且删除操作不可逆。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 7,
            title = "最小化数据收集",
            description = "仅收集实现功能所必需的最少数据，不过度收集。",
            isCompliant = null
        ),
        PrivacyCheckItem(
            id = 8,
            title = "儿童保护",
            description = "App 不得在未获得父母同意的情况下向 AI Glasses 收集儿童数据。",
            isCompliant = null
        )
    )
}
