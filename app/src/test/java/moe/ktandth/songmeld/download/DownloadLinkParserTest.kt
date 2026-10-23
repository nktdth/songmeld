package moe.ktandth.songmeld.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadLinkParserTest {
    @Test fun supportsSingleTracksFromThreeSources() {
        assertEquals(DownloadProvider.YOUTUBE,
            detectDownloadProvider("https://www.youtube.com/watch?v=abcdefghijk"))
        assertEquals(DownloadProvider.YOUTUBE,
            detectDownloadProvider("https://youtu.be/abcdefghijk"))
        assertEquals(DownloadProvider.YOUTUBE,
            detectDownloadProvider("https://m.youtube.com/shorts/abcdefghijk"))
        assertEquals(DownloadProvider.YOUTUBE_MUSIC,
            detectDownloadProvider("https://music.youtube.com/watch?v=abcdefghijk"))
        assertEquals(DownloadProvider.SOUNDCLOUD,
            detectDownloadProvider("https://soundcloud.com/artist/track-name"))
        assertEquals(DownloadProvider.SOUNDCLOUD,
            detectDownloadProvider("https://on.soundcloud.com/abc"))
    }

    @Test fun rejectsSpoofedHostsAndUnsafeSchemes() {
        listOf(
            "http://youtube.com/watch?v=123",
            "https://youtube.com.evil.test/watch?v=123",
            "https://youtube.com@evil.test/watch?v=123",
            "https://notyoutube.com/watch?v=123",
            "file:///etc/passwd",
            "javascript:alert(1)",
            "https://soundcloud.com.evil.test/u/track",
            "https://youtube.com/watch",
            "https://music.youtube.com/playlist?list=xyz",
            "https://soundcloud.com/u/sets/mix",
        ).forEach { assertNull(it, detectDownloadProvider(it)) }
    }
}
