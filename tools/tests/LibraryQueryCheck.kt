package moe.ktandth.songmeld.ui.songs

import android.net.Uri
import moe.ktandth.songmeld.data.local.artworkSampleSize
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
    val loading = LibraryLoadState()
    check(loading.loading && !loading.hasLoaded)
    check(loading.failed().error && !loading.failed().loading)
    val loaded = loading.loaded(songs)
    val refreshing = loaded.refreshing()
    check(refreshing.songs === songs && refreshing.loading && refreshing.hasLoaded)
    val failedRefresh = refreshing.failed()
    check(failedRefresh.songs === songs && failedRefresh.error && !failedRefresh.loading)
    val emptySuccess = failedRefresh.loaded(emptyList())
    check(emptySuccess.hasLoaded && emptySuccess.songs.isEmpty() && !emptySuccess.error)
    val emptyRefresh = emptySuccess.refreshing().failed()
    check(emptyRefresh.hasLoaded && emptyRefresh.error)
    for ((width, height) in listOf(100 to 100, 512 to 512, 513 to 20,
        20000 to 200, 200 to 20000, 5000 to 5000, Int.MAX_VALUE to 1)) {
        val sample = artworkSampleSize(width, height)
        check(sample > 0 && sample and (sample - 1) == 0)
        check(maxOf(width, height) / sample <= 512)
    }
    check(artworkSampleSize(-1, 0) == 1)
    println("Library refresh preservation and artwork sampling checks passed.")
    println("Library query checks passed: Vietnamese, token search, sort order, ties, empty input and cancellation.")
}
