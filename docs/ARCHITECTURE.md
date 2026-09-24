# Architecture

The app keeps its original native WindowContainer model. `Main.kt` hosts the spatial entry point; `platform/LaunchActivity.kt` and `SpatialApplication.kt` integrate the Android/PICO lifecycle. `HeadlineScreen.kt` composes the selected news window and manages an independent settings container.

All application code is under `app/src/main/java/com/headline/news/`.

| Area | Responsibility |
| --- | --- |
| `domain/model/Headline.kt` | Article/title/URL, edition/source/region, per-mode limits and defaults. |
| `domain/usecase/` | Valid settings/source combinations and visible-count rules. |
| `data/repository/NewsFeed.kt` | Publisher endpoints, HTTPS article allowlist, RSS 2.0 / RSS 1.0 parsing, deduplication, 30-item limit. |
| `data/repository/LiveHeadlineRepository.kt` | Network on IO, five-minute per-feed disk cache, same-source fallback. |
| `data/repository/LocalSettingsRepository.kt` | Persisted preferences. |
| `ui/headlines/HeadlineViewModel.kt` | Feed lifecycle, stale-response isolation, refresh coalescing/throttling, stable displayed queue and staged updates. |
| `ui/headlines/HeadlineUiState.kt` | State delivered to presentation. |
| `ui/headlines/components/` | Overhead/Trending widgets, shared surface, ±/toolbar, settings and feed status. |
| `ui/theme/HeadlineTheme.kt` | PicoTheme color roles and bundled Noto font families. |

The full feed is state; the viewport's row count is not a data limit. Each refresh can update a pending queue without replacing the current queue under the pointer. `View latest` explicitly promotes it and returns the viewport to the top. Source changes cancel prior work and start a source-specific reading session.

`app/src/test/` covers parsing, URL pairing, caching, source changes, bounds and scroll/refresh state. Publisher XML fixtures are test resources only. `app/src/androidTest/` drives the real spatial app through Android accessibility, captures the compositor and checks live feeds/browser handoff. It does not synthesize physical controller rays.

Release keeps shrinking disabled, matching the established SDK packaging strategy. Release signing is optional for local unsigned assembly and supplied entirely through environment variables for distribution. `-PtestBuildType=release` selects instrumentation against the actual Release variant with matching signing credentials.
