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
- Moderate density rather than large touch targets everywhere: settings icon discs **50dp**, glyphs **26dp**, theme tiles **28dp** and smaller padding.
- Music library rows: **72dp standard / 64dp compact** minimum heights, album artwork **50dp / 44dp**; group outer corners **22dp**.
- Default Material Symbols glyphs use **24dp** when no component-specific size is set.

## SongMeld design decisions

1. **Overview:** keep only Appearance and About (other old v1 account/download features are not implemented in the current app), with v1-style Preferences / SongMeld headings instead of a decorative hero.
2. **Color:** v1-inspired pastel icon accents for entries, and semantic Material 3 tokens for app theming and selection state. Preserve light/dark legibility and system-driven Dynamic Color in the app.
3. **Hierarchy:** use the v1-inspired small top bar and moderate title/body typography rather than enlarged headers and panels.
4. **Touch comfort:** keep full-row click targets, sensible grouped corners and comfortable default density; decorative icon size is smaller than the interaction area.
5. **Appearance:** preserve all three theme choices, live preview, Android 12+ Dynamic Color, icon customization, compact library, Reduce Motion and reset confirmation.
6. **Accessibility:** preserve semantic Switch/RadioButton roles, available/disabled states and reduced-motion policy; test TalkBack and font scaling on device.
7. **Related screens:** harmonize About, icon customization, catalog and legal/changelog pages with moderate visual density without changing callbacks.
8. **No third-party code copied:** Material 3 / other projects were used as design references; v1 is an owner-controlled source. Existing bundled notices remain unchanged.

## Validation checklist

- [x] Review existing navigation and state callbacks
- [x] Implement updated styling on independent feature branch
- [x] Check balanced Kotlin delimiters and settings resource names
- [ ] Compile debug APK on Android SDK-enabled CI or local developer machine
- [ ] Visual review at compact width / font scale >= 1.3 / Android 11 and Android 12+
- [ ] TalkBack traversal, high-contrast/dark mode and reduced-motion smoke tests

The remaining checkboxes are runtime checks, not claimed as completed.