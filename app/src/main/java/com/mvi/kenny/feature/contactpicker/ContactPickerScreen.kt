package com.mvi.kenny.feature.contactpicker

// ================================================================
// ContactPickerScreen — Google Play Contact Picker 强制迁移工具包主界面
// ================================================================
// PRD-234: Google Play Contact Picker 强制迁移工具包
// 5-Tab MVI: Scanner / API Guide / Decision Tree / Declaration / Location Button
// ================================================================

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.IconButton
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ContactPickerScreen(viewModel: ContactPickerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ContactPickerEffect.ShowSnackbar -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is ContactPickerEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Code", effect.code))
                }
                is ContactPickerEffect.NavigateToApiGuide -> {}
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(ContactPickerColors.Background)) {
        if (state.daysRemaining <= 30 && state.selectedTab == 0) {
            Surface(modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp), color = ContactPickerColors.UrgencyP0.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = ContactPickerColors.UrgencyP0)
                    Spacer(Modifier.width(8.dp))
                    Text("⚠️ 合规截止日期临近：剩余 ${state.daysRemaining} 天（2026-10-27）", color = ContactPickerColors.UrgencyP0, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        TabRow(selectedTabIndex = state.selectedTab, containerColor = ContactPickerColors.Surface, contentColor = ContactPickerColors.Primary) {
            ContactPickerTab.entries.forEachIndexed { index, tab ->
                Tab(selected = state.selectedTab == index, onClick = { viewModel.sendIntent(ContactPickerIntent.SelectTab(index)) }, text = { Text("${tab.emoji} ${tab.title}", fontSize = 11.sp, maxLines = 1) }, selectedContentColor = ContactPickerColors.Primary, unselectedContentColor = ContactPickerColors.OnSurfaceVariant)
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (state.selectedTab) {
                0 -> ScannerTab(state, { viewModel.sendIntent(ContactPickerIntent.StartScan) }, { viewModel.sendIntent(ContactPickerIntent.CopyCodeBlock(it)) })
                1 -> ApiGuideTab(state, { viewModel.sendIntent(ContactPickerIntent.SelectApiCategory(it)) }, { viewModel.sendIntent(ContactPickerIntent.CopyCodeBlock(it)) })
                2 -> DecisionTreeTab(state, { n, a -> viewModel.sendIntent(ContactPickerIntent.AnswerDecisionNode(n, a)) }, { viewModel.sendIntent(ContactPickerIntent.ResetDecisionTree) })
                3 -> DeclarationTab(state, { f, v -> viewModel.sendIntent(ContactPickerIntent.UpdateDeclarationField(f, v)) }, { viewModel.sendIntent(ContactPickerIntent.SaveDeclaration) })
                4 -> LocationButtonTab({ viewModel.sendIntent(ContactPickerIntent.CopyCodeBlock(it)) })
            }
        }
    }
}

// =============================================================
// ScannerTab — Tab 0: 合规扫描器
// =============================================================
@Composable private fun ScannerTab(state: ContactPickerState, onStartScan: () -> Unit, onCopy: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Button(onClick = onStartScan, enabled = !state.isScanning, colors = ButtonDefaults.buttonColors(containerColor = ContactPickerColors.Primary), modifier = Modifier.fillMaxWidth()) {
            if (state.isScanning) { CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("扫描中...") } else Text("🚀 开始扫描 / Start Scan")
        }
        Spacer(Modifier.height(16.dp))

        state.scanResult?.let { result ->
            val urgencyColor = when (result.urgencyLevel) { UrgencyLevel.MUST_MIGRATE -> ContactPickerColors.UrgencyP0; UrgencyLevel.CAN_EXEMPT -> ContactPickerColors.UrgencyP1; UrgencyLevel.NO_ACTION -> ContactPickerColors.UrgencyP2 }
            val urgencyText = when (result.urgencyLevel) { UrgencyLevel.MUST_MIGRATE -> "🔴 P0 必须迁移"; UrgencyLevel.CAN_EXEMPT -> "🟡 P1 可申请豁免"; UrgencyLevel.NO_ACTION -> "🟢 P2 无需处理" }
            Surface(modifier = Modifier.fillMaxWidth(), color = urgencyColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) { Column(Modifier.padding(16.dp)) { Text(urgencyText, color = urgencyColor, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text("截止日期: ${result.migrationDeadline}", color = ContactPickerColors.OnSurfaceVariant, fontSize = 13.sp) } }
            Spacer(Modifier.height(16.dp))

            if (result.manifestFindings.isNotEmpty()) {
                Text("📋 AndroidManifest.xml 权限声明", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                result.manifestFindings.forEach { f -> FindingCard(f.permission, "Line ${f.lineNumber}", f.context, null); Spacer(Modifier.height(8.dp)) }
                Spacer(Modifier.height(16.dp))
            }
            if (result.codeFindings.isNotEmpty()) {
                Text("💻 代码使用发现", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                result.codeFindings.forEach { f -> FindingCard("${f.fileName}:${f.lineNumber}", f.useCase, f.codeSnippet, { onCopy(f.codeSnippet) }); Spacer(Modifier.height(8.dp)) }
                Spacer(Modifier.height(16.dp))
            }
            if (result.migrationSteps.isNotEmpty()) {
                Text("📝 迁移步骤", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                result.migrationSteps.forEach { s -> MigrationStepCard(s, { s.codeDiff?.let { onCopy(it.after) } }); Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable private fun FindingCard(title: String, subtitle: String, content: String, onCopy: (() -> Unit)?) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(title, color = ContactPickerColors.OnSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium); Text(subtitle, color = ContactPickerColors.OnSurfaceVariant, fontSize = 11.sp) }
                onCopy?.let { IconButton(onClick = { it.invoke() }) { Text("📋", fontSize = 14.sp) } }
            }
            Spacer(Modifier.height(8.dp))
            CodeBlock(content)
        }
    }
}

@Composable private fun MigrationStepCard(step: MigrationStep, onCopy: (() -> Unit)?) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = ContactPickerColors.Primary, shape = RoundedCornerShape(4.dp)) { Text("Step ${step.stepNumber}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                Spacer(Modifier.width(8.dp))
                Text(step.title, color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(step.description, color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp)
            step.codeDiff?.let { d ->
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Code Diff:", color = ContactPickerColors.OnSurface, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    onCopy?.let { IconButton(onClick = { it.invoke() }) { Text("📋", fontSize = 14.sp) } }
                }
                Spacer(Modifier.height(4.dp))
                CodeDiffBlock(d)
            }
        }
    }
}

@Composable private fun CodeBlock(code: String) { Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)), color = ContactPickerColors.CodeBackground) { SelectionContainer { Text(code, color = ContactPickerColors.OnSurface, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(10.dp)) } } }

@Composable private fun CodeDiffBlock(diff: CodeDiff) {
    Column(Modifier.fillMaxWidth()) {
        Text("- Before:", color = ContactPickerColors.UrgencyP0, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)), color = ContactPickerColors.CodeBackground) { Text(diff.before, color = ContactPickerColors.OnSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.padding(8.dp)) }
        Spacer(Modifier.height(4.dp))
        Text("+ After:", color = ContactPickerColors.UrgencyP2, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)), color = ContactPickerColors.CodeBackground) { Text(diff.after, color = ContactPickerColors.OnSurface, fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.padding(8.dp)) }
    }
}

// =============================================================
// ApiGuideTab — Tab 1: API 指南
// =============================================================
@Composable private fun ApiGuideTab(state: ContactPickerState, onSelectCategory: (String) -> Unit, onCopy: (String) -> Unit) {
    val categories = listOf("basics", "session", "filtering", "privacy")
    val categoryLabels = mapOf("basics" to "📖 基础用法", "session" to "🔗 Session", "filtering" to "🔍 字段过滤", "privacy" to "🔒 隐私说明")
    Column(Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat -> FilterChip(selected = state.apiGuideSelectedCategory == cat, onClick = { onSelectCategory(cat) }, label = { Text(categoryLabels[cat] ?: cat, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ContactPickerColors.Primary, selectedLabelColor = Color.White)) }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) { items(1) { ApiContentCard(state.apiGuideSelectedCategory, onCopy) } }
    }
}

@Composable private fun ApiContentCard(category: String, onCopy: (String) -> Unit) {
    when (category) {
        "basics" -> BasicsApiContent(onCopy)
        "session" -> SessionApiContent(onCopy)
        "filtering" -> FilteringApiContent(onCopy)
        "privacy" -> PrivacyApiContent()
    }
}

@Composable private fun BasicsApiContent(onCopy: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Contact Picker 基础用法", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        ApiSection("单选联系人", "使用 Intent.ACTION_PICK_CONTACTS 打开系统联系人选择器。", """val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    data = ContactsContract.Contacts.CONTENT_URI
}
startActivityForResult(intent, REQUEST_PICK_CONTACT)

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
        data?.data?.let { contactUri ->
            val contactId = getContactId(contactUri)
        }
    }
}""", onCopy)
        ApiSection("多选联系人", "使用 EXTRA_ALLOW_MULTIPLE 允许选择多个联系人。", """val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    data = ContactsContract.Contacts.CONTENT_URI
    putExtra(EXTRA_ALLOW_MULTIPLE, true)
}
startActivityForResult(intent, REQUEST_PICK_CONTACTS)""", onCopy)
    }
}

@Composable private fun SessionApiContent(onCopy: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ContactPickerSession / Session Uri 用法", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        ApiSection("保留选择历史", "使用 ContactPickerSession 保留用户选择历史，避免每次都弹系统 picker。", """// First launch
val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply { data = ContactsContract.Contacts.CONTENT_URI }
startActivityForResult(intent, REQUEST_PICK_CONTACT)

// After selection: save session Uri
val sessionUri = ContactPickerSession.currentSessionUri

// Next launch: use session Uri
val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    data = ContactsContract.Contacts.CONTENT_URI
    sessionUri?.let { putExtra(EXTRA_SESSION_URI, it) }
}""", onCopy)
    }
}

@Composable private fun FilteringApiContent(onCopy: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("字段过滤 / Field Filtering", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        ApiSection("只选择有电话号码的联系人", "使用 CommonDataKinds.Phone.CONTENT_TYPE 过滤。", """val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
}
startActivityForResult(intent, REQUEST_PICK_PHONE)""", onCopy)
    }
}

@Composable private fun PrivacyApiContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("隐私保护说明", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        PrivacyCard("READ_CONTACTS vs Contact Picker", "🔐", listOf("READ_CONTACTS: 应用持有永久访问权限，可随时读取联系人列表", "Contact Picker: 用户每次单独授权，只获取单条记录，不持有列表权限", "隐私差异：Contact Picker 不需要 App 持有联系人列表访问权限"))
        PrivacyCard("Play 政策要求", "📜", listOf("2026-10-27 起：Play Console 新预审将强制检查 contact 权限", "分享、邀请联系人等场景必须使用 Contact Picker", "只有「can't function without READ_CONTACTS」的 App 才能申请豁免"))
    }
}

@Composable private fun PrivacyCard(title: String, icon: String, items: List<String>) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text("$icon $title", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            items.forEach { item -> Row(Modifier.padding(vertical = 2.dp)) { Text("• ", color = ContactPickerColors.OnSurfaceVariant); Text(item, color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp) } }
        }
    }
}

@Composable private fun ApiSection(title: String, desc: String, code: String, onCopy: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(desc, color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { onCopy(code) }, modifier = Modifier.size(32.dp)) { Text("📋", fontSize = 16.sp) } }
            CodeBlock(code)
        }
    }
}

// =============================================================
// DecisionTreeTab — Tab 2: 决策树
// =============================================================
@Composable private fun DecisionTreeTab(state: ContactPickerState, onAnswer: (Int, String) -> Unit, onReset: () -> Unit) {
    val step = state.decisionTreeStep
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("🌳 Contact Picker vs Sharesheet 决策树", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        when {
            step == 0 -> DecisionCard("你的 App 需要获取联系人信息吗？", listOf("是，需要读取联系人列表" to "yes_list", "是，只需要选择单个联系人" to "yes_one", "否，只是分享内容到联系人" to "no_share", "不确定" to "unsure"), { onAnswer(0, it) })
            step == 1 -> {
                when (state.decisionTreeAnswers[0]) {
                    "yes_list" -> DecisionCard("为什么必须读取完整联系人列表？", listOf("实现联系人搜索/过滤功能" to "search", "批量处理联系人" to "batch", "通讯录类 App" to "contacts_app"), { onAnswer(1, it) })
                    "yes_one" -> ResultCard("✅ 推荐使用 Contact Picker", "你的场景适合使用 Contact Picker，符合 Play 政策要求。", """val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    data = ContactsContract.Contacts.CONTENT_URI
}
startActivityForResult(intent, REQUEST_PICK_CONTACT)""")
                    "no_share" -> ResultCard("✅ 推荐使用 Sharesheet", "分享内容到联系人应该使用 Sharesheet。", """val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "Share content")
}
startActivity(Intent.createChooser(shareIntent, "Share"))""")
                    "unsure" -> DecisionCard("你的 App 属于哪类？", listOf("社交/通信 App" to "social", "企业/商务 App" to "enterprise", "工具类 App" to "utility"), { onAnswer(1, it) })
                    else -> {}
                }
            }
            step >= 2 -> {
                val a = state.decisionTreeAnswers
                val result = when {
                    a[0] == "yes_list" && a[1] == "search" -> "🔄 可能需要申请豁免或重构 — 联系人搜索功能需要访问完整列表，建议评估重构方案。"
                    a[0] == "yes_list" && a[1] == "batch" -> "🔄 需要申请 Play Developer Declaration 豁免 — 批量处理联系人可申请豁免。"
                    a[0] == "yes_list" && a[1] == "contacts_app" -> "✅ 可申请豁免（系统级 App）— 通讯录类 App 符合豁免条件。"
                    a[0] == "unsure" && a[1] == "social" -> "✅ 使用 Contact Picker — 社交 App 分享/邀请联系人场景应使用 Contact Picker。"
                    else -> "❓ 请进一步评估 — 建议使用扫描器进行详细分析。"
                }
                Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) { Text(result, color = ContactPickerColors.OnSurface, fontSize = 13.sp, modifier = Modifier.padding(16.dp)) }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (step > 0) OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("🔄 重新开始") }
    }
}

@Composable private fun DecisionCard(question: String, options: List<Pair<String, String>>, onAnswer: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(question, color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            options.forEach { (label, value) -> OutlinedButton(onClick = { onAnswer(value) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(label, fontSize = 12.sp) } }
        }
    }
}

@Composable private fun ResultCard(rec: String, reason: String, code: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(rec, color = ContactPickerColors.UrgencyP2, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(reason, color = ContactPickerColors.OnSurface, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Text("示例:", color = ContactPickerColors.OnSurfaceVariant, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            CodeBlock(code)
        }
    }
}

// =============================================================
// DeclarationTab — Tab 3: Declaration 申请
// =============================================================
@Composable private fun DeclarationTab(state: ContactPickerState, onUpdate: (String, String) -> Unit, onSave: () -> Unit) {
    val form = state.declarationForm
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("📝 Play Developer Declaration 申请", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("📜 申请条件", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("Google: 「READ_CONTACTS will be reserved for apps that can't function without it」", color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("典型可豁免: 系统级通讯录、MDM、备份 App", color = ContactPickerColors.UrgencyP2, fontSize = 12.sp)
                Text("典型不可豁免: 分享联系人、邀请好友、发送消息", color = ContactPickerColors.UrgencyP0, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        DeclField("App 名称", form.appName, { onUpdate("appName", it) }, "输入 App 名称")
        DeclField("包名", form.packageName, { onUpdate("packageName", it) }, "com.example.myapp")
        DeclField("READ_CONTACTS 使用场景", form.readContactsUseCase, { onUpdate("readContactsUseCase", it) }, "描述你的 App 为什么需要 READ_CONTACTS", false, 3)
        DeclField("为何无法使用 Contact Picker", form.whyCannotUseContactPicker, { onUpdate("whyCannotUseContactPicker", it) }, "解释为何无法用 Contact Picker 替代", false, 3)
        DeclField("已采取的替代隐私保护措施", form.alternativeMeasures, { onUpdate("alternativeMeasures", it) }, "描述你采取了哪些隐私保护措施", false, 2)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (form.savedToPrefs) ContactPickerColors.UrgencyP2 else ContactPickerColors.Primary)) { Text(if (form.savedToPrefs) "✅ 已保存" else "💾 保存") }
        if (form.savedToPrefs) { Spacer(Modifier.height(8.dp)); Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.UrgencyP2.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) { Text("✅ 表单已自动保存", color = ContactPickerColors.UrgencyP2, fontSize = 12.sp, modifier = Modifier.padding(12.dp)) } }
    }
}

@Composable private fun DeclField(label: String, value: String, onChange: (String) -> Unit, placeholder: String, single: Boolean = true, min: Int = 1) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, color = ContactPickerColors.OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(value = value, onValueChange = onChange, placeholder = { Text(placeholder, color = ContactPickerColors.OnSurfaceVariant, fontSize = 13.sp) }, singleLine = single, minLines = min, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ContactPickerColors.Primary, unfocusedBorderColor = ContactPickerColors.SurfaceVariant, focusedContainerColor = ContactPickerColors.Surface, unfocusedContainerColor = ContactPickerColors.Surface, cursorColor = ContactPickerColors.Primary, focusedTextColor = ContactPickerColors.OnSurface, unfocusedTextColor = ContactPickerColors.OnSurface), textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
    }
}

// =============================================================
// LocationButtonTab — Tab 4: Location Button 集成指南
// =============================================================
@Composable private fun LocationButtonTab(onCopy: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("📍 Location Button 集成指南", color = ContactPickerColors.OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Surface, shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("📱 Android 17 新特性", color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("Android 17 引入 `onlyForLocationButton` manifest flag，允许 App 请求一次性精确位置权限，而不需要持续访问位置。", color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Text("与 Contact Picker 并列成为 Play 合规两大技术主题。", color = ContactPickerColors.Accent, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        LocationSection("1️⃣ Manifest 权限声明", "使用 `usesPermissionFlags` 属性：", """<!-- Old: Permanent location permission -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- New: One-time location button (Android 17+) -->
<uses-permission
    android:name="android.permission.ACCESS_FINE_LOCATION"
    android:usesPermissionFlags="onlyForLocationButton" />""", onCopy)
        LocationSection("2️⃣ 运行时请求", "检查并请求位置权限：", """// Check location button permission
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    when {
        checkSelfPermission(ACCESS_FINE_LOCATION) == GRANTED -> { /* granted */ }
        shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> { /* show rationale */ }
        else -> requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_BUTTON)
    }
}""", onCopy)
        LocationSection("3️⃣ 位置按钮激活", "用户必须按下设备上的「Location Button」才能激活位置请求：", """// Get single location fix after button press
fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
    .addOnSuccessListener { location ->
        location?.let {
            Log.d("Location", "Got: ${'$'}{it.latitude}, ${'$'}{it.longitude}")
        }
    }""", onCopy)
        Surface(modifier = Modifier.fillMaxWidth(), color = ContactPickerColors.Primary.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("✅ Play 政策优势", color = ContactPickerColors.Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                listOf("• `onlyForLocationButton` 是 Play 推荐的精确位置请求方式", "• 相比持续位置跟踪，更容易通过 Play 审查", "• 符合 Android 17 隐私优先的设计理念").forEach { Text(it, color = ContactPickerColors.OnSurface, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp)) }
            }
        }
    }
}

@Composable private fun LocationSection(title: String, content: String, code: String, onCopy: (String) -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, color = ContactPickerColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(content, color = ContactPickerColors.OnSurfaceVariant, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { onCopy(code) }, modifier = Modifier.size(32.dp)) { Text("📋", fontSize = 16.sp) } }
        CodeBlock(code)
        Spacer(Modifier.height(8.dp))
    }
}
