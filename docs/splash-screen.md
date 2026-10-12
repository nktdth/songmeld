# SongMeld splash: color and motion

The splash icon never displays text. Only the **outer badge** rotates and softly
changes curvature; the **center wrench/music-note mark stays fixed**.

## Shared colors

Splash and launcher icons read the same Android resources:

- `@color/songmeld_launcher_background` — **badge fill** (API 26–30: `#FFA069`).
- `@color/songmeld_launcher_foreground` — **note fill** (API 26–30: `#57331E`).
- API 31+ overrides both resources from `system_accent1_*` wallpaper colors,
  including separate day/night tones.
- `songmeld_splash_background` is the surrounding canvas, not the badge:
  warm neutral on older Android and wallpaper-derived `system_neutral1_*`
  on Android 12+.

The checked-in `branding/songmeld-logo.svg` is **unchanged**. These are
runtime colors, not edits to the official static branding file.

## Startup without the static-then-rotating jump

### Android 12+ (API 31+)

`values-v31/themes.xml` uses `songmeld_splash_animated`, an Android
AnimatedVectorDrawable (AVD). The platform itself animates the badge during
startup. `MainActivity` fades away that *same* system splash view, without
switching to a second spinning Compose logo. This prevents the apparent
rotation reset seen in the first implementation.

- `songmeld_splash_vector.xml` — two independent paths/groups, badge and note.
- `animator/songmeld_badge_rotation.xml` — 7.2 s per clockwise turn.
- `animator/songmeld_badge_morph.xml` — subtle path-data deformation that loops.
- `MainActivity` avoids the first-frame jump by waiting only for a modest
  visible interval (up to 600 ms from the platform animation start) before
  fading. Real app startup can take longer.

The platform splash may be delayed by Android startup scheduling; an app cannot
force animation frames to appear before Android has begun drawing its window.

### Android 8–11 (API 26–30)

The AndroidX splash compatibility layer **cannot animate splash icons** on
those versions. A transparent platform icon avoids a frozen brand logo; the
first visible logo is a Compose canvas that immediately begins rotating and
morphing. The initial platform-only interval therefore shows the background
with no logo. The Compose overlay fades after ~760 ms and is never shown on
configuration recreation. See AndroidX `core-splashscreen` platform limitations.

## Performance and accessibility

- Compose samples the original brand badge path once and reuses its geometry.
- Compose rotation is 7.2 s per revolution and the travelling deformation is
  gentle (~1.2% + 0.6% radial displacement).
- Android's disabled animator setting skips the extra Compose animation and
  prevents the custom extended system splash exit delay.
- Do not block startup for music scans, permissions, network requests or the
  sake of finishing a rotation. Those states belong on the actual app screen.

## Brand ownership

The path data comes from the checked-in brand SVG; if the brand is redesigned,
update both `SongMeldSplashIntro.kt` and the animated-vector base shape and
recompute compatible morph end points. The existing launcher and full logo
assets are otherwise unchanged.
