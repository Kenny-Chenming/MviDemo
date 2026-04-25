package com.mvi.kenny.feature.onalaarmlistener

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// =============================================================
// SettingsScreen — CI 配置与合规阈值设置
// =============================================================
/**
 * Settings Screen / CI 配置与合规阈值设置
 *
 * Provides configuration options for:
 * - WakeLock duration threshold / WakeLock 时长阈值
 * - CI template selection / CI 模板选择
 * - Notification preferences / 通知偏好
 * - CI template download / CI 模板下载
 *
 * @param state Current UI state / 当前 UI 状态
 * @param onIntent Intent handler / 意图处理器
 */
@Composable
fun SettingsScreen(
    state: OnAlarmState,
    onIntent: (OnAlarmIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ─────────────────────────────────────────────────────
        // Compliance Threshold Section / 合规阈值部分
        // ─────────────────────────────────────────────────────
        item {
            SettingsSection(title = "Compliance Threshold / 合规阈值")
        }

        item {
            WakeLockThresholdCard(
                currentThreshold = state.wakeLockThresholdMinutes,
                onThresholdChange = { onIntent(OnAlarmIntent.UpdateWakeLockThreshold(it)) }
            )
        }

        // ─────────────────────────────────────────────────────
        // CI Template Section / CI 模板部分
        // ─────────────────────────────────────────────────────
        item {
            SettingsSection(title = "CI Configuration / CI 配置")
        }

        item {
            CiTemplateCard(
                selectedTemplate = state.ciTemplateType,
                onTemplateChange = { onIntent(OnAlarmIntent.UpdateCiTemplate(it)) },
                onDownload = { onIntent(OnAlarmIntent.DownloadCiTemplate) },
                isLoading = state.isLoading
            )
        }

        // ─────────────────────────────────────────────────────
        // Notification Section / 通知部分
        // ─────────────────────────────────────────────────────
        item {
            SettingsSection(title = "Notifications / 通知")
        }

        item {
            NotificationSettingsCard(
                enabled = state.notificationsEnabled,
                onToggle = { onIntent(OnAlarmIntent.ToggleNotifications) }
            )
        }

        // ─────────────────────────────────────────────────────
        // About Section / 关于部分
        // ─────────────────────────────────────────────────────
        item {
            SettingsSection(title = "About / 关于")
        }

        item {
            AboutCard()
        }
    }
}

// =============================================================
// SettingsSection — 设置部分标题
// =============================================================
/**
 * Settings section title / 设置部分标题
 */
@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

// =============================================================
// WakeLockThresholdCard — WakeLock 阈值设置卡片
// =============================================================
/**
 * WakeLock duration threshold settings card / WakeLock 时长阈值设置卡片
 *
 * @param currentThreshold Current threshold in minutes / 当前阈值（分钟）
 * @param onThresholdChange Callback when threshold changes / 阈值变化回调
 */
@Composable
private fun WakeLockThresholdCard(
    currentThreshold: Int,
    onThresholdChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "WakeLock Duration Threshold / WakeLock 时长阈值",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "WakeLock instances held longer than this threshold will be flagged as high risk. / 持有时间超过此阈值的 WakeLock 实例将被标记为高风险。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Threshold slider / 阈值滑块
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1 min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currentThreshold min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "30 min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = currentThreshold.toFloat(),
                    onValueChange = { onThresholdChange(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Threshold presets / 阈值预设
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 4, 10, 20).forEach { preset ->
                    FilterChip(
                        selected = currentThreshold == preset,
                        onClick = { onThresholdChange(preset) },
                        label = { Text("${preset}m") }
                    )
                }
            }

            // Explanation / 说明
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Google Play Battery Technical Quality Enforcement threshold: 4 minutes (default). / Google Play 电池技术质量强制执行阈值：4 分钟（默认）。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================
// CiTemplateCard — CI 模板设置卡片
// =============================================================
/**
 * CI template selection card / CI 模板选择卡片
 *
 * @param selectedTemplate Currently selected CI template / 当前选中的 CI 模板
 * @param onTemplateChange Callback when template changes / 模板变化回调
 * @param onDownload Callback to download template / 下载模板回调
 * @param isLoading Whether download is in progress / 下载是否进行中
 */
@Composable
private fun CiTemplateCard(
    selectedTemplate: CiTemplate,
    onTemplateChange: (CiTemplate) -> Unit,
    onDownload: () -> Unit,
    isLoading: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.IntegrationInstructions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "CI Template / CI 模板",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Select your CI system and download a pre-configured workflow template for automated battery compliance scanning. / 选择您的 CI 系统并下载预配置的流水线模板，以进行自动化电池合规扫描。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Template selection / 模板选择
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CiTemplate.entries.forEach { template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedTemplate == template,
                                onClick = { onTemplateChange(template) },
                                role = Role.RadioButton
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedTemplate == template,
                            onClick = null
                        )
                        Column {
                            Text(
                                text = template.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Download button / 下载按钮
            Button(
                onClick = onDownload,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when (selectedTemplate) {
                        CiTemplate.GITHUB_ACTIONS -> "Download GitHub Actions Template / 下载 GitHub Actions 模板"
                        CiTemplate.GITLAB_CI -> "Download GitLab CI Template / 下载 GitLab CI 模板"
                    }
                )
            }

            // Template info / 模板信息
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Template Features / 模板特性",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TemplateFeatureRow(Icons.Default.Check, "Runs on every PR and push to main / 每次 PR 和 main 分支推送时运行")
                    TemplateFeatureRow(Icons.Default.Check, "Battery compliance scan as part of CI / 作为 CI 的一部分进行电池合规扫描")
                    TemplateFeatureRow(Icons.Default.Check, "SARIF report generation / 生成 SARIF 格式报告")
                    TemplateFeatureRow(Icons.Default.Check, "GitHub/GitLab integration / GitHub/GitLab 集成")
                }
            }
        }
    }
}

/**
 * Template feature row / 模板特性行
 */
@Composable
private fun TemplateFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =============================================================
// NotificationSettingsCard — 通知设置卡片
// =============================================================
/**
 * Notification settings card / 通知设置卡片
 *
 * @param enabled Whether notifications are enabled / 通知是否启用
 * @param onToggle Callback to toggle notifications / 切换通知回调
 */
@Composable
private fun NotificationSettingsCard(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (enabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Compliance Alerts / 合规提醒",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Get notified when your app's battery compliance score drops. / 当应用的电池合规评分下降时收到通知。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

// =============================================================
// AboutCard — 关于卡片
// =============================================================
/**
 * About card / 关于卡片
 */
@Composable
private fun AboutCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Android 17 OnAlarmListener Toolkit / Android 17 OnAlarmListener 工具包",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider()

            AboutRow("Version / 版本", "1.0.0")
            AboutRow("Target SDK", "API 35 (Android 15)")
            AboutRow("Min SDK", "API 29 (Android 10)")
            AboutRow("Framework / 框架", "Jetpack Compose + MVI")

            HorizontalDivider()

            Text(
                text = "PRD-150 | Android 17 OnAlarmListener Battery Optimization & Background Task Scheduling Dev Toolkit. / Android 17 OnAlarmListener 电池优化与后台任务调度开发工具包。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "For Android 17 Beta 4 (API 35). OnAlarmListener API official docs: developer.android.com/about/versions/17. / 适用于 Android 17 Beta 4 (API 35)。OnAlarmListener API 官方文档：developer.android.com/about/versions/17。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
