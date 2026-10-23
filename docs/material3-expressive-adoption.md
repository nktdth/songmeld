# Native Material 3 Expressive adoption

This branch adopts upstream Compose Material 3 `1.5.0-beta02` while retaining Compose BOM `2026.09.00` for the other artifacts.

Google's `MaterialExpressiveTheme` now supplies upstream expressive motion and component defaults, while SongMeld retains its wallpaper-based and static color schemes, AMOLED overrides, and Google Sans Flex typography.

Library pull-to-refresh and initial loading now use Google's experimental `LoadingIndicator` API instead of custom or older progress indicators.

**Validation needed:** This update was committed through GitHub without a local Android SDK build. Run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`; test refresh gestures and light/dark/AMOLED on device before merging. The Material 3 dependency is a beta and may cause compatibility changes.
