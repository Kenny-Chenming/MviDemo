package com.mvi.kenny.feature.journeys

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// JourneysDashboardScreen — Journeys E2E 测试工具包主仪表盘
// PRD-099 | Journeys for Android Studio 自动化 E2E 测试工具包
// =============================================================
/**
 * Journeys Dashboard Screen / Journeys 仪表盘屏幕
 *
 * Main entry point for the Journeys E2E Testing Toolkit.
 * Displays:
 * - Statistics overview (total/passed/failed/running)
 * - Journey list with filtering
 * - Quick action buttons
 *
 * @param viewModel JourneysViewModel instance / JourneysViewModel 实例
 * @param onNavigateToEditor Callback to navigate to Journey editor / 导航到编辑器回调
 * @param onNavigateToTemplates Callback to navigate to template library / 导航到模板库回调
 * @param onNavigateToCIConfig Callback to navigate to CI/CD config / 导航到 CI 配置回调
 * @param onNavigateToComparison Callback to navigate to framework comparison / 导航到框架对比回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneysDashboardScreen(
    viewModel: JourneysViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToCIConfig: () -> Unit,
    onNavigateToComparison: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Local tab state / 本地 Tab 状态
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Journeys E2E 测试工具包",
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.processIntent(JourneysIntent.LoadJourneys) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh / 刷新",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onNavigateToCIConfig) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "CI Config / CI 配置",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.processIntent(JourneysIntent.CreateJourney(null)) },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Journey / 新建 Journey")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Tab Row / Tab 行
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF6750A4)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Dashboard / 仪表盘") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Templates / 模板库") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("CI/CD") }
                )
            }

            when (selectedTabIndex) {
                0 -> DashboardTab(
                    state = state,
                    onIntent = viewModel::processIntent,
                    onNavigateToEditor = onNavigateToEditor,
                    onNavigateToComparison = onNavigateToComparison
                )
                1 -> TemplatesTab(
                    state = state,
                    onIntent = viewModel::processIntent,
                    onNavigateToTemplates = onNavigateToTemplates
                )
                2 -> CIConfigTab(
                    state = state,
                    onIntent = viewModel::processIntent,
                    onNavigateToCIConfig = onNavigateToCIConfig
                )
            }
        }
    }
}

// ================================================================
// DashboardTab — 仪表盘 Tab
// ================================================================
/**
 * Dashboard tab content / 仪表盘 Tab 内容
 */
@Composable
private fun DashboardTab(
    state: JourneysState,
    onIntent: (JourneysIntent) -> Unit,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToComparison: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Overview Cards / 统计概览卡片
        item {
            StatisticsSection(state = state)
        }

        // Filter Chips / 筛选 Chips
        item {
            FilterChipsRow(
                currentFilter = state.filterStatus,
                onFilterChange = { onIntent(JourneysIntent.FilterJourneys(it)) }
            )
        }

        // Journey List / Journey 列表
        val filteredJourneys = when (state.filterStatus) {
            FilterStatus.ALL -> state.journeys
            else -> state.journeys.filter {
                when (state.filterStatus) {
                    FilterStatus.PASSED -> it.status == StepStatus.PASSED
                    FilterStatus.FAILED -> it.status == StepStatus.FAILED
                    FilterStatus.RUNNING -> it.status == StepStatus.RUNNING
                    else -> true
                }
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6750A4))
                }
            }
        } else if (filteredJourneys.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No Journeys found / 未找到 Journey",
                    subMessage = "Create your first Journey to get started / 创建你的第一个 Journey"
                )
            }
        } else {
            items(filteredJourneys, key = { it.id }) { journey ->
                JourneyCard(
                    journey = journey,
                    onOpen = { onIntent(JourneysIntent.OpenJourney(journey.id)) },
                    onRun = { onIntent(JourneysIntent.RunJourney(journey.id)) },
                    onDelete = { onIntent(JourneysIntent.DeleteJourney(journey.id)) }
                )
            }
        }

        // Quick Actions / 快速入口
        item {
            QuickActionsSection(
                onNavigateToComparison = onNavigateToComparison
            )
        }
    }
}

// ================================================================
// StatisticsSection — 统计概览区
// ================================================================
/**
 * Statistics overview section / 统计概览区
 *
 * Displays: Total / Passed / Failed / Running counts
 */
@Composable
private fun StatisticsSection(state: JourneysState) {
    val totalCount = state.journeys.size
    val passedCount = state.journeys.count { it.status == StepStatus.PASSED }
    val failedCount = state.journeys.count { it.status == StepStatus.FAILED }
    val runningCount = state.journeys.count { it.status == StepStatus.RUNNING }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "📋",
            label = "Total / 总数",
            value = totalCount.toString(),
            backgroundColor = Color(0xFF6750A4).copy(alpha = 0.1f)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "✅",
            label = "Passed / 通过",
            value = passedCount.toString(),
            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "❌",
            label = "Failed / 失败",
            value = failedCount.toString(),
            backgroundColor = Color(0xFFF44336).copy(alpha = 0.1f)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            emoji = "⏳",
            label = "Running / 运行中",
            value = runningCount.toString(),
            backgroundColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
    }
}

/**
 * Single statistics card / 单个统计卡片
 */
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF5F5F5F)
            )
        }
    }
}

// ================================================================
// FilterChipsRow — 筛选 Chips 行
// ================================================================
/**
 * Filter chips for journey list / Journey 列表筛选 Chips
 */
@Composable
private fun FilterChipsRow(
    currentFilter: FilterStatus,
    onFilterChange: (FilterStatus) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(FilterStatus.entries) { status ->
            FilterChip(
                selected = currentFilter == status,
                onClick = { onFilterChange(status) },
                label = {
                    Text("${status.emoji} ${status.label}")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6750A4),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ================================================================
// JourneyCard — Journey 列表卡片
// ================================================================
/**
 * Journey list card item / Journey 列表卡片项
 */
@Composable
private fun JourneyCard(
    journey: JourneyItem,
    onOpen: () -> Unit,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (journey.status) {
        StepStatus.PASSED -> Color(0xFF4CAF50)
        StepStatus.FAILED -> Color(0xFFF44336)
        StepStatus.RUNNING -> Color(0xFF2196F3)
        StepStatus.PENDING -> Color(0xFF9E9E9E)
        StepStatus.SKIPPED -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row / 头部行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📄",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = journey.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0xFF1F1F1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Status Badge / 状态徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${journey.status.emoji} ${journey.status.label}",
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meta info / 元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${journey.stepCount} steps / 步骤",
                    fontSize = 12.sp,
                    color = Color(0xFF5F5F5F)
                )
                Text(
                    text = formatDuration(journey.lastRunDurationMs),
                    fontSize = 12.sp,
                    color = Color(0xFF5F5F5F)
                )
            }

            // Pass rate bar / 通过率进度条
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pass Rate / 通过率: ",
                    fontSize = 12.sp,
                    color = Color(0xFF5F5F5F)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(journey.passRate)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF4CAF50))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(journey.passRate * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f),
                        contentColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete / 删除", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run / 运行", fontSize = 12.sp)
                }
            }
        }
    }
}

// ================================================================
// QuickActionsSection — 快速入口区
// ================================================================
/**
 * Quick action buttons / 快速操作按钮
 */
@Composable
private fun QuickActionsSection(
    onNavigateToComparison: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions / 快速入口",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "📝",
                    label = "Journey Editor\n编辑器",
                    onClick = { }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "📚",
                    label = "Templates\n模板库",
                    onClick = { }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "🔧",
                    label = "CI/CD\n配置",
                    onClick = { }
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    emoji = "⚖️",
                    label = "vs Espresso\n框架对比",
                    onClick = onNavigateToComparison
                )
            }
        }
    }
}

/**
 * Quick action button / 快速操作按钮
 */
@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6750A4).copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF6750A4),
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )
        }
    }
}

// ================================================================
// TemplatesTab — 模板库 Tab
// ================================================================
/**
 * Templates tab content / 模板库 Tab 内容
 */
@Composable
private fun TemplatesTab(
    state: JourneysState,
    onIntent: (JourneysIntent) -> Unit,
    onNavigateToTemplates: () -> Unit
) {
    val templatesByCategory = state.templates.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        templatesByCategory.forEach { (category, templates) ->
            item {
                Text(
                    text = "${category.emoji} ${category.label}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = Color(0xFF1F1F1F)
                )
            }
            items(templates) { template ->
                TemplateCard(
                    template = template,
                    onUse = { onIntent(JourneysIntent.SelectTemplate(template.id)) }
                )
            }
        }
    }
}

/**
 * Template card / 模板卡片
 */
@Composable
private fun TemplateCard(
    template: JourneyTemplate,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = template.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                fontSize = 14.sp,
                color = Color(0xFF5F5F5F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                template.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF6750A4).copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = Color(0xFF6750A4)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onUse,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Use Template / 使用模板", fontSize = 12.sp)
            }
        }
    }
}

// ================================================================
// CIConfigTab — CI/CD 配置 Tab
// ================================================================
/**
 * CI/CD Configuration tab / CI/CD 配置 Tab
 */
@Composable
private fun CIConfigTab(
    state: JourneysState,
    onIntent: (JourneysIntent) -> Unit,
    onNavigateToCIConfig: () -> Unit
) {
    val yamlContent = generateCIYaml(state.selectedCIPlatform)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "CI/CD 平台选择 / CI/CD Platform",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF1F1F1F)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CIPlatform.entries.forEach { platform ->
                    FilterChip(
                        selected = state.selectedCIPlatform == platform,
                        onClick = { onIntent(JourneysIntent.SelectCIPlatform(platform)) },
                        label = { Text(platform.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6750A4),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "${state.selectedCIPlatform.yamlFileName}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = yamlContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF4EC9B0)
                    )
                }
            }
        }

        item {
            Button(
                onClick = { /* Copy to clipboard */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy to Clipboard / 复制到剪贴板")
            }
        }
    }
}

/**
 * Generate CI YAML content / 生成 CI YAML 内容
 */
private fun generateCIYaml(platform: CIPlatform): String {
    return when (platform) {
        CIPlatform.GITHUB_ACTIONS -> """name: Journeys E2E Tests
on: [push, pull_request]
jobs:
  journey-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run Journeys
        run: |
          ./gradlew runJourneys
      - name: Upload results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: journey-results
          path: app/build/journeys/results/"""

        CIPlatform.GITLAB_CI -> """stages:
  - test
journeys-e2e:
  stage: test
  image: ubuntu:latest
  before_script:
    - apt-get update && apt-get install -y openjdk-17-jdk
  script:
    - ./gradlew runJourneys
  artifacts:
    when: always()
    paths:
      - app/build/journeys/results/"""

        CIPlatform.JENKINS -> """pipeline {
    agent any
    stages {
        stage('Journeys E2E') {
            steps {
                echo 'Running Journeys E2E Tests...'
                sh './gradlew runJourneys'
            }
            post {
                always {
                    archive 'app/build/journeys/results/**'
                }
            }
        }
    }
}"""
    }
}

// ================================================================
// EmptyStateCard — 空状态卡片
// ================================================================
/**
 * Empty state card / 空状态卡片
 */
@Composable
private fun EmptyStateCard(
    message: String,
    subMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📭", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subMessage,
                fontSize = 14.sp,
                color = Color(0xFF5F5F5F)
            )
        }
    }
}

// ================================================================
// Utility Functions — 工具函数
// ================================================================
/**
 * Format duration in milliseconds to human-readable string
 * / 将毫秒时长格式化为人类可读字符串
 */
private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "—"
    val seconds = durationMs / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

/**
 * Format timestamp to relative time string
 * / 将时间戳格式化为相对时间字符串
 */
private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    return when {
        minutes < 1 -> "just now / 刚刚"
        minutes < 60 -> "${minutes}m ago / ${minutes}分钟前"
        minutes < 1440 -> "${minutes / 60}h ago / ${minutes / 60}小时前"
        else -> "${minutes / 1440}d ago / ${minutes / 1440}天前"
    }
}
