package com.mvi.kenny.feature.xrsdkdevkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * XrDevKitScreen — Android XR SDK DP4 开发工具包主屏幕
 * XrDevKitScreen — Android XR SDK DP4 Dev Toolkit Main Screen
 * ============================================================
 * 11-Tab MVI screen for Android XR SDK Developer Preview 4 toolkit.
 *
 * Tabs: Overview | Compose XR | Glimmer | SceneCore | ARCore XR |
 *       Simulator | Audio Glasses | Display Glasses | Gemini × XR |
 *       Decision Tree | Catalyst
 *
 * @param onUpdateTopBar Update parent TopBar callback / 更新父 TopBar 回调
 * @param viewModel ViewModel instance / ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XrDevKitScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: XrDevKitViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar when tab changes / Tab 变化时更新 TopBar
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(TopBarConfig(
            title = "Android XR SDK DP4 · ${state.currentTab.titleZh}",
            actions = listOf(
                TopBarAction(
                    icon = if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (state.isSearchActive) "Close search" else "Search",
                    onClick = { viewModel.sendIntent(XrDevKitIntent.SetSearchActive(!state.isSearchActive)) }
                )
            )
        ))
    }

    // Search bar / 搜索栏
    if (state.isSearchActive) {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { viewModel.sendIntent(XrDevKitIntent.UpdateSearchQuery(it)) },
            onClear = { viewModel.sendIntent(XrDevKitIntent.ClearSearch) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is XrDevKitEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is XrDevKitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.code))
                }
                is XrDevKitEffect.ScrollToTop -> {
                    // Scroll to top handled by LazyColumn state
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            XrTabBar(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.sendIntent(XrDevKitIntent.SelectTab(it)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab content / Tab 内容
            when {
                state.isSearchActive && state.searchQuery.isNotBlank() -> {
                    // Search results / 搜索结果
                    SearchResultsContent(
                        results = state.searchResults,
                        favorites = state.favorites,
                        expandedCards = state.expandedCards,
                        onToggleFavorite = { viewModel.sendIntent(XrDevKitIntent.ToggleFavorite(it)) },
                        onToggleExpand = { viewModel.sendIntent(XrDevKitIntent.ToggleCardExpand(it)) },
                        onCopyCode = { code, id -> viewModel.copyCode(code, id) }
                    )
                }
                else -> {
                    when (state.currentTab) {
                        XrTab.OVERVIEW -> OverviewContent(
                            stats = state.overviewStats,
                            toolCards = state.toolCards,
                            favorites = state.favorites,
                            onTabSelected = { viewModel.sendIntent(XrDevKitIntent.SelectTab(it)) }
                        )
                        else -> ToolCardsContent(
                            cards = state.toolCards.filter { it.category == state.currentTab },
                            favorites = state.favorites,
                            expandedCards = state.expandedCards,
                            onToggleFavorite = { viewModel.sendIntent(XrDevKitIntent.ToggleFavorite(it)) },
                            onToggleExpand = { viewModel.sendIntent(XrDevKitIntent.ToggleCardExpand(it)) },
                            onCopyCode = { code, id -> viewModel.copyCode(code, id) }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Search Bar — 搜索栏
// =============================================================
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("搜索工具、指南、代码... / Search tools, guides, code...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = XrDevKitPrimary,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

// =============================================================
// Tab Bar — 底部 Tab 栏
// =============================================================
@Composable
private fun XrTabBar(
    currentTab: XrTab,
    onTabSelected: (XrTab) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        XrTab.entries.forEach { tab ->
            val isSelected = tab == currentTab
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab.titleZh,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = XrDevKitPrimary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.height(36.dp)
            )
        }
    }
}

// =============================================================
// Overview Content — 概览内容
// =============================================================
@Composable
private fun OverviewContent(
    stats: OverviewStats,
    toolCards: List<ToolCard>,
    favorites: Set<String>,
    onTabSelected: (XrTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Banner / 英雄横幅
        item {
            HeroBanner()
        }

        // Stats Cards / 统计卡片
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = "${stats.totalTools}",
                    label = "工具总数 / Tools",
                    color = XrDevKitPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${stats.newTools}",
                    label = "新增 / New",
                    color = XrDevKitSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${stats.categories}",
                    label = "类别 / Categories",
                    color = XrDevKitAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Quick Access / 快速访问
        item {
            Text(
                text = "快速访问 / Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Quick access grid / 快速访问网格
        item {
            QuickAccessGrid(
                toolCards = toolCards,
                favorites = favorites,
                onCardClick = { onTabSelected(it.category) }
            )
        }

        // Section: All Categories / 所有类别
        item {
            Text(
                text = "所有类别 / All Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(XrTab.entries.filter { it != XrTab.OVERVIEW }) { tab ->
            CategoryListItem(
                tab = tab,
                cardCount = toolCards.count { it.category == tab },
                isFavorite = toolCards.any { it.category == tab && favorites.contains(it.id) },
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun HeroBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = XrDevKitPrimary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Android XR SDK DP4",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "2026 年秋季 XR 眼镜硬件上市倒计时",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "XR Glasses Hardware Launching Fall 2026",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("DP4 发布 / Released", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        labelColor = Color.White
                    )
                )
                AssistChip(
                    onClick = { },
                    label = { Text("API 稳定化 / API Stabilizing", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        labelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun QuickAccessGrid(
    toolCards: List<ToolCard>,
    favorites: Set<String>,
    onCardClick: (ToolCard) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        toolCards.take(6).forEach { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCardClick(card) },
                colors = CardDefaults.cardColors(
                    containerColor = if (favorites.contains(card.id))
                        XrDevKitAccent.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.titleZh,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = card.descriptionZh,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (favorites.contains(card.id)) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Favorited",
                            tint = XrDevKitAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    tab: XrTab,
    cardCount: Int,
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tab.titleZh,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = XrDevKitAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "$cardCount tools / 工具",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

// =============================================================
// Tool Cards Content — 工具卡片内容
// =============================================================
@Composable
private fun ToolCardsContent(
    cards: List<ToolCard>,
    favorites: Set<String>,
    expandedCards: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    if (cards.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无工具 / No tools yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            ToolCardItem(
                card = card,
                isFavorite = favorites.contains(card.id),
                isExpanded = expandedCards.contains(card.id),
                onToggleFavorite = { onToggleFavorite(card.id) },
                onToggleExpand = { onToggleExpand(card.id) },
                onCopyCode = onCopyCode
            )
        }
    }
}

// =============================================================
// Tool Card Item — 工具卡片项
// =============================================================
@Composable
private fun ToolCardItem(
    card: ToolCard,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleExpand: () -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty badge / 难度徽章
                val difficultyColor = when (card.difficulty) {
                    Difficulty.BEGINNER -> XrDevKitSecondary
                    Difficulty.INTERMEDIATE -> XrDevKitPrimary
                    Difficulty.ADVANCED -> XrDevKitAccent
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = difficultyColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = card.difficulty.labelZh,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = difficultyColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Favorite button / 收藏按钮
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                        tint = if (isFavorite) XrDevKitAccent else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Expand/Collapse icon / 展开/折叠图标
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title / 标题
            Text(
                text = card.titleZh,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description / 描述
            Text(
                text = card.descriptionZh,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // Expanded content / 展开内容
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                card.contentItems.forEach { item ->
                    ContentItemSection(
                        item = item,
                        onCopyCode = onCopyCode
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// =============================================================
// Content Item Section — 内容条目部分
// =============================================================
@Composable
private fun ContentItemSection(
    item: ToolContentItem,
    onCopyCode: (String, String) -> Unit
) {
    // Title
    Text(
        text = item.titleZh,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = XrDevKitPrimary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Steps / 步骤
    if (item.steps.isNotEmpty()) {
        item.stepsZh.forEachIndexed { index, step ->
            Text(
                text = step,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Code snippets / 代码片段
    item.codeSnippets.forEach { snippet ->
        CodeSnippetBlock(
            snippet = snippet,
            onCopy = { onCopyCode(snippet.code, snippet.id ?: "${snippet.label}-${snippet.language}") }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Warnings / 警告
    if (item.warnings.isNotEmpty()) {
        item.warnings.forEach { warning ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = XrDevKitAccent.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

// =============================================================
// Code Snippet Block — 代码片段块
// =============================================================
@Composable
private fun CodeSnippetBlock(
    snippet: CodeSnippet,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(XrDevKitCodeBg)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        // Header / 头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snippet.labelZh,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            if (snippet.filename.isNotBlank()) {
                Text(
                    text = " · ${snippet.filename}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    modifier = Modifier.size(14.dp),
                    tint = Color.DarkGray
                )
            }
        }

        // Code content / 代码内容
        Text(
            text = snippet.code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            ),
            modifier = Modifier.padding(12.dp),
            color = Color(0xFF212121)
        )
    }
}

// =============================================================
// Search Results Content — 搜索结果内容
// =============================================================
@Composable
private fun SearchResultsContent(
    results: List<ToolCard>,
    favorites: Set<String>,
    expandedCards: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onCopyCode: (String, String) -> Unit
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "未找到结果 / No results found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "找到 ${results.size} 个结果 / Found ${results.size} results",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        items(results, key = { it.id }) { card ->
            ToolCardItem(
                card = card,
                isFavorite = favorites.contains(card.id),
                isExpanded = expandedCards.contains(card.id),
                onToggleFavorite = { onToggleFavorite(card.id) },
                onToggleExpand = { onToggleExpand(card.id) },
                onCopyCode = onCopyCode
            )
        }
    }
}
