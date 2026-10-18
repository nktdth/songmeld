# SongMeld branding assets

SongMeld uses a **fixed set of vector assets** for its approved music-note/wrench symbol. There is no asset-generation step, Python dependency, or branding build task. Normal Gradle builds consume the Android resources directly.

## Three logo forms

| Form | Source asset | Android resource | Where to use it |
| --- | --- | --- | --- |
| **Full badge** (wavy/scalloped outline + note) | [`branding/songmeld-logo.svg`](../branding/songmeld-logo.svg) | `res/drawable/songmeld_logo.xml` | In-app header, branded surfaces, splash artwork |
| **Standalone mark** (note/wrench without badge) | [`branding/songmeld-mark.svg`](../branding/songmeld-mark.svg) | `res/drawable/songmeld_mark.xml` | Minimal icon placements, future actions or branding |
| **Android adaptive/themed icon** (note only) | — | `res/mipmap-anydpi-v26/ic_launcher.xml`, `ic_launcher_round.xml`, plus `res/drawable/ic_launcher_foreground.xml` and `ic_launcher_monochrome.xml` | Device launcher; system applies the launcher mask and themed-icon tint |

The GitHub README uses [`branding/songmeld-logo-orange.svg`](../branding/songmeld-logo-orange.svg), a static full-badge export in orange (`#FFA069`) and brown (`#57331E`) because GitHub cannot resolve Android system color resources. The source SVG contains the approved badge and note geometry; the standalone SVG reuses its note path.

All `res/` paths above are relative to `app/src/main/`. Keep vector XMLs in the Android app: an SVG file under `branding/` is documentation/design artwork, not a directly consumable Android resource.

## Geometry and spacing

The standalone music/wrench note has its optical centering correction **already included in its path data** (relative to the first design: left by 0.42 and down by 0.38 in the 24×24 viewBox). Avoid adding another note-only translation in the header, splash, or launcher.

- In-app full badge: 24×24 vector with both shapes and runtime colors.
- Adaptive launcher foreground/monochrome layers: 108dp resources; the note is uniformly scaled to 0.80 around its center to respect launcher safe areas. The system, **not the SVG outline**, creates the final launcher mask.
- Animated system splash: 36×36 vector viewBox, with badge and note placed at +6,+6 and centered around (18,18). Only the badge rotates and morphs; the note stays still.
- Pre-Android-12 Compose splash: the same approved badge contour is stored in `ui/splash/SongMeldSplashIntro.kt` to animate it without starting another animation on Android 12+.

## Colors

| Version | Badge/background | Note/foreground |
| --- | --- | --- |
| Android 8–11 | `#FFA069` | `#57331E` |
| Android 12+ | `@android:color/system_accent1_100` (light) / `_700` (dark) | `@android:color/system_accent1_900` (light) / `_100` (dark) |
| Android 13+ themed icons | The launcher controls the monochrome icon tint | The one-color mark is provided by `ic_launcher_monochrome.xml` |

The actual wallpaper-derived palette depends on the device and launcher. The main application continues to use Material 3 Dynamic Color on Android 12+, with warm fallback colors on older versions. See [UI colors](ui-header-and-colors.md) and [splash](splash-screen.md).

## If the logo is changed in the future

The branding has been finalized for now, so assets are maintained **manually**. Only perform these steps if changing the SVG silhouette:

1. Edit `branding/songmeld-logo.svg`, then update the orange full-badge SVG and standalone SVG to use the approved final path shapes.
2. Copy the matching path data into `songmeld_logo.xml`, `songmeld_mark.xml`, the launcher foreground/monochrome vectors, and the splash note/vector resources. Preserve each resource's **color references, group placement, scale and animation target names**.
3. Update `BADGE_PATH` in `SongMeldSplashIntro.kt` and `android:valueFrom` in `animator/songmeld_badge_morph.xml`. If the outer silhouette changes, update `android:valueTo` with path commands compatible with `valueFrom` and visually check the morph.
4. Preview regular and themed icons on Android 8–11, 12+, and 13+, in light and dark modes; build with `./gradlew assembleDebug`.

**Do not** change the vector geometry just to reduce file count: some distinct Android resources are required to preserve different masks, palettes and animations.

> Branding note: The current scalloped badge outline resembles an earlier visual reference. Before a wide public branding release, consider whether a more distinct exterior outline is desirable. The standalone note is a separate mark.
