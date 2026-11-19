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

- 2026-11-18 — **ChatGPT Codex** — #19, authorized by Rick: implemented pure-
  black OLED neutral surfaces/system bars and persistent optional popular RSS
  Search tab, curated 15-feed directory/topic filtering and prefilled active-
  source add confirmation. App source 6f0ba16. Files: app theme/settings/nav/feed
  dialogs/directory/tests, README, release manifest and six .ai files. All 54 JVM
  tests pass per debug/release variant, both builds/lint pass (0 errors, 1 existing
  warning), instrumentation compiles; no actual phone/OLED/UI/account writes.
  All 15 included feed URLs returned HTTP 200/XML in read-only checks. Five
  publisher tests/actionlint pass. Hosted action 44256865471 SUCCESS, published
  v0.7.0-preview native FlipRSS.apk/checksum/metadata. Signature/alignment/version/
  SDK/signer equality and native downloaded hash verified. APK 13,250,779 bytes,
  SHA-256 a3b428e3958b52fc6ad0a7a06539dddc8798c89a2470fa0f7a19cdbb8cbf5f18,
  artifact 68fc7e2. Source awaits Rick-selected independent review/main merge; no
  new main merge performed or independent self-review claimed.

- 2026-11-18 — **ChatGPT Codex** — Opened PR #20 for #19 (base main) and
  confirmed review status. Native v0.7.0-preview APK bytes/hash and hosted
  publisher success verified. Updated handoff/project/task board with PR refs.
  No new main merge; Rick selects independent reviewer and validates on phone.

- 2026-11-18 — **ChatGPT Codex** — #21, Rick authorized main implementation,
  build and publication. Merged foundation PR #20 (0bd0077); implemented independent
  source tabs/combined Saved/routed writes, local-only Search,+ only FreshRSS, local
  feed URL/copy/confirmed server subscription, default-off scroll read, daily cached
  Feeder/Feedspot imports and website/direct-feed discovery. Source 7dc9763. Files
  app/**, README/release manifest and six .ai records. All 64 JVM tests pass per
  debug/release variant; builds/lint pass (0 errors, 1 existing warning), device
  tests compile only. Five publisher tests/actionlint pass. Production parsers
  extracted 114/454 public links from live downloaded pages; production-header
  HTTP checks both 200. No actual phone/installer/audio/account writes. APK
  artifact 04a95a4, 13,283,547 bytes, SHA-256
  39e89edc9e63ae0bd6c4b9f3a173b63a99c7013674cf94f8833eb26001bccaf6; signature/
  alignment/package/version/SDK and prior signer equality checked. Authorized
  merge and native publication follow; no independent self-review claimed.

- 2026-11-18 — **ChatGPT Codex** — Completed #21 at Rick's explicit instruction
  on main/build/publish. Foundation #20 merged at 0bd0077; implementation #22 at
  8a62d3d. Main app tree matches tested 7dc9763. Hosted publisher 44295156332
  SUCCESS; v0.8.0 confirmed latest regular, native FlipRSS.apk/checksum/metadata
  attached. All eight releases' APK digests match manifest; downloaded v0.8.0
  (13,283,547 bytes) SHA-256 39e89edc9e63ae0bd6c4b9f3a173b63a99c7013674cf94f8833eb26001bccaf6
  verified. Closed #19/#21, records/README updated, claims cleared. All 64 JVM tests
  per debug/release pass, both builds/lint (0 errors, 1 existing warning), device
  compilation, 5 publisher tests/actionlint pass. Live pages parsed 114/454 public
  links; no actual phone/installer/audio/account-write tests or independent review
  claimed. Source/binary histories/signing key preserved. Final records-only merge
  authorized by Rick, no additional app change.

- 2026-11-18 — **ChatGPT Codex** — #24: reproduced and fixed Feedspot's shared-
  ancestor first-heading bug, old-name cache invalidation and explicit 20-result
  search paging. Source 72b2956. Files importer/cache/search/nav/tests/version,
  README and six .ai records. 68 JVM tests pass per debug/release, build/lint pass
  (0 errors, 1 existing warning), instrumentation compiled only. Actual parser
  verified 453 URLs/449 titles, one NBC News, WordPress correct. No physical UI/
  audio/account checks. Merge and combined publication with added #25 speech
  request follow; no APK publication or independent self-review claimed here.

- 2026-11-18 — **ChatGPT Codex** — #25 natural speech request while #24 fix
  active. Merged Search correction #26 at 2b9525c. Added quality-ranked Android
  engine/voice selection, explicit online opt-in, preview, rate/pitch persistence
  and reader/settings entry points; source b1fb05f. Files speech/profile/controller/
  UI/settings/tests/version, README/release manifest/six records. 73 JVM tests
  pass per debug/release, build/lint pass (0 errors, 1 existing warning), device
  tests compile only. Five publisher tests/actionlint pass. No audible/phone/
  naturalness/account checks. Optional cloud preference unanswered; announced
  Android default used, no API key/account. APK artifact a166595, 13,299,931 bytes,
  SHA-256 823aba453b4923c0dbbb906349e36de510739902fa41c553dc3d462dd683fda4;
  signing/alignment/version/SDK/signer equality checked. Combined regular v0.9.0
  merge/publication follows; no native upload or independent review claimed yet.

- 2026-11-18 — **ChatGPT Codex** — Completed #24/#25, Search correction #26
  merged at 2b9525c and voice #27 at 155c680. Main app tree matches b1fb05f.
  Hosted publication 44300897612 SUCCESS; regular/latest v0.9.0 native APK/
  checksum/build-info attached. All nine APK asset digests match manifest; latest
  downloaded bytes (13,299,931) SHA-256
  823aba453b4923c0dbbb906349e36de510739902fa41c553dc3d462dd683fda4 verified.
  73 JVM tests per debug/release, both builds/lint (0 errors, 1 existing warning),
  instrumentation compilation, five publisher tests/actionlint passed. Actual
  phone/audio/naturalness/installer/account writes and independent review not
  claimed. Search old cache auto-cleared, 20-result pages; Android engine/voice/
  preview/tuning with explicit online opt-in shipped, no cloud key/account.
  Closed tasks, README/six records updated, no claims, binary/signing separation
  preserved. Final records-only merge continues authorized publication workflow.

- 2026-11-18 — **ChatGPT Codex** — ChatGPT Codex implemented #29: source-list bottom Mark all read, shared top/footer confirmation policy, persistent Confirm mark all read default on, source/category/cutoff and busy guards retained. Saved/Search excluded. Intermediate 0.9.1/code 11 is not separately published; #30 adds fuller public article text before one combined release. No other AI changes found in live issues/PRs/main since v0.9.0. Continuing Rick-authorized main/build/publish workflow; no independent self-review. Physical device/account tests not run.

- #29 validation: 76 JVM tests each debug/release pass, lint 0 errors / 1 existing warning, instrumentation compilation passed. Source c300c18.

- 2026-11-18 — **ChatGPT Codex** — #29 merged #31; #30 source 57db2bc implements public article text loading/retry/settings/cache/speech. Exact TechCrunch HTML smoke: 144 → 2,573 characters/9 blocks. 85 tests per debug/release pass; packaging/lint publication pending. No device validation or independent review claimed.

- #30 validation complete: 85 JVM tests each debug/release, builds/lint (0 errors/1 existing warning), instrumentation compile, five publisher tests/actionlint pass. Signed native v0.10.0 code12 artifact 6590f2e, 13,316,315 bytes, SHA-256 636928bfaed9eb0df748fb8f1a732ce4a5751a92100538dabbb5c6719cd75cf5. Signature/alignment/package/SDK/non-debuggable/signer equality verified; external metadata links source57db2bc because AGP worktree VCS unsupported. PR/main/publication follows; no independent review/device tests claimed.

- 2026-11-18 — **ChatGPT Codex** — Completed #29/#30. #31 merged2001f09,
  #32 merged74669a0, main app tree equals source57db2bc. Hosted publisher
  44400302581 SUCCESS; regular/latest v0.10.0 native FlipRSS.apk/checksum/build-info
  attached. All ten APK digests match manifest; latest downloaded13,316,315 bytes,
  SHA256636928bfaed9eb0df748fb8f1a732ce4a5751a92100538dabbb5c6719cd75cf5.
  85 JVM tests per debug/release, both builds/lint (0 errors/1 existing warning),
  instrumentation compile, five publisher tests/actionlint pass. Exact TechCrunch
  public HTML smoke144→2573chars/9blocks. Signature/alignment/version/SDK/signer
  equality verified; prototype test signer retained, no key published. Closed
  issues, README/six records updated, no claims. Device/audio/account-write tests
  and independent review not claimed. Final records-only PR follows.

- 2026-11-18 — **ChatGPT Codex** — #34 implemented reader screen-on lifecycle effect and two compiled device tests, source 25630005a5fc8f82a89af4907e8c2229ab5a286c. 85 JVM tests each debug/release, builds/lint (0 errors/1 existing warning), instrumentation compile, five publisher tests/actionlint pass. Disk-full interruption fixed by cleaning older generated outputs; source/keys/artifacts retained. Signed v0.10.1/code13 artifact 1ed3fd5, 13316315 bytes, SHA256 92b640a5fafb48d85996fed1417e4de97392c6a5e2aa8e46386c5a52d47822ee; signer/alignment/version/package/SDK checked. PR/publication pending, no runtime device/audio or independent review claimed.

- 2026-11-18 — **ChatGPT Codex** — #34 completed/closed, #35 merged at c0ef63d.
  Main app tree equals tested source 2563000. Hosted publisher 44411187965 SUCCESS;
  regular/latest v0.10.1 native FlipRSS.apk/checksum/build-info published. All
  eleven release APK digests match manifest. Latest native download verified:
  13,316,315 bytes, SHA256
  92b640a5fafb48d85996fed1417e4de97392c6a5e2aa8e46386c5a52d47822ee.
  85 JVM tests per debug/release, builds/lint (0 errors/1 existing warning),
  instrumentation compilation, five publisher tests/actionlint pass. Screen
  lifecycle/preservation tests compiled only; phone/timeout/audio not run.
  Signing/alignment/version/SDK/prior signer equality checked, no key published.
  Disk-full interruption resolved through generated-output cleanup, successful
  retry. README/six records synchronized, claims clear. No independent review
  claimed. Final records-only follow-up continues authorized publication workflow.

- 2026-11-19 — **ChatGPT Codex** — #37 implemented Gecko browser, pinned MIT BPC4.4.9.6 with controls/options/popup, basic web prompts, guarded navigation and separate browser profile. Source0289c2ccc82068025d7d7b2a9e9b3406e8d6fcd7; required SDK37.2/AGP9.5.1/Kotlin2.4.21/Gradle9.6 migration, retained target35/min26. 90 JVM tests each debug/release, release build/lint0errors17warnings, earlier debug lint0errors18warnings, instrumentation compile, 9 publisher tests/actionlint pass. Exact XPI/all assets/39 native libraries verified. Signed universal artifactda7515a5a2ad38cf979bae6fd2fb04d1ab320464, 261392018bytes, SHA25607f8cbba43ce1db0b9582388741f3e268776e3521b412f897ce248376dabd21c. New multipart publisher and updater512MB support; old80MB clients need one direct install. No physical device/extension/site-bypass or independent review claimed. Records/PR/publication follow; no signing key published.
