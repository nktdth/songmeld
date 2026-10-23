package moe.ktandth.songmeld.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dateAddedSeconds: Long,
    val albumId: Long? = null,
    val dateModifiedSeconds: Long = 0L,
    /** Parent directory for folder-scoped library views. */
    val folderPath: String? = null,
    val genre: String? = null,
    val year: Int? = null,
)
