package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.mvi.kenny.feature.remotetoolkit.TLSConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// =============================================================
// SecurityLayerScreen — 安全传输层配置页面
// Security Layer / 安全层
// =============================================================

@Composable
fun SecurityLayerScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Security Layer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
        }

        // ============================================================
        // Security Score Card / 安全评分卡片
        // ============================================================
        item {
            SecurityScoreCard(state = state)
        }

        // ============================================================
        // Security Features List / 安全特性列表
        // ============================================================
        item {
            Text(
                text = "Security Features / 安全特性",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            SecurityFeatureCard(
                title = "TLS 1.3",
                description = "Transport Layer Security with TLS 1.3",
                isEnabled = state.tlsConfig.protocol == "TLS 1.3",
                status = if (state.tlsConfig.protocol == "TLS 1.3") "Active" else "Not Configured",
                onToggle = { }
            )
        }

        item {
            SecurityFeatureCard(
                title = "Request Signing",
                description = "HMAC-SHA256 request signature verification",
                isEnabled = state.signingEnabled,
                status = if (state.signingEnabled) "Active" else "Disabled",
                onToggle = { }
            )
        }

        item {
            SecurityFeatureCard(
                title = "Anti-Tamper",
                description = "Binary integrity verification and anti-reverse engineering",
                isEnabled = state.antiTamperEnabled,
                status = if (state.antiTamperEnabled) "Active" else "Disabled",
                onToggle = { }
            )
        }

        item {
            SecurityFeatureCard(
                title = "Certificate Pinning",
                description = "Pin server certificates to prevent MITM attacks",
                isEnabled = state.certPinningEnabled,
                status = if (state.certPinningEnabled) "Configured" else "Not Configured ⚠️",
                onToggle = { }
            )
        }

        // ============================================================
        // TLS Config Panel / TLS 配置面板
        // ============================================================
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TLS Configuration / TLS 配置",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Protocol selector (simplified as text field for display)
                    Text(
                        text = "Protocol / 协议",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.tlsConfig.protocol,
                        onValueChange = { onIntent(RemoteToolkitIntent.UpdateTLSConfig(state.tlsConfig.copy(protocol = it))) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RemoteToolkitColors.Purple,
                            unfocusedBorderColor = RemoteToolkitColors.Outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Certificate Path / 证书路径",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.tlsConfig.certificatePath,
                        onValueChange = { onIntent(RemoteToolkitIntent.UpdateTLSConfig(state.tlsConfig.copy(certificatePath = it))) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., /path/to/cert.pem", color = RemoteToolkitColors.OnSurfaceVariant) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RemoteToolkitColors.Purple,
                            unfocusedBorderColor = RemoteToolkitColors.Outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Private Key Path / 私钥路径",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.tlsConfig.privateKeyPath,
                        onValueChange = { onIntent(RemoteToolkitIntent.UpdateTLSConfig(state.tlsConfig.copy(privateKeyPath = it))) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., /path/to/key.pem", color = RemoteToolkitColors.OnSurfaceVariant) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RemoteToolkitColors.Purple,
                            unfocusedBorderColor = RemoteToolkitColors.Outline
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onIntent(RemoteToolkitIntent.TestConnection) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RemoteToolkitColors.Teal)
                        ) {
                            Text("Test Connection")
                        }
                        Button(
                            onClick = { onIntent(RemoteToolkitIntent.SaveConfiguration) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RemoteToolkitColors.Purple)
                        ) {
                            Text("Save Configuration")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// SecurityScoreCard — 安全评分卡片
// =============================================================
@Composable
private fun SecurityScoreCard(state: RemoteToolkitState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Security Score / 安全评分",
                        style = MaterialTheme.typography.labelMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${state.securityScore}%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                state.securityScore >= 90 -> RemoteToolkitColors.PassColor
                                state.securityScore >= 70 -> RemoteToolkitColors.WarningColor
                                else -> RemoteToolkitColors.CriticalColor
                            }
                        )
                        if (state.securityScore >= 90) {
                            Text(" ✅", color = RemoteToolkitColors.PassColor)
                        }
                    }
                }
                SecurityScoreRing(score = state.securityScore)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { state.securityScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    state.securityScore >= 90 -> RemoteToolkitColors.PassColor
                    state.securityScore >= 70 -> RemoteToolkitColors.WarningColor
                    else -> RemoteToolkitColors.CriticalColor
                },
                trackColor = RemoteToolkitColors.SurfaceVariant
            )
        }
    }
}

// =============================================================
// SecurityScoreRing — 安全评分圆环
// =============================================================
@Composable
private fun SecurityScoreRing(score: Int) {
    val sweepAngle = score / 100f * 360f
    val color = when {
        score >= 90 -> RemoteToolkitColors.PassColor
        score >= 70 -> RemoteToolkitColors.WarningColor
        else -> RemoteToolkitColors.CriticalColor
    }
    Canvas(modifier = Modifier.size(80.dp)) {
        drawArc(
            color = RemoteToolkitColors.SurfaceVariant,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = Size(size.width, size.height)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
            size = Size(size.width, size.height)
        )
    }
}

// =============================================================
// SecurityFeatureCard — 安全特性卡片
// =============================================================
@Composable
private fun SecurityFeatureCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    status: String,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = RemoteToolkitColors.OnSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isEnabled) RemoteToolkitColors.PassColor else RemoteToolkitColors.WarningColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) RemoteToolkitColors.PassColor else RemoteToolkitColors.WarningColor
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RemoteToolkitColors.PassColor,
                    checkedTrackColor = RemoteToolkitColors.PassColor.copy(alpha = 0.3f),
                    uncheckedThumbColor = RemoteToolkitColors.OnSurfaceVariant,
                    uncheckedTrackColor = RemoteToolkitColors.SurfaceVariant
                )
            )
        }
    }
}
