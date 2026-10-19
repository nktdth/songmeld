package moe.ktandth.songmeld.ui.songs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.model.Song
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@StringRes
internal fun SongSort.label(): Int = when (this) {
    SongSort.TITLE -> R.string.library_sort_title
    SongSort.ARTIST -> R.string.library_sort_artist
    SongSort.NEWEST -> R.string.library_sort_newest
    SongSort.LONGEST -> R.string.library_sort_longest
}

@Composable
internal fun SongSortDialog(selected: SongSort, onDismiss: () -> Unit, onSelected: (SongSort) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.library_sort)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SongSort.entries.forEach { sort ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(selected = selected == sort, role = Role.RadioButton, onClick = { onSelected(sort) })
                            .heightIn(min = 56.dp).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = selected == sort, onClick = null)
                        Text(stringResource(sort.label()), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.about_close)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongDetailsSheet(song: Song, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.song_details), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Text(song.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            SelectionContainer {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SongDetail(R.string.song_artist, song.artist)
                        SongDetail(R.string.song_album, song.album.ifBlank { stringResource(R.string.song_unknown_album) })
                        SongDetail(R.string.song_duration, formatDuration(song.durationMs))
                        val added = remember(song.dateAddedSeconds) {
                            if (song.dateAddedSeconds <= 0) null else runCatching {
                                Instant.ofEpochSecond(song.dateAddedSeconds).atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            }.getOrNull()
                        }
                        if (added != null) SongDetail(R.string.song_date_added, added)
                    }
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.about_close))
            }
        }
    }
}

@Composable
private fun SongDetail(@StringRes label: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
