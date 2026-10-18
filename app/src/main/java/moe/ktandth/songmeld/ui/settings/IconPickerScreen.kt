package moe.ktandth.songmeld.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import moe.ktandth.songmeld.R
import moe.ktandth.songmeld.ui.icons.CatalogIcon
import moe.ktandth.songmeld.ui.icons.CatalogIconEntry
import moe.ktandth.songmeld.ui.icons.IconPack
import moe.ktandth.songmeld.ui.icons.IconReference
import moe.ktandth.songmeld.ui.icons.MaterialSymbolAppearance
import moe.ktandth.songmeld.ui.icons.SongMeldIconCatalog

/** Full offline icon browser. Only visible grid items are composed. */
@Composable
fun IconPickerScreen(
    appearance: MaterialSymbolAppearance,
    selected: IconReference?,
    onSelected: (IconReference) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pack by rememberSaveable { mutableStateOf(IconPack.SYMBOLS) }
    var query by rememberSaveable { mutableStateOf("") }
    var entries by remember { mutableStateOf<List<CatalogIconEntry>>(emptyList()) }
    var filtered by remember { mutableStateOf<List<CatalogIconEntry>>(emptyList()) }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(pack) {
        loading = true
        error = false
        entries = emptyList()
        try {
            entries = SongMeldIconCatalog.load(context, pack)
        } catch (_: Exception) {
            error = true
        } finally {
            loading = false
        }
    }
    LaunchedEffect(entries, query) {
        if (query.isNotBlank()) delay(130) // Avoid filtering thousands of entries per key press.
        val search = query.trim()
        filtered = withContext(Dispatchers.Default) {
            if (search.isEmpty()) entries else entries.filter {
                it.name.contains(search, ignoreCase = true)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconPack.entries.forEach { option ->
                FilterChip(
                    selected = pack == option,
                    onClick = { pack = option; query = "" },
                    label = { Text(
                        when (option) {
                            IconPack.SYMBOLS -> stringResource(R.string.icon_pack_symbols)
                            IconPack.MDI -> "MDI"
                            IconPack.FLUTTER -> "Flutter"
                        }
                    ) },
                )
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(R.string.icon_search_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(58.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected != null) CatalogIcon(
                        reference = selected,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        size = 32.dp,
                        appearance = appearance,
                    ) else Text("♪", style = MaterialTheme.typography.headlineMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.icon_selected_preview),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        selected?.let { ref ->
                            val name = if (ref.pack == pack) entries.firstOrNull {
                                it.reference == ref
                            }?.name else null
                            "${ref.pack.storageName}: ${name ?: "U+${ref.codepoint.toString(16).uppercase()}"}"
                        } ?: stringResource(R.string.icon_nothing_selected),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (loading) stringResource(R.string.icon_loading)
            else stringResource(R.string.icon_results, filtered.size),
            modifier = Modifier.padding(horizontal = 5.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.height(6.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.icon_load_error))
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.icon_no_results))
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 72.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            ) {
                items(filtered, key = { it.reference.codepoint }) { entry ->
                    val active = selected == entry.reference
                    val surface = if (active) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                    Column(
                        modifier = Modifier
                            .background(surface, RoundedCornerShape(16.dp))
                            .clickable(role = Role.RadioButton) { onSelected(entry.reference) }
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CatalogIcon(
                            reference = entry.reference,
                            contentDescription = entry.name.replace('_', ' ').replace('-', ' '),
                            size = 28.dp,
                            tint = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            appearance = appearance,
                        )
                        Text(
                            entry.name.replace("_", " ").replace("-", " "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
