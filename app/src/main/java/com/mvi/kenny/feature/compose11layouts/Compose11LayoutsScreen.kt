package com.mvi.kenny.feature.compose11layouts

// ============================================================
// Compose11LayoutsScreen — PRD-161 主界面
// Compose 1.11 Layout & Style APIs 开发工具包
// ============================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.compose11layouts.ComplianceSeverity
import com.mvi.kenny.feature.compose11layouts.ComplianceResult
import com.mvi.kenny.feature.compose11layouts.Compose11LayoutsEffect
import com.mvi.kenny.feature.compose11layouts.Compose11LayoutsIntent
import com.mvi.kenny.feature.compose11layouts.Compose11LayoutsState
import com.mvi.kenny.feature.compose11layouts.DependencyNode
import com.mvi.kenny.feature.compose11layouts.FlexBoxCategory
import com.mvi.kenny.feature.compose11layouts.FlexBoxTemplate
import com.mvi.kenny.feature.compose11layouts.GridCategory
import com.mvi.kenny.feature.compose11layouts.GridTemplate
import com.mvi.kenny.feature.compose11layouts.LayoutSection
import com.mvi.kenny.feature.compose11layouts.LayoutTab
import com.mvi.kenny.feature.compose11layouts.MediaQueryTemplate
import com.mvi.kenny.feature.compose11layouts.ReportFormat
import com.mvi.kenny.feature.compose11layouts.ScreenMode
import com.mvi.kenny.feature.compose11layouts.StyleGuideSection
import com.mvi.kenny.feature.compose11layouts.TemplateParameter

// ============================================================
// Compose11LayoutsScreen — PRD-161 主界面
// Compose 1.11 Layout & Style APIs 开发工具包
// ============================================================
/**
 * 主入口 Screen，接收 State + Intent，通过 MVI 模式渲染 UI。
 *
 * 页面结构：
 * —————————————————————————————————————————————————————
 * 顶部：Banner + BOM 版本选择器
 * 主体：分 Section 渲染不同功能区
 *   - HOME: 4 个工具卡片入口
 *   - GRID: Tab 视图（指南 / 模板库）+ Grid 模板预览
 *   - FLEXBOX: Tab 视图（指南 / 模板库）+ FlexBox 模板预览
 *   - STYLE: 可折叠章节文档
 *   - MEDIA_QUERY: 响应式模板 + 屏幕形态切换器
 *   - COMPLIANCE: CI 扫描工具 + 报告导出
 *   - VERIFICATION: 升级验证工具
 *
 * @param state 页面状态
 * @param onIntent 发送用户意图
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Compose11LayoutsScreen(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit,
) {
    // Track scroll position for scroll-to-top on section change
    val scrollState = androidx.compose.foundation.rememberScrollState()

    // Section change → reset scroll
    androidx.compose.runtime.LaunchedEffect(state.selectedSection) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ============================================================
        // 顶部 Banner + BOM 版本选择器
        // ============================================================
        ComposeApiTopBar(
            currentBom = state.currentComposeBom,
            onBomChange = { version ->
                onIntent(Compose11LayoutsIntent.ChangeBomVersion(version))
            }
        )

        // ============================================================
        // Section Navigation — 分区切换 Tab
        // ============================================================
        SectionNavigationTabs(
            selectedSection = state.selectedSection,
            onSectionSelect = { section ->
                onIntent(Compose11LayoutsIntent.SelectSection(section))
            }
        )

        // ============================================================
        // 主体内容区 — 按 Section 渲染
        // ============================================================
        when (state.selectedSection) {
            LayoutSection.HOME -> HomeSectionContent(onIntent = onIntent)
            LayoutSection.GRID -> GridSectionContent(state, onIntent)
            LayoutSection.FLEXBOX -> FlexBoxSectionContent(state, onIntent)
            LayoutSection.STYLE -> StyleGuideContent(state, onIntent)
            LayoutSection.MEDIA_QUERY -> MediaQuerySectionContent(state, onIntent)
            LayoutSection.COMPLIANCE -> ComplianceSectionContent(state, onIntent)
            LayoutSection.VERIFICATION -> VerificationSectionContent(state, onIntent)
        }
    }

    // ============================================================
    // Preview Dialog — 全屏模板预览
    // ============================================================
    if (state.isPreviewDialogOpen && state.selectedTemplate != null) {
        PreviewDialog(
            template = state.selectedTemplate,
            parameters = state.previewParameters,
            isDarkMode = state.isDarkMode,
            onParameterChange = { key, value ->
                onIntent(Compose11LayoutsIntent.UpdatePreviewParameter(key, value))
            },
            onToggleDarkMode = { onIntent(Compose11LayoutsIntent.ToggleDarkMode) },
            onDismiss = { onIntent(Compose11LayoutsIntent.ClosePreviewDialog) }
        )
    }
}

// ============================================================
// TopBar — 顶部 Banner + BOM 版本选择器
// ============================================================
@Composable
private fun ComposeApiTopBar(
    currentBom: String,
    onBomChange: (String) -> Unit
) {
    val bomOptions = listOf("2024.01.01", "2024.02.00", "2024.03.00")
    var bomExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Surface(
        color = Color(0xFF8B5CF6),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Banner text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🆕 Compose 1.11 Layout APIs 重磅发布",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Grid / FlexBox / Style API / MediaQuery × 折叠屏 — 工具层空白，等你来填！",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // BOM Version selector
                Box {
                    AssistChip(
                        onClick = { bomExpanded = true },
                        label = {
                            Text(
                                text = "BOM $currentBom",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.Transparent
                        )
                    )

                    DropdownMenu(
                        expanded = bomExpanded,
                        onDismissRequest = { bomExpanded = false }
                    ) {
                        bomOptions.forEach { version ->
                            DropdownMenuItem(
                                text = { Text(version) },
                                onClick = {
                                    onBomChange(version)
                                    bomExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Section Navigation Tabs — 分区切换导航
// ============================================================
@Composable
private fun SectionNavigationTabs(
    selectedSection: LayoutSection,
    onSectionSelect: (LayoutSection) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = LayoutSection.entries.indexOf(selectedSection),
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        LayoutSection.entries.forEach { section ->
            Tab(
                selected = selectedSection == section,
                onClick = { onSectionSelect(section) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(section.icon, style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = section.titleCn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedSection == section) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            )
        }
    }
}

// ============================================================
// HOME Section — 首页四大工具卡片
// ============================================================
@Composable
private fun HomeSectionContent(onIntent: (Compose11LayoutsIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Text(
            text = "📐 布局 API 工具箱",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 4 tool cards in 2x2 grid using LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(listOf(
                "grid" to LayoutSection.GRID,
                "flexbox" to LayoutSection.FLEXBOX,
                "style" to LayoutSection.STYLE,
                "media" to LayoutSection.MEDIA_QUERY
            )) { (type, section) ->
                when (type) {
                    "grid" -> ToolCard(
                        icon = "📐",
                        title = "Grid API",
                        subtitle = "指南与模板库",
                        description = "仪表盘 / 日历 / 棋盘 / 画廊模板",
                        color = Color(0xFF8B5CF6),
                        onClick = { onIntent(Compose11LayoutsIntent.SelectSection(section)) }
                    )
                    "flexbox" -> ToolCard(
                        icon = "📦",
                        title = "FlexBox API",
                        subtitle = "指南与模板库",
                        description = "导航栏 / 侧边栏 / 流式布局模板",
                        color = Color(0xFF06B6D4),
                        onClick = { onIntent(Compose11LayoutsIntent.SelectSection(section)) }
                    )
                    "style" -> ToolCard(
                        icon = "🎨",
                        title = "Style API",
                        subtitle = "开发指南",
                        description = "性能考量 / 动画过渡 / 最佳实践",
                        color = Color(0xFFF59E0B),
                        onClick = { onIntent(Compose11LayoutsIntent.SelectSection(section)) }
                    )
                    "media" -> ToolCard(
                        icon = "📱",
                        title = "MediaQuery × 折叠屏",
                        subtitle = "响应式模板",
                        description = "折叠屏 tabletop / 平板 / 手机自适应",
                        color = Color(0xFF22C55E),
                        onClick = { onIntent(Compose11LayoutsIntent.SelectSection(section)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // CI + Verification tools
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                icon = "🔍",
                title = "CI 合规检测",
                subtitle = "扫描项目合规性",
                description = "Grid/FlexBox/Style API 使用规范",
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
                onClick = { onIntent(Compose11LayoutsIntent.SelectSection(LayoutSection.COMPLIANCE)) }
            )
            ToolCard(
                icon = "✅",
                title = "升级验证",
                subtitle = "BOM 版本检测",
                description = "依赖树分析 + 升级建议",
                color = Color(0xFF22C55E),
                modifier = Modifier.weight(1f),
                onClick = { onIntent(Compose11LayoutsIntent.SelectSection(LayoutSection.VERIFICATION)) }
            )
        }
    }
}

// ============================================================
// ToolCard — 工具入口卡片
// ============================================================
@Composable
private fun ToolCard(
    icon: String,
    title: String,
    subtitle: String,
    description: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, style = MaterialTheme.typography.headlineSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// GRID Section — Grid API 指南与模板库
// ============================================================
@Composable
private fun GridSectionContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab: Guide / Templates
        TabRow(
            selectedTabIndex = if (state.selectedTab == LayoutTab.GUIDE) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = state.selectedTab == LayoutTab.GUIDE,
                onClick = { onIntent(Compose11LayoutsIntent.SelectTab(LayoutTab.GUIDE)) },
                text = { Text("📖 指南") }
            )
            Tab(
                selected = state.selectedTab == LayoutTab.TEMPLATES,
                onClick = { onIntent(Compose11LayoutsIntent.SelectTab(LayoutTab.TEMPLATES)) },
                text = { Text("📋 模板库") }
            )
        }

        when (state.selectedTab) {
            LayoutTab.GUIDE -> GridGuideContent()
            LayoutTab.TEMPLATES -> GridTemplatesContent(state, onIntent)
        }
    }
}

@Composable
private fun GridGuideContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text(
            text = "📐 Grid API 决策树",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Decision tree nodes
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("✅ 使用 Grid API 的场景", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "• 静态二维布局（不滚动）",
                    "• 需要跨行/跨列的复杂网格",
                    "• 仪表盘、日历、棋盘等非列表布局",
                    "• 性能敏感场景（Grid 比 LazyVerticalGrid 更轻量）"
                ).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚠️ 使用 LazyVerticalGrid 的场景", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "• 长列表场景（大量数据）",
                    "• 需要懒加载的网格布局",
                    "• 列表 + 网格混合（如 Pinterest 瀑布流）"
                ).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("❌ 使用 Box/Column/Row 自定义布局", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "• 简单的一维排列（直接用 Column/Row）",
                    "• 绝对定位场景（使用 Box + offset）"
                ).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Code example
        CodeBlockView(
            code = """
// Grid API 基本用法
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BasicGrid() {
    Grid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(GridItemSpan(2)) { /* 跨2列 */ }
        item { /* 标准单元格 */ }
    }
}
            """.trimIndent()
        )
    }
}

@Composable
private fun GridTemplatesContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Category filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GridCategory.entries.forEach { category ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(category.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Template cards
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.gridTemplates) { template ->
                TemplateCard(
                    title = "${template.nameCn} / ${template.nameEn}",
                    description = template.description,
                    category = template.category.label,
                    code = template.code,
                    onPreview = { onIntent(Compose11LayoutsIntent.SelectTemplate(template)) },
                    onCopy = { onIntent(Compose11LayoutsIntent.CopyTemplateCode(template.id)) }
                )
            }
        }
    }
}

// ============================================================
// FLEXBOX Section — FlexBox API 指南与模板库
// ============================================================
@Composable
private fun FlexBoxSectionContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (state.selectedTab == LayoutTab.GUIDE) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = state.selectedTab == LayoutTab.GUIDE,
                onClick = { onIntent(Compose11LayoutsIntent.SelectTab(LayoutTab.GUIDE)) },
                text = { Text("📖 指南") }
            )
            Tab(
                selected = state.selectedTab == LayoutTab.TEMPLATES,
                onClick = { onIntent(Compose11LayoutsIntent.SelectTab(LayoutTab.TEMPLATES)) },
                text = { Text("📋 模板库") }
            )
        }

        when (state.selectedTab) {
            LayoutTab.GUIDE -> FlexBoxGuideContent()
            LayoutTab.TEMPLATES -> FlexBoxTemplatesContent(state, onIntent)
        }
    }
}

@Composable
private fun FlexBoxGuideContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text(
            text = "📦 FlexBox API vs Web Flexbox",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF06B6D4).copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Compose FlexBox 与 Web Flexbox 对照", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val mappings = listOf(
                    ("flexDirection → Row/Column arrangement") to "flex-direction",
                    ("Arrangement.SpaceBetween → justify-content: space-between") to "justify-content: space-between",
                    ("Arrangement.spacedBy → gap") to "gap",
                    ("Alignment.CenterVertically → align-items: center") to "align-items: center",
                    ("weight → flex-grow") to "flex-grow"
                )
                mappings.forEach { (compose, css) ->
                    Row {
                        Text("• ", style = MaterialTheme.typography.bodySmall)
                        Text(compose, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(" = $css", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CodeBlockView(
            code = """
// FlexBox API 基本用法
@Composable
fun FlexBoxRow() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Acts like flex: 1
        Box(modifier = Modifier.weight(1f)) { Title() }
        // Acts like flex: 0 0 auto
        Box { ActionButton() }
    }
}
            """.trimIndent()
        )
    }
}

@Composable
private fun FlexBoxTemplatesContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FlexBoxCategory.entries.forEach { category ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(category.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.flexboxTemplates) { template ->
                TemplateCard(
                    title = "${template.nameCn} / ${template.nameEn}",
                    description = template.description,
                    category = template.category.label,
                    code = template.code,
                    onPreview = { onIntent(Compose11LayoutsIntent.SelectTemplate(template)) },
                    onCopy = { onIntent(Compose11LayoutsIntent.CopyTemplateCode(template.id)) }
                )
            }
        }
    }
}

// ============================================================
// STYLE API Section — Style API 开发指南
// ============================================================
@Composable
private fun StyleGuideContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🎨 Style API 开发指南",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        state.styleGuideSections.forEach { section ->
            StyleGuideSectionCard(
                section = section,
                isExpanded = state.expandedStyleSections.contains(section.id),
                onToggle = { onIntent(Compose11LayoutsIntent.ToggleStyleSection(section.id)) }
            )
        }
    }
}

@Composable
private fun StyleGuideSectionCard(
    section: StyleGuideSection,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = section.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (section.codeExample.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CodeBlockView(code = section.codeExample)
                }
            }
        }
    }
}

// ============================================================
// MEDIA QUERY Section — MediaQuery × 折叠屏模板
// ============================================================
@Composable
private fun MediaQuerySectionContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen mode switcher
        ScreenModeSwitcher(
            selectedMode = state.selectedScreenMode,
            onModeSelect = { mode -> onIntent(Compose11LayoutsIntent.SelectScreenMode(mode)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Template list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.mediaQueryTemplates.filter { it.screenMode == state.selectedScreenMode }) { template ->
                TemplateCard(
                    title = "${template.nameCn} / ${template.nameEn}",
                    description = template.description,
                    category = template.screenMode.label,
                    code = template.code,
                    onPreview = { onIntent(Compose11LayoutsIntent.SelectTemplate(template)) },
                    onCopy = { onIntent(Compose11LayoutsIntent.CopyTemplateCode(template.id)) }
                )
            }

            // If no templates for selected mode, show all
            if (state.mediaQueryTemplates.none { it.screenMode == state.selectedScreenMode }) {
                items(state.mediaQueryTemplates) { template ->
                    TemplateCard(
                        title = "${template.nameCn} / ${template.nameEn}",
                        description = template.description,
                        category = template.screenMode.label,
                        code = template.code,
                        onPreview = { onIntent(Compose11LayoutsIntent.SelectTemplate(template)) },
                        onCopy = { onIntent(Compose11LayoutsIntent.CopyTemplateCode(template.id)) }
                    )
                }
            }
        }
    }
}

// ============================================================
// Screen Mode Switcher — 屏幕形态切换器
// ============================================================
@Composable
private fun ScreenModeSwitcher(
    selectedMode: ScreenMode,
    onModeSelect: (ScreenMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScreenMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelect(mode) },
                label = {
                    Text(
                        text = "${mode.icon} ${mode.label}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============================================================
// COMPLIANCE Section — CI 合规检测工具
// ============================================================
@Composable
private fun ComplianceSectionContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🔍 CI 合规检测工具",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Project path input + Scan button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.projectPathInput,
                onValueChange = { onIntent(Compose11LayoutsIntent.UpdateProjectPath(it)) },
                label = { Text("项目路径 / Project Path") },
                placeholder = { Text("例如: /Users/kenny/workspace/MyProject") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !state.scanInProgress
            )

            Button(
                onClick = { onIntent(Compose11LayoutsIntent.StartComplianceScan(state.projectPathInput)) },
                enabled = !state.scanInProgress && state.projectPathInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                if (state.scanInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("扫描")
                }
            }
        }

        // Scan progress
        if (state.scanInProgress) {
            ScanProgressView(
                progress = state.scanProgress,
                currentFile = state.currentScanFile
            )
        }

        // Compliance results
        if (state.complianceResults.isNotEmpty()) {
            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "扫描结果：${state.complianceResults.size} 项",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "错误 ${state.complianceResults.count { it.severity == ComplianceSeverity.ERROR }} | " +
                                "警告 ${state.complianceResults.count { it.severity == ComplianceSeverity.WARN }} | " +
                                "信息 ${state.complianceResults.count { it.severity == ComplianceSeverity.INFO }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = { onIntent(Compose11LayoutsIntent.ExportComplianceReport(ReportFormat.JSON)) },
                        label = { Text("JSON") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onIntent(Compose11LayoutsIntent.ExportComplianceReport(ReportFormat.MARKDOWN)) },
                        label = { Text("MD") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Violation items
            state.complianceResults.forEach { result ->
                ViolationItemCard(result = result)
            }
        }
    }
}

// ============================================================
// Scan Progress View — 扫描进度视图
// ============================================================
@Composable
private fun ScanProgressView(
    progress: Float,
    currentFile: String?
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "正在扫描...",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFF8B5CF6),
                trackColor = Color(0xFF8B5CF6).copy(alpha = 0.15f)
            )
            currentFile?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
// Violation Item Card — 违规项卡片
// ============================================================
@Composable
private fun ViolationItemCard(result: ComplianceResult) {
    val severityColor = Color(result.severity.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Severity badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severityColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = result.severity.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // File path
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = result.filePath + (result.lineNumber?.let { ":$it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Suggestion
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF22C55E).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF22C55E)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = result.suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF22C55E)
                    )
                }
            }
        }
    }
}

// ============================================================
// VERIFICATION Section — 升级验证工具
// ============================================================
@Composable
private fun VerificationSectionContent(
    state: Compose11LayoutsState,
    onIntent: (Compose11LayoutsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✅ 升级验证工具",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // BOM info
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("当前 BOM", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.currentComposeBom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(horizontalAlignment = Alignment.End) {
                        Text("最新 BOM", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            state.verificationResult?.latestBom ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(Compose11LayoutsIntent.RunVerification) },
                    enabled = !state.verificationInProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.verificationInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("运行验证 / Run Verification")
                    }
                }
            }
        }

        // Verification results
        state.verificationResult?.let { result ->
            // Compliance badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (result.isCompliant) Color(0xFF22C55E).copy(alpha = 0.12f)
                            else Color(0xFFF59E0B).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (result.isCompliant) "✅ 合规" else "⚠️ 需要升级",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isCompliant) Color(0xFF22C55E) else Color(0xFFF59E0B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Suggestions
            if (result.suggestions.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.06f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💡 升级建议", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        result.suggestions.forEach { suggestion ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", style = MaterialTheme.typography.bodySmall)
                                Text(suggestion, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dependency tree
                Text("📦 依赖树 / Dependency Tree", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                DependencyTreeView(node = result.dependencyTree.firstOrNull(), depth = 0)
            }
        }
    }
}

// ============================================================
// Dependency Tree View — 依赖树可视化
// ============================================================
@Composable
private fun DependencyTreeView(node: DependencyNode?, depth: Int) {
    node ?: return
    val indent = Modifier.padding(start = (depth * 16).dp)

    Column(modifier = indent) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            if (depth > 0) Text("├─ ", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            Text(
                text = node.name,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = " : ${node.version}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = if (node.version != node.latestVersion) Color(0xFFF59E0B) else Color(0xFF22C55E)
            )
            if (node.version != node.latestVersion) {
                Text(
                    text = " → ${node.latestVersion}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF22C55E)
                )
            }
        }
        node.children.forEach { child ->
            DependencyTreeView(node = child, depth = depth + 1)
        }
    }
}

// ============================================================
// Template Card — 模板卡片组件
// ============================================================
@Composable
private fun TemplateCard(
    title: String,
    description: String,
    category: String,
    code: String,
    onPreview: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPreview,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("预览", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = onCopy,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            CodeBlockView(code = code, maxLines = 6)
        }
    }
}

// ============================================================
// Code Block View — 代码块展示组件
// ============================================================
@Composable
private fun CodeBlockView(
    code: String,
    maxLines: Int = Int.MAX_VALUE
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val lines = code.lines()
    val showExpand = lines.size > maxLines

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF4F4F5),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { /* Copy logic handled by parent intent */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (expanded || !showExpand) code else lines.take(maxLines).joinToString("\n") + "\n...",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF18181B),
                modifier = Modifier.fillMaxWidth()
            )

            if (showExpand) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (expanded) "收起" else "展开全部",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ============================================================
// Preview Dialog — 全屏模板预览 Dialog
// ============================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewDialog(
    template: Any,
    parameters: Map<String, Any>,
    isDarkMode: Boolean,
    onParameterChange: (String, Any) -> Unit,
    onToggleDarkMode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (template) {
                        is GridTemplate -> "${template.nameCn} 预览"
                        is FlexBoxTemplate -> "${template.nameCn} 预览"
                        is MediaQueryTemplate -> "${template.nameCn} 预览"
                        else -> "模板预览"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode"
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Parameter sliders
                val paramList = when (template) {
                    is GridTemplate -> template.parameters
                    is FlexBoxTemplate -> template.parameters
                    else -> emptyList()
                }

                if (paramList.isNotEmpty()) {
                    Text(
                        text = "⚙️ 参数调节",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    paramList.forEach { param ->
                        ParameterSlider(
                            parameter = param,
                            value = (parameters[param.key] as? Float)
                                ?: (param.defaultValue as? Float)
                                ?: 0f,
                            onValueChange = { onParameterChange(param.key, it) }
                        )
                    }
                }

                // Code preview
                Text(
                    text = "📄 代码",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF4F4F5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val code = when (template) {
                        is GridTemplate -> template.code
                        is FlexBoxTemplate -> template.code
                        is MediaQueryTemplate -> template.code
                        else -> ""
                    }
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

// ============================================================
// Parameter Slider — 参数调节滑块
// ============================================================
@Composable
private fun ParameterSlider(
    parameter: TemplateParameter,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = parameter.label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = (parameter.minValue ?: 0f)..(parameter.maxValue ?: 100f),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF8B5CF6),
                activeTrackColor = Color(0xFF8B5CF6)
            )
        )
    }
}

// ============================================================
// Extensions for Screen composable (imports)
// ============================================================
// Note: Full import list managed by the Kotlin compiler
// This composable uses experimental Grid API (ExperimentalLayoutApi)
// ============================================================

// Companion object for ScreenMode icon extension
private val ScreenMode.icon: String
    get() = when (this) {
        ScreenMode.PHONE -> "📱"
        ScreenMode.TABLET -> "📲"
        ScreenMode.FOLDABLE -> "📱"
        ScreenMode.TOPIC_TABLE_MODE -> "💻"
    }
