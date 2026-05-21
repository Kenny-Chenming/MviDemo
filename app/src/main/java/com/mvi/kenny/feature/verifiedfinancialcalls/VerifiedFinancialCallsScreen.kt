package com.mvi.kenny.feature.verifiedfinancialcalls

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ============================================================
 * VerifiedFinancialCallsScreen — Verified Financial Calls API 工具包主界面
 * Verified Financial Calls API Developer Toolkit Main Screen
 * ============================================================
 *
 * PRD-257 | Android Security 2026 Verified Financial Calls API 集成工具包
 * Ref: memory/agency/designs/PRD-257-Android-Security-Verified-Financial-Calls-API集成工具包.md
 *
 * Design: Professional Financial Toolkit Style
 * - Dark background (#121212) with security green (#66BB6A) primary
 * - Cyan secondary (#29B6F6) for tech accents
 * - Red error (#EF5350) for scam/warning indicators
 * - JetBrains Mono for code blocks
 *
 * 5 Tab Bottom Navigation:
 * - Tab 0: 概述 (Overview) — API background, core concepts, prerequisites
 * - Tab 1: 验证 API (Verification API) — Verified Financial Calls API integration
 * - Tab 2: 注册配置 (Registration) — Developer Console config, number registration
 * - Tab 3: 合规指南 (Compliance) — GDPR/CCPA compliance checklist
 * - Tab 4: 扩展能力 (Extensions) — Live Threat Detection / Dynamic Signal Monitoring
 */

// ============================================================
// Color Theme / 颜色主题
// ============================================================

private object SecurityColors {
    val Primary = Color(0xFF1B5E20)
    val PrimaryLight = Color(0xFF66BB6A)
    val Secondary = Color(0xFF0277BD)
    val SecondaryLight = Color(0xFF29B6F6)
    val Error = Color(0xFFC62828)
    val ErrorLight = Color(0xFFEF5350)
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val CodeBlockBg = Color(0xFF1E1E1E)
}

// ============================================================
// Code Block Component / 代码块组件
// ============================================================

@Composable
private fun CodeBlock(
    id: String,
    title: String,
    code: String,
    description: String = "",
    isCopied: Boolean = false,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.CodeBlockBg),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SecurityColors.PrimaryLight,
                    fontWeight = FontWeight.Medium
                )
                IconButton(
                    onClick = { onCopy(code) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Description else Icons.Default.ContentCopy,
                        contentDescription = "复制代码",
                        tint = if (isCopied) SecurityColors.PrimaryLight else Color.Gray
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color(0xFF0D0D0D), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFE0E0E0),
                    lineHeight = 18.sp
                )
            }
            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ============================================================
// Tab 0: Overview Content / 概述内容
// ============================================================

@Composable
private fun OverviewTabContent(
    state: VerifiedFinancialCallsState,
    onToggleCard: (Int) -> Unit,
    onCopyCodeBlock: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // API Introduction Card / API 介绍卡片
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SecurityColors.Primary.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SecurityColors.PrimaryLight,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Verified Financial Calls API",
                                style = MaterialTheme.typography.titleMedium,
                                color = SecurityColors.PrimaryLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Android Security 2026 — 来电反诈骗验证系统",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "当用户接到疑似来自银行的来电时，Android 自动查询银行 App 确认是否真实来电，虚假则自动挂断。诈骗损失 980M 美元/年。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Core Concepts Section / 核心概念
        item {
            Text(
                text = "核心概念 / Core Concepts",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Card 1: isCallFromBank / 核心验证接口
        item {
            ExpandableCard(
                title = "isCallFromBank(number)",
                description = "核心验证接口 — 判断来电号码是否来自银行官方",
                iconName = "security",
                isExpanded = 1 in state.expandedCards,
                onToggle = { onToggleCard(1) }
            ) {
                Text(
                    text = "银行 App 必须实现此接口，当系统查询时返回布尔值。返回 true 表示真实银行来电，false 表示可疑。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Card 2: inbound-only / 号码标记规则
        item {
            ExpandableCard(
                title = "inbound-only 标记规则",
                description = "官方号码声明 — 只能接听不能外呼",
                iconName = "phone",
                isExpanded = 2 in state.expandedCards,
                onToggle = { onToggleCard(2) }
            ) {
                Text(
                    text = "注册为 inbound-only 的号码只能接听来电，不能作为主叫方。这防止诈骗者冒充银行外呼。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Card 3: Developer Console / 配置流程
        item {
            ExpandableCard(
                title = "Developer Console 配置",
                description = "金融机构注册流程 — 实名/企业认证",
                iconName = "business",
                isExpanded = 3 in state.expandedCards,
                onToggle = { onToggleCard(3) }
            ) {
                Text(
                    text = "银行需在 Google Developer Console 中完成企业认证，提交官方号码清单，签署合规协议。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Prerequisites Section / 接入前提
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "接入前提 / Prerequisites",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(
            listOf(
                "Android 17 (API Level 37) 或更高版本",
                "Google Play 服务最新版本",
                "成为认证金融机构合作伙伴（Revolut/Itaú/Nubank 等）",
                "在 Developer Console 完成企业实名认证",
                "声明官方银行号码并标记 inbound-only"
            )
        ) { prerequisite ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(SecurityColors.PrimaryLight, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = prerequisite,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

// ============================================================
// Expandable Card Component / 可展开卡片组件
// ============================================================

@Composable
private fun ExpandableCard(
    title: String,
    description: String,
    iconName: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getFeatureIcon(iconName),
                        contentDescription = null,
                        tint = SecurityColors.PrimaryLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = Color.Gray
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    expandedContent()
                }
            }
        }
    }
}

@Composable
private fun getFeatureIcon(iconName: String): ImageVector = when (iconName) {
    "security" -> Icons.Default.Security
    "phone" -> Icons.Default.Info
    "business" -> Icons.Default.Build
    "layers" -> Icons.Default.Layers
    "warning" -> Icons.Default.Warning
    else -> Icons.Default.Info
}

// ============================================================
// Tab 1: Verification API Content / 验证 API 内容
// ============================================================

@Composable
private fun VerificationApiTabContent(
    state: VerifiedFinancialCallsState,
    onCopyCodeBlock: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "isCallFromBank() 实现 / Implementation",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            CodeBlock(
                id = "isCallFromBank_impl",
                title = "VerifiedFinancialCallsService.kt",
                code = """\
// VerifiedFinancialCallsService.kt
// 银行 App 端：实现 isCallFromBank 验证接口
// Bank App side: implement isCallFromBank verification interface

class VerifiedFinancialCallsService : Service() {

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onHandoffActivityRequested(
        intent: Intent,
        callback: HandoffActivityCallback
    ) {
        // 处理系统发起的来电验证请求
        // Handle system-initiated call verification request
        val phoneNumber = intent.getStringExtra(
            TelecomManager.EXTRA_CALLER_PHONE_NUMBER
        ) ?: return

        val isVerified = isCallFromBank(phoneNumber)
        val result = HandoffActivityData.Builder()
            .putBoolean("is_verified", isVerified)
            .putString("bank_name", getBankName())
            .build()

        callback.onSuccess(result)
    }

    /** 判断来电号码是否来自银行官方 / Check if call is from official bank */
    private fun isCallFromBank(number: String): Boolean {
        val normalizedNumber = number.normalizeForComparison()
        return officialNumbers.any { it == normalizedNumber }
    }

    /** 号码标准化 / Normalize phone number for comparison */
    private fun String.normalizeForComparison(): String {
        return this.replace(Regex("[^0-9+]"), "")
    }
}""".trimIndent(),
                description = "核心 Service 实现 — 必须使用 Activity Context",
                isCopied = state.copiedBlockId == "isCallFromBank_impl",
                onCopy = { code -> onCopyCodeBlock("isCallFromBank_impl", code) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Text(
                text = "AndroidManifest.xml 配置 / Configuration",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            CodeBlock(
                id = "manifest_config",
                title = "AndroidManifest.xml",
                code = """\
<!-- AndroidManifest.xml -->
<!-- 必须使用 Activity Context，不能用 Application Context -->
<!-- Must use Activity Context, Application Context will return wrong metrics -->

<service
    android:name=".VerifiedFinancialCallsService"
    android:permission="android.permission.BIND_VERIFIED_FINANCIAL_CALLS_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="
            android.telecom.AndroidVerifiedFinancialCalls
        " />
    </intent-filter>
</service>

<!-- 权限声明 / Permission declaration -->
<uses-permission android:name="
    android.permission.READ_PHONE_STATE
" />""".trimIndent(),
                description = "Service 注册 — 需要 BIND_VERIFIED_FINANCIAL_CALLS_SERVICE 权限",
                isCopied = state.copiedBlockId == "manifest_config",
                onCopy = { code -> onCopyCodeBlock("manifest_config", code) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Context Trap Warning / Context 陷阱警告
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SecurityColors.Error.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠️ Context 陷阱 / Context Trap",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecurityColors.ErrorLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "金融 API 调用必须在 Activity Context 下进行。Application Context 可能返回错误的签名信息，导致验证失败。与 App Bubbles 虚拟 display 陷阱类似。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ============================================================
// Tab 2: Registration Content / 注册配置内容
// ============================================================

@Composable
private fun RegistrationTabContent(
    state: VerifiedFinancialCallsState,
    onCopyCodeBlock: (String, String) -> Unit
) {
    val steps = remember {
        listOf(
            Triple("Step 1", "企业认证 / Enterprise Verification", "在 Google Play Developer Console 完成企业实名认证（Individual 或 Organization）"),
            Triple("Step 2", "申请白名单 / Apply Allowlist", "提交公司注册文件、监管牌照、官方号码清单"),
            Triple("Step 3", "签署合规协议 / Sign Compliance", "签署 GDPR Article 9、CCPA Section 1798.100 合规协议"),
            Triple("Step 4", "注册号码 / Register Numbers", "在 Console 中注册官方号码，标记 inbound-only 属性"),
            Triple("Step 5", "集成 API / Integrate API", "在银行 App 中实现 VerifiedFinancialCallsService")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Developer Console 配置流程 / Setup Steps",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(steps) { (step, title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(SecurityColors.Primary, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.last().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "号码注册 API / Number Registration API",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            CodeBlock(
                id = "number_registration",
                title = "NumberRegistration.kt",
                code = """\
// 号码注册 — 将银行官方号码注册到系统
// Number Registration — Register official bank numbers to system

val telecomManager = context.getSystemService(
    Context.TELECOM_SERVICE
) as TelecomManager

val registerRequest = VerifiedFinancialCallsRegistration(
    phoneNumbers = listOf(
        "+1-800-123-4567",
        "+1-800-765-4321"
    ),
    bankName = "MyBank",
    inboundOnly = true,
    effectiveDate = System.currentTimeMillis()
)

telecomManager.registerVerifiedFinancialCallsNumbers(
    registerRequest,
    executor,
    callback
)""".trimIndent(),
                description = "使用 TelecomManager.registerVerifiedFinancialCallsNumbers() 注册号码",
                isCopied = state.copiedBlockId == "number_registration",
                onCopy = { code -> onCopyCodeBlock("number_registration", code) }
            )
        }
    }
}

// ============================================================
// Tab 3: Compliance Content / 合规指南内容
// ============================================================

@Composable
private fun ComplianceTabContent(
    state: VerifiedFinancialCallsState,
    onToggleItem: (Int) -> Unit
) {
    val complianceItems = remember {
        listOf(
            // GDPR
            ComplianceItem(1, "GDPR Article 9 — 特殊类别数据处理", "来电验证涉及金融数据，属于特殊类别。必须明确记录处理目的（诈骗预防）和法律依据（合法权益）。", "GDPR"),
            ComplianceItem(2, "数据最小化 — 仅收集验证必需数据", "只收集来电号码和验证结果，不存储通话内容、录音或个人财务信息。", "GDPR"),
            ComplianceItem(3, "保留期限 — 来电记录不超过30天", "验证记录（号码+时间戳）保留期限不超过30天，法律另有规定除外。", "GDPR"),
            ComplianceItem(4, "数据主体权利 — 提供删除机制", "用户有权要求删除其来电验证记录，提供清晰的请求渠道和处理流程。", "GDPR"),
            ComplianceItem(5, "隐私声明披露 — 明确告知用户", "隐私政策中必须明确披露来电验证功能、数据处理方和数据保留期限。", "GDPR"),
            // CCPA
            ComplianceItem(6, "CCPA Section 1798.100 — 消费者知情权", "告知用户收集哪些数据、为什么收集、如何使用。提供不销售选项。", "CCPA"),
            ComplianceItem(7, "CCPA 数据安全义务 — 加密存储", "来电验证数据必须加密存储（AES-256），传输过程使用 TLS 1.3。", "CCPA"),
            ComplianceItem(8, "CCPA 违规通知 — 72小时报告", "发现数据泄露后72小时内向受影响用户和监管机构报告。", "CCPA"),
            // General
            ComplianceItem(9, "号码验证 — 防止枚举攻击", "实现速率限制，防止攻击者枚举大量号码探测银行关系。", "General"),
            ComplianceItem(10, "inbound-only 强制执行", "注册为 inbound-only 的号码必须禁用外呼功能，系统层强制执行。", "General"),
            ComplianceItem(11, "Accessibility Overlay 检测", "集成 Live Threat Detection，检测 SMS 转发和 Accessibility overlay 攻击。", "General"),
            ComplianceItem(12, "Intrusion Logging 配置", "启用取证日志记录，配置 Android Enterprise，支持安全事件审计。", "General")
        )
    }

    val uncheckedCount = complianceItems.size - state.complianceCheckedItems.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Red banner / 红色 Banner
        if (uncheckedCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SecurityColors.Error.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SecurityColors.ErrorLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$uncheckedCount 项合规要求尚未确认 / $uncheckedCount compliance items pending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecurityColors.ErrorLight
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "GDPR 合规 / GDPR Compliance",
                    style = MaterialTheme.typography.titleSmall,
                    color = SecurityColors.SecondaryLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
            }

            items(complianceItems.filter { it.category == "GDPR" }) { item ->
                ComplianceChecklistItem(
                    item = item,
                    isChecked = item.id in state.complianceCheckedItems,
                    onToggle = { onToggleItem(item.id) }
                )
            }

            item {
                Text(
                    text = "CCPA 合规 / CCPA Compliance",
                    style = MaterialTheme.typography.titleSmall,
                    color = SecurityColors.SecondaryLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
            }

            items(complianceItems.filter { it.category == "CCPA" }) { item ->
                ComplianceChecklistItem(
                    item = item,
                    isChecked = item.id in state.complianceCheckedItems,
                    onToggle = { onToggleItem(item.id) }
                )
            }

            item {
                Text(
                    text = "通用安全要求 / General Security",
                    style = MaterialTheme.typography.titleSmall,
                    color = SecurityColors.SecondaryLight,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
                )
            }

            items(complianceItems.filter { it.category == "General" }) { item ->
                ComplianceChecklistItem(
                    item = item,
                    isChecked = item.id in state.complianceCheckedItems,
                    onToggle = { onToggleItem(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ComplianceChecklistItem(
    item: ComplianceItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                SecurityColors.Primary.copy(alpha = 0.1f)
            else
                SecurityColors.Surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SecurityColors.PrimaryLight,
                    uncheckedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

// ============================================================
// Tab 4: Extensions Content / 扩展能力内容
// ============================================================

@Composable
private fun ExtensionsTabContent(
    state: VerifiedFinancialCallsState,
    onCopyCodeBlock: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Live Threat Detection / 实时威胁检测
        item {
            Text(
                text = "Live Threat Detection / 实时威胁检测",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Android Security 2026 新增两类实时威胁检测：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "SMS 转发检测 — 检测恶意 App 将用户收到的短信转发到攻击者",
                        "Accessibility Overlay 检测 — 检测覆盖层攻击，拦截用户输入的敏感信息"
                    ).forEach { threat ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SecurityColors.ErrorLight, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = threat, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            CodeBlock(
                id = "live_threat_detection",
                title = "LiveThreatDetectionService.kt",
                code = """\
// Live Threat Detection — 新增威胁检测类型
// SMS 转发检测 / Accessibility Overlay 检测

class LiveThreatDetectionService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                // 检测 SMS 转发 / Detect SMS forwarding
                if (isSmsForwarding(event)) {
                    reportThreat(ThreatType.SMS_FORWARDING)
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 检测 Overlay 攻击 / Detect overlay attack
                if (isOverlayAttack(event)) {
                    reportThreat(ThreatType.ACCESSIBILITY_OVERLAY)
                }
            }
        }
    }

    private fun isSmsForwarding(event: AccessibilityEvent): Boolean {
        return event.text.any { it.contains("验证码") }
    }

    private fun isOverlayAttack(event: AccessibilityEvent): Boolean {
        // 检测是否有其他窗口覆盖在敏感输入框上
        // Detect if another window overlays a sensitive input field
        return false // Implementation logic here
    }
}""".trimIndent(),
                description = "Live Threat Detection Service — 需声明 AccessibilityService 权限",
                isCopied = state.copiedBlockId == "live_threat_detection",
                onCopy = { code -> onCopyCodeBlock("live_threat_detection", code) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Dynamic Signal Monitoring / 动态信号监控
        item {
            Text(
                text = "Dynamic Signal Monitoring / 动态信号监控",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Android 17 新增实时监控能力",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dynamic Signal Monitoring 提供 Android 17 实时监控能力，涉及隐私合规与用户通知要求。用户可申诉误报，系统支持审计日志。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Intrusion Logging / 取证日志
        item {
            Text(
                text = "Intrusion Logging / 取证日志",
                style = MaterialTheme.typography.titleSmall,
                color = SecurityColors.PrimaryLight,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SecurityColors.Surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Android Enterprise 配置要求",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "启用企业设备管理（Device Owner / Profile Owner）",
                        "配置取证日志记录策略",
                        "设置日志保留期限和访问权限",
                        "确保日志的法律效力说明"
                    ).forEachIndexed { index, req ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SecurityColors.SecondaryLight, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = req, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Main Screen Composable / 主界面组合函数
// Main Screen Composable / 主界面组合函数
// ============================================================

/**
 * Verified Financial Calls Developer Toolkit main screen
 * Verified Financial Calls 开发者工具包主界面
 *
 * Features:
 * - 5-tab bottom navigation
 * - Code blocks with copy-to-clipboard
 * - Compliance checklist with persistent state
 * - FAB for quick navigation to any tab
 * - Dark theme code blocks (不受主题影响)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifiedFinancialCallsScreen(
    viewModel: VerifiedFinancialCallsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showQuickNavSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Collect effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VerifiedFinancialCallsEffect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is VerifiedFinancialCallsEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("code", effect.content))
                }
            }
        }
    }

    // Copy code block handler / 复制代码块处理
    val onCopyCodeBlock: (String, String) -> Unit = { blockId, content ->
        viewModel.sendIntent(VerifiedFinancialCallsIntent.CopyCodeBlock(blockId, content))
    }

    // Tab content renderer / Tab 内容渲染
    val tabContent: @Composable (Int) -> Unit = { tabIndex ->
        when (tabIndex) {
            0 -> OverviewTabContent(
                state = state,
                onToggleCard = { cardId ->
                    viewModel.sendIntent(VerifiedFinancialCallsIntent.ToggleCard(cardId))
                },
                onCopyCodeBlock = onCopyCodeBlock
            )
            1 -> VerificationApiTabContent(
                state = state,
                onCopyCodeBlock = onCopyCodeBlock
            )
            2 -> RegistrationTabContent(
                state = state,
                onCopyCodeBlock = onCopyCodeBlock
            )
            3 -> ComplianceTabContent(
                state = state,
                onToggleItem = { itemId ->
                    viewModel.sendIntent(VerifiedFinancialCallsIntent.ToggleComplianceItem(itemId))
                }
            )
            4 -> ExtensionsTabContent(
                state = state,
                onCopyCodeBlock = onCopyCodeBlock
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickNavSheet = true },
                containerColor = SecurityColors.PrimaryLight
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "快速导航",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SecurityColors.Background)
        ) {
            // Tab content / Tab 内容
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                tabContent(state.selectedTab)
            }

            // Bottom Navigation / 底部导航
            NavigationBar(
                containerColor = SecurityColors.Surface,
                contentColor = Color.White
            ) {
                VerifiedFinancialCallsTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Info
                                    1 -> Icons.Default.Security
                                    2 -> Icons.Default.Settings
                                    3 -> Icons.Default.FactCheck
                                    4 -> Icons.Default.Layers
                                    else -> Icons.Default.Info
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title, maxLines = 1) },
                        selected = state.selectedTab == index,
                        onClick = {
                            viewModel.sendIntent(VerifiedFinancialCallsIntent.SelectTab(index))
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SecurityColors.PrimaryLight,
                            selectedTextColor = SecurityColors.PrimaryLight,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = SecurityColors.Primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }

    // Quick Navigation Bottom Sheet / 快速导航 Bottom Sheet
    if (showQuickNavSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickNavSheet = false },
            sheetState = sheetState,
            containerColor = SecurityColors.Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "快速导航 / Quick Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                VerifiedFinancialCallsTab.entries.forEachIndexed { index, tab ->
                    val icon = when (index) {
                        0 -> Icons.Default.Info
                        1 -> Icons.Default.Security
                        2 -> Icons.Default.Settings
                        3 -> Icons.Default.FactCheck
                        4 -> Icons.Default.Layers
                        else -> Icons.Default.Info
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.sendIntent(VerifiedFinancialCallsIntent.SelectTab(index))
                                showQuickNavSheet = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.selectedTab == index)
                                SecurityColors.Primary.copy(alpha = 0.2f)
                            else
                                SecurityColors.Background
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (state.selectedTab == index)
                                    SecurityColors.PrimaryLight
                                else
                                    Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (state.selectedTab == index)
                                    SecurityColors.PrimaryLight
                                else
                                    Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
