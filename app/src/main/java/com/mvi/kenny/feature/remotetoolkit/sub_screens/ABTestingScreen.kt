package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.ABTest
import com.mvi.kenny.feature.remotetoolkit.ABTestStatus
import com.mvi.kenny.feature.remotetoolkit.ABVariant

// =============================================================
// ABTestingScreen — A/B 测试框架页面
// A/B Testing / A/B 测试
// =============================================================

private val sampleActiveTests = listOf(
    ABTest(
        id = "t1",
        name = "CTA Button Color",
        status = ABTestStatus.RUNNING,
        currentDay = 3,
        totalDays = 7,
        traffic = 10000,
        variants = listOf(
            ABVariant("a", "Blue", 0.52f, RemoteToolkitColors.Info),
            ABVariant("b", "Red", 0.48f, RemoteToolkitColors.Coral)
        )
    ),
    ABTest(
        id = "t2",
        name = "Product Card Layout",
        status = ABTestStatus.COLLECTING,
        currentDay = 1,
        totalDays = 7,
        traffic = 2340,
        variants = listOf(
            ABVariant("a", "Vertical", 0.45f, RemoteToolkitColors.Purple),
            ABVariant("b", "Horizontal", 0.42f, RemoteToolkitColors.Teal),
            ABVariant("c", "Grid", 0.13f, RemoteToolkitColors.Warning)
        )
    )
)

// =============================================================
// ABTestingScreen — 主内容
// =============================================================
@Composable
fun ABTestingScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    val tabs = listOf("Active / 活跃", "Completed / 完成")
    val selectedTab = state.abSelectedTab.coerceIn(0, tabs.size - 1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "A/B Testing Framework",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
        }

        // ============================================================
        // Tab Row / 标签栏
        // ============================================================
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RemoteToolkitColors.Surface,
                contentColor = RemoteToolkitColors.Purple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RemoteToolkitColors.Purple
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { },
                        text = {
                            Text(
                                title,
                                color = if (selectedTab == index) RemoteToolkitColors.Purple
                                        else RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // ============================================================
        // Active Tests / 活跃测试
        // ============================================================
        if (selectedTab == 0) {
            // Create new test button / 新建测试按钮
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RemoteToolkitColors.Purple)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Test",
                            tint = RemoteToolkitColors.OnPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (sampleActiveTests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active A/B tests",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RemoteToolkitColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(sampleActiveTests, key = { it.id }) { test ->
                    ABTestCard(
                        test = test,
                        onStop = { onIntent(RemoteToolkitIntent.StopABTest(test.id)) },
                        onDeclareWinner = { variantId ->
                            onIntent(RemoteToolkitIntent.DeclareWinner(test.id, variantId))
                        }
                    )
                }
            }
        }

        // ============================================================
        // Completed Tests / 已完成测试
        // ============================================================
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed tests yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// ABTestCard — A/B 测试卡片
// =============================================================
@Composable
private fun ABTestCard(
    test: ABTest,
    onStop: () -> Unit,
    onDeclareWinner: (String) -> Unit
) {
    val statusColor = when (test.status) {
        ABTestStatus.RUNNING -> RemoteToolkitColors.PassColor
        ABTestStatus.COLLECTING -> RemoteToolkitColors.WarningColor
        ABTestStatus.STOPPED -> RemoteToolkitColors.OnSurfaceVariant
    }

    val statusLabel = when (test.status) {
        ABTestStatus.RUNNING -> "Running / 运行中"
        ABTestStatus.COLLECTING -> "Collecting / 收集中"
        ABTestStatus.STOPPED -> "Stopped / 已停止"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = test.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RemoteToolkitColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Day ${test.currentDay}/${test.totalDays}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${test.traffic} users",
                    style = MaterialTheme.typography.labelMedium,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results bar chart / 结果柱状图
            Text(
                text = "Results / 结果",
                style = MaterialTheme.typography.labelMedium,
                color = RemoteToolkitColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            test.variants.forEach { variant ->
                VariantResultRow(variant = variant)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Statistical significance / 统计显著性
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Statistical Significance",
                        style = MaterialTheme.typography.labelSmall,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Text(
                        text = "94%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (94 >= 95) RemoteToolkitColors.PassColor else RemoteToolkitColors.WarningColor
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onStop,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.Coral)
                    ) {
                        Text("Stop Test", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { onDeclareWinner(test.variants.first().id) },
                        colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Purple)
                    ) {
                        Text("Declare Winner", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// =============================================================
// VariantResultRow — 变体结果行
// =============================================================
@Composable
private fun VariantResultRow(variant: ABVariant) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(variant.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = variant.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = RemoteToolkitColors.OnSurface
                )
            }
            Text(
                text = "${(variant.ctr * 100).toInt()}% CTR",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = variant.color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            drawRoundRect(
                color = RemoteToolkitColors.SurfaceVariant,
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = variant.color,
                size = Size(size.width * variant.ctr, size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
