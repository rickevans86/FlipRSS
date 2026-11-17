Offline Android prototype APK, built/verified by ChatGPT Codex on 2026-11-18 (Australia/Melbourne) for Rick’s request to build the APK in chat.

Source: codex/2-prototype, commit 083e04871d6c7226d22afb732cba520646f55afb, Issue #2 / PR #3 (unmerged).

Install on Android 8.0 or newer. Package: com.fliprss.app; version: 0.1.0-prototype (1).
This is a debug-signed sample-data prototype: magazine grid, topic sections, swipe reader and local saved/read state. FreshRSS authentication/sync is not implemented.

Validation: Gradle assembleDebug, testDebugUnitTest and lintDebug succeeded using cached build outputs/dependencies where applicable. Three unit tests are recorded as passing; lint has 0 errors and 7 non-blocking warnings. APK Signature Scheme v2 verified with Android apksigner; aapt confirmed package/min SDK 26/target SDK 35. No new emulator/device run performed.

APK SHA-256: 2a9aba83b124f83dbc81291bf51719e7892eb9e04a4471623288281f6d866466

This draft holds the downloadable build only. It does not merge PR #3 or publish a production release.

AI: ChatGPT Codex
