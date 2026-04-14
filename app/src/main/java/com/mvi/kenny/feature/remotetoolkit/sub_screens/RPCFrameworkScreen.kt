package com.mvi.kenny.feature.remotetoolkit.sub_screens

import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitState
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitIntent
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitEffect
import com.mvi.kenny.feature.remotetoolkit.RemoteToolkitColors


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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvi.kenny.feature.remotetoolkit.RemoteFunction

// =============================================================
// RPCFrameworkScreen — 类型安全 RPC 框架页面
// RPC Framework / RPC 框架
// =============================================================

@Composable
fun RPCFrameworkScreen(
    state: RemoteToolkitState,
    onIntent: (RemoteToolkitIntent) -> Unit
) {
    val scrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteToolkitColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Type-Safe RPC Framework",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RemoteToolkitColors.OnSurface
            )
        }

        // ============================================================
        // Explanation Card / 说明卡片
        // ============================================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "How it works / 工作原理",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = RemoteToolkitColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Define remote composable functions with type-safe signatures. The framework auto-generates client stubs and handles serialization. Functions are called as if they were local, but execute on the remote device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RemoteToolkitColors.OnSurfaceVariant
                    )
                }
            }
        }

        // ============================================================
        // Define Functions Section / 定义函数区
        // ============================================================
        item {
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
                        Text(
                            text = "Remote Functions / 远程函数",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = RemoteToolkitColors.OnSurface
                        )
                        IconButton(
                            onClick = { onIntent(RemoteToolkitIntent.AddRemoteFunction) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RemoteToolkitColors.Purple.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Function",
                                tint = RemoteToolkitColors.Purple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sample definition / 示例定义
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D1117))
                            .border(1.dp, RemoteToolkitColors.Outline, RoundedCornerShape(8.dp))
                            .horizontalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = """@RemoteFunction
fun getUserProfile(userId: String): RemoteResult<UserProfile>

// Auto-generated client stub:
val stub = UserProfileClient()
val profile = stub.getUserProfile(userId)

@RemoteFunction
fun submitOrder(order: Order): RemoteResult<OrderResult>

@RemoteFunction
fun getRecommendations(userId: String): RemoteResult<List<Product>>""",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color(0xFFE1E4E8)
                        )
                    }
                }
            }
        }

        // ============================================================
        // Registered Functions List / 已注册函数列表
        // ============================================================
        item {
            Text(
                text = "Registered Functions / 已注册函数",
                style = MaterialTheme.typography.titleMedium,
                color = RemoteToolkitColors.OnSurface
            )
        }

        if (state.registeredFunctions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No functions registered yet. Click + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RemoteToolkitColors.OnSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.registeredFunctions, key = { it.id }) { fn ->
                FunctionCard(
                    function = fn,
                    onRemove = { onIntent(RemoteToolkitIntent.RemoveRemoteFunction(fn.id)) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// =============================================================
// FunctionCard — 函数卡片
// =============================================================
@Composable
private fun FunctionCard(
    function: RemoteFunction,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RemoteToolkitColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RemoteToolkitColors.Purple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "@RemoteFunction",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = RemoteToolkitColors.Purple
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = function.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = RemoteToolkitColors.OnSurface
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = RemoteToolkitColors.Coral,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Returns: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.Teal
                )
                Text(
                    text = function.returnType,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Params: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.Teal
                )
                Text(
                    text = function.parameters.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = RemoteToolkitColors.OnSurfaceVariant
                )
            }
        }
    }
}
