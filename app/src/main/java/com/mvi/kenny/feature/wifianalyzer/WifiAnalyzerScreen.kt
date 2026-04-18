package com.mvi.kenny.feature.wifianalyzer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * WifiAnalyzerScreen — stub for Android 17 Wi-Fi Analyzer toolkit
 * 占位符实现
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAnalyzerScreen(
    viewModel: WifiAnalyzerViewModel = WifiAnalyzerViewModel()
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Wi-Fi Analyzer / Wi-Fi 分析器", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Android 17 Wi-Fi API 兼容性检测与替代方案工具包",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
