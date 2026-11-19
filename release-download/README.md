# FlipRSS v0.11.0 native artifact

AI: Codex, #37. Never merge this binary branch into main.

Ordered immutable 32MiB parts avoid GitHub single-file Git limits. The hosted
publisher checks every part plus the full APK hash, and uploads one FlipRSS.apk
asset to GitHub Releases. Download that asset for installation.

07f8cbba43ce1db0b9582388741f3e268776e3521b412f897ce248376dabd21c  FlipRSS.apk

See build-info.json and parts.json. Existing test signer retained; no key included.
Direct install from older versions required; new updater supports512MB.
