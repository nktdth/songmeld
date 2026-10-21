package moe.ktandth.songmeld.ui.songs

import java.text.Collator
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.ensureActive
import moe.ktandth.songmeld.data.model.Song
import kotlin.coroutines.coroutineContext

internal enum class SongSort { TITLE, ARTIST, NEWEST, MODIFIED, OLDEST, LONGEST, SHORTEST }

internal enum class DurationBucket(val minimum: Long, val maximumExclusive: Long?) {
    SHORT(0L, 180_000L),
    MEDIUM(180_000L, 300_000L),
    LONG(300_000L, 600_000L),
    VERY_LONG(600_000L, null);

    fun matches(durationMs: Long): Boolean =
        durationMs >= minimum && (maximumExclusive == null || durationMs < maximumExclusive)
}

/** Independent from how songs are sorted. Filters are applied before searching and sorting. */
internal sealed interface LibraryScope {
    data object AllSongs : LibraryScope
    data class Folder(val path: String) : LibraryScope
    data class Artist(val name: String) : LibraryScope
    data class Genre(val name: String) : LibraryScope
    data class Year(val year: Int) : LibraryScope
    data class Duration(val bucket: DurationBucket) : LibraryScope
}

internal fun LibraryScope.key(): String = when (this) {
    LibraryScope.AllSongs -> "all"
    is LibraryScope.Folder -> "folder:$path"
    is LibraryScope.Artist -> "artist:$name"
    is LibraryScope.Genre -> "genre:$name"
    is LibraryScope.Year -> "year:$year"
    is LibraryScope.Duration -> "duration:${bucket.name}"
}

internal fun parseLibraryScope(key: String): LibraryScope = when {
    key.startsWith("folder:") -> LibraryScope.Folder(key.removePrefix("folder:"))
    key.startsWith("artist:") -> LibraryScope.Artist(key.removePrefix("artist:"))
    key.startsWith("genre:") -> LibraryScope.Genre(key.removePrefix("genre:"))
    key.startsWith("year:") -> key.removePrefix("year:").toIntOrNull()
        ?.let(LibraryScope::Year) ?: LibraryScope.AllSongs
    key.startsWith("duration:") -> DurationBucket.entries
        .firstOrNull { it.name == key.removePrefix("duration:") }
        ?.let(LibraryScope::Duration) ?: LibraryScope.AllSongs
    else -> LibraryScope.AllSongs
}

internal fun LibraryScope.matches(song: Song): Boolean = when (this) {
    LibraryScope.AllSongs -> true
    is LibraryScope.Folder -> song.folderPath == path
    is LibraryScope.Artist -> song.artist == name
    is LibraryScope.Genre -> song.genre == name
    is LibraryScope.Year -> song.year == year
    is LibraryScope.Duration -> bucket.matches(song.durationMs)
}


private val combiningMarks = Regex("\\p{M}+")

/** Search Vietnamese with or without accents; đ is not decomposed by Unicode NFD. */
internal fun normalizeSongSearch(text: String): String =
    combiningMarks.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")
        .replace('đ', 'd').replace('Đ', 'D').lowercase(Locale.ROOT)

/** Runs on Dispatchers.Default, never on the UI thread. Does not mutate MediaStore data. */
internal suspend fun querySongs(
    songs: List<Song>, query: String, sort: SongSort,
    scope: LibraryScope = LibraryScope.AllSongs,
): List<Song> {
    val words = normalizeSongSearch(query).trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val filtered = songs.filter { song ->
        coroutineContext.ensureActive()
        scope.matches(song) && (words.isEmpty() || words.all { word ->
            val haystack = normalizeSongSearch("${song.title} ${song.artist} ${song.album}")
            word in haystack
        })
    }
    coroutineContext.ensureActive()
    val collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"))
    val titleOrder = Comparator<Song> { a, b -> collator.compare(a.title, b.title) }.thenBy { it.id }
    val order = when (sort) {
        SongSort.TITLE -> titleOrder
        SongSort.ARTIST -> Comparator<Song> { a, b -> collator.compare(a.artist, b.artist) }.then(titleOrder)
        SongSort.NEWEST -> compareByDescending<Song> { it.dateAddedSeconds }.then(titleOrder)
        SongSort.MODIFIED -> compareByDescending<Song> { it.dateModifiedSeconds }.then(titleOrder)
        SongSort.OLDEST -> compareBy<Song> { it.dateAddedSeconds }.then(titleOrder)
        SongSort.LONGEST -> compareByDescending<Song> { it.durationMs }.then(titleOrder)
        SongSort.SHORTEST -> compareBy<Song> { it.durationMs }.then(titleOrder)
    }
    return filtered.sortedWith(order)
}
