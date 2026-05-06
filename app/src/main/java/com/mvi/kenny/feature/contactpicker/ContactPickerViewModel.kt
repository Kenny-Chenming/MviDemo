package com.mvi.kenny.feature.contactpicker

// ================================================================
// ContactPickerViewModel — Google Play Contact Picker 强制迁移工具包 ViewModel
// ================================================================
// PRD-234: Google Play Contact Picker 强制迁移工具包
// MVI ViewModel: Intent → Process → State → UI
// ================================================================

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactPickerViewModel : ViewModel() {
    private val _state = MutableStateFlow(ContactPickerState())
    val state: StateFlow<ContactPickerState> = _state.asStateFlow()
    private val _effect = Channel<ContactPickerEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun sendIntent(intent: ContactPickerIntent) {
        when (intent) {
            is ContactPickerIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.index) }
            is ContactPickerIntent.StartScan -> handleStartScan()
            is ContactPickerIntent.AnswerDecisionNode -> _state.update {
                it.copy(decisionTreeStep = it.decisionTreeStep + 1, decisionTreeAnswers = it.decisionTreeAnswers + (intent.nodeId to intent.answer))
            }
            is ContactPickerIntent.ResetDecisionTree -> _state.update { it.copy(decisionTreeStep = 0, decisionTreeAnswers = emptyMap()) }
            is ContactPickerIntent.UpdateDeclarationField -> _state.update {
                it.copy(declarationForm = when (intent.field) {
                    "appName" -> it.declarationForm.copy(appName = intent.value)
                    "packageName" -> it.declarationForm.copy(packageName = intent.value)
                    "readContactsUseCase" -> it.declarationForm.copy(readContactsUseCase = intent.value)
                    "whyCannotUseContactPicker" -> it.declarationForm.copy(whyCannotUseContactPicker = intent.value)
                    "alternativeMeasures" -> it.declarationForm.copy(alternativeMeasures = intent.value)
                    else -> it.declarationForm
                })
            }
            is ContactPickerIntent.SaveDeclaration -> viewModelScope.launch {
                _state.update { it.copy(declarationForm = it.declarationForm.copy(savedToPrefs = true)) }
                _effect.send(ContactPickerEffect.ShowSnackbar("Declaration 已保存"))
            }
            is ContactPickerIntent.CopyCodeBlock -> viewModelScope.launch {
                _effect.send(ContactPickerEffect.CopyToClipboard(intent.code))
                _effect.send(ContactPickerEffect.ShowSnackbar("代码已复制"))
            }
            is ContactPickerIntent.SelectApiCategory -> _state.update { it.copy(apiGuideSelectedCategory = intent.category) }
        }
    }

    private fun handleStartScan() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true) }
            delay(1500)
            _state.update { it.copy(isScanning = false, scanResult = createSimulatedScanResult()) }
        }
    }

    private fun createSimulatedScanResult() = ScanResult(
        urgencyLevel = UrgencyLevel.MUST_MIGRATE,
        manifestFindings = listOf(ManifestFinding("READ_CONTACTS", 24, "<uses-permission android:name=\"android.permission.READ_CONTACTS\" />")),
        codeFindings = listOf(
            CodeFinding("ContactUtils.kt", 42, "contentResolver.query(ContactsContract.Contacts.CONTENT_URI, ...)", "获取联系人列表用于分享邀请"),
            CodeFinding("ShareActivity.kt", 78, "ActivityCompat.checkSelfPermission(this, READ_CONTACTS)", "检查 READ_CONTACTS 权限")
        ),
        migrationSteps = listOf(
            MigrationStep(1, "移除 READ_CONTACTS 权限声明", "从 AndroidManifest.xml 中删除 READ_CONTACTS 权限声明",
                CodeDiff("""<uses-permission android:name="android.permission.READ_CONTACTS" />""",
                    """<!-- READ_CONTACTS removed for Contact Picker migration (PRD-234) -->""", "xml")),
            MigrationStep(2, "使用 Intent.ACTION_PICK_CONTACTS 替代", "使用 Contact Picker 替代直接读取联系人列表",
                CodeDiff("""// Old: checkSelfPermission + query
if (checkSelfPermission(READ_CONTACTS) == GRANTED) {
    val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
}""",
                    """// New: Contact Picker (privacy-first)
val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    putExtra(EXTRA_URI, ContactsContract.Contacts.CONTENT_URI)
}
startActivityForResult(intent, REQUEST_PICK_CONTACT)""", "kotlin")),
            MigrationStep(3, "使用 ContactPickerSession 保留历史", "使用 Session Uri 保留用户选择历史，避免每次弹系统 picker",
                CodeDiff("""// No session tracking
val intent = Intent(Intent.ACTION_PICK_CONTACTS)
startActivityForResult(intent, REQUEST_CODE)""",
                    """// With Session Uri (retains selection history)
val sessionUri = ContactPickerSession.currentSessionUri
val intent = Intent(Intent.ACTION_PICK_CONTACTS).apply {
    putExtra(EXTRA_URI, ContactsContract.Contacts.CONTENT_URI)
    sessionUri?.let { putExtra(EXTRA_SESSION_URI, it) }
}""", "kotlin"))
        )
    )
}

class ContactPickerViewModelFactory : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactPickerViewModel::class.java)) return ContactPickerViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
