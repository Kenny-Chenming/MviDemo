package com.mvi.kenny.feature.aluminiumosdesktop

import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.base.TopBarAction

// ================================================================
// AluminiumOSDesktopScreen — Aluminium OS 桌面适配工具包主界面
// ================================================================
// Main Screen for Aluminium OS Android App Desktop Adaptation Toolkit.
//
// PRD-227: Aluminium OS Android App 桌面适配工具包
// Design Reference: memory/agency/designs/PRD-227-Aluminium-OS-桌面适配工具包.md
//
// 5-Tab Architecture:
//   Tab 0: 桌面兼容性自检 (Compatibility Scanner)
//   Tab 1: 桌面 UI 改造清单 (UI Modification Checklist)
//   Tab 2: 键鼠适配指南 (Keyboard/Mouse Adaptation Guide)
//   Tab 3: Gemini AI 集成 (Gemini AI Integration)
//   Tab 4: Play Store 优化 (Play Store Optimization)
//
// Visual Style: Dark Terminal — deep dark background, monospace fonts,
//               risk-level color coding (red/orange/yellow/green)
//
// @see AluminiumOSDesktopViewModel for MVI state management
// @see AluminiumOSDesktopContract for state/intent/effect definitions
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// Terminal Color Palette
// ─────────────────────────────────────────────────────────────────────────────
private val TerminalBg = Color(0xFF0D1117)
private val TerminalSurface = Color(0xFF161B22)
private val TerminalBorder = Color(0xFF30363D)
private val TerminalGreen = Color(0xFF00E676)
private val TerminalBlue = Color(0xFF58A6FF)
private val TerminalYellow = Color(0xFFFFD54F)
private val TerminalOrange = Color(0xFFFFB74D)
private val TerminalRed = Color(0xFFF85149)
private val TerminalGray = Color(0xFF8B949E)
private val TerminalWhite = Color(0xFFE0E0E0)

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen Entry Point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AluminiumOSDesktopScreen(
    viewModel: AluminiumOSDesktopViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Collect effects for one-time events
    // 收集一次性副作用事件
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is AluminiumOSDesktopEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AluminiumOSDesktopEffect.CopyToClipboard -> {
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                is AluminiumOSDesktopEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is AluminiumOSDesktopEffect.TerminalOutput -> { /* handled in scan tab */ }
                is AluminiumOSDesktopEffect.ScanCompleted -> { /* handled in scan tab */ }
                is AluminiumOSDesktopEffect.OpenUrl -> { /* URL opening not implemented */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        // ── Tab Row ──
        // Tab 切换行（5 个功能 Tab）
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = TerminalSurface,
            contentColor = TerminalGreen,
            edgePadding = 8.dp,
            divider = { HorizontalDivider(color = TerminalBorder) }
        ) {
            TabTitles.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.processIntent(AluminiumOSDesktopIntent.SelectTab(index)) },
                    text = {
                        Text(
                            text = tabTitle.label,
                            color = if (state.selectedTab == index) TerminalGreen else TerminalGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tabTitle.icon,
                            contentDescription = tabTitle.label,
                            tint = if (state.selectedTab == index) TerminalGreen else TerminalGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // ── Tab Content ──
        // Tab 内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (state.selectedTab) {
                0 -> CompatibilityScannerTab(state, viewModel)
                1 -> UIModificationTab(state, viewModel)
                2 -> KeyboardMouseTab(state, viewModel)
                3 -> GeminiIntegrationTab(state, viewModel)
                4 -> PlayStoreOptimizationTab(state, viewModel)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab Titles
// ─────────────────────────────────────────────────────────────────────────────

private data class TabTitle(val label: String, val icon: ImageVector)

private val TabTitles = listOf(
    TabTitle("桌面兼容性自检", Icons.Default.DesktopWindows),
    TabTitle("UI改造清单", Icons.Default.CheckCircle),
    TabTitle("键鼠适配指南", Icons.Default.Keyboard),
    TabTitle("Gemini AI", Icons.Default.Psychology),
    TabTitle("Play优化", Icons.Default.ShoppingCart)
)

// ─────────────────────────────────────────────────────────────────────────────
// Tab 0: Compatibility Scanner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompatibilityScannerTab(
    state: AluminiumOSDesktopState,
    viewModel: AluminiumOSDesktopViewModel
) {
    val scanState = state.scanState
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // ── Input Section ──
        // 输入区域
        TerminalCard(title = "扫描配置 / Scan Configuration") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.projectSourceDir,
                    onValueChange = { viewModel.processIntent(AluminiumOSDesktopIntent.UpdateSourceDir(it)) },
                    label = { Text("源码目录 / Source Directory", fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("例如：app/src/main", fontFamily = FontFamily.Monospace, color = TerminalGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = terminalTextFieldColors(),
                    enabled = !scanState.isScanning,
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.packageName,
                    onValueChange = { viewModel.processIntent(AluminiumOSDesktopIntent.UpdatePackageName(it)) },
                    label = { Text("包名 / Package Name", fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("例如：com.example.myapp", fontFamily = FontFamily.Monospace, color = TerminalGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = terminalTextFieldColors(),
                    enabled = !scanState.isScanning,
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.processIntent(AluminiumOSDesktopIntent.StartScan) },
                        enabled = !scanState.isScanning && (state.projectSourceDir.isNotBlank() || state.packageName.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = TerminalBg)
                    ) {
                        if (scanState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TerminalBg,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = if (scanState.isScanning) "扫描中..." else "开始扫描 / Start Scan",
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (scanState.issues.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.processIntent(AluminiumOSDesktopIntent.ClearScanResults) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalGray, contentColor = TerminalBg)
                        ) {
                            Text("清除 / Clear", fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Progress Section ──
        // 进度显示区域
        if (scanState.isScanning) {
            TerminalCard(title = "扫描进度 / Scan Progress") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { scanState.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = TerminalGreen,
                        trackColor = TerminalBorder
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${scanState.phase.name} — ${scanState.progress}%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalGreen
                        )
                        Text(
                            text = "已扫描 ${scanState.scannedFilesCount} 个文件",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TerminalGray
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Summary Cards ──
        // 结果摘要卡片
        if (scanState.phase == ScanPhase.COMPLETED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard(
                    label = "P0 严重",
                    count = scanState.p0Count,
                    color = TerminalRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "P1 高风险",
                    count = scanState.p1Count,
                    color = TerminalOrange,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "P2 中风险",
                    count = scanState.p2Count,
                    color = TerminalYellow,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "P3 低风险",
                    count = scanState.p3Count,
                    color = TerminalGray,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Results List ──
        // 结果列表
        if (scanState.issues.isNotEmpty()) {
            TerminalCard(title = "扫描结果 / Scan Results (${scanState.issues.size} issues)") {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(scanState.issues) { index, issue ->
                        IssueCard(
                            issue = issue,
                            isExpanded = scanState.selectedIssue?.id == issue.id,
                            onClick = {
                                viewModel.processIntent(
                                    AluminiumOSDesktopIntent.SelectIssue(
                                        if (scanState.selectedIssue?.id == issue.id) null else issue
                                    )
                                )
                            },
                            onCopyCode = { code -> }  // clipboard handled in CodeBlock composable
                        )
                    }
                }
            }
        }

        // ── Empty State ──
        // 空状态
        if (scanState.issues.isEmpty() && scanState.phase == ScanPhase.IDLE) {
            TerminalCard(title = "说明 / Instructions") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TerminalText("1. 输入项目源码目录或包名", TerminalGreen)
                    TerminalText("2. 点击「开始扫描」分析兼容性", TerminalGreen)
                    TerminalText("3. 查看 P0/P1 问题并按建议修复", TerminalGreen)
                    TerminalText("4. 点击卡片展开查看代码示例", TerminalGreen)
                    TerminalText("", TerminalGray)
                    TerminalText("Demo: 输入任意目录名即可体验示例扫描结果", TerminalGray)
                }
            }
        }
    }
}

@Composable
private fun IssueCard(
    issue: CompatibilityIssue,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val borderColor = when (issue.riskLevel) {
        RiskLevel.P0 -> TerminalRed
        RiskLevel.P1 -> TerminalOrange
        RiskLevel.P2 -> TerminalYellow
        RiskLevel.P3 -> TerminalGray
        RiskLevel.PASS -> TerminalGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskBadge(riskLevel = issue.riskLevel)
                    Column {
                        Text(
                            text = issue.issueType.displayName,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TerminalWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = issue.file + ":" + issue.line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TerminalGray
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Expand,
                    contentDescription = "Expand",
                    tint = TerminalGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = issue.description,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray
            )

            Text(
                text = issue.descriptionCn,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Fix: ${issue.suggestion}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGreen
            )
            Text(
                text = issue.suggestionCn,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGreen.copy(alpha = 0.7f)
            )

            // Expanded: Code examples
            AnimatedVisibility(
                visible = isExpanded && (issue.beforeCode != null || issue.afterCode != null),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    if (issue.beforeCode != null) {
                        CodeBlock(
                            title = "BEFORE (问题代码)",
                            code = issue.beforeCode,
                            codeColor = TerminalRed,
                            onCopy = { onCopyCode(issue.beforeCode) }
                        )
                    }
                    if (issue.afterCode != null) {
                        CodeBlock(
                            title = "AFTER (修复代码)",
                            code = issue.afterCode,
                            codeColor = TerminalGreen,
                            onCopy = { onCopyCode(issue.afterCode) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(
    title: String,
    code: String,
    codeColor: Color,
    onCopy: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = codeColor,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { clipboardManager.setText(AnnotatedString(code)) }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TerminalGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalBg, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = codeColor
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 1: UI Modification Checklist
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UIModificationTab(
    state: AluminiumOSDesktopState,
    viewModel: AluminiumOSDesktopViewModel
) {
    val groupedItems = state.checklistItems.groupBy { it.category }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TerminalCard(title = "桌面 UI 改造清单 — Phone → Desktop 自适应", subtitle = "共 ${state.checklistItems.size} 项 | 已完成 ${state.checklistItems.count { it.isCompleted }} 项") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupedItems.forEach { (category, items) ->
                    Text(
                        text = "${category.labelCn} (${category.label})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TerminalBlue,
                        fontWeight = FontWeight.Bold
                    )
                    items.forEach { item ->
                        ChecklistItem(
                            item = item,
                            isExpanded = state.expandedChecklistItem == item.id,
                            onToggleCheck = { viewModel.processIntent(AluminiumOSDesktopIntent.ToggleChecklistItem(item.id)) },
                            onExpand = {
                                viewModel.processIntent(
                                    AluminiumOSDesktopIntent.ExpandChecklistItem(
                                        if (state.expandedChecklistItem == item.id) null else item.id
                                    )
                                )
                            }
                        )
                    }
                    HorizontalDivider(color = TerminalBorder, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(
    item: UIModificationItem,
    isExpanded: Boolean,
    onToggleCheck: () -> Unit,
    onExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand)
            .border(
                width = 1.dp,
                color = if (item.isCompleted) TerminalGreen.copy(alpha = 0.5f) else TerminalBorder,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) TerminalGreen.copy(alpha = 0.05f) else TerminalSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleCheck() },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = TerminalGreen,
                            uncheckedColor = TerminalGray
                        )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (item.isCompleted) TerminalGreen else TerminalWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.titleCn,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TerminalGray
                        )
                    }
                }
                RiskBadge(riskLevel = item.priority, compact = true)
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGray
                    )
                    Text(
                        text = item.descriptionCn,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGray.copy(alpha = 0.7f)
                    )
                    if (item.codeExample != null) {
                        Spacer(Modifier.height(4.dp))
                        CodeBlock(
                            title = "Code Example",
                            code = item.codeExample,
                            codeColor = TerminalBlue,
                            onCopy = {}
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2: Keyboard/Mouse Adaptation Guide
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KeyboardMouseTab(
    state: AluminiumOSDesktopState,
    viewModel: AluminiumOSDesktopViewModel
) {
    val filteredGuides = viewModel.getFilteredKeyboardGuides()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Framework selector
        // 框架选择器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrameworkChip(
                label = "Compose",
                isSelected = state.selectedFramework == "Compose",
                onClick = { viewModel.processIntent(AluminiumOSDesktopIntent.SelectFramework("Compose")) }
            )
            FrameworkChip(
                label = "View",
                isSelected = state.selectedFramework == "View",
                onClick = { viewModel.processIntent(AluminiumOSDesktopIntent.SelectFramework("View")) }
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredGuides) { guide ->
                KeyboardGuideCard(
                    guide = guide,
                    isExpanded = state.expandedKeyboardGuide == guide.id,
                    onToggle = {
                        viewModel.processIntent(
                            AluminiumOSDesktopIntent.ExpandKeyboardGuide(
                                if (state.expandedKeyboardGuide == guide.id) null else guide.id
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FrameworkChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) TerminalGreen else TerminalSurface)
            .border(1.dp, if (isSelected) TerminalGreen else TerminalBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (isSelected) TerminalBg else TerminalGray
        )
    }
}

@Composable
private fun KeyboardGuideCard(
    guide: KeyboardMouseGuide,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val categoryColor = when (guide.category) {
        KeyboardCategory.KEYBOARD_NAV -> TerminalBlue
        KeyboardCategory.SHORTCUT_KEYS -> TerminalGreen
        KeyboardCategory.RIGHT_CLICK_MENU -> TerminalOrange
        KeyboardCategory.DRAG_DROP -> TerminalYellow
        KeyboardCategory.FOCUS_MANAGEMENT -> TerminalGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guide.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TerminalWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = guide.titleCn,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGray
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = guide.category.label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = categoryColor
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Expand,
                        contentDescription = "Expand",
                        tint = TerminalGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = guide.description,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray
            )
            Text(
                text = guide.descriptionCn,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray.copy(alpha = 0.7f)
            )

            AnimatedVisibility(visible = isExpanded && guide.codeExample.isNotBlank()) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CodeBlock(
                        title = "Code Example",
                        code = guide.codeExample,
                        codeColor = TerminalBlue,
                        onCopy = {}
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 3: Gemini AI Integration
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GeminiIntegrationTab(
    state: AluminiumOSDesktopState,
    viewModel: AluminiumOSDesktopViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Integration type filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.geminiGuides.groupBy { it.integrationType }.forEach { (type, _) ->
                IntegrationTypeChip(type = type, onClick = {})
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.geminiGuides) { guide ->
                GeminiGuideCard(
                    guide = guide,
                    isExpanded = state.expandedGeminiGuide == guide.id,
                    onToggle = {
                        viewModel.processIntent(
                            AluminiumOSDesktopIntent.ExpandGeminiGuide(
                                if (state.expandedGeminiGuide == guide.id) null else guide.id
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun IntegrationTypeChip(
    type: GeminiIntegrationType,
    onClick: () -> Unit
) {
    val color = when (type) {
        GeminiIntegrationType.APP_FUNCTIONS -> TerminalGreen
        GeminiIntegrationType.AICORE -> TerminalBlue
        GeminiIntegrationType.GEMINI_SDK -> TerminalOrange
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = type.labelCn,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
private fun GeminiGuideCard(
    guide: GeminiIntegrationGuide,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val typeColor = when (guide.integrationType) {
        GeminiIntegrationType.APP_FUNCTIONS -> TerminalGreen
        GeminiIntegrationType.AICORE -> TerminalBlue
        GeminiIntegrationType.GEMINI_SDK -> TerminalOrange
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = guide.integrationType.label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = typeColor
                        )
                        Text(
                            text = guide.framework,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TerminalGray
                        )
                    }
                    Text(
                        text = guide.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = TerminalWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = guide.titleCn,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TerminalGray
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Expand,
                    contentDescription = "Expand",
                    tint = TerminalGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = guide.description,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray
            )
            Text(
                text = guide.descriptionCn,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGray.copy(alpha = 0.7f)
            )

            AnimatedVisibility(visible = isExpanded && guide.codeExample.isNotBlank()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    CodeBlock(
                        title = "Code",
                        code = guide.codeExample,
                        codeColor = TerminalGreen,
                        onCopy = {}
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 4: Play Store Optimization
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlayStoreOptimizationTab(
    state: AluminiumOSDesktopState,
    viewModel: AluminiumOSDesktopViewModel
) {
    val completedCount = state.playStoreGuides.count { it.isCompleted }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TerminalCard(title = "Play Store 桌面优化进度", subtitle = "$completedCount / ${state.playStoreGuides.size} completed") {
            LinearProgressIndicator(
                progress = { if (state.playStoreGuides.isEmpty()) 0f else completedCount.toFloat() / state.playStoreGuides.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TerminalGreen,
                trackColor = TerminalBorder
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.playStoreGuides) { guide ->
                PlayStoreGuideCard(
                    guide = guide,
                    isExpanded = state.expandedPlayStoreGuide == guide.id,
                    onToggleComplete = {
                        viewModel.processIntent(AluminiumOSDesktopIntent.TogglePlayStoreGuide(guide.id))
                    },
                    onToggleExpand = {
                        viewModel.processIntent(
                            AluminiumOSDesktopIntent.ExpandPlayStoreGuide(
                                if (state.expandedPlayStoreGuide == guide.id) null else guide.id
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayStoreGuideCard(
    guide: PlayStoreGuide,
    isExpanded: Boolean,
    onToggleComplete: () -> Unit,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = guide.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = TerminalGreen,
                            uncheckedColor = TerminalGray
                        )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = guide.title,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = if (guide.isCompleted) TerminalGreen else TerminalWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = guide.titleCn,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TerminalGray
                        )
                        Text(
                            text = "路径: ${guide.consolePath}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TerminalBlue
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Expand,
                    contentDescription = "Expand",
                    tint = TerminalGray,
                    modifier = Modifier.size(18.dp).clickable(onClick = onToggleExpand)
                )
            }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        guide.steps.forEachIndexed { index, step ->
                            Text(
                                text = step,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TerminalGreen
                            )
                        }
                        guide.stepsCn.forEachIndexed { index, step ->
                            Text(
                                text = step,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TerminalGray
                            )
                        }
                    }
                }
            }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared UI Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TerminalCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = TerminalGreen,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TerminalGray
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun RiskBadge(riskLevel: RiskLevel, compact: Boolean = false) {
    val color = when (riskLevel) {
        RiskLevel.P0 -> TerminalRed
        RiskLevel.P1 -> TerminalOrange
        RiskLevel.P2 -> TerminalYellow
        RiskLevel.P3 -> TerminalGray
        RiskLevel.PASS -> TerminalGreen
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = if (compact) 4.dp else 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (compact) riskLevel.name else riskLevel.label,
            fontFamily = FontFamily.Monospace,
            fontSize = if (compact) 9.sp else 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TerminalText(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = color
    )
}

@Composable
private fun terminalTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TerminalGreen,
    unfocusedTextColor = TerminalWhite,
    focusedBorderColor = TerminalGreen,
    unfocusedBorderColor = TerminalBorder,
    cursorColor = TerminalGreen,
    focusedLabelColor = TerminalGreen,
    unfocusedLabelColor = TerminalGray,
    focusedPlaceholderColor = TerminalGray,
    unfocusedPlaceholderColor = TerminalGray.copy(alpha = 0.5f)
)

