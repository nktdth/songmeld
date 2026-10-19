package moe.ktandth.songmeld.ui.songs

import java.text.Collator
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.ensureActive
import moe.ktandth.songmeld.data.model.Song
import kotlin.coroutines.coroutineContext

internal enum class SongSort { TITLE, ARTIST, NEWEST, LONGEST }

private val combiningMarks = Regex("\\p{M}+")

/** Search Vietnamese with or without accents; đ is not decomposed by Unicode NFD. */
internal fun normalizeSongSearch(text: String): String =
    combiningMarks.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")
        .replace('đ', 'd').replace('Đ', 'D').lowercase(Locale.ROOT)

/** Runs on Dispatchers.Default, never on the UI thread. Does not mutate MediaStore data. */
internal suspend fun querySongs(songs: List<Song>, query: String, sort: SongSort): List<Song> {
    val words = normalizeSongSearch(query).trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val filtered = if (words.isEmpty()) songs else songs.filter { song ->
        coroutineContext.ensureActive()
        val haystack = normalizeSongSearch("${song.title} ${song.artist} ${song.album}")
        words.all { it in haystack }
    }
    coroutineContext.ensureActive()
    val collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"))
    val titleOrder = Comparator<Song> { a, b -> collator.compare(a.title, b.title) }.thenBy { it.id }
    val order = when (sort) {
        SongSort.TITLE -> titleOrder
        SongSort.ARTIST -> Comparator<Song> { a, b -> collator.compare(a.artist, b.artist) }.then(titleOrder)
        SongSort.NEWEST -> compareByDescending<Song> { it.dateAddedSeconds }.then(titleOrder)
        SongSort.LONGEST -> compareByDescending<Song> { it.durationMs }.then(titleOrder)
    }
    return filtered.sortedWith(order)
}
