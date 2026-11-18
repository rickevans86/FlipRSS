# HANDOFF

Agent: ChatGPT Codex. Tasks #24 Search regression and #25 natural speech request.
#26 merged search correction (2b9525c); existing main/build/publish workflow for
shipped update continued. Speech source b1fb05fda496fb8de60b0cc861706483f4b1cbd4
on codex/25-natural-speech. New PR to open after records, no independent review
claimed. No other AI overlap found.

Completed: Feedspot row/heading IDs, unambiguous row-local fallback/no ancestor
search, cache schema-2 invalidation/refetch preserving subscriptions and Saved,
20-result explicit Load more/reset on criteria/dataset. Actual page gives 453
URLs/449 distinct names, one NBC News, WordPress labelled The WordPress Blog.

Speech: installed Android engine/voice selection, engine-rated best available
quality for device language/region; rate/pitch persistence, preview from Settings
or Voice in reader. Online voices explicit opt-in/default off; network-required
voices cannot be selected in offline mode, uninstalled voices excluded. Missing
voice/engine errors allow installation/refresh/settings selection. Manual choice
overrides automatic; missing/disallowed voice falls back safely. Engine/voice
changes close old controller, tuning updates avoid engine churn; preview/reader
playback lifecycle, stop/pause/focus/generation cleanup retained. Article text may
be sent online only when user opts into engine network voices. No cloud API key
or new account; optional provider preference unanswered, Android path announced.
Audible naturalness depends on provider/device, not verified here.

Files: SpeechOptions, ArticleSpeech, SpeechControls/SpeechSettingsDialog, reader/
settings wiring, AndroidSettingsStore/AppSettings, tests/version/README, release
manifest and all six records.
Tests: 73 JVM tests in EACH debug/release pass; both build/lint pass (0 errors,
1 existing KTX warning), instrumentation APK compiles, not run. Five publisher
tests/actionlint pass. New tests cover network opt-in, engine-rated quality/locale
selection/manual choice/fallback and persisted tuning. Physical phone/audio/
naturalness/account-write tests NOT RUN. No remote Android CI success claimed.

Prepared regular v0.9.0/code 10, Android 8+, release/non-debuggable. APK
13,299,931 bytes, SHA-256:
823aba453b4923c0dbbb906349e36de510739902fa41c553dc3d462dd683fda4.
Artifact a166595bcf925c277f83892b22f794903c1070ad / codex/25-release-download,
release-download/FlipRSS.apk. Never merge binary branch. Signature v2/v3, alignment,
version/package/SDK/non-debuggable and signer equality with prior release checked.
Same existing test signer, production signing unconfigured, no key published.
Native publication pending after authorized merge and hosted dispatch.

Next: merge speech PR, publish combined v0.9.0 from main, verify all native assets/
latest downloaded hash and main app equality, then close #24/#25/update records.
No build blocker; no independent self-review. Rick listens to Preview voice on
phone, chooses installed engine/speaker or enables online voices, and validates
Search cache/paging. Cloud service path would need explicit provider/key setup.
Shared records updated Yes; append-only history retained, main fetched/ancestry
checked, source/artifact pushed.
