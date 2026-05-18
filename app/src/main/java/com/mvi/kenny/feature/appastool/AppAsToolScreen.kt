package com.mvi.kenny.feature.appastool

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * AppAsToolScreen — App-as-Tool 开发者工具包主界面
 * App-as-Tool Developer Toolkit Main Screen
 * ============================================================
 *
 * PRD-250 | Android AppFunctions App-as-Tool 开发工具包
 *
 * Design: Terminal Console Style
 * - Deep dark background (#121212)
 * - Cyan accent (#00E5FF)
 * - Green accent (#76FF03)
 * - Red error (#FF5252)
 * - JetBrains Mono for code / 等宽字体显示代码
 *
 * 5 Tab Bottom Navigation:
 * - Tab 1: Manifest Declaration Guide / Manifest 声明指南
 * - Tab 2: Function Schema Creation Tool / Schema 创作工具
 * - Tab 3: Intent Handling Skeleton / Intent 处理骨架
 * - Tab 4: Permission Delegation Integration / 权限委托集成
 * - Tab 5: CI Validation Plugin / CI 验证插件
 */

// ============================================================
// Terminal Style Color Palette / Terminal 风格配色
// ============================================================

private object TerminalColors {
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2D2D2D)
    val Primary = Color(0xFF00E5FF)       // Cyan / 青色
    val Secondary = Color(0xFF76FF03)     // Green / 绿色
    val Error = Color(0xFFFF5252)          // Red / 红色
    val Warning = Color(0xFFFFD740)         // Amber / 琥珀色
    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFF9E9E9E)
    val CodeBackground = Color(0xFF0D1117)
    val Divider = Color(0xFF3D3D3D)
}

private val tabIcons = listOf(
    Icons.Default.Build,
    Icons.Default.Description,
    Icons.Default.SettingsEthernet,
    Icons.Default.Security,
    Icons.Default.Verified
)

// ============================================================
// Main Screen / 主界面
// ============================================================

/**
 * App-as-Tool Developer Toolkit Screen
 * App-as-Tool 开发者工具包主界面
 *
 * @param viewModel AppAsToolViewModel instance
 * @param onNavigateBack Navigation callback / 导航回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAsToolScreen(
    viewModel: AppAsToolViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // File export launcher / 文件导出启动器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(state.generateSchemaPreview().toByteArray())
                }
            } catch (e: Exception) {
                // Silently fail / 静默失败
            }
        }
    }

    // Collect side effects / 收集副作用
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AppAsToolEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AppAsToolEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("AppAsTool", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is AppAsToolEffect.ExportSchemaFile -> {
                    exportLauncher.launch("app_function_schema.json")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${AppAsToolTab.entries[state.selectedTab].title} | App as Tool",
                        color = TerminalColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerminalColors.Surface
                ),
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", color = TerminalColors.Primary)
                    }
                }
            )
        },
        bottomBar = {
            TerminalNavigationBar(
                selectedIndex = state.selectedTab,
                onTabSelected = { viewModel.sendIntent(AppAsToolIntent.SelectTab(it)) }
            )
        },
        containerColor = TerminalColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.selectedTab) {
                0 -> ManifestTab(state, viewModel)
                1 -> SchemaTab(state, viewModel)
                2 -> IntentTab(state, viewModel)
                3 -> PermissionTab(state, viewModel)
                4 -> CITab(state, viewModel)
            }
        }
    }
}

// ============================================================
// Bottom Navigation Bar / 底部导航栏
// ============================================================

@Composable
private fun TerminalNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = TerminalColors.Surface,
        contentColor = TerminalColors.Primary
    ) {
        AppAsToolTab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tabIcons[index],
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TerminalColors.Primary,
                    selectedTextColor = TerminalColors.Primary,
                    unselectedIconColor = TerminalColors.TextSecondary,
                    unselectedTextColor = TerminalColors.TextSecondary,
                    indicatorColor = TerminalColors.Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============================================================
// Tab 1: Manifest Declaration Guide / Manifest 声明指南
// ============================================================

@Composable
private fun ManifestTab(state: AppAsToolState, viewModel: AppAsToolViewModel) {
    val manifestCards = rememberManifestCards()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Manifest 声明指南",
                color = TerminalColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Android Intelligence Hub 接入第一步：声明 App Function",
                color = TerminalColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = TerminalColors.Divider)
        }

        itemsIndexed(manifestCards) { index, card ->
            ExpandableManifestCard(
                card = card,
                isExpanded = index in state.expandedCards,
                onToggle = { viewModel.sendIntent(AppAsToolIntent.ToggleCard(index)) },
                onCopy = { viewModel.sendIntent(AppAsToolIntent.CopyToClipboard(card.codeContent)) }
            )
        }
    }
}

@Composable
private fun rememberManifestCards(): List<ManifestCard> = listOf(
    ManifestCard(
        id = 0,
        title = "<meta-data AI_FUNCTION> 声明",
        description = "在 AndroidManifest.xml 中声明 App Function，使 Gemini 能发现此 App",
        codeContent = """
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application>
        <!-- App Function 核心声明 / AI_FUNCTION metadata -->
        <meta-data
            android:name="android.app.AI_FUNCTION"
            android:value="MySearchFunction" />

        <!-- AI-friendly description / AI 友好的功能描述 -->
        <meta-data
            android:name="android.app.AI_FUNCTION.searchHotels"
            android:resource="@xml/function_schema_search_hotels" />
    </application>
</manifest>
        """.trimIndent(),
        language = "xml"
    ),
    ManifestCard(
        id = 1,
        title = "AI-Friendly Description 写法",
        description = "用自然语言描述 Function，让 Gemini 理解其用途和调用方式",
        codeContent = """
<!-- res/xml/function_schema_search_hotels.xml -->
<ai-function
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:name="searchHotels"
    android:description="
        Search for hotels near a given location.
        Parameters:
          - location: String (required) — City or address
          - checkIn: String (required) — Check-in date (YYYY-MM-DD)
          - checkOut: String (required) — Check-out date (YYYY-MM-DD)
          - guests: Int (optional) — Number of guests, default 1
        Returns:
          - List of hotel objects with name, price, rating
        Example: 'Find hotels in Tokyo from May 20 to May 25'
    "
    android:category="TRAVEL"
    android:minAndroidVersion="35" />
        """.trimIndent(),
        language = "xml"
    ),
    ManifestCard(
        id = 2,
        title = "Intent Filter 配置",
        description = "声明 App 能接收的 Intent 类型，使 Gemini 能正确路由调用",
        codeContent = """
<!-- AndroidManifest.xml -->
<activity
    android:name=".HotelSearchActivity"
    android:exported="true">

    <intent-filter>
        <!-- 声明 App Function Intent / App Function Intent filter -->
        <action android:name="android.app.action.INVOKE_AI_FUNCTION" />
        <category android:name="android.app.category.AI_FUNCTION" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>

    <!-- 指定支持的 function names / Supported function names -->
    <meta-data
        android:name="android.app.AI_FUNCTION_NAMES"
        android:value="searchHotels,bookHotel,cancelBooking" />
</activity>
        """.trimIndent(),
        language = "xml"
    ),
    ManifestCard(
        id = 3,
        title = "权限声明",
        description = "声明 App Function 所需的权限，确保运行时授权流程正确",
        codeContent = """
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- App as Tool 权限 / App as Tool permission -->
    <uses-permission android:name="android.permission.INVOKE_AI_FUNCTION" />

    <!-- 网络权限（如果 Function 涉及 API 调用） -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- 位置权限（如果 Function 需要位置信息） -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- 权限委托给 Gemini / Permission delegation -->
    <uses-permission android:name="android.permission.DELEGATE_AI_FUNCTION" />

</manifest>
        """.trimIndent(),
        language = "xml"
    )
)

@Composable
private fun ExpandableManifestCard(
    card: ManifestCard,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = TerminalColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        color = TerminalColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = card.description,
                        color = TerminalColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TerminalColors.TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalColors.CodeBackground)
                            .border(1.dp, TerminalColors.Divider, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = card.codeContent,
                            color = TerminalColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCopy,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = TerminalColors.Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 2: Function Schema Creation Tool / Schema 创作工具
// ============================================================

@Composable
private fun SchemaTab(state: AppAsToolState, viewModel: AppAsToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "// Function Schema 创作工具",
            color = TerminalColors.Primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "AI-friendly Function Description 模板化创作工具",
            color = TerminalColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )

        HorizontalDivider(color = TerminalColors.Divider)

        Text(
            text = "选择模板 / Select Template:",
            color = TerminalColors.TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SchemaTemplate.entries.forEach { template ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.sendIntent(AppAsToolIntent.SelectSchemaTemplate(template)) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.schemaTemplate == template,
                        onClick = { viewModel.sendIntent(AppAsToolIntent.SelectSchemaTemplate(template)) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = TerminalColors.Primary
                        )
                    )
                    Text(
                        text = template.displayName,
                        color = TerminalColors.TextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }

        HorizontalDivider(color = TerminalColors.Divider)

        TerminalTextField(
            label = "Schema Name",
            value = state.schemaName,
            onValueChange = { viewModel.sendIntent(AppAsToolIntent.UpdateSchemaName(it)) },
            placeholder = "e.g., searchHotels"
        )
        TerminalTextField(
            label = "Description",
            value = state.schemaDescription,
            onValueChange = { viewModel.sendIntent(AppAsToolIntent.UpdateSchemaDescription(it)) },
            placeholder = "Describe what this function does for Gemini..."
        )
        TerminalTextField(
            label = "Parameters (JSON)",
            value = state.schemaParameters,
            onValueChange = { viewModel.sendIntent(AppAsToolIntent.UpdateSchemaParameters(it)) },
            placeholder = """[{"name":"param1","type":"String","required":true}]"""
        )
        TerminalTextField(
            label = "Returns (JSON)",
            value = state.schemaReturns,
            onValueChange = { viewModel.sendIntent(AppAsToolIntent.UpdateSchemaReturns(it)) },
            placeholder = """{"type":"List","items":{"name":"String","price":"Int"}}"""
        )

        HorizontalDivider(color = TerminalColors.Divider)

        Text(
            text = "// Live Preview",
            color = TerminalColors.Secondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalColors.CodeBackground)
                .border(1.dp, TerminalColors.Secondary, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = state.generateSchemaPreview(),
                color = TerminalColors.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.sendIntent(AppAsToolIntent.CopyToClipboard(state.generateSchemaPreview())) },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalColors.Primary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("复制 / Copy", fontFamily = FontFamily.Monospace)
            }
            Button(
                onClick = { viewModel.sendIntent(AppAsToolIntent.ExportSchema(state.generateSchemaPreview())) },
                colors = ButtonDefaults.buttonColors(containerColor = TerminalColors.Secondary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("导出 / Export", fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TerminalTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column {
        Text(
            text = "// $label",
            color = TerminalColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontFamily = FontFamily.Monospace) },
            placeholder = { Text(placeholder, fontFamily = FontFamily.Monospace, color = TerminalColors.TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TerminalColors.TextPrimary,
                unfocusedTextColor = TerminalColors.TextPrimary,
                focusedBorderColor = TerminalColors.Primary,
                unfocusedBorderColor = TerminalColors.Divider,
                cursorColor = TerminalColors.Primary
            ),
            singleLine = false,
            minLines = 2
        )
    }
}

// ============================================================
// Tab 3: Intent Handling Skeleton / Intent 处理骨架
// ============================================================

@Composable
private fun IntentTab(state: AppAsToolState, viewModel: AppAsToolViewModel) {
    val intentSections = rememberIntentSections()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// Intent 处理骨架",
                color = TerminalColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Gemini 调用 App Function 时，App 收到的 Intent 处理流程",
                color = TerminalColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = TerminalColors.Divider)
        }

        itemsIndexed(intentSections) { index, section ->
            ExpandableIntentSection(
                section = section,
                isExpanded = index in state.expandedSections,
                onToggle = { viewModel.sendIntent(AppAsToolIntent.ToggleSection(index)) },
                onCopy = { viewModel.sendIntent(AppAsToolIntent.CopyToClipboard(section.codeContent)) }
            )
        }
    }
}

@Composable
private fun rememberIntentSections(): List<IntentSection> = listOf(
    IntentSection(
        id = 0,
        title = "Step 1: Activity 接收 Intent",
        explanation = "在 Activity 的 onCreate() 或 onNewIntent() 中接收 Gemini 的调用请求。Intent 包含 function name 和参数。\n\n/Gemini sends an Intent with action=INVOKE_AI_FUNCTION, extra contains functionName and parameters./",
        codeContent = """
class HotelSearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 处理 App Function 调用 / Handle App Function invocation
        if (intent?.action == "android.app.action.INVOKE_AI_FUNCTION") {
            handleAIFunctionInvoke(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 处理多轮调用 / Handle multi-turn invocations
        if (intent?.action == "android.app.action.INVOKE_AI_FUNCTION") {
            handleAIFunctionInvoke(intent)
        }
    }
}
        """.trimIndent(),
        templateVars = listOf("INVOKE_AI_FUNCTION", "functionName", "parameters")
    ),
    IntentSection(
        id = 1,
        title = "Step 2: 解析 Intent 参数",
        explanation = "从 Intent 中提取 function name、参数和调用上下文。\n\n/Extract function name, parameters, and invocation context from the Intent./",
        codeContent = """
private fun handleAIFunctionInvoke(intent: Intent) {
    // 获取 function 名称 / Get function name
    val functionName = intent.getStringExtra(
        "android.app.AI_FUNCTION_NAME"
    ) ?: return

    // 获取参数（JSON 格式）/ Get parameters (JSON format)
    val parametersJson = intent.getStringExtra(
        "android.app.AI_FUNCTION_PARAMS"
    ) ?: "{}"

    // 解析参数 / Parse parameters
    val parameters = try {
        Json.decodeFromString<Map<String, String>>(parametersJson)
    } catch (e: Exception) {
        emptyMap()
    }

    // 获取调用上下文 / Get invocation context
    val userIntent = intent.getStringExtra(
        "android.app.AI_USER_INTENT"
    )

    // 分发到对应的 Function handler / Dispatch to corresponding handler
    when (${'$'}functionName) {
        "searchHotels" -> searchHotels(parameters)
        "bookHotel" -> bookHotel(parameters)
        else -> sendErrorResponse("Unknown function: ${'$'}functionName")
    }
}
        """.trimIndent(),
        templateVars = listOf("AI_FUNCTION_NAME", "AI_FUNCTION_PARAMS", "parameters")
    ),
    IntentSection(
        id = 2,
        title = "Step 3: 执行 Function 并返回结果",
        explanation = "执行业务逻辑后，通过 setResult() 返回结果给 Gemini。\n\n/After executing business logic, return result to Gemini via setResult()./",
        codeContent = """
private fun searchHotels(parameters: Map<String, String>) {
    val location = parameters["location"] ?: return
    val checkIn = parameters["checkIn"]
    val checkOut = parameters["checkOut"]

    // TODO: 调用酒店搜索 API / Call hotel search API
    val results = listOf(
        mapOf(
            "name" to "Tokyo Grand Hotel",
            "price" to 15000,
            "rating" to 4.5
        )
    )

    // 返回结果给 Gemini / Return result to Gemini
    val resultJson = Json.encodeToString(results)
    val resultIntent = Intent().apply {
        putExtra("android.app.AI_FUNCTION_RESULT", resultJson)
        putExtra("android.app.AI_FUNCTION_STATUS", "SUCCESS")
    }
    setResult(Activity.RESULT_OK, resultIntent)
    finish()
}

private fun sendErrorResponse(errorMessage: String) {
    val errorIntent = Intent().apply {
        putExtra("android.app.AI_FUNCTION_RESULT", "{}")
        putExtra("android.app.AI_FUNCTION_STATUS", "ERROR")
        putExtra("android.app.AI_FUNCTION_ERROR", errorMessage)
    }
    setResult(Activity.RESULT_OK, errorIntent)
    finish()
}
        """.trimIndent(),
        templateVars = listOf("AI_FUNCTION_RESULT", "AI_FUNCTION_STATUS", "RESULT_OK")
    )
)

@Composable
private fun ExpandableIntentSection(
    section: IntentSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = TerminalColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    color = TerminalColors.Warning,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TerminalColors.TextSecondary
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = section.explanation,
                        color = TerminalColors.TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(TerminalColors.CodeBackground)
                            .border(1.dp, TerminalColors.Divider, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = section.codeContent,
                            color = TerminalColors.TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCopy,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = TerminalColors.Primary
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Code", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Tab 4: Permission Delegation Integration / 权限委托集成
// ============================================================

@Composable
private fun PermissionTab(state: AppAsToolState, viewModel: AppAsToolViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "// Permission Delegation 集成",
            color = TerminalColors.Primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "App 授权 Gemini 代表用户执行操作，无需重复授权",
            color = TerminalColors.TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )

        HorizontalDivider(color = TerminalColors.Divider)

        Text(
            text = "// 权限委托流程 / Permission Delegation Flow",
            color = TerminalColors.Warning,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalColors.CodeBackground)
                .border(1.dp, TerminalColors.Divider, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(
                text = """
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   User      │────▶│  Gemini     │────▶│  App        │────▶│  Approval   │
│  (speaks)   │     │  (orchest)  │     │  Function    │     │  UI (optional)│
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
                           │                    │
                           │                    │
                    Intent with DELEGATE     Check Permission
                    _PERMISSION flag          Delegation Token
                """.trimIndent(),
                color = TerminalColors.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 20.sp
            )
        }

        HorizontalDivider(color = TerminalColors.Divider)

        Text(
            text = "// Approval UI Compose 实现 / Approval UI Compose Template",
            color = TerminalColors.Secondary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalColors.CodeBackground)
                .border(1.dp, TerminalColors.Secondary, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = """
@Composable
fun ApprovalDialog(
    functionName: String,
    requestedPermission: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFFFD740)
            )
        },
        title = {
            Text(
                "Permission Request",
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFD740)
            )
        },
        text = {
            Column {
                Text(
                    "Gemini is requesting access to:",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Function: ${'$'}functionName",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    "• Permission: ${'$'}requestedPermission",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E5FF)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF76FF03)
                )
            ) {
                Text("Allow", fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            Button(
                onClick = onDeny,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252)
                )
            ) {
                Text("Deny", fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = Color(0xFF1E1E1Color(0xFF1E1E1E)
    )
}
                """.trimIndent(),
                color = TerminalColors.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        // Manifest config / Manifest 权限配置代码
        Text(
            text = "// Manifest 权限声明 / Manifest Permission Config",
            color = TerminalColors.Primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalColors.CodeBackground)
                .border(1.dp, TerminalColors.Divider, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = """
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- 权限委托声明 / Permission delegation declaration -->
    <uses-permission
        android:name="android.permission.DELEGATE_AI_FUNCTION" />

    <!-- 声明 App Function 需要用户确认的操作 -->
    <uses-permission
        android:name="android.permission.CONFIRM_AI_FUNCTION" />

</manifest>
                """.trimIndent(),
                color = TerminalColors.TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ============================================================
// Tab 5: CI Validation Plugin / CI 验证插件
// ============================================================

@Composable
private fun CITab(state: AppAsToolState, viewModel: AppAsToolViewModel) {
    val ciRules = rememberCIRules()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "// CI 验证插件",
                color = TerminalColors.Primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Gradle 插件：验证 Manifest 配置 + Function Schema 格式",
                color = TerminalColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = TerminalColors.Divider)
        }

        // Plugin setup / 插件配置
        item {
            Text(
                text = "// Gradle Plugin Setup",
                color = TerminalColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "// 添加插件到 build.gradle.kts / Add plugin to build.gradle.kts",
                color = TerminalColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalColors.CodeBackground)
                    .border(1.dp, TerminalColors.Secondary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = """
// build.gradle.kts (Project level)
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    // App as Tool CI validation plugin
    id("com.appastool.validation") version "1.0.0" apply false
}

// build.gradle.kts (Module level)
plugins {
    id("com.appastool.validation")
}

appAsToolValidation {
    failOnError.set(true)
    minSdkVersion.set(35)
}
                    """.trimIndent(),
                    color = TerminalColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(
                            AppAsToolIntent.CopyToClipboard(
                                """
plugins {
    id("com.appastool.validation")
}

appAsToolValidation {
    failOnError.set(true)
    minSdkVersion.set(35)
}
                                """.trimIndent()
                            )
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = TerminalColors.Primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Config", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }

        item { HorizontalDivider(color = TerminalColors.Divider) }

        item {
            Text(
                text = "// Validation Rules / 验证规则",
                color = TerminalColors.Warning,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        itemsIndexed(ciRules) { _, rule ->
            CIRuleCard(
                rule = rule,
                onCopy = { viewModel.sendIntent(AppAsToolIntent.CopyToClipboard(rule.description)) }
            )
        }

        item { HorizontalDivider(color = TerminalColors.Divider) }

        item {
            Text(
                text = "// CLI Output Example / CLI 输出示例",
                color = TerminalColors.Secondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalColors.CodeBackground)
                    .border(1.dp, TerminalColors.Secondary, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = """
$ ./gradlew validateAppAsTool

> Task :app:validateAppAsTool

[AppAsTool] Validation Report
  Manifest checks: 4 passed, 0 failed
  Schema format: 2 passed, 0 failed
  Intent filter: 1 passed, 0 failed
  Permissions: 3 passed, 0 failed

Details:
  [PASS] AI_FUNCTION meta-data declared
  [PASS] Intent filter for INVOKE_AI_FUNCTION present
  [PASS] Function schema format valid (searchHotels)
  [PASS] Permission DELEGATE_AI_FUNCTION declared

BUILD SUCCESSFUL
                    """.trimIndent(),
                    color = TerminalColors.TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun rememberCIRules(): List<CIValidationRule> = listOf(
    CIValidationRule(
        id = 0,
        name = "AI_FUNCTION metadata declared",
        description = "每个 App Function 必须在 AndroidManifest.xml 中声明 <meta-data android:name=\"android.app.AI_FUNCTION\" />",
        severity = "error"
    ),
    CIValidationRule(
        id = 1,
        name = "Intent filter for INVOKE_AI_FUNCTION",
        description = "Activity 必须包含 action=INVOKE_AI_FUNCTION 的 intent-filter，否则 Gemini 无法调用",
        severity = "error"
    ),
    CIValidationRule(
        id = 2,
        name = "Function schema format valid",
        description = "AI-Friendly Description 必须符合 Schema 格式要求：包含 name、description、parameters、returns 字段",
        severity = "error"
    ),
    CIValidationRule(
        id = 3,
        name = "Permission DELEGATE_AI_FUNCTION declared",
        description = "如果 App 支持 Permission Delegation，必须声明 android.permission.DELEGATE_AI_FUNCTION",
        severity = "warning"
    ),
    CIValidationRule(
        id = 4,
        name = "minSdkVersion >= 35",
        description = "App as Tool 基于 Android 15+ (API 35) 的 Android Intelligence Hub，minSdkVersion 必须 >= 35",
        severity = "error"
    ),
    CIValidationRule(
        id = 5,
        name = "Activity exported=true for AI",
        description = "处理 App Function 的 Activity 必须 android:exported=true，否则 Hub 无法启动该 Activity",
        severity = "error"
    )
)

@Composable
private fun CIRuleCard(
    rule: CIValidationRule,
    onCopy: () -> Unit
) {
    val severityColor = when (rule.severity) {
        "error" -> TerminalColors.Error
        "warning" -> TerminalColors.Warning
        else -> TerminalColors.TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TerminalColors.Surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(severityColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rule.name,
                        color = severityColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = rule.severity.uppercase(),
                    color = severityColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(
                            severityColor.copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rule.description,
                color = TerminalColors.TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
