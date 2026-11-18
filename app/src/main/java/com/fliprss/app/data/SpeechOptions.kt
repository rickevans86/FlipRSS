package com.fliprss.app.data

import java.util.Locale

data class SpeechOptions(val engine: String = "", val voice: String = "", val allowOnline: Boolean = false,
    val ratePercent: Int = 95, val pitchPercent: Int = 100) {
    fun normalized() = copy(engine = engine.trim(), voice = voice.trim(), ratePercent = ratePercent.coerceIn(60,140), pitchPercent = pitchPercent.coerceIn(80,120))
}
internal data class SpeechVoice(val name: String, val languageTag: String, val network: Boolean, val quality: Int, val latency: Int) {
    val label: String get() = "${Locale.forLanguageTag(languageTag).getDisplayName()} · ${if(network) "Online" else "On device"} · ${when { quality>=500->"Very high";quality>=400->"High";else->"Standard" }}"
}
internal data class SpeechEngine(val name: String, val label: String)
internal fun availableSpeechVoices(voices: List<SpeechVoice>, options: SpeechOptions, locale: Locale): List<SpeechVoice> =
    voices.filter { (options.allowOnline || !it.network) && Locale.forLanguageTag(it.languageTag).language == locale.language }
        .sortedWith(compareByDescending<SpeechVoice> { Locale.forLanguageTag(it.languageTag) == locale }
            .thenByDescending { it.quality }.thenByDescending { options.allowOnline && it.network }.thenBy { it.latency }.thenBy { it.name })
internal fun selectSpeechVoice(voices: List<SpeechVoice>, options: SpeechOptions, locale: Locale): SpeechVoice? {
    val available = availableSpeechVoices(voices, options, locale)
    return available.firstOrNull { it.name == options.voice } ?: available.firstOrNull()
}
