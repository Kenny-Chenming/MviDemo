package com.mvi.kenny.feature.aapm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.aapm.screens.AAPMStatusScreen
import com.mvi.kenny.feature.aapm.screens.AuditLogScreen
import com.mvi.kenny.feature.aapm.screens.ComplianceReportScreen
import com.mvi.kenny.feature.aapm.screens.MigrationGuideScreen
import com.mvi.kenny.feature.aapm.screens.ServiceImpactScreen
import kotlinx.coroutines.flow.collectLatest

// =============================================================
// AAPMScreen — AAPM 工具面板主屏幕
// =============================================================
/**
 * AAPM Dashboard Main Screen / AAPM 工具面板主屏幕
 *
 * Hosts the tabbed interface with 5 tool tabs:
 * - STATUS: Overview of AAPM status and risk summary
 * - IMPACT: Impact analysis of each Accessibility Service
 * - GUIDE: Migration guide for affected services
 * - AUDIT: Audit log of Accessibility API access
 * - REPORT: Compliance report generation and export
 *
 * @param viewModel AAPM ViewModel instance / AAPM ViewModel 实例
 * @param onNavigateToMigration Callback for migration navigation / 迁移导航回调
 */
@Composable
fun AAPMScreen(
    viewModel: AAPMViewModel = viewModel(),
    onNavigateToMigration: ((String) -> Unit)? = null
) {
    // =============================================================
    // State observation / 状态观察
    // =============================================================
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // =============================================================
    // Effect collection / 副作用收集
    // =============================================================
    /**
     * Collect one-time effects and handle them / 收集一次性副作用并处理
     *
     * Effects like navigation, toasts, snackbars are consumed only once.
     */
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AAPMDashboardEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is AAPMDashboardEffect.NavigateToMigration -> {
                    onNavigateToMigration?.invoke(effect.serviceName)
                }
                is AAPMDashboardEffect.ShowExportSuccess -> {
                    snackbarHostState.showSnackbar("Exported: ${effect.path}")
                }
                else -> {}
            }
        }
    }

    // =============================================================
    // UI Layout / 界面布局
    // =============================================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab content / Tab 内容区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.activeTab) {
                    AAPMTab.STATUS -> AAPMStatusScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AAPMTab.IMPACT -> ServiceImpactScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AAPMTab.GUIDE -> MigrationGuideScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AAPMTab.AUDIT -> AuditLogScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                    AAPMTab.REPORT -> ComplianceReportScreen(
                        state = state,
                        onIntent = viewModel::processIntent
                    )
                }
            }
        }
    }
}
