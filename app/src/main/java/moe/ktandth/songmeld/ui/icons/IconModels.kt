package moe.ktandth.songmeld.ui.icons

/** Only Symbols supports variable axes. The other two packs are static icon fonts. */
enum class IconPack(val storageName: String) {
    SYMBOLS("symbols"),
    MDI("mdi"),
    FLUTTER("flutter");

    companion object {
        fun fromStorage(value: String?): IconPack =
            entries.firstOrNull { it.storageName == value } ?: SYMBOLS
    }
}

data class IconReference(val pack: IconPack, val codepoint: Int) {
    init { require(isSupportedIconCodepoint(codepoint)) }
}

data class CatalogIconEntry(val reference: IconReference, val name: String)

internal fun isSupportedIconCodepoint(value: Int): Boolean = value in 0xE000..0x10FFFF
