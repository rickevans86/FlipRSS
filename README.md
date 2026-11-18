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
  Opening a settled reader page marks it read. **Mark unread/Mark read** changes
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
This feature branch is version `0.2.0-freshrss-preview` (versionCode 2); the
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
stale-response handling and secure-storage failure behavior.
Instrumentation tests cover demo reading/bookmark/topic flows, the connection
form and encrypted-session round trips. Compilation is separate from actual
device execution; see the PR/handoff for what ran in the current environment.
No real account credentials are in the test fixtures.

The workflow targets Rick’s self-hosted **Windows** runner with labels
`SelfHosted` and `Windows`; its emulator action is skipped on Windows. Successful
builds upload the **FlipRSS-debug** artifact.

## Code map

- `data/GReaderClient.kt`, `GReaderApi.kt`: direct Retrofit/OkHttp GReader API.
- `data/ArticleMapper.kt`: content/cover extraction and safe external URLs.
- `data/EncryptedSessionStore.kt`: token persistence and forgetting saved sign-in.
- `data/LibraryController.kt`: account lifecycle, paging and mutation state.
- `LibraryViewModel.kt`: Android wiring and separate demo preferences.
- `MainActivity.kt`, `AccountDialog.kt`: Compose reading/account UI.

AI: **ChatGPT Codex** — prototype (#2) and FreshRSS integration (#6).
Read [AGENTS.md](AGENTS.md) and `.ai/` before contributing. Rick selects the
independent reviewer and controls merging unless he explicitly instructs otherwise.
