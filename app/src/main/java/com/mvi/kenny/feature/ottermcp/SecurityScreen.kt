package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SecurityScreen — stub for OtterMCP security settings
 * 占位符实现
 */
@Composable
fun SecurityScreen(
    state: SecurityState,
    onIntent: (SecurityIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Security Settings / 安全设置",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Security configuration for Otter MCP Server / Otter MCP 服务器安全配置",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Stub types to satisfy compilation
// 存根类型以满足编译
typealias SecurityState = Unit
typealias SecurityIntent = Unit
typealias PermissionMatrix = Unit
