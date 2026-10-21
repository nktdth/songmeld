# Home library — source selector, sorting, and pull-to-refresh

## Design references

- SongMeld-v1 (private owner repo): `SongsScreen.kt` source selector bottom sheet with nested Folders/Artists/Genres/Years and separate right-hand sorting menu.
- [PixelPlayer LibrarySongsTab](https://github.com/PixelPlayerHQ/PixelPlayer/blob/main/app/src/main/java/com/theveloper/pixelplay/presentation/screens/LibrarySongsTab.kt): a library scroll area wrapped in `PullToRefreshBox` with `PullToRefreshDefaults.LoadingIndicator`.
- [Rhythm LibraryScreen](https://github.com/cromaguy/Rhythm/blob/main/app/src/main/java/chromahub/rhythm/app/features/local/presentation/screens/LibraryScreen.kt): Material 3 sorting/dropdown and refresh integrated with a local music library.
- Android official [Pull to refresh](https://developer.android.com/develop/ui/compose/components/pull-to-refresh) and [M3 Expressive pull refresh LoadingIndicator](https://developer.android.com/reference/kotlin/androidx/compose/material3/pulltorefresh/PullToRefreshDefaults).

## Implementation

- **Pull:** no refresh icon in the Home title row. Drag the scrollable library down from its top to invoke the existing guarded `onRetry` rescan. The M3 Expressive loading indicator reflects the parent `isRefreshing` state; a refresh error retains a text retry action. Initial-loading and empty-state cases are kept separate. An empty library instructs users to swipe to rescan.
- **Source, left:** one compact pill opens a modal hierarchical selector. The root offers All songs, Folders, Duration, Genres, Artists, Years; the next page lists available values with per-group song counts and a check for the active selection. The selected scope is persisted as a stable string key.
- **Sort, right:** an independent anchored dropdown (Title A–Z, Artist A–Z, Added newest/oldest, Modified newest, Duration longest/shortest), also persisted.
- **Execution:** a single off-main `querySongs(songs, query, sort, scope)` combines the selected source with accent-insensitive text search and deterministic sort order. Async results contain their query, sort and scope identities so stale screens do not flash when changing filters. Changing sort/query/source scrolls to the start; a plain library rescan retains position.
- **Metadata:** source categories consume `MediaStore` parent folders, `YEAR`, and `GENRE`. For Android 11+, query the `GENRE` media column; Android 8–10 uses the legacy `Audio.Genres` member map. Folders on Android 10+ use `RELATIVE_PATH`; older releases derive folders from the file path. Missing metadata simply leads to a disabled/empty source category.
- **Duration groups:** under 3m, 3–5m, 5–10m, 10m+. Adjacent groups do not overlap.
- **Folder semantics:** these are subfolders containing songs already indexed by MediaStore; this is not an arbitrary Storage Access Framework folder importer or a manually managed playlist.

## Build fix after owner testing

A local `./gradlew :app:assembleDebug :app:testDebugUnitTest` attempt reported Kotlin compile errors in two files. Follow-up fixes on the same PR:

- `MediaStoreSongRepository.kt`: explicitly type the MediaStore result `List<Song>` and `buildList<Song>` to resolve Kotlin 2.4's failed inference and its cascading member-resolution errors.
- `SongsScreen.kt`: replace `PullToRefreshDefaults.LoadingIndicator` and the unavailable `ExperimentalMaterial3ExpressiveApi` with the supported `PullToRefreshDefaults.Indicator` and standard `CircularProgressIndicator` for initial load. The current Material 3 dependency does not expose the newer expressive indicator; the pull gesture, rounded container and theme colors remain functional. The more expressive component can be revisited when its public API is available in this project's dependency version.
- The initial `git fetch` remote-tracking lock conflict was nonfatal: `git pull --ff-only` advanced the checked-out feature branch.

**These follow-up edits are statically checked but not locally compiled** in the assistant environment (no Android SDK). The Android device build/test remains required.

## Validation

- Added `LibraryScopesTest.kt` (4 unit tests): persistence round-tripping, invalid saved values, duration boundaries and Vietnamese normalization.
- Static checks cover resources, callback wiring, Kotlin delimiter balance and enum sort labels.
- **Build/device tests still needed:** `./gradlew :app:assembleDebug :app:testDebugUnitTest`, then `./gradlew installDebug`.
- On device test short/long lists, pull at the first item, pull after scrolling back to the first item, pull in empty/search/filter states, fast repeat pull while refreshing, rescan failure, source and sort persistence after app restart, Android 8–10 genre fallback, Android 11+ genre/year, long folder names, small width, large system fonts, and screen-reader labels.

