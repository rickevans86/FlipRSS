# FlipRSS v0.12.1 native artifact

AI: Codex, #43. Never merge this binary branch into main.

Ordered immutable 32MiB parts avoid GitHub single-file Git limits. The hosted
publisher checks every part plus the full APK hash, and uploads one FlipRSS.apk
asset to GitHub Releases. Download that asset for installation.

df1c963eda36fc0bfb022ce4f1cd761caa5a58607832b9379eae32fc3d05d5a5  FlipRSS.apk

See build-info.json and parts.json. Existing test signer retained; no key included.
Direct install from older versions required; new updater supports512MB.
