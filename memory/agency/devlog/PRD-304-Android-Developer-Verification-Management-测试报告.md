# PRD-304 测试报告

## 测试结论
❌ **不通过 — 需要修复后重测**

## 编译检查
- **结果**: ✅ 通过
- **编译命令**: `./gradlew :app:compileDebugKotlin --no-daemon -Dorg.gradle.jvmargs="-Xmx4096m"`
- **错误日志**: 无（仅有 deprecation warnings，属于已知技术债务，不阻塞合入）
- **备注**: 首次编译因内存不足（OOM）失败，增加 JVM heap 至 4096m 后通过

---

## 功能验证

| 功能点 | 设计要求 | 测试结果 | 备注 |
|--------|---------|---------|------|
| 主仪表板概览卡片 | 显示总开发者数、总App数、已验证/待验证/已过期统计 | ✅ 通过 | StatsCardsRow 正确展示 |
| 倒计时横幅 | 显示距离9月30日的倒计时 | ✅ 通过 | CountdownBanner 正确计算并展示 |
| 预警列表 | 显示即将过期/已过期的开发者预警 | ✅ 通过 | WarningCard 正确展示，Severity 分级正确 |
| 开发者列表 | LazyColumn 展示开发者卡片（头像/名称/邮箱/状态/关联App数） | ✅ 通过 | DeveloperCard 完整展示 |
| 开发者搜索 | 按名称/邮箱搜索 | ✅ 通过 | SearchFilterBar 搜索功能正常 |
| 开发者状态筛选 | FilterChip 按 Verified/Pending/Expired/Failed 筛选 | ✅ 通过 | FilterChip 状态筛选正常 |
| 开发者选择 | Checkbox 多选开发者 | ✅ 通过 | selectedDevelopers Set 更新正常 |
| 全选/清除选择 | 快捷全选/清除选择按钮 | ✅ 通过 | SelectAllFiltered / ClearSelection 正常 |
| 验证申请向导 Step 1 | 选择开发者 & App 确认页 | ⚠️ 部分通过 | 开发者预填正确；App 选择数为0（App 选择 UI 未实现） |
| 验证申请向导 Step 2 | 确认身份信息页 | ✅ 通过 | 展示开发者名称/邮箱/状态 |
| 验证申请向导 Step 3 | 选择验证类型（首次验证/续期） | ❌ 失败 | **Bug #1**: RadioButton selected=true 硬编码，两个选项均显示为选中 |
| 验证申请向导 Step 4 | 提交 & 生成 CI/CD 配置 | ⚠️ 部分通过 | 提交逻辑正常，但验证类型始终为 FirstTime（UI 无法切换） |
| App 列表 | LazyColumn 展示 App 卡片（名称/包名/状态/有效期） | ✅ 通过 | AppCard 正确展示 |
| CI/CD 配置片段生成 | 支持 GitHub Actions / GitLab CI / Jenkins | ✅ 通过 | 三种平台配置片段均正确生成 |
| CI/CD 配置复制 | 复制配置片段到剪贴板 | ✅ 通过 | Clipboard 复制功能正常 |
| 合规报告导出 CSV | 导出 CSV 格式报告 | ✅ 通过 | 导出按钮和 Loading 状态正常 |
| 合规报告导出 PDF | 导出 PDF 格式报告 | ✅ 通过 | 导出按钮和 Loading 状态正常 |
| Tab 切换 | Dashboard / Developers / Apps / CI-CD / Reports 五个 Tab | ⚠️ 警告 | **Bug #2**: 恰好 5 个 Tab，达到上限 |
| Pull-to-Refresh | 下拉刷新仪表板数据 | ✅ 通过 | PullToRefreshBox 集成正常 |
| MVI State/Intent/Effect | 完整的 MVI 架构实现 | ✅ 通过 | State/Intent/Effect 契约完整 |
| 中英双语注释 | 代码注释中英双语 | ✅ 通过 | 所有关键逻辑均有 CN+EN 注释 |

---

## Bug 列表

### Bug #1
- **描述**: 验证申请向导 Step 3（选择验证类型）两个 RadioButton 均显示为选中状态，用户无法实际切换"首次验证"和"续期"选项
- **复现步骤**:
  1. 在「开发者」Tab 选择任意开发者
  2. 点击「批量提交」按钮，打开验证申请向导
  3. 点击「下一步」直到 Step 3（选择验证类型）
  4. 观察 RadioButton 状态
- **预期行为**: 每次只选择一个验证类型；点击另一个时切换选择
- **实际行为**: 两个 RadioButton 同时显示为选中状态（`selected = true` 硬编码）
- **根本原因**: `VerificationDashboardScreen.kt` 第 1113 行 `RadioButton(selected = true, onClick = { })` 硬编码为 true；且 ViewModel 状态中无 `selectedVerificationType` 字段，Intent 的 `verificationType` 参数在 `BatchSubmit` 时硬编码为 `VerificationType.FirstTime`
- **严重程度**: **P1**（核心功能不可用）

### Bug #2
- **描述**: Dashboard 恰好有 5 个 Tab，达到 QA 规范上限（Tab 超过 5 个必须打回，4 个以内为合理），属于边界情况
- **复现步骤**:
  1. 打开 Dev 验证管理界面
  2. 观察 TabRow 中的 Tab 数量
- **预期行为**: Tab 数量 ≤ 4
- **实际行为**: 5 个 Tab（Dashboard / Developers / Apps / CI-CD / Reports）
- **严重程度**: **P2**（边界情况，建议重构，可商讨放行）

### Bug #3
- **描述**: 验证申请向导 Step 1 预填的 App 数量始终为 0，"selectedApps" 在 App 列表页无法被用户选中
- **复现步骤**:
  1. 在「开发者」Tab 选择任意开发者
  2. 点击「批量提交」打开向导
  3. Step 1 显示 "0 个 App"
  4. 进入「App」Tab，尝试选择 App
- **预期行为**: 用户可以在 App 列表中勾选 App，选中结果带入向导 Step 1
- **实际行为**: AppCard 无 Checkbox，`selectedApps` Set 无法被用户操作，始终为空
- **严重程度**: **P2**（向导 Step 4 的 `appIds` 参数始终为空列表）

### Bug #4
- **描述**: 合规报告导出时日期范围选择器缺失，报告时间范围硬编码为"最近 30 天"
- **复现步骤**:
  1. 进入「报告」Tab
  2. 点击「导出 CSV」或「导出 PDF」
  3. 观察导出内容
- **预期行为**: 设计文档（Section 3.6）要求有时间范围选择器，允许用户自定义起止日期
- **实际行为**: 无日期选择器 UI，直接使用硬编码的 30 天范围
- **严重程度**: **P2**（设计文档明确要求的功能缺失）

### Bug #5
- **描述**: SSO/OIDC 联动配置页（设计文档 Section 3.7）完全缺失
- **复现步骤**:
  1. 查看 DashboardTab 枚举
  2. 尝试找到 SSO 配置入口
- **预期行为**: 设计文档要求有 SSO/OIDC 配置页（Google Workspace / Okta / Azure AD）
- **实际行为**: DashboardTab 只有 Dashboard/Developers/Apps/CICD/Reports，SSO 配置页不存在
- **严重程度**: **P1**（设计文档明确要求的功能整页缺失）

---

## UI 可用性审查

| 检查项 | 结果 | 备注 |
|--------|------|------|
| Tab 数量检查 | ⚠️ 警告 | 恰好 5 个 Tab（上限为 5），建议评估是否可合并 |
| 布局合理性 | ✅ 通过 | 内容未截断，间距正常 |
| 交互流畅度 | ✅ 通过 | 滚动、切换、弹窗无卡顿 |
| 低能错误 | ⚠️ 警告 | Wizard Step 3 RadioButton 视觉异常（两个同时选中） |
| 空状态界面 | ✅ 通过 | 列表为空时显示合理 |
| 错误提示 | ✅ 通过 | Snackbar 错误提示正常 |

---

## 技术债（非阻塞）

以下 Deprecation Warnings 在编译时出现，不阻塞本次合入，但建议后续修复：
- `TabRow` → 应替换为 `PrimaryTabRow` / `SecondaryTabRow`（出现多次）
- `LocalClipboardManager` → 应替换为 `LocalClipboard`（支持 suspend）
- `Icons.Filled.ArrowBack` → 应使用 `Icons.AutoMirrored.Filled.ArrowBack`
- `Icons.Filled.FactCheck` → 应使用 `Icons.AutoMirrored.Filled.FactCheck`
- `Icons.Filled.Send` → 应使用 `Icons.AutoMirrored.Filled.Send`
- `Icons.Filled.CompareArrows` → 应使用 `Icons.AutoMirrored.Filled.CompareArrows`
- `Icons.Filled.MergeType` → 应使用 `Icons.AutoMirrored.Filled.MergeType`
- `Icons.Filled.OpenInNew` → 应使用 `Icons.AutoMirrored.Filled.OpenInNew`
- `Icons.Filled.List` → 应使用 `Icons.AutoMirrored.Filled.List`
- `Icons.Rounded.ArrowBack` → 应使用 `Icons.AutoMirrored.Rounded.ArrowBack`
- `Icons.Rounded.ArrowForward` → 应使用 `Icons.AutoMirrored.Rounded.ArrowForward`
- `Icons.Rounded.ViewList` → 应使用 `Icons.AutoMirrored.Rounded.ViewList`
- `Icons.Filled.KeyboardArrowRight` → 应使用 `Icons.AutoMirrored.Filled.KeyboardArrowRight`
- `LocalLifecycleOwner` → 应使用 `androidx.lifecycle.compose.LocalLifecycleOwner`

---

## 最终建议

- [ ] **需要修复后重测** — Bug #1（P1）和 Bug #5（P1）为阻塞性问题
- [ ] Bug #2（P2 边界情况）、Bug #3（P2）、Bug #4（P2）建议同步修复
- [ ] 技术债（deprecation warnings）不阻塞本次合入，建议在后续迭代中清理
- [ ] 合入前需要重新编译验证（建议增加 Gradle JVM heap 到 4096m 或更高）

---

## 测试环境

- **机器**: Kenny的MacBook Pro
- **OS**: Darwin 25.4.0 (arm64)
- **Node**: v24.14.0
- **Kotlin**: 编译通过（JVM heap 4096m）
- **分支**: `feature/prd-304-Android-Developer-Verification-Management`
- **测试日期**: 2026-07-01
