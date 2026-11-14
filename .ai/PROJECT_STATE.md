# PROJECT_STATE

_Current project overview. Keep this to the present state of the repo._

## What FlipRSS is

A native Android client for a self-hosted FreshRSS instance that presents feeds in a
Flipboard-style experience. Three experience pillars (all equal priority, per Rick):

1. **Magazine card grid** — large cover images, varied tile sizes, clean typography.
2. **Swipe/flip reader** — full-screen, one story per page, flip between stories.
3. **Topic sections** — content grouped into named sections (FreshRSS folders/tags)
   you move between.

## Backend

FreshRSS GReader API at `<instance>/api/greader.php`, reachable over the public
internet. Native HTTP client connects directly (no CORS proxy needed). Auth via
`ClientLogin` (username + FreshRSS API password) → `GoogleLogin auth=<token>` header.

## Current status

- **Stage:** Skeleton + CI. Buildable Kotlin/Compose app shell (landing screen
  only) with a committed Gradle wrapper and an Android CI workflow that builds the
  debug APK on Rick's self-hosted runner.
- **Verified:** Not yet — awaiting the first green run on the runner (the authoring
  sandbox can't download the Android SDK/AGP).
- **Next:** FreshRSS GReader API client + login, then the three UI pillars
  (magazine grid, swipe/flip reader, topic sections).

## Environment constraints

- Gradle/Android builds are run by Rick locally (Android Studio) or in CI; the AI
  cloud sessions cannot assume access to the Android SDK or a device.
- Rick builds and releases Android apps already (maintains a Mihon fork), so the
  deliverable is a buildable project, not a pre-compiled APK.
