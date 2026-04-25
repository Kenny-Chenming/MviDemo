package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffIntentBuilderScreen — Intent 构建预览工具
// Handoff Intent Builder Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 2: HandoffIntentBuilder
// 功能：预览和生成 Handoff Intent 代码
//
// Features / 功能:
// - Configure Intent package and class name / 配置 Intent 包名和类名
// - Preview generated Intent code / 预览生成的 Intent 代码
// - Copy code to clipboard / 复制代码

@Composable
fun HandoffIntentBuilderScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header / 标题
        Text(
            text = "Handoff Intent 构建预览",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "配置 Intent 参数，预览 Handoff Intent 构建代码 / Configure Intent parameters and preview Handoff Intent builder code",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Intent configuration / Intent 配置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Intent 配置 / Intent Configuration",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.intentPackageName,
                    onValueChange = { onIntent(HandoffIntent.UpdateIntentPackage(it)) },
                    label = { Text("包名 / Package Name") },
                    placeholder = { Text("com.example.myapp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.intentClassName,
                    onValueChange = { onIntent(HandoffIntent.UpdateIntentClass(it)) },
                    label = { Text("Activity 类名 / Activity Class Name") },
                    placeholder = { Text("MainActivity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Generated Intent code preview / 生成的 Intent 代码预览
        Text(
            text = "生成的 Intent 代码 / Generated Intent Code",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))

        val kotlinCode = buildIntentKotlinCode(state.intentPackageName, state.intentClassName)
        val javaCode = buildIntentJavaCode(state.intentPackageName, state.intentClassName)

        listOf(
            "Kotlin" to kotlinCode,
            "Java" to javaCode
        ).forEach { (lang, code) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { /* copy */ }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("复制 / Copy")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Handoff intent requirements / Handoff Intent 要求说明
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ Handoff Intent 规范 / Handoff Intent Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                val requirements = listOf(
                    "必须包含 CATEGORY_HANDOFF category / Must include CATEGORY_HANDOFF",
                    "必须指定目标设备类型 EXTRA_HANDOVER_TARGET_DEVICE / Must specify target device type",
                    "Activity 状态通过 HandoffActivityData 序列化传递 / State passed via HandoffActivityData serialization",
                    "Intent 需要 BIND_HANDOVER_API permission / Intent requires BIND_HANDOVER_API permission"
                )
                requirements.forEach { req ->
                    Text(
                        text = "• $req",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Build Kotlin Handoff Intent code / 构建 Kotlin Handoff Intent 代码
 */
private fun buildIntentKotlinCode(packageName: String, className: String): String {
    val pkg = packageName.ifEmpty { "com.example.myapp" }
    val cls = className.ifEmpty { "MainActivity" }
    return """
// Kotlin — Handoff Intent Builder / Kotlin — Handoff Intent 构建器
// Package: $pkg

import android.content.Intent
import android.os.HandoffActivityData

/**
 * Build Handoff Intent for $cls.
 * 为 $cls 构建 Handoff Intent。
 */
fun buildHandoffIntent(
    context: android.content.Context,
    handoffData: HandoffActivityData,
    targetDeviceType: String
): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
        // Set Handoff category / 设置 Handoff category
        addCategory(Intent.CATEGORY_HANDOFF)

        // Set target component / 设置目标组件
        component = android.content.ComponentName(
            "$pkg",
            "$pkg.$cls"
        )

        // Attach Handoff data / 附加 Handoff 数据
        putExtra(Intent.EXTRA_HANDOFF_ACTIVITY_DATA, handoffData)

        // Set target device type / 设置目标设备类型
        putExtra(Intent.EXTRA_HANDOVER_TARGET_DEVICE, targetDeviceType)

        // Require local network access for cross-device transfer
        // 需要本地网络访问权限以进行跨设备传输
        addFlags(Intent.FLAG_GRANT_LOCAL_AND_URI_PERMISSIONS)
    }
}

/**
 * Receive and process Handoff Intent.
 * 接收和处理 Handoff Intent。
 */
fun processHandoffIntent(intent: Intent): HandoffActivityData? {
    if (intent.hasCategory(Intent.CATEGORY_HANDOFF)) {
        return intent.getParcelableExtra(
            Intent.EXTRA_HANDOFF_ACTIVITY_DATA,
            HandoffActivityData::class.java
        )
    }
    return null
}
""".trimIndent()
}

/**
 * Build Java Handoff Intent code / 构建 Java Handoff Intent 代码
 */
private fun buildIntentJavaCode(packageName: String, className: String): String {
    val pkg = packageName.ifEmpty { "com.example.myapp" }
    val cls = className.ifEmpty { "MainActivity" }
    return """
// Java — Handoff Intent Builder / Java — Handoff Intent 构建器
// Package: $pkg

import android.content.Intent;
import android.os.HandoffActivityData;

/**
 * Build Handoff Intent for $cls.
 */
public static Intent buildHandoffIntent(
    Context context,
    HandoffActivityData handoffData,
    String targetDeviceType
) {
    Intent intent = new Intent(Intent.ACTION_VIEW);

    // Set Handoff category
    intent.addCategory(Intent.CATEGORY_HANDOFF);

    // Set target component
    intent.setComponent(new ComponentName(
        "$pkg",
        "$pkg.$cls"
    ));

    // Attach Handoff data
    intent.putExtra(Intent.EXTRA_HANDOFF_ACTIVITY_DATA, handoffData);

    // Set target device type
    intent.putExtra(Intent.EXTRA_HANDOVER_TARGET_DEVICE, targetDeviceType);

    // Grant local permissions
    intent.addFlags(Intent.FLAG_GRANT_LOCAL_AND_URI_PERMISSIONS);

    return intent;
}

/**
 * Receive and process Handoff Intent.
 */
public static HandoffActivityData processHandoffIntent(Intent intent) {
    if (intent.hasCategory(Intent.CATEGORY_HANDOFF)) {
        return intent.getParcelableExtra(
            Intent.EXTRA_HANDOFF_ACTIVITY_DATA
        );
    }
    return null;
}
""".trimIndent()
}
