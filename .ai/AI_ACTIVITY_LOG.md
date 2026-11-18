# AI_ACTIVITY_LOG

_Append-only. Add one dated entry at the end. Never edit or reorder earlier entries._

- 2026-11-15 — **Claude** — Bootstrapped the 3-AI collaboration system: created
  `AGENTS.md` (canonical protocol + filled PROJECT DETAILS) and the six `.ai/`
  files. Recorded the agreed design (native Android / Kotlin+Compose client for
  FreshRSS via the GReader API, Flipboard-style UI). No application code added.
  Branch `claude/coordination-setup`.

- 2026-11-15 — **ChatGPT Codex** — Issue #2, authorized by Rick’s prototype request.
  Implemented native offline Compose prototype (magazine grid, topics, swipe
  reader, persistent read/bookmark state), Gradle wrapper/project, bundled demo
  articles/photos with credits, unit/UI tests and Android build/emulator CI.
  Files: `app/**`, root Gradle files/wrappers, `.github/workflows/android.yml`,
  `README.md`, `PHOTO_CREDITS.md`, AGENTS.md project status and `.ai/**`.
  Local unit tests (3), lint, debug APK and test APK builds passed; instrumentation
  execution pending CI/device (cloud emulator blocked by disk constraints, no KVM).
  Branch `codex/2-prototype`; no merge performed. Claude’s merged PR #1 confirmed;
  stale bootstrap ownership/handoff corrected. Rick names the independent reviewer.

- 2026-11-15 — **ChatGPT Codex** — Published implementation commit `1f3eaaf`
  for #2 and opened PR #3 (https://github.com/rickevans86/FlipRSS/pull/3).
  Confirmed Issue #2 has `ai:codex`, `status:review`; requested Rick’s review and
  independent reviewer selection. Synced handoff/task-board/PR links. Final local
  checks passed (3 unit tests, lint 0 errors/7 warnings, debug and test APKs).
  CI/device results pending. PR remains unmerged.

- 2026-11-15 — **ChatGPT Codex** — Checked PR #3 CI run 43603822915 and its
  check annotations. Both jobs failed to start because GitHub reports recent
  account payment failures or a spending-limit restriction. No CI code/test
  execution occurred and no CI APK artifact exists. Local checks remain passed;
  device validation is pending. Updated handoff/state with the external blocker.

- 2026-11-15 — **ChatGPT Codex** — At Rick’s request, #2 / PR #3 CI retry.
  Routed both Android jobs to Rick’s `SelfHosted` label, report host OS/arch,
  serialize emulator after build and skip unsupported Windows/ARM emulator hosts.
  Removed hosted Ubuntu sudo/KVM changes and duplicate branch push triggers.
  Workflow actionlint and diff checks passed. App source unchanged; remote
  build/device results pending the new workflow run.

- 2026-11-15 — **ChatGPT Codex** — Confirmed self-hosted retry run 43646504809
  queued on label `SelfHosted` without an assigned runner. No new CI tests ran.
  Workflow syntax/diff checks passed; issue #2 returned to review. Removed file
  claim and synchronized handoff/state. PR #3 remains open and unmerged.

- 2026-11-18 — **ChatGPT Codex** — #2 / PR #3: Rick requested a new build,
  then explicitly authorized pushing to main and running Actions, superseding
  the earlier Rick-only merge rule for this publication. Locally forced APK
  packaging and reran 3 unit tests (0 failures/errors); lint passed with 0 errors
  and 7 warnings; APK signature verified. No application source changed.
  Updated shared handoff/state/task board and integrated latest main without
  discarding Claude’s scaffold/revert history. Authorized PR merge/Actions
  outcome to be confirmed in GitHub, not inferred from this pre-merge record.

- 2026-11-18 — **ChatGPT Codex** — Per Rick’s explicit instruction to push to
  main and run Actions, merged PR #3 as `1195fd8`; issue #2 closed automatically.
  Confirmed main push triggered run 44223942127, queued for `SelfHosted`.
  Rick then confirmed the runner is Windows. Updated only workflow/shared records:
  Windows labels, built-in Windows PowerShell, UTF-8 runner outputs and gradlew.bat.
  Emulator action skips Windows. actionlint/diff checks passed. Local APK
  packaging, 3 rerun unit tests and lint previously passed this session; no new
  remote success/device execution claimed. Windows update will trigger a new run.

- 2026-11-18 — **ChatGPT Codex** — #4, authorized by Rick: built and published
  v0.1.0-prototype GitHub prerelease from main `92613d6`. Release build, all 3
  release unit tests and lint passed (0 errors/1 warning; 57 tasks executed).
  APK non-debuggable, signed with existing prototype test identity (no production
  key configured/published), v2/v3 and alignment verified. APK 11,496,577 bytes;
  SHA-256 `8c392db42fd0640797faa33ef611506dd9d44cf2c5ea34527bb4c46f6cfddceb`.
  Release-asset upload returned HTTP 401; APK/checksum/metadata hosted separately
  at artifact commit `0b94348`, downloaded through GitHub blob API and verified.
  Published release/tag confirmed at https://github.com/rickevans86/FlipRSS/releases/tag/v0.1.0-prototype .
  Updated all six shared .ai/ files on records branch codex/4-prototype-release.
  No application source changes or device tests; Windows run was cancelled.

- 2026-11-18 — **ChatGPT Codex** — #6, authorized by Rick: implemented FreshRSS
  login, secure token persistence, catalogue/counts, paged live stories, feeds/
  topics/saved/reader UI and read/unread/star writes with token renewal, rollback
  and stale-account/request guards. Demo state remains separate. Jsoup text/cover
  extraction off UI thread, Coil images, HTTPS default/explicit HTTP, redirects
  disabled and no credential logging or API auth on images. Source `edcac98`.
  Files: app/**, README.md, AGENTS.md project details and all six .ai/ files.
  All 18 unit tests passed in each debug/release variant; lint/build passed and
  instrumentation APK compiled. No device/real-account tests. Preview APK v2
  test-signed and verified; 13,164,707 bytes, SHA-256
  `048a009c790c3c1960450dce2deae53966d669d847b6b48c7de67d83428b07ae`.
  Download branch codex/6-freshrss-download, immutable commit `6f6673b`; retrieved
  APK bytes verified. Preserved prior release log/decisions from PR #5, whose
  duplicate shared-record PR will be superseded. No #6 main merge or new release.

- 2026-11-18 — **ChatGPT Codex** — Opened FreshRSS PR #7 for #6; confirmed
  `ai:codex` / `status:review`. Preserved all release log/decision entries from
  PR #5 and closed that duplicate records-only PR as superseded; #4 now tracks
  records in PR #7. Preview/download verified; no main merge or new release.

- 2026-11-18 — **ChatGPT Codex** — #8: implemented Rick’s Add Feed, persistent
  top hide-read, confirmed/scoped mark-all-read including unloaded server items,
  and settings (theme/system bars, reader size, auto-read, images). Uses existing
  auth/write tokens, unread query filtering, cutoff/account/scope guards and
  confirmed-success refresh/error paths. Source 302a180, stacked on #6 / PR #7.
  Files: app/**, README/AGENTS project details and all six .ai/ files. All 28 JVM
  tests passed per debug/release variant; lint 0 errors/1 warning each; build and
  instrumentation APK compilation passed. No device/real-server execution.
  Preview v0.3 test-signed/non-debuggable; 13,197,475 bytes, SHA-256
  `15a59d88a5dc4dd421e94ebd02d347989c2f73904c03d47539bb6b0cc6afff69`.
  Artifact commit 1bdaf3d downloaded and checksum verified. No main merge or new
  release; follow-up PR must be retargeted after PR #7 merges.

- 2026-11-18 — **ChatGPT Codex** — Opened PR #9 for #8, base codex/6-freshrss
  (PR #7). Confirmed issue status review and recorded preview/checks. Main and
  published releases unchanged. Merge #7 first, retarget #9 to main and sync
  records before merging; Rick selects the independent reviewer.

- 2026-11-18 — **ChatGPT Codex** — Implemented #10 article speech on
  codex/10-article-speech, source 4b0dd285. Added Android TTS controller, reader
  controls, lifecycle/audio-focus cleanup, long-text chunking, voice errors/retry,
  package visibility, version 4 and text tests; updated README and six .ai files.
  31 JVM tests pass per debug/release variant; both builds/lint pass (0 errors,
  1 existing KTX warning each); instrumentation compiled, not executed. No actual
  audio/device/live-server tests. Preview artifact b639c9e, 13,217,955 bytes,
  SHA-256 81bf3914680dc07e028a1800da819b8ad561a4538f8f2ab9544a2740b494ee8d;
  test-signed release variant, signature/alignment/version/SDK checks pass.
  Stacked on PR #9 / #7; Rick selects reviewer and merges. No main merge or
  published release performed.

- 2026-11-18 — **ChatGPT Codex** — Opened PR #11 for #10, base
  codex/8-reading-controls (PR #9), and confirmed issue status review. Retrieved
  preview APK through GitHub blob API and verified bytes/SHA-256. Updated handoff,
  project state and task board from live Issues. Merge chain: #7 then retarget #9,
  then retarget #11; Rick selects independent reviewer. Main/release unchanged.

- 2026-11-18 — **ChatGPT Codex** — #12, explicitly authorized by Rick:
  implemented standalone local RSS/Atom, optional FreshRSS source switching and
  hide-empty-category preference. APK source 63f47b52, version 5. Files: app/**,
  README/AGENTS, release workflow/script/tests/manifest, Android CI, six .ai files.
  All 42 JVM tests pass per debug/release variant; both builds/lint pass (0 errors,
  1 existing warning each), instrumentation compiled. Four publisher tests and
  actionlint pass. No actual device/audio/real-account testing. Test-signed APK
  artifact 1ff1e66, 13,234,339 bytes, SHA-256
  1a21f4bab30d7aebb731556e63e949affc74853be84eb0d1850bb104f0f00f6c; signature/
  alignment/version/SDK and downloaded blob checksum verified. Published v0.2,
  v0.3, v0.4 and v0.5 GitHub prerelease pages with APK links; prior v0.1 retained.
  Direct asset upload returned HTTP 401. Triggered Windows SelfHosted publisher
  run 44247234997, queued; no asset-upload success claimed. Stacked on #11/#9/#7;
  Rick selects reviewer/merges; no main merge performed.

- 2026-11-18 — **ChatGPT Codex** — Opened PR #13 for #12, base PR #11,
  confirmed issue review label and updated records from live GitHub. Published
  v0.5 release page and verified APK download; all v0.1–v0.5 pages confirmed.
  Triggered latest publishing run 44247783494 (pending behind queued backfill
  44247234997). No assets attached yet; HTTP 401/session limitation documented.
  Shared records updated, source/artifact branches synchronized. No main merge.

- 2026-11-18 — **ChatGPT Codex** — #14, authorized by Rick: implemented regular
  release naming FlipRSS.apk, verified foreground GitHub updater/Android installer,
  always-visible Saved/read bookmarks and page heading/account-banner cleanup.
  Source de926519. Files: app updater/network/UI/controller/manifest/provider/tests,
  publisher/workflow/manifest/tests, README and six .ai files. 49 JVM tests pass per
  debug/release variant, both builds/lint pass (0 errors, 1 existing warning each),
  instrumentation compiles, 5 publisher tests and actionlint pass. No actual phone/
  installer/permission/audio/live-account testing. APK 13,234,395 bytes, SHA-256
  ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4, artifact
  3cd57a6. Signature/alignment/version/SDK/download hash and v0.5 signer equality
  verified. Published regular/latest v0.6.0. Direct asset upload again HTTP 401;
  changed publisher to any SelfHosted runner and standard filenames. Cancelled
  superseded runs 44247234997/44247783494; run 44251809984 queued. No main merge
  or native-asset completion claimed. Rick selects independent reviewer.

- 2026-11-18 — **ChatGPT Codex** — Opened PR #15 for #14, base PR #13, and
  confirmed issue review status from live GitHub. Cancelled superseded publisher
  run 44251809984 and triggered final six-build manifest run 44252735121 (queued).
  v0.6.0 confirmed regular/latest, native assets still empty; download link
  verified. Recorded outstanding runner/phone/independent review requirements.
  Source/artifact/release records synchronized; no main merge.

- 2026-11-18 — **ChatGPT Codex** — #16, Rick explicitly authorized all merges
  and native asset publication. Merged PR #7→#9→#11→#13→#15 into main using
  merge commits and retargeting; main 41548fc. Preserved history/binary separation.
  Main app/workflow/README matched tested final feature branch. No app change or
  independent self-review claimed. Direct upload still HTTP 401; moved publisher
  only to GitHub-hosted Ubuntu (public repo) on codex/16-publish-assets. Five Node
  tests and actionlint pass; prior 49 tests/variant remain applicable to unchanged
  app. Files: publication workflow, README and six .ai records. Native asset
  upload not yet complete; follow-up PR/action required.

- 2026-11-18 — **ChatGPT Codex** — Completed #16 at Rick's explicit merge-all/
  publish-assets instruction. PRs #7/#9/#11/#13/#15 merged sequentially; hosted
  publisher PR #17 merged at 12b2587. Main app tree equals tested de926519 source.
  Native asset publishing action 44254047153 SUCCESS; all six releases have
  FlipRSS.apk, SHA256SUMS.txt and build-info.json. Every APK asset digest matches
  manifest; downloaded v0.6.0 native bytes (13,234,395) and SHA-256
  ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4 verified.
  Closed #4/#6/#8/#10/#12/#14/#16. Files: publishing workflow, README and all
  six shared records. Five publisher tests/actionlint pass; unchanged app's prior
  49 tests per debug/release and build/lint remain applicable. No actual phone/
  installer/audio/live-account tests or independent review claimed; Windows build
  CI still queued. Binary history/signing key preserved separately. Publication
  blocker resolved using GitHub-hosted GITHUB_TOKEN, direct session still HTTP 401.
