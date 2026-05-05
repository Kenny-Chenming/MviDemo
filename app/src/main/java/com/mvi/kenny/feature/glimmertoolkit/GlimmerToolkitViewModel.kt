package com.mvi.kenny.feature.glimmertoolkit

// ================================================================
// GlimmerToolkitViewModel — Jetpack Compose Glimmer AI 眼镜 UI 开发工具包 ViewModel
// ================================================================
// MVI ViewModel for Jetpack Compose Glimmer AI Glasses UI Development Toolkit.
//
// PRD-230: Jetpack Compose Glimmer AI 眼镜 UI 开发工具包
//
// Responsibilities:
//   - Tab 0: Scan build.gradle.kts / Compose files for Glimmer migration candidates
//   - Tab 1: Provide Compose → Glimmer API mapping guide
//   - Tab 2: Display additive display best practices (5 principles)
//   - Tab 3: Guide Projected integration from host app to glasses
//   - Tab 4: Provide 1-meter focal length design specifications
//   - Tab 5: Compatibility self-check with risk-level categorized issues
//   - Tab 6: KMP modular architecture guide (expect/actual pattern)
//   - Tab 7: Projected collaboration decision tree
//   - Tab 8: CI compliance detection tool (Gradle plugin form)
//   - Tab 9: Google I/O 2026 expected preview (14-day countdown)
//
// MVI Pattern: State (UI) + Intent (User Action) + Effect (One-time events)
// Bilingual comments: CN + EN
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ============================================================
 * GlimmerToolkitViewModel — MVI ViewModel
 * ============================================================
 * Manages state for all 10 tabs, processes user intents, emits effects.
 */
class GlimmerToolkitViewModel : ViewModel() {

    // ── MVI State ──
    private val _state = MutableStateFlow(GlimmerToolkitState())
    val state: StateFlow<GlimmerToolkitState> = _state.asStateFlow()

    // ── MVI Effects (one-time side effects via Channel) ──
    private val _effects = Channel<GlimmerToolkitEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        _state.value = _state.value.copy(
            componentMappings = buildComponentMappings(),
            additivePrinciples = buildAdditivePrinciples(),
            projectedSteps = buildProjectedSteps(),
            focalLengthSpecs = buildFocalLengthSpecs(),
            compatibilityIssues = buildCompatibilityIssues(),
            kmpModuleGuides = buildKMPModuleGuides(),
            decisionNodes = buildDecisionNodes(),
            ciComplianceRules = buildCIComplianceRules(),
            io2026Sessions = buildIO2026Sessions(),
            io2026Countdown = computeIO2026Countdown()
        )
    }

    /** Process user intent — main entry point for all user actions */
    fun processIntent(intent: GlimmerToolkitIntent) {
        when (intent) {
            is GlimmerToolkitIntent.SelectTab -> selectTab(intent.index)
            is GlimmerToolkitIntent.UpdateProjectPath -> updateProjectPath(intent.path)
            is GlimmerToolkitIntent.StartScan -> startScan()
            is GlimmerToolkitIntent.ClearScanResults -> clearScanResults()
            is GlimmerToolkitIntent.ExpandScanResult -> expandScanResult(intent.id)
            is GlimmerToolkitIntent.SelectMappingCategory -> selectMappingCategory(intent.category)
            is GlimmerToolkitIntent.ExpandMapping -> expandMapping(intent.id)
            is GlimmerToolkitIntent.ExpandPrinciple -> expandPrinciple(intent.id)
            is GlimmerToolkitIntent.ToggleProjectedStep -> toggleProjectedStep(intent.id)
            is GlimmerToolkitIntent.ExpandStep -> expandStep(intent.id)
            is GlimmerToolkitIntent.ExpandFocalSpec -> expandFocalSpec(intent.id)
            is GlimmerToolkitIntent.FilterByRisk -> filterByRisk(intent.risk)
            is GlimmerToolkitIntent.ExpandIssue -> expandIssue(intent.id)
            is GlimmerToolkitIntent.ExpandKMPGuide -> expandKMPGuide(intent.id)
            is GlimmerToolkitIntent.SelectDecisionNode -> selectDecisionNode(intent.nodeId)
            is GlimmerToolkitIntent.ExpandCIRule -> expandCIRule(intent.id)
            is GlimmerToolkitIntent.ExpandIOSession -> expandIOSession(intent.id)
        }
    }

    // ══════════════════════════════════════════════════════
    // Tab Navigation
    // ══════════════════════════════════════════════════════

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    // ══════════════════════════════════════════════════════
    // Tab 0: Migration Scanner
    // ══════════════════════════════════════════════════════

    private fun updateProjectPath(path: String) {
        _state.value = _state.value.copy(projectPath = path)
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                scanState = GlimmerScanState(phase = GlimmerScanPhase.SCANNING_GRADLE, progress = 0)
            )
            delay(300)
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(phase = GlimmerScanPhase.SCANNING_COMPOSE, progress = 30)
            )
            delay(400)
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(phase = GlimmerScanPhase.ANALYZING, progress = 60)
            )
            delay(300)
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(phase = GlimmerScanPhase.GENERATING_REPORT, progress = 80)
            )
            delay(300)
            val results = buildSimulatedScanResults()
            _state.value = _state.value.copy(
                scanState = GlimmerScanState(
                    phase = GlimmerScanPhase.COMPLETED,
                    progress = 100,
                    results = results,
                    scannedFilesCount = 42
                )
            )
            _effects.send(GlimmerToolkitEffect.ScanCompleted(results.size, 1300))
        }
    }

    private fun clearScanResults() {
        _state.value = _state.value.copy(
            scanState = GlimmerScanState(),
            projectPath = ""
        )
    }

    private fun expandScanResult(id: String?) {
        _state.value = _state.value.copy(expandedScanResult = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 1: Component Mapping
    // ══════════════════════════════════════════════════════

    private fun selectMappingCategory(category: String?) {
        _state.value = _state.value.copy(selectedMappingCategory = category)
    }

    private fun expandMapping(id: String?) {
        _state.value = _state.value.copy(expandedMapping = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 2: Additive Display
    // ══════════════════════════════════════════════════════

    private fun expandPrinciple(id: String?) {
        _state.value = _state.value.copy(expandedPrinciple = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 3: Projected Template
    // ══════════════════════════════════════════════════════

    private fun toggleProjectedStep(id: String) {
        val steps = _state.value.projectedSteps.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _state.value = _state.value.copy(projectedSteps = steps)
    }

    private fun expandStep(id: String?) {
        _state.value = _state.value.copy(expandedStep = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 4: Focal Length Guide
    // ══════════════════════════════════════════════════════

    private fun expandFocalSpec(id: String?) {
        _state.value = _state.value.copy(expandedFocalSpec = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 5: Compatibility Checker
    // ══════════════════════════════════════════════════════

    private fun filterByRisk(risk: CompatibilityRiskLevel?) {
        _state.value = _state.value.copy(selectedRiskFilter = risk)
    }

    private fun expandIssue(id: String?) {
        _state.value = _state.value.copy(expandedIssue = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 6: KMP Module Guide
    // ══════════════════════════════════════════════════════

    private fun expandKMPGuide(id: String?) {
        _state.value = _state.value.copy(expandedKMPGuide = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 7: Decision Tree
    // ══════════════════════════════════════════════════════

    private fun selectDecisionNode(nodeId: String) {
        _state.value = _state.value.copy(currentNodeId = nodeId)
    }

    // ══════════════════════════════════════════════════════
    // Tab 8: CI Compliance
    // ══════════════════════════════════════════════════════

    private fun expandCIRule(id: String?) {
        _state.value = _state.value.copy(expandedCIRule = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 9: I/O 2026 Preview
    // ══════════════════════════════════════════════════════

    private fun expandIOSession(id: String?) {
        _state.value = _state.value.copy(expandedIOSession = id)
    }

    // ══════════════════════════════════════════════════════
    // Data Builders
    // ══════════════════════════════════════════════════════

    private fun buildSimulatedScanResults(): List<GlimmerScanResult> = listOf(
        GlimmerScanResult(
            id = "scan_1",
            file = "app/src/main/java/com/mvi/kenny/ui/components/MyText.kt",
            line = 23,
            category = GlimmerMigrationCategory.TEXT,
            severity = GlimmerSeverity.HIGH,
            composeApi = "Text(text = \"Hello\")",
            glimmerApi = "GlimmerText(text = \"Hello\", style = GlimmerTextStyle.Body)",
            description = "Replace Compose Text with GlimmerText for better optical performance",
            descriptionCn = "将 Compose Text 替换为 GlimmerText 以获得更好的光学性能",
            codeExample = "Text(text = \"Hello\", style = MaterialTheme.typography.bodyLarge)",
            glimmerExample = "GlimmerText(text = \"Hello\", style = GlimmerTextStyle.Body)"
        ),
        GlimmerScanResult(
            id = "scan_2",
            file = "app/src/main/java/com/mvi/kenny/ui/components/MyCard.kt",
            line = 45,
            category = GlimmerMigrationCategory.CARD,
            severity = GlimmerSeverity.MEDIUM,
            composeApi = "Card(modifier = Modifier.padding(8.dp))",
            glimmerApi = "GlimmerSurface(modifier = Modifier.glimmerPadding(8))",
            description = "Replace Card with GlimmerSurface for transparent background support",
            descriptionCn = "将 Card 替换为 GlimmerSurface 以支持透明背景",
            codeExample = "Card(modifier = Modifier.padding(8.dp)) { Text(\"Content\") }",
            glimmerExample = "GlimmerSurface(modifier = Modifier.glimmerPadding(8)) { GlimmerText(\"Content\") }"
        ),
        GlimmerScanResult(
            id = "scan_3",
            file = "app/src/main/java/com/mvi/kenny/ui/screens/HomeScreen.kt",
            line = 67,
            category = GlimmerMigrationCategory.BUTTON,
            severity = GlimmerSeverity.HIGH,
            composeApi = "Button(onClick = { }) { Text(\"Click\") }",
            glimmerApi = "GlimmerButton(text = \"Click\", onClick = { })",
            description = "Replace Button with GlimmerButton for AI glasses optimized touch targets",
            descriptionCn = "将 Button 替换为 GlimmerButton 以获得 AI 眼镜优化的触摸目标",
            codeExample = "Button(onClick = { doSomething() }) { Text(\"Click\") }",
            glimmerExample = "GlimmerButton(text = \"Click\", onClick = { doSomething() })"
        ),
        GlimmerScanResult(
            id = "scan_4",
            file = "app/src/main/java/com/mvi/kenny/ui/components/MyList.kt",
            line = 89,
            category = GlimmerMigrationCategory.LIST,
            severity = GlimmerSeverity.MEDIUM,
            composeApi = "LazyColumn { items(items) { ItemRow(it) } }",
            glimmerApi = "GlimmerList(items = items) { ItemRow(it) }",
            description = "Replace LazyColumn with GlimmerList for optimized virtual scrolling on Glasses",
            descriptionCn = "将 LazyColumn 替换为 GlimmerList 以获得眼镜优化的虚拟滚动",
            codeExample = "LazyColumn { items(items) { ItemRow(it) } }",
            glimmerExample = "GlimmerList(items = items) { item -> ItemRow(item) }"
        ),
        GlimmerScanResult(
            id = "scan_5",
            file = "app/src/main/java/com/mvi/kenny/ui/theme/Theme.kt",
            line = 12,
            category = GlimmerMigrationCategory.THEMING,
            severity = GlimmerSeverity.LOW,
            composeApi = "MaterialTheme(colors = darkColors)",
            glimmerApi = "GlimmerTheme(colors = glimmerDarkColors)",
            description = "Replace MaterialTheme with GlimmerTheme for additive display color model",
            descriptionCn = "将 MaterialTheme 替换为 GlimmerTheme 以支持加法显示颜色模型",
            codeExample = "MaterialTheme(colors = darkColors) { content() }",
            glimmerExample = "GlimmerTheme(colors = glimmerDarkColors) { content() }"
        )
    )

    private fun buildComponentMappings(): List<ComponentMapping> = listOf(
        ComponentMapping(
            id = "map_text", category = "Text", categoryCn = "文本",
            composeApi = "Text(text: String)",
            glimmerApi = "GlimmerText(text: String, style: GlimmerTextStyle)",
            description = "Compose Text vs GlimmerText — same content, different rendering pipeline",
            descriptionCn = "Compose Text vs GlimmerText — 相同内容，不同渲染管线",
            codeExample = "Text(\"Hello\", style = MaterialTheme.typography.bodyLarge)",
            glimmerExample = "GlimmerText(\"Hello\", style = GlimmerTextStyle.Body)",
            notes = "Use GlimmerTextStyle enum for consistent sizing across different glass hardware"
        ),
        ComponentMapping(
            id = "map_icon", category = "Icon", categoryCn = "图标",
            composeApi = "Icon(Icons.Default.X, contentDescription)",
            glimmerApi = "GlimmerIcon(icon: GlimmerIconType, tint: Color?)",
            description = "Compose Icon vs GlimmerIcon — vector vs monochrome additive display",
            descriptionCn = "Compose Icon vs GlimmerIcon — 矢量 vs 单色加法显示",
            codeExample = "Icon(Icons.Default.Settings, \"Settings\")",
            glimmerExample = "GlimmerIcon(icon = GlimmerIconType.Settings, tint = Color.White)",
            notes = "GlimmerIcon only supports monochrome icons; use GlimmerIconType enum"
        ),
        ComponentMapping(
            id = "map_card", category = "Card", categoryCn = "卡片",
            composeApi = "Card(modifier, elevation, shape)",
            glimmerApi = "GlimmerSurface(modifier, glowIntensity)",
            description = "Compose Card vs GlimmerSurface — Card has shadow, GlimmerSurface has glow",
            descriptionCn = "Compose Card vs GlimmerSurface — Card 有阴影，GlimmerSurface 有光晕",
            codeExample = "Card(modifier = Modifier.padding(8.dp)) { Text(\"Card\") }",
            glimmerExample = "GlimmerSurface(modifier = Modifier.glimmerPadding(8)) { GlimmerText(\"Surface\") }",
            notes = "GlimmerSurface background must be black for transparency effect"
        ),
        ComponentMapping(
            id = "map_button", category = "Button", categoryCn = "按钮",
            composeApi = "Button(onClick, modifier, enabled)",
            glimmerApi = "GlimmerButton(text, onClick, buttonStyle)",
            description = "Compose Button vs GlimmerButton — elevated vs flat additive design",
            descriptionCn = "Compose Button vs GlimmerButton — 立体 vs 平面加法设计",
            codeExample = "Button(onClick = { }) { Text(\"Submit\") }",
            glimmerExample = "GlimmerButton(text = \"Submit\", onClick = { })",
            notes = "GlimmerButton always uses high-contrast white text on black background"
        ),
        ComponentMapping(
            id = "map_list", category = "LazyColumn", categoryCn = "列表",
            composeApi = "LazyColumn(items, modifier)",
            glimmerApi = "GlimmerList(items, itemTemplate)",
            description = "Compose LazyColumn vs GlimmerList — standard scroll vs glasses-optimized",
            descriptionCn = "Compose LazyColumn vs GlimmerList — 标准滚动 vs 眼镜优化",
            codeExample = "LazyColumn { items(list) { Item(it) } }",
            glimmerExample = "GlimmerList(items = list) { item -> ItemRow(item) }",
            notes = "GlimmerList auto-fits item height for 1-meter focal length readability"
        ),
        ComponentMapping(
            id = "map_chip", category = "Chip", categoryCn = "标签",
            composeApi = "FilterChip(selected, onClick, label)",
            glimmerApi = "GlimmerChip(label, isSelected, onSelect)",
            description = "Compose FilterChip vs GlimmerChip — Material vs additive display model",
            descriptionCn = "Compose FilterChip vs GlimmerChip — Material vs 加法显示模型",
            codeExample = "FilterChip(selected = true, onClick = { }) { Text(\"Selected\") }",
            glimmerExample = "GlimmerChip(label = \"Selected\", isSelected = true, onSelect = { })",
            notes = "GlimmerChip selection state uses brightness level (100%=selected, 50%=unselected)"
        ),
        ComponentMapping(
            id = "map_box", category = "Box", categoryCn = "布局盒",
            composeApi = "Box(modifier, contentAlignment, propagateMinConstraints)",
            glimmerApi = "GlimmerCanvas(modifier, drawContent)",
            description = "Compose Box vs GlimmerCanvas — imperative vs declarative draw",
            descriptionCn = "Compose Box vs GlimmerCanvas — 命令式 vs 声明式绘制",
            codeExample = "Box(modifier = Modifier.fillMaxSize()) { content() }",
            glimmerExample = "GlimmerCanvas(modifier = Modifier.fillMaxSize()) { drawCircle() }",
            notes = "Use GlimmerCanvas for custom shapes that need additive rendering"
        ),
        ComponentMapping(
            id = "map_animation", category = "Animation", categoryCn = "动画",
            composeApi = "AnimatedVisibility(visible, enter, exit)",
            glimmerApi = "GlimmerFade(visible, durationMs) / GlimmerSlide",
            description = "Compose AnimatedVisibility vs GlimmerFade — crossfade vs additive fade",
            descriptionCn = "Compose AnimatedVisibility vs GlimmerFade — 淡入淡出 vs 加法淡化",
            codeExample = "AnimatedVisibility(visible) { Text(\"Appears\") }",
            glimmerExample = "GlimmerFade(visible = visible, durationMs = 300) { GlimmerText(\"Appears\") }",
            notes = "Glimmer animations always fade TO/FROM black (additive display baseline)"
        )
    )

    private fun buildAdditivePrinciples(): List<AdditiveDisplayPrinciple> = listOf(
        AdditiveDisplayPrinciple(
            id = "ap_1",
            title = "Black = Transparent",
            titleCn = "黑色 = 透明",
            principle = "Black pixels are OFF — they show the real-world background through the glass",
            principleCn = "黑色像素是关闭状态 — 通过眼镜显示真实世界背景",
            description = "Unlike LCD screens where black is a backlit color, OLED on glasses means no pixel illumination. Black = transparent window into reality.",
            descriptionCn = "与 LCD 屏幕不同，OLED 在眼镜上意味着没有像素照明。黑色 = 透明的现实世界窗口。",
            codeExample = "// Good: Use black background for natural overlay\nBox(modifier = Modifier.background(Color.Black)) {\n  GlimmerText(\"Floating UI\")\n}",
            codeExampleCn = "// 好：使用黑色背景实现自然叠加\nBox(modifier = Modifier.background(Color.Black)) {\n  GlimmerText(\"浮动 UI\")\n}",
            icon = "⬛"
        ),
        AdditiveDisplayPrinciple(
            id = "ap_2",
            title = "Glow Intensity Control",
            titleCn = "光晕强度控制",
            principle = "Limit simultaneous glowing elements to 3 max to preserve battery and readability",
            principleCn = "限制同时发光元素最多 3 个，以保持电池寿命和可读性",
            description = "Each glowing element drains battery. Keep max 3 bright elements visible at once.",
            descriptionCn = "每个发光元素都消耗电池。一次最多保持 3 个明亮元素可见。",
            codeExample = "// Good: Max 3 glowing elements\nGlimmerText(\"Title\", brightness = 1.0f)\nGlimmerText(\"Subtitle\", brightness = 0.5f)\nGlimmerText(\"Body\", brightness = 0.3f)",
            codeExampleCn = "// 好：最多 3 个发光元素\nGlimmerText(\"标题\", brightness = 1.0f)\nGlimmerText(\"副标题\", brightness = 0.5f)\nGlimmerText(\"正文\", brightness = 0.3f)",
            icon = "💡"
        ),
        AdditiveDisplayPrinciple(
            id = "ap_3",
            title = "Battery Optimization",
            titleCn = "电池优化",
            principle = "Use duty cycling: full-brightness ping every 2s instead of constant dim glow",
            principleCn = "使用占空比循环：每 2 秒一次全亮 ping，而非持续暗淡发光",
            description = "Constant dim glow uses more battery than periodic full-brightness pulses.",
            descriptionCn = "持续暗淡发光比周期性全亮脉冲消耗更多电池。",
            codeExample = "// Good: Burst refresh pattern\nLaunchedEffect(Unit) {\n  while (true) {\n    delay(2000)\n    visible = !visible\n  }\n}\nif (visible) {\n  GlimmerText(\"Active\", brightness = 1.0f)\n}",
            codeExampleCn = "// 好：突发刷新模式\nLaunchedEffect(Unit) {\n  while (true) {\n    delay(2000)\n    visible = !visible\n  }\n}",
            icon = "🔋"
        ),
        AdditiveDisplayPrinciple(
            id = "ap_4",
            title = "Bottom Anchor",
            titleCn = "底部锚定",
            principle = "Always anchor glanceable UI at the bottom 20% of the visual field",
            principleCn = "始终将瞥视 UI 锚定在视野底部 20% 区域",
            description = "Users look down to read glasses UI. Place critical info in the lower visual field.",
            descriptionCn = "用户向下看以阅读眼镜 UI。将关键信息放在下部视野区域。",
            codeExample = "// Good: Bottom-anchored glanceable UI\nGlimmerSurface(\n  modifier = Modifier\n    .align(Alignment.BottomCenter)\n    .fillMaxWidth()\n    .height(120.dp),\n  backgroundColor = Color.Black\n) {\n  GlimmerText(\"Status: OK\", style = GlimmerTextStyle.Caption)\n}",
            codeExampleCn = "// 好：底部锚定的瞥视 UI\nGlimmerSurface(\n  modifier = Modifier\n    .align(Alignment.BottomCenter)\n    .fillMaxWidth()\n    .height(120.dp),\n  backgroundColor = Color.Black\n) {\n  GlimmerText(\"状态: 正常\", style = GlimmerTextStyle.Caption)\n}",
            icon = "⚓"
        ),
        AdditiveDisplayPrinciple(
            id = "ap_5",
            title = "High Contrast",
            titleCn = "高对比度",
            principle = "Minimum 7:1 contrast ratio. White on Black = 21:1 (ideal).",
            principleCn = "最低 7:1 对比度。白色 on 黑色 = 21:1（理想）。",
            description = "Additive display color mixing with real-world background means contrast is everything.",
            descriptionCn = "加法显示与真实世界背景的颜色混合意味着对比度就是一切。",
            codeExample = "// Good: Maximum contrast\nGlimmerText(\"ALERT\", color = Color.White, brightness = 1.0f)\n\n// Bad: Low contrast — blue/purple hard to see outdoors\nGlimmerText(\"Warning\", color = Color(0xFF6B5BFF))",
            codeExampleCn = "// 好：最大对比度\nGlimmerText(\"警告\", color = Color.White, brightness = 1.0f)\n\n// 差：低对比度\nGlimmerText(\"警告\", color = Color(0xFF6B5BFF))",
            icon = "🎨"
        )
    )

    private fun buildProjectedSteps(): List<ProjectedStep> = listOf(
        ProjectedStep(
            id = "proj_1", stepNumber = 1,
            title = "Add Projected dependency",
            titleCn = "添加 Projected 依赖",
            description = "Add Jetpack Projected library to your host app's build.gradle.kts",
            descriptionCn = "在主机 App 的 build.gradle.kts 中添加 Jetpack Projected 库",
            codeSnippet = "// build.gradle.kts (app level)\ndependencies {\n  implementation(\"androidx.projected:projected:1.0.0\")\n}",
            codeSnippetCn = "// build.gradle.kts (应用级别)\ndependencies {\n  implementation(\"androidx.projected:projected:1.0.0\")\n}"
        ),
        ProjectedStep(
            id = "proj_2", stepNumber = 2,
            title = "Define ProjectedSession",
            titleCn = "定义 ProjectedSession",
            description = "Create a ProjectedSession that manages the host-glasses connection lifecycle",
            descriptionCn = "创建管理主机-眼镜连接生命周期的 ProjectedSession",
            codeSnippet = "class GlassesSession : ProjectedSession() {\n  override fun onStartProjection(request: ProjectionRequest) {\n    super.onStartProjection(request)\n    updateGlassesContent(currentContent)\n  }\n  override fun onStopProjection() {\n    super.onStopProjection()\n  }\n}",
            codeSnippetCn = "class GlassesSession : ProjectedSession() {\n  override fun onStartProjection(request: ProjectionRequest) {\n    super.onStartProjection(request)\n    updateGlassesContent(currentContent)\n  }\n  override fun onStopProjection() {\n    super.onStopProjection()\n  }\n}"
        ),
        ProjectedStep(
            id = "proj_3", stepNumber = 3,
            title = "Create Glimmer UI Composable",
            titleCn = "创建 Glimmer UI Composable",
            description = "Build Glimmer-optimized UI that renders on the glasses device",
            descriptionCn = "构建在眼镜设备上渲染的 Glimmer 优化 UI",
            codeSnippet = "@Composable\nfun GlimmerHomeScreen(state: HomeState) {\n  GlimmerTheme {\n    GlimmerSurface(\n      modifier = Modifier\n        .fillMaxWidth()\n        .height(200.dp)\n        .align(Alignment.BottomCenter),\n      backgroundColor = Color.Black\n    ) {\n      Column(modifier = Modifier.glimmerPadding(16.dp)) {\n        GlimmerText(text = state.greeting, style = GlimmerTextStyle.Headline)\n      }\n    }\n  }\n}",
            codeSnippetCn = "@Composable\nfun GlimmerHomeScreen(state: HomeState) {\n  GlimmerTheme {\n    GlimmerSurface(\n      modifier = Modifier\n        .fillMaxWidth()\n        .height(200.dp)\n        .align(Alignment.BottomCenter),\n      backgroundColor = Color.Black\n    ) {\n      Column(modifier = Modifier.glimmerPadding(16.dp)) {\n        GlimmerText(text = state.greeting, style = GlimmerTextStyle.Headline)\n      }\n    }\n  }\n}"
        ),
        ProjectedStep(
            id = "proj_4", stepNumber = 4,
            title = "Wire Host App to Glasses",
            titleCn = "将主机 App 连接到眼镜",
            description = "Connect the host app to glasses via ProjectedSessionManager",
            descriptionCn = "通过 ProjectedSessionManager 将主机 App 连接到眼镜",
            codeSnippet = "class MainActivity : Activity() {\n  private val sessionManager = ProjectedSessionManager()\n  override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n    sessionManager.registerProjectionHost(\n      activity = this,\n      sessionFactory = { GlassesSession() },\n      onGlassesConnected = { session ->\n        session.updateGlassesContent(GlimmerHomeScreen(state = viewModel.state))\n      }\n    )\n  }\n}",
            codeSnippetCn = "class MainActivity : Activity() {\n  private val sessionManager = ProjectedSessionManager()\n  override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)\n    sessionManager.registerProjectionHost(\n      activity = this,\n      sessionFactory = { GlassesSession() },\n      onGlassesConnected = { session ->\n        session.updateGlassesContent(GlimmerHomeScreen(state = viewModel.state))\n      }\n    )\n  }\n}"
        ),
        ProjectedStep(
            id = "proj_5", stepNumber = 5,
            title = "Test on Real Hardware",
            titleCn = "在真实硬件上测试",
            description = "Deploy to glasses device and verify additive display behavior",
            descriptionCn = "部署到眼镜设备并验证加法显示行为",
            codeSnippet = "# Verify Glimmer UI projection\nadb -d <glasses_serial> shell am start \\\n  -n com.android.settings/.DevelopmentSettings\nadb -d <glasses_serial> shell dumpsys \\\n  activity services com.android.projected",
            codeSnippetCn = "# 验证 Glimmer UI 投影\nadb -d <glasses_serial> shell am start \\\n  -n com.android.settings/.DevelopmentSettings\nadb -d <glasses_serial> shell dumpsys \\\n  activity services com.android.projected"
        )
    )

    private fun buildFocalLengthSpecs(): List<FocalLengthSpec> = listOf(
        FocalLengthSpec(
            id = "fls_1",
            elementType = "Headline / 大标题",
            elementTypeCn = "大标题",
            minSize = "24sp",
            recommendedSize = "32sp",
            lineHeight = "40sp",
            spacing = "16dp",
            example = "GlimmerText(\"Welcome\", style = GlimmerTextStyle.Headline)",
            notes = "Headlines are always full brightness (1.0f)"
        ),
        FocalLengthSpec(
            id = "fls_2",
            elementType = "Body / 正文",
            elementTypeCn = "正文",
            minSize = "16sp",
            recommendedSize = "20sp",
            lineHeight = "28sp",
            spacing = "12dp",
            example = "GlimmerText(\"Description\", style = GlimmerTextStyle.Body)",
            notes = "Body text can be dimmed to 0.7f for secondary info"
        ),
        FocalLengthSpec(
            id = "fls_3",
            elementType = "Caption / 辅助文字",
            elementTypeCn = "辅助文字",
            minSize = "12sp",
            recommendedSize = "14sp",
            lineHeight = "20sp",
            spacing = "8dp",
            example = "GlimmerText(\"Timestamp\", style = GlimmerTextStyle.Caption)",
            notes = "Caption should use brightness 0.5f or lower"
        ),
        FocalLengthSpec(
            id = "fls_4",
            elementType = "Icon / 图标",
            elementTypeCn = "图标",
            minSize = "24dp × 24dp",
            recommendedSize = "32dp × 32dp",
            lineHeight = "N/A",
            spacing = "8dp",
            example = "GlimmerIcon(icon = GlimmerIconType.Home, size = 32.dp)",
            notes = "Icons must be monochrome (white or brightness-adjusted white)"
        ),
        FocalLengthSpec(
            id = "fls_5",
            elementType = "Button / 按钮",
            elementTypeCn = "按钮",
            minSize = "48dp height",
            recommendedSize = "56dp height",
            lineHeight = "N/A",
            spacing = "16dp",
            example = "GlimmerButton(text = \"Confirm\", height = 56.dp)",
            notes = "Min touch target 48dp; recommended 56dp for glasses use"
        ),
        FocalLengthSpec(
            id = "fls_6",
            elementType = "List Item / 列表项",
            elementTypeCn = "列表项",
            minSize = "48dp height",
            recommendedSize = "64dp height",
            lineHeight = "N/A",
            spacing = "8dp",
            example = "GlimmerListItem(title = \"Item\", subtitle = \"Desc\")",
            notes = "GlimmerList auto-calculates item height based on 1m focal length"
        )
    )

    private fun buildCompatibilityIssues(): List<CompatibilityIssue> = listOf(
        CompatibilityIssue(
            id = "ci_1",
            title = "MaterialTheme color model incompatibility",
            titleCn = "MaterialTheme 颜色模型不兼容",
            description = "MaterialTheme uses subtractive color. Glimmer uses additive color. Dark theme backgrounds will appear dim instead of transparent.",
            descriptionCn = "MaterialTheme 使用减法颜色。Glimmer 使用加法颜色。深色主题背景会显得暗淡而不是透明。",
            riskLevel = CompatibilityRiskLevel.CRITICAL,
            affectedVersions = "All versions",
            suggestion = "Replace MaterialTheme with GlimmerTheme. Define colors as brightness percentages.",
            suggestionCn = "将 MaterialTheme 替换为 GlimmerTheme。将颜色定义为亮度百分比。"
        ),
        CompatibilityIssue(
            id = "ci_2",
            title = "Elevation/shadow not visible on additive display",
            titleCn = "elevation/阴影在加法显示上不可见",
            description = "Card shadows rely on ambient occlusion. On additive OLED displays, shadows cannot be rendered.",
            descriptionCn = "Card 阴影依赖环境光遮蔽。在 OLED 加法显示器上，阴影无法渲染。",
            riskLevel = CompatibilityRiskLevel.HIGH,
            affectedVersions = "All versions",
            suggestion = "Replace Card with GlimmerSurface. Use brightness instead of elevation for depth.",
            suggestionCn = "将 Card 替换为 GlimmerSurface。使用亮度而不是 elevation 来表现深度感。"
        ),
        CompatibilityIssue(
            id = "ci_3",
            title = "LazyColumn item height not optimized for 1m focal length",
            titleCn = "LazyColumn 项目高度未针对 1 米焦距优化",
            description = "Standard Android list item heights (48-56dp) appear too small at 1m focal length.",
            descriptionCn = "标准 Android 列表项高度（48-56dp）在 1 米焦距下显得太小。",
            riskLevel = CompatibilityRiskLevel.HIGH,
            affectedVersions = "All versions",
            suggestion = "Use GlimmerList instead of LazyColumn. GlimmerList auto-calculates item height (min 64dp).",
            suggestionCn = "使用 GlimmerList 而不是 LazyColumn。GlimmerList 自动计算项目高度（最少 64dp）。"
        ),
        CompatibilityIssue(
            id = "ci_4",
            title = "Blue/purple text colors have low optical density",
            titleCn = "蓝色/紫色文字颜色光学密度低",
            description = "Blue and purple hues have lower perceived brightness on additive displays, especially outdoors.",
            descriptionCn = "蓝色和紫色色调在加法显示器上感知亮度较低，尤其是在户外环境中。",
            riskLevel = CompatibilityRiskLevel.MEDIUM,
            affectedVersions = "All versions",
            suggestion = "Use only white(text on black background. If color coding is needed, use brightness levels instead of hue differences.",
            suggestionCn = "仅使用黑色背景上的白色文字。如果需要颜色编码，请使用亮度级别而不是色调差异。"
        ),
        CompatibilityIssue(
            id = "ci_5",
            title = "Continuous animation drains battery on glasses",
            titleCn = "持续动画消耗眼镜电池",
            description = "Looping animations keep the OLED pixel constant-state, which is power-intensive on glasses.",
            descriptionCn = "循环动画保持 OLED 像素恒定状态，这在眼镜上功耗很高。",
            riskLevel = CompatibilityRiskLevel.MEDIUM,
            affectedVersions = "Compose 1.4+",
            suggestion = "Replace with GlimmerFade/GlimmerSlide that operates in burst mode.",
            suggestionCn = "替换为以突发模式运行的 GlimmerFade/GlimmerSlide。"
        ),
        CompatibilityIssue(
            id = "ci_6",
            title = "No keyboard navigation support",
            titleCn = "不支持键盘导航",
            description = "Compose apps that only handle touch events will not work with glasses' gesture/touchpad input.",
            descriptionCn = "仅处理触摸事件的 Compose 应用无法配合眼镜的手势/触控板输入模式使用。",
            riskLevel = CompatibilityRiskLevel.LOW,
            affectedVersions = "All versions",
            suggestion = "Ensure all interactive elements respond to KeyEvent/PointerEvent.",
            suggestionCn = "确保所有交互元素响应 KeyEvent/PointerEvent。"
        )
    )

    private fun buildKMPModuleGuides(): List<KMPModuleGuide> = listOf(
        KMPModuleGuide(
            id = "kmp_1",
            moduleName = ":shared:glimmer-common",
            moduleNameCn = ":shared:glimmer-common",
            moduleType = KMPModuleType.SHARED,
            description = "Shared Glimmer UI primitives usable across Android, Wear, and Glasses targets",
            descriptionCn = "可在 Android、Wear 和眼镜目标之间共享的 Glimmer UI 原语",
            expectCode = "expect class GlimmerText(\n  text: String,\n  style: GlimmerTextStyle = GlimmerTextStyle.Body,\n  brightness: Float = 1.0f\n)",
            androidActual = "actual typealias GlimmerText = androidx.compose.foundation.text.BasicText",
            wearActual = "actual typealias GlimmerText = androidx.compose.material3.Text",
            glimmerActual = "actual class GlimmerText(\n  private val text: String,\n  private val style: GlimmerTextStyle,\n  private val brightness: Float\n) : GlimmerComposable"
        ),
        KMPModuleGuide(
            id = "kmp_2",
            moduleName = ":shared:glimmer-layouts",
            moduleNameCn = ":shared:glimmer-layouts",
            moduleType = KMPModuleType.SHARED,
            description = "Shared layout primitives (GlimmerColumn, GlimmerRow, GlimmerBox)",
            descriptionCn = "共享布局原语（GlimmerColumn、GlimmerRow、GlimmerBox）",
            expectCode = "expect object GlimmerLayouts {\n  expect fun Column(...) : GlimmerComposable\n  expect fun Row(...) : GlimmerComposable\n  expect fun Box(...) : GlimmerComposable\n}",
            androidActual = "actual object GlimmerLayouts {\n  actual fun Column(...) = androidx.compose.foundation.layout.Column(...)\n  actual fun Row(...) = androidx.compose.foundation.layout.Row(...)\n  actual fun Box(...) = androidx.compose.foundation.layout.Box(...)\n}",
            glimmerActual = "actual object GlimmerLayouts {\n  // Custom glass-optimized layouts with 1m focal length specs\n}"
        ),
        KMPModuleGuide(
            id = "kmp_3",
            moduleName = ":platform:glimmer-glasses",
            moduleNameCn = ":platform:glimmer-glasses",
            moduleType = KMPModuleType.GLIMMER_SPECIFIC,
            description = "Glimmer-specific: ProjectedSession, GlassesSensorManager, BatteryOptimization",
            descriptionCn = "Glimmer 特定：ProjectedSession、GlassesSensorManager、电池优化",
            expectCode = "expect object GlimmerPlatform",
            androidActual = "actual typealias GlimmerPlatform = Any",
            glimmerActual = "actual object GlimmerPlatform {\n  suspend fun requestProjection(session: GlassesSession) { ... }\n  fun getBatteryLevel(): Int { ... }\n}"
        )
    )

    private fun buildDecisionNodes(): List<DecisionNode> = listOf(
        DecisionNode(
            id = "root",
            question = "What is your primary target device?",
            questionCn = "您的主要目标设备是什么？",
            options = listOf(
                DecisionOption("📱 Phone / 手机", "手机", "phone"),
                DecisionOption("⌚ Wear OS Watch / 手表", "手表", "watch"),
                DecisionOption("👓 AI Glasses / 眼镜", "眼镜", "glasses")
            ),
            recommendation = "Use Glimmer UI Toolkit directly for glasses",
            recommendationCn = "直接使用 Glimmer UI Toolkit 进行眼镜开发",
            useCase = "Native Glasses UI with additive display optimization",
            useCaseCn = "具有加法显示优化的原生眼镜 UI"
        ),
        DecisionNode(
            id = "phone",
            question = "Do you need to share UI with connected glasses?",
            questionCn = "您需要与连接的眼镜共享 UI 吗？",
            options = listOf(
                DecisionOption("Yes — Project UI to glasses / 是", "是", "project"),
                DecisionOption("No — Phone only / 否", "否", "phone_only")
            ),
            recommendation = "Use Projected API to share existing phone UI to glasses",
            recommendationCn = "使用 Projected API 将现有手机 UI 共享到眼镜",
            useCase = "Minimal duplication — one codebase, two displays",
            useCaseCn = "最小化重复——一个代码库，两个显示器"
        ),
        DecisionNode(
            id = "project",
            question = "Is your phone UI already Compose-based?",
            questionCn = "您的手机 UI 已经是基于 Compose 的吗？",
            options = listOf(
                DecisionOption("✅ Yes, Compose / 是", "是", "compose"),
                DecisionOption("❌ No, Views / 否", "否", "views")
            ),
            recommendation = "Use Glimmer + Projected — minimal changes to existing Compose UI",
            recommendationCn = "使用 Glimmer + Projected——对现有 Compose UI 进行最小更改",
            useCase = "Glimmer wraps your Compose UI with additive display optimization",
            useCaseCn = "Glimmer 使用加法显示优化包装您的 Compose UI"
        )
    )

    private fun buildCIComplianceRules(): List<CIComplianceRule> = listOf(
        CIComplianceRule(
            id = "ci_rule_1",
            ruleId = "GLIMMER-001",
            title = "No MaterialTheme usage in Glimmer composables",
            titleCn = "Glimmer composables 中不得使用 MaterialTheme",
            description = "MaterialTheme must not appear in any file under the glimmer/ directory.",
            descriptionCn = "MaterialTheme 不得出现在 glimmer/ 目录下的任何文件中。",
            checkCommand = "grep -r \"MaterialTheme\" app/src/main/java/**/glimmer/ || echo \"PASS\"",
            fixCommand = "Replace MaterialTheme with GlimmerTheme and convert colors to brightness percentages",
            severity = CompatibilityRiskLevel.CRITICAL
        ),
        CIComplianceRule(
            id = "ci_rule_2",
            ruleId = "GLIMMER-002",
            title = "No elevation/shadow usage in Glimmer composables",
            titleCn = "Glimmer composables 中不得使用 elevation/阴影",
            description = "Card shadows and elevation are incompatible with additive OLED displays.",
            descriptionCn = "Card 阴影和 elevation 与 OLED 加法显示器不兼容。",
            checkCommand = "grep -rE \"elevation|shadow\" app/src/main/java/**/glimmer/ || echo \"PASS\"",
            fixCommand = "Replace elevation with brightness level: .brightness(0.8f) for near, .brightness(0.5f) for far",
            severity = CompatibilityRiskLevel.HIGH
        ),
        CIComplianceRule(
            id = "ci_rule_3",
            ruleId = "GLIMMER-003",
            title = "Glimmer composables must use Glimmer-prefixed types",
            titleCn = "Glimmer composables 必须使用 Glimmer 前缀类型",
            description = "All UI composables in glimmer/ must use GlimmerText, GlimmerSurface, GlimmerButton, etc.",
            descriptionCn = "glimmer/ 目录下所有 UI composables 必须使用 GlimmerText、GlimmerSurface、GlimmerButton 等。",
            checkCommand = "grep -rE \"^(fun |class )[A-Z]\" app/src/main/java/**/glimmer/ | grep -v Glimmer || echo \"PASS\"",
            fixCommand = "Replace all Compose UI types with their Glimmer equivalents",
            severity = CompatibilityRiskLevel.HIGH
        ),
        CIComplianceRule(
            id = "ci_rule_4",
            ruleId = "GLIMMER-004",
            title = "No continuous animations in Glimmer composables",
            titleCn = "Glimmer composables 中不得使用持续动画",
            description = "AnimatedVisibility and animateFloatAsState keep pixels constant-state, causing battery drain.",
            descriptionCn = "AnimatedVisibility 和 animateFloatAsState 保持像素恒定状态，导致电池消耗。",
            checkCommand = "grep -rE \"AnimatedVisibility|animateFloatAsState\" app/src/main/java/**/glimmer/ || echo \"PASS\"",
            fixCommand = "Replace with GlimmerFade / GlimmerSlide / GlimmerPulse (burst-based animations)",
            severity = CompatibilityRiskLevel.MEDIUM
        ),
        CIComplianceRule(
            id = "ci_rule_5",
            ruleId = "GLIMMER-005",
            title = "Background must be explicitly set to Color.Black",
            titleCn = "背景必须显式设置为 Color.Black",
            description = "GlimmerSurface and GlimmerCanvas must have explicit black background for transparency.",
            descriptionCn = "GlimmerSurface 和 GlimmerCanvas 必须有明确的黑色背景以实现透明效果。",
            checkCommand = "grep -r \"background\" app/src/main/java/**/glimmer/ | grep -v \"Color.Black\" || echo \"PASS\"",
            fixCommand = "Add backgroundColor = Color.Black to all GlimmerSurface and GlimmerCanvas composables",
            severity = CompatibilityRiskLevel.MEDIUM
        ),
        CIComplianceRule(
            id = "ci_rule_6",
            ruleId = "GLIMMER-006",
            title = "Brightness values must be in valid range [0.0, 1.0]",
            titleCn = "亮度值必须在有效范围内 [0.0, 1.0]",
            description = "brightness parameter must be validated to prevent out-of-range values causing display artifacts.",
            descriptionCn = "brightness 参数必须经过验证以防止超出范围的值导致显示伪影。",
            checkCommand = "grep -rE \"brightness\\s*=\\s*[+-]?[0-9]+(\\.[0-9]+)?\" app/src/main/java/**/glimmer/",
            fixCommand = "Clamp all brightness values with .coerceIn(0f, 1f)",
            severity = CompatibilityRiskLevel.LOW
        )
    )

    private fun buildIO2026Sessions(): List<IO2026Session> = listOf(
        IO2026Session(
            id = "io_1",
            title = "Keynote: The Future of Android",
            titleCn = "主题演讲：Android 的未来",
            category = "Keynote",
            categoryCn = "主题演讲",
            expectedDate = "May 12, 2026",
            description = "Major Android platform announcements, AI integration roadmap, and next-gen developer tools",
            descriptionCn = "主要 Android 平台公告、AI 集成路线图和下一代开发者工具",
            likelihood = IOLikelihood.CONFIRMED,
            relatedPRD = null
        ),
        IO2026Session(
            id = "io_2",
            title = "Jetpack Glimmer: Production-Ready AI Glasses UI",
            titleCn = "Jetpack Glimmer：生产就绪的 AI 眼镜 UI",
            category = "Jetpack",
            categoryCn = "Jetpack",
            expectedDate = "May 12, 2026",
            description = "Glimmer 1.0 stable release, migration tools from Compose, and case studies",
            descriptionCn = "Glimmer 1.0 稳定版发布、从 Compose 的迁移工具以及案例研究",
            likelihood = IOLikelihood.HIGHLY_LIKELY,
            relatedPRD = "PRD-230"
        ),
        IO2026Session(
            id = "io_3",
            title = "Projected API: Seamless Phone-to-Glasses UX",
            titleCn = "Projected API：无缝手机到眼镜的 UX",
            category = "Jetpack",
            categoryCn = "Jetpack",
            expectedDate = "May 12, 2026",
            description = "Deep dive into Projected API for sharing UI between phone and glasses devices",
            descriptionCn = "深入了解用于在手机和眼镜设备之间共享 UI 的 Projected API",
            likelihood = IOLikelihood.HIGHLY_LIKELY,
            relatedPRD = "PRD-230"
        ),
        IO2026Session(
            id = "io_4",
            title = "Kotlin Multiplatform for Android Developers",
            titleCn = "面向 Android 开发者的 Kotlin 多平台",
            category = "Kotlin",
            categoryCn = "Kotlin",
            expectedDate = "May 13, 2026",
            description = "KMP production readiness updates, new expect/actual patterns, and shared UI strategies",
            descriptionCn = "KMP 生产就绪更新、新 expect/actual 模式和共享 UI 策略",
            likelihood = IOLikelihood.CONFIRMED,
            relatedPRD = null
        ),
        IO2026Session(
            id = "io_5",
            title = "Gemini on Android: AICore and On-Device AI",
            titleCn = "Android 上的 Gemini：AICore 和设备端 AI",
            category = "AI/ML",
            categoryCn = "AI/ML",
            expectedDate = "May 13, 2026",
            description = "AICore API improvements, on-device model optimization, and Glasses AI integration patterns",
            descriptionCn = "AICore API 改进、设备端模型优化和 Glasses AI 集成模式",
            likelihood = IOLikelihood.CONFIRMED,
            relatedPRD = null
        ),
        IO2026Session(
            id = "io_6",
            title = "Wear OS 5: Glimmer-First Design",
            titleCn = "Wear OS 5：Glimmer-First 设计",
            category = "Wear OS",
            categoryCn = "Wear OS",
            expectedDate = "May 13, 2026",
            description = "Wear OS 5 adopts Glimmer as the recommended UI toolkit for new watch faces and apps",
            descriptionCn = "Wear OS 5 采用 Glimmer 作为新手表界面和应用推荐的 UI 工具包",
            likelihood = IOLikelihood.LIKELY,
            relatedPRD = "PRD-230"
        ),
        IO2026Session(
            id = "io_7",
            title = "Android 18 Developer Preview",
            titleCn = "Android 18 开发者预览",
            category = "Platform",
            categoryCn = "平台",
            expectedDate = "May 12, 2026",
            description = "First developer preview of Android 18 with new Glimmer-native APIs and developer tools",
            descriptionCn = "Android 18 首个开发者预览版，包含新的 Glimmer 原生 API 和开发者工具",
            likelihood = IOLikelihood.LIKELY,
            relatedPRD = null
        ),
        IO2026Session(
            id = "io_8",
            title = "Compose 2.0: Glimmer-Compatible Architecture",
            titleCn = "Compose 2.0：Glimmer 兼容架构",
            category = "Compose",
            categoryCn = "Compose",
            expectedDate = "May 14, 2026",
            description = "Compose 2.0 introduces Glimmer compatibility mode and unified UI model for phone, watch, and glasses",
            descriptionCn = "Compose 2.0 引入 Glimmer 兼容模式和用于手机、手表和眼镜的统一 UI 模型",
            likelihood = IOLikelihood.SPECULATIVE,
            relatedPRD = "PRD-230"
        )
    )

    private fun computeIO2026Countdown(): Long {
        val ioDate = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, 12, 10, 0, 0)
        }
        val now = Calendar.getInstance()
        val diffMs = ioDate.timeInMillis - now.timeInMillis
        return maxOf(0, diffMs / (1000 * 60 * 60 * 24))
    }
}
