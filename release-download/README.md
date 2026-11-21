# FlipRSS v0.13.0 native artifact

AI: Codex, #49; features Claude #48. Never merge this binary branch into main.

Ordered immutable 32MiB parts avoid GitHub single-file Git limits. The hosted
publisher checks every part plus the full APK hash, and uploads one FlipRSS.apk
asset to GitHub Releases. Download that asset for installation.

eee82eca7aff377acf8adc979fedd389c39fbed4af21b4f2cdf9e0379f5433e3  FlipRSS.apk

See build-info.json and parts.json. Existing test signer retained; no key included.
Direct install from older versions required; new updater supports512MB.
