package com.mvi.kenny.feature.android17resizability

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * Android17ResizabilityScreen — Android 17 大屏自适应迁移工具包主屏幕
 * ============================================================
 * 5-Tab MVI screen for Android 17 resizability migration toolkit.
 *
 * Tabs:
 * 1. Detection / 检测扫描
 * 2. Manifest Migration / Manifest迁移
 * 3. CI Compliance / CI合规
 * 4. Behavior Changes / 行为变更
 * 5. Decision Tree / 决策树
 *
 * @param onUpdateTopBar Update parent TopBar callback / 更新父 TopBar 回调
 * @param viewModel ViewModel instance / ViewModel 实例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Android17ResizabilityScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: Android17ResizabilityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Update TopBar when tab changes / Tab 变化时更新 TopBar
    LaunchedEffect(state.selectedTab) {
        onUpdateTopBar(TopBarConfig(
            title = "Android 17 Resizability · ${state.selectedTab.titleZh}",
            actions = emptyList()
        ))
    }

    // Listen for effects / 监听副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Android17ResizabilityEffect.ShowSnackbar -> { /* handled via state */ }
                is Android17ResizabilityEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.code))
                }
                is Android17ResizabilityEffect.ScanComplete -> { }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            state.snackbarMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.sendIntent(Android17ResizabilityIntent.DismissSnackbar) }) {
                            Text("Dismiss", color = ResizabilitySuccess)
                        }
                    }
                ) { Text(message) }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ResizabilitySurface)
        ) {
            // Tab Row / Tab 栏
            ResizabilityTabRow(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(Android17ResizabilityIntent.SelectTab(it)) }
            )

            // Tab Content / Tab 内容
            when (state.selectedTab) {
                ResizabilityTab.DETECTION -> DetectionTabContent(state, viewModel::sendIntent)
                ResizabilityTab.MANIFEST -> ManifestMigrationTabContent(state, viewModel::sendIntent)
                ResizabilityTab.CI -> CiComplianceTabContent(state, viewModel::sendIntent)
                ResizabilityTab.BEHAVIOR -> BehaviorChangesTabContent(state, viewModel::sendIntent)
                ResizabilityTab.DECISION -> DecisionTreeTabContent(state, viewModel::sendIntent)
            }
        }
    }
}

// =============================================================
// Tab Row
// =============================================================
@Composable
private fun ResizabilityTabRow(
    selectedTab: ResizabilityTab,
    onTabSelected: (ResizabilityTab) -> Unit
) {
    TabRow(
        selectedTabIndex = ResizabilityTab.entries.indexOf(selectedTab),
        containerColor = ResizabilityCardBg,
        contentColor = ResizabilityAccent
    ) {
        ResizabilityTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.titleZh,
                        fontSize = 11.sp,
                        color = if (selectedTab == tab) ResizabilityAccent else ResizabilityAccent.copy(alpha = 0.6f)
                    )
                }
            )
        }
    }
}

// =============================================================
// Tab 1: Detection Scanner
// =============================================================
@Composable
private fun DetectionTabContent(
    state: Android17ResizabilityState,
    onIntent: (Android17ResizabilityIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Scanner Input Card / 扫描器输入卡片
        item { ScannerInputCard(state, onIntent) }

        // Scan Results Summary / 扫描结果摘要
        if (state.scanResults.isNotEmpty() || state.cameraResults.isNotEmpty()) {
            item { ScanResultsSummaryCard(state) }
        }

        // Opt-out Results / Opt-out 扫描结果
        if (state.scanResults.isNotEmpty()) {
            item { SectionHeader("Manifest Opt-out Issues", "Manifest Opt-out 问题") }
            itemsIndexed(state.scanResults) { _, result ->
                OptOutResultCard(result) { onIntent(Android17ResizabilityIntent.CopyCode(result.fixSuggestion)) }
            }
        }

        // Camera Results / Camera 问题结果
        if (state.cameraResults.isNotEmpty()) {
            item { SectionHeader("Camera Aspect Ratio Issues", "Camera Aspect Ratio 问题") }
            itemsIndexed(state.cameraResults) { _, result ->
                CameraResultCard(result) { onIntent(Android17ResizabilityIntent.CopyCode(result.fixSuggestion)) }
            }
        }
    }
}

@Composable
private fun ScannerInputCard(state: Android17ResizabilityState, onIntent: (Android17ResizabilityIntent) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🔍 Manifest Opt-out Scanner", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("Scan AndroidManifest.xml for deprecated orientation/resize attributes on API 37+ large screens.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))

            OutlinedTextField(
                value = state.scannerInput,
                onValueChange = { onIntent(Android17ResizabilityIntent.UpdateScannerInput(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Project path / 项目路径") },
                placeholder = { Text("e.g. /Users/kenny/Projects/MyApp") },
                enabled = !state.isScanning,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ResizabilityAccent, unfocusedBorderColor = ResizabilityAccent.copy(alpha = 0.3f),
                    focusedLabelColor = ResizabilityAccent, unfocusedLabelColor = ResizabilityAccent.copy(alpha = 0.7f),
                    cursorColor = ResizabilityAccent, focusedTextColor = ResizabilityAccent, unfocusedTextColor = ResizabilityAccent
                ),
                singleLine = true
            )

            if (state.isScanning) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(progress = { state.scanProgress }, modifier = Modifier.fillMaxWidth(), color = ResizabilityAccent, trackColor = ResizabilityCardBg)
                    Text("Scanning... ${(state.scanProgress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isScanning) {
                    OutlinedButton(onClick = { onIntent(Android17ResizabilityIntent.CancelScan) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = ResizabilityDanger)) {
                        Icon(Icons.Default.Stop, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                } else {
                    Button(onClick = { onIntent(Android17ResizabilityIntent.StartScan) }, colors = ButtonDefaults.buttonColors(containerColor = ResizabilityAccent)) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Start Scan")
                    }
                    OutlinedButton(onClick = { onIntent(Android17ResizabilityIntent.StartScan) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = ResizabilityAccent)) {
                        Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Demo")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanResultsSummaryCard(state: Android17ResizabilityState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            SummaryItem("Opt-out Issues", state.scanResults.size, ResizabilityDanger)
            SummaryItem("Camera Issues", state.cameraResults.size, ResizabilityWarning)
        }
    }
}

@Composable
private fun SummaryItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
    }
}

@Composable
private fun OptOutResultCard(result: OptOutResult, onCopyCode: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(result.attribute, style = MaterialTheme.typography.titleSmall, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
                    Text(result.attributeZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
                PriorityBadge(result.priority)
            }
            Text("${result.element} · ${result.filePath}", style = MaterialTheme.typography.bodySmall, color = ResizabilitySuccess, fontFamily = FontFamily.Monospace)
            Text(result.description, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.8f))
            Text(result.descriptionZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.6f))
            CodeBlock(result.fixSuggestion, onCopyCode)
        }
    }
}

@Composable
private fun CameraResultCard(result: CameraIssueResult, onCopyCode: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("[${result.cameraApi}] ${result.methodName}", style = MaterialTheme.typography.titleSmall, color = ResizabilityWarning, fontWeight = FontWeight.Bold)
                    Text(result.filePath, style = MaterialTheme.typography.bodySmall, color = ResizabilitySuccess, fontFamily = FontFamily.Monospace)
                }
                PriorityBadge(result.priority)
            }
            Text(result.description, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.8f))
            Text(result.descriptionZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.6f))
            CodeBlock(result.fixSuggestion, onCopyCode)
        }
    }
}

// =============================================================
// Tab 2: Manifest Migration
// =============================================================
@Composable
private fun ManifestMigrationTabContent(state: Android17ResizabilityState, onIntent: (Android17ResizabilityIntent) -> Unit) {
    val guides = remember { getMigrationGuides() }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("📋 Manifest Migration Guides", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("Click to expand each guide. Before/After code snippets with explanations.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
        }
        itemsIndexed(guides) { index, guide ->
            MigrationGuideCard(guide, state.migrationGuidesExpanded == index,
                { onIntent(Android17ResizabilityIntent.ExpandMigrationGuide(index)) },
                { onIntent(Android17ResizabilityIntent.CopyCode(guide.afterCode)) }
            )
        }
    }
}

@Composable
private fun MigrationGuideCard(guide: MigrationGuideItem, isExpanded: Boolean, onToggle: () -> Unit, onCopyCode: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
        shape = RoundedCornerShape(12.dp),
        onClick = onToggle
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guide.title, style = MaterialTheme.typography.titleSmall, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
                    Text(guide.titleZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(guide.priority)
                    Text(if (isExpanded) "▲" else "▼", color = ResizabilityAccent, modifier = Modifier.clickable { onToggle() })
                }
            }
            if (isExpanded) {
                HorizontalDivider(color = ResizabilityAccent.copy(alpha = 0.2f))
                Text(guide.explanation, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.8f))
                Text(guide.explanationZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.6f))
                Text("Before:", style = MaterialTheme.typography.labelSmall, color = ResizabilityDanger, fontWeight = FontWeight.Bold)
                CodeBlock(guide.beforeCode, null)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("After:", style = MaterialTheme.typography.labelSmall, color = ResizabilitySuccess, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onCopyCode, modifier = Modifier.size(24.dp)) { Icon(imageVector = androidx.compose.material.icons.Icons.Filled.ContentCopy, contentDescription = "Copy", tint = ResizabilitySuccess) }
                }
                CodeBlock(guide.afterCode, onCopyCode)
            }
        }
    }
}

private fun getMigrationGuides(): List<MigrationGuideItem> = listOf(
    MigrationGuideItem(
        id = "screen_orientation", title = "android:screenOrientation → Dynamic Setting", titleZh = "android:screenOrientation → 动态设置",
        beforeCode = """<!-- Locked to portrait — IGNORED on API 37+ large screens -->
<activity android:name=".MainActivity"
    android:screenOrientation="portrait" />""",
        afterCode = """<!-- Remove fixed orientation from manifest -->
<activity android:name=".MainActivity"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize" />

// Dynamic orientation based on window size
val orientation = if (isLargeScreen(resources)) {
    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
} else {
    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}
requestedOrientation = orientation""",
        explanation = "Fixed screenOrientation is completely ignored on API 37+ large screens (sw>600dp).",
        explanationZh = "固定的 screenOrientation 在 API 37+ 大屏设备（sw>600dp）上被完全忽略。",
        priority = Priority.P0
    ),
    MigrationGuideItem(
        id = "resizeable_activity", title = "android:resizeableActivity=\"true\"", titleZh = "android:resizeableActivity=\"true\"",
        beforeCode = """<!-- Non-resizable — App forced into Desktop Windowing -->
<activity android:name=".MainActivity"
    android:resizeableActivity="false" />""",
        afterCode = """<!-- Resizable with proper configChanges -->
<activity android:name=".MainActivity"
    android:resizeableActivity="true"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />
    <!-- Multi-window and Desktop Windowing supported -->""",
        explanation = "resizeableActivity=false is ignored. All apps on API 37+ large screens support multi-window by default.",
        explanationZh = "resizeableActivity=false 会被忽略。所有 API 37+ 大屏上的 App 默认支持多窗口。",
        priority = Priority.P0
    ),
    MigrationGuideItem(
        id = "aspect_ratio", title = "Remove min/maxAspectRatio", titleZh = "移除 min/maxAspectRatio",
        beforeCode = """<!-- Fixed aspect ratio — IGNORED on API 37+ large screens -->
<activity android:name=".MainActivity"
    android:minAspectRatio="1.33"
    android:maxAspectRatio="1.86" />""",
        afterCode = """<!-- Remove aspect ratio constraints -->
<activity android:name=".MainActivity" />

// Use WindowSizeClass for adaptive layouts
val windowSizeClass = calculateWindowSizeClass(this)
when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.COMPACT -> PhoneLayout()
    WindowWidthSizeClass.MEDIUM -> TabletLayout()
    WindowWidthSizeClass.EXPANDED -> DesktopLayout()
}""",
        explanation = "minAspectRatio and maxAspectRatio are ignored on API 37+ large screens.",
        explanationZh = "minAspectRatio 和 maxAspectRatio 在 API 37+ 大屏上被忽略。",
        priority = Priority.P1
    ),
    MigrationGuideItem(
        id = "foldable_config_changes", title = "configChanges for Foldable Devices", titleZh = "折叠屏的 configChanges",
        beforeCode = """<!-- Incomplete configChanges — Activity destroyed on fold/unfold -->
<activity android:name=".MainActivity"
    android:configChanges="orientation" />""",
        afterCode = """<!-- Complete configChanges for foldables -->
<activity android:name=".MainActivity"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" />

// In Activity:
override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val metrics = windowManager.currentWindowMetrics
    val width = metrics.bounds.width() / resources.displayMetrics.density
    updateLayoutForWidth(width)
}""",
        explanation = "Foldable devices trigger config changes on fold/unfold. Activity will be recreated without proper configChanges.",
        explanationZh = "折叠屏在展开/折叠时会触发配置变更。没有正确的 configChanges 会导致 Activity 重建。",
        priority = Priority.P1
    )
)

// =============================================================
// Tab 3: CI Compliance
// =============================================================
@Composable
private fun CiComplianceTabContent(state: Android17ResizabilityState, onIntent: (Android17ResizabilityIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CiPluginCard(state.ciConfig) { onIntent(Android17ResizabilityIntent.UpdateCiConfig(it)) }
        GradleConfigTemplate { onIntent(Android17ResizabilityIntent.CopyCode(getGradlePluginTemplate())) }
        DecisionTreeReferenceCard()
    }
}

@Composable
private fun CiPluginCard(config: CiConfig, onConfigChange: (CiConfig) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🛡️ CI Compliance Gradle Plugin", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("Configure the Android 17 Resizability CI compliance plugin.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            HorizontalDivider(color = ResizabilityAccent.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Enable Gradle Plugin", style = MaterialTheme.typography.bodyMedium, color = ResizabilityAccent)
                    Text("Activate resizability compliance checking", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
                Switch(checked = config.gradlePluginEnabled, onCheckedChange = { onConfigChange(config.copy(gradlePluginEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ResizabilitySuccess, checkedTrackColor = ResizabilitySuccess.copy(alpha = 0.3f),
                        uncheckedThumbColor = ResizabilityAccent.copy(alpha = 0.5f), uncheckedTrackColor = ResizabilityCardBg))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Block CI on Violation", style = MaterialTheme.typography.bodyMedium, color = ResizabilityAccent)
                    Text("Fail build when resizability issues detected", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
                Switch(checked = config.blockOnViolation, onCheckedChange = { onConfigChange(config.copy(blockOnViolation = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ResizabilityDanger, checkedTrackColor = ResizabilityDanger.copy(alpha = 0.3f),
                        uncheckedThumbColor = ResizabilityAccent.copy(alpha = 0.5f), uncheckedTrackColor = ResizabilityCardBg))
            }
        }
    }
}

@Composable
private fun GradleConfigTemplate(onCopyCode: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📦 Gradle Plugin Configuration", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
                IconButton(onClick = onCopyCode) { Icon(Icons.Default.ContentCopy, "Copy", tint = ResizabilitySuccess) }
            }
            CodeBlock(getGradlePluginTemplate(), onCopyCode)
        }
    }
}

private fun getGradlePluginTemplate(): String = """// settings.gradle.kts
pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}

// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.example.resizability-compliance") version "1.0.0" apply false
}

// app/build.gradle.kts
plugins { id("com.example.resizability-compliance") }

androidResizability {
    enabled.set(true)
    blockOnViolation.set(true)
    reportFormat.set("json")
    targetApiLevel.set(37)
}"""

@Composable
private fun DecisionTreeReferenceCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🌳 Quick Reference / 快速参考", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            listOf(
                "screenOrientation=\"portrait|landscape\"" to ("🚨 P0 — Will be ignored" to ResizabilityDanger),
                "resizeableActivity=\"false\"" to ("🚨 P0 — Will be ignored" to ResizabilityDanger),
                "minAspectRatio / maxAspectRatio" to ("⚠️ P1 — May cause layout issues" to ResizabilityWarning),
                "Fixed Camera aspect ratio" to ("⚠️ P1 — Camera preview may break" to ResizabilityWarning),
                "No opt-out attributes" to ("✅ Compliant" to ResizabilitySuccess)
            ).forEach { (item, statusAndColor) ->
                val (status, color) = statusAndColor
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.8f), fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text(status, style = MaterialTheme.typography.bodySmall, color = color)
                }
            }
        }
    }
}

// =============================================================
// Tab 4: Behavior Changes
// =============================================================
@Composable
private fun BehaviorChangesTabContent(state: Android17ResizabilityState, onIntent: (Android17ResizabilityIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DesktopWindowingCard()
        CameraAspectRatioFixCard { onIntent(Android17ResizabilityIntent.CopyCode(getCameraFixCode())) }
        ActivityLifecycleCard()
    }
}

@Composable
private fun DesktopWindowingCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🖥️ Desktop Windowing Constraints", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("Window size constraints in Desktop Windowing mode on API 37+ large screens.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            HorizontalDivider(color = ResizabilityAccent.copy(alpha = 0.2f))
            listOf(
                "Mode" to ("Min Width" to "Min Height"),
                "Phone (compact)" to ("220dp" to "136dp"),
                "Tablet (medium)" to ("240dp" to "240dp"),
                "Desktop (expanded)" to ("320dp" to "240dp"),
                "Freeform window" to ("100dp" to "100dp")
            ).forEach { (mode, constraints) ->
                val (minW, minH) = if (constraints is Pair<*, *>) constraints as Pair<String, String> else ("—" to "—")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(mode.toString(), style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text(minW, style = MaterialTheme.typography.bodySmall, color = ResizabilitySuccess, fontFamily = FontFamily.Monospace)
                    Text(minH, style = MaterialTheme.typography.bodySmall, color = ResizabilitySuccess, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun CameraAspectRatioFixCard(onCopyCode: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("📷 Camera Aspect Ratio Fix", style = MaterialTheme.typography.titleMedium, color = ResizabilityWarning, fontWeight = FontWeight.Bold)
                IconButton(onClick = onCopyCode) { Icon(Icons.Default.ContentCopy, "Copy", tint = ResizabilitySuccess, modifier = Modifier.size(18.dp)) }
            }
            Text("Camera2 API / CameraX — Forced resize may break fixed aspect ratio assumptions.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            Text("Camera2 Fix:", style = MaterialTheme.typography.labelSmall, color = ResizabilitySuccess, fontWeight = FontWeight.Bold)
            CodeBlock(getCameraFixCode(), onCopyCode)
        }
    }
}

private fun getCameraFixCode(): String = """// Camera2 API — Dynamic sizing with AspectRatioStrategy
val aspectRatioStrategy = AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
val outputSizes = streamConfigMap.getOutputSizes(
    SurfaceTexture::class.java, aspectRatioStrategy
)

// CameraX — Adaptive preview sizing
val preview = Preview.Builder()
    .setTargetResolution(Size(INPUT.width, INPUT.height))
    .build()
preview.setSurfaceProvider(binding.preview.surfaceProvider)"""

@Composable
private fun ActivityLifecycleCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔄 Activity Lifecycle on Fold/Unfold", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("onConfigurationChanged is triggered when foldable device changes state.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            HorizontalDivider(color = ResizabilityAccent.copy(alpha = 0.2f))
            listOf(
                "✅ Declare configChanges" to "screenSize|smallestScreenSize|screenLayout|orientation",
                "⚠️ Without configChanges" to "Activity is destroyed and recreated",
                "📱 onConfigurationChanged" to "Update layout via WindowMetrics"
            ).forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent, modifier = Modifier.weight(0.4f))
                    Text(value, style = MaterialTheme.typography.bodySmall, color = ResizabilitySuccess, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.6f))
                }
            }
        }
    }
}

// =============================================================
// Tab 5: Decision Tree
// =============================================================
@Composable
private fun DecisionTreeTabContent(state: Android17ResizabilityState, onIntent: (Android17ResizabilityIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("🌳 Resizability Migration Decision Tree", style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text("Answer questions to get a personalized migration priority assessment.", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
        }

        state.decisionTreeState?.let { node ->
            item { DecisionNodeCard(node, state.decisionPath, onIntent) }
        }

        if (state.decisionTreeState?.result != null) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.decisionTreeState?.priority?.let { PriorityBadge(it) }
                        Text(state.decisionTreeState?.result ?: "", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.9f))
                        Text(state.decisionTreeState?.resultZh ?: "", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { onIntent(Android17ResizabilityIntent.ResetDecisionTree) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = ResizabilityAccent)) {
                            Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Restart / 重新开始")
                        }
                    }
                }
            }
        }

        // Breadcrumb / 面包屑导航
        if (state.decisionPath.size > 1) {
            item {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Path: ", style = MaterialTheme.typography.labelSmall, color = ResizabilityAccent.copy(alpha = 0.5f))
                    state.decisionPath.forEachIndexed { idx, nodeId ->
                        Text(
                            if (idx == state.decisionPath.lastIndex) "→ $nodeId" else "→ $nodeId",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (idx == state.decisionPath.lastIndex) ResizabilitySuccess else ResizabilityAccent.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionNodeCard(node: DecisionTreeNode, path: List<String>, onIntent: (Android17ResizabilityIntent) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = ResizabilityCardBg), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(node.question, style = MaterialTheme.typography.titleSmall, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
            Text(node.questionZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = ResizabilityAccent.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            node.options.forEach { option ->
                OutlinedButton(
                    onClick = { onIntent(Android17ResizabilityIntent.NavigateDecisionTree(option.nextNodeId)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ResizabilityAccent)
                ) {
                    Text(option.label, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(4.dp))
                    Text(option.labelZh, style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// =============================================================
// Shared Components
// =============================================================
@Composable
private fun SectionHeader(title: String, titleZh: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = ResizabilityAccent, fontWeight = FontWeight.Bold)
        Text("($titleZh)", style = MaterialTheme.typography.bodySmall, color = ResizabilityAccent.copy(alpha = 0.7f))
    }
}

@Composable
private fun PriorityBadge(priority: Priority) {
    Surface(
        color = priority.color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = priority.label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CodeBlock(code: String, onCopy: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ResizabilityCodeBg)
            .border(1.dp, ResizabilityAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = ResizabilityAccent.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                }
            }
        }
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            color = ResizabilityAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}
