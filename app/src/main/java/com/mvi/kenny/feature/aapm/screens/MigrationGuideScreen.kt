package com.mvi.kenny.feature.aapm.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.aapm.AAPMDashboardIntent
import com.mvi.kenny.feature.aapm.AAPMDashboardState
import com.mvi.kenny.feature.aapm.ResponseStrategy

// =============================================================
// MigrationGuideScreen — 迁移指南页面
// =============================================================
/**
 * Migration Guide Screen / 迁移指南页面
 *
 * Provides guidance for AAPM compliance including:
 * - isAccessibilityTool marker application process
 * - Degradation strategy selector under AAPM
 * - Code example display area
 *
 * @param state Current dashboard state / 当前仪表板状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun MigrationGuideScreen(
    state: AAPMDashboardState,
    onIntent: (AAPMDashboardIntent) -> Unit
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Google Play Store Certification Notice / Google Play 商店认证注意事项
        item {
            CertificationNoticeCard()
        }

        // isAccessibilityTool Marker Guide / isAccessibilityTool 标记申请指南
        item {
            AccessibilityToolMarkerCard()
        }

        // Degradation Strategy Guide / 降级策略指南
        item {
            DegradationStrategyCard(
                services = state.services,
                onUpdateStrategy = { serviceName, strategy ->
                    onIntent(AAPMDashboardIntent.UpdateServiceResponse(serviceName, strategy))
                }
            )
        }

        // Code Examples / 代码示例
        item {
            CodeExamplesCard()
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

// =============================================================
// CertificationNoticeCard — Google Play 商店认证注意事项
// =============================================================
/**
 * Google Play Store Certification Notice Card / Google Play 商店认证注意事项卡片
 */
@Composable
private fun CertificationNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0) // Orange tint / 橙色背景
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "⚠️ Google Play 认证周期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "isAccessibilityTool Google 认证需要数周时间，请提前申请！\n\n" +
                           "认证流程：\n" +
                           "1. 在 Google Play Console 中申请辅助功能工具认证\n" +
                           "2. 提交详细的隐私政策和使用场景说明\n" +
                           "3. 等待 Google 团队审核（通常 2-4 周）\n" +
                           "4. 认证通过后，在 AndroidManifest.xml 中添加标记",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4E342E)
                )
            }
        }
    }
}

// =============================================================
// AccessibilityToolMarkerCard — isAccessibilityTool 标记申请指南
// =============================================================
/**
 * isAccessibilityTool Marker Application Guide Card / isAccessibilityTool 标记申请指南卡片
 */
@Composable
private fun AccessibilityToolMarkerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "📋 isAccessibilityTool 标记申请流程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(
                "在 AndroidManifest.xml 的 <application> 标签中添加 android:accessibilityTool=\"true\" 属性",
                "在 Google Play Console 中提交辅助功能工具认证申请",
                "提供详细的隐私政策文档，说明数据收集和使用方式",
                "描述应用如何帮助残障用户",
                "等待 Google 审核通过",
                "在应用商店列表中明确标注辅助功能用途"
            )

            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF66BB6A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// =============================================================
// DegradationStrategyCard — 降级策略指南
// =============================================================
/**
 * Degradation Strategy Guide Card / 降级策略指南卡片
 */
@Composable
private fun DegradationStrategyCard(
    services: List<com.mvi.kenny.feature.aapm.AccessibilityServiceInfo>,
    onUpdateStrategy: (String, ResponseStrategy) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "🔄 降级策略选择器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "在 AAPM 状态下选择每个服务的降级策略",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (services.isEmpty()) {
                Text(
                    text = "请先在「影响分析」页面扫描服务",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                services.forEach { service ->
                    StrategySelectorItem(
                        serviceName = service.name,
                        currentStrategy = service.recommendedResponse,
                        onStrategyChange = { strategy ->
                            onUpdateStrategy(service.name, strategy)
                        }
                    )

                    if (service != services.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// StrategySelectorItem — 策略选择项
// =============================================================
@Composable
private fun StrategySelectorItem(
    serviceName: String,
    currentStrategy: ResponseStrategy,
    onStrategyChange: (ResponseStrategy) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = serviceName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResponseStrategy.values().forEach { strategy ->
                val isSelected = strategy == currentStrategy
                val color = when (strategy) {
                    ResponseStrategy.DISABLE -> Color(0xFFEF5350)
                    ResponseStrategy.GRACEFUL_DEGRADE -> Color(0xFFFFA726)
                    ResponseStrategy.USER_GUIDANCE -> Color(0xFF42A5F5)
                    ResponseStrategy.NONE -> Color(0xFF66BB6A)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) color.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        .clickable { onStrategyChange(strategy) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (strategy) {
                            ResponseStrategy.DISABLE -> "禁用"
                            ResponseStrategy.GRACEFUL_DEGRADE -> "降级"
                            ResponseStrategy.USER_GUIDANCE -> "引导"
                            ResponseStrategy.NONE -> "无"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// =============================================================
// CodeExamplesCard — 代码示例卡片
// =============================================================
/**
 * Code Examples Card / 代码示例卡片
 */
@Composable
private fun CodeExamplesCard() {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "💻 代码示例",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { /* Copy all code examples */ }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy all"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manifest Declaration Example / Manifest 声明示例
            CodeExampleBlock(
                title = "AndroidManifest.xml 声明",
                code = """
<!-- 在 <application> 标签中添加 -->
<application
    android:accessibilityTool="true"
    ...>

    <!-- Accessibility Service 声明 -->
    <service
        android:name=".service.MyAccessibilityService"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
        android:exported="false">
        <intent-filter>
            <action android:name="android.accessibilityservice.AccessibilityService" />
        </intent-filter>
        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibility_service_config" />
    </service>
</application>
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // isAccessibilityTool Check Example / isAccessibilityTool 检测示例
            CodeExampleBlock(
                title = "isAccessibilityTool 检测 (Kotlin)",
                code = """
// 检查是否为已认证辅助功能工具
fun isAccessibilityTool(context: Context): Boolean {
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(
        context.packageName,
        0
    )
    return packageInfo.applicationInfo?.accessibilityTool == true
}

// 在 AAPM 环境下优雅降级
fun handleAAPMAwareDegradation(context: Context) {
    if (!isAccessibilityTool(context)) {
        // 显示用户引导对话框
        showGuidanceDialog(context)
        // 或者禁用相关功能
        disableAccessibilityFeatures()
    }
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AdvancedProtectionManager Example / AdvancedProtectionManager 示例
            CodeExampleBlock(
                title = "AdvancedProtectionManager API (Android 17.2+)",
                code = """
// 仅在 Android 17.2+ (API 35+) 可用
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    val advancedProtectionManager = context.getSystemService(
        AdvancedProtectionManager::class.java
    )

    val protectionStatus = advancedProtectionManager.protectionStatus
    when (protectionStatus.status) {
        PROTECTION_STATUS_ENABLED -> {
            // AAPM 已启用
        }
        PROTECTION_STATUS_DISABLED -> {
            // AAPM 已禁用
        }
        else -> {
            // 未知状态
        }
    }
}
                """.trimIndent()
            )
        }
    }
}

// =============================================================
// CodeExampleBlock — 代码示例块
// =============================================================
/**
 * Code Example Block / 代码示例块
 *
 * Displays code with syntax highlighting background.
 */
@Composable
private fun CodeExampleBlock(
    title: String,
    code: String
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(code)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1A))
                .padding(12.dp)
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE0E0E0)
            )
        }
    }
}
