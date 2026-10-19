package moe.ktandth.songmeld.data.local

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import moe.ktandth.songmeld.R
import java.io.IOException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.data.model.Song

class MediaStoreSongRepository(
    private val context: Context,
) {
    suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        coroutineContext.ensureActive()
        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            buildList {
                while (cursor.moveToNext()) {
                    coroutineContext.ensureActive()
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn).orEmpty().ifBlank { context.getString(R.string.song_unknown_title) }
                    val artist = cursor.getString(artistColumn)
                        .orEmpty()
                        .takeUnless { it == MediaStore.UNKNOWN_STRING }
                        .orEmpty()
                        .ifBlank { context.getString(R.string.song_unknown_artist) }
                    val album = cursor.getString(albumColumn)
                        .orEmpty()
                        .takeUnless { it == MediaStore.UNKNOWN_STRING }
                        .orEmpty()
                    val duration = cursor.getLong(durationColumn).coerceAtLeast(0L)
                    val dateAdded = cursor.getLong(dateAddedColumn).coerceAtLeast(0L)
                    val albumId = cursor.getLong(albumIdColumn).takeIf { it > 0L }
                    val dateModified = cursor.getLong(dateModifiedColumn).coerceAtLeast(0L)

                    add(
                        Song(
                            id = id,
                            uri = ContentUris.withAppendedId(collection, id),
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            dateAddedSeconds = dateAdded,
                            albumId = albumId,
                            dateModifiedSeconds = dateModified,
                        )
                    )
                }
            }
        } ?: throw IOException("MediaStore query returned no cursor")
    }
}
