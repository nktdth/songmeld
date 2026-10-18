# SongMeld header and color architecture

## Persistent header

`ui/components/SongMeldHeader.kt` renders the SongMeld wordmark, animated
scalloped logo, and the reserved overflow icon. It lives in the root
`SongMeldApp()` Scaffold's `topBar`, **outside** the permission and library
screen branches. Permission requests and `ON_RESUME` permission refreshes do
not detach the header or restart the entrance animation.

The `SongsScreen` and `MusicPermissionScreen` are content-only composables;
the root Scaffold manages the top/status-bar and bottom/gesture-navigation
insets once. Each screen should not add a second Scaffold or top bar.

## Material 3 colors

- Android 8–11: use `SongMeldLightColors` / `SongMeldDarkColors` in
  `ui/theme/Theme.kt`. The warm light palette shares its `#FFA069` primary
  container with the launcher badge and `#57331E` on-primary-container
  with the launcher note. Readable primary text uses a deeper orange.
- Android 12+: keep `dynamicLightColorScheme` / `dynamicDarkColorScheme`
  to follow the user's wallpaper. Launcher and splash also use Android's
  system accent resources.
- The full in-app brand vector `songmeld_logo.xml` uses the same launcher
  color resources; no hardcoded purple or white colors. README retains its
  static orange SVG for use outside Android.
- Dark fallback surfaces share `#24170F` with the splash screen to avoid a
  flash when entering the main interface.

The overflow icon is a non-clickable placeholder and deliberately has no menu behavior yet.
