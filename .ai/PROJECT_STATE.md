# PROJECT_STATE

Agent responsible: ChatGPT Codex. Task #34 completed and closed, PR #35 merged at c0ef63dbef554e7e8c1210081084cd3f2c025561 under Rick's continuing main/build/publish workflow. No other AI changes found since v0.10.0; no independent self-review claimed.

Completed: reader screen stays awake while Read aloud actively plays and lifecycle is resumed. Pause, stop, completion, error or focus loss clears speech.playing; lifecycle pause immediately releases screen request and reader disposal restores previous view policy. Voice preview is excluded. No new permission/wake lock/setting. Files: ReadingScreenAwake.kt, MainActivity.kt, ReadingScreenAwakeTest.kt, version, README/release manifest/six records. Prior public-article expansion, combined Saved/source tabs, bulk-read/settings/discovery/OLED/speech features remain; see README for existing limits.

Published regular/latest v0.10.1/code13, Android 8+, release/non-debuggable:
https://github.com/rickevans86/FlipRSS/releases/tag/v0.10.1
https://github.com/rickevans86/FlipRSS/releases/download/v0.10.1/FlipRSS.apk
13,316,315 bytes; SHA256 92b640a5fafb48d85996fed1417e4de97392c6a5e2aa8e46386c5a52d47822ee.
Source 25630005a5fc8f82a89af4907e8c2229ab5a286c, app tree a06842eb2d12731cc04649d4d56b31f9bc685f73; main app equals tested source. Immutable artifact 1ed3fd5e4010532807429d3a39c38fa22e9e9c4e / codex/34-release-download / release-download/FlipRSS.apk; never merge binary branch or publish signing key. Signature/alignment/package/version/SDK/non-debuggable/prior signer equality verified. Existing prototype test signer retained; production signing unconfigured. AGP worktree VCS metadata unsupported; external build metadata identifies tested source.

Tests: 85 JVM tests EACH debug/release, both builds/lint (0 errors/1 existing KTX warning), instrumentation compilation, five publisher tests/actionlint pass. Two new device tests cover playback/pause/background/resume/exit and preservation of a pre-existing screen request, but compiled only. Actual phone/screen-timeout/audio/installer/account-write testing not run. Initial disk-full build resolved by cleaning only older generated app/build directories; retry succeeded. Source/keys/native artifacts preserved; 4GB+ recovered.

Hosted publisher 44411187965 SUCCESS. All eleven releases have native APK/checksum/build-info and matching APK digests. Latest regular endpoint verified; native v0.10.1 downloaded and byte count/SHA checked. #34 closed after publication. No implementation/publication blocker or active file claims. Final records-only follow-up changes no app code.

Branch codex/34-speech-screen; PR #35 merged, records-only follow-up. In progress: none after records merge. Next action: Rick install/update and check screen stays on during article speech, then restores normal timeout on pause/end/exit. Shared records updated Yes; append-only history preserved and live task board rebuilt. Remote main/source/artifact/PR/action/release/native bytes Confirmed. No device/runtime or independent review success claimed.
