# SongMeld Settings UI (19 October 2026)

## Principles

- **Vietnamese is the app's default locale.** The default `res/values/strings.xml` contains Vietnamese text, including the library and permission screens. `values-vi` mirrors those resources. Other locales can be added as `values-en`, etc., without changing Kotlin code.
- The Settings overview has only two grouped, compact destinations: **Giao diện** and **Giới thiệu**. Circular pastel accents are inspired by the old SongMeld v1 code, not copied artwork.
- The medium top app bar uses `MediumTopAppBar` and `exitUntilCollapsedScrollBehavior` (public Material 3 APIs). The title uses a bolder, slightly larger expanded treatment. **On a short Settings page, there may not be enough scrollable content to trigger collapse**; long child pages demonstrate the behavior.
- Theme choices use three horizontally aligned illustrated tiles (System / Light / Dark), stored via the existing `AppearancePreferences` store.
- Icon appearance uses an on/off **Fill** switch instead of a continuous fill slider. The remaining controls have discrete, intentional ranges: weight 300–600 (step 50), optical size 20–40 (step 4), grade -25–75 (step 25). Existing out-of-range saved values are normalized via `validated()`.
- The twelve shape presets are drawn locally in Compose (no additional icon font dependency). They affect only icon presentation backgrounds, not album covers or branding. Shape choices remain backwards-compatible with the three old IDs.
- The full offline icon catalog and variable fonts remain unchanged.

## References

- [Android Material 3 Segmented Buttons](https://developer.android.com/develop/ui/compose/components/segmented-button) (used for icon style)
- [Android Material 3 Top App Bars](https://developer.android.com/develop/ui/compose/quick-guides/content/display-app-bar)
- [Material 3 Expressive Catalog](https://github.com/emertozd/Compose-Material-3-Expressive-Catalog)
- [SongMeld v1](../app/src/main/java/moe/ktandth/songmeld/ui/settings/SettingsScreen.kt) (inspiration for pastel settings rows)
- Paisa app (visual reference supplied by developer)

## Manual QA on device

1. Build with `./gradlew installDebug`.
2. From the main header: Settings → Giao diện → Chủ đề. Change three theme choices and confirm visual update and persistence across relaunch.
3. In Kiểu biểu tượng: toggle Fill; drag three sliders and verify preview changes and safe numeric ranges. Switch Rounded/Outlined/Sharp.
4. Select every background shape; verify the icon preview/background remains visible and the selected shape persists across relaunch.
5. Use Khám phá biểu tượng and verify full offline browsing still works, including Back navigation.
6. Check theme/colors in both light and dark mode, including Android 12 Dynamic Color. Long pages should collapse their MediumTopAppBar.
