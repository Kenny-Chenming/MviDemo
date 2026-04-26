package com.mvi.kenny.feature.keyvault

// ================================================================
// KeyVaultContract — Android 17 Key Limit 合规检测工具 MVI 契约
// ================================================================
// MVI architecture contract for Android 17 Key Limit compliance detection & optimization toolkit.
//
// PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
// Design Reference: memory/agency/designs/PRD-171-Android-17-Key-Limit-Key-Value-Storage-Tools.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View            — Composable function, consumes State, renders UI
//   Intent          — User intentions, ViewModel processes and updates State
//   Effect          — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * RiskLevel — Key 数量风险等级枚举
 * ============================================================
 * 基于 Key 数量与 200,000 上限的距离评估风险等级。
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji 表示
 * @param color 风险颜色
 */
enum class RiskLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    /** 安全 — Key 数量 < 50% 上限 (100,000) */
    SAFE("安全", "🟢", Color(0xFF3FB950)),
    /** 警告 — Key 数量 50-80% 上限 (100,000-160,000) */
    CAUTION("警告", "🟡", Color(0xFFFFD700)),
    /** 危险 — Key 数量 80-95% 上限 (160,000-190,000) */
    DANGER("危险", "🟠", Color(0xFFF85149)),
    /** 严重 — Key 数量 > 95% 上限 (>190,000) */
    CRITICAL("严重", "🔴", Color(0xFFDA3633));

    companion object {
        /** 根据 Key 数量百分比推断风险等级 */
        fun fromKeyPercent(percent: Float): RiskLevel = when {
            percent < 50f -> SAFE
            percent < 80f -> CAUTION
            percent < 95f -> DANGER
            else -> CRITICAL
        }
    }
}

/**
 * ============================================================
 * ScanState — 扫描状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class ScanState(val displayName: String) {
    /** 初始空闲状态 */
    Idle("空闲"),
    /** 正在扫描中 */
    Scanning("扫描中"),
    /** 扫描完成 */
    Completed("完成"),
    /** 扫描出错 */
    Error("错误")
}

/**
 * ============================================================
 * SortMode — 贡献分析排序模式枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class SortMode(val displayName: String) {
    /** 按 Key 总量排序 */
    ByTotal("按总量"),
    /** 按增长率排序 */
    ByGrowth("按增长率"),
    /** 按实例数排序 */
    ByInstanceCount("按实例数")
}

/**
 * ============================================================
 * KeyVaultState — KeyVault 页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param totalKeyCount App 总 Key 数量
 * @param riskPercentage 当前 Key 数量占 200,000 上限的百分比 (0-100)
 * @param riskLevel 当前风险等级
 * @param spInstances 所有 SharedPreferences/DataStore 实例列表
 * @param moduleContributions 按模块分解的 Key 贡献列表
 * @param mergeSuggestions 可合并 Key 的建议列表
 * @param ciThreshold CI 监控阈值（默认 200,000）
 * @param ciHistory CI 构建历史
 * @param selectedTab 当前选中的 Tab 索引 (0-7)
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 (0-1)
 * @param currentScanModule 当前正在扫描的模块路径
 * @param expandedInstancePath 展开的实例路径（用于 Key 列表展开）
 * @param sortMode 贡献分析排序模式
 * @param selectedDegradationStrategy 选中的降级策略
 * @param errorMessage 错误消息
 */
data class KeyVaultState(
    val totalKeyCount: Int = 0,
    val riskPercentage: Float = 0f,
    val riskLevel: RiskLevel = RiskLevel.SAFE,
    val spInstances: List<SPInstance> = emptyList(),
    val moduleContributions: List<ModuleContribution> = emptyList(),
    val mergeSuggestions: List<KeyMergeSuggestion> = emptyList(),
    val ciThreshold: Int = 200_000,
    val ciHistory: List<CIBuildRecord> = emptyList(),
    val selectedTab: Int = 0,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentScanModule: String = "",
    val expandedInstancePath: String? = null,
    val sortMode: SortMode = SortMode.ByTotal,
    val selectedDegradationStrategy: DegradationStrategy? = null,
    val errorMessage: String? = null,
    // Key trend history (模拟数据)
    val keyTrendHistory: List<KeyTrendPoint> = emptyList(),
    // Migration related
    val selectedMigrationInstance: SPInstance? = null,
    val migrationCode: String? = null,
    // Strategy template
    val strategyTemplates: List<StrategyTemplate> = defaultStrategyTemplates,
    // Best practice items
    val bestPractices: List<BestPracticeItem> = defaultBestPractices
) {
    companion object {
        /** Initial/empty state */
        val Initial = KeyVaultState()
    }
}

/**
 * ============================================================
 * KeyVaultIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see KeyVaultViewModel.sendIntent handles all Intents
 */
sealed interface KeyVaultIntent {

    /** 用户点击"开始扫描"按钮 */
    data object StartScan : KeyVaultIntent

    /** 用户点击"停止扫描"按钮 */
    data object StopScan : KeyVaultIntent

    /** 用户切换 Tab
     * @param index Tab 索引
     */
    data class SelectTab(val index: Int) : KeyVaultIntent

    /** 用户展开/收起某个 SP 实例的 Key 列表
     * @param instancePath 实例路径
     */
    data class ToggleExpandInstance(val instancePath: String) : KeyVaultIntent

    /** 用户切换贡献分析排序模式
     * @param mode 排序模式
     */
    data class ChangeSortMode(val mode: SortMode) : KeyVaultIntent

    /** 用户生成 Key 合并代码
     * @param suggestion 合并建议
     */
    data class GenerateMergeCode(val suggestion: KeyMergeSuggestion) : KeyVaultIntent

    /** 用户选择迁移的 SP 实例
     * @param instance SP 实例
     */
    data class SelectMigrationInstance(val instance: SPInstance) : KeyVaultIntent

    /** 用户生成迁移代码
     * @param instance SP 实例
     */
    data class GenerateMigrationCode(val instance: SPInstance) : KeyVaultIntent

    /** 用户配置 CI 阈值
     * @param threshold 阈值
     */
    data class SetCIThreshold(val threshold: Int) : KeyVaultIntent

    /** 用户选择降级策略
     * @param strategy 降级策略
     */
    data class SelectDegradationStrategy(val strategy: DegradationStrategy) : KeyVaultIntent

    /** 用户导出 CI 配置 */
    data object ExportCIConfig : KeyVaultIntent

    /** 用户清除错误消息 */
    data object ClearError : KeyVaultIntent

    /** 用户刷新数据 */
    data object RefreshData : KeyVaultIntent
}

/**
 * ============================================================
 * KeyVaultEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see KeyVaultViewModel _effect.send() sends Effects
 */
sealed interface KeyVaultEffect {

    /** 显示 Toast 消息
     * @param message Toast 文本
     */
    data class ShowToast(val message: String) : KeyVaultEffect

    /** 导航到指定 Tab
     * @param index Tab 索引
     */
    data class NavigateToTab(val index: Int) : KeyVaultEffect

    /** 复制内容到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : KeyVaultEffect

    /** 导出 CI 配置成功
     * @param content 导出的配置内容
     */
    data class ExportCIConfigSuccess(val content: String) : KeyVaultEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : KeyVaultEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * SPInstance — SharedPreferences / DataStore 实例
 * ============================================================
 *
 * @param path 实例文件路径
 * @param name 实例名称
 * @param type 类型: "SharedPreferences" / "DataStore-Preferences" / "DataStore-Proto"
 * @param keyCount Key 数量
 * @param estimatedSizeBytes 预估大小（字节）
 * @param keys 该实例的所有 Key 列表
 * @param module 所属模块
 * @param isOverLimit 是否超额 (>100,000 key)
 */
data class SPInstance(
    val path: String,
    val name: String,
    val type: String,
    val keyCount: Int,
    val estimatedSizeBytes: Long,
    val keys: List<String> = emptyList(),
    val module: String,
    val isOverLimit: Boolean = false
)

/**
 * ============================================================
 * ModuleContribution — 模块 Key 贡献
 * ============================================================
 *
 * @param moduleName 模块名称
 * @param keyCount 该模块的 Key 总量
 * @param percentage 占 App 总 Key 数量的百分比
 * @param instanceCount 该模块的 SP/DataStore 实例数量
 * @param growthRate 增长率（每周 Key 增量）
 * @param instances 该模块下的所有实例
 */
data class ModuleContribution(
    val moduleName: String,
    val keyCount: Int,
    val percentage: Float,
    val instanceCount: Int,
    val growthRate: Int,
    val instances: List<SPInstance> = emptyList()
)

/**
 * ============================================================
 * KeyMergeSuggestion — Key 合并建议
 * ============================================================
 *
 * @param id 建议 ID
 * @param description 合并描述
 * @param originalKeys 合并前的 Key 列表
 * @param mergedKey 合并后的新 Key
 * @param mergedType 合并后的数据类型
 * @param savingsPercent 节省的空间百分比
 * @param sampleCode 示例代码
 */
data class KeyMergeSuggestion(
    val id: String,
    val description: String,
    val originalKeys: List<String>,
    val mergedKey: String,
    val mergedType: String,
    val savingsPercent: Float,
    val sampleCode: String
)

/**
 * ============================================================
 * CIBuildRecord — CI 构建记录
 * ============================================================
 *
 * @param id 构建 ID
 * @param timestamp 构建时间戳
 * @param keyCount 当时的 Key 数量
 * @param threshold 当时的阈值
 * @param status 通过/失败/警告状态
 * @param buildNumber CI 构建编号
 */
data class CIBuildRecord(
    val id: String,
    val timestamp: Long,
    val keyCount: Int,
    val threshold: Int,
    val status: CIStatus,
    val buildNumber: Int
)

/**
 * ============================================================
 * CIStatus — CI 构建状态枚举
 * ============================================================
 */
enum class CIStatus(val displayName: String, val emoji: String) {
    PASS("通过", "✅"),
    FAIL("失败", "❌"),
    WARNING("警告", "⚠️")
}

/**
 * ============================================================
 * DegradationStrategy — 降级策略枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param description 策略描述
 */
enum class DegradationStrategy(
    val displayName: String,
    val description: String
) {
    LRU_CLEANUP("LRU 清理", "Least Recently Used — 自动清理最久未使用的 Key，防止超出上限"),
    ARCHIVE_HISTORY("历史数据归档", "将历史数据归档到本地文件或云端，减少活跃 Key 数量"),
    USER_PROMPT("用户提示", "提示用户清理数据或确认继续操作，提供明确的降级选项"),
    KEY_EXPIRY("Key 过期", "为 Key 添加过期时间，自动清理过期数据")
}

/**
 * ============================================================
 * KeyTrendPoint — Key 数量趋势数据点
 * ============================================================
 *
 * @param timestamp 时间戳
 * @param keyCount 当时的 Key 数量
 */
data class KeyTrendPoint(
    val timestamp: Long,
    val keyCount: Int
)

/**
 * ============================================================
 * StrategyTemplate — 降级策略代码模板
 * ============================================================
 *
 * @param id 模板 ID
 * @param strategy 所属策略
 * @param title 模板标题
 * @param description 模板描述
 * @param codeTemplate Kotlin 代码模板
 * @param integrationGuide 集成指南
 */
data class StrategyTemplate(
    val id: String,
    val strategy: DegradationStrategy,
    val title: String,
    val description: String,
    val codeTemplate: String,
    val integrationGuide: String
)

/**
 * ============================================================
 * BestPracticeItem — 最佳实践条目
 * ============================================================
 *
 * @param id Unique ID
 * @param title 标题
 * @param description 描述
 * @param category 分类
 * @param impact 影响等级 (HIGH/MEDIUM/LOW)
 * @param isImplemented 是否已实施
 * @param isRecommendedPattern 是否为推荐模式（true=推荐，false=Anti-pattern）
 */
data class BestPracticeItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val impact: String,
    val isImplemented: Boolean = false,
    val isRecommendedPattern: Boolean = true
)

// ================================================================
// 默认数据 / Default Data
// ================================================================

/**
 * defaultStrategyTemplates — 降级策略代码模板清单
 */
val defaultStrategyTemplates = listOf(
    StrategyTemplate(
        id = "lru_001",
        strategy = DegradationStrategy.LRU_CLEANUP,
        title = "LRUCache Key 管理器",
        description = "使用 LRU 缓存策略管理 Key，自动清理最久未使用的 Key",
        codeTemplate = """
// LRU Key 管理器示例
class LRUKeyManager<K>(
    private val maxSize: Int,
    private val storage: SharedPreferences
) {
    private val accessOrder = LinkedHashMap<K, Long>(maxSize, 0.75f, true)

    fun put(key: K, value: String) {
        // 清理超量 Key
        while (accessOrder.size >= maxSize) {
            val oldest = accessOrder.keys.firstOrNull() ?: break
            storage.edit().remove(oldest.toString()).apply()
            accessOrder.remove(oldest)
        }
        storage.edit().putString(key.toString(), value).apply()
        accessOrder[key] = System.currentTimeMillis()
    }

    fun get(key: K): String? {
        accessOrder[key] = System.currentTimeMillis()
        return storage.getString(key.toString(), null)
    }
}
        """.trimIndent(),
        integrationGuide = """
集成步骤：
1. 将 LRUKeyManager 初始化为单例
2. 将所有 SP 写入替换为 manager.put()
3. 配置 maxSize 为预期最大 Key 数量
4. 在 Application.onCreate() 中初始化
        """.trimIndent()
    ),
    StrategyTemplate(
        id = "archive_001",
        strategy = DegradationStrategy.ARCHIVE_HISTORY,
        title = "历史数据归档器",
        description = "将历史数据归档到本地 JSON 文件，减少活跃 Key",
        codeTemplate = """
// 历史数据归档示例
class HistoryArchiver(private val sp: SharedPreferences) {
    private val archiveDir = File(context.filesDir, "key_archive")

    fun archiveOldKeys(prefix: String, olderThanDays: Int) {
        val cutoff = System.currentTimeMillis() - olderThanDays * 86400000L
        val toArchive = mutableMapOf<String, Any>()

        sp.all.forEach { (key, value) ->
            if (key.startsWith(prefix)) {
                val lastAccess = sp.getLong("${'$'}{key}_last_access", 0)
                if (lastAccess < cutoff) {
                    toArchive[key] = value
                }
            }
        }

        // 写入归档文件
        val archiveFile = File(archiveDir, "${'$'}{prefix}_archive.json")
        archiveFile.writeText(Gson().toJson(toArchive))

        // 从 SP 删除
        toArchive.keys.forEach { sp.edit().remove(it).remove("${'$'}{it}_last_access").apply() }
    }
}
        """.trimIndent(),
        integrationGuide = """
集成步骤：
1. 创建 HistoryArchiver 实例
2. 配置需要归档的 Key 前缀列表
3. 设置归档周期（如每天一次）
4. 在 WorkManager 中调度定期归档任务
        """.trimIndent()
    ),
    StrategyTemplate(
        id = "expiry_001",
        strategy = DegradationStrategy.KEY_EXPIRY,
        title = "带过期时间的 Key 存储",
        description = "为 Key 添加 TTL，自动清理过期数据",
        codeTemplate = """
// 带过期时间的 Key 存储
class ExpiringKeyStorage(private val sp: SharedPreferences) {
    companion object {
        const val TTL_MS = 7 * 24 * 3600 * 1000L // 默认 7 天
    }

    fun put(key: String, value: String, ttlMs: Long = TTL_MS) {
        sp.edit()
            .putString(key, value)
            .putLong("${'$'}{key}_expires_at", System.currentTimeMillis() + ttlMs)
            .apply()
    }

    fun get(key: String): String? {
        val expiresAt = sp.getLong("${'$'}{key}_expires_at", 0)
        if (expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
            remove(key)
            return null
        }
        return sp.getString(key, null)
    }

    fun remove(key: String) {
        sp.edit().remove(key).remove("${'$'}{key}_expires_at").apply()
    }

    fun cleanup() {
        val now = System.currentTimeMillis()
        sp.all.keys.filter { key ->
            !key.endsWith("_expires_at") &&
            sp.getLong("${'$'}{key}_expires_at", Long.MAX_VALUE) < now
        }.forEach { remove(it) }
    }
}
        """.trimIndent(),
        integrationGuide = """
集成步骤：
1. 将 ExpiringKeyStorage 包装现有 SP 操作
2. 对所有非关键数据设置合理 TTL
3. 在 App 启动时调用 cleanup()
4. 可选：使用 WorkManager 定期清理
        """.trimIndent()
    )
)

/**
 * defaultBestPractices — Key 管理最佳实践清单
 */
val defaultBestPractices = listOf(
    BestPracticeItem(
        id = "bp_kv_001",
        title = "使用 DataStore 替代 SharedPreferences",
        description = "DataStore 是 Jetpack 推荐的现代存储方案，支持类型安全且性能更好",
        category = "存储方案",
        impact = "HIGH",
        isRecommendedPattern = true
    ),
    BestPracticeItem(
        id = "bp_kv_002",
        title = "避免一个事件一个 Key 的模式",
        description = "埋点/事件不要每个事件一个 Key，使用 JSON 数组或 Proto 打包",
        category = "数据模型",
        impact = "HIGH",
        isRecommendedPattern = false
    ),
    BestPracticeItem(
        id = "bp_kv_003",
        title = "定期清理无用 Key",
        description = "在版本升级时清理不再使用的 Key，避免历史积累",
        category = "维护",
        impact = "MEDIUM",
        isRecommendedPattern = true
    ),
    BestPracticeItem(
        id = "bp_kv_004",
        title = "按模块隔离存储",
        description = "不同功能模块使用独立的 SP 实例，便于分析和清理",
        category = "架构",
        impact = "MEDIUM",
        isRecommendedPattern = true
    ),
    BestPracticeItem(
        id = "bp_kv_005",
        title = "避免在 SP 中存储大对象",
        description = "SP 适用于小数据，图片/大数据应使用文件存储或数据库",
        category = "数据模型",
        impact = "HIGH",
        isRecommendedPattern = false
    ),
    BestPracticeItem(
        id = "bp_kv_006",
        title = "使用加密存储敏感数据",
        description = "使用 EncryptedSharedPreferences 存储密码、Token 等敏感数据",
        category = "安全",
        impact = "HIGH",
        isRecommendedPattern = true
    ),
    BestPracticeItem(
        id = "bp_kv_007",
        title = "Boolean 标志位合并",
        description = "多个 boolean flag 可合并为一个 Int/Long 位掩码，节省 Key 数量",
        category = "数据模型",
        impact = "MEDIUM",
        isRecommendedPattern = true
    ),
    BestPracticeItem(
        id = "bp_kv_008",
        title = "监控 Key 数量增长趋势",
        description = "在 CI 中集成 Key 数量监控，及时发现异常增长",
        category = "CI",
        impact = "HIGH",
        isRecommendedPattern = true
    )
)

// ================================================================
// 辅助函数 / Helper Functions
// ================================================================

/**
 * 格式化 Key 数量为可读字符串
 *
 * @param count Key 数量
 * @return 格式化后的字符串（如 "12.5 万"）
 */
fun formatKeyCount(count: Int): String = when {
    count < 1000 -> "$count"
    count < 10000 -> "${"%.1f".format(count / 1000.0)} 千"
    count < 200000 -> "${"%.1f".format(count / 10000.0)} 万"
    else -> "${"%.1f".format(count / 10000.0)} 万 ⚠️"
}

/**
 * 计算风险百分比
 *
 * @param keyCount Key 数量
 * @param limit 上限（默认 200,000）
 * @return 风险百分比 (0-100)
 */
fun calculateRiskPercent(keyCount: Int, limit: Int = 200_000): Float {
    if (limit <= 0) return 0f
    return (keyCount.toFloat() / limit.toFloat() * 100f).coerceIn(0f, 100f)
}
