package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffPrivacyComplianceScreen — 隐私合规检测
// Privacy Compliance Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 4: Privacy Compliance Detection
// 功能：输入数据类型，输出 GDPR/CCPA 合规报告

@Composable
fun HandoffPrivacyComplianceScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    var newDataType by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "隐私合规检测",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "检测数据类型是否满足 GDPR/CCPA 合规要求 / Check if data types meet GDPR/CCPA compliance requirements",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data type input / 数据类型输入
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "数据类型 / Data Types (${state.dataTypesInput.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newDataType,
                        onValueChange = { newDataType = it },
                        label = { Text("数据类型 / Data Type") },
                        placeholder = { Text("e.g., email, location, health") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    FilledTonalButton(
                        onClick = {
                            if (newDataType.isNotBlank()) {
                                onIntent(HandoffIntent.AddDataType(newDataType))
                                newDataType = ""
                            }
                        },
                        enabled = newDataType.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("添加 / Add")
                    }
                }

                // Data type chips / 数据类型标签
                if (state.dataTypesInput.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.dataTypesInput.forEach { type ->
                            InputChip(
                                selected = false,
                                onClick = { onIntent(HandoffIntent.RemoveDataType(type)) },
                                label = { Text(type) },
                                trailingIcon = {
                                    Text("×", style = MaterialTheme.typography.labelMedium)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onIntent(HandoffIntent.GeneratePrivacyReport(state.dataTypesInput)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.dataTypesInput.isNotEmpty() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成合规报告 / Generate Compliance Report")
                }
            }
        }

        // Privacy report / 隐私合规报告
        state.privacyReport?.let { report ->
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Overall status / 整体状态
                item {
                    OverallStatusCard(report = report)
                }

                // Compliance badges / 合规徽章
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ComplianceBadge(
                            label = "GDPR",
                            compliant = report.gdprCompliant,
                            modifier = Modifier.weight(1f)
                        )
                        ComplianceBadge(
                            label = "CCPA",
                            compliant = report.ccpCompliant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Cross-device data items / 跨设备数据传输项
                if (report.crossDeviceDataItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "跨设备数据项 / Cross-Device Data Items",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    items(report.crossDeviceDataItems) { item ->
                        CrossDeviceDataItemCard(item = item)
                    }
                }

                // Violations / 违规项
                if (report.violations.isNotEmpty()) {
                    item {
                        Text(
                            text = "违规项 / Violations (${report.violations.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(report.violations) { violation ->
                        ViolationCard(violation = violation)
                    }
                }

                // Suggestions / 改进建议
                if (report.suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "改进建议 / Suggestions",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    items(report.suggestions) { suggestion ->
                        SuggestionCard(suggestion = suggestion)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallStatusCard(report: PrivacyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = report.overallStatus.color.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "整体合规状态 / Overall Compliance",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (report.overallStatus) {
                        ComplianceStatus.PASS -> "✅ 通过 / Pass — 无重大合规问题"
                        ComplianceStatus.WARN -> "⚠️ 警告 / Warn — 有改进空间"
                        ComplianceStatus.FAIL -> "❌ 不通过 / Fail — 存在合规违规"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = when (report.overallStatus) {
                    ComplianceStatus.PASS -> "PASS"
                    ComplianceStatus.WARN -> "WARN"
                    ComplianceStatus.FAIL -> "FAIL"
                },
                style = MaterialTheme.typography.headlineMedium,
                color = report.overallStatus.color
            )
        }
    }
}

@Composable
private fun ComplianceBadge(label: String, compliant: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (compliant) Color(0xFF146B3A).copy(alpha = 0.15f)
            else Color(0xFFB3261E).copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (compliant) "✅ 合规 / Compliant" else "❌ 不合规 / Non-Compliant",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CrossDeviceDataItemCard(item: CrossDeviceDataItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "敏感度: ${item.sensitivity.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = item.sensitivity.color
                )
            }
            if (item.requiresEncryption) {
                AssistChip(
                    onClick = {},
                    label = { Text("🔒 需加密", style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ViolationCard(violation: PrivacyViolation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = violation.regulation,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
                AssistChip(
                    onClick = {},
                    label = { Text(violation.severity.label, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = violation.severity.color.copy(alpha = 0.2f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = violation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
