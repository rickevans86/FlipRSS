# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #10 — Rick requested a play button to read the open article aloud.
**Completed:** Foreground Android TextToSpeech play/pause/resume/stop controls
above the article. Reads title and available body (summary fallback), splits long
text within engine input limit without cutting surrogate pairs. Defaults to device
language, prefers installed voice. Voice setup errors expose Android voice settings
and retry. Generation-guarded callbacks ignore stopped/switched utterances.
Audio focus/background pauses; switching article or leaving reader stops playback;
disposal shuts down engine. Android 11+ TTS service visibility query added.
No microphone permission, background service or automatic next article.

**Files:** ArticleSpeech.kt, SpeechControls.kt, SpeechText.kt, MainActivity.kt,
AndroidManifest.xml, app/build.gradle.kts, SpeechTextTest.kt, README.md and .ai/.
**Branch/PR:** codex/10-article-speech, source implementation
4b0dd2852137d5a312121e1de9885175d29da4b8. Speech PR to be opened after this record
update; base codex/8-reading-controls (PR #9), which depends on PR #7. Merge #7
first; retarget #9 to main before merging; then retarget speech PR to main and
sync shared records. Rick merges and selects independent reviewer.

**Preview:** 0.4.0-speech-preview (versionCode 4), Android 8.0+, release variant,
non-debuggable, existing prototype test signing identity.
https://github.com/rickevans86/FlipRSS/blob/b639c9e280123629601784373229c0bd5abab9e9/preview/FlipRSS-v0.4.0-speech-preview.apk?raw=true
13,217,955 bytes; SHA-256:
`81bf3914680dc07e028a1800da819b8ad561a4538f8f2ab9544a2740b494ee8d`.
Artifact branch codex/10-speech-download is not for merging. Signature v2/v3,
alignment, version/SDK and non-debuggable manifest checks passed.

**Tests:** 31 JVM tests pass in EACH debug/release variant. Includes new text
chunking/title/body/fallback/empty/Unicode tests and all previous integration tests.
Both builds and lint pass (0 errors, 1 existing KTX warning each). Instrumentation
APK compiles but was not executed. No actual device/audio/real-account test.
Diff check passed. No remote CI success claimed.
**In progress:** Independent review and Rick's phone validation.
**Blockers/limits:** No build blocker. Engines without range callbacks resume at
current chunk start. Voice follows device language; some engines need network.
Only content FreshRSS provides is read. Existing cache/renderer/test-signing
limitations remain. Verify play/pause/resume/stop, swiping, backgrounding and
audio interruptions on a phone; this environment has no device/emulator.
**Previous work:** All visible feature work by Codex. #6 / PR #7 and #8 / PR #9
remain open for review. #4 published offline v0.1.0 records are preserved in PR #7.
Main remains 92613d6 offline prototype; no main merge/new release performed.
**Next action:** Rick installs preview and selects independent reviewer, then
handles merge order above. Do not merge binary download branches.
**Shared records updated:** Yes; append-only history preserved.
**Remote synchronization:** origin/main and bases fetched, main ancestry checked,
source/artifact branches pushed. Download verification recorded after completion.
