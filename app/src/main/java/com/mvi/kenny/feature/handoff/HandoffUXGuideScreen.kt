package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ============================================================
 * HandoffUXGuideScreen — UX 设计规范
 * ============================================================
 * 通知样式、启动器集成、大屏适配指南
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoffUXGuideScreen(
    onNavigateBack: () -> Unit,
    viewModel: HandoffViewModel
) {
    val state by viewModel.uxGuideState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UX 设计规范") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Category Chips / 分类选择
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UXCategory.entries) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { viewModel.sendIntent(HandoffIntent.SelectUXCategory(category)) },
                            label = { Text(category.labelZh) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Content based on category / 根据分类显示内容
            item {
                when (state.selectedCategory) {
                    UXCategory.NOTIFICATIONS -> NotificationGuideContent(
                        showPreview = state.showNotificationPreview,
                        onTogglePreview = { viewModel.sendIntent(HandoffIntent.ToggleNotificationPreview) }
                    )
                    UXCategory.LAUNCHER -> LauncherGuideContent()
                    UXCategory.TASKBAR -> TaskbarGuideContent()
                    UXCategory.LARGE_SCREEN -> LargeScreenGuideContent()
                }
            }

            // Scenarios Section / 场景化指南
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("场景化指南", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getUXScenarios()) { scenario ->
                        ScenarioCard(
                            scenario = scenario,
                            isSelected = state.selectedScenario == scenario,
                            onClick = { viewModel.sendIntent(HandoffIntent.SelectUXScenario(scenario)) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

/**
 * Notification Guide Content / 通知样式指南内容
 */
@Composable
private fun NotificationGuideContent(
    showPreview: Boolean,
    onTogglePreview: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = HandoffColors.Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Handoff 通知样式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                FilterChip(
                    selected = showPreview,
                    onClick = onTogglePreview,
                    label = { Text(if (showPreview) "隐藏预览" else "显示预览") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Specs / 通知规范
            GuideSpecItem("图标", "使用 App Icon 或 Activity 专属图标")
            GuideSpecItem("标题", "「从另一设备继续」或 Activity 名称")
            GuideSpecItem("内容", "简短描述当前状态，如「正在编辑文档」")
            GuideSpecItem("操作按钮", "「继续」按钮，点击启动 Handoff")

            if (showPreview) {
                Spacer(modifier = Modifier.height(16.dp))
                NotificationPreview()
            }
        }
    }
}

/**
 * Guide Spec Item / 规范项
 */
@Composable
private fun GuideSpecItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

/**
 * Notification Preview / 通知预览
 */
@Composable
private fun NotificationPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HandoffColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("从另一设备继续", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("正在编辑文档 - 标题.txt", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(HandoffColors.Primary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("继续", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Launcher Guide Content / 启动器集成内容
 */
@Composable
private fun LauncherGuideContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launcher 快捷方式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            GuideSpecItem("Shortcuts", "在 Android 12+ 使用 Shortcuts API")
            GuideSpecItem("Intent Filter", "添加 android.intent.action.HANDOFF_FROM 过滤")
            GuideSpecItem("图标尺寸", "48dp / 72dp / 96dp 多尺寸")
            GuideSpecItem("Deep Link", "配置 handoff:// 协议支持")
        }
    }
}

/**
 * Taskbar Guide Content / 任务栏集成内容
 */
@Composable
private fun TaskbarGuideContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tab, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Taskbar 集成", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            GuideSpecItem("任务栏入口", "显示当前 App 的 Handoff 目标设备")
            GuideSpecItem("拖拽操作", "支持拖拽到任务栏设备图标")
            GuideSpecItem("跨设备 Resume", "从任务栏直接恢复其他设备的状态")
            GuideSpecItem("窗口管理", "配合 WindowManager 实现多窗口")
        }
    }
}

/**
 * Large Screen Guide Content / 大屏适配内容
 */
@Composable
private fun LargeScreenGuideContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Laptop, contentDescription = null, tint = HandoffColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("大屏适配指南", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Window size classes / 窗口尺寸分类
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WindowSizeClassChip("Compact", "手机", Icons.Default.PhoneAndroid)
                WindowSizeClassChip("Medium", "折叠/平板", Icons.Default.Tab)
                WindowSizeClassChip("Expanded", "桌面", Icons.Default.Laptop)
            }

            Spacer(modifier = Modifier.height(12.dp))
            GuideSpecItem("Layout 切换", "根据 WindowSizeClass 调整 UI")
            GuideSpecItem("双屏模式", "折叠屏展开时启用双屏布局")
            GuideSpecItem("键盘/鼠标", "大屏模式优先使用桌面交互")
        }
    }
}

/**
 * Window Size Class Chip / 窗口尺寸分类标签
 */
@Composable
private fun WindowSizeClassChip(label: String, desc: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HandoffColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HandoffColors.Primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

/**
 * Scenario Card / 场景卡片
 */
@Composable
private fun ScenarioCard(
    scenario: UXScenario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HandoffColors.Primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(scenario.type.labelZh, style = MaterialTheme.typography.labelSmall, color = HandoffColors.Primary)
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = HandoffColors.HandoffActive, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(scenario.titleZh, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                scenario.descriptionZh,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
    }
}

/**
 * Get UX Scenarios / 获取 UX 场景列表
 */
private fun getUXScenarios(): List<UXScenario> = listOf(
    UXScenario("email", AppType.EMAIL, "邮件 App", "邮件 App", "邮件类 App Handoff 指南", "延续邮件撰写、附件浏览等", listOf("保留草稿", "同步附件路径")),
    UXScenario("document", AppType.DOCUMENT, "文档 App", "文档 App", "文档类 App Handoff 指南", "延续文档编辑、光标位置", listOf("保留编辑位置", "同步格式")),
    UXScenario("notes", AppType.NOTES, "笔记 App", "笔记 App", "笔记类 App Handoff 指南", "笔记同步、多设备协作", listOf("增量同步", "冲突处理")),
    UXScenario("browser", AppType.BROWSER, "浏览器", "浏览器", "浏览器 Handoff 指南", "标签页同步、浏览历史", listOf("URL 共享", "会话恢复")),
    UXScenario("media", AppType.MEDIA, "媒体 App", "媒体 App", "媒体类 App Handoff 指南", "视频播放进度、音乐播放", listOf("进度同步", "播放状态"))
)

/**
 * Get category icon / 获取分类图标
 */
private fun getCategoryIcon(category: UXCategory): ImageVector {
    return when (category) {
        UXCategory.NOTIFICATIONS -> Icons.Default.Notifications
        UXCategory.LAUNCHER -> Icons.Default.Star
        UXCategory.TASKBAR -> Icons.Default.Tab
        UXCategory.LARGE_SCREEN -> Icons.Default.Laptop
    }
}
