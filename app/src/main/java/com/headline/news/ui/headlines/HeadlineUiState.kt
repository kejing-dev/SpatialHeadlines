package com.headline.news.ui.headlines

import com.headline.news.domain.model.*

data class HeadlineUiState(
    val headlines: List<Headline> = emptyList(),
    val settings: HeadlineSettings = HeadlineSettings(),
    val sources: List<NewsSource> = listOf(NewsSource.BBC),
    val message: String? = null,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val offline: Boolean = false,
    val fetchedAt: Long = 0L,
    val pendingHeadlineCount: Int = 0,
    val scrollGeneration: Int = 0,
    val refreshStatus: String? = null,
)

sealed interface HeadlineEvent {
    data class SetMode(val mode: DisplayMode) : HeadlineEvent
    data class AdjustCount(val delta: Int) : HeadlineEvent
    data class SetCount(val count: Int) : HeadlineEvent
    data class SetWidth(val width: WidgetWidth) : HeadlineEvent
    data class SetEdition(val edition: ContentEdition) : HeadlineEvent
    data class SetRegion(val region: NewsRegion) : HeadlineEvent
    data class SetSource(val source: NewsSource) : HeadlineEvent
    data object Refresh : HeadlineEvent
    data object ReachedEnd : HeadlineEvent
    data object ViewLatest : HeadlineEvent
    data class SetRefreshAtEnd(val enabled: Boolean) : HeadlineEvent
    data object Reset : HeadlineEvent
    data object BrowserUnavailable : HeadlineEvent
    data object DismissMessage : HeadlineEvent
}
