# Development context / 开发上下文

This is a reviewed project handoff, not a raw chat transcript. It preserves user requirements, accepted decisions, implementation evidence and remaining work without carrying credentials or unrelated host data.

## Current baseline / 当前状态

- Repository: private `kejing-dev/SpatialHeadlines`. App name remains Headline News; Android package `com.headline.news`.
- Version 1.4.1, code 6; native PICO Spatial SDK / Spatial UI BOM 6.1.9, Noto Sans + Noto Sans SC.
- Approved design: [Figma V3, node 46:1353](https://www.figma.com/design/8NIauHpHQG35aJrJf8iPuy/Headline?node-id=46-1353). Page 45:814; independent settings 45:815; refresh states 45:919. Earlier node 20:1173 is historical.
- Debug 1.4.1 was installed and launched on the user's explicitly selected physical headset. Package version/code and a live process were verified. This establishes installation/liveness only, not complete headset acceptance.
- This publication adds genuine non-debuggable Release packaging, a dedicated local signing key, bilingual documentation and release-runtime evidence. It does not change application behavior.

## Requirement history / 需求演变

1. Build the Figma Headline application with PICO CLI, Spatial SDK and Spatial UI, verify in PICO Emulator, provide an APK and ADB install command.
2. Fix hover information covering nearby headlines; implement a populated independent settings window; make English the default.
3. Introduce Overhead 1–3 rows and a side/lower-side Trending layout up to 6 rows, numbered entries and quick ± controls.
4. Later explicit font request changed PICO Sans to Noto Sans. That latest requirement is authoritative.
5. Replace Google News keyword search links with actual publisher headlines and the exact corresponding article pages. Do not invent heat counts or labels for feeds that do not supply them.
6. Add a manually scrollable complete feed and a separate toolbar row for − / + / settings / refresh. The user explicitly corrected “automatic scrolling” to **manual scrolling**. Automatic refreshing at the end is distinct from moving the list.
7. Preserve reading position through refresh; stage new stories behind View latest. Independent settings saves immediately.
8. Approved Figma comments #7 and #8: fix the gear icon; remove scroll prompts and the news-position footer. Runtime visual QA widened the native indicator gutter from 4 to 20 dp to separate it from article buttons.
9. Publish source, Release APK, Chinese/English READMEs, screenshots/video and this continuation context in a new private SpatialHeadlines GitHub repository.

10. Use the user-provided `kejing-dev/ai-context` spatial-app-showcase skill and Feishu CLI to fill the existing “Headlines | 头条新闻” entry with introduction, video, two room-background images and PRD/Figma/GitHub/APK links. See [showcase](SHOWCASE.md).

## Design provenance / 设计依据

Figma MCP required reauthentication during earlier work. Comments were read in the authenticated Figma UI, and the V3 SVG/PNG layouts were imported into the existing file through that UI. No unavailable MCP design-context call was represented as successful. Exported text is outlined vector content, not native Figma text or auto-layout. Earlier exports can show pre-comment hints; implemented runtime evidence and accepted corrections in `docs/design/DESIGN-NOTES.md` take precedence.

The historical `design/` folder retains earlier reads/reviews as provenance. Its demo/popularity and old control descriptions are superseded by `docs/PRD.md`; do not reintroduce them.

## Proven implementation constraints / 已验证约束

- Keep WindowContainer and native Spatial UI under PicoTheme. Do not introduce Material/Material3 or replace spatial surfaces with generic Android screens.
- Keep title–article URL pairing, per-source cache isolation and HTTPS publisher filtering. Six configured feed variants: BBC US/UK, Guardian US/UK, BBC Chinese, DW Chinese.
- Full publisher queue in state; ± adjusts visible rows only. No auto-scroll. Keep the current article/offset until View latest; source changes start their own queue.
- Separate 384 × 72 dp toolbar and independent 800 × 840 dp settings window. Fonts are bundled with their licenses.
- Browser Terms/Privacy prompts belong to the user. Automated tests must not accept them.

## QA lessons / 测试经验

- PICO CLI shell has a 30-second timeout; use raw ADB for the existing longer instrumentation cases, explicitly targeting the emulator. Run cases individually.
- Spatial IconButton labels live on a child; clickability and URL stateDescription live on a parent. Test helpers climb the accessibility tree.
- Await animated scrolling before checking visible articles. The end-refresh case verifies an advancing saved-response timestamp rather than ephemeral status copy.
- The live-feed test must launch the normal foreground app first. A test-only background process had DNS failures; after normal launch all six feeds passed. No production networking bypass was introduced.
- `UiAutomation.takeScreenshot` misses the spatial compositor; shell `screencap -p` captured the room correctly in the verified emulator. Always inspect artifacts.
- An earlier AVD stalled after macOS auto-lock. A fresh QA AVD worked; the original was preserved. If the host locks, ask the user to unlock; do not bypass authentication or wipe AVDs as a routine repair.
- Release uses a fresh `SpatialHeadlines_Release_6_1` AVD to avoid replacing the differently signed Debug app. Physical device serials and host-specific configuration are deliberately not included.

## Remaining acceptance / 待验收

See `docs/QA.md`. Physical hand/controller target ergonomics, spatial window dragging, sustained headset stability and frame-rate measurements remain unverified. Publisher availability depends on the headset network. Do not describe short emulator tests as long-duration device validation.

Release key material is retained locally outside the repository. Future releases must reuse it to support upgrades. A clone can build unsigned Release or use its own signing key, but that different certificate cannot upgrade this published APK.
