package moe.ktandth.songmeld.download

import android.content.Context
import java.io.IOException
import kotlinx.coroutines.CancellationException

/**
 * One entry point for preview across YouTube, YouTube Music and SoundCloud.
 *
 * yt-dlp is the preferred extractor because it handles segmented sources (HLS)
 * that NewPipe's Progressive-HTTP-only downloader cannot handle. If yt-dlp cannot
 * inspect a supported URL, keep the smaller NewPipe direct-stream path available.
 */
object SongAudioResolver {
    suspend fun preview(context: Context, url: String): TrackPreview {
        require(detectDownloadProvider(url) != null) {
            "Chỉ hỗ trợ liên kết bài hát YouTube, YouTube Music hoặc SoundCloud."
        }
        return try {
            YtDlpFallback.preview(context, url)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (primary: Exception) {
            try {
                // null avoids the old NewPipe -> yt-dlp fallback cycle for SoundCloud.
                NewPipeAudioResolver.preview(url, context = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (secondary: Exception) {
                val error = IOException(
                    "Không đọc được thông tin âm thanh từ nguồn này bằng yt-dlp hoặc NewPipe. " +
                        "Nếu bài hát có giới hạn truy cập hoặc nguồn vừa thay đổi, " +
                        "hãy thử bài khác hoặc cập nhật bộ tải.",
                    primary,
                )
                error.addSuppressed(secondary)
                throw error
            }
        }
    }
}
