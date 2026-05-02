package com.mvi.kenny.feature.android17memory

// ================================================================
// Android17MemoryScreen — Android 17 内存限制适配工具包主界面
// ================================================================
// Main screen for Android 17 Memory Limits Toolkit.
//
// PRD-213: Android 17 设备 RAM 内存限制适配工具包
// Design Reference: memory/agency/designs/PRD-213-Android-17-内存限制适配工具包.md
//
// Features:
//   Tab 1: 📊 内存检测 — App 当前内存使用检测
//   Tab 2: 📋 设备换算 — 设备 RAM → 内存限制换算表
//   Tab 3: ⚠️ 风险评估 — 内存限制影响评估扫描器
//   Tab 4: 🔧 Profiling — ProfilingManager 内存限制查询
//   Tab 5: 🧪 CI 测试 — 低 RAM 模拟 + CI 合规检测
// —————————————————————————————————————————————————————

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// Color definitions — 深色 Terminal 风格配色
// =============================================================
private val PrimaryColor = Color(0xFF00E5CC)
private val SecondaryColor = Color(0xFFFFB347)
private val BackgroundColor = Color(0xFF0D1117)
private val SurfaceColor = Color(0xFF161B22)
private val SurfaceVariantColor = Color(0xFF21262D)
private val OnSurfaceColor = Color(0xFFE6EDF3)
private val OnSurfaceVariantColor = Color(0xFF8B949E)
private val ErrorColor = Color(0xFFFF6B6B)
private val WarningColor = Color(0xFFFFB347)
private val SuccessColor = Color(0xFF00E5CC)
private val CodeBlockBg = Color(0xFF1C2128)
private val CodeHighlight = Color(0xFF79C0FF)

// =============================================================
// Android17MemoryScreen — 主界面入口
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android17MemoryScreen(
    viewModel: Android17MemoryViewModel = viewModel(
        factory = Android17MemoryViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle effects / 处理副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Android17MemoryEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is Android17MemoryEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(effect.label, effect.content))
                    snackbarHostState.showSnackbar("Copied: ${effect.label}")
                }
                is Android17MemoryEffect.ShareChecklist -> {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, effect.content)
                        type = "text/markdown"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Checklist"))
                }
            }
        }
    }

    // Auto-load data / 自动加载数据
    LaunchedEffect(Unit) {
        viewModel.processIntent(Android17MemoryIntent.LoadRamLimitTable)
        viewModel.processIntent(Android17MemoryIntent.LoadInstalledApps)
        viewModel.processIntent(Android17MemoryIntent.SetupCiCompliance)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = PrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android 17 Memory Limits",
                            color = OnSurfaceColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor),
                actions = {
                    IconButton(onClick = { viewModel.processIntent(Android17MemoryIntent.RefreshMemoryUsage) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryColor)
                    }
                }
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MemoryTabRow(
                selectedTabIndex = state.currentTab,
                onTabSelected = { viewModel.processIntent(Android17MemoryIntent.SelectTab(it)) }
            )

            AnimatedContent(
                targetState = state.currentTab,
                transitionSpec = {
                    (slideInHorizontally { it } + androidx.compose.animation.fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + androidx.compose.animation.fadeOut())
                },
                label = "Tab content"
            ) { tabIndex ->
                when (MemoryTab.entries[tabIndex]) {
                    MemoryTab.MEMORY_DETECTION -> MemoryDetectionTab(state, viewModel)
                    MemoryTab.RAM_TABLE -> RamLimitTableTab(state, viewModel)
                    MemoryTab.RISK_SCAN -> RiskScanTab(state, viewModel)
                    MemoryTab.PROFILING -> ProfilingTab(state, viewModel)
                    MemoryTab.CI_TEST -> CiTestTab(state, viewModel)
                }
            }
        }
    }
}

// =============================================================
// MemoryTabRow — Tab 导航行
// =============================================================
@Composable
private fun MemoryTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = SurfaceColor,
        contentColor = OnSurfaceColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = PrimaryColor,
                height = 2.dp
            )
        }
    ) {
        MemoryTab.entries.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = "${tab.emoji} ${tab.title}",
                        color = if (selectedTabIndex == index) PrimaryColor else OnSurfaceVariantColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selectedContentColor = PrimaryColor,
                unselectedContentColor = OnSurfaceVariantColor
            )
        }
    }
}

// =============================================================
// Tab 1: Memory Detection / 内存检测
// =============================================================
@Composable
private fun MemoryDetectionTab(
    state: Android17MemoryState,
    viewModel: Android17MemoryViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TerminalCard(title = "📊 Memory Detection / 内存检测", emoji = "📊") {
            Text("实时监控 App 内存使用情况 — Heap / Dalvik / Native / Total",
                color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }}
        item { AppSelectorCard(state, viewModel) }
        item { MemoryUsageCard(state, viewModel) }
        item { MemoryBreakdownCard(state) }
        item { TerminalCard(title = "💡 Tips / 提示", emoji = "💡") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TipRow("✅ Heap 使用率建议保持在 60% 以下，避免触发 GC")
                TipRow("✅ Dalvik 内存高通常由频繁对象分配导致，注意对象池")
                TipRow("✅ Native 内存高时检查 JNI 引用和 Bitmap 释放")
                TipRow("⚠️ 总内存接近设备限制时系统会主动限制 App")
                TipRow("🔧 使用 Debug.getMemoryInfo() 持续监控内存变化")
            }
        }}
    }
}

@Composable
private fun AppSelectorCard(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val displayName = state.installedApps.find { it.first == state.selectedPackage }?.second
        ?: state.selectedPackage.ifEmpty { "Select App / 选择 App" }

    TerminalCard(title = "🎯 Target App / 目标 App", emoji = "🎯") {
        Box {
            OutlinedButton(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = displayName, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }
            if (expanded && state.installedApps.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 44.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor)) {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(state.installedApps.take(50)) { (pkg, name) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.processIntent(Android17MemoryIntent.SelectApp(pkg, name))
                                    expanded = false
                                }.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null,
                                    tint = if (pkg == state.selectedPackage) PrimaryColor else OnSurfaceVariantColor,
                                    modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = name,
                                    color = if (pkg == state.selectedPackage) PrimaryColor else OnSurfaceColor,
                                    fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryUsageCard(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    val usage = state.currentMemoryUsage
    val level = usage.usageLevel
    val levelColor = when (level) {
        MemoryUsageLevel.HIGH -> ErrorColor
        MemoryUsageLevel.MEDIUM -> WarningColor
        MemoryUsageLevel.LOW -> SuccessColor
    }
    val levelLabel = when (level) {
        MemoryUsageLevel.HIGH -> "HIGH ⚠️  高内存占用"
        MemoryUsageLevel.MEDIUM -> "MEDIUM ⚡ 中等内存"
        MemoryUsageLevel.LOW -> "LOW ✅ 低内存"
    }

    TerminalCard(
        title = "📈 Current Memory / 当前内存",
        emoji = "📈",
        action = {
            IconButton(onClick = { viewModel.processIntent(Android17MemoryIntent.RefreshMemoryUsage) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryColor)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(levelColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(levelColor, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(levelLabel, color = levelColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Heap", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("${usage.heapUsedMb} / ${usage.heapTotalMb} MB",
                        color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
                LinearProgressIndicator(
                    progress = { usage.heapUsageRatio },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = levelColor, trackColor = SurfaceVariantColor
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = "Dalvik", value = "${usage.dalvikUsedMb} MB", color = SecondaryColor)
                StatCard(modifier = Modifier.weight(1f), label = "Native", value = "${usage.nativeUsedMb} MB", color = PrimaryColor)
                StatCard(modifier = Modifier.weight(1f), label = "Total", value = "${usage.totalUsedMb} MB", color = levelColor)
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Column(
        modifier = modifier.background(SurfaceVariantColor, RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun MemoryBreakdownCard(state: Android17MemoryState) {
    val usage = state.currentMemoryUsage
    TerminalCard(title = "🔍 Memory Breakdown / 内存分解", emoji = "🔍") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val total = usage.totalUsedMb.coerceAtLeast(1L)
            MemoryBar("Heap", usage.heapUsedMb, total, PrimaryColor)
            MemoryBar("Dalvik", usage.dalvikUsedMb, total, SecondaryColor)
            MemoryBar("Native", usage.nativeUsedMb, total, Color(0xFF79C0FF))
            val other = (usage.totalUsedMb - usage.heapUsedMb - usage.dalvikUsedMb - usage.nativeUsedMb).coerceAtLeast(0L)
            MemoryBar("Other", other, total, OnSurfaceVariantColor)
        }
    }
}

@Composable
private fun MemoryBar(label: String, value: Long, total: Long, color: Color) {
    val ratio = (value.toFloat() / total).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Text("${value}MB", color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color, trackColor = SurfaceVariantColor
        )
    }
}

// =============================================================
// Tab 2: RAM Limit Table / 设备换算表
// =============================================================
@Composable
private fun RamLimitTableTab(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TerminalCard(title = "📋 RAM → Memory Limit / 设备换算表", emoji = "📋") {
            Text("Android 17 per-device memory limits: 系统根据设备总 RAM 设置 App 内存上限",
                color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }}
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.ramLimitTable.any { !it.isLargeHeap },
                    onClick = { viewModel.processIntent(Android17MemoryIntent.FilterLargeHeap(false)) },
                    label = { Text("All / 全部", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryColor.copy(alpha = 0.2f), selectedLabelColor = PrimaryColor
                    )
                )
                FilterChip(
                    selected = state.ramLimitTable.all { it.isLargeHeap },
                    onClick = { viewModel.processIntent(Android17MemoryIntent.FilterLargeHeap(true)) },
                    label = { Text("Large Heap / Large Heap", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SecondaryColor.copy(alpha = 0.2f), selectedLabelColor = SecondaryColor
                    )
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceVariantColor, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableHeader("Device RAM", Modifier.weight(1f))
                TableHeader("Memory Limit", Modifier.weight(1f))
                TableHeader("Category", Modifier.weight(1f))
                TableHeader("Large Heap", Modifier.weight(1f))
            }
        }
        items(state.ramLimitTable) { entry ->
            val categoryColor = when (entry.deviceCategory) {
                "Low-end" -> ErrorColor; "Mid-range" -> WarningColor; "High-end" -> PrimaryColor; else -> SuccessColor
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell("${entry.deviceRamGb} GB", Modifier.weight(1f), OnSurfaceColor)
                TableCell("${entry.appMemoryLimitMb} MB", Modifier.weight(1f), PrimaryColor)
                TableCell(entry.deviceCategory, Modifier.weight(1f), categoryColor)
                TableCell(if (entry.isLargeHeap) "✅" else "—", Modifier.weight(1f),
                    if (entry.isLargeHeap) SuccessColor else OnSurfaceVariantColor)
            }
        }
        item {
            TerminalCard(title = "📝 Note / 备注", emoji = "📝") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• 换算数据基于 Android 17 Beta 4 官方文档及行业实测", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• 实际限制因 OEM 实现而异，这些是保守估计值", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    Text("• Large heap 限制约为普通限制的 1.5 倍", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable private fun TableHeader(text: String, modifier: Modifier = Modifier) {
    Text(text, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}
@Composable private fun TableCell(text: String, modifier: Modifier = Modifier, color: Color = OnSurfaceColor) {
    Text(text, color = color, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = modifier)
}

// =============================================================
// Tab 3: Risk Scan / 风险评估
// =============================================================
@Composable
private fun RiskScanTab(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TerminalCard(title = "⚠️ Risk Scan / 风险评估", emoji = "⚠️") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("扫描 App 代码中的高内存风险模式，提供优化建议",
                    color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                Button(
                    onClick = { viewModel.processIntent(Android17MemoryIntent.RunRiskScan) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundColor, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Risk Scan / 运行风险扫描", fontFamily = FontFamily.Monospace)
                }
            }
        }}
        if (state.riskScanResults.isNotEmpty()) {
            item { RiskSummaryCard(state.riskScanResults) }
            items(state.riskScanResults) { risk -> RiskItemCard(risk) }
        }
        if (state.riskScanResults.isEmpty() && !state.isLoading) {
            item { TerminalCard(title = "🔍 No Scan Results / 无扫描结果", emoji = "🔍") {
                Text("点击「运行风险扫描」分析 App 内存风险",
                    color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }}
        }
    }
}

@Composable
private fun RiskSummaryCard(risks: List<RiskItem>) {
    val criticalCount = risks.count { it.severity == Severity.CRITICAL }
    val highCount = risks.count { it.severity == Severity.HIGH }
    val mediumCount = risks.count { it.severity == Severity.MEDIUM }
    val lowCount = risks.count { it.severity == Severity.LOW }
    TerminalCard(title = "📊 Risk Summary / 风险汇总", emoji = "📊") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            SummaryBadge("🔴 CRITICAL", criticalCount, ErrorColor)
            SummaryBadge("🟠 HIGH", highCount, WarningColor)
            SummaryBadge("🟡 MEDIUM", mediumCount, Color(0xFFFFD93D))
            SummaryBadge("🟢 LOW", lowCount, SuccessColor)
        }
    }
}

@Composable private fun SummaryBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(label, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
    }
}

@Composable
private fun RiskItemCard(risk: RiskItem) {
    val severityColor = when (risk.severity) {
        Severity.CRITICAL -> ErrorColor; Severity.HIGH -> WarningColor
        Severity.MEDIUM -> Color(0xFFFFD93D); Severity.LOW -> SuccessColor
    }
    TerminalCard(title = "${risk.severity.emoji} ${risk.riskType.displayName}", emoji = risk.severity.emoji) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(risk.severity.displayName, color = severityColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(risk.codePath, color = CodeHighlight, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Text(risk.description, color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceVariantColor, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(risk.suggestion, color = PrimaryColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

// =============================================================
// Tab 4: Profiling / Profiling
// =============================================================
@Composable
private fun ProfilingTab(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TerminalCard(title = "🔧 ProfilingManager / ProfilingManager API", emoji = "🔧") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Android 17 (API 35+) 新增 ProfilingManager — App 可查询自身内存限制",
                    color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                Button(
                    onClick = { viewModel.processIntent(Android17MemoryIntent.QueryProfilingManager) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundColor, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Query Memory Limit / 查询内存限制", fontFamily = FontFamily.Monospace)
                }
            }
        }}
        if (state.profilingResult.profilingManagerAvailable || state.profilingResult.memoryLimitMb > 0) {
            item { ProfilingResultCard(state.profilingResult) }
        }
        item { TerminalCard(title = "📖 API Reference / API 参考", emoji = "📖") {
            CodeBlock(
                "// Get ProfilingManager (API 35+)\n" +
                "val profilingManager = getSystemService(Context.PROFILING_SERVICE) as ProfilingManager\n\n" +
                "// Query memory limit for this app\n" +
                "val memoryLimit = profilingManager.memoryLimit\n\n" +
                "// Check if large heap is enabled\n" +
                "val isLargeHeap = activityManager.isLowRamDevice.not()\n\n" +
                "// Request heap dump\n" +
                "val heapDumpRequest = ProfilingManager.AppHeapDumpRequest()\n" +
                "profilingManager.createAppHeapDump(heapDumpRequest)"
            )
        }}
        item { TerminalCard(title = "📋 JobScheduler Memory Awareness / 内存感知调度", emoji = "📋") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Android 17 的 JobScheduler 会根据 App 当前内存使用情况调整后台 Job 优先级：",
                    color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                TipRow("✅ 内存使用高时 → 后台 Job 被延迟或跳过")
                TipRow("✅ 内存使用低时 → 正常执行 Job")
                TipRow("⚠️ Critical memory → Job 被取消")
                TipRow("🔧 使用 setOverrideDeadline() 处理内存紧张场景")
            }
        }}
    }
}

@Composable
private fun ProfilingResultCard(result: ProfilingResult) {
    TerminalCard(
        title = "📊 Profiling Result / 查询结果",
        emoji = "📊"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!result.profilingManagerAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(WarningColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = WarningColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ProfilingManager 仅在 Android 17 (API 35+) 设备上可用，当前设备或模拟器不支持",
                        color = WarningColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().background(SurfaceVariantColor, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (result.profilingManagerAvailable) "${result.memoryLimitMb} MB" else "N/A",
                        color = PrimaryColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Memory Limit / 内存上限", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (result.isLargeHeapEnabled) "✅ Enabled" else "❌ Disabled",
                        color = if (result.isLargeHeapEnabled) SuccessColor else ErrorColor,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Large Heap / Large Heap", color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}

// =============================================================
// Tab 5: CI Test / CI 测试
// =============================================================
@Composable
private fun CiTestTab(state: Android17MemoryState, viewModel: Android17MemoryViewModel) {
    var targetRamMb by remember { mutableIntStateOf(512) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CI Compliance Status / CI 合规状态
        item { CiComplianceCard(state) }

        // Emulator Config Generator / 模拟器配置生成
        item { TerminalCard(title = "🧪 Low RAM Emulator / 低 RAM 模拟器", emoji = "🧪") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("在 CI 中使用低 RAM 模拟器测试 App 在受限环境下的行为",
                    color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target RAM:", color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    listOf(256, 384, 512, 768).forEach { mb ->
                        FilterChip(
                            selected = targetRamMb == mb,
                            onClick = { targetRamMb = mb },
                            label = { Text("${mb}MB", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryColor.copy(alpha = 0.2f), selectedLabelColor = PrimaryColor
                            )
                        )
                    }
                }
                Button(
                    onClick = { viewModel.processIntent(Android17MemoryIntent.GenerateEmulatorConfig(targetRamMb)) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Config / 生成配置", fontFamily = FontFamily.Monospace)
                }
            }
        }}

        // Emulator Config Output / 配置输出
        if (state.emulatorConfig != null) {
            item { EmulatorConfigCard(state.emulatorConfig!!, viewModel) }
        }

        // Checklist / 检查清单
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 Android 17                Checklist / 检查清单",
                    color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { viewModel.processIntent(Android17MemoryIntent.ToggleChecklist) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Icon(if (state.checklist.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.checklist.isExpanded) "Collapse / 收起" else "Expand / 展开", fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Checklist items / 检查清单内容
        if (state.checklist.isExpanded) {
            items(state.checklist.items) { item ->
                ChecklistItemCard(item)
            }
        }

        // Export button / 导出按钮
        item {
            Button(
                onClick = { viewModel.processIntent(Android17MemoryIntent.ExportChecklist) },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export as Markdown / 导出为 Markdown", fontFamily = FontFamily.Monospace)
            }
        }

        // CI Gradle Plugin Guide / CI Gradle 插件指南
        item { TerminalCard(title = "🔌 CI Gradle Plugin / CI Gradle 插件", emoji = "🔌") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("在项目中添加 Android 17 Memory Limits Gradle 插件：",
                    color = OnSurfaceColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                CodeBlock(
                    "// build.gradle.kts\nplugins {\n    id(\"com.android.application\")\n    id(\"androidx.room\")\n    // Add memory limits plugin\n    id(\"com.android.developer.memory-limits\") version \"1.0.0\"\n}\n\nandroidMemoryLimits {\n    // Set expected memory limit for this app\n    expectedMemoryLimitMb.set(512)\n    // Block CI if memory usage exceeds limit\n    blockOnViolation.set(true)\n    // Generate report on each build\n    generateReport.set(true)\n}"
                )
            }
        }}
    }
}

@Composable
private fun CiComplianceCard(state: Android17MemoryState) {
    val (color, emoji, bgColor) = when (state.ciComplianceStatus) {
        CiComplianceStatus.COMPLIANT -> Triple(SuccessColor, "✅", SuccessColor.copy(alpha = 0.15f))
        CiComplianceStatus.NON_COMPLIANT -> Triple(ErrorColor, "❌", ErrorColor.copy(alpha = 0.15f))
        CiComplianceStatus.SCANNING -> Triple(WarningColor, "🔄", WarningColor.copy(alpha = 0.15f))
        CiComplianceStatus.UNKNOWN -> Triple(OnSurfaceVariantColor, "⚪", SurfaceVariantColor)
    }
    TerminalCard(title = "🛡️ CI Compliance / CI 合规状态", emoji = "🛡️") {
        Row(
            modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(8.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(state.ciComplianceStatus.displayName, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    when (state.ciComplianceStatus) {
                        CiComplianceStatus.COMPLIANT -> "All checks passed / 所有检查通过"
                        CiComplianceStatus.NON_COMPLIANT -> "Critical memory risks found / 发现严重内存风险"
                        CiComplianceStatus.SCANNING -> "Scanning... / 扫描中..."
                        CiComplianceStatus.UNKNOWN -> "Run risk scan to check compliance / 运行风险扫描检查合规性"
                    },
                    color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmulatorConfigCard(config: EmulatorConfig, viewModel: Android17MemoryViewModel) {
    TerminalCard(
        title = "📄 Emulator Config / 模拟器配置",
        emoji = "📄",
        action = {
            IconButton(onClick = { viewModel.processIntent(Android17MemoryIntent.ExportChecklist) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryColor)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Target RAM: ${config.targetRamMb}MB | AVD Name: ${config.avdName}",
                color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            CodeBlock(config.startCommand)
        }
    }
}

@Composable
private fun ChecklistItemCard(item: ChecklistItem) {
    TerminalCard(
        title = if (item.isChecked) "✅ ${item.title}" else "⬜ ${item.title}",
        emoji = if (item.isChecked) "✅" else "⬜"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, color = if (item.isChecked) SuccessColor else OnSurfaceColor,
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(item.description, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            if (item.category.isNotEmpty()) {
                Box(modifier = Modifier.background(PrimaryColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(item.category, color = PrimaryColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
            }
        }
    }
}

// =============================================================
// Shared Components / 共享组件
// =============================================================

/**
 * Terminal-style card / Terminal 风格卡片
 *
 * @param title Card title / 卡片标题
 * @param emoji Emoji prefix / Emoji 前缀
 * @param action Optional action slot on the right / 右侧可选操作位
 * @param content Card content / 卡片内容
 */
@Composable
private fun TerminalCard(
    title: String,
    emoji: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = OnSurfaceColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                action?.invoke()
            }
            content()
        }
    }
}

/**
 * Code block with syntax highlighting style / 代码块
 *
 * @param code Code content / 代码内容
 */
@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CodeBlockBg, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            color = CodeHighlight,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

/**
 * Tip row with bullet / 提示行
 *
 * @param text Tip text / 提示文本
 */
@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("• ", color = PrimaryColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        Text(text, color = OnSurfaceVariantColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
    }
}
