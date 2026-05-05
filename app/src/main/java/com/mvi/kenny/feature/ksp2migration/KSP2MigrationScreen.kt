package com.mvi.kenny.feature.ksp2migration

// ================================================================
// KSP2MigrationScreen — KSP1→KSP2 迁移工具包主界面
// ================================================================
// Main Screen for KSP2 Migration Toolkit.
//
// PRD-229: KSP1→KSP2 迁移工具包
// Design Reference: memory/agency/designs/PRD-229-KSP2-Migration-Toolkit.md
//
// 5-Tab Architecture:
//   Tab 0: KSP2 扫描器 (KSPScannerTab)       — Scan build.gradle.kts for ksp { } configs
//   Tab 1: 兼容性矩阵 (CompatibilityMatrixTab) — Processor KSP2 compatibility matrix
//   Tab 2: 迁移指南 (MigrationGuideTab)      — Step-by-step migration guide
//   Tab 3: API 变化参考 (APIChangesTab)      — KSP1→KSP2 API changes
//   Tab 4: CI 合规检测 (CIComplianceTab)     — CI compliance check
//
// Visual Style: Dark Terminal — #0D1117 background, monospace fonts,
//               color coding: cyan (primary) / green (supported) / orange (beta) / red (unsupported)
//
// @see KSP2MigrationViewModel for MVI state management
// @see KSP2MigrationContract for state/intent/effect definitions
// ================================================================

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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// ─────────────────────────────────────────────────────────────────────────────
// Terminal Color Palette — Dark IDE Theme for KSP2
// ─────────────────────────────────────────────────────────────────────────────
private val TerminalBg = Color(0xFF0D1117)
private val TerminalSurface = Color(0xFF161B22)
private val TerminalBorder = Color(0xFF30363D)
private val TerminalCyan = Color(0xFF58A6FF)    // KSP2 Beta 感主色
private val TerminalGreen = Color(0xFF3FB950)    // 已支持
private val TerminalOrange = Color(0xFFD29922)  // Beta / Warning
private val TerminalRed = Color(0xFFF85149)      // 不支持 / Error
private val TerminalGray = Color(0xFF8B949E)
private val TerminalWhite = Color(0xFFE6EDF3)

// ─────────────────────────────────────────────────────────────────────────────
// Main Screen Entry Point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KSP2MigrationScreen(
    viewModel: KSP2MigrationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Collect effects for one-time events / 收集一次性副作用事件
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is KSP2MigrationEffect.ShowSnackbar -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is KSP2MigrationEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText(effect.label, effect.content)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "已复制: ${effect.label}", Toast.LENGTH_SHORT).show()
                }
                is KSP2MigrationEffect.ShareReport -> {
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, effect.content)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "分享合规报告"))
                }
                is KSP2MigrationEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
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
            selectedTabIndex = state.currentTab,
            containerColor = TerminalSurface,
            contentColor = TerminalCyan,
            edgePadding = 8.dp,
            divider = { HorizontalDivider(color = TerminalBorder) }
        ) {
            TabTitles.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = state.currentTab == index,
                    onClick = { viewModel.sendIntent(KSP2MigrationIntent.SetTab(index)) },
                    text = {
                        Text(
                            text = tabTitle.label,
                            color = if (state.currentTab == index) TerminalCyan else TerminalGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tabTitle.icon,
                            contentDescription = tabTitle.label,
                            tint = if (state.currentTab == index) TerminalCyan else TerminalGray,
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
            when (state.currentTab) {
                0 -> KSPScannerTab(state, viewModel)
                1 -> CompatibilityMatrixTab(state, viewModel)
                2 -> MigrationGuideTab(state, viewModel)
                3 -> APIChangesTab(state, viewModel)
                4 -> CIComplianceTab(state, viewModel)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab Titles
// ─────────────────────────────────────────────────────────────────────────────

private data class TabTitle(val label: String, val icon: ImageVector)

private val TabTitles = listOf(
    TabTitle("KSP2扫描", Icons.Default.Search),
    TabTitle("兼容性矩阵", Icons.Default.Layers),
    TabTitle("迁移指南", Icons.Default.CheckCircle),
    TabTitle("API变化", Icons.Default.Code),
    TabTitle("CI合规", Icons.Default.Check)
)

// ─────────────────────────────────────────────────────────────────────────────
// Tab 0: KSP2 扫描器 (KSPScannerTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KSPScannerTab(
    state: KSP2MigrationState,
    viewModel: KSP2MigrationViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header / 标题区
        SectionHeader(
            title = "KSP 配置扫描器",
            subtitle = "粘贴 build.gradle.kts 内容，检测 ksp { } 配置和 processor 兼容性"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Gradle Content Input / Gradle 内容输入区
        OutlinedTextField(
            value = state.gradleFileContent,
            onValueChange = { viewModel.sendIntent(KSP2MigrationIntent.PasteGradleContent(it)) },
            label = { Text("build.gradle.kts 内容", color = TerminalGray) },
            placeholder = {
                Text(
                    "plugins {\n    id(\"com.google.devtools.ksp\")\n}\n\ndependencies {\n    ksp(\"androidx.room:room-compiler:2.6.1\")\n    ksp(\"com.google.dagger:hilt-compiler:2.51\")\n}",
                    color = TerminalGray.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TerminalWhite,
                unfocusedTextColor = TerminalWhite,
                focusedBorderColor = TerminalCyan,
                unfocusedBorderColor = TerminalBorder,
                cursorColor = TerminalCyan,
                focusedLabelColor = TerminalCyan,
                unfocusedLabelColor = TerminalGray
            ),
            textStyle = androidx.compose.ui.text.TextStyle(color = TerminalGreen, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scan Button / 扫描按钮
        Button(
            onClick = { viewModel.sendIntent(KSP2MigrationIntent.StartScan) },
            enabled = !state.isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerminalCyan,
                contentColor = TerminalBg,
                disabledContainerColor = TerminalCyan.copy(alpha = 0.3f),
                disabledContentColor = TerminalCyan
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("扫描中... ${(state.scanProgress * 100).toInt()}%", fontFamily = FontFamily.Monospace)
            } else {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始扫描 / Start Scan", fontFamily = FontFamily.Monospace)
            }
        }

        // Scan Progress / 扫描进度条
        if (state.isScanning) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.scanProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TerminalCyan,
                trackColor = TerminalBorder
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detected Versions / 检测到的版本信息
        if (state.scanDetectedKotlinVersion != null || state.scanDetectedKspVersion != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TerminalSurface, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                state.scanDetectedKotlinVersion?.let { kotlinVer ->
                    VersionBadge(label = "Kotlin", version = kotlinVer)
                }
                state.scanDetectedKspVersion?.let { kspVer ->
                    VersionBadge(label = "KSP", version = kspVer)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Scan Results / 扫描结果列表
        if (state.scanResults.isNotEmpty()) {
            SectionHeader(
                title = "扫描结果 / Scan Results",
                subtitle = "检测到 ${state.scanResults.size} 个 KSP processor"
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.scanResults.forEach { result ->
                ProcessorScanCard(result = result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (!state.isScanning && state.gradleFileContent.isBlank()) {
            // Empty state / 空状态引导
            EmptyStateCard(
                icon = Icons.Default.Search,
                title = "粘贴 build.gradle.kts 内容开始扫描",
                subtitle = "或点击「开始扫描」查看演示数据"
            )
        }
    }
}

@Composable
private fun ProcessorScanCard(result: ProcessorScanResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main row / 主行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = result.name,
                            color = TerminalWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CompatibilityBadge(compatibility = result.compatibility)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "v${result.version}",
                        color = TerminalGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TerminalGray
                )
            }

            // Expanded details / 展开详情
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Config snippet / 配置代码片段
                    Text("配置片段 / Config:", color = TerminalCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    CodeBlock(code = result.configSnippet)

                    Spacer(modifier = Modifier.height(8.dp))

                    // KSP2 compatible version / 兼容版本
                    result.ksp2CompatibleVersion?.let { ver ->
                        Row {
                            Text("KSP2 兼容版本: ", color = TerminalGray, fontSize = 12.sp)
                            Text(ver, color = TerminalGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Notes / 备注
                    Text(result.notes, color = when (result.compatibility) {
                        Compatibility.SUPPORTED -> TerminalGreen
                        Compatibility.BETA -> TerminalOrange
                        Compatibility.UNSUPPORTED -> TerminalRed
                        Compatibility.UNKNOWN -> TerminalGray
                    }, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 1: 兼容性矩阵 (CompatibilityMatrixTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompatibilityMatrixTab(
    state: KSP2MigrationState,
    viewModel: KSP2MigrationViewModel
) {
    val filteredProcessors = state.processors.filter {
        state.matrixFilter.isBlank() ||
        it.name.contains(state.matrixFilter, ignoreCase = true) ||
        it.category.contains(state.matrixFilter, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search/Filter bar / 搜索过滤栏
        OutlinedTextField(
            value = state.matrixFilter,
            onValueChange = { viewModel.sendIntent(KSP2MigrationIntent.FilterMatrix(it)) },
            label = { Text("搜索 Processor", color = TerminalGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TerminalGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TerminalWhite,
                unfocusedTextColor = TerminalWhite,
                focusedBorderColor = TerminalCyan,
                unfocusedBorderColor = TerminalBorder,
                cursorColor = TerminalCyan,
                focusedLabelColor = TerminalCyan,
                unfocusedLabelColor = TerminalGray
            ),
            textStyle = androidx.compose.ui.text.TextStyle(color = TerminalWhite, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category filter chips / 分类过滤标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                label = "全部 / All",
                selected = state.matrixFilter.isBlank(),
                onClick = { viewModel.sendIntent(KSP2MigrationIntent.FilterMatrix("")) }
            )
            listOf("Database", "DI", "Serialization", "Storage", "Other").forEach { cat ->
                CategoryChip(
                    label = cat,
                    selected = state.matrixFilter == cat,
                    onClick = { viewModel.sendIntent(KSP2MigrationIntent.FilterMatrix(cat)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Matrix summary header / 矩阵汇总头
        val supported = filteredProcessors.count { it.compatibility == Compatibility.SUPPORTED }
        val beta = filteredProcessors.count { it.compatibility == Compatibility.BETA }
        val unsupported = filteredProcessors.count { it.compatibility == Compatibility.UNSUPPORTED }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MatrixSummaryBadge(label = "已支持", count = supported, color = TerminalGreen)
            MatrixSummaryBadge(label = "Beta", count = beta, color = TerminalOrange)
            MatrixSummaryBadge(label = "不支持", count = unsupported, color = TerminalRed)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Processor cards / Processor 卡片列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProcessors) { processor ->
                ProcessorMatrixCard(processor = processor)
            }
        }
    }
}

@Composable
private fun ProcessorMatrixCard(processor: ProcessorInfo) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(processor.name, color = TerminalWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    CompatibilityBadge(compatibility = processor.compatibility)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("v${processor.latestVersion}", color = TerminalGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("KSP2: ${processor.ksp2CompatibleSince}", color = TerminalCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("[${processor.category}]", color = TerminalGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(processor.description, color = TerminalGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("迁移指南 / Migration Guide:", color = TerminalCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(processor.migrationGuide, color = TerminalWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 2: 迁移指南 (MigrationGuideTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MigrationGuideTab(
    state: KSP2MigrationState,
    viewModel: KSP2MigrationViewModel
) {
    val completedCount = state.completedSteps.size
    val totalCount = state.migrationSteps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress header / 进度头
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("迁移进度 / Progress", color = TerminalCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$completedCount / $totalCount 步骤", color = TerminalGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TerminalGreen,
                trackColor = TerminalBorder
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Steps list / 步骤列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(state.migrationSteps) { _, step ->
                MigrationStepCard(
                    step = step,
                    isCompleted = state.completedSteps.contains(step.stepNumber),
                    onToggle = { viewModel.sendIntent(KSP2MigrationIntent.ToggleStep(step.stepNumber)) }
                )
            }
        }
    }
}

@Composable
private fun MigrationStepCard(
    step: MigrationStep,
    isCompleted: Boolean,
    onToggle: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) TerminalGreen.copy(alpha = 0.1f) else TerminalSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Step number / 步骤编号
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                when {
                                    isCompleted -> TerminalGreen
                                    step.critical -> TerminalRed
                                    else -> TerminalCyan
                                },
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.stepNumber.toString(),
                            color = TerminalBg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = step.title,
                            color = if (isCompleted) TerminalGreen else TerminalWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "约 ${step.estimatedMinutes} 分钟",
                            color = TerminalGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = TerminalGreen,
                        uncheckedColor = TerminalGray
                    )
                )
            }

            if (step.critical) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = TerminalRed, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("关键步骤", color = TerminalRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(step.description, color = TerminalGray, fontSize = 12.sp)

                    step.codeBefore?.let { before ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("迁移前 / Before:", color = TerminalRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        CodeBlock(code = before)
                    }

                    step.codeAfter?.let { after ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("迁移后 / After:", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        CodeBlock(code = after)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 3: API 变化参考 (APIChangesTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun APIChangesTab(
    state: KSP2MigrationState,
    viewModel: KSP2MigrationViewModel
) {
    val apiCategories = listOf("ALL", "Resolver", "Symbol", "Diagnostic", "Processor")
    val filteredItems = if (state.selectedApiCategory == "ALL") {
        state.apiChanges
    } else {
        state.apiChanges.filter { it.category == state.selectedApiCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category filter / 分类过滤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            apiCategories.forEach { cat ->
                CategoryChip(
                    label = cat,
                    selected = state.selectedApiCategory == cat,
                    onClick = { viewModel.sendIntent(KSP2MigrationIntent.SelectApiCategory(cat)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // API Changes list / API 变化列表
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems) { item ->
                APIChangeCard(item = item)
            }
        }
    }
}

@Composable
private fun APIChangeCard(item: APIChangeItem) {
    var isExpanded by remember { mutableStateOf(false) }
    val typeColor = when (item.changeType) {
        "REMOVED" -> TerminalRed
        "DEPRECATED" -> TerminalOrange
        "MODIFIED" -> TerminalOrange
        "NEW" -> TerminalGreen
        else -> TerminalCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.apiName, color = TerminalWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "[${item.category}]",
                            color = TerminalCyan,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.changeType,
                        color = typeColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TerminalGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(item.description, color = TerminalGray, fontSize = 12.sp)

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = TerminalBorder)

                    if (item.beforeCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("KSP1 / Before:", color = TerminalRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        CodeBlock(code = item.beforeCode)
                    }

                    if (item.afterCode.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("KSP2 / After:", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        CodeBlock(code = item.afterCode)
                    }

                    if (item.migrationNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TerminalOrange.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text("💡 迁移注意: ", color = TerminalOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(item.migrationNote, color = TerminalOrange, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab 4: CI 合规检测 (CIComplianceTab)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CIComplianceTab(
    state: KSP2MigrationState,
    viewModel: KSP2MigrationViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Compliance overview / 合规概览
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "整体合规状态",
                    color = TerminalGray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.overallCompliance.emoji,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.overallCompliance.displayName,
                    color = state.overallCompliance.color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (state.complianceResults.isNotEmpty()) {
                    val passed = state.complianceResults.count { it.passed }
                    val total = state.complianceResults.size
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$passed / $total 项检查通过",
                        color = TerminalGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Run check button / 运行检测按钮
        Button(
            onClick = { viewModel.sendIntent(KSP2MigrationIntent.RunComplianceCheck) },
            enabled = state.complianceState != ComplianceState.RUNNING,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TerminalCyan,
                contentColor = TerminalBg,
                disabledContainerColor = TerminalCyan.copy(alpha = 0.3f),
                disabledContentColor = TerminalCyan
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (state.complianceState == ComplianceState.RUNNING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = TerminalBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("检测中...", fontFamily = FontFamily.Monospace)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("运行 CI 合规检测", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Compliance results / 合规检测结果列表
        if (state.complianceResults.isNotEmpty()) {
            SectionHeader(title = "检测结果详情", subtitle = "点击查看修复建议")

            Spacer(modifier = Modifier.height(8.dp))

            state.complianceResults.forEach { result ->
                ComplianceResultCard(result = result)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Export report button / 导出报告按钮
            Button(
                onClick = { viewModel.sendIntent(KSP2MigrationIntent.ExportReport) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerminalGreen,
                    contentColor = TerminalBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出合规报告", fontFamily = FontFamily.Monospace)
            }
        } else if (state.complianceState == ComplianceState.IDLE) {
            EmptyStateCard(
                icon = Icons.Default.Check,
                title = "点击上方按钮开始 CI 合规检测",
                subtitle = "检测项目: ksp.useKSP2 配置、Kotlin 版本、Processor 兼容性"
            )
        }
    }
}

@Composable
private fun ComplianceResultCard(result: ComplianceResult) {
    var isExpanded by remember { mutableStateOf(!result.passed) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (result.passed) TerminalGreen.copy(alpha = 0.1f) else TerminalRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.passed) TerminalGreen else TerminalRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(result.checkName, color = TerminalWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "[${result.severity}]",
                            color = when (result.severity) {
                                "P0" -> TerminalRed
                                "P1" -> TerminalOrange
                                else -> TerminalGray
                            },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TerminalGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(result.message, color = if (result.passed) TerminalGreen else TerminalOrange, fontSize = 12.sp)

            AnimatedVisibility(visible = isExpanded && !result.passed) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = TerminalBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💡 修复建议:", color = TerminalCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(result.suggestion, color = TerminalWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared UI Components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            color = TerminalCyan,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        subtitle?.let {
            Text(
                text = it,
                color = TerminalGray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalBg, RoundedCornerShape(6.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code.trimIndent(),
            color = TerminalGreen,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun CompatibilityBadge(compatibility: Compatibility) {
    Box(
        modifier = Modifier
            .background(compatibility.color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${compatibility.emoji} ${compatibility.displayNameCn}",
            color = compatibility.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VersionBadge(label: String, version: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TerminalGray, fontSize = 11.sp)
        Text(version, color = TerminalCyan, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) TerminalCyan else TerminalSurface)
            .border(
                width = 1.dp,
                color = if (selected) TerminalCyan else TerminalBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) TerminalBg else TerminalGray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MatrixSummaryBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(label, color = color, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalSurface),
        shape = RoundedCornerShape(12.dp)
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
                tint = TerminalGray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TerminalGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = TerminalGray.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}