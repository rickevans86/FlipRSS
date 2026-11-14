# PROJECT_STATE

## Product

FlipRSS is a native Kotlin/Jetpack Compose Android client planned for the FreshRSS
GReader API. Equal UX pillars: magazine grid, swipe reader and topic sections.

## Current state

- Claude’s coordination bootstrap is merged in PR #1 (`b63f43b`).
- Codex’s Issue #2 / [PR #3](https://github.com/rickevans86/FlipRSS/pull/3)
  (open, unmerged), branch `codex/2-prototype`, adds an offline UI prototype:
  image-led Today grid, topic filtering/browsing, horizontal article pager,
  vertical article scrolling, persistent local bookmarks/read flags and Saved view.
- Six original fictional sample stories and bundled Unsplash photos. No server,
  credentials or internet permission; no live FreshRSS integration yet.
- Gradle wrapper, Android project, unit/instrumentation tests and build/emulator CI.
- Toolchain: Kotlin 2.1.20, Compose BOM 2025.03.01, AGP 8.9.1, Gradle 8.11.1,
  JDK 17+; min SDK 26, compile/target SDK 35. Versions are pinned for this prototype.

## Validation and limitations

Local debug APK, test APK, unit tests (3) and lint build successfully. Device UI
execution is pending CI or Rick’s device: cloud emulator startup was blocked by
workspace disk limits; KVM is unavailable. No manual visual/device check claimed.
GitHub Actions run 43603822915 could not start its jobs because GitHub reports
failed account payments or spending-limit restrictions. CI device validation and
APK artifact creation are blocked; Rick can build/run locally.
Lint has non-blocking dependency update / backup configuration warnings; see PR.
The reader slides horizontally; a 3D flip animation is not implemented.

## Next

Rick reviews the prototype and names an independent AI reviewer. Rick alone merges.
Proposed future work (not yet assigned): FreshRSS GReader client + secure auth,
live data/cache, real article rendering and UX refinement after device feedback.

## CI retry — 2026-11-15 (Codex)

Rick added runner label `SelfHosted`; workflow routed to it for #2 / PR #3.
New run pending. Emulator action is limited to x64 Linux/macOS hosts.
