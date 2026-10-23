package moe.ktandth.songmeld.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.*

private data class LoadedIconCatalog(
    val pack: IconPack,
    val entries: List<CatalogIconEntry> = emptyList(),
    val failed: Boolean = false,
)

private data class IconSearchResult(val pack: IconPack, val query: String, val entries: List<CatalogIconEntry>)

/** One lazy grid lets search, preview and recovery actions remain reachable at large font scales. */
@Composable
fun IconPickerScreen(
    appearance: MaterialSymbolAppearance,
    selected: IconReference?,
    onSelected: (IconReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    var pack by rememberSaveable { mutableStateOf(selected?.pack ?: IconPack.SYMBOLS) }
    var query by rememberSaveable { mutableStateOf("") }
    var retry by remember { mutableIntStateOf(0) }
    val gridState = rememberLazyGridState()
    var previousView by rememberSaveable { mutableStateOf("${pack.name}\u0000$query") }
    LaunchedEffect(pack, query) {
        val view = "${pack.name}\u0000$query"
        if (view != previousView) {
            gridState.scrollToItem(0)
            previousView = view
        }
    }

    val loaded by produceState<LoadedIconCatalog?>(null, context, pack, retry) {
        value = null
        value = try {
            LoadedIconCatalog(pack, SongMeldIconCatalog.load(context, pack))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            LoadedIconCatalog(pack, failed = true)
        }
    }
    // Keys travel with each result, so old rows cannot be selected under a new pack/query label.
    val catalog = loaded?.takeIf { it.pack == pack }
    val searchResult by produceState<IconSearchResult?>(null, catalog, query) {
        value = null
        if (catalog != null && !catalog.failed) {
            if (query.isNotBlank()) delay(130)
            value = IconSearchResult(catalog.pack, query, withContext(Dispatchers.Default) {
                filterIconCatalog(catalog.entries, query)
            })
        }
    }
    val filtered = searchResult?.takeIf { it.pack == pack && it.query == query }?.entries
    val loading = catalog == null || (!catalog.failed && filtered == null)
    val failed = catalog?.failed == true
    val selectedName = remember(selected, catalog) {
        catalog?.entries?.firstOrNull { it.reference == selected }?.name
    }

    val minimumCellWidth = 96.dp * LocalDensity.current.fontScale.coerceAtLeast(1f)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minimumCellWidth),
        state = gridState,
        modifier = modifier.fillMaxSize().imePadding(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        item(key = "packs", span = { GridItemSpan(maxLineSpan) }) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconPack.entries.forEach { option ->
                    FilterChip(selected = pack == option, onClick = { pack = option },
                        label = { Text(stringResource(packLabel(option))) })
                }
            }
        }
        item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text(stringResource(R.string.icon_search_hint)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                leadingIcon = { SongMeldIcon(SongMeldIconId.Search, null) },
                trailingIcon = if (query.isNotEmpty()) { {
                    IconButton(onClick = { query = "" }) {
                        SongMeldIcon(SongMeldIconId.Close, stringResource(R.string.clear_search))
                    }
                } } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            )
        }
        item(key = "hint", span = { GridItemSpan(maxLineSpan) }) {
            Text(stringResource(R.string.icon_search_help), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
        }
        item(key = "selected", span = { GridItemSpan(maxLineSpan) }) {
            SelectedIconPreview(selected, selectedName, appearance)
        }
        if (pack != IconPack.SYMBOLS) {
            item(key = "static-font", span = { GridItemSpan(maxLineSpan) }) {
                Text(stringResource(R.string.icon_static_font_note), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
            }
        }
        item(key = "count", span = { GridItemSpan(maxLineSpan) }) {
            Text(when {
                failed -> stringResource(R.string.icon_load_error)
                loading -> stringResource(R.string.icon_loading)
                else -> stringResource(R.string.icon_results, filtered.orEmpty().size)
            }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp))
        }
        when {
            loading -> item(key = "loading", span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            failed -> item(key = "error", span = { GridItemSpan(maxLineSpan) }) {
                TextButton(onClick = { retry++ }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.retry)) }
            }
            filtered.isNullOrEmpty() -> item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.icon_no_results))
                    if (query.isNotEmpty()) TextButton(onClick = { query = ""; focus.clearFocus() }) {
                        Text(stringResource(R.string.clear_search))
                    }
                }
            }
            else -> items(filtered, key = { "${it.reference.pack.name}:${it.reference.codepoint}" },
                contentType = { "icon" }) { entry ->
                IconChoice(entry, selected == entry.reference, appearance) {
                    focus.clearFocus()
                    onSelected(entry.reference)
                }
            }
        }
    }
}

@Composable
private fun SelectedIconPreview(selected: IconReference?, name: String?, appearance: MaterialSymbolAppearance) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    if (selected != null) CatalogIcon(selected, null, size = 30.dp, appearance = appearance)
                    else SongMeldIcon(SongMeldIconId.MusicNote, null, size = 30.dp)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.icon_selected_preview), style = MaterialTheme.typography.titleSmall)
                Text(if (selected == null) stringResource(R.string.icon_nothing_selected) else {
                    "${stringResource(packLabel(selected.pack))}: ${name ?: "U+${selected.codepoint.toString(16).uppercase()}"}"
                }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun IconChoice(entry: CatalogIconEntry, active: Boolean, appearance: MaterialSymbolAppearance, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.clip(RoundedCornerShape(16.dp))
            .background(if (active) colors.primaryContainer else colors.surfaceContainerLow)
            .selectable(selected = active, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 88.dp).padding(horizontal = 4.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CatalogIcon(entry.reference, null, size = 28.dp, appearance = appearance,
            tint = if (active) colors.onPrimaryContainer else colors.onSurface)
        Text(entry.name.replace('_', ' ').replace('-', ' '), maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) colors.onPrimaryContainer else colors.onSurfaceVariant)
    }
}

private fun packLabel(pack: IconPack): Int = when (pack) {
    IconPack.SYMBOLS -> R.string.icon_pack_symbols
    IconPack.MDI -> R.string.icon_pack_mdi
    IconPack.FLUTTER -> R.string.icon_pack_flutter
}
