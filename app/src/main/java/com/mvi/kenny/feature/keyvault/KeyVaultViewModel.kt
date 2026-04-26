package com.mvi.kenny.feature.keyvault

// ================================================================
// KeyVaultViewModel — Android 17 Key Limit 合规检测工具 MVI ViewModel
// ================================================================
// ViewModel for Android 17 Key Limit compliance detection & optimization toolkit.
//
// PRD-171: Android 17 Key Limit 合规检测与数据重构工具包
// Implements MVI pattern: Intent → ViewModel → State/Effect
//
// Key responsibilities:
//   - Scan all SP/DataStore instances and count keys
//   - Analyze module-level key contributions
//   - Generate key merge suggestions
//   - Generate SP → DataStore migration code
//   - Monitor CI build history
//   - Provide degradation strategy templates
//   - Expose one-time Effects (navigation, toast, errors)
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ============================================================
 * KeyVaultViewModel — KeyVault 工具 ViewModel
 * ============================================================
 * Manages the KeyVaultState and processes KeyVaultIntent.
 *
 * In a real implementation, this would:
 *   - Use Gradle plugin to scan SP/DataStore instances at compile time
 *   - Use adb shell to enumerate SP files on device
 *   - Query DataStore via asMap() to get key counts
 *
 * Current implementation uses simulated data for demonstration.
 *
 * @see KeyVaultState
 * @see KeyVaultIntent
 * @see KeyVaultEffect
 */
class KeyVaultViewModel : ViewModel() {

    // ─────────────────────────────────────────────────────────────
    // State — Single source of truth, exposed as immutable StateFlow
    // ─────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(KeyVaultState.Initial)
    val state: StateFlow<KeyVaultState> = _state.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // Effect — One-time events via SharedFlow
    // ─────────────────────────────────────────────────────────────
    private val _effect = MutableSharedFlow<KeyVaultEffect>()
    val effect = _effect.asSharedFlow()

    // ─────────────────────────────────────────────────────────────
    // Internal state
    // ─────────────────────────────────────────────────────────────
    private var scanJob: Job? = null

    init {
        // Initialize with mock data for demonstration
        _update {
            copy(
                totalKeyCount = 125_000,
                riskPercentage = 62.5f,
                riskLevel = RiskLevel.CAUTION,
                spInstances = createMockSPInstances(),
                moduleContributions = createMockModuleContributions(),
                mergeSuggestions = createMockMergeSuggestions(),
                ciHistory = createMockCIHistory(),
                keyTrendHistory = createMockKeyTrendHistory()
            )
        }
    }

    /**
     * ============================================================
     * sendIntent — Intent 处理入口
     * ============================================================
     * Entry point for all user intents. Called from UI layer.
     *
     * @param intent User intent
     * @see KeyVaultIntent
     */
    fun sendIntent(intent: KeyVaultIntent) {
        when (intent) {
            is KeyVaultIntent.StartScan -> startScan()
            is KeyVaultIntent.StopScan -> stopScan()
            is KeyVaultIntent.SelectTab -> selectTab(intent.index)
            is KeyVaultIntent.ToggleExpandInstance -> toggleExpandInstance(intent.instancePath)
            is KeyVaultIntent.ChangeSortMode -> changeSortMode(intent.mode)
            is KeyVaultIntent.GenerateMergeCode -> generateMergeCode(intent.suggestion)
            is KeyVaultIntent.SelectMigrationInstance -> selectMigrationInstance(intent.instance)
            is KeyVaultIntent.GenerateMigrationCode -> generateMigrationCode(intent.instance)
            is KeyVaultIntent.SetCIThreshold -> setCIThreshold(intent.threshold)
            is KeyVaultIntent.SelectDegradationStrategy -> selectDegradationStrategy(intent.strategy)
            is KeyVaultIntent.ExportCIConfig -> exportCIConfig()
            is KeyVaultIntent.ClearError -> clearError()
            is KeyVaultIntent.RefreshData -> refreshData()
        }
    }

    // ================================================================
    // Intent Handlers
    // ================================================================

    /**
     * StartScan — 开始扫描所有 SP/DataStore 实例
     */
    private fun startScan() {
        if (_state.value.isScanning) return

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _update {
                copy(
                    isScanning = true,
                    scanProgress = 0f,
                    currentScanModule = "",
                    errorMessage = null
                )
            }

            try {
                val modules = listOf(
                    "com.example.myapp",
                    "com.example.myapp.analytics",
                    "com.example.myapp.user",
                    "com.example.myapp.feature_a",
                    "com.example.myapp.feature_b",
                    "com.example.myapp.cache"
                )

                var totalKeys = 0
                val allInstances = mutableListOf<SPInstance>()

                modules.forEachIndexed { index, module ->
                    // Simulate scanning each module
                    _update {
                        copy(
                            currentScanModule = module,
                            scanProgress = (index + 1).toFloat() / modules.size
                        )
                    }

                    delay(800) // Simulate scan time

                    // Generate mock instances for this module
                    val moduleInstances = createMockInstancesForModule(module)
                    allInstances.addAll(moduleInstances)
                    totalKeys += moduleInstances.sumOf { it.keyCount }
                }

                // Update state with scan results
                val riskPct = calculateRiskPercent(totalKeys)
                _update {
                    copy(
                        totalKeyCount = totalKeys,
                        riskPercentage = riskPct,
                        riskLevel = RiskLevel.fromKeyPercent(riskPct),
                        spInstances = allInstances,
                        moduleContributions = calculateModuleContributions(allInstances),
                        isScanning = false,
                        scanProgress = 1f,
                        currentScanModule = ""
                    )
                }

                _effect.emit(KeyVaultEffect.ShowToast("扫描完成，共发现 ${allInstances.size} 个实例，${formatKeyCount(totalKeys)} 个 Key"))

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _update { copy(isScanning = false, errorMessage = e.message) }
                _effect.emit(KeyVaultEffect.ShowError("扫描失败: ${e.message}"))
            }
        }
    }

    /**
     * StopScan — 停止扫描
     */
    private fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _update { copy(isScanning = false, scanProgress = 0f, currentScanModule = "") }
        viewModelScope.launch {
            _effect.emit(KeyVaultEffect.ShowToast("扫描已停止"))
        }
    }

    /**
     * SelectTab — 切换 Tab
     */
    private fun selectTab(index: Int) {
        _update { copy(selectedTab = index) }
    }

    /**
     * ToggleExpandInstance — 展开/收起 SP 实例
     */
    private fun toggleExpandInstance(instancePath: String) {
        _update {
            copy(
                expandedInstancePath = if (expandedInstancePath == instancePath) null else instancePath
            )
        }
    }

    /**
     * ChangeSortMode — 切换排序模式
     */
    private fun changeSortMode(mode: SortMode) {
        _update { copy(sortMode = mode) }
        // Re-sort module contributions
        val sorted = when (mode) {
            SortMode.ByTotal -> _state.value.moduleContributions.sortedByDescending { it.keyCount }
            SortMode.ByGrowth -> _state.value.moduleContributions.sortedByDescending { it.growthRate }
            SortMode.ByInstanceCount -> _state.value.moduleContributions.sortedByDescending { it.instanceCount }
        }
        _update { copy(moduleContributions = sorted) }
    }

    /**
     * GenerateMergeCode — 生成 Key 合并代码
     */
    private fun generateMergeCode(suggestion: KeyMergeSuggestion) {
        viewModelScope.launch {
            _effect.emit(KeyVaultEffect.CopyToClipboard(suggestion.sampleCode))
            _effect.emit(KeyVaultEffect.ShowToast("合并代码已复制到剪贴板"))
        }
    }

    /**
     * SelectMigrationInstance — 选择迁移实例
     */
    private fun selectMigrationInstance(instance: SPInstance) {
        _update { copy(selectedMigrationInstance = instance) }
    }

    /**
     * GenerateMigrationCode — 生成 SP → DataStore 迁移代码
     */
    private fun generateMigrationCode(instance: SPInstance) {
        viewModelScope.launch {
            try {
                val code = generateMigrationKotlinCode(instance)
                _update { copy(migrationCode = code) }
                _effect.emit(KeyVaultEffect.CopyToClipboard(code))
                _effect.emit(KeyVaultEffect.ShowToast("迁移代码已复制到剪贴板"))
            } catch (e: Exception) {
                _effect.emit(KeyVaultEffect.ShowError("生成迁移代码失败: ${e.message}"))
            }
        }
    }

    /**
     * SetCIThreshold — 设置 CI 阈值
     */
    private fun setCIThreshold(threshold: Int) {
        _update { copy(ciThreshold = threshold) }
    }

    /**
     * SelectDegradationStrategy — 选择降级策略
     */
    private fun selectDegradationStrategy(strategy: DegradationStrategy) {
        _update { copy(selectedDegradationStrategy = strategy) }
    }

    /**
     * ExportCIConfig — 导出 CI 配置
     */
    private fun exportCIConfig() {
        viewModelScope.launch {
            try {
                val config = buildCIConfigYaml(_state.value.ciThreshold, _state.value.totalKeyCount)
                _effect.emit(KeyVaultEffect.ExportCIConfigSuccess(config))
                _effect.emit(KeyVaultEffect.CopyToClipboard(config))
                _effect.emit(KeyVaultEffect.ShowToast("CI 配置已复制到剪贴板"))
            } catch (e: Exception) {
                _effect.emit(KeyVaultEffect.ShowError("导出 CI 配置失败: ${e.message}"))
            }
        }
    }

    /**
     * ClearError — 清除错误消息
     */
    private fun clearError() {
        _update { copy(errorMessage = null) }
    }

    /**
     * RefreshData — 刷新数据
     */
    private fun refreshData() {
        viewModelScope.launch {
            try {
                delay(500)
                _update {
                    copy(
                        keyTrendHistory = createMockKeyTrendHistory(),
                        ciHistory = createMockCIHistory()
                    )
                }
                _effect.emit(KeyVaultEffect.ShowToast("数据已刷新"))
            } catch (e: Exception) {
                _effect.emit(KeyVaultEffect.ShowError("刷新失败: ${e.message}"))
            }
        }
    }

    // ================================================================
    // Private Helpers
    // ================================================================

    /**
     * update — State update helper using immutable copy
     */
    private inline fun _update(update: KeyVaultState.() -> KeyVaultState) {
        _state.update { it.update() }
    }

    /**
     * calculateModuleContributions — 计算模块贡献
     */
    private fun calculateModuleContributions(instances: List<SPInstance>): List<ModuleContribution> {
        return instances
            .groupBy { it.module }
            .map { (module, moduleInstances) ->
                ModuleContribution(
                    moduleName = module,
                    keyCount = moduleInstances.sumOf { it.keyCount },
                    percentage = moduleInstances.sumOf { it.keyCount }.toFloat() / _state.value.totalKeyCount * 100f,
                    instanceCount = moduleInstances.size,
                    growthRate = (10..500).random(),
                    instances = moduleInstances
                )
            }
            .sortedByDescending { it.keyCount }
    }

    /**
     * generateMigrationKotlinCode — 生成 SP → DataStore 迁移 Kotlin 代码
     */
    private fun generateMigrationKotlinCode(instance: SPInstance): String {
        return """
// ============================================================
// SP → DataStore 迁移代码
// 来源: ${instance.name}
// 类型: ${instance.type}
// Key 数量: ${instance.keyCount}
// ============================================================

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.${instance.name.replace(" ", "")}DataStore: DataStore<Preferences> by preferencesDataStore(name = "${instance.name}")

object ${instance.name.replace(" ", "")}Migration {

    suspend fun migrateFromSP(sp: android.content.SharedPreferences) {
        // 获取所有 Key 并迁移到 DataStore
        sp.all.forEach { (key, value) ->
            val dataStoreKey = stringPreferencesKey(key)
            ${instance.name.replace(" ", "")}DataStore.edit { preferences ->
                when (value) {
                    is String -> preferences[dataStoreKey] = value
                    is Int -> preferences[stringPreferencesKey(key)] = value.toString()
                    is Long -> preferences[stringPreferencesKey(key)] = value.toString()
                    is Float -> preferences[stringPreferencesKey(key)] = value.toString()
                    is Boolean -> preferences[stringPreferencesKey(key)] = value.toString()
                    is Set<*> -> preferences[stringPreferencesKey(key)] = (value as Set<String>).joinToString(",")
                    else -> preferences[dataStoreKey] = value.toString()
                }
            }
        }
    }

    // 读取迁移后的数据示例
    suspend fun getString(context: Context, key: String): String? {
        return context.${instance.name.replace(" ", "")}DataStore.data
            .map { preferences -> preferences[stringPreferencesKey(key)] }
            .first()
    }
}
        """.trimIndent()
    }

    /**
     * buildCIConfigYaml — 生成 CI 配置 YAML
     */
    private fun buildCIConfigYaml(threshold: Int, currentKeyCount: Int): String {
        return """
# ============================================================
# Android 17 Key Limit CI 监控配置
# 当前 Key 数量: ${'$'}currentKeyCount
# 阈值: ${'$'}threshold
# ============================================================

name: Android Key Limit Check

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [main]

jobs:
  key-limit-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'

      - name: Count Keys
        run: |
          # 使用 KeyVault Gradle 插件统计 Key 数量
          ./gradlew countKeys --quiet
          echo "KEY_COUNT=${'$'}(cat key_count.txt)" >> ${'$'}GITHUB_ENV

      - name: Check Key Limit
        run: |
          THRESHOLD=${'$'}threshold
          KEY_COUNT=${'$'}{{ env.KEY_COUNT }}

          echo "当前 Key 数量: ${'$'}KEY_COUNT"
          echo "阈值: ${'$'}THRESHOLD"

          if [ ${'$'}KEY_COUNT -gt ${'$'}THRESHOLD ]; then
            echo "❌ Key 数量 (${'$'}KEY_COUNT) 超过阈值 (${'$'}THRESHOLD)"
            exit 1
          elif [ ${'$'}KEY_COUNT -gt ${'$'}((${'$'}THRESHOLD * 80 / 100)) ]; then
            echo "⚠️ Key 数量 (${'$'}KEY_COUNT) 超过 80% 阈值"
          else
            echo "✅ Key 数量在安全范围内"
          fi
        """.trimIndent()
    }

    // ================================================================
    // Mock Data Generators (演示用)
    // ================================================================

    /**
     * createMockSPInstances — 创建模拟 SP/DataStore 实例列表
     */
    private fun createMockSPInstances(): List<SPInstance> {
        return listOf(
            SPInstance(
                path = "/data/data/com.example.myapp/shared_prefs/user_prefs.xml",
                name = "user_prefs",
                type = "SharedPreferences",
                keyCount = 35_000,
                estimatedSizeBytes = 2_100_000,
                keys = (1..100).map { "user_key_$it" },
                module = "com.example.myapp",
                isOverLimit = false
            ),
            SPInstance(
                path = "/data/data/com.example.myapp/shared_prefs/analytics_prefs.xml",
                name = "analytics_prefs",
                type = "SharedPreferences",
                keyCount = 42_000,
                estimatedSizeBytes = 3_800_000,
                keys = (1..100).map { "analytics_event_$it" },
                module = "com.example.myapp.analytics",
                isOverLimit = false
            ),
            SPInstance(
                path = "/data/data/com.example.myapp/shared_prefs/feature_a_prefs.xml",
                name = "feature_a_prefs",
                type = "SharedPreferences",
                keyCount = 18_000,
                estimatedSizeBytes = 1_200_000,
                keys = (1..100).map { "feature_a_$it" },
                module = "com.example.myapp.feature_a",
                isOverLimit = false
            ),
            SPInstance(
                path = "/data/data/com.example.myapp/datastore/user_settings.preferences_pb",
                name = "user_settings",
                type = "DataStore-Preferences",
                keyCount = 8_000,
                estimatedSizeBytes = 640_000,
                keys = (1..100).map { "setting_$it" },
                module = "com.example.myapp.user",
                isOverLimit = false
            ),
            SPInstance(
                path = "/data/data/com.example.myapp/shared_prefs/feature_b_prefs.xml",
                name = "feature_b_prefs",
                type = "SharedPreferences",
                keyCount = 22_000,
                estimatedSizeBytes = 1_800_000,
                keys = (1..100).map { "feature_b_$it" },
                module = "com.example.myapp.feature_b",
                isOverLimit = false
            )
        )
    }

    /**
     * createMockInstancesForModule — 为指定模块创建模拟实例
     */
    private fun createMockInstancesForModule(module: String): List<SPInstance> {
        val instanceCount = (1..3).random()
        return (1..instanceCount).map { index ->
            val keyCount = (5_000..50_000).random()
            SPInstance(
                path = "/data/data/com.example.myapp/shared_prefs/${module.replace(".", "_")}_prefs_$index.xml",
                name = "${module.substringAfterLast(".")}_prefs_$index",
                type = if (index % 2 == 0) "DataStore-Preferences" else "SharedPreferences",
                keyCount = keyCount,
                estimatedSizeBytes = (keyCount * 50L).toLong(),
                keys = (1..20).map { "key_${it}_${System.currentTimeMillis() % 1000}" },
                module = module,
                isOverLimit = keyCount > 100_000
            )
        }
    }

    /**
     * createMockModuleContributions — 创建模拟模块贡献列表
     */
    private fun createMockModuleContributions(): List<ModuleContribution> {
        return listOf(
            ModuleContribution(
                moduleName = "com.example.myapp.analytics",
                keyCount = 42_000,
                percentage = 33.6f,
                instanceCount = 2,
                growthRate = 320,
                instances = emptyList()
            ),
            ModuleContribution(
                moduleName = "com.example.myapp",
                keyCount = 35_000,
                percentage = 28.0f,
                instanceCount = 1,
                growthRate = 80,
                instances = emptyList()
            ),
            ModuleContribution(
                moduleName = "com.example.myapp.feature_b",
                keyCount = 22_000,
                percentage = 17.6f,
                instanceCount = 3,
                growthRate = 150,
                instances = emptyList()
            ),
            ModuleContribution(
                moduleName = "com.example.myapp.feature_a",
                keyCount = 18_000,
                percentage = 14.4f,
                instanceCount = 2,
                growthRate = 95,
                instances = emptyList()
            ),
            ModuleContribution(
                moduleName = "com.example.myapp.user",
                keyCount = 8_000,
                percentage = 6.4f,
                instanceCount = 1,
                growthRate = 25,
                instances = emptyList()
            )
        )
    }

    /**
     * createMockMergeSuggestions — 创建模拟合并建议
     */
    private fun createMockMergeSuggestions(): List<KeyMergeSuggestion> {
        return listOf(
            KeyMergeSuggestion(
                id = "merge_001",
                description = "将多个 boolean flag 合并为一个 Int 位掩码",
                originalKeys = listOf("feature_a_enabled", "feature_b_enabled", "feature_c_enabled", "feature_d_enabled", "feature_e_enabled"),
                mergedKey = "feature_flags",
                mergedType = "Int (位掩码)",
                savingsPercent = 80f,
                sampleCode = """
// 合并前: 5 个 boolean key
// sharedPreferences.edit()
//     .putBoolean("feature_a_enabled", true)
//     .putBoolean("feature_b_enabled", false)
//     .putBoolean("feature_c_enabled", true)
//     .putBoolean("feature_d_enabled", false)
//     .putBoolean("feature_e_enabled", true)
//     .apply()

// 合并后: 1 个 Int key (节省 80% key 数量)
const val FLAG_A = 0b00001
const val FLAG_B = 0b00010
const val FLAG_C = 0b00100
const val FLAG_D = 0b01000
const val FLAG_E = 0b10000

val featureFlags = FLAG_A or FLAG_C or FLAG_E // 0b10101 = 21
sharedPreferences.edit().putInt("feature_flags", featureFlags).apply()

// 读取时使用位运算
val isFeatureAEnabled = (featureFlags and FLAG_A) != 0
val isFeatureBEnabled = (featureFlags and FLAG_B) != 0
                """.trimIndent()
            ),
            KeyMergeSuggestion(
                id = "merge_002",
                description = "将多个 String 配置 key 合并为一个 JSON 对象",
                originalKeys = listOf("config_theme", "config_language", "config_font_size", "config_notifications", "config_sync"),
                mergedKey = "app_config",
                mergedType = "String (JSON)",
                savingsPercent = 60f,
                sampleCode = """
// 合并前: 5 个独立 key
// 合并后: 1 个 JSON key

data class AppConfig(
    val theme: String = "light",
    val language: String = "zh-CN",
    val fontSize: Int = 14,
    val notifications: Boolean = true,
    val syncEnabled: Boolean = false
)

val gson = Gson()
val config = AppConfig()

// 写入: 1 个 JSON key 代替 5 个独立 key
editor.putString("app_config", gson.toJson(config)).apply()

// 读取: 从 JSON 反序列化
val json = sharedPreferences.getString("app_config", "{}")
val loadedConfig = gson.fromJson(json, AppConfig::class.java)
                """.trimIndent()
            ),
            KeyMergeSuggestion(
                id = "merge_003",
                description = "将历史埋点事件压缩为数组存储",
                originalKeys = (1..20).map { "event_history_$it" },
                mergedKey = "event_history_batch",
                mergedType = "String (JSON Array)",
                savingsPercent = 75f,
                sampleCode = """
// 合并前: 20 个独立 event key
// 合并后: 1 个 JSON Array key

val events = listOf(
    Event("click", "button_a", System.currentTimeMillis()),
    Event("view", "screen_b", System.currentTimeMillis()),
    Event("click", "button_c", System.currentTimeMillis())
)

val gson = Gson()

// 写入: 1 个 JSON Array key 代替 20 个独立 key
editor.putString("event_history_batch", gson.toJson(events)).apply()

// 读取: 一次性获取所有历史事件
val json = sharedPreferences.getString("event_history_batch", "[]")
val eventList = gson.fromJson(json, Array<Event>::class.java).toList()
                """.trimIndent()
            )
        )
    }

    /**
     * createMockCIHistory — 创建模拟 CI 构建历史
     */
    private fun createMockCIHistory(): List<CIBuildRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            CIBuildRecord(
                id = "build_001",
                timestamp = now - 86400000,
                keyCount = 118_000,
                threshold = 200_000,
                status = CIStatus.PASS,
                buildNumber = 1247
            ),
            CIBuildRecord(
                id = "build_002",
                timestamp = now - 172800000,
                keyCount = 115_000,
                threshold = 200_000,
                status = CIStatus.PASS,
                buildNumber = 1246
            ),
            CIBuildRecord(
                id = "build_003",
                timestamp = now - 259200000,
                keyCount = 112_000,
                threshold = 200_000,
                status = CIStatus.PASS,
                buildNumber = 1245
            ),
            CIBuildRecord(
                id = "build_004",
                timestamp = now - 345600000,
                keyCount = 108_000,
                threshold = 200_000,
                status = CIStatus.PASS,
                buildNumber = 1244
            ),
            CIBuildRecord(
                id = "build_005",
                timestamp = now - 432000000,
                keyCount = 125_000, // Current - over previous threshold
                threshold = 200_000,
                status = CIStatus.PASS,
                buildNumber = 1243
            ),
            CIBuildRecord(
                id = "build_006",
                timestamp = now - 518400000,
                keyCount = 185_000, // Near threshold
                threshold = 200_000,
                status = CIStatus.WARNING,
                buildNumber = 1242
            )
        )
    }

    /**
     * createMockKeyTrendHistory — 创建模拟 Key 数量趋势历史
     */
    private fun createMockKeyTrendHistory(): List<KeyTrendPoint> {
        val now = System.currentTimeMillis()
        val history = mutableListOf<KeyTrendPoint>()
        var baseCount = 100_000
        for (i in 29 downTo 0) {
            baseCount += ((-500..800)).random()
            history.add(KeyTrendPoint(timestamp = now - i * 86400000L, keyCount = baseCount))
        }
        // Add current
        history.add(KeyTrendPoint(timestamp = now, keyCount = 125_000))
        return history
    }
}
