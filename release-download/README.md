# FlipRSS 0.6.0

FlipRSS.apk, Android 8.0+, versionCode 6, non-debuggable release variant. Existing
test signing identity matches v0.5 and preserves upgrade compatibility. Production
signing is not configured; no key is published.

Saved always includes read bookmarks and has no Hide read toggle. Removed the
large perspective heading/subtitle and account banner from library pages; account
controls remain in Settings. Settings → App updates checks regular GitHub Releases,
downloads FlipRSS.apk, verifies size/SHA-256/package/version/signature, and opens
Android's installer for confirmation. Android may ask to allow updates from FlipRSS.
Incomplete/untrusted releases are rejected. No silent installs/background checks.
Includes prior local RSS/FreshRSS/reading controls/speech features.

Source de926519c5f44c3c8f3f19553bcab78ffafb98fd, codex/14-release-updates, stacked
on PR #13 / #11 / #9 / #7. Rick alone merges; never merge this binary branch.
AI: ChatGPT Codex, #14. 49 JVM tests pass per debug/release variant, both builds/lint
pass (0 errors, 1 existing warning each), instrumentation compiled. Five publisher
tests and actionlint pass. APK signature/alignment/package and v0.5 signer equality
verified. Actual phone/installer/audio/real-account tests not run. See checksum and
build metadata. Regular GitHub Release requested by Rick; asset upload via runner.
