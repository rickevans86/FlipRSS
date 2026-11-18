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
