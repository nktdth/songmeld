# Settings — current screen structure

The top-right **More options** button in SongMeld's library header opens a Material 3 `DropdownMenu` containing **Settings**. The header stays accessible even when the app has no audio-library permission.

The Settings overview is intentionally minimal: **Appearance** and **About**. Both rows use pastel circular icon backgrounds inspired by SongMeld v1. Child pages use a collapsing `MediumTopAppBar` with `exitUntilCollapsedScrollBehavior()`; the currently configured Compose Material 3 version does not expose `MediumFlexibleTopAppBar` as a public API.

## Destination and Back behavior

`SongMeldApp.kt` stores one `SongMeldPage` destination (rather than several overlapping Boolean page flags). The Android Back button and app-bar Back icon share the same parent chain:

```text
Library → Settings → Appearance → Icon appearance → Browse icons
                   ↘ About
```

Back proceeds to the direct parent page, and the selected destination is saved across ordinary Compose activity recreation. Each settings destination owns its Scaffold, preventing duplicate insets. The Settings overview is intentionally short; the app bar only visibly collapses when the page has enough content to scroll (for example, Icon appearance).

## Active preferences

- **Theme:** Follow system (default), Light, or Dark, applied to Material 3 and system bars.
- **Dynamic color:** Android 12+ (API 31+) when enabled. Otherwise SongMeld's warm fallback palette.
- **Icon appearance:** Rounded/Outlined/Sharp, variable axes FILL/wght/GRAD/opsz, and preview background shape.
- **Browse icons:** Search the offline Symbols/MDI/Flutter icon catalogs and save a test selection (not yet linked to Tags/Playlists).

`AppearancePreferences.kt` stores these settings in private `songmeld_appearance` SharedPreferences and preserves the earlier `filled` preference migration. See [icon system](icon-system.md) for font provenance and update tooling.
