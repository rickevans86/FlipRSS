# PROJECT_STATE

## Main and release

Rick explicitly authorized all merges and native APK publication on 2026-11-18.
ChatGPT Codex merged #7/#9/#11/#13/#15 sequentially, then publisher follow-up #17.
Main application includes FreshRSS, local RSS/Atom and offline text/read/saved
persistence, subscriptions/settings/categories, article TTS, Saved/read fixes and
GitHub updater. Main publisher code at 12b2587 uses GitHub-hosted Ubuntu; Android
build CI remains Windows SelfHosted. Final shared-record follow-up contains no
app code changes. No independent self-review claimed.

Latest regular release v0.6.0, versionCode 6, Android 8+:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.6.0/FlipRSS.apk
13,234,395 bytes; SHA-256:
`ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4`.
Existing test signing identity retained for upgrade compatibility; production
signing remains unconfigured, no signing key published.

## Native asset publishing verified

GitHub-hosted action 44254047153 completed SUCCESS. All six releases v0.1–v0.6
have native FlipRSS.apk, SHA256SUMS.txt and build-info.json assets. Every APK asset
digest matches .release/previews.json. Downloaded v0.6.0 native release APK bytes
and verified SHA-256. In-app updater's stable-release metadata and APK endpoints
are now available. Direct session upload still HTTP 401; hosted GITHUB_TOKEN
resolved publication without using SelfHosted hardware or exposing signing keys.
Superseded queued publication jobs cancelled.

## Source and validation

Release app source de926519c5f44c3c8f3f19553bcab78ffafb98fd is an ancestor of main.
App tree 9c47d258bfb8cffd02ad0e8d83f2befe848a847d is identical on main and tested
source. Existing immutable artifact 3cd57a6 remains separate; binary branches
are never merged. 49 JVM tests passed in each debug/release variant, both builds/
lint passed (0 errors, 1 existing warning), instrumentation compiled. Five
publisher tests/actionlint passed, hosted publication action succeeded. Actual
phone/installer/audio/live-account execution not run; Windows Android CI still
queued and no remote Android test success claimed. Existing caching/TTS/signing
limits remain documented in README. Issues #4/#6/#8/#10/#12/#14/#16 closed after
Rick-authorized merges and confirmed native publication.

## OLED and feed search — #19 / PR #20 (review branch, not merged)

AI: ChatGPT Codex. codex/19-oled-feed-search from main e090d0a; app source
6f0ba16692077e2e93d0a218d8524b8128d6d524. Adds persistent OLED palette with pure
black background/neutral surfaces/system bars and no elevation tint/navigation
contrast scrim. Light/Dark/System remain. Settings chips wrap for small screens.
Search tab has 15 curated well-known RSS/Atom feeds, local word/topic filtering
and active-source add confirmation with prefilled URL. Demo/expired-account source
selection preserves candidate. Show feed search defaults on; disabling hides tab
and returns from Search to Today, without changing subscriptions.

54 JVM tests pass per debug/release variant; both builds/lint pass (0 errors,
1 existing warning), instrumentation compiles. All 15 included feed URLs returned
HTTP 200 with RSS/Atom roots. Five publisher tests/actionlint pass. No actual
phone/OLED-system-bar/UI/real-account tests.

Review prerelease v0.7.0-preview, APK version 0.7.0 / versionCode 7, existing test
signer matches v0.6. Source awaits independent reviewer/main merge; main stable
code remains v0.6. No new merge authorization assumed from completed #16. Hosted
publication 44256865471 succeeded; FlipRSS.apk, checksum/build-info attached.
Downloaded native APK verified: 13,250,779 bytes; SHA-256
a3b428e3958b52fc6ad0a7a06539dddc8798c89a2470fa0f7a19cdbb8cbf5f18.
https://github.com/rickevans86/FlipRSS/releases/tag/v0.7.0-preview
Artifact 68fc7e2 / codex/19-discovery-download (never merge).
