# FlipRSS 0.4.0-speech-preview

Install on Android 8.0+. VersionCode 4, release variant, existing prototype test
signing identity. It can update previews signed with that identity.

Open an article and tap Read aloud, Pause reading, Resume reading or Stop reading.
Reads the title and available article body, split into engine-sized chunks.
Pauses on background/audio focus loss; stops on article change or reader exit.
Resume uses word-position callbacks where supported, otherwise restarts the chunk.
Uses Android device language and configured speech engine. Installed voice preferred;
some engines/voices need network. Voice settings and Retry voice handle setup errors.
No background playback, automatic next article, or microphone permission.

Includes FreshRSS and reading controls from PRs #7 and #9. Source
4b0dd2852137d5a312121e1de9885175d29da4b8, codex/10-article-speech, stacked on
codex/8-reading-controls. Merge base PRs first and retarget follow-ups to main.
Never merge this binary download branch. No new published release.

AI: ChatGPT Codex. Issue #10. All 31 unit tests pass per debug/release variant;
build/lint, instrumentation compilation, signature and alignment checks pass.
Actual device/audio/real-account tests not run; Rick should verify speech playback,
pause/resume, navigation, backgrounding and interruptions on his phone. Production
signing is not configured. See build-info.json and SHA256SUMS.txt.
