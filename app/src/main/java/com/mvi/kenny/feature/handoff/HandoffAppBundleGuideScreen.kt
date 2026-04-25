package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffAppBundleGuideScreen — App Bundle 联动指南
// App Bundle Handoff Guide Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 7: App Bundle Guide
// 功能：当目标设备未安装 App 时，使用 Play Install API 引导安装

@Composable
fun HandoffAppBundleGuideScreen(
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
        Text(
            text = "App Bundle 联动",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "目标设备未安装 App 时，使用 Play Install API 引导安装 / When target device doesn't have the app, use Play Install API to guide installation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Bundle Configuration / App Bundle 配置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "App 配置 / App Configuration", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.appBundleConfig.packageName,
                    onValueChange = {
                        onIntent(HandoffIntent.UpdateAppBundleConfig(
                            state.appBundleConfig.copy(packageName = it)
                        ))
                    },
                    label = { Text("包名 / Package Name") },
                    placeholder = { Text("com.example.myapp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("目标设备已安装 App / App installed on target device")
                    Switch(
                        checked = state.appBundleConfig.hasInstalled,
                        onCheckedChange = {
                            onIntent(HandoffIntent.UpdateAppBundleConfig(
                                state.appBundleConfig.copy(hasInstalled = it)
                            ))
                        }
                    )
                }

                if (state.appBundleConfig.hasInstalled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.appBundleConfig.installUrl,
                        onValueChange = {
                            onIntent(HandoffIntent.UpdateAppBundleConfig(
                                state.appBundleConfig.copy(installUrl = it)
                            ))
                        },
                        label = { Text("Play Store 安装链接 / Install URL") },
                        placeholder = { Text("https://play.google.com/...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Flow diagram / 流程图
        Text(text = "Handoff → App Bundle 流程 / Flow", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val steps = listOf(
                    "1️⃣ 源设备发起 Handoff / Source initiates Handoff" to "检测目标设备是否安装 App / Check if app is installed on target",
                    "2️⃣ 目标设备未安装 App / App not installed on target" to "系统弹出安装引导 / System shows install prompt",
                    "3️⃣ 用户确认安装 / User confirms installation" to "通过 Play Install API 发起安装 / Initiate install via Play Install API",
                    "4️⃣ 安装完成后自动续接 / Auto-continue after install" to "目标设备安装完成后，Activity 自动恢复 / Activity auto-restores after install completes",
                    "5️⃣ Handoff 状态传递 / Handoff state transfer" to "通过 App 的云端账户同步 Handoff 状态 / Sync Handoff state via app's cloud account"
                )
                steps.forEachIndexed { index, (title, desc) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.4f)
                        )
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.1f)
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                    if (index < steps.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Code template / 代码模板
        Text(text = "Play Install API 集成模板 / Play Install API Template", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = playInstallApiTemplate,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key considerations / 关键注意事项
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚠️ 关键注意事项 / Key Considerations", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "安装取消处理：用户可能取消安装，需要提供手动续接入口 / Handle install cancellation — provide manual resume option",
                    "Handoff 状态需要通过云端账户中转，不能依赖本地传输 / Handoff state needs cloud account relay, not local transfer",
                    "需要处理目标设备 Play Store 不可用的情况（如部分设备无 Play Store）/ Handle cases where Play Store is unavailable on target device",
                    "安装完成后的 Activity 恢复需要与 App 的 onCreate 生命周期协调 / Activity restoration after install needs coordination with App onCreate lifecycle"
                ).forEach { note ->
                    Text("• $note", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

private val playInstallApiTemplate = """
// Play Install API — App Bundle 联动模板
// Play Install API — App Bundle Handoff Template

import android.content.Context
import androidx.annotation.OptIn
import androidx.play.core舍入.ExperimentalUpdateEncryption
import com.google.android.gms.play.appupdate.AppUpdateManagerFactory
import com.google.android.gms.play.appupdate.AppUpdateOptions
import com.google.android.gms.play.integrity.model.InstallAPI

/**
 * 处理目标设备未安装 App 时的引导安装
 * Handle install guidance when app is not installed on target device
 */
class AppBundleHandoffHelper(private val context: Context) {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    /**
     * 检查目标设备是否安装 App，并引导安装
     * Check if app is installed on target device and guide installation
     */
    suspend fun handleHandoffInstall(
        packageName: String,
        handoffToken: String
    ): InstallResult {
        val installApi = InstallAPI(context)

        // Step 1: Check if app is already installed
        // 步骤1: 检查 App 是否已安装
        val installStatus = installApi.getInstallStatus(packageName)

        return when {
            installStatus == InstallAPI.InstallStatus.INSTALLED -> {
                // App 已安装，可以直接发起 Handoff
                InstallResult.READY_FOR_HANDOFF
            }

            installStatus == InstallAPI.InstallStatus.NOT_INSTALLED -> {
                // App 未安装，发起安装引导
                // App not installed, initiate install guidance
                val sessionId = installApi.requestInstall(
                    packageName,
                    handoffToken,  // 传递 Handoff token 以便安装后自动续接 / Pass Handoff token for auto-resume after install
                    InstallAPI.InstallPriority.HIGH
                )
                InstallResult.INSTALL_REQUESTED(sessionId)
            }

            installStatus == InstallAPI.InstallStatus.INSTALLING -> {
                // 正在安装中，显示进度
                InstallResult.INSTALL_IN_PROGRESS
            }

            else -> InstallResult.UNAVAILABLE
        }
    }

    /**
     * 监听安装完成并自动续接 Handoff
     * Monitor install completion and auto-resume Handoff
     */
    fun observeInstallAndResume(
        sessionId: String,
        onReady: () -> Unit,
        onFailed: (Throwable) -> Unit
    ) {
        installApi.getInstallSessionState(sessionId) { state ->
            when (state.status) {
                InstallAPI.SessionStatus.COMPLETED -> {
                    // 安装完成，发起 Handoff
                    // Install completed, initiate Handoff
                    onReady()
                }
                InstallAPI.SessionStatus.FAILED -> {
                    onFailed(Exception("Install failed"))
                }
                else -> { /* Continue monitoring */ }
            }
        }
    }
}

enum class InstallResult {
    READY_FOR_HANDOFF,
    INSTALL_REQUESTED(String),  // sessionId
    INSTALL_IN_PROGRESS,
    UNAVAILABLE
}
""".trimIndent()
