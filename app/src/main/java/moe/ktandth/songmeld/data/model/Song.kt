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
)
