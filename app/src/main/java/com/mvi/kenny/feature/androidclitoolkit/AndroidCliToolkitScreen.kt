package com.mvi.kenny.feature.androidclitoolkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.androidclitoolkit.AndroidCliColors as Colors
import com.mvi.kenny.feature.androidclitoolkit.AndroidCliToolkitIntent as Intent
import com.mvi.kenny.feature.androidclitoolkit.AndroidCliToolkitEffect as Effect
import com.mvi.kenny.feature.androidclitoolkit.AndroidCliToolkitState as State
import com.mvi.kenny.feature.androidclitoolkit.AndroidCliToolkitViewModel as ViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AndroidCliToolkitScreen — Android CLI 1.0 AI Agent 开发集成工具包主界面
 * ============================================================
 * PRD-259 | Android CLI 1.0 AI Agent 开发集成工具包
 *
 * 5 Tab Layout:
 * - Tab 1: 命令参考 (Command Reference)
 * - Tab 2: Agent集成 (Agent Integration)
 * - Tab 3: 语义重构 (Semantic Refactoring)
 * - Tab 4: Preview渲染 (Preview Rendering)
 * - Tab 5: Journeys测试 (Journeys Testing)
 */

// ============================================================
// Tab Icons Map / Tab 图标映射
// ============================================================
private fun getTabIcon(index: Int) = when (index) {
    0 -> Icons.Default.Code           // 命令参考
    1 -> Icons.Default.SmartToy       // Agent集成
    2 -> Icons.Default.Edit           // 语义重构
    3 -> Icons.Default.Visibility     // Preview渲染
    4 -> Icons.Default.BugReport      // Journeys测试
    else -> Icons.Default.Code
}

private fun getTabTitle(index: Int) = when (index) {
    0 -> "命令参考"
    1 -> "Agent集成"
    2 -> "语义重构"
    3 -> "Preview渲染"
    4 -> "Journeys测试"
    else -> ""
}

// ============================================================
// Main Screen Entry Point / 主界面入口
// ============================================================

/**
 * Android CLI Toolkit Screen
 * Android CLI 1.0 AI Agent 开发集成工具包主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidCliToolkitScreen(
    viewModel: ViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.ShowCopiedToast -> {
                    Toast.makeText(context, effect.text, Toast.LENGTH_SHORT).show()
                }
                is Effect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Android CLI Code", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is Effect.NavigateToInstallGuide -> {
                    // Handle navigation to install guide / 处理导航到安装指南
                    Toast.makeText(context, "安装指南: ${effect.platform}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Android CLI 1.0 AI Agent 工具包",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Version Selector / 版本选择器
            VersionSelector(
                selectedVersion = state.selectedVersion,
                onVersionSelected = { version ->
                    viewModel.sendIntent(Intent.SelectVersion(version))
                }
            )

            // Tab Row / Tab 行
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = Colors.Primary
                    )
                }
            ) {
                (0 until 5).forEach { index ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(Intent.SelectTab(index)) },
                        text = {
                            Text(
                                text = getTabTitle(index),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = getTabIcon(index),
                                contentDescription = getTabTitle(index),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Colors.Primary
                )
            }

            // Tab 0: 命令参考 / Command Reference
            if (state.selectedTab == 0) {
                CommandReferenceTab(
                    commands = filterCommands(state.commands, state.searchQuery),
                    expandedCommandId = state.expandedCommandId,
                    searchQuery = state.searchQuery,
                    copiedSnippetId = state.copiedSnippetId,
                    onExpandCommand = { commandId ->
                        viewModel.sendIntent(Intent.ExpandCommand(commandId))
                    },
                    onCopy = { id, content ->
                        viewModel.sendIntent(Intent.CopyCodeBlock(id, content))
                    },
                    onSearch = { query ->
                        viewModel.sendIntent(Intent.Search(query))
                    }
                )
            }

            // Tab 1: Agent集成 / Agent Integration
            if (state.selectedTab == 1) {
                AgentIntegrationTab(
                    agentConfigs = state.agentConfigs,
                    copiedSnippetId = state.copiedSnippetId,
                    onCopy = { id, content ->
                        viewModel.sendIntent(Intent.CopyCodeBlock(id, content))
                    },
                    onNavigateToInstallGuide = { platform ->
                        viewModel.sendIntent(Intent.NavigateToInstallGuide(platform))
                    }
                )
            }

            // Tab 2: 语义重构 / Semantic Refactoring
            if (state.selectedTab == 2) {
                SemanticRefactoringTab(
                    items = state.semanticRefactors,
                    copiedSnippetId = state.copiedSnippetId,
                    onCopy = { id, content ->
                        viewModel.sendIntent(Intent.CopyCodeBlock(id, content))
                    }
                )
            }

            // Tab 3: Preview渲染 / Preview Rendering
            if (state.selectedTab == 3) {
                PreviewRenderingTab(
                    items = state.previewRenderItems,
                    copiedSnippetId = state.copiedSnippetId,
                    onCopy = { id, content ->
                        viewModel.sendIntent(Intent.CopyCodeBlock(id, content))
                    }
                )
            }

            // Tab 4: Journeys测试 / Journeys Testing
            if (state.selectedTab == 4) {
                JourneysTestingTab(
                    items = state.journeysTestItems,
                    copiedSnippetId = state.copiedSnippetId,
                    onCopy = { id, content ->
                        viewModel.sendIntent(Intent.CopyCodeBlock(id, content))
                    }
                )
            }
        }
    }
}

// ============================================================
// Version Selector / 版本选择器
// ============================================================

@Composable
private fun VersionSelector(
    selectedVersion: String,
    onVersionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val versions = listOf("1.0.0", "1.0.x")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "版本 / Version:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))

        Box {
            FilterChip(
                selected = true,
                onClick = { expanded = true },
                label = { Text(selectedVersion) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Colors.Primary.copy(alpha = 0.15f),
                    selectedLabelColor = Colors.Primary
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                versions.forEach { version ->
                    DropdownMenuItem(
                        text = { Text(version) },
                        onClick = {
                            onVersionSelected(version)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ============================================================
// Search Bar / 搜索栏
// ============================================================

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "搜索命令或关键词 / Search commands..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Colors.Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

// ============================================================
// Tab 0: Command Reference / 命令参考
// ============================================================

@Composable
private fun CommandReferenceTab(
    commands: List<CliCommand>,
    expandedCommandId: String?,
    searchQuery: String,
    copiedSnippetId: String?,
    onExpandCommand: (String) -> Unit,
    onCopy: (String, String) -> Unit,
    onSearch: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearch,
                placeholder = "搜索命令 / Search commands..."
            )
        }

        items(commands, key = { it.id }) { command ->
            CommandCard(
                command = command,
                isExpanded = expandedCommandId == command.id,
                isCopied = copiedSnippetId == "copy-${command.id}",
                onToggleExpand = { onExpandCommand(command.id) },
                onCopy = { content ->
                    onCopy("copy-${command.id}", content)
                }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CommandCard(
    command: CliCommand,
    isExpanded: Boolean,
    isCopied: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Command / 命令
                    Text(
                        text = command.command,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Colors.CodeCommand,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Description CN / 中文描述
                    Text(
                        text = command.descriptionCn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category badge / 分类标签
                    Text(
                        text = command.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Colors.Primary,
                        modifier = Modifier
                            .background(
                                Colors.Primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Description / 英文描述
                    Text(
                        text = command.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Example Usage / 使用示例
                    Text(
                        text = "用法 / Usage:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = command.exampleUsage,
                        isCopied = isCopied,
                        onCopy = { onCopy(command.exampleUsage) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Example Output / 输出示例
                    Text(
                        text = "输出 / Output:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = command.exampleOutput,
                        isCopied = false,
                        onCopy = { }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Versions / 适用版本
                    Text(
                        text = "版本 / Versions: ${command.versions.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Related Commands / 相关命令
                    if (command.relatedCommands.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "相关 / Related: ${command.relatedCommands.joinToString(" | ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Colors.Primary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 1: Agent Integration / Agent集成
// ============================================================

@Composable
private fun AgentIntegrationTab(
    agentConfigs: List<AgentConfig>,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit,
    onNavigateToInstallGuide: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "配置你的 AI Agent 使用 Android CLI 1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(agentConfigs, key = { it.id }) { config ->
            AgentConfigCard(
                config = config,
                copiedSnippetId = copiedSnippetId,
                onCopy = { id, content -> onCopy(id, content) },
                onNavigateToInstallGuide = onNavigateToInstallGuide
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AgentConfigCard(
    config: AgentConfig,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit,
    onNavigateToInstallGuide: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.titleCn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = config.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = config.agentType.displayNameCn,
                    style = MaterialTheme.typography.labelSmall,
                    color = Colors.Primary,
                    modifier = Modifier
                        .background(Colors.Primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Install Command / 安装命令
                    Text(
                        text = "安装命令 / Install Command:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = config.installCommand,
                        isCopied = copiedSnippetId == "copy-install-${config.id}",
                        onCopy = { onCopy("copy-install-${config.id}", config.installCommand) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Config Steps / 配置步骤
                    Text(
                        text = "配置步骤 / Config Steps:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    config.configSteps.forEachIndexed { index, (en, cn) ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Colors.CodeNumber
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = en,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Colors.CodeNormal
                                )
                                Text(
                                    text = cn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Config Example / 配置示例
                    Text(
                        text = "配置示例 / Config Example:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = config.configExample,
                        isCopied = copiedSnippetId == "copy-config-${config.id}",
                        onCopy = { onCopy("copy-config-${config.id}", config.configExample) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gradle Sync Note / Gradle 同步说明
                    Text(
                        text = "💡 Gradle Sync: ${config.gradleSyncNoteCn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Install Guide Button / 安装指南按钮
                    TextButton(
                        onClick = { onNavigateToInstallGuide(config.agentType.name) }
                    ) {
                        Text(text = "查看完整安装指南 →")
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 2: Semantic Refactoring / 语义重构
// ============================================================

@Composable
private fun SemanticRefactoringTab(
    items: List<SemanticRefactorItem>,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Semantic Symbol Resolution 最佳实践",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(items, key = { it.id }) { item ->
            SemanticRefactorCard(
                item = item,
                copiedSnippetId = copiedSnippetId,
                onCopy = onCopy
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SemanticRefactorCard(
    item: SemanticRefactorItem,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.titleCn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.descriptionCn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "代码示例 / Code Example:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = item.codeExample,
                        isCopied = copiedSnippetId == "copy-${item.id}",
                        onCopy = { onCopy("copy-${item.id}", item.codeExample) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Limitations / 限制
                    Text(
                        text = "⚠️ 限制 / Limitations:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = item.limitationsCn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Best Practices / 最佳实践
                    Text(
                        text = "✅ 最佳实践 / Best Practices:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    item.bestPracticesCn.forEachIndexed { index, practice ->
                        Text(
                            text = "• $practice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 3: Preview Rendering / Preview渲染
// ============================================================

@Composable
private fun PreviewRenderingTab(
    items: List<PreviewRenderItem>,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Compose Preview CI 集成指南",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(items, key = { it.id }) { item ->
            PreviewRenderCard(
                item = item,
                copiedSnippetId = copiedSnippetId,
                onCopy = onCopy
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun PreviewRenderCard(
    item: PreviewRenderItem,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.titleCn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.descriptionCn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // CI Command / CI 命令
                    Text(
                        text = "CI 命令 / CI Command:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = item.ciCommand,
                        isCopied = false,
                        onCopy = { }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code Example / 代码示例
                    Text(
                        text = "代码示例 / Code Example:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = item.codeExample,
                        isCopied = copiedSnippetId == "copy-preview-${item.id}",
                        onCopy = { onCopy("copy-preview-${item.id}", item.codeExample) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Image Note / 图片说明
                    Text(
                        text = "📷 ${item.imageNoteCn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 4: Journeys Testing / Journeys测试
// ============================================================

@Composable
private fun JourneysTestingTab(
    items: List<JourneysTestItem>,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Journeys UI 测试框架完全指南",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(items, key = { it.id }) { item ->
            JourneysTestCard(
                item = item,
                copiedSnippetId = copiedSnippetId,
                onCopy = onCopy
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun JourneysTestCard(
    item: JourneysTestItem,
    copiedSnippetId: String?,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.titleCn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.testFramework} v${item.frameworkVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Colors.Primary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.descriptionCn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Code / 测试代码
                    Text(
                        text = "测试代码 / Test Code:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CodeBlock(
                        code = item.testCode,
                        isCopied = copiedSnippetId == "copy-journeys-${item.id}",
                        onCopy = { onCopy("copy-journeys-${item.id}", item.testCode) },
                        isScrollable = true
                    )
                }
            }
        }
    }
}

// ============================================================
// Code Block Component (Monokai Style) / 代码块组件 (Monokai 风格)
// ============================================================

/**
 * Code block with Monokai-style syntax highlighting
 * 使用 Monokai 风格语法高亮的代码块
 */
@Composable
private fun CodeBlock(
    code: String,
    isCopied: Boolean,
    onCopy: () -> Unit,
    isScrollable: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Copy button row / 复制按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCopy,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = if (isCopied) Color(0xFF4CAF50) else Colors.Primary
                )
                ) {
                    Text(text = if (isCopied) "已复制 ✓" else "复制")
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                }
        }

        // Code container with Monokai background / 带 Monokai 背景的代码容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Colors.CodeBackground)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            if (isScrollable) {
                Box(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                ) {
                    MonokaiCodeText(code = code)
                }
            } else {
                MonokaiCodeText(code = code)
            }
        }
    }
}

// ============================================================
// Monokai Code Text (Simple Syntax Highlighting) / Monokai 代码文本 (简单语法高亮)
// ============================================================

/**
 * Simple Monokai-style syntax highlighting using Color
 * 使用颜色实现简单的 Monokai 风格语法高亮
 */
@Composable
private fun MonokaiCodeText(code: String) {
    val annotatedString = remember(code) {
        buildAnnotatedString {
            val keywords = setOf("fun", "val", "var", "class", "object", "data", "sealed", "interface",
                "override", "private", "public", "internal", "protected", "open", "abstract",
                "final", "suspend", "inline", "reified", "lateinit", "by", "lazy",
                "if", "else", "when", "for", "while", "do", "return", "break", "continue",
                "try", "catch", "finally", "throw", "import", "package", "typealias",
                "companion", "const", "enum", "annotation", "true", "false", "null",
                "this", "super", "is", "as", "in", "out", "where", "get", "set")
            val stringChars = setOf('"', '\'', '`')

            // Split by words and tokens / 按单词和标记分割
            val tokenRegex = Regex("""(\s+)|(//.*)|(/\*[\s\S]*?\*/)|("[^"]*"|'[^']*'|`[^`]*`)|([A-Za-z_]\w*)|([^\sA-Za-z_0-9]+)|(\d+\.?\d*)""")

            for (match in tokenRegex.findAll(code)) {
                val token = match.value

                when {
                    // Comment / 注释
                    token.startsWith("//") || token.startsWith("/*") -> {
                        pushStyle(SpanStyle(color = Colors.CodeComment))
                        append(token)
                        pop()
                    }
                    // String / 字符串
                    stringChars.contains(token.first()) -> {
                        pushStyle(SpanStyle(color = Colors.CodeString))
                        append(token)
                        pop()
                    }
                    // Keyword / 关键字
                    token.first().isLetter() && token.first().isLowerCase() && token in keywords -> {
                        pushStyle(SpanStyle(color = Colors.CodeKeyword))
                        append(token)
                        pop()
                    }
                    // Identifier or command / 标识符或命令
                    token.first().isLetter() || token.first() == '@' || token.first() == '$' -> {
                        when {
                            token.startsWith("android") || token.startsWith("./") || token.startsWith("gradle") -> {
                                pushStyle(SpanStyle(color = Colors.CodeCommand))
                                append(token)
                                pop()
                            }
                            token.first() == '@' || token.first() == '$' -> {
                                pushStyle(SpanStyle(color = Colors.CodeParam))
                                append(token)
                                pop()
                            }
                            else -> {
                                pushStyle(SpanStyle(color = Colors.CodeNormal))
                                append(token)
                                pop()
                            }
                        }
                    }
                    // Number / 数字
                    token.first().isDigit() -> {
                        pushStyle(SpanStyle(color = Colors.CodeNumber))
                        append(token)
                        pop()
                    }
                    // Symbol / 符号
                    else -> {
                        pushStyle(SpanStyle(color = Colors.CodeNormal.copy(alpha = 0.8f)))
                        append(token)
                        pop()
                    }
                }
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = Colors.CodeNormal
    )
}

// ============================================================
// Helper: Filter Commands by Search Query / 辅助函数：按搜索查询过滤命令
// ============================================================

private fun filterCommands(commands: List<CliCommand>, query: String): List<CliCommand> {
    if (query.isBlank()) return commands
    val lowerQuery = query.lowercase()
    return commands.filter { command ->
        command.command.lowercase().contains(lowerQuery) ||
        command.description.lowercase().contains(lowerQuery) ||
        command.descriptionCn.lowercase().contains(lowerQuery) ||
        command.category.lowercase().contains(lowerQuery)
    }
}
