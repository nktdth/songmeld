package moe.ktandth.songmeld.ui.songs

import moe.ktandth.songmeld.data.model.Song

/** Keep the last successful snapshot visible while rescanning or recovering from a refresh error. */
internal data class LibraryLoadState(
    val songs: List<Song> = emptyList(),
    val loading: Boolean = true,
    val hasLoaded: Boolean = false,
    val error: Boolean = false,
) {
    fun refreshing() = copy(loading = true, error = false)
    fun loaded(songs: List<Song>) = LibraryLoadState(songs, loading = false, hasLoaded = true)
    fun failed() = copy(loading = false, error = true)
}

/** Attach query identity to asynchronous results; old queries must not flash under new text. */
internal data class LibraryQueryResult(
    val query: String,
    val sort: SongSort,
    val scope: LibraryScope,
    val songs: List<Song>,
)
