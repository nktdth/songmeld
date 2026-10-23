package moe.ktandth.songmeld.ui.songs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryScopesTest {
    @Test fun allSourceTypesSurvivePreferenceSerialization() {
        val choices = listOf(
            LibraryScope.AllSongs,
            LibraryScope.Folder("Music/Chill:2026"),
            LibraryScope.Artist("Bích Phương"),
            LibraryScope.Genre("Pop/Rock"),
            LibraryScope.Year(2026),
            LibraryScope.Duration(DurationBucket.MEDIUM),
        )
        choices.forEach { source -> assertEquals(source, parseLibraryScope(source.key())) }
    }

    @Test fun unknownSavedValuesGracefullyReturnAllSongs() {
        assertEquals(LibraryScope.AllSongs, parseLibraryScope("missing"))
        assertEquals(LibraryScope.AllSongs, parseLibraryScope("year:invalid"))
        assertEquals(LibraryScope.AllSongs, parseLibraryScope("duration:unknown"))
    }

    @Test fun durationBucketsAreExclusiveAtBoundaries() {
        assertTrue(DurationBucket.SHORT.matches(179_999))
        assertFalse(DurationBucket.SHORT.matches(180_000))
        assertTrue(DurationBucket.MEDIUM.matches(180_000))
        assertFalse(DurationBucket.MEDIUM.matches(300_000))
        assertTrue(DurationBucket.LONG.matches(300_000))
        assertFalse(DurationBucket.LONG.matches(600_000))
        assertTrue(DurationBucket.VERY_LONG.matches(600_000))
        assertTrue(DurationBucket.VERY_LONG.matches(3_600_000))
    }

    @Test fun vietnameseSearchStillIgnoresCombiningAccents() {
        assertEquals("bich phuong", normalizeSongSearch("Bích Phương"))
        assertEquals("duong", normalizeSongSearch("Đường"))
    }
}
