package com.mvi.kenny.feature.systemloadcompliance

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * SystemLoadComplianceScreen — Android 17 System.load() 合规检测工具主界面
 * ============================================================
 * 7-Tab layout: Overview, Scanner, Marking, Migration, RootCause, Engine, CI
 *
 * @param viewModel ViewModel for state management / 状态管理的 ViewModel
 * @param onNavigateBack Callback when user wants to go back / 用户返回时的回调
 *
 * @see SystemLoadComplianceContract State/Intent/Effect definitions
 * @see SystemLoadComplianceViewModel ViewModel implementation
 */

// =============================================================
// Color Palette (from PRD-167 design spec)
// =============================================================
private val PrimaryOrange = Color(0xFFFF6B35)
private val SecondaryBlue = Color(0xFF00D9FF)
private val CompliantGreen = Color(0xFF4CAF50)
private val NonCompliantRed = Color(0xFFF44336)
private val WarningOrange = Color(0xFFFF9800)
private val CodeBackground = Color(0xFF1A1A2E)
private val SurfaceDark = Color(0xFF121212)

// =============================================================
// Tab Definitions
// =============================================================
private data class ComplianceTab(
    val title: String,
    val titleZh: String,
    val icon: ImageVector
)

// =============================================================
// Main Screen
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemLoadComplianceScreen(
    viewModel: SystemLoadComplianceViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val tabs = listOf(
        ComplianceTab("Overview", "概览", Icons.Default.Shield),
        ComplianceTab("Scanner", "扫描器", Icons.Default.Search),
        ComplianceTab("Marking", "标记生成", Icons.Default.Settings),
        ComplianceTab("Migration", "迁移指南", Icons.Default.Android),
        ComplianceTab("Root Cause", "根因分析", Icons.Default.BugReport),
        ComplianceTab("Engine", "引擎检测", Icons.Default.PlayArrow),
        ComplianceTab("CI Pipeline", "CI流水线", Icons.Default.Code)
    )

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SystemLoadComplianceEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SystemLoadComplianceEffect.ShowError -> {
                    snackbarHostState.showSnackbar("Error: ${effect.message}")
                }
                is SystemLoadComplianceEffect.CopyToClipboard -> {
                    // In production, use ClipboardManager
                    Toast.makeText(context, "Copied: ${effect.label}", Toast.LENGTH_SHORT).show()
                }
                is SystemLoadComplianceEffect.OpenFileLocation -> {
                    // In production, use Intent.ACTION_OPEN_DOCUMENT
                }
                is SystemLoadComplianceEffect.ShowScanComplete -> {
                    snackbarHostState.showSnackbar(
                        "Scan complete: ${effect.passedCount} passed, ${effect.failedCount} issues found"
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = SurfaceDark,
                contentColor = PrimaryOrange
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectTab(index)) },
                        text = {
                            Text(
                                text = tab.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (state.selectedTab) {
                0 -> OverviewTab(state, viewModel)
                1 -> ScannerTab(state, viewModel)
                2 -> MarkingTab(state, viewModel)
                3 -> MigrationTab(state, viewModel)
                4 -> RootCauseTab(state, viewModel)
                5 -> EngineTab(state, viewModel)
                6 -> CIPipelineTab(state, viewModel)
            }
        }
    }
}

// =============================================================
// Tab 0: Overview / 概览
// =============================================================
@Composable
private fun OverviewTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Compliance Score Card / 合规评分卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Android 17 合规评分",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ComplianceScoreGauge(
                        score = state.complianceScore,
                        modifier = Modifier.size(160.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when {
                            state.complianceScore >= 90 -> "✅ 优秀 — 基本合规"
                            state.complianceScore >= 70 -> "⚠️ 良好 — 存在改进空间"
                            state.complianceScore >= 50 -> "⚠️ 一般 — 需要关注"
                            else -> "❌ 不合规 — 需要立即处理"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            state.complianceScore >= 90 -> CompliantGreen
                            state.complianceScore >= 70 -> WarningOrange
                            else -> NonCompliantRed
                        }
                    )
                }
            }
        }

        // Stats Row / 统计行
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "已扫描",
                    value = "${state.scannedFilesCount}",
                    subtitle = "源文件",
                    color = SecondaryBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "不合规",
                    value = "${state.nonCompliantCallsCount}",
                    subtitle = "调用",
                    color = NonCompliantRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "已标记",
                    value = "${state.markedSoCount}",
                    subtitle = ".so 文件",
                    color = CompliantGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions / 快速入口
        item {
            Text(
                text = "快速入口",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "扫描代码",
                    subtitle = "检测 System.load() 调用",
                    icon = Icons.Default.Search,
                    color = PrimaryOrange,
                    onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectTab(1)) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "生成标记",
                    subtitle = "创建 read-only 代码",
                    icon = Icons.Default.Settings,
                    color = SecondaryBlue,
                    onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectTab(2)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "迁移指南",
                    subtitle = "插件化框架迁移",
                    icon = Icons.Default.Android,
                    color = CompliantGreen,
                    onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectTab(3)) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "CI 配置",
                    subtitle = "GitHub/GitLab CI",
                    icon = Icons.Default.Code,
                    color = WarningOrange,
                    onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectTab(6)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Framework Tags / 受影响框架标签
        if (state.nonCompliantCalls.isNotEmpty()) {
            item {
                Text(
                    text = "受影响框架",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FrameworkTag("VirtualAPK", NonCompliantRed)
                    FrameworkTag("RePlugin", WarningOrange)
                    FrameworkTag("Unity", NonCompliantRed)
                    FrameworkTag("DynamicLoad", WarningOrange)
                }
            }
        }
    }
}

// =============================================================
// Tab 1: Scanner / 扫描器
// =============================================================
@Composable
private fun ScannerTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scan Configuration / 扫描配置区
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "扫描配置",
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var sourceDir by remember { mutableStateOf(state.scanSourceDir) }
                    OutlinedTextField(
                        value = sourceDir,
                        onValueChange = { sourceDir = it },
                        label = { Text("源目录") },
                        placeholder = { Text("app/src/main/java") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = PrimaryOrange,
                            cursorColor = PrimaryOrange
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.sendIntent(SystemLoadComplianceIntent.StartScan(sourceDir))
                            },
                            enabled = state.scanStatus == ScanStatus.IDLE || state.scanStatus == ScanStatus.ERROR,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (state.scanStatus == ScanStatus.SCANNING) "扫描中..." else "开始扫描")
                        }
                        if (state.scanStatus == ScanStatus.SCANNING) {
                            Button(
                                onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.CancelScan) },
                                colors = ButtonDefaults.buttonColors(containerColor = NonCompliantRed)
                            ) {
                                Text("取消")
                            }
                        }
                    }
                }
            }
        }

        // Progress Panel / 进度面板
        if (state.scanStatus == ScanStatus.SCANNING || state.scanStatus == ScanStatus.ANALYZING) {
            item {
                ScanProgressBar(
                    progress = state.scanProgress,
                    status = state.scanStatus,
                    filesCount = state.scannedFilesCount
                )
            }
        }

        // Non-compliant Calls List / 不合规调用列表
        if (state.nonCompliantCalls.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "不合规调用 (${state.nonCompliantCalls.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = NonCompliantRed
                    )
                    Button(
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.ApplyFixAll) },
                        colors = ButtonDefaults.buttonColors(containerColor = CompliantGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("一键修复", fontSize = 12.sp)
                    }
                }
            }

            items(state.nonCompliantCalls) { call ->
                NonCompliantCallCard(
                    callLocation = call,
                    onFix = { viewModel.sendIntent(SystemLoadComplianceIntent.ApplyFix(call)) }
                )
            }
        }

        // Empty state / 空状态
        if (state.scanStatus == ScanStatus.COMPLETED && state.nonCompliantCalls.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CompliantGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CompliantGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "✅ 所有 System.load() 调用均合规",
                            style = MaterialTheme.typography.titleMedium,
                            color = CompliantGreen
                        )
                        Text(
                            text = "未检测到违反 Android 17 read-only 约束的调用",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Tab 2: Marking / 标记生成器
// =============================================================
@Composable
private fun MarkingTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // .so File List / .so 文件列表
        item {
            Text(
                text = ".so 文件列表 (${state.soFiles.size})",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }

        items(state.soFiles) { soFile ->
            SoFileListItem(
                soFile = soFile,
                markingMethod = state.markingMethod,
                onApplyMarking = { viewModel.sendIntent(SystemLoadComplianceIntent.ApplyMarking(soFile)) }
            )
        }

        // Marking Method Selection / 标记方式选择
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "标记方式",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MarkingMethod.entries.forEach { method ->
                    MarkingMethodCard(
                        method = method,
                        isSelected = state.markingMethod == method,
                        onSelect = { viewModel.sendIntent(SystemLoadComplianceIntent.SelectMarkingMethod(method)) }
                    )
                }
            }
        }

        // Code Preview / 代码预览
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "代码预览",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                IconButton(
                    onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.CopyCIConfig) }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SecondaryBlue)
                }
            }
        }

        item {
            MarkingCodePreview(
                code = state.markingCode,
                markingMethod = state.markingMethod
            )
        }
    }
}

// =============================================================
// Tab 3: Migration / 插件化迁移指南
// =============================================================
@Composable
private fun MigrationTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Framework Selector / 框架选择器
        item {
            Text(
                text = "选择插件化框架",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Framework.entries.filter { it != Framework.NONE }.forEachIndexed { index, framework ->
                    SegmentedButton(
                        selected = state.selectedFramework == framework,
                        onClick = {
                            viewModel.sendIntent(SystemLoadComplianceIntent.SelectFramework(framework))
                            viewModel.sendIntent(SystemLoadComplianceIntent.GenerateMigrationGuide(framework))
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Framework.entries.size - 1
                        ),
                        icon = {}
                    ) {
                        Text(framework.label, fontSize = 11.sp)
                    }
                }
            }
        }

        // Migration Guide / 迁移指南
        state.migrationGuide?.let { guide ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "迁移指南: ${guide.framework.label}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryOrange
                        )
                        Text(
                            text = "复杂度: ${"★".repeat(guide.framework.migrationComplexity)}${"☆".repeat(5 - guide.framework.migrationComplexity)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Migration Steps / 迁移步骤
            items(guide.steps) { step ->
                MigrationStepCard(step = step)
            }

            // Key Changes / 关键变更
            item {
                Text(
                    text = "关键变更",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(guide.keyChanges) { change ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", color = CompliantGreen)
                    Text(change, color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Warnings / 警告
            if (guide.warnings.isNotEmpty()) {
                item {
                    Text(
                        text = "注意事项",
                        style = MaterialTheme.typography.titleSmall,
                        color = WarningOrange,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(guide.warnings) { warning ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚠️ ", color = WarningOrange)
                        Text(warning, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Empty state / 空状态
        if (state.migrationGuide == null && state.selectedFramework == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Android,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "选择上方框架以生成迁移指南",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Tab 4: Root Cause / 根因分析
// =============================================================
@Composable
private fun RootCauseTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Crash Log Input / 崩溃日志输入
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UnsatisfiedLinkError 根因分析",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var crashLog by remember { mutableStateOf(state.crashLogInput) }
                    OutlinedTextField(
                        value = crashLog,
                        onValueChange = {
                            crashLog = it
                            viewModel.sendIntent(SystemLoadComplianceIntent.InputCrashLog(it))
                        },
                        label = { Text("粘贴 UnsatisfiedLinkError 崩溃日志") },
                        placeholder = { Text("java.lang.UnsatisfiedLinkError: ...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = PrimaryOrange,
                            cursorColor = PrimaryOrange
                        ),
                        maxLines = 8
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.AnalyzeRootCause) },
                        enabled = crashLog.isNotEmpty() && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("分析根因")
                    }
                }
            }
        }

        // Root Cause Result / 根因分析结果
        state.rootCauseResult?.let { result ->
            item {
                RootCauseResultCard(result = result)
            }
        }
    }
}

// =============================================================
// Tab 5: Game Engine / 游戏引擎检测
// =============================================================
@Composable
private fun EngineTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Engine Selection / 引擎选择
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "游戏引擎合规检测",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryOrange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameEngine.entries.forEach { engine ->
                            FilterChip(
                                selected = state.selectedEngine == engine,
                                onClick = {
                                    viewModel.sendIntent(SystemLoadComplianceIntent.SelectEngine(engine))
                                },
                                label = { Text(engine.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    var version by remember { mutableStateOf(state.engineVersion) }
                    OutlinedTextField(
                        value = version,
                        onValueChange = {
                            version = it
                            viewModel.sendIntent(SystemLoadComplianceIntent.InputEngineVersion(it))
                        },
                        label = { Text("引擎版本") },
                        placeholder = { Text(state.selectedEngine.defaultVersion) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryOrange,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = PrimaryOrange,
                            cursorColor = PrimaryOrange
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.DetectEngineCompliance) },
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("检测合规性")
                    }
                }
            }
        }

        // Engine Result / 引擎检测结果
        state.engineResult?.let { result ->
            item {
                EngineComplianceResultCard(result = result)
            }
        }
    }
}

// =============================================================
// Tab 6: CI Pipeline / CI 流水线
// =============================================================
@Composable
private fun CIPipelineTab(state: SystemLoadComplianceState, viewModel: SystemLoadComplianceViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CI Platform Selection / CI 平台选择
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CI 合规流水线",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryOrange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CIPlatform.entries.forEach { platform ->
                            FilterChip(
                                selected = state.ciPlatform == platform,
                                onClick = {
                                    viewModel.sendIntent(SystemLoadComplianceIntent.SelectCIPlatform(platform))
                                    viewModel.sendIntent(SystemLoadComplianceIntent.GenerateCIConfig)
                                },
                                label = { Text(platform.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.GenerateCIConfig) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成配置")
                    }
                }
            }
        }

        // CI Config Preview / CI 配置预览
        if (state.ciConfig.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.ciPlatform.label} 配置",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { viewModel.sendIntent(SystemLoadComplianceIntent.CopyCIConfig) }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SecondaryBlue)
                    }
                }
            }

            item {
                CIMessagePreview(config = state.ciConfig)
            }
        }
    }
}

// =============================================================
// Reusable Components
// =============================================================

/**
 * Compliance Score Gauge / 合规评分仪表盘
 *
 * Animated circular gauge showing 0-100 compliance score.
 */
@Composable
private fun ComplianceScoreGauge(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    val sweepAngle = (animatedScore / 100f) * 360f
    val gaugeColor = when {
        score >= 90 -> CompliantGreen
        score >= 70 -> WarningOrange
        else -> NonCompliantRed
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )

            // Background arc / 背景弧
            drawArc(
                color = Color.Gray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Score arc / 评分弧
            drawArc(
                color = gaugeColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Text(
                text = "分",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Stat Card / 统计卡片
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Quick Action Card / 快速操作卡片
 */
@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Framework Tag / 框架标签
 */
@Composable
private fun FrameworkTag(name: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * Scan Progress Bar / 扫描进度条
 */
@Composable
private fun ScanProgressBar(
    progress: Float,
    status: ScanStatus,
    filesCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryOrange
                )
                Text(
                    text = "$filesCount files",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryOrange,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * Non-compliant Call Card / 不合规调用卡片
 */
@Composable
private fun NonCompliantCallCard(
    callLocation: LoadCallLocation,
    onFix: () -> Unit
) {
    val riskColor = when (callLocation.callType.riskLevel) {
        3 -> NonCompliantRed
        2 -> WarningOrange
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(riskColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = callLocation.libName,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${callLocation.className}.${callLocation.methodName}():${callLocation.lineNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = callLocation.filePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = callLocation.callType.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = riskColor
                        )
                    }
                    if (callLocation.isDynamicConcat) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(NonCompliantRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚠️ DYNAMIC",
                                style = MaterialTheme.typography.labelSmall,
                                color = NonCompliantRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Raw code snippet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = callLocation.rawCode,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = SecondaryBlue,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fix suggestion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = callLocation.fixSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onFix) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CompliantGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修复", color = CompliantGreen, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * SoFile List Item / .so 文件列表项
 */
@Composable
private fun SoFileListItem(
    soFile: SoFileInfo,
    markingMethod: MarkingMethod,
    onApplyMarking: () -> Unit
) {
    val statusColor = soFile.complianceStatus.color

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = when (soFile.complianceStatus) {
                    ComplianceStatus.COMPLIANT -> Icons.Default.CheckCircle
                    ComplianceStatus.NON_COMPLIANT -> Icons.Default.Error
                    ComplianceStatus.WARNING -> Icons.Default.Warning
                    ComplianceStatus.UNKNOWN -> Icons.Default.Search
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = soFile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = "${soFile.architecture} • ${soFile.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (soFile.isReadOnlyMarked && soFile.markingMethod != null) {
                    Text(
                        text = "✓ ${soFile.markingMethod.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CompliantGreen
                    )
                }
            }

            if (!soFile.isReadOnlyMarked) {
                TextButton(
                    onClick = onApplyMarking,
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryOrange)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("标记", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Marking Method Card / 标记方式卡片
 */
@Composable
private fun MarkingMethodCard(
    method: MarkingMethod,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryOrange.copy(alpha = 0.15f) else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryOrange) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = method.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) PrimaryOrange else Color.White
                    )
                    if (method.isRecommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(CompliantGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("✅ 推荐", style = MaterialTheme.typography.labelSmall, color = CompliantGreen)
                        }
                    }
                }
                Text(
                    text = method.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryOrange)
            }
        }
    }
}

/**
 * Marking Code Preview / 标记代码预览
 */
@Composable
private fun MarkingCodePreview(code: String, markingMethod: MarkingMethod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CodeBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = markingMethod.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryBlue
                )
                Text(
                    text = "Kotlin",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
                    .background(CodeBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = SecondaryBlue
                )
            }
        }
    }
}

/**
 * Migration Step Card / 迁移步骤卡片
 */
@Composable
private fun MigrationStepCard(step: MigrationStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(PrimaryOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.stepNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (step.codeSnippet.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CodeBackground, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = step.codeSnippet,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = SecondaryBlue,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Root Cause Result Card / 根因分析结果卡片
 */
@Composable
private fun RootCauseResultCard(result: RootCauseResult) {
    val borderColor = if (result.isRootCauseFound) NonCompliantRed else WarningOrange

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.isRootCauseFound) Icons.Default.BugReport else Icons.Default.Warning,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (result.isRootCauseFound) "根因已确定" else "根因未明",
                    style = MaterialTheme.typography.titleMedium,
                    color = borderColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.rootCause,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )

            if (result.fixRecommendation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CompliantGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "修复建议",
                            style = MaterialTheme.typography.labelMedium,
                            color = CompliantGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.fixRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }

            if (result.affectedCallLocations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "受影响位置 (${result.affectedCallLocations.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarningOrange
                )
                Spacer(modifier = Modifier.height(4.dp))
                result.affectedCallLocations.take(3).forEach { location ->
                    Text(
                        text = "• ${location.className}.${location.methodName}():${location.lineNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Engine Compliance Result Card / 游戏引擎合规结果卡片
 */
@Composable
private fun EngineComplianceResultCard(result: EngineComplianceResult) {
    val statusColor = result.complianceStatus.color

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = result.engine.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "v${result.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = result.complianceStatus.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                }
            }

            if (result.detectedIssues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "检测到的问题 (${result.detectedIssues.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = NonCompliantRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                result.detectedIssues.forEach { issue ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", color = NonCompliantRed)
                        Text(issue, style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            }

            if (result.isUpgradeRequired) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningOrange.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "建议升级到: ${result.recommendedVersion}",
                            style = MaterialTheme.typography.titleSmall,
                            color = WarningOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.fixGuide,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * CI Message Preview / CI 配置预览
 */
@Composable
private fun CIMessagePreview(config: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CodeBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "YAML",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryBlue
                )
                Text(
                    text = "${config.lines().size} lines",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
                    .background(CodeBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = config,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = SecondaryBlue
                )
            }
        }
    }
}
