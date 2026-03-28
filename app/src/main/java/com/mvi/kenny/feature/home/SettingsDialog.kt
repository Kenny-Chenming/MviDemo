package com.mvi.kenny.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mvi.kenny.R
import com.mvi.kenny.util.SettingsManager
import com.mvi.kenny.util.ThemeMode

/**
 * ============================================================
 * SettingsDialog — 主题和语言设置对话框
 * ============================================================
 * 提供三种主题模式（Light/Dark/System）和多语言切换。
 * 语言切换后会触发 Activity recreate() 使配置生效。
 *
 * @param currentThemeMode 当前主题模式
 * @param currentLanguage 当前语言代码
 * @param onThemeModeChange 主题模式变更回调
 * @param onLanguageChange 语言变更回调，调用后会触发 Activity recreate()
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun SettingsDialog(
    currentThemeMode: ThemeMode,
    currentLanguage: String,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ====================== 主题设置 ======================
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // selectableGroup() 优化可访问性，同组 RadioButton 联动
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeModeRadio(
                            mode = mode,
                            isSelected = currentThemeMode == mode,
                            onSelect = { onThemeModeChange(mode) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // ====================== 语言设置 ======================
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.selectableGroup()) {
                    // 跟随系统
                    LanguageItem(
                        language = SettingsManager.LANGUAGE_FOLLOW_SYSTEM,
                        displayName = "跟随系统",
                        isSelected = currentLanguage == SettingsManager.LANGUAGE_FOLLOW_SYSTEM,
                        onSelect = { onLanguageChange(SettingsManager.LANGUAGE_FOLLOW_SYSTEM) }
                    )
                    // 简体中文
                    LanguageItem(
                        language = SettingsManager.LANGUAGE_CHINESE,
                        displayName = "简体中文",
                        isSelected = currentLanguage == SettingsManager.LANGUAGE_CHINESE,
                        onSelect = { onLanguageChange(SettingsManager.LANGUAGE_CHINESE) }
                    )
                    // English
                    LanguageItem(
                        language = SettingsManager.LANGUAGE_ENGLISH,
                        displayName = "English",
                        isSelected = currentLanguage == SettingsManager.LANGUAGE_ENGLISH,
                        onSelect = { onLanguageChange(SettingsManager.LANGUAGE_ENGLISH) }
                    )
                    // اردو (Urdu) - RTL 示例
                    LanguageItem(
                        language = SettingsManager.LANGUAGE_URDU,
                        displayName = "اردو (Urdu)",
                        isSelected = currentLanguage == SettingsManager.LANGUAGE_URDU,
                        onSelect = { onLanguageChange(SettingsManager.LANGUAGE_URDU) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_ok))
            }
        }
    )
}

/**
 * 主题模式单选行
 * —————————————————————————————————————————————————————
 *
 * @param mode 主题模式
 * @param isSelected 是否选中
 * @param onSelect 选择此选项时的回调
 */
@Composable
private fun ThemeModeRadio(
    mode: ThemeMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // selectable + Role.RadioButton 实现单选语义（无障碍支持）
            .selectable(selected = isSelected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = null)  // onClick 由 Row 处理
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = when (mode) {
                ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                ThemeMode.DARK -> stringResource(R.string.theme_dark)
                ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
            },
            style = MaterialTheme.typography.bodyLarge
        )
        // 选中时在右侧显示勾图标
        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 语言选项单选行
 * —————————————————————————————————————————————————————
 *
 * @param language 语言代码
 * @param displayName 显示名称
 * @param isSelected 是否选中
 * @param onSelect 选择此选项时的回调
 */
@Composable
private fun LanguageItem(
    language: String,
    displayName: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = displayName, style = MaterialTheme.typography.bodyLarge)
        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
