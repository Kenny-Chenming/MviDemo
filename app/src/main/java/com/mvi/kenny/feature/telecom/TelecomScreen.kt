package com.mvi.kenny.feature.telecom

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig

import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================
// TelecomScreen — Jetpack Telecom v1.1.0 VoIP Native Visibility
// 集成工具包主屏幕
// =============================================================
// PRD-254 | Jetpack Telecom v1.1.0 VoIP Native Visibility Integration Toolkit
// 2026-05-14 Google 官博发布 | Android 16.1 SDK 36.1 Alpha
//
// 6 Tool Tabs / 6个工具 Tab:
// 1. Overview              — 功能总览首页
// 2. Call Log Integration — 通话日志接入系统拨号器
// 3. Callback from Dialer  — 系统拨号器回拨 VoIP
// 4. Call Log Exclusion   — 通话日志排除（隐私）
// 5. Allowlist            — Secure Package Allowlist 申请
// 6. CI Tool              — CI 验证工具

// =============================================================
// Design System Constants / 设计系统常量
// =============================================================
private val DarkBackground = Color(0xFF0D1117)
private val DarkCardBackground = Color(0xFF161B22)
private val DarkSecondaryText = Color(0xFF8B949E)
private val AccentBlue = Color(0xFF58A6FF)
private val SuccessGreen = Color(0xFF3FB950)
private val WarningYellow = Color(0xFFD29922)
private val ErrorRed = Color(0xFFF85149)
private val CodeBackground = Color(0xFF161B22)

// =============================================================
// TelecomScreen — 主屏幕入口
// =============================================================

/**
 * Telecom Toolkit main screen / Telecom 工具包主屏幕
 *
 * @param viewModel TelecomViewModel instance / TelecomViewModel 实例
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 * @see TelecomContract For state/intent/effect definitions
 */
@Composable
fun TelecomScreen(
    viewModel: TelecomViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    // Update TopBar config / 更新 TopBar 配置
    LaunchedEffect(state.selectedTab, state.isDarkTheme) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Telecom · VoIP Visibility",
                actions = listOf(
                    TopBarAction(
                        icon = if (state.isDarkTheme) Icons.Default.DarkMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme / 切换主题",
                        onClick = { viewModel.sendIntent(TelecomIntent.ToggleTheme) }
                    )
                )
            )
        )
    }

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TelecomEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TelecomEffect.CodeCopied -> {
                    clipboardManager.setText(AnnotatedString(effect.code))
                    snackbarHostState.showSnackbar("Code copied! / 代码已复制!")
                }
                is TelecomEffect.AllowlistSubmitted -> {
                    snackbarHostState.showSnackbar("${effect.appName} submitted for review! / ${effect.appName} 已提交审核!")
                }
                is TelecomEffect.OpenExternalLink -> {
                    // In a real app, this would open the URL
                    snackbarHostState.showSnackbar("Opening: ${effect.url}")
                }
                is TelecomEffect.CIValidationComplete -> {
                    val message = if (effect.passed) {
                        "✅ CI Validation Passed! / CI 验证通过!"
                    } else {
                        "❌ CI Validation Failed — check report below / CI 验证失败 — 请查看下方报告"
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(if (state.isDarkTheme) DarkBackground else Color.White)
        ) {
            // Tab Navigation / Tab 导航
            TelecomTabNavigation(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(TelecomIntent.SelectTab(it)) },
                isDarkTheme = state.isDarkTheme
            )

            // Tab Content / Tab 内容
            AnimatedContent(
                targetState = state.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContentTransition"
            ) { tab ->
                when (tab) {
                    TelecomTab.OVERVIEW -> OverviewTab(state, viewModel)
                    TelecomTab.CALL_LOG_INTEGRATION -> CallLogIntegrationTab(state, viewModel)
                    TelecomTab.CALLBACK_FROM_DIALER -> CallbackFromDialerTab(state, viewModel)
                    TelecomTab.CALL_LOG_EXCLUSION -> CallLogExclusionTab(state, viewModel)
                    TelecomTab.ALLOWLIST -> AllowlistTab(state, viewModel)
                    TelecomTab.CI_TOOL -> CIToolTab(state, viewModel)
                }
            }
        }
    }
}

// =============================================================
// Tab Navigation / Tab 导航
// =============================================================

@Composable
private fun TelecomTabNavigation(
    selectedTab: TelecomTab,
    onTabSelected: (TelecomTab) -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) DarkCardBackground else Color(0xFFF6F8FA)
    val selectedColor = if (isDarkTheme) DarkBackground else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF24292F)
    val selectedTextColor = AccentBlue

    ScrollableTabRow(
        selectedTabIndex = TelecomTab.entries.indexOf(selectedTab),
        containerColor = backgroundColor,
        contentColor = textColor,
        edgePadding = 8.dp
    ) {
        TelecomTab.entries.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.titleZh,
                        color = if (selectedTab == tab) selectedTextColor else textColor,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

// =============================================================
// Overview Tab / 总览 Tab
// =============================================================

@Composable
private fun OverviewTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // PRD Introduction Card / PRD 介绍卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "PRD-254",
                            color = AccentBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Jetpack Telecom v1.1.0",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "VoIP Native Visibility 集成工具包 / VoIP Native Visibility Integration Toolkit",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "2026-05-14 Google 官博发布 | Android 16.1 SDK 36.1 Alpha",
                    color = secondaryText,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "首次为第三方 VoIP App 提供与系统拨号器同等的可见性——VoIP 通话记录可直接出现在系统通话历史中，用户可在系统拨号器中直接对 VoIP 联系发起回拨。",
                    color = secondaryText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Core Features Grid / 核心功能矩阵
        Text(
            "核心功能 / Core Features",
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        val features = listOf(
            Triple("📞", "Call Log Integration", "通话日志接入系统拨号器"),
            Triple("🔄", "Callback from Dialer", "系统拨号器回拨 VoIP"),
            Triple("🚫", "Call Log Exclusion", "通话日志排除（隐私）"),
            Triple("🔒", "Secure Package Allowlist", "白名单申请通道"),
            Triple("⚙️", "CI Validation Tool", "CI 验证工具"),
            Triple("📋", "API Reference Guide", "完整 API 参考指南")
        )

        features.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (emoji, titleEn, titleZh) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(emoji, fontSize = 24.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(titleEn, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(titleZh, color = secondaryText, fontSize = 11.sp)
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Telecom Support Status / Telecom 支持状态
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("设备状态 / Device Status", color = textColor, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                val isSupported = state.connectionServiceConfig.isSupported
                val supportColor = if (isSupported) SuccessGreen else ErrorRed
                val supportText = if (isSupported) "支持 Supported" else "不支持 Not Supported"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isSupported) Icons.Default.Check else Icons.Default.Error,
                        contentDescription = null,
                        tint = supportColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "TelecomManager.isSupported(): $supportText (Android ${android.os.Build.VERSION.SDK_INT})",
                        color = supportColor,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(4.dp))

                val supportsCallback = state.connectionServiceConfig.supportsCallback
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (supportsCallback) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (supportsCallback) SuccessGreen else WarningYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Dialer Callback: ${if (supportsCallback) "支持 Supported" else "不支持 Not Supported (需要 Android 14+ / API 34+)"}",
                        color = if (supportsCallback) SuccessGreen else WarningYellow,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick Actions / 快捷操作
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.sendIntent(TelecomIntent.CheckTelecomSupport) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("检测设备 / Check Device", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = { viewModel.sendIntent(TelecomIntent.RunCIValidation) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                enabled = !state.isRunningCIValidation
            ) {
                if (state.isRunningCIValidation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("运行 CI / Run CI", fontSize = 13.sp)
                }
            }
        }
    }
}

// =============================================================
// Call Log Integration Tab / 通话日志接入 Tab
// =============================================================

@Composable
private fun CallLogIntegrationTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val codeBg = if (isDark) CodeBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Mechanism Explanation / 机制说明
        SectionCard(
            title = "机制说明 / Mechanism",
            cardBg = cardBg,
            textColor = textColor,
            secondaryText = secondaryText
        ) {
            Text(
                "VoIP 通话通过 ConnectionService 接入系统通话记录。系统拨号器使用 CallCardInfo 展示 VoIP 通话，与原生通话呈现方式一致。",
                color = secondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Key API Cards / 关键 API 卡片
        Text("关键 API / Key APIs", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        ApiCard(
            title = "ConnectionService",
            description = "VoIP 连接服务基类，需实现 addConnection() 等方法",
            codeSnippet = """
// AndroidManifest.xml
<service
    android:name=".VoipConnectionService"
    android:permission="android.permission.BIND_TELECOM_CONNECTION_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.telecom.ConnectionService" />
    </intent-filter>
</service>
            """.trimIndent(),
            cardBg = cardBg,
            codeBg = codeBg,
            textColor = textColor,
            secondaryText = secondaryText,
            borderColor = borderColor,
            onCopy = { viewModel.sendIntent(TelecomIntent.CopyCodeBlock("conn-service", "android:name")) }
        )

        Spacer(Modifier.height(8.dp))

        ApiCard(
            title = "CallCardInfo",
            description = "通话卡片信息，用于系统拨号器展示",
            codeSnippet = """
val callCardInfo = CallCardInfo.Builder(
    "Example VoIP",           // Call party name
    "+86 138-0000-0001"      // Phone number
).setCallType(Call.Details.CALL_TYPE_OUTGOING)
  .setConnectTimeMillis(System.currentTimeMillis())
  .build()
            """.trimIndent(),
            cardBg = cardBg,
            codeBg = codeBg,
            textColor = textColor,
            secondaryText = secondaryText,
            borderColor = borderColor,
            onCopy = { viewModel.sendIntent(TelecomIntent.CopyCodeBlock("call-card-info", "CallCardInfo")) }
        )

        Spacer(Modifier.height(12.dp))

        // Call Log Sample / 通话记录示例
        Text("通话记录示例 / Sample Call Logs", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        state.callLogEntries.forEach { entry ->
            CallLogEntryCard(
                entry = entry,
                isDark = isDark,
                cardBg = cardBg,
                textColor = textColor,
                secondaryText = secondaryText,
                onToggleExclusion = {
                    viewModel.sendIntent(TelecomIntent.ToggleCallLogExclusion(entry.id))
                }
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

// =============================================================
// Callback from Dialer Tab / 拨号器回拨 Tab
// =============================================================

@Composable
private fun CallbackFromDialerTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val codeBg = if (isDark) CodeBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Warning Banner / 警告横幅
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = WarningYellow.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Callback from Dialer requires Secure Package Allowlist approval. Apply first! / 拨号器回拨需要白名单审批，请先申请！",
                    color = WarningYellow,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Mechanism / 机制说明
        SectionCard(
            title = "回拨机制 / Callback Mechanism",
            cardBg = cardBg,
            textColor = textColor,
            secondaryText = secondaryText
        ) {
            Text(
                "用户可在系统拨号器中直接对 VoIP 联系发起回拨。系统通过 TelecomCallback 回调到 VoIP App，App 收到回调后启动拨打流程。",
                color = secondaryText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // TelecomCallback Code / TelecomCallback 代码
        ApiCard(
            title = "TelecomCallback 实现 / TelecomCallback Implementation",
            description = "接收系统拨号器的回拨请求",
            codeSnippet = """
class VoipTelecomCallback : TelecomManager.TelecomCallback() {
    override fun onCallbackDial(withPhoneNumber: String) {
        // User tapped callback button in system dialer
        // 用户在系统拨号器中点击了回拨按钮
        val intent = Intent(this, VoipCallActivity::class.java).apply {
            action = ACTION_PLACE_CALL
            putExtra(EXTRA_PHONE_NUMBER, withPhoneNumber)
        }
        startActivity(intent)
    }
}

// Register callback / 注册回调
val telecomCallback = VoipTelecomCallback()
telecomManager.addTelecomCallback(telecomCallback)
            """.trimIndent(),
            cardBg = cardBg,
            codeBg = codeBg,
            textColor = textColor,
            secondaryText = secondaryText,
            borderColor = borderColor,
            onCopy = { viewModel.sendIntent(TelecomIntent.CopyCodeBlock("telecom-callback", "TelecomCallback")) }
        )

        Spacer(Modifier.height(12.dp))

        // Security Note / 安全注意事项
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🔒 安全说明 / Security Note", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• Callback from Dialer 依赖 Secure Package Allowlist\n• 仅白名单内 App 可注册 TelecomCallback\n• 必须验证回调请求的合法性",
                    color = secondaryText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// =============================================================
// Call Log Exclusion Tab / 通话日志排除 Tab
// =============================================================

@Composable
private fun CallLogExclusionTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val codeBg = if (isDark) CodeBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Use Cases / 使用场景
        SectionCard(
            title = "排除场景 / Exclusion Use Cases",
            cardBg = cardBg,
            textColor = textColor,
            secondaryText = secondaryText
        ) {
            val useCases = listOf(
                "🔒 私密通话 / Private calls" to "用户标记为私密的通话不应出现在系统通话记录",
                "⏱️ 临时通话 / Temporary calls" to "一次性或临时性通话（如验证码）不需记录",
                "📵 屏蔽通话 / Blocked calls" to "用户主动屏蔽的联系人通话"
            )
            useCases.forEach { (title, desc) ->
                Text(title, color = textColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(desc, color = secondaryText, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // API Code / API 代码
        ApiCard(
            title = "CallLogIntegration excludeFromCallLog()",
            description = "将通话标记为不进入系统通话日志",
            codeSnippet = """
// In VoipConnectionService / 在 VoipConnectionService 中
override fun onCreateIncomingConnection(
    connectionManagerPhoneAccount: PhoneAccountHandle,
    request: ConnectionRequest
) {
    val connection = VoipConnection().apply {
        // Mark as excluded from system call log
        // 标记为不进入系统通话日志
        setConnectionProperties(
            Connection.PROPERTY_DISABLE_CALL_LOG
        )
        setAddress(
            request.getAddress(),
            PhoneNumberUtils.convertKeypadLettersToDigits(
                request.getAddress().toString()
            )
        )
    }
    connection.connectionTime = System.currentTimeMillis()
    setConnection(connection)
    connection.addConnectionServiceListener(this)
}
            """.trimIndent(),
            cardBg = cardBg,
            codeBg = codeBg,
            textColor = textColor,
            secondaryText = secondaryText,
            borderColor = borderColor,
            onCopy = { viewModel.sendIntent(TelecomIntent.CopyCodeBlock("exclusion-api", "PROPERTY_DISABLE_CALL_LOG")) }
        )

        Spacer(Modifier.height(12.dp))

        // Privacy Design / 隐私设计
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🛡️ 隐私合规建议 / Privacy Compliance", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• 仅在用户明确要求时排除通话记录\n• 排除操作需要用户主动授权\n• 隐私政策中需披露通话记录排除行为",
                    color = secondaryText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// =============================================================
// Allowlist Tab / 白名单申请 Tab
// =============================================================

@Composable
private fun AllowlistTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val codeBg = if (isDark) CodeBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)
    val app = state.allowlistApplication

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Status Banner / 状态横幅
        val statusColor = app.status.color
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("申请状态 / Application Status", color = textColor, fontSize = 12.sp)
                    Text(
                        "${app.status.labelZh} / ${app.status.labelEn}",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (app.submittedAt > 0) {
                    Text(
                        "Submitted: ${formatTimestamp(app.submittedAt)}",
                        color = secondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Application Form / 申请表单
        SectionCard(
            title = "申请信息 / Application Info",
            cardBg = cardBg,
            textColor = textColor,
            secondaryText = secondaryText
        ) {
            OutlinedTextField(
                value = app.packageName,
                onValueChange = { viewModel.sendIntent(TelecomIntent.UpdateAllowlistApplication(app.copy(packageName = it))) },
                label = { Text("Package Name / 包名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = app.appName,
                onValueChange = { viewModel.sendIntent(TelecomIntent.UpdateAllowlistApplication(app.copy(appName = it))) },
                label = { Text("App Name / 应用名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = app.website,
                onValueChange = { viewModel.sendIntent(TelecomIntent.UpdateAllowlistApplication(app.copy(website = it))) },
                label = { Text("Website / 官网") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = app.description,
                onValueChange = { viewModel.sendIntent(TelecomIntent.UpdateAllowlistApplication(app.copy(description = it))) },
                label = { Text("Description / 应用描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = app.expectedMonthlyUsers,
                onValueChange = { viewModel.sendIntent(TelecomIntent.UpdateAllowlistApplication(app.copy(expectedMonthlyUsers = it))) },
                label = { Text("Expected Monthly Users / 预期月活用户") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(Modifier.height(12.dp))

        // Submit Button / 提交按钮
        Button(
            onClick = { viewModel.sendIntent(TelecomIntent.SubmitAllowlistApplication) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            enabled = !state.isLoading && app.status == AllowlistStatus.NOT_APPLIED
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Submitting... / 提交中...")
            } else {
                Text("提交申请 / Submit Application", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Timeline / 时间线
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("审批时间线 / Review Timeline", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "提交申请 Submit" to "Day 0",
                    "初步审核 Initial Review" to "1-3 business days",
                    "安全评估 Security Assessment" to "3-7 business days",
                    "批准/拒绝 Approve/Reject" to "7-14 business days"
                ).forEach { (step, time) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(step, color = textColor, fontSize = 12.sp)
                        Text(time, color = secondaryText, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// =============================================================
// CI Tool Tab / CI 验证工具 Tab
// =============================================================

@Composable
private fun CIToolTab(state: TelecomState, viewModel: TelecomViewModel) {
    val isDark = state.isDarkTheme
    val cardBg = if (isDark) DarkCardBackground else Color(0xFFF6F8FA)
    val codeBg = if (isDark) CodeBackground else Color(0xFFF6F8FA)
    val textColor = if (isDark) Color.White else Color(0xFF24292F)
    val secondaryText = if (isDark) DarkSecondaryText else Color(0xFF57606A)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Run CI Button / 运行 CI 按钮
        Button(
            onClick = { viewModel.sendIntent(TelecomIntent.RunCIValidation) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.ciValidationReport?.overallPassed == true) SuccessGreen else AccentBlue
            ),
            enabled = !state.isRunningCIValidation
        ) {
            if (state.isRunningCIValidation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
                Text("Validating... / 验证中...", fontSize = 14.sp)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("运行 CI 验证 / Run CI Validation", fontSize = 14.sp)
            }
        }

        if (state.isRunningCIValidation) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AccentBlue
            )
            Text(
                "Scanning ConnectionService... / 扫描 ConnectionService...",
                color = secondaryText,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Validation Report / 验证报告
        state.ciValidationReport?.let { report ->
            // Summary Card / 摘要卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.overallPassed) SuccessGreen.copy(alpha = 0.1f)
                    else ErrorRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (report.overallPassed) Icons.Default.Check else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (report.overallPassed) SuccessGreen else ErrorRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                if (report.overallPassed) "✅ ALL CHECKS PASSED" else "❌ VALIDATION FAILED",
                                color = if (report.overallPassed) SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "${report.passedChecks} passed, ${report.failedChecks} failed, ${report.warningChecks} warnings",
                                color = secondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Individual Results / 各检查项结果
            Text("检查详情 / Check Details", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            report.results.forEach { result ->
                CIResultCard(
                    result = result,
                    isDark = isDark,
                    cardBg = cardBg,
                    textColor = textColor,
                    secondaryText = secondaryText,
                    borderColor = borderColor
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// =============================================================// Helper Composable Functions / 辅助 Composable 函数
// =============================================================

@Composable
private fun SectionCard(
    title: String,
    cardBg: Color,
    textColor: Color,
    secondaryText: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ApiCard(
    title: String,
    description: String,
    codeSnippet: String,
    cardBg: Color,
    codeBg: Color,
    textColor: Color,
    secondaryText: Color,
    borderColor: Color,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, color = secondaryText, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(codeBg)
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .clickable { onCopy() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        codeSnippet,
                        color = textColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy / 复制",
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallLogEntryCard(
    entry: CallLogEntry,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    secondaryText: Color,
    onToggleExclusion: () -> Unit
) {
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)
    val callTypeColor = when (entry.callType) {
        CallType.INCOMING -> SuccessGreen
        CallType.OUTGOING -> AccentBlue
        CallType.MISSED -> ErrorRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.contactName, color = textColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(callTypeColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            entry.callType.labelZh,
                            color = callTypeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (entry.isExcluded) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .background(WarningYellow.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("已排除", color = WarningYellow, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Text(entry.phoneNumber, color = secondaryText, fontSize = 11.sp)
                Text(
                    "${formatTimestamp(entry.timestamp)} · ${formatDuration(entry.duration)}",
                    color = secondaryText,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onToggleExclusion, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (entry.isExcluded) Icons.Default.Share else Icons.Default.Check,
                    contentDescription = if (entry.isExcluded) "取消排除" else "排除通话",
                    tint = if (entry.isExcluded) WarningYellow else SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CIResultCard(
    result: CIValidationResult,
    isDark: Boolean,
    cardBg: Color,
    textColor: Color,
    secondaryText: Color,
    borderColor: Color
) {
    val resultColor = when {
        result.passed && result.message.contains("WARNING") -> WarningYellow
        result.passed -> SuccessGreen
        else -> ErrorRed
    }
    val icon = when {
        result.passed && result.message.contains("WARNING") -> Icons.Default.Warning
        result.passed -> Icons.Default.Check
        else -> Icons.Default.Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = resultColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(result.checkName, color = textColor, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                Text(result.message, color = secondaryText, fontSize = 11.sp, lineHeight = 15.sp)
                if (result.filePath != null) {
                    Text(
                        "${result.filePath}${result.lineNumber?.let { ":$it" } ?: ""}",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =============================================================// Utility Functions / 工具函数
// =============================================================

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDuration(seconds: Int): String {
    return when {
        seconds == 0 -> "未接 / Missed"
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}