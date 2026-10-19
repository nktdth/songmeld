# Appearance settings

The Appearance page provides a sample row that uses the app's real palette,
typography and Material Symbols configuration. The sample is labelled as a preview.
Album-art placeholder shape remains independent of interface-icon background shape.

## Controls

- Theme: system/light/dark tiles, with selected background color and subtle icon
  emphasis animated over 220 ms. Tiles stack vertically below 300 dp of available
  width or above 1.3x font scale. Labels wrap instead of ellipsizing.
- Dynamic color: enabled on Android 12+; older devices show the existing explanation.
- Icon appearance: keeps its separate editor. The selected background shape has a
  visible name. The fill switch uses toggleable semantics for TalkBack.
- Compact library: 40 dp artwork, 60 dp minimum row and 6 dp vertical padding;
  standard library: 48 dp artwork, 72 dp minimum row and 8 dp vertical padding.
  Row height can expand with system text size.
- Reduced motion: disables decorative theme/preview transitions and the in-app
  header entrance; skips the Compose launch intro and extra splash exit delay on
  future cold starts. Android's own launch splash and normal Material control
  feedback remain platform-controlled. The system animation-disable preference
  takes precedence over the app's preference.
- Restore default appearance: confirms the operation before resetting theme,
  dynamic-color choice, symbol settings, icon background and compact mode. It
  preserves reduced motion and the separate catalog test selection.

`InterfaceOptions` is persisted in the existing `songmeld_appearance` preferences.
MainActivity owns observable values and provides them to previews and the real
library. The Settings overview summarizes the current theme and effective palette.

## Validation

Ran `bash tools/check_appearance_preferences.sh /path/to/kotlinc` against the actual
preference store adapter, interface model and motion policy. In-memory Android
preferences and icon value doubles isolate storage behavior from UI rendering.
Checks cover saving/reopening options, defaults after reset, keeping reduced motion
and catalog selection, and system-motion precedence. This is not an Android disk
persistence, lifecycle or Compose rendering test.

Kotlin syntax parsing, XML/resource references, changelog JSON and diff whitespace
were also checked. See `docs/build-verification.md` for the latest Android build
status. On-device verification remains required before merging the draft PR.

Device checklist:

1. Toggle each option; restart and verify it stays selected.
2. Try system/light/dark and dynamic color; check preview, actual library and system
   bar icon contrast. Check Android 8–11 fallback colors as well.
3. Try 320 dp width and 200% font scale; all labels and actions remain reachable.
4. Enable reduced motion, restart; check header/intro and decorative transitions.
   Disable Android animations and verify the app does not override that setting.
5. Cancel the reset dialog, then confirm reset; verify motion choice is preserved.
6. Compare compact and standard rows with long titles and large system text.
7. Use TalkBack on theme selection, fill toggle and selected shape name.

## References

Implementation uses existing SongMeld components and standard Compose APIs:

- [Compose animation quick guide](https://developer.android.com/develop/ui/compose/animation/quick-guide)
- [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility)
- [Androidify theme reference](https://github.com/android/androidify/blob/main/core/theme/src/main/java/com/android/developers/androidify/theme/Theme.kt)

No third-party source files were copied.
