# FlipRSS 0.8.0

FlipRSS.apk, versionCode 8, Android 8+, signed release/non-debuggable. Existing
test identity supports updating prior builds. Production signing unconfigured.

FreshRSS and FlipRSS tabs keep server and local libraries independently active.
Saved combines read/unread bookmarks; Topics removed, category filters retained.
+ only appears on FreshRSS and subscribes on that server. Search adds only local
feeds. Local Manage feeds supports URL view/open/copy and confirmed subscription
to the current FreshRSS account without moving/deleting local data.

Mark as read on scroll is optional/default off and applies only after observed
cards pass the top of source pages. Search refreshes supplied Feeder/Feedspot
lists daily/on demand with deduplication/offline cache and partial-failure handling.
Website/feed URL discovery checks RSS/Atom, alternate links, RSS links/icons and
common paths; some sites have no feed. Existing OLED/Search-disable remain.

AI: ChatGPT Codex, #21. Source 7dc97632646dcec2ae6c3ed29a2c41e8eb529991.
64 JVM tests passed per debug/release variant; both build/lint pass (0 errors,
1 existing warning), instrumentation compiled, 5 publisher tests/actionlint pass.
Real page parsers extracted 114 Feeder/454 Feedspot links; each imported feed is
validated when subscribed, not all targets probed. Actual phone/installer/audio/
real-account-write tests not run. See README on source branch for limits.

Never merge this binary branch. Signing keys not included.
