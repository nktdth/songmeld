package moe.ktandth.songmeld.download

import android.content.Context

/** Only expose options already wired into the downloader. Conversion comes later. */
enum class DownloadQualityPolicy { BEST, SAVE_DATA, ASK_EACH_TIME }
enum class PreferredAudioCodec { AUTO, M4A_AAC, OPUS_WEBM }
enum class DownloadFolderPolicy { SINGLE, BY_SOURCE }

/**
 * Small, durable preferences store. Snapshot preferences when a download starts; changes in
 * Settings affect subsequent tasks, not files currently being written.
 */
class DownloadPreferences(context: Context) {
    private val storage = context.applicationContext.getSharedPreferences("songmeld_download", Context.MODE_PRIVATE)

    var quality: DownloadQualityPolicy
        get() = enumOrDefault(storage.getString("quality", null), DownloadQualityPolicy.BEST)
        set(value) { storage.edit().putString("quality", value.name).apply() }

    var preferredCodec: PreferredAudioCodec
        get() = enumOrDefault(storage.getString("codec", null), PreferredAudioCodec.AUTO)
        set(value) { storage.edit().putString("codec", value.name).apply() }

    var folderPolicy: DownloadFolderPolicy
        get() = enumOrDefault(storage.getString("folder", null), DownloadFolderPolicy.SINGLE)
        set(value) { storage.edit().putString("folder", value.name).apply() }

    /** SAF URI with persistable user-granted read/write permission; null uses MediaStore. */
    var customTreeUri: String?
        get() = storage.getString("custom_tree_uri", null)
        set(value) {
            storage.edit().apply {
                if (value == null) remove("custom_tree_uri")
                else putString("custom_tree_uri", value)
            }.apply()
        }

    var wifiOnly: Boolean
        get() = storage.getBoolean("wifi_only", false)
        set(value) { storage.edit().putBoolean("wifi_only", value).apply() }

    var completionNotifications: Boolean
        get() = storage.getBoolean("complete_notification", true)
        set(value) { storage.edit().putBoolean("complete_notification", value).apply() }

    companion object {
        private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
            enumValues<T>().firstOrNull { it.name == raw } ?: fallback

        /**
         * Match the source format; never pretend remuxing or codec transcoding happened.
         * The preference is a soft priority: unsupported formats fall back to available audio.
         */
        fun chooseDefaultFormat(
            formats: List<AudioFormatChoice>,
            quality: DownloadQualityPolicy,
            codec: PreferredAudioCodec,
        ): AudioFormatChoice? {
            if (quality == DownloadQualityPolicy.ASK_EACH_TIME) return null
            val candidates = formats.filter { format ->
                when (codec) {
                    PreferredAudioCodec.AUTO -> true
                    PreferredAudioCodec.M4A_AAC -> format.extension in setOf("m4a", "aac")
                    PreferredAudioCodec.OPUS_WEBM -> format.extension in setOf("webm", "opus", "ogg")
                }
            }.ifEmpty { formats }
            return when (quality) {
                DownloadQualityPolicy.BEST ->
                    candidates.maxByOrNull { it.bitrateKbps ?: 0 }
                DownloadQualityPolicy.SAVE_DATA ->
                    candidates.minByOrNull { it.bitrateKbps ?: Int.MAX_VALUE }
                DownloadQualityPolicy.ASK_EACH_TIME -> null
            }
        }
    }
}
