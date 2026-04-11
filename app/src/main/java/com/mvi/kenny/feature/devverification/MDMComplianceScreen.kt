package com.mvi.kenny.feature.devverification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.devverification.components.LoadingPulseBar
import kotlinx.coroutines.flow.collectLatest

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * MDMComplianceScreen — Enterprise MDM/EMM compliance detection
 *
 * Design: Section 3.3 — MDMComplianceScreen
 * MVI: MDMIntent / MDMComplianceState / MDMEffect
 * Bilingual comments: CN + EN
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MDMComplianceScreen(
    viewModel: DevVerificationViewModel,
    state: MDMComplianceState = MDMComplianceState(),
    onIntent: (MDMIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mdmEffects.collectLatest { effect ->
            when (effect) {
                is MDMEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is MDMEffect.OpenExternalUrl -> { /* Handle via Intent */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MDM Compliance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / 页头
            Text(
                text = "Enterprise App Distribution Check",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Prerequisite Note / 前置条件说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Prerequisite",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "MDM compliance detection requires enterprise admin permissions. Contact your IT administrator for access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Scan Progress / 扫描进度
            if (state.isScanning) {
                LoadingPulseBar(
                    message = "Scanning MDM-distributed apps...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Scan Button / 扫描按钮
            Button(
                onClick = { onIntent(MDMIntent.ScanMDM) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isScanning
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isScanning) "Scanning..." else "Scan MDM Apps")
            }

            // Violations List / 违规列表
            if (state.violations.isEmpty() && !state.isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No violations found", fontWeight = FontWeight.Medium)
                        Text(
                            "Tap 'Scan MDM Apps' to check distribution compliance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.violations, key = { it.packageName }) { violation ->
                    MDMViolationItem(
                        violation = violation,
                        onIgnore = { onIntent(MDMIntent.IgnoreViolation(violation.packageName)) },
                        onFix = { onIntent(MDMIntent.FixViolation(violation.packageName)) }
                    )
                }
            }

            // Manual Input Option / 手动输入选项
            OutlinedCard(
                onClick = { /* Manual input */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Manual Package Entry", fontWeight = FontWeight.Medium)
                            Text(
                                "Manually add package names for compliance check",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

/* ============ MDM Violation Item ============ */
/* ============ MDM 违规项 ============ */

@Composable
private fun MDMViolationItem(
    violation: MDMViolation,
    onIgnore: () -> Unit,
    onFix: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(violation.appName, fontWeight = FontWeight.Bold)
                    Text(
                        violation.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Severity Badge / 严重程度徽章
                Surface(
                    color = when (violation.severity) {
                        "High" -> MaterialTheme.colorScheme.errorContainer
                        "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        violation.severity,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (violation.severity) {
                            "High" -> MaterialTheme.colorScheme.error
                            "Medium" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Violation Type / 违规类型
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    violation.violationType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            // Suggested Fix / 修复建议
            Text(
                "Suggested Fix:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                violation.suggestedFix,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))

            // Action Buttons / 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onIgnore,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ignore")
                }
                Button(
                    onClick = onFix,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Fix")
                }
            }
        }
    }
}
