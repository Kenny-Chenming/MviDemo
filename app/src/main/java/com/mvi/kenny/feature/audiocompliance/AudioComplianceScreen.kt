package com.mvi.kenny.feature.audiocompliance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import com.mvi.kenny.feature.audiocompliance.AudioComplianceEffect
import com.mvi.kenny.feature.audiocompliance.AudioComplianceIntent
import com.mvi.kenny.feature.audiocompliance.AudioComplianceState
import com.mvi.kenny.feature.audiocompliance.AudioComplianceTab
import com.mvi.kenny.feature.audiocompliance.AudioComplianceViewModel
import com.mvi.kenny.feature.audiocompliance.AudioSeverity
import com.mvi.kenny.feature.audiocompliance.AudioTestCase
import com.mvi.kenny.feature.audiocompliance.SimulationBehavior
import com.mvi.kenny.feature.audiocompliance.SimulationResult
import com.mvi.kenny.feature.audiocompliance.TestResultStatus
import kotlinx.coroutines.flow.collectLatest

// Color constants / 颜色常量
private val ColorBlocked = Color(0xFFFF5252)
private val ColorDegraded = Color(0xFFFFC107)
private val ColorSafe = Color(0xFF4CAF50)
private val ColorBackground = Color(0xFF121212)

// ================================================================
// RadarChart — 合规健康度雷达图
// ================================================================
@Composable
private fun RadarChart(score: Int, modifier: Modifier = Modifier) {
    val animatedScore by animateFloatAsState(targetValue = score.toFloat(), animationSpec = tween(800), label = "score")
    val scoreColor = when { score >= 70 -> ColorSafe; score >= 40 -> ColorDegraded; else -> ColorBlocked }
    Box(modifier = modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val cx = size.width / 2; val cy = size.height / 2; val r = minOf(cx, cy) * 0.8f
            val angles = listOf(0f, 72f, 144f, 216f, 288f).map { Math.toRadians(it.toDouble() - 90) }
            val bg = androidx.compose.ui.graphics.Path()
            angles.forEachIndexed { i, a ->
                val x = cx + (r * kotlin.math.cos(a)).toFloat(); val y = cy + (r * kotlin.math.sin(a)).toFloat()
                if (i == 0) bg.moveTo(x, y) else bg.lineTo(x, y)
            }
            bg.close()
            drawPath(bg, Color.Gray.copy(alpha = 0.3f))
            val sr = r * (animatedScore / 100f)
            val sp = androidx.compose.ui.graphics.Path()
            angles.forEachIndexed { i, a ->
                val x = cx + (sr * kotlin.math.cos(a)).toFloat(); val y = cy + (sr * kotlin.math.sin(a)).toFloat()
                if (i == 0) sp.moveTo(x, y) else sp.lineTo(x, y)
            }
            sp.close()
            drawPath(sp, scoreColor.copy(alpha = 0.4f)); drawPath(sp, scoreColor, style = Stroke(width = 3f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${animatedScore.toInt()}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = scoreColor)
            Text("健康度", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// ================================================================
// SeverityBadge — 严重程度标签
// ================================================================
@Composable
private fun SeverityBadge(severity: AudioSeverity) {
    val (color, text) = when (severity) {
        AudioSeverity.BLOCKED -> ColorBlocked to "🔴 阻断"
        AudioSeverity.DEGRADED -> ColorDegraded to "🟡 降级"
        AudioSeverity.SAFE -> ColorSafe to "🟢 安全"
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ================================================================
// AffectedApiCard — 受影响 API 统计卡片
// ================================================================
@Composable
private fun AffectedApiCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// ================================================================
// PieChartView — 饼图
// ================================================================
@Composable
private fun PieChartView(blocked: Int, degraded: Int, safe: Int, modifier: Modifier = Modifier) {
    val total = (blocked + degraded + safe).coerceAtLeast(1)
    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width; val h = size.height
            var start = -90f
            val bAngle = 360f * blocked / total; val dAngle = 360f * degraded / total
            if (bAngle > 0) {
                rotate(start) { drawArc(ColorBlocked, 0f, bAngle, true, topLeft = Offset(0f, 0f), size = Size(w, h)) }
                start += bAngle
            }
            if (dAngle > 0) {
                rotate(start) { drawArc(ColorDegraded, 0f, dAngle, true, topLeft = Offset(0f, 0f), size = Size(w, h)) }
                start += dAngle
            }
            rotate(start) { drawArc(ColorSafe, 0f, 360f - bAngle - dAngle, true, topLeft = Offset(0f, 0f), size = Size(w, h)) }
        }
        Text(total.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// ================================================================
// LogViewer — 日志滚动展示
// ================================================================
@Composable
private fun LogViewer(logs: List<String>, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(logs) { log ->
                Text(log, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = when {
                    log.startsWith("[ERROR]") -> ColorBlocked
                    log.startsWith("[INFO]") -> ColorSafe
                    log.startsWith("[RESULT]") -> ColorDegraded
                    else -> Color.Gray
                })
            }
        }
    }
}

// ================================================================
// Main Screen
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioComplianceScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: AudioComplianceViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.selectedTab) {
        val actions = if (state.selectedTab == AudioComplianceTab.REPORT) {
            listOf(TopBarConfig.TopBarAction(icon = Icons.Default.Share, contentDescription = "分享报告", onClick = { }))
        } else emptyList()
        onUpdateTopBar(TopBarConfig(title = "音频合规检测", actions = actions))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AudioComplianceEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
                is AudioComplianceEffect.CopyToClipboard -> {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("FGS Template", effect.text))
                    snackbarHostState.showSnackbar("已复制到剪贴板")
                }
                is AudioComplianceEffect.ShareReport -> {
                    val intent = Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, effect.content); type = "text/plain" }
                    context.startActivity(Intent.createChooser(intent, "分享报告"))
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = state.selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                listOf("概览", "扫描器", "模拟器", "报告", "FGS指南", "降级方案", "回归测试").forEachIndexed { i, title ->
                    Tab(selected = state.selectedTab == i, onClick = { viewModel.sendIntent(AudioComplianceIntent.SelectTab(i)) }, text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title)
                            if (i == AudioComplianceTab.SCANNER && state.affectedApiCount > 0) {
                                Spacer(Modifier.width(4.dp))
                                Badge { Text(state.affectedApiCount.toString()) }
                            }
                        }
                    })
                }
            }
            AnimatedContent(targetState = state.selectedTab, transitionSpec = {
                fadeIn(tween(200)) + slideInHorizontally { it / 4 } togetherWith fadeOut(tween(200)) + slideOutHorizontally { -it / 4 }
            }, label = "tab") { tab ->
                when (tab) {
                    AudioComplianceTab.DASHBOARD -> DashboardTabContent(state, viewModel)
                    AudioComplianceTab.SCANNER -> ScannerTabContent(state, viewModel)
                    AudioComplianceTab.SIMULATOR -> SimulatorTabContent(state, viewModel)
                    AudioComplianceTab.REPORT -> ReportTabContent(state, viewModel)
                    AudioComplianceTab.FGS_GUIDE -> FgsGuideTabContent(state, viewModel)
                    AudioComplianceTab.FALLBACKS -> FallbacksTabContent(state, viewModel)
                    AudioComplianceTab.REGRESSION -> RegressionTabContent(state, viewModel)
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ================================================================
// T1 Dashboard
// ================================================================
@Composable
private fun DashboardTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("音频合规健康度", fontSize = 16.sp, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    RadarChart(score = state.healthScore)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        when { state.healthScore >= 70 -> "✅ 合规状态良好"; state.healthScore >= 40 -> "⚠️ 需要关注"; else -> "🔴 合规风险高" },
                        fontSize = 14.sp, color = Color.White
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AffectedApiCard("🔴 直接阻断", state.blockedCount, ColorBlocked, Modifier.weight(1f))
                AffectedApiCard("🟡 行为降级", state.degradedCount, ColorDegraded, Modifier.weight(1f))
                AffectedApiCard("🟢 无影响", state.safeCount, ColorSafe, Modifier.weight(1f))
            }
        }
        item { Text("快捷操作", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.SelectTab(AudioComplianceTab.SCANNER)) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text("开始扫描")
                }
                Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.SelectTab(AudioComplianceTab.FGS_GUIDE)) }, modifier = Modifier.weight(1f)) { Text("豁免指南") }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("影响范围摘要", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text(buildString {
                        append("• 检测到 ${state.affectedApiCount} 处音频 API 调用\n")
                        if (state.blockedCount > 0) append("• ${state.blockedCount} 处将在 Android 17 后台直接阻断\n")
                        if (state.degradedCount > 0) append("• ${state.degradedCount} 处将出现行为降级\n")
                        if (state.safeCount > 0) append("• ${state.safeCount} 处不受影响")
                    }, fontSize = 13.sp, color = Color.Gray, lineHeight = 22.sp)
                }
            }
        }
    }
}

// ================================================================
// T2 Scanner
// ================================================================
@Composable
private fun ScannerTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { viewModel.sendIntent(AudioComplianceIntent.StartScan) },
                    enabled = !state.isScanning,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isScanning) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isScanning) "扫描中..." else "开始扫描")
                }
            }
        }
        if (state.isScanning) {
            item {
                Column {
                    Text("正在扫描音频 API 调用路径...", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { state.scanProgress }, modifier = Modifier.fillMaxWidth())
                    Text("${(state.scanProgress * 100).toInt()}%", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End))
                }
            }
        }
        if (state.scanResults.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = state.severityFilter == null, onClick = { viewModel.sendIntent(AudioComplianceIntent.FilterBySeverity(null)) }, label = { Text("全部") })
                    AudioSeverity.entries.forEach { sev ->
                        FilterChip(
                            selected = state.severityFilter == sev,
                            onClick = { viewModel.sendIntent(AudioComplianceIntent.FilterBySeverity(sev)) },
                            label = { Text(when (sev) { AudioSeverity.BLOCKED -> "🔴 阻断"; AudioSeverity.DEGRADED -> "🟡 降级"; AudioSeverity.SAFE -> "🟢 安全" }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (sev) { AudioSeverity.BLOCKED -> ColorBlocked; AudioSeverity.DEGRADED -> ColorDegraded; AudioSeverity.SAFE -> ColorSafe }.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
        val filtered = if (state.severityFilter != null) state.scanResults.filter { it.severity == state.severityFilter } else state.scanResults
        items(filtered, key = { it.id }) { result ->
            var expanded by remember { mutableStateOf(false) }
            Card(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(result.apiName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("${result.filePath}:${result.lineNumber}", fontSize = 11.sp, color = Color.Gray)
                        }
                        SeverityBadge(result.severity)
                        Spacer(Modifier.width(8.dp))
                        Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    }
                    if (expanded) {
                        Spacer(Modifier.height(12.dp)); HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f)); Spacer(Modifier.height(12.dp))
                        Text("📍 场景描述", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(result.description, fontSize = 13.sp, color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text("💡 修复建议", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(result.fixSuggestion, fontSize = 13.sp, color = ColorSafe)
                    }
                }
            }
        }
        if (state.scanResults.isEmpty() && !state.isScanning) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MusicNote, null, Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("点击「开始扫描」检测音频 API 调用", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ================================================================
// T3 Simulator
// ================================================================
@Composable
private fun SimulatorTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    val scenarios = listOf(
        AudioScenario("music_player", "Music Player", "后台音乐播放器，屏幕关闭后继续播放", listOf("AudioTrack.start()", "MediaPlayer.start()")),
        AudioScenario("navigation", "Navigation", "导航 App 后台播报转向指令", listOf("AudioTrack.start()", "AudioFocusRequest")),
        AudioScenario("voice_assistant", "Voice Assistant", "语音助手后台音频处理", listOf("MediaPlayer.start()", "AudioFocusRequest")),
        AudioScenario("podcast_auto", "Podcast Auto-Play", "播客下载完成后自动播放下一集", listOf("MediaPlayer.start()"))
    )
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("选择音频场景", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scenarios, key = { it.id }) { s ->
                    FilterChip(selected = state.selectedScenario?.id == s.id, onClick = { viewModel.sendIntent(AudioComplianceIntent.SelectScenario(s)) }, label = { Text(s.name) })
                }
            }
        }
        if (state.selectedScenario != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.selectedScenario!!.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(state.selectedScenario!!.description, fontSize = 13.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("受影响 API: ${state.selectedScenario!!.affectedApis.joinToString(", ")}", fontSize = 12.sp, color = ColorDegraded)
                    }
                }
            }
        }
        item {
            Button(
                onClick = { viewModel.sendIntent(AudioComplianceIntent.RunSimulation) },
                enabled = state.selectedScenario != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("运行模拟")
            }
        }
        if (state.simulationResults.isNotEmpty()) {
            item { Text("模拟结果", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            items(state.simulationResults, key = { it.scenarioId }) { r ->
                val (color, emoji) = when (r.behavior) {
                    SimulationBehavior.BLOCKED -> ColorBlocked to "🔴"
                    SimulationBehavior.DEGRADED -> ColorDegraded to "🟡"
                    SimulationBehavior.NORMAL -> ColorSafe to "🟢"
                }
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$emoji ${r.scenarioName}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(r.behavior.name, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(r.detail, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
        if (state.simulationLogs.isNotEmpty()) {
            item { Text("模拟日志", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            item { LogViewer(state.simulationLogs, Modifier.height(250.dp)) }
        }
    }
}

// ================================================================
// T4 Report
// ================================================================
@Composable
private fun ReportTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    val report = state.reportData
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (report == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("请先运行扫描生成报告", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.SelectTab(AudioComplianceTab.SCANNER)) }) { Text("前往扫描") }
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("合规报告总览", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            PieChartView(report.blockedCount, report.degradedCount, report.safeCount)
                            Column {
                                listOf(ColorBlocked to "阻断: ${report.blockedCount}", ColorDegraded to "降级: ${report.degradedCount}", ColorSafe to "安全: ${report.safeCount}").forEach { (c, t) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).background(c, CircleShape)); Spacer(Modifier.width(8.dp))
                                        Text(t, fontSize = 13.sp, color = Color.White)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("健康度: ${report.healthScore}/100", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = when { report.healthScore >= 70 -> ColorSafe; report.healthScore >= 40 -> ColorDegraded; else -> ColorBlocked })
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("报告摘要", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(report.summary, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
            item { Text("按模块分组详情", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            report.moduleBreakdown.forEach { (module, apis) ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📁 $module", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.height(8.dp))
                            apis.forEach { api ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(api.apiName, fontSize = 13.sp, color = Color.White)
                                    Spacer(Modifier.weight(1f))
                                    SeverityBadge(api.severity)
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// T5 FGS Guide
// ================================================================
@Composable
private fun FgsGuideTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("while-in-use FGS 豁免条件", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("豁免规则说明", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Android 17 的后台音频限制采用 while-in-use FGS 执行模式。以下条件可获得音频豁免：\n\n1. App 当前有可见的 Foreground Service 通知\n2. 音频播放由用户主动发起\n3. 音频焦点由前台 Activity 持有\n4. 使用 FOREGROUND_SERVICE_MEDIA_PLAYBACK 类型\n\n注意：即使满足条件，未声明正确 foregroundServiceType 的 FGS 仍将被阻断。", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
        item { Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.DetectFgsConfig) }, modifier = Modifier.fillMaxWidth()) { Text("检测当前 FGS 配置") } }
        if (state.currentFgsConfig != null) {
            val cfg = state.currentFgsConfig!!
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (cfg.isCompliant) ColorSafe.copy(alpha = 0.1f) else ColorBlocked.copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(if (cfg.isCompliant) "✅ 合规" else "🔴 不合规", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (cfg.isCompliant) ColorSafe else ColorBlocked)
                        Spacer(Modifier.height(12.dp))
                        Text("媒体播放 FGS: ${if (cfg.hasMediaPlaybackService) "✅ 有" else "❌ 无"}", fontSize = 13.sp, color = Color.Gray)
                        Text("媒体处理 FGS: ${if (cfg.hasMediaProcessingService) "✅ 有" else "❌ 无"}", fontSize = 13.sp, color = Color.Gray)
                        if (cfg.missingTypes.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text("缺失: ${cfg.missingTypes.joinToString(", ")}", fontSize = 13.sp, color = ColorBlocked) }
                    }
                }
            }
        }
        if (state.fgsRecommendation.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("配置建议", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(state.fgsRecommendation, fontSize = 13.sp, color = ColorDegraded)
                    }
                }
            }
        }
        item {
            Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.GenerateFgsTemplate) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("生成 FGS 模板代码")
            }
        }
        if (state.generatedFgsTemplate != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("AndroidManifest.xml + Service.kt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("FGS Template", state.generatedFgsTemplate!!))
                            }) {
                                Icon(Icons.Default.ContentCopy, "复制", tint = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(state.generatedFgsTemplate!!, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = ColorSafe)
                    }
                }
            }
        }
    }
}

// ================================================================
// T6 Fallbacks
// ================================================================
@Composable
private fun FallbacksTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("替代方案知识库", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        state.fallbackSolutions.forEach { solution ->
            item {
                val expanded = state.expandedFallbackId == solution.id
                Card(modifier = Modifier.fillMaxWidth().clickable { viewModel.sendIntent(AudioComplianceIntent.ToggleFallbackDetail(solution.id)) }, colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(solution.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("适用: ${solution.applicableScenarios.joinToString(", ")}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Box(Modifier.clip(RoundedCornerShape(4.dp)).background(when (solution.implementationDifficulty) { "EASY" -> ColorSafe; "MEDIUM" -> ColorDegraded; else -> ColorBlocked }.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(solution.implementationDifficulty, fontSize = 11.sp, color = Color.White)
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
                        }
                        if (expanded) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text("效果描述", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(solution.effectDescription, fontSize = 13.sp, color = Color.White)
                            Spacer(Modifier.height(12.dp))
                            Text("实施步骤", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            solution.steps.forEachIndexed { i, step -> Text("${i + 1}. ${step.substringAfter(". ")}", fontSize = 12.sp, color = Color.Gray) }
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// T7 Regression
// ================================================================
@Composable
private fun RegressionTabContent(state: AudioComplianceState, viewModel: AudioComplianceViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("回归测试套件", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { viewModel.sendIntent(AudioComplianceIntent.RunTestSuite) },
                    enabled = !state.isRunningTests
                ) {
                    if (state.isRunningTests) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isRunningTests) "运行中..." else "运行测试")
                }
            }
        }
        item {
            Button(onClick = { viewModel.sendIntent(AudioComplianceIntent.SetAddTestDialogVisible(true)) }) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("新增用例")
            }
        }
        items(state.testCases, key = { it.id }) { testCase ->
            val result = state.testResults.find { it.caseId == testCase.id }
            val status = result?.status ?: testCase.lastResult
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ColorBackground)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(testCase.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Box(Modifier.clip(RoundedCornerShape(4.dp)).background(when (status) {
                            TestResultStatus.PASS -> ColorSafe.copy(alpha = 0.2f)
                            TestResultStatus.FAIL -> ColorBlocked.copy(alpha = 0.2f)
                            TestResultStatus.PENDING -> Color.Gray.copy(alpha = 0.2f)
                        }).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(when (status) {
                                TestResultStatus.PASS -> "✅ 通过"
                                TestResultStatus.FAIL -> "❌ 失败"
                                TestResultStatus.PENDING -> "⏳ 待测"
                            }, fontSize = 12.sp, color = when (status) {
                                TestResultStatus.PASS -> ColorSafe
                                TestResultStatus.FAIL -> ColorBlocked
                                TestResultStatus.PENDING -> Color.Gray
                            })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("场景: ${testCase.scenario}", fontSize = 12.sp, color = Color.Gray)
                    Text("预期: ${testCase.expectedBehavior}", fontSize = 12.sp, color = Color.Gray)
                    if (result != null) {
                        Spacer(Modifier.height(4.dp))
                        Text("结果: ${result.message}", fontSize = 11.sp, color = if (result.status == TestResultStatus.PASS) ColorSafe else ColorBlocked)
                    }
                }
            }
        }
    }
    if (state.isAddTestDialogVisible) {
        AddTestCaseDialogContent(
            onDismiss = { viewModel.sendIntent(AudioComplianceIntent.SetAddTestDialogVisible(false)) },
            onConfirm = { viewModel.sendIntent(AudioComplianceIntent.AddTestCase(it)) }
        )
    }
}

@Composable
private fun AddTestCaseDialogContent(onDismiss: () -> Unit, onConfirm: (AudioTestCase) -> Unit) {
    var name by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("") }
    var expected by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增测试用例") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, onValueChange = { name = it }, label = { Text("用例名称") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(scenario, onValueChange = { scenario = it }, label = { Text("测试场景") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(expected, onValueChange = { expected = it }, label = { Text("预期行为") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(AudioTestCase(
                        id = "custom_${System.currentTimeMillis()}",
                        name = name,
                        scenario = scenario,
                        expectedBehavior = expected,
                        verificationSteps = listOf("待补充")
                    ))
                }
            }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
