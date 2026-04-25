package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffFallbackStrategyScreen — 降级策略
// Handoff Fallback Strategy Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 8: Fallback Strategy
// 功能：Handoff 不可用时的降级方案配置

@Composable
fun HandoffFallbackStrategyScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "降级策略",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "当 Handoff 不可用时，选择合适的降级方案 / Choose appropriate fallback when Handoff is unavailable",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Handoff 可能失败的场景：设备不支持 Handoff、系统版本低于 Android 17、网络不可用等。建议配置至少一个降级方案。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "可用降级方案 / Available Fallback Options",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.fallbackOptions) { option ->
                FallbackOptionCard(
                    option = option,
                    isSelected = state.selectedFallback == option.type,
                    onSelect = { onIntent(HandoffIntent.SelectFallback(option.type)) }
                )
            }
        }
    }
}

@Composable
private fun FallbackOptionCard(
    option: FallbackOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected && option.isRecommended ->
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                isSelected ->
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                option.isRecommended ->
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else ->
                    MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (option.isRecommended) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        "推荐 / Recommended",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                FallbackDetailContent(option = option)
            }
        }
    }
}

@Composable
private fun FallbackDetailContent(option: FallbackOption) {
    val (title, steps, pros, cons) = when (option.type) {
        FallbackType.DEEP_LINK -> Triple(
            "深链接方案 / Deep Link Fallback",
            listOf(
                "1. 在 HandoffIntent 中包含 content URI / Include content URI in HandoffIntent",
                "2. 目标设备通过 Intent URI 启动对应页面 / Target device starts corresponding page via Intent URI",
                "3. 页面通过 content URI 加载数据 / Page loads data via content URI"
            ),
            listOf("✅ 无需安装 App / No app install needed", "✅ 实现简单 / Simple to implement", "✅ 兼容所有 Android 版本 / Works on all versions"),
            listOf("⚠️ 需要网络连接 / Requires network", "⚠️ 无法传递本地状态 / Cannot transfer local state")
        )
        FallbackType.WEB_FALLBACK -> Triple(
            "Web 降级方案 / Web Fallback",
            listOf(
                "1. 将内容发布到 Web 版本 / Publish content to web version",
                "2. 通过 URL 分享到目标设备 / Share via URL to target device",
                "3. 目标设备通过浏览器打开 / Open via browser on target device"
            ),
            listOf("✅ 完全无需 App / No app needed at all", "✅ 跨平台兼容 / Cross-platform compatible"),
            listOf("⚠️ 功能受限 / Limited functionality", "⚠️ 无离线支持 / No offline support")
        )
        FallbackType.NOTIFICATION -> Triple(
            "推送通知方案 / Push Notification Fallback",
            listOf(
                "1. Handoff 失败时，发送推送通知 / On Handoff failure, send push notification",
                "2. 通知包含继续操作入口 / Notification contains continue action",
                "3. 用户点击后拉取最新状态 / User pulls latest state after clicking"
            ),
            listOf("✅ 不打断用户当前任务 / Doesn't interrupt current task", "✅ 可延迟处理 / Can be deferred"),
            listOf("⚠️ 实时性差 / Poor real-time", "⚠️ 需要推送服务 / Requires push service")
        )
        FallbackType.QR_CODE -> Triple(
            "二维码方案 / QR Code Fallback",
            listOf(
                "1. 将 Handoff 数据编码为二维码 / Encode Handoff data as QR code",
                "2. 源设备显示二维码 / Source device displays QR code",
                "3. 目标设备扫码获取数据 / Target device scans to get data"
            ),
            listOf("✅ 无需网络 / No network needed", "✅ 隐私友好 / Privacy-friendly"),
            listOf("⚠️ 数据量有限 / Limited data size", "⚠️ 操作繁琐 / Tedious operation")
        )
        FallbackType.NONE -> Triple(
            "无可用方案 / No Fallback Available",
            listOf(
                "1. Handoff 是唯一方式 / Handoff is the only option",
                "2. 如果 Handoff 失败，显示明确错误信息 / Show clear error if Handoff fails",
                "3. 引导用户手动操作 / Guide user to manual action"
            ),
            listOf("✅ 最简单的技术实现 / Simplest technical implementation"),
            listOf("❌ 用户体验最差 / Worst user experience")
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Text("步骤 / Steps", style = MaterialTheme.typography.labelSmall)
            steps.forEach { step ->
                Text(step, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 1.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("优点 / Pros", style = MaterialTheme.typography.labelSmall)
            pros.forEach { pro ->
                Text(pro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 1.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("缺点 / Cons", style = MaterialTheme.typography.labelSmall)
            cons.forEach { con ->
                Text(con, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 1.dp))
            }
        }
    }
}
