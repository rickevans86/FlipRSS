# HANDOFF

Agent: ChatGPT Codex. Tasks #24 Search bug and #25 natural speech request.
Completed: Search fix PR #26 merged at 2b9525c; speech PR #27 merged at 155c680.
Main app equals tested b1fb05fda496fb8de60b0cc861706483f4b1cbd4, tree
ea433b702f8c382bff0929926f00de6f780a532f. Continuing main/build/publish workflow
for shipped correction and added request; no independent self-review claimed.

Search: correct Feedspot row/heading ID matching/local fallback, skip unmatched
links, discard obsolete directory cache without touching subscriptions/Saved/
account. Explicit 20-result pages/reset on filter/data. Regression on real HTML:
453 URLs/449 names, one NBC News; WordPress is The WordPress Blog. No need to
clear app data. Multi-sibling/cache/paging regression coverage included.

Speech: installed Android engine/speaker selection and engine-rated best quality
for phone language/region, preview, rate/pitch, persistence; Settings → Speech and
voices or Voice in article. Online voice opt-in defaults off and explains chosen
engine may send text over internet. Offline avoids network-required/uninstalled
voices, reports missing data/engine and supports install/refresh/system settings.
Manual selection overrides auto; missing/disallowed speaker falls back safely.
Playback tuning avoids engine churn; pause/stop/lifecycle/focus/callback cleanup
retained. Optional cloud provider question unanswered; announced Android path
requires no service key/account. No claim of audible naturalness; user previews.

Publication: regular/latest v0.9.0 (code 10), Android 8+, release/non-debuggable.
https://github.com/rickevans86/FlipRSS/releases/tag/v0.9.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.9.0/FlipRSS.apk
13,299,931 bytes; SHA-256:
823aba453b4923c0dbbb906349e36de510739902fa41c553dc3d462dd683fda4.
Same existing test signer as prior versions, production signing unconfigured;
no key published. Artifact a166595bcf925c277f83892b22f794903c1070ad,
codex/25-release-download, release-download/FlipRSS.apk (never merge binary branch).
Signature/alignment/package/version/SDK/non-debuggable/signer equality checked.
Hosted action 44300897612 SUCCESS. All nine releases have native APK/checksum/
build metadata, all APK digests match manifest; latest native APK downloaded and
SHA checked. 0.8.1/code 9 intermediate was never separately published.

Tests: 73 JVM tests pass in EACH debug/release variant, both builds/lint pass
(0 errors, 1 existing KTX warning), instrumentation compiled, not executed.
Five publisher tests/actionlint pass, actual publication successful. New speech
checks cover quality/locale/manual/fallback/network opt-in/persistence/bounds.
Compiled device tests cover settings persistence/dialog and search paging.
Actual phone/audio/naturalness/installer/account-write checks NOT RUN. Remote
Android success not claimed. Main/source tree and all native digests verified.

Files: importer/cache/search/paging/controller/profile/settings/reader speech UI/
tests/version, README/release manifest/six records. Branch codex/25-natural-speech;
#27 merged, final records-only follow-up authorized, no app changes. #24/#25 closed.
No active implementation/publication blocker. User should update and verify
Search/voice preview on phone, choose voice provider/online option if desired.
Prior source/cache/discovery/TTS/test-signing limitations remain.
Shared records updated Yes; append-only history retained, live board rebuilt,
claims cleared. Remote source/artifact/main/merge/release/action/native bytes
confirmed.
