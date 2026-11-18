# PROJECT_STATE

FlipRSS is a Kotlin/Jetpack Compose Android reading app for FreshRSS. The current
prototype is offline: magazine grid, topic sections, horizontal swipe reader,
saved/read persistence, six sample stories and bundled photos. FreshRSS login,
networking and synchronization are not implemented.

## 2026-11-18 — Codex build and authorized publication

Rick explicitly asked Codex to push the prototype to main and run Actions.
PR #3 contains the app; publication is being performed after fresh local checks.
GitHub PR/main history and Actions are authoritative for the final outcome.
Claude’s intervening scaffold and revert on main were fetched and preserved;
main’s net file tree before this publication matched the original bootstrap.

Local verification: debug APK freshly packaged, 3 unit tests rerun and passing,
lint passing (0 errors/7 warnings), APK signature verified. No device tests run.
SDK 26 minimum / 35 target; JDK 17+, Kotlin 2.1.20, AGP 8.9.1, Gradle 8.11.1.

The Actions workflow triggers on main pushes and uses runner label `SelfHosted`.
Remote builds need an online matching runner. Emulator job runs after build only
on x64 Linux/macOS. Do not treat historical billing/queued statuses as current.

An earlier verified APK and checksum are in `downloads/` on `codex/2-apk-build`.
Prototype source remains separate from that binary download branch.
