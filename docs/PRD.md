# Product requirements / 产品需求

Status: implemented baseline 1.4.1. Product decisions come from the user's instructions and accepted Figma comments. Newer explicit requirements supersede earlier drafts.

| Requirement / 需求 | Acceptance / 验收 |
| --- | --- |
| Native PICO app | Spatial SDK BOM 6.1.9; native Spatial UI under PicoTheme; preserve WindowContainer architecture. |
| English by default / 默认英文 | First launch uses English, BBC US, Overhead, three visible rows. Noto Sans supersedes the earlier PICO Sans request. |
| Overhead / 头顶横条 | 1–3 numbered visible headlines, 800/1000/1200 dp widths. System handle allows placement; no invented head-locking. |
| Trending / 紧凑新闻窗 | 1–6 numbered visible headlines, 480 dp width; user places at side/lower side. No fabricated popularity metrics. |
| Full feed / 完整列表 | Up to 30 validated publisher articles; ± changes viewport height only. |
| Manual scroll / 手动滚动 | The list moves only through user scroll input or explicit View latest. No auto-scroll or timer paging. |
| Toolbar / 独立工具栏 | 384 × 72 dp, separate glass surface, 24 dp below news, 48 dp actions: − / + / settings / refresh. |
| Hover / 悬停 | No tooltip covering other titles; stable two-line titles, reserved article-action area; native indicator has 20 dp gutter. |
| Settings / 设置 | Independent 800 × 840 dp window; layout, width, per-mode counts, edition, source/region, refresh-at-end persist. |
| Articles / 新闻原文 | Real publisher title and exact HTTPS article URL remain paired. Separate button opens browser; no search URL fallback. |
| Refresh / 刷新 | Manual refresh immediately checks. End refresh on by default, coalesces requests and throttles automatic checks to 60 seconds. |
| Reading continuity / 阅读连续性 | Keep current snapshot and offset; stage updates behind “N new stories · View latest”. Source changes reset their own queue. |
| Network / 网络 | IO off UI thread, timeout/cancellation, five-minute same-source cache, saved/error/retry/empty states. No fake news fallback. |
| Design comments / 评论 | #7 symmetric outlined gear; #8 remove SCROLL TO READ / SCROLL FOR MORE and position footer. |

Validation matrix and open device acceptance: [QA.md](QA.md). Review exports: [design](design/DESIGN-NOTES.md).
