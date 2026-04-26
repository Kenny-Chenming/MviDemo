package com.mvi.kenny.feature.androidxraiglasses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvi.kenny.feature.androidxraiglasses.*

/**
 * ============================================================
 * AndroidXRScreen — Android XR AI Glasses 开发工具包主界面
 * ============================================================
 * PRD-168 / Android XR AI Glasses Development Toolkit
 * Google I/O 2026: May 19-20, 2026
 */
@Composable
fun AndroidXRScreen(
    viewModel: AndroidXRViewModel,
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(com.mvi.kenny.base.TopBarConfig(title = "Android XR AI Glasses · ${state.currentTab.title}"))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AndroidXREffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is AndroidXREffect.ShowError -> snackbarHostState.showSnackbar("Error: ${effect.message}")
                is AndroidXREffect.ScrollToTop -> {}
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            XRTabRow(state.currentTab) { viewModel.sendIntent(AndroidXRIntent.SwitchTab(it)) }
            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                when (state.currentTab) {
                    XRTab.OVERVIEW -> OverviewContent(state, viewModel)
                    XRTab.GLIMMER_GUIDE -> GlimmerGuideContent(state, viewModel)
                    XRTab.GLIMMER_TEMPLATES -> GlimmerTemplatesContent(state, viewModel)
                    XRTab.PROJECTED_LIBRARY -> ProjectedLibraryContent(state, viewModel)
                    XRTab.ARCORE_GUIDE -> ARCoreGuideContent(state, viewModel)
                    XRTab.UX_RULES -> UXRulesContent(state, viewModel)
                    XRTab.DUAL_DEVICE -> DualDeviceContent(state, viewModel)
                    XRTab.EMULATOR -> EmulatorContent(state, viewModel)
                    XRTab.CI_PIPELINE -> CIPipelineContent(state, viewModel)
                    XRTab.APPENDIX -> AppendixContent(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun XRTabRow(currentTab: XRTab, onTabSelected: (XRTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            .background(XRColors.Surface).padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        XRTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == currentTab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.title, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = XRColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = XRColors.CardGradientStart,
                    labelColor = XRColors.OnSurface
                ),
                modifier = Modifier.height(32.dp)
            )
        }
    }
}

@Composable
private fun XRCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(XRColors.CardGradientStart, XRColors.CardGradientEnd)))
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = XRColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CodeBlock(code: String, modifier: Modifier = Modifier, onCopy: (() -> Unit)? = null) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Code", style = MaterialTheme.typography.labelSmall, color = XRColors.Secondary)
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.ContentCopy, "Copy",
                            tint = XRColors.OnSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                code,
                style = MaterialTheme.typography.bodySmall,
                color = XRColors.OnSurface,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun OverviewContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    Column {
        // Google I/O 2026 Countdown Card
        XRCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Google I/O 2026", style = MaterialTheme.typography.titleLarge, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    Text("May 19-20, 2026", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.daysUntilIO}", style = MaterialTheme.typography.headlineLarge, color = XRColors.Accent, fontWeight = FontWeight.Bold)
                    Text("days", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                }
            }
        }

        SectionTitle("选择你的应用场景", Icons.Default.Apps)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppType.entries.forEach { appType ->
                val selected = appType == state.appType
                Card(
                    modifier = Modifier.width(140.dp).clickable { viewModel.sendIntent(AndroidXRIntent.SetAppType(appType)) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) XRColors.Primary.copy(alpha = 0.3f) else XRColors.CardGradientStart
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, XRColors.Primary) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val icon = when (appType) {
                            AppType.EXISTING -> Icons.Default.Upgrade
                            AppType.NEW_GLASSES -> Icons.Default.Add
                            AppType.NEW_PHONE -> Icons.Default.Devices
                        }
                        Icon(icon, contentDescription = null, tint = if (selected) XRColors.Primary else XRColors.OnSurface, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(appType.label, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("推荐开发路径", Icons.Default.Route)
        XRCard {
            state.recommendedPath.forEachIndexed { index, module ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(28.dp)
                            .background(if (module.isRecommended) XRColors.Primary else XRColors.Secondary, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(module.name, style = MaterialTheme.typography.bodyLarge, color = XRColors.OnSurface, fontWeight = FontWeight.Medium)
                            if (module.isRecommended) {
                                Spacer(Modifier.width(8.dp))
                                Text("⭐ 推荐", style = MaterialTheme.typography.labelSmall, color = XRColors.Accent)
                            }
                        }
                        Text(module.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    }
                }
                if (index < state.recommendedPath.lastIndex) {
                    HorizontalDivider(color = XRColors.OnSurface.copy(alpha = 0.1f), modifier = Modifier.padding(start = 40.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("快速入口", Icons.Default.GridView)
        val quickEntries = listOf(
            QuickEntry("Glimmer迁移", Icons.Default.Transform, XRTab.GLIMMER_GUIDE, XRColors.Primary),
            QuickEntry("UI模板", Icons.Default.Dashboard, XRTab.GLIMMER_TEMPLATES, XRColors.Secondary),
            QuickEntry("Projected", Icons.Default.PhoneAndroid, XRTab.PROJECTED_LIBRARY, XRColors.Accent),
            QuickEntry("ARCore", Icons.Default.ViewInAr, XRTab.ARCORE_GUIDE, XRColors.Primary),
            QuickEntry("UX规范", Icons.Default.DesignServices, XRTab.UX_RULES, XRColors.Secondary),
            QuickEntry("模拟器", Icons.Default.Phone, XRTab.EMULATOR, XRColors.Accent)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            quickEntries.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { entry ->
                        Card(
                            modifier = Modifier.weight(1f).clickable { viewModel.sendIntent(AndroidXRIntent.SwitchTab(entry.tab)) },
                            colors = CardDefaults.cardColors(containerColor = entry.color.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(entry.icon, contentDescription = null, tint = entry.color, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(entry.title, style = MaterialTheme.typography.labelMedium, color = XRColors.OnSurface, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("目标眼镜设备", Icons.Default.Speed)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassesType.entries.forEach { glassesType ->
                val selected = glassesType == state.selectedGlassesType
                Card(
                    modifier = Modifier.width(120.dp).clickable { viewModel.sendIntent(AndroidXRIntent.SelectGlassesType(glassesType)) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) XRColors.Secondary.copy(alpha = 0.3f) else XRColors.CardGradientStart
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, XRColors.Secondary) else null
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = if (selected) XRColors.Secondary else XRColors.OnSurface, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(glassesType.label, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface, textAlign = TextAlign.Center)
                        Text("${glassesType.screenWidth}×${glassesType.screenHeight}", style = MaterialTheme.typography.labelSmall, color = XRColors.OnSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

private data class QuickEntry(val title: String, val icon: ImageVector, val tab: XRTab, val color: Color)

@Composable
private fun GlimmerGuideContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadGlimmerGuide) }
    val guide = state.glimmerGuide ?: return
    Column {
        SectionTitle("Glimmer vs 标准 Compose 对比", Icons.Default.TableChart)
        XRCard {
            guide.comparisonTable.forEach { item ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(item.feature, style = MaterialTheme.typography.titleSmall, color = XRColors.Primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("标准: ${item.standardCompose}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Glimmer: ${item.glimmer}", style = MaterialTheme.typography.bodySmall, color = XRColors.Secondary)
                        }
                    }
                    Text("差异: ${item.difference}", style = MaterialTheme.typography.labelSmall, color = XRColors.Accent)
                }
                HorizontalDivider(color = XRColors.OnSurface.copy(alpha = 0.1f))
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("迁移决策树", Icons.Default.AccountTree)
        XRCard {
            guide.decisionTree.forEachIndexed { index, node ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier.size(24.dp).background(XRColors.Primary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(node.question, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("✓ 是 → ${node.yesBranch}", style = MaterialTheme.typography.bodySmall, color = XRColors.Secondary)
                        Text("✗ 否 → ${node.noBranch}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.6f))
                        if (node.recommendation.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text("💡 ${node.recommendation}", style = MaterialTheme.typography.labelSmall, color = XRColors.Accent)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Glimmer 特有组件", Icons.Default.Widgets)
        guide.componentList.forEach { component ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(component.name, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                            if (component.isExperimental) {
                                Spacer(Modifier.width(8.dp))
                                Text("🔬 实验性", style = MaterialTheme.typography.labelSmall, color = XRColors.Accent)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(component.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                        Text("版本: ${component.version}", style = MaterialTheme.typography.labelSmall, color = XRColors.Secondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("迁移步骤指南", Icons.Default.Layers)
        guide.migrationSteps.forEach { step ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(24.dp).background(XRColors.Primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${step.stepNumber}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(step.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(step.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    if (step.codeExample.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        CodeBlock(step.codeExample, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(step.codeExample)) })
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("代码 Diff 示例", Icons.Default.Code)
        CodeBlock(guide.codeDiff, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(guide.codeDiff)) })
    }
}

@Composable
private fun GlimmerTemplatesContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    val templates = remember {
        listOf(
            GlimmerTemplate("n1", "通知卡片", TemplateCategory.NOTIFICATION_CARD, "显示简短通知，适合眼镜端信息提醒", "NotificationCard()", "@Composable fun NotificationCard(title: String, content: String, timestamp: String) { GlimmerCard { GlimmerText(title, GlimmerTypography.Heading); GlimmerText(content, GlimmerTypography.Body); GlimmerText(timestamp, GlimmerTypography.Caption) } }", listOf("通知", "卡片")),
            GlimmerTemplate("n2", "AR 导航叠加", TemplateCategory.AR_OVERLAY, "在现实世界上叠加导航信息", "ArNavigationOverlay()", "@Composable fun ArNavigationOverlay(direction: String, distance: String, landmark: String) { DepthStack { GlimmerText(direction, GlimmerTypography.Headline, color = XRColors.Accent); GlimmerText(distance + \" 米\", GlimmerTypography.Body); GlimmerText(landmark, GlimmerTypography.Caption) } }", listOf("AR", "导航")),
            GlimmerTemplate("n3", "语音助手界面", TemplateCategory.VOICE_ASSISTANT, "AI 语音助手响应界面", "VoiceAssistantUI()", "@Composable fun VoiceAssistantUI(message: String, isProcessing: Boolean) { GlimmerColumn(horizontalAlignment = Alignment.CenterHorizontally) { if (isProcessing) GlimmerCircularProgress() else Icon(Icons.Default.Mic, null, XRColors.Primary); GlimmerText(message, GlimmerTypography.Body, textAlign = TextAlign.Center) } }", listOf("语音", "助手")),
            GlimmerTemplate("n4", "导航指引", TemplateCategory.NAVIGATION, "转弯/路线指引界面", "NavigationGuide()", "@Composable fun NavigationGuide(instruction: String, nextStreet: String, eta: String) { GlimmerCard { GlimmerText(instruction, GlimmerTypography.Headline, color = XRColors.Accent); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { GlimmerText(nextStreet, GlimmerTypography.Body); GlimmerText(eta, GlimmerTypography.Caption) } } }", listOf("导航")),
            GlimmerTemplate("n5", "设置面板", TemplateCategory.SETTINGS_PANEL, "眼镜端设置选项界面", "SettingsPanel()", "@Composable fun SettingsPanel(options: List<SettingOption>, selectedIndex: Int, onSelect: (Int) -> Unit) { GlimmerColumn { options.forEachIndexed { index, option -> GlimmerListItem(option.title, option.subtitle, Modifier.clickable { onSelect(index) }) } } }", listOf("设置", "列表")),
            GlimmerTemplate("n6", "信息卡片", TemplateCategory.INFO_CARD, "通用信息展示卡片", "InfoCard()", "@Composable fun InfoCard(title: String, items: List<InfoItem>) { GlimmerCard { GlimmerText(title, GlimmerTypography.Heading); items.forEach { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { GlimmerText(it.label, GlimmerTypography.Body); GlimmerText(it.value, GlimmerTypography.Body, XRColors.Secondary) } } } }", listOf("信息"))
        )
    }
    var selectedTemplate by remember { mutableStateOf<GlimmerTemplate?>(null) }

    Column {
        SectionTitle("模板分类", Icons.Default.Category)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TemplateCategory.entries) { category ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(category.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = XRColors.CardGradientStart,
                        labelColor = XRColors.OnSurface
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("UI 模板库", Icons.Default.Dashboard)
        templates.chunked(2).forEach { rowTemplates ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowTemplates.forEach { template ->
                    Card(
                        modifier = Modifier.weight(1f).clickable { selectedTemplate = template },
                        colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(XRColors.Surface)
                                    .border(2.dp, XRColors.Primary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(template.name.take(4), style = MaterialTheme.typography.titleMedium, color = XRColors.OnSurface)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(template.name, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                            Text(template.category.label, style = MaterialTheme.typography.labelSmall, color = XRColors.Primary)
                            Text(template.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (rowTemplates.size < 2) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }

        selectedTemplate?.let { template ->
            AlertDialog(
                onDismissRequest = { selectedTemplate = null },
                title = { Text(template.name) },
                text = {
                    Column {
                        Text(template.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        CodeBlock(template.fullCode, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(template.fullCode)) })
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.sendIntent(AndroidXRIntent.CopyCode(template.fullCode))
                        selectedTemplate = null
                    }) {
                        Icon(Icons.Default.ContentCopy, null)
                        Spacer(Modifier.width(4.dp))
                        Text("复制代码")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTemplate = null }) {
                        Text("关闭")
                    }
                },
                containerColor = XRColors.CardGradientStart
            )
        }
    }
}

@Composable
private fun ProjectedLibraryContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadProjectedGuide) }
    val guide = state.projectedGuide ?: return
    Column {
        SectionTitle("核心概念", Icons.Default.Lightbulb)
        XRCard { Text(guide.conceptExplanation, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface) }

        Spacer(Modifier.height(16.dp))
        SectionTitle("API 使用步骤", Icons.Default.Api)
        guide.apiSteps.forEach { step ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(24.dp).background(XRColors.Secondary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${step.stepNumber}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(step.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(step.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    if (step.codeSnippet.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        CodeBlock(step.codeSnippet, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(step.codeSnippet)) })
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("协同场景模板", Icons.Default.Devices)
        guide.scenarioTemplates.forEach { scenario ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(scenario.name, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    Text(scenario.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    Text("流程: ${scenario.flow}", style = MaterialTheme.typography.labelSmall, color = XRColors.Secondary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("权限配置", Icons.Default.Security)
        guide.permissionConfig.forEach { perm ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(perm.permission, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface)
                        Text(perm.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    }
                    Text(if (perm.isRequired) "✅ 必需" else "⭕ 可选", style = MaterialTheme.typography.labelSmall, color = if (perm.isRequired) XRColors.Primary else XRColors.Accent)
                }
            }
        }
    }
}

@Composable
private fun ARCoreGuideContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadARCoreGuide) }
    val guide = state.arcoreGuide ?: return
    Column {
        SectionTitle("与标准 ARCore 差异", Icons.Default.CompareArrows)
        XRCard {
            guide.differencesFromStandard.forEach { diff ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", style = MaterialTheme.typography.bodyMedium, color = XRColors.Accent)
                    Text(diff, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Glimmer 下的 AR 能力", Icons.Default.ViewInAr)
        XRCard {
            guide.glimmerArCapabilities.forEach { cap ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✓ ", style = MaterialTheme.typography.bodyMedium, color = XRColors.Secondary)
                    Text(cap, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("开发步骤", Icons.Default.Layers)
        guide.developmentSteps.forEach { step ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(24.dp).background(XRColors.Primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${step.stepNumber}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(step.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(step.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    if (step.warning.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("⚠️ ${step.warning}", style = MaterialTheme.typography.labelSmall, color = XRColors.Accent)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("示例代码", Icons.Default.Code)
        CodeBlock(guide.sampleCode, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(guide.sampleCode)) })
    }
}

@Composable
private fun UXRulesContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadUXRules) }
    val rules = state.uxRules ?: return
    Column {
        SectionTitle("设计原则", Icons.Default.DesignServices)
        rules.principles.forEach { principle ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(principle.icon, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(principle.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                        Text(principle.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Do ✓", Icons.Default.CheckCircle)
        XRCard {
            rules.doList.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✓ ", style = MaterialTheme.typography.bodyMedium, color = XRColors.Secondary)
                    Text(item, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Don't ✗", Icons.Default.Cancel)
        XRCard {
            rules.dontList.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✗ ", style = MaterialTheme.typography.bodyMedium, color = XRColors.Accent)
                    Text(item, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }

        if (rules.typographySpec != null) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("字体规范", Icons.Default.TextFields)
            XRCard {
                Text("字体: ${rules.typographySpec.fontFamily}", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                Text("标题: ${rules.typographySpec.headingSize}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                Text("正文: ${rules.typographySpec.bodySize}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                Text("说明: ${rules.typographySpec.captionSize}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun DualDeviceContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadDualDeviceTemplate) }
    val template = state.dualDeviceTemplate ?: return
    Column {
        SectionTitle("双端架构图", Icons.Default.Architecture)
        XRCard {
            Text(
                template.architectureDiagram,
                style = MaterialTheme.typography.bodySmall,
                color = XRColors.OnSurface,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("数据同步策略", Icons.Default.Sync)
        XRCard { Text(template.dataSyncStrategy, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface) }

        Spacer(Modifier.height(16.dp))
        SectionTitle("代码模板", Icons.Default.Code)
        template.codeTemplates.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    CodeBlock(item.code, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(item.code)) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("测试方法", Icons.Default.BugReport)
        XRCard {
            template.testingMethods.forEachIndexed { index, method ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("${index + 1}. ", style = MaterialTheme.typography.bodyMedium, color = XRColors.Primary)
                    Text(method, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }
    }
}

@Composable
private fun EmulatorContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadEmulatorGuide) }
    val guide = state.emulatorGuide ?: return
    Column {
        SectionTitle("下载地址", Icons.Default.Download)
        XRCard { Text(guide.downloadUrl, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface) }

        Spacer(Modifier.height(16.dp))
        SectionTitle("配置参数", Icons.Default.Settings)
        XRCard {
            Column {
                Text("FoV: ${guide.fovSetting}", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                Text("分辨率: ${guide.resolutionSetting}", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                Text("DPI: ${guide.dpiSetting}", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("配置步骤", Icons.Default.Layers)
        guide.setupSteps.forEach { step ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(24.dp).background(XRColors.Secondary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${step.stepNumber}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(step.title, style = MaterialTheme.typography.titleSmall, color = XRColors.OnSurface, fontWeight = FontWeight.Bold)
                        Text(step.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("调试方法", Icons.Default.BugReport)
        guide.debuggingMethods.forEachIndexed { index, method ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(24.dp).background(XRColors.Secondary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(method, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("常见问题", Icons.Default.QuestionAnswer)
        guide.faq.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = XRColors.CardGradientStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(item.question, style = MaterialTheme.typography.titleSmall, color = XRColors.Accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("解答: ${item.answer}", style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface)
                }
            }
        }
    }
}

@Composable
private fun CIPipelineContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadCITemplate) }
    val template = state.ciTemplate ?: return
    Column {
        SectionTitle("GitHub Actions 模板", Icons.Default.Build)
        if (template.githubActionsTemplate.isNotEmpty()) {
            CodeBlock(template.githubActionsTemplate, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(template.githubActionsTemplate)) })
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("GitLab CI 模板", Icons.Default.Build)
        if (template.gitlabCiTemplate.isNotEmpty()) {
            CodeBlock(template.gitlabCiTemplate, onCopy = { viewModel.sendIntent(AndroidXRIntent.CopyCode(template.gitlabCiTemplate)) })
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("设备矩阵", Icons.Default.GridView)
        XRCard {
            Column {
                template.deviceMatrix.forEach { device ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(device.deviceName, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface)
                        Text("API ${device.apiLevel}", style = MaterialTheme.typography.bodySmall, color = XRColors.Secondary)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("PR 机器人配置", Icons.Default.Terminal)
        if (template.prBotConfig.isNotEmpty()) {
            XRCard {
                Text(
                    template.prBotConfig,
                    style = MaterialTheme.typography.bodySmall,
                    color = XRColors.OnSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun AppendixContent(state: AndroidXRState, viewModel: AndroidXRViewModel) {
    LaunchedEffect(Unit) { viewModel.sendIntent(AndroidXRIntent.LoadGlimmerGuide) }
    val guide = state.glimmerGuide ?: return
    Column {
        SectionTitle("Glimmer 特有组件速查", Icons.Default.Widgets)
        XRCard {
            Column {
                guide.componentList.forEach { component ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(component.name, style = MaterialTheme.typography.titleSmall, color = XRColors.Primary, fontWeight = FontWeight.Bold)
                            Text(component.description, style = MaterialTheme.typography.bodySmall, color = XRColors.OnSurface.copy(alpha = 0.7f))
                        }
                    }
                    HorizontalDivider(color = XRColors.OnSurface.copy(alpha = 0.1f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("标准 Compose 映射表", Icons.Default.TableChart)
        XRCard {
            Column {
                val mappings = listOf(
                    Pair("Column", "GlimmerColumn (垂直布局)"),
                    Pair("Row", "GlimmerRow (水平布局)"),
                    Pair("Box", "DepthStack (深度堆叠)"),
                    Pair("Text", "GlimmerText (XR优化)"),
                    Pair("Card", "GlimmerCard (浮动卡片)"),
                    Pair("LazyColumn", "GlimmerList (虚拟化列表)"),
                    Pair("LazyRow", "GlimmerHorizontalList"),
                    Pair("Button", "GlimmerTouchTarget (触控目标)"),
                    Pair("Icon", "GlimmerIcon (自适应图标)"),
                    Pair("Image", "GlimmerScene (3D场景)"),
                )
                mappings.forEach { mapping ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(
                            mapping.first,
                            style = MaterialTheme.typography.bodyMedium,
                            color = XRColors.Secondary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        Text("→", style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface.copy(alpha = 0.5f))
                        Text(
                            mapping.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = XRColors.Primary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = XRColors.OnSurface.copy(alpha = 0.05f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("版本兼容性参考", Icons.Default.Info)
        XRCard {
            Column {
                val versions = listOf(
                    Triple("Compose Glimmer", "实验性 API", "Glimmer 1.0+"),
                    Triple("Projected Library", "Google Play Services for XR", "Glamor 0.9+"),
                    Triple("ARCore for AI Glasses", "特定设备支持", "ARCore 1.4+"),
                    Triple("XR Emulator", "不支持 AR 功能", "Android Studio 2024+"),
                )
                versions.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(item.first, style = MaterialTheme.typography.bodyMedium, color = XRColors.OnSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text(item.second, style = MaterialTheme.typography.bodySmall, color = XRColors.Accent, modifier = Modifier.weight(1f))
                        Text(item.third, style = MaterialTheme.typography.bodySmall, color = XRColors.Secondary, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
