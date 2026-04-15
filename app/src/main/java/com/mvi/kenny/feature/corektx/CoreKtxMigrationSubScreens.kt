package com.mvi.kenny.feature.corektx

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ================================================================
// Scanner Tab / 扫描器 Tab
// ================================================================

/**
 * ============================================================
 * ScannerTab — 扫描结果列表 Tab
 * ================================================================
 * Displays scan results with filtering and fix capabilities.
 */
@Composable
internal fun ScannerTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    var showFixAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter Chips / 过滤 Chips
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipsRow(
                    selectedSeverities = state.filterSeverity,
                    onSeverityToggle = { severity ->
                        val newSet = if (severity in state.filterSeverity) {
                            state.filterSeverity - severity
                        } else {
                            state.filterSeverity + severity
                        }
                        viewModel.sendIntent(CoreKtxMigrationIntent.FilterBySeverity(newSet))
                    },
                    onFixAll = { showFixAllDialog = true },
                    hasResults = state.scanResults.isNotEmpty()
                )
            }

            // Scan Results / 扫描结果列表
            if (state.scanState == ScanState.Idle) {
                item {
                    EmptyScanState(
                        onStartScan = { viewModel.sendIntent(CoreKtxMigrationIntent.StartScan) }
                    )
                }
            } else if (state.scanState == ScanState.Scanning) {
                item {
                    ScanningIndicator(progress = state.scanProgress)
                }
            } else {
                items(
                    items = state.filteredResults,
                    key = { it.id }
                ) { result ->
                    ScanResultItem(
                        result = result,
                        onFix = { viewModel.sendIntent(CoreKtxMigrationIntent.FixItem(result.id)) }
                    )
                }
            }
        }
    }

    // Fix All Confirmation Dialog / 全部修复确认对话框
    if (showFixAllDialog) {
        AlertDialog(
            onDismissRequest = { showFixAllDialog = false },
            title = { Text("确认全部修复 / Confirm Fix All") },
            text = {
                Text("将修复所有 P${state.settings.minSeverityForFix.order}+ 级别的问题项，确定继续？")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendIntent(CoreKtxMigrationIntent.FixAll)
                        showFixAllDialog = false
                    }
                ) {
                    Text("确认 / Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFixAllDialog = false }) {
                    Text("取消 / Cancel")
                }
            }
        )
    }

    // Single Fix Confirmation Dialog / 单项修复确认对话框
    if (state.showFixDialog && state.pendingFixItem != null) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(CoreKtxMigrationIntent.CancelFix) },
            title = { Text("确认修复 / Confirm Fix") },
            text = {
                Column {
                    Text("文件 / File: ${state.pendingFixItem!!.filePath}")
                    Text("行号 / Line: ${state.pendingFixItem!!.lineNumber}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.pendingFixItem!!.fixSuggestion,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.sendIntent(CoreKtxMigrationIntent.ConfirmFix) }
                ) {
                    Text("修复 / Fix")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(CoreKtxMigrationIntent.CancelFix) }) {
                    Text("取消 / Cancel")
                }
            }
        )
    }
}

/**
 * ============================================================
 * FilterChipsRow — 过滤 Chips 行
 * ================================================================
 */
@Composable
private fun FilterChipsRow(
    selectedSeverities: Set<Severity>,
    onSeverityToggle: (Severity) -> Unit,
    onFixAll: () -> Unit,
    hasResults: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = Severity.P0 in selectedSeverities,
            onClick = { onSeverityToggle(Severity.P0) },
            label = { Text("P0") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = if (Severity.P0 in selectedSeverities) Color.White else Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFF44336)
            )
        )

        FilterChip(
            selected = Severity.P1 in selectedSeverities,
            onClick = { onSeverityToggle(Severity.P1) },
            label = { Text("P1") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (Severity.P1 in selectedSeverities) Color.White else Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFF9800)
            )
        )

        FilterChip(
            selected = Severity.P2 in selectedSeverities,
            onClick = { onSeverityToggle(Severity.P2) },
            label = { Text("P2") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (hasResults) {
            Button(
                onClick = onFixAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("一键修复", fontSize = 13.sp)
            }
        }
    }
}

/**
 * ============================================================
 * EmptyScanState — 空闲扫描状态
 * ================================================================
 */
@Composable
private fun EmptyScanState(onStartScan: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "尚未扫描 / Not Scanned",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "点击开始扫描项目中的 core-ktx 使用情况",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStartScan) {
                Text("开始扫描 / Start Scan")
            }
        }
    }
}

/**
 * ============================================================
 * ScanningIndicator — 扫描中指示器
 * ================================================================
 */
@Composable
private fun ScanningIndicator(progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "扫描中... ${(progress * 100).toInt()}% / Scanning... ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * ============================================================
 * ScanResultItem — 扫描结果项
 * ================================================================
 */
@Composable
private fun ScanResultItem(
    result: ScanResult,
    onFix: () -> Unit
) {
    val severityColor = when (result.severity) {
        Severity.P0 -> Color(0xFFF44336)
        Severity.P1 -> Color(0xFFFF9800)
        Severity.P2 -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row / 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Severity Badge / 严重程度徽章
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(severityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = result.filePath,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Line ${result.lineNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (result.isFixed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "已修复 / Fixed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (result.fixAvailable) {
                    OutlinedButton(
                        onClick = onFix,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("修复", fontSize = 12.sp)
                    }
                }
            }

            // Code Snippet / 代码片段
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                Text(
                    text = result.content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Fix Suggestion / 修复建议
            if (result.fixAvailable && !result.isFixed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "建议 / Suggestion: ${result.fixSuggestion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ================================================================
// Validator Tab / 验证器 Tab
// ================================================================

/**
 * ============================================================
 * ValidatorTab — Import 验证 Tab
 * ================================================================
 * Validates Kotlin extension function import compatibility.
 */
@Composable
internal fun ValidatorTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    var inputPath by remember { mutableStateOf("app/src/main/java/com/mvi/kenny/feature") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input Section / 输入区域
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Kotlin 扩展函数 Import 验证器",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "验证迁移到 core 后 import 语句是否仍然兼容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputPath,
                    onValueChange = { inputPath = it },
                    label = { Text("文件路径 / File Path") },
                    placeholder = { Text("app/src/main/java/com/mvi/kenny/feature") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { viewModel.sendIntent(CoreKtxMigrationIntent.ValidateImports(inputPath)) },
                        enabled = inputPath.isNotBlank() && !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("验证 / Validate")
                    }
                }
            }
        }

        // Loading / 加载中
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text = "验证中... / Validating...",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Validation Results / 验证结果
        if (state.validatorResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "验证结果 / Validation Results (${state.validatorResults.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.validatorResults) { result ->
                    ValidationResultItem(result = result)
                }
            }
        }
    }
}

/**
 * ============================================================
 * ValidationResultItem — 验证结果项
 * ================================================================
 */
@Composable
private fun ValidationResultItem(result: ValidationResult) {
    val (icon, color) = when (result.status) {
        ValidationStatus.Pass -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        ValidationStatus.Warn -> Icons.Default.Warning to Color(0xFFFF9800)
        ValidationStatus.Fail -> Icons.Default.Error to Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.filePath,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = result.importStatement,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
        }
    }
}

// ================================================================
// Impact Tab / 影响分析 Tab
// ================================================================

/**
 * ============================================================
 * ImpactTab — 第三方库影响分析 Tab
 * ================================================================
 * Analyzes impact of core-ktx on third-party dependencies.
 */
@Composable
internal fun ImpactTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    // Generate mock impact data / 生成模拟影响数据
    val mockImpacts = remember {
        listOf(
            ThirdPartyImpact(
                dependencyName = "androidx.lifecycle",
                version = "2.8.0",
                hasCoreKtx = true,
                impactLevel = "Low",
                transitiveDeps = listOf("core-ktx:1.15.0"),
                recommendation = "升级到最新版本以解除 core-ktx 依赖 / Upgrade to latest version"
            ),
            ThirdPartyImpact(
                dependencyName = "androidx.navigation",
                version = "2.8.0",
                hasCoreKtx = true,
                impactLevel = "Low",
                transitiveDeps = listOf("core-ktx:1.12.0"),
                recommendation = "迁移后自动解除 / Automatically resolved after migration"
            ),
            ThirdPartyImpact(
                dependencyName = "coil-compose",
                version = "2.6.0",
                hasCoreKtx = false,
                impactLevel = "None",
                recommendation = "无需操作 / No action needed"
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "第三方库 core-ktx 依赖影响分析",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "以下库直接依赖 core-ktx，迁移后可能需要升级",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(mockImpacts) { impact ->
            ThirdPartyImpactItem(impact = impact)
        }
    }
}

/**
 * ============================================================
 * ThirdPartyImpactItem — 第三方库影响项
 * ================================================================
 */
@Composable
private fun ThirdPartyImpactItem(impact: ThirdPartyImpact) {
    val impactColor = when (impact.impactLevel) {
        "High" -> Color(0xFFF44336)
        "Medium" -> Color(0xFFFF9800)
        "Low" -> Color(0xFF4CAF50)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = impact.dependencyName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "v${impact.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(impactColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = impact.impactLevel,
                        style = MaterialTheme.typography.labelSmall,
                        color = impactColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (impact.transitiveDeps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "传递依赖 / Transitive: ${impact.transitiveDeps.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = impact.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ================================================================
// Compliance Tab / 合规检查 Tab
// ================================================================

/**
 * ============================================================
 * ComplianceTab — CI/CD 合规检查 Tab
 * ================================================================
 * Displays CI/CD compliance rules and curl command examples.
 */
@Composable
internal fun ComplianceTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CI/CD 合规检查 / Compliance Rules",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "配置 CI/CD 流水线，自动化检测 core-ktx 迁移合规性",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(state.complianceRules) { rule ->
            ComplianceRuleItem(
                rule = rule,
                onCopyCurl = { /* Copy to clipboard */ }
            )
        }
    }
}

/**
 * ============================================================
 * ComplianceRuleItem — 合规规则项
 * ================================================================
 */
@Composable
private fun ComplianceRuleItem(
    rule: ComplianceRule,
    onCopyCurl: () -> Unit
) {
    val levelColor = when (rule.level) {
        ComplianceLevel.Compliant -> Color(0xFF4CAF50)
        ComplianceLevel.Warning -> Color(0xFFFF9800)
        ComplianceLevel.Violation -> Color(0xFFF44336)
    }

    val levelIcon = when (rule.level) {
        ComplianceLevel.Compliant -> Icons.Default.CheckCircle
        ComplianceLevel.Warning -> Icons.Default.Warning
        ComplianceLevel.Violation -> Icons.Default.Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = rule.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = levelIcon,
                        contentDescription = null,
                        tint = levelColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rule.level.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = levelColor
                    )
                }
            }

            if (rule.curlCommand.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF263238))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rule.curlCommand,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onCopyCurl) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "最后检查 / Last checked: ${rule.lastChecked}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ================================================================
// Settings Tab / 设置 Tab
// ================================================================

/**
 * ============================================================
 * SettingsTab — 设置 Tab
 * ================================================================
 * Configure migration tool settings.
 */
@Composable
internal fun SettingsTab(
    state: CoreKtxMigrationState,
    viewModel: CoreKtxMigrationViewModel
) {
    val settings = state.settings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "扫描配置 / Scan Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto Fix Toggle / 自动修复开关
                    SettingsSwitchItem(
                        title = "自动修复 / Auto Fix",
                        description = "允许自动修复 P${settings.minSeverityForFix.order}+ 级别问题",
                        checked = settings.autoFixEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.sendIntent(
                                CoreKtxMigrationIntent.UpdateSettings(
                                    settings.copy(autoFixEnabled = enabled)
                                )
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Backup Toggle / 备份开关
                    SettingsSwitchItem(
                        title = "修复前备份 / Backup Before Fix",
                        description = "修复前自动创建备份",
                        checked = settings.backupEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.sendIntent(
                                CoreKtxMigrationIntent.UpdateSettings(
                                    settings.copy(backupEnabled = enabled)
                                )
                            )
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // R8 Rules Check / R8 规则检查
                    SettingsSwitchItem(
                        title = "R8/Proguard 规则检查 / R8 Rules Check",
                        description = "检查混淆规则是否需要迁移",
                        checked = settings.enableR8RulesCheck,
                        onCheckedChange = { enabled ->
                            viewModel.sendIntent(
                                CoreKtxMigrationIntent.UpdateSettings(
                                    settings.copy(enableR8RulesCheck = enabled)
                                )
                            )
                        }
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "排除路径 / Excluded Paths",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    settings.scanExcludePaths.forEach { path ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = path,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "关于 / About",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AndroidX core-ktx 合并至 core 历史性迁移检测与自动化工具包",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "PRD-115 | AndroidX core-ktx → core Migration Toolkit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * ============================================================
 * SettingsSwitchItem — 设置开关项
 * ================================================================
 */
@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
