package com.fliprss.app.data

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class SpeechOptionsTest {
    private val low=SpeechVoice("low","en-GB",false,200,100)
    private val high=SpeechVoice("high","en-GB",false,500,200)
    private val online=SpeechVoice("online","en-GB",true,500,300)
    private val foreign=SpeechVoice("foreign","fr-FR",false,500,100)
    private val voices=listOf(low,online,foreign,high)
    @Test fun bestInstalledVoiceIsDefaultWithoutNetworkOptIn() {
        assertFalse(SpeechOptions().allowOnline)
        assertEquals(high,selectSpeechVoice(voices,SpeechOptions(),Locale.UK))
        assertFalse(availableSpeechVoices(voices,SpeechOptions(),Locale.UK).any { it.network })
    }
    @Test fun onlineQualityIsChosenOnlyWhenAllowed() {
        assertEquals(online,selectSpeechVoice(voices,SpeechOptions(allowOnline=true),Locale.UK))
        assertEquals(high,selectSpeechVoice(voices,SpeechOptions(voice="online"),Locale.UK))
        assertNull(selectSpeechVoice(listOf(online),SpeechOptions(),Locale.UK))
    }
    @Test fun userSelectedVoiceOverridesAutomaticQualityAndMissingVoiceFallsBack() {
        assertEquals(low,selectSpeechVoice(voices,SpeechOptions(voice="low"),Locale.UK))
        assertEquals(high,selectSpeechVoice(voices,SpeechOptions(voice="uninstalled"),Locale.UK))
        assertEquals(high,selectSpeechVoice(voices,SpeechOptions(voice="foreign"),Locale.UK))
    }
    @Test fun deviceRegionIsPreferredAndAnotherRegionRemainsUsable() {
        val us=high.copy(name="US",languageTag="en-US")
        assertEquals(high,selectSpeechVoice(listOf(us,high),SpeechOptions(),Locale.UK))
        assertEquals(us,selectSpeechVoice(listOf(us),SpeechOptions(),Locale.UK))
        assertNull(selectSpeechVoice(listOf(foreign),SpeechOptions(),Locale.UK))
    }
    @Test fun playbackSettingsNormalizeAndSurviveSettingsStore() {
        val store=MemorySettingsStore()
        store.save(AppSettings(speech=SpeechOptions(" engine "," high ",true,999,0)))
        val actual=store.load().speech
        assertEquals("engine",actual.engine);assertEquals("high",actual.voice)
        assertTrue(actual.allowOnline);assertEquals(140,actual.ratePercent);assertEquals(80,actual.pitchPercent)
        assertEquals(95,SpeechOptions().ratePercent);assertEquals(100,SpeechOptions().pitchPercent)
    }
}
