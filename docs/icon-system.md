# SongMeld Material Symbols Variable Font

SongMeld's UI icons are rendered by `ui/icons/SongMeldIcons.kt` as glyphs from Google **Material Symbols Variable Fonts** (not `androidx.compose.material:material-icons-extended`, and not drawable XMLs). Branded assets, splash assets and cover art are separate and unchanged.

## Fonts and attribution

- Source: [Google Material Design Icons](https://github.com/google/material-design-icons/tree/master/variablefont), Apache License 2.0 (see `app/src/main/assets/licenses/Apache-2.0-Material-Symbols.txt`).
- Font family: Google Material Symbols Rounded / Outlined / Sharp; version **2.960**, copyright Google LLC 2026.
- The current bundled font subsets were produced from the corresponding original Google Material Symbols fonts included in the **user-provided Paisa APK**, then reduced locally with fontTools. This environment could not download the files from GitHub directly. For reproducible refreshes, obtain these three upstream TTFs from Google's repository and run `tools/subset_material_symbols.py` against them.
- Only **14 glyphs** are included in each file. These are *true variable fonts*: the axes `FILL`, `wght`, `GRAD`, `opsz` and associated variation data remain present. Total uncompressed font size is approximately 65 KB, compared with more than 30 MB for the three original full fonts.

## Runtime behavior

`SongMeldIcon(id, contentDescription, size, tint)` chooses the appropriate font and renders a Unicode private-use glyph with Compose `BasicText`. `MaterialSymbolAppearance` provides:

| Setting | Range | Initial value |
| --- | --- | --- |
| Style | Rounded, Outlined, Sharp | Rounded |
| Fill (`FILL`) | 0.0..1.0 | 0 |
| Weight (`wght`) | 100..700 | 400 |
| Grade (`GRAD`) | -50..200 | 0 |
| Optical size (`opsz`) | 20..48 | 24 |

The icon settings are available through the header menu → Settings → Icon appearance. They are persisted using `AppearancePreferences` (SharedPreferences), including the previous version's background shapes. Older `icon_style=filled` preferences migrate to Rounded with `FILL=1`.

`LocalMaterialSymbolAppearance` propagates the user's choices to all SongMeld UI icons. The icon's backdrop shape is provided separately by `LocalSongMeldIconShape` and is only used where a colored background is intentional. The image assets for album covers and the app logo are never transformed by icon preferences.

The UI intentionally doesn't expose an arbitrary icon catalog yet. The font is subsetted to the known `SongMeldIconId` names so a future searchable icon picker will require expanding the subset (or loading a separate icon pack). Do NOT display arbitrary strings or glyph names from this subsetted font.

## Adding a new icon

1. Find the glyph's Google Material Symbols Unicode codepoint in the upstream `*.codepoints` file.
2. Add it to the `CHARACTERS` mapping in `tools/subset_material_symbols.py` and run that script against the three full upstream TTFs.
3. Add the same codepoint to the `SongMeldIconId` enum. Use `SongMeldIcon(id=...)` at call sites, including a meaningful `contentDescription` for interactive controls (or `null` for decorative elements).
4. Inspect the preview at 20dp, 24dp, 48dp with Filled/Outlined/Sharp settings, and test accessibility font scaling.
5. Build and test Android locally: `./gradlew assembleDebug` and `./gradlew installDebug`.

Full Google Material Symbols names and graphics: https://fonts.google.com/icons

## Licensing and release checks

The fonts are Apache-2.0 licensed, independently of SongMeld's repository license. Keep the bundled Apache 2.0 license, attribution and these source notes when distributing the app. After adding glyphs or changing the original fonts, compare APK sizes and verify four OpenType axes remain.
