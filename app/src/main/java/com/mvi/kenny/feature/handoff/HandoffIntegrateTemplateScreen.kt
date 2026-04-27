package com.mvi.kenny.feature.handoff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

// =============================================================
// HandoffIntegrateTemplateScreen — Handoff 集成模板工具
// 状态打包配置页面 / Activity Data Builder Screen
// =============================================================
// PRD-153 | Android 17 Handoff API Cross-Device Continuity Dev Toolkit
//
// Tool 1: Handoff Integration Template SDK
// 功能：配置 onHandoffActivityRequested() 集成模板
//
// Features / 功能:
// - Add/remove/edit data fields to serialize / 添加/删除/编辑序列化数据字段
// - Generate Kotlin/Java integration code / 生成 Kotlin/Java 集成代码
// - Copy code to clipboard / 复制代码到剪贴板

@Composable
fun HandoffIntegrateTemplateScreen(
    state: HandoffState,
    onIntent: (HandoffIntent) -> Unit
) {
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var editingFieldIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header / 标题
        Text(
            text = "onHandoffActivityRequested() 集成模板",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "配置 Activity 状态序列化字段，生成集成代码 / Configure Activity state serialization fields and generate integration code",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Data fields section / 数据字段区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "数据字段 / Data Fields (${state.activityDataFields.size})",
                style = MaterialTheme.typography.titleSmall
            )
            FilledTonalButton(
                onClick = { showAddFieldDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加字段 / Add Field", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (state.activityDataFields.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无数据字段 / No data fields",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击「添加字段」配置需要序列化的 Activity 状态 / Click 'Add Field' to configure Activity state fields to serialize",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.activityDataFields) { index, field ->
                    DataFieldCard(
                        field = field,
                        index = index,
                        onRemove = { onIntent(HandoffIntent.RemoveDataField(index)) },
                        onEdit = { editingFieldIndex = index }
                    )
                }

                // Generate code button / 生成代码按钮
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onIntent(HandoffIntent.GenerateCode) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.activityDataFields.isNotEmpty() && !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("生成集成代码 / Generate Integration Code")
                    }
                }
            }
        }

        // Generated code section / 生成的代码区域
        if (state.generatedCodeTemplates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "生成的代码 / Generated Code",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            state.generatedCodeTemplates.forEach { template ->
                GeneratedCodeCard(
                    template = template,
                    onCopy = { /* Copy handled via effect */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add field dialog / 添加字段对话框
    if (showAddFieldDialog || editingFieldIndex >= 0) {
        AddDataFieldDialog(
            existingField = if (editingFieldIndex >= 0) state.activityDataFields.getOrNull(editingFieldIndex) else null,
            onDismiss = {
                showAddFieldDialog = false
                editingFieldIndex = -1
            },
            onConfirm = { field ->
                if (editingFieldIndex >= 0) {
                    onIntent(HandoffIntent.UpdateDataField(editingFieldIndex, field))
                } else {
                    onIntent(HandoffIntent.AddDataField(field))
                }
                showAddFieldDialog = false
                editingFieldIndex = -1
            }
        )
    }
}

// =============================================================
// DataFieldCard — 数据字段卡片
// =============================================================
/**
 * Card displaying a single data field configuration.
 * 显示单个数据字段配置。
 */
@Composable
private fun DataFieldCard(
    field: DataField,
    index: Int,
    onRemove: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (field.isEncrypted)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = field.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                    if (field.isRequired) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Required", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    if (field.isEncrypted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("🔒 Encrypted", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(20.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "类型: ${field.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Text("✏️", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// =============================================================
// GeneratedCodeCard — 生成的代码卡片
// =============================================================
/**
 * Card displaying generated integration code.
 * 显示生成的集成代码。
 */
@Composable
private fun GeneratedCodeCard(
    template: HandoffCodeTemplate,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.language.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制 / Copy")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = template.code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp),
                    maxLines = 20
                )
            }
        }
    }
}

// =============================================================
// AddDataFieldDialog — 添加数据字段对话框
// =============================================================
/**
 * Dialog for adding or editing a data field.
 * 添加或编辑数据字段的对话框。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDataFieldDialog(
    existingField: DataField?,
    onDismiss: () -> Unit,
    onConfirm: (DataField) -> Unit
) {
    var name by remember { mutableStateOf(existingField?.name ?: "") }
    var type by remember { mutableStateOf(existingField?.type ?: "String") }
    var isRequired by remember { mutableStateOf(existingField?.isRequired ?: false) }
    var isEncrypted by remember { mutableStateOf(existingField?.isEncrypted ?: false) }

    val typeOptions = listOf("String", "Int", "Long", "Boolean", "Float", "Double", "Parcelable", "Serializable")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingField != null) "编辑字段 / Edit Field" else "添加字段 / Add Field")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("字段名 / Field Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Type dropdown / 类型下拉
                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("类型 / Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = isRequired, onClick = { isRequired = !isRequired }, role = Role.Checkbox),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isRequired, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("必填 / Required")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = isEncrypted, onClick = { isEncrypted = !isEncrypted }, role = Role.Checkbox),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isEncrypted, onCheckedChange = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("加密传输 / Encrypted")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(DataField(name = name, type = type, isRequired = isRequired, isEncrypted = isEncrypted))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确认 / Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消 / Cancel")
            }
        }
    )
}
