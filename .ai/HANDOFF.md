# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #21 — Rick authorized all agreed changes on main, build and publication.
**Completed:** Merged OLED/search foundation PR #20 at 0bd0077. New app source
7dc97632646dcec2ae6c3ed29a2c41e8eb529991 on codex/21-source-tabs. Independent
FreshRSS and local controllers share UI settings, preserve old encrypted account
and local file. FreshRSS/FlipRSS/Search/Saved tabs, no Topics; category filters stay.
Saved combines both sources, including read bookmarks, with owner-routed writes.
+ is only on FreshRSS; all Search additions use local repository. Local feed
manager offers view/open/copy URL and account-confirmed FreshRSS subscription
without moving/deleting local data. Optional Mark as read on scroll defaults off;
only observed cards passed above viewport in source tabs qualify. Reader automatic
read preference stays separate, Saved remains visible.

Search imports public feeds from exact supplied Feeder/Feedspot pages, daily on
app/Search opening when enabled, plus manual refresh. Atomic private cache keeps
failed-source data, deduplicates normalized URLs, shows status/time/source links
and bundled fallback. Website discovery supports direct RSS/Atom, alternate links,
relative/base URLs, RSS links/icons and common paths; candidates must validate XML.
Blocked homepage fallback still checks feed paths. Bounds/timeouts/redirect checks
and separate unauthenticated client; no implicit subscriptions. Some sites have
no accessible feeds/login/JS restrictions. Existing OLED/Search-disable retained.

**Files:** app source coordinator/state/controller/metadata, MainActivity and
settings/account/search/feed actions, FeedDiscovery, FeedSearchModel, SourceReading,
version/tests; README/release manifest and six .ai files.
**Branch/PR:** codex/21-source-tabs from main, feature PR to open after records.
Rick explicitly authorizes merging this task and publishing; no independent self-
review claimed. No other AI changes/claims found.
**APK:** v0.8.0, code 8, Android 8+, signed release/non-debuggable. Existing test
signer matches v0.6; production signing unconfigured, no key published.
13,283,547 bytes; SHA-256:
`39e89edc9e63ae0bd6c4b9f3a173b63a99c7013674cf94f8833eb26001bccaf6`.
Artifact 04a95a49389528f5c897bc9c8b0d21fad638bbc9 / codex/21-release-download,
release-download/FlipRSS.apk (never merge binary branch). Package/version/SDK,
signature v2/v3, alignment and signer equality checked. Native release awaits
main merge and dispatch of hosted Publish APK releases workflow.

**Tests:** 64 JVM tests pass in EACH debug/release variant. Both builds and lint
pass (0 errors, 1 existing KTX warning), instrumentation compiled, not executed.
Five Node publisher tests/actionlint pass. New checks cover combined read bookmarks/
source metadata, scroll qualification, actual list formats, daily cache/no refetch/
partial failure, URL/base/common paths, RSS/Atom, no credential forwarding, bounded
size/downgrade and blocked-homepage fallback. Compiled device tests updated for
source tabs, + visibility, Search-to-local and existing read/Saved/theme flows.
Real downloaded pages parsed by compiled production code: Feeder 114, Feedspot
454 public feed URLs before cross-source dedup. Both original URLs HTTP 200 with
production headers. Not every imported feed target probed; validation occurs
on subscription. No actual phone/installer/audio/live-account/write test.
**In progress:** Authorized merge, native asset publish, final verification/records.
**Blockers/limits:** No build blocker. Feed lists may change/public links only;
cache/foreground daily refresh, no periodic OS worker. Website probing is bounded
(best effort, not all websites support RSS). Local cache retains 500/feed plus
saved, remote cache remains in-memory with paged Saved. Existing TTS/renderer/
prototype-signing limits remain.
**Next action:** Merge feature PR with explicit Rick authorization, dispatch
publisher on main, verify v0.8.0/native downloaded bytes/source tree equality,
close completed #19/#21 and update final shared records. Rick validates on phone.
**Shared records updated:** Yes; append-only history retained.
**Remote synchronization:** main/base fetched/ancestry checked, app source/artifact
pushed. Publication not yet claimed.
