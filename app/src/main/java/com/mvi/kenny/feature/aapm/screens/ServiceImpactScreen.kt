package com.mvi.kenny.feature.aapm.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AccessibilityServiceInfo
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.RiskLevel
import com.mvi.kenny.feature.aapm.ResponseStrategy

// =============================================================
// ServiceImpactScreen — 服务影响分析屏幕
// =============================================================
/**
 * Service Impact Screen / 服务影响分析屏幕
 *
 * Displays impact analysis of each Accessibility Service:
 * - Service details (name, class, risk level)
 * - Recommended response strategy
 * - AAPM impact assessment
 *
 * @param state Current dashboard state / 当前仪表盘状态
 * @param onIntent Intent callback to ViewModel / Intent 回调到 ViewModel
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
            .padding(16.dp)
    ) {
        // =============================================================
        // Header / 标题区
        // =============================================================
        Text(
            text = "服务影响分析",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Accessibility Services 对 AAPM 的影响评估",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Services List / 服务列表
        // =============================================================
        if (state.services.isEmpty()) {
            EmptyServicesView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.services) { service ->
                    ServiceImpactCard(
                        service = service,
                        onClick = { onIntent(AAPMDashboardIntent.SelectService(service)) },
                        onNavigateToMigration = {
                            onIntent(AAPMDashboardIntent.NavigateToMigration(service.name))
                        }
                    )
                }
            }
        }
    }
}

// =============================================================
// ServiceImpactCard — 服务影响卡片
// =============================================================
/**
 * Service Impact Card / 服务影响卡片
 *
 * @param service Service information / 服务信息
 * @param onClick Click callback / 点击回调
 * @param onNavigateToMigration Migration navigation callback / 迁移导航回调
 */
@Composable
private fun ServiceImpactCard(
    service: AccessibilityServiceInfo,
    onClick: () -> Unit,
    onNavigateToMigration: () -> Unit
) {
    val riskColor = when (service.riskLevel) {
        RiskLevel.HIGH -> Color(0xFFFF5252)
        RiskLevel.MEDIUM -> Color(0xFFFFA726)
        RiskLevel.LOW -> Color(0xFF66BB6A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row / 头部行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Risk badge / 风险徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(riskColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = service.riskLevel.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Enabled badge / 启用状态徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (service.isEnabled) Color(0xFF66BB6A).copy(alpha = 0.15f)
                            else Color(0xFF9E9E9E).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (service.isEnabled) "已启用" else "已停用",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (service.isEnabled) Color(0xFF66BB6A) else Color(0xFF9E9E9E)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Service name / 服务名称
            Text(
                text = service.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Class name / 类名
            Text(
                text = service.className,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (service.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Response strategy / 响应策略
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "建议策略",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = service.recommendedResponse.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (service.riskLevel != RiskLevel.LOW) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E88E5).copy(alpha = 0.1f))
                            .clickable(onClick = onNavigateToMigration)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "迁移指南",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1E88E5)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// EmptyServicesView — 空服务视图
// =============================================================
/**
 * Empty Services View / 空服务视图
 *
 * Shown when no Accessibility Services are found.
 */
@Composable
private fun EmptyServicesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF66BB6A).copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未检测到 Accessibility Services",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No Accessibility Services detected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
