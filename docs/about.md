# About SongMeld

The About page uses the app theme (including dynamic color), the bundled brand
logo, the installed package version, and an offline copy of the repository GPLv3
license. It does not request network or storage permission.

## People and roles

Edit `ui/settings/AboutCredits.kt` to maintain the credits. Entries are curated,
not inferred from commits, PRs, GitHub contributor counts, or activity.

- `primaryDevelopers`: people responsible for the overall project.
- `secondaryDevelopers`: people with an explicitly agreed ongoing development role.
- `contributors`: people credited for ideas, bug reports, translations or PRs.
  Add a localized string describing each person's actual contribution.

A person belongs in the appropriate group; a submitted or merged PR does not
implicitly make them a developer. Unconfirmed names are not displayed. Empty
secondary-developer and contributor sections say that no one is listed yet.

Each entry supports a bundled avatar, display name, contribution text, GitHub
profile and optional email. Missing avatars use an initial. Profile buttons wrap
at large font scales. Use locally bundled images to keep credits available offline.

The owner has authorized publishing their GitHub avatar.
`developer_ktandth.jpg` is bundled from the public profile image at
https://github.com/nktdth.png?size=192 (retrieved on 2026-10-19, Vietnam time).
The image remains available offline; no avatar request is made by the app.

## Validation

Check the About page in light/dark themes, with dynamic colors on/off, large
system text, and offline. Check back navigation, the license list and full-page document reader,
GitHub/email actions, and empty/listed people in each role. Version comes from
PackageManager and should match the installed build.

## Information pages

About links to Licenses, Changelog, and Terms. Each page uses the existing
collapsing settings toolbar and app theme. Android Back and toolbar Back return
to About; a license document returns to the license list. Selected document and
current destination survive activity recreation.

Licenses lists the four notices currently packaged in `assets/licenses/`.
This is a catalog of bundled notices, not a complete dependency license audit.
Before a release, review dependency attributions and the font provenance noted
in `docs/icon-system.md`. Original license documents are displayed unchanged.

Changelog reads `assets/changelog/vi.json` offline. Newest entries go first;
use `development` or `released` for `status`. The initial 0.1.0 entry describes
the development build, without claiming a release date. Add actual changes per
version; do not automatically label historical entries with the installed version.

Terms are localized in `values/strings.xml`. They provide usage information and
preserve GPLv3 rights without adding an acceptance gate. Update content when new
services or file-editing features are implemented.

Document reads run on Dispatchers.IO. Missing/unreadable assets show a retry
button. A malformed changelog shows a readable error instead of crashing.

Each changelog release can set `lastUpdated` to an ISO calendar date (`yyyy-MM-dd`),
for example `"lastUpdated": "2026-10-19"`. Set it to the date that specific
version's entry was last updated, not the build date or the device's current date.
The Vietnamese UI displays it as `Cập nhật cuối: 19/10/2026` under the status.
Older entries without the field, or with an invalid date, still display their
changes and omit the date line. Keep valid dates on all new entries.
