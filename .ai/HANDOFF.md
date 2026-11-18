# HANDOFF

Agent: ChatGPT Codex. Task #24: Rick reported Search endlessly labelling unrelated
feed URLs NBC News. Reproduced shared-parent heading bug in Feedspot importer.
Source 72b2956 on codex/24-search-fix pairs numeric row/heading IDs, only uses
unambiguous row-local fallback, drops unrelated links, invalidates schema-old
directory cache and immediately refreshes, and limits catalogue to 20 results
with explicit Load more (resets per filter/dataset). Subscriptions/Saved untouched.

Tests: 68 JVM tests pass in EACH debug/release variant; both builds and lint pass
(0 errors, 1 existing KTX warning), instrumentation APK compiled, not run. New
regressions reproduce multiple sibling entries under one shared parent, mismatched
IDs/footer links, stale cache after upgrade; compiled paging UI test added.
Production parser on actual page: 453 URLs/449 titles, one NBC News, WordPress
URL labelled The WordPress Blog. No actual phone/network-account/audio tests.

Files: importer/cache/search UI/navigation, tests/version/README and six records.
Branch/PR: codex/24-search-fix from main, PR to open. Main correction continues
Rick's main/build/publish directive for the shipped update; no independent self-
review claimed. Next #25 is Rick's added request for more natural TTS. Optional
Android/cloud preference asked asynchronously; no reply yet. Android voices with
explicit online opt-in is announced default; cloud would require provider key.

Next: merge search correction, branch #25 for voice/engine/speed/preview controls,
test/build and publish combined regular successor to v0.8.0. No patch asset yet.
Current regular release still v0.8.0. Shared records updated Yes; source/main
fetched/ancestry checked and source branch pushed. Binary branches remain unmerged.
