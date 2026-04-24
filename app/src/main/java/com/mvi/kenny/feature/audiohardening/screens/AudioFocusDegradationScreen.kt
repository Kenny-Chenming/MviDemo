package com.mvi.kenny.feature.audiohardening.screens

// ================================================================
// AudioFocusDegradationScreen — Audio Focus 降级策略（工具⑥）
// ================================================================
// 音频焦点降级策略模板。
//
// PRD-145 工具⑥
// ================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvi.kenny.feature.audiohardening.AudioFocusDegradationIntent
import com.mvi.kenny.feature.audiohardening.AudioFocusDegradationViewModel
import com.mvi.kenny.feature.audiohardening.AudioFocusStrategy

private val ColorCodeBackground = Color(0xFF1E1E1E)

@Composable
fun AudioFocusDegradationScreen(
    onBack: () -> Unit = {},
    viewModel: AudioFocusDegradationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "⑥ Audio Focus 降级策略",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "音频焦点降级策略模板",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 策略选择
        Text("降级策略", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))

        AudioFocusStrategy.entries.forEach { strategy ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.selectedStrategy == strategy)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedStrategy == strategy,
                        onClick = { viewModel.sendIntent(AudioFocusDegradationIntent.SelectStrategy(strategy)) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(strategy.titleCn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(strategy.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 代码模板
        Text("代码模板", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = ColorCodeBackground
        ) {
            Text(
                text = getStrategyCode(state.selectedStrategy),
                fontFamily = FontFamily.Monospace,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = Color(0xFFD4D4D4),
                modifier = Modifier.padding(16.dp),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 行为预览
        Text("行为预览", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        state.previewBehaviors.forEach { (focusType, behavior) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    focusType,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    behavior,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 复制按钮
        Button(
            onClick = { viewModel.sendIntent(AudioFocusDegradationIntent.CopyCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("复制代码")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun getStrategyCode(strategy: AudioFocusStrategy): String {
    return when (strategy) {
        AudioFocusStrategy.PauseAndWait -> """
val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mediaPlayer.pause()
            AudioManager.AUDIOFOCUS_GAIN -> mediaPlayer.start()
        }
    }
    .build()
        """.trimIndent()
        AudioFocusStrategy.DuckAndRestore -> """
val audioFocusRequest = AudioFocusRequest.Builder(
    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer.setVolume(1f, 1f)
                if (!mediaPlayer.isPlaying) mediaPlayer.start()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.setVolume(0.2f, 0.2f)
            }
        }
    }
    .build()
        """.trimIndent()
        AudioFocusStrategy.DuckAndPause -> """
val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
    .setOnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaPlayer.pause()
                mediaPlayer.setVolume(1f, 1f)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer.setVolume(0.1f, 0.1f)
                handler.postDelayed({ mediaPlayer.pause() }, 1000)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer.setVolume(1f, 1f)
                mediaPlayer.start()
            }
        }
    }
    .build()
        """.trimIndent()
    }
}
