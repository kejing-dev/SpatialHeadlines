package com.headline.news.domain.usecase

import com.headline.news.domain.model.*

/** Keep a source valid for its edition and reset to the regional default on region changes. */
class ConfigureFeedUseCase {
    fun sources(settings: HeadlineSettings): List<NewsSource> = when {
        settings.edition == ContentEdition.Chinese -> listOf(NewsSource.BBCChinese, NewsSource.DWChinese)
        else -> listOf(NewsSource.BBC, NewsSource.Guardian)
    }
    operator fun invoke(settings: HeadlineSettings): HeadlineSettings {
        val available = sources(settings)
        return settings.copy(count = settings.count.coerceIn(1, 3), trendingCount = settings.trendingCount.coerceIn(1, 6), source = settings.source.takeIf { it in available } ?: available.first())
    }
    fun edition(settings: HeadlineSettings, edition: ContentEdition) = invoke(settings.copy(
        edition = edition, source = if (edition == ContentEdition.Chinese) NewsSource.BBCChinese else NewsSource.BBC,
    ))
    fun region(settings: HeadlineSettings, region: NewsRegion) = invoke(settings.copy(region = region))
}
