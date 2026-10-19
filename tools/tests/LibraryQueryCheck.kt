package moe.ktandth.songmeld.ui.songs

import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.ktandth.songmeld.data.model.Song

private fun song(id: Long, title: String, artist: String = "Nghệ sĩ", album: String = "",
                 duration: Long = 0, added: Long = 0) =
    Song(id, Uri.parse("content://media/$id"), title, artist, album, duration, added)

fun main() = runBlocking {
    check(normalizeSongSearch("ĐƯỜNG đến Trái Tim") == "duong den trai tim")
    check(normalizeSongSearch("Ha\u0300 No\u0323\u0302i") == normalizeSongSearch("Hà Nội"))
    val songs = listOf(
        song(3, "Cơn mưa", "Đen", "Hà Nội", 240000, 30),
        song(1, "Ánh nắng", "Bích", "Mùa hè", 120000, 10),
        song(2, "Bình yên", "An", "Đường về", 360000, 20),
    )
    check(querySongs(songs, "DEN ha noi", SongSort.TITLE).map { it.id } == listOf(3L))
    check(querySongs(songs, "  duong   BINH  ", SongSort.TITLE).map { it.id } == listOf(2L))
    check(querySongs(songs, "mua", SongSort.TITLE).map { it.id }.toSet() == setOf(1L, 3L))
    check(querySongs(songs, "khong ton tai", SongSort.NEWEST).isEmpty())
    check(querySongs(songs, "   ", SongSort.TITLE).map { it.id } == listOf(1L, 2L, 3L))
    check(querySongs(songs, "", SongSort.ARTIST).map { it.id } == listOf(2L, 1L, 3L))
    check(querySongs(songs, "", SongSort.NEWEST).map { it.id } == listOf(3L, 2L, 1L))
    check(querySongs(songs, "", SongSort.LONGEST).map { it.id } == listOf(2L, 3L, 1L))
    val ties = listOf(song(2, "Same"), song(1, "Same"))
    SongSort.entries.forEach { sort -> check(querySongs(ties, "", sort).map { it.id } == listOf(1L, 2L)) }
    check(querySongs(emptyList(), "test", SongSort.TITLE).isEmpty())
    check(songs.map { it.id } == listOf(3L, 1L, 2L)) // Sorting must not reorder the source list.
    var cancelled = false
    val job = launch {
        cancel()
        try { querySongs(songs, "mua", SongSort.TITLE) }
        catch (_: CancellationException) { cancelled = true }
    }
    job.join()
    check(cancelled)
    println("Library query checks passed: Vietnamese, token search, sort order, ties, empty input and cancellation.")
}
