package com.mvi.kenny.feature.nav310rc01

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Nav310Rc01Screen — stub for Jetpack Navigation 3.1.0-rc01 toolkit
 * 占位符实现
 */
@Composable
fun Nav310Rc01Screen(onNavigateTo: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nav 3.1.0-rc01 新版API变更", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Jetpack Navigation 3.1.0-rc01 新版 API 变更检测与迁移工具包",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
