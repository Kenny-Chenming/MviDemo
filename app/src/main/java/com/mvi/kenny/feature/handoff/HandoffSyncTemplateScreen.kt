package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffSyncTemplateScreen — 多设备同步框架集成模板
// Multi-Device Sync Framework Integration Template Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 6: Sync Template
// 功能：配置 Room/DataStore/Cloud Sync 与 Handoff 集成方式

@Composable
fun HandoffSyncTemplateScreen(
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
            text = "多设备同步框架集成",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "配置 Room/DataStore/Cloud Sync 与 Handoff 集成 / Configure Room/DataStore/Cloud Sync integration with Handoff",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sync configuration / 同步配置
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "同步策略 / Sync Strategy", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                // Sync method / 同步方法
                Text("同步方法 / Sync Method", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                SyncMethod.entries.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = state.syncConfig.syncMethod == method,
                            onClick = {
                                onIntent(HandoffIntent.UpdateSyncConfig(
                                    state.syncConfig.copy(syncMethod = method)
                                ))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(method.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Conflict resolution / 冲突解决
                Text("冲突解决策略 / Conflict Resolution", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                ConflictResolution.entries.forEach { resolution ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = state.syncConfig.conflictResolution == resolution,
                            onClick = {
                                onIntent(HandoffIntent.UpdateSyncConfig(
                                    state.syncConfig.copy(conflictResolution = resolution)
                                ))
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(resolution.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.syncConfig.enabled,
                        onCheckedChange = {
                            onIntent(HandoffIntent.UpdateSyncConfig(
                                state.syncConfig.copy(enabled = it)
                            ))
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("启用多设备同步 / Enable multi-device sync")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Integration template code / 集成模板代码
        Text(text = "集成模板 / Integration Template", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Room + Handoff 集成示例 / Room + Handoff Integration Example",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = syncTemplateCode,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Best practices / 最佳实践
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💡 同步最佳实践 / Sync Best Practices", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Handoff 状态与云端同步状态冲突时，建议采用「最后写入优先」+ 用户确认策略 / When Handoff state conflicts with cloud sync, use 'Last Write Wins' + user confirmation",
                    "使用 Room 的 ConflictResolutionStrategy 处理并发写入 / Use Room's ConflictResolutionStrategy for concurrent writes",
                    "Handoff 传输建议使用增量同步而非全量同步，减少带宽消耗 / Use incremental sync over full sync to reduce bandwidth",
                    "DataStore 适合小规模配置数据，Room 适合结构化业务数据 / DataStore for small config data, Room for structured business data"
                ).forEach { practice ->
                    Text("• $practice", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

private val syncTemplateCode = """
// Handoff + Room Sync Integration Template
// Handoff 状态与 Room 数据库同步集成模板

class HandoffSyncManager(
    private val db: AppDatabase,
    private val handoffData: HandoffActivityData
) {
    private val documentDao = db.documentDao()

    /**
     * 将 Handoff 数据同步到 Room 数据库
     * Sync Handoff data to Room database
     */
    suspend fun syncToLocal() {
        val docs = handoffData.getDocuments()
        docs.forEach { doc ->
            val localDoc = documentDao.findById(doc.id)
            when (syncConfig.conflictResolution) {
                ConflictResolution.LAST_WRITE_WINS -> {
                    // 最后写入优先 — 直接覆盖
                    // Last Write Wins — direct overwrite
                    if (localDoc == null || doc.lastModified > localDoc.lastModified) {
                        documentDao.upsert(doc)
                    }
                }
                ConflictResolution.USER_CONFIRMED -> {
                    // 需要用户确认后再处理
                    // Requires user confirmation before processing
                    pendingConflicts.add(doc)
                }
                ConflictResolution.KEEP_BOTH -> {
                    // 保留双方版本
                    // Keep both versions
                    documentDao.insert(doc.copy(id = "${"$"}{doc.id}_handoff_copy"))
                }
                ConflictResolution.PRIORITY_BASED -> {
                    // 按优先级处理
                    // Process by priority
                    if (doc.priority > (localDoc?.priority ?: 0)) {
                        documentDao.upsert(doc)
                    }
                }
            }
        }
    }

    /**
     * 处理数据冲突
     * Handle data conflicts
     */
    suspend fun resolveConflicts(userChoice: ConflictResolution) {
        // 实现冲突解决逻辑 / Implement conflict resolution logic
    }
}
""".trimIndent()
