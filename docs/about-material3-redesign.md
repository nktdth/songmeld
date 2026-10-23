# About and information-page design review — 2026-10-22

This iteration addresses the owner's request on PR #8. The supplied
[Material You app list](https://github.com/nyas1/Material-You-app-list/blob/main/README.md)
was used to select source references. These are design observations from code,
not claims that those apps were installed or visually tested here. No external
implementation, artwork or dependencies were copied into the production UI.

## Sources actually inspected

| Source | Observation | SongMeld decision |
| --- | --- | --- |
| [Rhythm AboutScreen](https://github.com/cromaguy/Rhythm/blob/85fea209c4d44046192d830faa9181db3ece82ad/app/src/main/java/chromahub/rhythm/app/shared/presentation/screens/settings/AboutScreen.kt) | Clear brand area, grouped information cards, separate contributor purposes | Use a compact identity card and grouped actions; honor the owner's single developer group instead of copying Rhythm's multiple role groups. |
| [Rhythm LicensesBottomSheet](https://github.com/cromaguy/Rhythm/blob/85fea209c4d44046192d830faa9181db3ece82ad/app/src/main/java/chromahub/rhythm/app/shared/presentation/components/bottomsheets/LicensesBottomSheet.kt) | Library name, purpose and license stay together in selectable rows | Keep attribution and license together; retain SongMeld's offline full-page document reader. |
| [Rhythm ChangelogFilter](https://github.com/cromaguy/Rhythm/blob/85fea209c4d44046192d830faa9181db3ece82ad/app/src/main/java/chromahub/rhythm/app/util/ChangelogFilter.kt) | User-facing notes are separated from low-value commit noise | Curate explicit JSON categories and reconcile removed features; do not infer headings from commit prefixes or translated text. |
| [Chocola AboutCard](https://github.com/sosauce/Chocola/blob/14b4e1a13e3a24c92e65dcce39bdc81d25056748/app/src/main/java/com/sosauce/chocola/feature/settings/presentation/components/AboutCard.kt) | Compact tonal identity card, rounded shape and adjacent app/version information | Shrink the isolated hero into one card; keep wrapped text actions instead of adding more icon-only controls. |
| [WikiReader AboutScreen](https://github.com/nsh07/WikiReader/blob/fc5c0177bd7ee24696fadd6dbf027837bf2508f1/app/src/main/java/org/nsh07/wikireader/ui/aboutScreen/AboutScreen.kt) | Segmented list groups with small internal gaps and larger outer corners | Group related navigation rows with 4dp gaps, 24dp outer corners and 6dp joining corners. |
| [Namida AboutPage](https://github.com/namidaco/namida/blob/e6b36666b97e9f053a9dc311c9d2ae1a420a2129/lib/ui/pages/about_page.dart) | App/version identity, local avatar fallback and a strong developer presence | Keep bundled branding and a featured first person; avoid a large animated illustration in a document-reading flow. |
| [Compose Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3) and [Material motion](https://m3.material.io/styles/motion) | Theme roles, shape hierarchy and physics-based motion | Use existing theme colors, readable typography, grouped surfaces and spring expansion; no runtime dependency upgrades. |

PixelPlayer and Gramophone were also looked up, but the supplied repository
locations redirected and did not yield usable source in this session. They are
not counted as inspected sources for this iteration.

## Concrete UI decisions

- **About:** 72dp logo, one identity card, version/status badges and a short static
  musical wave. Changelog, Licenses and Terms share a segmented navigation group.
  Repository and contribution links form a separate project-action group.
- **People:** one container per role, first person emphasized inside it, five
  entries maximum, and a text overflow action. The modal shows the full list,
  including the first five. The same component serves contributors.
- **Changelog:** version header plus Added / Improved / Fixed / Removed groups,
  each with a distinct theme role, symbol and count. All groups of the first
  version are visible initially. Release expansion uses a spring and fade.
- **Licenses:** featured project license, grouped resource notices, bounded-width
  reader and explicit copy-all action. Long legal texts use lazy paragraph layout.
- **Terms:** existing wording stays visible in five numbered sections. Decorative
  content is restricted to the header; no collapsing legal paragraphs.
- **Accessibility:** text wrapping, FlowRow badges/actions, normal button touch
  targets, heading semantics, expansion state semantics, no perpetual motion,
  and no meaning conveyed by color alone.

## Verification

See `about-redesign-validation.md` for build, test and remaining device results.
