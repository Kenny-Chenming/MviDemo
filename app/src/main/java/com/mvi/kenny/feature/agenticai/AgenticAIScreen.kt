package com.mvi.kenny.feature.agenticai

// ================================================================
// AgenticAIScreen — Android Agentic AI AppFunctions 主界面
// ================================================================
// Main screen for Android Agentic AI AppFunctions & UI Automation Framework.
//
// PRD-169: Android Agentic AI AppFunctions & UI Automation Framework 开发工具包
// Design Reference: memory/agency/designs/PRD-169-Android-Agentic-AI-AppFunctions-Framework.md
//
// 9 Tabs:
//   0. Home — IODCountdown + AgenticPathMap + QuickStart
//   1. AppFunctions — 函数定义与接入指南
//   2. Gemini Integration — AppFunctions × Gemini 集成模板
//   3. UI Automation — UI 自动化框架指南
//   4. Design Standards — Agentic App 设计规范
//   5. Security & Privacy — 安全隐私指南
//   6. A2A Protocol — Agent 间通信协议
//   7. CLI Skills — Android CLI Skills 开发指南
//   8. CI Compliance — CI 合规检测报告
// —————————————————————————————————————————————————————————

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow

// ================================================================
// Agentic AI Theme Colors
// ================================================================

private val AgenticPrimary = Color(0xFF6366F1)   // Indigo AI主色
private val AgenticSecondary = Color(0xFF8B5CF6) // Purple 辅助色
private val AgenticTertiary = Color(0xFF06B6D4)  // Cyan 强调色
private val AgenticSurface = Color(0xFF0F172A)   // 深色背景
private val AgenticOnSurface = Color(0xFFE2E8F0) // 浅色文字
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)

// ================================================================
// AgenticAIScreen — 主入口
// ================================================================

@Composable
fun AgenticAIScreen(
    state: AgenticUIState,
    onIntent: (AgenticIntent) -> Unit,
    effect: Flow<AgenticEffect>,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = AgenticTab.entries

    // Collect effects
    LaunchedEffect(Unit) {
        effect.collect { e ->
            when (e) {
                is AgenticEffect.ShowToast -> snackbarHostState.showSnackbar(e.message)
                is AgenticEffect.CodeCopied -> { /* handled by toast */ }
                is AgenticEffect.NavigateToTab -> onIntent(AgenticIntent.SelectTab(e.tabIndex))
                is AgenticEffect.ReportExported -> snackbarHostState.showSnackbar("报告已导出: ${e.filePath}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = DarkSurface,
                contentColor = AgenticPrimary,
                edgePadding = 8.dp,
                modifier = Modifier.height(52.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { onIntent(AgenticIntent.SelectTab(index)) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                Text(
                                    text = tab.subtitle,
                                    fontSize = 9.sp,
                                    color = AgenticOnSurface.copy(alpha = 0.6f),
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (tabs[state.selectedTab]) {
                AgenticTab.HOME -> HomeTab(state = state, onIntent = onIntent)
                AgenticTab.APP_FUNCTIONS -> AppFunctionsTab(onIntent = onIntent)
                AgenticTab.GEMINI_INTEGRATION -> GeminiIntegrationTab(onIntent = onIntent)
                AgenticTab.UI_AUTOMATION -> UIAutomationTab(onIntent = onIntent)
                AgenticTab.DESIGN_STANDARDS -> DesignStandardsTab(state = state, onIntent = onIntent)
                AgenticTab.SECURITY_PRIVACY -> SecurityPrivacyTab(onIntent = onIntent)
                AgenticTab.A2A_PROTOCOL -> A2AProtocolTab(state = state, onIntent = onIntent)
                AgenticTab.CLI_SKILLS -> CLISkillsTab(state = state, onIntent = onIntent)
                AgenticTab.CI_COMPLIANCE -> CIComplianceTab(state = state, onIntent = onIntent)
            }
        }
    }
}

// ================================================================
// Tab 0: Home — Agentic AI 全景图
// ================================================================

@Composable
private fun HomeTab(state: AgenticUIState, onIntent: (AgenticIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // I/O 2026 Countdown Banner
        item {
            IODCountdownBanner(countdown = state.ioCountdown)
        }

        // Agentic Path Map
        item {
            AgenticPathMapCard(
                selectedNode = state.pathMapSelectedNode,
                onNodeClick = { nodeId -> onIntent(AgenticIntent.SelectPathNode(nodeId)) }
            )
        }

        // Quick Start Cards
        item {
            Text(
                text = "快速开始",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStartCard(
                    title = "AppFunctions",
                    description = "将 App 能力暴露给 AI Agent",
                    icon = Icons.Default.Code,
                    color = AgenticPrimary,
                    onClick = { onIntent(AgenticIntent.SelectPathNode("node_app_functions")) },
                    modifier = Modifier.weight(1f)
                )
                QuickStartCard(
                    title = "Gemini 集成",
                    description = "让 App 被 Gemini 发现",
                    icon = Icons.Default.AutoAwesome,
                    color = AgenticSecondary,
                    onClick = { onIntent(AgenticIntent.SelectPathNode("node_gemini")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStartCard(
                    title = "UI Automation",
                    description = "支持 Agent 操作 App UI",
                    icon = Icons.Default.Info,
                    color = AgenticTertiary,
                    onClick = { onIntent(AgenticIntent.SelectPathNode("node_ui_automation")) },
                    modifier = Modifier.weight(1f)
                )
                QuickStartCard(
                    title = "A2A 协议",
                    description = "Agent 间互操作",
                    icon = Icons.Default.Share,
                    color = Color(0xFF10B981),
                    onClick = { onIntent(AgenticIntent.SelectPathNode("node_a2a")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IODCountdownBanner(countdown: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AgenticPrimary, AgenticSecondary)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google I/O 2026",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = countdown.ifEmpty { "加载中..." },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Android Agentic AI 能力全面开放",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AgenticPathMapCard(
    selectedNode: String?,
    onNodeClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Agentic AI 全链路路径图",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Text(
                text = "点击节点跳转到对应模块",
                style = MaterialTheme.typography.bodySmall,
                color = AgenticOnSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Path nodes in flow layout
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PathNodeRow(
                    nodes = listOf(
                        "node_app_functions" to "AppFunctions",
                        "node_gemini" to "Gemini 发现"
                    ),
                    selectedNode = selectedNode,
                    onNodeClick = onNodeClick
                )
                PathConnector()
                PathNodeRow(
                    nodes = listOf(
                        "node_ui_automation" to "UI Automation",
                        "node_a2a" to "A2A 协议"
                    ),
                    selectedNode = selectedNode,
                    onNodeClick = onNodeClick
                )
                PathConnector()
                PathNodeRow(
                    nodes = listOf(
                        "node_design" to "设计规范",
                        "node_security" to "安全隐私"
                    ),
                    selectedNode = selectedNode,
                    onNodeClick = onNodeClick
                )
                PathConnector()
                PathNodeRow(
                    nodes = listOf(
                        "node_cli" to "CLI Skills",
                        "node_ci" to "CI 合规"
                    ),
                    selectedNode = selectedNode,
                    onNodeClick = onNodeClick
                )
            }
        }
    }
}

@Composable
private fun PathNodeRow(
    nodes: List<Pair<String, String>>,
    selectedNode: String?,
    onNodeClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        nodes.forEach { (id, label) ->
            PathNodeChip(
                label = label,
                isSelected = selectedNode == id,
                onClick = { onNodeClick(id) }
            )
        }
    }
}

@Composable
private fun PathNodeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) AgenticPrimary else AgenticPrimary.copy(alpha = 0.2f)
            )
            .border(
                width = 1.dp,
                color = AgenticPrimary,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else AgenticPrimary,
        )
    }
}

@Composable
private fun PathConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(16.dp)
                .background(AgenticPrimary.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun QuickStartCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AgenticOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ================================================================
// Tab 1: AppFunctions 接入指南
// ================================================================

@Composable
private fun AppFunctionsTab(onIntent: (AgenticIntent) -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val exampleCode = """
@AgentFunction(
    id = "order.place",
    name = "placeOrder",
    description = "Placing an order for the user",
    capabilities = ["order.write"],
    requiresConfirmation = true
)
suspend fun placeOrder(
    context: AgentContext,
    params: OrderParams
): OrderResult {
    // Implementation
    return OrderResult(orderId = "ORD_${System.currentTimeMillis()}")
}

@RequiresPermission(Manifest.permission.ORDER_WRITE)
data class OrderParams(
    val productId: String,
    val quantity: Int,
    val address: String
)
    """.trimIndent()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(title = "什么是 AppFunctions？", icon = Icons.Default.Info)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AppFunctions 是 Android 17 引入的全新能力暴露机制，允许 App 将自身功能以结构化函数的形式注册到系统 Agent Runtime，供 Gemini 等 AI Agent 调用执行。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "核心特点：",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    listOf(
                        "📋 结构化函数定义（@AgentFunction 注解）",
                        "🔒 细粒度权限控制（@RequiresPermission）",
                        "✅ 用户确认机制（requiresConfirmation）",
                        "📡 标准发现协议（Gemini 可自动发现）"
                    ).forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = AgenticOnSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "函数定义规范", icon = Icons.Default.Code)
        }

        item {
            CodeExampleBlock(
                title = "AppFunctions Kotlin 示例",
                code = exampleCode,
                language = "kotlin",
                onCopy = { onIntent(AgenticIntent.CopyCode(exampleCode)) }
            )
        }

        item {
            SectionTitle(title = "合规检查清单", icon = Icons.Default.FactCheck)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ChecklistItem(
                        title = "使用 @AgentFunction 注解声明所有可暴露函数",
                        isChecked = true
                    )
                    ChecklistItem(
                        title = "为每个函数提供清晰的 description",
                        isChecked = true
                    )
                    ChecklistItem(
                        title = "敏感操作设置 requiresConfirmation = true",
                        isChecked = false
                    )
                    ChecklistItem(
                        title = "使用 @RequiresPermission 声明权限",
                        isChecked = true
                    )
                    ChecklistItem(
                        title = "在 AndroidManifest.xml 注册 AgentService",
                        isChecked = false
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AgenticPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChecklistItem(title: String, isChecked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isChecked) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isChecked) AgenticOnSurface else AgenticOnSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CodeExampleBlock(
    title: String,
    code: String,
    language: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgenticOnSurface.copy(alpha = 0.7f),
                )
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = AgenticPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // Code content
            SelectionContainer {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF79C0FF),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ================================================================
// Tab 2: AppFunctions × Gemini 集成模板
// ================================================================

private val TEMPLATES = listOf(
    AppFunctionTemplate(
        id = "tpl_001",
        name = "电商购物模板",
        category = "E-Commerce",
        description = "商品搜索、下单、支付、物流查询全流程模板",
        codeSnippet = """
@AgentFunction(id = "ecommerce.search", name = "searchProducts")
suspend fun searchProducts(query: String): List<Product>

@AgentFunction(id = "ecommerce.order", name = "placeOrder")
suspend fun placeOrder(order: OrderRequest): OrderResult

@AgentFunction(id = "ecommerce.pay", name = "initiatePayment")
suspend fun initiatePayment(orderId: String): PaymentIntent
        """.trimIndent()
    ),
    AppFunctionTemplate(
        id = "tpl_002",
        name = "外卖订餐模板",
        category = "Food Delivery",
        description = "餐厅搜索、菜品浏览、下单配送模板",
        codeSnippet = """
@AgentFunction(id = "food.search", name = "searchRestaurants")
suspend fun searchRestaurants(location: Location): List<Restaurant>

@AgentFunction(id = "food.order", name = "placeFoodOrder")
suspend fun placeFoodOrder(order: FoodOrderRequest): FoodOrderResult
        """.trimIndent()
    ),
    AppFunctionTemplate(
        id = "tpl_003",
        name = "出行预订模板",
        category = "Travel",
        description = "机票、酒店、火车票预订模板",
        codeSnippet = """
@AgentFunction(id = "travel.flight", name = "searchFlights")
suspend fun searchFlights(params: FlightSearchParams): List<Flight>

@AgentFunction(id = "travel.hotel", name = "searchHotels")
suspend fun searchHotels(location: Location, checkIn: Date): List<Hotel>
        """.trimIndent()
    ),
    AppFunctionTemplate(
        id = "tpl_004",
        name = "音乐播放模板",
        category = "Music",
        description = "音乐搜索、播放控制、歌单管理模板",
        codeSnippet = """
@AgentFunction(id = "music.search", name = "searchTracks")
suspend fun searchTracks(query: String): List<Track>

@AgentFunction(id = "music.play", name = "playTrack")
suspend fun playTrack(trackId: String): PlaybackState
        """.trimIndent()
    )
)

@Composable
private fun GeminiIntegrationTab(onIntent: (AgenticIntent) -> Unit) {
    var selectedTemplate by remember { mutableStateOf<AppFunctionTemplate?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(title = "AppFunctions × Gemini 发现流程", icon = Icons.Default.AutoAwesome)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemini 发现 App 的标准流程：",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val steps = listOf(
                        "1️⃣ App 通过 AppFunctions 注册能力到系统",
                        "2️⃣ Gemini 查询本地 Agent Registry",
                        "3️⃣ Gemini 获取 Agent Card（包含能力描述）",
                        "4️⃣ 用户授权 Gemini 调用 AppFunctions",
                        "5️⃣ Gemini 代表用户执行操作"
                    )
                    steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgenticOnSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "集成模板库", icon = Icons.Default.Description)
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(TEMPLATES) { template ->
                    TemplateCard(
                        template = template,
                        isSelected = selectedTemplate?.id == template.id,
                        onClick = { selectedTemplate = template }
                    )
                }
            }
        }

        selectedTemplate?.let { template ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF161B22))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                )
                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AgenticOnSurface.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = { onIntent(AgenticIntent.CopyCode(template.codeSnippet)) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = AgenticPrimary
                                )
                            }
                        }
                        SelectionContainer {
                            Text(
                                text = template.codeSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF79C0FF),
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(title = "用户授权流程", icon = Icons.Default.Shield)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AuthorizationStep(
                        step = "1",
                        title = "首次发现授权",
                        description = "Gemini 首次发现 App 时，请求用户授权"
                    )
                    AuthorizationStep(
                        step = "2",
                        title = "能力级别授权",
                        description = "用户可为不同能力设置不同权限级别"
                    )
                    AuthorizationStep(
                        step = "3",
                        title = "敏感操作二次确认",
                        description = "支付、删除等操作需要每次单独确认"
                    )
                    AuthorizationStep(
                        step = "4",
                        title = "权限撤回",
                        description = "用户可在系统设置中随时撤回授权"
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: AppFunctionTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AgenticPrimary.copy(alpha = 0.2f) else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AgenticPrimary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = template.category,
                style = MaterialTheme.typography.labelSmall,
                color = AgenticPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = AgenticOnSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AuthorizationStep(step: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AgenticPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AgenticOnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ================================================================
// Tab 3: UI Automation Framework
// ================================================================

@Composable
private fun UIAutomationTab(onIntent: (AgenticIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(title = "UI Automation Framework 概述", icon = Icons.Default.AccountTree)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UI Automation Framework 是 Android 17 引入的核心能力，允许 AI Agent 以无障碍服务的方式读取和操作 App UI。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "工作原理：",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Agent 通过系统 AccessibilityService 获取 App UI 树结构（AccessibilityNodeInfo），然后通过注入 PointerEvent 模拟用户操作（点击、滑动、输入）。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                }
            }
        }

        item {
            SectionTitle(title = "Agent 操作流程", icon = Icons.Default.PlayArrow)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val steps = listOf(
                        Pair("1. UI 读取", "Agent 通过 AccessibilityService 遍历 UI 树，找到目标节点"),
                        Pair("2. 意图理解", "Gemini 理解用户指令，确定需要执行的操作序列"),
                        Pair("3. 操作模拟", "Agent 通过 AccessibilityNodeInfo.performAction 注入操作"),
                        Pair("4. 结果验证", "Agent 读取更新后的 UI，确认操作结果")
                    )
                    steps.forEach { (title, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AgenticTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AgenticOnSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            AppDesignChecklist(onIntent = onIntent)
        }
    }
}

@Composable
private fun AppDesignChecklist(onIntent: (AgenticIntent) -> Unit) {
    val checklistItems = listOf(
        "所有交互元素设置 contentDescription（供 Agent 读取）",
        "关键操作按钮支持 TalkBack 和 Agent 发现",
        "表单验证错误信息通过 UI 文本展示，不只依赖颜色",
        "复杂列表项提供稳定的 View#ID，便于 Agent 定位",
        "支持通过 adb shell uiauto dump 导出完整 UI 树",
        "敏感 UI 区域标记 ROLE_MASK，避免 Agent 误操作"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            checklistItems.forEach { item ->
                ChecklistItem(title = item, isChecked = false)
            }
        }
    }
}

// ================================================================
// Tab 4: Agentic App 设计规范
// ================================================================

@Composable
private fun DesignStandardsTab(state: AgenticUIState, onIntent: (AgenticIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionTitle(title = "Agentic App 设计原则", icon = Icons.Default.AutoAwesome)
        }

        item {
            Text(
                text = "透明性 · 授权 · 可控性",
                style = MaterialTheme.typography.titleMedium,
                color = AgenticPrimary,
            )
        }

        item {
            PrincipleCard(
                title = "透明性原则",
                emoji = "🔍",
                description = "AI Agent 代表用户操作时，App 应清晰告知用户正在发生什么",
                items = listOf(
                    "展示当前 Agent 操作的状态和进度",
                    "通过 Notification 通知用户重要操作",
                    "Agent Card 准确描述 App 能力边界",
                    "操作结果和状态变更及时同步给用户"
                )
            )
        }

        item {
            PrincipleCard(
                title = "用户授权原则",
                emoji = "🔐",
                description = "敏感操作必须经用户明确授权，遵循最小权限原则",
                items = listOf(
                    "支付、删除等操作需要二次确认",
                    "用户可随时撤回 Agent 操作权限",
                    "按功能粒度授权，不过度授权",
                    "授权记录可查可导出"
                )
            )
        }

        item {
            PrincipleCard(
                title = "可控性原则",
                emoji = "🎛️",
                description = "用户始终保持对 App 的完全控制权",
                items = listOf(
                    "Agent 操作可随时取消或回退",
                    "完整的操作审计日志",
                    "合理的超时保护机制",
                    "清晰的错误处理和恢复策略"
                )
            )
        }

        item {
            SectionTitle(title = "UX 最佳实践示例", icon = Icons.Default.Info)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val patterns = listOf(
                        Triple("💳 支付确认", "在支付前显示清晰的金额、收款方、用途信息，并要求用户输入密码或生物识别确认", "Agent 无法绕过支付确认流程"),
                        Triple("📂 数据访问", "读取联系人、照片等数据前，展示具体会读取哪些数据并获得用户授权", "Agent 只能访问用户明确授权的数据"),
                        Triple("🛒 操作取消", "任何进行中的 Agent 操作都提供取消按钮，用户可随时中断", "支持 graceful cancellation，不留脏数据")
                    )
                    patterns.forEach { (title, desc, note) ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(text = title, style = MaterialTheme.typography.titleSmall, color = AgenticPrimary, fontWeight = FontWeight.Bold)
                            Text(text = desc, style = MaterialTheme.typography.bodyMedium, color = AgenticOnSurface)
                            Text(text = "✓ $note", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(title = "设计检查清单", icon = Icons.Default.FactCheck)
        }

        items(state.checklistItems.take(6)) { item ->
            DesignChecklistCard(
                item = item,
                onToggle = { onIntent(AgenticIntent.ToggleChecklistItem(item.id)) }
            )
        }
    }
}

@Composable
private fun PrincipleCard(
    title: String,
    emoji: String,
    description: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AgenticOnSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "•", color = AgenticPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface)
                }
            }
        }
    }
}

@Composable
private fun DesignChecklistCard(
    item: DesignChecklistItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.isChecked) Color(0xFF4CAF50) else Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AgenticOnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = AgenticPrimary,
                )
            }
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

// ================================================================
// Tab 5: 安全隐私指南
// ================================================================

@Composable
private fun SecurityPrivacyTab(onIntent: (AgenticIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle(title = "安全边界", icon = Icons.Default.Security) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当 AI Agent 代表用户操作 App 时，安全边界至关重要：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "🛡️ Agent 只能操作用户已授权的功能",
                        "🔒 敏感数据访问必须经过用户明确同意",
                        "⏱️ 所有操作都应设置合理的超时限制",
                        "📝 完整的操作审计日志不可缺失"
                    ).forEach { item ->
                        Text(text = item, style = MaterialTheme.typography.bodyMedium, color = AgenticOnSurface, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        item { SectionTitle(title = "权限分级矩阵", icon = Icons.Default.Lock) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    PermissionRow("NORMAL", "普通数据访问", "自动授权", Color(0xFF4CAF50))
                    PermissionRow("ELEVATED", "写入操作、位置数据", "需用户确认", Color(0xFFFF9800))
                    PermissionRow("CRITICAL", "支付、删除、密码", "每次单独授权", Color(0xFFF44336))
                }
            }
        }

        item { SectionTitle(title = "用户确认对话框规范", icon = Icons.Default.Info) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val specs = listOf(
                        "显示操作类型和具体内容（如「将向商家支付 ¥128.00」）",
                        "明确告知正在由 AI Agent 代为执行",
                        "提供「允许」「取消」「不再提示」三个选项",
                        "敏感操作使用系统原生确认对话框"
                    )
                    specs.forEachIndexed { index, spec ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = spec, style = MaterialTheme.typography.bodyMedium, color = AgenticOnSurface)
                        }
                    }
                }
            }
        }

        item { SectionTitle(title = "审计日志规范", icon = Icons.Default.Description) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "审计日志必须包含以下字段：", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val fields = listOf("时间戳 (timestamp)", "Agent ID / 名称", "操作用户 ID", "操作类型 (operation_type)", "操作目标 (target_resource)", "操作结果 (success/fail)", "请求参数 (params, 脱敏后)")
                    fields.forEach { field ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(text = "•", color = AgenticPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(level: String, desc: String, auth: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface)
        }
    }
}

// ================================================================
// Tab 6: A2A Protocol
// ================================================================

@Composable
private fun A2AProtocolTab(state: AgenticUIState, onIntent: (AgenticIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle(title = "A2A Protocol 原理", icon = Icons.Default.Share) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Agent-to-Agent (A2A) Protocol 是 Android 17 引入的跨 App Agent 通信协议，允许不同 App 的 Agent 之间相互发现、协作完成任务。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "核心概念：", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    listOf("🔍 Agent Discovery — 通过 Agent Registry 发现其他 Agent", "📋 Agent Card — 每个 Agent 的能力描述文件（JSON）", "📨 JSON-RPC — Agent 间通信采用 JSON-RPC 2.0 格式", "🔄 Task State — 支持长时任务的进度跟踪").forEach {
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        item { SectionTitle(title = "A2A 通信流程", icon = Icons.Default.AccountTree) }

        item { ProtocolFlowDiagram() }

        item { SectionTitle(title = "Agent Card 示例", icon = Icons.Default.Info) }

        item {
            val agentCardJson = """
{
  \"name\": \"ShoppingAssistant\",
  \"description\": \"E-commerce shopping agent\",
  \"version\": \"1.0.0\",
  \"capabilities\": [\"order.write\", \"payment.initiate\"],
  \"skills\": [\"product.search\", \"price.compare\"],
  \"endpoints\": {
    \"a2a\": \"https://app.example.com/a2a\",
    \"events\": \"wss://app.example.com/a2a/events\"
  }
}
            """.trimIndent()
            CodeExampleBlock(
                title = "Agent Card (agent.json)",
                code = agentCardJson,
                language = "json",
                onCopy = { onIntent(AgenticIntent.CopyCode(agentCardJson)) }
            )
        }

        item { SectionTitle(title = "适用场景", icon = Icons.Default.AutoAwesome) }

        items(state.agentCards) { card ->
            AgentCardItem(card = card)
        }
    }
}

@Composable
private fun ProtocolFlowDiagram() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FlowStep(from = "Agent A", to = "Agent Registry", action = "Register / Discover", color = AgenticPrimary)
            FlowArrow()
            FlowStep(from = "Agent A", to = "Agent B", action = "JSON-RPC Request (A2A)", color = AgenticSecondary)
            FlowArrow()
            FlowStep(from = "Agent B", to = "Agent A", action = "JSON-RPC Response / Task Update", color = AgenticTertiary)
        }
    }
}

@Composable
private fun FlowStep(from: String, to: String, action: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
        }
    }
}

@Composable
private fun FlowArrow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "▼", color = AgenticPrimary)
    }
}

@Composable
private fun AgentCardItem(card: AgentCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            }
            Text(text = card.description, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Capabilities:", style = MaterialTheme.typography.labelSmall, color = AgenticSecondary)
            card.capabilities.take(3).forEach { cap ->
            }
        }
    }
}

// ================================================================
// Tab 7: CLI Skills 开发指南
// ================================================================

@Composable
private fun CLISkillsTab(state: AgenticUIState, onIntent: (AgenticIntent) -> Unit) {
    var selectedSkill by remember { mutableStateOf<SkillDefinition?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle(title = "android/skills 仓库", icon = Icons.Default.Terminal) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "android/skills 是一个 GitHub 仓库，包含了 Android App 的 Skill 定义文件（SKILL.md），供 Android Studio Agent Mode 和命令行工具使用。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📁 仓库结构: android/skills/<package_name>/SKILL.md",
                        style = MaterialTheme.typography.bodySmall,
                        color = AgenticTertiary,
                    )
                }
            }
        }

        item { SectionTitle(title = "Skill 定义编辑器", icon = Icons.Default.Code) }

        item {
            val skillMd = """
# SKILL.md — AppFunctions CLI Skill

## Basic Information
- **Name**: com.example.app
- **Description**: Example App functions
- **Category**: AppFunctions
- **Version**: 1.0.0

## Functions
| ID | Name | Description | Confirmation |
|----|------|-------------|---------------|
| order.place | Place Order | Placing an order | true |
| order.cancel | Cancel Order | Cancelling an order | true |
| product.search | Search Products | Search product catalog | false |

## CLI Commands
\`\`\`bash
adb shell app_functions register com.example.app
adb shell app_functions invoke order.place --params '{...}'
\`\`\`
            """.trimIndent()
            CodeExampleBlock(
                title = "SKILL.md 示例",
                code = skillMd,
                language = "markdown",
                onCopy = { onIntent(AgenticIntent.CopyCode(skillMd)) }
            )
        }

        item { SectionTitle(title = "可用的 CLI Skills", icon = Icons.Default.Terminal) }

        items(state.skillDefinitions) { skill ->
            SkillDefinitionCard(
                skill = skill,
                isSelected = selectedSkill?.id == skill.id,
                onClick = { selectedSkill = skill },
                onCopyCommand = { cmd -> onIntent(AgenticIntent.CopyCode(cmd)) }
            )
        }
    }
}

@Composable
private fun SkillDefinitionCard(
    skill: SkillDefinition,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCopyCommand: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AgenticPrimary.copy(alpha = 0.15f) else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AgenticPrimary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = skill.description, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface.copy(alpha = 0.8f))
                }
            }
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                skill.commands.forEach { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1117), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onCopyCommand(cmd) }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = AgenticPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 8: CI 合规检测
// ================================================================

@Composable
private fun CIComplianceTab(state: AgenticUIState, onIntent: (AgenticIntent) -> Unit) {
    var packageName by remember { mutableStateOf("com.example.myapp") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionTitle(title = "CI 合规检测概述", icon = Icons.Default.FactCheck) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "自动化检测 App 是否正确实现了 Android Agentic AI 能力（AppFunctions、UI Automation、A2A Protocol 等），并生成合规报告。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AgenticOnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AgenticTertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "输入 App Package Name 即可开始检测", style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { onIntent(AgenticIntent.RunComplianceCheck(packageName)) },
                    colors = ButtonDefaults.buttonColors(containerColor = AgenticPrimary),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("检测")
                    }
                }
            }
        }

        // Compliance Score Gauge
        if (state.complianceChecks.isNotEmpty()) {
            item {
                ComplianceScoreCard(score = state.complianceScore)
            }

            item { SectionTitle(title = "检测结果明细", icon = Icons.Default.FactCheck) }

            items(state.complianceChecks) { check ->
                ComplianceCheckCard(check = check)
            }

            item {
                SectionTitle(title = "导出报告", icon = Icons.Default.Download)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("pdf", "json", "md").forEach { format ->
                        OutlinedButton(
                            onClick = { onIntent(AgenticIntent.ExportReport(format)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AgenticPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplianceScoreCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComplianceScoreGauge(score = score, modifier = Modifier.size(100.dp))
            Column {
                Text(text = "合规评分", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = scoreLabel(score), style = MaterialTheme.typography.bodySmall, color = scoreColor(score))
            }
        }
    }
}

@Composable
private fun ComplianceScoreGauge(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(targetValue = score.toFloat(), animationSpec = tween(1000), label = "score")
    val scoreColor = scoreColor(score)

    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        drawArc(
            color = Color(0xFF2A2A2A),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = scoreColor,
            startAngle = 135f,
            sweepAngle = 270f * (animatedScore / 100f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Color(0xFF4CAF50)
    score >= 60 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}

private fun scoreLabel(score: Int): String = when {
    score >= 80 -> "✅ 良好 — 符合 Agentic AI 规范"
    score >= 60 -> "⚠️ 需改进 — 部分合规项需要修复"
    else -> "❌ 不合规 — 存在多项严重问题"
}

@Composable
private fun ComplianceCheckCard(check: ComplianceCheckItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (check.status) {
                    ComplianceStatus.PASS -> Icons.Default.CheckCircle
                    ComplianceStatus.FAIL -> Icons.Default.Warning
                    ComplianceStatus.WARNING -> Icons.Default.Info
                    ComplianceStatus.SKIP -> Icons.Default.Info
                },
                contentDescription = null,
                tint = check.status.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = check.title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = check.description, style = MaterialTheme.typography.bodySmall, color = AgenticOnSurface.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "💡 ${check.suggestion}", style = MaterialTheme.typography.bodySmall, color = AgenticTertiary)
            }
            Text(text = check.status.emoji, style = MaterialTheme.typography.titleMedium)
        }
    }
}
