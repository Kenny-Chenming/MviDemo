package com.mvi.kenny.feature.aluminiumosdesktop

// ================================================================
// AluminiumOSDesktopViewModel — Aluminium OS 桌面适配工具包 ViewModel
// ================================================================
// MVI ViewModel for Aluminium OS desktop adaptation toolkit.
//
// PRD-227: Aluminium OS Android App 桌面适配工具包
// Design Reference: memory/agency/designs/PRD-227-Aluminium-OS-桌面适配工具包.md
//
// Responsibilities:
//   - Tab 0: Scan AndroidManifest.xml and source code for desktop compatibility issues
//   - Tab 1: Display UI modification checklist for phone→desktop adaptation
//   - Tab 2: Provide keyboard/mouse adaptation code examples (Compose + View)
//   - Tab 3: Guide Gemini/AICore integration for Aluminium OS
//   - Tab 4: Guide Play Store desktop optimization label application
//
// MVI Pattern: State (UI) + Intent (User Action) + Effect (One-time events)
// Bilingual comments: CN + EN
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ============================================================
 * AluminiumOSDesktopViewModel — MVI ViewModel
 * ============================================================
 * Manages state for all 5 tabs, processes user intents, emits effects.
 */
class AluminiumOSDesktopViewModel : ViewModel() {

    // ── MVI State ──
    private val _state = MutableStateFlow(AluminiumOSDesktopState())
    val state: StateFlow<AluminiumOSDesktopState> = _state.asStateFlow()

    // ── MVI Effects (one-time side effects via Channel) ──
    private val _effects = Channel<AluminiumOSDesktopEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        // Initialize static data for Tabs 1-4 on creation
        // 初始化 Tab 1-4 的静态数据
        _state.value = _state.value.copy(
            checklistItems = buildChecklistItems(),
            keyboardGuides = buildKeyboardGuides(),
            geminiGuides = buildGeminiGuides(),
            playStoreGuides = buildPlayStoreGuides()
        )
    }

    /** Process user intent — main entry point for all user actions */
    fun processIntent(intent: AluminiumOSDesktopIntent) {
        when (intent) {
            is AluminiumOSDesktopIntent.SelectTab -> selectTab(intent.index)
            is AluminiumOSDesktopIntent.UpdateSourceDir -> updateSourceDir(intent.dir)
            is AluminiumOSDesktopIntent.UpdatePackageName -> updatePackageName(intent.packageName)
            is AluminiumOSDesktopIntent.StartScan -> startScan()
            is AluminiumOSDesktopIntent.CancelScan -> cancelScan()
            is AluminiumOSDesktopIntent.ClearScanResults -> clearScanResults()
            is AluminiumOSDesktopIntent.SelectIssue -> selectIssue(intent.issue)
            is AluminiumOSDesktopIntent.ToggleChecklistItem -> toggleChecklistItem(intent.id)
            is AluminiumOSDesktopIntent.ExpandChecklistItem -> expandChecklistItem(intent.id)
            is AluminiumOSDesktopIntent.SelectFramework -> selectFramework(intent.framework)
            is AluminiumOSDesktopIntent.ExpandKeyboardGuide -> expandKeyboardGuide(intent.id)
            is AluminiumOSDesktopIntent.ExpandGeminiGuide -> expandGeminiGuide(intent.id)
            is AluminiumOSDesktopIntent.TogglePlayStoreGuide -> togglePlayStoreGuide(intent.id)
            is AluminiumOSDesktopIntent.ExpandPlayStoreGuide -> expandPlayStoreGuide(intent.id)
        }
    }

    // ══════════════════════════════════════════════════════
    // Tab Navigation
    // ══════════════════════════════════════════════════════

    private fun selectTab(index: Int) {
        _state.value = _state.value.copy(selectedTab = index)
    }

    // ══════════════════════════════════════════════════════
    // Tab 0: Compatibility Scanner
    // ══════════════════════════════════════════════════════

    private fun updateSourceDir(dir: String) {
        _state.value = _state.value.copy(projectSourceDir = dir)
    }

    private fun updatePackageName(packageName: String) {
        _state.value = _state.value.copy(packageName = packageName)
    }

    private fun clearScanResults() {
        _state.value = _state.value.copy(
            scanState = CompatibilityScanState(),
            projectSourceDir = "",
            packageName = ""
        )
    }

    private fun selectIssue(issue: CompatibilityIssue?) {
        _state.value = _state.value.copy(
            scanState = _state.value.scanState.copy(selectedIssue = issue)
        )
    }

    private fun cancelScan() {
        _state.value = _state.value.copy(
            scanState = _state.value.scanState.copy(
                phase = ScanPhase.IDLE,
                progress = 0
            )
        )
    }

    private fun startScan() {
        val currentState = _state.value
        if (currentState.projectSourceDir.isBlank() && currentState.packageName.isBlank()) {
            sendEffect(AluminiumOSDesktopEffect.ShowToast("Please enter source directory or package name"))
            return
        }

        _state.value = _state.value.copy(
            scanState = CompatibilityScanState(
                phase = ScanPhase.SCANNING_MANIFEST,
                progress = 0,
                startTime = System.currentTimeMillis()
            )
        )

        viewModelScope.launch {
            try {
                val issues = runCompatibilityScan()
                val endTime = System.currentTimeMillis()

                _state.value = _state.value.copy(
                    scanState = _state.value.scanState.copy(
                        phase = ScanPhase.COMPLETED,
                        progress = 100,
                        issues = issues,
                        scannedFilesCount = issues.size,
                        endTime = endTime
                    )
                )

                val p0 = issues.count { it.riskLevel == RiskLevel.P0 }
                val p1 = issues.count { it.riskLevel == RiskLevel.P1 }
                val p2 = issues.count { it.riskLevel == RiskLevel.P2 }
                val p3 = issues.count { it.riskLevel == RiskLevel.P3 }

                sendEffect(
                    AluminiumOSDesktopEffect.ScanCompleted(
                        total = issues.size,
                        p0 = p0, p1 = p1, p2 = p2, p3 = p3,
                        durationMs = endTime - _state.value.scanState.startTime
                    )
                )
                sendEffect(AluminiumOSDesktopEffect.ShowToast("Scan complete: ${issues.size} issues found"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    scanState = _state.value.scanState.copy(
                        phase = ScanPhase.ERROR,
                        errorMessage = e.message ?: "Unknown scan error",
                        endTime = System.currentTimeMillis()
                    )
                )
                sendEffect(AluminiumOSDesktopEffect.Error("Scan failed: ${e.message}"))
            }
        }
    }

    /**
     * Run compatibility scan on the given directory/package
     * 执行兼容性扫描
     *
     * In production: scan AndroidManifest.xml + Kotlin/Java source files
     * 生产环境：扫描 AndroidManifest.xml + Kotlin/Java 源文件
     * Demo mode: returns curated demo issues
     * Demo 模式：返回预设的演示问题
     */
    private suspend fun runCompatibilityScan(): List<CompatibilityIssue> =
        withContext(Dispatchers.IO) {
            val sourceDir = _state.value.projectSourceDir
            val issues = mutableListOf<CompatibilityIssue>()

            // Phase 1: Scan manifest (simulated)
            // 阶段 1：扫描 manifest（模拟）
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(
                    phase = ScanPhase.SCANNING_MANIFEST,
                    progress = 20
                )
            )
            delay(500)

            // Phase 2: Scan Kotlin files
            // 阶段 2：扫描 Kotlin 文件
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(
                    phase = ScanPhase.SCANNING_KOTLIN,
                    progress = 50
                )
            )
            delay(800)

            // Phase 3: Scan Java files
            // 阶段 3：扫描 Java 文件
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(
                    phase = ScanPhase.SCANNING_JAVA,
                    progress = 70
                )
            )
            delay(500)

            // Phase 4: Analyze
            // 阶段 4：分析
            _state.value = _state.value.copy(
                scanState = _state.value.scanState.copy(phase = ScanPhase.ANALYZING, progress = 85)
            )
            delay(300)

            // Demo data: realistic Aluminium OS compatibility issues
            // 演示数据：真实的 Aluminium OS 兼容性问题
            issues.addAll(buildDemoIssues(sourceDir))
            issues  // Return the list / 返回列表
        }

    /** Build demo issues for demonstration mode */
    private fun buildDemoIssues(sourceDir: String): List<CompatibilityIssue> {
        val base = if (sourceDir.isNotBlank()) sourceDir else "~/MyAndroidApp/src/main"
        return listOf(
            CompatibilityIssue(
                id = "al-001",
                file = "$base/AndroidManifest.xml",
                line = 12,
                issueType = IssueType.FIXED_ORIENTATION,
                riskLevel = RiskLevel.P0,
                description = "android:screenOrientation=\"portrait\" forces portrait mode on desktop",
                descriptionCn = "android:screenOrientation=\"portrait\" 强制竖屏，桌面模式下无法自适应",
                suggestion = "Remove screenOrientation or use \"unspecified\" for automatic adaptation",
                suggestionCn = "移除 screenOrientation 或使用 \"unspecified\" 以自动适应",
                beforeCode = """<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait" />""",
                afterCode = """<activity
    android:name=".MainActivity"
    android:screenOrientation="unspecified"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden" />"""
            ),
            CompatibilityIssue(
                id = "al-002",
                file = "$base/AndroidManifest.xml",
                line = 8,
                issueType = IssueType.RESIZE_DISABLED,
                riskLevel = RiskLevel.P0,
                description = "android:resizeableActivity=\"false\" disables window resizing on desktop",
                descriptionCn = "android:resizeableActivity=\"false\" 禁用了桌面端窗口调整大小功能",
                suggestion = "Set android:resizeableActivity=\"true\" and handle configuration changes",
                suggestionCn = "设置 android:resizeableActivity=\"true\" 并处理配置变更",
                beforeCode = """<activity
    android:name=".MainActivity"
    android:resizeableActivity="false" />""",
                afterCode = """<activity
    android:name=".MainActivity"
    android:resizeableActivity="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />"""
            ),
            CompatibilityIssue(
                id = "al-003",
                file = "$base/java/com/example/app/ui/MainActivity.kt",
                line = 45,
                issueType = IssueType.TOUCH_DEPENDENCY,
                riskLevel = RiskLevel.P1,
                description = "onTouchEvent used without keyboard alternative — desktop users cannot interact",
                descriptionCn = "onTouchEvent 使用但无键盘替代方案 — 桌面用户无法操作",
                suggestion = "Implement onKeyDown/keyPressed equivalents for all touch interactions",
                suggestionCn = "为所有触摸交互实现 onKeyDown/keyPressed 等效替代",
                beforeCode = """view.setOnTouchListener { v, event ->
    // Handle tap gesture
    handleTap(event.x, event.y)
    true
}""",
                afterCode = """view.setOnTouchListener { v, event ->
    handleTap(event.x, event.y)
    true
}
// Add keyboard navigation support
view.setOnKeyListener { v, keyCode, event ->
    if (event.action == KeyEvent.ACTION_DOWN) {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> { handleTap-focused(); true }
            KeyEvent.KEYCODE_DPAD_CENTER -> { handleTap-focused(); true }
            else -> false
        }
    } else false
}"""
            ),
            CompatibilityIssue(
                id = "al-004",
                file = "$base/java/com/example/app/ui/MainActivity.kt",
                line = 78,
                issueType = IssueType.NO_KEYBOARD_NAV,
                riskLevel = RiskLevel.P1,
                description = "Custom view with no focusable elements — keyboard users cannot navigate",
                descriptionCn = "自定义 View 无可聚焦元素 — 键盘用户无法导航",
                suggestion = "Add android:focusable=\"true\" and proper focus order (focusUp/Down/Left/Right)",
                suggestionCn = "添加 android:focusable=\"true\" 和正确的焦点顺序",
                beforeCode = """<CustomView
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />""",
                afterCode = """<CustomView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:focusable="true"
    android:focusableInTouchMode="true"
    android:nextFocusUp="@id/btn_previous"
    android:nextFocusDown="@id/btn_next" />"""
            ),
            CompatibilityIssue(
                id = "al-005",
                file = "$base/java/com/example/app/ui/MainActivity.kt",
                line = 102,
                issueType = IssueType.RIGHT_CLICK_MISSING,
                riskLevel = RiskLevel.P2,
                description = "No right-click (context menu) implementation — desktop users expect context menus",
                descriptionCn = "无右键（上下文菜单）实现 — 桌面用户期望上下文菜单",
                suggestion = "Implement onContextMenu or ActionMode for desktop context interactions",
                suggestionCn = "实现 onContextMenu 或 ActionMode 以支持桌面上下文交互",
                beforeCode = """// No right-click handling
button.setOnClickListener { toast("Clicked!") }""",
                afterCode = """button.setOnClickListener { toast("Clicked!") }
// Right-click context menu
button.setOnCreateContextMenuListener { menu, v, menuInfo ->
    menu.add(0, R.id.action_copy, 0, "Copy")
    menu.add(0, R.id.action_share, 0, "Share")
}"""
            ),
            CompatibilityIssue(
                id = "al-006",
                file = "$base/java/com/example/app/gallery/PhotoViewer.kt",
                line = 55,
                issueType = IssueType.ASPECT_RATIO_FIXED,
                riskLevel = RiskLevel.P2,
                description = "Fixed aspect ratio assumption (16:9) breaks in freeform desktop windows",
                descriptionCn = "固定宽高比假设（16:9）在自由窗口模式下会破坏布局",
                suggestion = "Use layout_weight and ConstraintLayout for flexible aspect ratios",
                suggestionCn = "使用 layout_weight 和 ConstraintLayout 实现灵活的宽高比",
                beforeCode = """// Hardcoded 16:9 assumption
val aspectRatio = 16f / 9f
imageView.layoutParams = FixedAspectRatioLayoutParams(width, width / aspectRatio)""",
                afterCode = """// Responsive layout using ConstraintLayout
// width:0dp + match_constraint + bias gives flexible sizing
// imageView adjusts to window size automatically"""
            ),
            CompatibilityIssue(
                id = "al-007",
                file = "$base/java/com/example/app/MainActivity.kt",
                line = 30,
                issueType = IssueType.NO_SHORTCUT,
                riskLevel = RiskLevel.P3,
                description = "No keyboard shortcuts implemented — desktop power users rely on shortcuts",
                descriptionCn = "未实现键盘快捷键 — 桌面高级用户依赖快捷键",
                suggestion = "Add common shortcuts: Ctrl+S (save), Ctrl+N (new), Ctrl+F (find), Esc (cancel)",
                suggestionCn = "添加常用快捷键：Ctrl+S（保存）、Ctrl+N（新建）、Ctrl+F（查找）、Esc（取消）",
                beforeCode = """// No keyboard shortcuts""",
                afterCode = """override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return when {
        event.isCtrlPressed -> when (keyCode) {
            KeyEvent.KEYCODE_S -> { saveDocument(); true }
            KeyEvent.KEYCODE_N -> { createNew(); true }
            KeyEvent.KEYCODE_F -> { showSearch(); true }
            else -> super.onKeyDown(keyCode, event)
        }
        keyCode == KeyEvent.KEYCODE_ESCAPE -> { finish(); true }
        else -> super.onKeyDown(keyCode, event)
    }
}"""
            ),
            CompatibilityIssue(
                id = "al-008",
                file = "$base/java/com/example/app/ui/DragItem.kt",
                line = 88,
                issueType = IssueType.DRAG_DROP_MISSING,
                riskLevel = RiskLevel.P1,
                description = "No drag-and-drop support — desktop users expect drag-and-drop file handling",
                descriptionCn = "无拖放支持 — 桌面用户期望拖放文件处理",
                suggestion = "Implement View.OnDragListener and DragEvent for file drop support",
                suggestionCn = "实现 View.OnDragListener 和 DragEvent 以支持文件拖放",
                beforeCode = """// No drag-drop support
// User must use file picker (less intuitive on desktop)""",
                afterCode = """view.setOnDragListener { v, event ->
    when (event.action) {
        DragEvent.ACTION_DROP -> {
            val clipData = event.clipData ?: return@setOnDragListener false
            // Handle dropped files
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                handleDroppedFile(uri)
            }
            true
        }
        DragEvent.ACTION_DRAG_STARTED -> true
        DragEvent.ACTION_DRAG_ENTERED -> { v.alpha = 0.7f; true }
        DragEvent.ACTION_DRAG_EXITED -> { v.alpha = 1.0f; true }
        DragEvent.ACTION_DRAG_ENDED -> { v.alpha = 1.0f; true }
        else -> false
    }
}"""
            )
        )
    }

    // ══════════════════════════════════════════════════════
    // Tab 1: UI Modification Checklist
    // ══════════════════════════════════════════════════════

    private fun toggleChecklistItem(id: String) {
        val items = _state.value.checklistItems.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _state.value = _state.value.copy(checklistItems = items)
    }

    private fun expandChecklistItem(id: String?) {
        _state.value = _state.value.copy(expandedChecklistItem = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 2: Keyboard/Mouse Adaptation
    // ══════════════════════════════════════════════════════

    private fun selectFramework(framework: String) {
        _state.value = _state.value.copy(selectedFramework = framework)
    }

    private fun expandKeyboardGuide(id: String?) {
        _state.value = _state.value.copy(expandedKeyboardGuide = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 3: Gemini AI Integration
    // ══════════════════════════════════════════════════════

    private fun expandGeminiGuide(id: String?) {
        _state.value = _state.value.copy(expandedGeminiGuide = id)
    }

    // ══════════════════════════════════════════════════════
    // Tab 4: Play Store Optimization
    // ══════════════════════════════════════════════════════

    private fun togglePlayStoreGuide(id: String) {
        val guides = _state.value.playStoreGuides.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _state.value = _state.value.copy(playStoreGuides = guides)
    }

    private fun expandPlayStoreGuide(id: String?) {
        _state.value = _state.value.copy(expandedPlayStoreGuide = id)
    }

    // ══════════════════════════════════════════════════════
    // Static Data Builders
    // ══════════════════════════════════════════════════════

    /** Build Tab 1: UI modification checklist items */
    private fun buildChecklistItems(): List<UIModificationItem> = listOf(
        // Window Management
        UIModificationItem(
            id = "ck-wn-001",
            category = UIModCategory.WINDOW_MANAGEMENT,
            title = "Enable resizableActivity in AndroidManifest",
            titleCn = "在 AndroidManifest 中启用 resizableActivity",
            description = "Set android:resizeableActivity=\"true\" to allow freeform windows",
            descriptionCn = "设置 android:resizeableActivity=\"true\" 以允许自由窗口",
            codeExample = """<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:resizeableActivity="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation|keyboard|keyboardHidden" />""",
            codeExampleCn = """<!-- AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:resizeableActivity="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation|keyboard|keyboardHidden" />""",
            priority = RiskLevel.P0
        ),
        UIModificationItem(
            id = "ck-wn-002",
            category = UIModCategory.WINDOW_MANAGEMENT,
            title = "Handle onConfigurationChanged for window resize",
            titleCn = "处理 onConfigurationChanged 窗口大小变更",
            description = "React to window size changes via onConfigurationChanged or Jetpack WindowManager",
            descriptionCn = "通过 onConfigurationChanged 或 Jetpack WindowManager 响应窗口大小变化",
            codeExample = """// In Activity
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val windowMetrics = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(this)
    val bounds = windowMetrics.bounds
    // Re-layout based on new bounds
    adaptLayoutToWindow(bounds.width(), bounds.height())
}""",
            codeExampleCn = """// 在 Activity 中
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val windowMetrics = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(this)
    val bounds = windowMetrics.bounds
    // 根据新的边界重新布局
    adaptLayoutToWindow(bounds.width(), bounds.height())
}""",
            priority = RiskLevel.P0
        ),
        // Layout Adjustment
        UIModificationItem(
            id = "ck-la-001",
            category = UIModCategory.LAYOUT_ADJUSTMENT,
            title = "Use WindowSizeClass for adaptive layouts",
            titleCn = "使用 WindowSizeClass 实现自适应布局",
            description = "Use Material3 WindowSizeClass to switch between compact/medium/expanded layouts",
            descriptionCn = "使用 Material3 WindowSizeClass 在 compact/medium/expanded 布局间切换",
            codeExample = """val windowSizeClass = calculateWindowSizeClass(this)
when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.COMPACT -> CompactLayout()
    WindowWidthSizeClass.MEDIUM -> MediumLayout()
    WindowWidthSizeClass.EXPANDED -> ExpandedLayout()
}""",
            codeExampleCn = """val windowSizeClass = calculateWindowSizeClass(this)
when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.COMPACT -> CompactLayout()
    WindowWidthSizeClass.MEDIUM -> MediumLayout()
    WindowWidthSizeClass.EXPANDED -> ExpandedLayout()
}""",
            priority = RiskLevel.P1
        ),
        UIModificationItem(
            id = "ck-la-002",
            category = UIModCategory.LAYOUT_ADJUSTMENT,
            title = "Avoid hardcoded pixel dimensions",
            titleCn = "避免硬编码像素尺寸",
            description = "Use dp/sp units and ConstraintLayout percentages instead of hardcoded px values",
            descriptionCn = "使用 dp/sp 单位而非硬编码 px 值，用 ConstraintLayout 百分比替代固定尺寸",
            priority = RiskLevel.P1
        ),
        // Desktop Windowing
        UIModificationItem(
            id = "ck-dw-001",
            category = UIModCategory.DESKTOP_WINDOWING,
            title = "Declare Desktop Windowing support in build config",
            titleCn = "在构建配置中声明 Desktop Windowing 支持",
            description = "In gradle.properties: set DEPLOYMENT_TARGET_android17=true and declare support",
            descriptionCn = "在 gradle.properties 中设置 DEPLOYMENT_TARGET_android17=true 并声明支持",
            codeExample = """# gradle.properties
android.app.desktop.windowing.mode=freeform
android.app.desktop.windowing.minWidthDp=320
android.app.desktop.windowing.minHeightDp=240""",
            codeExampleCn = """# gradle.properties
android.app.desktop.windowing.mode=freeform
android.app.desktop.windowing.minWidthDp=320
android.app.desktop.windowing.minHeightDp=240""",
            priority = RiskLevel.P1
        ),
        // Interaction Mode
        UIModificationItem(
            id = "ck-im-001",
            category = UIModCategory.INTERACTION_MODE,
            title = "Add keyboard navigation support",
            titleCn = "添加键盘导航支持",
            description = "Make all interactive elements focusable and define focus order",
            descriptionCn = "使所有交互元素可聚焦，并定义焦点顺序",
            priority = RiskLevel.P1
        ),
        UIModificationItem(
            id = "ck-im-002",
            category = UIModCategory.INTERACTION_MODE,
            title = "Implement keyboard shortcuts",
            titleCn = "实现键盘快捷键",
            description = "Override onKeyDown to handle Ctrl+key and function key shortcuts",
            descriptionCn = "重写 onKeyDown 处理 Ctrl+键和功能键快捷键",
            priority = RiskLevel.P2
        ),
        // Orientation Support
        UIModificationItem(
            id = "ck-or-001",
            category = UIModCategory.ORIENTATION_SUPPORT,
            title = "Remove fixed orientation restrictions",
            titleCn = "移除固定方向限制",
            description = "Remove android:screenOrientation from manifest or set to \"unspecified\"",
            descriptionCn = "从 manifest 中移除 android:screenOrientation 或设置为 \"unspecified\"",
            priority = RiskLevel.P0
        ),
        // Multi-Window
        UIModificationItem(
            id = "ck-mw-001",
            category = UIModCategory.MULTI_WINDOW,
            title = "Handle multi-window mode correctly",
            titleCn = "正确处理多窗口模式",
            description = "Test and adapt layouts for split-screen and freeform multi-window scenarios",
            descriptionCn = "测试并适配分屏和自由形式多窗口场景的布局",
            priority = RiskLevel.P2
        )
    )

    /** Build Tab 2: Keyboard/mouse adaptation guides */
    private fun buildKeyboardGuides(): List<KeyboardMouseGuide> = listOf(
        KeyboardMouseGuide(
            id = "kb-001",
            title = "Keyboard Navigation with FocusOrder",
            titleCn = "键盘导航与焦点顺序",
            description = "Define explicit focus order using android:nextFocusUp/Down/Left/Right",
            descriptionCn = "使用 android:nextFocusUp/Down/Left/Right 定义显式焦点顺序",
            category = KeyboardCategory.KEYBOARD_NAV,
            codeExample = """<!-- XML layout -->
<Button
    android:id="@+id/btn_back"
    android:nextFocusUp="@id/btn_home"
    android:nextFocusDown="@id/btn_next"
    android:focusable="true" />

<Button
    android:id="@+id/btn_home"
    android:nextFocusUp="@id/btn_settings"
    android:nextFocusDown="@id/btn_back" />""",
            codeExampleCn = """<!-- XML 布局 -->
<Button
    android:id="@+id/btn_back"
    android:nextFocusUp="@id/btn_home"
    android:nextFocusDown="@id/btn_next"
    android:focusable="true" />

<Button
    android:id="@+id/btn_home"
    android:nextFocusUp="@id/btn_settings"
    android:nextFocusDown="@id/btn_back" />""",
            framework = "View"
        ),
        KeyboardMouseGuide(
            id = "kb-002",
            title = "Compose: Keyboard Navigation with FocusRequester",
            titleCn = "Compose：使用 FocusRequester 的键盘导航",
            description = "Use FocusRequester and Modifier.focusOrder() for Compose keyboard nav",
            descriptionCn = "使用 FocusRequester 和 Modifier.focusOrder() 实现 Compose 键盘导航",
            category = KeyboardCategory.KEYBOARD_NAV,
            codeExample = """@Composable
fun KeyboardNavDemo() {
    val (first, second, third) = remember { FocusRequester.createRefs() }

    Column {
        TextButton(
            modifier = Modifier
                .focusOrder(first)
                .focusable(),
            onClick = { first.requestFocus() }
        ) { Text("First") }

        TextButton(
            modifier = Modifier
                .focusOrder(second)
                .focusable(),
            onClick = { second.requestFocus() }
        ) { Text("Second") }

        TextButton(
            modifier = Modifier
                .focusOrder(third)
                .focusable(),
            onClick = { third.requestFocus() }
        ) { Text("Third") }
    }
}""",
            codeExampleCn = """@Composable
fun KeyboardNavDemo() {
    val (first, second, third) = remember { FocusRequester.createRefs() }

    Column {
        TextButton(
            modifier = Modifier
                .focusOrder(first)
                .focusable(),
            onClick = { first.requestFocus() }
        ) { Text("第一个") }

        TextButton(
            modifier = Modifier
                .focusOrder(second)
                .focusable(),
            onClick = { second.requestFocus() }
        ) { Text("第二个") }

        TextButton(
            modifier = Modifier
                .focusOrder(third)
                .focusable(),
            onClick = { third.requestFocus() }
        ) { Text("第三个") }
    }
}""",
            framework = "Compose"
        ),
        KeyboardMouseGuide(
            id = "kb-003",
            title = "Right-Click Context Menu (View system)",
            titleCn = "右键上下文菜单（View 系统）",
            description = "Register OnCreateContextMenuListener to show context menu on right-click",
            descriptionCn = "注册 OnCreateContextMenuListener 在右键时显示上下文菜单",
            category = KeyboardCategory.RIGHT_CLICK_MENU,
            codeExample = """// In Activity or Fragment
view.setOnCreateContextMenuListener { menu, v, menuInfo ->
    menu.setHeaderTitle("Actions")
    menu.add(0, R.id.action_copy, 0, "Copy")
    menu.add(0, R.id.action_share, 0, "Share")
    menu.add(0, R.id.action_delete, 0, "Delete")
}

override fun onContextItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_copy -> { copyItem(); true }
        R.id.action_share -> { shareItem(); true }
        R.id.action_delete -> { deleteItem(); true }
        else -> super.onContextItemSelected(item)
    }
}""",
            codeExampleCn = """// 在 Activity 或 Fragment 中
view.setOnCreateContextMenuListener { menu, v, menuInfo ->
    menu.setHeaderTitle("操作")
    menu.add(0, R.id.action_copy, 0, "复制")
    menu.add(0, R.id.action_share, 0, "分享")
    menu.add(0, R.id.action_delete, 0, "删除")
}

override fun onContextItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_copy -> { copyItem(); true }
        R.id.action_share -> { shareItem(); true }
        R.id.action_delete -> { deleteItem(); true }
        else -> super.onContextItemSelected(item)
    }
}""",
            framework = "View"
        ),
        KeyboardMouseGuide(
            id = "kb-004",
            title = "Drag and Drop (View system)",
            titleCn = "拖放操作（View 系统）",
            description = "Implement OnDragListener to handle desktop drag-and-drop (file drops, reorder)",
            descriptionCn = "实现 OnDragListener 处理桌面拖放（文件拖放、重新排序）",
            category = KeyboardCategory.DRAG_DROP,
            codeExample = """val dropTarget = findViewById<View>(R.id.drop_zone)
dropTarget.setOnDragListener { v, event ->
    when (event.action) {
        DragEvent.ACTION_DRAG_STARTED -> true
        DragEvent.ACTION_DRAG_ENTERED -> {
            v.alpha = 0.7f
            v.setBackgroundColor(Color.LTGRAY)
            true
        }
        DragEvent.ACTION_DRAG_EXITED -> {
            v.alpha = 1.0f
            v.setBackgroundColor(Color.TRANSPARENT)
            true
        }
        DragEvent.ACTION_DROP -> {
            val clipData = event.clipData
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                handleFileDrop(uri)
            }
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {
            v.alpha = 1.0f
            true
        }
        else -> false
    }
}""",
            codeExampleCn = """val dropTarget = findViewById<View>(R.id.drop_zone)
dropTarget.setOnDragListener { v, event ->
    when (event.action) {
        DragEvent.ACTION_DRAG_STARTED -> true
        DragEvent.ACTION_DRAG_ENTERED -> {
            v.alpha = 0.7f
            v.setBackgroundColor(Color.LTGRAY)
            true
        }
        DragEvent.ACTION_DRAG_EXITED -> {
            v.alpha = 1.0f
            v.setBackgroundColor(Color.TRANSPARENT)
            true
        }
        DragEvent.ACTION_DROP -> {
            val clipData = event.clipData
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                handleFileDrop(uri)
            }
            true
        }
        DragEvent.ACTION_DRAG_ENDED -> {
            v.alpha = 1.0f
            true
        }
        else -> false
    }
}""",
            framework = "View"
        ),
        KeyboardMouseGuide(
            id = "kb-005",
            title = "Keyboard Shortcuts (View system)",
            titleCn = "键盘快捷键（View 系统）",
            description = "Override onKeyDown to handle Ctrl+S, Ctrl+N, Ctrl+F, Esc and more",
            descriptionCn = "重写 onKeyDown 处理 Ctrl+S、Ctrl+N、Ctrl+F、Esc 等快捷键",
            category = KeyboardCategory.SHORTCUT_KEYS,
            codeExample = """override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    if (!event.isCtrlPressed) return super.onKeyDown(keyCode, event)

    return when (keyCode) {
        KeyEvent.KEYCODE_S -> { saveDocument(); true }
        KeyEvent.KEYCODE_N -> { newDocument(); true }
        KeyEvent.KEYCODE_F -> { showSearch(); true }
        KeyEvent.KEYCODE_C -> { copySelection(); true }
        KeyEvent.KEYCODE_V -> { pasteClipboard(); true }
        KeyEvent.KEYCODE_Z -> { undo(); true }
        KeyEvent.KEYCODE_Y -> { redo(); true }
        else -> super.onKeyDown(keyCode, event)
    }
}""",
            codeExampleCn = """override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    if (!event.isCtrlPressed) return super.onKeyDown(keyCode, event)

    return when (keyCode) {
        KeyEvent.KEYCODE_S -> { saveDocument(); true }
        KeyEvent.KEYCODE_N -> { newDocument(); true }
        KeyEvent.KEYCODE_F -> { showSearch(); true }
        KeyEvent.KEYCODE_C -> { copySelection(); true }
        KeyEvent.KEYCODE_V -> { pasteClipboard(); true }
        KeyEvent.KEYCODE_Z -> { undo(); true }
        KeyEvent.KEYCODE_Y -> { redo(); true }
        else -> super.onKeyDown(keyCode, event)
    }
}""",
            framework = "View"
        ),
        KeyboardMouseGuide(
            id = "kb-006",
            title = "Compose: Context Menu with DropdownMenu",
            titleCn = "Compose：使用 DropdownMenu 的右键菜单",
            description = "Use DropdownMenu triggered by right-click (secondary pointer) in Compose",
            descriptionCn = "在 Compose 中使用由右键（辅助指针）触发的 DropdownMenu",
            category = KeyboardCategory.RIGHT_CLICK_MENU,
            codeExample = """@Composable
fun ContextMenuDemo() {
    var expanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val isSecondary = it == PointerInputScope.MouseInfopresent
                        if (isSecondary) {
                            contextMenuOffset = offset
                            expanded = true
                        }
                        null
                    }
                )
            },
        modifier = Modifier.size(200.dp)
    ) {
        Text("Right-click me")
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = Offset(contextMenuOffset.x, contextMenuOffset.y)
        ) {
            DropdownMenuItem(text = { Text("Copy") }, onClick = { copyItem() })
            DropdownMenuItem(text = { Text("Share") }, onClick = { shareItem() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { deleteItem() })
        }
    }
}""",
            codeExampleCn = """@Composable
fun ContextMenuDemo() {
    var expanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val isSecondary = it == PointerInputScope.MouseInfopresent
                        if (isSecondary) {
                            contextMenuOffset = offset
                            expanded = true
                        }
                        null
                    }
                )
            },
        modifier = Modifier.size(200.dp)
    ) {
        Text("右键点击我")
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = Offset(contextMenuOffset.x, contextMenuOffset.y)
        ) {
            DropdownMenuItem(text = { Text("复制") }, onClick = { copyItem() })
            DropdownMenuItem(text = { Text("分享") }, onClick = { shareItem() })
            DropdownMenuItem(text = { Text("删除") }, onClick = { deleteItem() })
        }
    }
}""",
            framework = "Compose"
        )
    )

    /** Build Tab 3: Gemini AI integration guides */
    private fun buildGeminiGuides(): List<GeminiIntegrationGuide> = listOf(
        GeminiIntegrationGuide(
            id = "gem-001",
            title = "AppFunctions API — On-Device AI (Recommended for Aluminium OS)",
            titleCn = "AppFunctions API — 设备端 AI（Aluminium OS 推荐方式）",
            description = "Use AppFunctions to access Gemini Nano/AICore running on device NPU",
            descriptionCn = "使用 AppFunctions 访问运行在设备 NPU 上的 Gemini Nano/AICore",
            codeExample = """// build.gradle.kts
dependencies {
    implementation("androidx.appfunctions:appfunctions-sdk:1.0.0")
}

// Declare in AndroidManifest.xml
// <queries>
//   <package android:name="com.google.android.appfunctions" />
// </queries>

// Usage
suspend fun invokeAISummarize(text: String): String {
    val result = AppFunctionCall.create("com.google.appfunctions.SUMMARIZE_TEXT")
        .addArgument("input_text", text)
        .setCallbackHandler { result ->
            result.getString("summary")
        }
        .execute()
    return result
}""",
            codeExampleCn = """// build.gradle.kts
dependencies {
    implementation("androidx.appfunctions:appfunctions-sdk:1.0.0")
}

// 在 AndroidManifest.xml 中声明
// <queries>
//   <package android:name="com.google.android.appfunctions" />
// </queries>

// 使用示例
suspend fun invokeAISummarize(text: String): String {
    val result = AppFunctionCall.create("com.google.appfunctions.SUMMARIZE_TEXT")
        .addArgument("input_text", text)
        .setCallbackHandler { result ->
            result.getString("summary")
        }
        .execute()
    return result
}""",
            integrationType = GeminiIntegrationType.APP_FUNCTIONS,
            framework = "Kotlin"
        ),
        GeminiIntegrationGuide(
            id = "gem-002",
            title = "AICore API — Direct Device AI Access",
            titleCn = "AICore API — 直接访问设备 AI",
            description = "Use AICore to directly interact with on-device Gemini models",
            descriptionCn = "使用 AICore 直接与设备上的 Gemini 模型交互",
            codeExample = """// Check AICore availability
val aicoreAvailable = AICore.isAvailable(context)
if (!aicoreAvailable) return

// Create AICore session
val session = AICore.createSession(
    context,
    AICore.Model.GEMINI_NANO,
    AICoreSession.Config(
        temperature = 0.7f,
        maxTokens = 2048
    )
)

// Generate response
val prompt = "Summarize: ${'$'}userInput"
val response = session.generate(prompt)
Log.d("AICore", "Response: ${'$'}response")""",
            codeExampleCn = """// 检查 AICore 可用性
val aicoreAvailable = AICore.isAvailable(context)
if (!aicoreAvailable) return

// 创建 AICore 会话
val session = AICore.createSession(
    context,
    AICore.Model.GEMINI_NANO,
    AICoreSession.Config(
        temperature = 0.7f,
        maxTokens = 2048
    )
)

// 生成响应
val prompt = "总结：${'$'}userInput"
val response = session.generate(prompt)
Log.d("AICore", "响应: ${'$'}response")""",
            integrationType = GeminiIntegrationType.AICORE,
            framework = "Kotlin"
        ),
        GeminiIntegrationGuide(
            id = "gem-003",
            title = "Gemini SDK (Cloud) — For Advanced Cloud Features",
            titleCn = "Gemini SDK（云端）— 高级云端功能",
            description = "Use Gemini SDK when cloud features (large context, vision, etc.) are needed",
            descriptionCn = "当需要云端功能（长上下文、视觉等）时使用 Gemini SDK",
            codeExample = """// build.gradle.kts
dependencies {
    implementation("com.google.ai:client-android:1.0.0")
}

// Initialize with API key (from LocalProperties/BuildConfig)
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = BuildConfig.GEMINI_API_KEY
)

// Generate content
val response = generativeModel.generateContent(
    content { text("Explain this code: ${'$'}codeSnippet") }
)
Log.d("Gemini", response.text)""",
            codeExampleCn = """// build.gradle.kts
dependencies {
    implementation("com.google.ai:client-android:1.0.0")
}

// 使用 API key 初始化（从 LocalProperties/BuildConfig 获取）
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = BuildConfig.GEMINI_API_KEY
)

// 生成内容
val response = generativeModel.generateContent(
    content { text("解释这段代码：${'$'}codeSnippet") }
)
Log.d("Gemini", response.text)""",
            integrationType = GeminiIntegrationType.GEMINI_SDK,
            framework = "Kotlin"
        )
    )

    /** Build Tab 4: Play Store optimization guides */
    private fun buildPlayStoreGuides(): List<PlayStoreGuide> = listOf(
        PlayStoreGuide(
            id = "ps-001",
            title = "Enable Desktop Optimization in Play Console",
            titleCn = "在 Play Console 中启用桌面优化",
            description = "Mark your app as optimized for desktop in Play Console to get the desktop badge",
            descriptionCn = "在 Play Console 中将应用标记为已为桌面优化，以获得桌面徽章",
            steps = listOf(
                "1. Log in to Play Console at https://play.google.com/console",
                "2. Select your app from the app list",
                "3. Go to Release > Setup > Advanced settings",
                "4. Navigate to Form factors tab",
                "5. Check 'Chrome OS' and 'Laptop/Desktop'",
                "6. Save and submit for review"
            ),
            stepsCn = listOf(
                "1. 登录 Play Console：https://play.google.com/console",
                "2. 从应用列表中选择您的应用",
                "3. 进入 Release > Setup > Advanced settings",
                "4. 导航到 Form factors 标签页",
                "5. 勾选 'Chrome OS' 和 'Laptop/Desktop'",
                "6. 保存并提交审核"
            ),
            consolePath = "Release > Setup > Advanced settings > Form factors"
        ),
        PlayStoreGuide(
            id = "ps-002",
            title = "Add Desktop Screenshots to Play Store Listing",
            titleCn = "为 Play Store 列表添加桌面截图",
            description = "Upload desktop-specific screenshots showing your app on a desktop/laptop environment",
            descriptionCn = "上传展示应用在桌面/笔记本电脑环境中的桌面专用截图",
            steps = listOf(
                "1. In Play Console, go to Store presence > Store listing",
                "2. Scroll to Screenshots section",
                "3. Click 'Add from device' for Desktop (2K) or Desktop (4K)",
                "4. Upload screenshots at 2560x1440 (2K) or 3840x2160 (4K)",
                "5. Include screenshots showing: windowed mode, keyboard interactions, desktop UI",
                "6. Write localized descriptions for desktop use cases"
            ),
            stepsCn = listOf(
                "1. 在 Play Console 中，进入 Store presence > Store listing",
                "2. 滚动到 Screenshots 部分",
                "3. 点击 'Add from device' 添加 Desktop (2K) 或 Desktop (4K) 截图",
                "4. 上传 2560x1440 (2K) 或 3840x2160 (4K) 分辨率的截图",
                "5. 包含展示以下内容的截图：窗口模式、键盘交互、桌面 UI",
                "6. 为桌面用例编写本地化描述"
            ),
            consolePath = "Store presence > Store listing > Screenshots"
        ),
        PlayStoreGuide(
            id = "ps-003",
            title = "Declare Desktop/Tablet Support in build.gradle",
            titleCn = "在 build.gradle 中声明桌面/平板支持",
            description = "Configure resizableActivity and desktop windowing in build config",
            descriptionCn = "在构建配置中配置 resizableActivity 和桌面窗口化",
            steps = listOf(
                "1. In app/build.gradle.kts, add in android {} block:",
                "2. Enable CoreLibraryDesugaring if using java.time on older APIs",
                "3. Set minSdk to 24+ for best desktop compatibility",
                "4. Test with Android Studio Desktop Device (API 35+)"
            ),
            stepsCn = listOf(
                "1. 在 app/build.gradle.kts 的 android {} 块中添加：",
                "2. 如果在旧版 API 上使用 java.time，启用 CoreLibraryDesugaring",
                "3. 将 minSdk 设置为 24+ 以获得最佳桌面兼容性",
                "4. 使用 Android Studio Desktop Device (API 35+) 进行测试"
            ),
            consolePath = "app/build.gradle.kts > android {}"
        ),
        PlayStoreGuide(
            id = "ps-004",
            title = "Add Aluminium OS to App Compatibility Declaration",
            titleCn = "在应用兼容性声明中添加 Aluminium OS",
            description = "Declare compatibility with Aluminium OS in Play Console device catalog",
            descriptionCn = "在 Play Console 设备目录中声明与 Aluminium OS 的兼容性",
            steps = listOf(
                "1. In Play Console, go to Release > Device catalog",
                "2. Search for 'Aluminium OS' or 'Chrome OS' devices",
                "3. View app compatibility status per device",
                "4. Use 'Request form factor declaration' for Aluminium OS",
                "5. Self-certify that app meets desktop UI guidelines",
                "6. Wait for Google review (typically 1-3 days)"
            ),
            stepsCn = listOf(
                "1. 在 Play Console 中，进入 Release > Device catalog",
                "2. 搜索 'Aluminium OS' 或 'Chrome OS' 设备",
                "3. 查看每个设备的应用程序兼容性状态",
                "4. 使用 'Request form factor declaration' 申请 Aluminium OS",
                "5. 自我证明应用程序符合桌面 UI 指南",
                "6. 等待 Google 审核（通常 1-3 天）"
            ),
            consolePath = "Release > Device catalog > Form factors"
        )
    )

    // ══════════════════════════════════════════════════════
    // Utility Functions
    // ══════════════════════════════════════════════════════

    private fun sendEffect(effect: AluminiumOSDesktopEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    /**
     * Get filtered keyboard guides by selected framework
     * 获取按选定框架过滤的键盘指南
     */
    fun getFilteredKeyboardGuides(): List<KeyboardMouseGuide> {
        val framework = _state.value.selectedFramework
        return _state.value.keyboardGuides.filter { it.framework == framework }
    }
} // class AluminiumOSDesktopViewModel
