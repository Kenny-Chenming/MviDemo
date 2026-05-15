package com.mvi.kenny.feature.appastool

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * AppAsToolContract — App-as-Tool 开发工具包 MVI 契约
 * AppAsTool MVI Contract — Android Intelligence Hub App-as-Tool Dev Toolkit
 * ============================================================
 *
 * PRD-250 | Android AppFunctions App-as-Tool 开发工具包
 * Ref: memory/agency/designs/PRD-250-Android-AppFunctions-App-as-Tool-开发工具包.md
 *
 * MVI Architecture:
 * - Model (State): Immutable page state, single source of truth
 * - View: Composable functions that consume State and render UI
 * - Intent: User intentions; ViewModel executes logic on receiving Intent
 * - Effect: One-time side effects (Toast, Navigation) delivered via Channel
 * —————————————————————————————————————————————————————
 *
 * Bottom Tab Navigation (5 Tabs):
 * - Tab 1: Manifest Declaration Guide
 * - Tab 2: Function Schema Creation Tool
 * - Tab 3: Intent Handling Skeleton
 * - Tab 4: Permission Delegation Integration
 * - Tab 5: CI Validation Plugin
 */

// ============================================================
// Data Models / 数据模型
// ============================================================

/**
 * Bottom navigation tab enumeration
 * 底部导航 Tab 枚举
 *
 * @param title Tab display title
 * @param iconName Material icon name
 */
enum class AppAsToolTab(val title: String, val iconName: String) {
    MANIFEST("Manifest声明", "build"),
    SCHEMA("Schema创作", "description"),
    INTENT("Intent骨架", "settings_ethernet"),
    PERMISSION("权限委托", "security"),
    CI_PLUGIN("CI验证", "verified")
}

/**
 * Schema template type for Tab 2
 * Schema 模板类型枚举
 *
 * @param displayName Template display name
 */
enum class SchemaTemplate(val displayName: String) {
    BASIC("基础模板"),
    ADVANCED("高级模板"),
    WITH_PARAMS("带参数模板"),
    WITH_RETURN("带返回值模板")
}

/**
 * Expandable card data model for Tab 1 Manifest Guide
 * 可展开卡片数据模型
 *
 * @param id Card unique identifier
 * @param title Card title / 卡片标题
 * @param description Brief description / 简要描述
 * @param codeContent Code snippet / 代码片段
 * @param language Programming language (kotlin/xml)
 * @param isExpanded Expansion state / 是否展开
 */
data class ManifestCard(
    val id: Int,
    val title: String,
    val description: String,
    val codeContent: String,
    val language: String = "kotlin",
    val isExpanded: Boolean = false
)

/**
 * Intent skeleton section data model for Tab 3
 * Intent 处理骨架段落数据模型
 *
 * @param id Section unique identifier
 * @param title Section title / 段落标题
 * @param explanation Step-by-step explanation / 分步说明
 * @param codeContent Code snippet / 代码片段
 * @param templateVars Highlighted template variables / 高亮模板变量
 */
data class IntentSection(
    val id: Int,
    val title: String,
    val explanation: String,
    val codeContent: String,
    val templateVars: List<String> = emptyList()
)

/**
 * CI validation rule data model for Tab 5
 * CI 验证规则数据模型
 *
 * @param id Rule unique identifier
 * @param name Rule name / 规则名称
 * @param description Rule description / 规则描述
 * @param severity Error severity level / 严重级别
 */
data class CIValidationRule(
    val id: Int,
    val name: String,
    val description: String,
    val severity: String // "error" | "warning" | "info"
)

// ============================================================
// State / 页面状态
// ============================================================

/**
 * App-as-Tool Developer Toolkit page state
 * App-as-Tool 开发者工具包页面状态
 *
 * MVI Architecture: Model layer, holds all page state.
 * State is Immutable — each state change creates a new State object.
 *
 * @param selectedTab Current bottom navigation tab index / 当前选中的 Tab
 * @param schemaTemplate Selected schema template type / 选中的 Schema 模板类型
 * @param schemaName Schema name input / Schema 名称输入
 * @param schemaDescription Schema description input / Schema 描述输入
 * @param schemaParameters Schema parameters input (JSON) / Schema 参数输入
 * @param schemaReturns Schema returns input (JSON) / Schema 返回值输入
 * @param expandedCards Set of expanded card indices / 已展开的卡片索引集合
 * @param expandedSections Set of expanded intent section indices / 已展开的段落索引集合
 * @param error Error message, null means no error / 错误信息，null 表示无错误
 *
 * @see AppAsToolIntent
 * @see AppAsToolViewModel
 */
data class AppAsToolState(
    val selectedTab: Int = 0,
    // Tab 2 Schema state / Schema 状态
    val schemaTemplate: SchemaTemplate = SchemaTemplate.BASIC,
    val schemaName: String = "",
    val schemaDescription: String = "",
    val schemaParameters: String = "",
    val schemaReturns: String = "",
    // Expansion state / 展开状态
    val expandedCards: Set<Int> = emptySet(),
    val expandedSections: Set<Int> = emptySet(),
    // Error state / 错误状态
    val error: String? = null
) {
    companion object {
        /** Initial / default state / 初始状态 */
        val Initial = AppAsToolState()
    }

    /**
     * Generate live JSON Schema preview for Tab 2
     * 生成 Tab 2 的实时 JSON Schema 预览
     */
    fun generateSchemaPreview(): String {
        val paramsJson = if (schemaParameters.isNotBlank()) schemaParameters else "{}"
        val returnsJson = if (schemaReturns.isNotBlank()) schemaReturns else "{}"
        val name = schemaName.ifBlank { "MyAppFunction" }
        val desc = schemaDescription.ifBlank { "My app function description" }

        return """
{
  "name": "$name",
  "description": "$desc",
  "parameters": $paramsJson,
  "returns": $returnsJson
}
        """.trimIndent()
    }
}

// ============================================================
// Intent / 用户意图
// ============================================================

/**
 * User intentions for App-as-Tool Developer Toolkit
 * App-as-Tool 开发者工具包用户意图
 *
 * Every user interaction on the page corresponds to an Intent.
 * ViewModel receives Intent, executes business logic, then updates State.
 *
 * @see AppAsToolViewModel.sendIntent
 */
sealed interface AppAsToolIntent {

    /**
     * Switch bottom navigation tab / 切换底部 Tab
     *
     * @param index Target tab index / 目标 Tab 索引
     */
    data class SelectTab(val index: Int) : AppAsToolIntent

    /**
     * Select schema template type (Tab 2)
     * 选择 Schema 模板类型
     *
     * @param template Selected template / 选中的模板
     */
    data class SelectSchemaTemplate(val template: SchemaTemplate) : AppAsToolIntent

    /**
     * Update schema name field (Tab 2)
     * 更新 Schema 名称
     *
     * @param name Schema name / Schema 名称
     */
    data class UpdateSchemaName(val name: String) : AppAsToolIntent

    /**
     * Update schema description field (Tab 2)
     * 更新 Schema 描述
     *
     * @param description Schema description / Schema 描述
     */
    data class UpdateSchemaDescription(val description: String) : AppAsToolIntent

    /**
     * Update schema parameters field (Tab 2)
     * 更新 Schema 参数
     *
     * @param parameters JSON parameters string / JSON 参数字符串
     */
    data class UpdateSchemaParameters(val parameters: String) : AppAsToolIntent

    /**
     * Update schema returns field (Tab 2)
     * 更新 Schema 返回值
     *
     * @param returns JSON returns string / JSON 返回值字符串
     */
    data class UpdateSchemaReturns(val returns: String) : AppAsToolIntent

    /**
     * Toggle expandable card expansion state
     * 切换可展开卡片的展开/收起状态
     *
     * @param cardId Card unique identifier / 卡片唯一标识
     */
    data class ToggleCard(val cardId: Int) : AppAsToolIntent

    /**
     * Toggle intent section expansion state (Tab 3)
     * 切换 Intent 段落展开/收起状态
     *
     * @param sectionId Section unique identifier / 段落唯一标识
     */
    data class ToggleSection(val sectionId: Int) : AppAsToolIntent

    /**
     * Copy text to clipboard (all tabs)
     * 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AppAsToolIntent

    /**
     * Export schema as JSON file (Tab 2)
     * 导出 Schema 为 JSON 文件
     *
     * @param json JSON content / JSON 内容
     */
    data class ExportSchema(val json: String) : AppAsToolIntent
}

// ============================================================
// Effect / 副作用
// ============================================================

/**
 * One-time side effects for App-as-Tool Developer Toolkit
 * App-as-Tool 开发者工具包副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect {}
 *
 * @see AppAsToolViewModel
 */
sealed interface AppAsToolEffect {

    /**
     * Show toast message / 显示 Toast
     *
     * @param message Toast message text / Toast 文本
     */
    data class ShowToast(val message: String) : AppAsToolEffect

    /**
     * Copy text to system clipboard / 复制文本到剪贴板
     *
     * @param text Text to copy / 要复制的文本
     */
    data class CopyToClipboard(val text: String) : AppAsToolEffect

    /**
     * Export schema file (Tab 2)
     * 导出 Schema 文件
     *
     * @param json JSON content to save / 要保存的 JSON 内容
     */
    data class ExportSchemaFile(val json: String) : AppAsToolEffect
}
