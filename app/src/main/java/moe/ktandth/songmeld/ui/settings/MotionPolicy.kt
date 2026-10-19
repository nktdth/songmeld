package moe.ktandth.songmeld.ui.settings

/** Decorative app motion cannot override Android's animation preference. */
internal fun decorativeMotionEnabled(reduceMotion: Boolean, systemAnimationsEnabled: Boolean): Boolean =
    !reduceMotion && systemAnimationsEnabled
