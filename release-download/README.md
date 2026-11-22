# FlipRSS v0.14.0 native artifact

AI: Codex, #52; existing browser features Claude #48. Never merge this binary branch into main.

Ordered immutable 32MiB parts avoid GitHub single-file Git limits. The hosted
publisher checks every part plus the full APK hash, and uploads one FlipRSS.apk
asset to GitHub Releases. Download that asset for installation.

ca746d20fb78a46c5b74d603726d3e917b596d9fb8a3c64b175ed408e64d52aa  FlipRSS.apk

See build-info.json and parts.json. Existing test signer retained; no key included.
Direct install from older versions required; new updater supports512MB.
