# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #21 — Rick explicitly authorized all discussed changes on main, build
and publication. This instruction authorized foundation/implementation and final
records merges. No independent self-review claimed.
**Completed:** Foundation PR #20 merged (0bd0077), implementation #22 merged
(8a62d3d). Source app 7dc97632646dcec2ae6c3ed29a2c41e8eb529991 is an ancestor of
main, identical app tree 58c84fec2d2045cd16132215f33fa93e263eb61f. Independent
FreshRSS/FlipRSS sources, combined read/unread Saved with owning-source writes,
no Topics/category chips retained, + only FreshRSS, Search only adds local. Local
feed manager provides view/select/open/copy URL and confirmed Add to FreshRSS
with destination account, no local move/deletion. Existing local file and encrypted
account retained. Settings shared across sources; optional Mark as read on scroll
default off, only previously observed cards passed above source viewport. Reader
auto-read separate; no scroll-read on Saved/Search. OLED/Search-disable retained.

Daily/manual foreground list imports from supplied Feeder/Feedspot pages with
atomic per-source cache, failure retention, normalized dedup, bundled fallback,
timestamps/source links. Disabled Search stops automatic list refresh. Website/
feed discovery validates RSS/Atom, alternate/relative/base/RSS/icon links and
common paths; blocked homepage can still yield feed. Bounded requests/probes/
redirects, no embedded credentials/FreshRSS auth forwarded, no silent subscription.

**Publication:** Hosted action 44295156332 completed SUCCESS. All eight releases
have native FlipRSS.apk/checksum/build metadata; every APK digest matches manifest.
Downloaded latest native bytes and SHA-256 verified. Latest API confirms regular
v0.8.0. Issues #19/#21 closed.
https://github.com/rickevans86/FlipRSS/releases/tag/v0.8.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.8.0/FlipRSS.apk
VersionCode 8, Android 8+, release/non-debuggable, 13,283,547 bytes. SHA-256:
`39e89edc9e63ae0bd6c4b9f3a173b63a99c7013674cf94f8833eb26001bccaf6`.
Existing test signer matches older builds; production signing remains unconfigured.
No keystore published. Signature v2/v3, alignment, version/package/SDK and signer
equality verified. Immutable artifact 04a95a49389528f5c897bc9c8b0d21fad638bbc9,
codex/21-release-download/release-download/FlipRSS.apk; binary branch never merges.

**Files:** app source/model/controller/UI/settings/metadata/discovery/cache/scroll/
tests, README/release manifest and six .ai records.
**Branch/PR:** codex/21-source-tabs; #22 merged at 8a62d3d. Final records-only
follow-up to main authorized by Rick. Source code/build identical to verified APK.
**Tests:** 64 JVM tests pass per debug/release variant; both builds/lint pass
(0 errors, 1 existing warning); instrumentation compiles, not run. Five publisher
tests/actionlint pass and actual hosted publication succeeded. New tests cover
Saved/source metadata, scroll qualification/defaults, list formats/cache/daily/no
refetch/partial failure, URL/relative/base/paths/blocked home/RSS/Atom/credential/
size/downgrade handling. Production parser on downloaded actual HTML extracted
114 Feeder/454 Feedspot public links, both source URLs HTTP 200 with app headers.
Not every resulting feed URL fetched; validation happens on subscription.
Actual phone/installer/audio/live-account-write tests NOT RUN. Android remote CI
success not claimed. Main app tree/native download and all release APK digests
verified; diff check passed.
**In progress:** No implementation/merge/publication blocker. Rick phone validation
and optional independent review remain.
**Limits:** Public formats/access/feeds may change; bounded best-effort discovery
with no RSS on some sites/login/JS restriction. Foreground daily refresh only,
no OS worker. Remote Saved paged/in-memory, local cache 500/feed plus saved.
Existing TTS/renderer/prototype signing limits.
**Next action:** Install/update v0.8.0 and validate independent sources, Saved,
scroll setting, feed transfer/copy, discovery/list refresh and existing OLED/TTS/
Android installer on phone. Future releases use verified manifest+hosted publisher.
**Shared records updated:** Yes, append-only histories preserved, no active claims.
**Remote synchronization:** Source/artifact/main/merges/regular release/native
assets/latest downloaded bytes confirmed.
