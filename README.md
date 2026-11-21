# FlipRSS

A native Android client for FreshRSS, with a magazine grid, topic sections,
a full-screen swipe reader and synchronized saved/read state.

## Connect your FreshRSS account

1. Enable API access in your FreshRSS administration settings.
2. Set a **FreshRSS API password** in your user profile. This can differ from
   your web login password.
3. Open FlipRSS and tap **Connect FreshRSS** (or the account icon).
4. Enter your instance URL, username and API password, then **Sign in**.

An instance URL such as `https://example.org/freshrss/` or the full endpoint
`https://example.org/freshrss/api/greader.php` works. HTTPS is the default.
For a trusted local HTTP instance, explicitly select **Allow unencrypted HTTP**;
this sends credentials over an unencrypted connection. Certificate validation is
never bypassed. Redirects are not followed: use your server’s final URL.

The API password is used for login only. The returned auth token, endpoint and
username are stored in Android Keystore-backed EncryptedSharedPreferences,
excluded from backup/transfer. No API password is saved. Open **Account** to sign
out and use demo mode. **Forget saved sign-in** also resets an unreadable saved
session. Demo bookmarks stay separate from account bookmarks.

## Read your feeds

- **Today:** newest stories, with folder/tag filters. Use the refresh icon to
  reload and **Load more stories** to fetch the next page.
- **Topics:** FreshRSS folders/tags and individual subscriptions, with unread
  counts where provided by the server. Open a section or feed to browse it.
- **Reader:** swipe horizontally or use the arrows; scroll within each story.
  Opening a settled reader page marks it read by default (change this in Settings). **Mark unread/Mark read** changes
  that state explicitly. **Open original article** opens the source in a browser.
- **Saved:** FreshRSS starred articles, paginated independently of Today.
  Bookmark changes sync to FreshRSS from both cards and the reader.
- Failed writes are rolled back locally and show an error. Refresh to reconcile
  server state after a network failure. Expired sessions prompt a new sign-in.
- **Demo:** six bundled sample stories work without an account or network.
  Demo read/bookmark state persists locally. Sample photos: [credits](PHOTO_CREDITS.md).

Articles and catalogue data are held in memory; this version does not provide
Room/offline feed caching, background synchronization or an offline mutation queue.
Article HTML is parsed into readable text blocks with Jsoup. Scripts/embedded
active content are removed and never executed. Cover images load with Coil;
inline rich layouts, inline images and interactive HTML are not implemented.
Use **Open original article** for the publisher’s full layout. Image requests
are separate from API requests and do not receive your auth token.

## Feed management and settings

- **Add feed:** tap the **+** button at the top, enter an RSS/Atom feed URL and
  choose **Add to FreshRSS**. This creates a real server subscription in its
  default folder; catalogue/stories refresh after success. Demo mode opens
  sign-in first. Rejected/invalid URLs show an error without claiming success.
- **Hide read:** the top switch hides read articles immediately and requests
  unread-only pages from FreshRSS, including subsequent pages. The preference
  persists across restarts and is shared with Settings. The reader’s page queue
  stays stable while stories become read.
- **Mark all read:** applies to the current **All stories**, folder/tag, feed or
  **Saved stories** stream. Confirmation names that scope. The server operation
  includes stories not loaded on the phone; a microsecond cutoff captured when
  confirmation opens avoids including newer arrivals. It does not just loop over
  loaded cards. Errors remain visible and require refresh to reconcile.
- **Settings:** tap the gear for system/light/dark theme, reader text size,
  automatic read marking, cover images and hide-read. Changes persist and remain
  independent of account sign-in. **Reset defaults** restores those preferences;
  it does not clear your subscriptions, bookmarks or account. The account entry
  in Settings opens sign-in/sign-out controls.

## Build and run

Open in Android Studio, sync Gradle and run **app**, or with JDK 17+ and Android
SDK platform 37.2 and build tools 36 installed (Gradle 9.6 / AGP 9.5.1 / built-in Kotlin):

```sh
./gradlew assembleDebug installDebug
# Windows:
.\gradlew.bat assembleDebug installDebug
```

Set `ANDROID_HOME` or your ignored `local.properties` with `sdk.dir` as needed.
APK: `app/build/outputs/apk/debug/app-debug.apk`. Android 8.0+ (min SDK 26).
This feature branch is version `0.3.0-controls-preview` (versionCode 3); the
published `v0.1.0-prototype` release contains the older offline-only app.
Production signing configuration is not included.

## Validation

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
./gradlew assembleRelease testReleaseUnitTest lintRelease
# A connected device or emulator is required for:
./gradlew connectedDebugAndroidTest
```

JVM tests use MockWebServer/fakes to verify login/form encoding, API headers,
subdirectory and folder URLs, pagination, write-token renewal, read/star payloads,
redirect protection, content/image parsing, account boundaries, rollback,
stale-response handling, secure-storage failure behavior, subscription results,
unread-only paging, confirmed bulk scope/cutoff and settings persistence.
Instrumentation tests cover demo reading/bookmark/topic flows, the connection
form, reading controls/settings and encrypted-session round trips. Compilation is separate from actual
device execution; see the PR/handoff for what ran in the current environment.
No real account credentials are in the test fixtures.

The workflow targets Rick’s self-hosted **Windows** runner with labels
`SelfHosted` and `Windows`; its emulator action is skipped on Windows. Successful
builds upload the **FlipRSS-debug** artifact.

## Code map

- `data/GReaderClient.kt`, `GReaderApi.kt`: direct Retrofit/OkHttp GReader API.
- `data/ArticleMapper.kt`: content/cover extraction and safe external URLs.
- `data/EncryptedSessionStore.kt`: token persistence and forgetting saved sign-in.
- `data/LibraryController.kt`: account lifecycle, paging, subscriptions and read/bulk state.
- `data/AppSettings.kt`, `AndroidSettingsStore.kt`: persistent appearance/reading preferences.
- `LibraryViewModel.kt`: Android wiring and separate demo preferences.
- `MainActivity.kt`, `AccountDialog.kt`, `AddFeedDialog.kt`, `SettingsDialog.kt`: Compose UI.

AI: **ChatGPT Codex** — prototype (#2), FreshRSS integration (#6) and feed/reading controls (#8).
Read [AGENTS.md](AGENTS.md) and `.ai/` before contributing. Rick selects the
independent reviewer and controls merging unless he explicitly instructs otherwise.

## Read articles aloud (#10)

In an article, tap **Read aloud** to hear its title and available article text.
Use **Pause reading**, **Resume reading**, or **Stop reading** above the article.
Long articles are split into speech-engine-sized chunks. Resume uses the engine's
word position when available; engines without position callbacks restart the
current chunk. Switching articles or leaving the reader stops playback; putting
the app in the background or losing audio focus pauses it. Background playback
and automatic next-article playback are not implemented.

Speech uses Android's configured text-to-speech engine and device language. An
installed voice is preferred; the configured engine may require network access
when no offline voice is available. No microphone permission is needed. If voice
setup fails, open **Android voice settings**, install/select a voice, return and
tap **Retry voice**. This preview needs real-device audio/lifecycle verification.

Implementation: `ArticleSpeech.kt`, `SpeechControls.kt`, `SpeechText.kt`; AI:
**ChatGPT Codex**, #10. Preview version 0.4.0-speech-preview (versionCode 4).

## Local feeds and optional FreshRSS (#12)

New installations open an empty **local library**. Tap **Add feed**, paste an RSS
or Atom XML feed URL, then **Add to app**. Subscriptions, cached text, read state
and bookmarks stay on this device in an app-private file; Refresh fetches updates
while the app is open. Cached articles work offline after restart. No server or
FreshRSS account is required. Local categories use feed titles.

Existing FreshRSS users stay connected after updating. Settings → **Use local
feeds** switches to the separate local library while preserving the saved account.
**Connect FreshRSS (optional)** lets you sign in or **Use saved FreshRSS account**
to return. Add Feed uses the active source: device in local mode, server in
FreshRSS mode. Read/saved state is separate; switching does not import or merge
subscriptions. Sign out forgets the FreshRSS token and opens local feeds.
**Try sample stories** opens the original demo for the current session.

Settings → **Hide categories with no unread articles** filters Today chips and
Topics categories/feeds. Unknown unread counts remain visible. A selected category
that becomes empty returns to All stories. Turning the setting off shows all
categories again; it is independent of Hide read.

Local mode accepts direct HTTP(S) RSS/Atom URLs, not website discovery, OPML,
authenticated feeds with embedded username/password, or background synchronization.
HTTP is unencrypted. Redirects are bounded and HTTPS downgrade is rejected; no
FreshRSS auth is sent to feeds. A feed download is limited to 5 MB and 500 entries.
Cache retains up to 500 stories per feed plus saved stories. Feed URL query tokens
are stored only as part of the local subscription. Device backup/export, custom
local folders, subscription removal and production signing remain future work.

## GitHub Releases

Install builds from https://github.com/rickevans86/FlipRSS/releases . Previews are
marked prerelease while their source PRs await review. All use the existing test
signing identity; signing keys are never uploaded. Checksums and source provenance
are included in each release. Main is merged only by Rick.

`.release/previews.json` lists immutable APK artifact commits, source commits and
SHA-256 hashes. **Publish APK releases** runs on GitHub-hosted Ubuntu when a
`publish-previews-*` tag is pushed or the workflow is manually dispatched on main.
It verifies APK bytes before attaching them and refuses mismatched existing assets.
It backfills earlier releases as well. Local session uploads returned HTTP 401;
the hosted workflow successfully uses its scoped GitHub Actions token. Future
releases add their verified artifact to the manifest and trigger publishing.

AI: **ChatGPT Codex**, #12. Local implementation: `data/LocalFeeds.kt`, controller,
settings/account/feed UI; publication: `.github/scripts/publish-releases.cjs`.

## Regular releases and app updates (#14)

Regular release builds use the filename **FlipRSS.apk** on GitHub Releases.
Historical previews keep their versioned source artifacts; the publisher attaches
the same signed bytes as FlipRSS.apk to each release. SHA256SUMS.txt names that
standard asset. Regular versions are marked as releases and made latest; previous
previews remain prereleases. Existing test signing identity is preserved for
upgrades; production signing is still not configured.

In **Settings → App updates**, tap **Check for updates** to query the latest
regular GitHub release. **Download update** retrieves FlipRSS.apk; the app checks
its exact size and SHA-256 against release metadata, then checks its Android
package, newer versionCode and signing certificate against the installed app.
**Install update** opens Android's installer. Android may first ask you to allow
updates from FlipRSS; return and tap Install update again. Installation always
requires Android's confirmation. Downloads use GitHub HTTPS hosts without any
FreshRSS authentication. Prereleases, incomplete releases, untrusted redirects,
checksum/signature/version mismatches and request failures are reported without
installing. The settings link also opens GitHub Releases manually.

The updater uses GitHub's public API (rate limits apply); no automatic background
checks or silent installs. A downloaded update is kept in app-private cache and
shared read-only with Android's installer through a scoped FileProvider. APKs
with a different signing identity cannot update this installation. Actual
permission/installer/voice/device flows still need phone verification.

Saved always includes read bookmarks, regardless of global Hide read. The Saved
page has no Hide read switch, and FreshRSS saved queries omit the unread-only
filter on every page. Today retains Hide read. The large heading/subtitle and
account banner are removed from all library pages; account controls live in Settings.

Publication runs independently of SelfHosted hardware on GitHub-hosted Ubuntu.
All v0.1–v0.6 releases have native FlipRSS.apk, SHA256SUMS.txt and build-info.json
assets. Use the direct latest APK link:
https://github.com/rickevans86/FlipRSS/releases/download/v0.6.0/FlipRSS.apk
Signing keys remain private; immutable artifact branches retain provenance.

AI: **ChatGPT Codex**, #14. Updater: `data/GitHubUpdates.kt`, `AppUpdater.kt`,
`UpdateControls.kt`; reader filtering: `LibraryController.kt`, `MainActivity.kt`.

## Publishing from main (#16)

Rick authorized merging the feature chain into main. APK publishing now runs on
GitHub-hosted Ubuntu; Android build/test jobs can still use Rick's Windows
SelfHosted runner. The publisher only retrieves already-built, signed APKs from
immutable artifact commits and verifies hashes, so no Android SDK or signing key
is needed on the publishing runner. Dispatch **Publish APK releases** on main to
attach FlipRSS.apk/checksum/build metadata for every manifest version.

Publication succeeded in GitHub Actions run 44254047153. All six APK asset hashes
match the manifest, and the v0.6.0 native download was retrieved and SHA-256 checked.
The in-app updater now has native APK/build metadata assets to use. AI: ChatGPT
Codex, #16; Rick explicitly authorized merging feature and publishing follow-ups.

## OLED theme and popular feed search (#19)

Settings → Appearance → **OLED** uses pure black (#000000) for the page, navigation,
reader, cards and dialog surfaces. Dark mode remains available separately. OLED
also disables Material elevation tinting and the system navigation contrast scrim;
text, controls, images and selection accents remain visible. Theme choice persists.

The **Search** tab browses a bundled directory of 15 well-known RSS/Atom feeds in
News, Technology, Science and Culture. Search titles/descriptions/topics and filter
by topic. **Add [publisher]** opens the existing Add feed confirmation with its URL
filled in, targeting the active local library or FreshRSS account. Demo/expired
sessions first ask you to choose/connect a source, then preserve the selected feed.
The + button still accepts other feed URLs. Search does not send queries anywhere;
it searches this curated list, not the whole web. Feed availability/content remains
under each publisher's control. No subscriptions are added without confirmation.

Settings → **Show feed search** removes the Search tab. Turning it off while Search
is open returns to Today; re-enable it in Settings. Existing subscriptions and the
local/FreshRSS connection are preserved. Reset defaults re-enables feed search.

AI: ChatGPT Codex, #19. Implementation: AppAppearance.kt, FeedSearchPage.kt,
data/FeedDirectory.kt, settings persistence, navigation and feed-URL prefill.

## Source tabs and live feed discovery (0.8.0, #21)

Tabs are **FreshRSS**, **FlipRSS**, **Search** and **Saved**. Topics is removed;
category filters remain on each source page. FreshRSS keeps the connected server's
feeds and synchronization, while FlipRSS keeps the independent on-device library.
Saved combines bookmarks from both, including read stories; server bookmarks are
paged with Load more and remain subject to server availability. Cached server
bookmarks in this session can still be shown after a failed refresh; local
bookmarks survive restart. Read/star actions always target their owning source.

The **+ icon appears only on FreshRSS** and subscribes on that server. Everything
added through Search goes to local FlipRSS, even when a server is connected. Under
FlipRSS → **Manage local feeds**, each feed has **View feed URL**, **Copy feed URL**
and **Add to FreshRSS**. View supports selectable URL and opening the actual feed.
Server subscription asks for the destination account and confirmation; it does not
move/delete the local subscription or bookmarks.

Settings → **Mark as read on scroll** defaults off. When enabled on either source
page, previously visible story cards are marked read after scrolling fully past
the top. It does not mark unseen stories skipped in a jump, react to backwards
scroll alone, or act on Search/Saved. The existing reader auto-mark setting remains
separate. Settings changes apply to both source pages. Saved always stays visible.

Search updates its popular feed catalogue from the exact Feeder/Feedspot pages Rick
provided, once daily on app open when Search is enabled, plus **Refresh feed lists**.
Results are cached privately, deduplicated by normalized feed URL, and attributed
with source links/last update time. Each source refreshes independently: failures
retain its previous cached feeds and show a message. Bundled feeds remain available
offline. No background worker or API key is required. Real-page parser validation
extracted 114 Feeder and 454 Feedspot public feed links; publishers can change their
format, access policy or feed content. Imported URLs are validated as actual feeds
when subscribed; no claim that every external feed is always available.

**Website or feed URL → Find feeds** accepts a site or direct RSS/Atom URL. It checks
feed XML, RSS/Atom alternate links (including relative URLs/base elements), RSS
links/icons and common /feed, /rss, /rss.xml, /atom.xml and /feed.xml paths. Discovered
candidates must return RSS/Atom before being offered. No feed found is a normal
result; some sites publish none or require login/JavaScript. Requests have bounded
redirects, time/size limits and no embedded credentials/FreshRSS auth; HTTPS
redirect downgrade is rejected. HTTP sites are unencrypted. No automatic subscribe.

OLED and the setting to hide Search remain. Local data and saved FreshRSS sign-in
are migrated in place; no library import or deletion occurs. Local mode no longer
needs to replace the connected server to be viewed. Existing storage/renderer/TTS
and test-signing limitations remain. AI: ChatGPT Codex, #21.

The 0.8.0 update is on main at Rick's instruction. Native release APK:
https://github.com/rickevans86/FlipRSS/releases/download/v0.8.0/FlipRSS.apk
Both builds/lint and 64 unit tests per variant passed; device tests compile only.
Hosted publication 44295156332 succeeded, all eight APK asset digests match the
manifest and the latest native download checksum was verified. Actual phone,
Android installer, audio and real-account write validation remain not run.

## Search regression correction (#24)

Feedspot's shared page container must never supply one publisher name to every
RSS row. The importer now pairs numeric row/heading IDs (or an unambiguous local
entry), scopes fallback RSS links to that row, and skips unmatched rows. The old
directory cache is versioned out so incorrect names disappear on update and lists
refresh immediately; subscriptions and Saved are untouched. Catalogue results show
20 at a time with an explicit Load more feeds button, resetting on query/topic or
catalogue changes. Production parser verification on the actual page found 453
feed URLs, 449 distinct titles, one NBC News and the correctly named WordPress URL.

## Speech and voices (0.9.0, #25)

Open **Settings → Speech and voices**, or **Voice** in an article. Choose an
installed Android speech engine and speaker, then use **Preview voice** before
reading. Automatic selects the highest engine-rated quality available for your
phone language/region. Reading speed and pitch are adjustable and persist across
restarts; preview stops when changing playback settings so you can replay them.
The default pace is 95% with neutral pitch. Rate changes do not repeatedly restart
the native engine. Play/pause/resume/stop, article navigation, lifecycle and audio
focus cleanup remain supported.

**Allow online voices** defaults off. Enabling it allows the chosen Android engine
to send article text over the internet and can unlock more natural voices. When
o on-device voice is available, choose/install another voice/engine or explicitly
allow online voices. Offline selection never chooses a voice the engine reports
as network-required. Voices requiring installation are excluded. Use **Android
voice settings** to install/configure voice packs, then **Refresh voices**. Exact
voice quality/catalogue depends on the installed provider, and preview/listening
must be checked on your phone. No new service account or API key is needed for
this Android-engine path. No audible naturalness/device test has been run here.

This release also corrects #24's repeated Feedspot names, clears only the old
public directory cache, and shows 20 search results per explicit Load more.
Subscriptions, saved stories and the FreshRSS account are preserved.

Speech controls were introduced in v0.9.0. Previous native APK:
https://github.com/rickevans86/FlipRSS/releases/download/v0.9.0/FlipRSS.apk
Both builds/lint and 73 unit tests per variant passed. Hosted publication
44300897612 succeeded; downloaded native APK and all nine release APK hashes
verified. Actual phone/audio/naturalness testing remains for Preview voice.

## Bulk-read footer (#29)

FreshRSS and FlipRSS each have **Mark all read** after the loaded stories and
Load more control. It applies to the current source/category, including unloaded
stories, using the same captured scope, arrival cutoff and busy guards as the top
button. Saved and Search do not offer bulk read. Settings → **Confirm mark all
read** defaults on; switch it off to apply either button immediately. The choice
persists across restarts.

## Fuller articles inside the reader (#30)

Opening a FreshRSS, local or Saved story automatically fetches its public webpage
and extracts longer article paragraphs when available. The feed text stays visible
while loading and remains the fallback for errors, restricted pages, JavaScript-only
sites or pages without useful extra text. The reader shows loading/result status
and **Load full article / Retry full article**. Settings → **Load full articles**
turns automatic fetching off; manual loading still works. Read aloud uses the
currently displayed paragraphs and stops if the loaded text changes.

Only the open story is fetched. Requests go directly to the publisher, without
FreshRSS credentials, cookies or JavaScript. Existing HTTP size/time/redirect
limits apply. Hidden/script/advert/navigation/comment blocks are removed;
explicitly restricted article metadata is rejected. This does not bypass logins or
paywalls, and extraction cannot guarantee complete text for every publisher.
Successful text is cached in memory for up to 20 stories, not saved permanently
or synced to FreshRSS; the original feed content/read/bookmark state is preserved.

Verified against the public TechCrunch Snapchat/Lenses story from Rick's screenshot:
144-character feed excerpt expands to 2,573 characters across nine blocks (three
minutes). Regression tests cover extraction filtering, longer-feed preservation,
restricted/landing pages, nesting, manual/cache behavior, speech input, retry,
late response rejection and demo/no-fetch. This is HTML/JVM validation, not a
physical phone or audible playback test.

Combined regular release: **v0.10.0**, code 12, Android 8+.
[Release](https://github.com/rickevans86/FlipRSS/releases/tag/v0.10.0) ·
[FlipRSS.apk](https://github.com/rickevans86/FlipRSS/releases/download/v0.10.0/FlipRSS.apk).
85 JVM tests each debug/release, both builds/lint, five publisher tests/actionlint
pass; instrumentation compiled only. Same existing test signer for updates.
Hosted publisher 44400302581 succeeded. All ten release APK digests and the
latest native download were verified. Native APK: 13,316,315 bytes; SHA-256
`636928bfaed9eb0df748fb8f1a732ce4a5751a92100538dabbb5c6719cd75cf5`.
No physical phone/audio/account-write validation or independent review claimed.

## Screen awake during article speech (#34)

Read aloud keeps the reader screen awake while actively playing. Pausing, stopping,
completion, speech errors, audio focus loss, backgrounding or leaving the reader
releases the request and restores normal screen timeout. Voice preview does not
request this behavior. No wake-lock permission or background playback is added.

Screen-awake patch release: **v0.10.1**, code 13, Android 8+.
[FlipRSS.apk](https://github.com/rickevans86/FlipRSS/releases/download/v0.10.1/FlipRSS.apk).
85 JVM tests per debug/release, builds/lint and publisher checks pass; new screen
lifecycle tests compile only. Actual phone screen-timeout/audio checks pending.
Hosted publisher 44411187965 succeeded. All eleven release native APK digests
and latest downloaded bytes were verified. APK SHA256:
`92b640a5fafb48d85996fed1417e4de97392c6a5e2aa8e46386c5a52d47822ee`.

## Built-in browser and Bypass Paywalls Clean (#37)

**Open original article** now opens a GeckoView browser inside FlipRSS. The toolbar
has close/back/forward/reload/stop, address entry, tabs, an External shortcut and
Browser and add-on controls. Settings → **Use built-in browser** switches back to
an external browser. Browser sessions survive activity recreation and pause when
backgrounded; browser website cookies are separate from the FreshRSS API client.

Settings → **Browser and add-on** (also the extension icon in the browser) includes
**Enable bundled add-on**, **Add-on site controls** and **Extension options**.
Bypass Paywalls Clean is bundled but starts **off** until enabled. It can read and
change page content, cookies and requests on its supported websites; optional
extra site permissions are shown for Allow/Deny. Site support varies. The browser
uses the actual Firefox extension, rather than a WebView imitation. Reader text can now use the rendered browser page/add-on output through the
owned reader bridge described in #40 below.

Pinned GeckoView: `160.0.20261116130455` (MPL 2.0). Pinned Firefox add-on: `4.4.9.6`,
MIT, from the exact repository Rick supplied. The XPI SHA256 matches upstream
release-hashes.txt; all unpacked assets remain byte-for-byte identical. See
[third-party notices](THIRD_PARTY_NOTICES.md), `third_party/bpc/provenance.json`
and Browser → Licences and source. Browser prompts/downloads/file uploads and
media permission integrations are limited; this is not a full Firefox replacement.
Physical browser/site-specific extension tests require a device and are not claimed
from build/unit checks. HTTP(S) navigation is validated; only this bundled add-on's
own origin may open extension pages, and certificate failures are not bypassed.

**One-time update note:** the universal APK is about 250 MiB (261 MB) because it includes
ARM64, ARMv7 and x86-64 browser engines. Versions up to 0.10.1 reject updates above
80 MB: download/install this APK directly over the existing app to retain data.
This version raises the updater limit to 512 MB and allows longer downloads for
future updates, retaining GitHub HTTPS host/size/hash checks. Large release binaries
use ordered, individually verified immutable 32 MiB artifact parts internally;
GitHub Releases still exposes one normal `FlipRSS.apk` asset.

Published regular browser release: **v0.11.0**, code14.
[FlipRSS.apk](https://github.com/rickevans86/FlipRSS/releases/download/v0.11.0/FlipRSS.apk).
Hosted publisher 44443483017 succeeded; all twelve native release APK digests
verified, and latest native bytes/build-info/checksum downloaded and matched.
APK: 261,392,018 bytes; SHA256
`07f8cbba43ce1db0b9582388741f3e268776e3521b412f897ce248376dabd21c`. 90 JVM tests
per debug/release, release build/lint (0 errors/17 warnings), nine publisher tests
and actionlint pass; instrumentation compiles only. Runtime browser/site-specific
add-on tests remain pending.

## Browser text in the reader and Read aloud (#40)

As of v0.13.0, Claude’s compact browser removes the manual **Read in app** button.
The automatic reader loader and **Load browser text** remain available. The manual
return instructions below describe the earlier v0.12.x browser.

The reader now uses the built-in browser when a plain article fetch cannot expand
the feed text. If Bypass Paywalls Clean is enabled, its page changes apply to this
load too; with the add-on enabled the app also checks for more text after a partial
plain-HTML result. **Load full articles** and **Use built-in browser** control the
automatic requests. **Load browser text** explicitly retries rendering for one
story, using the current add-on choice. Existing feed/static text stays visible
and is retained if rendering fails. The result is used by **Read aloud**.

If you can see more by opening **Open original article**, let the page/add-on finish
and tap **Read in app** in the browser. That brings the article paragraphs back to
the same reader story. It will not import a different story you browsed to. Return
uses an expiring in-process token, not a large article body in Android Binder.

An owned, separately bundled reader bridge extracts rendered article paragraphs
on native request. It does not read article bodies from hidden JSON/scripts, grant
new BPC permissions or enable the add-on by itself. CSS-hidden/menu/ad/comment/
form content is excluded; visible modal consent/login dialogs need user action in
the browser first. Replies require the owned extension/top frame/registered
session/current port/native document URL and matching random request nonce.
Original/canonical publisher URL, headline, size and longer-text checks apply.
Article identity, source ownership and read/bookmark state remain unchanged.

Rendering uses a noninteractive, transparent TextureView behind the reader with a
real viewport, shared publisher browser profile and current add-on settings. Only
the selected article is loaded; backgrounding/leaving/swiping cancels its request
and closes its session. Automatic capture waits for dynamic content with a bounded
26-second total; manual capture is bounded to 16 seconds. Successful text remains
in the existing 20-story memory cache. Sites that require scrolling/interaction or
take longer can still need manual **Read in app**; full text is not guaranteed.

Bridge source is under `assets/reader-bridge`, currently version **1.0.0**. Bump its
manifest version whenever its code changes in future releases so Gecko reinstalls
it. Original BPC assets/XPI remain byte-identical. The static parser still rejects
restricted metadata; browser import reads only the rendered page's article DOM.

Development checks: `npm ci --ignore-scripts`, `npx playwright install chromium`,
`npm run test:reader-bridge` (or set `CHROMIUM_PATH` to an existing Chromium).
These are headless browser DOM fixture tests, including simulated add-on updates;
they do not establish real Gecko/BPC behavior on every site or a physical phone.

Published regular/latest **v0.12.0/code15**: source9dda973, 107 JVM tests each variant, six
headless Chromium fixtures, release build/lint0errors17warnings and nine publisher
tests/actionlint pass; instrumentation compiled only. Signed APK 261408558 bytes,
SHA256 `b55d2dbba7bdedd5fa7940eadcefa229fc51094e658dc9992dcaff05caec99f5`. Hosted publisher44537613170 SUCCESS; all13 native release digests verified,
and latest APK/build-info/checksum downloaded and matched.
[Download FlipRSS.apk](https://github.com/rickevans86/FlipRSS/releases/download/v0.12.0/FlipRSS.apk).
Actual Gecko/BPC/The Age/device behavior remains to be verified on a phone.

## Source tabs in bottom navigation (#43)

Settings → Bottom navigation has independent **Show FreshRSS** and **Show FlipRSS**
switches, both on by default. Turn one off to hide its bottom bar icon. The choice
persists across restarts. Existing accounts, feeds and combined Saved stories are
retained. Search stays independently configurable and can still add local feeds.
If the selected tab is hidden, the app moves to FlipRSS, FreshRSS, Search or Saved
in that order of availability and clears the previous category. Saved always stays
visible, including when every optional tab is off. These switches control tab
visibility; they do not sign out or stop background account restoration.

Published regular/latest **v0.12.1/code16**:109 JVM tests each variant, release build/lint
0errors12warnings, instrumentation compilation,9 publisher tests/actionlint pass.
APK 261408558 bytes, SHA256 `df1c963eda36fc0bfb022ce4f1cd761caa5a58607832b9379eae32fc3d05d5a5`.
Hosted publisher44737339847 SUCCESS; all14 native release digests verified.
Latest native APK/build-info/checksum downloaded and matched signed build.
[Download FlipRSS.apk](https://github.com/rickevans86/FlipRSS/releases/download/v0.12.1/FlipRSS.apk).
Actual phone preference/navigation checks not run.

## Compact browser, private tabs, uBlock and feed layout (#48/#49)

Claude implemented these features in main commit `2985325`; Codex builds and
publishes them in v0.13.0 (code 17). The browser shows a page title, **External**
and loading progress. Use Android system Back to navigate back or close it.
Browser add-on controls remain under **Settings → Browser and add-on**. The prior
address toolbar, tab switcher, add-on icon and manual **Read in app** action are
removed by this design; automatic article text loading remains available.

Browser tabs and add-on popups use Gecko private sessions, and tabs close on exit.
The Settings page has independent switches for bundled Bypass Paywalls Clean and
**uBlock Origin 1.76.7.0**. Both are opt-in; existing choices are retained. uBlock
runs as a background ad blocker. Its pinned XPI, source reference and GPL-3.0
licence are included in `third_party/ublock`, the asset bundle and notices.

Under **Settings → Appearance → Feed layout**, choose **Tiles** for the existing
magazine layout or **List** for compact rows with right-hand thumbnails. This
preference is saved. Real device checks for navigation, private-session behavior,
add-on operation and layout persistence remain pending; build and unit checks do
not establish those runtime results.

Before publishing, run `python3 scripts/verify-apk-assets.py path/to/FlipRSS.apk`.
This checks every bundled asset against source, including uBlock's required
`_locales` files. Android's default asset filter excludes underscore-prefixed
folders; this project overrides that filter to keep extension locales in the APK.
