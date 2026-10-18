# Icon appearance foundation

Settings > Icon appearance now opens a functional page. The app currently offers
three *static Compose Material Icon* styles: Rounded (default), Outlined, Filled.
These are not the adjustable Material Symbols variable-font axes.

The central `SongMeldIconId` mapping in `ui/icons/SongMeldIcons.kt` resolves
screen icons using `LocalSongMeldIconStyle`. New screen icons should use
`songMeldIcon(SongMeldIconId.SomeName)`, and add each new name for all presets.
Do not persist `R.drawable` IDs or raw numeric icon codepoints.

The optional icon background shape is a separate setting; it changes the
illustrative icon containers in the music-permission and empty-state screens.
It deliberately does **not** change album covers, the splash logo or launcher.

Both settings are persisted in `AppearancePreferences` (SharedPreferences)
and state is hosted in `MainActivity`. Device Back and the header Back button
navigate Icon appearance -> Settings -> music library.

Future plans: Material Symbols Variable Font with FILL, wght, GRAD and opsz;
additional providers for user-selected tag/playlist icons. Keep that engine
separate from static icon presets to avoid pseudo-sliders.
