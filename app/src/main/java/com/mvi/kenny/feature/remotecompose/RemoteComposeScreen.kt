package com.mvi.kenny.feature.remotecompose

// ================================================================
// RemoteComposeScreen — AndroidX Remote Compose 服务器驱动 UI 开发工具包 Screen
// ================================================================
// Developer toolkit browser screen for AndroidX Remote Compose.
//
// PRD-191: AndroidX Remote Compose 服务器驱动 UI 开发工具包
// Design Reference: memory/agency/designs/PRD-191-AndroidX-Remote-Compose-开发工具包.md
//
// This screen provides a developer-toolkit browser for:
//   - 10 tool sections organized by module (client/server/security/CI/etc.)
//   - Code viewer with syntax annotations
//   - Demo mode for RemoteComposePlayer behavior
//   - Search and bookmark functionality
//
// Visual Style:
//   - Clean card-based layout with section headers
//   - Code viewer with line numbers and annotations
//   - Terminal-style demo output for RemoteComposePlayer
//   - Material 3 theming throughout
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.roundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mvi.kenny.feature.remotecompose.RemoteComposeContract.Companion.RemoteComposeSection
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Screen — Main Composable
// ================================================================

/**
 * ============================================================
 * RemoteComposeScreen — Main Screen Composable
 * ============================================================
 * Developer toolkit browser for AndroidX Remote Compose.
 *
 * @param viewModel RemoteComposeViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteComposeScreen(
    viewModel: RemoteComposeViewModel = remember { RemoteComposeViewModel() }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collect effects in LaunchedEffect
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collectLatest { effect ->
                when (effect) {
                    is RemoteComposeEffect.CodeCopied -> {
                        // Clipboard copy is handled by the code viewer component
                    }
                    is RemoteComposeEffect.ShowToast -> {
                        Toast.makeText(
                            context,
                            effect.message,
                            if (effect.isSuccess) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                        ).show()
                    }
                    is RemoteComposeEffect.LogAnalytics -> {
                        // In production: send to analytics service
                        // println("[Analytics] ${effect.event}: ${effect.params}")
                    }
                    is RemoteComposeEffect.Error -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                    }
                    is RemoteComposeEffect.OpenExternalDoc -> {
                        // In production: open external browser
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Remote Compose",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "AndroidX 服务器驱动 UI 开发工具包",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // 搜索按钮
                    IconButton(onClick = { /* TODO: Toggle search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索 / Search")
                    }
                    // Demo 按钮
                    AssistChip(
                        onClick = {
                            viewModel.processIntent(
                                RemoteComposeIntent.SimulatePayloadDownload(
                                    "https://api.example.com/remote-ui/v1/payload"
                                )
                            )
                        },
                        label = { Text("Demo") },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Payload 状态演示条
            // Payload status demo bar
            PayloadStatusBar(
                status = state.payloadStatus,
                onDismiss = { viewModel.processIntent(RemoteComposeIntent.DismissError) }
            )

            // 主 Tab
            MainTabs(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.processIntent(RemoteComposeIntent.SwitchTab(it)) }
            )

            // Tab 内容
            when (state.activeTab) {
                MainTab.TOOLS -> ToolsTabContent(
                    state = state,
                    viewModel = viewModel,
                    onIntent = { viewModel.processIntent(it) }
                )
                MainTab.DECISION -> DecisionTabContent(
                    state = state,
                    viewModel = viewModel,
                    onIntent = { viewModel.processIntent(it) }
                )
                MainTab.FALLBACK -> FallbackTabContent(
                    state = state,
                    viewModel = viewModel,
                    onIntent = { viewModel.processIntent(it) }
                )
            }
        }

        // 代码查看器弹窗
        // Code viewer dialog
        if (state.isCodeViewerVisible && state.selectedTool != null) {
            CodeViewerDialog(
                tool = state.selectedTool!!,
                code = state.codeViewerContent,
                language = state.codeViewerLanguage,
                onDismiss = { viewModel.processIntent(RemoteComposeIntent.CloseCodeViewer) },
                onCopy = { code ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("code", code)
                    clipboard.setPrimaryClip(clip)
                    viewModel.processIntent(RemoteComposeIntent.CopyCode(code))
                }
            )
        }
    }
}

// ================================================================
// Payload Status Bar
// ================================================================

/**
 * Payload 状态演示条
 * Shows RemoteComposePlayer simulation status
 */
@Composable
private fun PayloadStatusBar(
    status: PayloadStatus,
    onDismiss: () -> Unit
) {
    val (bgColor, contentColor, icon, message) = when (status) {
        PayloadStatus.IDLE -> null
        PayloadStatus.DOWNLOADING -> listOf(
            Color(0xFF1E3A5F), Color.White, Icons.Default.Download,
            "正在下载 Skia Payload... / Downloading Skia Payload..."
        )
        PayloadStatus.READY -> listOf(
            Color(0xFF1B4332), Color.White, Icons.Default.Info,
            "✅ Payload 就绪，RemoteComposePlayer 渲染成功 / Payload ready, RemoteComposePlayer rendering success"
        )
        PayloadStatus.ERROR -> listOf(
            Color(0xFF5C1B1B), Color.White, Icons.Default.Error,
            "❌ Payload 下载失败，请检查网络 / Download failed, check network"
        )
        PayloadStatus.DEPRECATED -> listOf(
            Color(0xFF4A3A1B), Color.White, Icons.Default.Warning,
            "⚠️ Payload 版本过旧，请更新 / Payload version deprecated, please update"
        )
    }

    if (bgColor != null && contentColor != null && icon != null && message != null) {
        @Suppress("UNCHECKED_CAST")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = bgColor as Color,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status == PayloadStatus.DOWNLOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message as String,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor as Color,
                    modifier = Modifier.weight(1f)
                )
                if (status == PayloadStatus.ERROR || status == PayloadStatus.DEPRECATED) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭 / Close",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Main Tabs
// ================================================================

@Composable
private fun MainTabs(
    activeTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    val tabs = MainTab.entries
    val selectedIndex = tabs.indexOf(activeTab)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp,
        divider = {
            HorizontalDivider()
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tab.titleCn)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.titleEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

// ================================================================
// Tools Tab Content
// ================================================================

@Composable
private fun ToolsTabContent(
    state: RemoteComposeState,
    viewModel: RemoteComposeViewModel,
    onIntent: (RemoteComposeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 搜索栏
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(RemoteComposeIntent.UpdateSearch(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索工具... / Search tools...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onIntent(RemoteComposeIntent.UpdateSearch("")) }) {
                            Icon(Icons.Default.Close, contentDescription = "清除 / Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 模块列表
        val filteredTools = if (state.searchQuery.isEmpty()) {
            // Show all sections
            items(RemoteComposeSection.entries.sortedBy { it.priority }) { section ->
                SectionCard(
                    section = section,
                    isExpanded = section in state.expandedSections,
                    selectedTool = if (section == state.selectedSection) state.selectedTool else null,
                    onToggle = { onIntent(RemoteComposeIntent.ToggleSection(section)) },
                    onSelectSection = { onIntent(RemoteComposeIntent.SelectSection(section)) },
                    onSelectTool = { tool -> onIntent(RemoteComposeIntent.SelectTool(tool)) },
                    onToggleBookmark = { toolId -> onIntent(RemoteComposeIntent.ToggleBookmark(toolId)) },
                    isBookmarked = { viewModel.isToolBookmarked(it) }
                )
            }
        } else {
            // Search mode: show flat list of matching tools
            val allFiltered = viewModel.getAllToolsFiltered()
            items(allFiltered) { tool ->
                val section = RemoteComposeSection.entries.first {
                    tool.id.startsWith(it.name.lowercase().split(" ").first().take(4))
                }
                ToolCard(
                    tool = tool,
                    section = section,
                    isBookmarked = viewModel.isToolBookmarked(tool.id),
                    onSelect = { onIntent(RemoteComposeIntent.SelectTool(tool)) },
                    onToggleBookmark = { onIntent(RemoteComposeIntent.ToggleBookmark(tool.id)) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ================================================================
// Section Card
// ================================================================

@Composable
private fun SectionCard(
    section: RemoteComposeSection,
    isExpanded: Boolean,
    selectedTool: RemoteComposeTool?,
    onToggle: () -> Unit,
    onSelectSection: () -> Unit,
    onSelectTool: (RemoteComposeTool) -> Unit,
    onToggleBookmark: (String) -> Unit,
    isBookmarked: (String) -> Boolean
) {
    val tools = getToolsForSection(section)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp)
    ) {
        Column {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectSection()
                        onToggle()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji
                Text(text = section.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))

                // Title and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.titleCn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = section.titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge: tool count
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("${tools.size}")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "折叠 / Collapse" else "展开 / Expand"
                )
            }

            // Tools list (expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider()
                    tools.forEachIndexed { index, tool ->
                        ToolCard(
                            tool = tool,
                            section = section,
                            isBookmarked = isBookmarked(tool.id),
                            onSelect = { onSelectTool(tool) },
                            onToggleBookmark = { onToggleBookmark(tool.id) }
                        )
                        if (index < tools.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Tool Card
// ================================================================

@Composable
private fun ToolCard(
    tool: RemoteComposeTool,
    section: RemoteComposeSection,
    isBookmarked: Boolean,
    onSelect: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Language chip
        AssistChip(
            onClick = { },
            label = {
                Text(
                    text = tool.language.displayName,
                    style = MaterialTheme.typography.labelSmall
                )
            },
            modifier = Modifier.height(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Tool name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tool.nameCn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = tool.nameEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bookmark button
        IconButton(onClick = onToggleBookmark) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isBookmarked) "取消收藏 / Remove bookmark" else "收藏 / Bookmark",
                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // View code button
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = "查看代码 / View code",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ================================================================
// Decision Tab Content
// ================================================================

@Composable
private fun DecisionTabContent(
    state: RemoteComposeState,
    viewModel: RemoteComposeViewModel,
    onIntent: (RemoteComposeIntent) -> Unit
) {
    val decisionSection = RemoteComposeSection.DECISION_TREE
    val tools = getToolsForSection(decisionSection)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Remote Compose vs JSON SDUI 选型决策树",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            DecisionCard(
                title = "核心区别",
                emoji = "🔑",
                items = listOf(
                    Triple("渲染方式", "Remote Compose: Skia 二进制指令", "JSON SDUI: JSON widget tree"),
                    Triple("性能", "Remote Compose: 接近原生", "JSON SDUI: 需要解析 + 构建 widget tree"),
                    Triple("Payload 大小", "Remote Compose: 100KB-500KB（Skia 二进制较大）", "JSON SDUI: 10-50KB（纯数据）"),
                    Triple("跨平台", "Remote Compose: 仅 Android", "JSON SDUI: 跨平台（iOS/Android/Web）"),
                    Triple("成熟度", "Remote Compose: alpha06（实验性）", "JSON SDUI: 成熟稳定")
                )
            )
        }

        item {
            DecisionCard(
                title = "选择 Remote Compose 当且仅当：",
                emoji = "✅",
                items = listOf(
                    Triple("✓", "团队使用 Jetpack Compose", "享受 Compose 开发者体验"),
                    Triple("✓", "极致渲染性能是关键指标", "60fps、复杂动画、电商列表"),
                    Triple("✓", "Payload 大小可接受", "Wi-Fi/5G 环境，弱网可降级"),
                    Triple("✓", "Android 独占即可", "无需 iOS/Web 支持")
                )
            )
        }

        item {
            DecisionCard(
                title = "选择 JSON SDUI 当：",
                emoji = "📋",
                items = listOf(
                    Triple("→", "跨平台支持是刚需", "iOS + Android + Web"),
                    Triple("→", "团队主要是 Web/后端背景", "不熟悉 Compose"),
                    Triple("→", "弱网场景为主", "2G/3G 环境，Payload 必须小"),
                    Triple("→", "需要成熟的生态和工具链", "已有 JSON SDUI 基础设施")
                )
            )
        }

        item {
            // Code viewer button for decision tree
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tools.firstOrNull()?.let { tool ->
                            onIntent(RemoteComposeIntent.SelectTool(tool))
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("查看完整决策树代码", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Decision Tree Markdown + ASCII Flow Chart",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun DecisionCard(
    title: String,
    emoji: String,
    items: List<Triple<String, String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { (label, remoteCompose, jsonSdui) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(80.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RC: $remoteCompose",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1A73E8)
                        )
                        Text(
                            text = "JSON: $jsonSdui",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5F6368)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ================================================================
// Fallback Tab Content
// ================================================================

@Composable
private fun FallbackTabContent(
    state: RemoteComposeState,
    viewModel: RemoteComposeViewModel,
    onIntent: (RemoteComposeIntent) -> Unit
) {
    val fallbackSection = RemoteComposeSection.FALLBACK_STRATEGY
    val tools = getToolsForSection(fallbackSection)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "降级策略与错误处理",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            FallbackScenarioCard(
                scenario = "网络失败 / Network Failure",
                description = "Payload 下载失败时，优先使用缓存内容，同时异步重试",
                retryStrategy = "指数退避：1s → 2s → 4s，最多 3 次",
                status = "必须实现 / Must Implement"
            )
        }

        item {
            FallbackScenarioCard(
                scenario = "版本不兼容 / Version Mismatch",
                description = "客户端 alpha06 与服务器下发 Payload 版本不兼容",
                retryStrategy = "展示降级 UI，引导用户更新到最新 App 版本",
                status = "必须实现 / Must Implement"
            )
        }

        item {
            FallbackScenarioCard(
                scenario = "渲染崩溃 / Render Crash",
                description = "RemoteComposePlayer 在渲染时崩溃",
                retryStrategy = "捕获异常，展示错误信息，上报崩溃日志",
                status = "必须实现 / Must Implement"
            )
        }

        item {
            FallbackScenarioCard(
                scenario = "Payload 损坏 / Payload Corrupt",
                description = "Skia 二进制 Payload 在网络传输中损坏",
                retryStrategy = "验签失败时拒绝 Payload，使用缓存或展示错误",
                status = "必须实现 / Must Implement"
            )
        }

        item {
            // View code templates
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tools.firstOrNull()?.let { tool ->
                            onIntent(RemoteComposeIntent.SelectTool(tool))
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("查看 Fallback UI + 网络失败处理器代码", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Fallback UI Template + NetworkFailureHandler Kotlin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun FallbackScenarioCard(
    scenario: String,
    description: String,
    retryStrategy: String,
    status: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = scenario,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "策略：$retryStrategy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ================================================================
// Code Viewer Dialog
// ================================================================

@Composable
private fun CodeViewerDialog(
    tool: RemoteComposeTool,
    code: String,
    language: CodeLanguage,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.nameCn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tool.nameEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    // Language badge
                    AssistChip(
                        onClick = { },
                        label = { Text(language.displayName) }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Copy button
                    IconButton(onClick = { onCopy(code) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制 / Copy")
                    }

                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭 / Close")
                    }
                }

                // Annotations bar
                if (tool.annotations.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        tool.annotations.forEach { annotation ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${annotation.lineNumber}:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = "${annotation.type.emoji} ${annotation.text}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = annotation.type.color
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }

                // Code content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val lines = code.lines()
                    val lineCount = lines.size
                    val lineNumberWidth = lineCount.toString().length

                    Row {
                        // Line numbers
                        Column(
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            lines.forEachIndexed { index, _ ->
                                Text(
                                    text = (index + 1).toString().padStart(lineNumberWidth),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF858585)
                                )
                            }
                        }

                        // Code
                        Column {
                            lines.forEach { line ->
                                Text(
                                    text = line.ifEmpty { " " },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFD4D4D4)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
