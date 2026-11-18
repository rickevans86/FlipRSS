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
SDK platform/build tools 35 installed:

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
SHA-256 hashes. **Publish APK previews** runs on the Windows SelfHosted runner
when a `publish-previews-*` tag is pushed (or manually after the workflow reaches
the default branch). It verifies APK bytes before attaching them and checks
existing assets without overwriting mismatches. This also backfills earlier
previews. Local session uploads to uploads.github.com returned HTTP 401; the
workflow uses its scoped GitHub Actions token. Release-note download links remain
available independently if the runner is offline. Future releases add their
verified artifact to the manifest and trigger publishing.

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

Publication uses any runner labelled SelfHosted (the script works on Windows or
Linux). Older queued publishing runs are superseded by the standard-filename job.
If no matching runner accepts it, APK links in release notes remain available,
while attaching APK assets waits for runner execution. No signing key is committed.

AI: **ChatGPT Codex**, #14. Updater: `data/GitHubUpdates.kt`, `AppUpdater.kt`,
`UpdateControls.kt`; reader filtering: `LibraryController.kt`, `MainActivity.kt`.

## Publishing from main (#16)

Rick authorized merging the feature chain into main. APK publishing now runs on
GitHub-hosted Ubuntu; Android build/test jobs can still use Rick's Windows
SelfHosted runner. The publisher only retrieves already-built, signed APKs from
immutable artifact commits and verifies hashes, so no Android SDK or signing key
is needed on the publishing runner. Dispatch **Publish APK releases** on main to
attach FlipRSS.apk/checksum/build metadata for every manifest version.
