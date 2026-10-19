package moe.ktandth.songmeld.ui.icons

import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val lines = sequenceOf(
        "E405\tmusic_note", "e405\tduplicate", "E5C4\tarrow_back", "F0001\tarrow-left",
        "110000\ttoo_high", "D800\tsurrogate", "0041\ttoo_low", "-1\tnegative",
        "nothex\tbad", "broken row", "E000\t   ", "  E001\t spaced_name  ",
    )
    val entries = parseIconCatalog(lines, IconPack.SYMBOLS)
    check(entries.map { it.name } == listOf("music_note", "arrow_back", "arrow-left", "spaced_name"))
    check(entries.map { it.reference }.toSet().size == entries.size)
    check(filterIconCatalog(entries, "MUSIC note").single().name == "music_note")
    check(filterIconCatalog(entries, "back arrow").single().name == "arrow_back")
    check(filterIconCatalog(entries, "arrow-left").single().name == "arrow-left")
    check(filterIconCatalog(entries, "  U+e405 ").single().name == "music_note")
    check(filterIconCatalog(entries, "0xF0001").single().name == "arrow-left")
    check(filterIconCatalog(entries, "U+oops").isEmpty())
    check(filterIconCatalog(entries, " ") == entries)
    check(filterIconCatalog(entries, "missing").isEmpty())
    check(parseIconCatalog(emptySequence(), IconPack.MDI).isEmpty())
    var cancelled = false
    val job = launch {
        cancel()
        try { filterIconCatalog(entries, "music") } catch (_: CancellationException) { cancelled = true }
    }
    job.join()
    check(cancelled)
    IconPack.entries.forEach { pack ->
        val catalog = File(args[0], "${pack.storageName}.tsv").useLines { parseIconCatalog(it, pack) }
        check(catalog.isNotEmpty())
        check(catalog.all { it.reference.pack == pack })
        check(catalog.map { it.reference }.toSet().size == catalog.size)
        println("${pack.storageName}: ${catalog.size} valid unique glyphs")
    }
    println("Icon catalog checks passed: actual assets, invalid/duplicate rows, name/codepoint search and cancellation.")
}
