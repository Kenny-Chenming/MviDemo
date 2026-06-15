# PRD-257 测试报告（三测）

## 测试结论
❌ **不通过** — P1 Bug 仍未修复，打回开发

---

## 编译检查
- **结果**: ✅ 通过
- **编译时间**: 8s
- **备注**: UP-TO-DATE（无新编译，缓存有效）

---

## 功能验证

| 功能点 | 设计要求 | 测试结果 | 备注 |
|--------|---------|---------|------|
| Tab 数量 | 5个 以内 | ⚠️ 5个（软上限4个，设计合理例外） | 概述/验证API/注册配置/合规指南/扩展能力 |
| 底部 NavigationBar | BottomNavigation + NavigationBarItem | ✅ 通过 | 5个 NavigationBarItem，激活色 PrimaryLight |
| 5个 Tab 图标 | Info/Security/Settings/FactCheck/Layers | ✅ 通过 | 全部实现（Icons.Default） |
| MVI State | VerifiedFinancialCallsState | ✅ 通过 | selectedTab/complianceCheckedItems/isLoading/errorMessage/copiedBlockId/expandedCards |
| MVI Intent | SelectTab/ToggleComplianceItem/CopyCodeBlock/ToggleCard/DismissError | ✅ 通过 | 所有 Intent 已实现 |
| MVI Effect | ShowSnackbar/CopyToClipboard | ✅ 通过 | Effect 通道已配置（Channel.BUFFERED） |
| Quick Navigation FAB | 快速导航 FAB + ModalBottomSheet | ✅ 通过 | FAB 弹出 Bottomsheet，列出所有功能入口 |
| 代码块复制 | 代码块复制 + Snackbar 提示 | ✅ 通过 | ClipboardManager + ClipData 实现 |
| 深色/浅色主题 | dynamicColor = true | ✅ 通过 | 跟随系统主题 |
| 代码块背景固定深色 | 不受主题影响 | ✅ 通过 | 代码块背景固定 #1E1E1E |
| 合规清单勾选 | Checkbox + 点击即保存 | ✅ 通过（交互层） | |
| **合规清单持久化** | **complianceCheckedItems 保存到 DataStore** | ❌ **P1 Bug** | **见下方 Bug 详情** |
| 合规清单 Banner | 未完成项显示红色 Banner | ✅ 通过 | uncheckedCount > 0 时显示警告 Banner |
| 可展开卡片 | ExpandableCard + AnimatedVisibility | ✅ 通过 | 展开/收起动画正常 |
| Context 陷阱警告 | 提示必须在 Activity Context 下调用 | ✅ 通过 | Tab 1 显示红色警告 Card |

---

## Bug 列表

### Bug #1（P1 — 阻塞性）
- **描述**: 合规清单勾选状态未持久化，进程死亡后状态丢失
- **复现步骤**:
  1. 打开"合规指南" Tab（Tab 3）
  2. 勾选若干合规清单项（如 Item 1: GDPR Article 9）
  3. 确认勾选状态已更新（红色 Banner 数量应减少）
  4. 杀死 App 进程（`adb shell am kill com.mvi.kenny` 或系统强杀）
  5. 重新打开 App
  6. 进入"合规指南" Tab
- **预期行为**: 勾选状态从 DataStore 恢复，之前勾选的项目保持勾选
- **实际行为**: 所有勾选状态丢失，合规清单恢复为全部未勾选，红色 Banner 显示全部 12 项未确认
- **根因**: `VerifiedFinancialCallsViewModel.handleToggleComplianceItem()` 仅更新 `_state.update { it.copy(complianceCheckedItems = ...) }`，无任何 DataStore 持久化调用。设计文档 4.3 明确要求"点击即保存状态到 DataStore，反映开发者合规进度。"
- **代码证据**:
  - `VerifiedFinancialCallsViewModel.kt` 中无任何 `androidx.datastore` import
  - ViewModel 中无 `DataStore<Preferences>` 实例
  - `complianceCheckedItems` 完全依赖 `MutableStateFlow`，进程死亡后完全丢失
  - 设计文档注释声称 "via remember to persist"，但 `remember` 仅为 Compose 重组缓存，不具备进程间持久化能力
- **修复建议**:
  ```kotlin
  // 1. 添加 DataStore 依赖（build.gradle.kts）
  implementation = "androidx.datastore:datastore-preferences:1.1.1"

  // 2. ViewModel 中注入 DataStore
  private val dataStore: DataStore<Preferences>

  // 3. complianceCheckedItems 改为从 DataStore 读取
  val complianceCheckedItems: StateFlow<Set<Int>> = dataStore.data
      .map { prefs ->
          prefs[stringSetPreferencesKey("compliance_checked")]?.map { it.toInt() }?.toSet()
              ?: emptySet()
      }
      .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

  // 4. handleToggleComplianceItem 改为 DataStore 写入
  private fun handleToggleComplianceItem(itemId: Int) {
      viewModelScope.launch {
          dataStore.edit { prefs ->
              val current = prefs[stringSetPreferencesKey("compliance_checked")]
                  ?.map { it.toInt() }?.toMutableSet()
                  ?: mutableSetOf()
              if (itemId in current) current.remove(itemId) else current.add(itemId)
              prefs[stringSetPreferencesKey("compliance_checked")] = current.map { it.toString() }.toSet()
          }
      }
  }
  ```
- **严重程度**: **P1**（合规清单数据不持久化，违反设计文档明确要求，无法满足 GDPR/CCPA 合规场景开发者的真实需求）

---

## P1 Bug 回顾

| 字段 | 要求 | 实际 | 状态 |
|------|------|------|------|
| `complianceCheckedItems` | 持久化到 DataStore | 仅 in-memory `MutableStateFlow`，进程死亡后丢失 | ❌ 仍未修复 |

**三测结果**: 与二测完全相同，P1 Bug 未修复。
**设计文档明确要求**（4.3 Checklist 交互）:
> 点击即保存状态到 DataStore，反映开发者合规进度

---

## 最终建议
- [ ] 允许合入 agency-product-sprint
- [x] **需要修复后重测**（P1 Bug 必须修复）

---

**测试时间**: 2026-06-12 23:15 (GMT+8)
**测试分支**: feature/prd-257-verified-financial-calls
**Bug 状态**: P1 Bug 仍未修复（三测打回）
**测试人**: 开心果 🥜 测试部守门 Agent
