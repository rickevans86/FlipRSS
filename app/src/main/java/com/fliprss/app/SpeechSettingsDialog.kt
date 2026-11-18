package com.fliprss.app

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fliprss.app.data.*
import kotlin.math.roundToInt

@Composable internal fun SpeechSettingsDialog(settings: AppSettings, update: (AppSettings) -> Unit, onClose: () -> Unit) {
    val options = settings.speech
    val speech = rememberArticleSpeech("voice-preview", options)
    val context = LocalContext.current
    var engineMenu by remember { mutableStateOf(false) }
    var voiceMenu by remember { mutableStateOf(false) }
    val preview = remember { DemoLibrary.articles.first().copy(title = "Voice preview", summary = "",
        paragraphs = listOf("Take your time. Enjoy a more comfortable listening experience, with the voice and pace you prefer.")) }
    fun change(value: SpeechOptions) = update(settings.copy(speech = value.normalized()))
    AlertDialog(onDismissRequest = onClose, title = { Text("Speech and voices") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Choose a voice for your phone's language and preview it. Quality and available voices depend on the installed Android engine.")
            Box {
                OutlinedButton(onClick = { engineMenu = true }) {
                    Text("Engine: " + if (options.engine.isBlank()) "System default" else speech.engines.find { it.name == options.engine }?.label ?: "Unavailable engine")
                }
                DropdownMenu(engineMenu, { engineMenu = false }) {
                    DropdownMenuItem(text = { Text("System default") }, onClick = { engineMenu = false; change(options.copy(engine = "", voice = "")) })
                    speech.engines.forEach { engine -> DropdownMenuItem(text = { Text(engine.label) }, onClick = {
                        engineMenu = false; change(options.copy(engine = engine.name, voice = ""))
                    }) }
                }
            }
            Row {
                Switch(options.allowOnline, { change(options.copy(allowOnline = it, voice = "")) })
                Column(Modifier.padding(start = 8.dp)) {
                    Text("Allow online voices")
                    Text("May provide more natural voices. Your chosen engine may send article text over the internet.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Box {
                OutlinedButton(onClick = { voiceMenu = true }, enabled = speech.voices.isNotEmpty()) {
                    Text(if (options.voice.isBlank()) "Voice: Automatic — best available" else "Voice: " + (speech.voices.find { it.name == options.voice }?.name ?: "Unavailable; automatic fallback"))
                }
                DropdownMenu(voiceMenu, { voiceMenu = false }, modifier = Modifier.heightIn(max = 320.dp)) {
                    DropdownMenuItem(text = { Text("Automatic — best available") }, onClick = { voiceMenu = false; change(options.copy(voice = "")) })
                    speech.voices.forEach { voice -> DropdownMenuItem(text = {
                        Column { Text(voice.label); Text(voice.name, style = MaterialTheme.typography.labelSmall) }
                    }, onClick = { voiceMenu = false; change(options.copy(voice = voice.name)) }) }
                }
            }
            speech.selectedVoice?.let { Text("Using ${it.label}\n${it.name}", style = MaterialTheme.typography.bodySmall) }
            Text("Reading speed: ${options.ratePercent}%")
            Slider(options.ratePercent.toFloat(), { change(options.copy(ratePercent = it.roundToInt())) }, valueRange = 60f..140f, steps = 15)
            Text("Pitch: ${options.pitchPercent}% (100% is normal)")
            Slider(options.pitchPercent.toFloat(), { change(options.copy(pitchPercent = it.roundToInt())) }, valueRange = 80f..120f, steps = 7)
            Button(enabled = speech.ready, onClick = { if (speech.playing || speech.paused) speech.stop() else speech.toggle(preview) }) {
                Text(if (speech.playing || speech.paused) "Stop preview" else "Preview voice")
            }
            if (!speech.ready && speech.error == null) Text("Loading voices…", style = MaterialTheme.typography.bodySmall)
            speech.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(onClick = speech::retryVoice) { Text("Refresh voices") }
            TextButton(onClick = {
                runCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS")) }.onFailure {
                    Toast.makeText(context, "Open Android Settings to install or configure speech voices.", Toast.LENGTH_LONG).show()
                }
            }) { Text("Android voice settings") }
            TextButton(onClick = { change(SpeechOptions()) }) { Text("Reset speech defaults") }
        }
    }, confirmButton = { TextButton(onClick = onClose) { Text("Done") } })
}
