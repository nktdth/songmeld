# Typography and bundled fonts

SongMeld uses two variable font families originally integrated from SongMeld v1:

| Family | Resource | Use |
| --- | --- | --- |
| **Google Sans Flex** | `app/src/main/res/font/google_sans_flex.ttf` | Default Material 3 typography, `ROND = 100` for the rounded interface |
| **Google Sans Code** | `app/src/main/res/font/google_sans_code.ttf` | Monospaced durations; intended for later LRC timestamps and technical fields (`MONO = 1`) |

`ui/theme/Font.kt` registers both families and their weight variations. `ui/theme/Type.kt` applies Google Sans Flex to Material 3 text roles and defines `SongMeldCodeStyle` for technical values. `ui/theme/Theme.kt` installs the typography into `MaterialTheme`. Typography is separate from Android 12+ Dynamic Color and the older warm-color fallback.

## License and resources

- Google Sans Flex — Copyright 2015 Google LLC. All Rights Reserved.
- Google Sans Code — Copyright 2026 The Google Sans Code Project Authors (github.com/googlefonts/googlesans-code).
- Both original font resources report **SIL Open Font License 1.1** in embedded metadata. The corresponding license is kept at `app/src/main/assets/licenses/OFL-1.1.txt`.

The source repository contains both `.ttf` resources. Their names must remain lowercase with underscores so that Android generates `R.font.google_sans_flex` and `R.font.google_sans_code`. If either file is missing from a local copy, restore it from your own checkout before building; otherwise Kotlin resource references will not resolve.
