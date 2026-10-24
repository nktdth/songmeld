package moe.ktandth.songmeld.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadEnginePolicyTest {
    private fun preview(provider: DownloadProvider, id: String) =
        TrackPreview(
            sourceUrl = "https://example.invalid/test",
            provider = provider,
            title = "Test",
            uploader = "Test",
            durationSeconds = 120,
            formats = listOf(AudioFormatChoice(id, "m4a", 128, "audio/mp4")),
        )

    @Test fun ytdlpIsPrimaryForAllSupportedProviders() {
        for (provider in DownloadProvider.entries) {
            assertTrue(
                "yt-dlp routing should not depend on provider: $provider",
                DownloadEnginePolicy.isYtDlpPreview(preview(provider, "ytdlp:140")),
            )
        }
    }

    @Test fun nativeNewPipeDirectFormatUsesDirectFileDownloader() {
        for (provider in DownloadProvider.entries) {
            assertFalse(
                DownloadEnginePolicy.isYtDlpPreview(preview(provider, "140")),
            )
        }
    }
}
