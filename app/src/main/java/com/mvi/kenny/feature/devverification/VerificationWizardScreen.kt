package com.mvi.kenny.feature.devverification

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.devverification.components.WizardStepIndicator
import kotlinx.coroutines.flow.collectLatest

/**
 * PRD-078 | Android Developer Verification Compliance Toolkit
 * VerificationWizardScreen — 5-step verification flow
 *
 * Step 1: Account Registration
 * Step 2: Developer Profile
 * Step 3: App Scan
 * Step 4: Advanced Flow Preview
 * Step 5: Compliance Report
 *
 * Design: Section 3.2 — VerificationWizardScreen
 * MVI: WizardIntent / VerificationWizardState / WizardEffect
 * Bilingual comments: CN + EN
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VerificationWizardScreen(
    viewModel: DevVerificationViewModel,
    state: VerificationWizardState = VerificationWizardState(),
    onIntent: (WizardIntent) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(
        initialPage = state.currentStep.index,
        pageCount = { WizardStep.entries.size }
    )

    // Sync pager with state / 同步分页器与状态
    LaunchedEffect(state.currentStep) {
        if (pagerState.currentPage != state.currentStep.index) {
            pagerState.animateScrollToPage(state.currentStep.index)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val targetStep = WizardStep.entries.getOrNull(pagerState.currentPage)
        if (targetStep != null && targetStep != state.currentStep) {
            onIntent(WizardIntent.GoToStep(targetStep))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.wizardEffects.collectLatest { effect ->
            when (effect) {
                is WizardEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is WizardEffect.NavigateToDashboard -> onNavigateBack()
                is WizardEffect.OpenExternalUrl -> { /* Handle via Intent */ }
                is WizardEffect.ReportExported -> snackbarHostState.showSnackbar("Report: ${effect.path}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification Wizard", fontWeight = FontWeight.Bold) },
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
        ) {
            // ============ Step Indicator ============
            // ============ 步骤指示器 ============
            WizardStepIndicator(
                steps = WizardStep.entries.map { it.title },
                currentStep = state.currentStep.index,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // ============ Step Content Pager ============
            // ============ 步骤内容分页器 ============
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                beyondViewportPageCount = 0
            ) { page ->
                val step = WizardStep.entries.getOrNull(page) ?: return@HorizontalPager
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { it / 4 } togetherWith
                                fadeOut() + slideOutHorizontally { -it / 4 }
                    },
                    label = "wizard_step_transition"
                ) { _ ->
                    when (step) {
                        WizardStep.ACCOUNT_REGISTRATION -> AccountRegistrationStep(
                            state = state.accountRegistration,
                            onUpdate = { onIntent(WizardIntent.UpdateAccountRegistration(it)) },
                            onOpenUrl = { /* Open external URL */ }
                        )
                        WizardStep.DEVELOPER_PROFILE -> DeveloperProfileStep(
                            state = state.developerProfile,
                            onUpdate = { onIntent(WizardIntent.UpdateDeveloperProfile(it)) }
                        )
                        WizardStep.APP_SCAN -> AppScanStep(
                            state = state,
                            onTriggerScan = { onIntent(WizardIntent.TriggerAppScan) }
                        )
                        WizardStep.ADVANCED_FLOW_PREVIEW -> AdvancedFlowPreviewStep(
                            state = state.advancedFlowPreview,
                            onPreview = { onIntent(WizardIntent.PreviewAdvancedFlow) }
                        )
                        WizardStep.COMPLIANCE_REPORT -> ComplianceReportStep(
                            report = state.complianceReport,
                            onExport = { onIntent(WizardIntent.ExportReport) },
                            onSubmit = { onIntent(WizardIntent.SubmitCompliance) }
                        )
                    }
                }
            }

            // ============ Navigation Buttons ============
            // ============ 导航按钮 ============
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.currentStep.index > 0) {
                    OutlinedButton(
                        onClick = { onIntent(WizardIntent.PrevStep) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Previous")
                    }
                }

                if (state.currentStep.index < WizardStep.entries.size - 1) {
                    Button(
                        onClick = { onIntent(WizardIntent.NextStep) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = { onIntent(WizardIntent.SubmitCompliance) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Submit")
                    }
                }
            }
        }
    }
}

/* ============ Step 1: Account Registration ============ */
/* ============ 步骤 1: 账户注册 ============ */

@Composable
private fun AccountRegistrationStep(
    state: AccountRegistrationState,
    onUpdate: (AccountRegistrationState) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 1: Account Registration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Set up your Google Play Developer account to enable verification.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Google Play Account / Google Play 账户
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Google Play Developer Account", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = state.hasGooglePlayAccount,
                        onCheckedChange = {
                            onUpdate(state.copy(hasGooglePlayAccount = it))
                        }
                    )
                }
                if (!state.hasGooglePlayAccount) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { onOpenUrl(state.registrationUrl) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Open Play Console ($25 one-time)")
                    }
                }
            }
        }

        // Payment Profile / 支付资料
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Google Payment Profile", fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = state.hasPaymentProfile,
                        onCheckedChange = {
                            onUpdate(state.copy(hasPaymentProfile = it))
                        }
                    )
                }
                if (!state.hasPaymentProfile) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { onOpenUrl(state.paymentProfileUrl) }) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Set up Payment Profile")
                    }
                }
            }
        }

        // Business Account / 企业账户
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Business Account", fontWeight = FontWeight.Medium)
                            Text(
                                "Requires verified website + business docs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = state.isBusinessAccount,
                        onCheckedChange = {
                            onUpdate(state.copy(isBusinessAccount = it))
                        }
                    )
                }
            }
        }
    }
}

/* ============ Step 2: Developer Profile ============ */
/* ============ 步骤 2: 开发者资料 ============ */

@Composable
private fun DeveloperProfileStep(
    state: DeveloperProfileState,
    onUpdate: (DeveloperProfileState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 2: Developer Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Complete your developer profile. This information will be visible to users.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.developerName,
            onValueChange = { onUpdate(state.copy(developerName = it)) },
            label = { Text("Developer Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )

        OutlinedTextField(
            value = state.developerEmail,
            onValueChange = { onUpdate(state.copy(developerEmail = it)) },
            label = { Text("Contact Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        OutlinedTextField(
            value = state.verifiedWebsite,
            onValueChange = { onUpdate(state.copy(verifiedWebsite = it)) },
            label = { Text("Verified Website URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
            supportingText = {
                Text("Must be a publicly accessible website")
            }
        )

        // Website verification status / 网站验证状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (state.websiteVerified)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (state.websiteVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (state.websiteVerified)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (state.websiteVerified) "Website Verified" else "Website Not Verified",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (state.websiteVerified)
                            "Your website passes verification checks"
                        else
                            "Add and verify your website in Play Console",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/* ============ Step 3: App Scan ============ */
/* ============ 步骤 3: App 扫描 ============ */

@Composable
private fun AppScanStep(
    state: VerificationWizardState,
    onTriggerScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 3: App Scan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Scan your project to detect all apps and their verification status.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Scanning apps via Gradle plugin...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (state.appScanResults.isEmpty()) {
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
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No scan results yet", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onTriggerScan) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Run Scan")
                    }
                }
            }
        } else {
            Text(
                text = "${state.appScanResults.size} apps found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            state.appScanResults.forEach { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(result.app.appName, fontWeight = FontWeight.Medium)
                            Text(
                                result.app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                result.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (result.needsVerification)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            if (result.needsVerification) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (result.needsVerification)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = onTriggerScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Re-scan")
            }
        }
    }
}

/* ============ Step 4: Advanced Flow Preview ============ */
/* ============ 步骤 4: Advanced Flow 预览 ============ */

@Composable
private fun AdvancedFlowPreviewStep(
    state: AdvancedFlowPreviewState,
    onPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 4: Advanced Flow Preview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Preview how unverified apps appear to users under the 'advanced flow' — users can still install but must acknowledge risk.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Advanced Flow Simulator Card / Advanced Flow 模拟器卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Advanced Flow Simulator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "System UI Preview (not a real screenshot)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                if (state.isSimulating) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading simulation...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Button(onClick = onPreview) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Run Preview")
                    }
                }
            }
        }

        // User Risk Assessment / 用户风险评估
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (state.user流失风险评估) {
                    "Low" -> MaterialTheme.colorScheme.primaryContainer
                    "Medium" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("User Conversion Risk Assessment", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Risk Level: ${state.user流失风险评估}")
                Text(
                    "Based on 'advanced flow' opt-in rates in similar categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Warning Note / 警告说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Note", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text(
                        "This is a static UI preview for illustration purposes. The actual 'advanced flow' is a Google System Services UI and cannot be directly rendered.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/* ============ Step 5: Compliance Report ============ */
/* ============ 步骤 5: 合规报告 ============ */

@Composable
private fun ComplianceReportStep(
    report: ComplianceReport?,
    onExport: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step 5: Compliance Report",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (report == null) {
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
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No report generated yet", fontWeight = FontWeight.Medium)
                    Text(
                        "Complete previous steps to generate your compliance report",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Report Summary / 报告摘要
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Report Summary", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${report.appsVerified.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Verified", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${report.appsRequiringVerification.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("Need Action", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${report.generatedAt}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Generated", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Region Status / 地区状态
            if (report.regionStatuses.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Regional Compliance", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        report.regionStatuses.forEach { (region, score) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(region.displayName)
                                Text(
                                    score.label,
                                    color = when (score) {
                                        ComplianceScore.PASS -> MaterialTheme.colorScheme.primary
                                        ComplianceScore.AT_RISK -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Export & Submit / 导出和提交
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Export PDF")
                }
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Submit")
                }
            }
        }
    }
}
