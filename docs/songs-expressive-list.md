# Songs list — connected Material 3 surfaces

This change is intentionally limited to the local Songs screen. No playback, sorting, or account logic is introduced.

## UI

- One connected list of song rows: 24dp top corners on the first result, square intermediate rows, 24dp bottom corners on the last result, 24dp all-round for a single result.
- Each row uses `surfaceContainer`, separated from neighbors by 2dp, with 16dp outer horizontal margins.
- 56dp rounded cover thumbnail, one-line ellipsized title, then `artist · duration` on the same second line. Album is still indexed for search and held in the song model, but is not shown on the row.
- `LazyColumn.itemsIndexed` uses the MediaStore `_ID` as stable key. Filtering recomputes first/last boundaries.

## Local artwork

- Read audio metadata using `MediaMetadataRetriever.getEmbeddedPicture()` on `Dispatchers.IO`, preferring the actual file cover to an album-shared image.
- If no embedded picture is readable, fall back to the provider's legacy MediaStore album-art URI using `ALBUM_ID`.
- Decode a downsampled ~256px image, not full-resolution album art, and cache results with a bounded `LruCache` (2–16MiB, depending on heap budget); cache unavailable artwork separately to avoid repeated extraction on every scroll.
- `DATE_MODIFIED` participates in the cache key to refresh covers after file-tag edits when MediaStore observes them.
- Show a theme-colored music-note placeholder for audio files without artwork or where the device/media provider cannot read the art.
- No new dependencies and no network requests.

## QA on device

1. Test a track with embedded PNG/JPEG album art, and one without an embedded picture.
2. Test a track where the media provider has album cover art but the audio file does not.
3. Scroll a library with hundreds/thousands of tracks and revisit previously shown artwork.
4. Search for one matching track (all corners rounded), then multiple tracks (first/middle/last corners).
5. Test very long song titles/artist names and long durations; time should stay visible.
6. Test dark/light theme and permission denial/approval.
7. After editing a song cover externally, rescan/reopen the library to refresh the MediaStore modification metadata.

## References

- Compose-Settings Material 3 Expressive (`segmentedShapes(index, count)`): https://github.com/alorma/Compose-Settings
- MetroList Android Music Player Material 3 UX: https://github.com/MetrolistGroup/Metrolist
- Android `MediaMetadataRetriever` API: https://developer.android.com/reference/android/media/MediaMetadataRetriever
- Android lazy layout guidance: https://developer.android.com/agents/skills/jetpack-compose/jetpack-compose-performance/references/lazy-layouts
