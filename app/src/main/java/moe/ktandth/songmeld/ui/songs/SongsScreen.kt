package moe.ktandth.songmeld.ui.songs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.model.Song
import java.util.Locale

@Composable
fun SongsScreen(
    songs: List<Song>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val filteredSongs = remember(songs, query) {
        if (query.isBlank()) {
            songs
        } else {
            val needle = query.trim()
            songs.filter { song ->
                song.title.contains(needle, ignoreCase = true) ||
                    song.artist.contains(needle, ignoreCase = true) ||
                    song.album.contains(needle, ignoreCase = true)
            }
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SongsHeader(songCount = songs.size)
            SongSearchBar(
                query = query,
                onQueryChange = { query = it },
                focusManager = focusManager,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> LoadingState()
                errorMessage != null -> ErrorState(onRetry = onRetry)
                songs.isEmpty() -> EmptyLibraryState()
                filteredSongs.isEmpty() -> EmptySearchState(query = query)
                else -> SongList(songs = filteredSongs)
            }
        }
    }
}

@Composable
private fun SongsHeader(songCount: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.songs_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (songCount != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pluralStringResource(R.plurals.song_count, songCount, songCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SongSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        placeholder = { Text(stringResource(R.string.search_songs_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
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
private fun SongList(songs: List<Song>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(
            items = songs,
            key = { it.id },
        ) { song ->
            SongRow(song = song)
            HorizontalDivider(
                modifier = Modifier.padding(start = 92.dp, end = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
        }
    }
}

@Composable
private fun SongRow(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = song.secondaryText(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionArtwork() {
    Box(
        modifier = Modifier.size(128.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(44.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            shadowElevation = 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
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
private fun EmptyLibraryState() {
    LibraryMessage(
        icon = Icons.Rounded.LibraryMusic,
        title = stringResource(R.string.empty_library_title),
        body = stringResource(R.string.empty_library_body),
    )
}

@Composable
private fun EmptySearchState(query: String) {
    LibraryMessage(
        icon = Icons.Rounded.SearchOff,
        title = stringResource(R.string.empty_search_title),
        body = stringResource(R.string.empty_search_body, query),
    )
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
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
                Icon(Icons.Rounded.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun LibraryMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Song.secondaryText(): String = buildString {
    append(artist)
    if (album.isNotBlank()) {
        append(" • ")
        append(album)
    }
}

private fun formatDuration(durationMs: Long): String {
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
