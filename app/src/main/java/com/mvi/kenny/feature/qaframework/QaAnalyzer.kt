package com.mvi.kenny.feature.qaframework

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/**
 * QA 问题分析器 — 分析 UI 层次结构 XML，检测可访问性和质量问题
 * 使用 Android 内置的 XmlPullParser
 */
object QaAnalyzer {

    /**
     * 分析 UI 层次结构 XML，返回问题列表
     */
    fun analyze(uiXml: String, screenIndex: Int): List<QAIssue> {
        val issues = mutableListOf<QAIssue>()
        if (uiXml.isBlank()) return issues

        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(uiXml))

            var eventType = parser.eventType
            val nodeAttributes = mutableMapOf<String, String>()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        nodeAttributes.clear()
                        for (i in 0 until parser.attributeCount) {
                            nodeAttributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                        }

                        val tagName = parser.name
                        val bounds = nodeAttributes["bounds"] ?: ""
                        val text = nodeAttributes["text"] ?: ""
                        val clickable = nodeAttributes["clickable"] ?: "false"
                        val contentDesc = nodeAttributes["content-desc"] ?: nodeAttributes["contentDescription"] ?: ""
                        val textSize = nodeAttributes["textSize"] ?: ""
                        val background = nodeAttributes["background"] ?: ""
                        val textColor = nodeAttributes["textColor"] ?: ""

                        // 1. 可访问性问题：可点击的 ImageView 缺少 contentDescription
                        if (tagName.contains("ImageView", ignoreCase = true) ||
                            tagName.contains("ImageButton", ignoreCase = true)) {
                            if (contentDesc.isBlank() && clickable == "true") {
                                issues.add(
                                    QAIssue(
                                        id = "acc_${screenIndex}_${System.currentTimeMillis()}",
                                        screenIndex = screenIndex,
                                        screenshotPath = "",
                                        description = "可访问性问题：可点击图标缺少 contentDescription，无障碍用户无法识别该元素。",
                                        severity = IssueSeverity.WARNING,
                                        suggestions = listOf(
                                            "添加 android:contentDescription 属性",
                                            "如为装饰用途，设置 focusable=\"false\""
                                        )
                                    )
                                )
                            }
                        }

                        // 2. 文本可读性：字号过小
                        if ((tagName.contains("TextView", ignoreCase = true) ||
                             tagName.contains("Button", ignoreCase = true)) &&
                            text.isNotBlank()) {
                            val sizeSp = textSize.replace("sp", "").toFloatOrNull() ?: 14f
                            if (sizeSp < 12f && sizeSp > 0f) {
                                issues.add(
                                    QAIssue(
                                        id = "txt_${screenIndex}_${System.currentTimeMillis()}",
                                        screenIndex = screenIndex,
                                        screenshotPath = "",
                                        description = "可读性问题：文本 \"$text\" 字长 ${sizeSp}sp，偏小",
                                        severity = IssueSeverity.INFO,
                                        suggestions = listOf(
                                            "建议文本字号 ≥ 12sp",
                                            "确保满足 WCAG 2.1 最小字体要求"
                                        )
                                    )
                                )
                            }
                        }

                        // 3. 触控区域过小
                        if (clickable == "true" && bounds.isNotBlank()) {
                            parseBounds(bounds)?.let { (w, h) ->
                                val minDim = minOf(w, h)
                                if (minDim in 1..47) {
                                    issues.add(
                                        QAIssue(
                                            id = "tap_${screenIndex}_${System.currentTimeMillis()}",
                                            screenIndex = screenIndex,
                                            screenshotPath = "",
                                            description = "触控问题：可点击元素最小边长 ${minDim}dp，小于 Material 48dp 推荐值",
                                            severity = IssueSeverity.WARNING,
                                            suggestions = listOf(
                                                "将点击目标调整为 ≥ 48dp × 48dp",
                                                "设置 minHeight/minWidth 属性"
                                            )
                                        )
                                    )
                                }
                            }
                        }

                        // 4. 对比度问题
                        if ((background.contains("FF", ignoreCase = true) || background.contains("F5", ignoreCase = true)) &&
                            (textColor.contains("AA", ignoreCase = true) || textColor.contains("CC", ignoreCase = true))) {
                            issues.add(
                                QAIssue(
                                    id = "ctr_${screenIndex}_${System.currentTimeMillis()}",
                                    screenIndex = screenIndex,
                                    screenshotPath = "",
                                    description = "对比度问题：文本颜色与背景色对比度可能不足",
                                    severity = IssueSeverity.INFO,
                                    suggestions = listOf(
                                        "验证颜色组合对比度 ≥ 4.5:1（WCAG AA）",
                                        "使用 WebAIM 对比度检查工具验证"
                                    )
                                )
                            )
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        nodeAttributes.clear()
                    }
                }
                eventType = parser.next()
            }

        } catch (e: Exception) {
            // XML 解析失败，静默忽略
        }

        // 去重：同一类型同一屏幕最多保留3个
        return issues
            .groupBy { it.id.substringBefore("_${it.screenIndex}") }
            .values
            .flatMap { it.take(3) }
    }

    private fun parseBounds(bounds: String): Pair<Int, Int>? {
        return try {
            val nums = bounds
                .replace("[", " ")
                .replace("]", " ")
                .split("\\s+".toRegex())
                .filter { it.isNotBlank() && it.matches(Regex("-?\\d+")) }
                .map { it.toInt() }
            if (nums.size >= 4) {
                Pair(nums[2] - nums[0], nums[3] - nums[1])
            } else null
        } catch (e: Exception) { null }
    }
}
