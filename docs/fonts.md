# Fonts used by SongMeld

SongMeld includes two variable font families, originally bundled in SongMeld v1:

- **Google Sans Flex** — Copyright 2015 Google LLC. All Rights Reserved.
- **Google Sans Code** — Copyright 2026 The Google Sans Code Project Authors (github.com/googlefonts/googlesans-code).

Both included font files identify their license as **SIL Open Font License, Version 1.1** in embedded font metadata. The corresponding license text is bundled in `app/src/main/assets/licenses/OFL-1.1.txt`.

- UI typography: Google Sans Flex (ROND = 100 for rounded interface text).
- Technical values / timestamps / future LRC editor: Google Sans Code (MONO = 1).
- The colors are controlled separately by MaterialTheme and Android 12+ dynamic color.

## Add the font binaries from the original SongMeld v1

This project archive contains complete Compose integration and licenses, but **not**
the binary font files. To make this Android project build and display the fonts:

1. Open the original `songmeld-v1-main.zip` archive with a file manager.
2. Go to `songmeld-v1-main/app/src/main/res/font/`.
3. Drag both `google_sans_flex.ttf` and `google_sans_code.ttf` into
   **this project's** `app/src/main/res/font/`.
4. Open the project in Android Studio/VS Code and build normally.

Expected layout:

```text
app/src/main/res/font/
├── google_sans_flex.ttf
└── google_sans_code.ttf
```

Do not rename these font files: `Font.kt` uses their resource identifiers.
Without them, Android resource compilation will fail with missing `R.font` entries.

No shell scripts or Git patches are required.
