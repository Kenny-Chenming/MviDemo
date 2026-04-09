package com.mvi.kenny.feature.agentskillsdevkit

/**
 * ============================================================
 * AgentSkillsDevKitScreen — UI 层
 * ============================================================
 * PRD-041 | Android Studio Panda 3 Agent Skills 开发工具包
 * Mobile Prototype — 三标签页界面
 *
 * 页面结构（Three-tab layout）:
 * — Tab 0: Skills 编辑器（Skill Editor）
 * — Tab 1: 权限策略（Permission Dashboard）
 * — Tab 2: 执行审计（Audit Timeline）
 *
 * 视觉规范对应（Visual Spec Mapping）:
 * — Section 7.1: 暗色主题 Darcula 配色
 *   Background: #1E1E1E, Surface: #252526
 *   Accent: #4FC3F7, Success: #81C784, Warning: #FFB74D, Error: #E57373
 * — Section 7.2: JetBrains Mono 等宽字体（代码区域）
 * — Section 7.3: 4dp 基准网格，12dp 内边距
 *
 * @param viewModel AgentSkillsDevKitViewModel 实例
 * @param onUpdateTopBar TopBar 配置更新回调
 * @see AgentSkillsDevKitViewModel
 * @see AgentSkillsDevKitContract
 * —————————————————————————————————————————————————————
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// Color Palette (Darcula Dark Theme — Section 7.1)
// ================================================================

private val AccentBlue = Color(0xFF4FC3F7)
private val SuccessGreen = Color(0xFF81C784)
private val WarningYellow = Color(0xFFFFB74D)
private val ErrorRed = Color(0xFFE57373)
private val DangerRed = Color(0xFFEF5350)

// Dark theme palette
private val DarkBg1 = Color(0xFF1E1E1E)      // 面板背景
private val DarkBg2 = Color(0xFF252526)      // 编辑器背景
private val DarkBg3 = Color(0xFF2D2D30)      // 输入框背景
private val DarkBorder = Color(0xFF3C3C3C)
private val DarkTextPrimary = Color(0xFFCCCCCC)
private val DarkTextSecondary = Color(0xFF808080)

// JetBrains Mono font for code
private val CodeFontFamily = FontFamily.Monospace

// ================================================================
// Tab definitions
// ================================================================

private data class DevKitTab(
    val label: String,
    val labelZh: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val tabs = listOf(
    DevKitTab("Editor", "编辑器", Icons.Default.Edit),
    DevKitTab("Permissions", "权限策略", Icons.Default.Security),
    DevKitTab("Audit", "执行审计", Icons.Default.History)
)

// ================================================================
// AgentSkillsDevKitScreen — Main Container
// ================================================================

@Composable
fun AgentSkillsDevKitScreen(
    viewModel: AgentSkillsDevKitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar
    LaunchedEffect(state.currentTab) {
        val tab = tabs[state.currentTab]
        onUpdateTopBar(
            TopBarConfig(
                title = "AgentSkills DevKit",
                subtitle = tab.labelZh,
                actions = emptyList()
            )
        )
    }

    // Collect effects for snackbar
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AgentSkillsDevKitEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg1
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row — Navigation tabs
            TabRow(
                selectedTabIndex = state.currentTab,
                containerColor = DarkBg2,
                contentColor = AccentBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[state.currentTab]),
                        color = AccentBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = { viewModel.handleIntent(AgentSkillsDevKitIntent.SwitchTab(index)) },
                        text = {
                            Text(
                                text = tab.labelZh,
                                fontFamily = FontFamily.Default,
                                color = if (state.currentTab == index) AccentBlue else DarkTextSecondary
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (state.currentTab == index) AccentBlue else DarkTextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (state.currentTab) {
                0 -> SkillEditorTab(
                    state = state.skillEditor,
                    onIntent = { viewModel.handleIntent(AgentSkillsDevKitIntent.EditorIntent(it)) }
                )
                1 -> PermissionDashboardTab(
                    state = state.permissionDashboard,
                    onIntent = { viewModel.handleIntent(AgentSkillsDevKitIntent.PermissionIntent(it)) }
                )
                2 -> AuditTab(
                    state = state.audit,
                    onIntent = { viewModel.handleIntent(AgentSkillsDevKitIntent.AuditIntent(it)) }
                )
            }
        }
    }
}

// ================================================================
// Tab 0: Skill Editor
// ================================================================

@Composable
private fun SkillEditorTab(
    state: SkillEditorState,
    onIntent: (SkillEditorIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg1)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section: Basic Info
        SectionHeader(title = "Skill 基本信息", icon = Icons.Default.Info)

        // Name field
        FormField(label = "名称 (Name)") {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onIntent(SkillEditorIntent.UpdateName(it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
                placeholder = { Text("Enter skill name", color = DarkTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary,
                    cursorColor = AccentBlue
                ),
                singleLine = true
            )
        }

        // Description field
        FormField(label = "描述 (Description)") {
            OutlinedTextField(
                value = state.description,
                onValueChange = { onIntent(SkillEditorIntent.UpdateDescription(it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
                placeholder = { Text("Describe what this skill does", color = DarkTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary,
                    cursorColor = AccentBlue
                ),
                minLines = 2,
                maxLines = 3
            )
        }

        HorizontalDivider(color = DarkBorder, thickness = 1.dp)

        // Section: DSL Code Editor
        SectionHeader(title = "DSL 代码编辑器", icon = Icons.Default.Code)

        // Validation errors display
        if (state.validationErrors.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "⚠ ${state.validationErrors.size} 个 DSL 错误",
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    state.validationErrors.take(5).forEach { error ->
                        Text(
                            text = "  Line ${error.line}: ${error.message}",
                            color = ErrorRed.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontFamily = CodeFontFamily
                        )
                    }
                    if (state.validationErrors.size > 5) {
                        Text(
                            text = "  ... 还有 ${state.validationErrors.size - 5} 个错误",
                            color = ErrorRed.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // DSL code editor (OutlinedTextField with monospace font)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(DarkBg2, RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = state.dslContent,
                onValueChange = { onIntent(SkillEditorIntent.UpdateDsl(it)) },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = AccentBlue,
                    fontSize = 13.sp,
                    fontFamily = CodeFontFamily,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(AccentBlue),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.dslContent.isEmpty()) {
                            Text(
                                text = "// Enter DSL code here...",
                                color = DarkTextSecondary,
                                fontSize = 13.sp,
                                fontFamily = CodeFontFamily
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        HorizontalDivider(color = DarkBorder, thickness = 1.dp)

        // Section: Version
        SectionHeader(title = "版本配置", icon = Icons.Default.NewReleases)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormField(label = "版本号", modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = state.version,
                    onValueChange = { onIntent(SkillEditorIntent.UpdateVersion(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
                    placeholder = { Text("1.0.0", color = DarkTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary,
                        cursorColor = AccentBlue
                    ),
                    singleLine = true
                )
            }
        }

        FormField(label = "版本变更说明") {
            OutlinedTextField(
                value = state.versionNotes,
                onValueChange = { onIntent(SkillEditorIntent.UpdateVersionNotes(it)) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = DarkTextPrimary, fontSize = 14.sp),
                placeholder = { Text("Describe what changed in this version", color = DarkTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary,
                    cursorColor = AccentBlue
                ),
                minLines = 2,
                maxLines = 3
            )
        }

        HorizontalDivider(color = DarkBorder, thickness = 1.dp)

        // Test Result display
        state.testResult?.let { result ->
            TestResultCard(result = result)
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Validate Button
            OutlinedButton(
                onClick = { onIntent(SkillEditorIntent.ValidateDsl(state.dslContent)) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("校验")
            }

            // Test Button
            OutlinedButton(
                onClick = { onIntent(SkillEditorIntent.RunLocalTest) },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningYellow),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = WarningYellow,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("测试")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save Button
            Button(
                onClick = { onIntent(SkillEditorIntent.SaveSkill) },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving && state.isDirty,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = DarkBg3,
                    disabledContentColor = DarkTextSecondary
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DarkTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(if (state.isDirty) "保存" else "已保存")
            }

            // Discard Button
            OutlinedButton(
                onClick = { onIntent(SkillEditorIntent.DiscardChanges) },
                modifier = Modifier.weight(1f),
                enabled = state.isDirty,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                )
            ) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("放弃")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ================================================================
// Tab 1: Permission Dashboard
// ================================================================

@Composable
private fun PermissionDashboardTab(
    state: PermissionDashboardState,
    onIntent: (PermissionDashboardIntent) -> Unit
) {
    var showTemplateDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg1)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with template selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "权限树", icon = Icons.Default.AccountTree)
            Box {
                OutlinedButton(
                    onClick = { showTemplateDropdown = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(state.activeTemplate ?: "应用模板")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showTemplateDropdown,
                    onDismissRequest = { showTemplateDropdown = false },
                    modifier = Modifier.background(DarkBg2)
                ) {
                    listOf("Read-Only", "Standard Dev", "Admin").forEach { template ->
                        DropdownMenuItem(
                            text = { Text(template, color = DarkTextPrimary) },
                            onClick = {
                                onIntent(PermissionDashboardIntent.ApplyTemplate(template))
                                showTemplateDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PermissionMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(mode.colorArgb), CircleShape)
                    )
                    Text(
                        text = when (mode) {
                            PermissionMode.ALLOW -> "允许"
                            PermissionMode.READ_ONLY -> "只读"
                            PermissionMode.DENY -> "拒绝"
                            PermissionMode.UNSET -> "未设置"
                        },
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Permission tree
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(DarkBg2, RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyColumn {
                itemsIndexed(state.rootNodes) { _, node ->
                    PermissionTreeNodeItem(
                        node = node,
                        depth = 0,
                        onSetPermission = { path, mode ->
                            onIntent(PermissionDashboardIntent.SetNodePermission(path, mode))
                        },
                        onToggleExpand = { path ->
                            onIntent(PermissionDashboardIntent.ToggleNodeExpand(path))
                        },
                        onSelect = { onIntent(PermissionDashboardIntent.SelectNode(it)) },
                        selectedNode = state.selectedNode
                    )
                }
            }
        }

        // Save button
        Button(
            onClick = { onIntent(PermissionDashboardIntent.SavePolicy) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && state.isDirty,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.Black,
                disabledContainerColor = DarkBg3,
                disabledContentColor = DarkTextSecondary
            )
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = DarkTextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (state.isDirty) "保存策略" else "策略已保存")
        }
    }
}

@Composable
private fun PermissionTreeNodeItem(
    node: PermissionNode,
    depth: Int,
    onSetPermission: (String, PermissionMode) -> Unit,
    onToggleExpand: (String) -> Unit,
    onSelect: (PermissionNode) -> Unit,
    selectedNode: PermissionNode?
) {
    val isSelected = selectedNode?.path == node.path
    var showModeMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(node) }
                .padding(start = (depth * 20).dp, top = 6.dp, bottom = 6.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand/collapse icon for directories
            if (node.isDirectory) {
                IconButton(
                    onClick = { onToggleExpand(node.path) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (node.isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = if (node.isExpanded) "Collapse" else "Expand",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(Modifier.width(24.dp))
            }

            // Icon
            Icon(
                imageVector = if (node.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (node.isDirectory) WarningYellow else DarkTextSecondary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            // Name
            Text(
                text = node.name,
                color = if (isSelected) AccentBlue else DarkTextPrimary,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )

            // Mode indicator (colored dot)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(node.mode.colorArgb), CircleShape)
                    .clickable { showModeMenu = true }
            )

            // Mode menu
            Box {
                DropdownMenu(
                    expanded = showModeMenu,
                    onDismissRequest = { showModeMenu = false },
                    modifier = Modifier.background(DarkBg2)
                ) {
                    PermissionMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(mode.colorArgb), CircleShape)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = when (mode) {
                                            PermissionMode.ALLOW -> "✓ 允许"
                                            PermissionMode.READ_ONLY -> "R 只读"
                                            PermissionMode.DENY -> "✕ 拒绝"
                                            PermissionMode.UNSET -> "— 未设置"
                                        },
                                        color = DarkTextPrimary
                                    )
                                }
                            },
                            onClick = {
                                onSetPermission(node.path, mode)
                                showModeMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Render children if expanded
        if (node.isExpanded && node.children.isNotEmpty()) {
            node.children.forEach { child ->
                PermissionTreeNodeItem(
                    node = child,
                    depth = depth + 1,
                    onSetPermission = onSetPermission,
                    onToggleExpand = onToggleExpand,
                    onSelect = onSelect,
                    selectedNode = selectedNode
                )
            }
        }
    }
}

// ================================================================
// Tab 2: Audit Timeline
// ================================================================

@Composable
private fun AuditTab(
    state: AuditState,
    onIntent: (AuditIntent) -> Unit
) {
    var selectedEntry by remember { mutableStateOf<AuditEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg1)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        SectionHeader(title = "执行审计时间线", icon = Icons.Default.Timeline)

        // Summary stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBg2, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val successCount = state.entries.count { it.outcome == AuditOutcome.SUCCESS }
            val deniedCount = state.entries.count { it.outcome == AuditOutcome.DENIED }
            val warningCount = state.entries.count { it.outcome == AuditOutcome.WARNING }

            StatChip(label = "成功", count = successCount, color = SuccessGreen)
            StatChip(label = "拒绝", count = deniedCount, color = ErrorRed)
            StatChip(label = "警告", count = warningCount, color = WarningYellow)
        }

        // Timeline list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(DarkBg2, RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.entries) { entry ->
                    AuditTimelineCard(
                        entry = entry,
                        onClick = { selectedEntry = entry }
                    )
                }
            }
        }
    }

    // Detail bottom sheet simulation (using Dialog)
    selectedEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { selectedEntry = null },
            containerColor = DarkBg2,
            title = {
                Text(
                    text = entry.skillName,
                    color = AccentBlue,
                    fontFamily = FontFamily.Default
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("Agent", entry.agentName)
                    DetailRow("操作", entry.action)
                    entry.targetPath?.let { DetailRow("路径", it) }
                    DetailRow("耗时", "${entry.durationMs}ms")
                    DetailRow("结果", entry.outcome.displayName)
                    DetailRow("时间", entry.timestamp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEntry = null }) {
                    Text("关闭", color = AccentBlue)
                }
            }
        )
    }
}

@Composable
private fun AuditTimelineCard(
    entry: AuditEntry,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg1, RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Outcome indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(top = 4.dp)
                .background(Color(entry.outcome.colorArgb), CircleShape)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.action,
                    color = DarkTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${entry.durationMs}ms",
                    color = DarkTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.skillName,
                    color = AccentBlue,
                    fontSize = 12.sp
                )
                Text(
                    text = entry.agentName,
                    color = DarkTextSecondary,
                    fontSize = 12.sp
                )
            }

            entry.targetPath?.let { path ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = path,
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = CodeFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = entry.timestamp,
                color = DarkTextSecondary.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }

        // Outcome badge
        Box(
            modifier = Modifier
                .background(
                    Color(entry.outcome.colorArgb).copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = entry.outcome.displayName,
                color = Color(entry.outcome.colorArgb),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ================================================================
// Reusable Components
// ================================================================

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            color = DarkTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 12.sp
        )
        content()
    }
}

@Composable
private fun TestResultCard(result: TestResult) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (result.passed) SuccessGreen.copy(alpha = 0.1f)
                else ErrorRed.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (result.passed) SuccessGreen.copy(alpha = 0.3f)
                else ErrorRed.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (result.passed) "✓ 测试通过" else "✕ 测试失败",
                color = if (result.passed) SuccessGreen else ErrorRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            result.logs.forEach { log ->
                Text(
                    text = log,
                    color = when {
                        log.startsWith("[ERROR]") || log.startsWith("[WARN]") -> WarningYellow
                        log.startsWith("[SUCCESS]") -> SuccessGreen
                        log.startsWith("[INFO]") -> DarkTextSecondary
                        else -> DarkTextPrimary
                    },
                    fontSize = 11.sp,
                    fontFamily = CodeFontFamily
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = DarkTextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = DarkTextSecondary, fontSize = 13.sp)
        Text(text = value, color = DarkTextPrimary, fontSize = 13.sp)
    }
}
