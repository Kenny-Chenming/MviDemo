package com.mvi.kenny.feature.androidagenttoolkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color constants / 颜色常量
// ================================================================
private object AgentColors {
    val Primary = Color(0xFF4285F4)         // Google Blue
    val Secondary = Color(0xFF34A853)        // Green
    val Tertiary = Color(0xFFFBBC04)         // Yellow
    val Error = Color(0xFFEA4335)            // Red
    val Surface = Color(0xFF1C1C1E)          // Dark terminal bg
    val OnSurface = Color(0xFFE5E5E7)       // Terminal text
    val TerminalGreen = Color(0xFF00FF00)   // stdout
    val TerminalRed = Color(0xFFFF6B6B)      // stderr
    val TerminalYellow = Color(0xFFFFD93D)  // warning
}

// ================================================================
// Main Screen / 主屏幕
// ================================================================
/**
 * Android Agent Toolkit Screen — Android CLI/Skills/KB AI Agent 工具链主屏幕
 *
 * PRD-126 | Android CLI + Android Skills + Android Knowledge Base AI Agent 开发工具包
 *
 * 6-Tab structure:
 * — Dashboard: 工具链总览 + Token 节省统计
 * — CLI: Android CLI 命令执行面板
 * — Skills: Skill 市场 + 兼容性矩阵
 * — Knowledge Base: 知识库查询 + 离线缓存管理
 * — CI/CD: 流水线配置 + YAML 编辑器
 * — Settings: AI Provider / 输出格式 / 日志级别
 *
 * @param onUpdateTopBar TopBar 配置更新回调 / TopBar config update callback
 * @param viewModel ViewModel 实例 / ViewModel instance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidAgentToolkitScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: AndroidAgentToolkitViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AndroidAgentToolkitEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AndroidAgentToolkitEffect.ShowError -> {
                    snackbarHostState.showSnackbar("${effect.title}: ${effect.message}")
                }
                is AndroidAgentToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Android Agent Toolkit", effect.text))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
                is AndroidAgentToolkitEffect.TriggerPipelineExport -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pipeline YAML", effect.yaml))
                    Toast.makeText(context, "Pipeline YAML copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                is AndroidAgentToolkitEffect.LogAgentCommand -> {
                    // Agent log handled internally / Agent 日志内部处理
                }
            }
        }
    }

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(state.selectedTab) {
        val titles = listOf(
            "Agent 工具链", "CLI", "Skills", "Knowledge Base", "CI/CD", "设置"
        )
        val actions = when (state.selectedTab) {
            AndroidAgentToolkitTab.DASHBOARD -> listOf(
                TopBarAction(icon = Icons.Default.Refresh, contentDescription = "刷新") {
                    viewModel.sendIntent(AndroidAgentToolkitIntent.RefreshDashboard)
                }
            )
            AndroidAgentToolkitTab.CLI -> listOf(
                TopBarAction(icon = Icons.Default.Clear, contentDescription = "清空历史") {
                    viewModel.sendIntent(AndroidAgentToolkitIntent.ClearCommandHistory)
                }
            )
            AndroidAgentToolkitTab.KNOWLEDGE_BASE -> listOf(
                TopBarAction(icon = Icons.Default.Sync, contentDescription = "同步") {
                    viewModel.sendIntent(AndroidAgentToolkitIntent.SyncKnowledgeBase)
                }
            )
            AndroidAgentToolkitTab.SKILLS -> listOf(
                TopBarAction(icon = Icons.Default.Add, contentDescription = "新建 Skill") {
                    viewModel.sendIntent(AndroidAgentToolkitIntent.CreateNewSkill)
                }
            )
            else -> emptyList()
        }
        onUpdateTopBar(TopBarConfig(title = titles[state.selectedTab], actions = actions))
    }

    // Load initial dashboard data / 加载初始仪表盘数据
    LaunchedEffect(Unit) {
        viewModel.sendIntent(AndroidAgentToolkitIntent.RefreshDashboard)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 栏
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                TabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.SelectTab(index)) },
                        text = { Text(title, fontSize = 12.sp) },
                        icon = {
                            Icon(
                                imageVector = TabIcons[index],
                                contentDescription = title,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            // Tab Content / Tab 内容
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = {
                    fadeIn(tween(200)) + slideInHorizontally { it / 4 } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally { -it / 4 }
                },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    AndroidAgentToolkitTab.DASHBOARD -> DashboardTabContent(state, viewModel)
                    AndroidAgentToolkitTab.CLI -> CLITabContent(state, viewModel)
                    AndroidAgentToolkitTab.SKILLS -> SkillsTabContent(state, viewModel)
                    AndroidAgentToolkitTab.KNOWLEDGE_BASE -> KnowledgeBaseTabContent(state, viewModel)
                    AndroidAgentToolkitTab.CICD -> CICDTabContent(state, viewModel)
                    AndroidAgentToolkitTab.SETTINGS -> SettingsTabContent(state, viewModel)
                }
            }
        }
    }
}

// ================================================================
// Tab titles and icons / Tab 标题和图标
// ================================================================
private val TabTitles = listOf(
    "总览", "CLI", "Skills", "知识库", "CI/CD", "设置"
)

private val TabIcons = listOf(
    Icons.Default.Dashboard,
    Icons.Default.Terminal,
    Icons.Default.Code,
    Icons.Default.Cloud,
    Icons.Default.Build,
    Icons.Default.Settings
)

// ================================================================
// Dashboard Tab / 仪表盘 Tab
// ================================================================
@Composable
private fun DashboardTabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Token Saving Card / Token 节省卡片
        item {
            TokenSavingCard(state.tokenSavingStats)
        }

        // Module Status Cards / 模块状态卡片
        items(state.moduleStatuses) { module ->
            ModuleStatusCard(module)
        }

        // Quick Start Guide / 快速上手指南
        item {
            QuickStartGuideCard(
                onSetupCLI = { viewModel.sendIntent(AndroidAgentToolkitIntent.SelectTab(AndroidAgentToolkitTab.CLI)) },
                onExploreSkills = { viewModel.sendIntent(AndroidAgentToolkitIntent.SelectTab(AndroidAgentToolkitTab.SKILLS)) },
                onSyncKB = { viewModel.sendIntent(AndroidAgentToolkitIntent.SyncKnowledgeBase) }
            )
        }
    }
}

/**
 * Token Saving Card / Token 节省统计卡片
 * Displays token savings from Android CLI + Skills usage
 */
@Composable
private fun TokenSavingCard(stats: TokenSavingStats?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AgentColors.Primary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Token 节省统计 / Token Savings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (stats != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("总 Token / Total", "${stats.totalTokens}")
                    StatItem("已节省 / Saved", "${stats.savedTokens}")
                    StatItem("节省率 / Rate", "${stats.savingPercent}%")
                    StatItem("项目数 / Projects", "${stats.projectCount}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Token saving gauge / Token 节省仪表
                val progress by animateFloatAsState(targetValue = stats.savingPercent / 100f, label = "GaugeProgress")
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AgentColors.Secondary,
                    trackColor = AgentColors.Secondary.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Google 内部数据显示：CLI + Skills 节省 70% token，提升 3x 速度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AgentColors.Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Module Status Card / 模块状态卡片
 */
@Composable
private fun ModuleStatusCard(module: ModuleStatus) {
    val stateColor = when (module.state) {
        ModuleState.INSTALLED -> AgentColors.Secondary
        ModuleState.NOT_INSTALLED -> AgentColors.Error
        ModuleState.PENDING_CONFIG -> AgentColors.Tertiary
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator / 状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(stateColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "v${module.version}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Quick Start Guide Card / 快速上手指南卡片
 */
@Composable
private fun QuickStartGuideCard(
    onSetupCLI: () -> Unit,
    onExploreSkills: () -> Unit,
    onSyncKB: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快速上手 / Quick Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            QuickStartItem(
                step = "1",
                title = "配置 Android CLI",
                description = "安装并验证 CLI 工具链",
                onClick = onSetupCLI
            )
            QuickStartItem(
                step = "2",
                title = "探索 Skills 库",
                description = "发现并安装 AI Agent 技能",
                onClick = onExploreSkills
            )
            QuickStartItem(
                step = "3",
                title = "同步知识库",
                description = "下载最新的 Android 开发知识",
                onClick = onSyncKB
            )
        }
    }
}

@Composable
private fun QuickStartItem(
    step: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AgentColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AgentColors.Primary, modifier = Modifier.size(20.dp))
    }
}

// ================================================================
// CLI Tab / CLI Tab
// ================================================================
@Composable
private fun CLITabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Available commands shortcuts / 可用命令快捷面板
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.availableCommands) { cmd ->
                FilterChip(
                    selected = state.commandInput.startsWith(cmd),
                    onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateCommandInput(cmd)) },
                    label = { Text(cmd, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AgentColors.Primary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        // Terminal output area / 终端输出区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(AgentColors.Surface)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.commandHistory.isEmpty()) {
                    Text(
                        text = "# Android CLI Terminal\n# Type a command below and press Enter",
                        color = AgentColors.OnSurface.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
                state.commandHistory.forEach { record ->
                    TerminalCommandRecord(record)
                }
            }
        }

        // Command input bar / 命令输入栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "›",
                color = AgentColors.TerminalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = state.commandInput,
                onValueChange = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateCommandInput(it)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = AgentColors.OnSurface
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (state.commandInput.isEmpty()) {
                            Text(
                                text = "Enter command (e.g. sdkmanager --list)",
                                color = AgentColors.OnSurface.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (state.isExecuting) {
                IconButton(onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.CancelCommand) }) {
                    Icon(Icons.Default.Cancel, "Cancel", tint = AgentColors.Error)
                }
            } else {
                IconButton(
                    onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.ExecuteCommand) },
                    enabled = state.commandInput.isNotEmpty()
                ) {
                    Icon(Icons.Default.PlayArrow, "Execute", tint = AgentColors.Secondary)
                }
            }
        }
    }
}

/**
 * Terminal Command Record / 终端命令记录
 */
@Composable
private fun TerminalCommandRecord(record: CommandRecord) {
    Column {
        // Command line / 命令行
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = AgentColors.TerminalYellow)) {
                    append("[${formatTimestamp(record.timestamp)}] ")
                }
                withStyle(SpanStyle(color = AgentColors.OnSurface)) {
                    append(record.command)
                }
            },
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )

        // Output / 输出
        if (record.output.isNotEmpty()) {
            Text(
                text = record.output,
                color = AgentColors.TerminalGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Error output / 错误输出
        if (record.errorOutput.isNotEmpty()) {
            Text(
                text = record.errorOutput,
                color = AgentColors.TerminalRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Status indicator / 状态指示
        val statusColor = when (record.status) {
            CommandStatus.SUCCESS -> AgentColors.TerminalGreen
            CommandStatus.FAILED -> AgentColors.TerminalRed
            CommandStatus.RUNNING -> AgentColors.TerminalYellow
        }
        Text(
            text = "→ ${record.status.name}",
            color = statusColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun formatTimestamp(ts: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}

// ================================================================
// Skills Tab / Skills Tab
// ================================================================
@Composable
private fun SkillsTabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    var showMarketplace by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar / 搜索栏
        OutlinedTextField(
            value = state.skillSearchQuery,
            onValueChange = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateSkillSearch(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search skills... / 搜索 Skill...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (state.skillSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateSkillSearch("")) }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true
        )

        // Toggle: Installed vs Marketplace / 切换：已安装 vs 市场
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showMarketplace,
                onClick = { showMarketplace = false },
                label = { Text("已安装 / Installed (${state.installedSkills.size})") }
            )
            FilterChip(
                selected = showMarketplace,
                onClick = { showMarketplace = true },
                label = { Text("市场 / Marketplace") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displaySkills = if (showMarketplace) state.skillMarketplace else state.installedSkills
        val filteredSkills = if (state.skillSearchQuery.isNotEmpty()) {
            displaySkills.filter {
                it.name.contains(state.skillSearchQuery, ignoreCase = true) ||
                it.description.contains(state.skillSearchQuery, ignoreCase = true)
            }
        } else displaySkills

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredSkills) { skill ->
                SkillCard(
                    skill = skill,
                    isSelected = state.selectedSkill?.id == skill.id,
                    onSelect = { viewModel.sendIntent(AndroidAgentToolkitIntent.SelectSkill(skill)) },
                    onInstall = { viewModel.sendIntent(AndroidAgentToolkitIntent.InstallSelectedSkill) },
                    onUninstall = { viewModel.sendIntent(AndroidAgentToolkitIntent.UninstallSelectedSkill) }
                )
            }
        }

        // Compatibility Matrix / 兼容性矩阵
        if (state.selectedSkill != null) {
            CompatibilityMatrixSection(state.skillCompatibilityMatrix)
        }
    }
}

@Composable
private fun SkillCard(
    skill: SkillInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AgentColors.Primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AgentColors.Primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "v${skill.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (skill.isInstalled) {
                    Badge(containerColor = AgentColors.Secondary) { Text("INSTALLED", fontSize = 10.sp) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(skill.scenarios) { tag ->
                    Badge(containerColor = AgentColors.Primary.copy(alpha = 0.2f)) {
                        Text(tag, fontSize = 10.sp, color = AgentColors.Primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("CLI: ${skill.cliVersion}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Text("AGP: ${skill.agpVersion}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (skill.isInstalled) {
                        Button(
                            onClick = onUninstall,
                            colors = ButtonDefaults.buttonColors(containerColor = AgentColors.Error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("卸载")
                        }
                    } else {
                        Button(
                            onClick = onInstall,
                            colors = ButtonDefaults.buttonColors(containerColor = AgentColors.Secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("安装")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityMatrixSection(matrix: CompatibilityMatrix?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("兼容性矩阵 / Compatibility Matrix", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (matrix != null) {
                // Simplified matrix display / 简化矩阵展示
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Skill\\CLI", modifier = Modifier.width(80.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    matrix.cliVersions.forEach { cli ->
                        Text(cli, modifier = Modifier.weight(1f), fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                matrix.cells.take(6).forEach { cell ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${cell.skillVersion}", modifier = Modifier.width(80.dp), fontSize = 10.sp)
                        matrix.cliVersions.forEach { _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .background(
                                        if (cell.isCompatible) AgentColors.Secondary.copy(alpha = 0.3f)
                                        else AgentColors.Error.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (cell.isCompatible) Icons.Default.Check else Icons.Default.Close,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (cell.isCompatible) AgentColors.Secondary else AgentColors.Error
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    "Select a skill to view compatibility matrix / 选择 Skill 查看兼容性矩阵",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ================================================================
// Knowledge Base Tab / 知识库 Tab
// ================================================================
@Composable
private fun KnowledgeBaseTabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Sync status bar / 同步状态栏
        SyncStatusBar(state.kbSyncStatus, state.kbLastSyncTime, state.kbCacheSize) {
            viewModel.sendIntent(AndroidAgentToolkitIntent.SyncKnowledgeBase)
        }

        // Category chips / 分类标签
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.kbCategories) { category ->
                FilterChip(
                    selected = false,
                    onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateKBQuery(category)) },
                    label = { Text(category) }
                )
            }
        }

        // Query input / 查询输入
        OutlinedTextField(
            value = state.kbQueryInput,
            onValueChange = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateKBQuery(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search Android knowledge... / 搜索 Android 知识...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (state.kbQueryInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateKBQuery("")) }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Results / 搜索结果
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.kbSearchResults.isEmpty() && state.kbQueryInput.isNotEmpty()) {
                item {
                    Text(
                        "No results found / 未找到结果",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.kbSearchResults) { entry ->
                KBEntryCard(entry)
            }
        }
    }
}

@Composable
private fun SyncStatusBar(
    syncStatus: SyncStatus,
    lastSyncTime: String,
    cacheSize: String,
    onSync: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Last sync: $lastSyncTime", style = MaterialTheme.typography.labelSmall)
            Text("Cache: $cacheSize", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val statusColor = when (syncStatus.state) {
                SyncState.IDLE -> AgentColors.Secondary
                SyncState.SYNCING -> AgentColors.Primary
                SyncState.ERROR -> AgentColors.Error
                SyncState.OFFLINE -> AgentColors.Tertiary
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(syncStatus.state.name, style = MaterialTheme.typography.labelSmall)

            if (syncStatus.state == SyncState.SYNCING) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun KBEntryCard(entry: KBEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(containerColor = AgentColors.Primary.copy(alpha = 0.2f)) {
                            Text(entry.category, fontSize = 10.sp, color = AgentColors.Primary)
                        }
                        Badge(containerColor = AgentColors.Secondary.copy(alpha = 0.2f)) {
                            Text("Android ${entry.androidVersion}", fontSize = 10.sp, color = AgentColors.Secondary)
                        }
                    }
                }
                Text(
                    text = "${(entry.relevanceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = AgentColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.snippet,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
        }
    }
}

// ================================================================
// CI/CD Tab / CI/CD Tab
// ================================================================
@Composable
private fun CICDTabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Template selector / 模板选择器
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            PipelineTemplate.entries.forEachIndexed { index, template ->
                SegmentedButton(
                    selected = state.selectedPipelineTemplate == template,
                    onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.SelectPipelineTemplate(template)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = PipelineTemplate.entries.size)
                ) {
                    Text(
                        when (template) {
                            PipelineTemplate.GITHUB_ACTIONS -> "GitHub"
                            PipelineTemplate.GITLAB_CI -> "GitLab"
                            PipelineTemplate.JENKINS -> "Jenkins"
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }

        // YAML Editor / YAML 编辑器
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pipeline YAML",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.RunPipelineDiagnostics) }) {
                            Icon(Icons.Default.Info, "Diagnostics", tint = AgentColors.Primary)
                        }
                        IconButton(onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.ExportPipelineConfig) }) {
                            Icon(Icons.Default.ContentCopy, "Export", tint = AgentColors.Secondary)
                        }
                    }
                }

                // Diagnostics issues / 诊断问题
                if (state.pipelineDiagnostics.isNotEmpty()) {
                    state.pipelineDiagnostics.forEach { issue ->
                        val issueColor = when (issue.severity) {
                            "error" -> AgentColors.Error
                            "warning" -> AgentColors.Tertiary
                            else -> AgentColors.Primary
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(issueColor.copy(alpha = 0.1f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (issue.severity) {
                                    "error" -> Icons.Default.Error
                                    "warning" -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                null,
                                tint = issueColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (issue.lineNumber != null) "Line ${issue.lineNumber}: ${issue.message}"
                                else issue.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = issueColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // YAML text area / YAML 文本区
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(AgentColors.Surface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    BasicTextField(
                        value = state.pipelineYaml,
                        onValueChange = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdatePipelineYaml(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = AgentColors.OnSurface
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (state.pipelineYaml.isEmpty()) {
                                    Text(
                                        "# Pipeline YAML / 流水线 YAML\n# Select a template or write your own",
                                        color = AgentColors.OnSurface.copy(alpha = 0.4f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ================================================================
// Settings Tab / 设置 Tab
// ================================================================
@Composable
private fun SettingsTabContent(
    state: AndroidAgentToolkitState,
    viewModel: AndroidAgentToolkitViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Provider / AI Provider 配置
        item {
            SettingsCard(title = "AI Provider / AI 提供商") {
                AIProvider.entries.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateAIProvider(provider)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.aiProvider == provider,
                            onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateAIProvider(provider)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (provider) {
                                AIProvider.GEMINI -> "Google Gemini"
                                AIProvider.CLAUDE -> "Anthropic Claude"
                                AIProvider.GPT -> "OpenAI GPT"
                                AIProvider.LOCAL -> "Local Model / 本地模型"
                            }
                        )
                    }
                }
            }
        }

        // Output Format / 输出格式
        item {
            SettingsCard(title = "Output Format / 输出格式") {
                OutputFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateOutputFormat(format)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.outputFormat == format,
                            onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateOutputFormat(format)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (format) {
                                OutputFormat.HUMAN_READABLE -> "Human Readable / 人类可读"
                                OutputFormat.JSON -> "JSON"
                                OutputFormat.MARKDOWN -> "Markdown"
                            }
                        )
                    }
                }
            }
        }

        // Log Level / 日志级别
        item {
            SettingsCard(title = "Log Level / 日志级别") {
                LogLevel.entries.forEach { level ->
                    val levelColor = when (level) {
                        LogLevel.DEBUG -> AgentColors.Primary
                        LogLevel.INFO -> AgentColors.Secondary
                        LogLevel.WARNING -> AgentColors.Tertiary
                        LogLevel.ERROR -> AgentColors.Error
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateLogLevel(level)) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.logLevel == level,
                            onClick = { viewModel.sendIntent(AndroidAgentToolkitIntent.UpdateLogLevel(level)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(levelColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(level.name)
                    }
                }
            }
        }

        // About / 关于
        item {
            SettingsCard(title = "About / 关于") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AboutItem("Version / 版本", "1.0.0")
                    AboutItem("Android CLI", "2.0.0")
                    AboutItem("Android Skills", "2.0.0")
                    AboutItem("Knowledge Base", "1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Android CLI + Skills + Knowledge Base AI Agent Toolchain\n" +
                        "Android 官方 AI Agent 开发工具链\n" +
                        "Google 内部数据显示：70% token 节省 + 3x 速度提升",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun AboutItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
