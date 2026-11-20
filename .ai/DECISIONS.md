# DECISIONS

_Append-only architectural decisions. Add one dated entry at the end; never edit or
reorder earlier entries._

## 2026-11-15 — Initial architecture (Claude, agreed with Rick)

- **D1. Platform: native Android.** Chosen over web/PWA and desktop. Rick already
  builds and ships Android apps, and native gesture handling suits the Flipboard
  swipe/flip reader.
- **D2. Language/UI: Kotlin + Jetpack Compose (Material 3).** Modern standard for
  new Android apps; declarative UI fits the card-grid and pager layouts.
- **D3. Backend: FreshRSS GReader API** (`/api/greader.php`). The instance is on the
  public internet, so a native OkHttp client connects directly — no CORS proxy or
  intermediate backend. Auth: `ClientLogin` (user + FreshRSS *API password*) →
  `GoogleLogin auth=<token>` header; a write/edit token is fetched for mutations.
- **D4. Three equal UX pillars:** magazine card grid, full-screen swipe/flip reader,
  topic sections (FreshRSS folders/tags). None is secondary.
- **D5. Cover images:** derive from item enclosures/`media:content`, else first
  `<img>` in the item HTML (Jsoup). Load with Coil.
- **D6. Credentials:** stored with EncryptedSharedPreferences / DataStore; never
  committed, logged, or placed in Issues/PRs.

### Open items to confirm (flagged in PROJECT DETAILS)

- min SDK / target SDK versions.
- Offline cache: Room vs in-memory first pass.
- DI: Hilt vs manual — defer until the skeleton exists.
- Networking: Retrofit vs Ktor (leaning Retrofit+OkHttp).

## 2026-11-15 — Offline Android prototype, Issue #2 (Codex)

- Rick authorized building the app prototype in chat. Implement all three UX
  pillars using original fictional sample content, visibly marked as a demo;
  no live connection or credentials required. Bundled photos work offline.
- Use min SDK 26 and compile/target SDK 35 with AGP 8.9.1, Gradle 8.11.1,
  Kotlin 2.1.20 and Compose BOM 2025.03.01 as a pinned prototype baseline.
  Re-evaluate target SDK/dependency versions before a production release.
- Keep only demo story IDs in SharedPreferences, managed by an AndroidViewModel.
  This is not a credentials architecture decision; secure storage and GReader
  authentication remain future assigned work. No networking/Room/DI added yet.
- Use Compose HorizontalPager with a stable captured story queue and accessible
  previous/next controls; unsaving a story does not change reader position.
  A 3D flip/page-curl effect is deferred pending prototype feedback.

## 2026-11-18 — Prototype prerelease distribution, Issue #4 (Codex)

- Rick authorized building and publishing a release. Existing version is
  0.1.0-prototype and FreshRSS integration is absent, so publish a prerelease.
- Build release variant and sign with the existing prototype test identity;
  explicitly disclose test signing and non-production status. No production
  signing configuration or private keys added to Git.
- Release-asset uploads reject connected credentials (HTTP 401). Publish the
  release with immutable links to a separate artifact branch; tag still points
  to actual source main commit. Verify retrieved APK bytes against SHA-256.
- Keep artifact branch separate from record-only PR to avoid merging APKs into
  main. Production signing and live account integration remain future tasks.

## 2026-11-18 — FreshRSS online integration, Issue #6 (Codex)

- Use Retrofit/OkHttp directly against GReader, Jsoup for text/cover extraction
  and Coil for images. Preserve folders/tags/feed IDs and opaque continuation/item
  IDs without numeric narrowing. Resolve subdirectory roots/API endpoint URLs.
- HTTPS by default; explicit trusted-local HTTP opt-in. Reject URL credentials,
  query/fragment in instance URL. Do not follow API redirects; no credential
  replay. Image requests are separate and never inherit API auth headers.
- Store auth token (not password), username, endpoint and HTTP setting with
  Android Keystore-backed EncryptedSharedPreferences per the recorded plan;
  security-crypto 1.1 APIs are deprecated and future migration can be separate.
  Exclude session from backup/transfer; explicit forget can reset damaged storage.
- Pure StateFlow controller owns account boundaries, pagination and optimistic
  read/star operations with rollback/errors. Separate auth-attempt and active-
  account generations preserve valid writes after failed reauthentication.
- Keep live articles in memory for this milestone; no Room/offline mutation
  queue/background sync. Preserve explicit offline demo and separate preferences.
- Render extracted text without WebView/active HTML; cover images and open-original
  links are supported. Rich inline layout/images remain future work.
- Deliver versionCode 2 / 0.2.0-freshrss-preview as a test-signed preview download;
  no new release/main merge is implied by the implementation request.

## 2026-11-18 — Feed and reading controls, Issue #8 (Codex)

- Stack on unmerged FreshRSS PR #7, with a separate follow-up PR for review.
- Subscribe through GReader subscription/quickadd; validate HTTP(S) URL, use
  write token and require numResults=1/streamId before claiming success. Default
  server folder only. Do not echo raw server diagnostics or persist feed URL.
- Hide-read is one global preference, uses server xt=read on every page and local
  visible filtering so read changes disappear immediately while reader queue
  stays stable. Keep it across account changes and restarts.
- Bulk read uses server mark-all-as-read with confirmed scope and microsecond
  cutoff captured at confirmation opening. Includes unloaded items, not a loaded
  card loop. Snapshot account/known IDs, block conflicting writes and refresh
  authoritative counts after success; report failures without claiming success.
- Persist nonsensitive UI preferences in separate SharedPreferences: theme,
  reader size, auto-read, images, hide-read. Token storage remains encrypted and
  untouched. Reset defaults changes preferences only. Theme updates system bars.
- Deliver versionCode 3 / 0.3.0-controls-preview with existing test identity on a
  separate download branch. No main merge/new release implied by feature request.

## 2026-11-18 — Article speech (#10, ChatGPT Codex)

- Use Android TextToSpeech with foreground play/pause/resume/stop controls. Read
  title and supplied body, summary only when body is empty; no external scraping.
- Split text below engine input limit at word boundaries where possible and never
  bisect UTF-16 surrogate pairs. Resume from range callbacks when supplied;
  otherwise restart current chunk. Generation IDs reject stale engine callbacks.
- Pause on audio focus loss/background; stop on page change/reader exit and shut
  down engine on disposal. No background service or automatic next article.
- Device language/configured engine; prefer installed voice, expose voice settings
  and retry for setup errors. Engine may use network when offline voice unavailable.
  Add TTS service query for Android 11+, no microphone permission or own cloud API.
- VersionCode 4 preview uses existing test identity and separate binary download
  branch. Keep #10 stacked on #9 / #7; no main merge/new release implied.

## 2026-11-18 — Standalone feeds, categories and release delivery (#12, Codex)

- Direct RSS/Atom feeds use a separate local repository with atomic private JSON
  cache/subscriptions/read/saved persistence; reuse common reader/controller
  behavior. New installs default local; preserve existing FreshRSS sessions and
  isolate data. Switching modes keeps both sources; no implicit import/merge.
- Direct HTTP(S) XML URLs, stable feed-scoped hashed entry IDs, bounded redirects
  without HTTPS downgrade, 5 MB/500-entry input limit, no FreshRSS auth on feeds.
  Keep 500 cached stories per feed plus saved stories. Refresh in foreground;
  cached text works offline. Local categories use feed titles.
- Persist hide-empty-categories independently from hide-read. Filter zero unread
  categories/feeds everywhere; keep unknown counts visible. Return to All stories
  when selected category disappears; read/unread changes can restore categories.
- Rick explicitly requested GitHub Releases for all previews. Publish prerelease
  pages with verified download links and immutable source/artifact provenance;
  leave main unmerged. Use Windows Actions token for asset uploads because this
  session's uploads.github.com authentication returns HTTP 401. Verify hashes and
  refuse mismatched existing assets; no signing keys or binaries in source PR.

## 2026-11-18 — Regular APK releases, updater and Saved (#14, Codex)

- Rick explicitly requested regular release: use v0.6.0 (versionCode 6), regular/
  latest GitHub Release, FlipRSS.apk asset filename for all versions. Preserve
  older immutable versioned binaries/history; alias identical signed bytes in
  native release assets. Keep existing test signer for upgrade compatibility.
- Publisher can run on any SelfHosted runner; Node script has no OS dependency.
  Replace superseded queued publisher jobs. Use GITHUB_TOKEN for native uploads
  because session uploads.github.com authentication still returns HTTP 401.
- Update checks are manual from Settings and use latest regular release metadata.
  Require exact APK filename/repository URL, valid newer versionCode and bounded
  size/SHA-256. Download from allowlisted GitHub HTTPS hosts without account auth.
  Before installation require same Android package and signing certificate and
  matching newer version. Share private cache APK read-only through FileProvider,
  request Android unknown-source permission and use system installer confirmation.
  Missing native assets show pending publication instead of claiming update success.
- Saved scope ignores global hide-read in server paging and local visibility.
  Remove Hide read on Saved; retain global setting on Today. Remove page perspective
  heading/subtitle and account banner; account remains available via Settings.

## 2026-11-18 — Rick-authorized merges and hosted release publishing (#16, Codex)

- Rick's explicit "Merge all to main" instruction authorizes Codex to merge this
  feature chain and necessary publication/record follow-up. Retarget stacked PRs
  sequentially and use merge commits to preserve ancestry. No self-review claimed.
- Keep binary branches unmerged. Existing signed v0.6.0 APK app source is identical
  to merged app, so retain its immutable provenance/signature and release tag.
- Run upload-only publication on GitHub-hosted Ubuntu, independent of queued
  SelfHosted hardware. Repo is public. Retrieve immutable signed artifacts, verify
  hashes and upload with scoped GITHUB_TOKEN; never transfer signing keystore.

## 2026-11-18 — Native release assets are canonical (#16, Codex)

- Hosted publication succeeded: use native releases/download/<tag>/FlipRSS.apk
  and matching release build metadata for installations and in-app updates.
  Immutable repository artifact links remain provenance/fallback, with no retag
  or APK rebuild when merged app tree matches the tested signed source.
- Attach APK/checksum/build metadata to every manifest release and verify GitHub
  APK digests plus a downloaded native latest APK before claiming publication.

## 2026-11-18 — OLED and optional feed directory (#19, Codex)

- Add OLED separately from Dark: all neutral app surfaces/background #000000,
  transparent elevation tint and no navigation contrast scrim; retain readable
  text/controls/selection accents and existing theme choices. Persist enum by name.
- Bundle curated well-known feed URLs and search/filter locally by words/topic.
  No API keys, external query service or whole-web scraping. Reuse existing Add
  feed validation/confirmation and active local/FreshRSS backend; preserve selected
  URL across required source selection. New subscriptions require confirmation.
- Show feed search defaults on; disabling removes nav tab and returns active Search
  to Today, without touching subscriptions/account or other page preferences.
- Publish installable review APK as v0.7.0-preview with standard FlipRSS.apk and
  native checksum/metadata. Leave source PR for Rick's merge; after review/merge
  create matching regular v0.7.0 tag using same verified version-7 signed artifact.

## 2026-11-18 — Independent sources and live discovery (#21, Codex)

- Keep local and FreshRSS controllers concurrently, same persisted UI settings;
  account/source stores remain separate. Combined Saved includes read bookmarks
  and routes writes by article origin. Preserve existing local file/token.
- FreshRSS/FlipRSS/Search/Saved tabs; remove Topics, retain source category chips.
  + only FreshRSS. Search always adds local. Local feed menus expose URL/copy and
  explicit account-confirmed server add without moving/deleting local data.
- Default-off scroll read observes actual rendered cards and marks only on forward
  scroll past viewport top; not Saved/Search or unseen jumped items.
- Import actual public Feeder table/add-feed URLs and Feedspot data-site/RSS links,
  bounded/unauthenticated requests. Atomic cache per source, daily foreground and
  manual refresh, failed-source retention, normalized dedup/bundled fallback.
- Website discovery validates RSS/Atom from direct URL, alternate/RSS/icon links
  and common paths, including when home page blocked. Bound size/time/redirects/
  candidates; no Basic credentials or FreshRSS auth forwarded, no silent subscribe.
- Rick explicitly authorizes merging/publishing #21. Use hosted publisher with
  immutable signed artifact for regular v0.8.0 and preserve source provenance.

## 2026-11-18 — Verified regular v0.8.0 publication (#21, Codex)

- Publish regular v0.8.0 (versionCode 8) from the tested app source identical to
  main; retain earlier preview/history and existing upgrade signer. Native APK/
  checksum/build metadata verified by hosted publisher plus downloaded APK hash.
- Keep lists cached per source with bundled fallback; do not modify subscriptions
  when directory entries disappear. Daily refresh is foreground and disabled
  with Search; metadata source counts indicate extraction, not validation of all
  external targets. Actual source/installer UI and account writes require phone
  validation; do not infer that from JVM/build/publication success.

## 2026-11-18 — Search name regression and cache upgrade (#24, Codex)

- Match Feedspot numeric row ID with its own heading; only use row-local fallback
  when no numeric ID exists. Never search a shared ancestor or the global RSS link
  list for titles. Skip unmatched entries rather than mislabel subscriptions.
- Version directory cache (schema 2); old bad labels are discarded and refetched
  immediately, regardless of daily timer. Local subscriptions and Saved unaffected.
- Limit displayed catalogue to 20 per explicit Load more and reset on criteria/
  dataset change; preserve distinct feeds with legitimately identical publishers.

## 2026-11-18 — Quality-ranked configurable Android speech (#25, Codex)

- Use installed Android engines/voices with preview and persistent speaker/rate/
  pitch controls. Optional cloud/provider question unanswered; announced Android
  path avoids requiring a new account/key. No claim of audible human-like quality.
- Rank locale/engine quality, respect manual speaker, exclude uninstalled and
  network-required voices unless explicit online opt-in. Do not silently fall
  back to a network-only default when offline voice absent. Explain text sharing.
- Recreate engine only for engine/speaker/network policy changes; tuning updates
  stop current playback and update rate/pitch without repeated engine churn.
  Lifecycle/focus/generation safety and user preview remain.
- Ship Search cache/title/paging correction together with speech as regular
  v0.9.0/code 10; intermediate 0.8.1/code 9 was not separately published.

## 2026-11-18 — Combined v0.9.0 verified (#24/#25, Codex)

- Publish one regular code-10 update containing Search correction and requested
  speech controls; intermediate code 9 never became a separate release. Preserve
  installed-data/signing compatibility, invalidate only obsolete public directory
  cache and verify actual native bytes plus all asset digests.
- Online voice selection remains explicit opt-in, quality engine-rated and preview
  user-controlled. Do not describe unit/build success as audible naturalness or
  claim a separate paid cloud AI provider was integrated.

## 2026-11-18 — Bulk-read shortcuts (#29, Codex)

- Top and footer share one controller request policy. Confirmation defaults on;
  disabling it immediately calls the same guarded bulk operation with captured
  source/category/cutoff. The footer follows Load more and is absent from Saved/Search.
- Complete #30 separately, then ship both in one native release.

## 2026-11-18 — Public article expansion (#30, Codex)

- Fetch only the opened article using existing unauthenticated bounded FeedWeb;
  prefer semantic article body/entry/main blocks, strip non-story/hidden content,
  reject explicitly restricted metadata, and replace feed paragraphs only when
  materially longer. Never execute scripts or carry FreshRSS credentials.
- Keep original article identity/ownership/read/bookmark state. Displayed expanded
  paragraphs also drive speech; text changes stop playback. Generation guards
  discard canceled selections, successes use a 20-story memory cache.
- Automatic loading defaults on per request with persistent off switch/manual
  retry and feed fallback. No permanent full-article cache or paywall bypass.
- Ship #29/#30 together as regular v0.10.0/code 12; code 11 remains intermediate.

## 2026-11-18 — Combined v0.10.0 publication verified (#29/#30, Codex)

- Publish one regular code-12 update for bulk-read controls and public article
  expansion, retain prior signing identity, verify actual native bytes and all
  ten release digests. Intermediate code11 was not separately released.
- Keep publisher-specific extraction limits explicit. Build/JVM/public HTML
  validation does not establish phone UI/audio/account-write behavior.

## 2026-11-18 — Screen awake during article speech (#34, Codex)

- Use the Compose reader view keepScreenOn flag only while speech actively plays
  and the reader lifecycle is resumed. Restore the previous view flag on effect
  disposal and release our request immediately on lifecycle pause.
- Reuse existing speech playing state, which clears on pause/stop/end/failure/
  focus loss. No wake lock, new permission, preference or preview behavior change.
- Publish patch v0.10.1/code13 with existing update-compatible signing identity;
  device assertions compile only and require actual phone validation.

## 2026-11-18 — Screen-awake patch publication verified (#34, Codex)

- Ship v0.10.1/code13 as a regular upgrade with existing signing identity; verify
  actual native download and all eleven release APK digests after hosted publish.
- Keep physical screen-timeout/audio validation explicitly pending; compiling
  lifecycle assertions is not a device test result.

## 2026-11-19 — Native browser and pinned add-on (#37, Codex)

- Use current GeckoView, since Android WebView cannot execute the requested Firefox
  WebExtension. Upgrade required compile toolchain and retain target35/min26.
- Bundle original BPC XPI/byte-identical extracted files with MIT/provenance;
  include Gecko MPL and DOMPurify source/licence notices. Add-on defaults off with
  access description, optional-permission prompts, site popup/options. No automatic
  add-on binary updates; its own optional rule/version settings remain upstream.
- Browser profile/session lifecycle stays separate from FreshRSS API. Guard web
  URLs and own extension origin; don't bypass certificate failures. Reader text/
  speech are unaffected by browser extension output.
- Universal native engine APK exceeds Git's100MB file limit and old updater80MB
  cap. Use immutable ordered32MiB parts with per-part+whole-file verification,
  expose normal FlipRSS.apk on Releases, raise future updater cap512MB/timeout.
  Existing clients require one direct install over current app, same signer.
- Ship v0.11.0/code14 for ARM64/ARMv7/x86-64. Runtime/browser/site-bypass testing
  requires actual device and is not inferred from compiler/JVM/native-byte checks.

## 2026-11-19 — Browser v0.11.0 native publication verified (#37, Codex)

- Verify real eight-part reconstruction/upload and all twelve native release
  digests, then download latest APK/metadata/checksum. Keep one standard filename
  for users; multipart transport is internal and never merged into main.
- Record one-time direct-install requirement for old80MB updater clients, keep
  signing/data compatibility, and state add-on OFF-by-default enable instructions.
- Do not infer browser/site-specific extension runtime success from JVM/build/
  native-byte checks. Device validation remains an explicit next action.

## 2026-11-19 — Rendered page is the bridge between browser and reader (#40, Codex)

Use a separately owned native-messaging extension, not changes to BPC. Capture visible rendered article DOM with origin/story/session/nonce bounds and cleanup; respect existing add-on enable state. Retain static/feed fallback; explicit Read in app transfers bounded in-process matching-article token data. Automatic real-viewport renderer lives behind reader. Future reader-bridge changes require manifest version bump beyond1.0.0. Static metadata restrictions remain; browser capture excludes hidden script article data. Physical site-specific behavior needs device verification.

## 2026-11-19 — Rendered-reader native publication verified (#40, Codex)

Verify published native APK bytes and metadata/checksum against signed release; all13 historical APK digests match manifest. Keep one FlipRSS.apk release asset, internal eight-part transport only. Current v0.11 updater accepts size; older<=0.10.1 needs direct install. Record rendered extraction fixtures separately from unexecuted real Gecko/BPC/phone behavior; successful DOM simulation does not establish actual publisher compatibility.

## 2026-11-20 — Independent source-tab visibility (#43, Codex)

Persist showFreshRss/showFlipRss independently, default true for existing installs. Treat switches as navigation visibility; keep account and feed data and combined Saved contents. Always retain Saved for reachability when all optional tabs are hidden. Resolve active destination from available tabs immediately, then persist fallback selection and reset category; do not return automatically to re-enabled tabs. Continue explicit user main/build/publish authorization.

## 2026-11-20 — Source-tab release publication verified (#43, Codex)

Publish standard native FlipRSS.apk asset with same upgrade signer; verify hosted APK bytes, metadata and checksum against signed local build and all14 release digests against manifest. Keep eight-part transport branch unmerged. Distinguish all8-combination JVM navigation coverage and compiled Android settings assertion from unexecuted physical UI/restart checks.
