# PROJECT_STATE

## Current application

Offline native Kotlin/Jetpack Compose Android prototype is on main after PR #3
merged as `1195fd8` on 2026-11-18 by Codex, explicitly authorized by Rick in chat.
Magazine grid, topics, swipe reader, saved/read persistence and bundled samples.
FreshRSS authentication, networking and synchronization are not implemented.

## Build verification

Local fresh APK packaging and 3 unit tests passed; lint 0 errors/7 warnings; APK
signature verified. SDK min 26 / target 35. No new device/emulator test execution.
Earlier verified APK/checksum: `downloads/` on branch `codex/2-apk-build`.

## Actions

Rick confirmed `SelfHosted` is Windows. Build job now targets `[SelfHosted,
Windows]` and uses Windows PowerShell plus `gradlew.bat`, then uploads the
FlipRSS-debug APK artifact. The existing emulator action does not support Windows
and is skipped. actionlint passed for workflow changes; remote outcome pending.
First main push triggered run 44223942127, queued with no assigned runner at last
observation. The Windows update triggers a newer main run; consult live GitHub
Actions for its outcome rather than historical queue/billing records.
