package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Connection Wizard Screen / 连接向导屏幕
 * Step-by-step MCP Server connection configuration
 * 分步配置 MCP Server 连接
 */
@Composable
fun ConnectionWizardScreen(
    state: ConnectionWizardState,
    onIntent: (ConnectionWizardIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress indicator / 进度指示器
        LinearProgressIndicator(
            progress = { (state.currentStep + 1) / 3f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Step content / 步骤内容
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (state.currentStep) {
                0 -> StepTypeSelection(
                    state = state,
                    onIntent = onIntent
                )
                1 -> StepConfiguration(
                    state = state,
                    onIntent = onIntent
                )
                2 -> StepVerifyAndSave(
                    state = state,
                    onIntent = onIntent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons / 导航按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.currentStep > 0) {
                OutlinedButton(
                    onClick = { onIntent(ConnectionWizardIntent.PreviousStep) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back / 上一步")
                }
            }
            Button(
                onClick = {
                    if (state.currentStep == 2) {
                        onIntent(ConnectionWizardIntent.SaveConnection)
                    } else {
                        onIntent(ConnectionWizardIntent.NextStep)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = when (state.currentStep) {
                    0 -> state.connectionType != null
                    1 -> state.serverUrl.isNotBlank()
                    else -> !state.isSaving
                }
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        when (state.currentStep) {
                            0 -> "Next / 下一步"
                            1 -> "Next / 下一步"
                            else -> "Save / 保存"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepTypeSelection(
    state: ConnectionWizardState,
    onIntent: (ConnectionWizardIntent) -> Unit
) {
    Text(
        "选择连接类型 / Select Connection Type",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    ConnectionTypeCard(
        type = ConnectionType.EXTERNAL,
        isSelected = state.connectionType == ConnectionType.EXTERNAL,
        onClick = { onIntent(ConnectionWizardIntent.SelectConnectionType(ConnectionType.EXTERNAL)) }
    )
    Spacer(modifier = Modifier.height(12.dp))
    ConnectionTypeCard(
        type = ConnectionType.ANDROID_TEMPLATE,
        isSelected = state.connectionType == ConnectionType.ANDROID_TEMPLATE,
        onClick = { onIntent(ConnectionWizardIntent.SelectConnectionType(ConnectionType.ANDROID_TEMPLATE)) }
    )
}

@Composable
private fun StepConfiguration(
    state: ConnectionWizardState,
    onIntent: (ConnectionWizardIntent) -> Unit
) {
    Text(
        "配置连接参数 / Configure Connection",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = state.serverName,
        onValueChange = { onIntent(ConnectionWizardIntent.UpdateServerName(it)) },
        label = { Text("Server Name / 服务器名称") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.serverUrl,
        onValueChange = { onIntent(ConnectionWizardIntent.UpdateServerUrl(it)) },
        label = { Text("Server URL / 服务器地址") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = state.authToken,
        onValueChange = { onIntent(ConnectionWizardIntent.UpdateAuthToken(it)) },
        label = { Text("Auth Token (optional) / 认证令牌") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        "Tools / 工具权限",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )
    listOf("device.screenshot", "device.install", "device.logcat", "device.input", "device.reboot").forEach { tool ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = state.selectedTools.contains(tool),
                onCheckedChange = { onIntent(ConnectionWizardIntent.ToggleTool(tool)) }
            )
            Text(tool, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StepVerifyAndSave(
    state: ConnectionWizardState,
    onIntent: (ConnectionWizardIntent) -> Unit
) {
    Text(
        "验证与保存 / Verify & Save",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Summary / 摘要",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Name: ${state.serverName.ifBlank { "N/A" }}")
            Text("URL: ${state.serverUrl}")
            Text("Type: ${state.connectionType?.label ?: "N/A"}")
            Text("Tools: ${state.selectedTools.size} selected")
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = { onIntent(ConnectionWizardIntent.TestConnection) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isTesting
    ) {
        if (state.isTesting) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text("Test Connection / 测试连接")
        }
    }

    state.connectionTestResult?.let { result ->
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (result.success) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.success) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (result.success) "Connection successful!" else "Connection failed!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ConnectionTypeCard(
    type: ConnectionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (type) {
                    ConnectionType.EXTERNAL -> Icons.Default.CloudQueue
                    ConnectionType.ANDROID_TEMPLATE -> Icons.Default.PhoneAndroid
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    type.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
