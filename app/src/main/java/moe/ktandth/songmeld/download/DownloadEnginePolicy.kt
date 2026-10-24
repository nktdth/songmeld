package moe.ktandth.songmeld.download

/** Keep download engine routing independent of provider: yt-dlp handles all three sources. */
internal object DownloadEnginePolicy {
    fun isYtDlpPreview(preview: TrackPreview): Boolean =
        preview.formats.any { it.streamId.startsWith(YtDlpFallback.FORMAT_PREFIX) }
}
