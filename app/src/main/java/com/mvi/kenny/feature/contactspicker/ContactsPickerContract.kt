package com.mvi.kenny.feature.contactspicker

/**
 * ============================================================
 * ContactsPickerContract — Android 17 Contacts Picker 工具 MVI 契约
 * ============================================================
 * MVI (Model-View-Intent) Architecture Pattern.
 *
 * @see ContactsPickerViewModel State management
 * @see ContactsPickerScreen Main screen UI
 */

// =============================================================
// ContactsPickerToolState — 页面状态
// =============================================================
/**
 * Contacts Picker Tool State / Contacts Picker 工具页面状态
 *
 * Single source of truth for the entire Contacts Picker Tool UI.
 * All UI state is derived from this data class.
 *
 * @param scanStatus Current scan status / 当前扫描状态
 * @param scanProgress Scan progress 0.0~1.0 / 扫描进度
 * @param scanResults List of READ_CONTACTS call sites / READ_CONTACTS 调用点列表
 * @param selectedCallSite Selected call site for detail view / 选中查看详情的调用点
 * @param pickerConfig Contacts Picker configuration / Contacts Picker 配置
 * @param reportConfig Privacy report configuration / 隐私报告配置
 * @param generatedReport Generated report / 生成的报告
 * @param selectedTemplate Selected integration template / 选中的集成模板
 * @param compatibilityResult Compatibility check result / 兼容性检测结果
 * @param activeTab Currently active tool tab / 当前活跃的 Tab
 */
data class ContactsPickerToolState(
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val scanProgress: Float = 0f,
    val scanResults: List<PermissionCallSite> = emptyList(),
    val selectedCallSite: PermissionCallSite? = null,
    val pickerConfig: PickerConfig = PickerConfig(),
    val reportConfig: ReportConfig = ReportConfig(),
    val generatedReport: GeneratedReport? = null,
    val selectedTemplate: Template? = null,
    val compatibilityResult: CompatibilityResult? = null,
    val activeTab: ToolTab = ToolTab.SCANNER
) {
    companion object {
        /** Initial state / 初始状态 */
        val Initial = ContactsPickerToolState()
    }

    /**
     * P0 count in scan results / 扫描结果中 P0 数量
     */
    val p0Count: Int
        get() = scanResults.count { it.migrability == Migrability.P0 }

    /**
     * P1 count in scan results / 扫描结果中 P1 数量
     */
    val p1Count: Int
        get() = scanResults.count { it.migrability == Migrability.P1 }

    /**
     * P2 count in scan results / 扫描结果中 P2 数量
     */
    val p2Count: Int
        get() = scanResults.count { it.migrability == Migrability.P2 }
}

// =============================================================
// ContactsPickerIntent — 用户意图
// =============================================================
/**
 * Contacts Picker Tool User Intents / Contacts Picker 工具用户意图
 *
 * Every user action in the UI corresponds to an Intent.
 * ViewModel receives Intent and executes business logic.
 *
 * @see ContactsPickerViewModel.sendIntent Process all intents
 */
sealed class ContactsPickerIntent {
    /** Start project scan for READ_CONTACTS usage / 开始扫描 READ_CONTACTS 使用 */
    data object StartScan : ContactsPickerIntent()

    /** Cancel ongoing scan / 取消正在进行的扫描 */
    data object CancelScan : ContactsPickerIntent()

    /** Select a call site to view detail / 选择调用点查看详情
     * @param callSite Call site to select / 要选择的调用点
     */
    data class SelectCallSite(val callSite: PermissionCallSite) : ContactsPickerIntent()

    /** Update Contacts Picker configuration / 更新 Contacts Picker 配置
     * @param config New picker config / 新的 picker 配置
     */
    data class UpdatePickerConfig(val config: PickerConfig) : ContactsPickerIntent()

    /** Generate privacy compliance report / 生成隐私合规报告
     * @param format Report format / 报告格式
     */
    data class GenerateReport(val format: ReportFormat) : ContactsPickerIntent()

    /** Select an integration template / 选择集成模板
     * @param template Template to select / 要选择的模板
     */
    data class SelectTemplate(val template: Template) : ContactsPickerIntent()

    /** Export template to target path / 导出模板到目标路径
     * @param template Template to export / 要导出的模板
     * @param targetPath Target file path / 目标文件路径
     */
    data class ExportTemplate(val template: Template, val targetPath: String) : ContactsPickerIntent()

    /** Check Android version compatibility / 检查 Android 版本兼容性 */
    data object CheckCompatibility : ContactsPickerIntent()

    /** Switch active tab / 切换活跃 Tab
     * @param tab Tab to switch to / 要切换到的 Tab
     */
    data class SwitchTab(val tab: ToolTab) : ContactsPickerIntent()
}

// =============================================================
// ContactsPickerEffect — 副作用
// =============================================================
/**
 * Contacts Picker Tool Side Effects / Contacts Picker 工具副作用
 *
 * One-time events, immutable, consumed only once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see ContactsPickerViewModel Send via _effect.send()
 */
sealed class ContactsPickerEffect {
    /** Show snackbar message / 显示 snackbar 消息
     * @param message Snackbar text / 消息文本
     * @param type Snackbar type / 消息类型
     */
    data class ShowSnackbar(val message: String, val type: SnackbarType) : ContactsPickerEffect()

    /** Export success notification / 导出成功通知
     * @param filePath Exported file path / 导出文件路径
     */
    data class ExportSuccess(val filePath: String) : ContactsPickerEffect()

    /** Scan complete notification / 扫描完成通知 */
    data object ScanComplete : ContactsPickerEffect()

    /** Navigate to template detail page / 导航到模板详情页
     * @param template Template to show detail / 要显示详情的模板
     */
    data class NavigateToTemplateDetail(val template: Template) : ContactsPickerEffect()
}
