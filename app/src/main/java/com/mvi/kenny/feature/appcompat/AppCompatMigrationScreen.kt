package com.mvi.kenny.feature.appcompat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Stub types
typealias AppCompatState = kotlin.Unit
typealias AppCompatIntent = kotlin.Unit

/**
 * AppCompatMigrationScreen — stub for AndroidX AppCompat 1.8.0 minSdk migration
 * 占位符实现
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCompatMigrationScreen(
    state: AppCompatState = kotlin.Unit,
    onIntent: (AppCompatIntent) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AppCompat 1.8.0 minSdk 迁移工具", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "minSdk 23 强制迁移与 JSpecify 合规工具包",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
