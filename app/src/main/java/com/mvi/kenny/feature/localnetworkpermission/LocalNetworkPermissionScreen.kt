package com.mvi.kenny.feature.localnetworkpermission

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.R
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ============================================================
 * LocalNetworkPermissionScreen — 本地网络权限检测主界面
 * ============================================================
 * PRD-122 | Android 17 ACCESS_LOCAL_NETWORK 运行时权限迁移检测工具包
 *
 * 4-Tab BottomNav 结构：
 * - Dashboard: 整体风险评分 + RadarChart + 影响范围摘要
 * - Scanner: 代码扫描 + 受影响 API 列表
 * - Migration: 权限迁移引导 + 代码 Diff
 * - Settings: 权限状态模拟 + 测试套件生成
 *
 * @param onUpdateTopBar 向 MainScreen 上报 TopBar 配置
 * @see LocalNetworkPermissionViewModel
 */

// ================================================================
// Colors / 颜色规范
// ================================================================
private object LocalNetworkColors {
    val Primary = Color(0xFF1565C0)
    val Error = Color(0xFFD32F2F)
    val Warning = Color(0xFFF57C00)
    val Caution = Color(0xFFFBC02D)
    val Success = Color(0xFF388E3C)
    val Surface = Color(0xFFFAFAFA)
    val Background = Color(0xFFF5F5F5)
    val OnSurface = Color(0xFF212121)
    val CodeBackground = Color(0xFFEEEEEE)
    val RadarFill = Color(0x401565C0)
    val RadarStroke = Color(0xFF1565C0)
}

// ================================================================
// Tab titles / Tab 标题
// ================================================================
private val tabTitles = listOf(
    "Dashboard" to "概览",
    "Scanner" to "扫描",
    "Migration" to "迁移",
    "Settings" to "设置"
)

// ================================================================
// Main Screen / 主界面
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalNetworkPermissionScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val viewModel: LocalNetworkPermissionViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Update TopBar when tab changes / Tab 切换时更新 TopBar
    LaunchedEffect(state.currentTab) {
        onUpdateTopBar(
            TopBarConfig(
                title = "${tabTitles[state.currentTab].second} - ACCESS_LOCAL_NETWORK",
                actions = emptyList()
            )
        )
    }

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LocalNetworkPermissionEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LocalNetworkPermissionEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.text))
                }
                is LocalNetworkPermissionEffect.NavigateToScanner -> {
                    // Navigation handled by parent / 由父组件处理
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row / Tab 切换
            TabRow(
                selectedTabIndex = state.currentTab,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                tabTitles.forEachIndexed { index, (en, zh) ->
                    Tab(
                        selected = state.currentTab == index,
                        onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.SelectTab(index)) },
                        text = { Text(zh, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Tab Content / Tab 内容区
            AnimatedContent(
                targetState = state.currentTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { tab ->
                when (tab) {
                    LocalNetworkPermissionTab.DASHBOARD -> DashboardTab(
                        state = state,
                        viewModel = viewModel
                    )
                    LocalNetworkPermissionTab.SCANNER -> ScannerTab(
                        state = state,
                        viewModel = viewModel
                    )
                    LocalNetworkPermissionTab.MIGRATION -> MigrationTab(
                        state = state,
                        viewModel = viewModel
                    )
                    LocalNetworkPermissionTab.SETTINGS -> SettingsTab(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ================================================================
// Dashboard Tab / 概览 Tab
// ================================================================
@Composable
private fun DashboardTab(
    state: LocalNetworkPermissionState,
    viewModel: LocalNetworkPermissionViewModel
) {
    val radar = state.radarScores
    val overallScore = with(radar) {
        (manifestDeclaration + permissionRequestPath + gracefulDegradation +
                testCoverage + dependencyLibraries + apiCalls) / 6
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Score Card / 整体评分卡片
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score Circle / 评分圆圈
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { overallScore / 100f },
                            modifier = Modifier.size(100.dp),
                            color = when {
                                overallScore >= 70 -> LocalNetworkColors.Success
                                overallScore >= 40 -> LocalNetworkColors.Warning
                                else -> LocalNetworkColors.Error
                            },
                            strokeWidth = 8.dp
                        )
                        Text(
                            text = "$overallScore",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                overallScore >= 70 -> LocalNetworkColors.Success
                                overallScore >= 40 -> LocalNetworkColors.Warning
                                else -> LocalNetworkColors.Error
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column {
                        Text(
                            text = "整体健康度" /* Overall Health Score */,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                overallScore >= 70 -> "风险可控，建议优化" /* Manageable risk, optimize recommended */
                                overallScore >= 40 -> "中等风险，需要关注" /* Medium risk, attention needed */
                                else -> "高风险，阻断发布" /* High risk, blocks release */
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                overallScore >= 70 -> LocalNetworkColors.Success
                                overallScore >= 40 -> LocalNetworkColors.Warning
                                else -> LocalNetworkColors.Error
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.SelectTab(LocalNetworkPermissionTab.SCANNER)) },
                            colors = ButtonDefaults.buttonColors(containerColor = LocalNetworkColors.Primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("立即扫描" /* Start Scan */)
                        }
                    }
                }
            }
        }

        // Radar Chart / 雷达图
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "六维度健康度评分" /* 6-Dimension Health Score */,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RadarChart(
                            scores = radar,
                            modifier = Modifier.size(240.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Radar Legend / 雷达图例
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        RadarLegendItem("Manifest声明", radar.manifestDeclaration, LocalNetworkColors.Primary)
                        RadarLegendItem("权限请求路径", radar.permissionRequestPath, LocalNetworkColors.Warning)
                        RadarLegendItem("降级策略", radar.gracefulDegradation, LocalNetworkColors.Success)
                        RadarLegendItem("测试覆盖", radar.testCoverage, LocalNetworkColors.Caution)
                        RadarLegendItem("依赖库兼容", radar.dependencyLibraries, Color(0xFF7B1FA2))
                        RadarLegendItem("API调用合规", radar.apiCalls, LocalNetworkColors.Error)
                    }
                }
            }
        }

        // Impact Metrics / 影响范围卡片
        item {
            Text(
                text = "影响范围摘要" /* Impact Summary */,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactCard(
                    title = "受影响API",
                    value = "${state.affectedApis.size}",
                    subtitle = "处代码调用",
                    color = LocalNetworkColors.Error,
                    modifier = Modifier.weight(1f)
                )
                val p0Count = state.affectedApis.count { it.severity == Severity.P0 }
                ImpactCard(
                    title = "P0阻断",
                    value = "$p0Count",
                    subtitle = "处需立即修复",
                    color = LocalNetworkColors.Error,
                    modifier = Modifier.weight(1f)
                )
                val p1Count = state.affectedApis.count { it.severity == Severity.P1 }
                ImpactCard(
                    title = "P1严重",
                    value = "$p1Count",
                    subtitle = "处需尽快处理",
                    color = LocalNetworkColors.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val p2Count = state.affectedApis.count { it.severity == Severity.P2 }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImpactCard(
                    title = "P2注意",
                    value = "$p2Count",
                    subtitle = "处建议检查",
                    color = LocalNetworkColors.Caution,
                    modifier = Modifier.weight(1f)
                )
                val modulesCount = state.affectedApis.map { it.filePath.substringAfter("feature/").substringBefore("/") }.distinct().size
                ImpactCard(
                    title = "受影响模块",
                    value = "$modulesCount",
                    subtitle = "个功能模块",
                    color = LocalNetworkColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ================================================================
// Radar Legend Item / 雷达图例项
// ================================================================
@Composable
private fun RadarLegendItem(label: String, score: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(
            "$score/100",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ================================================================
// Impact Card / 影响卡片
// ================================================================
@Composable
private fun ImpactCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    MaterialCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = color)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
    }
}

// ================================================================
// Radar Chart / 雷达图（Canvas 自绘）
// ================================================================
@Composable
private fun RadarChart(
    scores: RadarScores,
    modifier: Modifier = Modifier
) {
    val labels = listOf(
        "Manifest", "权限路径", "降级", "测试", "依赖库", "API合规"
    )
    val values = listOf(
        scores.manifestDeclaration,
        scores.permissionRequestPath,
        scores.gracefulDegradation,
        scores.testCoverage,
        scores.dependencyLibraries,
        scores.apiCalls
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(centerX, centerY) * 0.85f
        val sides = 6

        // Draw grid / 绘制网格（3层同心六边形）
        for (level in 1..3) {
            val radius = maxRadius * level / 3
            val path = Path()
            for (i in 0 until sides) {
                val angle = (i * 2 * PI / sides - PI / 2).toFloat()
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = Color.Gray.copy(alpha = 0.2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw axes / 绘制轴线
        for (i in 0 until sides) {
            val angle = (i * 2 * PI / sides - PI / 2).toFloat()
            val x = centerX + maxRadius * cos(angle)
            val y = centerY + maxRadius * sin(angle)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(centerX, centerY),
                end = Offset(x, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw score area / 绘制分数区域
        val scorePath = Path()
        for (i in 0 until sides) {
            val angle = (i * 2 * PI / sides - PI / 2).toFloat()
            val radius = maxRadius * values[i] / 100f
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (i == 0) scorePath.moveTo(x, y) else scorePath.lineTo(x, y)
        }
        scorePath.close()

        drawPath(
            path = scorePath,
            color = LocalNetworkColors.RadarFill
        )
        drawPath(
            path = scorePath,
            color = LocalNetworkColors.RadarStroke,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw score dots / 绘制分数点
        for (i in 0 until sides) {
            val angle = (i * 2 * PI / sides - PI / 2).toFloat()
            val radius = maxRadius * values[i] / 100f
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            drawCircle(
                color = LocalNetworkColors.RadarStroke,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

// ================================================================
// Scanner Tab / 扫描 Tab
// ================================================================
@Composable
private fun ScannerTab(
    state: LocalNetworkPermissionState,
    viewModel: LocalNetworkPermissionViewModel
) {
    val filteredApis = when (state.severityFilter) {
        Severity.ALL -> state.affectedApis
        else -> state.affectedApis.filter { it.severity == state.severityFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Scan button + progress / 扫描按钮 + 进度
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.StartScan) },
                    enabled = !state.isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = LocalNetworkColors.Primary)
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.isScanning) "扫描中..." else "开始扫描" /* Scanning... / Start Scan */)
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (state.isScanning) {
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${(state.scanProgress * 100).toInt()}%" /* Progress percentage */,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Text(
                        "发现 ${state.affectedApis.size} 处受影响 API" /* Found X affected APIs */,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Severity filter chips / 风险级别过滤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Severity.entries.forEach { severity ->
                FilterChip(
                    selected = state.severityFilter == severity,
                    onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.SetSeverityFilter(severity)) },
                    label = {
                        Text(
                            when (severity) {
                                Severity.ALL -> "全部"
                                Severity.P0 -> "P0阻断"
                                Severity.P1 -> "P1严重"
                                Severity.P2 -> "P2注意"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (severity) {
                            Severity.ALL -> LocalNetworkColors.Primary
                            Severity.P0 -> LocalNetworkColors.Error
                            Severity.P1 -> LocalNetworkColors.Warning
                            Severity.P2 -> LocalNetworkColors.Caution
                        }.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        // Affected API list / 受影响 API 列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredApis) { index, api ->
                AffectedApiCard(
                    api = api,
                    isExpanded = state.expandedApiIndex == index,
                    onToggle = {
                        val globalIndex = state.affectedApis.indexOf(api)
                        viewModel.sendIntent(LocalNetworkPermissionIntent.ToggleApiExpansion(globalIndex))
                    },
                    onCopy = { viewModel.sendIntent(LocalNetworkPermissionIntent.CopyCode(api.codeContext)) }
                )
            }
        }
    }
}

// ================================================================
// Affected API Card / 受影响 API 卡片
// ================================================================
@Composable
private fun AffectedApiCard(
    api: AffectedApi,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    val severityColor = when (api.severity) {
        Severity.P0 -> LocalNetworkColors.Error
        Severity.P1 -> LocalNetworkColors.Warning
        Severity.P2 -> LocalNetworkColors.Caution
        Severity.ALL -> LocalNetworkColors.Primary
    }

    val severityLabel = when (api.severity) {
        Severity.P0 -> "P0 阻断"
        Severity.P1 -> "P1 严重"
        Severity.P2 -> "P2 注意"
        Severity.ALL -> "全部"
    }

    MaterialCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250)),
        colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Severity badge / 风险徽章
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        severityLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        api.apiName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${api.filePath}:${api.lineNumber}" /* File path + line number */,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "折叠" else "展开" /* Collapse / Expand */
                    )
                }
            }

            // Expandable content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "代码上下文" /* Code Context */,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Code block / 代码块
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LocalNetworkColors.CodeBackground, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                api.codeContext,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "修复建议" /* Suggested Fix */,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        api.suggestedFix,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ================================================================
// Migration Tab / 迁移 Tab
// ================================================================
@Composable
private fun MigrationTab(
    state: LocalNetworkPermissionState,
    viewModel: LocalNetworkPermissionViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "选择权限请求路径" /* Select Permission Request Path */,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Permission path cards / 权限路径卡片
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PermissionPathCard(
                    title = "NEARBY_WIFI_DEVICES",
                    subtitle = "临时访问\n90天有效期",
                    description = "适合配网、设备发现等短期场景",
                    isSelected = state.migrationGuide?.path == PermissionRequestPath.NEARBY_WIFI_DEVICES,
                    onClick = {
                        viewModel.sendIntent(LocalNetworkPermissionIntent.ApplyMigration(PermissionRequestPath.NEARBY_WIFI_DEVICES))
                    },
                    modifier = Modifier.weight(1f)
                )

                PermissionPathCard(
                    title = "ACCESS_LOCAL_NETWORK",
                    subtitle = "持久访问\n长期局域网通信",
                    description = "适合智能家居、NAS、视频流等长期连接",
                    isSelected = state.migrationGuide?.path == PermissionRequestPath.ACCESS_LOCAL_NETWORK,
                    onClick = {
                        viewModel.sendIntent(LocalNetworkPermissionIntent.ApplyMigration(PermissionRequestPath.ACCESS_LOCAL_NETWORK))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            PermissionPathCard(
                title = "打印机专用 API",
                subtitle = "System Print\n不需要本地网络权限",
                description = "通过系统打印工作流，无需 ACCESS_LOCAL_NETWORK",
                isSelected = state.migrationGuide?.path == PermissionRequestPath.PRINTER_API,
                onClick = {
                    viewModel.sendIntent(LocalNetworkPermissionIntent.ApplyMigration(PermissionRequestPath.PRINTER_API))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Code diff preview / 代码差异预览
        state.migrationGuide?.let { guide ->
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "代码修改对比" /* Code Modification Diff */,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                CodeDiffCard(
                    title = "修改前 (Before)",
                    code = guide.beforeCode,
                    isAddition = false,
                    onCopy = { viewModel.sendIntent(LocalNetworkPermissionIntent.CopyCode(guide.beforeCode)) }
                )
            }

            item {
                CodeDiffCard(
                    title = "修改后 (After)",
                    code = guide.afterCode,
                    isAddition = true,
                    onCopy = { viewModel.sendIntent(LocalNetworkPermissionIntent.CopyCode(guide.afterCode)) }
                )
            }

            item {
                Text(
                    "AndroidManifest.xml 变更" /* AndroidManifest Changes */,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeDiffCard(
                    title = "Manifest Diff",
                    code = guide.manifestDiff,
                    isAddition = true,
                    onCopy = { viewModel.sendIntent(LocalNetworkPermissionIntent.CopyCode(guide.manifestDiff)) }
                )
            }
        }
    }
}

// ================================================================
// Permission Path Card / 权限路径卡片
// ================================================================
@Composable
private fun PermissionPathCard(
    title: String,
    subtitle: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MaterialCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LocalNetworkColors.Primary.copy(alpha = 0.1f)
            else LocalNetworkColors.Surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, LocalNetworkColors.Primary) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = LocalNetworkColors.Primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LocalNetworkColors.Primary else LocalNetworkColors.OnSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ================================================================
// Code Diff Card / 代码差异卡片
// ================================================================
@Composable
private fun CodeDiffCard(
    title: String,
    code: String,
    isAddition: Boolean,
    onCopy: () -> Unit
) {
    val bgColor = if (isAddition) LocalNetworkColors.Success.copy(alpha = 0.1f)
    else LocalNetworkColors.Error.copy(alpha = 0.1f)
    val borderColor = if (isAddition) LocalNetworkColors.Success
    else LocalNetworkColors.Error

    MaterialCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(borderColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = borderColor
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalNetworkColors.CodeBackground, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            )
        }
    }
}

// ================================================================
// Settings Tab / 设置 Tab
// ================================================================
@Composable
private fun SettingsTab(
    state: LocalNetworkPermissionState,
    viewModel: LocalNetworkPermissionViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Permission State Selector / 权限状态选择
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "权限状态模拟" /* Permission State Simulation */,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionState.entries.forEach { permState ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.sendIntent(LocalNetworkPermissionIntent.SelectPermissionState(permState))
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedPermissionState == permState,
                                onClick = {
                                    viewModel.sendIntent(LocalNetworkPermissionIntent.SelectPermissionState(permState))
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = LocalNetworkColors.Primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    when (permState) {
                                        PermissionState.GRANTED -> "已授权 (Granted)"
                                        PermissionState.DENIED -> "已拒绝 (Denied)"
                                        PermissionState.PERMANENTLY_DENIED -> "永久拒绝 (Permanently Denied)"
                                        PermissionState.NOT_ASKED -> "未询问 (Not Asked)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (state.selectedPermissionState == permState) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    when (permState) {
                                        PermissionState.GRANTED -> "用户已授权本地网络访问，应用正常运行"
                                        PermissionState.DENIED -> "用户拒绝授权，访问被阻断"
                                        PermissionState.PERMANENTLY_DENIED -> "用户永久拒绝，需要引导至系统设置"
                                        PermissionState.NOT_ASKED -> "尚未向用户请求权限"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Test Suite Generator / 测试套件生成
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "测试套件生成" /* Test Suite Generator */,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "为当前权限状态生成对应的测试用例代码" /* Generate test cases for current permission state */,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.GenerateTestSuite) },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalNetworkColors.Primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("生成测试用例" /* Generate Test Cases */)
                    }
                }
            }
        }

        // Test code preview / 测试代码预览
        state.testSuiteCode?.let { code ->
            item {
                Text(
                    "生成的测试代码" /* Generated Test Code */,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeDiffCard(
                    title = "Test Suite",
                    code = code,
                    isAddition = true,
                    onCopy = { viewModel.sendIntent(LocalNetworkPermissionIntent.CopyCode(code)) }
                )
            }
        }

        // Export Report / 导出报告
        item {
            MaterialCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LocalNetworkColors.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "导出迁移报告" /* Export Migration Report */,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "导出完整的 Markdown 格式迁移检测报告" /* Export complete migration report in Markdown */,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.sendIntent(LocalNetworkPermissionIntent.ExportReport) },
                        colors = ButtonDefaults.buttonColors(containerColor = LocalNetworkColors.Success)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("导出 Markdown 报告" /* Export Markdown Report */)
                    }
                }
            }
        }

        // Exported report preview / 报告预览
        state.exportedReport?.let { report ->
            item {
                Text(
                    "报告预览" /* Report Preview */,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(LocalNetworkColors.CodeBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        report,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
