package com.mvi.kenny.feature.nav3toolkit

// ================================================================
// Nav3ToolkitScreen — Jetpack Navigation 3 响应式导航集成工具包主界面
// ================================================================
// Main screen for Jetpack Navigation 3 Responsive Navigation Toolkit.
//
// PRD-258: Jetpack Navigation 3 响应式导航集成工具包
// Design Reference: memory/agency/designs/PRD-258-Jetpack-Navigation-3-响应式导航集成工具包.md
//
// Architecture:
//   - Bottom Tab Navigation (5 tabs)
//   - Tab content via LazyColumn scrollable
//   - SideBySideCodeBlock: v2.x (gray #757575) vs v3 (Teal #4DB6AC)
//   - Decision Tree: RadioGroup + ResultCard
//   - NavGraph XML syntax highlighting
//   - Chapter anchor navigation
//
// Visual Spec:
//   Primary color: Teal #4DB6AC (matches Navigation Component brand)
//   v2.x code label: Gray #757575 | v3 code label: Teal #4DB6AC
//   Tab active: #4DB6AC | inactive: #757575
//   Card background: SurfaceVariant, cornerRadius 12dp, spacing 12dp
//
// @param viewModel Nav3ToolkitViewModel instance
// @param onNavigateTo Internal navigation callback
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// 颜色常量 / Color Constants (top-level, accessible to all functions)
// ================================================================

/** Teal 主题色 — Navigation Component 品牌色 */
private val TealColor = Color(0xFF4DB6AC)

/** 灰色标签色 — v2.x 代码标签 */
private val GrayColor = Color(0xFF757575)

// ================================================================
// 主界面入口 / Main Screen Entry Point
// ================================================================

/**
 * ============================================================
 * Nav3ToolkitScreen — Navigation 3 工具包主界面
 * ============================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Nav3ToolkitScreen(
    viewModel: Nav3ToolkitViewModel,
    onNavigateTo: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // ── Effect 处理 ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Nav3ToolkitEffect.ShowCopiedToast -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("NavGraph XML", state.navGraphXml.ifEmpty { DEFAULT_NAV_GRAPH_XML })
                    clipboard.setPrimaryClip(clip)
                    snackbarHostState.showSnackbar("NavGraph XML 已复制到剪贴板")
                }
                is Nav3ToolkitEffect.ScrollToChapter -> {
                    val chapterIndex = state.chapters.indexOfFirst { it.id == effect.chapterId }
                    if (chapterIndex >= 0) {
                        listState.animateScrollToItem(chapterIndex)
                    }
                }
                is Nav3ToolkitEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nav3 响应式导航工具包",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { /* Toggle search visibility */ }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealColor.copy(alpha = 0.15f),
                    titleContentColor = TealColor
                )
            )
        },
        bottomBar = {
            Nav3BottomTabBar(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(Nav3ToolkitIntent.SelectTab(it)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LegacyModeToggle(
                isLegacy = state.isLegacy,
                onToggle = { viewModel.sendIntent(Nav3ToolkitIntent.ToggleLegacyMode(it)) }
            )

            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(Nav3ToolkitIntent.Search(it)) }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.chapters, key = { it.id }) { chapter ->
                    ChapterCard(
                        chapter = chapter,
                        isExpanded = state.expandedChapterId == chapter.id,
                        isLegacy = state.isLegacy,
                        onToggle = { viewModel.sendIntent(Nav3ToolkitIntent.ToggleChapter(chapter.id)) },
                        onCopyNavGraph = { viewModel.sendIntent(Nav3ToolkitIntent.CopyNavGraph) },
                        navGraphXml = if (chapter.id == "ch-4-1") state.navGraphXml else ""
                    )
                }

                // 决策树（仅 Tab 1 显示）
                if (state.selectedTab == 1 && state.chapters.isNotEmpty()) {
                    item(key = "decision_tree") {
                        DecisionTreeSection(
                            nodes = state.decisionTreeNodes,
                            selectedPath = state.selectedDecisionPath,
                            activeNode = state.activeDecisionNode,
                            onSelectOption = { qId, aId ->
                                viewModel.sendIntent(Nav3ToolkitIntent.SelectDecisionPath(qId, aId))
                            }
                        )
                    }
                }

                // NavGraph XML 预览（Tab 4）
                if (state.selectedTab == 4) {
                    item(key = "navgraph_preview") {
                        NavGraphPreviewCard(
                            xml = state.navGraphXml.ifEmpty { DEFAULT_NAV_GRAPH_XML },
                            onCopy = { viewModel.sendIntent(Nav3ToolkitIntent.CopyNavGraph) }
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// 底部 Tab 导航 / Bottom Tab Navigation
// ================================================================

@Composable
private fun Nav3BottomTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = Nav3Tab.entries

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = TealColor
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedTab == index) TealColor else GrayColor
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealColor,
                    selectedTextColor = TealColor,
                    unselectedIconColor = GrayColor,
                    unselectedTextColor = GrayColor,
                    indicatorColor = TealColor.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ================================================================
// Legacy Toggle / 视角切换
// ================================================================

@Composable
private fun LegacyModeToggle(
    isLegacy: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = !isLegacy,
            onClick = { onToggle(false) },
            label = { Text("v3", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (!isLegacy) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TealColor.copy(alpha = 0.2f),
                selectedLabelColor = TealColor
            )
        )

        FilterChip(
            selected = isLegacy,
            onClick = { onToggle(true) },
            label = { Text("v2.x", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = if (isLegacy) {
                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GrayColor.copy(alpha = 0.2f),
                selectedLabelColor = GrayColor
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (isLegacy) "显示 v2.x 代码" else "显示 v3 代码",
            style = MaterialTheme.typography.labelSmall,
            color = GrayColor
        )
    }
}

// ================================================================
// 搜索栏 / Search Bar
// ================================================================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text("搜索章节...", style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索", tint = GrayColor) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

// ================================================================
// 章节卡片 / Chapter Card
// ================================================================

@Composable
private fun ChapterCard(
    chapter: NavChapter,
    isExpanded: Boolean,
    isLegacy: Boolean,
    onToggle: () -> Unit,
    onCopyNavGraph: () -> Unit,
    navGraphXml: String
) {
    val animatedExpand by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = "expand"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 章节标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = TealColor
                )
            }

            // 章节内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = chapter.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 代码示例
                    if (chapter.codeBlocks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        chapter.codeBlocks.forEach { codeBlock ->
                            SideBySideCodeBlock(codeBlock = codeBlock)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // NavGraph XML 复制按钮
                    if (navGraphXml.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onCopyNavGraph) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("复制 NavGraph XML")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// 代码对比块 / Side-by-Side Code Block
// ================================================================

@Composable
private fun SideBySideCodeBlock(codeBlock: CodeBlock) {
    val backgroundColor = CodeBlockLightBg
    val labelColor = if (codeBlock.labelColor == Nav3Teal) TealColor else GrayColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, labelColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        // 标签行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(labelColor.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = codeBlock.label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Bold
            )
            if (codeBlock.filename.isNotEmpty()) {
                Text(
                    text = codeBlock.filename,
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayColor
                )
            }
        }

        // 代码内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            SyntaxHighlightedCode(
                code = codeBlock.code,
                language = codeBlock.language
            )
        }
    }
}

// ================================================================
// 语法高亮 / Syntax Highlighting
// ================================================================

@Composable
private fun SyntaxHighlightedCode(
    code: String,
    language: String
) {
    // Define colors as top-level vals for access by extension functions
    val keywordColor = Color(0xFF7C4DFF)
    val stringColor = Color(0xFF43A047)
    val commentColor = GrayColor
    val annotationColor = Color(0xFFFFB300)
    val defaultColor = MaterialTheme.colorScheme.onSurface

    Text(
        text = buildAnnotatedString {
            val lines = code.split("\n")
            lines.forEachIndexed { lineIndex, line ->
                when (language) {
                    "kotlin" -> highlightKotlinLine(line, keywordColor, stringColor, commentColor, annotationColor, defaultColor)
                    "xml" -> highlightXmlLine(line, commentColor, defaultColor)
                    else -> append(line)
                }
                if (lineIndex < lines.size - 1) append("\n")
            }
        },
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        color = defaultColor
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightKotlinLine(
    line: String,
    keywordColor: Color,
    stringColor: Color,
    commentColor: Color,
    annotationColor: Color,
    defaultColor: Color
) {
    val keywords = listOf(
        "fun", "val", "var", "class", "interface", "object", "data", "sealed",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "import", "package", "private", "public", "internal", "protected", "open",
        "override", "abstract", "final", "suspend", "inline", "noinline", "crossinline",
        "companion", "const", "lateinit", "by", "lazy", "enum", "annotation",
        "NavHost", "composable", "Composable", "NavGraph", "NavController",
        "remember", "State", "mutableStateOf", "LaunchedEffect", "collectAsState",
        "AdaptivePane", "ListPane", "DetailPane"
    )

    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("//", i) -> {
                withStyle(SpanStyle(color = commentColor)) { append(line.substring(i)); return }
            }
            line[i] == '"' -> {
                var j = i + 1
                while (j < line.length && line[j] != '"') { j++ }
                withStyle(SpanStyle(color = stringColor)) { append(line.substring(i, j + 1)) }
                i = j + 1
            }
            line[i] == '@' -> {
                var j = i + 1
                while (j < line.length && line[j] !in " \t\n()") { j++ }
                withStyle(SpanStyle(color = annotationColor)) { append(line.substring(i, j)) }
                i = j
            }
            else -> {
                var j = i
                while (j < line.length && line[j] !in " \t\n(){}[]=,:.<>\"@") { j++ }
                val word = line.substring(i, j)
                if (word in keywords) {
                    withStyle(SpanStyle(color = keywordColor)) { append(word) }
                } else {
                    withStyle(SpanStyle(color = defaultColor)) { append(word) }
                }
                i = j
            }
        }
        if (i < line.length && line[i] !in " \t\n(){}[]=,:.<>\"@/") {
            if (i < line.length) { withStyle(SpanStyle(color = defaultColor)) { append(line[i]) } }
            i++
        } else if (i < line.length) {
            withStyle(SpanStyle(color = defaultColor)) { append(line[i]) }
            i++
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.highlightXmlLine(line: String, commentColor: Color, defaultColor: Color) {
    val tagColor = Color(0xFF1E88E5)
    val valueColor = Color(0xFF43A047)

    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("<!--", i) -> {
                val end = line.indexOf("-->", i).let { if (it == -1) line.length else it + 3 }
                withStyle(SpanStyle(color = commentColor)) { append(line.substring(i, end)); return }
            }
            line[i] == '<' -> {
                var j = i + 1
                while (j < line.length && line[j] != '>') { j++ }
                withStyle(SpanStyle(color = tagColor)) { append(line.substring(i, j + 1)) }
                i = j + 1
            }
            line[i] == '"' -> {
                var j = i + 1
                while (j < line.length && line[j] != '"') { j++ }
                withStyle(SpanStyle(color = valueColor)) { append(line.substring(i, j + 1)) }
                i = j + 1
            }
            else -> {
                withStyle(SpanStyle(color = defaultColor)) { append(line[i]) }
                i++
            }
        }
    }
}

// ================================================================
// 决策树 / Decision Tree
// ================================================================

@Composable
private fun DecisionTreeSection(
    nodes: List<DecisionNode>,
    selectedPath: Map<String, String>,
    activeNode: DecisionNode?,
    onSelectOption: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TealColor.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "决策树：选择导航方案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TealColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            val currentQuestion = activeNode ?: nodes.find { it.type == "question" && it.id !in selectedPath.keys }

            if (currentQuestion != null && currentQuestion.type == "question") {
                Text(
                    text = currentQuestion.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    currentQuestion.options.forEach { option ->
                        val isSelected = selectedPath[currentQuestion.id] == option.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectOption(currentQuestion.id, option.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectOption(currentQuestion.id, option.id) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TealColor,
                                    unselectedColor = GrayColor
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) TealColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (activeNode?.type == "result" && activeNode.resultCard != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = TealColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                ResultCardView(resultCard = activeNode.resultCard)
            }
        }
    }
}

// ================================================================
// 结果卡片 / Result Card
// ================================================================

@Composable
private fun ResultCardView(resultCard: ResultCard) {
    Column {
        Text(
            text = resultCard.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TealColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = resultCard.recommendation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (resultCard.codeExample.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CodeBlockLightBg)
                    .border(1.dp, TealColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = resultCard.codeExample,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (resultCard.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = resultCard.notes,
                style = MaterialTheme.typography.bodySmall,
                color = GrayColor
            )
        }
    }
}

// ================================================================
// NavGraph XML 预览 / NavGraph XML Preview
// ================================================================

@Composable
private fun NavGraphPreviewCard(
    xml: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NavGraph XML 预览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = TealColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CodeBlockLightBg)
                    .border(1.dp, TealColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
            ) {
                SyntaxHighlightedCode(
                    code = xml,
                    language = "xml"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "点击右上角按钮复制完整 XML",
                style = MaterialTheme.typography.labelSmall,
                color = GrayColor,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ================================================================
// 折叠屏状态示意图 / Fold State Diagram (Static)
// ================================================================

@Composable
private fun FoldStateDiagram(isFolded: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isFolded) "折叠态" else "展开态",
            style = MaterialTheme.typography.titleSmall,
            color = TealColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .width(if (isFolded) 120.dp else 240.dp)
                .height(if (isFolded) 60.dp else 120.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE0E0E0),
            shadowElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFolded) "单面板" else "双面板",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayColor
                )
            }
        }
    }
}
