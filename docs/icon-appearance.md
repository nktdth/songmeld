# Icon appearance

Settings → Appearance → Icon appearance is now powered by **Google Material Symbols Variable Font** (not the legacy Compose Material Icons Extended dependency).

- Choose the Rounded, Outlined, or Sharp variable font family.
- Adjust the genuine font axes Fill (`FILL`), Weight (`wght`), Grade (`GRAD`) and Optical size (`opsz`) with live preview.
- Choose Circle / Rounded square / Soft square as a separate background shape preference.
- Changes apply to SongMeld's UI icons across the header, Songs, permission screen and Settings, and are saved in `AppearancePreferences` using SharedPreferences.
- A stored legacy `icon_style=filled` setting migrates to Rounded and `FILL=1`.
- Device Back and the header Back button navigate Icon appearance → Appearance → Settings → Songs.
- **Browse icons** opens the full offline catalog for Symbols, MDI, and Flutter Material Icons. Choosing an icon saves a test selection; it does not change the app navigation icons.

New call sites should use `SongMeldIcon(SongMeldIconId.YourName, contentDescription = ...)`. See [Icon system](icon-system.md) for font sourcing, attribution, extension instructions, and licensing.

The launcher icon, splash logo, branding vector XMLs, and actual song album art never change based on these settings. The included full offline catalog already supplies candidate glyphs for future Tags/Playlists; those features and their per-item icon persistence are not implemented yet.
