# SongMeld offline icon catalog — full font experiment

SongMeld uses a **single Compose glyph renderer** with three complete Google Material Symbols variable fonts plus two static-font packs. It does not use Material Icons Extended. The old 14-symbol subset has been replaced; every Unicode-mapped glyph in these provided font versions can be searched and rendered without network access.

## Included fonts

| Android resource | Pack | OpenType variation axes | Source |
| --- | --- | --- | --- |
| `material_symbols_rounded.ttf` | Symbols | FILL, wght, GRAD, opsz | Google Material Symbols v2.960 |
| `material_symbols_outlined.ttf` | Symbols | FILL, wght, GRAD, opsz | Google Material Symbols v2.960 |
| `material_symbols_sharp.ttf` | Symbols | FILL, wght, GRAD, opsz | Google Material Symbols v2.960 |
| `material_design_icons.ttf` | MDI | None | Pictogrammers / MaterialDesign-Webfont |
| `flutter_material_icons.otf` | Flutter Material | None | Flutter Material Icons |

**Font provenance:** The matching font binaries were identified for this experiment from the **Paisa APK explicitly supplied by the user**. Each project lists its own redistribution license. To update from source, download verified font binaries and regenerate the index with `tools/build_full_icon_catalog.py`. Do not mistake the extraction source for the original publisher.

These are not all variable fonts. `MaterialSymbolAppearance` (Rounded/Outlined/Sharp and FILL, wght, GRAD, opsz) applies only to the Symbols pack; MDI and Flutter Material Icons remain static. App branding and artwork are unaffected.

### Offline indexes

FontTools extracts mappings from the Unicode cmap into three UTF-8 tab-separated text files in `app/src/main/assets/icon_catalog/`:

- **Symbols:** 4,311 searchable codepoints (shared by all 3 variations)
- **MDI:** 7,442 searchable codepoints
- **Flutter Material:** 8,629 searchable codepoints

Total: **20,382 selectable cmap entries**, not 20,382 unique designs, and not a count of all internal OpenType glyphs. Icons with no direct cmap entry (such as any ligature-only glyphs) are outside this prototype's picker.

Each line is `<hex codepoint>\t<glyph name>`. The names come from the actual bundled font version. No network calls, runtime font parsing or external search API are necessary. `SongMeldIconCatalog.load` reads catalogs off the main thread and caches per pack. `LazyVerticalGrid` composes only visible entries; filtering runs on `Dispatchers.Default` with a short debounce.

## Install fonts once on the developer machine

The distributed **code-only patch does not embed font binaries**. After applying the patch on the `feat/material-symbols-variable-font` branch, run:

```bash
python3 tools/install_full_icon_fonts.py --paisa /path/to/Paisa_2026.09.1210_antisplit.apk
./gradlew installDebug
```

The installer uses Python's standard library and validates SHA-256 hashes for all five fonts **before writing any**, to guarantee the supplied lookup catalogs and font versions match. It replaces the three existing subset fonts in `res/font/` and adds the two static fonts. It preserves Google Sans Flex/Code and branding assets. The APK is not checked into Git and can stay outside the repository.

To install from independently acquired full-font binaries, use `--directory /path/to/renamed-fonts/`; the filenames and hashes must match. For a future different font release, update catalogs and expected hashes together.

## UI integration

**Settings → Appearance → Icon appearance → Browse icons** exposes all three icon packs, an offline search and live preview. Tapping a glyph **persists a test selection** (`IconReference(pack, codepoint)`) via SharedPreferences. Returning to the picker or restarting SongMeld restores the selection. It intentionally **does not replace the application's navigation/icons**: the chosen icon will be reusable by future Tags/Playlists but those features are not implemented yet.

`SongMeldIconId` remains a small enum for stable built-in UI semantics; custom icons use `IconReference`. Never store `R.font`/`R.drawable` numeric IDs in user data.

## Regenerating the indexes

Install FontTools (`pip install fonttools`), then run:

```bash
python tools/build_full_icon_catalog.py \
  --symbols-rounded app/src/main/res/font/material_symbols_rounded.ttf \
  --symbols-outlined app/src/main/res/font/material_symbols_outlined.ttf \
  --symbols-sharp app/src/main/res/font/material_symbols_sharp.ttf \
  --mdi app/src/main/res/font/material_design_icons.ttf \
  --flutter app/src/main/res/font/flutter_material_icons.otf
```

The legacy `tools/subset_material_symbols.py` remains available for a future Lite build; do not run it over the bundled full-font experiment unless deliberately restoring subsets.

## Attribution and licensing

- Google Material Symbols and Material Icons: https://github.com/google/material-design-icons , **Apache 2.0**. See `app/src/main/assets/licenses/Apache-2.0-Material-Symbols.txt`.
- Pictogrammers Material Design Icons: https://github.com/Templarian/MaterialDesign-Webfont , **Pictogrammers Free License** (icon/font distribution under Apache 2.0; code MIT). See `Pictogrammers-Free-License.txt`.
- Flutter's Material Icons font is derived from Google's Material Icons; refer to Google's Material Icons Apache 2.0 terms and Flutter's attribution guidance.

Note: some community icon names/glyphs may represent third-party trademarks. Review branding usage before shipping them in a product.

## Performance and release checklist

- After local installation, fonts are ~35.8 MiB uncompressed in resources, plus a small catalog (roughly 0.45 MiB before compression). Final APK size must be measured with a local Android build.
- Benchmark opening picker, grid scrolling and changing pack on a low-end device; monitor app start and memory because font caches consume RAM.
- Verify `./gradlew assembleDebug` and `./gradlew installDebug`; validate dynamic theme, TalkBack and selection persistence.
- Avoid accidentally shrinking these full fonts during packaging: all mapped glyphs must remain available to the dynamic picker.
