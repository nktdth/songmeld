package moe.ktandth.songmeld.data.local

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.data.model.Song

/**
 * Loads the cover from the audio file first, then tries MediaStore album artwork.
 * Work and downsampling happen on Dispatchers.IO; only visible LazyColumn rows request art.
 * The small bounded bitmap cache avoids decoding the same cover on every scroll.
 */
internal class LocalSongArtworkLoader(context: Context) {
    private val resolver = context.applicationContext.contentResolver
    private val appContext = context.applicationContext

    // Cache sizes are in KiB; leave memory for the rest of the application.
    private val maxCacheKiB = (Runtime.getRuntime().maxMemory() / 16L / 1024L)
        .coerceIn(2L * 1024L, 16L * 1024L).toInt()
    private val covers = object : LruCache<String, Bitmap>(maxCacheKiB) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }
    private val missingCovers = LruCache<String, Boolean>(256)

    suspend fun load(song: Song): Bitmap? = withContext(Dispatchers.IO) {
        val key = "${song.id}:${song.dateModifiedSeconds}:${song.albumId}"
        synchronized(covers) {
            covers.get(key)?.let { return@withContext it }
            if (missingCovers.get(key) == true) return@withContext null
        }

        val bitmap = embeddedArtwork(song.uri) ?: albumArtwork(song.albumId)
        synchronized(covers) {
            if (bitmap != null) {
                missingCovers.remove(key)
                covers.put(key, bitmap)
            } else {
                missingCovers.put(key, true)
            }
        }
        bitmap
    }

    private fun embeddedArtwork(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(appContext, uri)
            retriever.embeddedPicture?.let(::decodeSampledBytes)
        } catch (_: Exception) {
            // Missing/unsupported metadata or inaccessible media: try the album's art.
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // Some media codecs fail while releasing their resources.
            }
        }
    }

    private fun albumArtwork(albumId: Long?): Bitmap? {
        if (albumId == null) return null
        val albumUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(albumUri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
            }
            resolver.openInputStream(albumUri)?.use { BitmapFactory.decodeStream(it, null, options) }
        } catch (_: Exception) {
            // Some Android versions and media providers don't expose this URI.
            null
        }
    }

    private fun decodeSampledBytes(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= COVER_SIZE_PX && height / (sample * 2) >= COVER_SIZE_PX) {
            sample *= 2
        }
        return sample
    }

    private companion object {
        const val COVER_SIZE_PX = 256
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}
