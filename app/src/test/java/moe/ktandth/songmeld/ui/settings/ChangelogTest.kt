package moe.ktandth.songmeld.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class ChangelogTest {
    private fun release(changes: String, metadata: String = "") =
        """{"releases":[{"version":"0.1.0","status":"development","changes":$changes$metadata}]}"""

    @Test fun categoriesHaveStableOrderAndKeepEveryChange() {
        val result = parseChangelog(release("""{"fixed":["Fix"],"improved":["Improve"],"added":["A","B"]}""")).single()
        assertEquals(listOf(ChangeCategory.ADDED, ChangeCategory.IMPROVED, ChangeCategory.FIXED), result.groups.map { it.category })
        assertEquals(listOf("A", "B", "Improve", "Fix"), result.groups.flatMap { it.changes })
        assertEquals(4, result.changeCount)
    }

    @Test fun legacyReleasesRemainReadableWithoutGuessingCategories() {
        val result = parseChangelog(release("""["Old change","Another change"]""")).single()
        assertEquals(ChangeCategory.OTHER, result.groups.single().category)
        assertEquals(2, result.changeCount)
    }

    @Test fun unknownCategoriesArePreservedAndCombinedWithOther() {
        val result = parseChangelog(release("""{"security":["Security change"],"other":["Other"],"added":[]}""")).single()
        assertEquals(ChangeCategory.OTHER, result.groups.single().category)
        assertEquals(setOf("Security change", "Other"), result.groups.single().changes.toSet())
    }

    @Test fun optionalDatesDoNotHideChanges() {
        assertNull(parseChangelog(release("""["Change"]""")).single().lastUpdated)
        assertNull(parseChangelog(release("""["Change"]""", """, "lastUpdated":"invalid"""")).single().lastUpdated)
        assertEquals(LocalDate.of(2026, 10, 22),
            parseChangelog(release("""["Change"]""", """, "lastUpdated":"2026-10-22"""")).single().lastUpdated)
    }

    @Test fun multipleReleasesRetainTheirOwnVersionAndStatus() {
        val result = parseChangelog("""{"releases":[
            {"version":"0.2.0","status":"development","changes":{"added":["New"]}},
            {"version":"0.1.0","status":"released","changes":["Old"]}
        ]}""")
        assertEquals(listOf("0.2.0", "0.1.0"), result.map { it.version })
        assertEquals(listOf("development", "released"), result.map { it.status })
    }

    @Test fun emptyAndMalformedContentIsRejectedForTheVisibleErrorState() {
        listOf("[]", "{}", "42", """[""]""", """[4]""", """{"fixed":"not an array"}""").forEach { changes ->
            assertThrows(Exception::class.java) { parseChangelog(release(changes)) }
        }
        assertThrows(Exception::class.java) { parseChangelog("""{"releases":[]}""") }
        assertThrows(Exception::class.java) { parseChangelog("not json") }
        assertThrows(Exception::class.java) {
            parseChangelog(release("""["Change"]""").replace("development", "unknown"))
        }
        assertThrows(Exception::class.java) {
            parseChangelog(release("""["Change"]""").replace("0.1.0", " "))
        }
    }
}
