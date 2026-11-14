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

## 2026-11-15 — Build toolchain + CI (Claude)

- **D7. CI on Rick's self-hosted runner.** `.github/workflows/android-ci.yml`
  runs on `self-hosted`, sets up JDK 17 + Android SDK (`android-actions/setup-android`),
  builds `assembleDebug`, uploads the APK artifact. Triggers: push/PR to `main`
  and manual dispatch. Rationale: Rick runs a GitHub Actions runner on Unraid; this
  gives a build/verify loop since the authoring sandbox can't download the Android
  SDK/AGP.
- **D8. Versions.** AGP 8.7.3, Kotlin 2.1.0 (+ `kotlin.plugin.compose`), Compose
  BOM 2024.12.01, Gradle wrapper 8.14.3, compileSdk/targetSdk 35, **minSdk 26**
  (lets the launcher icon be vector-only adaptive — no PNG assets). May be bumped
  once the runner confirms a green build.
- **D9. Workflow (Rick's call 2026-11-15):** push straight to `main` for now
  instead of branch-per-Issue PRs — solo-with-AI speed. Protocol's PR flow remains
  documented and can be reinstated.
