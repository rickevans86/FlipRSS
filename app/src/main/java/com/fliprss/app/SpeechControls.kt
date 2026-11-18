package com.fliprss.app

import android.content.Intent
import com.fliprss.app.data.SpeechOptions
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable internal fun rememberArticleSpeech(articleId: String, options: SpeechOptions = SpeechOptions()): ArticleSpeech {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val speech = remember(context, owner, options.engine, options.voice, options.allowOnline) { ArticleSpeech(context, options) }
    LaunchedEffect(articleId, speech) { speech.stop() }
    LaunchedEffect(speech, options.ratePercent, options.pitchPercent) { speech.setPlayback(options.ratePercent, options.pitchPercent) }
    DisposableEffect(speech, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_PAUSE) speech.pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); speech.close() }
    }
    return speech
}

@Composable internal fun SpeechControls(article: Article, speech: ArticleSpeech, onSettings: () -> Unit = {}) {
    val context = LocalContext.current
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(enabled = speech.ready, onClick = { speech.toggle(article) }) {
                Icon(if (speech.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text(if (speech.playing) "Pause reading" else if (speech.paused) "Resume reading" else "Read aloud")
            }
            TextButton(onClick = { speech.stop(); onSettings() }) { Text("Voice") }
            if (speech.playing || speech.paused) IconButton(onClick = speech::stop) { Icon(Icons.Filled.Stop, "Stop reading") }
        }
        if (!speech.ready && speech.error == null) Text("Preparing speech…", style = MaterialTheme.typography.bodySmall)
        speech.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (!speech.ready && speech.error != null) TextButton(onClick = speech::retryVoice) { Text("Retry voice") }
        if (speech.error != null) TextButton(onClick = {
            runCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS")) }.onFailure {
                Toast.makeText(context, "Open Android Settings to configure text-to-speech.", Toast.LENGTH_LONG).show()
            }
        }) { Text("Android voice settings") }
    }
}
