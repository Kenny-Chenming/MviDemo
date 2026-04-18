package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * OtterMcpScreen — Main container for Otter MCP feature
 * Simplified version using only DeviceInteractionScreen
 * 使用 DeviceInteractionScreen 的简化版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtterMcpScreen() {
    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Device")

    // Create ViewModel
    val deviceVm = remember { DeviceInteractionViewModel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Otter MCP Server") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Tab Content
            when (currentTab) {
                0 -> DeviceInteractionScreen(viewModel = deviceVm)
            }
        }
    }
}
