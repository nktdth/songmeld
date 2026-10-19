package moe.ktandth.songmeld.data.local

/** Bound the longest decoded edge, including panoramas. Power-of-two samples work on older codecs. */
internal fun artworkSampleSize(width: Int, height: Int): Int {
    if (width <= 0 || height <= 0) return 1
    var sample = 1
    val longest = maxOf(width, height)
    while (longest / sample > 512) sample *= 2
    return sample
}
