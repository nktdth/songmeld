package moe.ktandth.songmeld.ui.settings

/** Small, persistent UI options; independent of icon/font customization. */
data class InterfaceOptions(
    val reduceMotion: Boolean = false,
    val compactLibrary: Boolean = false,
)
