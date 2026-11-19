# PROJECT_STATE

Agent responsible: ChatGPT Codex. Task #37 completed/closed; feature PR #38 merged at d361fdb922c60ef178d847fde3d46740a4d3fc36 under Rick's continuing main/build/publish workflow. No other AI changes/overlapping claims found since v0.10.1. No independent self-review claimed. Final records-only follow-up changes no app code.

Published regular/latest v0.11.0 (code 14), Android 8+, release/non-debuggable:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.11.0
https://github.com/rickevans86/FlipRSS/releases/download/v0.11.0/FlipRSS.apk
261,392,018 bytes (about 249.3 MiB); SHA256
07f8cbba43ce1db0b9582388741f3e268776e3521b412f897ce248376dabd21c.
Hosted publisher 44443483017 SUCCESS in 1m30s. All twelve releases have native
FlipRSS.apk/checksum/build-info assets and matching APK digests. Latest endpoint
verified; downloaded native v0.11.0 bytes, metadata and checksum match signed build.

User instructions: Open original article now uses the built-in Gecko browser.
Settings → Use built-in browser restores external opening. BPC is bundled but OFF:
Settings → Browser and add-on → Enable bundled add-on. Site controls/popup and
Extension options are available there. Optional extra permissions use Allow/Deny.
The add-on can read/change supported website pages/cookies/requests; support varies.
Reader extraction/Read aloud stay separate from browser/add-on output.

IMPORTANT upgrade: installed <=0.10.1 has an 80 MB updater cap and rejects this
261 MB APK. Download/install directly over existing app once; do NOT uninstall.
Same signing identity retained for data-compatible upgrades. Future updater cap
512 MB with longer timeout, GitHub HTTPS/size/hash guards retained. Supported
ABIs ARM64/ARMv7/x86-64; no 32-bit x86. Production signing remains unconfigured;
prototype test signer retained; signing key never published.

Implementation: BrowserActivity/Model/Engine/Prompts/Urls provide address entry,
back/forward/reload/stop, tabs, external-open, lifecycle/session cleanup, standard
alert/confirm/text/choice dialogs. Browser profile and add-on preference store are
separate from FreshRSS API/account settings. HTTP(S) and own add-on origin only,
no certificate-error override. Files/downloads/media/HTTP-auth prompt integration
remains limited; not a complete Firefox replacement. Native rendering, popups,
permission UI and site-bypass behavior need actual device validation.

Components: current GeckoView 160.0.20261116130455 MPL 2.0, Mozilla source
b1145b0710192f716be5f59f5b2dfeed650d5294. Firefox BPC 4.4.9.6, MIT, from requested
GitFlic uploads commit50f74c1e667eff99e04a22f28158634509234e8d; original XPI SHA256
0d0b9c627eb5085c16a263f61415c5706428ce7ba259566d0d50c4ac7e096dac matches upstream
release-hashes. Byte-identical extracted assets, original XPI, provenance/notices/
licences and bundled DOMPurify 3.4.18 MPL source offer included. No add-on binary
auto-updater; upstream optional rule/version options remain unchanged. Build
requires AGP 9.5.1 / Gradle 9.6 / built-in Kotlin+Compose 2.4.21 / JDK17 /
compileSDK37.2 / build-tools36. min26/target35 retained. AGENTS project details
updated, collaboration protocol unchanged. Existing reader/source/Saved/speech/
OLED/search/bulk-read features remain; see README for prior cache/renderer limits.

Source0289c2ccc82068025d7d7b2a9e9b3406e8d6fcd7, app tree
366dd1dd3a65fd4a7e90244d97b3ce25008e997a. Main app equals tested source. Code
matches tested695cd96; final source adds sanitizer notice, repackaged and asset
bytes checked. AGP worktree VCS metadata unavailable; exact source in external
build-info. Immutable artifactda7515a5a2ad38cf979bae6fd2fb04d1ab320464 /
codex/37-release-download / release-download: eight ordered32MiB parts. Never
merge binary branch. Publisher enforces names/count/order/part size+hash and full
APK hash, uploading one standard native FlipRSS.apk asset. Nine publisher tests
and actual large reconstruction/upload succeed; old manifests remain supported.

Tests: 90 JVM tests EACH debug/release, final release build/lint pass (0 errors,
17 target/dependency/version/KTX warnings), earlier debug lint0errors18warnings.
Instrumentation compiled only; nine publisher tests/actionlint pass. Original
XPI/hash/all assets and39 Mozilla native libraries match upstream bytes.
Signature/alignment/package/version/SDK/non-debuggable/three ABIs/prior signer
verified. Physical browser/rendering/popup/permission/site-bypass/audio/installer/
real-account-write tests and independent review NOT RUN (no device/emulator).
Workspace capacity managed by removing only completed generated outputs and old
unsigned intermediates; source, keys and published artifacts preserved.

In progress: none after records merge. Branch codex/37-browser; #38 merged,
records-only follow-up. Blockers none. Next action: Rick directly install APK,
enable add-on and verify original-story browsing/site controls on phone. Shared
records updated Yes, append-only history preserved, live board rebuilt, claims
clear. Remote main/source/artifact/PR/action/release/native bytes Confirmed.
