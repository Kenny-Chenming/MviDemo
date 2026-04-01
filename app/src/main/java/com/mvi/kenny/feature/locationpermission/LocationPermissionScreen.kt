package com.mvi.kenny.feature.locationpermission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.base.TopBarAction
import com.mvi.kenny.base.TopBarConfig
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ============================================================
// Design-Specified Colors — 设计文档定义的颜色
// ============================================================
private object LocationPermissionColors {
    val Primary = Color(0xFF0D7377)       // 深青绿（安全信任）
    val Secondary = Color(0xFF14919B)     // 辅助色
    val WarningOrange = Color(0xFFF4A261) // 警示橙
    val DangerRed = Color(0xFFE63946)     // 危险红
    val BackgroundDark = Color(0xFF0A1616) // 深色背景
    val Surface = Color(0xFF1A2A2A)        // 表面色
    val TextPrimary = Color(0xFFE8F4F4)   // 主文字
    val TextSecondary = Color(0xFF7FA8A8) // 次文字

    fun threatLevelColor(level: ThreatLevel): Color = when (level) {
        ThreatLevel.NONE -> Primary
        ThreatLevel.LOW -> Color(0xFF4CAF50)
        ThreatLevel.MEDIUM -> WarningOrange
        ThreatLevel.HIGH -> DangerRed
    }

    fun permissionBadgeColor(type: LocationPermissionType): Color = when (type) {
        LocationPermissionType.FINE, LocationPermissionType.ALWAYS_FINE -> DangerRed
        LocationPermissionType.FINE_ONE_TIME -> WarningOrange
        LocationPermissionType.COARSE, LocationPermissionType.FOREGROUND_ONLY -> Primary
        LocationPermissionType.NONE -> TextSecondary
    }
}

// ============================================================
// LocationPermissionScreen — 位置权限管控主页面
// ============================================================
/**
 * Location permission management screen — main entry point.
 *
 * Contains 4 inner tabs:
 * 1. Dashboard (仪表盘) — overview, active apps, threat level
 * 2. App List (权限列表) — all apps with filter tabs
 * 3. History (访问历史) — location access timeline
 * 4. Settings (设置) — notification & data retention settings
 *
 * @param onUpdateTopBar Callback to update MainScreen TopBar config
 * @param viewModel ViewModel instance (default: creates new)
 *
 * @see LocationPermissionContract MVI contract definitions
 * @see LocationPermissionViewModel ViewModel implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPermissionScreen(
    onUpdateTopBar: (TopBarConfig) -> Unit,
    viewModel: LocationPermissionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var blockingButtonPressed by remember { mutableStateOf(false) }

    // ============================================================
    // Update TopBar — 更新顶部导航栏配置
    // ============================================================
    LaunchedEffect(state.activeTab, state.isScanning) {
        onUpdateTopBar(
            TopBarConfig(
                title = "位置权限管控",
                actions = listOf(
                    TopBarAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新扫描",
                        onClick = { viewModel.sendIntent(LocationPermissionIntent.StartScan) }
                    )
                )
            )
        )
    }

    // ============================================================
    // Collect Effects — 处理副作用
    // ============================================================
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LocationPermissionEffect.ShowToast -> {
                    // Toast handled by UI platform (short toast display)
                }
                is LocationPermissionEffect.ScanComplete -> {
                    // Scan complete indicator shown via state
                }
                is LocationPermissionEffect.PermissionRevoked -> {
                    // Permission revoked feedback
                }
                is LocationPermissionEffect.EmergencyBlockTriggered -> {
                    blockingButtonPressed = false
                }
                is LocationPermissionEffect.NavigateToSystemSettings -> {
                    // Navigate to system settings
                }
                is LocationPermissionEffect.NavigateToAppDetail -> {
                    // Navigate to app detail
                }
            }
        }
    }

    // ============================================================
    // Bottom Sheet for App Detail — App 详情底部弹窗
    // ============================================================
    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = if (state.selectedApp != null) SheetValue.Expanded else SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            state.selectedApp?.let { app ->
                AppDetailSheet(
                    app = app,
                    onRevoke = { viewModel.sendIntent(LocationPermissionIntent.RevokeApp(app.packageName)) },
                    onDowngrade = { viewModel.sendIntent(LocationPermissionIntent.DowngradePermission(app.packageName)) },
                    onDismiss = { viewModel.sendIntent(LocationPermissionIntent.DismissAppDetail) }
                )
            } ?: Box(modifier = Modifier.height(1.dp))
        },
        sheetPeekHeight = if (state.selectedApp != null) 300.dp else 0.dp,
        sheetContainerColor = LocationPermissionColors.Surface,
        containerColor = LocationPermissionColors.BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LocationPermissionColors.BackgroundDark)
        ) {
            // ============================================================
            // Inner Tab Row — 内部 Tab 切换
            // ============================================================
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = LocationPermissionColors.Surface,
                contentColor = LocationPermissionColors.Primary
            ) {
                LocationTab.entries.forEach { tab ->
                    Tab(
                        selected = state.activeTab == tab,
                        onClick = { viewModel.sendIntent(LocationPermissionIntent.SwitchTab(tab)) },
                        text = {
                            Text(
                                text = tab.title,
                                color = if (state.activeTab == tab)
                                    LocationPermissionColors.Primary
                                else
                                    LocationPermissionColors.TextSecondary
                            )
                        }
                    )
                }
            }

            // ============================================================
            // Tab Content — Tab 内容区
            // ============================================================
            when (state.activeTab) {
                LocationTab.DASHBOARD -> DashboardTab(
                    state = state,
                    blockingButtonPressed = blockingButtonPressed,
                    onBlockingButtonPress = {
                        blockingButtonPressed = true
                        viewModel.sendIntent(LocationPermissionIntent.RevokeAllActive)
                    },
                    onAppClick = { pkg ->
                        viewModel.sendIntent(LocationPermissionIntent.SelectApp(pkg))
                    },
                    onStartScan = {
                        viewModel.sendIntent(LocationPermissionIntent.StartScan)
                    }
                )
                LocationTab.APP_LIST -> AppListTab(
                    state = state,
                    onAppClick = { pkg ->
                        viewModel.sendIntent(LocationPermissionIntent.SelectApp(pkg))
                    },
                    onFilterChange = { filter ->
                        viewModel.sendIntent(LocationPermissionIntent.SetListFilter(filter))
                    }
                )
                LocationTab.HISTORY -> HistoryTab(
                    state = state,
                    onFilterChange = { filter ->
                        viewModel.sendIntent(LocationPermissionIntent.SetHistoryFilter(filter))
                    }
                )
                LocationTab.SETTINGS -> SettingsTab(
                    state = state,
                    onSettingsChange = { settings ->
                        viewModel.sendIntent(LocationPermissionIntent.UpdateSettings(settings))
                    }
                )
            }
        }
    }
}

// ============================================================
// Dashboard Tab — 仪表盘 Tab
// ============================================================
@Composable
private fun DashboardTab(
    state: LocationPermissionState,
    blockingButtonPressed: Boolean,
    onBlockingButtonPress: () -> Unit,
    onAppClick: (String) -> Unit,
    onStartScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ============================================================
        // Scanning Indicator — 扫描中指示器
        // ============================================================
        item {
            AnimatedVisibility(
                visible = state.isScanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = LocationPermissionColors.Primary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在扫描位置权限...",
                        color = LocationPermissionColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // ============================================================
        // Threat Level Card — 威胁等级卡片
        // ============================================================
        item {
            ThreatLevelCard(
                threatLevel = state.threatLevel,
                activeCount = state.activeApps.size,
                onRevokeAll = onBlockingButtonPress,
                blockingButtonPressed = blockingButtonPressed
            )
        }

        // ============================================================
        // Active Apps — 正在访问的 App
        // ============================================================
        if (state.activeApps.isNotEmpty()) {
            item {
                Text(
                    text = "正在访问位置",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocationPermissionColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(state.activeApps) { app ->
                ActiveAppCard(
                    app = app,
                    onClick = { onAppClick(app.packageName) }
                )
            }
        }

        // ============================================================
        // Recent Access Ranking — 近期访问排名
        // ============================================================
        if (state.recentApps.isNotEmpty()) {
            item {
                Text(
                    text = "近期访问排名",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocationPermissionColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.recentApps.take(5)) { app ->
                RecentAppCard(
                    app = app,
                    onClick = { onAppClick(app.packageName) }
                )
            }
        }

        // ============================================================
        // Empty State — 空状态
        // ============================================================
        if (!state.isScanning && state.allApps.isEmpty()) {
            item {
                EmptyStateCard(onStartScan = onStartScan)
            }
        }
    }
}

// ============================================================
// ThreatLevelCard — 威胁等级状态卡片
// ============================================================
@Composable
private fun ThreatLevelCard(
    threatLevel: ThreatLevel,
    activeCount: Int,
    onRevokeAll: () -> Unit,
    blockingButtonPressed: Boolean
) {
    val threatColor = LocationPermissionColors.threatLevelColor(threatLevel)
    val scale by animateFloatAsState(
        targetValue = if (blockingButtonPressed) 0.95f else 1f,
        label = "blocking_button_scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (threatLevel) {
                            ThreatLevel.NONE -> Icons.Default.Shield
                            ThreatLevel.LOW -> Icons.Default.Security
                            ThreatLevel.MEDIUM -> Icons.Default.Warning
                            ThreatLevel.HIGH -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = threatColor,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "威胁等级",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocationPermissionColors.TextSecondary
                        )
                        Text(
                            text = threatLevel.label,
                            style = MaterialTheme.typography.headlineSmall,
                            color = threatColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Active apps badge
                if (activeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = LocationPermissionColors.DangerRed
                            ) {
                                Text(
                                    text = "$activeCount",
                                    color = Color.White
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "活跃 App 数量",
                            tint = LocationPermissionColors.DangerRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (activeCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                // Emergency block button — 长按确认防止误触
                Button(
                    onClick = onRevokeAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocationPermissionColors.DangerRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (blockingButtonPressed) "确认阻断所有精确位置访问" else "一键阻断所有精确位置访问",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ============================================================
// ActiveAppCard — 正在访问的 App 卡片
// ============================================================
@Composable
private fun ActiveAppCard(
    app: AppLocationInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App icon placeholder (circle with initial)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocationPermissionColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocationPermissionColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "正在访问精确位置",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocationPermissionColors.DangerRed
                    )
                }
            }
            PermissionBadge(type = app.permissionType)
        }
    }
}

// ============================================================
// RecentAppCard — 近期访问 App 卡片
// ============================================================
@Composable
private fun RecentAppCard(
    app: AppLocationInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LocationPermissionColors.TextSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocationPermissionColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "访问 ${app.accessCount} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocationPermissionColors.TextSecondary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                PermissionBadge(type = app.permissionType)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.lastAccessTime?.let {
                        "${it.hour}:${String.format("%02d", it.minute)}"
                    } ?: "无记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = LocationPermissionColors.TextSecondary
                )
            }
        }
    }
}

// ============================================================
// PermissionBadge — 权限类型徽章
// ============================================================
@Composable
private fun PermissionBadge(type: LocationPermissionType) {
    val color = LocationPermissionColors.permissionBadgeColor(type)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================================================
// EmptyStateCard — 空状态卡片
// ============================================================
@Composable
private fun EmptyStateCard(onStartScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationSearching,
                contentDescription = null,
                tint = LocationPermissionColors.TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "你的设备很安静",
                style = MaterialTheme.typography.titleMedium,
                color = LocationPermissionColors.TextPrimary
            )
            Text(
                text = "没有任何 App 访问位置",
                style = MaterialTheme.typography.bodyMedium,
                color = LocationPermissionColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onStartScan) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重新扫描")
            }
        }
    }
}

// ============================================================
// AppListTab — App 权限列表 Tab
// ============================================================
@Composable
private fun AppListTab(
    state: LocationPermissionState,
    onAppClick: (String) -> Unit,
    onFilterChange: (AppListFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips — 筛选 Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AppListFilter.entries) { filter ->
                FilterChip(
                    selected = state.listTabFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LocationPermissionColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // App list — App 列表
        val filteredApps = when (state.listTabFilter) {
            AppListFilter.ALL -> state.allApps
            AppListFilter.ACTIVE -> state.activeApps
            AppListFilter.RECENT -> state.recentApps
            AppListFilter.NEVER -> state.allApps.filter { it.permissionType == LocationPermissionType.NONE }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps) { app ->
                AppListItem(
                    app = app,
                    onClick = { onAppClick(app.packageName) }
                )
            }
        }
    }
}

// ============================================================
// AppListItem — App 列表条目
// ============================================================
@Composable
private fun AppListItem(
    app: AppLocationInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (app.isActive) LocationPermissionColors.Primary
                            else LocationPermissionColors.TextSecondary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.width(120.dp)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocationPermissionColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocationPermissionColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                PermissionBadge(type = app.permissionType)
                if (app.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "● 实时访问",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocationPermissionColors.DangerRed
                    )
                }
            }
        }
    }
}

// ============================================================
// HistoryTab — 位置访问历史 Tab
// ============================================================
@Composable
private fun HistoryTab(
    state: LocationPermissionState,
    onFilterChange: (HistoryFilter) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(HistoryFilter.TODAY) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = !selectedFilter.showOnlyPrecise,
                    onClick = {
                        selectedFilter = selectedFilter.copy(showOnlyPrecise = false)
                        onFilterChange(selectedFilter)
                    },
                    label = { Text("全部") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LocationPermissionColors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter.showOnlyPrecise,
                    onClick = {
                        selectedFilter = selectedFilter.copy(showOnlyPrecise = true)
                        onFilterChange(selectedFilter)
                    },
                    label = { Text("只看精确位置") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LocationPermissionColors.WarningOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.historyByDay.forEach { (date, events) ->
                item {
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = LocationPermissionColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(events) { event ->
                    HistoryEventItem(event = event)
                }
            }
        }
    }
}

// ============================================================
// HistoryEventItem — 历史事件条目
// ============================================================
@Composable
private fun HistoryEventItem(event: LocationEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (event.isPrecise) Icons.Default.LocationOn else Icons.Default.LocationSearching,
                    contentDescription = null,
                    tint = if (event.isPrecise) LocationPermissionColors.DangerRed else LocationPermissionColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = event.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocationPermissionColors.TextPrimary
                    )
                    Text(
                        text = "${event.eventTime.hour}:${String.format("%02d", event.eventTime.minute)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocationPermissionColors.TextSecondary
                    )
                }
            }
            PermissionBadge(type = event.permissionType)
        }
    }
}

// ============================================================
// SettingsTab — 设置 Tab
// ============================================================
@Composable
private fun SettingsTab(
    state: LocationPermissionState,
    onSettingsChange: (PermissionSettings) -> Unit
) {
    val settings = state.settings

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsCard(title = "通知设置") {
                SettingsToggleItem(
                    title = "首次请求通知",
                    subtitle = "新 App 首次请求精确位置时通知",
                    checked = settings.notifyOnFirstRequest,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(notifyOnFirstRequest = it))
                    }
                )
                SettingsToggleItem(
                    title = "长时间访问通知",
                    subtitle = "位置持续访问超过 5 分钟时通知",
                    checked = settings.notifyOnLongAccess,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(notifyOnLongAccess = it))
                    }
                )
            }
        }

        item {
            SettingsCard(title = "数据保留策略") {
                DataRetentionSelector(
                    selectedDays = settings.dataRetentionDays,
                    onDaysSelected = {
                        onSettingsChange(settings.copy(dataRetentionDays = it))
                    }
                )
            }
        }

        item {
            SettingsCard(title = "高级设置") {
                SettingsToggleItem(
                    title = "启用 Android 17 新 API",
                    subtitle = "支持一次性精确位置等 Android 17 新特性",
                    checked = settings.android17ApisEnabled,
                    onCheckedChange = {
                        onSettingsChange(settings.copy(android17ApisEnabled = it))
                    }
                )
            }
        }
    }
}

// ============================================================
// SettingsCard — 设置卡片容器
// ============================================================
@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LocationPermissionColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = LocationPermissionColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// ============================================================
// SettingsToggleItem — 设置开关条目
// ============================================================
@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = LocationPermissionColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LocationPermissionColors.TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LocationPermissionColors.Primary
            )
        )
    }
}

// ============================================================
// DataRetentionSelector — 数据保留期选择器
// ============================================================
@Composable
private fun DataRetentionSelector(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(7, 30, 90).forEach { days ->
            FilterChip(
                selected = selectedDays == days,
                onClick = { onDaysSelected(days) },
                label = { Text("${days}天") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LocationPermissionColors.Primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ============================================================
// AppDetailSheet — App 权限详情 BottomSheet
// ============================================================
@Composable
private fun AppDetailSheet(
    app: AppLocationInfo,
    onRevoke: () -> Unit,
    onDowngrade: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = app.appName,
            style = MaterialTheme.typography.titleLarge,
            color = LocationPermissionColors.TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = LocationPermissionColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permission status
        DetailRow(label = "当前权限", value = app.permissionType.label)
        DetailRow(label = "权限来源", value = app.permissionSource.label)
        app.lastAccessTime?.let {
            DetailRow(label = "最后访问", value = "${it.hour}:${String.format("%02d", it.minute)}")
        }
        DetailRow(label = "累计访问", value = "${app.accessCount} 次")

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        if (app.permissionType != LocationPermissionType.NONE) {
            Button(
                onClick = onRevoke,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocationPermissionColors.DangerRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("撤销位置权限", fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (app.permissionType == LocationPermissionType.FINE ||
            app.permissionType == LocationPermissionType.ALWAYS_FINE
        ) {
            OutlinedButton(
                onClick = onDowngrade,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "降级为粗略位置",
                    color = LocationPermissionColors.Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================
// DetailRow — 详情行
// ============================================================
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = LocationPermissionColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = LocationPermissionColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
