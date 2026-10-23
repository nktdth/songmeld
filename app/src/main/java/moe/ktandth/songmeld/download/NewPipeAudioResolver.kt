package moe.ktandth.songmeld.download

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo

data class ResolvedAudio(
    val provider: DownloadProvider,
    val title: String,
    val uploader: String,
    val streamUrl: String,
    val format: AudioFormatChoice,
)

/** NewPipe Extractor handles source parsing; SongMeld downloads only direct audio streams. */
object NewPipeAudioResolver {
    private val lock = Any()
    private var initialized = false
    private val allowedExtensions = setOf("m4a", "mp3", "webm", "opus", "ogg", "aac")

    private fun initialize() {
        synchronized(lock) {
            if (!initialized) {
                NewPipe.init(SimpleHttpDownloader)
                initialized = true
            }
        }
    }

    suspend fun preview(url: String): TrackPreview = withContext(Dispatchers.IO) {
        val provider = detectDownloadProvider(url)
            ?: throw IllegalArgumentException("Chỉ hỗ trợ liên kết bài hát HTTPS từ YouTube, YouTube Music và SoundCloud.")
        initialize()
        // YouTube Music and YouTube share a source video ID. Search is a separate future feature.
        val resolvedUrl = if (provider == DownloadProvider.YOUTUBE_MUSIC)
            url.replaceFirst("https://music.youtube.com/", "https://www.youtube.com/")
        else url
        val info = StreamInfo.getInfo(resolvedUrl)
        val audio = supportedStreams(info.audioStreams)
        if (audio.isEmpty()) {
            throw IOException("Nguồn này không cung cấp luồng âm thanh trực tiếp được hỗ trợ.")
        }
        TrackPreview(
            sourceUrl = url,
            provider = provider,
            title = info.name.ifBlank { "Bài hát" },
            uploader = info.uploaderName.orEmpty(),
            durationSeconds = info.duration.coerceAtLeast(0),
            formats = audio.map { it.second },
        )
    }

    suspend fun resolve(preview: TrackPreview, requestedFormatId: String?): ResolvedAudio =
        withContext(Dispatchers.IO) {
            initialize()
            val url = if (preview.provider == DownloadProvider.YOUTUBE_MUSIC)
                preview.sourceUrl.replaceFirst(
                    "https://music.youtube.com/", "https://www.youtube.com/"
                ) else preview.sourceUrl
            val info = StreamInfo.getInfo(url)
            val streams = supportedStreams(info.audioStreams)
            val match = streams.firstOrNull { it.second.streamId == requestedFormatId }
                ?: streams.maxByOrNull { it.second.bitrateKbps ?: 0 }
                ?: throw IOException("Không tìm thấy định dạng âm thanh khả dụng.")
            ResolvedAudio(
                provider = preview.provider,
                title = info.name.ifBlank { preview.title },
                uploader = info.uploaderName.orEmpty(),
                streamUrl = match.first.content,
                format = match.second,
            )
        }

    private fun supportedStreams(all: List<AudioStream>): List<Pair<AudioStream, AudioFormatChoice>> =
        all.mapNotNull { stream ->
            val extension = stream.format?.suffix?.lowercase() ?: return@mapNotNull null
            if (!stream.isUrl || !stream.content.startsWith("https://") ||
                stream.deliveryMethod != DeliveryMethod.PROGRESSIVE_HTTP ||
                extension !in allowedExtensions
            ) return@mapNotNull null
            stream to AudioFormatChoice(
                streamId = stream.id,
                extension = extension,
                bitrateKbps = stream.averageBitrate.takeIf { it > 0 },
                mimeType = stream.format?.mimeType ?: "application/octet-stream",
            )
        }.distinctBy { it.second.streamId }
}

/**
 * Minimal request adapter used exclusively by the extractor.
 * Requests originate from a known media service; do not log signed URLs or request bodies.
 */
private object SimpleHttpDownloader : Downloader() {
    override fun execute(request: Request): Response {
        val connection = (URL(request.url()).openConnection() as HttpURLConnection).apply {
            requestMethod = request.httpMethod()
            connectTimeout = 15_000
            readTimeout = 25_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
            request.headers().forEach { (name, values) ->
                values.forEach { value -> addRequestProperty(name, value) }
            }
            request.dataToSend()?.let { data ->
                doOutput = true
                outputStream.use { it.write(data) }
            }
        }
        return try {
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val headers = connection.headerFields.entries.mapNotNull { (name, values) ->
                if (name == null || values == null) null else name to values
            }.toMap()
            Response(code, connection.responseMessage.orEmpty(), headers, body, connection.url.toString())
        } finally {
            connection.disconnect()
        }
    }
}
