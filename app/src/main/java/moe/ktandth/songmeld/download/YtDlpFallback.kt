package moe.ktandth.songmeld.download

import android.content.Context
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified primary audio download engine, inspired by Seal and SongMeld-v1.
 *
 * yt-dlp handles SoundCloud HLS and YouTube/YT Music media extraction as well as
 * download/fragment assembly. A source audio file is imported through SongMeld's
 * existing MediaStore/SAF writer without transcoding or a misleading extension.
 * NewPipe remains a direct-stream fallback managed by SongAudioResolver.
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
            val provider = detectDownloadProvider(url)
                ?: throw IllegalArgumentException("Chỉ hỗ trợ liên kết bài hát YouTube, YouTube Music hoặc SoundCloud.")
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

            // A native HLS/manifest format might not be present in the list of direct
            // downloadable files. yt-dlp still understands this audio-only selector.
            // Always expose it as an option, especially for SoundCloud HLS.
            val automatic = AudioFormatChoice(
                streamId = FORMAT_PREFIX + "bestaudio",
                extension = "auto",
                bitrateKbps = null,
                mimeType = "application/octet-stream",
            )
            TrackPreview(
                sourceUrl = url,
                provider = provider,
                title = info.title?.takeIf(String::isNotBlank) ?: "Bài hát",
                uploader = info.uploader.orEmpty(),
                durationSeconds = info.duration.toLong().coerceAtLeast(0),
                formats = formats + automatic,
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
        if (detectDownloadProvider(sourceUrl) == null) {
            throw IllegalArgumentException("Liên kết nhạc không hợp lệ.")
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
            provider = preview.provider,
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
