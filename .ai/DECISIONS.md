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
