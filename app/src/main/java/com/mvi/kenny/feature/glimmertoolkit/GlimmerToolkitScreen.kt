package com.mvi.kenny.feature.glimmertoolkit

import com.mvi.kenny.base.TopBarConfig

// ================================================================
// GlimmerToolkitScreen — Jetpack Compose Glimmer AI 眼镜 UI 开发工具包主界面
// ================================================================
// Main Screen for Jetpack Compose Glimmer AI Glasses UI Development Toolkit.
//
// PRD-230: Jetpack Compose Glimmer AI 眼镜 UI 开发工具包
//
// 10-Tab Architecture:
//   Tab 0:  MigrationScannerScreen      — Compose Material → Glimmer 迁移扫描器
//   Tab 1:  ComponentMappingGuideScreen — 组件对照指南（Compose → Glimmer API）
//   Tab 2:  AdditiveDisplayBPMScreen    — 加法显示最佳实践（5大原则）
//   Tab 3:  ProjectedTemplateScreen   — Jetpack Projected 集成模板
//   Tab 4:  FocalLengthGuideScreen    — 1米焦距设计规范
//   Tab 5:  CompatibilityCheckerScreen — 兼容性自检工具
//   Tab 6:  KMPModuleGuideScreen      — KMP 模块化架构指南
//   Tab 7:  ProjectedDecisionTreeScreen— Projected 协同决策树
//   Tab 8:  CIComplianceScreen        — CI 合规检测工具
//   Tab 9:  IO2026PreviewScreen       — Google I/O 2026 预期预览
//
// Visual Style: Dark Terminal — deep dark background (#121212), white text,
//               purple-blue accent (#7C4DFF), risk-level color coding
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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

// ────────────────────────────────────────────────────────────────
// Theme Colors — Terminal Dark + Agency Purple-Blue
// ────────────────────────────────────────────────────────────────

private val TerminalBg = Color(0xFF121212)
private val TerminalSurface = Color(0xFF1E1E1E)
private val TerminalBorder = Color(0xFF2D2D2D)
private val AgencyPurple = Color(0xFF7C4DFF)
private val AgencyPurpleLight = Color(0xFFB388FF)
private val AgencyBlue = Color(0xFF448AFF)
private val TerminalGreen = Color(0xFF3FB950)
private val TerminalYellow = Color(0xFFFFD54F)
private val TerminalOrange = Color(0xFFFFB74D)
private val TerminalRed = Color(0xFFF85149)
private val TerminalGray = Color(0xFF8B949E)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFAAAAAA)

// ────────────────────────────────────────────────────────────────
// Tab definitions
// ────────────────────────────────────────────────────────────────

private data class GlimmerTab(
    val label: String,
    val labelCn: String,
    val icon: ImageVector
)

// ────────────────────────────────────────────────────────────────
// Main Screen Composable
// ────────────────────────────────────────────────────────────────

@Composable
fun GlimmerToolkitScreen(
    viewModel: GlimmerToolkitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val tabs = listOf(
        GlimmerTab("Scanner", "扫描器", Icons.Default.Scanner),
        GlimmerTab("Mapping", "组件对照", Icons.Default.Layers),
        GlimmerTab("BPM", "加法原则", Icons.Default.AutoAwesome),
        GlimmerTab("Projected", "投射模板", Icons.Default.Share),
        GlimmerTab("Focal", "焦距规范", Icons.Default.Search),
        GlimmerTab("Compat", "兼容性", Icons.Default.Security),
        GlimmerTab("KMP", "KMP模块", Icons.Default.Code),
        GlimmerTab("Decision", "决策树", Icons.Default.Psychology),
        GlimmerTab("CI", "CI合规", Icons.Default.CheckCircle),
        GlimmerTab("I/O 2026", "I/O 2026", Icons.AutoMirrored.Filled.ArrowForward),
    )

    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(TopBarConfig(title = "Glimmer AI 眼镜工具包 · ${tabs[state.selectedTab].labelCn}"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBg)
    ) {
        // ── Tab Row ──
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = TerminalBg,
            contentColor = TextWhite,
            edgePadding = 8.dp,
            divider = {},
            indicator = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.processIntent(GlimmerToolkitIntent.SelectTab(index)) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (state.selectedTab == index) AgencyPurple else TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab.labelCn,
                                fontSize = 12.sp,
                                color = if (state.selectedTab == index) AgencyPurple else TextMuted,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        // ── Tab Content ──
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.selectedTab) {
                0 -> MigrationScannerTab(state, viewModel, context)
                1 -> ComponentMappingTab(state, viewModel)
                2 -> AdditiveDisplayTab(state, viewModel)
                3 -> ProjectedTemplateTab(state, viewModel)
                4 -> FocalLengthGuideTab(state, viewModel)
                5 -> CompatibilityCheckerTab(state, viewModel)
                6 -> KMPModuleGuideTab(state, viewModel)
                7 -> ProjectedDecisionTreeTab(state, viewModel)
                8 -> CIComplianceTab(state, viewModel, context)
                9 -> IO2026PreviewTab(state, viewModel)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 0: Migration Scanner
// ════════════════════════════════════════════════════════════════

@Composable
private fun MigrationScannerTab(
    state: GlimmerToolkitState,
    viewModel: GlimmerToolkitViewModel,
    context: Context
) {
    val scanState = state.scanState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Compose → Glimmer Migration Scanner",
                titleCn = "Compose Material → Glimmer 迁移扫描器",
                icon = Icons.Default.Scanner
            )
        }

        item {
            TerminalCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "📁 Project Source Path / 项目源码路径",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = state.projectPath,
                        onValueChange = { viewModel.processIntent(GlimmerToolkitIntent.UpdateProjectPath(it)) },
                        placeholder = {
                            Text(
                                "e.g., /Users/kenny/project/app/src/main/java",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = AgencyPurple,
                            unfocusedBorderColor = TerminalBorder,
                            cursorColor = AgencyPurple
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.processIntent(GlimmerToolkitIntent.StartScan) },
                            enabled = !scanState.isScanning && state.projectPath.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = AgencyPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (scanState.isScanning) "Scanning..." else "Start Scan / 开始扫描",
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.processIntent(GlimmerToolkitIntent.ClearScanResults) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalRed),
                            enabled = !scanState.isScanning
                        ) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (scanState.isScanning) {
            item {
                TerminalCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AgencyPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${scanState.phase.name.replace("_", " ")} / ${scanState.progress}%",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        LinearProgressIndicator(
                            progress = { scanState.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = AgencyPurple,
                            trackColor = TerminalBorder
                        )
                    }
                }
            }
        }

        if (scanState.results.isNotEmpty()) {
            item {
                val high = scanState.results.count {
                    it.severity == GlimmerSeverity.HIGH || it.severity == GlimmerSeverity.BLOCKER
                }
                val medium = scanState.results.count { it.severity == GlimmerSeverity.MEDIUM }
                val low = scanState.results.count { it.severity == GlimmerSeverity.LOW }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip("🔴 $high", TerminalRed, Modifier.weight(1f))
                    StatChip("🟡 $medium", TerminalYellow, Modifier.weight(1f))
                    StatChip("🟢 $low", TerminalGreen, Modifier.weight(1f))
                    StatChip("📄 ${scanState.scannedFilesCount}", AgencyPurple, Modifier.weight(1f))
                }
            }

            items(scanState.results) { result ->
                ScanResultItem(
                    result = result,
                    isExpanded = state.expandedScanResult == result.id,
                    onToggle = {
                        viewModel.processIntent(
                            GlimmerToolkitIntent.ExpandScanResult(
                                if (state.expandedScanResult == result.id) null else result.id
                            )
                        )
                    },
                    onCopy = { code ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                        Toast.makeText(context, "Copied! / 已复制!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        if (scanState.results.isEmpty() && scanState.phase == GlimmerScanPhase.IDLE) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Scanner,
                    message = "Enter project path and tap 'Start Scan' to detect migration candidates",
                    messageCn = "输入项目路径并点击「开始扫描」以检测迁移候选项"
                )
            }
        }
    }
}

@Composable
private fun ScanResultItem(
    result: GlimmerScanResult,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: (String) -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.composeApi,
                        color = TerminalRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "→ ${result.glimmerApi}",
                        color = TerminalGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "[${result.file.split("/").lastOrNull()}:${result.line}]",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text(result.descriptionCn, color = AgencyPurpleLight, fontSize = 12.sp)
                    Text("Before:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(result.codeExample ?: "", TerminalRed.copy(alpha = 0.8f))
                    Text("After:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(result.glimmerExample ?: "", TerminalGreen.copy(alpha = 0.8f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onCopy(result.codeExample ?: "") },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalBorder),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Before", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onCopy(result.glimmerExample ?: "") },
                            colors = ButtonDefaults.buttonColors(containerColor = AgencyPurple),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy After", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 1: Component Mapping Guide
// ════════════════════════════════════════════════════════════════

@Composable
private fun ComponentMappingTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    val categories = state.componentMappings.map { it.category to it.categoryCn }.distinct()
    val filtered = state.selectedMappingCategory?.let { cat ->
        state.componentMappings.filter { it.category == cat }
    } ?: state.componentMappings

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Compose → Glimmer API Mapping",
                titleCn = "组件对照指南：Compose → Glimmer API",
                icon = Icons.Default.Layers
            )
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = state.selectedMappingCategory == null,
                    onClick = { viewModel.processIntent(GlimmerToolkitIntent.SelectMappingCategory(null)) },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AgencyPurple,
                        selectedLabelColor = TextWhite
                    )
                )
                categories.forEach { (cat, catCn) ->
                    FilterChip(
                        selected = state.selectedMappingCategory == cat,
                        onClick = { viewModel.processIntent(GlimmerToolkitIntent.SelectMappingCategory(cat)) },
                        label = { Text("$cat ($catCn)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgencyPurple,
                            selectedLabelColor = TextWhite
                        )
                    )
                }
            }
        }

        items(filtered) { mapping ->
            MappingItem(
                mapping = mapping,
                isExpanded = state.expandedMapping == mapping.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandMapping(
                            if (state.expandedMapping == mapping.id) null else mapping.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun MappingItem(
    mapping: ComponentMapping,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${mapping.category} / ${mapping.categoryCn}",
                        color = AgencyPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Compose: ${mapping.composeApi}",
                        color = TerminalRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Glimmer: ${mapping.glimmerApi}",
                        color = TerminalGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text(mapping.descriptionCn, color = TextWhite, fontSize = 12.sp)
                    if (mapping.notes != null) {
                        Text("📝 ${mapping.notes}", color = TerminalYellow, fontSize = 11.sp)
                    }
                    Text("Compose:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(mapping.codeExample, TerminalRed.copy(alpha = 0.8f))
                    Text("Glimmer:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(mapping.glimmerExample, TerminalGreen.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 2: Additive Display Best Practices
// ════════════════════════════════════════════════════════════════

@Composable
private fun AdditiveDisplayTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Additive Display Best Practices",
                titleCn = "加法显示最佳实践（5大原则）",
                icon = Icons.Default.AutoAwesome
            )
        }

        item {
            Text(
                text = "OLED 加法显示：黑色=透明，白色=发光。与 LCD 背光模型完全相反。",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(state.additivePrinciples) { principle ->
            PrincipleItem(
                principle = principle,
                isExpanded = state.expandedPrinciple == principle.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandPrinciple(
                            if (state.expandedPrinciple == principle.id) null else principle.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun PrincipleItem(
    principle: AdditiveDisplayPrinciple,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(principle.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${principle.title} / ${principle.titleCn}",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = principle.principleCn,
                            color = AgencyPurpleLight,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text(principle.descriptionCn, color = TextWhite, fontSize = 12.sp)
                    Text("Code / 代码:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(principle.codeExampleCn, AgencyPurpleLight)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 3: Projected Template
// ════════════════════════════════════════════════════════════════

@Composable
private fun ProjectedTemplateTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Jetpack Projected Integration Template",
                titleCn = "Jetpack Projected 集成模板",
                icon = Icons.Default.Share
            )
        }

        item {
            Text(
                text = "Projected API 让主机 App 能够将 Glimmer UI 投射到连接的 AI 眼镜。",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(state.projectedSteps) { step ->
            StepItem(
                step = step,
                isExpanded = state.expandedStep == step.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandStep(
                            if (state.expandedStep == step.id) null else step.id
                        )
                    )
                },
                onToggleComplete = {
                    viewModel.processIntent(GlimmerToolkitIntent.ToggleProjectedStep(step.id))
                }
            )
        }
    }
}

@Composable
private fun StepItem(
    step: ProjectedStep,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onToggleComplete: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (step.isCompleted) TerminalGreen else AgencyPurple)
                            .clickable { onToggleComplete() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (step.isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                "${step.stepNumber}",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = step.titleCn,
                            color = if (step.isCompleted) TerminalGreen else TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(step.descriptionCn, color = TextMuted, fontSize = 11.sp)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    CodeBlock(step.codeSnippet, AgencyPurpleLight)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 4: Focal Length Guide
// ════════════════════════════════════════════════════════════════

@Composable
private fun FocalLengthGuideTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "1-Meter Focal Length Design Specs",
                titleCn = "1米焦距设计规范",
                icon = Icons.Default.Search
            )
        }

        item {
            Text(
                text = "眼镜显示焦距约 1 米。所有文本和 UI 元素必须在此距离下可读。",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            TerminalCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Element",
                        color = AgencyPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Min",
                        color = AgencyPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        "Rec.",
                        color = AgencyPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        "Spacing",
                        color = AgencyPurpleLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }

        items(state.focalLengthSpecs) { spec ->
            FocalSpecItem(
                spec = spec,
                isExpanded = state.expandedFocalSpec == spec.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandFocalSpec(
                            if (state.expandedFocalSpec == spec.id) null else spec.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun FocalSpecItem(
    spec: FocalLengthSpec,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spec.elementTypeCn,
                    color = TextWhite,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = spec.minSize,
                    color = TerminalRed,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = spec.recommendedSize,
                    color = TerminalGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = spec.spacing,
                    color = TerminalYellow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.7f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text("Line Height / 行高: ${spec.lineHeight}", color = TextMuted, fontSize = 11.sp)
                    Text("Example / 示例:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(spec.example, AgencyPurpleLight)
                    Text("Notes / 备注: ${spec.notes}", color = TerminalYellow, fontSize = 11.sp)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 5: Compatibility Checker
// ════════════════════════════════════════════════════════════════

@Composable
private fun CompatibilityCheckerTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    val filteredIssues = state.selectedRiskFilter?.let { risk ->
        state.compatibilityIssues.filter { it.riskLevel == risk }
    } ?: state.compatibilityIssues

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Compatibility Self-Check",
                titleCn = "兼容性自检工具",
                icon = Icons.Default.Security
            )
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = state.selectedRiskFilter == null,
                    onClick = { viewModel.processIntent(GlimmerToolkitIntent.FilterByRisk(null)) },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AgencyPurple,
                        selectedLabelColor = TextWhite
                    )
                )
                CompatibilityRiskLevel.entries.forEach { risk ->
                    FilterChip(
                        selected = state.selectedRiskFilter == risk,
                        onClick = { viewModel.processIntent(GlimmerToolkitIntent.FilterByRisk(risk)) },
                        label = { Text("${risk.labelCn}", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = risk.color,
                            selectedLabelColor = if (risk == CompatibilityRiskLevel.PASS) Color.Black else TextWhite
                        )
                    )
                }
            }
        }

        items(filteredIssues) { issue ->
            CompatibilityIssueItem(
                issue = issue,
                isExpanded = state.expandedIssue == issue.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandIssue(
                            if (state.expandedIssue == issue.id) null else issue.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CompatibilityIssueItem(
    issue: CompatibilityIssue,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(issue.riskLevel.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = issue.titleCn,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = issue.descriptionCn,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "影响版本: ${issue.affectedVersions}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text("修复建议:", color = AgencyPurpleLight, fontSize = 12.sp)
                    Text(issue.suggestionCn, color = TextWhite, fontSize = 11.sp)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 6: KMP Module Guide
// ════════════════════════════════════════════════════════════════

@Composable
private fun KMPModuleGuideTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "KMP Modular Architecture Guide",
                titleCn = "KMP 模块化架构指南（expect/actual 模式）",
                icon = Icons.Default.Code
            )
        }

        item {
            Text(
                text = "Kotlin Multiplatform 允许在 Android、手表和眼镜之间共享 Glimmer UI 代码。",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(state.kmpModuleGuides) { guide ->
            KMPGuideItem(
                guide = guide,
                isExpanded = state.expandedKMPGuide == guide.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandKMPGuide(
                            if (state.expandedKMPGuide == guide.id) null else guide.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun KMPGuideItem(
    guide: KMPModuleGuide,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guide.moduleName,
                        color = AgencyPurpleLight,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${guide.moduleType.labelCn} / ${guide.moduleType.label}",
                        color = TerminalGreen,
                        fontSize = 11.sp
                    )
                    Text(
                        text = guide.descriptionCn,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text("expect:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(guide.expectCode, AgencyPurpleLight)
                    Text("androidActual:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(guide.androidActual, TerminalGreen)
                    if (guide.wearActual != null) {
                        Text("wearActual:", color = TextMuted, fontSize = 11.sp)
                        CodeBlock(guide.wearActual!!, TerminalYellow)
                    }
                    if (guide.glimmerActual != null) {
                        Text("glimmerActual:", color = TextMuted, fontSize = 11.sp)
                        CodeBlock(guide.glimmerActual!!, TerminalOrange)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 7: Projected Decision Tree
// ════════════════════════════════════════════════════════════════

@Composable
private fun ProjectedDecisionTreeTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    val currentNode = state.decisionNodes.find { it.id == (state.currentNodeId ?: "root") }
        ?: state.decisionNodes.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Projected Collaboration Decision Tree",
                titleCn = "Projected 协同决策树",
                icon = Icons.Default.Psychology
            )
        }

        if (currentNode != null) {
            item {
                DecisionNodeCard(
                    node = currentNode,
                    onSelectOption = { nextId ->
                        viewModel.processIntent(
                            GlimmerToolkitIntent.SelectDecisionNode(nextId ?: currentNode.id)
                        )
                    }
                )
            }

            item {
                Button(
                    onClick = { viewModel.processIntent(GlimmerToolkitIntent.SelectDecisionNode("root")) },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restart / 重新开始", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DecisionNodeCard(
    node: DecisionNode,
    onSelectOption: (String?) -> Unit
) {
    TerminalCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = node.questionCn,
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = node.question,
                color = TextMuted,
                fontSize = 12.sp
            )

            HorizontalDivider(color = TerminalBorder)

            node.options.forEach { option ->
                Button(
                    onClick = { onSelectOption(option.nextNodeId) },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AgencyPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${option.labelCn} / ${option.label}",
                        color = TextWhite,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = TerminalBorder)

            Text("Recommendation / 建议: ${node.recommendationCn}", color = TerminalGreen, fontSize = 12.sp)
            Text("Scenario / 场景: ${node.useCaseCn}", color = AgencyPurpleLight, fontSize = 11.sp)
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 8: CI Compliance
// ════════════════════════════════════════════════════════════════

@Composable
private fun CIComplianceTab(
    state: GlimmerToolkitState,
    viewModel: GlimmerToolkitViewModel,
    context: Context
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "CI Compliance Detection Tool",
                titleCn = "CI 合规检测工具（Gradle 插件形式）",
                icon = Icons.Default.CheckCircle
            )
        }

        item {
            Text(
                text = "在 CI 流水线中检测 Glimmer 合规性问题。集成到 build.gradle.kts。",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(state.ciComplianceRules) { rule ->
            CIRuleItem(
                rule = rule,
                isExpanded = state.expandedCIRule == rule.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandCIRule(
                            if (state.expandedCIRule == rule.id) null else rule.id
                        )
                    )
                },
                onCopy = { text ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("ci_rule", text))
                    Toast.makeText(context, "Copied! / 已复制!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun CIRuleItem(
    rule: CIComplianceRule,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: (String) -> Unit
) {
    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(rule.severity.color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${rule.ruleId}]",
                            color = AgencyPurpleLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = rule.titleCn,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rule.descriptionCn,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text("Check Command / 检查命令:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(rule.checkCommand, TerminalGreen)
                    Text("Fix Command / 修复命令:", color = TextMuted, fontSize = 11.sp)
                    CodeBlock(rule.fixCommand, TerminalYellow)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onCopy(rule.checkCommand) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalBorder),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Check", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onCopy(rule.fixCommand) },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalBorder),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Fix", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Tab 9: I/O 2026 Preview
// ════════════════════════════════════════════════════════════════

@Composable
private fun IO2026PreviewTab(state: GlimmerToolkitState, viewModel: GlimmerToolkitViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Google I/O 2026 Expected Preview",
                titleCn = "Google I/O 2026 预期预览",
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }

        item {
            CountdownCard(days = state.io2026Countdown)
        }

        item {
            Text(
                text = "以下为基于当前趋势的 I/O 2026 预期内容。",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(state.io2026Sessions) { session ->
            IOSessionItem(
                session = session,
                isExpanded = state.expandedIOSession == session.id,
                onToggle = {
                    viewModel.processIntent(
                        GlimmerToolkitIntent.ExpandIOSession(
                            if (state.expandedIOSession == session.id) null else session.id
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun CountdownCard(days: Long) {
    TerminalCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$days",
                    color = AgencyPurple,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("DAYS", color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("May 12-14, 2026", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Mountain View, CA", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun IOSessionItem(
    session: IO2026Session,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val likelihoodColor = when (session.likelihood) {
        IOLikelihood.CONFIRMED -> TerminalGreen
        IOLikelihood.HIGHLY_LIKELY -> TerminalOrange
        IOLikelihood.LIKELY -> TerminalYellow
        IOLikelihood.SPECULATIVE -> TerminalGray
    }

    TerminalCard(onClick = onToggle) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.likelihood.labelCn,
                            color = likelihoodColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = session.categoryCn,
                            color = AgencyBlue,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = session.titleCn,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = session.expectedDate,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = TerminalBorder)
                    Text(session.descriptionCn, color = TextWhite, fontSize = 12.sp)
                    if (session.relatedPRD != null) {
                        Text("相关 PRD: ${session.relatedPRD}", color = AgencyPurpleLight, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Shared UI Components
// ════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String, titleCn: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AgencyPurple,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titleCn,
                color = AgencyPurpleLight,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun TerminalCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalSurface)
            .border(1.dp, TerminalBorder, RoundedCornerShape(8.dp))
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
private fun CodeBlock(code: String, highlightColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(TerminalBg)
            .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = code,
            color = highlightColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StatChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyStateCard(icon: ImageVector, message: String, messageCn: String) {
    TerminalCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                message,
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                messageCn,
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
