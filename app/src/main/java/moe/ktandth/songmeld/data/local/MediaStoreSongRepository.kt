package moe.ktandth.songmeld.data.local

import android.content.ContentUris
import android.content.Context
import android.os.Build
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
            MediaStore.Audio.Media.YEAR,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.RELATIVE_PATH
            else MediaStore.Audio.Media.DATA,
        ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(MediaStore.Audio.Media.GENRE)
        } else emptyArray<String>()


        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        coroutineContext.ensureActive()
        val localSongs = context.contentResolver.query(
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
            val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val folderColumn = cursor.getColumnIndex(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.RELATIVE_PATH
                else MediaStore.Audio.Media.DATA
            )
            val genreColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) else -1

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
                    val year = if (yearColumn >= 0 && !cursor.isNull(yearColumn))
                        cursor.getInt(yearColumn).takeIf { it in 1000..3000 } else null
                    val folder = if (folderColumn >= 0) cursor.getString(folderColumn)
                        ?.takeIf { it.isNotBlank() }?.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.trimEnd('/')
                            else it.substringBeforeLast('/', "").trimEnd('/')
                        } else null
                    val genre = if (genreColumn >= 0) cursor.getString(genreColumn)
                        ?.trim()?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                        else null

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
                            folderPath = folder,
                            genre = genre,
                            year = year,
                        )
                    )
                }
            }
        } ?: throw IOException("MediaStore query returned no cursor")

        // Android 8–10 keeps genres in a separate MediaStore collection.
        // A failed or unavailable provider must never hide the song library.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val genreByAudioId = legacyGenreNames()
            localSongs.map { song ->
                if (song.genre == null) song.copy(genre = genreByAudioId[song.id]) else song
            }
        } else localSongs
    }

    private fun legacyGenreNames(): Map<Long, String> {
        val result = mutableMapOf<Long, String>()
        val provider = context.contentResolver
        runCatching {
            provider.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
                null, null, null,
            )?.use { genres ->
                val idIdx = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameIdx = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                while (genres.moveToNext()) {
                    val name = genres.getString(nameIdx)?.takeIf { it.isNotBlank() } ?: continue
                    val genreId = genres.getLong(idIdx)
                    runCatching {
                        provider.query(
                            MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                            arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                            null, null, null,
                        )?.use { members ->
                            val songIdIdx = members.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID)
                            while (members.moveToNext()) {
                                result.putIfAbsent(members.getLong(songIdIdx), name)
                            }
                        }
                    }
                }
            }
        }
        return result
    }
}
