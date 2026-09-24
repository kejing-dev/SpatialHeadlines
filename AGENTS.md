# SpatialHeadlines continuation guide

Read `README.md`, `docs/PRD.md`, `docs/CONTEXT.md` and `docs/QA.md` before changing behavior. Current baseline: Headline News 1.4.1 (code 6), package `com.headline.news`, PICO Spatial SDK / Spatial UI BOM 6.1.9, English default, Noto Sans / Noto Sans SC.

## Invariants

- Native Spatial UI under PicoTheme; no Material/Material3. Preserve the existing WindowContainer architecture and independent settings window.
- Real publisher headlines paired with their exact HTTPS article URLs. No Google News search fallback, fabricated news or invented popularity.
- Full feed is state; ± changes visible rows only (Overhead 1–3, Trending 1–6). Manual scrolling only. Refresh preserves reading position and stages updates until View latest.
- Separate − / + / settings / refresh toolbar, 48 dp targets, 20 dp scrollbar gutter. Comments #7/#8 are accepted: symmetric gear and no scroll/position text hints.
- Bundled font licenses stay with font assets. Do not publish signing secrets or machine-specific configuration.

## Verification

Use the Gradle wrapper. `:app:testReleaseUnitTest`, `:app:lintRelease`, and signed release assembly are documented in `docs/RELEASING.md`. Set `-PtestBuildType=release` for Release instrumentation. Test APK and target app must share a signing certificate. Always select an explicit device serial; do not replace a physical-device app or uninstall it to work around signature mismatch without authorization.

Run instrumentation cases individually through ADB (PICO CLI shell timeout is 30 seconds). Launch the app before live network checks. Tests use accessibility to operate the real UI; they do not prove physical ray/hand interaction. Inspect actual compositor captures before visual claims. Never accept publisher agreements automatically.

## Local PICO workflow

If the PICO CLI plugin is installed, read its environment/emulator workflow skills before relevant execution. A generated `PICO-SPATIAL-AGENTIC-TOOLS.AGENTS.md` may exist locally; it is not part of the portable source distribution. Reuse a recent healthy environment check where appropriate, prefer high-level CLI operations, and preserve emulator data. Do not change SDK/container strategy without project evidence and applicable public API documentation.

## Handoff

`docs/CONTEXT.md` preserves request history and implementation decisions. `docs/QA.md` distinguishes Debug history, release verification and pending physical-device acceptance. Keep those records accurate when extending the app.
