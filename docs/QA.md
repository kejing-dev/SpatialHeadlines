# QA evidence / 验收记录

## Published Release 1.4.1 (code 6)

Package `com.headline.news`; PICO Spatial BOM 6.1.9 / native Spatial UI; Android API 35; arm64-v8a. The exact published APK was installed and tested on PICO Emulator 6.1.0 in the dedicated `SpatialHeadlines_Release_6_1` AVD. The runtime package flags exclude DEBUGGABLE. Signature v2 verification passed with the dedicated 3072-bit RSA release key.

```text
34cf55451fe884f9ba2ef8c8e23af71ea870dd5ee140cf33d30f7422c188ace8  SpatialHeadlines-1.4.1-release.apk
```

[Signature evidence](qa/release-1.4.1/signature.txt) · [Build summary](qa/release-1.4.1/logs/build-summary.log)

| Check | Result | Evidence |
| --- | --- | --- |
| Release app and Release instrumentation build | PASS | Build summary above |
| Release JVM unit suite | 30 passed, 0 failures/errors/skips | Build summary above |
| Release Android lint | 0 errors, 21 warnings | Dependency/toolchain/style warnings; not a zero-warning claim |
| Release three-view compositor capture | PASS, 17.237 s | [log](qa/release-1.4.1/logs/capture.log) |
| Manual scroll, no auto-scroll, ± and refresh position retention | PASS, 49.811 s | [log](qa/release-1.4.1/logs/manual-scroll.log) |
| Settings, width/count bounds, languages/sources, repeated window transitions | PASS, 69.564 s | [log](qa/release-1.4.1/logs/modes-settings.log) |
| Automatic end refresh with stable reading position | PASS, 15.282 s | [log](qa/release-1.4.1/logs/end-refresh.log) |
| Six live publisher feeds | PASS, 3.139 s | [log](qa/release-1.4.1/logs/live-feeds.log) |
| Exact-article browser handoff | PASS, 30.08 s | [log](qa/release-1.4.1/logs/article-browser.log) |
| Application crash buffer during release checks | No application fatal exception recorded | [log](qa/release-1.4.1/logs/crash-buffer.log) |

## Visual evidence / 界面证据

Captured from the actual release spatial compositor and inspected for populated settings, stable layout, reserved article-action space and separate toolbar. No design render is presented as a runtime capture.

- [Overhead full resolution](qa/release-1.4.1/overhead.png)
- [Trending full resolution](qa/release-1.4.1/trending.png)
- [Independent settings full resolution](qa/release-1.4.1/settings.png)
- [Publisher browser handoff](qa/release-1.4.1/article-browser.png)
- [45-second emulator recording](media/SpatialHeadlines-1.4.1-emulator.mp4): H.264, 1080 × 1080, 30 fps encoded, 44.967 seconds. The emulator may update less frequently than the encoded frame rate. The continuous excerpt starts 3.5 seconds into a 49.178-second raw recording, preserving action speed. Accessibility-driven actions are not evidence of physical hand/controller interaction.
- [Two-column showcase stills](SHOWCASE.md): matching 1600 × 1000 crops keep the simulator room visible.

The video covers manual scroll, a stationary reading interval, row adjustment, refresh, settings and Trending. Exact-article assertions and the publisher page capture are recorded separately. Publisher Terms/Privacy prompts were not accepted automatically.

## Previous Debug evidence / 先前 Debug 验证

Debug 1.4.1 SHA-256: `e8f87ec7e61b93c1ae6b9f15438319a2d59dd70aaf5e6be581c6e23ebb8df64c`. This is a different artifact and certificate from the published Release.

The earlier Debug run passed 30 JVM tests, manual scroll (44.456 s), end refresh (15.260 s), six live feeds (3.570 s), exact browser handoff (30.948 s), and native Spatial UI style checks. The original full settings regression passed on Debug 1.4 (70.326 s); the Release baseline above reruns that regression on 1.4.1. [Retained reviewed logs](qa/debug-1.4.1/logs/) and [prior screenshots](qa/debug-1.4.1/) are historical, explicitly labeled evidence.

Debug 1.4.1 was subsequently installed and launched on the user's selected physical headset. Version 1.4.1 / code 6 and a running app process were confirmed. The physical installation was not replaced while preparing this differently signed Release.

## Scope and remaining device acceptance / 范围与待验收

Automated tests use accessibility to exercise real native UI, live network requests and Android browser intents. They do not validate physical ray/hand target ergonomics, real headset window dragging, frame rate or sustained headset stability. These remain device acceptance tasks. Short successful tests and an empty crash buffer are not a long-duration stability guarantee.

The initial standalone feed test once failed DNS before opening the normal foreground app. Its foreground setup was fixed; all six feeds then passed, including the release run. No production network bypass was added. Earlier host lock/ADB stalls were isolated by preserving the original AVD and using a fresh one. See [context](CONTEXT.md) for continuation details.

Release/debug certificate migration: see [RELEASING.md](RELEASING.md). Network reachability, publisher content changes, consent prompts and subscriptions depend on external services and the headset network.
