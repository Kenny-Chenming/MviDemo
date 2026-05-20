package com.mvi.kenny.feature.migrationassistant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * MigrationToolkitScreen — Android Studio Migration Assistant 移植工具包主界面
 * MigrationToolkitScreen — Android Studio Migration Assistant Dev Toolkit Main Screen
 * ============================================================
 *
 * PRD-268 | Android Studio iOS/React Native/Web Migration Assistant 移植工具包
 * Ref: memory/agency/designs/PRD-268-Android-Studio-iOS-RN-Web-Migration-Assistant移植工具包.md
 *
 * Page Structure:
 * ┌──────────────────────────────────────────────────────────┐
 * │ TopAppBar: "Migration Assistant" + Search Icon             │
 * ├──────────────────────────────────────────────────────────┤
 * │ Hero Banner: "从现有代码库，一键迁移到原生 Android"           │
 * ├──────────────────────────────────────────────────────────┤
 * │ Platform Tabs: [iOS] [React Native] [Web] [通用]           │
 * ├──────────────────────────────────────────────────────────┤
 * │                                                          │
 * │  Platform-specific content (scrollable)                   │
 * │                                                          │
 * └──────────────────────────────────────────────────────────┘
 */

// ============================================================
// Constants — Platform Colors
// ============================================================

private val IosColor = Color(0xFFA3A3A3)
private val RnColor = Color(0xFF61DAFB)
private val WebColor = Color(0xFFF7DF1E)
private val SuccessColor = Color(0xFF34A853)
private val WarningColor = Color(0xFFFBBC04)
private val ErrorColor = Color(0xFFEA4335)
private val PurplePrimary = Color(0xFF7E57C2)

// ============================================================
// Main Entry Point / 主入口
// ============================================================

/**
 * MigrationToolkitScreen — Main composable entry point
 * 移植工具包主界面
 *
 * @param viewModel ViewModel instance / ViewModel 实例
 * @param onUpdateTopBar TopBar configuration callback / TopBar 配置回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationToolkitScreen(
    viewModel: MigrationToolkitViewModel,
    onUpdateTopBar: (TopBarConfig) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Update TopBar configuration on state changes
    // 状态变化时更新 TopBar 配置
    LaunchedEffect(state.activePlatform) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Migration Assistant",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Search,
                        contentDescription = "搜索",
                        onClick = { }
                    )
                )
            )
        )
    }

    // Collect effects for one-time events
    // 收集副作用（一次性事件）
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MigrationToolkitEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is MigrationToolkitEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(effect.label, effect.text)
                    clipboard.setPrimaryClip(clip)
                }
                is MigrationToolkitEffect.ScrollToCase -> { }
                is MigrationToolkitEffect.ShowQualityReportReady -> {
                    Toast.makeText(context, "质量报告已生成 (${effect.itemCount} 项已检查)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hero Banner / 英雄横幅
            HeroBanner()

            // Platform Tabs / 平台 Tab
            PlatformTabRow(
                selectedPlatform = state.activePlatform,
                onPlatformSelected = { viewModel.sendIntent(MigrationToolkitIntent.SelectPlatform(it)) }
            )

            // Content based on selected platform / 根据选中平台显示内容
            when (state.activePlatform) {
                Platform.IOS -> IosContent(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                Platform.REACT_NATIVE -> ReactNativeContent(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
                Platform.WEB -> WebContent(
                    state = state,
                    onIntent = viewModel::sendIntent
                )
            }
        }
    }
}

// ============================================================
// Hero Banner / 英雄横幅
// ============================================================

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(PurplePrimary, PurplePrimary.copy(alpha = 0.7f))
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Android Studio Migration Assistant",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "从现有代码库，一键迁移到原生 Android",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlatformChip(label = "iOS → Android", color = IosColor)
                PlatformChip(label = "RN → Android", color = RnColor)
                PlatformChip(label = "Web → Android", color = WebColor)
            }
        }
    }
}

@Composable
private fun PlatformChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

// ============================================================
// Platform Tab Row / 平台 Tab 行
// ============================================================

@Composable
private fun PlatformTabRow(
    selectedPlatform: Platform,
    onPlatformSelected: (Platform) -> Unit
) {
    val platforms = Platform.entries
    val selectedIndex = platforms.indexOf(selectedPlatform)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = PurplePrimary
            )
        }
    ) {
        platforms.forEachIndexed { index, platform ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onPlatformSelected(platform) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (platform) {
                                Platform.IOS -> Icons.Default.PhoneIphone
                                Platform.REACT_NATIVE -> Icons.Default.Refresh
                                Platform.WEB -> Icons.Default.Web
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (index == selectedIndex) PurplePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = platform.displayName,
                            color = if (index == selectedIndex) PurplePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }
    }
}

// ============================================================
// iOS → Android Content / iOS 内容区
// ============================================================

@Composable
private fun IosContent(
    state: MigrationToolkitState,
    onIntent: (MigrationToolkitIntent) -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("上手指南", "API映射", "踩坑案例", "质量验证")

    Column {
        // Sub-section tabs / 子模块 Tab
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = { }
        ) {
            subTabs.forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    label = { Text(title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary.copy(alpha = 0.1f),
                        selectedLabelColor = PurplePrimary
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Sub-section content / 子模块内容
        when (selectedSubTab) {
            0 -> IosGettingStartedSection()
            1 -> ApiMappingSection(
                mappings = state.getFilteredMappings(),
                searchQuery = state.searchQuery,
                onSearch = { onIntent(MigrationToolkitIntent.SearchMappings(it)) },
                expandedMappingId = state.expandedMappingId,
                onToggleMapping = { onIntent(MigrationToolkitIntent.ToggleMappingExpanded(it)) },
                onCopyMapping = { onIntent(MigrationToolkitIntent.CopyMappingCsv(it)) }
            )
            2 -> PitfallCasesSection(
                cases = state.pitfallCases,
                expandedCaseId = state.expandedCaseId,
                onToggleCase = { onIntent(MigrationToolkitIntent.ToggleCaseExpanded(it)) }
            )
            3 -> QualityVerificationSection(
                checklist = state.qualityChecklist,
                onToggleItem = { onIntent(MigrationToolkitIntent.ToggleQualityCheck(it)) }
            )
        }
    }
}

// ============================================================
// React Native Content / React Native 内容区
// ============================================================

@Composable
private fun ReactNativeContent(
    state: MigrationToolkitState,
    onIntent: (MigrationToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Decision banner / 决策横幅
        item {
            DecisionBanner(
                title = "React Native → Android",
                description = "评估是否适合使用 Migration Assistant",
                decisionPoints = listOf(
                    "RN 版本 < 0.70？建议先升级",
                    "使用了大量原生模块（Native Modules）？手动移植",
                    "UI 逻辑与平台无关？Migration Assistant 效果好",
                    "状态管理 Redux/MobX？需要架构重构"
                )
            )
        }

        // Component mapping section / 组件映射
        item {
            SectionHeader(
                title = "RN 组件 → Android Compose 映射",
                icon = Icons.Default.Layers
            )
        }

        // RN component mappings / RN 组件映射卡片
        val rnMappings = state.apiMappings.filter {
            it.id.startsWith("rn-")
        }.take(10)

        items(rnMappings.chunked(2)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { mapping ->
                    ApiMappingCard(
                        mapping = mapping,
                        isExpanded = state.expandedMappingId == mapping.id,
                        onToggle = { onIntent(MigrationToolkitIntent.ToggleMappingExpanded(mapping.id)) },
                        onCopy = { onIntent(MigrationToolkitIntent.CopyMappingCsv(mapping)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // State migration section / 状态迁移
        item {
            SectionHeader(
                title = "状态管理迁移",
                icon = Icons.Default.Refresh
            )
        }

        item {
            InfoCard(
                title = "Redux / MobX → ViewModel",
                items = listOf(
                    "Redux store → 单个 ViewModel + StateFlow",
                    "useSelector → 观察 StateFlow 暴露的 State",
                    "useDispatch → ViewModelScope.launch { }",
                    "MobX observable → MutableStateFlow",
                    "action → suspend function",
                    "computed → val derived from StateFlow"
                )
            )
        }

        // Library recommendations / 库推荐
        item {
            SectionHeader(
                title = "等价 Android 库推荐",
                icon = Icons.Default.Download
            )
        }

        item {
            LibraryRecommendationCard(
                library = "Navigation",
                rnLib = "react-native-navigation / @react-navigation",
                androidLib = "Jetpack Navigation (Navigation Compose)",
                notes = "RN 导航库众多，建议统一迁移到 Navigation Compose"
            )
        }

        item {
            LibraryRecommendationCard(
                library = "HTTP Client",
                rnLib = "axios / fetch",
                androidLib = "Retrofit + OkHttp / Ktor",
                notes = "Retrofit 对 REST API 支持最好，Ktor 更现代"
            )
        }

        item {
            LibraryRecommendationCard(
                library = "Storage",
                rnLib = "@react-native-async-storage/async-storage",
                androidLib = "DataStore (Preferences) / Room",
                notes = "简单 KV 存储用 DataStore，结构化数据用 Room"
            )
        }
    }
}

// ============================================================
// Web Content / Web 内容区
// ============================================================

@Composable
private fun WebContent(
    state: MigrationToolkitState,
    onIntent: (MigrationToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Decision banner / 决策横幅
        item {
            DecisionBanner(
                title = "Web → Android",
                description = "评估迁移方案",
                decisionPoints = listOf(
                    "PWA 应用？推荐 Migration Assistant",
                    "纯内容展示型 Web App？效果最好",
                    "复杂交互 Web App？建议保留 Web App",
                    "需要原生功能（PWA 不足）？混合方案"
                )
            )
        }

        // PWA Conversion / PWA 转换
        item {
            SectionHeader(title = "PWA 转换清单", icon = Icons.Default.Web)
        }

        item {
            ChecklistCard(
                title = "PWA → 原生 Android 检查项",
                items = listOf(
                    "manifest.json → AndroidManifest.xml 配置",
                    "Service Worker → WorkManager 实现",
                    "Web 推送通知 → Firebase Cloud Messaging",
                    "离线缓存策略 → Room/DataStore 持久化",
                    "Camera/Microphone → CameraX/MediaRecorder",
                    "Geolocation → Google Play Services Location"
                )
            )
        }

        // Responsive adaptation / 响应式适配
        item {
            SectionHeader(title = "响应式 Web 适配", icon = Icons.Default.Share)
        }

        item {
            InfoCard(
                title = "CSS Media Query → WindowSizeClass",
                items = listOf(
                    "@media (max-width: 600px) → WindowWidthSizeClass.Compact",
                    "@media (600-840px) → WindowWidthSizeClass.Medium",
                    "@media (min-width: 840px) → WindowWidthSizeClass.Expanded",
                    "使用 Accompanist Adaptive 进行响应式布局"
                )
            )
        }

        // Native enhancement / 原生增强
        item {
            SectionHeader(title = "原生功能增强", icon = Icons.Default.Shield)
        }

        item {
            FeatureEnhancementCard(
                feature = "Camera / 相机",
                webApi = "navigator.mediaDevices.getUserMedia()",
                androidApi = "CameraX",
                enhancement = "更高画质、更快对焦、HDR 支持"
            )
        }

        item {
            FeatureEnhancementCard(
                feature = "Location / 位置",
                webApi = "navigator.geolocation",
                androidApi = "Google Play Services Location",
                enhancement = "后台定位、地理围栏、 fused location"
            )
        }

        item {
            FeatureEnhancementCard(
                feature = "Push Notifications / 推送",
                webApi = "Web Push API (PushManager)",
                androidApi = "Firebase Cloud Messaging",
                enhancement = "系统级推送、更长送达率、细分用户群"
            )
        }
    }
}

// ============================================================
// iOS Getting Started Section / iOS 上手指南
// ============================================================

@Composable
private fun IosGettingStartedSection() {
    val steps = listOf(
        StepInfo("步骤 1", "准备 iOS 代码库", "确保代码在 Git 仓库中，且为最新稳定版本。推荐 Xcode 15+"),
        StepInfo("步骤 2", "在 Android Studio 启动 Migration Assistant", "File → New → Import Project from iOS/Xcode，或使用菜单 Search → Migration Assistant"),
        StepInfo("步骤 3", "选择迁移范围", "全量迁移或选择性迁移（只迁移部分模块/功能）。建议先全量了解，再按需处理"),
        StepInfo("步骤 4", "审阅自动生成的 Kotlin 代码", "Migration Assistant 生成代码后，逐个审阅。重点关注：API 映射标记为「手动」的条目"),
        StepInfo("步骤 5", "手动补充未映射 API", "对于 HealthKit/CoreLocation/ARKit 等无自动映射的 API，按踩坑案例集处理"),
        StepInfo("步骤 6", "在 Android Studio 打开并调试", "运行 ./gradlew assembleDebug，修复编译错误，在真机/模拟器上调试")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(steps) { index, step ->
            StepCard(step = step, stepNumber = index + 1)
        }
    }
}

data class StepInfo(val label: String, val title: String, val description: String)

@Composable
private fun StepCard(step: StepInfo, stepNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step number badge / 步骤编号徽章
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PurplePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ============================================================
// API Mapping Section / API 映射区
// ============================================================

@Composable
private fun ApiMappingSection(
    mappings: List<ApiMapping>,
    searchQuery: String,
    onSearch: (String) -> Unit,
    expandedMappingId: String?,
    onToggleMapping: (String) -> Unit,
    onCopyMapping: (ApiMapping) -> Unit
) {
    Column {
        // Search bar / 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜索 API 映射 (iOS API / Kotlin / Swift...)") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurplePrimary,
                cursorColor = PurplePrimary
            )
        )

        // Copy all button / 复制全部按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制全部 (${mappings.size})")
            }
        }

        // Mapping list / 映射列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            items(mappings) { mapping ->
                ApiMappingCard(
                    mapping = mapping,
                    isExpanded = expandedMappingId == mapping.id,
                    onToggle = { onToggleMapping(mapping.id) },
                    onCopy = { onCopyMapping(mapping) }
                )
            }
        }
    }
}

@Composable
private fun ApiMappingCard(
    mapping: ApiMapping,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                PurplePrimary.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp)
        ) {
            // Header / 表头
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Source API tag / 源 API 标签
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(IosColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mapping.sourceLanguage,
                            style = MaterialTheme.typography.labelSmall,
                            color = IosColor
                        )
                    }
                    Text(
                        text = mapping.sourceApi,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Arrow / 箭头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(PurplePrimary.copy(alpha = 0.3f))
                )
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    tint = PurplePrimary
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(PurplePrimary.copy(alpha = 0.3f))
                )
            }

            // Target API / 目标 API
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PurplePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mapping.targetApi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PurplePrimary
                )
            }

            // Automation badge / 自动化状态徽章
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (mapping.isAutomated) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (mapping.isAutomated) SuccessColor else WarningColor
                )
                Text(
                    text = if (mapping.isAutomated) "自动映射" else "需要手动处理",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mapping.isAutomated) SuccessColor else WarningColor
                )
            }

            // Expanded content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    Text(
                        text = "说明",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = mapping.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCopy) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制")
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Pitfall Cases Section / 踩坑案例区
// ============================================================

@Composable
private fun PitfallCasesSection(
    cases: List<PitfallCase>,
    expandedCaseId: String?,
    onToggleCase: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "真实迁移踩坑案例与解决方案",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(cases) { caseItem ->
            PitfallCaseCard(
                pitfallCase = caseItem,
                isExpanded = expandedCaseId == caseItem.id,
                onToggle = { onToggleCase(caseItem.id) }
            )
        }
    }
}

@Composable
private fun PitfallCaseCard(
    pitfallCase: PitfallCase,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                WarningColor.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp)
        ) {
            // Title row / 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = pitfallCase.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Problem / 问题
            AnimatedVisibility(
                visible = !isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = pitfallCase.problem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Expanded content / 展开内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Problem / 问题描述
                    InfoBlock(title = "问题", content = pitfallCase.problem, color = ErrorColor)

                    // Solution / 解决方案
                    InfoBlock(title = "解决方案", content = pitfallCase.solution, color = SuccessColor)

                    // Code example / 代码示例
                    pitfallCase.codeExample?.let { code ->
                        CodeBlock(code = code)
                    }
                }
            }
        }
    }
}

// ============================================================
// Quality Verification Section / 质量验证区
// ============================================================

@Composable
private fun QualityVerificationSection(
    checklist: List<QualityCheckItem>,
    onToggleItem: (String) -> Unit
) {
    val groupedChecklist = checklist.groupBy { it.category }
    val checkedCount = checklist.count { it.isChecked }
    val totalCount = checklist.size
    val progress = checkedCount.toFloat() / totalCount

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        // Progress header / 进度表头
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PurplePrimary.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "迁移质量验证",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$checkedCount / $totalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PurplePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (progress >= 1f) SuccessColor else PurplePrimary,
                        trackColor = PurplePrimary.copy(alpha = 0.1f)
                    )
                    if (progress >= 1f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "质量验证完成！",
                                style = MaterialTheme.typography.labelMedium,
                                color = SuccessColor
                            )
                        }
                    }
                }
            }
        }

        // Grouped checklist / 分组清单
        groupedChecklist.forEach { (category, items) ->
            item {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(items) { checkItem ->
                QualityCheckItemRow(
                    item = checkItem,
                    onToggle = { onToggleItem(checkItem.id) }
                )
            }
        }
    }
}

@Composable
private fun QualityCheckItemRow(
    item: QualityCheckItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked)
                SuccessColor.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = SuccessColor
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                    color = if (item.isChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            if (item.isChecked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================
// Shared Components / 共享组件
// ============================================================

/**
 * Section header with icon / 带图标的小节标题
 */
@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PurplePrimary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Info card with bullet items / 带项目符号的信息卡片
 */
@Composable
private fun InfoCard(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("•", color = PurplePrimary)
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Info block with colored title / 带颜色标题的信息块
 */
@Composable
private fun InfoBlock(title: String, content: String, color: Color) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

/**
 * Code block with syntax highlighting placeholder / 带语法高亮占位符的代码块
 */
@Composable
private fun CodeBlock(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kotlin / Swift",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = Color(0xFFD4D4D4),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}

/**
 * Decision banner / 决策横幅
 */
@Composable
private fun DecisionBanner(
    title: String,
    description: String,
    decisionPoints: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PurplePrimary.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = PurplePrimary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            decisionPoints.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = PurplePrimary)
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Checklist card / 检查清单卡片
 */
@Composable
private fun ChecklistCard(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Library recommendation card / 库推荐卡片
 */
@Composable
private fun LibraryRecommendationCard(
    library: String,
    rnLib: String,
    androidLib: String,
    notes: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = library,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PurplePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RN 库",
                        style = MaterialTheme.typography.labelSmall,
                        color = RnColor
                    )
                    Text(
                        text = rnLib,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android 库",
                        style = MaterialTheme.typography.labelSmall,
                        color = PurplePrimary
                    )
                    Text(
                        text = androidLib,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Feature enhancement card / 功能增强卡片
 */
@Composable
private fun FeatureEnhancementCard(
    feature: String,
    webApi: String,
    androidApi: String,
    enhancement: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SuccessColor.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "原生增强",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Web,
                    contentDescription = null,
                    tint = WebColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = webApi,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = androidApi,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = PurplePrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✦ $enhancement",
                style = MaterialTheme.typography.bodySmall,
                color = SuccessColor
            )
        }
    }
}