# PROJECT_STATE

## Current main / regular release

ChatGPT Codex corrected shipped Search (#24/#26) and added requested natural-voice
controls (#25/#27) under continuing main/build/publish workflow. Main application
merge 155c680 contains source b1fb05fda496fb8de60b0cc861706483f4b1cbd4, identical
app tree ea433b702f8c382bff0929926f00de6f780a532f. Final records have no app changes.

Latest regular v0.9.0, code 10, Android 8+, signed release/non-debuggable:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.9.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.9.0/FlipRSS.apk
13,299,931 bytes; SHA-256:
`823aba453b4923c0dbbb906349e36de510739902fa41c553dc3d462dd683fda4`.
Existing test signer retained for updates, production signing unconfigured.
Artifact a166595bcf925c277f83892b22f794903c1070ad / codex/25-release-download
remains immutable; never merge binary branch or publish signing key.

## Corrections / speech behavior

Feedspot row ID matches its heading, only unambiguous local fallback, no shared
ancestor title reuse/global fallback. Old directory cache schema resets to 2 and
refetches immediately; subscriptions, account and Saved untouched. Catalogue shows
20 per explicit Load more and resets on query/topic/dataset. Actual page check:
453 feed URLs, 449 names, one NBC News, WordPress correctly named.

Speech and voices in Settings and Voice in reader provide installed engine/voice
choice, engine-rated best available quality for device language/region, preview,
persistent rate/pitch. Online voices default off/explicit consent; user-selected
engine may send text online. Offline selection excludes network-required and
uninstalled voices. Missing voice/engine data has install/settings/refresh.
Tuning does not repeatedly recreate engine, playback cleanup/focus/lifecycle
retained. Announced Android path used, no new account/API key, audible quality
depends on device/provider. Cloud preference was optional and unanswered.

## Verification

73 JVM tests per debug/release variant passed; both build/lint pass (0 errors,
1 existing warning), instrumentation compiles only. Five publisher tests/actionlint
pass. Hosted publisher 44300897612 SUCCESS; all nine releases have native APK/
checksum/build-info, all APK digests matched and latest native bytes downloaded/
SHA verified. Latest endpoint confirms regular v0.9.0. No physical phone/audio/
naturalness/account-write tests or independent review claimed; remote Android
CI success not claimed. #24/#25 closed, no implementation/publication blocker.
Prior source tabs/Saved/OLED/discovery/scroll features preserved; existing cache/
renderer/TTS/signing limits remain in README.
