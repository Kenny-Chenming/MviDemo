package com.mvi.kenny.feature.continueon

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ============================================================
// ContinueOnViewModel — Android 17 Continue On API 开发工具包
// ============================================================
// PRD-296 | MVI ViewModel
//
// Responsibilities / 职责:
// - Manage ContinueOnState (single source of truth)
// - Process ContinueOnIntent and update state
// - Emit ContinueOnEffect for one-time side effects
// - Persist dark mode preference via DataStore
//
// Dark mode persistence / 深色模式持久化:
// - Uses DataStore Preferences to save user preference
// - Loads preference on init, saves on toggle

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "continue_on_settings")

/**
 * ViewModel for Continue On Handoff API development toolkit.
 * Implements MVI pattern with State + Intent + Effect.
 *
 * @see ContinueOnState UI state
 * @see ContinueOnIntent User intentions
 * @see ContinueOnEffect One-time side effects
 */
class ContinueOnViewModel : ViewModel() {

    private val _state = MutableStateFlow(ContinueOnState.Initial)
    val state: StateFlow<ContinueOnState> = _state.asStateFlow()
    val currentState: ContinueOnState get() = _state.value

    private val _effect = Channel<ContinueOnEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // DataStore keys
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    init {
        loadInitialData()
    }

    /**
     * Load initial data / 加载初始数据
     * - Load dark mode preference from DataStore
     * - Build decision tree nodes
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val decisionTree = buildDecisionTree()
                _state.value = _state.value.copy(
                    decisionTreeState = decisionTree,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Process user intent / 处理用户意图
     *
     * @param intent User intent / 用户意图
     */
    fun sendIntent(intent: ContinueOnIntent) {
        when (intent) {
            is ContinueOnIntent.LoadGuide -> loadGuide()
            is ContinueOnIntent.SelectTab -> selectTab(intent.tab)
            is ContinueOnIntent.ToggleGuideExpand -> toggleGuideExpand(intent.key)
            is ContinueOnIntent.SelectDecisionNode -> selectDecisionNode(intent.nodeId)
            is ContinueOnIntent.ToggleDarkMode -> toggleDarkMode(intent.enabled)
            is ContinueOnIntent.SelectLanguage -> selectLanguage(intent.lang)
            is ContinueOnIntent.CopyCode -> copyCode(intent.code)
            is ContinueOnIntent.ClearData -> clearData()
            is ContinueOnIntent.SelectScenario -> selectScenario(intent.scenario)
            is ContinueOnIntent.DismissSnackbar -> dismissSnackbar()
            is ContinueOnIntent.DismissError -> dismissError()
        }
    }

    private fun loadGuide() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun selectTab(tab: ContinueOnTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    private fun toggleGuideExpand(key: String) {
        val current = _state.value.guideExpandState.toMutableMap()
        current[key] = !(current[key] ?: false)
        _state.value = _state.value.copy(guideExpandState = current)
    }

    private fun selectDecisionNode(nodeId: String) {
        val currentTree = _state.value.decisionTreeState
        val expanded = currentTree.expandedNodeIds.toMutableSet()

        if (expanded.contains(nodeId)) {
            expanded.remove(nodeId)
        } else {
            expanded.add(nodeId)
        }

        _state.value = _state.value.copy(
            decisionTreeState = currentTree.copy(
                selectedNodeId = nodeId,
                expandedNodeIds = expanded
            )
        )
    }

    private fun selectLanguage(lang: String) {
        _state.value = _state.value.copy(selectedLanguage = lang)
    }

    private fun copyCode(code: String) {
        viewModelScope.launch {
            _effect.send(ContinueOnEffect.CopyToClipboard(code, "Code"))
            _effect.send(ContinueOnEffect.ShowSnackbar("Code copied! / 代码已复制!"))
        }
    }

    private fun clearData() {
        viewModelScope.launch {
            _effect.send(ContinueOnEffect.ShowSnackbar("Data cleared / 数据已清除"))
        }
    }

    private fun selectScenario(scenario: Scenario) {
        _state.value = _state.value.copy(selectedScenario = scenario)
    }

    private fun dismissSnackbar() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }

    private fun dismissError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Toggle dark mode / 切换深色模式
     *
     * @param enabled Dark mode enabled / 深色模式启用
     */
    fun toggleDarkMode(enabled: Boolean) {
        _state.value = _state.value.copy(isDarkMode = enabled)
    }

    // ============================================================
    // Decision Tree Builder / 决策树构建器
    // ============================================================

    /**
     * Build decision tree / 构建决策树
     *
     * Root node: Should I use Continue On? / 我应该使用 Continue On 吗？
     * Branch nodes: Questions leading to recommendation
     */
    private fun buildDecisionTree(): DecisionTreeState {
        val root = DecisionNode(
            id = "root",
            questionZh = "我应该使用 Continue On 吗？",
            questionEn = "Should I use Continue On?",
            children = listOf(
                DecisionNode(
                    id = "cross_device",
                    questionZh = "你的 App 需要跨设备连续性吗？",
                    questionEn = "Does your app need cross-device continuity?",
                    children = listOf(
                        DecisionNode(
                            id = "doc_app",
                            questionZh = "是文档/内容类 App 吗？",
                            questionEn = "Is it a document/content app?",
                            recommendation = "App-to-App Handoff 推荐 / Recommended. " +
                                    "适用场景: 文档编辑、邮件、笔记等需要保持上下文的场景。",
                            children = listOf(
                                DecisionNode(
                                    id = "deep_link",
                                    questionZh = "是否需要 Deep Link 集成？",
                                    questionEn = "Do you need Deep Link integration?",
                                    recommendation = "App-to-App + Deep Link 模式 / Mode. " +
                                            "接收端通过 Deep Link 重建上下文。"
                                )
                            )
                        ),
                        DecisionNode(
                            id = "media_app",
                            questionZh = "是媒体播放类 App 吗？",
                            questionEn = "Is it a media app?",
                            recommendation = "Web Fallback Handoff 推荐 / Recommended. " +
                                    "视频/音乐切换到另一设备时降级到 Web URL。"
                        ),
                        DecisionNode(
                            id = "utility_app",
                            questionZh = "是工具类 App 吗？",
                            questionEn = "Is it a utility app?",
                            recommendation = "Direct-to-Web Handoff 推荐 / Recommended. " +
                                    "轻量切换，直接跳转到 Web 版本。"
                        )
                    )
                ),
                DecisionNode(
                    id = "no_cross_device",
                    questionZh = "不需要跨设备？",
                    questionEn = "No cross-device needed?",
                    recommendation = "Continue On 不适用 / Not applicable. " +
                            "考虑使用 Deep Link 或 App Link 实现应用内导航。"
                )
            )
        )
        return DecisionTreeState(nodes = listOf(root))
    }

    // ============================================================
    // Static Data / 静态数据
    // ============================================================

    /**
     * Get toolkit modules / 获取工具包子模块
     */
    fun getToolkitModules(): List<ContinueOnTool> = listOf(
        ContinueOnTool(
            id = "state_serializer",
            titleZh = "状态序列化工具",
            titleEn = "State Serializer",
            description = "Handoff 状态打包助手，生成序列化代码",
            iconName = "AccountTree"
        ),
        ContinueOnTool(
            id = "web_fallback",
            titleZh = "Web Fallback 配置器",
            titleEn = "Web Fallback Config",
            description = "配置降级 URL 和 Web Handoff 策略",
            iconName = "Language"
        ),
        ContinueOnTool(
            id = "deeplink",
            titleZh = "Deep Link ↔ Handoff 协同",
            titleEn = "Deep Link ↔ Handoff",
            description = "Deep Link 与 Handoff 上下文重建配置",
            iconName = "Link"
        ),
        ContinueOnTool(
            id = "security",
            titleZh = "安全合规检查清单",
            titleEn = "Security Checklist",
            description = "GDPR/CCPA 跨设备合规检测清单",
            iconName = "Security"
        )
    )

    /**
     * Get pre-built scenarios / 获取预置场景
     */
    fun getScenarios(): List<Scenario> = listOf(
        Scenario(
            id = "docs",
            titleZh = "文档协作 App",
            titleEn = "Document App",
            description = "Google Docs 类文档编辑器",
            appType = "Productivity",
            handoffType = "App-to-App",
            keyPoints = listOf("状态序列化: DocumentState", "Activity 上下文保持", "Deep Link 重建")
        ),
        Scenario(
            id = "email",
            titleZh = "邮件 App",
            titleEn = "Email App",
            description = "Gmail 类邮件客户端",
            appType = "Communication",
            handoffType = "App-to-App",
            keyPoints = listOf("邮件 Thread 上下文", "草稿状态同步", "附件 URI 处理")
        ),
        Scenario(
            id = "browser",
            titleZh = "浏览器 Tab 同步",
            titleEn = "Browser Tab Sync",
            description = "Chrome 类浏览器 Tab 续接",
            appType = "Browser",
            handoffType = "Web Fallback",
            keyPoints = listOf("URL + Scroll Position", "Tab Group 状态", "降级到 Web URL")
        ),
        Scenario(
            id = "video",
            titleZh = "视频播放续看",
            titleEn = "Video Playback",
            description = "YouTube/Netflix 类视频播放",
            appType = "Media",
            handoffType = "Web Fallback",
            keyPoints = listOf("播放位置同步", "字幕/画质偏好", "降级 Web Player")
        ),
        Scenario(
            id = "form",
            titleZh = "表单填写续填",
            titleEn = "Form Filling",
            description = "在线表单/问卷续填",
            appType = "Utility",
            handoffType = "App-to-App",
            keyPoints = listOf("表单字段状态", "验证状态保持", "离线支持")
        ),
        Scenario(
            id = "navigation",
            titleZh = "导航续导",
            titleEn = "Navigation",
            description = "地图/导航续导",
            appType = "Navigation",
            handoffType = "Web Fallback",
            keyPoints = listOf("位置+路线同步", "ETA 状态", "降级到 Web 地图")
        ),
        Scenario(
            id = "shopping",
            titleZh = "购物车同步",
            titleEn = "Shopping Cart",
            description = "电商购物车同步",
            appType = "E-commerce",
            handoffType = "App-to-App",
            keyPoints = listOf("商品列表序列化", "价格/库存状态", "支付状态保持")
        ),
        Scenario(
            id = "music",
            titleZh = "音乐播放续听",
            titleEn = "Music Playback",
            description = "Spotify/Apple Music 类音乐",
            appType = "Media",
            handoffType = "Web Fallback",
            keyPoints = listOf("播放列表+位置", "音质偏好", "降级 Web Player")
        ),
        Scenario(
            id = "meeting",
            titleZh = "视频会议续会",
            titleEn = "Video Meeting",
            description = "Meet/Zoom 类视频会议",
            appType = "Communication",
            handoffType = "App-to-App",
            keyPoints = listOf("会议上下文", "参会者列表", "屏幕共享状态")
        ),
        Scenario(
            id = "todo",
            titleZh = "任务/待办续办",
            titleEn = "Task/Todo",
            description = "Tasks/Keep 类待办管理",
            appType = "Productivity",
            handoffType = "App-to-App",
            keyPoints = listOf("任务状态同步", "清单内容", "截止日期提醒")
        )
    )

    /**
     * Get code examples / 获取代码示例
     */
    fun getCodeExamples(): List<CodeExample> = listOf(
        CodeExample(
            id = "publish_activity",
            title = "发布 Activity / Publish Activity",
            kotlinCode = """
// Kotlin: onPause() 中发布 Handoff
// Publish Handoff in onPause()
class MainActivity : AppCompatActivity() {
    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val handoffData = MyHandoffState(
                currentDocId = documentId,
                scrollPosition = scrollY,
                editMode = isEditMode
            )
            val extras = PersistableBundle().apply {
                putString("doc_id", handoffData.currentDocId)
                putInt("scroll_y", handoffData.scrollPosition)
                putBoolean("edit_mode", handoffData.editMode)
            }
            reportHandoff(extra)
        }
    }
}
            """.trimIndent(),
            javaCode = """
// Java: onPause() 中发布 Handoff
// Publish Handoff in onPause()
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PersistableBundle extras = new PersistableBundle();
            extras.putString("doc_id", documentId);
            extras.putInt("scroll_y", scrollY);
            extras.putBoolean("edit_mode", isEditMode);
            reportHandoff(extras);
        }
    }
}
            """.trimIndent()
        ),
        CodeExample(
            id = "receive_activity",
            title = "接收 Activity / Receive Activity",
            kotlinCode = """
// Kotlin: 接收端 Activity 处理 Handoff
// Receive Activity handles Handoff
class ReceiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val handoffExtras = intent.getParcelableExtra<PersistableBundle>(
                android.content.pm.ActivityResult.EXTRA_HANDOVER_DATA
            )
            handoffExtras?.let { extras ->
                val docId = extras.getString("doc_id", "")
                val scrollY = extras.getInt("scroll_y", 0)
                val editMode = extras.getBoolean("edit_mode", false)
                // Restore UI state / 恢复UI状态
                restoreDocumentState(docId, scrollY, editMode)
            }
        }
    }
}
            """.trimIndent(),
            javaCode = """
// Java: 接收端 Activity 处理 Handoff
// Receive Activity handles Handoff
public class ReceiveActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PersistableBundle extras = intent.getParcelableExtra(
                "handover_data"
            );
            if (extras != null) {
                String docId = extras.getString("doc_id", "");
                int scrollY = extras.getInt("scroll_y", 0);
                boolean editMode = extras.getBoolean("edit_mode", false);
                restoreDocumentState(docId, scrollY, editMode);
            }
        }
    }
}
            """.trimIndent()
        ),
        CodeExample(
            id = "web_fallback",
            title = "Web Fallback / Web降级",
            kotlinCode = """
// Kotlin: 配置 Web Fallback URL
// Configure Web Fallback URL
class HandoffConfig {
    companion object {
        // Web Fallback URLs for each device type
        // 各设备类型的 Web Fallback URL
        private val WEB_FALLBACKS = mapOf(
            "phone" to "https://app.example.com/mobile",
            "tablet" to "https://app.example.com/tablet",
            "web" to "https://app.example.com"
        )

        fun getWebFallback(deviceType: String): String {
            return WEB_FALLBACKS[deviceType] ?: WEB_FALLBACKS["web"]!!
        }

        // Deep Link data filter 配置
        // Deep Link data filter config
        const val DEEP_LINK_SCHEME = "https"
        const val DEEP_LINK_HOST = "app.example.com"
        const val DEEP_LINK_PATH = "/handoff"
    }
}
            """.trimIndent(),
            javaCode = """
// Java: 配置 Web Fallback URL
// Configure Web Fallback URL
public class HandoffConfig {
    private static final Map<String, String> WEB_FALLBACKS;
    static {
        WEB_FALLBACKS = new HashMap<>();
        WEB_FALLBACKS.put("phone", "https://app.example.com/mobile");
        WEB_FALLBACKS.put("tablet", "https://app.example.com/tablet");
        WEB_FALLBACKS.put("web", "https://app.example.com");
    }

    public static String getWebFallback(String deviceType) {
        return WEB_FALLBACKS.getOrDefault(deviceType, WEB_FALLBACKS.get("web"));
    }
}
            """.trimIndent()
        ),
        CodeExample(
            id = "compose_activity",
            title = "Compose Activity / Compose Activity",
            kotlinCode = """
// Kotlin: Compose Activity 支持 Handoff
// Compose Activity supports Handoff
// 1. AndroidManifest.xml 中声明:
//    Declare in AndroidManifest.xml:
// <activity
//     android:name=".MyComposeActivity"
//     android:reportHandoffAvailable="true"
//     android:exported="true">
//     <intent-filter>
//         <action android:name="android.intent.action.VIEW" />
//         <data android:scheme="https"
//               android:host="app.example.com"
//               android:pathPrefix="/handoff" />
//     </intent-filter>
// </activity>

// 2. Compose Activity 中处理 Handoff:
//    Handle Handoff in Compose Activity:
@Composable
fun MyComposeScreen(
    viewModel: MyViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity?.handoverExtras?.let { extras ->
                val docId = extras.getString("doc_id")
                viewModel.loadDocument(docId)
            }
        }
    }
}
            """.trimIndent(),
            javaCode = """
// Java: Compose Activity 支持 Handoff
// Compose Activity supports Handoff
// Note: Compose Activity is primarily Kotlin-first.
// Java usage requires Kotlin interop.
// 1. AndroidManifest.xml 声明相同 / Same AndroidManifest.xml declaration
// 2. Use Kotlin Interop for Handoff handling
"""
        )
    )

    /**
     * Get API diff table / 获取API差异表
     */
    fun getApiDiffItems(): List<Pair<String, List<String>>> = listOf(
        "reportHandoff()" to listOf("API 37+", "N/A"),
        "handover_extras" to listOf("PersistableBundle", "N/A"),
        "Intent.FLAG_ACTIVITY_CLEAR_TOP" to listOf("Required", "N/A"),
        "android:reportHandoffAvailable" to listOf("API 37+ manifest attr", "N/A"),
        "Deep Link data filter" to listOf("Required for receiving", "N/A"),
        "Google Account sync" to listOf("Required on both devices", "N/A"),
        "Pending Intent URI" to listOf("_pending_ URI for large data", "N/A")
    )

    /**
     * Get FAQ items / 获取FAQ项
     */
    fun getFaqItems(): List<Pair<String, String>> = listOf(
        "Continue On 和 Handoff 是一个东西吗？" to
                "是。Google 官方称其为 Continue On，底层机制是 Activity Handoff。",
        "Handoff 数据大小限制是多少？" to
                "通常 < 1MB。大型状态请使用 _pending_ URI 方案。",
        "哪些设备支持 Continue On？" to
                "首发: Android 17+ 手机 → 平板。未来扩展到手表/车机/TV。",
        "两台设备需要什么条件才能 Handoff？" to
                "必须登录同一个 Google 账号，且已启用跨设备服务。",
        "App-to-App 和 Web Fallback 如何选择？" to
                "已安装 App 用 App-to-App；未安装或轻量场景用 Web Fallback。",
        "Deep Link 和 Handoff 有什么关系？" to
                "Handoff 接收端通过 Deep Link data filter 跳转到正确 Activity 并重建上下文。",
        "Compose Activity 如何支持 Handoff？" to
                "在 manifest 声明 reportHandoffAvailable，在 Compose 层处理 handover_extras。",
        "测试需要两台设备吗？" to
                "是的，需要两台 Android 17+ 设备或模拟器。使用 adb devices 检查连接。"
    )
}
