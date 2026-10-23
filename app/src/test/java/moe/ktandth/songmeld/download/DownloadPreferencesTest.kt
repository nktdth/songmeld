package moe.ktandth.songmeld.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadPreferencesTest {
    private val sources = listOf(
        AudioFormatChoice("opus64", "webm", 64, "audio/webm"),
        AudioFormatChoice("aac128", "m4a", 128, "audio/mp4"),
        AudioFormatChoice("opus160", "webm", 160, "audio/webm"),
    )

    @Test fun bestPicksHighestBitrate() {
        val result = DownloadPreferences.chooseDefaultFormat(
            sources, DownloadQualityPolicy.BEST, PreferredAudioCodec.AUTO
        )
        assertEquals("opus160", result?.streamId)
    }

    @Test fun savingDataPicksLowestBitrate() {
        val result = DownloadPreferences.chooseDefaultFormat(
            sources, DownloadQualityPolicy.SAVE_DATA, PreferredAudioCodec.AUTO
        )
        assertEquals("opus64", result?.streamId)
    }

    @Test fun preferredCodecFiltersBeforeQuality() {
        val result = DownloadPreferences.chooseDefaultFormat(
            sources, DownloadQualityPolicy.BEST, PreferredAudioCodec.M4A_AAC
        )
        assertEquals("aac128", result?.streamId)
    }

    @Test fun missingPreferredCodecFallsBackToAvailable() {
        val result = DownloadPreferences.chooseDefaultFormat(
            sources.filter { it.extension == "webm" },
            DownloadQualityPolicy.BEST,
            PreferredAudioCodec.M4A_AAC
        )
        assertEquals("opus160", result?.streamId)
    }

    @Test fun manualModeNeverPreselects() {
        assertNull(
            DownloadPreferences.chooseDefaultFormat(
                sources, DownloadQualityPolicy.ASK_EACH_TIME, PreferredAudioCodec.M4A_AAC
            )
        )
    }
}
