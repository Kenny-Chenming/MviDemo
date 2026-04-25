package com.mvi.kenny.feature.largescreen.screens

// ================================================================
// FallbackDetectorScreen — 多窗口降级策略检测器
// ================================================================
// Tool 4: Multi-window fallback strategy detector.
//
// PRD-155: Android 17 大屏强制适配
// Detects App fallback paths when resizeableActivity=false is declared.
// Visualizes how the App degrades in non-resizable mode.
//
// Features:
//   - Fallback path detection and analysis
//   - Fallback path visualization diagram
//   - Affected configurations listing
// —————————————————————————————————————————————————————————————

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvi.kenny.feature.largescreen.FallbackPath
import com.mvi.kenny.feature.largescreen.LargeScreenIntent
import com.mvi.kenny.feature.largescreen.LargeScreenState

/**
 * ============================================================
 * FallbackDetectorScreen — 降级检测器主界面
 * ============================================================
 */
@Composable
fun FallbackDetectorScreen(
    state: LargeScreenState,
    onIntent: (LargeScreenIntent) -> Unit
) {
    var targetPath by remember { mutableStateOf(state.checkTargetPath) }
    var detectedPaths by remember { mutableStateOf(sampleFallbackPaths) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ─────────────────────────────────────────────────────
        // Info Banner — Android 17 背景说明
        // ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6750A4).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "关于降级策略",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "当 App 声明 android:resizeableActivity=\"false\" 时，在多窗口或大屏模式下，系统会应用降级策略。Android 17 targeting API 37+ 后，这种声明不再被允许。请使用合规检测器检查您的 App。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Fallback Path Visualization — 降级路径可视化
        // ─────────────────────────────────────────────────────
        Text(
            text = "降级路径图",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "当 resizeableActivity=false 时，App 在不同场景下的降级行为",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        FallbackPathVisualization(paths = detectedPaths)

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Fallback Path Details — 降级路径详情
        // ─────────────────────────────────────────────────────
        Text(
            text = "降级路径详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(detectedPaths, key = { it.id }) { path ->
                FallbackPathCard(path = path)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Affected Configurations — 受影响配置
        // ─────────────────────────────────────────────────────
        Text(
            text = "受影响的配置项",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                listOf(
                    "android:resizeableActivity=\"false\"",
                    "android:configChanges=\"orientation|screenSize\"",
                    "android:screenOrientation=\"locked\"",
                    "android:maxAspectRatio 设置过小"
                ).forEach { config ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFB3261E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = config,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color(0xFFB3261E)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─────────────────────────────────────────────────────
        // Recommended Actions — 推荐操作
        // ─────────────────────────────────────────────────────
        Text(
            text = "推荐操作",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF146B3A).copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                listOf(
                    "删除 android:resizeableActivity=\"false\" 声明",
                    "移除 configChanges 中的 orientation|screenSize",
                    "提供 sw=600dp 和 sw=840dp 布局资源",
                    "使用 Jetpack WindowManager 处理窗口变更",
                    "在 CI 中添加多窗口合规检测步骤"
                ).forEachIndexed { index, action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF146B3A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * ============================================================
 * FallbackPathVisualization — 降级路径可视化图
 * ============================================================
 */
@Composable
private fun FallbackPathVisualization(
    paths: List<FallbackPath>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Start: Phone mode
            FallbackNode(
                icon = Icons.Default.PhoneAndroid,
                label = "手机模式",
                sublabel = "全屏 正常",
                color = Color(0xFF146B3A),
                isStart = true
            )

            paths.forEach { path ->
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Condition
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFF5A623).copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = path.condition,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF5A623),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                FallbackNode(
                    icon = when {
                        path.id == "split" -> Icons.Default.SwapHoriz
                        path.id == "rotate" -> Icons.Default.ScreenRotation
                        else -> Icons.Default.Tablet
                    },
                    label = when (path.id) {
                        "split" -> "分屏模式"
                        "rotate" -> "旋转大屏"
                        else -> "大屏模式"
                    },
                    sublabel = path.behavior,
                    color = Color(0xFFB3261E),
                    isStart = false
                )
            }
        }
    }
}

/**
 * ============================================================
 * FallbackNode — 降级路径节点
 * ============================================================
 */
@Composable
private fun FallbackNode(
    icon: ImageVector,
    label: String,
    sublabel: String,
    color: Color,
    isStart: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), CircleShape)
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * ============================================================
 * FallbackPathCard — 降级路径卡片
 * ============================================================
 */
@Composable
private fun FallbackPathCard(path: FallbackPath) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = path.id.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB3261E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = path.condition,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = path.behavior,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (path.affectedConfigurations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                path.affectedConfigurations.forEach { config ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFF625B71),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = config,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color(0xFF625B71)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sample fallback paths for demonstration.
 */
private val sampleFallbackPaths = listOf(
    FallbackPath(
        id = "split",
        condition = "用户进入分屏模式（split-screen）",
        behavior = "App 被强制调整为半屏，无法自行控制",
        affectedConfigurations = listOf(
            "android:resizeableActivity=\"false\"",
            "android:configChanges 不包含 screenSize"
        )
    ),
    FallbackPath(
        id = "rotate",
        condition = "平板旋转到大屏方向（landscape > 840dp）",
        behavior = "App 保持全屏但以 letterbox 居中显示，上下/左右留黑边",
        affectedConfigurations = listOf(
            "android:maxAspectRatio 未设置",
            "android:screenOrientation=\"locked\""
        )
    ),
    FallbackPath(
        id = "freeform",
        condition = "Android 17+ 进入自由窗口模式（freeform）",
        behavior = "App 以固定尺寸窗口运行，无法自由拖动边框",
        affectedConfigurations = listOf(
            "android:resizeableActivity=\"false\"",
            "android:freeformSize 属性未声明"
        )
    )
)
