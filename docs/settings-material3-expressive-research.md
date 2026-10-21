# Settings UI / UX — Material 3 Expressive reference study

This document records a targeted design review for the SongMeld Settings refresh. We examined the project READMEs and, where accessible, Settings Compose source implementations. The intent is to learn design principles, **not copy third-party code or artwork**. The primary references for concrete layout were PixelPlayerHQ, Rhythm, Chocola, Dustvalve Next, Lune, VIVI, Umihi and Compose-Settings.

## References reviewed

| Project | Source | Pattern / finding |
|---|---|---|
| PixelPlayer | [Repository](https://github.com/PixelPlayerHQ/PixelPlayer) | Grouped segmented cards, grouped-category navigation, tonal surfaces |
| Rhythm | [Repository](https://github.com/cromaguy/Rhythm) | Grouped preferences, section separation, active colors and icon hierarchy |
| Chocola | [Repository](https://github.com/sosauce/Chocola) | Expressive category cards and approachable music-oriented UI |
| Dustvalve Next | [Repository](https://github.com/Pingasmaster/dustvalve_next) | Section hierarchy and touch-friendly setting rows |
| Lune | [Repository](https://github.com/MrDemonc/Lune) | Large app bar, segmented card corners and list row spacing |
| VIVI Music | [Repository](https://github.com/vivizzz007/vivi-music) | Reusable grouped setting components |
| Umihi Music | [Repository](https://github.com/ilianoKokoro/umihi-music) | Reusable settings sections, concise labels and preference navigation |
| Meld | [Repository](https://github.com/FrancescoGrazioso/Meld) | Preferences for music services, content organization |
| Compose-Settings | [Repository](https://github.com/alorma/Compose-Settings) | SegmentedListItem expressive layout, navigation/toggle components |
| MaterialGallery | [Repository](https://github.com/NUmeroAndDev/MaterialGallery-android) | Material component catalog and baseline conventions |
| PixelPlayer source | [Repository](https://github.com/theovilardo/PixelPlayer) | Concept reference only; license must be checked before reuse |
| Material 3 developer guide | [Repository](https://developer.android.com/develop/ui/compose/designsystems/material3) | MaterialTheme tokens, components and semantic colors |

## SongMeld v1 as the primary visual reference

**Source:** Private repository `nktdth/songmeld-v1`, `SettingsScreen.kt`, `SettingsOverviewComponents.kt`, `GeneralSettingsScreen.kt`, and `ui/components/SettingsComponents.kt` (read using the authorized GitHub connection).

The owner preferred the v1 Settings design over the initial expressive redesign. In response, this revision restores the v1 visual hierarchy while keeping the modern project's existing options, navigation and persistence.

- Compact fixed-height top app bar with bold heading (no oversized collapsing toolbar).
- Lightly separated sections, grouped round rows and v1-like pastel round icon backgrounds.
- No oversized branded banner on Settings overview; only functional Appearance and About entry points.
- Soft pastel contrast is adjusted for light and dark themes; functional theme controls still use Material 3 semantic tokens.
- Moderate density rather than large touch targets everywhere: settings icon discs **44dp**, glyphs **23dp**, compact horizontal theme buttons and smaller padding.
- Music library rows: a single **68dp** minimum height, album artwork **46dp**; group outer corners **20dp**.
- Default Material Symbols glyphs use **24dp** when no component-specific size is set.

## SongMeld design decisions

1. **Overview:** keep only Appearance and About (other old v1 account/download features are not implemented in the current app), with v1-style Preferences / SongMeld headings instead of a decorative hero.
2. **Color:** v1-inspired pastel icon accents for entries, and semantic Material 3 tokens for app theming and selection state. Preserve light/dark legibility and system-driven Dynamic Color in the app.
3. **Hierarchy:** use the v1-inspired small top bar and moderate title/body typography rather than enlarged headers and panels.
4. **Touch comfort:** keep full-row click targets, sensible grouped corners and comfortable default density; decorative icon size is smaller than the interaction area.
5. **Appearance:** preserve all three theme choices, real-time theme controls, Android 12+ Dynamic Color, icon customization. Compact Library, in-app Reduce Motion and Reset Appearance have been retired.
6. **Accessibility:** preserve semantic Switch/RadioButton roles, available/disabled states and Android system animator preferences; test TalkBack and font scaling on device.
7. **Related screens:** harmonize About, icon customization, catalog and legal/changelog pages with moderate visual density without changing callbacks.
8. **No third-party code copied:** Material 3 / other projects were used as design references; v1 is an owner-controlled source. Existing bundled notices remain unchanged.

## Song list scrolling — ported from SongMeld v1

The original implementation was studied in the owner's `songmeld-v1` repository:

- `ui/songs/SongsScreen.kt`: right-edge `SongsFastScrollbar`, A–Z bubble, scroll progress from `LazyListState`.
- `ui/songs/SongsActionFab.kt`: scroll-to-top FAB with a direction-sensitive expanding label.

These interactions now live in `ui/songs/SongsScrollControls.kt` and are wired into the existing SongMeld local library without importing old player/download/selection logic.

- 6dp visible vertical thumb, 28dp gesture target confined to the right edge, with a rounded alphabet bubble while dragging in title A–Z sort.
- Vietnamese initials such as `Đ`, `Â` and `Ế` group under the equivalent Latin letter.
- Scrubbing works in all sort modes; only TITLE sort displays letter groups.
- The small Material-colored scroll-to-top FAB appears only after meaningful scrolling, expands its label on upward gestures, and avoids tiny-gesture flicker using distinct thresholds.
- Extra bottom padding lets the final song scroll clear of the FAB.
- The scrollbar exposes an accessibility progress range and adjustable progress action; the FAB has a Vietnamese content description.
- Decorative animations honor `LocalDecorativeMotionEnabled`.
- The v1 Add Songs FAB is **not ported** because the new app has no matching implemented add action.

## Appearance preferences: v1 horizontal theme selector

The owner requested removing the decorative Appearance preview entirely. The unused `AppearancePreview.kt` component and its three supporting strings have been deleted.

The three-mode **System / Light / Dark** selector now follows the compact SongMeld-v1 horizontal segmented control, with these refinements:

- Material Symbols icon **before** the text in each 52dp-high option.
- One highlighted pill underneath the choices, moving horizontally using `animateDpAsState` when the selected theme changes (270ms).
- Material 3 semantic colors for selected state, with individual content color transitions.
- `selectableGroup` and radio-button roles retained for accessibility; labels allow up to two lines on narrow displays.
- When Android disables decorative motion, the selection switches without sliding animation.
- Appearance preferences, persistence callbacks and theme handling are otherwise unchanged.

## Theme layout and AMOLED black option

- Shortened the segmented theme's first label from **Theo hệ thống** to **Hệ thống**; both outer and animated selected-pill surfaces use full pill shapes.
- The segmented selector now precedes the **Chủ đề** heading, with no redundant theme explanation.
- **Chủ đề** groups AMOLED black first (only when the effective theme is dark) and Android 12+ dynamic wallpaper colors second. The latter remains disabled below Android 12.
- `AppearancePreferences.amoledBlackEnabled` persists the AMOLED preference (default off). The Reset Appearance option was retired.
- `SongMeldTheme` preserves existing static/wallpaper accents but uses `#000000` for dark-mode background and surface and nearly-black elevation containers when AMOLED is enabled. AMOLED does not override Light mode, including System when the device is currently light.
- Activity/app callbacks update Compose theme immediately; the Settings summary reflects an active AMOLED dark appearance.

## Shared toggle thumb and app-wide density pass

The owner requested a less oversized baseline and explicit on/off visual state on toggles.

- Added a reusable `ui/components/SongMeldSwitch.kt` which renders a Material Symbols **check ✓** in the thumb when on and a **close ×** in the thumb when off, using the Material 3 `Switch.thumbContent` / `SwitchDefaults.IconSize` APIs.
- Wired this component into Appearance toggles (AMOLED and Dynamic Color) and the icon-appearance Fill preference. The parent row retains one accessible `Role.Switch` click target instead of adding a competing clickable child.
- Rebalanced app-wide typography toward standard Material 3: `titleMedium` 18→16sp, `bodyLarge` 18→16sp, `bodyMedium` 16→14sp and `bodySmall` 14→13sp. Other titles/labels were reduced slightly, keeping Android font scaling.
- Reduced decorative/presentation sizes in Settings, About, Icon Appearance, icon catalog, informational pages and the main SongMeld header. Interactive app-bar buttons still use large touch targets.
- Local-song row minimum heights now **68dp** and album artwork **46dp**, with more restrained padding and grouped corner radii.
- No device-build success is implied by static checking. After `installDebug`, verify both switch states, disabled Dynamic Color (below Android 12), font scaling and no clipping of song metadata.

## Minimal Settings pass: retired options

- Removed Compact Library, Reduce Motion and Reset Appearance from Settings and their unused persistence/UI plumbing. Songs now use one balanced list layout.
- Decorative animations still honor Android's system animator state via `LocalDecorativeMotionEnabled`.
- The theme selector clips pressed and long-pressed indications to fully rounded `CircleShape`, not just the selected pill.
- AMOLED appears only when the effective theme is dark: explicitly Dark, or System when Android is in dark mode. Its saved preference survives switching temporarily to Light.
- Dynamic Color remains accessible by itself in Light mode and uses a fully rounded single row.
## AMOLED row expand/collapse motion

The previous conditional `if (darkModeActive)` removed the AMOLED row instantly when switching Light / Dark. We reviewed implementation patterns from three M3/M3 Expressive Compose repositories:

- [Rhythm Settings search](https://github.com/cromaguy/Rhythm/blob/main/app/src/main/java/chromahub/rhythm/app/shared/presentation/screens/settings/SettingsSearch.kt) uses `AnimatedVisibility` with vertical expand/shrink and fade.
- [PixelPlayer's LibraryActionRow](https://github.com/PixelPlayerHQ/PixelPlayer/blob/main/app/src/main/java/com/theveloper/pixelplay/presentation/components/subcomps/LibraryActionRow.kt) combines conditional visibility and animated layout transitions.
- [Chocola SettingsPlayback](https://github.com/sosauce/Chocola/blob/main/app/src/main/java/com/sosauce/chocola/feature/settings/presentation/SettingsPlayback.kt) conditionally reveals settings rows using `AnimatedVisibility`.
- Android official documentation: [Animation modifiers and composables](https://developer.android.com/develop/ui/compose/animation/composables-modifiers).

**Chosen implementation for SongMeld:** one `AnimatedVisibility` for the AMOLED row using 270ms vertical expansion/collapse and brief crossfading, while keeping the entire 4dp inter-row gap inside the animated content. This prevents the spacer from disappearing abruptly after the animation. The Dynamic Color row stays mounted and its top corner radius animates from 8dp (connected pair) to 28dp (solo card) on the same 270ms timeline. The behavior works when Android system Dark Mode changes in System mode, and respects `LocalDecorativeMotionEnabled` with immediate transitions when Android animations are disabled. No external implementation was copied.

## Validation checklist

- [x] Review existing navigation and state callbacks
- [x] Implement updated styling on independent feature branch
- [x] Check balanced Kotlin delimiters and settings resource names
- [ ] Compile debug APK on Android SDK-enabled CI or local developer machine
- [ ] Visual review at compact width / font scale >= 1.3 / Android 11 and Android 12+
- [ ] TalkBack traversal, high-contrast/dark mode and reduced-motion smoke tests

The remaining checkboxes are runtime checks, not claimed as completed.