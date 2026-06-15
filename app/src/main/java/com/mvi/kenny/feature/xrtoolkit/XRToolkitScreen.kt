// ================================================================
// XRToolkitScreen — Android XR Gemini 智能眼镜开发工具包主界面
// ================================================================
// Main Composable screen for Android XR Gemini Smart Glasses Toolkit.
//
// PRD-267: Android XR Gemini 智能眼镜应用开发工具包
// Design Reference: memory/agency/designs/PRD-267-Android-XR-Gemini智能眼镜开发工具包.md
//
// Architecture: MVI (Model-View-Intent)
//   - XRToolkitState: Immutable UI state
//   - XRToolkitIntent: User actions
//   - XRToolkitEffect: One-time side effects (toast, clipboard)
// ================================================================

package com.mvi.kenny.feature.xrtoolkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.xrtoolkit.XRToolkitColors as Colors
import com.mvi.kenny.feature.xrtoolkit.XRToolkitPage as Page
import com.mvi.kenny.feature.xrtoolkit.XRSimulationType as SimType

// ================================================================
// XRToolkitScreen — Main Entry Point
// ================================================================

/**
 * XRToolkitScreen — XR 工具包主界面
 * Entry point Composable for the entire XR toolkit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XRToolkitScreen(
    viewModel: XRToolkitViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Collect effects — 监听一次性副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is XRToolkitEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is XRToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("XR Toolkit Code", effect.code)
                    clipboard.setPrimaryClip(clip)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = Colors.Surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Colors.Surface)
        ) {
            // Top app bar
            TopAppBar(
                title = {
                    Text(
                        text = if (state.currentPage == Page.INDEX) "🥽 Android XR 工具箱" else "${state.currentPage.emoji} ${state.currentPage.title}",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (state.currentPage != Page.INDEX) {
                        IconButton(onClick = { viewModel.sendIntent(XRToolkitIntent.NavigateBack) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Colors.SurfaceVariant,
                    titleContentColor = Color.White
                )
            )

            // Page content
            Box(modifier = Modifier.fillMaxSize()) {
                when (state.currentPage) {
                    Page.INDEX -> IndexPage(state = state, onIntent = viewModel::sendIntent)
                    Page.GETTING_STARTED -> GettingStartedPage(state = state, onIntent = viewModel::sendIntent)
                    Page.GEMINI -> GeminiIntegrationPage(state = state, onIntent = viewModel::sendIntent)
                    Page.UI_DESIGN -> UIDesignPage(state = state, onIntent = viewModel::sendIntent)
                    Page.LIVE_TRANSLATE -> LiveTranslatePage(state = state, onIntent = viewModel::sendIntent)
                    Page.ARCORE_VS_XR -> ARCoreVsXRPage(state = state, onIntent = viewModel::sendIntent)
                    Page.VIDEO_CALL -> VideoCallPage(state = state, onIntent = viewModel::sendIntent)
                    Page.TEST_DEBUG -> TestDebugPage(state = state, onIntent = viewModel::sendIntent)
                    Page.PRIVACY -> PrivacyPage(state = state, onIntent = viewModel::sendIntent)
                    Page.CROSS_DEVICE -> CrossDevicePage(state = state, onIntent = viewModel::sendIntent)
                }
            }
        }
    }
}

// ================================================================
// Index Page
// ================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IndexPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { state.learningPath.size })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SectionHeader(title = "📚 学习路径", subtitle = "按阶段掌握 Android XR 开发")
        }

        item {
            LearningPathPager(
                stages = state.learningPath,
                completedStages = state.completedStages,
                pagerState = pagerState,
                onCompleteStage = { index -> onIntent(XRToolkitIntent.CompleteStage(index)) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "🛠️ 开发工具", subtitle = "10 个专项工具，覆盖完整开发流程")
        }

        items(XR_TOOL_CARDS.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { tool ->
                    ToolCardXR(tool = tool, modifier = Modifier.weight(1f)) {
                        onIntent(XRToolkitIntent.NavigateTo(tool.page))
                    }
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LearningPathPager(
    stages: List<LearningStage>,
    completedStages: Set<Int>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onCompleteStage: (Int) -> Unit
) {
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(280.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp
        ) { page ->
            val stage = stages[page]
            val isCompleted = completedStages.contains(page)
            LearningStageCard(stage, isCompleted) { onCompleteStage(page) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(stages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Colors.Primary else Color.Gray.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
private fun LearningStageCard(stage: LearningStage, isCompleted: Boolean, onComplete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Colors.SuccessGreen.copy(alpha = 0.15f) else Colors.SurfaceVariant
        ),
        border = BorderStroke(1.dp, if (isCompleted) Colors.SuccessGreen else Colors.Primary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${stage.icon} ${stage.title}", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                if (isCompleted) Icon(Icons.Default.CheckCircle, "已完成", tint = Colors.SuccessGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(stage.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))
            stage.items.take(3).forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = Colors.Primary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("⏱ ${stage.estimatedTime}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                if (!isCompleted) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = Colors.Primary),
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("完成阶段", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCardXR(tool: ToolCardXR, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Colors.Primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tool.icon, fontSize = 20.sp)
                PriorityBadge(priority = tool.priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(tool.name, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), maxLines = 2)
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val (color, bgColor) = when (priority) {
        "P0" -> Colors.ErrorRed to Colors.ErrorRed.copy(alpha = 0.15f)
        "P1" -> Colors.WarningOrange to Colors.WarningOrange.copy(alpha = 0.15f)
        else -> Colors.SuccessGreen to Colors.SuccessGreen.copy(alpha = 0.15f)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(priority, style = MaterialTheme.typography.labelSmall, color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

// ================================================================
// Getting Started Page
// ================================================================

@Composable
private fun GettingStartedPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val gs = state.gettingStartedState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, "SDK", tint = Colors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Android XR SDK 安装", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!gs.isSdkInstalled) {
                        LinearProgressIndicator(
                            progress = { gs.sdkDownloadProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Colors.Primary,
                            trackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (gs.sdkDownloadProgress > 0) "下载中... ${(gs.sdkDownloadProgress * 100).toInt()}%" else "点击按钮开始下载",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onIntent(XRToolkitIntent.DownloadSDK("https://developer.android.com/xr")) },
                            colors = ButtonDefaults.buttonColors(containerColor = Colors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("开始下载 Android XR SDK")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Colors.SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SDK 已安装 ✓", color = Colors.SuccessGreen)
                        }
                    }
                }
            }
        }

        item { SectionTitle("模拟器配置步骤") }

        itemsIndexed(gs.emulatorConfigSteps) { index, step ->
            ConfigStepCard(step, index + 1) { onIntent(XRToolkitIntent.CompleteConfigStep(index)) }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, "Build", tint = Colors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("构建 HelloXR 示例", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("验证环境配置是否正确，运行第一个 XR App", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!gs.isHelloXRBuilt) {
                        Button(
                            onClick = { onIntent(XRToolkitIntent.BuildHelloXR("~/xr-projects/helloxr")) },
                            colors = ButtonDefaults.buttonColors(containerColor = Colors.Primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("构建 HelloXR")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Colors.SuccessGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("HelloXR 构建成功 ✓", color = Colors.SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigStepCard(step: ConfigStep, stepNumber: Int, onComplete: () -> Unit) {
    val borderColor = when (step.status) {
        StepStatus.DONE -> Colors.SuccessGreen
        StepStatus.IN_PROGRESS -> Colors.Primary
        StepStatus.ERROR -> Colors.ErrorRed
        StepStatus.PENDING -> Color.Gray.copy(alpha = 0.3f)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(
                        when (step.status) {
                            StepStatus.DONE -> Colors.SuccessGreen
                            StepStatus.IN_PROGRESS -> Colors.Primary
                            else -> Color.Gray.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.status == StepStatus.DONE) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(stepNumber.toString(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(step.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(step.description, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                step.command?.let { cmd ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp)) {
                        Text(cmd, color = Colors.Primary, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(4.dp))
                    }
                }
            }
            if (step.status == StepStatus.PENDING || step.status == StepStatus.IN_PROGRESS) {
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.ChevronRight, "完成", tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// ================================================================
// Gemini Integration Page
// ================================================================

@Composable
private fun GeminiIntegrationPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val gemini = state.geminiState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini × Android XR 集成", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Gemini 是眼镜的主要 AI 接口，用户通过对话与 Gemini 交互，眼镜负责视觉输入和语音输出。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onIntent(XRToolkitIntent.SetVoiceInput(!gemini.useVoiceInput)) }.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, "Voice", tint = if (gemini.useVoiceInput) Colors.Primary else Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("语音输入模式", color = Color.White)
                }
                Switch(checked = gemini.useVoiceInput, onCheckedChange = { onIntent(XRToolkitIntent.SetVoiceInput(it)) }, colors = SwitchDefaults.colors(checkedTrackColor = Colors.Primary))
            }
        }

        item {
            OutlinedTextField(
                value = gemini.inputPrompt,
                onValueChange = { onIntent(XRToolkitIntent.UpdatePrompt(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("输入 Prompt", color = Color.White.copy(alpha = 0.6f)) },
                placeholder = { Text("例如：如何在 XR 眼镜中实现实时翻译？", color = Color.White.copy(alpha = 0.3f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Colors.Primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    cursorColor = Colors.Primary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                minLines = 3,
                maxLines = 5
            )
        }

        item {
            Button(
                onClick = { onIntent(XRToolkitIntent.SendGeminiPrompt(gemini.inputPrompt)) },
                enabled = gemini.inputPrompt.isNotBlank() && !gemini.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (gemini.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini 思考中...")
                } else {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("发送 Prompt")
                }
            }
        }

        if (gemini.geminiResponse.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Colors.Primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Colors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini 响应", color = Colors.Primary, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(gemini.geminiResponse, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            CodeSampleCard("Gemini XR 集成示例代码", GEMINI_XR_INTEGRATION_CODE) {
                onIntent(XRToolkitIntent.CopyCodeSample(GEMINI_XR_INTEGRATION_CODE))
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.WarningOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Colors.WarningOrange.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Colors.WarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini XR 场景限制", color = Colors.WarningOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("音频优先：眼镜场景以语音输入为主", "视觉上下文有限：摄像头帧率高但分辨率受限", "响应延迟要求 < 2s（XR 体验要求）", "需要 Gemini API Key（通过 Google Cloud 配置）").forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", color = Colors.WarningOrange)
                            Text(item, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// UI Design Page
// ================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UIDesignPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val uiState = state.uiDesignState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScrollableTabRow(
                selectedTabIndex = UISpecTab.entries.indexOf(uiState.selectedSpecTab),
                containerColor = Colors.SurfaceVariant,
                contentColor = Colors.Primary,
                edgePadding = 0.dp
            ) {
                UISpecTab.entries.forEach { tab ->
                    Tab(
                        selected = uiState.selectedSpecTab == tab,
                        onClick = { onIntent(XRToolkitIntent.SelectUISpecTab(tab)) },
                        text = { Text("${tab.emoji} ${tab.title}", color = if (uiState.selectedSpecTab == tab) Colors.Primary else Color.White.copy(alpha = 0.5f)) }
                    )
                }
            }
        }

        when (uiState.selectedSpecTab) {
            UISpecTab.FONT_SIZE -> {
                item { FontSizeGuideCard() }
                item { GlassesSimulatorCard("字号模拟（Canvas）", isActive = uiState.glassesSimulationActive) { onIntent(XRToolkitIntent.SetGlassesSimulation(!uiState.glassesSimulationActive)) } }
            }
            UISpecTab.COLOR -> {
                item { ColorGuideCard() }
                item { GlassesSimulatorCard("颜色对比度模拟（Canvas）", isActive = uiState.glassesSimulationActive) { onIntent(XRToolkitIntent.SetGlassesSimulation(!uiState.glassesSimulationActive)) } }
            }
            UISpecTab.LAYOUT -> {
                item { LayoutSafetyGuideCard() }
                item { GlassesSimulatorCard("布局安全区模拟（Canvas）", isActive = uiState.glassesSimulationActive) { onIntent(XRToolkitIntent.SetGlassesSimulation(!uiState.glassesSimulationActive)) } }
            }
            UISpecTab.INTERACTION -> {
                item { InteractionGuideCard() }
            }
        }
    }
}

@Composable
private fun GlassesSimulatorCard(title: String, isActive: Boolean, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall)
                Switch(checked = isActive, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedTrackColor = Colors.Primary))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Canvas: glasses FOV simulation
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Glasses frame outline
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.2f),
                        topLeft = Offset(w * 0.08f, h * 0.05f),
                        size = Size(w * 0.84f, h * 0.9f),
                        cornerRadius = CornerRadius(60f, 60f),
                        style = Stroke(width = 3f)
                    )

                    // Safe zone (center 60%)
                    drawRoundRect(
                        color = Colors.SafeZoneBorder.copy(alpha = 0.35f),
                        topLeft = Offset(w * 0.2f, h * 0.15f),
                        size = Size(w * 0.6f, h * 0.7f),
                        cornerRadius = CornerRadius(20f, 20f),
                        style = Stroke(width = 2f)
                    )

                    // Edge danger zone indicators
                    listOf(w * 0.05f, w * 0.95f - 12f).forEach { xPos ->
                        drawRect(
                            color = Colors.DangerZone.copy(alpha = 0.3f),
                            topLeft = Offset(xPos, h * 0.15f),
                            size = Size(12f, h * 0.7f)
                        )
                    }

                    // Sample text drawn with native canvas
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 18.sp.toPx()
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.nativeCanvas.drawText(
                            if (isActive) "👁️ 你好，Android XR!" else "开启模拟查看效果",
                            w / 2,
                            h / 2 + 8f,
                            paint
                        )
                    }
                }

                if (!isActive) {
                    Text("点击开关启用模拟", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("边缘危险区", Colors.DangerZone)
                LegendItem("安全可见区", Colors.SafeZoneBorder)
                LegendItem("眼镜边框", Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FontSizeGuideCard() = SpecContentCard(title = "📏 字号规范", content = """
• 眼镜视野内文字最小 16sp（推荐 20sp+）
• 高对比度白色文字（对比度 > 4.5:1）
• 标题使用 24sp， 正文使用 18-20sp
• 避免使用 12sp 以下的小字
    """.trimIndent())

@Composable
private fun ColorGuideCard() = SpecContentCard(title = "🎨 颜色规范", content = """
• 背景色：深色 #121212（眼镜沉浸感）
• 文字色：白色 #FFFFFF 或浅黄 #FFF9C4
• UI 层透明度 ≤ 80%
• 推荐高对比度组合：白字/深色背景
    """.trimIndent())

@Composable
private fun LayoutSafetyGuideCard() = SpecContentCard(title = "📐 布局安全区", content = """
• 左右 15% 区域为边缘，不放重要内容
• 中心 60% 为最佳可见区（Sweet Spot）
• 文字不要贴边，保持至少 24dp 内边距
• 交互元素放在中心可见区内
    """.trimIndent())

@Composable
private fun InteractionGuideCard() = SpecContentCard(title = "👆 交互模式", content = """
• 触摸：眼镜侧边触控板
• 语音：主要输入方式（优先设计语音交互）
• 手势：点头/摇头等头部手势（部分设备）
• 视线：注视点触发（未来支持）
    """.trimIndent())

@Composable
private fun SpecContentCard(title: String, content: String) = Card(
    colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
    shape = RoundedCornerShape(12.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
    }
}

// ================================================================
// Live Translate Page
// ================================================================

@Composable
private fun LiveTranslatePage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val lt = state.liveTranslateState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, null, tint = Colors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("实时翻译 — MLKit + 眼镜 Overlay", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("翻译结果以 Overlay 方式显示在眼镜视野中，高对比度白色文字。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Translation simulation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("源语言", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                            Text(lt.sourceLanguage, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Colors.Primary)
                        Column {
                            Text("目标语言", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                            Text(lt.targetLanguage, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated translation overlay simulation
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = lt.showAnimation,
                            enter = fadeIn(animationSpec = tween(500)),
                            exit = fadeOut(animationSpec = tween(500))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = lt.translatedText,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "← 翻译结果浮现在视野中",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onIntent(XRToolkitIntent.SetTranslateAnimation(!lt.showAnimation)) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("翻译浮现动画", color = Color.White)
                        Switch(
                            checked = lt.showAnimation,
                            onCheckedChange = { onIntent(XRToolkitIntent.SetTranslateAnimation(it)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Colors.Primary)
                        )
                    }
                }
            }
        }

        item {
            CodeSampleCard("MLKit 翻译 + Overlay 示例代码", MLKIT_TRANSLATION_CODE) {
                onIntent(XRToolkitIntent.CopyCodeSample(MLKIT_TRANSLATION_CODE))
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.WarningOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Colors.WarningOrange.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Colors.WarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("隐私合规提示", color = Colors.WarningOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("摄像头数据不能离开设备处理（隐私沙盒），翻译需在本地完成。", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ================================================================
// ARCore vs XR Page
// ================================================================

@Composable
private fun ARCoreVsXRPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val scenarios = listOf(
        "indoor_navigation" to "室内导航",
        "outdoor_navigation" to "户外导航",
        "translation" to "实时翻译",
        "video_call" to "视频通话",
        "object_recognition" to "物体识别",
        "gaming" to "游戏/娱乐"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔍 ARCore vs Android XR 选型决策", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("根据使用场景选择合适的平台。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { SectionTitle("选择使用场景") }

        items(scenarios) { (id, label) ->
            val isSelected = state.arcoreVsXrState.selectedScenario == id
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onIntent(XRToolkitIntent.SelectARCoreVsXRScenario(id)) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Colors.Primary.copy(alpha = 0.15f) else Colors.SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(2.dp, Colors.Primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = if (isSelected) Colors.Primary else Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = Colors.Primary)
                }
            }
        }

        item { SectionTitle("对比分析") }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ComparisonTable(
                        headers = listOf("维度", "ARCore", "Android XR"),
                        rows = listOf(
                            listOf("设备形态", "手机为主", "眼镜为主"),
                            listOf("显示方式", "手机屏幕", "眼镜视野投射"),
                            listOf("AI 集成", "Gemini on 手机", "Gemini Nano 设备端"),
                            listOf("隐私", "摄像头数据可上传", "本地处理优先"),
                            listOf("开发框架", "ARCore SDK", "Android XR SDK"),
                            listOf("典型场景", "游戏/导航/LBS", "翻译/语音助手/无屏交互")
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonTable(headers: List<String>, rows: List<List<String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.2f)).padding(8.dp)) {
            headers.forEachIndexed { index, header ->
                Text(header, modifier = Modifier.weight(if (index == 0) 1.2f else 1f), color = Colors.Primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        // Rows
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                row.forEachIndexed { index, cell ->
                    Text(cell, modifier = Modifier.weight(if (index == 0) 1.2f else 1f), color = if (index == 0) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ================================================================
// Video Call Page
// ================================================================

@Composable
private fun VideoCallPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val vc = state.videoCallState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, null, tint = Colors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("视频通话开发指南", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("使用 WebRTC 进行实时视频通话，摄像头数据需遵循隐私沙盒规范。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ControlCard(
                    icon = Icons.Default.Videocam,
                    title = "摄像头",
                    isEnabled = vc.isCameraEnabled,
                    onToggle = { onIntent(XRToolkitIntent.SetCamera(!vc.isCameraEnabled)) },
                    modifier = Modifier.weight(1f)
                )
                ControlCard(
                    icon = Icons.Default.Mic,
                    title = "麦克风",
                    isEnabled = vc.isMicrophoneEnabled,
                    onToggle = { onIntent(XRToolkitIntent.SetMicrophone(!vc.isMicrophoneEnabled)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CodeSampleCard("WebRTC 集成伪代码", """
// WebRTC 视频通话初始化
val eglBase = EglBase.create()
val videoCapturer = Camera2Capturer(context, cameraId)
val peerConnectionFactory = PeerConnectionFactory.builder()
    .setVideoEncoderFactory(...)
    .setVideoDecoderFactory(...)
    .createPeerConnectionFactory()

// 建立 P2P 连接
val peerConnection = peerConnectionFactory.createPeerConnection(config, observer)

// 添加本地视频轨道
val localVideoTrack = createVideoTrack(videoCapturer, eglBase)
localVideoTrack.addSink(localRenderer)
peerConnection.addTrack(localVideoTrack)
            """.trimIndent()) { onIntent(XRToolkitIntent.CopyCodeSample("")) }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.WarningOrange.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Colors.WarningOrange.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Colors.WarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("隐私合规", color = Colors.WarningOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("摄像头开启前必须获得用户明确授权", "视频流不得在未经授权情况下录制或传输", "使用隐私沙盒 API 处理敏感数据").forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", color = Colors.WarningOrange)
                            Text(item, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isEnabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isEnabled) Colors.SuccessGreen else Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isEnabled) Colors.SuccessGreen else Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (isEnabled) "已启用" else "已禁用", color = if (isEnabled) Colors.SuccessGreen else Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ================================================================
// Test Debug Page
// ================================================================

@Composable
private fun TestDebugPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, null, tint = Colors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("测试与调试工具", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("XR 应用调试指南与常见问题排查。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item { SectionTitle("调试环境") }

        item {
            DebugItemCard(
                title = "XR 模拟器调试",
                items = listOf("使用 Android Studio XR AVD 运行应用", "通过 Logcat 过滤 'XR' 标签查看日志", "使用 Layout Inspector 检查 UI 层级")
            )
        }

        item {
            DebugItemCard(
                title = "真机调试",
                items = listOf("通过 WiFi 连接真机（adb connect <ip>:5555）", "启用开发者选项中的 XR 调试", "使用 ADB 获取眼镜视野截图")
            )
        }

        item { SectionTitle("常见问题排查") }

        items(
            listOf(
                Triple("SDK 版本不兼容", "确保使用 Android XR SDK API 34+", "⚠️"),
                Triple("Gemini API 调用失败", "检查 API Key 配置和网络连接", "🔑"),
                Triple("眼镜视野无内容显示", "检查 GlassesDisplay 是否正确初始化", "👁️"),
                Triple("语音输入无响应", "检查 RECORD_AUDIO 权限是否授予", "🎤"),
                Triple("跨设备连接失败", "确保手机和眼镜在同一局域网", "📱")
            )
        ) { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(desc, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugItemCard(title: String, items: List<String>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("• ", color = Colors.Primary)
                    Text(item, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ================================================================
// Privacy Page
// ================================================================

@Composable
private fun PrivacyPage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val privacy = state.privacyState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Colors.PrivacyPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("隐私合规检测", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("摄像头数据处理、隐私沙盒合规、审计报告生成。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Summary
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    value = "${privacy.passedItems}",
                    label = "通过",
                    color = Colors.SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${privacy.failedItems}",
                    label = "待修复",
                    color = Colors.ErrorRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Button(
                onClick = { onIntent(XRToolkitIntent.RunPrivacyAudit) },
                enabled = !privacy.isAuditing,
                colors = ButtonDefaults.buttonColors(containerColor = Colors.PrivacyPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (privacy.isAuditing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("审计中...")
                } else {
                    Icon(Icons.Default.Policy, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("运行隐私审计")
                }
            }
        }

        item { SectionTitle("审计结果") }

        items(privacy.auditResults) { item ->
            PrivacyAuditCard(item)
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PrivacyAuditCard(item: PrivacyAuditItem) {
    val (icon, color) = when (item.status) {
        PrivacyAuditStatus.PASS -> "✅" to Colors.SuccessGreen
        PrivacyAuditStatus.FAIL -> "❌" to Colors.ErrorRed
        PrivacyAuditStatus.WARNING -> "⚠️" to Colors.WarningOrange
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.description, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("💡 ${item.recommendation}", color = color.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

// ================================================================
// Cross Device Page
// ================================================================

@Composable
private fun CrossDevicePage(state: XRToolkitState, onIntent: (XRToolkitIntent) -> Unit) {
    val cd = state.crossDeviceState

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Colors.Surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, null, tint = Colors.CrossDeviceTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("跨设备协同架构", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("手机作为计算中枢，眼镜作为显示/音频端。使用 Android CrossDeviceService 实现状态同步。", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onIntent(XRToolkitIntent.SetCrossDeviceConnection(!cd.isConnected)) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("跨设备连接", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                when (cd.syncStatus) {
                                    SyncStatus.DISCONNECTED -> "未连接"
                                    SyncStatus.CONNECTING -> "连接中..."
                                    SyncStatus.SYNCING -> "同步中..."
                                    SyncStatus.SYNCED -> "已连接 · 上次同步: ${cd.lastSyncTime}"
                                },
                                color = when (cd.syncStatus) {
                                    SyncStatus.SYNCED -> Colors.SuccessGreen
                                    SyncStatus.CONNECTING, SyncStatus.SYNCING -> Colors.WarningOrange
                                    else -> Color.White.copy(alpha = 0.5f)
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = cd.isConnected,
                            onCheckedChange = { onIntent(XRToolkitIntent.SetCrossDeviceConnection(it)) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Colors.CrossDeviceTeal)
                        )
                    }
                }
            }
        }

        item { SectionTitle("协同模式") }

        items(
            listOf(
                Triple("计算卸载", "复杂推理在手机端执行，结果传回眼镜显示", Icons.Default.Memory),
                Triple("音频路由", "语音输入走眼镜麦克风，音频输出走眼镜扬声器", Icons.Default.Headphones),
                Triple("状态同步", "用户上下文在设备和眼镜间实时同步", Icons.Default.Sync),
                Triple("隐私分离", "敏感数据留在手机端，眼镜仅接收渲染指令", Icons.Default.Security)
            )
        ) { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Colors.SurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = Colors.CrossDeviceTeal, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(desc, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            CodeSampleCard("CrossDeviceService 示例", """
// CrossDeviceService 使用示例
val crossDeviceService = CrossDeviceService.getInstance(context)

crossDeviceService.registerDevice(DeviceType.GLASSES) { device ->
    device.onMessageReceived = { message ->
        // 处理来自眼镜的消息
        when (message.type) {
            "audio_input" -> processAudio(message.data)
            "touch_event" -> processTouch(message.data)
        }
    }
}

// 发送渲染指令到眼镜
crossDeviceService.sendMessage(
    deviceType = DeviceType.GLASSES,
    message = RenderMessage(
        type = "text_overlay",
        content = "你好，Android XR!",
        position = Position.CENTER
    )
)
            """.trimIndent()) { }
        }
    }
}

// ================================================================
// Shared UI Components
// ================================================================

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun CodeSampleCard(title: String, code: String, onCopy: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, "复制", tint = Colors.Primary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E), shape = RoundedCornerShape(8.dp)).horizontalScroll(rememberScrollState()).padding(12.dp)
            ) {
                Text(code, color = Color(0xFFD4D4D4), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
