# UX review — local library and navigation

## Implemented

- Library: visible title, rescan action, sort dialog, current order and result count.
- Persist the user's sort order; support title, artist, newest-added and longest-first.
- Search title/artist/album together using all words, ignoring case and Vietnamese
  accents (including đ). Run filtering/sorting on Dispatchers.Default and cancel
  outdated queries.
- Tap a song for a read-only metadata sheet; missing album is labelled, not blank.
- Empty library offers rescan; empty search offers clear. Error/empty/permission
  content can scroll on smaller screens and at larger font sizes.
- Keep saveable destination state, including search and list/child-page scroll,
  when opening settings and returning. Only IDs, query and small UI state are saved;
  song collections and document contents are not saved into the Android Bundle.
- Show loading/error text instead of a misleading zero-song count while scanning.
- Keep metadata fallbacks in Vietnamese resources rather than hard-coded English.

## References studied

- [Jetsnack Search](https://github.com/android/compose-samples/blob/main/Jetsnack/app/src/main/java/com/example/jetsnack/ui/home/search/Search.kt):
  separate search results, pending and no-results states.
- [Androidify theme](https://github.com/android/androidify/blob/main/core/theme/src/main/java/com/android/developers/androidify/theme/Theme.kt):
  theme-based surfaces, shapes and consistent Material 3 Expressive identity.
- [Compose state saving](https://developer.android.com/develop/ui/compose/state-saving)
  and [SaveableStateHolder source](https://android.googlesource.com/platform/frameworks/support/+/31bd95abfbc90051974b3f3753d6651552b49f50/compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/SaveableStateHolder.kt).
- [Compose accessibility defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults):
  labelled controls, row-level selection and standard Material touch targets.

These are design references; no third-party source files were copied.

## Checks

`bash tools/check_library_query.sh /path/to/kotlinc` compiles the actual Song model
and query implementation with a local Kotlin distribution and its coroutines JAR.
It checks accent-insensitive multi-word search, ordering, stable ties, empty input,
source-list preservation and cancellation. The opaque Android Uri type is stubbed
only in this standalone JVM harness. It does not test Android APIs or Compose.

A Kotlin PSI syntax parse of app sources and XML/resource/asset checks were also
performed. See `docs/build-verification.md` for the latest Android build status.
On-device UI verification is still required.

Before merging, build and check on a device:

1. Grant/deny permission; use landscape and 200% font scale. Buttons remain reachable.
2. Search `duong` for a title containing `Đường`; combine title/artist/album words.
3. Try every order; restart the app and confirm the selected order is retained.
4. Scroll a long list, open Settings/About/licenses, then return. Confirm query and
   scroll position; changing query/order should return to the top.
5. Rescan an empty library and clear a no-results query; check permission revocation.
6. Tap a song with long/missing metadata; read/copy text, dismiss with Back and Close.
7. Check light/dark, dynamic color, TalkBack labels and the keyboard on small screens.

## Next priorities

Validate the existing flows on-device first, including the icon browser and library
refresh behavior. Consider additional library filters after these controls are tested.
