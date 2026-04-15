package com.mvi.kenny.feature.ottermcp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OtterMcpScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: OtterMcpViewModel = OtterMcpViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.currentScreen) {
        onUpdateTopBar(TopBarConfig(title = "Otter MCP Server", actions = emptyList()))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OtterMcpEffect.ShowToast -> { }
                is OtterMcpEffect.ShowError -> { }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = SubScreen.entries.indexOf(state.currentScreen),
            modifier = Modifier.padding(0.dp),
            edgePadding = 16.dp
        ) {
            SubScreen.entries.forEach { screen ->
                Tab(
                    selected = state.currentScreen == screen,
                    onClick = { viewModel.sendIntent(OtterMcpIntent.NavigateTo(screen)) },
                    text = {
                        Text(
                            when (screen) {
                                SubScreen.DASHBOARD -> "仪表盘"
                                SubScreen.CONNECTION_WIZARD -> "连接向导"
                                SubScreen.TEMPLATE_GALLERY -> "模板库"
                                SubScreen.SECURITY -> "安全策略"
                                SubScreen.AUDIT_LOG -> "审计日志"
                                SubScreen.DEVICE_SERVER -> "设备交互"
                            }
                        )
                    }
                )
            }
        }

        when (state.currentScreen) {
            SubScreen.DASHBOARD -> DashboardScreen(
                state.dashboardState,
                { viewModel.sendIntent(OtterMcpIntent.Dashboard(it)) },
                { viewModel.sendIntent(OtterMcpIntent.NavigateTo(it)) }
            )
            SubScreen.CONNECTION_WIZARD -> ConnectionWizardScreen(
                state.connectionWizardState,
                { viewModel.sendIntent(OtterMcpIntent.ConnectionWizard(it)) }
            )
            SubScreen.TEMPLATE_GALLERY -> TemplateGalleryScreen(
                state.templateGalleryState,
                { viewModel.sendIntent(OtterMcpIntent.TemplateGallery(it)) }
            )
            SubScreen.SECURITY -> SecurityScreen(
                state.securityState,
                { viewModel.sendIntent(OtterMcpIntent.Security(it)) }
            )
            SubScreen.AUDIT_LOG -> AuditLogScreen(
                state.auditLogState,
                { viewModel.sendIntent(OtterMcpIntent.AuditLog(it)) }
            )
            SubScreen.DEVICE_SERVER -> DeviceServerScreen(
                state.deviceInteractionState,
                { viewModel.sendIntent(OtterMcpIntent.DeviceInteraction(it)) }
            )
        }
    }
}
