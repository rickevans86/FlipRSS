# FlipRSS 0.5.0-local-preview

Android 8.0+, versionCode 5, test-signed non-debuggable release variant. Updates
earlier previews signed with the same identity. AI: ChatGPT Codex, #12.

New installations use local feeds. Add feed accepts direct RSS/Atom XML URLs and
saves subscriptions, cached articles, read state and bookmarks on this device.
Refresh downloads updates; cached articles survive offline restart. FreshRSS is
optional via Account/Settings. Existing connected accounts remain connected.
Switch to local feeds in Settings; return with Use saved FreshRSS account. Data
is separate; no automatic import/merge. Settings also hides categories/feeds with
zero unread articles; unknown counts stay visible.

Local categories use feed titles. No background sync, OPML, feed discovery, custom
folders or unsubscribe UI in this preview. Cache: 500 stories per feed plus saved
items; max download 5 MB per feed. HTTP is unencrypted. No FreshRSS credentials
are sent to feeds. Local subscription URLs (including query tokens) remain in the
app-private store. No production signing or device backup/export configured.

Includes prior FreshRSS, reading controls and article speech. Source
63f47b52e3c80b5264bef766a7f12009c779dcf2, codex/12-local-feeds, stacked on PR #11
(and #9 / #7). Rick controls merging. Never merge this binary download branch.

42 JVM tests pass per debug/release variant. Both builds/lint pass (0 errors,
1 existing warning each), instrumentation compiles. Four publisher tests and
actionlint pass. Signature/alignment/package checks pass. Actual phone/audio/
real-account tests not run; Rick should verify mode switching, feeds, offline
restart and category filtering on device. See metadata/checksum files.
