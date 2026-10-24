package moe.ktandth.songmeld.download

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Alternative SoundCloud extractor/download engine, reusing the mature library used by
 * Seal and SongMeld-v1. Invoked only when NewPipe cannot resolve an eligible track.
 *
 * This downloads source audio as-is to the app cache, then SongMeld's MediaStore/SAF
 * writer imports the real file. No codec transcoding, DRM bypass, or login workaround.
 */
internal object YtDlpFallback {
    const val FORMAT_PREFIX = "ytdlp:"
    private val allowedExtensions = setOf("mp3", "m4a", "aac", "webm", "opus", "ogg")
    private val mimeTypes = mapOf(
        "mp3" to "audio/mpeg",
        "m4a" to "audio/mp4",
        "aac" to "audio/aac",
        "webm" to "audio/webm",
        "opus" to "audio/ogg",
        "ogg" to "audio/ogg",
    )

    fun isFallbackFormat(id: String?): Boolean = id?.startsWith(FORMAT_PREFIX) == true

    suspend fun preview(context: Context, url: String): TrackPreview =
        withContext(Dispatchers.IO) {
            if (detectDownloadProvider(url) != DownloadProvider.SOUNDCLOUD) {
                throw IllegalArgumentException("Bộ tải SoundCloud dự phòng chỉ hỗ trợ URL SoundCloud hợp lệ.")
            }
            YoutubeDL.init(context.applicationContext)
            val info = YoutubeDL.getInfo(
                YoutubeDLRequest(url).addOption("--no-playlist")
            )
            val formats = info.formats.orEmpty().mapNotNull { candidate ->
                val extension = candidate.ext?.lowercase()?.takeIf { it in allowedExtensions }
                    ?: return@mapNotNull null
                if (candidate.vcodec != null && candidate.vcodec != "none") return@mapNotNull null
                if (candidate.acodec == "none") return@mapNotNull null
                val formatId = candidate.formatId?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null
                AudioFormatChoice(
                    streamId = FORMAT_PREFIX + formatId,
                    extension = extension,
                    bitrateKbps = candidate.abr.takeIf { it > 0 }
                        ?: candidate.tbr.takeIf { it > 0 },
                    mimeType = requireNotNull(mimeTypes[extension]),
                )
            }.distinctBy { it.streamId }

            // yt-dlp can download sources with no enumerated direct HTTP format
            // (e.g. delegated HLS). An explicit "auto" choice does not claim a codec.
            TrackPreview(
                sourceUrl = url,
                provider = DownloadProvider.SOUNDCLOUD,
                title = info.title?.takeIf(String::isNotBlank) ?: "Bài hát SoundCloud",
                uploader = info.uploader.orEmpty(),
                durationSeconds = info.duration.toLong().coerceAtLeast(0),
                formats = formats.ifEmpty {
                    listOf(
                        AudioFormatChoice(
                            streamId = FORMAT_PREFIX + "bestaudio",
                            extension = "auto",
                            bitrateKbps = null,
                            mimeType = "application/octet-stream",
                        )
                    )
                },
            )
        }

    /**
     * The process ID is the SongMeld task ID; the foreground service can explicitly kill
     * the child process when the user presses Cancel. Cache files are cleaned by its caller.
     */
    suspend fun download(
        context: Context,
        sourceUrl: String,
        taskId: String,
        requestedFormatId: String?,
        onProgress: (Float) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        if (detectDownloadProvider(sourceUrl) != DownloadProvider.SOUNDCLOUD) {
            throw IllegalArgumentException("Liên kết SoundCloud không hợp lệ.")
        }
        YoutubeDL.init(context.applicationContext)
        val directory = File(context.cacheDir, "songmeld-yt-dlp/$taskId")
        directory.deleteRecursively()
        if (!directory.mkdirs()) throw IOException("Không thể tạo thư mục tải tạm.")
        val formatId = requestedFormatId
            ?.takeIf(::isFallbackFormat)
            ?.removePrefix(FORMAT_PREFIX)
            ?.takeIf(String::isNotBlank)
            ?: "bestaudio"
        val request = YoutubeDLRequest(sourceUrl)
            .addOption("--no-playlist")
            .addOption("--no-mtime")
            .addOption("-f", formatId)
            .addOption("-o", File(directory, "audio.%(ext)s").absolutePath)
        try {
            YoutubeDL.execute(request, taskId) { percent, _, _ ->
                onProgress((percent / 100f).coerceIn(0f, 1f))
            }
            directory.listFiles()
                ?.filter { it.isFile && it.length() > 0 && it.extension.lowercase() in allowedExtensions }
                ?.maxByOrNull(File::length)
                ?: throw IOException("yt-dlp không tạo được tệp âm thanh phù hợp.")
        } catch (exception: Exception) {
            directory.deleteRecursively()
            throw exception
        }
    }

    fun cancel(taskId: String) {
        YoutubeDL.destroyProcessById(taskId)
    }

    fun clearTemporaryFiles(context: Context, taskId: String) {
        File(context.cacheDir, "songmeld-yt-dlp/$taskId").deleteRecursively()
    }

    fun toResolvedAudio(preview: TrackPreview, file: File): ResolvedAudio {
        val extension = file.extension.lowercase()
        val mime = mimeTypes[extension] ?: throw IOException("Định dạng yt-dlp trả về không được hỗ trợ.")
        return ResolvedAudio(
            provider = DownloadProvider.SOUNDCLOUD,
            title = preview.title,
            uploader = preview.uploader,
            streamUrl = "",
            format = AudioFormatChoice(
                streamId = FORMAT_PREFIX + "local",
                extension = extension,
                bitrateKbps = null,
                mimeType = mime,
            ),
        )
    }
}
