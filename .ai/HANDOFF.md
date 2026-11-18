# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #14 — Rick requested a regular release named FlipRSS.apk, native release
assets, in-app GitHub updates, always-visible Saved/read bookmarks with no Hide
read switch, and removal of perspective/account page panels.
**Completed:** Removed page heading/subtitle and account banner; account remains
in Settings. Saved scope always requests read+unread on every page and bypasses
local read filtering. Top Hide read hidden only on Saved; Today preference remains.
Settings App updates checks latest regular GitHub release, requires matching build
metadata, downloads standard FlipRSS.apk with HTTPS/GitHub host restrictions,
checks size/SHA-256/package/newer version/same signing certificate, then opens
Android installer. Explicit unknown-source settings flow, no silent install.
Scoped cache FileProvider read grant; no FreshRSS credentials sent to GitHub.

**Files:** AppUpdater.kt, UpdateControls.kt, data/GitHubUpdates.kt, MainActivity,
SettingsDialog, LibraryController, manifest/update_paths.xml, app version, update/
Saved JVM and instrumentation tests, release publisher/workflow/tests/manifest,
README and all six .ai records.
**Branch/PR:** codex/14-release-updates; app source
de926519c5f44c3c8f3f19553bcab78ffafb98fd. [PR #15](https://github.com/rickevans86/FlipRSS/pull/15) open; #14 status:review,
BASE codex/12-local-feeds (PR #13), dependent on #11/#9/#7. Merge #7 first and
retarget each later PR to main before merging; sync shared records. Rick selects
independent reviewer and merges. Author does not self-review.

**Regular release:** v0.6.0 is published, regular/latest (prerelease=false),
https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
**Download:**
https://github.com/rickevans86/FlipRSS/blob/3cd57a66a4672990f0a2a52dc179f7199b3f99e5/release-download/FlipRSS.apk?raw=true
VersionCode 6, Android 8+, release/non-debuggable, 13,234,395 bytes. SHA-256:
`ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4`.
Artifact branch codex/14-release-download is not for merging. Downloaded GitHub
blob verified. Signature v2/v3, alignment, package/version/SDK checks and signing
certificate equality with v0.5 passed. Existing prototype test identity retained
for upgrades; production signing is not configured and no key was published.

**Native release assets:** Direct upload of FlipRSS.apk to v0.6.0 returned HTTP
401 from uploads.github.com. All native release Assets remain empty at last check;
verified release-note download links work. Changed publisher to attach FlipRSS.apk
plus checksum/build metadata for all six builds and promote explicit regular
versions. Publisher now needs only SelfHosted label, not Windows (Node works on
both). Cancelled superseded publishing runs 44247234997 and 44247783494. Standard
filename run 44251809984 cancelled as superseded; final manifest including v0.6
triggered run 44252735121, queued. No runner execution/native attachment success claimed.
Asked Rick asynchronously whether runner is online; no answer at record creation.
In-app downloads need native assets attached; missing assets show a clear pending
message and GitHub Releases link.

**Tests:** 49 JVM tests passed in EACH debug/release variant. Both builds and lint
passed (0 errors, 1 existing KTX warning each), instrumentation APK compiled but
not executed. Five Node publisher tests and actionlint passed. Covers Saved query
paging/global toggles/demo state, regular release/version/URL/digest validation,
unauthenticated metadata, download integrity/partial cleanup and unsafe redirects.
Actual device/permission/installer/audio/real-account testing NOT RUN. Diff check
passed; signature/version/downloading bytes verified independently of tests.
**In progress:** Independent review, phone validation, blocked native attachments.
**Blockers/limits:** No build blocker. Need a matching runner to execute publishing
job; listing runners returns HTTP 403 so do not assume why queued. GitHub public
API rate limits apply. Manual foreground update checks, Android confirmation, same
signer required; no silent install or signing rotation. Existing local cache,
FreshRSS in-memory/offline, TTS and prototype signing limits remain.
**Previous work:** PR #13/#11/#9/#7 still open, all visible changes by Codex; no
other AI work found. Main remains 92613d6; no main merge performed.
**Next action:** Rick installs FlipRSS.apk from release-note link, validates Saved,
clean pages and Settings update/permission/installer behavior. Ensure SelfHosted
runner accepts publisher, verify FlipRSS.apk/SHA256SUMS/build-info on all releases,
then select independent reviewer and handle merge chain.
**Shared records updated:** Yes, append-only history preserved.
**Remote synchronization:** main/base fetched and ancestry checked; source/artifact
branches pushed, regular latest release and downloaded APK confirmed.
