# UI readability and sizing

Branch starts from main after PR #6 (95a166f).

## Reading scale

The shared typography keeps Google Sans Flex and Android sp scaling. Display and
headline roles keep Material defaults; body and control roles gain size and line
height so the change reaches Settings, About, documents, dialogs and the library.

| Role | Before | Now | Line height now |
| --- | --- | --- | --- |
| Body small | 12sp | 14sp | 20sp |
| Body medium | 14sp | 16sp | 24sp |
| Body large | 16sp | 18sp | 28sp |
| Title small | 14sp | 16sp | 24sp |
| Title medium | 16sp | 18sp | 26sp |
| Title large | 22sp | 24sp | 30sp |
| Label small | 11sp | 12sp | 16sp |
| Label medium | 12sp | 14sp | 20sp |
| Label large | 14sp | 16sp | 24sp |

License text uses body medium (16sp) instead of body small. It is split at existing
blank lines into selectable paragraphs, bounding individual text layouts at large
font scales while preserving the document wording. Timecodes retain the code font
at the larger body-small size.

## Visual scale and layout

- Settings/About entry badges: 56dp background with 28dp icon (previously 38–40dp
  backgrounds and 20–22dp icons). Settings entry padding increases to 18dp.
- Default action glyphs: 26dp, within existing Material button touch targets.
  Entry chevrons are 24dp. Theme symbols are 34dp.
- About logo: 128dp; developer avatars: 72dp. Information headers: 72dp badge,
  36dp symbol. Empty-library/search illustrations: 96dp badge, 44dp symbol.
- Library artwork: 56dp standard / 48dp compact; minimum rows: 80dp / 68dp.
  Row heights may grow for larger text. Appearance preview uses matching artwork.
- Icon browser: at least 96dp cell width, multiplied by system font scale above
  1.0; minimum 100dp cell height, 36dp glyph, wrapping 14sp labels. Larger text
  reduces the number of columns. Selected preview is 68dp with a 38dp glyph.
- Theme tiles share height when a label wraps. They stack on narrow screens or
  larger font scales. Icon-style choices also stack rather than truncate on
  narrow screens/large fonts. Slider titles receive flexible width beside values.

## Verification

`clean :app:assembleDebug :app:lintDebug` passes with the existing project dependency
versions. Kotlin/Compose compilation and debug APK assembly pass. Android Lint
reports 0 errors and 15 existing warnings (11 KTX suggestions, two splash-vector
notices, one redundant resource qualifier and one unused branding drawable).
Resource XML, changelog JSON and diff whitespace checks also pass. No suppressions
or lint baseline were added.
No new behavioral tests are added for size-only changes. Device visual checks are
still required at 320dp and 360dp widths, landscape, and 100%/130%/200% font scales:
check theme/style selections, slider values, long developer names, license text,
icon-grid labels, library rows, keyboard access and standard/compact preview.
Also check light/dark colors and TalkBack selection semantics.
