package moe.ktandth.songmeld.ui.songs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.data.model.Song
import moe.ktandth.songmeld.ui.icons.SongMeldIcon
import moe.ktandth.songmeld.ui.icons.SongMeldIconId
import moe.ktandth.songmeld.ui.settings.LocalDecorativeMotionEnabled

internal enum class SourcePage { ROOT, FOLDERS, DURATION, GENRES, ARTISTS, YEARS }

internal data class SourceEntry(
    val scope: LibraryScope,
    val label: String,
    val count: Int,
    val subtitle: String? = null,
)

internal fun sourceEntries(songs: List<Song>, page: SourcePage): List<SourceEntry> {
    val grouped: List<SourceEntry> = when (page) {
        SourcePage.ROOT -> emptyList()
        SourcePage.FOLDERS -> songs.filter { !it.folderPath.isNullOrBlank() }
            .groupBy { it.folderPath.orEmpty() }
            .map { (folder, members) ->
                SourceEntry(LibraryScope.Folder(folder),
                    folder.substringAfterLast('/').ifBlank { folder }, members.size, folder)
            }
        SourcePage.ARTISTS -> songs.filter { it.artist.isNotBlank() }
            .groupBy { it.artist }
            .map { (artist, members) -> SourceEntry(LibraryScope.Artist(artist), artist, members.size) }
        SourcePage.GENRES -> songs.filter { !it.genre.isNullOrBlank() }
            .groupBy { it.genre.orEmpty() }
            .map { (genre, members) -> SourceEntry(LibraryScope.Genre(genre), genre, members.size) }
        SourcePage.YEARS -> songs.filter { it.year != null }
            .groupBy { it.year ?: 0 }
            .map { (year, members) ->
                SourceEntry(LibraryScope.Year(year), year.toString(), members.size)
            }
        SourcePage.DURATION -> DurationBucket.entries.mapNotNull { bucket ->
            val count = songs.count { bucket.matches(it.durationMs) }
            if (count > 0) SourceEntry(LibraryScope.Duration(bucket), bucket.name, count) else null
        }
    }
    return if (page == SourcePage.YEARS) grouped.sortedByDescending { it.label.toIntOrNull() ?: 0 }
    else if (page == SourcePage.DURATION) grouped
    else grouped.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
}

@Composable
internal fun LibraryScope.displayLabel(): String = when (this) {
    LibraryScope.AllSongs -> stringResource(R.string.library_source_all)
    is LibraryScope.Folder -> path.substringAfterLast('/').ifBlank { path }
    is LibraryScope.Artist -> name
    is LibraryScope.Genre -> name
    is LibraryScope.Year -> year.toString()
    is LibraryScope.Duration -> stringResource(bucket.label())
}

internal fun DurationBucket.label(): Int = when (this) {
    DurationBucket.SHORT -> R.string.library_duration_short
    DurationBucket.MEDIUM -> R.string.library_duration_medium
    DurationBucket.LONG -> R.string.library_duration_long
    DurationBucket.VERY_LONG -> R.string.library_duration_very_long
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibrarySourceSheet(
    songs: List<Song>,
    selected: LibraryScope,
    onSelect: (LibraryScope) -> Unit,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(SourcePage.ROOT) }
    val motion = LocalDecorativeMotionEnabled.current
    BackHandler(enabled = page != SourcePage.ROOT) { page = SourcePage.ROOT }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        AnimatedContent(
            targetState = page,
            contentKey = { it },
            transitionSpec = {
                if (!motion) EnterTransition.None togetherWith ExitTransition.None
                else {
                    val back = targetState == SourcePage.ROOT
                    (slideInHorizontally(animationSpec = tween(220)) { if (back) -it / 5 else it / 5 } +
                        fadeIn(animationSpec = tween(150))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(220)) {
                            if (back) it / 5 else -it / 5
                        } + fadeOut(animationSpec = tween(130)))
                }
            },
            label = "Library source selector",
        ) { current ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (current != SourcePage.ROOT) {
                        TextButton(onClick = { page = SourcePage.ROOT }) {
                            SongMeldIcon(SongMeldIconId.ArrowBack, null, size = 22.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.settings_back))
                        }
                    }
                    Text(
                        text = if (current == SourcePage.ROOT) stringResource(R.string.library_source_title)
                        else stringResource(current.label()),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                if (current == SourcePage.ROOT) {
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                        item {
                            SourceSheetRow(
                                text = stringResource(R.string.library_source_all),
                                count = songs.size,
                                icon = SongMeldIconId.LibraryMusic,
                                selected = selected == LibraryScope.AllSongs,
                                onClick = { onSelect(LibraryScope.AllSongs) },
                            )
                        }
                        items(SourcePage.entries.filter { it != SourcePage.ROOT }) { section ->
                            val available = remember(songs, section) { sourceEntries(songs, section).isNotEmpty() }
                            SourceSheetRow(
                                text = stringResource(section.label()),
                                icon = section.icon(),
                                enabled = available,
                                chevron = true,
                                onClick = { page = section },
                            )
                        }
                    }
                } else {
                    val entries = remember(songs, current) { sourceEntries(songs, current) }
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                        items(entries, key = { it.scope.key() }) { entry ->
                            SourceSheetRow(
                                text = if (current == SourcePage.DURATION)
                                    stringResource((entry.scope as LibraryScope.Duration).bucket.label())
                                else entry.label,
                                subtitle = entry.subtitle,
                                icon = current.icon(),
                                count = entry.count,
                                selected = entry.scope == selected,
                                onClick = { onSelect(entry.scope) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun SourcePage.label(): Int = when (this) {
    SourcePage.ROOT -> R.string.library_source_title
    SourcePage.FOLDERS -> R.string.library_source_folders
    SourcePage.DURATION -> R.string.library_source_duration
    SourcePage.GENRES -> R.string.library_source_genres
    SourcePage.ARTISTS -> R.string.library_source_artists
    SourcePage.YEARS -> R.string.library_source_years
}

private fun SourcePage.icon(): SongMeldIconId = when (this) {
    SourcePage.ROOT -> SongMeldIconId.LibraryMusic
    SourcePage.FOLDERS -> SongMeldIconId.FolderOpen
    SourcePage.DURATION -> SongMeldIconId.History
    SourcePage.GENRES -> SongMeldIconId.Palette
    SourcePage.ARTISTS -> SongMeldIconId.Info
    SourcePage.YEARS -> SongMeldIconId.History
}

@Composable
private fun SourceSheetRow(
    text: String,
    icon: SongMeldIconId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    count: Int? = null,
    selected: Boolean = false,
    chevron: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.secondaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SongMeldIcon(icon, null, size = 22.dp)
            Column(Modifier.weight(1f)) {
                Text(text, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null && subtitle != text) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
            if (count != null) Text(count.toString(), style = MaterialTheme.typography.labelMedium)
            if (selected) SongMeldIcon(SongMeldIconId.Check, null, size = 21.dp)
            else if (chevron) SongMeldIcon(SongMeldIconId.ChevronRight, null, size = 20.dp)
        }
    }
}
