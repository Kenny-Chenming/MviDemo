package com.mvi.kenny.feature.webmcp

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.webmcp.ComplianceLevel

// =============================================================
// WebMcpScreen — WebMCP Android WebView Agent 集成工具包主屏幕
// PRD-262 | WebMCP Android WebView Agent 集成工具包
// =============================================================
// WebMCP Toolkit Main Screen / WebMCP 工具包主屏幕
//
// Pages: Dashboard | Compatibility | API Guide | Migration | Security

/**
 * WebMCP Toolkit Screen / WebMCP 工具包主屏幕
 *
 * Entry point for the WebMCP Toolkit feature.
 * Routes to appropriate page based on currentPage state.
 *
 * @param viewModel WebMCP ViewModel instance
 * @param onNavigateBack Callback when user wants to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebMcpScreen(
    viewModel: WebMcpViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect state from ViewModel / 从 ViewModel 收集状态
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle effects / 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WebMcpEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is WebMcpEffect.CopyToClipboard -> {
                    // Handled via clipboard manager below / 通过剪贴板管理器处理
                }
                is WebMcpEffect.OpenExternalUrl -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(effect.url))
                    context.startActivity(intent)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WebMCP 工具包",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            AnimatedContent(
                targetState = state.currentPage,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "page_transition"
            ) { page ->
                when (page) {
                    ToolkitPage.DASHBOARD -> DashboardPage(
                        state = state,
                        onNavigateTo = { viewModel.processIntent(WebMcpIntent.NavigateTo(it)) }
                    )
                    ToolkitPage.COMPATIBILITY -> CompatibilityPage(
                        state = state,
                        onStartDetection = { viewModel.processIntent(WebMcpIntent.StartDetection(it)) },
                        onClearDetection = { viewModel.processIntent(WebMcpIntent.ClearDetection) }
                    )
                    ToolkitPage.API_GUIDE -> ApiGuidePage(
                        state = state,
                        onCopyCode = { viewModel.processIntent(WebMcpIntent.CopyCode(it)) },
                        onOpenUrl = { viewModel.processIntent(WebMcpIntent.OpenExternalUrl(it)) }
                    )
                    ToolkitPage.MIGRATION -> MigrationPage(
                        state = state,
                        onGoToStep = { viewModel.processIntent(WebMcpIntent.GoToMigrationStep(it)) },
                        onCompleteStep = { viewModel.processIntent(WebMcpIntent.CompleteMigrationStep(it)) },
                        onCopyCode = { viewModel.processIntent(WebMcpIntent.CopyCode(it)) }
                    )
                    ToolkitPage.SECURITY -> SecurityPage(
                        state = state,
                        onAnswer = { q, a -> viewModel.processIntent(WebMcpIntent.AnswerDecisionTree(q, a)) },
                        onReset = { viewModel.processIntent(WebMcpIntent.ResetDecisionTree) }
                    )
                }
            }
        }
    }
}

// =============================================================
// Dashboard Page / 主页
// =============================================================

/**
 * Dashboard page / 工具包主页
 *
 * Shows overview cards for all 4 main sections:
 * Compatibility Detection / API Guide / Migration Tool / Security Config
 */
@Composable
private fun DashboardPage(
    state: WebMcpToolkitState,
    onNavigateTo: (ToolkitPage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header card / 头部信息卡
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "WebMCP Toolkit",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Web Model Context Protocol — Android WebView Agent 集成工具包",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chrome 149+ Origin Trial",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Google + Microsoft 联合提出",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section title / 分区标题
        Text(
            text = "工具导航",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Tool cards grid / 工具卡片网格
        val tools = listOf(
            ToolCardData(
                icon = Icons.Default.VerifiedUser,
                title = "兼容性检测",
                subtitle = "检测 WebView 是否支持 WebMCP\n检测工具声明合规性",
                color = Color(0xFF4CAF50),
                page = ToolkitPage.COMPATIBILITY
            ),
            ToolCardData(
                icon = Icons.Default.Code,
                title = "API 指南",
                subtitle = "window.webMCP.exposeTools()\n完整用法与代码示例",
                color = Color(0xFF2196F3),
                page = ToolkitPage.API_GUIDE
            ),
            ToolCardData(
                icon = Icons.Default.Security,
                title = "迁移工具",
                subtitle = "WebView → Agent-Ready\n分步骤迁移向导",
                color = Color(0xFFFF9800),
                page = ToolkitPage.MIGRATION
            ),
            ToolCardData(
                icon = Icons.Default.Security,
                title = "安全配置",
                subtitle = "Origin 限制与权限控制\nDeep Link vs WebMCP 决策树",
                color = Color(0xFF9C27B0),
                page = ToolkitPage.SECURITY
            )
        )

        tools.forEach { tool ->
            ToolCard(
                data = tool,
                onClick = { onNavigateTo(tool.page) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Quick links section / 快捷链接分区
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "快捷链接",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        QuickLinkCard(
            title = "Chrome Origin Trial 申请",
            url = "https://developer.chrome.com/docs/ai/webmcp",
            onOpen = { /* Open URL */ }
        )
        Spacer(modifier = Modifier.height(8.dp))
        QuickLinkCard(
            title = "MDN WebMCP 文档",
            url = "https://developer.mozilla.org/en-US/docs/Web/API/WebMCP",
            onOpen = { /* Open URL */ }
        )
        Spacer(modifier = Modifier.height(8.dp))
        QuickLinkCard(
            title = "Chrome DevTools WebMCP 调试",
            url = "https://developer.chrome.com/docs/devtools",
            onOpen = { /* Open URL */ }
        )
    }
}

/**
 * Tool card data / 工具卡片数据
 */
private data class ToolCardData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val color: Color,
    val page: ToolkitPage
)

/**
 * Tool card / 工具卡片
 */
@Composable
private fun ToolCard(
    data: ToolCardData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(data.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer()
            )
        }
    }
}

/**
 * Quick link card / 快捷链接卡片
 */
@Composable
private fun QuickLinkCard(
    title: String,
    url: String,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =============================================================
// Compatibility Detection Page / 兼容性检测页
// =============================================================

/**
 * Compatibility detection page / 兼容性检测页
 *
 * Allows user to input a URL and scan it for WebMCP compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompatibilityPage(
    state: WebMcpToolkitState,
    onStartDetection: (String) -> Unit,
    onClearDetection: () -> Unit
) {
    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            text = "WebMCP 合规检测",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "输入 WebView 页面 URL，检测 WebMCP 工具声明合规性",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // URL input / URL 输入框
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("WebView 页面 URL") },
            placeholder = { Text("https://example.com/page") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state.detectionState != DetectionState.SCANNING
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons / 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onStartDetection(urlInput) },
                modifier = Modifier.weight(1f),
                enabled = state.detectionState != DetectionState.SCANNING && urlInput.isNotBlank()
            ) {
                if (state.detectionState == DetectionState.SCANNING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (state.detectionState == DetectionState.SCANNING) "检测中..." else "开始检测")
            }

            if (state.detectionState != DetectionState.IDLE) {
                OutlinedButton(
                    onClick = {
                        onClearDetection()
                        urlInput = ""
                    }
                ) {
                    Text("清除")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Disclaimer / 免责声明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "ℹ️",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "静态分析结果，最终以实际测试为准。无法检测 JavaScript 启用状态等运行时配置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF795548)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Detection results / 检测结果
        state.detectionResult?.let { result ->
            DetectionResultCard(result = result)
        }

        // Detection state indicator / 检测状态指示器
        if (state.detectionState == DetectionState.SCANNING) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在分析页面内容...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

/**
 * Detection result card / 检测结果卡片
 */
@Composable
private fun DetectionResultCard(result: DetectionResult) {
    val statusColor by animateColorAsState(
        targetValue = result.level.color,
        animationSpec = tween(500),
        label = "status_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status header / 状态头部
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (result.level) {
                            ComplianceLevel.COMPLIANT -> Icons.Default.Check
                            else -> Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = result.level.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "URL: ${result.url}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary stats / 摘要统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "工具声明", value = "${result.toolsFound}/${result.totalTools}")
                StatItem(
                    label = "合规等级",
                    value = when (result.level) {
                        ComplianceLevel.COMPLIANT -> "✅ 完全"
                        ComplianceLevel.PARTIALLY_COMPLIANT -> "⚠️ 部分"
                        ComplianceLevel.NON_COMPLIANT -> "❌ 不合规"
                    }
                )
                StatItem(label = "分析类型", value = "静态")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Findings / 发现详情
            Text(
                text = "检测详情",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            result.findings.forEach { finding ->
                FindingItem(finding = finding)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Recommendations / 修复建议
            if (result.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "修复建议",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                result.recommendations.forEachIndexed { index, rec ->
                    Text(
                        text = "$index. $rec",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stat item / 统计项
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

/**
 * Individual finding item / 单个发现条目
 */
@Composable
private fun FindingItem(finding: ComplianceFinding) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = finding.severity.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(finding.severity.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = finding.severity.label.split("—").first().trim(),
                style = MaterialTheme.typography.labelSmall,
                color = finding.severity.color,
                fontSize = 9.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            if (finding.codeLocation != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = finding.codeLocation,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2196F3)
                )
            }
            if (finding.suggestion.isNotBlank() && finding.suggestion != "无需操作") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 ${finding.suggestion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

// =============================================================
// API Guide Page / API 指南页
// =============================================================

/**
 * API Guide page / API 指南页
 *
 * Shows complete WebMCP API usage with code examples.
 */
@Composable
private fun ApiGuidePage(
    state: WebMcpToolkitState,
    onCopyCode: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "WebMCP API 指南",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "完整的 window.webMCP.exposeTools() API 用法与代码示例",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Global Object / 全局对象
        ApiSection(title = "1. 初始化 WebMCP 全局对象") {
            CodeBlock(
                code = """
// JavaScript: 初始化 window.webMCP
window.webMCP = window.webMCP || {};
                """.trimIndent(),
                onCopy = onCopyCode
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "必须在页面加载时首先初始化 window.webMCP 全局对象。",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: exposeTools / 工具声明
        ApiSection(title = "2. exposeTools() 完整用法") {
            CodeBlock(
                code = """
// JavaScript: 声明 WebMCP 工具
window.webMCP.exposeTools({
  tools: [
    {
      // Tool name — Agent 调用时使用
      name: 'queryProducts',
      // Tool description — 帮助 Agent 理解工具用途
      description: 'Query products by category and return results',
      // JSON Schema 格式的参数定义
      parameters: {
        type: 'object',
        properties: {
          category: {
            type: 'string',
            description: 'Product category to search for'
          },
          limit: {
            type: 'number',
            description: 'Maximum number of results',
            default: 10
          }
        },
        required: ['category']  // 必需参数
      }
    }
  ]
});
                """.trimIndent(),
                onCopy = onCopyCode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Android WebView Config / Android WebView 配置
        ApiSection(title = "3. Android WebView 配置 (Kotlin)") {
            CodeBlock(
                code = """
// Kotlin: WebView 基本配置
val webView: WebView = binding.webView
webView.settings.apply {
    javaScriptEnabled = true          // 必须启用 JavaScript
    domStorageEnabled = true          // 启用 DOM 存储
    allowFileAccess = true            // 允许文件访问（如需要）
}

// Kotlin: 添加 JavaScript 接口
webView.addJavascriptInterface(
    WebMcpBridge(),
    "AndroidBridge"
)

// Kotlin: WebMcpBridge 类示例
class WebMcpBridge {
    @JavascriptInterface
    fun onToolCall(toolName: String, params: String) {
        // 处理 Agent 的工具调用
        Log.d("WebMCP", "Tool: ${'$'}toolName, Params: ${'$'}params")
    }
}
                """.trimIndent(),
                onCopy = onCopyCode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: AndroidManifest / 配置
        ApiSection(title = "4. AndroidManifest 权限配置") {
            CodeBlock(
                code = """
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 如需从外部 URL 加载内容 -->
<application
    android:usesCleartextTraffic="true"
    ... >
</application>
                """.trimIndent(),
                onCopy = onCopyCode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 5: Origin Trial / Origin Trial
        ApiSection(title = "5. Origin Trial Token 配置（可选）") {
            CodeBlock(
                code = """
<!-- HTML: 在 <head> 中添加 Origin Trial Token -->
<head>
  <meta http-equiv="origin-trial"
        content="YOUR_CHROME_ORIGIN_TRIAL_TOKEN_HERE">
</head>

<!-- 申请地址: https://developer.chrome.com/docs/ai/webmcp -->
                """.trimIndent(),
                onCopy = onCopyCode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 6: JSON Schema / Schema 详解
        ApiSection(title = "6. exposedTools JSON Schema 详解") {
            val schemaCode = """
// exposedTools 完整 Schema
{
  "tools": [
    {
      "name": String,        // 工具名称，Agent 调用标识
      "description": String,  // 工具描述，帮助 Agent 理解
      "parameters": {         // 参数 Schema（JSON Schema Draft-07）
        "type": "object",
        "properties": {
          "<paramName>": {
            "type": "string" | "number" | "boolean" | "object" | "array",
            "description": "参数描述",
            "default": 默认值,        // 可选
            "enum": [...]            // 可选，枚举值
          }
        },
        "required": ["param1", ...]  // 必需参数列表
      }
    }
  ]
}
            """.trimIndent()
            CodeBlock(code = schemaCode, onCopy = onCopyCode)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // External links / 外部链接
        OutlinedButton(
            onClick = { onOpenUrl("https://developer.chrome.com/docs/ai/webmcp") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("查看 Chrome 官方 WebMCP 文档")
        }
    }
}

/**
 * API section / API 分区
 */
@Composable
private fun ApiSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

/**
 * Code block with copy button / 带复制按钮的代码块
 */
@Composable
private fun CodeBlock(
    code: String,
    onCopy: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "JavaScript / Kotlin",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        onCopy(code)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD4D4D4),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

// =============================================================
// Migration Page / 迁移工具页
// =============================================================

/**
 * Migration page / 迁移工具页
 *
 * Step-by-step guide for migrating WebView to Agent-Ready WebView.
 */
@Composable
private fun MigrationPage(
    state: WebMcpToolkitState,
    onGoToStep: (Int) -> Unit,
    onCompleteStep: (Int) -> Unit,
    onCopyCode: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header / 头部
        Text(
            text = "WebView → Agent-Ready 迁移向导",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "按步骤完成 WebView 的 WebMCP 工具声明",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator / 进度指示器
        val progress by animateFloatAsState(
            targetValue = state.migrationProgress / 100f,
            animationSpec = tween(500),
            label = "progress"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "迁移进度",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${state.completedStepsCount}/${state.migrationSteps.size} 步骤完成",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Step tabs / 步骤标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.migrationSteps.forEachIndexed { index, step ->
                val isActive = index == state.currentMigrationStep
                val isCompleted = step.isCompleted

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onGoToStep(index) },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> Color(0xFF4CAF50)
                        isActive -> Color(0xFF2196F3)
                        else -> Color(0xFFE0E0E0)
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isActive) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current step detail / 当前步骤详情
        val currentStep = state.migrationSteps.getOrNull(state.currentMigrationStep)
        currentStep?.let { step ->
            MigrationStepCard(
                step = step,
                onCopyCode = onCopyCode,
                onComplete = { onCompleteStep(step.stepNumber - 1) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.currentMigrationStep > 0) {
                OutlinedButton(
                    onClick = { onGoToStep(state.currentMigrationStep - 1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("上一步")
                }
            }

            if (currentStep != null && !currentStep.isCompleted) {
                Button(
                    onClick = { onCompleteStep(state.currentMigrationStep) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("标记完成")
                }
            }

            if (state.currentMigrationStep < state.migrationSteps.size - 1) {
                Button(
                    onClick = { onGoToStep(state.currentMigrationStep + 1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("下一步")
                }
            }
        }
    }
}

/**
 * Migration step card / 迁移步骤卡片
 */
@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    onCopyCode: (String) -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Step header / 步骤头部
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (step.isCompleted) Color(0xFF4CAF50)
                            else Color(0xFF2196F3)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${step.stepNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (step.isCompleted) {
                        Text(
                            text = "✅ 已完成",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / 描述
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Code template / 代码模板
            CodeBlock(
                code = step.codeTemplate,
                onCopy = onCopyCode
            )
        }
    }
}

// =============================================================
// Security Page / 安全配置页
// =============================================================

/**
 * Security page / 安全配置页
 *
 * WebMCP security model and Deep Link vs WebMCP decision tree.
 */
@Composable
private fun SecurityPage(
    state: WebMcpToolkitState,
    onAnswer: (Int, Boolean) -> Unit,
    onReset: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "安全配置与决策树",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "WebMCP 安全机制说明与 Deep Link 决策树",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Security model section / 安全模型分区
        SecuritySection(title = "WebMCP 安全模型") {
            SecurityItem(
                title = "Origin 限制",
                description = "WebMCP 工具只能被同 Origin 的页面中的 Agent 调用，防止跨站攻击"
            )
            SecurityItem(
                title = "权限控制",
                description = "Agent 调用 WebMCP 工具时，用户会收到系统通知，可选择拒绝"
            )
            SecurityItem(
                title = "工具声明可见性",
                description = "WebMCP 工具声明是页面级别的，Agent 需要先发现工具才能调用"
            )
            SecurityItem(
                title = "与 Deep Link 的关系",
                description = "WebMCP 处理 Agent 自动化场景，Deep Link 处理用户手动触发场景，两者互补"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Decision tree section / 决策树分区
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Deep Link vs WebMCP 决策树",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "回答问题，找到适合你的方案",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Decision tree content / 决策树内容
        if (state.decisionTreeResult != null) {
            // Show final result / 显示最终结果
            DecisionResultCard(result = state.decisionTreeResult, onReset = onReset)
        } else {
            // Show current question / 显示当前问题
            state.decisionTreeCurrentNode?.let { node ->
                DecisionQuestionCard(
                    node = node,
                    questionIndex = state.decisionTreeAnswers.size,
                    onAnswer = onAnswer
                )
            }
        }

        // Reset button / 重置按钮
        if (state.decisionTreeAnswers.isNotEmpty() || state.decisionTreeResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重新开始决策树")
            }
        }
    }
}

/**
 * Security section / 安全分区
 */
@Composable
private fun SecuritySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

/**
 * Security item / 安全条目
 */
@Composable
private fun SecurityItem(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3))
                    .align(Alignment.Top)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

/**
 * Decision question card / 决策问题卡片
 */
@Composable
private fun DecisionQuestionCard(
    node: DecisionTreeNode,
    questionIndex: Int,
    onAnswer: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Question number / 问题编号
            Text(
                text = "问题 ${questionIndex + 1}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Question text / 问题文本
            Text(
                text = node.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Answer buttons / 答案按钮
            node.options.forEachIndexed { index, option ->
                val isYes = index == 0
                Button(
                    onClick = { onAnswer(questionIndex, isYes) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYes) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    )
                ) {
                    Text(option.answer)
                }
            }
        }
    }
}

/**
 * Decision result card / 决策结果卡片
 */
@Composable
private fun DecisionResultCard(
    result: DecisionResult,
    onReset: () -> Unit
) {
    val resultColor = if (result.useWebMcp) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Result header / 结果头部
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(resultColor.copy(alpha = 0.15f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = result.verdict,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = resultColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.recommendation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reason / 理由
            Text(
                text = "理由",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161)
            )

            // Secondary recommendation / 次要建议
            result.secondaryRecommendation?.let { secondary ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "同时推荐",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recommendation tag / 推荐标签
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (result.useWebMcp) Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else Color(0xFFFF9800).copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (result.useWebMcp) "✅ 建议使用 WebMCP" else "ℹ️ WebMCP 非必需",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (result.useWebMcp) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// =============================================================
// Stub composable to fix compilation / 用于修复编译的 stub
// =============================================================

/**
 * Stub for graphicsLayer import / graphicsLayer 导入桩
 */
@Composable
private fun Modifier.graphicsLayer(): Modifier = this
