# SongMeld icon system — full offline catalogs

The application renders icons with **one Compose glyph component** (`ui/icons/IconCatalog.kt`) and bundles **five full font files**. Built-in interface icons use named `SongMeldIconId` entries; the search picker uses `IconReference(pack, codepoint)`. SongMeld does not depend on Compose Material Icons Extended.

| Bundled font | Pack | Variable axes |
| --- | --- | --- |
| `material_symbols_rounded.ttf` | Google Material Symbols | FILL, wght, GRAD, opsz |
| `material_symbols_outlined.ttf` | Google Material Symbols | FILL, wght, GRAD, opsz |
| `material_symbols_sharp.ttf` | Google Material Symbols | FILL, wght, GRAD, opsz |
| `material_design_icons.ttf` | Pictogrammers MDI | Static |
| `flutter_material_icons.otf` | Flutter Material Icons | Static |

Fonts reside in `app/src/main/res/font/`. The icon index assets are `app/src/main/assets/icon_catalog/{symbols,mdi,flutter}.tsv`. Each line is a hex Unicode codepoint and glyph name separated by a tab. The supplied snapshot indexes have **4,311 Symbols + 7,442 MDI + 8,629 Flutter entries** (20,382 searchable cmap entries, not distinct visual designs). An icon without a direct cmap entry does not appear in this prototype browser.

`SongMeldIconCatalog.load` parses a catalog on an IO dispatcher and caches it. The picker filters on a background dispatcher and uses `LazyVerticalGrid` to compose visible icons. Only Symbols supports the four variable-font axes; **MDI and Flutter glyphs do not change weight or fill**.

## Build and browse

The current source ZIP includes all five font binaries and matching TSV catalogs. **A Paisa APK is never required for a normal build**:

```bash
./gradlew installDebug
```

Navigate to **Settings → Appearance → Icon appearance → Browse icons** to filter and select any listed glyph offline. The current picker saves a *test selection* in SharedPreferences; it deliberately does **not** replace global UI icons. Future Tags/Playlists can consume that selection without altering SongMeld's logo or album artwork.

## Updating from the original publishers

The supported maintenance entry point is **`tools/sync_official_icon_fonts.py`**. It pins source revisions for Google Material Symbols, Pictogrammers MDI, and the font artifact referenced by Flutter's SDK. It downloads font binaries and attribution files, checks OpenType glyph tables and Symbols axes, then **regenerates all three indexes for those exact fonts**. It writes a SHA-256 lockfile after successful installation.

Python 3.10+ and FontTools are required for synchronizing, **not for building**:

```bash
python3 -m venv .venv-icons
.venv-icons/bin/python -m pip install fonttools
.venv-icons/bin/python tools/sync_official_icon_fonts.py
.venv-icons/bin/python tools/sync_official_icon_fonts.py --verify
./gradlew installDebug
```

Do not commit `.venv-icons/` or Python caches. The repository's `.gitignore` excludes them. On **the current uploaded snapshot**, `tools/icon-fonts.lock.json` is absent: `--verify` correctly reports *No lockfile* until the first successful sync. Existing font files in the ZIP are present, but the absence of that lockfile means we **cannot assert they were fetched by this tool from upstream**. Do not fabricate a provenance lock from local files. If a download or validation fails, leave the current font resources intact; inspect the error and update the pinned source only after checking upstream.

The sync script also accepts `--offline-dir /path/to/font-directory` to validate local files, but this mode explicitly records **local-file provenance**, not a claimed official download.

> Older tools that extracted fonts from a Paisa APK or generated the old 14-glyph subset have been removed. The full-font approach intentionally trades APK size for an unrestricted offline picker.

## Keeping new features compatible

- For a fixed UI icon: add a named entry to `SongMeldIconId`, with the corresponding Unicode codepoint present in the full Symbols font.
- For a user-selected icon: save the pack identity and stable glyph identifier. The current test picker stores a pack and codepoint; before a production Tags/Playlists release, plan for codepoint changes across upstream font revisions (a stable icon name plus pack may be preferable).
- Keep the logo, launcher, splash, and album covers independent of the icon appearance settings.
- Treat `FILL`, `wght`, `GRAD`, `opsz` as Material Symbols properties only.

## Licenses and release checks

Material Symbols / Material Icons come from Google's [material-design-icons](https://github.com/google/material-design-icons) project (Apache 2.0). MDI font and glyphs come from [Pictogrammers](https://github.com/Templarian/MaterialDesign-Webfont) and have the Pictogrammers Free License, with special care for third-party brand marks. Flutter supplies the classic Material Icons font as part of the [Flutter SDK](https://github.com/flutter/flutter). The available license files are in `app/src/main/assets/licenses/`; a successful upstream sync adds the Flutter artifact's own license notice as well. Before release, verify the installed files and preserve required license/attribution notices.

For performance, measure APK size and memory on-device, open the picker and scroll through all packs, check dynamic themes, and test restarting the app with a selected icon. `--verify` checks the local SHA-256 lock contents; it does **not** cryptographically attest the publishers' signatures.
