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

## SongMeld design decisions

1. **Overview:** retain exactly two navigation choices (Appearance and About), but add a simple tonal SongMeld identity surface above a compact grouped navigation block.
2. **Material tokens:** use `MaterialTheme.colorScheme.primaryContainer`, `secondaryContainer`, `tertiaryContainer`, and their matching on-colors for the colored settings icons. Avoid manually hardcoded accent palettes that disregard wallpaper Dynamic Color or dark mode.
3. **Hierarchy:** use larger title typography and clearer section headings while preserving the Vietnamese-first information architecture.
4. **Touch comfort:** keep full-row clickable settings, grouped round outer corners and comfortably sized controls, including when users increase font scale.
5. **Appearance:** preserve the three theme choices (System / Light / Dark), Live Preview, dynamic color option (Android 12+ only), icon appearance entry, compact music list and Reduce Motion.
6. **Accessibility:** maintain semantic switch / radio roles, OS-restricted toggle disabled state and reduced-decorative-motion policy. Keep reset confirmation and existing persistence callbacks unchanged.
7. **Related screens:** bring About, icon customization, icon catalog and legal/change history screens onto a common surface and type scale, keeping their functionality and links.
8. **No borrowed implementation:** layouts are independently written in SongMeld Compose. The original assets, credit records and bundled licensing information are unchanged.

## Validation checklist

- [x] Review existing navigation and state callbacks
- [x] Implement updated styling on independent feature branch
- [x] Check balanced Kotlin delimiters and referenced settings resource names
- [ ] Compile debug APK on Android SDK-enabled CI or local developer machine
- [ ] Visual review at compact width / font scale >= 1.3 / Android 11 and Android 12+
- [ ] TalkBack traversal, high-contrast/dark mode and reduced-motion smoke tests

The remaining checkboxes are runtime checks, not claimed as completed.