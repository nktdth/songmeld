package moe.ktandth.songmeld.ui.settings

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

internal enum class ChangeCategory(val jsonKey: String) {
    ADDED("added"), IMPROVED("improved"), FIXED("fixed"), REMOVED("removed"), OTHER("other"),
}

internal data class ChangelogGroup(val category: ChangeCategory, val changes: List<String>)

internal data class ChangelogRelease(
    val version: String,
    val status: String,
    val lastUpdated: LocalDate?,
    val groups: List<ChangelogGroup>,
) {
    val changeCount: Int get() = groups.sumOf { it.changes.size }
}

/** Explicit categories, independent of Vietnamese wording. Legacy arrays remain readable. */
internal fun parseChangelog(text: String): List<ChangelogRelease> {
    val array = JSONObject(text).getJSONArray("releases")
    require(array.length() > 0)
    return List(array.length()) { index ->
        val release = array.getJSONObject(index)
        val changes = release.get("changes")
        val grouped = linkedMapOf<ChangeCategory, MutableList<String>>()
        when (changes) {
            is JSONArray -> grouped[ChangeCategory.OTHER] = changes.changeStrings().toMutableList()
            is JSONObject -> changes.keys().forEach { key ->
                // Preserve future categories as "Other" instead of silently discarding entries.
                val category = ChangeCategory.entries.firstOrNull { it.jsonKey == key } ?: ChangeCategory.OTHER
                grouped.getOrPut(category) { mutableListOf() }.addAll(changes.getJSONArray(key).changeStrings())
            }
            else -> error("Expected a change-category object or a legacy array")
        }
        val groups = ChangeCategory.entries.mapNotNull { category ->
            grouped[category]?.takeIf { it.isNotEmpty() }?.let { ChangelogGroup(category, it.toList()) }
        }
        require(groups.isNotEmpty())
        val status = release.getString("status")
        require(status == "development" || status == "released")
        ChangelogRelease(
            version = release.getString("version").also { require(it.isNotBlank()) },
            status = status,
            // Invalid optional metadata must not hide an otherwise valid release.
            lastUpdated = release.optString("lastUpdated").takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            },
            groups = groups,
        )
    }
}

private fun JSONArray.changeStrings(): List<String> = List(length()) { index ->
    val value = get(index)
    require(value is String && value.isNotBlank())
    value
}
