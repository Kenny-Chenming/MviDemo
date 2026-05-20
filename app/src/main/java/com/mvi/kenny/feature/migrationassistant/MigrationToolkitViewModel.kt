package com.mvi.kenny.feature.migrationassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ============================================================
 * MigrationToolkitViewModel — Android Studio Migration Assistant 移植工具包 ViewModel
 * MigrationToolkitViewModel — Android Studio Migration Assistant Dev Toolkit ViewModel
 * ============================================================
 *
 * PRD-268 | Android Studio iOS/React Native/Web Migration Assistant 移植工具包
 * Ref: memory/agency/designs/PRD-268-Android-Studio-iOS-RN-Web-Migration-Assistant移植工具包.md
 *
 * MVI Architecture:
 * - State: Managed as MutableStateFlow<MigrationToolkitState>
 * - Intent: Received via sendIntent(), processed in ViewModelScope
 * - Effect: Sent to UI via effect Channel (one-time events)
 * —————————————————————————————————————————————————————
 *
 * Responsibilities:
 * 1. Manage all page state (platform selection, search, expansion)
 * 2. Provide initial data (API mappings, pitfall cases, CI templates)
 * 3. Handle user intents and update state accordingly
 * 4. Emit one-time effects for UI (toast, clipboard, scroll)
 *
 * @see MigrationToolkitContract for State/Intent/Effect definitions
 */
class MigrationToolkitViewModel : ViewModel() {

    // ============================================================
    // State — Single source of truth for UI
    // ============================================================

    private val _state = MutableStateFlow(MigrationToolkitState.Initial)
    val state: StateFlow<MigrationToolkitState> = _state.asStateFlow()

    // ============================================================
    // Effect — One-time side effects channel
    // ============================================================

    private val _effect = Channel<MigrationToolkitEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ============================================================
    // Initialization — Load initial data
    // ============================================================

    init {
        // Load all initial data on ViewModel creation
        // ViewModel 创建时加载所有初始数据
        loadInitialData()
    }

    /**
     * Main intent processing entry point
     * 主意图处理入口
     *
     * All user interactions flow through this method.
     * Each when branch handles one Intent type.
     *
     * @param intent User intention / 用户意图
     */
    fun sendIntent(intent: MigrationToolkitIntent) {
        when (intent) {
            is MigrationToolkitIntent.SelectPlatform -> selectPlatform(intent.platform)
            is MigrationToolkitIntent.SelectIosSection -> selectIosSection(intent.section)
            is MigrationToolkitIntent.SelectReactNativeSection -> selectReactNativeSection(intent.section)
            is MigrationToolkitIntent.SelectWebSection -> selectWebSection(intent.section)
            is MigrationToolkitIntent.SearchMappings -> searchMappings(intent.query)
            is MigrationToolkitIntent.ToggleCaseExpanded -> toggleCaseExpanded(intent.caseId)
            is MigrationToolkitIntent.ToggleMappingExpanded -> toggleMappingExpanded(intent.mappingId)
            is MigrationToolkitIntent.ToggleQualityCheck -> toggleQualityCheck(intent.itemId)
            is MigrationToolkitIntent.SelectApiMapping -> selectApiMapping(intent.mapping)
            is MigrationToolkitIntent.CopyMappingCsv -> copyMappingCsv(intent.mapping)
            is MigrationToolkitIntent.CopyAllMappingsCsv -> copyAllMappingsCsv()
            is MigrationToolkitIntent.ToggleDecisionNode -> toggleDecisionNode(intent.nodeId)
            is MigrationToolkitIntent.LoadData -> loadInitialData()
            is MigrationToolkitIntent.SelectCommonSection -> selectCommonSection(intent.section)
        }
    }

    // ============================================================
    // Intent Handlers — Business logic for each intent
    // ============================================================

    /**
     * Handle platform tab selection
     * 处理平台 Tab 选择
     *
     * @param platform Selected platform / 选中的平台
     */
    private fun selectPlatform(platform: Platform) {
        _state.value = _state.value.copy(
            activePlatform = platform,
            searchQuery = "", // Clear search on platform switch / 切换平台时清除搜索
            expandedCaseId = null,
            expandedMappingId = null
        )
    }

    /**
     * Handle iOS sub-section selection
     * 处理 iOS 子模块选择
     *
     * @param section Selected section / 选中的子模块
     */
    private fun selectIosSection(section: IosSection) {
        _state.value = _state.value.copy(activeIosSection = section)
    }

    /**
     * Handle React Native sub-section selection
     * 处理 React Native 子模块选择
     *
     * @param section Selected section / 选中的子模块
     */
    private fun selectReactNativeSection(section: ReactNativeSection) {
        _state.value = _state.value.copy(activeReactNativeSection = section)
    }

    /**
     * Handle Web sub-section selection
     * 处理 Web 子模块选择
     *
     * @param section Selected section / 选中的子模块
     */
    private fun selectWebSection(section: WebSection) {
        _state.value = _state.value.copy(activeWebSection = section)
    }

    /**
     * Handle API mapping search
     * 处理 API 映射搜索
     *
     * @param query Search query / 搜索关键词
     */
    private fun searchMappings(query: String) {
        val currentMappings = _state.value.apiMappings
        val filtered = if (query.isBlank()) {
            currentMappings
        } else {
            val lowerQuery = query.lowercase()
            currentMappings.filter {
                it.sourceApi.lowercase().contains(lowerQuery) ||
                it.targetApi.lowercase().contains(lowerQuery) ||
                it.sourceLanguage.lowercase().contains(lowerQuery)
            }
        }
        _state.value = _state.value.copy(
            searchQuery = query,
            filteredApiMappings = filtered
        )
    }

    /**
     * Toggle pitfall case expansion state
     * 切换踩坑案例展开状态
     *
     * @param caseId Case unique identifier / 案例唯一标识
     */
    private fun toggleCaseExpanded(caseId: String) {
        val current = _state.value.expandedCaseId
        _state.value = _state.value.copy(
            expandedCaseId = if (current == caseId) null else caseId
        )
    }

    /**
     * Toggle API mapping expansion state
     * 切换 API 映射展开状态
     *
     * @param mappingId Mapping unique identifier / 映射唯一标识
     */
    private fun toggleMappingExpanded(mappingId: String) {
        val current = _state.value.expandedMappingId
        _state.value = _state.value.copy(
            expandedMappingId = if (current == mappingId) null else mappingId
        )
    }

    /**
     * Toggle quality check item checked state
     * 切换质量检查项勾选状态
     *
     * @param itemId Item unique identifier / 清单项唯一标识
     */
    private fun toggleQualityCheck(itemId: String) {
        val updated = _state.value.qualityChecklist.map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        }
        _state.value = _state.value.copy(qualityChecklist = updated)
    }

    /**
     * Select API mapping for detail view
     * 选中 API 映射查看详情
     *
     * @param mapping Selected mapping or null / 选中的映射或 null
     */
    private fun selectApiMapping(mapping: ApiMapping?) {
        _state.value = _state.value.copy(selectedApiMapping = mapping)
    }

    /**
     * Copy single API mapping as CSV
     * 复制单个 API 映射为 CSV
     *
     * @param mapping Mapping to copy / 要复制的映射
     */
    private fun copyMappingCsv(mapping: ApiMapping) {
        val csv = "\"${mapping.sourceApi}\",\"${mapping.targetApi}\",\"${mapping.sourceLanguage}\",\"${mapping.notes}\",${mapping.isAutomated}"
        viewModelScope.launch {
            _effect.send(MigrationToolkitEffect.CopyToClipboard(csv, "API Mapping"))
            _effect.send(MigrationToolkitEffect.ShowToast("已复制映射到剪贴板"))
        }
    }

    /**
     * Copy all visible API mappings as CSV
     * 复制所有可见映射为 CSV
     */
    private fun copyAllMappingsCsv() {
        val mappings = _state.value.getFilteredMappings()
        val header = "\"Source API\",\"Target API\",\"Source Language\",\"Notes\",\"Automated\""
        val rows = mappings.joinToString("\n") {
            "\"${it.sourceApi}\",\"${it.targetApi}\",\"${it.sourceLanguage}\",\"${it.notes}\",${it.isAutomated}"
        }
        val csv = "$header\n$rows"
        viewModelScope.launch {
            _effect.send(MigrationToolkitEffect.CopyToClipboard(csv, "All API Mappings"))
            _effect.send(MigrationToolkitEffect.ShowToast("已复制 ${mappings.size} 条映射到剪贴板"))
        }
    }

    /**
     * Toggle decision tree node expansion
     * 切换决策树节点展开状态
     *
     * @param nodeId Node unique identifier / 节点唯一标识
     */
    private fun toggleDecisionNode(nodeId: String) {
        val current = _state.value.decisionTreeExpandedNodes
        val updated = if (nodeId in current) {
            current - nodeId
        } else {
            current + nodeId
        }
        _state.value = _state.value.copy(decisionTreeExpandedNodes = updated)
    }

    /**
     * Handle common section selection
     * 处理通用章节选择
     *
     * @param section Selected section / 选中的章节
     */
    private fun selectCommonSection(section: CommonSection) {
        _state.value = _state.value.copy(activeCommonSection = section)
    }

    // ============================================================
    // Data Loading — Initialize all tool data
    // ============================================================

    /**
     * Load all initial data (API mappings, pitfall cases, CI templates, checklist)
     * 加载所有初始数据（API 映射、踩坑案例、CI 模板、检查清单）
     */
    private fun loadInitialData() {
        _state.value = _state.value.copy(isLoading = true)

        // Load all data sets / 加载所有数据集
        val apiMappings = loadApiMappings()
        val pitfallCases = loadPitfallCases()
        val ciTemplates = loadCITemplates()
        val qualityChecklist = loadQualityChecklist()

        _state.value = _state.value.copy(
            apiMappings = apiMappings,
            filteredApiMappings = apiMappings,
            pitfallCases = pitfallCases,
            ciTemplates = ciTemplates,
            qualityChecklist = qualityChecklist,
            isLoading = false
        )
    }

    /**
     * Load iOS → Android API mappings
     * 加载 iOS → Android API 映射
     */
    private fun loadApiMappings(): List<ApiMapping> = listOf(
        // UIKit → Jetpack Compose / CoreLibs Mappings
        ApiMapping("ios-001", "UIViewController", "ComponentActivity", "Swift/Obj-C", "UIKit 容器 → Android Activity", true),
        ApiMapping("ios-002", "UIView", "Compose UI (Box/Column/Row)", "Swift/Obj-C", "UIKit 可视元素 → Compose 声明式 UI", true),
        ApiMapping("ios-003", "UIImage", "ImageBitmap", "Swift/Obj-C", "图片类型映射", true),
        ApiMapping("ios-004", "UIImageView", "AsyncImage / Image", "Swift/Obj-C", "图片展示组件", true),
        ApiMapping("ios-005", "UILabel", "Text", "Swift/Obj-C", "文本标签", true),
        ApiMapping("ios-006", "UIButton", "Button", "Swift/Obj-C", "按钮组件", true),
        ApiMapping("ios-007", "UITextField", "TextField / OutlinedTextField", "Swift/Obj-C", "单行文本输入", true),
        ApiMapping("ios-008", "UITextView", "Text / BasicTextField", "Swift/Obj-C", "多行文本展示/编辑", true),
        ApiMapping("ios-009", "UITableView", "LazyColumn", "Swift/Obj-C", "垂直列表", true),
        ApiMapping("ios-010", "UICollectionView", "LazyVerticalGrid / LazyHorizontalGrid", "Swift/Obj-C", "网格/瀑布流布局", true),
        ApiMapping("ios-011", "UINavigationController", "NavHost + NavGraph", "Swift/Obj-C", "页面导航容器", true),
        ApiMapping("ios-012", "UINavigationBar", "TopAppBar", "Swift/Obj-C", "顶部导航栏", true),
        ApiMapping("ios-013", "UITabBarController", "NavigationBar (Bottom)", "Swift/Obj-C", "底部 Tab 导航", true),
        ApiMapping("ios-014", "UITabBar", "NavigationBar (Bottom)", "Swift/Obj-C", "底部标签栏", true),
        ApiMapping("ios-015", "UIScrollView", "LazyColumn / Modifier.scrollable", "Swift/Obj-C", "滚动容器", true),
        ApiMapping("ios-016", "UITouchEvent", "PointerInput / Compose Gesture", "Swift/Obj-C", "触摸手势处理", false),
        ApiMapping("ios-017", "CoreLocation", "Google Play Services Location", "Swift/Obj-C", "地理位置服务 — 需要手动映射", false),
        ApiMapping("ios-018", "CoreData", "Room Database", "Swift/Obj-C", "本地数据持久化 — 架构差异大", false),
        ApiMapping("ios-019", "NSUserDefaults", "DataStore / SharedPreferences", "Swift/Obj-C", "轻量级键值存储", true),
        ApiMapping("ios-020", "CoreAnimation", "Compose Animation APIs", "Swift/Obj-C", "动画系统", false),
        ApiMapping("ios-021", "AVFoundation (Audio)", "MediaPlayer / ExoPlayer", "Swift/Obj-C", "音视频播放 — 部分自动映射", false),
        ApiMapping("ios-022", "HealthKit", "Health Connect API", "Swift/Obj-C", "健康数据 — 无官方映射，需手动", false),
        ApiMapping("ios-023", "ARKit", "ARCore", "Swift/Obj-C", "增强现实 — API 设计差异显著", false),
        ApiMapping("ios-024", "StoreKit", "Google Play Billing Library", "Swift/Obj-C", "应用内购买 — 完全不同的 API", false),
        ApiMapping("ios-025", "PushKit", "Firebase Cloud Messaging", "Swift/Obj-C", "推送通知 — 平台差异大", false),
        // Swift → Kotlin specific mappings
        ApiMapping("swift-001", "var / let", "val / var", "Swift", "变量声明语法差异", true),
        ApiMapping("swift-002", "Optional<T>", "T?", "Swift", "可选类型映射", true),
        ApiMapping("swift-003", "guard let", "?.let { } / if (val x = ...)", "Swift", "安全解包语法", true),
        ApiMapping("swift-004", "extension", "Extension function / Kotlin extension", "Swift", "扩展语法", true),
        ApiMapping("swift-005", "protocol", "Interface", "Swift", "协议 → 接口", true),
        ApiMapping("swift-006", "struct", "data class / class", "Swift", "结构体 → 数据类", true),
        ApiMapping("swift-007", "enum (associated values)", "sealed class + when", "Swift", "枚举关联值映射", false),
        ApiMapping("swift-008", "@Published", "StateFlow / MutableStateFlow", "Swift", "响应式属性包装器", true),
        ApiMapping("swift-009", "async/await", "suspend function / Coroutine", "Swift", "异步编程模型", true),
        ApiMapping("swift-010", "actor", "kotlin.concurrent.coroutines", "Swift", "并发类型 — 模型不同", false),
        // React Native → Android mappings
        ApiMapping("rn-001", "View", "Compose Box / Column / Row", "RN JS", "基础布局组件", true),
        ApiMapping("rn-002", "Text", "Text", "RN JS", "文本组件", true),
        ApiMapping("rn-003", "Image", "Image / AsyncImage", "RN JS", "图片组件", true),
        ApiMapping("rn-004", "ScrollView", "LazyColumn / Modifier.verticalScroll", "RN JS", "滚动容器", true),
        ApiMapping("rn-005", "FlatList", "LazyColumn (with keys)", "RN JS", "高性能列表", true),
        ApiMapping("rn-006", "SectionList", "LazyColumn (grouped sections)", "RN JS", "分组列表", true),
        ApiMapping("rn-007", "TextInput", "TextField / OutlinedTextField", "RN JS", "文本输入", true),
        ApiMapping("rn-008", "TouchableOpacity", "Modifier.clickable / Button", "RN JS", "可点击容器", true),
        ApiMapping("rn-009", "StyleSheet", "Modifier / Compose Theme", "RN JS", "样式系统 — 完全不同", false),
        ApiMapping("rn-010", "useState", "MutableStateFlow / remember { mutableStateOf }", "RN JS", "组件状态管理", true),
        ApiMapping("rn-011", "useEffect", "LaunchedEffect / SideEffect", "RN JS", "副作用处理", true),
        ApiMapping("rn-012", "useContext", "CompositionLocal / Ambient", "RN JS", "上下文传递", true),
        ApiMapping("rn-013", "Redux / MobX", "ViewModel + StateFlow", "RN JS", "状态管理迁移 — 架构重构", false),
        ApiMapping("rn-014", "react-native-navigation", "Jetpack Navigation", "RN JS", "导航库替代", false),
        ApiMapping("rn-015", "axios / fetch", "Ktor / Retrofit / OkHttp", "RN JS", "HTTP 客户端替代", false),
        ApiMapping("rn-016", "AsyncStorage", "DataStore / Room", "RN JS", "本地存储替代", false),
        ApiMapping("rn-017", "react-native-push-notification", "Firebase Cloud Messaging", "RN JS", "推送通知替代", false),
        // Web → Android mappings
        ApiMapping("web-001", "window.location", "Intent / DeepLink", "Web JS", "路由跳转 — 平台差异", false),
        ApiMapping("web-002", "localStorage", "SharedPreferences / DataStore", "Web JS", "客户端存储", true),
        ApiMapping("web-003", "sessionStorage", "DataStore", "Web JS", "会话存储", true),
        ApiMapping("web-004", "fetch / XMLHttpRequest", "Ktor / Retrofit / OkHttp", "Web JS", "网络请求", true),
        ApiMapping("web-005", "Service Worker", "WorkManager", "Web JS", "后台任务 — 架构不同", false),
        ApiMapping("web-006", "PWA Manifest", "AndroidManifest.xml", "Web JS", "应用清单配置", true),
        ApiMapping("web-007", "Notification API", "Firebase Cloud Messaging", "Web JS", "推送通知", false),
        ApiMapping("web-008", "Geolocation API", "Google Play Services Location", "Web JS", "地理位置", true),
        ApiMapping("web-009", "MediaDevices API (Camera/Mic)", "CameraX / MediaRecorder", "Web JS", "媒体设备 — API 差异大", false),
        ApiMapping("web-010", "Responsive CSS (media queries)", "WindowSizeClass / Adaptive Layout", "Web JS", "响应式布局适配", true)
    )

    /**
     * Load iOS → Android migration pitfall cases
     * 加载 iOS → Android 迁移踩坑案例
     */
    private fun loadPitfallCases(): List<PitfallCase> = listOf(
        PitfallCase(
            id = "pitfall-001",
            title = "CoreLocation API 无等价映射",
            problem = "HealthKit、CoreLocation、ARKit 等 iOS 专有框架在 Migration Assistant 中没有内置映射规则，迁移后代码会标记为「未处理」。",
            solution = "① 在 Migration Assistant 报告中找出所有 HealthKit/CoreLocation/ARKit 调用；② 评估替换方案（Google Play Services / 第三方库 / 手动实现）；③ 对于关键路径，建议手动移植而非依赖自动迁移。",
            codeExample = """// Before (iOS - Swift)
import CoreLocation
let locationManager = CLLocationManager()
locationManager.requestWhenInUseAuthorization()
locationManager.startUpdatingLocation()

// After (Android - Kotlin)
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
if (ActivityCompat.checkSelfPermission(...)) {
    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
}"""
        ),
        PitfallCase(
            id = "pitfall-002",
            title = "UIKit 手势 vs Compose 手势差异",
            problem = "UIKit 的 UIGestureRecognizer 是命令式的，Migration Assistant 转换后的 Compose 代码使用 PointerInputScope，可能存在行为差异。",
            solution = "① 手动审阅所有手势处理代码；② 使用 Compose 的 pointerInput modifier 重写复杂手势；③ 对比 iOS 和 Android 上的手势行为是否一致。",
            codeExample = """// iOS Swift - UITapGestureRecognizer
let tap = UITapGestureRecognizer(target: self, action: #selector(handleTap))
view.addGestureRecognizer(tap)

// Android Compose - pointerInput
Modifier.pointerInput(Unit) {
    detectTapGestures(
        onTap = { /* handle tap */ }
    )
}"""
        ),
        PitfallCase(
            id = "pitfall-003",
            title = "Swift Optional 处理方式差异",
            problem = "Swift 的 Optional (T?) 和 Kotlin 的 nullable (T?) 语义相近但语法和操作方式不同，自动转换可能产生不地道的 Kotlin 代码。",
            solution = "① 手动审阅所有 ?. 和 !! 操作；② 使用 let ?: return / early-return 模式替代 !!；③ 复杂链式 Optional 建议重写为更符合 Kotlin 习惯的代码。",
            codeExample = """// Swift - Optional chaining
let name = user?.profile?.address?.city ?? "Unknown"

// Kotlin - Idiomatic
val name = user?.profile?.address?.city ?: "Unknown"

// Note: Migration Assistant handles this well, but complex chains need manual review"""
        ),
        PitfallCase(
            id = "pitfall-004",
            title = "React Native 库无等价 Android 实现",
            problem = "许多 React Native 第三方库（如 react-native-maps、react-native-video）没有高质量的 Android 原生替代，导致迁移后功能缺失。",
            solution = "① 迁移前审查所有第三方 RN 库；② 对于无等价 Android 库的依赖，评估：替换库 / 手动实现 / 简化功能；③ 使用 AndroidX/Google Play Services 原生库替代。",
            codeExample = """// RN 库 → Android 替代方案映射
react-native-maps → Google Maps SDK for Android
react-native-video → ExoPlayer (AndroidX Media3)
react-native-camera → CameraX
react-native-push-notification → Firebase Cloud Messaging"""
        ),
        PitfallCase(
            id = "pitfall-005",
            title = "PWA Service Worker ≠ Android WorkManager",
            problem = "Web 的 Service Worker 和 Android 的 WorkManager 都能处理后台任务，但编程模型完全不同。Migration Assistant 无法自动转换。",
            solution = "① 识别 Web App 中所有 Service Worker 用途；② 对于网络请求/缓存：使用 WorkManager + OkHttp 缓存；③ 对于推送通知：使用 FCM；④ 考虑使用 WorkManager 的 Constraints API 表达等效逻辑。",
            codeExample = null
        ),
        PitfallCase(
            id = "pitfall-006",
            title = "UIKit → Compose 映射后的声明式 vs 命令式差异",
            problem = "UIKit 是命令式框架（你告诉视图做什么），Compose 是声明式框架（你描述视图应该是什么样子）。自动转换的代码可能产生嵌套回调地狱。",
            solution = "① 审阅所有 UIViewController 转换后的 Composable；② 将状态提升到 ViewModel；③ 使用 StateFlow 驱动 UI 状态；④ 删除所有 setState/dismiss 代码。",
            codeExample = null
        )
    )

    /**
     * Load CI integration templates
     * 加载 CI 集成模板
     */
    private fun loadCITemplates(): List<CITemplate> = listOf(
        CITemplate(
            id = "ci-001",
            name = "iOS → Android 自动化迁移流水线",
            description = "GitHub Actions 模板：在 PR 创建时自动运行 Migration Assistant，生成迁移报告并上传到 Artifacts",
            workflowYaml = """name: iOS to Android Migration
on:
  pull_request:
    paths:
      - 'ios/**'

jobs:
  migrate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Setup Android Studio
        uses: mancelabs/setup-android@v2
      - name: Run Migration Assistant
        run: |
          # Run Android Studio Migration Assistant CLI
          android-studio --migration-assistant \\
            --source ios \\
            --target android \\
            --output ./migration-report
      - name: Upload Report
        uses: actions/upload-artifact@v4
        with:
          name: migration-report
          path: ./migration-report"""
        ),
        CITemplate(
            id = "ci-002",
            name = "React Native → Android 批量迁移",
            description = "批量处理多个 React Native 模块的迁移任务，包含质量门槛检查",
            workflowYaml = """name: RN to Android Batch Migration
on:
  workflow_dispatch:
    inputs:
      modules:
        description: 'Comma-separated module names'
        required: true

jobs:
  migrate:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        module: ${'$'}{{ fromJSON(github.event.inputs.modules) }}
    steps:
      - uses: actions/checkout@v4
      - name: Migrate module
        run: |
          android-studio --migration-assistant \\
            --source react-native \\
            --module ${'$'}{{ matrix.module }} \\
            --quality-gate 80
      - name: Quality Check
        if: success()
        run: |
          # Run quality verification
          ./gradlew :app:lintDebug
          # Check migration coverage
          ./gradlew checkMigrationCoverage"""
        ),
        CITemplate(
            id = "ci-003",
            name = "迁移质量自动报告",
            description = "每次迁移后自动生成质量报告，包含 API 映射覆盖率、踩坑预警、Play Store 合规检查",
            workflowYaml = """name: Migration Quality Report
on:
  push:
    branches: [migration/*]

jobs:
  quality-report:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Generate Quality Report
        run: |
          android-studio --migration-assistant \\
            --generate-report \\
            --format pdf \\
            --output ./quality-report.pdf
      - name: Comment on PR
        uses: actions/github-script@v7
        with:
          script: |
            github.rest.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: 'Migration Quality Report generated. @reviewer please check.'
            })"""
        )
    )

    /**
     * Load quality verification checklist
     * 加载质量验证清单
     */
    private fun loadQualityChecklist(): List<QualityCheckItem> = listOf(
        // Functionality / 功能性
        QualityCheckItem("q-001", "所有 iOS 核心功能已映射到 Android", "检查每个 iOS 功能在 Android 端是否可用", category = "功能性"),
        QualityCheckItem("q-002", "数据模型已正确迁移", "检查 Realm/SQLite/CoreData → Room 迁移", category = "功能性"),
        QualityCheckItem("q-003", "网络请求逻辑验证通过", "检查 API 调用、数据解析、错误处理", category = "功能性"),
        QualityCheckItem("q-004", "推送通知功能已迁移", "APNs → FCM 迁移验证", category = "功能性"),
        // UI/UX
        QualityCheckItem("q-005", "所有屏幕布局已适配 Android 屏幕", "检查屏幕适配、SafeArea、导航栏高度", category = "UI/UX"),
        QualityCheckItem("q-006", "手势操作已正确转换", "检查滑动、缩放、长按等手势行为", category = "UI/UX"),
        QualityCheckItem("q-007", "字体和排版符合 Android 设计规范", "检查字体大小、行高、字重", category = "UI/UX"),
        QualityCheckItem("q-008", "动画效果已正确实现", "检查过渡动画、微交互动画", category = "UI/UX"),
        // Performance
        QualityCheckItem("q-009", "应用启动时间 < 2秒", "使用 Android Profiler 检查冷启动时间", category = "性能"),
        QualityCheckItem("q-010", "内存使用正常，无内存泄漏", "使用 Memory Profiler 检查", category = "性能"),
        QualityCheckItem("q-011", "列表滚动流畅 (60fps)", "使用 GPU 呈现模式分析", category = "性能"),
        // Play Store Compliance
        QualityCheckItem("q-012", "隐私政策已更新", "检查隐私政策是否包含 Android 特有数据收集", category = "合规"),
        QualityCheckItem("q-013", "权限已精简", "检查 AndroidManifest 权限是否最小化", category = "合规"),
        QualityCheckItem("q-014", "targetSdkVersion ≥ 33", "检查 targetSdk 是否满足 Play Store 要求", category = "合规"),
        QualityCheckItem("q-015", "隐私清单文件已生成", "使用 Privacy Sandbox 工具生成", category = "合规"),
        // CI/CD
        QualityCheckItem("q-016", "CI 流水线包含迁移后构建验证", "检查 CI 是否包含 ./gradlew assembleDebug", category = "CI/CD"),
        QualityCheckItem("q-017", "迁移报告已存档", "每次迁移生成 PDF 报告并保存", category = "CI/CD")
    )
}
