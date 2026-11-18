# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #8 — Rick requested Add Feed, top hide-read toggle, mark all read and
settings for app changes, on top of the working FreshRSS preview.

**Completed:** Add Feed posts a validated URL to FreshRSS subscription/quickadd
with auth/write token, rejects unsuccessful JSON results and refreshes catalogue
on confirmed success. Top hide-read is persistent, requests unread-only pages via
xt=read, and hides newly read items immediately without changing reader queue.
Mark all read confirms/captures the current all/folder/feed/saved stream and a
microsecond cutoff; server endpoint includes unloaded stories. Busy/account/scope
checks prevent cross-account or widened operations. Errors do not claim success.
Settings gear persists system/light/dark theme (including system bars), reader
text size 16–26, auto-mark-read, cover images and the same hide-read preference.
Account entry/reset-defaults preserve credentials, bookmarks and subscriptions.

**Branch/PR:** codex/8-reading-controls, source implementation `302a1808cdd0e342ec55e54f35b79c473be4beec`.
[PR #9](https://github.com/rickevans86/FlipRSS/pull/9) is open; #8 is `ai:codex`,
`status:review`. BASE IS codex/6-freshrss (PR #7), not main. Merge #7 first,
then retarget this follow-up to main and sync records before merging. No other
agent changes discarded; no main merge or new published release performed.

**Preview:** 0.3.0-controls-preview (versionCode 3), Android 8.0+, release variant,
non-debuggable, same prototype test signing identity. No production key published.
https://github.com/rickevans86/FlipRSS/blob/1bdaf3d143dc9701a3d3663901644d7768446db4/preview/FlipRSS-v0.3.0-controls-preview.apk?raw=true
13,197,475 bytes; SHA-256:
`15a59d88a5dc4dd421e94ebd02d347989c2f73904c03d47539bb6b0cc6afff69`.
Artifact branch codex/8-controls-download is NOT for merging. Retrieved APK bytes
from GitHub blob API and checksum verified. Existing published v0.1.0 release
and older previews remain unchanged.

**Tests:** Debug/release builds and lint passed; all 28 JVM tests passed in EACH
variant. Lint 0 errors/1 warning in each (existing KTX suggestion). Tests add
subscription forms/results/validation, unread filtering on all pages, scoped bulk
cutoff/unloaded semantics, failures, stale accounts, demo snapshots and settings
persistence/clamping. Existing 18 integration tests remain passing. Instrumentation
APK compiles with settings/theme, hide/bulk demo and manual-read tests, but was
not executed. No real account/server or device test performed. Signature v2/v3,
alignment, version/SDK and debuggable=false verified. Diff check passed.

**In progress:** Independent review and Rick’s real-device/account validation.
**Blockers/limits:** No build blocker. Prior live-cache/text-reader/test-signing
limits remain. Bulk cutoff uses the device clock in microseconds. Add Feed uses
FreshRSS default folder; folder management is not implemented. No offline queue.
Existing Windows emulator action is skipped; remote CI success is not claimed.

**Previous work:** PR #7 / #6 contains base FreshRSS integration and preserved
published-release records for #4 (former PR #5 closed as superseded). Source main
still holds the offline prototype; do not confuse branch previews with releases.
**Next action:** Rick installs preview and selects independent reviewer. After
base PR #7 merges, retarget follow-up to main before merging it. The author does
not provide independent review of its own code.
**Shared records updated:** Yes, all six files; append-only history retained.
**Remote synchronization:** main/base fetched, ancestry checked, source/artifact
branches pushed and download verified. No #8 main merge/new release publication.
