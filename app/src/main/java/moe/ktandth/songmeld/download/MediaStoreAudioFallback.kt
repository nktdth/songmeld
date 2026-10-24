package moe.ktandth.songmeld.download

/**
 * Android's MediaStore Audio collection is strict about known audio MIME types,
 * and vendor MIME mappings vary. Fall back to the generic Downloads collection
 * only for the exact unsupported MIME category (not permissions, storage full,
 * or arbitrary provider exceptions).
 */
internal object MediaStoreAudioFallback {
    fun shouldUseDownloads(providerError: String?): Boolean =
        providerError?.contains("Unsupported MIME type", ignoreCase = true) == true
}
