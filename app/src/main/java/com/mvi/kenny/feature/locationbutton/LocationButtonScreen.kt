package com.mvi.kenny.feature.locationbutton

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mvi.kenny.base.TopBarConfig
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * ============================================================
 * LocationButtonScreen — Location Button 功能主界面
 * ============================================================
 * Main entry screen for the Android 17 Location Button Jetpack Library.
 *
 * Design Reference: designs/PRD-081-Android-17-Location-Button-Jetpack-Library.md
 *
 * Features:
 * 1. LocationButton Composable component demonstration
 * 2. Three integration templates: Map, CheckIn, Social
 * 3. Support status display and fallback indicator
 * 4. Permission result handling with appropriate UI feedback
 * —————————————————————————————————————————————————————
 */
@Composable
fun LocationButtonScreen(
    viewModel: LocationButtonViewModel = viewModel(),
    onUpdateTopBar: (TopBarConfig) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var permissionResult by remember { mutableStateOf<LocationPermissionResult?>(null) }

    // Permission launcher for traditional permission flow
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        val isPrecise = fineGranted == true
        val permanentlyDenied = !granted // Simplified: if not granted, treat as potential permanent denial

        permissionResult = LocationPermissionResult(
            granted = granted,
            isPrecise = isPrecise,
            deniedType = if (!granted) DeniedType.UserDenied else null
        )
        viewModel.onPermissionResult(granted, isPrecise, permanentlyDenied)
    }

    // Update TopBar config when state changes
    LaunchedEffect(state) {
        onUpdateTopBar(
            TopBarConfig(
                title = "Location Button"
            )
        )
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LocationButtonEffect.LocationGranted -> {
                    snackbarHostState.showSnackbar(
                        message = if (effect.isPrecise) "精确位置已获取 🎉" else "粗略位置已获取"
                    )
                }
                is LocationButtonEffect.LocationDenied -> {
                    snackbarHostState.showSnackbar(message = "位置权限被拒绝: ${effect.type.description}")
                }
                is LocationButtonEffect.FallbackTriggered -> {
                    snackbarHostState.showSnackbar(message = effect.reason)
                }
                is LocationButtonEffect.OpenSettings -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
                is LocationButtonEffect.Error -> {
                    snackbarHostState.showSnackbar(message = "错误: ${effect.message}")
                }
                is LocationButtonEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ============================================================
            // Support Status Banner / 支持状态横幅
            // ============================================================
            SupportStatusBanner(
                isSupported = state.isSupported,
                fallbackUsed = state.fallbackUsed
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================================
            // Template Tab Row / 模板 Tab 行
            // ============================================================
            TemplateTabRow(
                activeTab = state.activeTemplate,
                onTabSelected = { viewModel.sendIntent(LocationButtonIntent.SwitchTemplate(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ============================================================
            // Template Content / 模板内容
            // ============================================================
            when (state.activeTemplate) {
                TemplateTab.Main -> MainTemplateContent(
                    state = state,
                    permissionResult = permissionResult,
                    onRequestLocation = {
                        if (state.isSupported) {
                            // On Android 17+, the system Location Button dialog is shown
                            // For this demo, we use the traditional permission flow
                            // In production, Location Button would be triggered via
                            // the system LocationButtonPicker API
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            viewModel.sendIntent(LocationButtonIntent.RequestLocation)
                        } else {
                            // Fallback to traditional permission
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                            viewModel.sendIntent(LocationButtonIntent.RequestLocation)
                        }
                    },
                    onOpenSettings = { viewModel.sendIntent(LocationButtonIntent.OpenAppSettings) },
                    onReset = { viewModel.sendIntent(LocationButtonIntent.Reset) }
                )
                TemplateTab.Map -> MapTemplateContent()
                TemplateTab.CheckIn -> CheckInTemplateContent()
                TemplateTab.Social -> SocialTemplateContent()
            }
        }
    }
}

/**
 * ============================================================
 * SupportStatusBanner — 支持状态横幅组件
 * ============================================================
 * Displays whether the device supports Android 17 Location Button
 * or is using the traditional permission fallback.
 *
 * @param isSupported Whether Location Button is supported
 * @param fallbackUsed Whether fallback mode is active
 */
@Composable
private fun SupportStatusBanner(
    isSupported: Boolean,
    fallbackUsed: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSupported) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        label = "banner_background"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSupported) Icons.Default.LocationOn else Icons.Default.LocationOff,
                contentDescription = null,
                tint = if (isSupported) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isSupported) "Location Button 已启用" else "使用传统权限流程",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSupported) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
                Text(
                    text = "Android ${Build.VERSION.SDK_INT} / API 37 (Android 17)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSupported) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

/**
 * ============================================================
 * TemplateTabRow — 模板 Tab 行组件
 * ============================================================
 *
 * @param activeTab Currently active template tab
 * @param onTabSelected Callback when a tab is selected
 */
@Composable
private fun TemplateTabRow(
    activeTab: TemplateTab,
    onTabSelected: (TemplateTab) -> Unit
) {
    TabRow(
        selectedTabIndex = TemplateTab.entries.indexOf(activeTab)
    ) {
        TemplateTab.entries.forEach { tab ->
            Tab(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.title) }
            )
        }
    }
}

/**
 * ============================================================
 * MainTemplateContent — 主入口模板内容
 * ============================================================
 * Demonstrates the core LocationButton Composable with status display.
 *
 * @param state Current LocationButton state
 * @param permissionResult Latest permission result
 * @param onRequestLocation Callback when user clicks the location button
 * @param onOpenSettings Callback when user clicks "Open Settings"
 * @param onReset Callback when user clicks "Reset"
 */
@Composable
private fun MainTemplateContent(
    state: LocationButtonState,
    permissionResult: LocationPermissionResult?,
    onRequestLocation: () -> Unit,
    onOpenSettings: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ============================================================
        // LocationButton Demo / 位置按钮演示
        // ============================================================
        LocationButton(
            state = state,
            onRequestLocation = onRequestLocation
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ============================================================
        // Status Card / 状态卡片
        // ============================================================
        StatusCard(state = state, permissionResult = permissionResult)

        Spacer(modifier = Modifier.height(16.dp))

        // ============================================================
        // Action Buttons / 操作按钮
        // ============================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("重置")
            }

            if (state.status == LocationStatus.PermanentlyDenied) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("去设置")
                }
            }
        }
    }
}

/**
 * ============================================================
 * LocationButton — 一键式位置按钮 Composable
 * ============================================================
 * Main entry Composable for the Location Button library.
 * Handles the complete location permission flow with fallback support.
 *
 * Design Reference: Section 3.2, 5 in PRD-081 design doc
 *
 * @param state Current location button state
 * @param onRequestLocation Callback when button is clicked
 * @param modifier Compose modifier
 */
@Composable
fun LocationButton(
    state: LocationButtonState,
    onRequestLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = state.status == LocationStatus.Requesting
    val isGranted = state.status == LocationStatus.Granted

    val buttonColor by animateColorAsState(
        targetValue = when {
            isGranted -> MaterialTheme.colorScheme.tertiary
            state.status == LocationStatus.PermanentlyDenied || state.status == LocationStatus.Denied -> MaterialTheme.colorScheme.error
            isLoading -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "button_color"
    )

    Button(
        onClick = { if (!isLoading) onRequestLocation() },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("请求中...")
            }
            isGranted -> {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("位置已获取")
            }
            state.status == LocationStatus.PermanentlyDenied || state.status == LocationStatus.Denied -> {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("权限被拒绝")
            }
            else -> {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("获取位置")
            }
        }
    }
}

/**
 * ============================================================
 * StatusCard — 权限状态详情卡片
 * ============================================================
 *
 * @param state Current LocationButton state
 * @param permissionResult Latest permission result
 */
@Composable
private fun StatusCard(
    state: LocationButtonState,
    permissionResult: LocationPermissionResult?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "状态详情",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            StatusRow("当前状态", state.status.displayName)
            StatusRow("支持 Location Button", if (state.isSupported) "是" else "否")
            StatusRow("使用回退模式", if (state.fallbackUsed) "是" else "否")

            if (permissionResult != null) {
                StatusRow("权限结果", if (permissionResult.granted) "已授权" else "已拒绝")
                if (permissionResult.granted) {
                    StatusRow("精度", if (permissionResult.isPrecise) "精确位置" else "粗略位置")
                }
            }
        }
    }
}

/**
 * ============================================================
 * StatusRow — 状态行组件
 * ============================================================
 */
@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * ============================================================
 * MapTemplateContent — 地图 App 集成模板内容
 * ============================================================
 * Integration template for map/location-based apps.
 * Shows how to integrate LocationButton with map centering logic.
 *
 * Design Reference: Section 9.1 in PRD-081 design doc
 */
@Composable
private fun MapTemplateContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🗺️ 地图 App 集成模板",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "集成示例：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))

            CodeSnippet("""
                MapScreen {
                    LocationButton(state, onIntent)

                    // onGranted: Update map center to user location
                    when (effect) {
                        is LocationGranted -> mapView.centerAtUser()
                        is LocationDenied -> showErrorSnackbar()
                        is FallbackTriggered -> showFallbackHint()
                    }
                }
            """.trimIndent())

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "关键行为：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            BulletPoint("onGranted: 更新地图中心点到用户位置 + 标记")
            BulletPoint("onDenied: 显示 Snackbar + 回退到默认坐标（如北京）")
            BulletPoint("Fallback: 提示用户当前使用传统权限流程")
        }
    }
}

/**
 * ============================================================
 * CheckInTemplateContent — 签到 App 集成模板内容
 * ============================================================
 * Integration template for check-in/location-verification apps.
 * Shows how to integrate LocationButton with geofencing logic.
 *
 * Design Reference: Section 9.2 in PRD-081 design doc
 */
@Composable
private fun CheckInTemplateContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📍 签到 App 集成模板",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "集成示例：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))

            CodeSnippet("""
                CheckInScreen {
                    LocationButton(state, onIntent)

                    // onGranted: Check if within geofence
                    when (effect) {
                        is LocationGranted -> {
                            val inRange = checkGeofence(userLoc, targetLoc)
                            if (inRange) showCheckInSuccess()
                            else showOutOfRangeError()
                        }
                    }
                }
            """.trimIndent())

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "关键行为：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            BulletPoint("onGranted: 检查用户是否在指定范围内（Geofencing）")
            BulletPoint("在范围内: 允许签到 + 显示打卡成功动画")
            BulletPoint("不在范围内: 提示距离目标位置的距离")
        }
    }
}

/**
 * ============================================================
 * SocialTemplateContent — 社交 App 集成模板内容
 * ============================================================
 * Integration template for social/location-sharing apps.
 * Shows how to integrate LocationButton for optional location tagging.
 *
 * Design Reference: Section 9.3 in PRD-081 design doc
 */
@Composable
private fun SocialTemplateContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "👥 社交 App 集成模板",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "集成示例：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))

            CodeSnippet("""
                SocialShareScreen {
                    LocationButton(state, onIntent)

                    // onGranted: Attach location tag to shared content
                    // onDenied: Share without location
                    when (effect) {
                        is LocationGranted -> attachLocationTag()
                        is LocationDenied -> proceedWithoutLocation()
                    }
                }
            """.trimIndent())

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "关键行为：",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            BulletPoint("onGranted: 附加位置标签到分享内容")
            BulletPoint("onDenied: 不带位置信息分享（优雅降级）")
            BulletPoint("位置标签可选，不影响主流程")
        }
    }
}

/**
 * ============================================================
 * CodeSnippet — 代码片段展示组件
 * ============================================================
 * Displays code snippets with monospace font and background.
 */
@Composable
private fun CodeSnippet(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ============================================================
 * BulletPoint — 项目符号文本组件
 * ============================================================
 */
@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


