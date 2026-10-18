package moe.ktandth.songmeld.ui.icons

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Stable values for user preferences. Never persist Android resource IDs. */
enum class AppIconStyle(val preferenceValue: String) {
    ROUNDED("rounded"), OUTLINED("outlined"), FILLED("filled");

    companion object {
        fun fromPreference(value: String?): AppIconStyle =
            entries.firstOrNull { it.preferenceValue == value } ?: ROUNDED
    }
}

enum class AppIconShape(val preferenceValue: String) {
    CIRCLE("circle"), ROUNDED_SQUARE("rounded_square"), SQUARE("square");

    companion object {
        fun fromPreference(value: String?): AppIconShape =
            entries.firstOrNull { it.preferenceValue == value } ?: CIRCLE
    }
}

/** Central semantic catalog, shared by screens and future custom icon sources. */
enum class SongMeldIconId {
    ArrowBack, ChevronRight, Close, FolderOpen, Info, LibraryMusic, Lock, MoreVert, MusicNote, Palette, Refresh, Search, SearchOff, Settings
}

val LocalSongMeldIconStyle = compositionLocalOf { AppIconStyle.ROUNDED }
val LocalSongMeldIconShape = compositionLocalOf { AppIconShape.CIRCLE }

@Composable
fun songMeldIcon(id: SongMeldIconId): ImageVector = when (LocalSongMeldIconStyle.current) {
    AppIconStyle.ROUNDED -> when (id) {
        SongMeldIconId.ArrowBack -> Icons.Rounded.ArrowBack
        SongMeldIconId.ChevronRight -> Icons.Rounded.ChevronRight
        SongMeldIconId.Close -> Icons.Rounded.Close
        SongMeldIconId.FolderOpen -> Icons.Rounded.FolderOpen
        SongMeldIconId.Info -> Icons.Rounded.Info
        SongMeldIconId.LibraryMusic -> Icons.Rounded.LibraryMusic
        SongMeldIconId.Lock -> Icons.Rounded.Lock
        SongMeldIconId.MoreVert -> Icons.Rounded.MoreVert
        SongMeldIconId.MusicNote -> Icons.Rounded.MusicNote
        SongMeldIconId.Palette -> Icons.Rounded.Palette
        SongMeldIconId.Refresh -> Icons.Rounded.Refresh
        SongMeldIconId.Search -> Icons.Rounded.Search
        SongMeldIconId.SearchOff -> Icons.Rounded.SearchOff
        SongMeldIconId.Settings -> Icons.Rounded.Settings
    }
    AppIconStyle.OUTLINED -> when (id) {
        SongMeldIconId.ArrowBack -> Icons.Outlined.ArrowBack
        SongMeldIconId.ChevronRight -> Icons.Outlined.ChevronRight
        SongMeldIconId.Close -> Icons.Outlined.Close
        SongMeldIconId.FolderOpen -> Icons.Outlined.FolderOpen
        SongMeldIconId.Info -> Icons.Outlined.Info
        SongMeldIconId.LibraryMusic -> Icons.Outlined.LibraryMusic
        SongMeldIconId.Lock -> Icons.Outlined.Lock
        SongMeldIconId.MoreVert -> Icons.Outlined.MoreVert
        SongMeldIconId.MusicNote -> Icons.Outlined.MusicNote
        SongMeldIconId.Palette -> Icons.Outlined.Palette
        SongMeldIconId.Refresh -> Icons.Outlined.Refresh
        SongMeldIconId.Search -> Icons.Outlined.Search
        SongMeldIconId.SearchOff -> Icons.Outlined.SearchOff
        SongMeldIconId.Settings -> Icons.Outlined.Settings
    }
    AppIconStyle.FILLED -> when (id) {
        SongMeldIconId.ArrowBack -> Icons.Filled.ArrowBack
        SongMeldIconId.ChevronRight -> Icons.Filled.ChevronRight
        SongMeldIconId.Close -> Icons.Filled.Close
        SongMeldIconId.FolderOpen -> Icons.Filled.FolderOpen
        SongMeldIconId.Info -> Icons.Filled.Info
        SongMeldIconId.LibraryMusic -> Icons.Filled.LibraryMusic
        SongMeldIconId.Lock -> Icons.Filled.Lock
        SongMeldIconId.MoreVert -> Icons.Filled.MoreVert
        SongMeldIconId.MusicNote -> Icons.Filled.MusicNote
        SongMeldIconId.Palette -> Icons.Filled.Palette
        SongMeldIconId.Refresh -> Icons.Filled.Refresh
        SongMeldIconId.Search -> Icons.Filled.Search
        SongMeldIconId.SearchOff -> Icons.Filled.SearchOff
        SongMeldIconId.Settings -> Icons.Filled.Settings
    }
}

/** Only for icon backgrounds, not album art and not the app launcher icon. */
@Composable
fun songMeldIconShape(): Shape = iconBackgroundShape(LocalSongMeldIconShape.current)

fun iconBackgroundShape(shape: AppIconShape): Shape = when (shape) {
    AppIconShape.CIRCLE -> CircleShape
    AppIconShape.ROUNDED_SQUARE -> RoundedCornerShape(24.dp)
    AppIconShape.SQUARE -> RoundedCornerShape(8.dp)
}
