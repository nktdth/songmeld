package moe.ktandth.songmeld.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaStoreAudioFallbackTest {
    @Test fun unsupportedMimeTriggersGenericMediaFallback() {
        assertTrue(MediaStoreAudioFallback.shouldUseDownloads("Unsupported MIME type audio/webm"))
        assertTrue(MediaStoreAudioFallback.shouldUseDownloads("Unsupported MIME type audio/opus"))
    }

    @Test fun unrelatedFailuresAreNotSilenced() {
        assertFalse(MediaStoreAudioFallback.shouldUseDownloads("Permission denied"))
        assertFalse(MediaStoreAudioFallback.shouldUseDownloads("Not enough free storage"))
        assertFalse(MediaStoreAudioFallback.shouldUseDownloads(null))
    }
}
