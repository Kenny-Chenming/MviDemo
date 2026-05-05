package com.mvi.kenny.feature.swiftexporttool

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.swiftexporttool.SwiftExportColors as C
import com.mvi.kenny.feature.swiftexporttool.SwiftExportTab as Tab
import com.mvi.kenny.feature.swiftexporttool.SwiftExportToolIntent as Intent
import com.mvi.kenny.feature.swiftexporttool.SwiftExportToolEffect as Effect
import com.mvi.kenny.feature.swiftexporttool.ScanState as ScanStatus
import com.mvi.kenny.feature.swiftexporttool.ScanResultType as ResultType
import com.mvi.kenny.feature.swiftexporttool.DecisionNodeType as NodeType
import com.mvi.kenny.feature.swiftexporttool.CICheckState as CICheck
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * SwiftExportToolScreen — Swift Export iOS 原生互联络工具包主屏幕
 * ============================================================
 * PRD-232 | Kotlin 2.2.20 Swift Export iOS 原生互联络工具包
 *
 * 5 Tab 布局（Terminal/CLI 暗色主题）：
 * - Tab 1: Swift Export 配置扫描器
 * - Tab 2: SKIE → Swift Export 迁移扫描器
 * - Tab 3: Swift Export iOS 集成模板
 * - Tab 4: CI 校验工具
 * - Tab 5: Swift Concurrency × Swift Export 最佳实践
 *
 * 架构：
 * - MVI：ViewModel 处理所有 Intent，State 驱动 UI 重组
 * - Effect：通过 LaunchedEffect 收集副作用（Toast 等）
 *
 * @see SwiftExportToolViewModel 状态管理
 * @see SwiftExportToolContract MVI 契约
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwiftExportToolScreen(
    onNavigateTo: (String) -> Unit = {},
    viewModel: SwiftExportToolViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    // 监听 Effect
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.ShowToast -> { /* Toast shown via snackbar or ignored in demo */ }
                is Effect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }
                is Effect.ShowError -> { /* Error dialog */ }
            }
        }
    }

    // 主布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(C.Background)
    ) {
        // ============================================================
        // Tab Row / 顶部 Tab 栏
        // ============================================================
        SwiftExportTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = { viewModel.processIntent(Intent.SelectTab(it)) }
        )

        // ============================================================
        // Tab Content / Tab 内容区
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(C.Background)
        ) {
            when (state.selectedTab) {
                Tab.SWIFT_EXPORT_SCANNER -> SwiftExportScannerContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                Tab.SKIE_MIGRATION -> SKIEMigrationContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                Tab.IOS_TEMPLATE -> IOSTemplateContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                Tab.CI_VALIDATION -> CIValidationContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
                Tab.CONCURRENCY -> ConcurrencyContent(
                    state = state,
                    onIntent = viewModel::processIntent
                )
            }
        }
    }
}

// ============================================================
// Tab Row / Tab 栏组件
// ============================================================

@Composable
private fun SwiftExportTabRow(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    TabRow(
        selectedTabIndex = Tab.entries.indexOf(selectedTab),
        containerColor = C.Surface,
        contentColor = C.Primary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[Tab.entries.indexOf(selectedTab)]),
                color = C.Primary
            )
        }
    ) {
        Tab.entries.forEach { tab ->
            val icon: ImageVector = when (tab) {
                Tab.SWIFT_EXPORT_SCANNER -> Icons.Default.Radar
                Tab.SKIE_MIGRATION -> Icons.Default.SwapHoriz
                Tab.IOS_TEMPLATE -> Icons.Default.Description
                Tab.CI_VALIDATION -> Icons.Default.CheckCircle
                Tab.CONCURRENCY -> Icons.Default.Loop
            }
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        color = if (selectedTab == tab) C.Primary else C.TextSecondary
                    )
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = if (selectedTab == tab) C.Primary else C.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

// ============================================================
// Tab 1: Swift Export 配置扫描器
// ============================================================

@Composable
private fun SwiftExportScannerContent(
    state: com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState,
    onIntent: (Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Swift Export 配置扫描器",
                    style = TextStyle(color = C.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "扫描 build.gradle.kts 中的 swiftExport 配置",
                    style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                )
            }
            Button(
                onClick = { onIntent(Intent.StartSwiftExportScan) },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = C.TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${state.scanProgress}%", color = C.TextPrimary)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = C.TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("开始扫描", color = C.TextPrimary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Scan Progress
        if (state.isScanning) {
            LinearProgressIndicator(
                progress = { state.scanProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = C.Primary,
                trackColor = C.Surface
            )
            Spacer(Modifier.height(16.dp))
        }

        // Results
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.scanState == ScanStatus.COMPLETED) {
                item {
                    Text(
                        text = "扫描结果（${state.swiftExportConfigs.size} 项）",
                        style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                    )
                }
                items(state.swiftExportConfigs) { config ->
                    ConfigResultCard(
                        config = config,
                        isSelected = config.id == state.selectedConfigId,
                        onClick = { onIntent(Intent.SelectConfig(config.id)) }
                    )
                }
            } else if (state.scanState == ScanStatus.IDLE) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Radar,
                        title = "准备就绪",
                        description = "点击「开始扫描」分析 build.gradle.kts 配置"
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigResultCard(
    config: com.mvi.kenny.feature.swiftexporttool.SwiftExportConfigResult,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when (config.resultType) {
        ResultType.SUCCESS -> C.Success
        ResultType.WARNING -> C.Warning
        ResultType.ERROR -> C.Error
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        border = if (isSelected) BorderStroke(1.dp, C.Primary) else BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (config.resultType) {
                    ResultType.SUCCESS -> Icons.Default.CheckCircle
                    ResultType.WARNING -> Icons.Default.Warning
                    ResultType.ERROR -> Icons.Default.Error
                },
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.configName,
                    style = TextStyle(color = C.Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = config.configValue,
                    style = TextStyle(color = C.TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${config.sourceFile}:${config.lineNumber}",
                    style = TextStyle(color = C.TextSecondary, fontSize = 10.sp)
                )
            }
            if (config.message.isNotEmpty()) {
                Text(
                    text = config.message,
                    style = TextStyle(color = borderColor, fontSize = 11.sp)
                )
            }
        }
    }
}

// ============================================================
// Tab 2: SKIE → Swift Export 迁移扫描器
// ============================================================

@Composable
private fun SKIEMigrationContent(
    state: com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState,
    onIntent: (Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SKIE → Swift Export 迁移扫描器",
                    style = TextStyle(color = C.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "扫描 @SKIE 注解并生成迁移决策树",
                    style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                )
            }
            Button(
                onClick = { onIntent(Intent.StartSKIEScan) },
                enabled = !state.isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
            ) {
                if (state.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = C.TextPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${state.scanProgress}%", color = C.TextPrimary)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = C.TextPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("分析 SKIE", color = C.TextPrimary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 决策树
            if (state.rootDecisionNode != null) {
                item {
                    Text(
                        text = "迁移决策树",
                        style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                    )
                }
                item {
                    DecisionTreeCard(
                        node = state.rootDecisionNode!!,
                        onToggle = { nodeId -> onIntent(Intent.ToggleDecisionNode(nodeId)) }
                    )
                }
            }

            // SKIE 注解列表
            if (state.skieAnnotationResults.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "检测到的 @SKIE 注解（${state.skieAnnotationResults.size} 个）",
                        style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                    )
                }
                items(state.skieAnnotationResults) { annotation ->
                    SKIEAnnotationCard(
                        annotation = annotation,
                        isSelected = annotation.id == state.selectedAnnotationId,
                        onClick = { onIntent(Intent.SelectAnnotation(annotation.id)) }
                    )
                }
            }

            if (state.scanState == ScanStatus.IDLE) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.SwapHoriz,
                        title = "准备分析",
                        description = "点击「分析 SKIE」扫描项目中的 @SKIE 注解"
                    )
                }
            }
        }
    }
}

@Composable
private fun DecisionTreeCard(
    node: com.mvi.kenny.feature.swiftexporttool.DecisionTreeNode,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 根节点
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (node.nodeType) {
                        NodeType.CONDITION -> Icons.Default.Hub
                        NodeType.ACTION -> Icons.Default.PlayArrow
                        NodeType.END -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when (node.nodeType) {
                        NodeType.CONDITION -> C.Primary
                        NodeType.ACTION -> C.Warning
                        NodeType.END -> C.Success
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.label,
                        style = TextStyle(color = C.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )
                    if (node.description.isNotEmpty()) {
                        Text(
                            text = node.description,
                            style = TextStyle(color = C.TextSecondary, fontSize = 11.sp)
                        )
                    }
                }
                if (node.children.isNotEmpty()) {
                    IconButton(onClick = { onToggle(node.id) }) {
                        Icon(
                            imageVector = if (node.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "展开/折叠",
                            tint = C.TextSecondary
                        )
                    }
                }
            }

            // 子节点
            AnimatedVisibility(visible = node.isExpanded && node.children.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(start = 28.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    node.children.forEach { child ->
                        DecisionTreeCard(node = child, onToggle = onToggle)
                    }
                }
            }

            // 推荐操作
            AnimatedVisibility(visible = node.recommendation.isNotEmpty()) {
                Text(
                    text = "💡 ${node.recommendation}",
                    style = TextStyle(color = C.Success, fontSize = 11.sp),
                    modifier = Modifier.padding(start = 28.dp, top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SKIEAnnotationCard(
    annotation: com.mvi.kenny.feature.swiftexporttool.SKIEAnnotationResult,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface),
        border = if (isSelected) BorderStroke(1.dp, C.Primary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = if (annotation.isMigratable) C.Success else C.Error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = annotation.annotationName,
                    style = TextStyle(color = C.Primary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = annotation.annotatedElement,
                    style = TextStyle(color = C.TextPrimary, fontSize = 12.sp)
                )
                Text(
                    text = "${annotation.sourceFile}:${annotation.lineNumber}",
                    style = TextStyle(color = C.TextSecondary, fontSize = 10.sp)
                )
            }
            Text(
                text = if (annotation.isMigratable) "可迁移 ✓" else "不支持 ✗",
                style = TextStyle(
                    color = if (annotation.isMigratable) C.Success else C.Error,
                    fontSize = 11.sp
                )
            )
        }
    }
}

// ============================================================
// Tab 3: iOS 集成模板
// ============================================================

@Composable
private fun IOSTemplateContent(
    state: com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState,
    onIntent: (Intent) -> Unit
) {
    var selectedTemplateType by remember { mutableStateOf(com.mvi.kenny.feature.swiftexporttool.TemplateType.PACKAGE_SWIFT) }
    val selectedTemplate = state.templates.find { it.type == selectedTemplateType }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Swift Export iOS 集成模板",
            style = TextStyle(color = C.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "生成 Package.swift、SPM 步骤和 Gradle 配置",
            style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
        )

        Spacer(Modifier.height(16.dp))

        // Template Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.templates.forEach { template ->
                FilterChip(
                    selected = selectedTemplateType == template.type,
                    onClick = { selectedTemplateType = template.type },
                    label = { Text(template.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = C.Primary,
                        selectedLabelColor = C.Background
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Template Content
        selectedTemplate?.let { template ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = C.CodeBlockBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = template.name,
                                style = TextStyle(color = C.Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = template.description,
                                style = TextStyle(color = C.TextSecondary, fontSize = 11.sp)
                            )
                        }
                        Button(
                            onClick = { onIntent(Intent.CopyTemplate(template.type)) },
                            colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = C.TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("复制", color = C.TextPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    HorizontalDivider(color = C.Border)

                    Spacer(Modifier.height(8.dp))

                    // Code Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = template.content,
                            style = TextStyle(
                                color = C.TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Checksum
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = C.TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "SHA256: ${template.checksum.take(16)}...",
                            style = TextStyle(color = C.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 4: CI 校验工具
// ============================================================

@Composable
private fun CIValidationContent(
    state: com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState,
    onIntent: (Intent) -> Unit
) {
    val ciResult = state.ciValidationResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CI 校验工具",
                    style = TextStyle(color = C.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "校验 .swiftmodule 版本一致性和 Gradle 任务链",
                    style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                )
            }
            Button(
                onClick = { onIntent(Intent.RunCIValidation) },
                enabled = ciResult.items.isEmpty() || ciResult.items.all {
                    it.status == CICheck.PASSED || it.status == CICheck.FAILED
                },
                colors = ButtonDefaults.buttonColors(containerColor = C.Primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = C.TextPrimary)
                Spacer(Modifier.width(8.dp))
                Text("运行 CI 校验", color = C.TextPrimary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Overall Status
        if (ciResult.items.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = C.Surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (ciResult.overallPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (ciResult.overallPassed) C.Success else C.Error,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (ciResult.overallPassed) "所有检查通过 ✓" else "部分检查失败 ✗",
                            style = TextStyle(color = C.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "${ciResult.passedChecks}/${ciResult.totalChecks} 通过，${ciResult.failedChecks} 失败",
                            style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Gradle Task Chain
        if (ciResult.gradleTaskChain.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = C.CodeBlockBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Gradle 任务链",
                        style = TextStyle(color = C.TextSecondary, fontSize = 11.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ciResult.gradleTaskChain,
                        style = TextStyle(color = C.Primary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Check Items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ciResult.items) { check ->
                CICheckCard(check = check)
            }

            if (ciResult.items.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.CheckCircle,
                        title = "准备就绪",
                        description = "点击「运行 CI 校验」开始检查项目配置"
                    )
                }
            }
        }
    }
}

@Composable
private fun CICheckCard(check: com.mvi.kenny.feature.swiftexporttool.CICheckItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (check.status) {
                CICheck.PENDING -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = C.TextSecondary,
                    strokeWidth = 2.dp
                )
                CICheck.RUNNING -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = C.Primary,
                    strokeWidth = 2.dp
                )
                CICheck.PASSED -> Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = C.Success,
                    modifier = Modifier.size(20.dp)
                )
                CICheck.FAILED -> Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    tint = C.Error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = check.name,
                    style = TextStyle(color = C.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = check.description,
                    style = TextStyle(color = C.TextSecondary, fontSize = 11.sp)
                )
            }
            if (check.message.isNotEmpty()) {
                Text(
                    text = check.message,
                    style = TextStyle(
                        color = when (check.status) {
                            CICheck.PASSED -> C.Success
                            CICheck.FAILED -> C.Error
                            else -> C.TextSecondary
                        },
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ============================================================
// Tab 5: Swift Concurrency 最佳实践
// ============================================================

@Composable
private fun ConcurrencyContent(
    state: com.mvi.kenny.feature.swiftexporttool.SwiftExportToolState,
    onIntent: (Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Swift Concurrency × Swift Export 最佳实践",
            style = TextStyle(color = C.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "async/await、数据流和 Actor 隔离模式",
            style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.bestPractices) { practice ->
                ConcurrencyPracticeCard(
                    practice = practice,
                    isExpanded = state.expandedPracticeId == practice.id,
                    onToggle = { onIntent(Intent.TogglePracticeExpanded(practice.id)) },
                    onCopy = { onIntent(Intent.CopyPracticeCode(practice.id)) }
                )
            }
        }
    }
}

@Composable
private fun ConcurrencyPracticeCard(
    practice: com.mvi.kenny.feature.swiftexporttool.ConcurrencyBestPractice,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = null,
                    tint = C.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = practice.patternName,
                        style = TextStyle(color = C.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = practice.category,
                        style = TextStyle(color = C.Primary, fontSize = 11.sp)
                    )
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "展开/折叠",
                        tint = C.TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = practice.description,
                        style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
                    )
                    Spacer(Modifier.height(8.dp))

                    // Benefits
                    if (practice.benefits.isNotEmpty()) {
                        Text(
                            text = "✓ 优势",
                            style = TextStyle(color = C.Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                        practice.benefits.forEach { benefit ->
                            Text(
                                text = "  • $benefit",
                                style = TextStyle(color = C.TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Code Example
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = C.CodeBlockBg)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "代码示例",
                                    style = TextStyle(color = C.TextSecondary, fontSize = 10.sp)
                                )
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "复制代码",
                                        tint = C.TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = practice.codeExample,
                                style = TextStyle(color = C.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Common Components / 通用组件
// ============================================================

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = C.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = C.TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = TextStyle(color = C.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                style = TextStyle(color = C.TextSecondary, fontSize = 12.sp)
            )
        }
    }
}
