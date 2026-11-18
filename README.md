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
