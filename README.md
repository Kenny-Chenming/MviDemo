# MyMviProject

基于 Jetpack Compose + MVI 架构的 Android 示例项目。

## 技术栈

| 分类 | 技术 |
|------|------|
| **语言** | Kotlin 2.0 |
| **UI 框架** | Jetpack Compose (BOM 2026.03.01) |
| **架构模式** | MVI (Model-View-Intent) |
| **导航** | HorizontalPager + BottomNavigationBar（Tab 间滑动切换） |
| **状态管理** | StateFlow + Channel |
| **DI** | 手动 ViewModel 获取（viewModel() 扩展函数） |
| **目标 SDK** | 36 / minSdk 24 |

## 项目结构

```
app/src/main/java/com/mvi/kenny/
├── MainActivity.kt              # 唯一 Activity 入口
├── base/
│   ├── BaseActivity.kt          # 所有 Activity 基类（语言切换 + Compose 环境）
│   ├── ThemeContext.kt          # 主题上下文（主题/语言状态 + 回调）
│   ├── TopBarConfig.kt         # 顶部导航栏配置（共享 TopAppBar）
│   ├── Effect.kt               # 全局 Effect（预留）
│   └── LoadingOverlay.kt       # 全局 Loading 遮罩
├── util/
│   ├── SettingsManager.kt       # SharedPreferences 封装（主题/语言）
│   └── LocaleHelper.kt         # 语言切换工具（RTL 支持）
├── navigation/
│   └── NavRoutes.kt            # 路由常量 + BottomNav 配置
├── ui/
│   ├── screen/
│   │   └── MainScreen.kt       # 主界面（TopAppBar + Pager + BottomNav）
│   └── theme/
│       ├── Theme.kt            # Compose Theme 定义
│       ├── Color.kt            # 颜色定义
│       ├── Type.kt             # Typography 字体规范
│       └── AppContent.kt       # rememberThemeContext() 获取函数
└── feature/
    ├── home/                   # 首页
    │   ├── HomeContract.kt     # MVI 契约（State / Intent / Effect）
    │   ├── HomeViewModel.kt    # 状态管理
    │   ├── HomeScreen.kt       # 页面 Composable
    │   └── SettingsDialog.kt    # 主题/语言设置对话框
    ├── list/                   # 列表页
    │   ├── ListContract.kt
    │   ├── ListViewModel.kt
    │   ├── ListScreen.kt
    │   └── ListActivity.kt     # 可被独立 Intent 唤起
    ├── login/                   # 登录页
    │   ├── LoginContract.kt
    │   ├── LoginViewModel.kt
    │   ├── LoginScreen.kt
    │   └── LoginActivity.kt    # 可被独立 Intent 唤起
    └── profile/                 # 个人中心
        ├── ProfileContract.kt
        ├── ProfileViewModel.kt
        └── ProfileScreen.kt
```

## 架构说明

### 整体架构

```
MainActivity（唯一 Activity）
  └─ BaseActivity.screen()
       └─ MainScreen
            ├─ TopAppBar（共享，根据 Tab 动态切换标题 + action）
            ├─ HorizontalPager（左右滑动切换三个 Tab）
            │    ├─ HomeScreen  → HomeViewModel
            │    ├─ ListScreen  → ListViewModel
            │    └─ ProfileScreen → ProfileViewModel
            └─ BottomNavigationBar（点击切换 Tab）
```

### MVI 架构

每个功能页面遵循 MVI 模式：

```
User Action → Intent → ViewModel.process() → 更新 State → Composable 重组
                  ↓
             发送 Effect → UI 处理（Toast / 导航）
```

**三要素：**
- **State**：`data class`，页面状态的唯一真相来源
- **Intent**：用户操作密封接口
- **Effect**：Channel 热流，传递一次性副作用（Toast、导航）

### BaseActivity 职责

1. **语言切换**：`attachBaseContext()` 中注入 Locale，`recreate()` 后生效
2. **主题分发**：通过 `CompositionLocal` 分发 `ThemeContext`，子组件无需参数传递即可访问
3. **Compose 环境**：统一调用 `setContent {}`，子类只需实现 `screen()`

### TopBar 动态更新

MainScreen 持有唯一的 TopAppBar，每个 Tab 页面通过 `onUpdateTopBar` 回调上报自己的配置（标题 + 操作按钮），实现共享 TopAppBar 的效果。

## Compose + MVI = 零 Fragment

**这是本项目的核心理念之一。**

### 为什么 Compose 时代不需要 Fragment

传统 Android 架构里 Fragment 解决的核心问题是：

| 传统问题 | Fragment 的解法 |
|---------|--------------|
| Activity 太重，不能拆 | 把 UI 拆成多个 Fragment |
| 屏幕需要模块化复用 | Fragment 可以动态添加/移除 |
| 导航需要回退栈 | FragmentTransaction + back stack |

**有了 Compose，这些问题都不存在了：**

- **UI 就是 Composable 函数**，函数比类轻量得多，不需要 Fragment 这种"轻量 Activity"的概念
- **导航就是状态变化**，Pager 切 Tab 不需要事务，回退栈由系统隐式管理
- **复用是函数组合**，把 Composable 抽出来直接用，参数传递比 Fragment 之间通过接口通信简洁得多

### 架构对比

```
传统（XML + Fragment）：
Activity
  └─ FragmentManager
       ├─ Fragment1（XML 布局 + onCreateView）
       ├─ Fragment2
       └─ Fragment3
每个 Fragment 持有自己的 View，需要接口回调通信，生命周期复杂

Compose + MVI：
Activity
  └─ setContent { MainScreen() }
       └─ Composable 函数（就是 UI）
            ├─ HomeScreen()  → HomeViewModel
            ├─ ListScreen()  → ListViewModel
            └─ ProfileScreen() → ProfileViewModel
状态驱动，函数组合，无生命周期地狱
```

### 什么时候还是需要 Fragment

1. **混合迁移**：老项目部分页面迁移到 Compose，宿主 Activity 用 `FragmentContainerView` + `ComposeView` 承载
2. **某些第三方 SDK 绑定 Fragment**：比如某些广告 SDK、地图 SDK 要求 Fragment 作为容器
3. **动态功能模块**：需要真正运行时卸载/加载 UI 模块（极为罕见）

### 本项目零 Fragment

本项目没有任何 Fragment，三个页面就是三个 Composable 函数 + 三个 ViewModel，状态各自管理，通过回调与父组件通信。Activity 只负责语言切换和主题分发这种全局能力。

**结论：新项目用 Compose，就忘掉 Fragment 吧。**

## 功能清单

### 已实现
- [x] 三个 Tab 页面（首页 / 列表 / 我的）
- [x] Tab 间左右滑动切换
- [x] 底部导航栏点击切换
- [x] 共享 TopAppBar（每个 Tab 有自己的标题和操作按钮）
- [x] 主题切换（Light / Dark / System）
- [x] 多语言切换（简体中文 / English / اردو）
- [x] RTL 语言布局支持
- [x] 登录页（邮箱 + 密码，前端校验）
- [x] 列表页（下拉刷新 / 删除）
- [x] 用户信息卡片 + 编辑昵称
- [x] 退出登录

### 登录测试账号
```
邮箱：a@a.com
密码：123456
```

## 编译与运行

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 安装到已连接设备
./gradlew installDebug

# 清除构建缓存
./gradlew clean
```

## 主要依赖版本

```
Kotlin:           2.0.0
Compose BOM:       2026.03.01
AGP:              9.1.0
Navigation:       2.8.5
Coroutines:       1.9.0
Lifecycle:         2.8.0
Activity:          1.13.0
Core-ktx:         1.15.0
```
