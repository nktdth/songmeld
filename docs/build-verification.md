# Build verification — 2026-10-19

## Android build

`clean :app:assembleDebug :app:lintDebug` completed successfully with the project’s
existing dependency versions. Kotlin/Compose compilation, resource processing,
debug APK assembly and Android Lint passed. Lint reports **0 errors and 15 warnings**;
no baseline or suppression was added to bypass findings.

The warnings are 11 optional KTX suggestions, two large splash-vector notices,
one redundant API-26 resource qualifier and one unused branding drawable. These
are retained for a separate focused cleanup; splash dimensions were not changed.

The build exposed 93 missing-translation errors in a stale, partial `values-vi`
copy. Vietnamese is already the default source language. The redundant copy was
removed, the default resource locale is declared as Vietnamese, unused old strings
were removed, and Vietnamese plurals retain only `other`. This also prevents the
older localized wording from overriding the current Settings descriptions.

A Kotlin incremental compile reported duplicate declarations after source edits;
a clean rebuild resolved it. Use a clean build if the same local cache issue recurs.

Toolchain used: Temurin JDK 21, Gradle 9.6.0, Android SDK Platform 37.0 and Build
Tools 36.0.0. Toolchain installations, SDK paths and network configuration are local
environment files and are not project changes. Project dependencies were unchanged.

## Additional checks

- Standalone Kotlin checks compile and execute the actual library query, refresh
  state and artwork sampling helpers. Search/sort/cancellation, refresh/failure
  snapshot preservation, successful empty results and extreme image dimensions pass.
- Catalog checks pass against all 20,382 bundled entries and malformed input.
- Appearance preference and motion-policy checks pass using in-memory platform
  doubles; see their feature documents for exact scope.
- XML/resources, changelog JSON and diff whitespace checks pass.

## Remaining device verification

No emulator/device UI or instrumentation testing was performed. Build success does
not verify visual layout, MediaStore provider behavior, permission/lifecycle flows
or native artwork codecs at runtime.

On a provisioned developer machine, run:

```bash
./gradlew clean assembleDebug lintDebug
```

Then complete the device checklists in `ux-review.md`, `settings-appearance.md`,
`icon-browser.md` and `library-refresh.md` before merging the draft PR.
