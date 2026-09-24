# Headline V3 design review

Approved V3 design; implementation is Headline News 1.4.1. User correction: manual scrolling only.

- Separate toolbar: minus, plus, settings, refresh, in one row outside the news.
- Overhead 1–3 visible; Trending 1–6 visible. The article list can contain more stories than the visible row count.
- Manual vertical scrolling only. No timer, autoplay, automatic paging, or scroll speed setting. The reading position stays still after interaction.
- Reaching the end of the list checks for updates. Coalesce simultaneous requests; trigger once per end reach, rate limit repeated checks, and never spin when the list is shorter than the viewport.
- The refresh icon checks for news immediately. Loading is represented in the icon and status below the toolbar.
- Preserve the current article and offset when refreshing. Show a separate “6 new stories · View latest” action below the toolbar; only that explicit action moves to the latest news. Do not replace an item under the pointer.
- Changing row count/width or closing settings preserves reading position. A different source starts its own list.
- Empty result: show an empty state with refresh. Network error: retain cache and show retry/status outside the headlines. No new result: “You’re up to date.”
- Hover makes the row more opaque and reveals a separate action opening the exact publisher article. Reserve space for the action. Long titles use two lines with ellipsis within a stable row, avoiding movement of adjacent actions.
- Settings are a separate spatial window. Refresh-at-end is on by default.
- English default. The imported SVG uses the app’s Noto Sans and Noto Sans SC as vector outlines to preserve appearance; these are editable vector layers, not native text or auto-layout components.
- Figma MCP requires reauthentication; imported through the authenticated Figma UI into the original file. Prior V2 remains available.
- Existing V2 glass palette and rounded controls retained. No app Code Connect mappings were found; prior component/token inventory: work/design-system-state-headline-v2.json.

Queue counts and headlines are illustrative design content.

Implementation overrides from the user's two approved Figma comments (2026-09-23):
- #7 “齿轮图标有问题改下”: replace the malformed gear with a symmetrical outlined gear matching the other 24 dp icons.
- #8 “不需要这个提示”: remove SCROLL TO READ / SCROLL FOR MORE and the news-position footer. The native scroll indicator remains; the toolbar retains its visible-row count for ±.

The original PNG/SVG review exports predate these comments. The 1.4 emulator screenshots and QA report record the implemented result.

Final emulator QA in 1.4.1 widened the native indicator gutter to 20 dp so the scroll indicator does not touch the reserved 48 dp article action.
