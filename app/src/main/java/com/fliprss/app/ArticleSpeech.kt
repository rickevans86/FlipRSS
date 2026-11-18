package com.fliprss.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import com.fliprss.app.data.*

/** Foreground reader speech. No background service; all engine callbacks are serialized on main. */
class ArticleSpeech(context: Context, options: SpeechOptions = SpeechOptions()) {
    private val options = options.normalized()
    private var ratePercent = this.options.ratePercent
    private var pitchPercent = this.options.pitchPercent
    private val appContext = context.applicationContext
    private var initialization = 0
    private val handler = Handler(Looper.getMainLooper())
    private val audio = context.applicationContext.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener({ change ->
            if (change < 0) pause()
        }, handler).build()
    private var engine: TextToSpeech? = null
    private var closed = false
    private var generation = 0
    private var chunkIndex = 0
    private var offset = 0
    private var chunks = emptyList<String>()
    private var focused = false
    internal var voices by mutableStateOf<List<SpeechVoice>>(emptyList()); private set
    internal var engines by mutableStateOf<List<SpeechEngine>>(emptyList()); private set
    internal var selectedVoice by mutableStateOf<SpeechVoice?>(null); private set
    var ready by mutableStateOf(false); private set
    var playing by mutableStateOf(false); private set
    var paused by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    init { startEngine() }

    private fun startEngine() {
        val attempt = ++initialization
        try {
            val listener = TextToSpeech.OnInitListener { status -> handler.post {
                if (!closed && attempt == initialization) initialize(status)
            } }
            engine = if (options.engine.isBlank()) TextToSpeech(appContext, listener)
                else TextToSpeech(appContext, listener, options.engine)
        } catch (_: Exception) { error = "Text-to-speech is unavailable. Check Android voice settings." }
    }

    fun retryVoice() {
        if (closed) return
        stop(); ready = false; voices = emptyList(); selectedVoice = null
        engine?.shutdown(); engine = null
        error = null
        startEngine()
    }

    private fun initialize(status: Int) {
        val tts = engine ?: return
        engines = runCatching { tts.engines.map { SpeechEngine(it.name, it.label) }.sortedBy { it.label } }.getOrDefault(emptyList())
        if (status != TextToSpeech.SUCCESS) { error = "Could not start text-to-speech. Check Android voice settings."; return }
        try {
            val language = Locale.getDefault()
            val supported = tts.setLanguage(language)
            if (options.engine.isNotBlank() && engines.none { it.name == options.engine }) {
                error = "The selected speech engine is unavailable. Choose System default in Speech settings."; return
            }
            val installed = tts.voices.orEmpty().filterNot { TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED in it.features.orEmpty() }
            val catalog = installed.map { SpeechVoice(it.name, it.locale.toLanguageTag(), it.isNetworkConnectionRequired, it.quality, it.latency) }
            voices = availableSpeechVoices(catalog, options, language)
            selectedVoice = selectSpeechVoice(catalog, options, language)
            val selected = selectedVoice
            if (selected == null && (supported == TextToSpeech.LANG_MISSING_DATA || supported == TextToSpeech.LANG_NOT_SUPPORTED)) {
                error = "Install a speech voice for your device language in Android voice settings."; return
            }
            if (selected == null && !options.allowOnline) {
                error = "No on-device voice is available for your language. Install a voice, choose another engine, or allow online voices in Speech settings."; return
            }
            selected?.let { voice ->
                if (tts.setVoice(installed.first { it.name == voice.name }) != TextToSpeech.SUCCESS) {
                    error = "Could not select this voice. Choose another voice in Speech settings."; return
                }
            }
            tts.setSpeechRate(ratePercent / 100f)
            tts.setPitch(pitchPercent / 100f)
            tts.setAudioAttributes(attributes)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) = Unit
                override fun onDone(id: String?) { callback(id) {
                    chunkIndex++; offset = 0
                    if (chunkIndex < chunks.size) speakChunk() else { playing = false; paused = false; releaseFocus() }
                } }
                @Deprecated("Android legacy callback")
                override fun onError(id: String?) { callback(id) { fail() } }
                override fun onError(id: String?, code: Int) { callback(id) { fail() } }
                override fun onRangeStart(id: String?, start: Int, end: Int, frame: Int) {
                    callback(id) { offset = (utteranceOffset + start).coerceAtMost(chunks[chunkIndex].length) }
                }
            })
            ready = true
        } catch (_: Exception) { error = "Could not load a speech voice. Check Android voice settings." }
    }
    fun setPlayback(rate: Int, pitch: Int) {
        val nextRate = rate.coerceIn(60, 140); val nextPitch = pitch.coerceIn(80, 120)
        if (nextRate != ratePercent || nextPitch != pitchPercent) {
            if (playing || paused) stop()
            ratePercent = nextRate; pitchPercent = nextPitch
            if (ready) { engine?.setSpeechRate(ratePercent / 100f); engine?.setPitch(pitchPercent / 100f) }
        }
    }
    private var utteranceOffset = 0
    private fun callback(id: String?, action: () -> Unit) {
        handler.post { if (!closed && playing && id == "$generation:$chunkIndex") action() }
    }
    fun toggle(article: Article) {
        if (playing) { pause(); return }
        if (!ready || closed) return
        if (!paused || chunkIndex >= chunks.size) {
            chunks = SpeechText.chunks(article, minOf(3500, TextToSpeech.getMaxSpeechInputLength()))
            chunkIndex = 0; offset = 0
        }
        if (chunks.isEmpty()) { error = "This article has no text to read."; return }
        if (audio.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            error = "Audio is unavailable. Try playing again."; return
        }
        focused = true; generation++; error = null; playing = true; paused = false
        speakChunk()
    }
    private fun speakChunk() {
        val text = chunks[chunkIndex]
        utteranceOffset = offset.coerceIn(0, text.length)
        if (utteranceOffset == text.length) { chunkIndex++; offset = 0; if (chunkIndex < chunks.size) speakChunk() else stop(); return }
        val result = engine?.speak(text.substring(utteranceOffset), TextToSpeech.QUEUE_FLUSH, null, "$generation:$chunkIndex")
        if (result != TextToSpeech.SUCCESS) fail()
    }
    fun pause() {
        if (!playing) return
        generation++; playing = false; paused = true
        engine?.stop(); releaseFocus()
    }
    fun stop() {
        generation++; playing = false; paused = false
        engine?.stop(); releaseFocus(); chunks = emptyList(); chunkIndex = 0; offset = 0; if (ready) error = null
    }
    private fun fail() { stop(); error = if (selectedVoice?.network == true) "Online voice playback failed. Check your connection or choose an on-device voice." else "Speech playback failed. Try again or check Android voice settings." }
    private fun releaseFocus() { if (focused) { audio.abandonAudioFocusRequest(focus); focused = false } }
    fun close() { stop(); closed = true; engine?.shutdown(); engine = null }
}
