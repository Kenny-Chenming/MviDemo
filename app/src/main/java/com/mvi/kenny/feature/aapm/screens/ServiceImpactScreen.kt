package com.mvi.kenny.feature.aapm.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AccessibilityServiceInfo
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.ResponseStrategy
import com.mvi.kenny.feature.aapm.RiskLevel

// =============================================================
// ServiceImpactScreen — Service 影响分析页面
// =============================================================
/**
 * Service Impact Screen / Service 影响分析页面
 *
 * Displays all AccessibilityService declarations in the app,
 * their AAPM risk ratings, and recommended response strategies.
 *
 * @param state Current dashboard state / 当前仪表板状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun ServiceImpactScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header / 标题栏
        ServiceImpactHeader(
            totalCount = state.services.size,
            highRiskCount = state.highRiskCount,
            mediumRiskCount = state.mediumRiskCount,
            lowRiskCount = state.lowRiskCount,
            isScanning = state.isScanning,
            onScan = { onIntent(AAPMDashboardIntent.ScanAccessibilityServices) }
        )

        // Service List / 服务列表
        if (state.isScanning) {
            // Scanning Progress / 扫描进度
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在扫描 Accessibility Services…",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (state.services.isEmpty()) {
            // Empty State / 空状态
            EmptyServiceList()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(state.services, key = { it.className }) { service ->
                    ServiceImpactCard(
                        service = service,
                        onSelect = { onIntent(AAPMDashboardIntent.SelectService(service)) },
                        onNavigateToMigration = {
                            onIntent(AAPMDashboardIntent.NavigateToMigration(service.name))
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

// =============================================================
// ServiceImpactHeader — 服务影响分析标题栏
// =============================================================
/**
 * Service Impact Header / 服务影响分析标题栏
 */
@Composable
private fun ServiceImpactHeader(
    totalCount: Int,
    highRiskCount: Int,
    mediumRiskCount: Int,
    lowRiskCount: Int,
    isScanning: Boolean,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Service 影响分析",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onScan,
                    enabled = !isScanning
                ) {
                    Text("重新扫描")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip(label = "总计", count = totalCount, color = Color(0xFF757575))
                SummaryChip(label = "高风险", count = highRiskCount, color = Color(0xFFFF5252))
                SummaryChip(label = "中风险", count = mediumRiskCount, color = Color(0xFFFFA726))
                SummaryChip(label = "低风险", count = lowRiskCount, color = Color(0xFF66BB6A))
            }
        }
    }
}

// =============================================================
// SummaryChip — 摘要徽章
// =============================================================
@Composable
private fun SummaryChip(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// =============================================================
// ServiceImpactCard — 服务影响卡片
// =============================================================
/**
 * Service Impact Card / 服务影响卡片
 *
 * Card displaying a single Accessibility Service with risk level
 * and recommended response strategy. High risk items have a pulsing border.
 */
@Composable
private fun ServiceImpactCard(
    service: AccessibilityServiceInfo,
    onSelect: () -> Unit,
    onNavigateToMigration: () -> Unit
) {
    val isHighRisk = service.riskLevel == RiskLevel.HIGH && service.isEnabled

    // Pulsing animation for high risk / 高风险脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isHighRisk) Color(0xFFFF5252).copy(alpha = pulseAlpha) else Color.Transparent,
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isHighRisk) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk Badge / 风险徽章
                    RiskBadge(riskLevel = service.riskLevel)

                    Spacer(modifier = Modifier.size(8.dp))

                    Column {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = service.className,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Enabled Status / 启用状态
                EnabledBadge(isEnabled = service.isEnabled)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description / 描述
            if (service.description.isNotEmpty()) {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Recommended Response / 推荐响应
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ResponseStrategyChip(strategy = service.recommendedResponse)

                // Navigate to Migration Guide / 导航到迁移指南
                if (service.riskLevel != RiskLevel.LOW) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF66BB6A).copy(alpha = 0.1f))
                            .clickable { onNavigateToMigration() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "迁移指南",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF66BB6A)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF66BB6A)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// RiskBadge — 风险等级徽章
// =============================================================
/**
 * Risk Badge / 风险等级徽章
 *
 * Displays the risk level with appropriate color coding.
 */
@Composable
fun RiskBadge(riskLevel: RiskLevel) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(riskLevel.color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (riskLevel == RiskLevel.HIGH) Icons.Rounded.Warning else Icons.Rounded.Shield,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = riskLevel.color
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = when (riskLevel) {
                    RiskLevel.HIGH -> "高"
                    RiskLevel.MEDIUM -> "中"
                    RiskLevel.LOW -> "低"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = riskLevel.color
            )
        }
    }
}

// =============================================================
// EnabledBadge — 启用状态徽章
// =============================================================
/**
 * Enabled Badge / 启用状态徽章
 */
@Composable
private fun EnabledBadge(isEnabled: Boolean) {
    val color = if (isEnabled) Color(0xFF66BB6A) else Color(0xFF757575)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isEnabled) "启用" else "禁用",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// =============================================================
// ResponseStrategyChip — 响应策略徽章
// =============================================================
/**
 * Response Strategy Chip / 响应策略徽章
 */
@Composable
private fun ResponseStrategyChip(strategy: ResponseStrategy) {
    val color = when (strategy) {
        ResponseStrategy.DISABLE -> Color(0xFFEF5350)
        ResponseStrategy.GRACEFUL_DEGRADE -> Color(0xFFFFA726)
        ResponseStrategy.USER_GUIDANCE -> Color(0xFF42A5F5)
        ResponseStrategy.NONE -> Color(0xFF66BB6A)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = when (strategy) {
                ResponseStrategy.DISABLE -> "禁用"
                ResponseStrategy.GRACEFUL_DEGRADE -> "优雅降级"
                ResponseStrategy.USER_GUIDANCE -> "用户引导"
                ResponseStrategy.NONE -> "无需操作"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// =============================================================
// EmptyServiceList — 空服务列表
// =============================================================
/**
 * Empty Service List State / 空服务列表状态
 */
@Composable
private fun EmptyServiceList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无 Accessibility Services",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "当前 App 未使用 Accessibility API",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
