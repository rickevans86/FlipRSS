# FlipRSS 0.7.0 review build

FlipRSS.apk, versionCode 7, Android 8+, signed release/non-debuggable. Existing
test identity supports updating v0.6; production signing remains unconfigured.

Settings → Appearance → OLED uses pure-black neutral surfaces/system bars with
no elevation tint. Search browses 15 curated popular RSS/Atom feeds, filters names/
topics and opens Add feed with URL prefilled for the active local/FreshRSS source.
Settings → Show feed search hides the tab and returns to Today if currently open.
No queries sent to external directory; not a whole-web search.

AI: ChatGPT Codex, #19. Source 6f0ba16692077e2e93d0a218d8524b8128d6d524,
codex/19-oled-feed-search, awaiting Rick-selected independent review/main merge.
54 unit tests pass per debug/release variant; both build/lint pass (0 errors,
1 existing warning each); instrumentation compiled, not run. All 15 included feed
URLs returned HTTP 200/XML in environment checks. Five publisher tests/actionlint
pass. Actual phone/system-bar/UI/real-account verification not run.

Never merge this binary branch. See build-info and checksum for provenance.
