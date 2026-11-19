# FlipRSS v0.12.0 native artifact

AI: Codex, #40. Never merge this binary branch into main.

Ordered immutable 32MiB parts avoid GitHub single-file Git limits. The hosted
publisher checks every part plus the full APK hash, and uploads one FlipRSS.apk
asset to GitHub Releases. Download that asset for installation.

b55d2dbba7bdedd5fa7940eadcefa229fc51094e658dc9992dcaff05caec99f5  FlipRSS.apk

See build-info.json and parts.json. Existing test signer retained; no key included.
Direct install from older versions required; new updater supports512MB.
