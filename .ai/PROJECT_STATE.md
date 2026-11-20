# PROJECT_STATE

Last reconciled: 2026-11-20 by ChatGPT Codex, task #46 (Rick: “Update ai files”).
Verified remote main before this records-only change: cb08049268e1b9e87b6b26a661226076eca944ce, final #43 release-records PR #45. Feature PR #44 merged at 2e3606ef586d65f7916758b0598f955dd22a2f1e. #43 is closed. No later changes from another AI or overlapping claims were found. Only #46 was open during this documentation refresh; this records PR closes it on merge. No application work is in progress.

Latest regular release: v0.12.1, version code 16.
https://github.com/rickevans86/FlipRSS/releases/tag/v0.12.1
https://github.com/rickevans86/FlipRSS/releases/download/v0.12.1/FlipRSS.apk
Native assets: FlipRSS.apk, SHA256SUMS.txt and build-info.json.
APK: 261,408,558 bytes; SHA256 df1c963eda36fc0bfb022ce4f1cd761caa5a58607832b9379eae32fc3d05d5a5.
Source: 78756ccea778b26b909319bd6f1264419f865131.
App tree: 94fbed4c7d68aff17cfdb04a2c9215b249768312, confirmed equal to current main.
Artifact: 28186f022e1d76014abf449d86616de42871ef14, codex/43-release-download, eight ordered 32 MiB parts. Never merge this binary branch.
Publisher 44737339847 completed successfully. The release publication task downloaded the native APK/metadata/checksum and verified all 14 native release digests. This documentation refresh rechecked the latest release assets/digest, publisher conclusion and app tree; it did not repeat the large download or build.

Completed behavior: Settings → Bottom navigation → Show FreshRSS and Show FlipRSS are independent and default on. Preferences persist across restarts. Hiding a tab keeps its account, feeds and combined Saved stories. Search remains separately configurable. If the selected tab is hidden, select FlipRSS, FreshRSS, Search or Saved in availability order and clear the old category. Saved stays visible even if every optional tab is off. These are visibility settings; they do not sign out or suspend source restoration.

Existing reader: public/static article loading plus rendered Gecko text using the existing BPC enable state; Open original → Read in app imports matching visible paragraphs for the reader/Read aloud. BPC remains opt-in. Owned reader bridge is version 1.0.0; bump its manifest version for future bridge changes. Browser/add-on assets were unchanged by #43. Prior feed discovery, independent local/FreshRSS sources, combined Saved, OLED, speech and bulk-read features remain.

Release validation already completed for #43: 109 JVM tests in each debug/release variant; release build/lint (0 errors, 12 warnings); instrumentation compiled only; 9 publisher tests and actionlint with the custom SelfHosted label configuration. Assets/39 Mozilla native libraries, signing/alignment/version/package/SDK/ABI/prior signer verified. These tests were not rerun for this documentation-only task. #46 checks are live GitHub reconciliation, .ai-only diff, unchanged app tree, append-only history and git diff --check.

Outstanding validation: actual phone preference/restart/navigation/installation tests, The Age/Gecko/BPC behavior and audio remain unexecuted; no independent review is claimed. Windows SelfHosted build jobs remain queued, including run 44737966312 at main cb08049. Runner-list access previously returned HTTP 403; runner health/labels have not been established. No runner repair is authorized by this records request.

Upgrade/provenance: same prototype test signing identity retained; production signing is unconfigured. Android 8+, min26/target35, ARM64/ARMv7/x86-64. v0.11+ updater supports the APK size; <=0.10.1 requires direct installation over the existing app because of its old 80 MB limit. Keys must not be committed. Build toolchain remains JDK17, AGP9.5.1/Gradle9.6, built-in Kotlin/Compose2.4.21, SDK37.2/build-tools36.
