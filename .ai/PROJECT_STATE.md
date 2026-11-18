# PROJECT_STATE

## On main / published

main `92613d6` contains the offline prototype from PR #3. Codex published
v0.1.0-prototype on 2026-11-18 at Rick’s request:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.1.0-prototype
It remains offline-only, test-signed, with verified APK/checksum hosted on
codex/4-release-download because release-asset uploads returned HTTP 401.

## FreshRSS feature — #6, codex/6-freshrss (not merged)

Version 0.2.0-freshrss-preview adds account login using API password, Keystore-backed
encrypted token session, sign-out/forget, subscriptions/folders/tags/unread counts,
paginated streams, refresh/load more, live cards/reader, server starred view and
read/unread/star synchronization. Write failures roll back with visible errors;
account/request guards prevent stale data leaks. Demo state remains separate.
HTTPS default/HTTP opt-in; redirects disabled; API token not sent to image hosts.
Text/cover parsing with Jsoup runs off the UI thread; Coil loads covers.

Implementation commit edcac98; preview APK at immutable download commit 6f6673b
on codex/6-freshrss-download (artifact branch, do not merge). See handoff for link
and checksum. This preview is not a published release and does not change v0.1.0.

## Verification / next

18 JVM tests pass in each debug/release variant; build/lint and instrumentation
APK compilation pass. Preview signature/alignment/SDK/version and downloaded
checksum verified. No actual device/real-instance tests; Rick must validate with
his account. Live cache is memory-only, no offline queue/background sync; article
reader is extracted text rather than rich HTML. Production signing remains
unconfigured. Independent review is pending. Prior release records PR #5 facts
are preserved here and its duplicate record updates will be superseded.
