# About and information pages

The About page uses the installed version, bundled SongMeld logo, app colors and
curated offline credits. Its compact header and segmented navigation rows share
the same 680dp maximum reading width as Changelog, Licenses and Terms.

## People

Edit `ui/settings/AboutCredits.kt` to maintain the two ordered lists:

- `developers`: confirmed developers, with the project creator first.
- `contributors`: people credited for ideas, bug reports, translations or PRs.

There are no primary/secondary developer sections. The first entry of each list
has a 64dp avatar, larger name and a tonal background **inside the same container**.
Subsequent entries use 40dp avatars and normal text. Each section displays at most
five people in total. With six or more, a text button shows the total and
opens a modal sheet containing **everyone**, in the original
order. The sheet has an independently scrolling lazy list and a pinned close
button, and supports Android Back and outside-tap dismissal. Empty groups show a
short message. No placeholders or inferred GitHub contributors enter production.

Entries support a bundled avatar, display name, localized contribution, GitHub
URL and optional email. Text is allowed to wrap; profile actions use a FlowRow.
The GitHub and email actions still handle a missing external app gracefully.
The owner's previously authorized avatar remains bundled as
`developer_ktandth.jpg`; no remote avatar or storage permission is needed.

## Changelog format

`assets/changelog/vi.json` is read offline. Newest entries go first. Set `status`
to `development` or `released`; `lastUpdated` is the date that the entry was
actually edited, in ISO `yyyy-MM-dd` format. The app formats it as `dd/MM/yyyy`.
Missing or malformed optional dates do not hide the release.

```json
{
  "releases": [{
    "version": "0.1.0",
    "status": "development",
    "lastUpdated": "2026-10-22",
    "changes": {
      "added": ["A newly available feature"],
      "improved": ["An improvement"],
      "fixed": ["A corrected problem"],
      "removed": ["A removed feature"]
    }
  }]
}
```

The category order is Added, Improved, Fixed, Removed, Other. Empty categories
are omitted. Unknown future category keys and legacy flat arrays remain readable
under Other, with no wording-based guessing. Invalid data produces a visible
retry state. `ChangelogTest` exercises the real parser, including backward
compatibility, counts, dates, ordering and malformed input.

Each version has a status, update date and change count. The first version starts
expanded; older versions start collapsed. Header taps expand/collapse the content
with spring size motion and fade, disabled by `LocalDecorativeMotionEnabled` when
Android animations are off. Group headings have a symbol, label and count so
color is not the sole distinction. A static wave adds a small music-themed accent.

The 0.1.0 development entry has been reconciled with this branch: it no longer
claims that removed preview, compact-list or reset controls are still available.
It does not claim a new released version or an official release date.

## Licenses and terms

Licenses distinguishes SongMeld's GPLv3 from bundled font/icon notices. The four
original files in `assets/licenses/` are unchanged. This remains a catalog of
bundled notices, not a complete dependency audit. The reader lazily lays out
paragraphs, permits selection within paragraphs, and copies the full unmodified
asset with the explicit Copy action. It uses Android's copy feedback on API 33+
and a toast on older versions. Documents load on Dispatchers.IO and expose a retry
action on read failure.

Terms presents all five existing sections with numbered headings and selectable
text. The wording and GPLv3 rights are unchanged, with no acceptance gate.

## Navigation and device checks

These pages use the existing forward/Back transitions. Document Back returns to
the license list; information-page Back returns to About. Lazy scroll position
and saved modal/expansion state participate in the existing page state holder.

Before merging, check a real device in light, dark, dynamic color and AMOLED modes:

- Developer and contributor lists with 0, 1, 5, 6 and many entries; long names,
  missing avatars and missing profile links; all people reachable in the sheet.
- 320dp width, landscape, 200% font scaling, TalkBack headings and actions.
- Release expansion, legacy entries, malformed assets, Android animations off.
- All four original licenses, selection/copy, Terms, system/toolbar Back and
  returning to the same scroll position after opening a profile or a subpage.

Research and design decisions: `about-material3-redesign.md`.
