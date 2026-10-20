package moe.ktandth.songmeld.ui.songs

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.songMeldIconShape
import moe.ktandth.songmeld.data.model.Song
import moe.ktandth.songmeld.data.local.LocalSongArtworkLoader
import moe.ktandth.songmeld.ui.theme.SongMeldCodeStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SongsScreen(
    songs: List<Song>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    refreshFailed: Boolean = false,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val preferences = remember(context) {
        context.applicationContext.getSharedPreferences("songmeld_library", Context.MODE_PRIVATE)
    }
    var sort by rememberSaveable {
        mutableStateOf(SongSort.entries.firstOrNull {
            it.name == preferences.getString("sort", null)
        } ?: SongSort.TITLE)
    }
    var sortOpen by rememberSaveable { mutableStateOf(false) }
    var selectedSongId by rememberSaveable { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    var previousView by rememberSaveable { mutableStateOf(query + "\u0000" + sort.name) }
    LaunchedEffect(query, sort) {
        val view = query + "\u0000" + sort.name
        if (previousView != view) {
            listState.scrollToItem(0)
            previousView = view
        }
    }
    val artworkLoader = remember(context.applicationContext) {
        LocalSongArtworkLoader(context.applicationContext)
    }
    // Preserve the same view during a rescan, but never display another query's results.
    val queryResult by produceState<LibraryQueryResult?>(null, songs, query, sort) {
        val found = withContext(Dispatchers.Default) { querySongs(songs, query, sort) }
        value = LibraryQueryResult(query, sort, found)
    }
    val results = queryResult?.takeIf { it.query == query && it.sort == sort }?.songs

    Column(modifier = modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.songs_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onRetry, enabled = !isLoading && !isRefreshing) {
                SongMeldIcon(SongMeldIconId.Refresh, stringResource(R.string.library_refresh))
            }
            IconButton(onClick = { focusManager.clearFocus(); sortOpen = true },
                enabled = !isLoading && errorMessage == null && songs.isNotEmpty()) {
                SongMeldIcon(SongMeldIconId.Sort, stringResource(R.string.library_sort))
            }
        }
        val summary = when {
            isLoading -> stringResource(R.string.loading_music)
            isRefreshing -> stringResource(R.string.library_refreshing)
            errorMessage != null -> stringResource(R.string.load_error_title)
            query.isNotBlank() && results != null -> stringResource(R.string.library_result_count, results.size, songs.size)
            else -> pluralStringResource(R.plurals.song_count, songs.size, songs.size)
        }
        val summaryWithSort = if (!isLoading && errorMessage == null && songs.isNotEmpty()) {
            stringResource(R.string.library_summary_sort, summary, stringResource(sort.label()))
        } else summary
        Text(summaryWithSort, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SongSearchBar(
            query = query,
            onQueryChange = { query = it },
            focusManager = focusManager,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp))
            Spacer(Modifier.height(8.dp))
        }
        if (refreshFailed) {
            Surface(shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(stringResource(R.string.library_refresh_error), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
        when {
            isLoading -> LoadingState()
            errorMessage != null -> ErrorState(onRetry = onRetry)
            songs.isEmpty() -> EmptyLibraryState(onRefresh = onRetry, refreshing = isRefreshing)
            results == null -> LoadingState()
            results.isEmpty() -> EmptySearchState(query = query, onClear = { query = ""; focusManager.clearFocus() })
            else -> SongList(songs = results, artworkLoader = artworkLoader, state = listState,
                sort = sort, onSongClick = { focusManager.clearFocus(); selectedSongId = it.id })
        }
    }
    if (sortOpen) {
        SongSortDialog(selected = sort, onDismiss = { sortOpen = false }, onSelected = {
            sort = it
            preferences.edit().putString("sort", it.name).apply()
            sortOpen = false
        })
    }
    val selectedSong = songs.firstOrNull { it.id == selectedSongId }
    if (selectedSong != null && !isLoading && errorMessage == null) {
        SongDetailsSheet(song = selectedSong, onDismiss = { selectedSongId = null })
    }
}

@Composable
private fun SongSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    val contextSearchLabel = stringResource(R.string.search_songs_hint)
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = contextSearchLabel },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text(stringResource(R.string.search_songs_hint)) },
        leadingIcon = {
            SongMeldIcon(
                id = SongMeldIconId.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    SongMeldIcon(
                        id = SongMeldIconId.Close,
                        contentDescription = stringResource(R.string.clear_search),
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

@Composable
private fun SongList(
    songs: List<Song>,
    artworkLoader: LocalSongArtworkLoader,
    state: LazyListState,
    sort: SongSort,
    onSongClick: (Song) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            // The floating scroll action must not cover the last library entry.
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(
                items = songs,
                key = { _, song -> song.id },
                contentType = { _, _ -> "song" },
            ) { index, song ->
                SongRow(
                    song = song,
                    artworkLoader = artworkLoader,
                    onClick = { onSongClick(song) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(songItemShape(index, songs.size))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                )
            }
        }
        SongsFastScrollbar(
            listState = state,
            songs = songs,
            sort = sort,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
        SongsScrollToTopFab(
            listState = state,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 18.dp),
        )
    }
}

/** Keep the outer 24dp corners while gently rounding the seams between songs. */
private fun songItemShape(index: Int, count: Int): RoundedCornerShape = when {
    count == 1 -> RoundedCornerShape(20.dp)
    index == 0 -> RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 6.dp,
        bottomEnd = 6.dp,
    )
    index == count - 1 -> RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 6.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp,
    )
    else -> RoundedCornerShape(6.dp)
}

@Composable
private fun SongRow(
    song: Song,
    artworkLoader: LocalSongArtworkLoader,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailsLabel = stringResource(R.string.song_details)
    Row(
        modifier = modifier
            .clickable(onClickLabel = detailsLabel, onClick = onClick)
            .heightIn(min = 68.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArtwork(song = song, artworkLoader = artworkLoader)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = song.artist,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatDuration(song.durationMs),
                    style = SongMeldCodeStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SongArtwork(
    song: Song,
    artworkLoader: LocalSongArtworkLoader,
) {
    val artwork by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = song.id,
        key2 = song.dateModifiedSeconds,
        key3 = song.albumId,
    ) {
        value = artworkLoader.load(song)
    }
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        val cover = artwork
        if (cover != null) {
            Image(
                bitmap = cover.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            SongMeldIcon(
                id = SongMeldIconId.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                size = 28.dp,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading_music),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(onRefresh: () -> Unit, refreshing: Boolean) {
    LibraryMessage(
        icon = SongMeldIconId.LibraryMusic,
        title = stringResource(R.string.empty_library_title),
        body = stringResource(R.string.empty_library_body),
        action = { FilledTonalButton(onClick = onRefresh, enabled = !refreshing) { Text(stringResource(R.string.library_refresh)) } },
    )
}

@Composable
private fun EmptySearchState(query: String, onClear: () -> Unit) {
    LibraryMessage(
        icon = SongMeldIconId.SearchOff,
        title = stringResource(R.string.empty_search_title),
        body = stringResource(R.string.empty_search_body, query),
        action = { FilledTonalButton(onClick = onClear) { Text(stringResource(R.string.clear_search)) } },
    )
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.load_error_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.load_error_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            FilledTonalButton(onClick = onRetry) {
                SongMeldIcon(SongMeldIconId.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun LibraryMessage(
    icon: SongMeldIconId,
    title: String,
    body: String,
    action: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = songMeldIconShape(),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    SongMeldIcon(
                        id = icon,
                        contentDescription = null,
                        size = 44.dp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
            action()
        }
    }
}

internal fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L

    return if (hours > 0L) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
