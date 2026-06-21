package com.mvi.kenny.feature.xrsdkdevkit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * XrDevKitViewModel — Android XR SDK DP4 开发工具包状态管理
 * ============================================================
 * MVI: State + Intent + Effect pattern.
 *
 * @see XrDevKitState Page state definition
 * @see XrDevKitIntent User intentions
 * @see XrDevKitEffect Side effects
 */
class XrDevKitViewModel : ViewModel() {

    private val _state = MutableStateFlow(XrDevKitState.Initial)
    val state: StateFlow<XrDevKitState> = _state.asStateFlow()
    val currentState: XrDevKitState get() = _state.value

    private val _effect = Channel<XrDevKitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadToolCards()
    }

    fun sendIntent(intent: XrDevKitIntent) {
        when (intent) {
            is XrDevKitIntent.SelectTab -> selectTab(intent.tab)
            is XrDevKitIntent.SetSearchActive -> setSearchActive(intent.active)
            is XrDevKitIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is XrDevKitIntent.Search -> search(intent.query)
            is XrDevKitIntent.ToggleFavorite -> toggleFavorite(intent.cardId)
            is XrDevKitIntent.ToggleCardExpand -> toggleCardExpand(intent.cardId)
            is XrDevKitIntent.ClearSearch -> clearSearch()
            is XrDevKitIntent.ScrollToTop -> scrollToTop()
            is XrDevKitIntent.DismissCopiedToast -> dismissCopiedToast(intent.codeId)
        }
    }

    private fun selectTab(tab: XrTab) {
        _state.value = _state.value.copy(
            currentTab = tab,
            expandedCards = emptySet()
        )
    }

    private fun setSearchActive(active: Boolean) {
        _state.value = _state.value.copy(
            isSearchActive = active,
            searchQuery = if (!active) "" else _state.value.searchQuery,
            searchResults = if (!active) emptyList() else _state.value.searchResults
        )
    }

    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        viewModelScope.launch {
            delay(300)
            if (_state.value.searchQuery == query && query.isNotBlank()) {
                search(query)
            }
        }
    }

    private fun search(query: String) {
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = emptyList())
            return
        }
        val q = query.lowercase()
        val results = _state.value.toolCards.filter { card ->
            card.title.lowercase().contains(q) ||
            card.titleZh.contains(query) ||
            card.description.lowercase().contains(q) ||
            card.descriptionZh.contains(query) ||
            card.contentItems.any { item ->
                item.title.lowercase().contains(q) ||
                item.titleZh.contains(query)
            }
        }
        _state.value = _state.value.copy(searchResults = results)
    }

    private fun clearSearch() {
        _state.value = _state.value.copy(
            isSearchActive = false,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    private fun toggleFavorite(cardId: String) {
        val favorites = _state.value.favorites.toMutableSet()
        if (favorites.contains(cardId)) favorites.remove(cardId) else favorites.add(cardId)
        _state.value = _state.value.copy(favorites = favorites)
    }

    private fun toggleCardExpand(cardId: String) {
        val expanded = _state.value.expandedCards.toMutableSet()
        if (expanded.contains(cardId)) expanded.remove(cardId) else expanded.add(cardId)
        _state.value = _state.value.copy(expandedCards = expanded)
    }

    private fun scrollToTop() {
        viewModelScope.launch { _effect.send(XrDevKitEffect.ScrollToTop) }
    }

    private fun dismissCopiedToast(codeId: String?) {
        _state.value = _state.value.copy(copiedCodeId = null)
    }

    fun copyCode(code: String, codeId: String) {
        viewModelScope.launch {
            _effect.send(XrDevKitEffect.CopyToClipboard(code, codeId))
            _effect.send(XrDevKitEffect.ShowToast("Copied / 已复制"))
        }
    }

    private fun loadToolCards() {
        val cards = buildList {
            // 1. Compose XR
            add(ToolCard(
                id = "compose-xr-guide",
                title = "Jetpack Compose for XR Complete Guide",
                titleZh = "Jetpack Compose for XR 开发指南",
                description = "Space UI layouts, 3D panels, spatial anchoring, and ComposeXR API.",
                descriptionZh = "空间 UI 布局、3D 面板、环境锚定、ComposeXR API 参考。",
                category = XrTab.COMPOSE_XR,
                difficulty = Difficulty.INTERMEDIATE,
                contentItems = listOf(
                    ToolContentItem(
                        id = "compose-xr-setup",
                        title = "Project Setup",
                        titleZh = "项目配置",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "build.gradle.kts",
                                labelZh = "Gradle 依赖",
                                code = """// build.gradle.kts (Module: app)
dependencies {
    // Compose XR (Developer Preview 4)
    implementation("androidx.compose.xr:xr-runtime:1.4.0-dev4")
    implementation("androidx.compose.xr:compose-xr:1.4.0-dev4")
    implementation("androidx.compose.xr:spatial:1.4.0-dev4")
    implementation("androidx.scene:scenecore:1.0.0-alpha03")
    implementation("androidx.ar:arcore-xr:1.0.0-alpha02")
    implementation("androidx.window:window:1.3.0-alpha03")
}""",
                                filename = "build.gradle.kts"
                            )
                        ),
                        steps = listOf(
                            "1. Add Compose XR dependencies to build.gradle.kts",
                            "2. Create AndroidManifest.xml with XR device support",
                            "3. Set minSdk to 24, targetSdk to 35+",
                            "4. Enable XR features in gradle.properties"
                        ),
                        stepsZh = listOf(
                            "1. 在 build.gradle.kts 中添加 Compose XR 依赖",
                            "2. 在 AndroidManifest.xml 中声明 XR 设备支持",
                            "3. 设置 minSdk 为 24，targetSdk 为 35+",
                            "4. 在 gradle.properties 中启用 XR 功能"
                        ),
                        warnings = listOf(
                            "Compose XR is in Developer Preview. API may change before GA.",
                            "XR features require physical XR device or emulator to test."
                        ),
                        warningsZh = listOf(
                            "Compose XR 处于 Developer Preview，GA 前 API 可能变化。",
                            "XR 功能需要物理 XR 眼镜或模拟器测试。"
                        )
                    ),
                    ToolContentItem(
                        id = "compose-xr-spatial",
                        title = "Spatial Layout (3D Panel)",
                        titleZh = "空间布局（3D 面板）",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "SpatialPanel.kt",
                                labelZh = "空间面板示例",
                                code = """@Composable
fun SpatialPanelDemo() {
    SpatialPanel(
        modifier = Modifier
            .spatialPosition(0f, 0f, -1f) // x, y, z in meters
            .spatialSize(0.5f, 0.3f),   // width, height in meters
        surfaceStyle = SurfaceStyle.CARDBOARD,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hello XR!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("3D Spatial Panel", style = MaterialTheme.typography.bodyLarge)
        }
    }
}""",
                                filename = "SpatialPanelDemo.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Import androidx.compose.xr.spatial.*",
                            "2. Use SpatialPanel with spatialPosition() modifier",
                            "3. Position panels in 3D space (meters)",
                            "4. Set surfaceStyle for XR device types"
                        ),
                        stepsZh = listOf(
                            "1. 导入 androidx.compose.xr.spatial.*",
                            "2. 使用 SpatialPanel 配合 spatialPosition() 修饰符",
                            "3. 使用 x、y、z 坐标在 3D 空间中定位",
                            "4. 为不同 XR 设备类型设置 surfaceStyle"
                        ),
                        warnings = listOf(
                            "Coordinate system: Y-up, Z toward user. Units in meters."
                        ),
                        warningsZh = listOf(
                            "坐标系：Y轴向上，Z轴朝向用户。单位为米。"
                        )
                    )
                )
            ))

            // 2. Glimmer
            add(ToolCard(
                id = "glimmer-guide",
                title = "Jetpack Compose Glimmer Development Guide",
                titleZh = "Glimmer 眼镜 UI 框架指南",
                description = "Glimmer API for transparent displays, glanceable UI, high-contrast design.",
                descriptionZh = "Glimmer API 用于透明显示器、瞥视 UI、高对比度设计。",
                category = XrTab.GLIMMER,
                difficulty = Difficulty.ADVANCED,
                contentItems = listOf(
                    ToolContentItem(
                        id = "glimmer-design",
                        title = "Glimmer Design System",
                        titleZh = "Glimmer 设计系统",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "GlimmerTheme.kt",
                                labelZh = "Glimmer 主题",
                                code = """@Composable
fun GlimmerTheme(
    deviceType: GlimmerDeviceType = GlimmerDeviceType.DISPLAY_GLASSES,
    content: @Composable () -> Unit
) {
    val colors = when (deviceType) {
        GlimmerDeviceType.AUDIO_GLASSES -> AudioGlassesColors()
        GlimmerDeviceType.DISPLAY_GLASSES -> DisplayGlassesColors()
    }
    val colorScheme = darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        secondary = Color.Cyan,
        background = Color.Transparent, // Transparent for AR overlay
        surface = Color.Black.copy(alpha = 0.6f),
        onSurface = Color.White
    )
    CompositionLocalProvider(
        LocalGlimmerDeviceType provides deviceType,
        LocalGlimmerContrast provides HighContrast
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = GlimmerTypography, content = content)
    }
}""",
                                filename = "GlimmerTheme.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Use GlimmerTheme with AUDIO_GLASSES or DISPLAY_GLASSES",
                            "2. High contrast: White on Transparent/Black",
                            "3. Large typography for glanceability",
                            "4. Audio glasses: UI must be audio-only (no display)"
                        ),
                        stepsZh = listOf(
                            "1. 使用 GlimmerTheme 设置 AUDIO_GLASSES 或 DISPLAY_GLASSES",
                            "2. 高对比度：白色在透明/黑色上",
                            "3. 使用大字体以便于瞥视",
                            "4. 音频眼镜：UI 必须是纯音频的"
                        ),
                        warnings = listOf(
                            "Audio glasses have NO display. UI must be audio-only.",
                            "Display glasses: 1-second glance max. Keep text minimal."
                        ),
                        warningsZh = listOf(
                            "音频眼镜没有显示屏。UI 必须是纯音频的。",
                            "显示眼镜：最多 1 秒瞥视。保持文字最少。"
                        )
                    )
                )
            ))

            // 3. SceneCore
            add(ToolCard(
                id = "scenecore-guide",
                title = "SceneCore 3D Development Guide",
                titleZh = "SceneCore 3D 开发指南",
                description = "glTF/GLB model loading, spatial anchoring, 3D entity management.",
                descriptionZh = "glTF/GLB 模型加载、空间锚定、3D 实体管理。",
                category = XrTab.SCENE_CORE,
                difficulty = Difficulty.ADVANCED,
                contentItems = listOf(
                    ToolContentItem(
                        id = "scenecore-loading",
                        title = "3D Model Loading",
                        titleZh = "3D 模型加载",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "SceneCoreLoader.kt",
                                labelZh = "SceneCore 模型加载",
                                code = """suspend fun loadModel(sceneView: SceneView, modelUri: String): SceneEntity {
    val modelLoader = ModelLoader(sceneView)
    val assetKey = sceneView.context.createAssetKey(modelUri)
    val gltfInstance = modelLoader.loadInstancedModel(assetKey)
    val entity = SpatialEntity(
        gltfInstance = gltfInstance,
        position = Pose(x = 0f, y = 0f, z = -2f),
        rotation = Quaternion.identity,
        anchor = null
    )
    sceneView.scene.addEntity(entity)
    return entity
}""",
                                filename = "SceneCoreLoader.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Add SceneCore dependency",
                            "2. Create SceneView composable",
                            "3. Use ModelLoader to load glTF/GLB models",
                            "4. Attach entities to scene with spatial Pose"
                        ),
                        stepsZh = listOf(
                            "1. 添加 SceneCore 依赖",
                            "2. 在布局中创建 SceneView 可组合函数",
                            "3. 使用 ModelLoader 加载 glTF/GLB 模型",
                            "4. 使用空间 Pose 将实体附加到场景"
                        ),
                        warnings = listOf(
                            "Only glTF 2.0 and GLB formats supported.",
                            "Large models (>10MB) may cause memory pressure."
                        ),
                        warningsZh = listOf(
                            "仅支持 glTF 2.0 和 GLB 格式。",
                            "大模型（>10MB）可能导致内存压力。"
                        )
                    )
                )
            ))

            // 4. ARCore XR
            add(ToolCard(
                id = "arcore-xr-guide",
                title = "ARCore for Jetpack XR Integration Guide",
                titleZh = "ARCore for Jetpack XR 集成指南",
                description = "AR sensing, gesture detection, spatial mapping, differences from ARCore.",
                descriptionZh = "AR 感知、手势检测、空间映射、与标准 ARCore 的差异。",
                category = XrTab.AR_CORE,
                difficulty = Difficulty.ADVANCED,
                contentItems = listOf(
                    ToolContentItem(
                        id = "arcore-session",
                        title = "ARCore XR Session",
                        titleZh = "ARCore XR 会话",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "ARCoreXR.kt",
                                labelZh = "ARCore XR 示例",
                                code = """@Composable
fun ARCoreXRSession() {
    val sessionState = remember { mutableStateOf<ArSessionState>(ArSessionState.NotStarted) }
    AndroidXRArSurface(
        modifier = Modifier.fillMaxSize(),
        onSessionCreated = { session ->
            session.configure {
                enablePlaneDetection = true
                enableLightEstimation = true
                enableDepth = true
            }
            sessionState.value = ArSessionState.Running
        },
        onSessionError = { error ->
            sessionState.value = ArSessionState.Error(error.message ?: "Unknown")
        }
    ) { frame ->
        val planes = frame.getUpdatedTrackables(Plane::class.java)
        planes.forEach { plane -> drawPlane(plane) }
    }
}""",
                                filename = "ARCoreXRSession.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Add ARCore for Jetpack XR dependency",
                            "2. Request CAMERA permission",
                            "3. Create AndroidXRArSurface composable",
                            "4. Handle session lifecycle"
                        ),
                        stepsZh = listOf(
                            "1. 添加 ARCore for Jetpack XR 依赖",
                            "2. 请求 CAMERA 权限",
                            "3. 创建 AndroidXRArSurface 可组合函数",
                            "4. 处理会话生命周期"
                        ),
                        warnings = listOf(
                            "ARCore XR is DIFFERENT from standard ARCore.",
                            "Requires ARCore compatible device with XR support."
                        ),
                        warningsZh = listOf(
                            "ARCore XR 与标准 ARCore 不同。",
                            "需要支持 ARCore 且有 XR 功能的设备。"
                        )
                    )
                )
            ))

            // 5. Simulator
            add(ToolCard(
                id = "simulator-guide",
                title = "Android Studio XR Simulator Complete Guide",
                titleZh = "Android Studio XR 模拟器完整指南",
                description = "Four XR device emulators: Headset, Wired Glasses, Audio Glasses, Display Glasses.",
                descriptionZh = "四种 XR 设备模拟器：头显、有线眼镜、音频眼镜、显示眼镜。",
                category = XrTab.SIMULATOR,
                difficulty = Difficulty.BEGINNER,
                contentItems = listOf(
                    ToolContentItem(
                        id = "simulator-setup",
                        title = "XR Simulator Setup",
                        titleZh = "XR 模拟器设置",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "XRDeviceTypes.kt",
                                labelZh = "XR 设备类型",
                                code = """// Four XR Device Types / 四种 XR 设备类型
enum class XRDeviceType {
    XR_HEADSET,       // Full immersive mixed reality
    WIRED_GLASSES,    // Phone-powered tethered glasses
    AUDIO_GLASSES,    // No display, audio-only
    DISPLAY_GLASSES   // Transparent display overlay
}

// Emulator launch commands / 模拟器启动命令
// XR Headset: emulator -avd "XR_Headset_API_35"
// Wired Glasses: emulator -avd "XR_WiredGlasses_API_35"
// Audio Glasses: emulator -avd "XR_AudioGlasses_API_35"
// Display Glasses: emulator -avd "XR_DisplayGlasses_API_35" """,
                                filename = "XRDeviceTypes.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Open Android Studio Device Manager",
                            "2. Select XR Headset/Wired/Audio/Display Glasses",
                            "3. Download XR system image (API 35+)",
                            "4. Launch and test your XR app"
                        ),
                        stepsZh = listOf(
                            "1. 打开 Android Studio 设备管理器",
                            "2. 选择 XR 头显/有线眼镜/音频眼镜/显示眼镜",
                            "3. 下载 XR 系统镜像（API 35+）",
                            "4. 启动模拟器并测试"
                        )
                    )
                )
            ))

            // 6. Audio Glasses
            add(ToolCard(
                id = "audio-glasses-guide",
                title = "Android XR Audio Glasses Development Guide",
                titleZh = "音频眼镜开发实战框架",
                description = "Voice dialogue design, audio response, notification without display.",
                descriptionZh = "语音对话设计、音频响应、无屏幕通知。",
                category = XrTab.AUDIO_GLASSES,
                difficulty = Difficulty.INTERMEDIATE,
                contentItems = listOf(
                    ToolContentItem(
                        id = "audio-voice",
                        title = "Voice Interaction Design",
                        titleZh = "语音交互设计",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "AudioGlassesAssistant.kt",
                                labelZh = "音频眼镜助手",
                                code = """@Composable
fun AudioGlassesAssistant() {
    val voiceState = remember { mutableStateOf<VoiceState>(VoiceState.Idle) }
    SpeechRecognizer.createIntent { intent ->
        intent.putExtra(EXTRA_WAKEWORD, "Hey Glasses")
    }
    LaunchedEffect(voiceState.value) {
        when (voiceState.value) {
            is VoiceState.Listening -> playAudioFeedback(R.raw.listening_beep)
            is VoiceState.Processing -> playAudioFeedback(R.raw.processing_chime)
            is VoiceState.Responding -> {
                val response = (voiceState.value as VoiceState.Responding).text
                textToSpeech.speak(response, QUEUE_FLUSH, null, "response_1")
            }
            else -> {}
        }
    }
}""",
                                filename = "AudioGlassesAssistant.kt"
                            )
                        ),
                        steps = listOf(
                            "1. No display available - design audio-first UX",
                            "2. Use TTS for all responses",
                            "3. Implement wake word detection",
                            "4. Keep responses short (< 15 seconds TTS)"
                        ),
                        stepsZh = listOf(
                            "1. 无显示屏，设计音频优先 UX",
                            "2. 使用 TTS 进行所有响应",
                            "3. 实现唤醒词检测",
                            "4. 保持响应简短（< 15 秒 TTS）"
                        ),
                        warnings = listOf(
                            "No visual feedback possible. Audio cues are critical.",
                            "Privacy: Audio glasses may be in public."
                        ),
                        warningsZh = listOf(
                            "无法提供视觉反馈。音频提示至关重要。",
                            "隐私：音频眼镜可能在公共场合使用。"
                        )
                    )
                )
            ))

            // 7. Display Glasses
            add(ToolCard(
                id = "display-glasses-guide",
                title = "Android XR Display Glasses Development Guide",
                titleZh = "显示眼镜开发实战框架",
                description = "Glimmer UI, glanceable design, AR overlay, high-contrast UI.",
                descriptionZh = "Glimmer UI、瞥视设计、AR 叠加、透明显示器高对比度 UI。",
                category = XrTab.DISPLAY_GLASSES,
                difficulty = Difficulty.INTERMEDIATE,
                contentItems = listOf(
                    ToolContentItem(
                        id = "display-glimmer",
                        title = "Glimmer UI for Display Glasses",
                        titleZh = "显示眼镜的 Glimmer UI",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "DisplayGlassesOverlay.kt",
                                labelZh = "显示眼镜 UI",
                                code = """@Composable
fun DisplayGlassesOverlay() {
    GlimmerTheme(deviceType = GlimmerDeviceType.DISPLAY_GLASSES) {
        GlanceableCard(
            modifier = Modifier
                .glimmerPosition(x = 0.2f, y = 0.1f)
                .glimmerSize(width = 0.4f, height = 0.15f),
            displayDuration = 3.seconds,
            fadeOut = 500.ms
        ) {
            Column {
                Text("导航: 200m 左转", style = GlimmerTypography.headlineMedium, maxLines = 1)
                Text("北京路", style = GlimmerTypography.bodyMedium, maxLines = 1)
            }
        }
        ArOverlay(alpha = 0.85f) {
            WorldLockedCard(position = Pose(0f, 1.5f, -3f)) {
                Text("Coffee Shop", style = GlimmerTypography.headlineLarge)
            }
        }
    }
}""",
                                filename = "DisplayGlassesOverlay.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Use GlimmerTheme with DISPLAY_GLASSES device type",
                            "2. Design for 1-second glance: minimal text, high contrast",
                            "3. Use GlanceableCard for auto-dismissing info",
                            "4. Use WorldLockedCard for AR-persisted content"
                        ),
                        stepsZh = listOf(
                            "1. 使用 DISPLAY_GLASSES 的 GlimmerTheme",
                            "2. 为 1 秒瞥视设计：最少文字、高对比度",
                            "3. 使用 GlanceableCard 自动消失信息",
                            "4. 使用 WorldLockedCard 进行 AR 持久内容"
                        ),
                        warnings = listOf(
                            "Display glasses have limited FOV.",
                            "Battery drain is significant with always-on display."
                        ),
                        warningsZh = listOf(
                            "显示眼镜视野有限。",
                            "常亮显示屏耗电严重。"
                        )
                    )
                )
            ))

            // 8. Gemini XR
            add(ToolCard(
                id = "gemini-xr-guide",
                title = "Gemini x XR Integration Development Guide",
                titleZh = "Gemini x XR 集成开发指南",
                description = "AI assistant + spatial UI + glasses hardware integration with Gemini.",
                descriptionZh = "AI 助手 + 空间 UI + 眼镜硬件与 Gemini 协同。",
                category = XrTab.GEMINI_XR,
                difficulty = Difficulty.ADVANCED,
                contentItems = listOf(
                    ToolContentItem(
                        id = "gemini-integration",
                        title = "Gemini + XR Integration",
                        titleZh = "Gemini + XR 集成",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "GeminiXRAssistant.kt",
                                labelZh = "Gemini XR 助手",
                                code = """class GeminiXRAssistant(
    private val geminiModel: GenerativeModel,
    private val speechRecognizer: SpeechRecognizer,
    private val textToSpeech: TextToSpeech
) {
    private var currentSession: ChatSession? = null
    private var spatialContext: SpatialContext? = null

    suspend fun initialize(spatialContext: SpatialContext) {
        this.spatialContext = spatialContext
        currentSession = geminiModel.startChat(
            systemInstruction = Content.fromString(
                "You are an AI assistant for XR glasses. Keep responses short (< 50 words)."
            )
        )
    }

    suspend fun processVoiceQuery(audioData: ByteArray): GeminiXRResponse {
        val query = speechRecognizer.transcribe(audioData)
        val spatialPrompt = buildSpatialPrompt(
            gazeDirection = spatialContext?.gazeDirection,
            handGesture = spatialContext?.lastHandGesture,
            nearbyObjects = spatialContext?.detectedObjects
        )
        val response = currentSession?.sendMessage(
            Content.fromString("${'$'}query\n\nContext: ${'$'}spatialPrompt")
        )?.text ?: "I'm not sure."
        val spatialDirection = geminiModel.analyzeSpatialIntent(response)
        return GeminiXRResponse(
            text = response,
            spatialDirection = spatialDirection,
            ttsOutput = truncateForTTS(response)
        )
    }
}""",
                                filename = "GeminiXRAssistant.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Initialize Gemini model with XR-specific system instruction",
                            "2. Collect spatial context: gaze, hand gestures, nearby objects",
                            "3. Inject spatial context into Gemini prompts",
                            "4. Map Gemini responses to spatial UI directions"
                        ),
                        stepsZh = listOf(
                            "1. 使用 XR 特定系统指令初始化 Gemini 模型",
                            "2. 收集空间上下文：视线方向、手势、附近物体",
                            "3. 将空间上下文注入 Gemini 提示词",
                            "4. 将 Gemini 响应映射到空间 UI 方向"
                        ),
                        warnings = listOf(
                            "Gemini API calls may add latency.",
                            "Privacy: Spatial context data sent to cloud requires user consent."
                        ),
                        warningsZh = listOf(
                            "Gemini API 调用可能增加延迟。",
                            "隐私：空间上下文数据发送到云端需要用户同意。"
                        )
                    )
                )
            ))

            // 9. Decision Tree
            add(ToolCard(
                id = "decision-tree-guide",
                title = "Android XR Platform Selection Decision Tree",
                titleZh = "Android XR 平台选择决策树",
                description = "App type to XR value assessment and gradual expansion strategy.",
                descriptionZh = "App 类型到 XR 价值评估和渐进式扩展策略。",
                category = XrTab.DECISION_TREE,
                difficulty = Difficulty.BEGINNER,
                contentItems = listOf(
                    ToolContentItem(
                        id = "decision-flow",
                        title = "XR Platform Selection Flowchart",
                        titleZh = "XR 平台选择流程图",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "XRDecisionTree.kt",
                                labelZh = "XR 决策树",
                                code = """// Q1: Does your app need real-time spatial awareness?
// -> Yes: ARCore XR or SceneCore
// Q2: Does your app deliver information passively?
// -> Yes: Audio Glasses (no display needed)
// Q3: Does your app need persistent AR overlay?
// -> Yes: Display Glasses + Glimmer
// Q4: Does your app provide immersive 3D experience?
// -> Yes: XR Headset + Compose XR + SceneCore

enum class XRAppType {
    SPATIAL_AWARENESS,
    PASSIVE_INFORMATION,
    AR_OVERLAY,
    IMMERSIVE_3D,
    STANDARD_WITH_XR_FALLBACK
}

fun selectXRStrategy(appType: XRAppType): XRPlatformRecommendation {
    return when (appType) {
        XRAppType.SPATIAL_AWARENESS -> XRPlatformRecommendation(
            primary = "ARCore for Jetpack XR",
            secondary = listOf("SceneCore", "Compose XR"),
            priority = Priority.P1
        )
        XRAppType.PASSIVE_INFORMATION -> XRPlatformRecommendation(
            primary = "Audio Glasses + TTS",
            secondary = listOf("Mobile notifications"),
            priority = Priority.P1
        )
        XRAppType.AR_OVERLAY -> XRPlatformRecommendation(
            primary = "Display Glasses + Glimmer",
            secondary = listOf("Glimmer Compose UI"),
            priority = Priority.P1
        )
        XRAppType.IMMERSIVE_3D -> XRPlatformRecommendation(
            primary = "XR Headset + Compose XR + SceneCore",
            secondary = listOf("ARCore XR"),
            priority = Priority.P2
        )
        XRAppType.STANDARD_WITH_XR_FALLBACK -> XRPlatformRecommendation(
            primary = "Standard Compose App (auto 2D on XR)",
            secondary = listOf("Glimmer UI for future"),
            priority = Priority.P2
        )
    }
}""",
                                filename = "XRDecisionTree.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Assess your app core value proposition",
                            "2. Determine if XR adds unique value vs. phone-only",
                            "3. Start with Audio Glasses or full XR",
                            "4. Use decision tree to select the right platform"
                        ),
                        stepsZh = listOf(
                            "1. 评估你的应用核心价值主张",
                            "2. 确定 XR 是否比纯手机增加独特价值",
                            "3. 从音频眼镜或全 XR 开始",
                            "4. 使用决策树选择正确的平台"
                        )
                    )
                )
            ))

            // 10. Catalyst
            add(ToolCard(
                id = "catalyst-guide",
                title = "Catalyst Program Application Toolkit",
                titleZh = "Catalyst Program 申请工具包",
                description = "Application process, evaluation criteria, hardware access strategy.",
                descriptionZh = "申请流程、评审标准、硬件获取策略。",
                category = XrTab.CATALYST,
                difficulty = Difficulty.BEGINNER,
                contentItems = listOf(
                    ToolContentItem(
                        id = "catalyst-app",
                        title = "Catalyst Program Application",
                        titleZh = "Catalyst 申请",
                        codeSnippets = listOf(
                            CodeSnippet(
                                language = "kotlin",
                                label = "CatalystChecklist.kt",
                                labelZh = "Catalyst 申请清单",
                                code = """// Stage 1: Eligibility
// - Active Google Play developer account
// - Existing or planned XR app
// - Technical team capable of XR development

// Stage 2: Application Materials
// - App concept deck (5-10 slides)
// - Technical architecture overview
// - Go-to-market strategy

// Stage 3: Technical Requirements
// - Minimum one Compose XR screen implemented
// - Pass XR emulator testing
// - ARCore XR: minimum 3 anchor types used
// - SceneCore: minimum 1 glTF model loaded

// Stage 4: Review Timeline
// Week 1-2: Application review
// Week 3-4: Technical interview
// Week 5-6: Final decision

data class CatalystChecklistItem(
    val stage: CatalystStage,
    val requirement: String,
    val requirementZh: String,
    val isCompleted: Boolean = false
)

enum class CatalystStage(val label: String, val labelZh: String) {
    ELIGIBILITY("Eligibility", "资格要求"),
    MATERIALS("Materials", "申请材料"),
    TECHNICAL("Technical", "技术要求"),
    REVIEW("Review", "审核时间线")
}""",
                                filename = "CatalystChecklist.kt"
                            )
                        ),
                        steps = listOf(
                            "1. Verify eligibility: Google Play account + XR concept",
                            "2. Prepare app concept deck (5-10 slides)",
                            "3. Implement minimum viable XR feature",
                            "4. Submit via Google I/O XR portal",
                            "5. Prepare for technical interview (Week 3-4)"
                        ),
                        stepsZh = listOf(
                            "1. 验证资格：Google Play 账号 + XR 应用概念",
                            "2. 准备应用概念文档（5-10 页）",
                            "3. 实现最小可行 XR 功能",
                            "4. 通过 Google I/O XR 门户提交申请",
                            "5. 准备技术面试（第 3-4 周）"
                        ),
                        warnings = listOf(
                            "Hardware is LIMITED. Apply early.",
                            "Catalyst Program is for serious developers only."
                        ),
                        warningsZh = listOf(
                            "硬件有限。尽早申请。",
                            "Catalyst Program 仅针对认真的开发者。"
                        )
                    )
                )
            ))
        }

        _state.value = _state.value.copy(
            toolCards = cards,
            isLoading = false
        )
    }
}
