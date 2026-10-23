# About redesign validation — 2026-10-22

Scope: PR #8, `feat/settings-m3-expressive-redesign`, based on `7954e2f`.

## Automated results

The Android build uses the repository's unchanged production toolchain versions:
JDK 21, Gradle 9.6.0, Android Gradle Plugin 9.4.0, Kotlin 2.4.10,
Compose BOM 2026.09.00 and Android SDK 37.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

- Debug Kotlin/Compose compilation, resource processing and APK assembly: passed.
- JVM tests: **6 passed, 0 failed, 0 skipped**. These execute the production
  changelog parser with grouped/legacy data, category ordering, future categories,
  release identities, optional dates, empty content and malformed input.
- Android Lint: **0 errors, 22 warnings**. No new suppression or lint baseline.
  Warnings include existing splash/vector, scroll-state, KTX and resource
  suggestions, plus advisory version/catalog suggestions for the new test-only
  JUnit and JSON dependencies. They do not prevent APK assembly.
- XML parsing, duplicate strings, referenced string IDs, the four new Material
  Symbols codepoints and diff whitespace checks: passed.
- Original license asset files: unchanged.

The local toolchain was restored outside the repository. An initial Gradle plugin
cache failure recovered on rerun; no production dependency version was changed to
work around it. SDK/cache warnings are environment-specific. Local SDK paths,
proxy configuration, build outputs and toolchain downloads are not committed.

## Review and remaining limits

At the time of inspection, PR #8 was open and mergeable, with no review threads,
comments or GitHub check runs to resolve. This update is committed to the existing
head branch and does not merge the PR.

Code review confirms the same people component handles developers and
contributors, limits each inline list to five, and passes the **complete** list
to the modal. Only curated production credits are shown. No fabricated names or
avatar downloads were added.

No emulator/device visual or instrumentation run was performed. Build and parser
tests do not establish visual correctness. Real-device checks remain for the
six-person overflow boundary, modal dismissal and scrolling, large fonts,
landscape, light/dark/AMOLED/dynamic colors, TalkBack, document copy, page-state
restoration and animation feel. See the checklist in `about.md`.
