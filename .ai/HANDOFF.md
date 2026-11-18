# HANDOFF

**Agent:** ChatGPT Codex
**Task:** #6 — FreshRSS integration, authorized by Rick on 2026-11-18.

**Completed:** GReader login, encrypted token restoration/sign-out, subscriptions,
folders/tags, unread counts, stream pagination and refresh/load more. Live stories
integrated into magazine/topics/feed/saved/reader screens. Read/unread and starred
writes use CSRF write tokens (renew once on 403), optimistic state with rollback,
and account/request-generation guards. Demo data/preferences stay separate.
Safe text extraction and cover loading; original HTTP(S) links open in browser.
API password is not retained; auth token uses Keystore-backed encrypted prefs
excluded from backup. HTTPS default, explicit HTTP opt-in, no redirect credential
replay and no API auth on image requests. Article parsing runs off the UI thread.

**Branch/PR:** codex/6-freshrss, implementation source commit `edcac9836b8981b3e678962350cd77a32869bd79`.
Feature PR pending creation; do not merge or publish a new release without Rick’s
instruction. PR #5’s release facts and append-only logs are preserved here; it
will be superseded by this feature PR to avoid conflicting shared-memory updates.

**Preview APK:** Version 0.2.0-freshrss-preview (versionCode 2), 13,164,707 bytes,
release variant, debuggable=false, existing prototype test signing identity.
https://github.com/rickevans86/FlipRSS/blob/6f6673b766c8e000406ec582f3c61596dfe0261f/preview/FlipRSS-v0.2.0-freshrss-preview.apk?raw=true
SHA-256: `048a009c790c3c1960450dce2deae53966d669d847b6b48c7de67d83428b07ae`.
Artifact-only branch codex/6-freshrss-download must not be merged into main.
APK bytes retrieved through GitHub blob API and checksum verified.

**Tests:** Debug and release builds/lint passed; all 18 JVM tests passed in EACH
variant. Debug lint 0 errors/8 warnings, release lint 0 errors/1 warning (dependency
update notices / KTX suggestion). MockWebServer tests cover login/form/header/URL,
folder encoding/pagination, write token renewal, exact mutation IDs/tags, rejected
credentials and password redirect protection. Mapper tests cover HTML removal,
relative/media/lazy image URLs and malformed IDs. Controller tests cover paging,
rollback, session expiry, old-account responses, refresh/write races, token-only
restore, secure-store failure and failed-login/current-write races. Demo tests
remain passing. Instrumentation APK compiles (demo/account UI + encrypted store
round trip), but no device/emulator run. No real account/instance test performed.
APK signatures v2/v3, alignment, package/version/SDK and debuggable=false verified.

**In progress:** Awaiting Rick’s device/account validation and independent review.
**Blockers/limits:** No implementation/build blocker. Live articles are in memory;
no Room/offline queue/background sync. Reader extracts text, not rich interactive
HTML/inline images. security-crypto APIs are deprecated but follow the recorded
EncryptedSharedPreferences architecture; no plaintext fallback. Device crypto
behavior is covered by compiled instrumentation tests, not execution here.

**Previous release:** v0.1.0-prototype remains published and unchanged, pointing to
main `92613d6`; it is the OLD OFFLINE-ONLY version. APK on separate branch
codex/4-release-download. Release-asset uploads returned HTTP 401; verified Git
branch links are used. This feature preview is not a new published release.

**Next action:** Rick installs preview, enables FreshRSS API access, sets an API
password in FreshRSS profile, then uses Connect FreshRSS in the app. Rick selects
an independent reviewer; code must not be reviewed independently by its author.
**Shared records updated:** Yes — all six files; previous release log/decisions retained.
**Remote synchronization:** main fetched/ancestry verified; source/download commits
pushed and retrieved APK checksum confirmed. No main merge performed for #6.
