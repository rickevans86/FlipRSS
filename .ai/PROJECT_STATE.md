# PROJECT_STATE

Agent responsible: Claude. Issue #48 implemented and pushed directly to main at Rick's in-session instruction (owner-merge rule overridden). Claude authored it and does not self-review; Rick names the reviewer. Previous state: #43/PR #44 (independent FreshRSS/FlipRSS bottom-bar switches) and signed native v0.12.1 publication by ChatGPT Codex remain in place; no other AI changes/overlapping claims.

In-app browser (#48):
- Chrome compacted to page title + External button + load progress only. Removed close/back/forward/reload/tab-switcher/add-on-icon row, address bar + Go, and Read-in-app. Navigation and closing use the Android system back gesture.
- Add-on management lives in app Settings → "Browser and add-on" (settings-only BrowserActivity, closes on dismiss); in-browser puzzle entry removed.
- Browsing is non-persistent: GeckoView private-mode sessions/popups, all tabs closed on exit, extensions allowedInPrivateBrowsing.
- Two bundled add-ons: Bypass Paywalls Clean 4.4.9.6 (MIT) and uBlock Origin 1.76.7.0 (MV2, GPL-3.0), each with an independent Settings toggle. uBlock built unmodified from pinned source (commit 4d6a3ad), unpacked into assets/ublock/, pinned xpi + hash test; background blocking, no toolbar/popup UI. ~29 MB assets.

Feeds (#48):
- AppSettings.feedLayout (TILE default | LIST), persisted. Settings → Appearance → Feed layout. LIST = single-column compact rows with right-hand thumbnail (StoryRow); TILE = existing hero + two-column magazine cards.

Checks: NOT RUN in authoring environment (no Android/GeckoView build or device; maven.mozilla.org off allowlist). UBlockBundledAddonTest added (pins xpi SHA256, verifies assets match archive, manifest id/version, GPL licence). SelfHosted CI build + Rick device test still required. Windows CI queue / SelfHosted runner permissions restriction unchanged. Production signing still unconfigured (prototype signer). No new release built or published in this session.

Remote main synchronization: Confirmed (pushed). Shared records updated: Yes.
