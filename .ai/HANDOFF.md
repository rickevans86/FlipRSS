# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #16 — Rick explicitly requested merging all work into main and publishing
the APK as an asset. This instruction authorized the merges in this session.
**Completed:** Merged feature PRs #7/#9/#11/#13/#15 in order, retargeting to main
and preserving commit ancestry. Merged publisher PR #17. Source/app shared history
preserved, binary branches unmerged. Moved upload-only publisher to GitHub-hosted
Ubuntu (public repo), cancelled superseded queued publication, dispatched main.
Action 44254047153 completed SUCCESS. All six releases now have FlipRSS.apk,
SHA256SUMS.txt and build-info.json as native release assets. Every APK digest
verified against manifest; downloaded latest native bytes and checked checksum.
Closed completed Issues #4/#6/#8/#10/#12/#14/#16. Independent review not claimed;
Rick explicitly directed merge.

**Release:** v0.6.0, regular/latest, versionCode 6, Android 8+, non-debuggable.
https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.6.0/FlipRSS.apk
13,234,395 bytes; SHA-256:
`ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4`.
Existing test signer matches v0.5; production signing remains unconfigured. No
keystore published. Source app de926519c5f44c3c8f3f19553bcab78ffafb98fd is now an
ancestor of main; identical app tree 9c47d258bfb8cffd02ad0e8d83f2befe848a847d.
Artifact 3cd57a66a4672990f0a2a52dc179f7199b3f99e5 remains immutable on binary
branch codex/14-release-download, which must never be merged.

**Branch/PR:** codex/16-publish-assets, publisher PR #17 merged at 12b2587.
Final records/README follow-up to main contains no application changes and is
authorized by Rick's instruction. Files: publishing workflow, README, six .ai
records. Feature merge commits: #7 33e0df9, #9 efa71ea, #11 e5dcaa9, #13 9d49139,
#15 41548fc. All feature work is now on main.

**Tests:** 49 JVM tests pass per debug/release variant; both builds/lint and
instrumentation compilation passed on unchanged application source. Main/source
app tree equality verified. Five publisher tests and actionlint passed. Hosted
publisher action succeeded; all six native APK digests and latest downloaded SHA
verified. Actual phone/permission/installer/audio/live-account tests NOT RUN.
Windows Android CI is still queued; no remote Android success claimed.
**In progress:** No unfinished merge/publication work. Physical device validation
and optional independent review remain for Rick.
**Blockers:** No release attachment blocker remains. Session uploads.github.com
authentication still HTTP 401, so use hosted publisher for future assets.
Existing local cache/FreshRSS in-memory/TTS/test-signing limitations remain.
**Next action:** Install native FlipRSS.apk; validate reader/Saved/source switching
and Settings updater/permission/installer on phone. Future releases add immutable
verified artifact/version to manifest and dispatch Publish APK releases on main.
**Shared records updated:** Yes; append-only activity/decision history preserved,
live issue board rebuilt, no active implementation claims.
**Remote synchronization:** Feature/publisher merges confirmed, main fetched,
all native release assets and latest downloaded bytes verified.
