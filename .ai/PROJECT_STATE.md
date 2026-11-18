# PROJECT_STATE

## Main / released

main 92613d6 and published v0.1.0-prototype contain the offline-only app.
Release: https://github.com/rickevans86/FlipRSS/releases/tag/v0.1.0-prototype
Release assets use verified download-branch links due upload HTTP 401.

## Feature PR #7 — #6 (not merged)

codex/6-freshrss adds login/encrypted token, catalogue/counts, paginated feeds/
topics/saved reader, read/star sync and separate demo mode. Source edcac98;
0.2.0 preview on codex/6-freshrss-download. Its 18 tests pass in both variants.

## Reading controls — #8, stacked on PR #7 (not merged)

codex/8-reading-controls, source 302a180: Add Feed subscribes on FreshRSS; top
hide-read persists and filters server pages; confirmed mark-all-read scopes to
current all/folder/feed/saved stream with timestamp cutoff and includes unloaded
items. Gear settings persist theme/system bars, reader size, auto-read and images.

Preview 0.3.0-controls-preview / versionCode 3, test-signed release APK at artifact
commit 1bdaf3d (codex/8-controls-download; do not merge). See handoff for download
and checksum. 28 JVM tests pass per debug/release variant; both lint/build pass,
instrumentation APK compiles, signatures/alignment/download checksum verified.
No actual device/server execution claimed. Base live-cache/offline/renderer and
production-signing limitations remain. Add Feed uses default server folder.

Merge PR #7 first, then retarget the controls follow-up to main before merging.
No new published release or main merge performed for these controls.
