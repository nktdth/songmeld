package moe.ktandth.songmeld.ui.icons

import java.util.Locale
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/** Skip malformed rows and duplicate keys; preserve the first valid entry for each glyph. */
internal fun parseIconCatalog(lines: Sequence<String>, pack: IconPack): List<CatalogIconEntry> {
    val entries = LinkedHashMap<Int, CatalogIconEntry>()
    lines.forEach { line ->
        val split = line.indexOf('\t')
        if (split <= 0) return@forEach
        val codepoint = line.substring(0, split).trim().toIntOrNull(16) ?: return@forEach
        val name = line.substring(split + 1).trim()
        if (!isSupportedIconCodepoint(codepoint) || name.isEmpty()) return@forEach
        entries.putIfAbsent(codepoint, CatalogIconEntry(IconReference(pack, codepoint), name))
    }
    return entries.values.toList()
}

private fun searchableIconName(value: String): String =
    value.lowercase(Locale.ROOT).replace('_', ' ').replace('-', ' ')

/** Match multiple words across separator styles, or an exact U+E405 / 0xE405 codepoint. */
internal suspend fun filterIconCatalog(entries: List<CatalogIconEntry>, query: String): List<CatalogIconEntry> {
    val search = query.trim()
    val hex = when {
        search.startsWith("U+", ignoreCase = true) -> search.drop(2)
        search.startsWith("0x", ignoreCase = true) -> search.drop(2)
        else -> null
    }
    val codepoint = hex?.toIntOrNull(16)
    val words = searchableIconName(search).split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) {
        coroutineContext.ensureActive()
        return entries
    }
    return entries.filter { entry ->
        coroutineContext.ensureActive()
        if (hex != null) entry.reference.codepoint == codepoint
        else {
            val name = searchableIconName(entry.name)
            words.all { it in name }
        }
    }
}
