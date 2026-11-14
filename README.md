# FlipRSS

A native Android reading app for FreshRSS, with a magazine-style grid, topic
sections and a full-screen swipe reader.

## Try the prototype

Open this folder in Android Studio, let Gradle sync, then run **app** on an
Android 8.0+ device or emulator. No account or server is required.

Or, with JDK 17+ and the Android SDK installed:

```sh
./gradlew assembleDebug
./gradlew installDebug
```

If needed, set `ANDROID_HOME` or put `sdk.dir=/path/to/android-sdk` in your local
`local.properties` (ignored by Git). The SDK needs platform 35 and build tools
35.0.0. The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.
GitHub Actions also builds and uploads a **FlipRSS-debug** APK artifact on PRs.

### What you can do

- **Today:** browse an image-led reading room; filter by Nature, Design,
  Technology or Culture; tap a photo or headline to read.
- **Topics:** open a subject to see its stories.
- **Reader:** swipe horizontally or use the previous/next buttons; scroll
  vertically within longer stories; use Android Back or the back arrow to return.
- **Saved:** bookmark stories from a card or the reader and find them here.
- Articles become **Read** when their reader page settles. Bookmarks and read
  state survive app restarts; they are stored locally and can be cleared using
  Android Settings → Apps → FlipRSS → Storage → Clear storage.
- Supports system light/dark theme, system insets, screen rotation, and labelled
  controls for screen readers. Reader queues stay stable while bookmarking.

## Prototype boundaries

All six stories are original fictional demo content. Images are bundled locally
(see [photo credits](PHOTO_CREDITS.md)); the app works without internet access
and requests no network permission. No FreshRSS connection, authentication,
synchronisation, real feeds, or backend read/star actions are implemented yet.
The pager uses a horizontal slide, not a 3D page-curl animation.

The demo stores only story IDs in SharedPreferences. This is not a credential
store. Add secure authentication and the GReader data layer in separate Issues
before connecting a real account. Networking, Room, Coil and DI are deliberately
left out of this focused prototype.

## Development

Kotlin 2.1.20 / Compose Material 3 / AGP 8.9.1 / Gradle 8.11.1.
SDK 26 minimum, SDK 35 compile/target. JDK 17 is used in CI.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
# Requires a running emulator or connected device:
./gradlew connectedDebugAndroidTest
```

JVM tests exercise combined topic/bookmark filtering, stale bookmarks and content
identity. The instrumentation tests cover opening a story, turning the page, returning
to the library, bookmarking and selecting topics. A separate CI job runs them on
an API 30 emulator. Local device testing needs an emulator or connected device.

`DemoLibrary.kt` holds sample data and pure selection logic;
`LibraryViewModel.kt` owns persistent demo reading state;
`MainActivity.kt` contains the Compose UI and reader navigation.

AI: **ChatGPT Codex** — prototype implementation, Issue #2.
Read [AGENTS.md](AGENTS.md) and `.ai/` before contributing. Rick merges into main
and names the independent reviewer.
