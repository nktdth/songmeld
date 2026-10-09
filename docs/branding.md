# SongMeld branding

SongMeld uses two related but distinct assets. Keep them separate so changing
Android launcher masks cannot accidentally alter the full brand logo.

| Asset | Source | Usage |
| --- | --- | --- |
| Full logo | [`branding/songmeld-logo.svg`](../branding/songmeld-logo.svg) | GitHub, About, project documentation, future splash screens |
| Standalone mark | [`branding/songmeld-mark.svg`](../branding/songmeld-mark.svg) | Single wrench-note, without the outer badge |
| Android full logo | `app/src/main/res/drawable/songmeld_logo.xml` | Render the complete logo with Android's `painterResource` |
| Android mark | `app/src/main/res/drawable/songmeld_mark.xml` | Render the note alone inside the UI |
| Launcher foreground | `app/src/main/res/drawable/ic_launcher_foreground.xml` | Wallpaper-adaptive note with small left/down alignment correction |
| Launcher monochrome | `app/src/main/res/drawable/ic_launcher_monochrome.xml` | Monochrome mark tinted by compatible Android 13+ launchers |
| Launcher background | `@color/songmeld_launcher_background` | Orange `#FFA069` on Android 8–11; wallpaper colors on Android 12+ |

## Launcher setup

The manifest references `@mipmap/ic_launcher` and
`@mipmap/ic_launcher_round`. Both are adaptive icons (Android 8.0+):

- Foreground: the existing wrench-note silhouette only, with no outer badge.
  It uses `#754428` on Android 8–11 and the wallpaper-derived system
  accent palette on Android 12+.
- Background: `#FFA069` on Android 8–11, wallpaper-derived on Android 12+.
- Monochrome: same mark geometry, allowing supported Android 13+ launchers to
  apply their themed-icon coloring when the user enables that feature.

Resources are intentionally split across `res/values/colors.xml`,
`res/values-v31/colors.xml`, and `res/values-night-v31/colors.xml`.
Android 12 does **not** guarantee native launcher-themed icons. Its launcher
icon still uses dynamic wallpaper colors via the system palette references;
native monochrome themed icons are supported from Android 13 and depend on
launcher support and user settings. Android 12+ OEM behavior can vary.

The original SVG geometry uses a 24 × 24 viewport. Android draws the
adaptive foreground in a 108 × 108 dp layer, but launchers may mask that layer
to circles, rounded squares, or other shapes. The foreground uses an 0.80
centered scale with a small launcher-only translation (`x=-0.42`, `y=+0.38`
in the 24-unit viewport) to visually balance the larger upper flag. The same
translation is applied to the monochrome icon. Keep it inside the 66 dp
adaptive-icon safe area; do not enlarge it without checking mask clipping.

This change deliberately does **not** edit the source logo, source standalone
mark, or the Android in-app brand drawables.

## Updating the logo

1. Keep `branding/songmeld-logo.svg` as the approved full logo source.
2. If changing the note shape, sync its path into `songmeld-mark.svg`
   and the four Android drawable resources.
3. Do **not** add `logo-star` to the launcher foreground or monochrome mark.
4. Preview on both circular and rounded-square launchers and test themed icons.

> Design note: The approved full logo currently reuses the perimeter path of
> the TikTok You reference SVG that inspired it. Before a wider branding
> release, replace that perimeter with an original outline to avoid undue
> similarity. The standalone launcher mark does not contain that perimeter.
