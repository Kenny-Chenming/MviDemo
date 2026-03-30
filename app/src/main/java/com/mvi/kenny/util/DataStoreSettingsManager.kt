package com.mvi.kenny.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mvi.kenny.util.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ============================================================
 * DataStoreSettingsManager — DataStore 持久化设置管理器
 * ============================================================
 * 使用 Jetpack DataStore（Preferences DataStore）替代 SharedPreferences
 * 进行应用设置的持久化存储。
 *
 * DataStore 优势：
 * - 完全异步 API（SharedPreferences 为同步 API）
 * - 类型安全（Flow<T>）
 * - 符合 Kotlin 协程和 Flow 最佳实践
 * - 支持数据迁移
 *
 * 本类展示如何在 MVI 项目中使用 DataStore 进行持久化，
 * 替代传统的 SharedPreferences，演示现代 Android 数据持久化方案。
 *
 * DataStore 使用场景：
 * - 用户偏好设置（主题、语言、字体大小等）
 * - 应用配置信息
 * - 简单键值对数据存储
 *
 * 注意：对于复杂结构化数据，推荐使用 Proto DataStore。
 *
 * @see SettingsManager 基于 SharedPreferences 的旧版实现（对比参考）
 * @see ThemeMode 主题模式枚举
 *
 * Usage example / 使用示例：
 * ```
 * // 在 BaseActivity 或 Application 中初始化
 * val dataStoreManager = DataStoreSettingsManager(context)
 *
 * // 读取主题模式（Flow）
 * lifecycleScope.launch {
 *     dataStoreManager.themeModeFlow.collect { themeMode ->
 *         // update UI
 *     }
 * }
 *
 * // 写入主题模式
 * lifecycleScope.launch {
 *     dataStoreManager.setThemeMode(ThemeMode.DARK)
 * }
 * ```
 */

// DataStore 扩展属性，需要在 Context 上调用
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings_datastore"
)

/**
 * ============================================================
 * DataStoreSettingsManager — 设置管理器
 * ============================================================
 * 使用 Preferences DataStore 存储应用设置。
 * 支持 Flow 订阅设置变化，实现响应式配置更新。
 *
 * @param context Application Context（避免内存泄漏）
 */
class DataStoreSettingsManager(private val context: Context) {

    // ============================================================
    // Preferences Keys — 定义存储键
    // ============================================================

    /** 主题模式存储键 */
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode_datastore")

    /** 语言设置存储键 */
    private val LANGUAGE_KEY = stringPreferencesKey("language_datastore")

    // ============================================================
    // Flow API — 响应式读取（推荐方式）
    // ============================================================

    /**
     * 主题模式 Flow
     * 当 DataStore 中的主题模式变化时，自动向下游发射新值。
     * 初始值为 ThemeMode.SYSTEM。
     *
     * @return Flow<ThemeMode> 主题模式流
     */
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val value = preferences[THEME_MODE_KEY]
            ThemeMode.fromString(value)
        }

    /**
     * 语言设置 Flow
     * 当 DataStore 中的语言设置变化时，自动向下游发射新值。
     * 初始值为 LANGUAGE_FOLLOW_SYSTEM。
     *
     * @return Flow<String> 语言代码流
     */
    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: SettingsManager.LANGUAGE_FOLLOW_SYSTEM
        }

    /**
     * 完整设置 Flow（用于一次性获取所有设置）
     *
     * @return Flow<SettingsData> 所有设置的数据类流
     */
    val settingsFlow: Flow<SettingsData> = context.dataStore.data
        .map { preferences ->
            SettingsData(
                themeMode = ThemeMode.fromString(preferences[THEME_MODE_KEY]),
                language = preferences[LANGUAGE_KEY] ?: SettingsManager.LANGUAGE_FOLLOW_SYSTEM
            )
        }

    // ============================================================
    // 写入 API — 修改设置
    // ============================================================

    /**
     * 设置主题模式
     * DataStore.edit 是原子操作，保证数据一致性。
     *
     * @param themeMode 新的主题模式
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /**
     * 设置语言
     *
     * @param language 新的语言代码（如 "zh"、"en"）
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    /**
     * 清空所有设置（恢复默认值）
     */
    suspend fun clearAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // ============================================================
    // 便捷扩展 — Flow → 常用类型
    // ============================================================

    /**
     * 判断是否为深色主题（Flow）
     * 组合 themeModeFlow 和系统深色模式判断。
     *
     * @param isSystemDark 系统是否处于深色模式
     * @return Flow<Boolean> 是否显示深色主题
     */
    fun isDarkThemeFlow(isSystemDark: Boolean): Flow<Boolean> {
        return themeModeFlow.map { mode ->
            when (mode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemDark
            }
        }
    }
}

/**
 * ============================================================
 * SettingsData — 设置数据快照
 * ============================================================
 * 用于一次性传递所有设置数据。
 *
 * @param themeMode 当前主题模式
 * @param language 当前语言代码
 */
data class SettingsData(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = SettingsManager.LANGUAGE_FOLLOW_SYSTEM
)
