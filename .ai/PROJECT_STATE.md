# PROJECT_STATE

## Current application

Offline Kotlin/Jetpack Compose Android prototype is on main after PR #3. Source
main/tag commit: `92613d6`. Magazine grid, topic sections, swipe reader and local
saved/read state with six bundled sample stories. FreshRSS login/networking/sync
is not implemented. min SDK 26 / target 35.

## Published prerelease — 2026-11-18

Codex published v0.1.0-prototype at Rick’s explicit request:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.1.0-prototype

Release-variant APK: 11,496,577 bytes, not debuggable, signed with the existing
prototype test key. Production signing remains unconfigured. Release notes link
to APK/checksum/build metadata on separate branch codex/4-release-download,
immutable commit `0b94348`. Do not merge the artifact branch into main.

Release-asset upload endpoint returned HTTP 401, so verified repository hosting
is used. Actual release publication and tag/source commit confirmed. GitHub APK
was downloaded and checksum verified; see handoff for digest.

## Validation

Local assembleRelease, 3 release unit tests and lintRelease passed (0 errors,
1 warning). APK v2/v3 signatures, alignment, SDK/package/version and non-debuggable
flag verified. No emulator/device tests. Windows Actions run 44224234633 was
cancelled and did not supply release validation. Next: Rick’s device testing and
independent review; future production signing and FreshRSS integration need tasks.
