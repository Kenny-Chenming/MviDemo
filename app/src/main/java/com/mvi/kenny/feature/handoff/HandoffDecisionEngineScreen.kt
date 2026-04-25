package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffDecisionEngineScreen — 适用性决策引擎
// Handoff Suitability Decision Engine Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 3: Decision Engine
// 功能：分析 Activity 列表，输出「适合 Handoff」vs「不适合 Handoff」报告
//
// Features / 功能:
// - Add/remove Activity inputs / 添加/删除 Activity 输入
// - Analyze suitability for Handoff / 分析 Handoff 适用性
// - Display decision results with confidence scores / 显示决策结果和置信度

@Composable
fun HandoffDecisionEngineScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header / 标题
        Text(
            text = "适用性决策引擎",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "分析 Activity 是否适合 Handoff / Analyze if Activities are suitable for Handoff",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Activity button / 添加 Activity 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity 列表 / Activities (${state.activityInputs.size})",
                style = MaterialTheme.typography.titleSmall
            )
            FilledTonalButton(
                onClick = { showAddDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加 / Add", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.activityInputs) { input ->
                ActivityInputCard(
                    input = input,
                    onRemove = { onIntent(HandoffIntent.RemoveActivityInput(input.id)) },
                    onUpdate = { onIntent(HandoffIntent.UpdateActivityInput(it)) }
                )
            }

            // Analyze button / 分析按钮
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onIntent(HandoffIntent.AnalyzeActivities) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.activityInputs.isNotEmpty() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始分析 / Start Analysis")
                }
            }

            // Decision results / 决策结果
            if (state.decisionResults.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "分析结果 / Analysis Results",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                items(state.decisionResults) { result ->
                    DecisionResultCard(result = result)
                }
            }
        }
    }

    if (showAddDialog) {
        AddActivityDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onIntent(HandoffIntent.AddActivityInput(it))
                showAddDialog = false
            }
        )
    }
}

// =============================================================
// ActivityInputCard — Activity 输入卡片
// =============================================================
@Composable
private fun ActivityInputCard(
    input: ActivityInput,
    onRemove: () -> Unit,
    onUpdate: (ActivityInput) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = input.name,
                        onValueChange = { onUpdate(input.copy(name = it)) },
                        label = { Text("Activity 名称 / Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = input.hasUserData,
                            onClick = { onUpdate(input.copy(hasUserData = !input.hasUserData)) },
                            label = { Text("用户数据 / User Data") }
                        )
                        FilterChip(
                            selected = input.hasNetworkData,
                            onClick = { onUpdate(input.copy(hasNetworkData = !input.hasNetworkData)) },
                            label = { Text("网络依赖 / Network") }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Sensitivity selector / 敏感度选择
                    var sensExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = sensExpanded,
                        onExpandedChange = { sensExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = input.dataSensitivity.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("敏感度 / Sensitivity") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sensExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = sensExpanded,
                            onDismissRequest = { sensExpanded = false }
                        ) {
                            DataSensitivity.entries.forEach { sens ->
                                DropdownMenuItem(
                                    text = { Text(sens.label) },
                                    onClick = {
                                        onUpdate(input.copy(dataSensitivity = sens))
                                        sensExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// =============================================================
// DecisionResultCard — 决策结果卡片
// =============================================================
@Composable
private fun DecisionResultCard(result: DecisionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.suitable)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.activityName,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (result.suitable) "✅ 适合 / Suitable" else "❌ 不适合 / Not Suitable",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (result.suitable)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence bar / 置信度条
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("置信度 / Confidence: ", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { result.confidence },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = if (result.suitable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(result.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (result.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                result.reasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (result.risks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                result.risks.forEach { risk ->
                    Text(
                        text = "⚠️ $risk",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// =============================================================
// AddActivityDialog — 添加 Activity 对话框
// =============================================================
@Composable
private fun AddActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (ActivityInput) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hasUserData by remember { mutableStateOf(false) }
    var hasNetworkData by remember { mutableStateOf(false) }
    var dataSensitivity by remember { mutableStateOf(DataSensitivity.LOW) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 Activity / Add Activity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Activity 类名 / Class Name") },
                    placeholder = { Text("com.example.MyActivity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasUserData, onCheckedChange = { hasUserData = it })
                    Text("包含用户数据 / Contains user data")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasNetworkData, onCheckedChange = { hasNetworkData = it })
                    Text("依赖网络 / Depends on network")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        ActivityInput(
                            name = name,
                            hasUserData = hasUserData,
                            hasNetworkData = hasNetworkData,
                            dataSensitivity = dataSensitivity
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("确认 / Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消 / Cancel") }
        }
    )
}
