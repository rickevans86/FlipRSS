# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #16 — Rick explicitly said "Merge all to main / Publish the apk as an asset".
This session authorization supersedes the default no-agent-merge rule for this task.
**Completed:** Merged #7 (33e0df9), #9 (efa71ea), #11 (e5dcaa9), #13 (9d49139),
#15 (41548fc), retargeting each to main in order. Preserved commits, all feature
code/shared-record history and all binary branches separately. No independent
review/self-review claimed. Main app matches tested v0.6.0 source de926519.
**Files:** .github/workflows/publish-previews.yml, README.md, six .ai records.
**Branch/PR:** codex/16-publish-assets, publisher follow-up PR to open from main.
Upload-only runner changes from SelfHosted to ubuntu-latest; public repo hosted
runner needs no local Android SDK/keystore. Existing Windows build CI unchanged.

**Release:** https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
**APK:** FlipRSS.apk, versionCode 6, Android 8+, 13,234,395 bytes, release variant,
non-debuggable, existing test signer matching v0.5. SHA-256:
`ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4`.
Source app de926519c5f44c3c8f3f19553bcab78ffafb98fd is now an ancestor of main.
Immutable artifact 3cd57a66a4672990f0a2a52dc179f7199b3f99e5,
release-download/FlipRSS.apk, codex/14-release-download (never merge).

**In progress:** Native assets pending. Direct upload again HTTP 401. Prior
SelfHosted run 44252735121 still queued. Merge publisher follow-up, cancel that
superseded run, dispatch on main and verify actual assets/downloaded SHA.
**Tests:** Merged app/workflow/README equality against final feature branch
confirmed. Prior 49 tests pass per debug/release, builds/lint and instrumentation
compilation passed; no app changes here. Five publisher tests and actionlint pass.
Actual phone/permission/installer/audio/real-account tests not run.
**Blockers:** Direct session upload credential restriction; hosted action may
resolve it. Do not claim successful attachment until release API and downloaded
bytes confirm it. No signing key uploaded.
**Next action:** Complete hosted publisher and update Issue/records with outcome.
**Shared records updated:** Yes, append-only activity/decision history preserved.
**Remote synchronization:** Feature PR merges confirmed by GitHub, main fetched,
source/app tree equality verified; publisher branch pushed.
