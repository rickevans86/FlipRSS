# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #12 — Rick requested hide-empty-category setting, all APK previews on
GitHub Releases and direct RSS feeds with FreshRSS optional.
**Completed:** RSS/Atom parser and local repository with bounded redirects/downloads,
no FreshRSS auth on feeds, atomic private-file subscriptions/cache/read/saved state,
foreground refresh, offline restart, long-text reader/speech reuse. New installs
use local mode; existing connected users remain FreshRSS. Settings/Account switch
source without deleting account/library; saved FreshRSS account can be restored.
Local categories use feed titles. Category setting filters Today chips and Topics
categories/feeds using unread counts, retains unknown counts, and returns from a
now-empty selection to All stories. Preference persists separately from Hide read.

**Files:** data/LocalFeeds.kt, LibraryController, AppSettings/AndroidSettingsStore,
LibraryViewModel, MainActivity, Account/AddFeed/Settings dialogs, app version,
LocalFeedsTest/LibraryControllerTest/instrumentation fixtures, release publisher
workflow/script/tests/manifest, Android CI, README/AGENTS and all six .ai records.
**Branch/PR:** codex/12-local-feeds, source implementation
63f47b52e3c80b5264bef766a7f12009c779dcf2. [PR #13](https://github.com/rickevans86/FlipRSS/pull/13) open; #12 status:review, BASE
codex/10-article-speech (PR #11), dependent on PR #9 and PR #7. Merge #7 first;
retarget each later PR to main before merging and synchronize shared records.
Rick alone merges and selects independent reviewer. Author does not self-review.

**Release/preview:**
https://github.com/rickevans86/FlipRSS/releases/tag/v0.5.0-local-preview
https://github.com/rickevans86/FlipRSS/blob/1ff1e6676473f3aa4a33df8a586244714059912d/preview/FlipRSS-v0.5.0-local-preview.apk?raw=true
VersionCode 5, Android 8+, test-signed release/non-debuggable. Same prototype test
identity; no key published. 13,234,339 bytes, SHA-256:
`1a21f4bab30d7aebb731556e63e949affc74853be84eb0d1850bb104f0f00f6c`.
Artifact branch codex/12-local-download is NOT for merging. GitHub blob retrieved
and checksum verified. Signature v2/v3, alignment, version/SDK/manifest checked.

**Releases:** Explicitly authorized by Rick. Backfilled release pages for v0.2,
v0.3, v0.4 and published v0.5, all prereleases with download links. Existing v0.1
unchanged. Uploads.github.com asset upload returns HTTP 401 for this session.
Publish APK previews workflow on [SelfHosted, Windows] uses Actions token to attach
verified APK/checksum/build metadata from immutable artifact commits, refuses
asset mismatches, and updates completion notes. Manifest .release/previews.json.
Triggered publish-previews-20261118-backfill: run 44247234997 queued. New manifest
includes v0.5; publish-previews-20261118-local-v5 triggered run 44247783494,
pending behind backfill. No runner
execution/asset-upload success claimed. Download links work independently.

**Tests:** 42 JVM tests passed in EACH debug/release variant; debug/release build
and lint passed (0 errors, 1 existing KTX warning each). Four Node publisher tests
and actionlint passed. Added RSS/Atom mapping, script stripping, stable feed IDs,
no external entity expansion, offline persistence, read/star restoration, cutoff,
failed refresh/storage, URL validation/redirects/size limits and mode separation.
Instrumentation compiled, not executed; adjusted legacy demo fixtures to explicitly
select sample mode. No actual phone/audio/real-account test. Diff check passed.
**In progress:** Independent review, phone validation, queued release asset uploads.
**Blockers/limits:** No build blocker. Asset uploads wait for runner execution.
Unable to inspect runner listing (GitHub integration returned HTTP 403); do not
assume why queued. Local feed max 5 MB/500 entries; retains 500 per feed plus saved
stories. Direct XML URL only; no discovery/OPML/background sync/custom folders/
unsubscribe UI/backup-export. HTTP is unencrypted. Separate sources, no auto-import.
FreshRSS cache remains in memory; existing speech/renderer/test-signing limits.
**Previous work:** PR #7/#9/#11 remain open, all visible implementations by Codex.
No new work by other AIs found. Main remains 92613d6 offline prototype.
**Next action:** Rick installs latest preview and validates local feed add/refresh,
restart offline, source switching, category setting and existing reader/speech.
Select independent reviewer, handle merge chain, ensure Windows SelfHosted runner
executes publishing tags, then verify release assets and final notes.
**Shared records updated:** Yes; append-only history preserved.
**Remote synchronization:** main/base fetched and main ancestry checked; source
and artifact branches pushed, release pages/API and APK download confirmed.
No main merge performed.
