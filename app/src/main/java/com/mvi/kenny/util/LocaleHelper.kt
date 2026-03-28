package com.mvi.kenny.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * ============================================================
 * RTL_LANGUAGES — RTL 语言集合
 * ============================================================
 * RTL（Right-to-Left，从右到左）语言需要特殊的布局方向处理。
 * 这些语言的文字方向是从右到左，UI 布局也需要镜像。
 *
 * @see LocaleHelper.isRtlLanguage 判断语言是否为 RTL
 */
val RTL_LANGUAGES = setOf(
    SettingsManager.LANGUAGE_ARABIC,
    SettingsManager.LANGUAGE_PERSIAN,
    SettingsManager.LANGUAGE_URDU,
    SettingsManager.LANGUAGE_HEBREW
)

/**
 * ============================================================
 * LocaleHelper — 语言切换工具类
 * ============================================================
 * 提供语言相关的配置和查询方法。
 * Android 7.0 (API 24) 及以上支持多语言切换，但需要通过 Context 注入实现。
 *
 * 核心原理：
 * Android 的资源（字符串、布局方向）由 Context 决定。
 * 通过 createConfigurationContext() 创建一个注入了目标 Locale 的新 Context，
 * 然后用这个新 Context 替换 Activity 的 base Context，
 * Activity 就会使用新的语言资源。
 *
 * @see SettingsManager 应用设置管理器
 * @see BaseActivity.attachBaseContext 使用此工具的关键入口
 */
object LocaleHelper {

    /**
     * 设置语言并返回配置后的 Context
     * —————————————————————————————————————————————————————
     * 这是语言切换的核心方法。
     * 读取目标语言的 Locale 对象 → 调用 updateResources() 创建新的配置 Context。
     *
     * 使用场景：
     * - BaseActivity.attachBaseContext()
     * - 任何需要以特定语言显示内容的 Context
     *
     * @param context 原始 Context（通常是 Activity 的 base Context）
     * @param language 语言代码，如 "zh"、"en"、"ar"
     * @return 注入了目标语言的 Context
     */
    fun setLocale(context: Context, language: String): Context {
        val locale = getLocale(language)
        return updateResources(context, locale)
    }

    /**
     * 根据语言代码获取对应的 Locale 对象
     * —————————————————————————————————————————————————————
     * @param language 语言代码（如 "zh"、"ar"）
     * @return 对应的 Java Locale 对象
     */
    fun getLocale(language: String): Locale {
        return when (language) {
            SettingsManager.LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
            SettingsManager.LANGUAGE_ENGLISH -> Locale.ENGLISH
            SettingsManager.LANGUAGE_ARABIC -> Locale("ar")
            SettingsManager.LANGUAGE_PERSIAN -> Locale("fa")
            SettingsManager.LANGUAGE_URDU -> Locale("ur", "PK")  // 乌尔都语（巴基斯坦）
            SettingsManager.LANGUAGE_HEBREW -> Locale("he", "IL")  // 希伯来语（以色列）
            // 跟随系统时使用系统默认 Locale
            else -> getSystemLocale()
        }
    }

    /**
     * 判断语言是否为 RTL（从右到左）
     * —————————————————————————————————————————————————————
     * RTL 语言在阿拉伯语系和希伯来语系中使用，UI 需要镜像布局。
     * Compose 的 layoutDirection = LayoutDirection.Rtl 会自动处理镜像。
     *
     * 判断逻辑：
     * 1. 精确匹配 RTL_LANGUAGES 集合中的语言
     * 2. 前缀匹配（兼容以 "ar"/"fa"/"he"/"ur" 开头的语言代码）
     *
     * @param language 语言代码
     * @return true 表示 RTL 语言，需要镜像布局
     */
    fun isRtlLanguage(language: String): Boolean {
        return language in RTL_LANGUAGES ||
            language.startsWith("ar") ||
            language.startsWith("fa") ||
            language.startsWith("he") ||
            language.startsWith("ur")
    }

    /**
     * 为 Activity 应用语言配置（直接修改 Activity 的资源配置）
     * —————————————————————————————————————————————————————
     * 与 setLocale() 的区别：
     * - setLocale() 创建新 Context，不修改原 Activity
     * - applyLocale() 直接修改 Activity 当前资源的语言配置
     *
     * 使用场景：
     * - 在 Activity 内部切换语言（不需要 recreate）
     * - 某些不方便 recreate() 的场景
     *
     * @param activity 目标 Activity
     * @param language 语言代码
     */
    fun applyLocale(activity: Activity, language: String) {
        val locale = getLocale(language)
        Locale.setDefault(locale)
        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)  // 设置 RTL 方向
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    /**
     * 获取语言对应的显示名称（本地化语言名）
     * —————————————————————————————————————————————————————
     * 用于设置页面中显示语言选项的用户友好名称。
     *
     * @param language 语言代码
     * @return 语言的本地化名称，如 "简体中文"、"English"、"العربية"
     */
    fun getLanguageDisplayName(language: String): String {
        return when (language) {
            SettingsManager.LANGUAGE_CHINESE -> "简体中文"
            SettingsManager.LANGUAGE_ENGLISH -> "English"
            SettingsManager.LANGUAGE_ARABIC -> "العربية"
            SettingsManager.LANGUAGE_PERSIAN -> "فارسی"
            SettingsManager.LANGUAGE_URDU -> "اردو"
            SettingsManager.LANGUAGE_HEBREW -> "עברית"
            else -> "跟随系统"
        }
    }

    /**
     * 获取系统默认 Locale
     * —————————————————————————————————————————————————————
     * Android 6.0 (API 23) 以下使用 Locale.getDefault()，
     * Android 7.0+ 推荐使用 LocaleList.getDefault()
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault()
        }
    }

    /**
     * 更新 Context 的资源配置
     * —————————————————————————————————————————————————————
     * 创建并返回一个新的 Configuration Context，
     * 该 Context 的 resources 使用指定的 Locale 和 LayoutDirection。
     *
     * 关键 API：Context.createConfigurationContext()
     * 这个方法在 Android 7.0+ 可以正确处理语言资源回退。
     *
     * @param context 原始 Context
     * @param locale 目标 Locale
     * @return 配置了指定语言的 Context
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)  // 设置 RTL/LTR 方向
        return context.createConfigurationContext(config)
    }
}
