package com.mvi.kenny.feature.ranging

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
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Battery3Bar
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// PowerAnalyzerScreen — 功耗分析页面
// UWB Ranging API 开发工具包 - 功耗分析 Tab
// =============================================================
/**
 * Power Analyzer Screen / 功耗分析页面
 *
 * Analyzes and compares power consumption of different
 * ranging technologies.
 *
 * Features:
 * - UWB ranging power consumption model / UWB 测距功耗模型
 * - BLE scanning power comparison / BLE 扫描功耗对比
 * - Device battery life impact estimation / 设备续航影响估算
 * - Visual power comparison charts / 可视化功耗对比图表
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 *
 * @see RangingDashboardState Full UI state
 * @see RangingIntent User intents
 */
@Composable
fun PowerAnalyzerScreen(
    state: RangingDashboardState,
    onIntent: (RangingIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ============================================================
        // Header / 头部
        // ============================================================
        item {
            PowerAnalyzerHeader()
        }

        // ============================================================
        // Power Model Explanation / 功耗模型说明
        // ============================================================
        item {
            PowerModelCard()
        }

        // ============================================================
        // Power Comparison Chart / 功耗对比柱状图
        // ============================================================
        item {
            PowerComparisonChartCard(estimates = state.powerEstimates)
        }

        // ============================================================
        // Power Estimates List / 功耗估算列表
        // ============================================================
        items(
            items = state.powerEstimates,
            key = { it.technology }
        ) { estimate ->
            PowerEstimateCard(estimate = estimate)
        }

        // ============================================================
        // Battery Impact Summary / 电池影响摘要
        // ============================================================
        item {
            BatteryImpactSummaryCard(estimates = state.powerEstimates)
        }

        // ============================================================
        // Power Saving Tips / 节能建议
        // ============================================================
        item {
            PowerSavingTipsCard()
        }
    }
}

// =============================================================
// PowerAnalyzerHeader — 功耗分析头部
// =============================================================
/**
 * Power analyzer header / 功耗分析头部
 */
@Composable
private fun PowerAnalyzerHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD54F).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ElectricBolt,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "功耗分析 / Power Analyzer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UWB 测距功耗模型与续航影响估算",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "UWB Ranging Power Model & Battery Impact Estimation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// PowerModelCard — 功耗模型说明卡片
// =============================================================
/**
 * Power model explanation card / 功耗模型说明卡片
 */
@Composable
private fun PowerModelCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "UWB 测距功耗模型 / UWB Ranging Power Model",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Power consumption breakdown / 功耗分解
            val components = listOf(
                Triple("RF 发射 / RF TX", "60%", Color(0xFFFF6B6B)),
                Triple("RF 接收 / RF RX", "25%", Color(0xFFFFD54F)),
                Triple("基带处理 / Baseband", "10%", Color(0xFF4FC3F7)),
                Triple("传感器融合 / Sensor Fusion", "5%", Color(0xFF69F0AE))
            )

            components.forEach { (name, percent, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = percent,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "关键结论 / Key Insight",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B6B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "UWB 测距的功耗主要来自 RF 发射，占总功耗的 60%。" +
                        "降低更新频率（如从 100ms 调整为 1000ms）可显著降低功耗。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "UWB power consumption is dominated by RF transmission (60%). " +
                        "Reducing update frequency (e.g., 100ms → 1000ms) can significantly reduce power draw.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// PowerComparisonChartCard — 功耗对比柱状图卡片
// =============================================================
/**
 * Power comparison bar chart card / 功耗对比柱状图卡片
 */
@Composable
private fun PowerComparisonChartCard(
    estimates: List<PowerEstimate>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "功耗对比 / Power Comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "每小时消耗 (mAh) / Hourly Consumption",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bar chart / 柱状图
            PowerBarChart(
                estimates = estimates,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legend / 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                estimates.forEach { estimate ->
                    val color = when (estimate.technology) {
                        "UWB 测距" -> Color(0xFFFF6B6B)
                        "BLE 扫描" -> Color(0xFF69F0AE)
                        "WiFi RTT" -> Color(0xFF4FC3F7)
                        else -> Color.Gray
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = estimate.technology,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// PowerBarChart — 功耗柱状图
// =============================================================
/**
 * Power consumption bar chart / 功耗柱状图
 */
@Composable
private fun PowerBarChart(
    estimates: List<PowerEstimate>,
    modifier: Modifier = Modifier
) {
    val maxMah = estimates.maxOfOrNull { it.hourlyMah } ?: 1f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        estimates.forEach { estimate ->
            val barHeight = (estimate.hourlyMah / maxMah)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Value label / 数值标签
                Text(
                    text = "${estimate.hourlyMah.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bar / 柱
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(barHeight.coerceIn(0.05f, 1f))
                        .background(
                            when (estimate.technology) {
                                "UWB 测距" -> Color(0xFFFF6B6B)
                                "BLE 扫描" -> Color(0xFF69F0AE)
                                "WiFi RTT" -> Color(0xFF4FC3F7)
                                else -> Color.Gray
                            },
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Technology name / 技术名称
                Text(
                    text = estimate.technology.replace(" ", "\n"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// =============================================================
// PowerEstimateCard — 功耗估算详情卡片
// =============================================================
/**
 * Power estimate detail card / 功耗估算详情卡片
 */
@Composable
private fun PowerEstimateCard(
    estimate: PowerEstimate
) {
    val color = when (estimate.technology) {
        "UWB 测距" -> Color(0xFFFF6B6B)
        "BLE 扫描" -> Color(0xFF69F0AE)
        "WiFi RTT" -> Color(0xFF4FC3F7)
        else -> Color.Gray
    }

    val batteryIcon = when {
        estimate.batteryImpactDays >= 3 -> Icons.Rounded.Battery1Bar
        estimate.batteryImpactDays >= 2 -> Icons.Rounded.Battery3Bar
        estimate.batteryImpactDays >= 1 -> Icons.Rounded.Battery5Bar
        else -> Icons.Rounded.BatteryFull
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = estimate.technology,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active: 8h/day / 每天活跃 8 小时",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Battery impact / 电池影响
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = batteryIcon,
                        contentDescription = null,
                        tint = if (estimate.batteryImpactDays >= 2) Color(0xFFFF6B6B) else Color(0xFF69F0AE),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "-${String.format("%.1f", estimate.batteryImpactDays)}d",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (estimate.batteryImpactDays >= 2) Color(0xFFFF6B6B) else Color(0xFF69F0AE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics / 指标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PowerMetric(
                    label = "每小时 / Hourly",
                    value = "${estimate.hourlyMah.toInt()} mAh",
                    color = color
                )
                PowerMetric(
                    label = "每天 / Daily",
                    value = "${estimate.dailyMah.toInt()} mAh",
                    color = color
                )
                PowerMetric(
                    label = "续航影响 / Impact",
                    value = "-${String.format("%.1f", estimate.batteryImpactDays)} 天",
                    color = if (estimate.batteryImpactDays >= 2) Color(0xFFFF6B6B) else Color(0xFF69F0AE)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / 描述
            Text(
                text = estimate.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// PowerMetric — 功耗指标
// =============================================================
/**
 * Power metric display / 功耗指标显示
 */
@Composable
private fun PowerMetric(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// =============================================================
// BatteryImpactSummaryCard — 电池影响摘要卡片
// =============================================================
/**
 * Battery impact summary card / 电池影响摘要卡片
 */
@Composable
private fun BatteryImpactSummaryCard(
    estimates: List<PowerEstimate>
) {
    val uwbImpact = estimates.find { it.technology == "UWB 测距" }?.batteryImpactDays ?: 0f
    val bleImpact = estimates.find { it.technology == "BLE 扫描" }?.batteryImpactDays ?: 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Battery1Bar,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "电池续航影响摘要 / Battery Impact Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "假设设备电池容量为 4000mAh，每天活跃使用 8 小时：",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Assuming 4000mAh battery capacity with 8h daily active use:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Impact comparison / 影响对比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "UWB 测距",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-${String.format("%.1f", uwbImpact)} 天续航",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6B6B)
                    )
                    Text(
                        text = "Battery life reduced by ${String.format("%.1f", uwbImpact)} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BLE 扫描",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-${String.format("%.1f", bleImpact)} 天续航",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69F0AE)
                    )
                    Text(
                        text = "Battery life reduced by ${String.format("%.1f", bleImpact)} days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 建议：社交类 App 可考虑使用 Privacy 模式并降低更新频率至 1-2s，以延长设备续航。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFD54F)
            )
        }
    }
}

// =============================================================
// PowerSavingTipsCard — 节能建议卡片
// =============================================================
/**
 * Power saving tips card / 节能建议卡片
 */
@Composable
private fun PowerSavingTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF69F0AE).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 节能建议 / Power Saving Tips",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "降低更新频率：将测距间隔从 100ms 调整为 1000ms，功耗降低约 70%" to "Reduce update frequency: 100ms → 1000ms reduces power by ~70%",
                "使用 Privacy 模式：适用于社交 App，无需持续上报精确位置" to "Use Privacy mode: Suitable for social apps without continuous precise location",
                "启用运动检测：设备静止时不测距，仅在移动时激活" to "Enable motion detection: Range only when moving, not when stationary",
                "批量数据上报：将多次测距结果打包一次上传，减少通信次数" to "Batch data upload: Bundle multiple results into one upload to reduce communication"
            )

            tips.forEachIndexed { index, (chinese, english) ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF69F0AE)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = chinese,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = english,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
