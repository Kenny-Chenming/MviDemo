package com.mvi.kenny.feature.adkandroid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ============================================================
 * AdkAndroidScreen — Google ADK for Android 主界面
 * ============================================================
 * PRD-292 | Google ADK for Android & Kotlin 开发工具包
 * Design: 工具型 App，4 Tab 结构
 *
 * Tab 0: 📚 学习中心（HomeScreen）
 * Tab 1: 🔧 配置实验室（LabScreen）
 * Tab 2: 💻 代码工坊（CodeScreen）
 * Tab 3: 🧪 实战演练（BenchmarkScreen）
 *
 * @param viewModel 状态管理 ViewModel
 * @param onNavigateToLab 跳转到配置实验室的回调
 */

// —————————————————————————————————————————————————————
// Colors — Gemini 主题色
// —————————————————————————————————————————————————————
private val GeminiBlue = Color(0xFF4285F4)
private val SuccessGreen = Color(0xFF34A853)
private val GeminiYellow = Color(0xFFFBBC04)
private val SurfaceLow = Color(0xFFF8F9FA)

/**
 * AdkAndroidScreen 主入口
 * —————————————————————————————————————————————————————
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdkAndroidScreen(
    viewModel: AdkAndroidViewModel = viewModel(),
    onNavigateToLab: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })

    // 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdkAndroidEffect.ShowToast -> { /* Toast handled externally */ }
                is AdkAndroidEffect.NavigateToLab -> onNavigateToLab()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLow)
    ) {
        // —————————————————————————————————————————————————————
        // Tab 指示器（Custom TabRow）
        // —————————————————————————————————————————————————————
        val tabs = listOf("📚 学习中心", "🔧 配置实验室", "💻 代码工坊", "🧪 实战演练")
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        // 同步 ViewModel state
                        viewModel.sendIntent(AdkAndroidIntent.SelectTab(index))
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // —————————————————————————————————————————————————————
        // HorizontalPager — 4 个 Tab 页面
        // —————————————————————————————————————————————————————
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LearningCenterTab(state, viewModel)
                1 -> LabTab(state, viewModel)
                2 -> CodeWorkshopTab(state, viewModel)
                3 -> BenchmarkTab(state, viewModel)
            }
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 0: 📚 学习中心（Learning Center）
// —————————————————————————————————————————————————————
@Composable
private fun LearningCenterTab(state: AdkAndroidState, viewModel: AdkAndroidViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card — 核心价值主张
        item {
            HeroCard(
                title = "Google ADK for Android",
                subtitle = "首个 Android 原生 On-Device AI Agent 开发框架",
                tags = listOf("On-Device", "Zero Cost", "Privacy-First"),
                onQuickStart = { viewModel.sendIntent(AdkAndroidIntent.SelectTab(1)) }
            )
        }

        // Feature Cards 标题
        item {
            Text(
                text = "核心功能模块",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Feature Cards 网格
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(520.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(state.featureCards) { card ->
                    FeatureCardItem(card)
                }
            }
        }
    }
}

/**
 * Hero Card — 主横幅
 * @param title 主标题
 * @param subtitle 副标题
 * @param tags 标签列表
 * @param onQuickStart 快速开始按钮回调
 */
@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    tags: List<String>,
    onQuickStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GeminiBlue
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onQuickStart,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("快速开始")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

/**
 * Feature Card 单项
 */
@Composable
private fun FeatureCardItem(card: AdkFeatureCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = card.icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = card.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 1: 🔧 配置实验室（Lab）
// —————————————————————————————————————————————————————
@Composable
private fun LabTab(state: AdkAndroidState, viewModel: AdkAndroidViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 环境检测面板
        item {
            EnvironmentCheckPanel(state, viewModel)
        }

        // 项目配置向导
        item {
            ProjectConfigWizard()
        }

        // Hybrid Orchestration 可视化
        item {
            HybridOrchestrationDiagram()
        }
    }
}

/**
 * 环境检测面板
 */
@Composable
private fun EnvironmentCheckPanel(state: AdkAndroidState, viewModel: AdkAndroidViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 环境检测",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val env = state.environment
            if (env.isChecking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("正在检测 ADK 环境...")
                }
            } else {
                EnvironmentCheckRow("Gemini Nano", env.geminiNanoAvailable)
                EnvironmentCheckRow("ML Kit GenAI", env.mlKitGenAiAvailable)
                EnvironmentCheckRow("ADK Version", env.adkVersion?.let { true } ?: false, env.adkVersion ?: "未知")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.sendIntent(AdkAndroidIntent.CheckEnvironment) },
                enabled = !env.isChecking
            ) {
                Text(if (env.isChecking) "检测中..." else "重新检测")
            }
        }
    }
}

@Composable
private fun EnvironmentCheckRow(
    label: String,
    available: Boolean?,
    value: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        when (available) {
            true -> Text("✅ 可用" + (value?.let { " ($it)" } ?: ""), color = SuccessGreen)
            false -> Text("❌ 不可用", color = Color.Red)
            null -> Text("⏳ 未检测", color = Color.Gray)
        }
    }
}

/**
 * 项目配置向导（静态展示）
 */
@Composable
private fun ProjectConfigWizard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚙️ 项目配置向导",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            listOf(
                "1️⃣ 添加 Gradle 依赖: adk-android:0.1.0",
                "2️⃣ 配置 AndroidManifest（若需要 Gemini Nano）",
                "3️⃣ 初始化 AdkAndroidAgent",
                "4️⃣ 定义 @Tool 自定义工具",
                "5️⃣ 启动 Agent 并处理 Session"
            ).forEach { step ->
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Hybrid Orchestration 可视化示意图（静态 Canvas）
 */
@Composable
private fun HybridOrchestrationDiagram() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val cloudColor = Color(0xFF4285F4)
            val deviceColor = Color(0xFF34A853)
            Text(
                text = "☁️ Hybrid Orchestration 架构",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height

                // Cloud node
                drawCircle(cloudColor, radius = 30f, center = Offset(w * 0.25f, h * 0.4f))
                drawCircle(Color.White, radius = 20f, center = Offset(w * 0.25f, h * 0.4f))
                drawLine(cloudColor, Offset(w * 0.25f + 30f, h * 0.4f), Offset(w * 0.45f, h * 0.4f), strokeWidth = 3f)

                // Device node
                drawCircle(deviceColor, radius = 30f, center = Offset(w * 0.75f, h * 0.4f))
                drawCircle(Color.White, radius = 20f, center = Offset(w * 0.75f, h * 0.4f))

                // Arrow down from orchestrator
                drawLine(Color.Gray, Offset(w * 0.5f, h * 0.2f), Offset(w * 0.5f, h * 0.1f), strokeWidth = 2f)
                drawLine(Color.Gray, Offset(w * 0.25f, h * 0.4f), Offset(w * 0.5f, h * 0.1f), strokeWidth = 1f)
                drawLine(Color.Gray, Offset(w * 0.75f, h * 0.4f), Offset(w * 0.5f, h * 0.1f), strokeWidth = 1f)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(cloudColor)
                    )
                    Text("云端协调器", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(deviceColor)
                    )
                    Text("端侧 Agent", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 2: 💻 代码工坊（Code Workshop）
// —————————————————————————————————————————————————————
@Composable
private fun CodeWorkshopTab(state: AdkAndroidState, viewModel: AdkAndroidViewModel) {
    val filteredSnippets = remember(state.codeSnippets, state.selectedCategory) {
        if (state.selectedCategory == null) state.codeSnippets
        else state.codeSnippets.filter { it.category == state.selectedCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 分类筛选
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.sendIntent(AdkAndroidIntent.FilterByCategory(null)) },
                    label = { Text("全部") }
                )
                SnippetCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategory == cat,
                        onClick = { viewModel.sendIntent(AdkAndroidIntent.FilterByCategory(cat)) },
                        label = { Text(cat.label) }
                    )
                }
            }
        }

        // 代码片段列表
        items(filteredSnippets) { snippet ->
            CodeSnippetCard(
                snippet = snippet,
                isCopied = state.copiedSnippetId == snippet.id,
                onCopy = { viewModel.sendIntent(AdkAndroidIntent.CopySnippet(snippet.id)) }
            )
        }
    }
}

/**
 * 代码片段卡片
 */
@Composable
private fun CodeSnippetCard(
    snippet: AdkCodeSnippet,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = snippet.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = snippet.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = if (isCopied) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 代码区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text(
                    text = snippet.code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFFD4D4D4),
                    maxLines = 8
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GeminiBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = snippet.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = GeminiBlue
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GeminiYellow.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = snippet.adkVersionRange,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

// —————————————————————————————————————————————————————
// Tab 3: 🧪 实战演练（Benchmark）
// —————————————————————————————————————————————————————
@Composable
private fun BenchmarkTab(state: AdkAndroidState, viewModel: AdkAndroidViewModel) {
    val completedCount = state.benchmarkScenarios.count { it.completed }
    val totalCount = state.benchmarkScenarios.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度环形图
        item {
            BenchmarkProgressCard(completedCount, totalCount)
        }

        // 场景列表
        items(state.benchmarkScenarios) { scenario ->
            BenchmarkScenarioCard(
                scenario = scenario,
                onStart = { viewModel.sendIntent(AdkAndroidIntent.StartBenchmark(scenario.id)) }
            )
        }
    }
}

/**
 * 基准测试进度环形图
 */
@Composable
private fun BenchmarkProgressCard(completed: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "测试进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completed / $total 场景已完成",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressCanvas(
                    progress = if (total > 0) completed.toFloat() / total else 0f,
                    size = 80f,
                    strokeWidth = 8f
                )
                Text(
                    text = "${(if (total > 0) completed * 100 / total else 0)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Canvas 绘制环形进度
 */
@Composable
private fun CircularProgressCanvas(progress: Float, size: Float, strokeWidth: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Canvas(modifier = Modifier.size(size.dp)) {
        val diameter = size - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        // Background arc
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = GeminiBlue,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * 基准测试场景卡片
 */
@Composable
private fun BenchmarkScenarioCard(
    scenario: BenchmarkScenario,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (scenario.completed)
                SuccessGreen.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scenario.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (scenario.completed && scenario.lastResult != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✅ ${scenario.lastResult}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                }
            }
            if (!scenario.completed) {
                IconButton(onClick = onStart) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "开始测试",
                        tint = GeminiBlue
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已完成",
                    tint = SuccessGreen
                )
            }
        }
    }
}
