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
system text, and offline. Check back navigation, the scrollable license dialog,
GitHub/email actions, and empty/listed people in each role. Version comes from
PackageManager and should match the installed build.
