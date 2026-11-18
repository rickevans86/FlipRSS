# PROJECT_STATE

Latest regular release: v0.6.0 (#14, Codex), source de926519 on an unmerged
review branch. FlipRSS.apk download link is live; native assets await the queued
SelfHosted publisher because direct asset upload returns HTTP 401.

## Main / released

main 92613d6 and published v0.1.0-prototype contain the offline-only app.
Release: https://github.com/rickevans86/FlipRSS/releases/tag/v0.1.0-prototype
Release assets use verified download-branch links due upload HTTP 401.

## Feature PR #7 — #6 (not merged)

codex/6-freshrss adds login/encrypted token, catalogue/counts, paginated feeds/
topics/saved reader, read/star sync and separate demo mode. Source edcac98;
0.2.0 preview on codex/6-freshrss-download. Its 18 tests pass in both variants.

## Reading controls — #8 / PR #9, stacked on PR #7 (not merged)

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

## Article speech — #10 / PR #11, stacked on PR #9 (not merged)

AI: ChatGPT Codex. codex/10-article-speech, source 4b0dd285. Reader play/pause/
resume/stop uses Android TextToSpeech and device language, prefers installed voice,
handles voice setup errors, chunks long text and protects against stale callbacks.
Audio focus/background pauses; page change/reader exit stops and releases engine.
No background service, microphone permission or automatic next article. Some
configured engines need network; resume precision depends on range callbacks.

Preview 0.4.0-speech-preview, versionCode 4, test-signed release APK at artifact
commit b639c9e (codex/10-speech-download; never merge). All 31 JVM tests pass in
each variant, debug/release build/lint pass (0 errors, existing 1 KTX warning),
instrumentation compiles. Audio/device/live-account execution remains unverified.
Merge PR #7, then retarget/merge #9, then retarget the speech PR to main and sync
shared records. No main merge or new release publication for this feature.

## Local RSS and category visibility — #12 / PR #13 (stacked on PR #11)

AI: ChatGPT Codex. codex/12-local-feeds; APK app source 63f47b52. Adds direct
RSS/Atom subscriptions with atomic app-private JSON cache/read/saved persistence,
foreground refresh and offline restart. New installs use local mode; existing
FreshRSS accounts remain active. Source switching preserves both libraries and
saved account, with explicit return/sign-out/demo controls. Local categories use
feed titles. Hide-empty-category preference filters chips/Topics/feeds; unknown
counts stay visible and an emptied selected category returns to All stories.

42 JVM tests pass per debug/release variant, both build/lint pass (0 errors,
1 existing warning each), instrumentation compiles. Four publisher tests and
actionlint pass. APK v5 test-signed, signature/alignment/package/download verified.
No phone/audio/real-account execution. No background sync, OPML, custom local
folders, subscription removal or production signing.

Rick explicitly authorized all previews on GitHub Releases: v0.2, v0.3, v0.4 and
v0.5 prereleases now published alongside prior v0.1; each has APK download link.
Direct asset upload returned HTTP 401. New Windows SelfHosted publishing workflow
verifies immutable artifact hashes and attaches assets using GITHUB_TOKEN. First
backfill run 44247234997 is queued; latest run 44247783494 pending behind it.
No remote upload success claimed.
Latest: https://github.com/rickevans86/FlipRSS/releases/tag/v0.5.0-local-preview
Artifact 1ff1e66 / codex/12-local-download (never merge). Main remains 92613d6.
Merge #7, retarget/merge #9, retarget/merge #11, then retarget #12's PR and sync
shared records. Rick controls merging and selects independent reviewer.

## Regular release, updater and Saved cleanup — #14 (base PR #13)

Source codex/14-release-updates / de926519. Saved excludes global unread filtering
from server/local queries and visible stories, and hides its top Hide read switch.
Page perspective heading/subtitle and account banner removed; account in Settings.
Settings updater queries latest regular GitHub release, downloads FlipRSS.apk over
GitHub HTTPS without FreshRSS auth, checks size/SHA-256/package/newer version/same
signer, then invokes Android installer with explicit unknown-source permission
flow. Scoped FileProvider exposes only verified update cache, read-only.

49 JVM tests pass per debug/release variant; both build/lint pass (0 errors,
1 existing warning each), instrumentation compiles. Five publisher tests and
actionlint pass. No actual phone/permission/installer/audio/live-account execution.
APK signature/alignment/version/SDK/download hash and v0.5 signer equality checked.
Artifact 3cd57a6 / codex/14-release-download, file release-download/FlipRSS.apk.
SHA-256 ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4.

v0.6.0 is confirmed regular/latest, NOT a prerelease. Existing test signing
identity retained for upgrades; production signing remains unconfigured. Publisher
attaches standard FlipRSS.apk to all releases, verifies hashes, promotes explicit
regular versions only, and targets any SelfHosted runner. Cancelled superseded
runs 44247234997/44247783494; standard-name run 44251809984 queued and will be
superseded by final manifest including v0.6. No native assets attached yet.
Latest URL: https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
Main remains 92613d6. Merge #7, retarget #9, then #11, then #13, then #14 PR;
Rick selects independent reviewer and merges.
