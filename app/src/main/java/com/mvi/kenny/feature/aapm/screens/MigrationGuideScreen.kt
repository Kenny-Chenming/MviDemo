package com.mvi.kenny.feature.aapm.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AccessibilityServiceInfo
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.RiskLevel
import com.mvi.kenny.feature.aapm.ResponseStrategy

// =============================================================
// MigrationGuideScreen — 迁移指南屏幕
// =============================================================
/**
 * Migration Guide Screen / 迁移指南屏幕
 *
 * Provides step-by-step migration guidance for each
 * Accessibility Service based on its risk level and
 * recommended response strategy.
 *
 * @param state Current dashboard state / 当前仪表盘状态
 * @param onIntent Intent callback to ViewModel / Intent 回调到 ViewModel
 */
@Composable
fun MigrationGuideScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // =============================================================
        // Header / 标题区
        // =============================================================
        Text(
            text = "AAPM 迁移指南",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Android 17 Advanced Protection Mode 合规迁移",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // General Guide / 通用指南
        // =============================================================
        GeneralGuideCard()

        Spacer(modifier = Modifier.height(16.dp))

        // =============================================================
        // Service-specific Guides / 服务特定指南
        // =============================================================
        if (state.services.isNotEmpty()) {
            Text(
                text = "服务迁移方案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.services.filter { it.riskLevel != RiskLevel.LOW }.forEach { service ->
                ServiceMigrationCard(service = service)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// =============================================================
// GeneralGuideCard — 通用指南卡片
// =============================================================
/**
 * General Guide Card / 通用指南卡片
 *
 * Provides general migration guidance for all services.
 */
@Composable
private fun GeneralGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E88E5).copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AAPM 合规检查清单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E88E5)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GuideChecklistItem(
                text = "审查所有 Accessibility Service 声明",
                subtext = "Review all AccessibilityService declarations in AndroidManifest.xml"
            )
            GuideChecklistItem(
                text = "评估每个服务的风险等级",
                subtext = "Assess the risk level of each service"
            )
            GuideChecklistItem(
                text = "实施优雅降级策略",
                subtext = "Implement graceful degradation strategy"
            )
            GuideChecklistItem(
                text = "添加用户引导和通知",
                subtext = "Add user guidance and notifications"
            )
            GuideChecklistItem(
                text = "测试 AAPM 影响场景",
                subtext = "Test AAPM impact scenarios"
            )
        }
    }
}

// =============================================================
// GuideChecklistItem — 指南检查项
// =============================================================
/**
 * Guide Checklist Item / 指南检查项
 */
@Composable
private fun GuideChecklistItem(
    text: String,
    subtext: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF66BB6A),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================
// ServiceMigrationCard — 服务迁移卡片
// =============================================================
/**
 * Service Migration Card / 服务迁移卡片
 *
 * @param service Service to migrate / 要迁移的服务
 */
@Composable
private fun ServiceMigrationCard(service: AccessibilityServiceInfo) {
    val riskColor = when (service.riskLevel) {
        RiskLevel.HIGH -> Color(0xFFFF5252)
        RiskLevel.MEDIUM -> Color(0xFFFFA726)
        RiskLevel.LOW -> Color(0xFF66BB6A)
    }

    val (strategyIcon, strategyColor) = when (service.recommendedResponse) {
        ResponseStrategy.DISABLE -> Icons.Default.Block to Color(0xFFFF5252)
        ResponseStrategy.GRACEFUL_DEGRADE -> Icons.Default.Build to Color(0xFFFFA726)
        ResponseStrategy.USER_GUIDANCE -> Icons.Default.PhoneAndroid to Color(0xFF1E88E5)
        ResponseStrategy.NONE -> Icons.Default.CheckCircle to Color(0xFF66BB6A)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header / 头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Strategy / 策略
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(strategyColor.copy(alpha = 0.1f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = strategyIcon,
                    contentDescription = null,
                    tint = strategyColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = service.recommendedResponse.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = strategyColor
                    )
                    Text(
                        text = service.recommendedResponse.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Migration steps / 迁移步骤
            Text(
                text = "迁移步骤 / Migration Steps",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val steps = getMigrationSteps(service)
            steps.forEachIndexed { index, step ->
                MigrationStepItem(
                    stepNumber = index + 1,
                    text = step,
                    icon = getStepIcon(index)
                )
                if (index < steps.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// =============================================================
// MigrationStepItem — 迁移步骤项
// =============================================================
/**
 * Migration Step Item / 迁移步骤项
 */
@Composable
private fun MigrationStepItem(
    stepNumber: Int,
    text: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================
// getMigrationSteps — 获取迁移步骤
// =============================================================
/**
 * Get migration steps for a service / 获取服务的迁移步骤
 */
private fun getMigrationSteps(service: AccessibilityServiceInfo): List<String> {
    return when (service.recommendedResponse) {
        ResponseStrategy.DISABLE -> listOf(
            "在 AndroidManifest.xml 中移除或注释 AccessibilityService 配置",
            "Remove or comment out AccessibilityService configuration in AndroidManifest.xml",
            "更新代码，移除所有 AccessibilityService 引用",
            "Update code to remove all AccessibilityService references",
            "使用 App Ops 或 Settings.canDrawOverlays() 检查替代方案",
            "Check alternatives using App Ops or Settings.canDrawOverlays()"
        )
        ResponseStrategy.GRACEFUL_DEGRADE -> listOf(
            "检测 AccessibilityService 是否可用 (android.accessibilityservice.AccessibilityService)",
            "Detect if AccessibilityService is available (android.accessibilityservice.AccessibilityService)",
            "实现功能降级，在无障碍服务不可用时提供基础功能",
            "Implement graceful degradation to provide basic functionality when accessibility is unavailable",
            "添加 onServiceConnected() 和 onServiceDisconnected() 回调处理",
            "Add onServiceConnected() and onServiceDisconnected() callback handlers",
            "使用其他 API 替代：App Ops、Statistics API、UsageStatsManager",
            "Use alternative APIs: App Ops, Statistics API, UsageStatsManager"
        )
        ResponseStrategy.USER_GUIDANCE -> listOf(
            "实现 AccessibilityService 的 onServiceDisconnected() 回调",
            "Implement AccessibilityService's onServiceDisconnected() callback",
            "检测 AAPM 状态，使用 DetectionService API (Android 17.2+)",
            "Detect AAPM state using DetectionService API (Android 17.2+)",
            "显示用户友好的通知，解释功能限制",
            "Show user-friendly notifications explaining functionality limitations",
            "提供清晰的迁移指南和操作指引",
            "Provide clear migration guide and operation instructions"
        )
        ResponseStrategy.NONE -> listOf(
            "无需迁移，服务已符合 AAPM 要求",
            "No migration needed, service complies with AAPM requirements",
            "定期检查 AAPM 政策更新",
            "Regularly check for AAPM policy updates"
        )
    }
}

// =============================================================
// getStepIcon — 获取步骤图标
// =============================================================
/**
 * Get icon for a migration step / 获取迁移步骤图标
 */
private fun getStepIcon(index: Int): ImageVector {
    return when (index % 4) {
        0 -> Icons.Default.Code
        1 -> Icons.Default.Description
        2 -> Icons.Default.Build
        else -> Icons.Default.CheckCircle
    }
}
