package com.mvi.kenny.feature.room3importmigration

// ================================================================
// Room3ImportMigrationContract — Room 3.0 Import 批量迁移工具包 MVI 契约
// ================================================================
// MVI architecture contract for Room 3.0 Import Batch Migration Toolkit.
//
// PRD-225: Room 3.0 破坏性变更迁移工具包
// Design Reference: memory/agency/designs/PRD-225-Room-3-0-破坏性变更迁移工具包.md
//
// MVI 三要素 / Three pillars:
//   Model (State)  — Immutable page state, single source of truth
//   View           — Composable function, consumes State, renders UI
//   Intent         — User intentions, ViewModel processes and updates State
//   Effect         — One-time side effects (clipboard, toast), via Channel
// ================================================================
//
// Tab Structure:
//   Tab 0: Import迁移 — Scan & batch-replace androidx.room → androidx.room3 imports
//   Tab 1: SQLiteDriver扫描 — Detect SupportSQLiteDatabase usage, output migration diff
//   Tab 2: Suspend函数指南 — Sync DAO → suspend migration patterns with Before/After
//   Tab 3: 双版本兼容 — Dual-version (2.x & 3.0) code organization strategy
//   Tab 4: CI合规检测 — Gradle plugin compliance check + migration checklist
// ================================================================

import androidx.compose.ui.graphics.Color

// ================================================================
// Risk Level & Status Enums / 风险等级 & 状态枚举
// ================================================================

/**
 * ============================================================
 * RiskLevel — 风险等级枚举
 * ============================================================
 * Room 迁移风险等级。
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
    P0_CRITICAL("P0 严重", "🔴", Color(0xFFF85149)),
    P1_HIGH("P1 高风险", "🟠", Color(0xFFD29922)),
    P2_MEDIUM("P2 中风险", "🟡", Color(0xFF3FB950)),
    P3_LOW("P3 低风险", "🟢", Color(0xFF6BCF7F)),
    UNKNOWN("未知", "⚪", Color(0xFF8B949E))
}

/**
 * ============================================================
 * MigrationStatus — 迁移状态枚举
 * ============================================================
 */
enum class MigrationStatus(val displayName: String, val emoji: String) {
    PENDING("待处理", "⏳"),
    IN_PROGRESS("进行中", "🔄"),
    COMPLETED("已完成", "✅"),
    FAILED("失败", "❌")
}

/**
 * ============================================================
 * Platform — 多平台目标枚举
 * ============================================================
 */
enum class Platform(val displayName: String, val icon: String) {
    ANDROID("Android", "🤖"),
    IOS("iOS", "🍎"),
    JS("JavaScript/WASM", "🌐"),
    DESKTOP("Desktop JVM", "🖥️")
}

// ================================================================
// State / 状态
// ================================================================

/**
 * ============================================================
 * Room3ImportMigrationState — Room 3.0 迁移工具页面状态（MVI State）
 * ============================================================
 *
 * @param selectedTab 当前 Tab 索引 (0-4)
 * @param projectPath 用户输入的项目路径
 * @param isScanning 是否正在扫描
 * @param scanProgress 扫描进度 0.0~1.0
 * @param scanProgressText 扫描进度文本
 *
 * Tab 0 — Import 迁移
 * @param importScanResults Import 扫描结果列表
 *
 * Tab 1 — SQLiteDriver 扫描
 * @param sqliteDriverResults SQLiteDriver 扫描结果列表
 *
 * Tab 2 — Suspend 函数指南
 * @param suspendGuideItems Suspend 改写模式列表
 *
 * Tab 3 — 双版本兼容
 * @param dualVersionStrategies 双版本兼容策略列表
 *
 * Tab 4 — CI 合规检测
 * @param ciChecklistItems CI 检查清单列表
 *
 * @param snackbarMessage Snackbar 消息
 */
data class Room3ImportMigrationState(
    // ── 全局状态 ───────────────────────────────────────────────
    val selectedTab: Int = 0,
    val projectPath: String = "",
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanProgressText: String = "",

    // ── Tab 0: Import 迁移 ───────────────────────────────────
    val importScanResults: List<ImportScanResult> = emptyList(),

    // ── Tab 1: SQLiteDriver 扫描 ──────────────────────────────
    val sqliteDriverResults: List<SQLiteDriverResult> = emptyList(),

    // ── Tab 2: Suspend 函数指南 ───────────────────────────────
    val suspendGuideItems: List<SuspendGuideItem> = emptyList(),

    // ── Tab 3: 双版本兼容 ─────────────────────────────────────
    val dualVersionStrategies: List<DualVersionStrategy> = emptyList(),

    // ── Tab 4: CI 合规检测 ───────────────────────────────────
    val ciChecklistItems: List<CIChecklistItem> = emptyList(),
    val ciCompliancePassed: Int = 0,
    val ciComplianceFailed: Int = 0,
    val ciComplianceWarning: Int = 0,
    val overallRiskLevel: RiskLevel = RiskLevel.UNKNOWN,

    // ── 全局 ─────────────────────────────────────────────────
    val snackbarMessage: String? = null
) {
    companion object {
        val Initial = Room3ImportMigrationState()
    }
}

// ================================================================
// Intent / 用户意图
// ================================================================

/**
 * ============================================================
 * Room3ImportMigrationIntent — 用户意图（User Intent）
 * ============================================================
 * Every user action corresponds to an Intent.
 */
sealed interface Room3ImportMigrationIntent {

    /** 用户切换主 Tab
     * @param index Tab 索引 (0-4)
     */
    data class SelectTab(val index: Int) : Room3ImportMigrationIntent

    /** 用户输入项目路径
     * @param path 项目路径
     */
    data class SetProjectPath(val path: String) : Room3ImportMigrationIntent

    /** 用户触发 Import 扫描
     * @param simulate 是否使用模拟数据（跳过实际文件扫描）
     */
    data class RunImportScan(val simulate: Boolean = false) : Room3ImportMigrationIntent

    /** 用户触发 SQLiteDriver 扫描
     * @param simulate 是否使用模拟数据
     */
    data class RunSQLiteDriverScan(val simulate: Boolean = false) : Room3ImportMigrationIntent

    /** 用户复制修复后代码
     * @param code 要复制的代码
     */
    data class CopyFixCode(val code: String) : Room3ImportMigrationIntent

    /** 用户切换 Checklist 条目状态
     * @param itemId 条目 ID
     * @param checked 是否勾选
     */
    data class ToggleChecklistItem(val itemId: String, val checked: Boolean) : Room3ImportMigrationIntent

    /** 用户关闭 Snackbar */
    data object DismissSnackbar : Room3ImportMigrationIntent

    /** 用户重置所有状态 */
    data object ResetAll : Room3ImportMigrationIntent
}

// ================================================================
// Effect / 副作用
// ================================================================

/**
 * ============================================================
 * Room3ImportMigrationEffect — 一次性副作用（Effect）
 * ============================================================
 * One-time events, immutable, can only be consumed once.
 */
sealed interface Room3ImportMigrationEffect {

    /** 复制到剪贴板
     * @param content 要复制的内容
     */
    data class CopyToClipboard(val content: String) : Room3ImportMigrationEffect

    /** 显示 Snackbar
     * @param message 消息文本
     */
    data class ShowSnackbar(val message: String) : Room3ImportMigrationEffect
}

// ================================================================
// Data Models / 数据模型
// ================================================================

/**
 * ============================================================
 * ImportScanResult — Import 扫描结果
 * ============================================================
 * Single import statement that needs to be migrated.
 *
 * @param originalImport 原始 import 语句（androidx.room.*）
 * @param newImport 修复后 import 语句（androidx.room3.*）
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param packageName 包名（androidx.room.Entity → androidx.room3.Entity）
 * @param isReplaced 是否已替换
 */
data class ImportScanResult(
    val originalImport: String,
    val newImport: String,
    val filePath: String,
    val lineNumber: Int,
    val packageName: String,
    val isReplaced: Boolean = false
)

/**
 * ============================================================
 * SQLiteDriverResult — SQLiteDriver 扫描结果
 * ============================================================
 * Code location that uses deprecated SupportSQLite API.
 *
 * @param filePath 文件路径
 * @param lineNumber 行号
 * @param apiUsage API 使用代码片段
 * @param suggestedFix 建议的修复方案
 * @param riskLevel 风险等级
 * @param beforeCode 修复前代码
 * @param afterCode 修复后代码
 */
data class SQLiteDriverResult(
    val filePath: String,
    val lineNumber: Int,
    val apiUsage: String,
    val suggestedFix: String,
    val riskLevel: RiskLevel,
    val beforeCode: String,
    val afterCode: String
)

/**
 * ============================================================
 * SuspendGuideItem — Suspend 函数改写指南条目
 * ============================================================
 *
 * @param id 条目 ID
 * @param patternName 模式名称
 * @param description 模式描述
 * @param beforeCode 改写前代码
 * @param afterCode 改写后代码
 * @param applicableScenario 适用场景
 * @param notes 注意事项
 * @param riskLevel 风险等级
 */
data class SuspendGuideItem(
    val id: String,
    val patternName: String,
    val description: String,
    val beforeCode: String,
    val afterCode: String,
    val applicableScenario: String,
    val notes: String = "",
    val riskLevel: RiskLevel = RiskLevel.P1_HIGH
)

/**
 * ============================================================
 * DualVersionStrategy — 双版本兼容策略
 * ============================================================
 *
 * @param id 策略 ID
 * @param name 策略名称
 * @param description 策略描述
 * @param approach Approach (Flavor/SourceSet/Conditional)
 * @param beforeCode 示例代码（迁移前）
 * @param afterCode 示例代码（迁移后）
 * @param pros 优点
 * @param cons 缺点
 * @param recommendedFor 推荐使用场景
 */
data class DualVersionStrategy(
    val id: String,
    val name: String,
    val description: String,
    val approach: String,
    val beforeCode: String,
    val afterCode: String,
    val pros: List<String>,
    val cons: List<String>,
    val recommendedFor: String
)

/**
 * ============================================================
 * CIChecklistItem — CI 合规检查清单条目
 * ============================================================
 *
 * @param id 条目 ID
 * @param title 检查项标题
 * @param description 检查项描述
 * @param riskLevel 风险等级
 * @param isChecked 是否已勾选
 * @param category 类别（Import/SQLiteDriver/Suspend/Driver/KMP）
 */
data class CIChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val isChecked: Boolean = false,
    val category: String
)

// ================================================================
// Default & Simulation Data / 默认 & 模拟数据
// ================================================================

/**
 * ============================================================
 * SIMULATED_IMPORT_RESULTS — 模拟 Import 扫描结果
 * ============================================================
 */
val SIMULATED_IMPORT_RESULTS = listOf(
    ImportScanResult(
        originalImport = "import androidx.room.Entity",
        newImport = "import androidx.room3.Entity",
        filePath = "app/src/main/java/com/example/app/data/local/User.kt",
        lineNumber = 3,
        packageName = "Entity"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Dao",
        newImport = "import androidx.room3.Dao",
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        lineNumber = 5,
        packageName = "Dao"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Database",
        newImport = "import androidx.room3.Database",
        filePath = "app/src/main/java/com/example/app/data/local/AppDatabase.kt",
        lineNumber = 7,
        packageName = "Database"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.RoomDatabase",
        newImport = "import androidx.room3.RoomDatabase",
        filePath = "app/src/main/java/com/example/app/data/local/AppDatabase.kt",
        lineNumber = 8,
        packageName = "RoomDatabase"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Query",
        newImport = "import androidx.room3.Query",
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        lineNumber = 9,
        packageName = "Query"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Insert",
        newImport = "import androidx.room3.Insert",
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        lineNumber = 10,
        packageName = "Insert"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Delete",
        newImport = "import androidx.room3.Delete",
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        lineNumber = 11,
        packageName = "Delete"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Update",
        newImport = "import androidx.room3.Update",
        filePath = "app/src/main/java/com/example/app/data/local/UserDao.kt",
        lineNumber = 12,
        packageName = "Update"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.TypeConverter",
        newImport = "import androidx.room3.TypeConverter",
        filePath = "app/src/main/java/com/example/app/data/local/Converters.kt",
        lineNumber = 2,
        packageName = "TypeConverter"
    ),
    ImportScanResult(
        originalImport = "import androidx.room.Fts3",
        newImport = "import androidx.room3.Fts3",
        filePath = "app/src/main/java/com/example/app/data/local/UserFts.kt",
        lineNumber = 4,
        packageName = "Fts3"
    )
)

/**
 * ============================================================
 * SIMULATED_SQLITE_DRIVER_RESULTS — 模拟 SQLiteDriver 扫描结果
 * ============================================================
 */
val SIMULATED_SQLITE_DRIVER_RESULTS = listOf(
    SQLiteDriverResult(
        filePath = "data/src/main/java/com/example/data/local/DatabaseHelper.kt",
        lineNumber = 23,
        apiUsage = "SupportSQLiteDatabase",
        suggestedFix = "迁移到 SQLiteDriver，使用 SupportSQLiteQuery 替代原生 SQL",
        riskLevel = RiskLevel.P0_CRITICAL,
        beforeCode = """// ❌ Room 2.x SupportSQLiteDatabase
val db: SupportSQLiteDatabase = helper.writableDatabase
db.execSQL("DELETE FROM users WHERE id = ?", id)

val cursor = db.query(
    "SELECT * FROM users",
    selectionArgs
)""",
        afterCode = """// ✅ Room 3.0 SQLiteDriver + SupportSQLiteQuery
import androidx.room3.driver.SqlUtils
import androidx.sqlite.db.SupportSQLiteQuery

// 使用 SimpleSQLiteQuery 替代原生 cursor
val query = SimpleSQLiteQuery(
    "DELETE FROM users WHERE id = ?",
    arrayOf(id)
)
// 在 @Query DAO 中执行
// @RawQuery 适配复杂查询"""
    ),
    SQLiteDriverResult(
        filePath = "data/src/main/java/com/example/data/local/OpenHelper.kt",
        lineNumber = 15,
        apiUsage = "SupportSQLiteOpenHelper",
        suggestedFix = "SupportSQLiteOpenHelper 已在 Room 3.0 中移除，使用 Room.databaseBuilder",
        riskLevel = RiskLevel.P0_CRITICAL,
        beforeCode = """// ❌ Room 2.x SupportSQLiteOpenHelper
val helper = object : SupportSQLiteOpenHelper.Callback {
    override fun onCreate(db: SupportSQLiteDatabase) { ... }
    override fun onUpgrade(db: SupportSQLiteDatabase, ...) { ... }
}
val config = SupportSQLiteOpenHelper.Configuration(conf)
    .callback(helper)
    .build()
val helper = SupportSQLiteOpenHelper(context, config)""",
        afterCode = """// ✅ Room 3.0 AndroidDriver
import androidx.room3.driver.AndroidDriver

val driver = AndroidDriver(
    context = context,
    name = "myapp.db"
)

val db = database {
    driver(AndroidDriver::class)
    // ✅ Room 自动处理 onCreate/onUpgrade
}"""
    ),
    SQLiteDriverResult(
        filePath = "app/src/main/java/com/example/app/util/DBUtil.kt",
        lineNumber = 8,
        apiUsage = "androidx.sqlite:sqlite (legacy wrapper)",
        suggestedFix = "移除 legacy sqlite dependency，迁移到 androidx.sqlite:sqlite3 或直接使用 Room 3.0 driver",
        riskLevel = RiskLevel.P1_HIGH,
        beforeCode = """// ❌ Room 2.x legacy sqlite wrapper
dependencies {
    implementation("androidx.sqlite:sqlite:2.4.0")
    // ❌ Room 3.0 已移除 legacy wrapper
}""",
        afterCode = """// ✅ Room 3.0 — 使用 Room 驱动而非直接 sqlite
// Room 3.0 内部使用 androidx.sqlite.driver.* driver
// 不再需要手动依赖 sqlite wrapper
dependencies {
    // 只需依赖 Room 3.0 KMP driver
    implementation("androidx.room3:room3-sqlite-android:3.0.0-alpha01")
}"""
    )
)

/**
 * ============================================================
 * SIMULATED_SUSPEND_GUIDE_ITEMS — Suspend 函数改写模式
 * ============================================================
 */
val SIMULATED_SUSPEND_GUIDE_ITEMS = listOf(
    SuspendGuideItem(
        id = "sus-001",
        patternName = "同步查询 → suspend 查询",
        description = "将同步 DAO 查询函数改为 suspend 函数",
        beforeCode = """// ❌ Room 2.x 同步写法
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUser(id: Int): User

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>
}""",
        afterCode = """// ✅ Room 3.0 suspend 写法
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUser(id: Int): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}""",
        applicableScenario = "单结果查询、多结果非 Flow 列表查询",
        notes = "List<T> 改为 Flow<List<T>> 以获得响应式更新；单结果改为 suspend nullable",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    SuspendGuideItem(
        id = "sus-002",
        patternName = "同步 Insert/Update/Delete → suspend",
        description = "将写操作从同步改为 suspend，Room 自动处理事务",
        beforeCode = """// ❌ Room 2.x 同步写操作
@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)
}""",
        afterCode = """// ✅ Room 3.0 suspend 写操作
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}""",
        applicableScenario = "所有 Insert/Update/Delete 操作",
        notes = "Room 3.0 所有写操作必须为 suspend，Room 自动在事务中执行",
        riskLevel = RiskLevel.P0_CRITICAL
    ),
    SuspendGuideItem(
        id = "sus-003",
        patternName = "Callback 模式 → suspend 模式",
        description = "将旧版 Callback 风格的 DAO 迁移到 suspend 函数",
        beforeCode = """// ❌ Room 2.x Callback 风格（已废弃）
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    abstract fun getUsers(callback: QueryCallback<List<User>>)

    @Query("SELECT * FROM users")
    abstract fun getAllUsers(
        onSuccess: (List<User>) -> Unit,
        onError: (Throwable) -> Unit
    )
}""",
        afterCode = """// ✅ Room 3.0 suspend 风格
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<User>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    // 在 ViewModel/Repository 中调用:
    // val users = viewModelScope.launch {
    //     dao.getUsers()
    // }
}""",
        applicableScenario = "遗留 Callback 代码、Java 风格异步 DAO",
        notes = "Callback 模式在 Room 3.0 中完全移除，所有异步改为 Kotlin Coroutines",
        riskLevel = RiskLevel.P1_HIGH
    ),
    SuspendGuideItem(
        id = "sus-004",
        patternName = "LiveData → Flow + suspend",
        description = "将 LiveData 返回改为 Flow 或 suspend",
        beforeCode = """// ❌ Room 2.x LiveData 写法
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersLiveData(): LiveData<List<User>>
}""",
        afterCode = """// ✅ Room 3.0 Flow 写法
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    // 如需一次性数据，使用 suspend
    @Query("SELECT * FROM users")
    suspend fun getAllUsersOnce(): List<User>
}""",
        applicableScenario = "已有 LiveData 依赖的 DAO",
        notes = "Flow 是 Room 3.0 推荐方案，如有 LiveData 依赖建议迁移到 Flow",
        riskLevel = RiskLevel.P2_MEDIUM
    ),
    SuspendGuideItem(
        id = "sus-005",
        patternName = "@RawQuery 迁移模板",
        description = "复杂查询场景使用 @RawQuery + SupportSQLiteQuery",
        beforeCode = """// ❌ 可能的旧版复杂查询写法
@Query("SELECT u.*, p.* FROM users u LEFT JOIN posts p ON u.id = p.userId WHERE u.active = 1")
abstract fun getUsersWithPosts(): List<UserWithPost>""",
        afterCode = """// ✅ Room 3.0 @RawQuery 模板
@RawQuery(observedEntities = [User::class, Post::class])
abstract fun getUsersWithPosts(query: SupportSQLiteQuery): Flow<List<UserWithPost>>

// 在 Repository 层构造 query:
fun UserDao.getUsersWithPosts(activeOnly: Boolean): Flow<List<UserWithPost>> {
    val sql = "SELECT u.*, p.* FROM users u LEFT JOIN posts p " +
              "ON u.id = p.userId WHERE u.active = ?"
    val args = if (activeOnly) arrayOf(1) else arrayOf()
    return getUsersWithPosts(SimpleSQLiteQuery(sql, args))
}""",
        applicableScenario = "动态 WHERE 子句、多表 JOIN、需要程序化构造的查询",
        notes = "@RawQuery 是 Room 3.0 处理复杂动态查询的主要方案，observedEntities 确保 Flow 正确订阅",
        riskLevel = RiskLevel.P1_HIGH
    )
)

/**
 * ============================================================
 * SIMULATED_DUAL_VERSION_STRATEGIES — 双版本兼容策略
 * ============================================================
 */
val SIMULATED_DUAL_VERSION_STRATEGIES = listOf(
    DualVersionStrategy(
        id = "dvs-001",
        name = "Flavor 分离方案",
        description = "通过 Gradle productFlavor 分别构建 Room 2.x 和 Room 3.0 版本",
        approach = "ProductFlavor",
        beforeCode = """// ❌ 混在一起的写法
dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
}
// 所有代码使用 androidx.room.*""",
        afterCode = """// build.gradle.kts
android {
    flavorDimensions += "roomVersion"
    productFlavors {
        create("room2x") {
            dimension = "roomVersion"
            buildConfigField("String", "ROOM_VERSION", "\"2.6.1\"")
        }
        create("room3x") {
            dimension = "roomVersion"
            buildConfigField("String", "ROOM_VERSION", "\"3.0.0-alpha01\"")
        }
    }
}

dependencies {
    // room2x flavor
    "room2xImplementation"("androidx.room:room-runtime:2.6.1")
    "room2xImplementation"("androidx.room:room-compiler:2.6.1")
    // room3x flavor
    "room3xImplementation"("androidx.room3:room3-runtime:3.0.0-alpha01")
    "room3xImplementation"("androidx.room3:room3-compiler:3.0.0-alpha01")
}""",
        pros = listOf("完全隔离，无代码冲突", "可独立测试各版本", "发布流程清晰"),
        cons = listOf("维护两套 build variant", "CI 构建时间翻倍", "代码无法共享"),
        recommendedFor = "大型团队，Room 版本过渡期需要同时维护新旧版本 App"
    ),
    DualVersionStrategy(
        id = "dvs-002",
        name = "SourceSet 条件编译",
        description = "通过 Kotlin source set 条件编译，在同一 module 中兼容 2.x 和 3.x",
        approach = "SourceSet + @RequiresOptIn",
        beforeCode = """// ❌ 无条件使用 Room 2.x
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getUsers(): List<User>
}""",
        afterCode = """// src/commonMain/kotlin/ (共享代码)
// 使用 opt-in 标记 Room 3.0 特性
@OptIn(ExperimentalSqlApi::class)
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getUsers(): Flow<List<User>>
}

// src/jvmMain/kotlin/ (JVM/Desktop 专用)
@OptIn(ExperimentalSqlApi::class)
val driver = SQLiteDriver()

// src/androidMain/kotlin/ (Android 专用)
val driver = AndroidDriver(context, "myapp.db")""",
        pros = listOf("代码高度共享", "KMP 项目天然支持", "无需重复构建"),
        cons = listOf("需要 Kotlin 1.9+ / KMP 配置", "部分平台特有代码无法完全共享", "Opt-In annotation 复杂度"),
        recommendedFor = "Kotlin Multiplatform 项目，Android + iOS + JS + Desktop"
    ),
    DualVersionStrategy(
        id = "dvs-003",
        name = "接口抽象 + 运行时分发",
        description = "将 DAO 接口抽象为接口，通过运行时判断加载对应实现",
        approach = "Interface + Factory",
        beforeCode = """// ❌ 直接依赖 Room 2.x
class UserRepository {
    private val dao = db.userDao()
    fun getUsers() = dao.getUsers()
}""",
        afterCode = """// ✅ 接口抽象
interface UserDaoInterface {
    suspend fun getUsers(): List<User>
}

// Room 2.x 实现
class UserDaoV2(appDatabase: AppDatabase) : UserDaoInterface {
    private val dao = appDatabase.userDao()
    override suspend fun getUsers(): List<User> = dao.getUsers()
}

// Room 3.0 实现
class UserDaoV3(database: Database): UserDaoInterface {
    private val dao = database.userDao()
    override suspend fun getUsers(): List<User> = dao.getUsers()
}

// Factory 动态选择
class UserRepository {
    fun create(version: Int): UserDaoInterface {
        return when {
            version >= 3 -> UserDaoV3(database)
            else -> UserDaoV2(appDatabase)
        }
    }
}""",
        pros = listOf("运行时灵活切换", "新旧代码隔离", "易于回滚"),
        cons = listOf("接口抽象增加维护成本", "Factory 复杂度", "需要处理两套 schema"),
        recommendedFor = "渐进迁移，先跑通新流程再逐步废弃旧实现"
    ),
    DualVersionStrategy(
        id = "dvs-004",
        name = "完全重写（Big Bang）",
        description = "一次性将所有 Room 2.x 代码迁移到 Room 3.0，不保留双版本兼容",
        approach = "Big Bang Rewrite",
        beforeCode = """// 全部 Room 2.x 旧代码
import androidx.room.*
val db = Room.databaseBuilder(...)
    .build()
// 所有 import androidx.room.*""",
        afterCode = """// 全部 Room 3.0 新代码
import androidx.room3.*
val db = database {
    driver(AndroidDriver::class)
}
// 所有 import androidx.room3.*""",
        pros = listOf("无技术债积累", "代码库干净", "无需维护兼容层"),
        cons = listOf("迁移窗口长（数周）", "风险集中爆发", "需要完整回归测试"),
        recommendedFor = "Room 依赖不深的小型项目，或有充足迁移窗口的大型项目"
    )
)

/**
 * ============================================================
 * SIMULATED_CI_CHECKLIST — CI 合规检查清单
 * ============================================================
 */
val SIMULATED_CI_CHECKLIST = listOf(
    CIChecklistItem(
        id = "ci-001",
        title = "androidx.room3 import 检测",
        description = "检测项目中是否还有残留的 androidx.room (2.x) import 语句",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "Import"
    ),
    CIChecklistItem(
        id = "ci-002",
        title = "KSP 替代 KAPT 验证",
        description = "确认 build.gradle 中已移除 kapt plugin 和 room-compiler kapt 依赖",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "KSP"
    ),
    CIChecklistItem(
        id = "ci-003",
        title = "所有 DAO suspend 函数检查",
        description = "确认所有 DAO 函数都是 suspend 或返回 Flow，不存在同步 DAO 函数",
        riskLevel = RiskLevel.P0_CRITICAL,
        category = "Suspend"
    ),
    CIChecklistItem(
        id = "ci-004",
        title = "SupportSQLiteDatabase 清除",
        description = "确认代码中已无 SupportSQLiteDatabase、SupportSQLiteOpenHelper 使用",
        riskLevel = RiskLevel.P1_HIGH,
        category = "SQLiteDriver"
    ),
    CIChecklistItem(
        id = "ci-005",
        title = "Driver API 迁移验证",
        description = "确认数据库初始化已迁移到 AndroidDriver/SQLiteDriver 等新 Driver API",
        riskLevel = RiskLevel.P1_HIGH,
        category = "Driver"
    ),
    CIChecklistItem(
        id = "ci-006",
        title = "Room 3.0 依赖版本检查",
        description = "确认 room3-* 依赖版本一致且为推荐版本（3.0.0-alpha01 或稳定版）",
        riskLevel = RiskLevel.P1_HIGH,
        category = "KSP"
    ),
    CIChecklistItem(
        id = "ci-007",
        title = "KMP 多平台 Driver 配置",
        description = "KMP 项目需验证各平台 Driver 已正确配置（Android/iOS/JS/Desktop）",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "KMP"
    ),
    CIChecklistItem(
        id = "ci-008",
        title = "CI Gradle Plugin 合规",
        description = "确认 CI 已集成 room3-ci-check Gradle 插件，阻塞不合规构建",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "CI"
    ),
    CIChecklistItem(
        id = "ci-009",
        title = "@RawQuery 迁移验证",
        description = "复杂查询已迁移到 @RawQuery + SupportSQLiteQuery 模式",
        riskLevel = RiskLevel.P2_MEDIUM,
        category = "Suspend"
    ),
    CIChecklistItem(
        id = "ci-010",
        title = "WASM/JS 平台限制说明",
        description = "WASM/JS 平台不支持的功能（事务、Flow 等）已有文档说明",
        riskLevel = RiskLevel.P3_LOW,
        category = "KMP"
    )
)
