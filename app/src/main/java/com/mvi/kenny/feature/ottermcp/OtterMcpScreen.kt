package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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

// Main Container for OtterMcp feature
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtterMcpScreen() {
    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Connection Wizard", "Templates", "Audit Log", "Device")

    // Create ViewModels
    val dashboardVm = remember { DashboardViewModel() }
    val wizardVm = remember { ConnectionWizardViewModel() }
    val templateVm = remember { TemplateGalleryViewModel() }
    val auditVm = remember { AuditLogViewModel() }
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
            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = currentTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (currentTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = dashboardVm,
                    onNavigateToWizard = { currentTab = 1 },
                    onNavigateToAudit = { currentTab = 3 },
                    onNavigateToTemplate = { currentTab = 2 },
                    onNavigateToDevice = { currentTab = 4 }
                )
                1 -> ConnectionWizardScreen(viewModel = wizardVm)
                2 -> TemplateGalleryScreen(viewModel = templateVm)
                3 -> AuditLogScreen(viewModel = auditVm)
                4 -> DeviceInteractionScreen(viewModel = deviceVm)
            }
        }
    }
}
