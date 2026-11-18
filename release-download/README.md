# FlipRSS 0.9.0

FlipRSS.apk, code 10, Android 8+, signed release/non-debuggable. Same existing
test signing identity as earlier versions, no production signing/key published.

Fixes repeated NBC News names via proper Feedspot entry/heading association and
invalidates only old directory cache. No need to clear app data; subscriptions/
Saved/account preserved. Search results show 20 at a time with Load more.

New Speech and voices from Settings or Voice in reader: installed engine/speaker
selection, engine-rated best quality, preview, persisted speed/pitch. Online voices
require explicit opt-in; selected engine may send text over internet. Offline mode
excludes network-required/uninstalled voices. Install voice packs via Android
settings and Refresh voices. Actual sound depends on installed engine. No API key
needed for this Android path; audible/device validation not run here.

AI: ChatGPT Codex, #24/#25. Source b1fb05fda496fb8de60b0cc861706483f4b1cbd4.
73 JVM tests pass per debug/release, both builds/lint pass (0 errors, 1 existing
warning), instrumentation compiles. Five publisher tests/actionlint pass. Real
Feedspot parser check: 453 URLs/449 names, one NBC News, WordPress correctly named.
No actual phone/audio/account tests. Never merge this binary branch.
