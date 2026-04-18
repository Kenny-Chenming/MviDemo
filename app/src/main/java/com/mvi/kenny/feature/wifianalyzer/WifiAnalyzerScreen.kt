package com.mvi.kenny.feature.wifianalyzer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiAnalyzerScreen(viewModel: WifiAnalyzerViewModel = WifiAnalyzerViewModel()) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Wi-Fi Analyzer", style = MaterialTheme.typography.titleLarge)
        }
    }
}
