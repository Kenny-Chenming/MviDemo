package com.mvi.kenny.util

import android.content.Context
import android.content.SharedPreferences

/**
 * ============================================================
 * SettingsManager — 应用设置管理器
 * ============================================================
 * 封装 SharedPreferences，提供类型安全的读写接口。
 * 使用单例模式，通过 getInstance(context) 获取实例。
 *
 * 管理的设置项：
 * - themeMode：主题模式（Light / Dark / System）
 * - language：界面语言（zh / en / ar / fa / ur / he / follow_system）
 *
 * 注意：写入使用 commit()（同步）而非 apply()（异步），
 * 因为语言切换后需要立即 recreate()，必须保证写入完成。
 *
 * @see ThemeMode 主题模式枚举
 * @see LocaleHelper 语言切换工具类
 */
class SettingsManager(context: Context) {

    /** SharedPreferences 实例 */
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ============================================================
    // themeMode
    // ============================================================
    /** 主题模式（Light / Dark / System）
     * —————————————————————————————————————————————————————
     * 读取：直接从 SharedPreferences 解析字符串为 ThemeMode
     * 写入：commit() 同步写入，确保 recreate() 前已持久化
     */
    var themeMode: ThemeMode
        get() = ThemeMode.fromString(prefs.getString(KEY_THEME_MODE, null))
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).commit()
        }

    // ============================================================
    // language
    // ============================================================
    /** 界面语言
     * —————————————————————————————————————————————————————
     * 读取：默认为 LANGUAGE_FOLLOW_SYSTEM（跟随系统）
     * 写入：commit() 同步写入，确保 recreate() 前已持久化
     *
     * 可选值：zh（简体中文）、en（English）、ar（العربية）、fa（فارسی）、
     *         ur（اردو）、he（עברית）、follow_system（跟随系统）
     */
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, LANGUAGE_FOLLOW_SYSTEM) ?: LANGUAGE_FOLLOW_SYSTEM
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value).commit()
        }

    // ============================================================
    // Companion — 单例工厂和常量定义
    // ============================================================
    companion object {
        /** SharedPreferences 文件名 */
        private const val PREFS_NAME = "app_settings"

        /** SharedPreferences Key：主题模式 */
        private const val KEY_THEME_MODE = "theme_mode"

        /** SharedPreferences Key：界面语言 */
        private const val KEY_LANGUAGE = "language"

        // -------------------- 语言常量 --------------------
        /** 跟随系统语言（默认） */
        const val LANGUAGE_FOLLOW_SYSTEM = "follow_system"

        /** 简体中文 */
        const val LANGUAGE_CHINESE = "zh"

        /** 英语 */
        const val LANGUAGE_ENGLISH = "en"

        /** 阿拉伯语（RTL） */
        const val LANGUAGE_ARABIC = "ar"

        /** 波斯语/波斯语（RTL） */
        const val LANGUAGE_PERSIAN = "fa"

        /** 乌尔都语（RTL） */
        const val LANGUAGE_URDU = "ur"

        /** 希伯来语（RTL） */
        const val LANGUAGE_HEBREW = "he"

        /** 单例引用（volatile 保证线程安全） */
        @Volatile
        private var instance: SettingsManager? = null

        /**
         * 获取 SettingsManager 单例
         * 使用 DCL（Double-Checked Locking）保证线程安全
         *
         * @param context ApplicationContext 或 Activity Context 均可
         *               内部会自动转换为 applicationContext 避免内存泄漏
         * @return SettingsManager 单例
         */
        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * ============================================================
 * ThemeMode — 主题模式枚举
 * ============================================================
 *
 * @see SettingsManager.themeMode 应用设置项
 * @see BaseActivity 中根据 ThemeMode 决定深色/浅色模式
 */
enum class ThemeMode {
    /** 浅色模式 */
    LIGHT,

    /** 深色模式 */
    DARK,

    /** 跟随系统设置 */
    SYSTEM;

    /**
     * 从字符串恢复 ThemeMode
     *
     * @param value 字符串值（通常来自 SharedPreferences）
     * @return 对应的 ThemeMode，无法识别时默认返回 SYSTEM
     */
    companion object {
        fun fromString(value: String?): ThemeMode {
            return when (value) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                else -> SYSTEM
            }
        }
    }
}
