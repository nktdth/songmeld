package moe.ktandth.songmeld.download

data class AudioFormatChoice(
    val streamId: String,
    val extension: String,
    val bitrateKbps: Int?,
    val mimeType: String,
) {
    val label: String
        get() = buildString {
            append(extension.uppercase())
            bitrateKbps?.takeIf { it > 0 }?.let {
                append(" · ")
                append(it)
                append(" kb/s")
            }
        }
}

data class TrackPreview(
    val sourceUrl: String,
    val provider: DownloadProvider,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val formats: List<AudioFormatChoice>,
)

enum class DownloadStatus {
    QUEUED, PREPARING, DOWNLOADING, SAVING, COMPLETED, FAILED, CANCELED,
}

data class DownloadTask(
    val id: String,
    val sourceUrl: String,
    val provider: DownloadProvider,
    val title: String,
    val status: DownloadStatus,
    val requestedFormatId: String?,
    val progress: Float? = null,
    val downloadedBytes: Long = 0,
    val totalBytes: Long? = null,
    val error: String? = null,
) {
    val isActive: Boolean
        get() = status in setOf(
            DownloadStatus.QUEUED, DownloadStatus.PREPARING,
            DownloadStatus.DOWNLOADING, DownloadStatus.SAVING,
        )
}
