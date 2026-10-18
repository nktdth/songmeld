# Settings foundation

SongMeld's top-right **More options** button is an accessible `IconButton` with a
Material 3 `DropdownMenu`. The **Settings** item navigates to a Compose settings
page. Android's system Back and the settings back arrow return to the Songs
Library. Settings are available even before the user grants audio access.

## Working preferences

- **Theme:** Follow system (default), Light, or Dark. Updates the Material 3
  theme and system-bar icon contrast immediately.
- **Dynamic color:** Enabled by default on Android 12+. The switch is disabled
  with an explanation on Android 8–11, which retain SongMeld's orange palette.
- Changes persist in the private `songmeld_appearance` SharedPreferences file.

`AppearancePreferences` isolates persistence from the Compose UI. This is a
small starting point; DataStore can replace it as preferences become more
numerous or need asynchronous persistence.

## Later: configurable icon system

Settings contains a clearly labeled *informational* Icons section. No nonfunctional
icon controls are exposed. When the Material Symbols Variable Font engine exists,
add real fill/weight/grade/optical-size controls here and decide whether the
settings apply globally or only to user-created Tags / Playlists. Keep branding
resources separate and avoid bundling thousands of unused icons upfront.

## QA checklist

1. With and without audio permission, open More options → Settings; use both
   the toolbar arrow and device Back.
2. Toggle Light, Dark and Follow system. Check surfaces, text contrast and
   status/navigation bar icon visibility.
3. On Android 12+, toggle Dynamic color; wallpaper colors should be used when
   enabled, SongMeld orange/brown colors when disabled.
4. On Android 8–11, verify the dynamic-color switch is disabled and the custom
   palette remains usable.
5. Relaunch the app and rotate the device; preferences should persist.
6. Return to Songs and ensure search, refresh, the music-permission prompt and
   library listing still work.
