package com.mvi.kenny.feature.wearos7tools

// ================================================================
// WearOS7ToolsScreen — Wear OS 7 开发者工具箱主界面
// ================================================================
// Main screen for Wear OS 7 developer toolkit.
//
// PRD-164: Wear OS 7 开发工具包
// Design Reference: memory/agency/designs/PRD-164-Wear-OS-7-开发工具包.md
//
// 8 Tools: Live Updates migration/UI, Health Connect, Compose for Wear OS,
// Multi-screen CI, Health Mock, API Compliance, Android XR synergy
//
// Google I/O 2026 (May 19-20) — Wear OS 7 will be officially announced.
// ================================================================

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig

// ================================================================
// Data Classes for Screen
// ================================================================

/**
 * Code block data for display.
 */
data class CodeBlockData(
    val id: String,
    val title: String,
    val code: String
)

// ================================================================
// Main Screen
// ================================================================

/**
 * WearOS7ToolsScreen — Root composable for Wear OS 7 developer toolkit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearOS7ToolsScreen(
    state: WearOS7ToolsState,
    onIntent: (WearOS7ToolsIntent) -> Unit,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Update parent TopBar when selectedTool changes
    LaunchedEffect(state.selectedTool) {
        onUpdateTopBar(
            TopBarConfig(
                title = state.selectedTool?.displayName ?: "Wear OS 7 工具箱",
                actions = if (state.selectedTool != null) {
                    listOf(
                        TopBarAction(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            onClick = { onIntent(WearOS7ToolsIntent.BackToOverview) }
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.selectedTool?.displayName ?: "Wear OS 7 工具箱",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (state.selectedTool != null) {
                        IconButton(onClick = { onIntent(WearOS7ToolsIntent.BackToOverview) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (state.selectedTool == WearOS7Tool.Compliance) {
                        IconButton(onClick = { onIntent(WearOS7ToolsIntent.ExportReport) }) {
                            Icon(Icons.Default.Share, contentDescription = "导出报告")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.selectedTool == null) {
                BottomTabBar(
                    selectedTab = state.selectedTab,
                    onTabSelected = { onIntent(WearOS7ToolsIntent.SelectTab(it)) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedTool == WearOS7Tool.Compliance && !state.isScanning) {
                FloatingActionButton(
                    onClick = { onIntent(WearOS7ToolsIntent.StartScan) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Radar, contentDescription = "开始扫描")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = state.selectedTool,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "tool_panel_transition"
            ) { tool ->
                if (tool != null) {
                    ToolDetailPanel(tool = tool, state = state, onIntent = onIntent)
                } else {
                    DashboardContent(
                        selectedTab = state.selectedTab,
                        onToolClick = { onIntent(WearOS7ToolsIntent.SelectTool(it)) }
                    )
                }
            }
        }
    }
}

// ================================================================
// Dashboard Content
// ================================================================

/**
 * Dashboard / Overview content showing all tools in the selected tab.
 */
@Composable
private fun DashboardContent(
    selectedTab: WearOS7Tab,
    onToolClick: (WearOS7Tool) -> Unit
) {
    val tools = wearOS7ToolsByTab[selectedTab] ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = selectedTab.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "${tools.size} 个工具",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(tools) { toolCard ->
            ToolCard(toolCard = toolCard, onClick = { onToolClick(toolCard.tool) })
        }
    }
}

/**
 * Tool card for dashboard display.
 */
@Composable
private fun ToolCard(toolCard: ToolCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tool icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getToolIcon(toolCard.tool),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Tool info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = toolCard.tool.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (toolCard.isNew) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = toolCard.tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = 180f }
            )
        }
    }
}

// ================================================================
// Tool Detail Panels
// ================================================================

/**
 * Detail panel router for each tool.
 */
@Composable
private fun ToolDetailPanel(
    tool: WearOS7Tool,
    state: WearOS7ToolsState,
    onIntent: (WearOS7ToolsIntent) -> Unit
) {
    when (tool) {
        WearOS7Tool.LiveUpdatesGuide -> LiveUpdatesGuidePanel(onIntent = onIntent, state = state)
        WearOS7Tool.LiveUpdatesUI -> LiveUpdatesUIPanel(
            formFactor = state.watchFormFactor,
            onFormFactorChange = { onIntent(WearOS7ToolsIntent.ChangeWatchFormFactor(it)) },
            onCopyCode = { onIntent(WearOS7ToolsIntent.CopyCode(it)) },
            state = state
        )
        WearOS7Tool.HealthConnect -> HealthConnectPanel(onIntent = onIntent)
        WearOS7Tool.ComposeWearOS -> ComposeWearOSPanel(onIntent = onIntent)
        WearOS7Tool.MultiScreenCI -> MultiScreenCIPanel(onIntent = onIntent)
        WearOS7Tool.HealthMock -> HealthMockPanel(onIntent = onIntent)
        WearOS7Tool.Compliance -> CompliancePanel(
            isScanning = state.isScanning,
            scanProgress = state.scanProgress,
            scanResults = state.scanResults,
            onStartScan = { onIntent(WearOS7ToolsIntent.StartScan) }
        )
        WearOS7Tool.XR协同 -> XR协同Panel()
    }
}

/**
 * Live Updates migration guide — dual-screen phone ↔ watch view.
 */
@Composable
private fun LiveUpdatesGuidePanel(
    onIntent: (WearOS7ToolsIntent) -> Unit,
    state: WearOS7ToolsState
) {
    val codeBlocks = remember {
        listOf(
            CodeBlockData(
                id = "manifest_perm",
                title = "AndroidManifest.xml — 手表端权限",
                code = """<!-- Wear OS 7 Live Updates 手表端权限 -->
<uses-permission android:name="android.permission.permission-group.LIVE_UPDATES" />
<queries>
    <package android:name="com.google.android.wearable.app" />
</queries>"""
            ),
            CodeBlockData(
                id = "phone_service",
                title = "LiveUpdateService.kt — 手机端发布",
                code = """class LiveUpdateService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) {
        val update = LiveUpdate.Builder("wear_os_update")
            .setTitle("健身目标达成！")
            .setText("你已完成今日步数目标")
            .setRelevanceScore(80).build()
        LiveUpdateManager.publish(update)
        return START_NOT_STICKY
    }
}"""
            ),
            CodeBlockData(
                id = "watch_receiver",
                title = "WatchLiveUpdateReceiver.kt — 手表端接收",
                code = """class WatchLiveUpdateReceiver : WearableListenerService() {
    override fun onLiveUpdateReceived(update: LiveUpdate) {
        val notification = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_update)
            .setContentTitle(update.title)
            .setContentText(update.text).build()
        NotificationManager.notify(update.id, notification)
    }
}"""
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Live Updates 迁移指南", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Live Update 如何同时在手机和手表上正确渲染，跨设备协同模板",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { DualScreenIllustration() }

        items(codeBlocks) { block ->
            CodeBlockCard(
                block = block,
                isExpanded = state.expandedCodeBlockId == block.id,
                isCopied = state.codeCopied,
                onToggle = { onIntent(WearOS7ToolsIntent.ToggleCodeBlock(block.id)) },
                onCopy = { onIntent(WearOS7ToolsIntent.CopyCode(block.id)) }
            )
        }
    }
}

/**
 * Dual-screen phone ↔ watch illustration.
 */
@Composable
private fun DualScreenIllustration() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WatchSimulator(label = "手机端", formFactor = WatchFormFactor.Round, content = "📱 手机通知", isPhone = true)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text("Live Update", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        WatchSimulator(label = "手表端", formFactor = WatchFormFactor.Round, content = "⌚ Watch 通知", isPhone = false)
    }
}

/**
 * Watch screen simulator for UI preview.
 */
@Composable
private fun WatchSimulator(label: String, formFactor: WatchFormFactor, content: String, isPhone: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        val shape = when {
            isPhone -> RoundedCornerShape(16.dp)
            formFactor == WatchFormFactor.Round -> CircleShape
            formFactor == WatchFormFactor.OLED -> RoundedCornerShape(8.dp)
            else -> RoundedCornerShape(4.dp)
        }
        val bgColor = if (formFactor == WatchFormFactor.OLED) Color.Black else Color(0xFF1C1B1F)
        val borderColor = if (formFactor == WatchFormFactor.OLED) Color(0xFF333333) else Color.Transparent

        Box(
            modifier = Modifier
                .size(if (isPhone) 80.dp else 100.dp)
                .background(bgColor, shape)
                .then(if (formFactor == WatchFormFactor.OLED) Modifier.border(1.dp, borderColor, shape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(text = content, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

/**
 * Live Updates Watch UI template — adaptive for round/OLED/square.
 */
@Composable
private fun LiveUpdatesUIPanel(
    formFactor: WatchFormFactor,
    onFormFactorChange: (WatchFormFactor) -> Unit,
    onCopyCode: (String) -> Unit,
    state: WearOS7ToolsState
) {
    val previewCode = remember(formFactor) {
        when (formFactor) {
            WatchFormFactor.Round -> """@Composable
fun RoundWatchLiveUpdate(view: LiveUpdateView) {
    Box(
        modifier = Modifier.fillMaxSize()
            .clip(CircleShape).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(view.title, color = Color.White, fontSize = 14.sp)
            Text(view.text, color = Color.Gray, fontSize = 10.sp)
        }
    }
}"""
            WatchFormFactor.OLED -> """@Composable
fun OLEDWatchLiveUpdate(view: LiveUpdateView) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(view.title, color = Color.White, fontSize = 14.sp)
            Text(view.text, color = Color(0xFFAAAAAA), fontSize = 10.sp)
        }
    }
}"""
            WatchFormFactor.Square -> """@Composable
fun SquareWatchLiveUpdate(view: LiveUpdateView) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1C1B1F)),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(view.title, color = Color.White, fontSize = 14.sp)
            Text(view.text, color = Color.Gray, fontSize = 10.sp)
        }
    }
}"""
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Watch 屏幕适配模板", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            Text("选择屏幕形态", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchFormFactor.entries.forEach { factor ->
                    FilterChip(
                        selected = formFactor == factor,
                        onClick = { onFormFactorChange(factor) },
                        label = { Text(factor.displayName) }
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bgColor = when (formFactor) {
                        WatchFormFactor.OLED -> Color.Black
                        else -> Color(0xFF1C1B1F)
                    }
                    val shape = when (formFactor) {
                        WatchFormFactor.Round -> CircleShape
                        WatchFormFactor.OLED -> RoundedCornerShape(8.dp)
                        WatchFormFactor.Square -> RoundedCornerShape(4.dp)
                    }
                    Box(
                        modifier = Modifier.size(140.dp).background(bgColor, shape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏃 健身目标达成！", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("步数: 10,000", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item {
            CodeBlockCard(
                block = CodeBlockData(id = "watch_ui", title = "${formFactor.displayName} Watch UI 模板", code = previewCode),
                isExpanded = true,
                isCopied = state.codeCopied,
                onToggle = {},
                onCopy = { onCopyCode("watch_ui") }
            )
        }
    }
}

/**
 * Health API × Health Connect synergy panel.
 */
@Composable
private fun HealthConnectPanel(onIntent: (WearOS7ToolsIntent) -> Unit) {
    val codeBlock = CodeBlockData(
        id = "health_connect_code",
        title = "Health Connect 跨设备同步示例",
        code = """class HealthConnectManager(private val context: Context) {
    suspend fun syncHeartRateToWatch(heartRate: Int) {
        // 1. 写入 Health Connect
        val records = listOf(HeartRateRecord(
            startTime = Instant.now(), endTime = Instant.now(),
            samples = listOf(HeartRateRecord.Sample(Instant.now(), heartRate.toLong()))
        ))
        val client = HealthConnectClient.getOrCreate(context)
        client.insertRecords(records)
        
        // 2. 通过 Wear OS DataLayer 同步到手表
        val dataClient = Wear.getDataClient(context)
        val dataMap = DataMap().apply {
            putLong("heart_rate", heartRate.toLong())
            putLong("timestamp", System.currentTimeMillis())
        }
        val putDataReq = PutDataRequest.Builder("/health/heart_rate")
            .setData(dataMap.asApduEquivalentByteArray()).build()
        dataClient.putDataItem(putDataReq)
    }
}"""
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("健康 API × Health Connect", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("跨设备健康数据同步 SDK，标准化数据读写路径",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            FeatureHighlightCard(icon = Icons.Default.Favorite, title = "心率数据同步",
                description = "Health Connect 作为统一数据源，DataLayer 同步到手表")
        }
        item {
            CodeBlockCard(block = codeBlock, isExpanded = true, isCopied = false,
                onToggle = {}, onCopy = { onIntent(WearOS7ToolsIntent.CopyCode(codeBlock.id)) })
        }
    }
}

/**
 * Compose for Wear OS best practices panel.
 */
@Composable
private fun ComposeWearOSPanel(onIntent: (WearOS7ToolsIntent) -> Unit) {
    val codeBlock = CodeBlockData(
        id = "compose_wearos_code",
        title = "Tiles/Complications/WatchFace Compose 化",
        code = """@Composable
fun WatchFaceScreen(state: WatchFaceState, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 圆形屏幕 — 内容居中，边缘不被裁剪
            maxWidth < 200.dp -> CircularWatchFace(state)
            // 方形/OLED — 左对齐，留出圆形裁剪区域
            else -> SquareWatchFace(state)
        }
    }
}

@Composable
private fun CircularWatchFace(state: WatchFaceState) {
    Box(
        modifier = Modifier.fillMaxSize()
            .clip(CircleShape).background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(state.time, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)
            Text(state.date, color = Color.Gray, fontSize = 12.sp)
        }
    }
}"""
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Compose for Wear OS 最佳实践", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            CodeBlockCard(block = codeBlock, isExpanded = true, isCopied = false,
                onToggle = {}, onCopy = { onIntent(WearOS7ToolsIntent.CopyCode(codeBlock.id)) })
        }
    }
}

/**
 * Multi-screen CI adaptation detector panel.
 */
@Composable
private fun MultiScreenCIPanel(onIntent: (WearOS7ToolsIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("多屏幕尺寸 CI 适配检测", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Gradle 插件，检测 Watch App 在不同屏幕形态下的布局合规性",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            FeatureHighlightCard(icon = Icons.Rounded.GridOn, title = "布局合规性检测",
                description = "检测 Watch 布局在不同屏幕形态（圆形/OLED/方形）下的裁剪/适配问题")
        }
        item {
            FeatureHighlightCard(icon = Icons.Default.Build, title = "Gradle 插件集成",
                description = "在 CI 流水线中自动运行检测，输出合规报告")
        }
    }
}

/**
 * Health API Mock testing framework panel.
 */
@Composable
private fun HealthMockPanel(onIntent: (WearOS7ToolsIntent) -> Unit) {
    val codeBlock = CodeBlockData(
        id = "health_mock_code",
        title = "Health API Mock 测试模板",
        code = """class HeartRateMockTest {
    @Mock private lateinit var recordingClient: RecordingClient

    @Before fun setup() {
        MockitoAnnotations.openMocks(this)
        HealthServicesFacade.setRecordingClient(recordingClient)
    }

    @Test fun testHeartRateRecording() = runTest {
        val mockHeartRate = 72
        whenever(recordingClient.registerCallback(any()))
            .thenAnswer {
                val callback = it.getArgument<RecordingClient.Callback>(0)
                callback.onHeartRateChanged(mockHeartRate.toLong())
            }
        val viewModel = HealthViewModel()
        viewModel.startHeartRateMonitoring()
        assertEquals(mockHeartRate, viewModel.currentHeartRate.value)
    }
}"""
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Health API Mock 测试框架", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            CodeBlockCard(block = codeBlock, isExpanded = true, isCopied = false,
                onToggle = {}, onCopy = { onIntent(WearOS7ToolsIntent.CopyCode(codeBlock.id)) })
        }
    }
}

/**
 * Wear OS 7 API compliance detection panel.
 */
@Composable
private fun CompliancePanel(
    isScanning: Boolean,
    scanProgress: Float,
    scanResults: List<ComplianceResult>,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Wear OS 7 新 API 合规检测", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("扫描中...", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { scanProgress }, modifier = Modifier.fillMaxWidth())
                        Text("${(scanProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        if (scanResults.isNotEmpty() && !isScanning) {
            items(scanResults) { result ->
                ComplianceResultCard(result = result)
            }
        }

        if (scanResults.isEmpty() && !isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("点击右下角按钮开始扫描", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * Compliance result card.
 */
@Composable
private fun ComplianceResultCard(result: ComplianceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.complianceLevel.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.issueDescription,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${result.filePath}${if (result.lineNumber != null) ":${result.lineNumber}" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "建议: ${result.suggestedFix}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Wear OS × Android XR synergy panel.
 */
@Composable
private fun XR协同Panel() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Wear OS × Android XR 协同", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("手表 + XR 眼镜的跨设备场景示例：通知接力/健康数据共享",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            FeatureHighlightCard(icon = Icons.Default.Share, title = "通知接力",
                description = "手表收到通知后，可接力到 XR 眼镜显示")
        }
        item {
            FeatureHighlightCard(icon = Icons.Default.Favorite, title = "健康数据共享",
                description = "Watch 健康数据实时同步到 XR 眼镜显示")
        }
    }
}

// ================================================================
// Reusable Components
// ================================================================

/**
 * Bottom tab bar for tool category switching.
 */
@Composable
private fun BottomTabBar(
    selectedTab: WearOS7Tab,
    onTabSelected: (WearOS7Tab) -> Unit
) {
    NavigationBar {
        WearOS7Tab.entries.forEach { tab ->
            NavigationBarItem(
                icon = { Icon(imageVector = getTabIcon(tab), contentDescription = tab.title) },
                label = { Text(tab.title) },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

/**
 * Code block card with expand/collapse and copy.
 */
@Composable
private fun CodeBlockCard(
    block: CodeBlockData,
    isExpanded: Boolean,
    isCopied: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = block.title, style = MaterialTheme.typography.labelMedium, color = Color(0xFFAAAAAA))
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.ContentCopy else Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            tint = if (isCopied) Color(0xFF4CAF50) else Color(0xFFAAAAAA)
                        )
                    }
                }
            }

            // Code content
            if (isExpanded) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFF2D2D2D))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = block.code,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = Color(0xFFD4D4D4),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Feature highlight card.
 */
@Composable
private fun FeatureHighlightCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ================================================================
// Icon Helpers
// ================================================================

/**
 * Get icon for a tool.
 */
private fun getToolIcon(tool: WearOS7Tool): ImageVector {
    return when (tool) {
        WearOS7Tool.LiveUpdatesGuide -> Icons.Default.Share
        WearOS7Tool.LiveUpdatesUI -> Icons.Default.PlayArrow
        WearOS7Tool.HealthConnect -> Icons.Default.Favorite
        WearOS7Tool.ComposeWearOS -> Icons.Default.Build
        WearOS7Tool.MultiScreenCI -> Icons.Rounded.GridOn
        WearOS7Tool.HealthMock -> Icons.Rounded.MonitorHeart
        WearOS7Tool.Compliance -> Icons.Default.Shield
        WearOS7Tool.XR协同 -> Icons.Default.Share
    }
}

/**
 * Get icon for a tab.
 */
private fun getTabIcon(tab: WearOS7Tab): ImageVector {
    return when (tab) {
        WearOS7Tab.Overview -> Icons.Default.Shield
        WearOS7Tab.LiveUpdates -> Icons.Default.Share
        WearOS7Tab.Health -> Icons.Default.Favorite
        WearOS7Tab.Compose -> Icons.Default.Build
        WearOS7Tab.Compliance -> Icons.Default.Radar
    }
}
