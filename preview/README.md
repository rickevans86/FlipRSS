# FreshRSS preview — 0.2.0-freshrss-preview

Install the APK on Android 8.0+. It has versionCode 2 and uses the existing
prototype test signing identity; it can update the earlier prototype signed with
that identity. It is a release variant (not debuggable), not a production release.

Tap **Connect FreshRSS**, enter the instance URL, username and FreshRSS API
password. Enable API access and create an API password in FreshRSS first.
HTTPS is the default; HTTP requires explicit opt-in for a trusted local server.

Includes live subscriptions/folders/tags, unread counts, paged articles, cover
images and text reader, refresh/load more, read/unread and starred synchronization,
Keystore-backed encrypted session storage and separate offline demo mode.
Live articles are held in memory; background/offline queues and rich inline HTML
are not implemented. Passwords are not retained, and images do not receive your
API token.

Source: edcac9836b8981b3e678962350cd77a32869bd79, branch codex/6-freshrss, Issue #6.
Built by ChatGPT Codex. All 18 unit tests passed in each debug/release variant;
lint/build passed. Instrumentation tests compile but were not run. No real account
or device validation is claimed. Enter your credentials in the app, not in GitHub.

See build-info.json and SHA256SUMS.txt for artifact details. This download branch
is not intended to be merged into main. The feature PR is separate; the published
v0.1.0-prototype release remains the older offline-only version.
