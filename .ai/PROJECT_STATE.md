# PROJECT_STATE

## Main

Rick explicitly authorized merging all feature PRs on 2026-11-18. Codex merged
#7/#9/#11/#13/#15 sequentially, retargeting each to main and preserving history.
Main feature merge 41548fc contains FreshRSS, local RSS/Atom, subscriptions, reading
settings, categories, TTS, GitHub updater and Saved/page fixes. Its app/workflow/
README tree matched the tested final feature branch before the publisher change.
Release app source de926519 has the same app tree; no APK rebuild needed.

Regular/latest release v0.6.0, FlipRSS.apk, versionCode 6:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.6.0
SHA-256 ddf8033b0f01af9512887b4c8ce38ac06738afc54c705cee57b1a0de4600acf4.
Existing test signer retained for upgrades; production signing unconfigured.

## Publication task #16

Direct release asset upload still HTTP 401. SelfHosted publisher run 44252735121
queued. Codex moves upload-only job to GitHub-hosted Ubuntu (public repo); build
jobs remain Windows SelfHosted. Publisher retrieves six immutable signed APKs and
checksums before native uploads; no signing key on hosted runner. Need merge
publisher follow-up, cancel superseded queued publication, dispatch main and
verify native assets. No successful asset attachment claimed yet.

## Validation / limits

49 JVM tests passed in each debug/release variant, both builds/lint passed
(0 errors, 1 existing warning), instrumentation compiled. Merged app tree equality
checked. Five publisher tests/actionlint pass for hosted-runner follow-up. Actual
phone/installer/audio/live-account tests not run. Existing local cache/FreshRSS
in-memory/TTS/test-signing limits remain in README. Binary download branches never
merge into main. Independent review not claimed; Rick explicitly requested merge.
