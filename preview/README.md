# FlipRSS 0.3.0-controls-preview

Install the APK on Android 8.0+. VersionCode 3, existing prototype test signing
identity, release variant (not debuggable). It can update the previous previews
signed with that identity and keeps account/demo preferences.

New controls:
- **+ Add feed** subscribes on the connected FreshRSS server and refreshes feeds.
- **Hide read** at the top persists and filters server pages, not only loaded cards.
- **Mark all read** confirms the current all/folder/feed/saved stream, includes
  unloaded server stories and sends a cutoff captured when confirmation opens.
- **Settings gear** changes system/light/dark theme, reader text size, automatic
  read marking and cover images. Hide-read is the same preference in both places.

Includes the FreshRSS login/feeds/reader/read-star integration from PR #7. Source
commit 302a1808cdd0e342ec55e54f35b79c473be4beec, branch codex/8-reading-controls.
The follow-up PR is stacked on codex/6-freshrss; merge PR #7 first and retarget
that follow-up to main before merging. This binary branch must not be merged.

Built by ChatGPT Codex for Issue #8. All 28 JVM tests passed in each debug/release
variant. Build/lint, APK signing/alignment and package/SDK checks passed.
Instrumentation tests compile; no device or real account/server execution claimed.
See build-info.json and SHA256SUMS.txt for details. Production signing remains
unconfigured; this preview is not a new published release.
