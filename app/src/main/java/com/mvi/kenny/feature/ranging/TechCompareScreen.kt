package com.mvi.kenny.feature.ranging

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.SignalCellular4Bar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =============================================================
// TechCompareScreen — Ranging vs BLE/WiFi RTT 对比工具页面
// UWB Ranging API 开发工具包 - 技术对比 Tab
// =============================================================
/**
 * Tech Compare Screen / 技术对比页面
 *
 * Provides technology comparison and decision-making tools for
 * choosing the right ranging/positioning technology.
 *
 * Features:
 * - Decision tree for technology selection / 技术选择决策树
 * - Accuracy/power/range comparison table / 精度/功耗/距离对比表格
 * - Use case recommendations / 使用场景推荐
 * - Pros and cons for each technology / 各技术优缺点
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Send intent to ViewModel / 发送意图到 ViewModel
 *
 * @see RangingDashboardState Full UI state
 * @see RangingIntent User intents
 */
@Composable
fun TechCompareScreen(
    state: RangingDashboardState,
    onIntent: (RangingIntent) -> Unit
) {
    var expandedTech by remember { mutableStateOf<String?>(null) }

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
            TechCompareHeader()
        }

        // ============================================================
        // Decision Tree / 决策树
        // ============================================================
        item {
            DecisionTreeCard()
        }

        // ============================================================
        // Comparison Table / 对比表格
        // ============================================================
        item {
            ComparisonTableCard(comparisons = state.techComparisons)
        }

        // ============================================================
        // Technology Details / 技术详情
        // ============================================================
        items(
            items = state.techComparisons,
            key = { it.name }
        ) { tech ->
            TechDetailCard(
                tech = tech,
                isExpanded = expandedTech == tech.name,
                onToggle = {
                    expandedTech = if (expandedTech == tech.name) null else tech.name
                }
            )
        }
    }
}

// =============================================================
// TechCompareHeader — 技术对比头部
// =============================================================
/**
 * Tech comparison header / 技术对比头部
 */
@Composable
private fun TechCompareHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4FC3F7).copy(alpha = 0.1f)
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
                imageVector = Icons.Rounded.CompareArrows,
                contentDescription = null,
                tint = Color(0xFF4FC3F7),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "技术选型对比 / Technology Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UWB vs BLE vs WiFi RTT",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================
// DecisionTreeCard — 决策树卡片
// =============================================================
/**
 * Decision tree for technology selection / 技术选择决策树
 */
@Composable
private fun DecisionTreeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "决策树 / Decision Tree",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "根据场景选择最适合的技术 / Choose the best technology for your use case",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Decision nodes / 决策节点
            DecisionNode(
                question = "需要厘米级精度？ / Need centimeter-level accuracy?",
                options = listOf(
                    DecisionOption("是 / Yes", Color(0xFF69F0AE)) {
                        DecisionNode(
                            question = "需要室内定位？ / Indoor positioning?",
                            options = listOf(
                                DecisionOption("UWB ✓", Color(0xFF4FC3F7)) {},
                                DecisionOption("返回", Color.Gray) {}
                            )
                        )
                    },
                    DecisionOption("否 / No", Color(0xFFFFD54F)) {
                        DecisionNode(
                            question = "设备功耗敏感？ / Power-sensitive?",
                            options = listOf(
                                DecisionOption("是 / Yes → BLE ✓", Color(0xFF69F0AE)) {},
                                DecisionOption("否 / No → WiFi RTT ✓", Color(0xFF4FC3F7)) {}
                            )
                        )
                    }
                )
            )
        }
    }
}

// =============================================================
// DecisionNode — 决策节点
// =============================================================
/**
 * Decision tree node / 决策树节点
 */
@Composable
private fun DecisionNode(
    question: String,
    options: List<DecisionOption>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                DecisionOptionChip(option = option)
            }
        }
    }
}

/**
 * Decision option / 决策选项
 */
private data class DecisionOption(
    val label: String,
    val color: Color,
    val onClick: @Composable () -> Unit
)

/**
 * Decision option chip / 决策选项标签
 */
@Composable
private fun DecisionOptionChip(option: DecisionOption) {
    Box(
        modifier = Modifier
            .background(
                color = option.color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = option.label,
            style = MaterialTheme.typography.labelMedium,
            color = option.color
        )
    }
}

// =============================================================
// ComparisonTableCard — 对比表格卡片
// =============================================================
/**
 * Technology comparison table / 技术对比表格
 */
@Composable
private fun ComparisonTableCard(
    comparisons: List<TechComparison>
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
                text = "对比表格 / Comparison Table",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "精度 / 功耗 / 距离 / Cost",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Table header / 表格头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TableHeaderCell("技术 / Tech", Modifier.width(120.dp))
                TableHeaderCell("精度 / Accuracy", Modifier.width(80.dp))
                TableHeaderCell("典型距离 / Range", Modifier.width(80.dp))
                TableHeaderCell("功耗 / Power", Modifier.width(80.dp))
                TableHeaderCell("成本 / Cost", Modifier.width(80.dp))
            }

            // Table rows / 表格行
            comparisons.forEach { tech ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(tech.name, Modifier.width(120.dp), fontWeight = FontWeight.Bold)
                    TableCell(tech.accuracyRange, Modifier.width(80.dp), color = Color(0xFF69F0AE))
                    TableCell(tech.typicalRange, Modifier.width(80.dp))
                    PowerLevelCell(tech.powerConsumption, Modifier.width(80.dp))
                    TableCell(tech.cost.label, Modifier.width(80.dp))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }
    }
}

/**
 * Table header cell / 表格头部单元格
 */
@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Table cell / 表格单元格
 */
@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

/**
 * Power level cell with indicator / 带指示器的功耗等级单元格
 */
@Composable
private fun PowerLevelCell(
    powerLevel: PowerLevel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            Icon(
                imageVector = if (index < powerLevel.bars) {
                    if (powerLevel == PowerLevel.HIGH || powerLevel == PowerLevel.VERY_HIGH) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    }
                } else {
                    Icons.Rounded.SignalCellular4Bar
                },
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (index < powerLevel.bars) {
                    when (powerLevel) {
                        PowerLevel.LOW -> Color(0xFF69F0AE)
                        PowerLevel.MEDIUM -> Color(0xFF4FC3F7)
                        PowerLevel.HIGH -> Color(0xFFFFD54F)
                        PowerLevel.VERY_HIGH -> Color(0xFFFF6B6B)
                    }
                } else {
                    Color.Gray.copy(alpha = 0.3f)
                }
            )
        }
    }
}

// =============================================================
// TechDetailCard — 技术详情卡片
// =============================================================
/**
 * Technology detail card with expandable pros and cons / 带可展开优缺点详情的技术卡片
 */
@Composable
private fun TechDetailCard(
    tech: TechComparison,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = when (tech.name) {
        "UWB (Ranging API)" -> Color(0xFF4FC3F7)
        "BLE 蓝牙" -> Color(0xFF69F0AE)
        "WiFi RTT (IEEE 802.11mc)" -> Color(0xFF80DEEA)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle,
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
                            .background(borderColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tech.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tech.compatibility,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Best use cases / 最佳场景
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "最佳场景 / Best Use Cases",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tech.bestUseCases.take(3).forEach { useCase ->
                    Box(
                        modifier = Modifier
                            .background(
                                borderColor.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = useCase,
                            style = MaterialTheme.typography.labelSmall,
                            color = borderColor
                        )
                    }
                }
            }

            // Expanded content / 展开内容
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Pros / 优势
                Text(
                    text = "优势 / Pros",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF69F0AE)
                )
                Spacer(modifier = Modifier.height(4.dp))
                tech.pros.forEach { pro ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF69F0AE),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pro,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cons / 劣势
                Text(
                    text = "劣势 / Cons",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                tech.cons.forEach { con ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = con,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
