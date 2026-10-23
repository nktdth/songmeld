package moe.ktandth.songmeld.download

import java.net.URI
import java.util.Locale

enum class DownloadProvider(val displayName: String) {
    YOUTUBE("YouTube"),
    YOUTUBE_MUSIC("YouTube Music"),
    SOUNDCLOUD("SoundCloud"),
}

/**
 * Accept only well-formed, public HTTPS source links. Never accept hosts by substring,
 * user-info (`youtube.com@evil.example`) or suffix look-alikes.
 * This intentionally limits the first downloader release to single tracks.
 */
fun detectDownloadProvider(value: String): DownloadProvider? {
    val text = value.trim()
    if (text.length !in 1..2048) return null
    val uri = runCatching { URI(text) }.getOrNull() ?: return null
    if (uri.scheme?.lowercase(Locale.ROOT) != "https" ||
        uri.rawUserInfo != null || uri.fragment != null || uri.port != -1
    ) return null
    val host = uri.host?.lowercase(Locale.ROOT) ?: return null
    val segments = uri.path.orEmpty().trim('/').split('/').filter { it.isNotBlank() }
    if (segments.isEmpty()) return null

    val hasVideoId = uri.rawQuery.orEmpty().split('&').any { part ->
        part.substringBefore('=') == "v" && part.substringAfter('=', "").isNotBlank()
    }
    return when {
        host == "youtu.be" && segments.size == 1 -> DownloadProvider.YOUTUBE
        host == "music.youtube.com" && segments == listOf("watch") && hasVideoId ->
            DownloadProvider.YOUTUBE_MUSIC
        host in setOf("youtube.com", "www.youtube.com", "m.youtube.com") -> when {
            segments == listOf("watch") && hasVideoId -> DownloadProvider.YOUTUBE
            segments.size == 2 && segments[0] in setOf("shorts", "live") ->
                DownloadProvider.YOUTUBE
            else -> null
        }
        host == "on.soundcloud.com" && segments.size == 1 -> DownloadProvider.SOUNDCLOUD
        host in setOf("soundcloud.com", "www.soundcloud.com", "m.soundcloud.com") &&
            segments.size >= 2 && segments[1] != "sets" ->
            DownloadProvider.SOUNDCLOUD
        else -> null
    }
}
