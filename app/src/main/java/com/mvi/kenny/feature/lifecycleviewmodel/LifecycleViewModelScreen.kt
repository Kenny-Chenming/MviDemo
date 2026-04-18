package com.mvi.kenny.feature.lifecycleviewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * LifecycleViewModelScreen — stub for Lifecycle ViewModel Compose 1.0.0 toolkit
 * 占位符实现
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleViewModelScreen(
    onUpdateTopBar: (com.mvi.kenny.base.TopBarConfig) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Lifecycle ViewModel Compose 1.0.0", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Lifecycle ViewModel Compose 1.0.0 Kotlin DSL 新 API 开发工具包",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
