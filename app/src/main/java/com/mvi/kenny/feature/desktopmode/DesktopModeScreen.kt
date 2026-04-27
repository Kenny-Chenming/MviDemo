package com.mvi.kenny.feature.desktopmode

// ================================================================
// DesktopModeScreen — Android 17 Desktop Mode 主界面
// ================================================================
// Main screen for Android 17 Desktop Mode development toolkit.
//
// PRD-170: Android 17 Desktop Mode 开发工具包
// Design Reference: memory/agency/designs/PRD-170-Android-17-Desktop-Mode-开发工具包.md
//
// Features (8 Tabs):
//   1. Overview — Dashboard with quality gauge, radar chart, tool entry cards
//   2. Quartz API — Desktop Windowing API guide with code examples
//   3. AI Reflect — Reflect Layer compliance status and detection
//   4. CI Scan — Floating window layout issue scan results
//   5. zRAM Test — Hibernation/wake behavior test
//   6. Decision Guide — Desktop Mode vs Large Screen decision tree
//   7. Floating Windows — UX design spec for floating windows
//   8. Quality Score — Four-dimensional radar chart and improvement suggestions
// ————————————————————————————————————————————————————————————

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.desktopmode.DesktopModeIntent
import com.mvi.kenny.feature.desktopmode.DesktopModeState
import kotlinx.coroutines.flow.Flow

// ================================================================
// Design Tokens (from design doc)
// ================================================================

/** Primary color — 科技紫，Desktop Mode 桌面生产力主题 */
private val DesktopPrimary = Color(0xFF6B4EFF)

/** Secondary color — 桌面蓝，Floating Window 和多窗口相关元素 */
private val DesktopSecondary = Color(0xFF00D9FF)

/** Background color — 深空背景 */
private val DesktopBackground = Color(0xFF0F0F1A)

/** Surface color — 卡片背景 */
private val DesktopSurface = Color(0xFF1A1A2E)

/** OnSurface color — 主文本 */
private val DesktopOnSurface = Color(0xFFE8E8F0)

/** OnSurfaceVariant — 次要文本 */
private val DesktopOnSurfaceVariant = Color(0xFF8888A0)

/** Error color */
private val DesktopError = Color(0xFFFF6B6B)

/** Success color */
private val DesktopSuccess = Color(0xFF4ADE80)

// ================================================================
// DesktopModeScreen — Main Screen Composable
// ================================================================

/**
 * ============================================================
 * DesktopModeScreen — Android 17 Desktop Mode 主界面
 * ============================================================
 *
 * @param state Current MVI state
 * @param onIntent Intent sender
 * @param effect Effect collector (for LaunchedEffect)
 */
@Composable
fun DesktopModeScreen(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit,
    effect: Flow<DesktopModeEffect>? = null
) {
    // Collect effects
    LaunchedEffect(effect) {
        effect?.collect { e ->
            when (e) {
                is DesktopModeEffect.ShowToast -> { /* handled externally */ }
                is DesktopModeEffect.ReportReady -> { /* handled externally */ }
                is DesktopModeEffect.ShowError -> { /* handled externally */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DesktopBackground)
    ) {
        // Tab Indicator
        TabLayout(
            activeTab = state.activeTab,
            onTabSelected = { onIntent(DesktopModeIntent.SelectTab(it)) }
        )

        // Tab Content
        AnimatedContent(
            targetState = state.activeTab,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "TabContent"
        ) { tab ->
            when (tab) {
                DesktopTab.OVERVIEW -> OverviewTab(state, onIntent)
                DesktopTab.QUARTZ_API -> QuartzAPITab(state, onIntent)
                DesktopTab.AI_REFLECT -> AIReflectTab(state, onIntent)
                DesktopTab.CI_SCAN -> CIScanTab(state, onIntent)
                DesktopTab.ZRAM_TEST -> ZRAMTestTab(state, onIntent)
                DesktopTab.DECISION_GUIDE -> DecisionGuideTab(state, onIntent)
                DesktopTab.FLOATING_WINDOWS -> FloatingWindowsTab(state, onIntent)
                DesktopTab.QUALITY_SCORE -> QualityScoreTab(state, onIntent)
            }
        }
    }
}

// ================================================================
// Tab Layout
// ================================================================

/**
 * TabLayout — Desktop Mode Tab 切换指示器
 *
 * @param activeTab Currently active tab
 * @param onTabSelected Callback when tab is selected
 */
@Composable
private fun TabLayout(
    activeTab: DesktopTab,
    onTabSelected: (DesktopTab) -> Unit
) {
    val tabs = DesktopTab.entries
    val selectedIndex = tabs.indexOf(activeTab)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = DesktopSurface,
        contentColor = DesktopOnSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = DesktopPrimary,
                height = 3.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selectedContentColor = DesktopPrimary,
                unselectedContentColor = DesktopOnSurfaceVariant
            )
        }
    }
}

// ================================================================
// Tab 1: Overview Dashboard
// ================================================================

/**
 * OverviewTab — Desktop Mode 首页总览
 *
 * Contains: I/O 2026 banner, quality score gauge, 8 tool entry cards, recent scan results
 */
@Composable
private fun OverviewTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // I/O 2026 Banner
        item {
            IO2026Banner()
        }

        // Quality Score Gauge
        item {
            QualityScoreGaugeCard(score = state.qualityScore)
        }

        // Radar Score Summary
        item {
            RadarSummaryCard(radarScores = state.radarScores)
        }

        // Tool Entry Cards (2 columns)
        item {
            Text(
                text = "工具入口 / Tool Entry",
                style = MaterialTheme.typography.titleMedium,
                color = DesktopOnSurface,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val toolEntries = listOf(
                    Triple(Icons.Default.Code, "Quartz API", DesktopTab.QUARTZ_API),
                    Triple(Icons.Default.Radar, "AI Reflect", DesktopTab.AI_REFLECT),
                    Triple(Icons.Default.BugReport, "CI 扫描", DesktopTab.CI_SCAN),
                    Triple(Icons.Default.Memory, "zRAM 测试", DesktopTab.ZRAM_TEST),
                    Triple(Icons.Default.AccountTree, "适配决策", DesktopTab.DECISION_GUIDE),
                    Triple(Icons.Default.Hardware, "浮动窗口", DesktopTab.FLOATING_WINDOWS),
                    Triple(Icons.Default.Analytics, "质量评分", DesktopTab.QUALITY_SCORE)
                )
                items(toolEntries) { (icon, label, tab) ->
                    ToolEntryCard(
                        icon = icon,
                        label = label,
                        onClick = { onIntent(DesktopModeIntent.SelectTab(tab)) }
                    )
                }
            }
        }

        // Recent CI Results Summary
        if (state.ciResults.isNotEmpty()) {
            item {
                Text(
                    text = "最近 CI 扫描结果 / Recent CI Results",
                    style = MaterialTheme.typography.titleMedium,
                    color = DesktopOnSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(state.ciResults.take(3)) { result ->
                CIResultCard(result = result, compact = true)
            }
        }
    }
}

/**
 * I/O 2026 Countdown Banner
 */
@Composable
private fun IO2026Banner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DesktopPrimary.copy(alpha = 0.3f), DesktopSecondary.copy(alpha = 0.3f))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Google I/O 2026",
                        style = MaterialTheme.typography.titleMedium,
                        color = DesktopOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "5月19-20日 · Desktop Mode 专场预告",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesktopOnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .background(DesktopPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "即将开启",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * ToolEntryCard — 工具入口卡片
 */
@Composable
private fun ToolEntryCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DesktopPrimary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DesktopOnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * QualityScoreGaugeCard — 质量分仪表盘卡片
 */
@Composable
private fun QualityScoreGaugeCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Desktop Mode 质量评分",
                style = MaterialTheme.typography.titleMedium,
                color = DesktopOnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            QualityGauge(score = score)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    score >= 80 -> "优秀 — App 在 Desktop Mode 下表现良好"
                    score >= 60 -> "良好 — 存在少量可优化项"
                    score >= 40 -> "一般 — 建议按以下清单优化"
                    else -> "较差 — 需要重点优化 Desktop Mode 适配"
                },
                style = MaterialTheme.typography.bodySmall,
                color = DesktopOnSurfaceVariant
            )
        }
    }
}

/**
 * QualityGauge — 弧形仪表盘
 *
 * @param score Score (0-100)
 */
@Composable
private fun QualityGauge(score: Int) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "GaugeAnimation"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val sweepAngle = 240f
            val startAngle = 150f

            // Background arc
            drawArc(
                color = Color(0xFF2A2A4A),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )

            // Score arc
            val scoreAngle = (animatedScore / 100f) * sweepAngle
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(DesktopError, DesktopPrimary, DesktopSuccess)
                ),
                startAngle = startAngle,
                sweepAngle = scoreAngle,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                color = DesktopOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "/ 100",
                style = MaterialTheme.typography.bodySmall,
                color = DesktopOnSurfaceVariant
            )
        }
    }
}

/**
 * RadarSummaryCard — 雷达评分摘要卡片
 */
@Composable
private fun RadarSummaryCard(radarScores: RadarScores) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "四维雷达评分",
                style = MaterialTheme.typography.titleSmall,
                color = DesktopOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RadarScoreItem(label = "多窗口", score = radarScores.multiWindow)
                RadarScoreItem(label = "状态恢复", score = radarScores.stateRecovery)
                RadarScoreItem(label = "输入适配", score = radarScores.inputAdaptation)
                RadarScoreItem(label = "视觉布局", score = radarScores.visualLayout)
            }
        }
    }
}

/**
 * RadarScoreItem — 单项雷达分展示
 */
@Composable
private fun RadarScoreItem(label: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    when {
                        score >= 70 -> DesktopSuccess.copy(alpha = 0.2f)
                        score >= 40 -> DesktopPrimary.copy(alpha = 0.2f)
                        else -> DesktopError.copy(alpha = 0.2f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    score >= 70 -> DesktopSuccess
                    score >= 40 -> DesktopPrimary
                    else -> DesktopError
                },
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DesktopOnSurfaceVariant
        )
    }
}

// ================================================================
// Tab 2: Quartz Compositor API Guide
// ================================================================

/**
 * QuartzAPITab — Quartz Compositor 窗口 API 开发指南
 */
@Composable
private fun QuartzAPITab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Quartz Compositor API", subtitle = "Android 17 Desktop Windowing API")
        }

        item {
            APICodeBlock(
                title = "1. 获取 WindowManager 实例",
                code = """
// Quartz WindowManager (API 35+)
val windowManager = WindowManager.getInstance(context)

// 获取当前窗口信息
val windowInfo = windowManager.getWindowInfo(activity)
val windowBounds = windowInfo.bounds  // 当前窗口边界
val windowSize = windowInfo.size      // 当前窗口尺寸 (dp)
                """.trimIndent()
            )
        }

        item {
            APICodeBlock(
                title = "2. 监听窗口尺寸变化",
                code = """
// 使用 WindowInfoTracker 监听窗口变化
val windowInfoTracker = WindowInfoTracker.getOrCreate(context)

 lifecycleScope.launch {
     windowInfoTracker.windowLayoutInfo(activity)
         .collect { layoutInfo ->
             val bounds = layoutInfo.displayFeatures
                 .filterIsInstance<Bounds>()
                 .firstOrNull()
             // 处理新的窗口边界
         }
 }
                """.trimIndent()
            )
        }

        item {
            APICodeBlock(
                title = "3. 浮动窗口最小尺寸约束",
                code = """
// AndroidManifest.xml 中声明最小窗口尺寸
<meta-data
    android:name="android.window.PROPERTY_ACTIVITY_EMBEDDING_SPLITS_ENABLED"
    android:value="true" />

// 或在代码中动态设置
window.attributes = window.attributes.apply {
    minWidth = 220  // dp
    minHeight = 275 // dp
}
                """.trimIndent()
            )
        }

        item {
            ComparisonTable()
        }

        item {
            MigrationChecklistCard(
                checklist = state.checklist.filter { it.category == "WindowAPI" },
                onToggle = { onIntent(DesktopModeIntent.ToggleCheckItem(it)) }
            )
        }
    }
}

/**
 * APICodeBlock — 代码示例展示卡片
 */
@Composable
private fun APICodeBlock(title: String, code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = DesktopSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DesktopOnSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = Color(0xFF79C0FF),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * ComparisonTable — 传统 vs Quartz 对比表
 */
@Composable
private fun ComparisonTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "传统 Multi-Window vs Quartz Compositor",
                style = MaterialTheme.typography.titleSmall,
                color = DesktopOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Table header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("特性", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant, modifier = Modifier.weight(1f))
                Text("传统", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant, modifier = Modifier.weight(1f))
                Text("Quartz", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DesktopOnSurfaceVariant.copy(alpha = 0.3f))

            val rows = listOf(
                Triple("窗口数量", "最多2个", "无限制（浮动窗口）"),
                Triple("最小尺寸", "不支持", "220dp × 275dp"),
                Triple("窗口叠加", "分屏固定", "自由拖动"),
                Triple("Resize", "固定比例", "任意比例缩放"),
                Triple("API级别", "API 7+", "API 35+")
            )

            rows.forEach { (feature, traditional, quartz) ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(feature, style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface, modifier = Modifier.weight(1f))
                    Text(traditional, style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant, modifier = Modifier.weight(1f))
                    Text(quartz, style = MaterialTheme.typography.bodySmall, color = DesktopPrimary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ================================================================
// Tab 3: AI Reflect Layer
// ================================================================

/**
 * AIReflectTab — AI Reflect Layer 合规检测
 */
@Composable
private fun AIReflectTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "AI Reflect Layer", subtitle = "AI 反射层 — 自动将移动 UI 适配为桌面优化布局")
        }

        // Reflect Status Card
        item {
            ReflectStatusCard(
                status = state.reflectStatus,
                isChecking = state.isReflectChecking,
                onCheck = { onIntent(DesktopModeIntent.StartReflectCheck) }
            )
        }

        // Common Failure Scenarios
        item {
            Text(
                text = "常见 Reflect 失败场景",
                style = MaterialTheme.typography.titleSmall,
                color = DesktopOnSurface
            )
        }

        items(
            listOf(
                "固定像素尺寸 (px)" to "使用 dp/sp 替代，或使用 ConstraintLayout 自适应",
                "超出容器边界" to "使用 wrap_content 和 maxWidth/maxHeight",
                "绝对定位布局" to "改用链式布局或 Grid",
                "固定宽高比元素" to "使用 aspectRatio + matchParentSize"
            )
        ) { (scenario, fix) ->
            ReflectFailureCard(scenario = scenario, fix = fix)
        }

        // Checklist
        item {
            MigrationChecklistCard(
                checklist = state.checklist.filter { it.category == "Reflect" },
                onToggle = { onIntent(DesktopModeIntent.ToggleCheckItem(it)) }
            )
        }
    }
}

/**
 * ReflectStatusCard — Reflect 适配状态卡片
 */
@Composable
private fun ReflectStatusCard(
    status: ReflectStatus,
    isChecking: Boolean,
    onCheck: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                when (status) {
                                    ReflectStatus.ADAPTED -> DesktopSuccess.copy(alpha = 0.2f)
                                    ReflectStatus.FAILED -> DesktopError.copy(alpha = 0.2f)
                                    ReflectStatus.REFLECTING -> DesktopPrimary.copy(alpha = 0.2f)
                                    else -> DesktopOnSurfaceVariant.copy(alpha = 0.2f)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DesktopPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = status.emoji,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Reflect 适配状态",
                            style = MaterialTheme.typography.titleMedium,
                            color = DesktopOnSurface
                        )
                        Text(
                            text = if (isChecking) "检测中..." else status.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DesktopOnSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onCheck,
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = DesktopPrimary)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("开始检测")
                    }
                }
            }
        }
    }
}

/**
 * ReflectFailureCard — Reflect 失败场景卡片
 */
@Composable
private fun ReflectFailureCard(scenario: String, fix: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = DesktopError,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = scenario,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesktopOnSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "→ $fix",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesktopSuccess
                )
            }
        }
    }
}

// ================================================================
// Tab 4: CI Scan
// ================================================================

/**
 * CIScanTab — Desktop Mode CI 合规检测
 */
@Composable
private fun CIScanTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "CI 合规扫描", subtitle = "Gradle 插件扫描 — 浮动窗口布局问题检测")
        }

        // Scan Control Card
        item {
            ScanControlCard(
                isScanning = state.isScanning,
                scanProgress = state.scanProgress,
                resultCount = state.ciResults.size,
                onScan = { onIntent(DesktopModeIntent.StartCIScan) }
            )
        }

        // CI Results
        if (state.ciResults.isNotEmpty()) {
            item {
                Text(
                    text = "检测结果 (${state.ciResults.size} 项)",
                    style = MaterialTheme.typography.titleSmall,
                    color = DesktopOnSurface
                )
            }

            items(state.ciResults) { result ->
                CIResultCard(result = result, compact = false)
            }
        }

        if (state.ciResults.isEmpty() && !state.isScanning) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.BugReport,
                    message = "点击上方「开始扫描」检测 Desktop Mode 布局问题"
                )
            }
        }
    }
}

/**
 * ScanControlCard — CI 扫描控制卡片
 */
@Composable
private fun ScanControlCard(
    isScanning: Boolean,
    scanProgress: Float,
    resultCount: Int,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
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
                        text = "CI 合规扫描",
                        style = MaterialTheme.typography.titleMedium,
                        color = DesktopOnSurface
                    )
                    Text(
                        text = if (isScanning) "扫描中..." else "$resultCount 项问题待修复",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesktopOnSurfaceVariant
                    )
                }
                Button(
                    onClick = onScan,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = DesktopPrimary)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("开始扫描")
                    }
                }
            }

            if (isScanning) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = DesktopPrimary,
                    trackColor = DesktopOnSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(scanProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = DesktopOnSurfaceVariant
                )
            }
        }
    }
}

/**
 * CIResultCard — CI 检测结果卡片
 */
@Composable
private fun CIResultCard(result: CIResult, compact: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(if (compact) 12.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            when (result.severity) {
                                "ERROR" -> DesktopError.copy(alpha = 0.2f)
                                "WARNING" -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                else -> DesktopOnSurfaceVariant.copy(alpha = 0.2f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.severity,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (result.severity) {
                            "ERROR" -> DesktopError
                            "WARNING" -> Color(0xFFFF9800)
                            else -> DesktopOnSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.issueType.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = DesktopOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = DesktopOnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${result.filePath}:${result.lineNumber}",
                style = MaterialTheme.typography.bodySmall,
                color = DesktopOnSurfaceVariant,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = DesktopSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = result.suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesktopSuccess
                )
            }
        }
    }
}

// ================================================================
// Tab 5: zRAM Hibernation Test
// ================================================================

/**
 * ZRAMTestTab — zRAM 休眠/唤醒行为测试
 */
@Composable
private fun ZRAMTestTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "zRAM Hibernation Test", subtitle = "内存休眠与唤醒 — 状态持久化验证")
        }

        // Test Control Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
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
                                text = "zRAM 休眠/唤醒测试",
                                style = MaterialTheme.typography.titleMedium,
                                color = DesktopOnSurface
                            )
                            Text(
                                text = "测试 App 状态在 zRAM 休眠后是否正确恢复",
                                style = MaterialTheme.typography.bodySmall,
                                color = DesktopOnSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onIntent(DesktopModeIntent.TestZRAMHibernation) },
                            enabled = !state.isZramTesting,
                            colors = ButtonDefaults.buttonColors(containerColor = DesktopPrimary)
                        ) {
                            if (state.isZramTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("开始测试")
                            }
                        }
                    }
                }
            }
        }

        // RAM Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("模拟设备 RAM", style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface)
                        Text(
                            text = "${state.deviceRAM} MB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesktopPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = state.deviceRAM.toFloat(),
                        onValueChange = { onIntent(DesktopModeIntent.SetDeviceRAM(it.toInt())) },
                        valueRange = 4096f..16384f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = DesktopPrimary,
                            activeTrackColor = DesktopPrimary,
                            inactiveTrackColor = DesktopOnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("4GB", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                        Text("8GB", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                        Text("16GB", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                    }
                }
            }
        }

        // Test Result
        state.zramTestResult?.let { result ->
            item {
                ZRAMResultCard(result = result)
            }
        }

        // Checklist
        item {
            MigrationChecklistCard(
                checklist = state.checklist.filter { it.category == "zRAM" },
                onToggle = { onIntent(DesktopModeIntent.ToggleCheckItem(it)) }
            )
        }
    }
}

/**
 * ZRAMResultCard — zRAM 测试结果卡片
 */
@Composable
private fun ZRAMResultCard(result: ZRAMTestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "测试结果",
                    style = MaterialTheme.typography.titleMedium,
                    color = DesktopOnSurface
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (result.stateConsistent) DesktopSuccess.copy(alpha = 0.2f) else DesktopError.copy(alpha = 0.2f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (result.stateConsistent) "状态一致 ✅" else "状态不一致 ❌",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (result.stateConsistent) DesktopSuccess else DesktopError
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("休眠耗时", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                    Text("${result.hibernationDurationMs} ms", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("唤醒耗时", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                    Text("${result.wakeDurationMs} ms", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                }
            }

            if (result.issues.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                result.issues.forEach { issue ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Default.Warning, null, tint = DesktopError, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(issue, style = MaterialTheme.typography.bodySmall, color = DesktopError)
                    }
                }
            }
        }
    }
}

// ================================================================
// Tab 6: Decision Guide
// ================================================================

/**
 * DecisionGuideTab — Desktop Mode vs 大屏适配决策指南
 */
@Composable
private fun DecisionGuideTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "适配决策指南", subtitle = "Desktop Mode vs 传统大屏适配 — 优先级与精力分配")
        }

        // Decision Analysis Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "智能决策分析",
                        style = MaterialTheme.typography.titleMedium,
                        color = DesktopOnSurface
                    )
                    Text(
                        text = "基于 Checklist 完成度自动推荐适配策略",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesktopOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onIntent(DesktopModeIntent.RunDecisionAnalysis) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DesktopPrimary)
                    ) {
                        Icon(Icons.Default.AccountTree, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("运行决策分析")
                    }
                }
            }
        }

        // Decision Result
        state.decisionResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "推荐方案",
                            style = MaterialTheme.typography.titleSmall,
                            color = DesktopOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.recommendedApproach,
                            style = MaterialTheme.typography.titleMedium,
                            color = DesktopPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Box(modifier = Modifier.background(DesktopPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("优先级: ${result.priority}", style = MaterialTheme.typography.bodySmall, color = DesktopPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.background(DesktopSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("预计: ${result.effortEstimate}", style = MaterialTheme.typography.bodySmall, color = DesktopSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = result.reasoning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DesktopOnSurface
                        )
                    }
                }
            }
        }

        // Decision Tree Visual
        item {
            DecisionTreeCard()
        }

        // Scenario Comparison Cards
        item {
            Text(
                text = "场景对比",
                style = MaterialTheme.typography.titleSmall,
                color = DesktopOnSurface
            )
        }

        items(
            listOf(
                Triple(
                    "Desktop Mode 原生适配",
                    "需要完整适配 Quartz Compositor API，适合已有桌面级交互的 App",
                    DesktopPrimary
                ),
                Triple(
                    "AI Reflect Layer 依赖",
                    "不修改代码，依赖 AI 自动适配，适合快速上线桌面端",
                    DesktopSecondary
                ),
                Triple(
                    "传统大屏适配 (sw≥600dp)",
                    "优先适配平板折叠屏，Desktop Mode 自然受益",
                    DesktopSuccess
                )
            )
        ) { (title, desc, color) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(4.dp, 40.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface, fontWeight = FontWeight.Medium)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * DecisionTreeCard — 决策树可视化卡片
 */
@Composable
private fun DecisionTreeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("决策树 / Decision Tree", style = MaterialTheme.typography.titleSmall, color = DesktopOnSurface)
            Spacer(modifier = Modifier.height(12.dp))

            // Tree visualization
            Column {
                DecisionTreeNode(text = "App 类型？", isRoot = true)
                Row(modifier = Modifier.padding(start = 24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DesktopOnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("工具类 App → Desktop Mode 优先", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface)
                }
                Row(modifier = Modifier.padding(start = 24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DesktopOnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("内容消费 App → 大屏适配优先", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface)
                }
                Row(modifier = Modifier.padding(start = 24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DesktopOnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Text("企业级 App → 两者兼顾", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface)
                }
            }
        }
    }
}

/**
 * DecisionTreeNode — 决策树节点
 */
@Composable
private fun DecisionTreeNode(text: String, isRoot: Boolean = false) {
    Box(
        modifier = Modifier
            .background(if (isRoot) DesktopPrimary.copy(alpha = 0.2f) else DesktopSurface, RoundedCornerShape(4.dp))
            .border(1.dp, DesktopPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface, fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Normal)
    }
}

// ================================================================
// Tab 7: Floating Windows
// ================================================================

/**
 * FloatingWindowsTab — 浮动窗口触控/键鼠交互设计规范
 */
@Composable
private fun FloatingWindowsTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "Floating Windows 设计规范", subtitle = "浮动窗口 — 触控/键鼠交互 UX 设计指南")
        }

        // Window Size Spec
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("窗口尺寸规范", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("最小尺寸", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                            Text("220 × 275 dp", style = MaterialTheme.typography.titleMedium, color = DesktopPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("默认尺寸", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                            Text("450 × 600 dp", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("最大尺寸", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                            Text("全屏", style = MaterialTheme.typography.titleMedium, color = DesktopSecondary)
                        }
                    }
                }
            }
        }

        // Input Adaptation Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("输入设备适配", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        Triple("键盘支持", "Tab 导航 / Enter 确认 / Esc 取消 / 快捷键", Icons.Default.Keyboard),
                        Triple("鼠标支持", "Hover 状态 / 右键菜单 / 滚轮支持", Icons.Default.Hardware),
                        Triple("触控支持", "触摸手势 / 长按 / 双指缩放", Icons.Default.TouchApp)
                    ).forEach { (title, desc, icon) ->
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            Icon(icon, null, tint = DesktopPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface, fontWeight = FontWeight.Medium)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Best Practices Checklist
        item {
            MigrationChecklistCard(
                checklist = state.checklist.filter { it.category == "Input" || it.category == "Layout" },
                onToggle = { onIntent(DesktopModeIntent.ToggleCheckItem(it)) }
            )
        }
    }
}

// ================================================================
// Tab 8: Quality Score
// ================================================================

/**
 * QualityScoreTab — Desktop Mode 应用质量评分
 */
@Composable
private fun QualityScoreTab(
    state: DesktopModeState,
    onIntent: (DesktopModeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "质量评分详情", subtitle = "四维雷达图 + 改进建议")
        }

        // Radar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Desktop Mode 四维质量雷达", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    RadarChart(
                        scores = state.radarScores,
                        modifier = Modifier.size(240.dp)
                    )
                }
            }
        }

        // Score Breakdown
        item {
            ScoreBreakdownCard(radarScores = state.radarScores)
        }

        // Improvement Suggestions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesktopSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("改进建议", style = MaterialTheme.typography.titleMedium, color = DesktopOnSurface)
                        Button(
                            onClick = { onIntent(DesktopModeIntent.ExportReport) },
                            colors = ButtonDefaults.buttonColors(containerColor = DesktopPrimary)
                        ) {
                            Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出报告")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val suggestions = buildList {
                        if (state.radarScores.multiWindow < 70) add("优先处理多窗口支持 — 添加 Quartz WindowManager API 支持")
                        if (state.radarScores.stateRecovery < 70) add("加强状态恢复能力 — 完善 onSaveInstanceState 实现")
                        if (state.radarScores.inputAdaptation < 70) add("完善键鼠交互 — 添加键盘快捷键和鼠标 hover 状态")
                        if (state.radarScores.visualLayout < 70) add("优化视觉布局 — 使用自适应布局替代固定尺寸")
                    }

                    if (suggestions.isEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = DesktopSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("所有维度均达标！App 在 Desktop Mode 下表现优秀", style = MaterialTheme.typography.bodyMedium, color = DesktopSuccess)
                        }
                    } else {
                        suggestions.forEach { suggestion ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.Info, null, tint = DesktopPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(suggestion, style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * RadarChart — 四维雷达图
 */
@Composable
private fun RadarChart(scores: RadarScores, modifier: Modifier = Modifier) {
    val animatedMultiWindow by animateFloatAsState(targetValue = scores.multiWindow.toFloat(), animationSpec = tween(800), label = "")
    val animatedStateRecovery by animateFloatAsState(targetValue = scores.stateRecovery.toFloat(), animationSpec = tween(800), label = "")
    val animatedInputAdaptation by animateFloatAsState(targetValue = scores.inputAdaptation.toFloat(), animationSpec = tween(800), label = "")
    val animatedVisualLayout by animateFloatAsState(targetValue = scores.visualLayout.toFloat(), animationSpec = tween(800), label = "")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2 - 24.dp.toPx()
            val labels = listOf("多窗口", "状态恢复", "输入适配", "视觉布局")
            val values = listOf(animatedMultiWindow, animatedStateRecovery, animatedInputAdaptation, animatedVisualLayout)

            // Draw grid circles
            for (i in 1..4) {
                val r = maxRadius * i / 4
                drawCircle(
                    color = Color(0xFF3A3A5A),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw axes
            for (i in labels.indices) {
                val angle = Math.toRadians((270.0 + i * 90.0))
                val x = center.x + maxRadius * kotlin.math.cos(angle).toFloat()
                val y = center.y + maxRadius * kotlin.math.sin(angle).toFloat()
                drawLine(
                    color = Color(0xFF3A3A5A),
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw data polygon
            val path = Path()
            for (i in labels.indices) {
                val angle = Math.toRadians((270.0 + i * 90.0))
                val value = values[i] / 100f
                val x = center.x + maxRadius * value * kotlin.math.cos(angle).toFloat()
                val y = center.y + maxRadius * value * kotlin.math.sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = DesktopPrimary.copy(alpha = 0.4f)
            )
            drawPath(
                path = path,
                color = DesktopPrimary,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Labels
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("多窗口\n${scores.multiWindow}", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(60.dp))
            Row {
                Text("状态恢复\n${scores.stateRecovery}", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(60.dp))
                Text("输入适配\n${scores.inputAdaptation}", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(60.dp))
            Text("视觉布局\n${scores.visualLayout}", style = MaterialTheme.typography.bodySmall, color = DesktopOnSurface, textAlign = TextAlign.Center)
        }
    }
}

/**
 * ScoreBreakdownCard — 分数明细卡片
 */
@Composable
private fun ScoreBreakdownCard(radarScores: RadarScores) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("分数明细", style = MaterialTheme.typography.titleSmall, color = DesktopOnSurface)
            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                Triple("多窗口支持", radarScores.multiWindow, "WindowAPI 实现度"),
                Triple("状态恢复", radarScores.stateRecovery, "zRAM 休眠恢复能力"),
                Triple("输入适配", radarScores.inputAdaptation, "键鼠/触控交互完善度"),
                Triple("视觉布局", radarScores.visualLayout, "自适应布局实现度")
            ).forEach { (label, score, desc) ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = DesktopOnSurface)
                        Text("$score / 100", style = MaterialTheme.typography.bodyMedium, color = when {
                            score >= 70 -> DesktopSuccess
                            score >= 40 -> DesktopPrimary
                            else -> DesktopError
                        }, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = when {
                            score >= 70 -> DesktopSuccess
                            score >= 40 -> DesktopPrimary
                            else -> DesktopError
                        },
                        trackColor = DesktopOnSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = DesktopOnSurfaceVariant)
                }
            }
        }
    }
}

// ================================================================
// Shared Components
// ================================================================

/**
 * SectionHeader — 区块标题
 */
@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DesktopOnSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = DesktopOnSurfaceVariant
        )
    }
}

/**
 * EmptyStateCard — 空状态占位卡片
 */
@Composable
private fun EmptyStateCard(icon: ImageVector, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
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
                tint = DesktopOnSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = DesktopOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * MigrationChecklistCard — 迁移检查清单卡片
 */
@Composable
private fun MigrationChecklistCard(
    checklist: List<CheckItem>,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DesktopSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("适配检查清单", style = MaterialTheme.typography.titleSmall, color = DesktopOnSurface)
                val checked = checklist.count { it.isChecked }
                Text(
                    text = "$checked / ${checklist.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (checked == checklist.size) DesktopSuccess else DesktopOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            checklist.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item.id) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { onToggle(item.id) },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = DesktopSuccess,
                            uncheckedColor = DesktopOnSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isChecked) DesktopSuccess else DesktopOnSurface
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = DesktopOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
