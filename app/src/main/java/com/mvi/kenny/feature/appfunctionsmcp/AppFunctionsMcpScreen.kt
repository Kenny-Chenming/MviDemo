package com.mvi.kenny.feature.appfunctionsmcp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AppFunctionsMcpScreen — PRD-274 AppFunctions On-Device MCP 开发工具包
 * AppFunctions On-Device MCP Developer Toolkit Screen
 * ============================================================
 *
 * Design Doc: memory/agency/designs/PRD-274-Android-AppFunctions-On-Device-MCP开发工具包.md
 *
 * Page Structure:
 * - 顶部: 标题 + 搜索栏
 * - 主体: 章节列表 (0x00-0x09)，每个章节可展开
 * - 章节内: 代码示例 + 信息卡片
 * - 代码示例: 可点击放大查看，支持复制
 *
 * Visual Spec:
 * - Primary: #4285F4 (Google Blue)
 * - Accent: #34A853 (Google Green), #FBBC05 (Google Yellow)
 * - Info Cards: Tip=#E8F5E9, Warning=#FFF3E0, Key=#E3F2FD
 */

// ============================================================
// Main Screen Composable / 主屏幕
// ============================================================

/**
 * AppFunctions On-Device MCP 开发工具包主屏幕
 * Main screen for AppFunctions On-Device MCP Developer Toolkit
 *
 * @param viewModel MVI ViewModel，持有页面状态和处理用户意图
 * @param onNavigateBack 返回导航回调
 */
@Composable
fun AppFunctionsMcpScreen(
    viewModel: AppFunctionsMcpViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // 收集状态和副作用 / Collect state and effects
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 处理副作用 / Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppFunctionsMcpEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AppFunctionsMcpEffect.CopyCodeToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Code", effect.code)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                }
                is AppFunctionsMcpEffect.ScrollToSection -> {
                    // 滚动处理在 LazyColumn 中通过 key 实现
                }
            }
        }
    }

    // 加载初始章节内容 / Load initial section content
    LaunchedEffect(Unit) {
        viewModel.sendIntent(AppFunctionsMcpIntent.LoadSectionContent(state.currentSection.id))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ——————————————————————————————————
            // Header / 页面头部
            // ——————————————————————————————————
            AppFunctionsMcpHeader(
                searchQuery = state.searchQuery,
                onSearchChange = { query ->
                    viewModel.sendIntent(AppFunctionsMcpIntent.SearchSections(query))
                },
                onClearSearch = {
                    viewModel.sendIntent(AppFunctionsMcpIntent.ClearSearch)
                },
                onExpandAll = {
                    viewModel.sendIntent(AppFunctionsMcpIntent.ExpandAllSections)
                },
                onCollapseAll = {
                    viewModel.sendIntent(AppFunctionsMcpIntent.CollapseAllSections)
                }
            )

            HorizontalDivider()

            // ——————————————————————————————————
            // Section List / 章节列表
            // ——————————————————————————————————
            val displaySections = state.filteredSections ?: DocSection.entries

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(displaySections, key = { it.id }) { section ->
                    val isExpanded = state.expandedSections.contains(section.id)

                    DocSectionCard(
                        section = section,
                        isExpanded = isExpanded,
                        isSelected = state.currentSection == section,
                        codeExamples = if (state.currentSection == section) state.codeExamples else emptyList(),
                        infoCards = if (state.currentSection == section) state.infoCards else emptyList(),
                        isLoading = state.isLoading && state.currentSection == section,
                        onSectionClick = {
                            viewModel.sendIntent(AppFunctionsMcpIntent.NavigateToSection(section))
                        },
                        onToggleExpand = {
                            viewModel.sendIntent(AppFunctionsMcpIntent.ToggleSectionExpand(section.id))
                        },
                        onCodeExampleClick = { example ->
                            viewModel.sendIntent(AppFunctionsMcpIntent.SelectCodeExample(example))
                        },
                        onCopyCode = { code ->
                            viewModel.sendIntent(AppFunctionsMcpIntent.SelectCodeExample(null))
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // ——————————————————————————————————
        // Code Example Dialog / 代码示例放大对话框
        // ——————————————————————————————————
        if (state.selectedCodeExample != null) {
            CodeExampleDialog(
                example = state.selectedCodeExample!!,
                onDismiss = {
                    viewModel.sendIntent(AppFunctionsMcpIntent.SelectCodeExample(null))
                },
                onCopy = { code ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Code", code)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    viewModel.sendIntent(AppFunctionsMcpIntent.SelectCodeExample(null))
                }
            )
        }
    }
}

// ============================================================
// Header Component / 头部组件
// ============================================================

/**
 * 页面头部 — 包含标题、搜索栏和操作按钮
 * Page header with title, search bar and action buttons
 */
@Composable
private fun AppFunctionsMcpHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4285F4))  // Google Blue
            .padding(16.dp)
    ) {
        // 标题行 / Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AppFunctions",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "On-Device MCP 开发工具包",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 搜索栏 / Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("搜索章节、内容或标签...", color = Color.White.copy(alpha = 0.6f))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 操作按钮 / Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onExpandAll) {
                Text("全部展开", color = Color.White.copy(alpha = 0.9f))
            }
            TextButton(onClick = onCollapseAll) {
                Text("全部折叠", color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

// ============================================================
// Section Card Component / 章节卡片组件
// ============================================================

/**
 * 文档章节卡片
 * Document section card
 *
 * 可展开/折叠，显示章节标题、描述、难度标签、代码示例和信息卡片
 * Expandable card showing section title, description, difficulty tag, code examples, and info cards
 */
@Composable
private fun DocSectionCard(
    section: DocSection,
    isExpanded: Boolean,
    isSelected: Boolean,
    codeExamples: List<CodeExample>,
    infoCards: List<InfoCard>,
    isLoading: Boolean,
    onSectionClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onCodeExampleClick: (CodeExample) -> Unit,
    onCopyCode: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSectionClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF3F9FF) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 章节头部 / Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 章节 ID 标签 / Section ID badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4285F4), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = section.id,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 标题和描述 / Title and description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.titleCn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = section.titleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // 展开/折叠按钮 / Expand/collapse button
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            // 描述和标签 / Description and tags
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 标签行 / Tags row
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 难度标签 / Difficulty tag
                val difficultyColor = when (section.difficulty) {
                    "入门" -> Color(0xFF3FB950)
                    "进阶" -> Color(0xFFFBBC05)
                    else -> Color(0xFFF85149)
                }
                Box(
                    modifier = Modifier
                        .background(difficultyColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = section.difficulty,
                        color = difficultyColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 技术标签 / Technology tags
                section.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFF4285F4),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // ——————————————————————————————————
            // Expanded Content / 展开内容
            // ——————————————————————————————————
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // 信息卡片 / Info cards
                        if (infoCards.isNotEmpty()) {
                            infoCards.forEach { card ->
                                InfoCardItem(card)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 代码示例列表 / Code examples list
                        if (codeExamples.isNotEmpty()) {
                            Text(
                                text = "Code Examples",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            codeExamples.forEach { example ->
                                CodeExampleItem(
                                    example = example,
                                    onClick = { onCodeExampleClick(example) },
                                    onCopy = { onCopyCode(example.code) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Info Card Component / 信息卡片组件
// ============================================================

/**
 * 信息卡片组件 — 显示提示、注意、关键等信息
 * Info card component — shows tips, warnings, key points, etc.
 */
@Composable
private fun InfoCardItem(card: InfoCard) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(card.type.backgroundColor))
    } catch (e: Exception) {
        Color(0xFFE8F5E9)
    }

    val borderColor = try {
        Color(android.graphics.Color.parseColor(card.type.borderColor))
    } catch (e: Exception) {
        Color(0xFF3FB950)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = card.type.symbol,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = card.content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ============================================================
// Code Example Item Component / 代码示例组件
// ============================================================

/**
 * 代码示例项组件
 * Code example item component
 *
 * 显示代码标题、描述、运行要求，点击可放大查看
 * Shows code title, description, requirements; click to expand
 */
@Composable
private fun CodeExampleItem(
    example: CodeExample,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 标题行 / Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = Color(0xFF4285F4),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = example.title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 描述 / Description
            Text(
                text = example.description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            // 运行要求 / Requirements
            if (example.requirements.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF3FB950),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = example.requirements,
                        color = Color(0xFF3FB950),
                        fontSize = 11.sp
                    )
                }
            }

            // 代码预览 / Code preview
            Text(
                text = example.code.take(200) + if (example.code.length > 200) "..." else "",
                color = Color(0xFFD4D4D4),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================
// Code Example Dialog / 代码示例对话框
// ============================================================

/**
 * 代码示例放大查看对话框
 * Expanded code example dialog
 */
@Composable
private fun CodeExampleDialog(
    example: CodeExample,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 对话框头部 / Dialog header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = example.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = example.description,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (example.requirements.isNotEmpty()) {
                            Text(
                                text = "✅ ${example.requirements}",
                                color = Color(0xFF3FB950),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    IconButton(onClick = { onCopy(example.code) }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // 代码内容 / Code content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = example.code,
                        color = Color(0xFFD4D4D4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
