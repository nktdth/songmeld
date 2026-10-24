package moe.ktandth.songmeld.download

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.DocumentsContract
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Export an actual audio file, NOT a Media3 playback cache. Preserve the source
 * container/codec; in particular, never rename a WebM/Opus stream to .mp3.
 */
internal data class SavedAudio(val uri: Uri, val folderLabel: String)

internal object AudioFileSaver {
    suspend fun save(
        context: Context,
        audio: ResolvedAudio,
        onProgress: (Long, Long?) -> Unit,
    ): SavedAudio {
        val filename = safeFilename(audio.title) + "." + audio.format.extension
        val preferences = DownloadPreferences(context.applicationContext)
        val customTree = preferences.customTreeUri?.let(Uri::parse)
        // SAF roots are user-chosen; their content is saved directly in that folder.
        val sourceFolder = if (preferences.folderPolicy == DownloadFolderPolicy.BY_SOURCE)
            "/" + audio.provider.displayName.replace(" ", "") else ""
        val connection = (URL(audio.streamUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
            instanceFollowRedirects = true
        }
        var outputUri: Uri? = null
        val usesMediaStore = customTree == null && Build.VERSION.SDK_INT >= 29
        var folderLabel = if (customTree != null) "Thư mục đã chọn" else
            Environment.DIRECTORY_MUSIC + "/SongMeld" + sourceFolder
        var published = false
        var legacyFile: File? = null
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IOException(
                "Nguồn phát từ chối yêu cầu (HTTP " + code + "). Vui lòng thử lại sau."
            )
            val contentLength = connection.contentLengthLong.takeIf { it > 0 }
            val resolver = context.contentResolver
            if (customTree != null) {
                val directoryId = DocumentsContract.getTreeDocumentId(customTree)
                val directory = DocumentsContract.buildDocumentUriUsingTree(customTree, directoryId)
                outputUri = DocumentsContract.createDocument(
                    resolver, directory, audio.format.mimeType, filename
                ) ?: throw IOException("Không thể tạo tệp trong thư mục đã chọn.")
            } else if (usesMediaStore) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Audio.Media.MIME_TYPE, audio.format.mimeType)
                    put(MediaStore.Audio.Media.TITLE, audio.title)
                    put(MediaStore.Audio.Media.ARTIST, audio.uploader)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/SongMeld" + sourceFolder)
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                // Android OEM MediaProviders often reject audio/webm or audio/opus on the
                // AUDIO_MEDIA collection: IllegalArgumentException("Unsupported MIME type ...").
                // Generic Downloads accepts media files without forcing a fake MP3 MIME or suffix.
                // Other insert errors are deliberately not swallowed.
                outputUri = try {
                    resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                } catch (unsupported: IllegalArgumentException) {
                    if (!MediaStoreAudioFallback.shouldUseDownloads(unsupported.message)) {
                        throw unsupported
                    }
                    folderLabel = Environment.DIRECTORY_DOWNLOADS + "/SongMeld" + sourceFolder
                    val genericValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, audio.format.mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, folderLabel)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, genericValues)
                        ?: throw IOException("Không thể lưu định dạng này trong Downloads/SongMeld.")
                } ?: throw IOException("Không thể tạo tệp âm thanh trong MediaStore.")
            } else {
                // API 26–28: app-specific external music directory, no broad storage permission.
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    ?: throw IOException("Bộ nhớ ngoài không khả dụng.")
                val appDir = File(dir, "SongMeld" + sourceFolder).apply { mkdirs() }
                folderLabel = "Thư mục nhạc riêng của SongMeld"
                legacyFile = File(appDir, filename)
                if (legacyFile!!.exists()) {
                    legacyFile = File(appDir, System.currentTimeMillis().toString() + "-" + filename)
                }
                outputUri = Uri.fromFile(legacyFile)
            }
            val destination = requireNotNull(outputUri)
            val target = if (legacyFile != null) legacyFile!!.outputStream()
                else resolver.openOutputStream(destination, "w")
                    ?: throw IOException("Không thể mở tệp đích.")
            var written = 0L
            var lastUpdateAt = 0L
            connection.inputStream.use { input ->
                target.buffered().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        written += count
                        val now = android.os.SystemClock.elapsedRealtime()
                        if (now - lastUpdateAt > 350L) {
                            onProgress(written, contentLength)
                            lastUpdateAt = now
                        }
                    }
                }
            }
            if (written == 0L) throw IOException("Tệp nhận được rỗng.")
            if (contentLength != null && written < contentLength) {
                throw IOException("Tệp tải về không đầy đủ.")
            }
            if (usesMediaStore) {
                resolver.update(destination, ContentValues().apply {
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                }, null, null)
            } else {
                legacyFile?.let {
                    MediaScannerConnection.scanFile(
                        context, arrayOf(it.absolutePath), arrayOf(audio.format.mimeType), null
                    )
                }
            }
            published = true
            onProgress(written, contentLength ?: written)
            return SavedAudio(destination, folderLabel)
        } finally {
            connection.disconnect()
            if (!published) {
                if (legacyFile != null) legacyFile?.delete()
                else outputUri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
            }
        }
    }

    private fun safeFilename(title: String): String {
        val cleaned = title.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '.')
            .take(95)
        return cleaned.ifBlank { "SongMeld audio" }
    }
}
