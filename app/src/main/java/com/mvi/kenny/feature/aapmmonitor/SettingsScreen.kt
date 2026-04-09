package com.mvi.kenny.feature.aapmmonitor

/**
 * ============================================================
 * SettingsScreen.kt — 设置页
 * SettingsScreen.kt — Settings Screen
 * ============================================================
 * AAPM 检测工具配置页，包括扫描范围、报告格式、通知设置等
 * AAPM detection tool configuration page including scan scope,
 * report format, notification settings, etc.
 */

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// Theme Colors — 主题颜色
// Theme Colors — Consistent with AAPMonitorScreen
// ============================================================

private object SettingsColors {
    val Background = Color(0xFF1A1A2E)
    val CardBackground = Color(0xFF16213E)
    val SurfaceVariant = Color(0xFF1F2B47)
    val SafeColor = Color(0xFF27AE60)
    val P1Color = Color(0xFFF39C12)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0B0)
    val TextTertiary = Color(0xFF6B6B80)
}

// ============================================================
// Settings Screen — 设置页
// ============================================================

/**
 * 设置页
 * Settings screen
 *
 * @param onNavigateBack 返回回调
 * @param onExportReport 导出报告回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onExportReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scanScope by remember { mutableStateOf(ScanScope.FULL_PROJECT) }
    var reportFormat by remember { mutableStateOf(ReportFormat.JSON) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var autoRefreshAAPM by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        color = SettingsColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = SettingsColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsColors.CardBackground
                )
            )
        },
        containerColor = SettingsColors.Background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== 扫描配置 ==========
            // Scan Configuration
            item {
                SettingsSectionHeader(
                    title = "扫描配置",
                    icon = Icons.Default.Search
                )
            }

            item {
                ScanScopeCard(
                    selectedScope = scanScope,
                    onScopeChange = { scanScope = it }
                )
            }

            // ========== 报告设置 ==========
            // Report Settings
            item {
                SettingsSectionHeader(
                    title = "报告设置",
                    icon = Icons.Default.Description
                )
            }

            item {
                ReportFormatCard(
                    selectedFormat = reportFormat,
                    onFormatChange = { reportFormat = it }
                )
            }

            item {
                SettingsSwitchCard(
                    title = "自动导出报告",
                    description = "扫描完成后自动导出报告到 Downloads 目录",
                    icon = Icons.Default.Save,
                    checked = false, // Always false, just for display
                    onCheckedChange = { }
                )
            }

            // ========== 通知与监控 ==========
            // Notification & Monitoring
            item {
                SettingsSectionHeader(
                    title = "通知与监控",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                SettingsSwitchCard(
                    title = "扫描完成通知",
                    description = "扫描完成后发送系统通知",
                    icon = Icons.Default.Notifications,
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }

            item {
                SettingsSwitchCard(
                    title = "自动刷新 AAPM 状态",
                    description = "每 2 秒自动刷新设备 AAPM 状态（设备处于后台时降低频率）",
                    icon = Icons.Default.Refresh,
                    checked = autoRefreshAAPM,
                    onCheckedChange = { autoRefreshAAPM = it }
                )
            }

            // ========== 界面设置 ==========
            // UI Settings
            item {
                SettingsSectionHeader(
                    title = "界面设置",
                    icon = Icons.Default.Palette
                )
            }

            item {
                SettingsSwitchCard(
                    title = "深色模式",
                    description = "使用深色主题界面",
                    icon = Icons.Default.DarkMode,
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            // ========== 关于 ==========
            // About
            item {
                SettingsSectionHeader(
                    title = "关于",
                    icon = Icons.Default.Info
                )
            }

            item {
                AboutCard()
            }

            // ========== 底部操作 ==========
            // Bottom Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onExportReport,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SettingsColors.SafeColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出当前配置", fontSize = 16.sp)
                }
            }

            item {
                OutlinedButton(
                    onClick = { /* Reset to defaults */ },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, SettingsColors.SurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重置为默认设置", fontSize = 16.sp, color = SettingsColors.TextPrimary)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ============================================================
// Section Header — 设置区块标题
// ============================================================

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SettingsColors.SafeColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = SettingsColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ============================================================
// Scan Scope Card — 扫描范围卡片
// ============================================================

@Composable
private fun ScanScopeCard(
    selectedScope: ScanScope,
    onScopeChange: (ScanScope) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "扫描范围",
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            ScanScope.entries.forEach { scope ->
                ScanScopeOption(
                    scope = scope,
                    isSelected = selectedScope == scope,
                    onSelect = { onScopeChange(scope) }
                )
                if (scope != ScanScope.entries.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ScanScopeOption(
    scope: ScanScope,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) SettingsColors.SafeColor.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onSelect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = SettingsColors.SafeColor
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scope.title,
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = scope.description,
                color = SettingsColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================================
// Report Format Card — 报告格式卡片
// ============================================================

@Composable
private fun ReportFormatCard(
    selectedFormat: ReportFormat,
    onFormatChange: (ReportFormat) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "报告格式",
                color = SettingsColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportFormat.entries.forEach { format ->
                    FormatChip(
                        format = format,
                        isSelected = selectedFormat == format,
                        onSelect = { onFormatChange(format) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatChip(
    format: ReportFormat,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) SettingsColors.SafeColor
                else SettingsColors.SurfaceVariant
            )
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (format) {
                    ReportFormat.JSON -> Icons.Default.DataObject
                    ReportFormat.MARKDOWN -> Icons.Default.Description
                    ReportFormat.HTML -> Icons.Default.Web
                },
                contentDescription = null,
                tint = if (isSelected) SettingsColors.Background else SettingsColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = format.extension.uppercase(),
                color = if (isSelected) SettingsColors.Background else SettingsColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ============================================================
// Settings Switch Card — 设置开关卡片
// ============================================================

@Composable
private fun SettingsSwitchCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SettingsColors.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = SettingsColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = SettingsColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SettingsColors.SafeColor,
                    checkedTrackColor = SettingsColors.SafeColor.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// ============================================================
// About Card — 关于卡片
// ============================================================

@Composable
private fun AboutCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsColors.CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SettingsColors.SafeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SettingsColors.Background,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AAPM 检测工具",
                color = SettingsColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "版本 1.0.0",
                color = SettingsColors.TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AboutInfoItem(
                    label = "Android 版本",
                    value = "17+"
                )
                AboutInfoItem(
                    label = "构建日期",
                    value = "2026-04-10"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "基于 Android 17 AdvancedProtectionManager API 设计\n提供 AccessibilityService 迁移检测与合规工具",
                color = SettingsColors.TextTertiary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AboutInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = SettingsColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = SettingsColors.TextSecondary,
            fontSize = 12.sp
        )
    }
}

// ============================================================
// Enums — 枚举定义
// ============================================================

/**
 * 扫描范围选项
 * Scan scope options
 */
private enum class ScanScope(
    val title: String,
    val description: String
) {
    FULL_PROJECT("全项目扫描", "扫描整个项目所有模块"),
    CURRENT_MODULE("当前模块", "仅扫描当前选中的模块"),
    CUSTOM("自定义范围", "手动选择要扫描的模块和文件")
}

/**
 * 报告格式
 * Report export format
 */
private enum class ReportFormat(val extension: String) {
    JSON("json"),
    MARKDOWN("md"),
    HTML("html")
}
