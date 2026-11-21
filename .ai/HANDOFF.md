# HANDOFF

Agent responsible: Claude. Issue #48, authorized by Rick in-session (direct-to-main; owner-merge rule overridden by explicit session instruction). Claude authored this change and therefore does not review it — Rick names the reviewer.

Completed (in-app browser + feeds):
- Compact browser chrome: the browser now shows only the page title + External button + load progress. Removed the top row (close, back, forward, reload/stop, tab switcher, add-on puzzle icon), the address bar + Go, and the Read-in-app button. Close/back use the Android system back gesture (existing BackHandler: history back → close extra tab → finish).
- Add-on controls relocated: the in-browser puzzle entry is gone. Add-on management stays at Settings → "Browser and add-on" (reuses the settings-only BrowserActivity, which now closes on dismiss).
- Non-persistent browsing: GeckoView sessions/popups use private mode (no persisted cookies/history/cache); all tabs close on exit; BPC and uBlock are marked allowedInPrivateBrowsing so they still run.
- uBlock Origin 1.76.7.0 (MV2, GPL-3.0) added as a second bundled add-on: built unmodified from pinned upstream source (gorhill/uBlock commit 4d6a3adc716c1a8e47167234f37279eb83613e66) with uBlockOrigin/uAssets filter lists, unpacked into app/src/main/assets/ublock/, pinned xpi at third_party/ublock/uBlock0_1.76.7.0.firefox.xpi (SHA256 302d0966c04c905425306e53bc87e9660f91e0210b4639cb70234f7c3e72cd21). Installed via ensureBuiltIn, independent toggle/pref ("ublock-enabled"), no toolbar/popup UI (background blocking). ~29 MB added to assets.
- Feed layout setting: AppSettings.feedLayout (TILE default | LIST), persisted in app-settings ("feed-layout"); Settings → Appearance → Feed layout. LIST renders single-column compact rows with a right-hand thumbnail (StoryRow).

Files changed: app/src/main/java/com/fliprss/app/browser/{BrowserActivity,BrowserEngine,BrowserModel}.kt; MainActivity.kt; SettingsDialog.kt; data/{AppSettings,AndroidSettingsStore}.kt; app/src/main/assets/ublock/** (new, 667 files); app/src/main/assets/notices/{browser.txt,GPL-3.0.txt}; THIRD_PARTY_NOTICES.md; third_party/ublock/uBlock0_1.76.7.0.firefox.xpi (new); app/src/test/java/com/fliprss/app/browser/UBlockBundledAddonTest.kt (new).

Tests: NOT RUN. No Android SDK / GeckoView build or device in the authoring environment (maven.mozilla.org is outside the allowlist, so Gradle cannot resolve GeckoView here). Needs the SelfHosted CI build + on-device verification by Rick. Added UBlockBundledAddonTest mirrors BundledAddonTest (pins xpi hash, verifies assets match the archive byte-for-byte, checks manifest id/version and GPL licence).

Blockers: Existing Windows CI queue / SelfHosted runner permissions restriction unchanged (not touched). Production signing still unconfigured (prototype signer).

Next action: Rick build v-next on SelfHosted + install, then verify — browser shows only title/External and system-back closes it; add-on + ad-blocker toggles from Settings; open a news/paywall page with BPC on and an ad-heavy page with uBlock on; confirm nothing persists after exit (cookies/history); switch Feed layout Tiles↔List and confirm it persists across restart.

Shared records updated: Yes. Remote synchronization: Confirmed (pushed to main). Reviewer: Rick to name (not Claude).
