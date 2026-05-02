package com.mvi.kenny.feature.room3migration

// ================================================================
// Room3MigrationContract — Room 3.0 破坏性变更迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for Room 3.0 Migration Toolkit.
//
// PRD-212: Room 3.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-212-Room-3-0-破坏性变更迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (navigation, toast), via Channel
// ================================================================

import androidx.compose.ui.graphics.Color

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 * Room 迁移风险等级，从安全到危险共 5 档。
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param color 风险颜色
 */
enum class RiskLevel(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    /** 严重 — 核心 breaking change，必须立即处理 */
    P0_CRITICAL("P0 严重", "🔴", Color(0xFFFF6B6B)),
    /** 高风险 — 主要 breaking change，需要认真处理 */
    P1_HIGH("P1 高风险", "🟠", Color(0xFFFFB347)),
    /** 中风险 — 次要变更，建议处理 */
    P2_MEDIUM("P2 中风险", "🟡", Color(0xFFFFD93D)),
    /** 低风险 — 建议处理，非强制 */
    P3_LOW("P3 低风险", "🟢", Color(0xFF6BCF7F)),
    /** 未知 — 尚未扫描 */
    UNKNOWN("未知", "⚪", Color(0xFF8B949E))
}

/**
 * ============================================================
 * ScanTab — 扫描 Tab 枚举
 * ============================================================
 * 风险扫描 Tab 中的子工具。
 */
enum class ScanTab(val title: String, val emoji: String) {
    KAPT_SCANNER("KAPT 检测", "🔍"),
    CHECKLIST("完整检查清单", "📋")
}

/**
 * ============================================================
 * MigrationStatus — 迁移状态枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 */
enum class MigrationStatus(val displayName: String, val emoji: String) {
    PENDING("待处理", "⏳"),
    IN_PROGRESS("进行中", "🔄"),
    COMPLETED("已完成", "✅"),
    FAILED("失败", "❌")
}

/**
 * ============================================================
 * GradleFileType — Gradle 文件类型枚举
 * ============================================================
 *
 * @param displayName 中文显示名称
 */
enum class GradleFileType(val displayName: String) {
    KOTLIN_DSL("Kotlin DSL (.kts)"),
    GROOVY_DSL("Groovy DSL (.gradle)")
}

/**
 * ============================================================
 * Platform — Driver API 目标平台枚举
 * ============================================================
 *
 * @param displayName 平台显示名称
 * @param icon Emoji 图标
 */
enum class Platform(val displayName: String, val icon: String) {
    ANDROID("Android", "🤖"),
    IOS("iOS", "🍎"),
    JS("JavaScript / WASM", "🌐"),
    DESKTOP("Desktop (JVM)", "🖥️")
}

/**
 * ============================================================
 * Room3MigrationState — Room 3.0 迁移工具页面状态（MVI State）
 * ============================================================
 * Immutable page state, single source of truth.
 *
 * @param currentTab 当前 Tab 索引 (0-4)
 * @param projectPath 项目路径
 * @param gradleFileType Gradle 文件类型
 * @param gradleFileContent 用户粘贴的 Gradle 文件内容
 * @param isScanning 是否正在扫描
 * @param scanResults 扫描结果
 * @param kspMigrationDiff KSP 迁移 Diff
 * @param daoMigrations DAO 迁移列表
 * @param packageReplacements Package 重命名替换列表
 * @param driverApiGuide Driver API 指南
 * @param ciComplianceStatus CI 合规状态
 * @param migrationPlan 迁移计划
 * @param decisionGuide 决策指南
 * @param exportedReport 导出的报告
 * @param snackbarMessage Snackbar 消息
 * @param isSimulatingFile 是否正在模拟文件导入
 *
 * @see RiskLevel
 * @see ScanTab
 */
data class Room3MigrationState(
    // ── Navigation / 全局状态 ────────────────────────────────────
    val currentTab: Int = 0,
    val projectPath: String = "",
    val gradleFileType: GradleFileType = GradleFileType.KOTLIN_DSL,
    val gradleFileContent: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,

    // ── Tab 1: 风险扫描 ────────────────────────────────────────
    val scanTab: ScanTab = ScanTab.KAPT_SCANNER,
    val scanResults: ScanResults = ScanResults(),

    // ── Tab 2: KSP 迁移 ─────────────────────────────────────────
    val kspMigrationDiff: KspDiff? = null,
    val kspVersion: String = "1.0.25", // Room 3.0 recommended KSP version
    val isGeneratingKspDiff: Boolean = false,

    // ── Tab 3: DAO 改造 ─────────────────────────────────────────
    val daoMigrations: List<DaoMigration> = emptyList(),
    val rawQueryTemplates: List<RawQueryTemplate> = emptyList(),
    val isScanningDao: Boolean = false,

    // ── Tab 4: 包与驱动 ──────────────────────────────────────────
    val packageReplacements: List<PackageReplacement> = emptyList(),
    val driverApiGuide: DriverApiGuide? = null,
    val selectedPlatform: Platform = Platform.ANDROID,
    val isGeneratingDriverGuide: Boolean = false,

    // ── Tab 5: 验证上线 ──────────────────────────────────────────
    val ciComplianceStatus: CiComplianceStatus = CiComplianceStatus.UNKNOWN,
    val migrationPlan: MigrationPlan? = null,
    val decisionGuide: DecisionGuide? = null,
    val migrationProgress: MigrationProgress = MigrationProgress(),

    // ── 全局 ────────────────────────────────────────────────────
    val exportedReport: String? = null,
    val snackbarMessage: String? = null,
    val isSimulatingFile: Boolean = false
) {
    companion object {
        /** Initial/empty state */
        val Initial = Room3MigrationState()
    }
}

/**
 * ============================================================
 * Room3MigrationIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 * ViewModel receives Intent, processes business logic, then updates State.
 *
 * @see Room3MigrationViewModel.sendIntent handles all Intents
 */
sealed interface Room3MigrationIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : Room3MigrationIntent

    /** 用户切换扫描子 Tab
     * @param tab 子 Tab
     */
    data class SelectScanTab(val tab: ScanTab) : Room3MigrationIntent

    /** 用户输入项目路径
     * @param path 项目路径
     */
    data class SetProjectPath(val path: String) : Room3MigrationIntent

    /** 用户选择 Gradle 文件类型
     * @param type 文件类型
     */
    data class SelectGradleFileType(val type: GradleFileType) : Room3MigrationIntent

    /** 用户粘贴 Gradle 文件内容
     * @param content 文件内容
     */
    data class PasteGradleContent(val content: String) : Room3MigrationIntent

    /** 用户导入项目（模拟扫描）
     * @param simulate 是否使用模拟数据
     */
    data class ImportProject(val simulate: Boolean = false) : Room3MigrationIntent

    /** 用户触发完整扫描 */
    data object RunFullScan : Room3MigrationIntent

    /** 用户生成 KSP 迁移 Diff */
    data object GenerateKspMigration : Room3MigrationIntent

    /** 用户复制 DAO 迁移代码
     * @param dao DAO 迁移项
     */
    data class CopyDaoMigration(val dao: DaoMigration) : Room3MigrationIntent

    /** 用户运行 Package 重命名替换 */
    data object RunPackageReplacement : Room3MigrationIntent

    /** 用户生成 Driver API 指南
     * @param platform 目标平台
     */
    data class GenerateDriverGuide(val platform: Platform) : Room3MigrationIntent

    /** 用户选择 Driver 平台
     * @param platform 平台
     */
    data class SelectPlatform(val platform: Platform) : Room3MigrationIntent

    /** 用户设置 KSP 版本
     * @param version KSP 版本
     */
    data class SetKspVersion(val version: String) : Room3MigrationIntent

    /** 用户触发 CI 合规检测 */
    data object SetupCiCompliance : Room3MigrationIntent

    /** 用户生成渐进迁移计划 */
    data object GenerateMigrationPlan : Room3MigrationIntent

    /** 用户导出迁移报告 */
    data object ExportReport : Room3MigrationIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : Room3MigrationIntent

    /** 用户重置所有状态 */
    data object ResetAll : Room3MigrationIntent
}

/**
 * ============================================================
 * Room3MigrationEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 * UI layer listens via LaunchedEffect + flow.collect{}.
 *
 * @see Room3MigrationViewModel _effect.send() sends Effects
 */
sealed interface Room3MigrationEffect {

    /** 显示 Snackbar 消息
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : Room3MigrationEffect

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : Room3MigrationEffect

    /** 分享报告
     * @param content 报告内容
     */
    data class ShareReport(val content: String) : Room3MigrationEffect

    /** 显示错误
     * @param message 错误消息
     */
    data class ShowError(val message: String) : Room3MigrationEffect
}

// ================================================================
// 数据模型 / Data Models
// ================================================================

/**
 * ============================================================
 * ScanResults — 扫描结果
 * ============================================================
 *
 * @param kaptUsages KAPT 使用位置列表
 * @param breakingChanges 所有 breaking change 列表
 * @param daoNonSuspend 非 suspend DAO 函数列表
 * @param packageImports room.* import 列表
 * @param riskLevel 整体风险等级
 */
data class ScanResults(
    val kaptUsages: List<KaptUsage> = emptyList(),
    val breakingChanges: List<BreakingChange> = emptyList(),
    val daoNonSuspend: List<DaoFunction> = emptyList(),
    val packageImports: List<PackageImport> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.UNKNOWN,
    val modules: List<String> = emptyList() // 多模块项目模块列表
)

/**
 * ============================================================
 * KaptUsage — KAPT 使用位置
 * ============================================================
 *
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param configSnippet 配置代码片段
 * @param severity 严重程度
 */
data class KaptUsage(
    val filePath: String,
    val lineNumber: Int,
    val configSnippet: String,
    val severity: RiskLevel = RiskLevel.P1_HIGH,
    val module: String = "app"
)

/**
 * ============================================================
 * BreakingChange — 破坏性变更条目
 * ============================================================
 *
 * @param id 变更 ID
 * @param title 变更标题
 * @param description 变更描述
 * @param beforeBefore 变更前代码
 * @param afterCode 变更后代码
 * @param riskLevel 风险等级
 * @param category 类别（KSP/DAO/Package/Driver/RawQuery）
 * @param estimatedEffortMinutes 预估工作量（分钟）
 */
data class BreakingChange(
    val id: String,
    val title: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String,
    val riskLevel: RiskLevel,
    val category: String,
    val estimatedEffortMinutes: Int,
    val isCompleted: Boolean = false
)

/**
 * ============================================================
 * DaoFunction — DAO 函数
 * ============================================================
 *
 * @param filePath 文件路径
 * @param functionName 函数名
 * @param isSuspend 是否为 suspend 函数
 * @param returnType 返回类型
 * @param daoInterfaceName DAO 接口名
 * @param module 模块名
 * @param suggestedMigration 建议的迁移方式
 * @param migrationDiff 迁移 Diff（Before → After）
 */
data class DaoFunction(
    val filePath: String,
    val functionName: String,
    val isSuspend: Boolean,
    val returnType: String,
    val daoInterfaceName: String,
    val module: String = "app",
    val suggestedMigration: String = "",
    val migrationDiff: String = ""
)

/**
 * ============================================================
 * PackageImport — Package Import 条目
 * ============================================================
 *
 * @param originalPackage 原始 package（androidx.room.*）
 * @param newPackage 新 package（androidx.room3.*）
 * @param occurrences 出现次数
 * @param affectedFiles 受影响文件列表
 * @param isReplaced 是否已替换
 */
data class PackageImport(
    val originalPackage: String,
    val newPackage: String,
    val occurrences: Int,
    val affectedFiles: List<String>,
    val isReplaced: Boolean = false
)

/**
 * ============================================================
 * DaoMigration — DAO 迁移项
 * ============================================================
 *
 * @param daoFunction 关联的 DAO 函数
 * @param status 迁移状态
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 * @param notes 备注
 */
data class DaoMigration(
    val daoFunction: DaoFunction,
    val status: MigrationStatus = MigrationStatus.PENDING,
    val beforeCode: String,
    val afterCode: String,
    val notes: String = ""
)

/**
 * ============================================================
 * KspDiff — KSP 迁移 Diff
 * ============================================================
 *
 * @param beforeContent 迁移前内容
 * @param afterContent 迁移后内容
 * @param kspVersion 推荐的 KSP 版本
 * @param roomVersion Room 版本
 */
data class KspDiff(
    val beforeContent: String,
    val afterContent: String,
    val kspVersion: String,
    val roomVersion: String = "3.0.0-alpha01"
)

/**
 * ============================================================
 * RawQueryTemplate — @RawQuery 迁移模板
 * ============================================================
 *
 * @param id 模板 ID
 * @param name 模板名称
 * @param description 模板描述
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 * @param适用场景 applicable scenario
 */
data class RawQueryTemplate(
    val id: String,
    val name: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String,
    val applicableScenario: String
)

/**
 * ============================================================
 * PackageReplacement — Package 重命名替换项
 * ============================================================
 *
 * @param packageImport Package import 信息
 * @param status 替换状态
 * @param filesToUpdate 需要更新的文件列表
 */
data class PackageReplacement(
    val packageImport: PackageImport,
    val status: MigrationStatus = MigrationStatus.PENDING,
    val filesToUpdate: List<String> = emptyList()
)

/**
 * ============================================================
 * DriverApiGuide — Driver API 迁移指南
 * ============================================================
 *
 * @param platform 目标平台
 * @param beforeCode 迁移前代码
 * @param afterCode 迁移后代码
 * @param description 描述
 * @param notes 注意事项
 */
data class DriverApiGuide(
    val platform: Platform,
    val beforeCode: String,
    val afterCode: String,
    val description: String,
    val notes: String = ""
)

/**
 * ============================================================
 * CiComplianceStatus — CI 合规状态
 * ============================================================
 *
 * @param displayName 中文显示名称
 * @param emoji Emoji representation
 * @param passedChecks 通过的检查数
 * @param failedChecks 失败的检查数
 * @param warningChecks 警告的检查数
 */
enum class CiComplianceStatus(
    val displayName: String,
    val emoji: String,
    val passedChecks: Int,
    val failedChecks: Int,
    val warningChecks: Int
) {
    UNKNOWN("未知", "⚪", 0, 0, 0),
    PASS("通过", "✅", 10, 0, 0),
    FAIL("失败", "❌", 3, 7, 0),
    WARNING("警告", "⚠️", 6, 1, 3)
}

/**
 * ============================================================
 * MigrationPlan — 渐进迁移计划
 * ============================================================
 *
 * @param phases 迁移阶段列表
 * @param totalEstimatedHours 预估总工时（小时）
 * @param criticalPath 关键路径模块
 */
data class MigrationPlan(
    val phases: List<MigrationPhase> = emptyList(),
    val totalEstimatedHours: Int = 0,
    val criticalPath: List<String> = emptyList()
)

/**
 * ============================================================
 * MigrationPhase — 迁移阶段
 * ============================================================
 *
 * @param phaseNumber 阶段编号
 * @param title 阶段标题
 * @param description 阶段描述
 * @param tasks 任务列表
 * @param estimatedHours 预估工时（小时）
 * @param dependencies 依赖的前置阶段
 * @param isCompleted 是否完成
 */
data class MigrationPhase(
    val phaseNumber: Int,
    val title: String,
    val description: String,
    val tasks: List<MigrationTask> = emptyList(),
    val estimatedHours: Int,
    val dependencies: List<Int> = emptyList(),
    val isCompleted: Boolean = false
)

/**
 * ============================================================
 * MigrationTask — 迁移任务
 * ============================================================
 *
 * @param taskId 任务 ID
 * @param title 任务标题
 * @param description 任务描述
 * @param riskLevel 关联的风险等级
 * @param isCompleted 是否完成
 */
data class MigrationTask(
    val taskId: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel = RiskLevel.P2_MEDIUM,
    val isCompleted: Boolean = false
)

/**
 * ============================================================
 * DecisionGuide — 决策指南
 * ============================================================
 *
 * @param recommendation 建议（升级/等待）
 * @param reasoning 理由
 * @param currentRoomVersion 当前 Room 版本
 * @param recommendedRoomVersion 推荐的 Room 版本
 * @param factors 决策因素列表
 */
data class DecisionGuide(
    val recommendation: String, // "UPGRADE_NOW" / "WAIT" / "CONDITIONAL"
    val reasoning: String,
    val currentRoomVersion: String,
    val recommendedRoomVersion: String,
    val factors: List<String> = emptyList()
)

/**
 * ============================================================
 * MigrationProgress — 迁移进度
 * ============================================================
 *
 * @param kaptMigrated KAPT 已迁移模块数
 * @param kaptTotal KAPT 总模块数
 * @param daoMigrated DAO 已迁移数
 * @param daoTotal DAO 总数
 * @param packagesMigrated Package 已迁移数
 * @param packagesTotal Package 总数
 * @param driverMigrated Driver 已迁移数
 * @param driverTotal Driver 总数
 */
data class MigrationProgress(
    val kaptMigrated: Int = 0,
    val kaptTotal: Int = 0,
    val daoMigrated: Int = 0,
    val daoTotal: Int = 0,
    val packagesMigrated: Int = 0,
    val packagesTotal: Int = 0,
    val driverMigrated: Int = 0,
    val driverTotal: Int = 0
) {
    val overallPercent: Float
        get() {
            val total = kaptTotal + daoTotal + packagesTotal + driverTotal
            if (total == 0) return 0f
            val done = kaptMigrated + daoMigrated + packagesMigrated + driverMigrated
            return (done.toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f)
        }
}

// ================================================================
// 默认/模拟数据 / Default & Simulation Data
// ================================================================

/**
 * ============================================================
 * SIMULATED_BREAKING_CHANGES — Room 3.0 破坏性变更清单
 * ============================================================
 * Reference: Android Developers Blog "Room 3.0 - Modernizing the Room" (2026-03)
 */
val SIMULATED_BREAKING_CHANGES = listOf(
    BreakingChange(
        id = "bc-001",
        title = "KAPT 彻底移除 — 必须迁移到 KSP",
        description = "Room 3.0 完全抛弃 KAPT，所有项目必须迁移到 KSP。旧版 kapt plugin 和 room-compiler kapt 配置全部失效。",
        beforeCode = """// build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")  // ❌ Room 3.0 必须移除
}

dependencies {
    kapt("androidx.room:room-compiler:2.6.1")  // ❌ 改用 ksp
}""",
        afterCode = """// build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")  // ✅ Room 3.0 使用 KSP
}

dependencies {
    ksp("androidx.room:room-compiler:3.0.0-alpha01")  // ✅
}""",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "KSP",
        estimatedEffortMinutes = 30
    ),
    BreakingChange(
        id = "bc-002",
        title = "全员 suspend — 所有同步 DAO 函数必须改为 suspend",
        description = "Room 3.0 要求所有 DAO 函数都是 suspend 函数。返回类型必须受 Room 管理或 Flow/Iterable，不支持的场景用 @RawQuery。",
        beforeCode = """// UserDao.kt ❌ Room 2.x 同步写法
@Dao
interface UserDao {
    fun getUser(id: Int): User
    fun getAllUsers(): List<User>
    fun insert(user: User)
}""",
        afterCode = """// UserDao.kt ✅ Room 3.0 suspend 写法
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Int): User?
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>  // ✅ Flow 不需要 suspend
    
    @Insert
    suspend fun insert(user: User)
}""",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "DAO",
        estimatedEffortMinutes = 120
    ),
    BreakingChange(
        id = "bc-003",
        title = "Package 重命名 — androidx.room.* → androidx.room3.*",
        description = "Room 3.0 所有类迁移到新包名 androidx.room3.*。新旧包不共存，必须批量替换所有 import 语句。",
        beforeCode = """// ❌ Room 2.x imports
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.RoomDatabase
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update""",
        afterCode = """// ✅ Room 3.0 imports
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.RoomDatabase
import androidx.room3.Query
import androidx.room3.Insert
import androidx.room3.Delete
import androidx.room3.Update""",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Package",
        estimatedEffortMinutes = 60
    ),
    BreakingChange(
        id = "bc-004",
        title = "Driver API 重写 — 新的多平台驱动架构",
        description = "Room 3.0 引入 SQLiteDriver/SQLiteDriverLite/AndroidDriver 等新驱动架构，多平台项目需要重写数据库初始化代码。",
        beforeCode = """// ❌ Room 2.x 旧 Driver 写法
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "myapp.db"
).build()""",
        afterCode = """// ✅ Room 3.0 新 Driver 写法 (Android)
val db = Room.databaseBuilder<AppDatabase>(
    driver = AndroidDriver("myapp.db")  // ✅ 新 API
).build()

// ✅ Room 3.0 多平台写法
val driver = SQLiteDriver()
val db = database {
    legacyGenerateSchema = false
    return@database AppDatabase::class
}""",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Driver",
        estimatedEffortMinutes = 90
    ),
    BreakingChange(
        id = "bc-005",
        title = "@RawQuery 替代复杂查询",
        description = "Room 3.0 中某些复杂查询场景需要迁移到 @RawQuery，或重新设计的 Query API。",
        beforeCode = """// ❌ 可能不兼容的旧查询写法
@Query("SELECT u.*, p.* FROM users u LEFT JOIN posts p ON u.id = p.userId")
abstract fun getUsersWithPosts(): List<UserWithPost>""",
        afterCode = """// ✅ @RawQuery 迁移模板
@RawQuery(observedEntities = [User::class, Post::class])
abstract fun getUsersWithPosts(
    query: SupportSQLiteQuery
): Flow<List<UserWithPost>>""",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "RawQuery",
        estimatedEffortMinutes = 45
    )
)

/**
 * ============================================================
 * SIMULATED_KAPT_USAGES — 模拟 KAPT 使用位置
 * ============================================================
 */
val SIMULATED_KAPT_USAGES = listOf(
    KaptUsage(
        filePath = "app/build.gradle.kts",
        lineNumber = 18,
        configSnippet = """kapt("androidx.room:room-compiler:2.6.1")""",
        severity = RiskLevel.P0_CRITICAL,
        module = "app"
    ),
    KaptUsage(
        filePath = "data/build.gradle.kts",
        lineNumber = 22,
        configSnippet = """kapt("androidx.room:room-compiler:2.6.1")""",
        severity = RiskLevel.P0_CRITICAL,
        module = "data"
    ),
    KaptUsage(
        filePath = "features/user/build.gradle.kts",
        lineNumber = 15,
        configSnippet = """kapt("androidx.room:room-compiler:2.6.1")""",
        severity = RiskLevel.P1_HIGH,
        module = "features:user"
    )
)

/**
 * ============================================================
 * SIMULATED_DAO_FUNCTIONS — 模拟非 Suspend DAO 函数
 * ============================================================
 */
val SIMULATED_DAO_FUNCTIONS = listOf(
    DaoFunction(
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        functionName = "getUser",
        isSuspend = false,
        returnType = "User",
        daoInterfaceName = "UserDao",
        module = "app",
        suggestedMigration = "改为 suspend fun，并在调用方使用 viewModelScope.launch 或其他协程作用域",
        migrationDiff = """// Before ❌
fun getUser(id: Int): User

// After ✅
suspend fun getUser(id: Int): User?"""
    ),
    DaoFunction(
        filePath = "data/src/main/java/com/example/data/local/PostDao.kt",
        functionName = "getAllPosts",
        isSuspend = false,
        returnType = "List<Post>",
        daoInterfaceName = "PostDao",
        module = "data",
        suggestedMigration = "改为 Flow<List<Post>>，Room 会自动管理订阅生命周期",
        migrationDiff = """// Before ❌
fun getAllPosts(): List<Post>

// After ✅
fun getAllPosts(): Flow<List<Post>>  // 不需要 suspend，Flow 自动异步"""
    ),
    DaoFunction(
        filePath = "data/src/main/java/com/example/data/local/PostDao.kt",
        functionName = "insertPost",
        isSuspend = false,
        returnType = "Long",
        daoInterfaceName = "PostDao",
        module = "data",
        suggestedMigration = "改为 suspend fun，Room 会自动处理写操作的事务管理",
        migrationDiff = """// Before ❌
fun insertPost(post: Post): Long

// After ✅
suspend fun insertPost(post: Post): Long"""
    )
)

/**
 * ============================================================
 * SIMULATED_PACKAGE_IMPORTS — 模拟 Package Import 统计
 * ============================================================
 */
val SIMULATED_PACKAGE_IMPORTS = listOf(
    PackageImport(
        originalPackage = "androidx.room",
        newPackage = "androidx.room3",
        occurrences = 47,
        affectedFiles = listOf(
            "app/src/main/java/com/example/app/data/local/UserDao.kt",
            "app/src/main/java/com/example/app/data/local/PostDao.kt",
            "data/src/main/java/com/example/data/local/Database.kt",
            "data/src/main/java/com/example/data/local/Entity.kt"
        )
    ),
    PackageImport(
        originalPackage = "androidx.room.persistence",
        newPackage = "androidx.room3.persistence",
        occurrences = 12,
        affectedFiles = listOf(
            "data/src/main/java/com/example/data/local/Database.kt"
        )
    )
)

/**
 * ============================================================
 * SIMULATED_RAW_QUERY_TEMPLATES — @RawQuery 迁移模板
 * ================================================================
 */
val SIMULATED_RAW_QUERY_TEMPLATES = listOf(
    RawQueryTemplate(
        id = "rqt-001",
        name = "单结果查询（Single）",
        description = "返回单个对象的 @RawQuery 写法",
        beforeCode = """@Query("SELECT * FROM users WHERE id = :id")
abstract fun getUserById(id: Int): User""",
        afterCode = """@RawQuery(observedEntities = [User::class])
abstract fun getUserById(query: SupportSQLiteQuery): User?

// Helper:
suspend fun UserDao.getUserById(id: Int): User? {
    return getUserById(SimpleSQLiteQuery("SELECT * FROM users WHERE id = ?", id))
}""",
        applicableScenario = "需要动态构建查询条件的场景"
    ),
    RawQueryTemplate(
        id = "rqt-002",
        name = "多结果查询（Flow）",
        description = "返回 Flow<List> 的 @RawQuery 写法",
        beforeCode = """@Query("SELECT * FROM users ORDER BY name ASC")
abstract fun getAllUsers(): List<User>""",
        afterCode = """@RawQuery(observedEntities = [User::class])
abstract fun getAllUsersFlow(query: SupportSQLiteQuery): Flow<List<User>>

// Helper:
fun UserDao.getAllUsersFlow(): Flow<List<User>> {
    return getAllUsersFlow(SimpleSQLiteQuery("SELECT * FROM users ORDER BY name ASC"))
}""",
        applicableScenario = "需要 Flow 响应式数据流的场景"
    ),
    RawQueryTemplate(
        id = "rqt-003",
        name = "带参数的单结果查询",
        description = "带参数的 @RawQuery 单结果查询",
        beforeCode = """@Query("SELECT * FROM posts WHERE authorId = :authorId AND published = 1")
abstract fun getPublishedPosts(authorId: Int): List<Post>""",
        afterCode = """@RawQuery(observedEntities = [Post::class])
abstract fun getPublishedPosts(query: SupportSQLiteQuery): Flow<List<Post>>

// Helper:
fun PostDao.getPublishedPosts(authorId: Int): Flow<List<Post>> {
    return getPublishedPosts(SimpleSQLiteQuery(
        "SELECT * FROM posts WHERE authorId = ? AND published = 1",
        authorId
    ))
}""",
        applicableScenario = "复杂 WHERE 子句，需要动态组装的场景"
    )
)

/**
 * ============================================================
 * SIMULATED_DRIVER_GUIDES — 各平台 Driver API 指南
 * ================================================================
 */
val SIMULATED_DRIVER_GUIDES = mapOf(
    Platform.ANDROID to DriverApiGuide(
        platform = Platform.ANDROID,
        beforeCode = """// Room 2.x Android Driver
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "myapp.db"
).build()""",
        afterCode = """// Room 3.0 Android Driver
import androidx.room3.driver.AndroidDriver
import androidx.room3.driver.toAndroidDriver

val driver = AndroidDriver(
    context = context,
    name = "myapp.db",
    // 可选：配置迁移
    callback = RoomDatabase.Callback { /* migration logic */ }
)

val db = database {
    // ✅ 使用新 Driver API
    driver(AndroidDriver::class) {
        // Android-specific 配置
    }
}""",
        description = "Android 平台使用 AndroidDriver，自动处理 Android 特有的线程管理和生命周期",
        notes = "AndroidDriver 内部使用 HandlerThread 确保所有 Room 操作在正确的线程执行"
    ),
    Platform.IOS to DriverApiGuide(
        platform = Platform.IOS,
        beforeCode = """// Room 2.x iOS（不支持）
// Room 2.x 没有官方 iOS 支持""",
        afterCode = """// Room 3.0 iOS Driver
import androidx.room3.driver.native.NativeDriver
import androidx.room3.driver.native.toNativeDriver

// 在 iOS 原生代码中
val driver = NativeDriver(
    databasePath = "myapp.db"
)

val db = database {
    driver(NativeDriver::class)
}""",
        description = "iOS 平台使用 NativeDriver，直接操作 SQLite，需要 gradle.native.target = 'ios'",
        notes = "iOS Driver 需要 Kotlin Multiplatform 项目配置，支持 arm64 和 x64 模拟器"
    ),
    Platform.JS to DriverApiGuide(
        platform = Platform.JS,
        beforeCode = """// Room 2.x — 不支持 JS""",
        afterCode = """// Room 3.0 JS/WASM Driver
import androidx.room3.driver.sqlite.WasmDriver
import androidx.room3.driver.sqlite.open. open

// Web Worker 中运行 SQLite
val driver = open {
    url("file:myapp.db")  // WASM 内嵌 SQLite
}

val db = database {
    driver(WasmDriver::class)
}

// 或使用 JS SQL.js 驱动
import androidx.room3.driver.sqlite.js.JsSqliteDriver""",
        description = "JavaScript / WasmJS 平台使用 WASM 嵌入式 SQLite，DAO suspend 设计适配 web workers",
        notes = "WASM 平台下所有 DAO 函数必须是 suspend，因为 WASM 不支持真实的多线程，使用协程模拟异步"
    ),
    Platform.DESKTOP to DriverApiGuide(
        platform = Platform.DESKTOP,
        beforeCode = """// Room 2.x Desktop (通过 Android Driver 间接支持)
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "myapp.db"
).build()""",
        afterCode = """// Room 3.0 Desktop Driver
import androidx.room3.driver.jdbc.JdbcDriver
import androidx.room3.driver.sqlite.SQLiteDriver

// JVM Desktop 使用 JDBC 或原生 SQLite
val driver = SQLiteDriver(
    directory = Path("~/.myapp/"),
    name = "myapp.db"
)

val db = database {
    driver(SQLiteDriver::class)
}""",
        description = "JVM Desktop 平台使用 SQLiteDriver，支持原生 SQLite 或 JDBC 桥接",
        notes = "Desktop Driver 支持进程内 SQLite，无需网络，适合桌面应用的离线存储"
    )
)

/**
 * ============================================================
 * SIMULATED_DECISION_GUIDE — Room 3.0 vs 2.6.x 决策指南
 * ================================================================
 */
val SIMULATED_DECISION_GUIDE = DecisionGuide(
    recommendation = "CONDITIONAL",
    reasoning = "Room 3.0 的 KSP-only 和全员 suspend 是重大破坏性变更，对于已有大量 Room 代码的存量项目，建议等 Room 3.0 稳定版发布后再升级（预计2026年Q3）。对于新项目或从零开始的项目，建议立即使用 Room 3.0 alpha/beta 版本。KSP-only 迁移复杂度高，DAO suspend 化需要全面重构，建议留出至少 2 周的迁移窗口。",
    currentRoomVersion = "2.6.1",
    recommendedRoomVersion = "3.0.0-alpha01",
    factors = listOf(
        "✅ KSP-only 是 Android 生态的正确方向，KAPT 已进入维护模式",
        "✅ Room 3.0 是真正的 KMP-first ORM，JS/WASM 支持是亮点",
        "⚠️ Room 3.0 稳定版尚未发布，alpha/beta API 可能变化",
        "⚠️ KAPT→KSP 迁移需要全面测试，增量编译验证",
        "⚠️ DAO suspend 化涉及大量代码改动，需要完整的 CI 回归测试",
        "⚠️ 新包名 androidx.room3.* 需要 IDE 全局替换和验证",
        "💡 Google I/O 2026（5月）预计有更多 Room 3.0 相关信息",
        "💡 Room 官方博客专门发迁移博文，说明复杂度高，建议留足时间"
    )
)