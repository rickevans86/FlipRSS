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
