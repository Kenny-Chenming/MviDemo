# PRD-293 Backlog 状态更新

**更新日期:** 2026-06-21 08:20 GMT+8  
**PR:** feature/prd-293-antigravity2-android  
**Commit:** 40956b6

## 状态: ❌ 开发中

---

## Bug 列表

### Bug #1: DataStore 持久化缺失 (Critical)
- **模块:** EnterpriseTab / EnterpriseChecklist
- **问题:** 企业部署 Checklist 状态未持久化，应用重启后丢失
- **修复建议:** 使用 PreferencesDataStore 存储 checklist 状态

### Bug #2: Skills 搜索无 Debounce (Minor)
- **模块:** SkillsMarketTab
- **问题:** 搜索框每次 keystroke 直接触发过滤，无 300ms debounce
- **修复建议:** 在 ViewModel 中使用 `debounce` operator

---

## 修复后重新测试清单

- [ ] DataStore 持久化实现并验证
- [ ] Skills 搜索 300ms debounce 实现并验证
- [ ] 重新编译通过
- [ ] 功能回归测试通过

---

*QA Engineer (Subagent) - 2026-06-21*
