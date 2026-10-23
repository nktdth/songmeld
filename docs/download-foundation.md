# SongMeld multi-source download — phase 1

Branch: feat/multi-source-downloader

## Design and old-app reference

The lower-right FAB is inspired by SongMeld-v1's \`SongsActionFab.kt\` and
\`HomeActionFab.kt\`. SongMeld uses upstream Google Material 3 Expressive
\`FloatingActionButtonMenu\` and \`ToggleFloatingActionButton\`, avoiding
the previous custom shape/motion implementation.

The Fab offers:
- **Thêm từ liên kết** → the Add Song preview page
- **Tải xuống** → in-session download queue and error states

The library's existing scroll-to-top FAB is moved above the new menu's
activation region to avoid tap-target collision.

## First implementation

- Explicit, strictly validated HTTPS single-track links:
  YouTube, YouTube Music and SoundCloud.
- NewPipe Extractor v0.26.5 supplies per-track metadata and direct audio formats.
  The YouTube Music video link resolves through the shared YouTube video backend.
- Preview shows available source audio containers and the advertised bitrate.
- One single-track audio download at a time in a foreground dataSync Service,
  with FIFO queue, progress, cancel and retry.
- Save an actual audio file using MediaStore on Android 10+, or the app-specific
  music folder on Android 8–9 (then request a media scan).
- Keep the source container extension (m4a/webm/opus/mp3/ogg/aac) rather than
  relabeling bytes as MP3. Imported MediaStore columns include title/uploader;
  embedded metadata/artwork editing is not part of this phase.

## Known limits

The queue is process-local: it does not survive process death, reboot or the
system stopping a foreground service. No resumable/ranged transfers yet.
No playlists, bulk selection, general search or MP3 conversion yet. No promise
that a stream is publicly downloadable: availability depends on source policies
and NewPipe's current extractor compatibility. Users must have permission to
download the selected content.

NewPipe is GPL-3.0-or-later and SongMeld is GPL-3.0; retain full corresponding
source, attributions and licenses. Verify the JitPack release and test Android
API 26/29/33/35/37 before shipping.

## Validation

Run:
\`\`\`bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
\`\`\`

Exercise an authorized test track on each provider, verify playback in a
separate audio app, correct MIME/extension, cancellation cleanup, duplicate
names, network errors and rotation. No DRM/subscription/geo access bypass.

## Functional settings and native FAB row

The library Scroll-to-top FAB is now to the **left** of the Add/Download FAB in
the same bottom-end row. The plus rotates 45 degrees into an X, reminiscent of
SongMeld-v1, while the menu uses Google's native M3 Expressive transitions.

Settings → Download Settings, plus settings actions in the Add Song and Downloads
screens, now expose actual supported behavior:
- Highest bitrate, lowest bitrate, or manual choice before each download.
- Preferred M4A/AAC or Opus/WebM when available, with source fallback.
- A flat `Music/SongMeld` folder or provider subfolders.
- Wi-Fi-only admission check on the network the device is using.
- Optional completion alert; active foreground-service notifications still run.

New settings apply to subsequent downloads. Format conversion, playlist, and
parallel downloads are deliberately not presented as functioning toggles.

Custom folders: a system OpenDocumentTree picker grants a persistable SAF URI;
files are created directly in that directory without broad storage permissions.
The optional per-source grouping is only applied to SongMeld's default folder.

## FAB visual parity with SongMeld-v1 (follow-up)

The older v1 `SongsActionFab.kt` uses a **single end-aligned Column** containing
an `AnimatedVisibility` action area and **one bottom Row** containing Scroll-to-top
and Add. The previous M3E `FloatingActionButtonMenu` was measured in the same
parent Row as the scroll action. Its width changed with the labels, visibly moving
the scroll FAB far to the left and changing bottom alignment between states.

The FAB implementation now ports the original composition and its exact timings:
- Main Add FAB scales to 1.06 with spring damping 0.58/stiffness 420;
  plus icon rotates 45° using damping 0.62/stiffness 430.
- Actions slide up by a quarter-height and scale from 0.78 using the same v1
  springs, exiting with 120/160/150-ms fade, slide and scale to 0.82.
- Bottom Row uses CenterVertically alignment and 12dp spacing, with the native
  M3 FAB and ExtendedFAB containers and 6dp fixed ExtendedFAB elevation.
- Scroll-to-top uses original 16dp horizontal icon inset, 8dp expanded label gap,
  and 120/150-ms entrance and 100/120-ms exit.

The action area has two ExtendedFAB options (links and downloads) rather than the
single legacy option; this preserves the functional download history. No custom
colors or overlay backgrounds are imposed on the native Material FABs. This
intentionally uses v1's exact motion instead of M3E FAB Menu motion because the
user specifically requested the original bounce, arrangement and background.
