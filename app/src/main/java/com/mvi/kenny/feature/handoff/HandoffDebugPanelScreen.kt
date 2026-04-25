package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffDebugPanelScreen — 跨设备调试面板
// Cross-Device Handoff Debug Panel Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 5: Debug Panel
// 功能：模拟源设备→目标设备的 Handoff 流程，实时展示状态恢复结果
//
// Features / 功能:
// - Select source/target device types / 选择源/目标设备类型
// - Start debug session simulation / 启动调试会话模拟
// - View session history / 查看会话历史

@Composable
fun HandoffDebugPanelScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "跨设备调试面板",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "模拟 Handoff 流程，测试状态恢复 / Simulate Handoff flow and test state restoration",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device selection / 设备选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "设备配置 / Device Configuration", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source device / 源设备
                    DeviceSelector(
                        label = "源设备 / Source",
                        selectedDevice = state.selectedSourceDevice,
                        onDeviceSelected = { onIntent(HandoffIntent.SelectSourceDevice(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    // Target device / 目标设备
                    DeviceSelector(
                        label = "目标设备 / Target",
                        selectedDevice = state.selectedTargetDevice,
                        onDeviceSelected = { onIntent(HandoffIntent.SelectTargetDevice(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Device compatibility note / 设备兼容性说明
                val compatible = isDevicePairCompatible(state.selectedSourceDevice, state.selectedTargetDevice)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (compatible)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (compatible)
                            "✅ 设备配对兼容 / Device pair compatible — ${getDevicePairNote(state.selectedSourceDevice, state.selectedTargetDevice)}"
                        else "⚠️ 设备配对可能有问题 / Device pair may have issues",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onIntent(HandoffIntent.StartDebugSession) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("启动调试会话 / Start Debug Session")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active session / 当前会话
        state.activeSession?.let { session ->
            Text(text = "当前会话 / Active Session", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ActiveSessionCard(session = session)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session history / 会话历史
        if (state.debugSessions.isNotEmpty()) {
            Text(
                text = "会话历史 / Session History (${state.debugSessions.size})",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.debugSessions.filter { it.id != state.activeSession?.id }) { session ->
                    SessionHistoryCard(
                        session = session,
                        onClick = { onIntent(HandoffIntent.SelectDebugSession(session)) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DeviceSelector(
    label: String,
    selectedDevice: DeviceType,
    onDeviceSelected: (DeviceType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier
        ) {
            OutlinedTextField(
                value = selectedDevice.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DeviceType.entries.forEach { device ->
                    DropdownMenuItem(
                        text = { Text(device.label) },
                        onClick = {
                            onDeviceSelected(device)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(session: DebugSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (session.status) {
                SessionStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                SessionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                SessionStatus.RUNNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                SessionStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${session.sourceDevice.label} → ${session.targetDevice.label}",
                    style = MaterialTheme.typography.titleSmall
                )
                AssistChip(
                    onClick = {},
                    label = { Text(session.status.label, style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            session.steps.forEach { step ->
                HandoffStepRow(step = step)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HandoffStepRow(step: HandoffStep) {
    val stepColor = when (step.status) {
        StepStatus.DONE -> MaterialTheme.colorScheme.primary
        StepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        StepStatus.ERROR -> MaterialTheme.colorScheme.error
        StepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (step.status) {
                StepStatus.DONE -> "✅"
                StepStatus.IN_PROGRESS -> "⏳"
                StepStatus.ERROR -> "❌"
                StepStatus.PENDING -> "○"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.bodySmall,
                color = stepColor
            )
            Text(
                text = step.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (step.status == StepStatus.IN_PROGRESS) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun SessionHistoryCard(
    session: DebugSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                Text(
                    text = "${session.sourceDevice.label} → ${session.targetDevice.label}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${session.steps.count { it.status == StepStatus.DONE }}/${session.steps.size} steps completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        when (session.status) {
                            SessionStatus.SUCCESS -> "✅"
                            SessionStatus.FAILED -> "❌"
                            else -> "⏳"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}

private fun isDevicePairCompatible(source: DeviceType, target: DeviceType): Boolean {
    return when {
        source == DeviceType.PHONE && target == DeviceType.TABLET -> true
        source == DeviceType.TABLET && target == DeviceType.PHONE -> true
        source == target -> true
        source == DeviceType.PHONE && target == DeviceType.AUTOMOTIVE -> true
        source == DeviceType.AUTOMOTIVE && target == DeviceType.PHONE -> true
        else -> source != DeviceType.WATCH && target != DeviceType.WATCH
    }
}

private fun getDevicePairNote(source: DeviceType, target: DeviceType): String {
    return when {
        source == DeviceType.PHONE && target == DeviceType.TABLET ->
            "手机→平板是最佳 Handoff 场景 / Phone→Tablet is the best Handoff scenario"
        source == DeviceType.TABLET && target == DeviceType.PHONE ->
            "平板→手机需要确认 Activity 在手机上可用 / Ensure Activity is available on Phone"
        source == DeviceType.PHONE && target == DeviceType.AUTOMOTIVE ->
            "手机→车机需要特殊权限配置 / Special permission config required for Phone→Automotive"
        source == DeviceType.AUTOMOTIVE && target == DeviceType.PHONE ->
            "车机→手机需要确认应用状态可迁移 / Confirm app state is transferable"
        else -> "标准 Handoff 行为 / Standard Handoff behavior"
    }
}
