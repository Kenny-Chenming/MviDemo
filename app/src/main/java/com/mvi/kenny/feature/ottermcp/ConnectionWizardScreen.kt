package com.mvi.kenny.feature.ottermcp

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionWizardScreen(viewModel: ConnectionWizardViewModel) {
    val uiState by viewModel.state.collectAsState()
    
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        StepperIndicator(uiState.currentStep, 3)
        Spacer(Modifier.height(24.dp))
        
        AnimatedContent(
            uiState.currentStep,
            transitionSpec = {
                if (targetState > initialState) (slideInHorizontally { it } + fadeIn()) with (slideOutHorizontally { -it } + fadeOut())
                else (slideInHorizontally { -it } + fadeIn()) with (slideOutHorizontally { it } + fadeOut())
            },
            label = "step"
        ) { step ->
            when (step) {
                0 -> Step1SelectType(uiState.connectionType) { viewModel.sendIntent(ConnectionWizardIntent.SelectConnectionType(it)) }
                1 -> Step2Configure(uiState.serverUrl, uiState.authToken, uiState.selectedTools,
                    { viewModel.sendIntent(ConnectionWizardIntent.UpdateServerUrl(it)) },
                    { viewModel.sendIntent(ConnectionWizardIntent.UpdateAuthToken(it)) },
                    { viewModel.sendIntent(ConnectionWizardIntent.ToggleTool(it)) },
                    { viewModel.sendIntent(ConnectionWizardIntent.TestConnection) },
                    uiState.isTesting, uiState.connectionTestResult)
                2 -> Step3Verify(uiState.serverUrl, uiState.selectedTools.toList(), uiState.isSaving)
            }
        }
        
        Spacer(Modifier.weight(1f))
        NavigationButtons(uiState.currentStep, uiState.currentStep > 0, uiState.currentStep < 2 && uiState.canProceed, uiState.currentStep == 2, uiState.isSaving,
            { viewModel.sendIntent(ConnectionWizardIntent.PreviousStep) },
            { viewModel.sendIntent(ConnectionWizardIntent.NextStep) },
            { viewModel.sendIntent(ConnectionWizardIntent.SaveConnection) })
    }
}

@Composable
private fun StepperIndicator(currentStep: Int, totalSteps: Int) {
    val labels = listOf("Select Type", "Configure", "Verify")
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { index, label ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(when { isCompleted -> MaterialTheme.colorScheme.primary; isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f); else -> MaterialTheme.colorScheme.surfaceVariant }), contentAlignment = Alignment.Center) {
                        if (isCompleted) Icon(Icons.Default.CheckCircle, "Completed", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        else Text("${index + 1}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (index < totalSteps - 1) Box(Modifier.weight(1f).height(2.dp).padding(top = 16.dp).background(if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator({ (currentStep + 1).toFloat() / totalSteps }, Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@Composable
private fun Step1SelectType(selected: ConnectionType?, onSelect: (ConnectionType) -> Unit) {
    Column {
        Text("Choose Connection Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Select how you want to connect to an MCP Server", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        ConnectionTypeCard("Connect to Existing Server", "Connect to an external MCP Server using URL and authentication", Icons.Default.Cloud, selected == ConnectionType.EXTERNAL) { onSelect(ConnectionType.EXTERNAL) }
        Spacer(Modifier.height(12.dp))
        ConnectionTypeCard("Create from Android Template", "Create an Android-specific MCP Server using pre-built templates", Icons.Default.PhoneAndroid, selected == ConnectionType.ANDROID_TEMPLATE) { onSelect(ConnectionType.ANDROID_TEMPLATE) }
    }
}

@Composable
private fun ConnectionTypeCard(title: String, desc: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick).then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)) else Modifier), colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, title, Modifier.size(40.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Column { Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium); Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun Step2Configure(serverUrl: String, authToken: String, selectedTools: Set<String>, onUrlChange: (String) -> Unit, onTokenChange: (String) -> Unit, onToggleTool: (String) -> Unit, onTest: () -> Unit, isTesting: Boolean, testResult: TestResult?) {
    val tools = listOf("adb-shell", "screenshot", "logcat", "screenrecord", "gradle-build", "gradle-clean", "gradle-test", "firebase-crash", "firebase-analytics")
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Configure Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        item { OutlinedTextField(serverUrl, onUrlChange, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Server URL") }, placeholder = { Text("http://localhost:5037") }) }
        item { OutlinedTextField(authToken, onTokenChange, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Auth Token (Optional)") }) }
        item {
            Button(onTest, Modifier.fillMaxWidth(), enabled = !isTesting) {
                if (isTesting) { CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("Testing...") }
                else Text("Test Connection")
            }
            when (testResult) {
                is TestResult.Success -> { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = Color(0xFF4CAF50)); Spacer(Modifier.width(4.dp)); Text("Connection successful!", Modifier.padding(8.dp), Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall) } }
                is TestResult -> { if (!testResult.success) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Error, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(4.dp)); Text(testResult.message, Modifier.padding(8.dp), MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) } } }
                null -> {}
            }
        }
        item { Text("Allowed Tools", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary) }
        items(tools.chunked(2)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { tool ->
                    Row(Modifier.weight(1f).clickable { onToggleTool(tool) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(selectedTools.contains(tool), {}); Text(tool, style = MaterialTheme.typography.bodySmall) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Step3Verify(serverUrl: String, connectedTools: List<String>, isSaving: Boolean) {
    Column {
        Text("Verify & Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Connection Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Server URL", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(serverUrl.ifEmpty { "Not configured" }, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.height(8.dp))
                Text("Connected Tools (${connectedTools.size})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                connectedTools.take(6).forEach { Text("- $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun NavigationButtons(currentStep: Int, canGoBack: Boolean, canGoNext: Boolean, canSave: Boolean, isSaving: Boolean, onBack: () -> Unit, onNext: () -> Unit, onSave: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (canGoBack) OutlinedButton(onBack, Modifier.weight(1f)) { Text("Back") }
        if (canSave) Button(onSave, Modifier.weight(1f), enabled = !isSaving) { if (isSaving) { CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }; Text("Save Connection") }
        else if (canGoNext) Button(onNext, Modifier.weight(1f)) { Text("Next") }
    }
}
