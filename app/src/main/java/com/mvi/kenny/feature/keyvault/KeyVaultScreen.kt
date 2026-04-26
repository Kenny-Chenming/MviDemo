package com.mvi.kenny.feature.keyvault

// ================================================================
// KeyVaultScreen — Android 17 Key Limit 合规检测工具主界面
// ================================================================
// Main screen for Android 17 Key Limit compliance detection & optimization toolkit.
//
// PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
// Design Reference: memory/agency/designs/PRD-171-Android-17-Key-Limit-Key-Value-Storage-Tools.md
//
// Features:
//   0. Overview Tab — Dashboard with key count gauge, trend chart, risk level
//   1. Key Scanner Tab — Scan all SP/DataStore instances
//   2. Contribution Analysis Tab — Key breakdown by module
//   3. Key Merger Tab — Merge suggestions with diff viewer
//   4. Migration Tab — SP → DataStore migration code generator
//   5. CI Monitor Tab — CI build history and threshold config
//   6. Degradation Strategies Tab — Strategy templates
//   7. Best Practices Tab — Storage best practices guide
// —————————————————————————————————————————————————————

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ================================================================
// KeyVault Storage Theme Colors
// ================================================================

private val KeyVaultPrimary = Color(0xFFFFD700)      // 金色/黄铜
private val KeyVaultSecondary = Color(0xFF708090)    // 石板灰
private val KeyVaultTertiary = Color(0xFFCD853F)    // 秘鲁色/古铜色
private val KeyVaultBackground = Color(0xFF0D1117)  // 深黑
private val KeyVaultSurface = Color(0xFF161B22)      // 深灰
private val KeyVaultError = Color(0xFFF85149)       // 红色
private val KeyVaultSuccess = Color(0xFF3FB950)     // 绿色
private val MonoWhite = Color(0xFFE0E0E0)
private val SecondaryText = Color(0xFFB0B0B0)

// ================================================================
// Tab definitions
// ================================================================

private enum class KeyVaultTab(val title: String, val icon: ImageVector) {
    Overview("首页概览", Icons.Default.Home),
    KeyScanner("KeyScanner", Icons.Default.Key),
    Contribution("贡献分析", Icons.Default.Analytics),
    KeyMerger("KeyMerger", Icons.Default.MergeType),
    Migration("Migration", Icons.Default.SwapHoriz),
    CIMonitor("CIMonitor", Icons.Default.History),
    Degradation("降级策略", Icons.Default.Security),
    BestPractices("最佳实践", Icons.Default.AutoAwesome)
}

// ================================================================
// KeyVaultScreen — 主入口
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyVaultScreen(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(state.selectedTab) }
    val tabs = KeyVaultTab.entries.toTypedArray()

    LaunchedEffect(state.selectedTab) {
        selectedTab = state.selectedTab
    }

    Scaffold(
        containerColor = KeyVaultBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = KeyVaultSurface,
                contentColor = KeyVaultPrimary,
                modifier = Modifier.height(48.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            onIntent(KeyVaultIntent.SelectTab(index))
                        },
                        text = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Tab content
            when (tabs[selectedTab]) {
                KeyVaultTab.Overview -> OverviewTab(state = state, onIntent = onIntent)
                KeyVaultTab.KeyScanner -> KeyScannerTab(state = state, onIntent = onIntent)
                KeyVaultTab.Contribution -> ContributionTab(state = state, onIntent = onIntent)
                KeyVaultTab.KeyMerger -> KeyMergerTab(state = state, onIntent = onIntent)
                KeyVaultTab.Migration -> MigrationTab(state = state, onIntent = onIntent)
                KeyVaultTab.CIMonitor -> CIMonitorTab(state = state, onIntent = onIntent)
                KeyVaultTab.Degradation -> DegradationTab(state = state, onIntent = onIntent)
                KeyVaultTab.BestPractices -> BestPracticesTab(state = state)
            }
        }
    }
}

// ================================================================
// Tab 0: Overview — 首页概览
// ================================================================

@Composable
private fun OverviewTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Key Count Gauge Card
        item {
            KeyCountGaugeCard(state = state)
        }

        // Risk Level Card
        item {
            RiskLevelCard(state = state)
        }

        // Key Trend Chart
        item {
            KeyTrendChartCard(state = state, onIntent = onIntent)
        }

        // Quick Actions
        item {
            QuickActionsCard(onIntent = onIntent, state = state)
        }

        // Top Contributors Summary
        item {
            TopContributorsSummaryCard(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun KeyCountGaugeCard(state: KeyVaultState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Key 总量监控",
                style = MaterialTheme.typography.titleMedium,
                color = MonoWhite,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Android 17 上限: 200,000",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Gauge
            KeyCountGauge(
                keyCount = state.totalKeyCount,
                limit = 200_000,
                riskLevel = state.riskLevel,
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formatKeyCount(state.totalKeyCount),
                style = MaterialTheme.typography.headlineMedium,
                color = state.riskLevel.color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "当前 Key 数量",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.riskPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = state.riskLevel.color,
                trackColor = Color(0xFF2A2A2A),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.1f".format(state.riskPercentage)}% of 200,000",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun KeyCountGauge(
    keyCount: Int,
    limit: Int,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier
) {
    val percent = (keyCount.toFloat() / limit).coerceIn(0f, 1f)
    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 800),
        label = "key_gauge"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Background arc
        drawArc(
            color = Color(0xFF2A2A2A),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = riskLevel.color,
            startAngle = 135f,
            sweepAngle = 270f * animatedPercent,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun RiskLevelCard(state: KeyVaultState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "风险等级",
                    style = MaterialTheme.typography.titleSmall,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when (state.riskLevel) {
                        RiskLevel.SAFE -> "Key 数量在安全范围内"
                        RiskLevel.CAUTION -> "Key 数量接近上限，建议优化"
                        RiskLevel.DANGER -> "Key 数量接近危险阈值！"
                        RiskLevel.CRITICAL -> "⚠️ Key 数量严重超标！"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.riskLevel.emoji,
                    fontSize = 32.sp
                )
                Text(
                    text = state.riskLevel.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = state.riskLevel.color,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun KeyTrendChartCard(state: KeyVaultState, onIntent: (KeyVaultIntent) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Key 数量趋势",
                        style = MaterialTheme.typography.titleSmall,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "最近 30 天",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                IconButton(onClick = { onIntent(KeyVaultIntent.RefreshData) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = KeyVaultPrimary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (state.keyTrendHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无趋势数据", color = SecondaryText, fontFamily = FontFamily.Monospace)
                }
            } else {
                KeyTrendLineChart(
                    dataPoints = state.keyTrendHistory,
                    limit = 200_000,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }
    }
}

@Composable
private fun KeyTrendLineChart(
    dataPoints: List<KeyTrendPoint>,
    limit: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas

        val maxKey = dataPoints.maxOf { it.keyCount }.toFloat()
        val minKey = dataPoints.minOf { it.keyCount }.toFloat()
        val range = (maxKey - minKey).coerceAtLeast(1f)

        val padding = 8.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        // Draw limit line
        val limitY = padding + chartHeight * (1 - (limit - minKey) / range)
        drawLine(
            color = KeyVaultError.copy(alpha = 0.5f),
            start = Offset(padding, limitY),
            end = Offset(size.width - padding, limitY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw threshold zone (80%)
        val warnY = padding + chartHeight * (1 - (limit * 0.8f - minKey) / range)
        drawLine(
            color = KeyVaultPrimary.copy(alpha = 0.3f),
            start = Offset(padding, warnY),
            end = Offset(size.width - padding, warnY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw curve
        val path = Path()
        dataPoints.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val normalizedY = (point.keyCount - minKey) / range
            val y = padding + chartHeight * (1 - normalizedY)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = KeyVaultPrimary,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun QuickActionsCard(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快捷操作",
                style = MaterialTheme.typography.titleSmall,
                color = MonoWhite,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(KeyVaultIntent.StartScan) },
                    colors = ButtonDefaults.buttonColors(containerColor = KeyVaultPrimary),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    enabled = !state.isScanning
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = KeyVaultBackground)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("立即扫描", fontSize = 12.sp, color = KeyVaultBackground)
                }
                Button(
                    onClick = { onIntent(KeyVaultIntent.ExportCIConfig) },
                    colors = ButtonDefaults.buttonColors(containerColor = KeyVaultSecondary),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导出 CI", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { onIntent(KeyVaultIntent.RefreshData) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("刷新", fontSize = 12.sp)
                }
            }

            // Scanning progress
            if (state.isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = KeyVaultPrimary,
                    trackColor = Color(0xFF2A2A2A),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "正在扫描: ${state.currentScanModule}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun TopContributorsSummaryCard(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top 模块贡献者",
                    style = MaterialTheme.typography.titleSmall,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = { onIntent(KeyVaultIntent.SelectTab(2)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("查看全部 →", fontSize = 12.sp, color = KeyVaultPrimary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            state.moduleContributions.take(3).forEach { contribution ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contribution.moduleName.substringAfterLast("."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MonoWhite,
                            fontFamily = FontFamily.Monospace
                        )
                        LinearProgressIndicator(
                            progress = { contribution.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = KeyVaultPrimary,
                            trackColor = Color(0xFF2A2A2A),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatKeyCount(contribution.keyCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = KeyVaultPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.1f".format(contribution.percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 1: Key Scanner — 扫描器
// ================================================================

@Composable
private fun KeyScannerTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SP / DataStore 实例扫描",
                        style = MaterialTheme.typography.titleMedium,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "发现 ${state.spInstances.size} 个实例，共 ${formatKeyCount(state.totalKeyCount)} Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                if (state.isScanning) {
                    IconButton(onClick = { onIntent(KeyVaultIntent.StopScan) }) {
                        Icon(Icons.Default.Stop, contentDescription = "停止扫描", tint = KeyVaultError)
                    }
                } else {
                    Button(
                        onClick = { onIntent(KeyVaultIntent.StartScan) },
                        colors = ButtonDefaults.buttonColors(containerColor = KeyVaultPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = KeyVaultBackground)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始扫描", fontSize = 12.sp, color = KeyVaultBackground)
                    }
                }
            }
        }

        if (state.isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "正在扫描: ${state.currentScanModule}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KeyVaultPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.scanProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = KeyVaultPrimary,
                            trackColor = Color(0xFF2A2A2A),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(state.scanProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (state.spInstances.isEmpty() && !state.isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无扫描结果",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击「开始扫描」扫描所有 SP/DataStore 实例",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }
            }
        }

        items(state.spInstances) { instance ->
            SPInstanceCard(
                instance = instance,
                isExpanded = state.expandedInstancePath == instance.path,
                onToggleExpand = { onIntent(KeyVaultIntent.ToggleExpandInstance(instance.path)) }
            )
        }
    }
}

@Composable
private fun SPInstanceCard(
    instance: SPInstance,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val typeColor = when (instance.type) {
        "SharedPreferences" -> KeyVaultPrimary
        "DataStore-Preferences" -> KeyVaultSuccess
        "DataStore-Proto" -> KeyVaultSecondary
        else -> KeyVaultSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (instance.isOverLimit) KeyVaultError.copy(alpha = 0.1f) else KeyVaultSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = instance.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MonoWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        if (instance.isOverLimit) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ 超额",
                                style = MaterialTheme.typography.bodySmall,
                                color = KeyVaultError,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = instance.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatKeyCount(instance.keyCount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (instance.isOverLimit) KeyVaultError else KeyVaultPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Key",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypeChip(type = instance.type, color = typeColor)
                    Text(
                        text = instance.module.substringAfterLast("."),
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Visibility else Icons.Default.Visibility,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = KeyVaultPrimary
                    )
                }
            }

            // Expanded key list
            if (isExpanded && instance.keys.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Key 列表 (${instance.keys.size} 个示例)",
                        style = MaterialTheme.typography.bodySmall,
                        color = KeyVaultPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    instance.keys.take(20).forEach { key ->
                        Text(
                            text = "• $key",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                    if (instance.keys.size > 20) {
                        Text(
                            text = "... 等等，共 ${instance.keys.size} 个 Key",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(type: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
}

// ================================================================
// Tab 2: Contribution — 贡献分析
// ================================================================

@Composable
private fun ContributionTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "模块 Key 贡献分析",
                        style = MaterialTheme.typography.titleMedium,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "按模块分解 Key 来源分布",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Box {
                    OutlinedButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(state.sortMode.displayName, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.displayName) },
                                onClick = {
                                    onIntent(KeyVaultIntent.ChangeSortMode(mode))
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Module contribution bars
        items(state.moduleContributions) { contribution ->
            ModuleContributionCard(
                contribution = contribution,
                onExpand = { onIntent(KeyVaultIntent.ToggleExpandInstance(contribution.moduleName)) }
            )
        }
    }
}

@Composable
private fun ModuleContributionCard(
    contribution: ModuleContribution,
    onExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpand() },
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contribution.moduleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${contribution.instanceCount} 个实例 | 周增长 +${contribution.growthRate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatKeyCount(contribution.keyCount),
                        style = MaterialTheme.typography.titleMedium,
                        color = KeyVaultPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${"%.1f".format(contribution.percentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { contribution.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = KeyVaultPrimary,
                trackColor = Color(0xFF2A2A2A),
            )
        }
    }
}

// ================================================================
// Tab 3: Key Merger — Key 合并工具
// ================================================================

@Composable
private fun KeyMergerTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Key 合并建议",
                    style = MaterialTheme.typography.titleMedium,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "识别可合并的 Key 模式，减少 Key 总数",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        if (state.mergeSuggestions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MergeType, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("暂无合并建议", color = SecondaryText, fontFamily = FontFamily.Monospace)
                        Text("扫描完成后将自动分析可合并的 Key", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                    }
                }
            }
        }

        items(state.mergeSuggestions) { suggestion ->
            KeyMergeSuggestionCard(
                suggestion = suggestion,
                onCopyCode = { onIntent(KeyVaultIntent.GenerateMergeCode(suggestion)) }
            )
        }
    }
}

@Composable
private fun KeyMergeSuggestionCard(
    suggestion: KeyMergeSuggestion,
    onCopyCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.description,
                        style = MaterialTheme.typography.titleSmall,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "节省 ${suggestion.savingsPercent.toInt()}% Key 数量",
                        style = MaterialTheme.typography.bodySmall,
                        color = KeyVaultSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(onClick = onCopyCode) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制代码", tint = KeyVaultPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diff viewer: original keys
            Text("合并前 (${suggestion.originalKeys.size} 个 Key):", style = MaterialTheme.typography.bodySmall, color = KeyVaultError, fontFamily = FontFamily.Monospace)
            suggestion.originalKeys.take(5).forEach { key ->
                Text("  - $key", style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            if (suggestion.originalKeys.size > 5) {
                Text("  ... 共 ${suggestion.originalKeys.size} 个", style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("合并后 (1 个 Key):", style = MaterialTheme.typography.bodySmall, color = KeyVaultSuccess, fontFamily = FontFamily.Monospace)
            Text("  + ${suggestion.mergedKey} (${suggestion.mergedType})", style = MaterialTheme.typography.bodySmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text("示例代码:", style = MaterialTheme.typography.bodySmall, color = KeyVaultSecondary, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = suggestion.sampleCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MonoWhite,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ================================================================
// Tab 4: Migration — SP → DataStore 迁移工具
// ================================================================

@Composable
private fun MigrationTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SP → DataStore 迁移",
                    style = MaterialTheme.typography.titleMedium,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "将 SharedPreferences 迁移到 DataStore，减少 Key 数量并提升性能",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
            }
        }

        // Select instance
        item {
            Text(
                text = "选择要迁移的实例:",
                style = MaterialTheme.typography.titleSmall,
                color = MonoWhite,
                fontFamily = FontFamily.Monospace
            )
        }

        items(state.spInstances.filter { it.type == "SharedPreferences" }) { instance ->
            val isSelected = state.selectedMigrationInstance?.path == instance.path
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(KeyVaultIntent.SelectMigrationInstance(instance)) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) KeyVaultPrimary.copy(alpha = 0.1f) else KeyVaultSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(instance.name, style = MaterialTheme.typography.bodyMedium, color = MonoWhite, fontFamily = FontFamily.Monospace)
                        Text("${instance.module} | ${formatKeyCount(instance.keyCount)} Key", style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "选中", tint = KeyVaultPrimary)
                    }
                }
            }
        }

        // Generated migration code
        if (state.migrationCode != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("迁移代码:", style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace)
                    IconButton(onClick = { /* Already copied */ }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = KeyVaultPrimary)
                    }
                }
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.migrationCode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Generate button
        item {
            Button(
                onClick = { state.selectedMigrationInstance?.let { onIntent(KeyVaultIntent.GenerateMigrationCode(it)) } },
                colors = ButtonDefaults.buttonColors(containerColor = KeyVaultPrimary),
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedMigrationInstance != null
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = KeyVaultBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("生成迁移代码", color = KeyVaultBackground)
            }
        }
    }
}

// ================================================================
// Tab 5: CI Monitor — CI 监控
// ================================================================

@Composable
private fun CIMonitorTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    var thresholdInput by remember { mutableStateOf(state.ciThreshold.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "CI Key 数量监控",
                    style = MaterialTheme.typography.titleMedium,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace
                )
                Text("在 CI 流水线中监控 Key 数量是否超过阈值", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
        }

        // Threshold config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("阈值配置", style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = thresholdInput,
                            onValueChange = { thresholdInput = it },
                            label = { Text("阈值 (Key 数量)") },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { thresholdInput.toIntOrNull()?.let { onIntent(KeyVaultIntent.SetCIThreshold(it)) } },
                            colors = ButtonDefaults.buttonColors(containerColor = KeyVaultPrimary)
                        ) {
                            Text("设置", color = KeyVaultBackground)
                        }
                    }
                }
            }
        }

        // Current status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("当前 Key 数量", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        Text(formatKeyCount(state.totalKeyCount), style = MaterialTheme.typography.headlineSmall, color = state.riskLevel.color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("阈值", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        Text(formatKeyCount(state.ciThreshold), style = MaterialTheme.typography.headlineSmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Build history
        item {
            Text("构建历史", style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace)
        }

        if (state.ciHistory.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = KeyVaultSurface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("暂无构建记录", color = SecondaryText, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        items(state.ciHistory) { record ->
            CIBuildRecordCard(record = record)
        }

        // Export CI config
        item {
            Button(
                onClick = { onIntent(KeyVaultIntent.ExportCIConfig) },
                colors = ButtonDefaults.buttonColors(containerColor = KeyVaultSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出 CI 配置 (GitHub Actions)")
            }
        }
    }
}

@Composable
private fun CIBuildRecordCard(record: CIBuildRecord) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val statusColor = when (record.status) {
        CIStatus.PASS -> KeyVaultSuccess
        CIStatus.FAIL -> KeyVaultError
        CIStatus.WARNING -> KeyVaultPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("#${record.buildNumber}  ${dateFormat.format(Date(record.timestamp))}", style = MaterialTheme.typography.bodyMedium, color = MonoWhite, fontFamily = FontFamily.Monospace)
                Text("阈值: ${formatKeyCount(record.threshold)}", style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatKeyCount(record.keyCount), style = MaterialTheme.typography.bodyMedium, color = statusColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("${record.status.emoji} ${record.status.displayName}", style = MaterialTheme.typography.bodySmall, color = statusColor, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ================================================================
// Tab 6: Degradation Strategies — 降级策略
// ================================================================

@Composable
private fun DegradationTab(
    state: KeyVaultState,
    onIntent: (KeyVaultIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("降级策略", style = MaterialTheme.typography.titleMedium, color = MonoWhite, fontFamily = FontFamily.Monospace)
                Text("Key 超限时的优雅降级策略模板", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
        }

        items(DegradationStrategy.entries) { strategy ->
            val isSelected = state.selectedDegradationStrategy == strategy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(KeyVaultIntent.SelectDegradationStrategy(strategy)) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) KeyVaultPrimary.copy(alpha = 0.1f) else KeyVaultSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (strategy) {
                                DegradationStrategy.LRU_CLEANUP -> Icons.Default.Storage
                                DegradationStrategy.ARCHIVE_HISTORY -> Icons.Default.AccountTree
                                DegradationStrategy.USER_PROMPT -> Icons.Default.Warning
                                DegradationStrategy.KEY_EXPIRY -> Icons.Default.History
                            },
                            contentDescription = null,
                            tint = if (isSelected) KeyVaultPrimary else KeyVaultSecondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strategy.displayName, style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace)
                            Text(strategy.description, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "选中", tint = KeyVaultPrimary)
                        }
                    }

                    // Show templates for selected strategy
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        state.strategyTemplates.filter { it.strategy == strategy }.forEach { template ->
                            StrategyTemplateCard(template = template)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyTemplateCard(template: StrategyTemplate) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(template.title, style = MaterialTheme.typography.titleSmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace)
            Text(template.description, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    text = template.codeTemplate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MonoWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("集成指南:", style = MaterialTheme.typography.bodySmall, color = KeyVaultSecondary, fontFamily = FontFamily.Monospace)
            Text(template.integrationGuide, style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
    }
}

// ================================================================
// Tab 7: Best Practices — 最佳实践
// ================================================================

@Composable
private fun BestPracticesTab(state: KeyVaultState) {
    val recommendedPractices = state.bestPractices.filter { it.isRecommendedPattern }
    val antiPatterns = state.bestPractices.filter { !it.isRecommendedPattern }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Key 管理最佳实践", style = MaterialTheme.typography.titleMedium, color = MonoWhite, fontFamily = FontFamily.Monospace)
                Text("减少 Key 数量的推荐模式与 Anti-pattern", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
        }

        // Recommended patterns
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KeyVaultSuccess, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("推荐模式", style = MaterialTheme.typography.titleSmall, color = KeyVaultSuccess, fontFamily = FontFamily.Monospace)
            }
        }

        items(recommendedPractices) { practice ->
            BestPracticeCard(practice = practice)
        }

        // Anti-patterns
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = KeyVaultError, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Anti-pattern", style = MaterialTheme.typography.titleSmall, color = KeyVaultError, fontFamily = FontFamily.Monospace)
            }
        }

        items(antiPatterns) { practice ->
            BestPracticeCard(practice = practice)
        }

        // Storage comparison
        item {
            Spacer(modifier = Modifier.height(8.dp))
            StorageComparisonCard()
        }
    }
}

@Composable
private fun BestPracticeCard(practice: BestPracticeItem) {
    val impactColor = when (practice.impact) {
        "HIGH" -> KeyVaultError
        "MEDIUM" -> KeyVaultPrimary
        else -> KeyVaultSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(practice.title, style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Text("[${practice.impact}]", style = MaterialTheme.typography.bodySmall, color = impactColor, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(practice.description, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(practice.category, style = MaterialTheme.typography.bodySmall, color = KeyVaultSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StorageComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KeyVaultSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("存储方案对比", style = MaterialTheme.typography.titleSmall, color = MonoWhite, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("方案", style = MaterialTheme.typography.bodySmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(2f))
                Text("Key 数量", style = MaterialTheme.typography.bodySmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                Text("适用场景", style = MaterialTheme.typography.bodySmall, color = KeyVaultPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(2f))
            }

            val comparisons = listOf(
                Triple("SharedPreferences", "无限制 ⚠️", "简单配置、小数据"),
                Triple("DataStore Prefs", "无限制 ⚠️", "类型安全、Flow"),
                Triple("DataStore Proto", "无限制 ⚠️", "复杂结构化数据"),
                Triple("Room DB", "0 (无 Key)", "大量数据、关系型"),
                Triple("MMKV", "无限制 ⚠️", "高性能 kv 存储")
            )

            comparisons.forEach { (name, keyLimit, scenario) ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(name, style = MaterialTheme.typography.bodySmall, color = MonoWhite, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(2f))
                    Text(keyLimit, style = MaterialTheme.typography.bodySmall, color = if (keyLimit.contains("⚠️")) KeyVaultError else KeyVaultSuccess, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text(scenario, style = MaterialTheme.typography.bodySmall, color = SecondaryText, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(2f))
                }
            }
        }
    }
}