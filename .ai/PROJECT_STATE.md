# PROJECT_STATE

Agent: ChatGPT Codex. Task #34, Rick-assigned. Latest verified regular release remains v0.10.0 pending v0.10.1 publication. No other AI changes/open competing PRs found since main c9617bc.

Completed: reader-only ReadingScreenAwake effect follows speech.playing while lifecycle is resumed. Pause/stop/completion/error/focus-loss clear playback state, lifecycle pause immediately releases screen request, disposal restores previous view policy. No new permission, wake lock or setting; voice preview excluded. Files: ReadingScreenAwake.kt, MainActivity.kt, ReadingScreenAwakeTest.kt, version, README/release manifest/six shared records.

Source 25630005a5fc8f82a89af4907e8c2229ab5a286c, app tree a06842eb2d12731cc04649d4d56b31f9bc685f73, branch codex/34-speech-screen, PR pending. v0.10.1/code13 signed release artifact 1ed3fd5e4010532807429d3a39c38fa22e9e9c4e, codex/34-release-download (never merge), release-download/FlipRSS.apk. 13316315 bytes, SHA256 92b640a5fafb48d85996fed1417e4de97392c6a5e2aa8e46386c5a52d47822ee. Signature/alignment/SDK/version/non-debuggable/prior signer equality verified. Prototype test signing retained; production signing unconfigured, no key published.

Tests: 85 JVM tests EACH debug/release, both builds/lint (0 errors/1 existing KTX warning), instrumentation compilation, five publisher tests/actionlint pass. Two new device tests cover playback/pause/background/resume/exit and pre-existing screen request preservation; compiled only, no runtime device/screen-timeout/audio validation or independent review claimed. Initial disk-full build resolved by deleting only older generated app/build outputs; retry succeeds. Source, keys, APK artifacts preserved.

In progress: main merge/native publication under Rick's continuing workflow. Blockers none. Next: PR/merge, hosted publisher, actual native asset/digest/download verification, task close/final records. Prior full-article/local/FreshRSS/speech features and limitations remain as documented. Shared records updated Yes; remote synchronization Confirmed.
