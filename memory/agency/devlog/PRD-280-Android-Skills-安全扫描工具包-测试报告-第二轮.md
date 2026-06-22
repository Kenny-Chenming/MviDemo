# PRD-280 Android Skills 安全扫描工具包 — 测试报告（第二轮）

**测试分支:** `feature/prd-280-android-skills-security-scan`
**测试时间:** 2026-06-22 08:13 GMT+8
**测试人:** 测试部守门 Agent
**编译环境:** Kotlin 2.2.10, Gradle 9.3.1, JVM 21

---

## 一、编译检查（Bug #1 验证）

### 结果: ✅ P0 Bug #1 已修复 — 编译通过

**验证命令:**
```bash
./gradlew :app:compileDebugKotlin --no-daemon -Dorg.gradle.jvmargs="-Xmx4096m"
```

**结果:** `BUILD SUCCESSFUL in 3s`

**修复方式:** 通过将 JVM 堆内存从默认 2048m 提升到 4096m 解决 OOM。
> ⚠️ **注意:** 需要在 `gradle.properties` 中持久化 `org.gradle.jvmargs=-Xmx4096m` 或更高，否则常规开发中仍可能触发 OOM。编译能通过不代表解决了根因——在 CI 环境或低内存机器上仍可能复现。

---

## 二、代码审查（Bug #2、#3、#4 验证）

### Bug #2 (P1): Scanner 使用 Mock 数据（关键词匹配）

**文件:** `SkillsSecurityToolkitViewModel.kt`，方法 `simulateSecurityScan()`

```kotlin
private fun simulateSecurityScan(): ScanResult {
    val inputText = _state.value.scannerState.inputText
    val hasMaliciousContent = inputText.contains("hidden", ignoreCase = true) ||
            inputText.contains("curl http", ignoreCase = true) ||
            inputText.contains("wget http", ignoreCase = true) ||
            inputText.contains("rm -rf", ignoreCase = true)

    return if (hasMaliciousContent || _state.value.scannerState.inputMode == InputMode.TEXT) {
        SIMULATED_SCAN_RESULT.copy(...)
    } else {
        ScanResult(...)  // 安全结果（全靠碰巧没有关键词）
    }
}
```

**问题:**
- 核心逻辑是 4 个 `contains` 关键词匹配，未实现真实 SKILL.md 解析
- `SKILL.md` 的结构（`shellCommands`、`permissions`、`description`、`tools` 等 section）完全未被解析
- `// In production, this would parse SKILL.md markdown structure (use Kotlin regex + manual state machine)` 注释说明这是 TODO

**结论:** ❌ **Bug #2 未修复** — 关键词匹配不等于真实 SKILL.md 解析

---

### Bug #3 (P1): Gradle 依赖投毒检测未实现

**文件:** `SkillsSecurityToolkitViewModel.kt`

**证据 1 — 无文件读取逻辑:**
```bash
grep -rn "build.gradle\|File\|readFile\|readText\|BufferedReader" skillssecuritytoolkit/
```
结果：无任何实际读取 `build.gradle` 的代码。

**证据 2 — `gradleDependencies` 永远来自模拟数据:**
```kotlin
// simulateSecurityScan() 中：
gradleDependencies = emptyList(),  // 非 TEXT 模式时为空
// 或来自：
SIMULATED_SCAN_RESULT.gradleDependencies  // TEXT 模式时用模拟数据
```

**证据 3 — `build.gradle:3` 只是模拟数据里的硬编码字符串:**
```kotlin
// SkillsSecurityToolkitContract.kt
codeSnippet = "implementation 'com.unknown:lib:1.0.0'"
location = "build.gradle:3"  // 硬编码，不是真实解析
```

**结论:** ❌ **Bug #3 未修复** — `build.gradle` 内容解析逻辑完全缺失

---

### Bug #4 (P2): PDF 导出提示未实现

**文件:** `SkillsSecurityToolkitViewModel.kt`，方法 `generateExportContent()`

```kotlin
ExportFormat.PDF -> "# PDF Export\nPDF export requires Android Print Framework integration"
```

**问题:**
- 直接返回占位字符串，非真实 PDF 生成
- 无 Android Print Framework 集成
- 无实际文件输出

**结论:** ❌ **Bug #4 未修复**

---

## 三、测试结论

### 综合判定: ❌ 不通过

| Bug # | 严重等级 | 状态 | 说明 |
|-------|---------|------|------|
| #1    | P0      | ✅ 已修复 | 编译成功（需注意持久化内存配置）|
| #2    | P1      | ❌ 未修复 | 仍为关键词匹配，无真实 SKILL.md 解析 |
| #3    | P1      | ❌ 未修复 | `build.gradle` 解析逻辑完全缺失 |
| #4    | P2      | ❌ 未修复 | PDF 导出仍为占位字符串 |

### QA 铁律: 不带 Bug 合入

- Bug #2 (P1) 和 Bug #3 (P1) 均为 P1 及以上未修复
- **禁止合并到 `agency-product-sprint`**

---

## 四、待修复清单

### 1. Bug #2 — 真实 SKILL.md 解析（必须实现）

需实现内容（参考 ViewModel.kt 注释）：
- 使用 Kotlin 正则 + 状态机解析 SKILL.md markdown 结构
- 识别 `shellCommands`、`permissions`、`description`、`tools` 等 section
- 基于解析结果而非关键词匹配判断恶意模式

参考结构：
```yaml
# SKILL.md 应解析的字段
shellCommands:
  - curl http://...
  - rm -rf /
permissions:
  - android.permission.ADB
description: |
  # 正常描述
  <!-- 隐藏指令 -->
tools:
  - name: ...
```

### 2. Bug #3 — Gradle 依赖投毒检测（必须实现）

需实现：
- 读取 `build.gradle` / `build.gradle.kts` 文件
- 解析 `dependencies` block 中的 Maven coordinates
- 检测未签名/无版本号/来自不信任仓库的依赖

### 3. Bug #4 — PDF 导出（建议实现）

使用 Android Print Framework 或 PDF 库（如 iText）实现真实 PDF 生成。

---

## 五、测试通过条件

要通过测试，必须：
1. ✅ Bug #1 编译通过 — **已满足**
2. ❌ Bug #2 真实 SKILL.md 解析 — **未满足**
3. ❌ Bug #3 Gradle 依赖投毒检测 — **未满足**
4. ⚠️ Bug #4 PDF 导出 — P2，建议但非强制

**下次测试前请确保 Bug #2 和 Bug #3 已修复。**
