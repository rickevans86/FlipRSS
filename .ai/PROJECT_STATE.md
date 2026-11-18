# PROJECT_STATE

## Main / current release

Rick explicitly authorized the agreed next update on main, build and publication.
ChatGPT Codex merged OLED/search foundation #20 (0bd0077) and source/discovery
implementation #22 (8a62d3d). Current app source
7dc97632646dcec2ae6c3ed29a2c41e8eb529991 has identical app tree to main:
58c84fec2d2045cd16132215f33fa93e263eb61f. No independent review claimed.

Regular/latest v0.8.0, versionCode 8, Android 8+, signed non-debuggable release:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.8.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.8.0/FlipRSS.apk
13,283,547 bytes; SHA-256:
`39e89edc9e63ae0bd6c4b9f3a173b63a99c7013674cf94f8833eb26001bccaf6`.
Same test identity as previous builds; production signing remains unconfigured.
Artifact 04a95a49389528f5c897bc9c8b0d21fad638bbc9 on codex/21-release-download
remains immutable/separate. Never merge binary branches or upload signing keys.

## Current behavior

FreshRSS and FlipRSS tabs keep independent server/local sources concurrently.
Saved combines both, includes read bookmarks, and routes writes to their owner.
Topics removed; category chips retained. + only FreshRSS. Search always adds local.
Local feed manager supports selectable/open/copy URL and explicit account-confirmed
Add to FreshRSS, preserving local subscription/read/saved data.

Default-off Mark as read on scroll marks previously observed cards passed above
the top in source pages only. Reader auto-read separate. Search updates Feeder/
Feedspot public lists daily on foreground opening when enabled plus manual refresh,
with atomic per-source cache/dedup/failure retention/bundled fallback/attribution.
Website/feed URL discovery validates direct XML, alternate/RSS/icon links/common
paths, including blocked-homepage fallback. OLED/Search-disable remain.

## Verification / limits

64 JVM tests pass per debug/release variant; both builds/lint pass (0 errors,
1 existing KTX warning); instrumentation compiles only. Five publisher tests and
actionlint pass. Compiled production parsers extracted 114 Feeder/454 Feedspot
public URLs from current pages; both sources HTTP 200 with app headers. Not every
feed target probed, subscription validates. Actual phone/installer/audio/live-
account-write tests NOT RUN. Remote Android CI success not claimed.

Hosted publication 44295156332 SUCCESS. All eight releases have native FlipRSS.apk,
SHA256SUMS.txt and build-info.json; all APK digests match manifest. Downloaded
v0.8.0 native bytes and verified SHA-256; latest endpoint confirms regular v0.8.0.
#19/#21 closed, no implementation/publication blocker. Source format/access may
change; bounded best-effort discovery, no periodic OS worker. Remote cache/Saved
is in-memory/paged; local cache retains 500/feed plus saved. Existing TTS/renderer/
signing limitations remain in README. Rick performs phone validation.

## Search regression #24 and speech follow-up #25

Codex reproduced v0.8 Feedspot assigning first common-parent heading NBC News to
unrelated URLs. Source 72b2956 fixes row/heading ID pairing, row-scoped fallback,
unmatched-row skip, schema-2 directory cache reset and explicit 20-result paging.
Actual downloaded page: 453 URLs/449 names, one NBC News, WordPress correctly
named The WordPress Blog. 68 JVM tests pass per debug/release; both build/lint
pass, instrumentation compiles. Existing subscriptions/Saved untouched.
#24 correction will merge under ongoing shipped-update directive, then #25 adds
requested natural speech options; one combined regular APK release planned.
No physical-device/audio check performed. Current published v0.8.0 remains until
combined successor, no native patch asset claimed yet.
