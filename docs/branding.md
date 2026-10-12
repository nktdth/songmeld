# SongMeld branding

## Canonical icon geometry

**`branding/songmeld-logo.svg` is the single source of truth** for the outer
scalloped badge (`logo-star`) and the inner wrench/music note (`logo-note`).
The note's optical adjustment is baked into the path coordinates:
**left by 0.42 and down by 0.38** in the 24 × 24 SVG viewport, relative to the
original design. This balances the elongated upper flag without an individual
`translateX` / `translateY` in the launcher, splash, or in-app logo.

Do not repeat this adjustment in code or when embedding the logo elsewhere.
The Android launcher foreground and monochrome icon keep only a centered
`0.80` **scale** to respect adaptive-icon safe areas. The animated Android
splash uses a `+6,+6` **canvas placement** to center the complete logo in a
36 × 36 viewport; this is not an optical correction to the note.

## Asset inventory

| Asset | File | Purpose |
| --- | --- | --- |
| Canonical full SVG | [`branding/songmeld-logo.svg`](../branding/songmeld-logo.svg) | Master badge + note geometry, original purple/white presentation |
| README default icon | [`branding/songmeld-logo-orange.svg`](../branding/songmeld-logo-orange.svg) | Static orange (`#FFA069`) + brown (`#57331E`) logo for GitHub |
| Standalone SVG mark | [`branding/songmeld-mark.svg`](../branding/songmeld-mark.svg) | Note only, matching the canonical geometry |
| In-app full logo | `res/drawable/songmeld_logo.xml` | Brand-colored badge and note |
| In-app mark | `res/drawable/songmeld_mark.xml` | Note without the badge |
| Adaptive foreground | `res/drawable/ic_launcher_foreground.xml` | Launcher note, scaled to fit the safe zone |
| Monochrome launcher | `res/drawable/ic_launcher_monochrome.xml` | Themed icon using the same note geometry |
| Compose splash note | `res/drawable/songmeld_splash_note.xml` | Stationary note over a rotating badge |
| System splash | `res/drawable/songmeld_splash_vector.xml` | Badge and note, center placed for animated vector splash |

Paths under `res/` are relative to `app/src/main`.

## Updating or reusing the logo

After editing the canonical `branding/songmeld-logo.svg`, run from the repo root:

```bash
python3 tools/sync_branding.py
python3 tools/sync_branding.py --check
```

The script uses only Python's standard library. It updates the README variant,
standalone mark, Android VectorDrawables, and the source badge used by the
Android/Compose splash. `--check` fails when generated assets are out of sync,
which makes it suitable for CI.

The badge morph animation contains an intentionally hand-tuned target path:
`app/src/main/res/animator/songmeld_badge_morph.xml` (`valueTo`). If changing
the **outer badge geometry**, update that morph target to a path with a
compatible command structure, then visually verify the animated shape.

## Platform colors and launcher behavior

- **Android 8–11:** orange `#FFA069` badge/background and dark brown `#57331E` note.
- **Android 12+:** launcher and splash colors use wallpaper-derived system palettes;
  supported devices and launchers may vary.
- **Android 13+ themed icons:** the monochrome adaptive layer is tinted by the
  launcher where supported and enabled.
- **GitHub README:** uses a separate static orange/brown SVG because GitHub
  cannot access Android's wallpaper color system.

> Design note: The current approved full badge was inspired by a reference
> that has a notably similar exterior outline. Before wide public branding,
> consider redrawing the exterior badge to be distinct. The standalone note
> is a separate symbol.
