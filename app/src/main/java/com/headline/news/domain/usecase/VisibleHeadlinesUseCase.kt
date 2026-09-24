package com.headline.news.domain.usecase

import com.headline.news.domain.model.Headline

class VisibleHeadlinesUseCase {
    operator fun invoke(headlines: List<Headline>, count: Int, limit: Int = 3): List<Headline> = headlines.take(count.coerceIn(1, limit.coerceIn(1, 6)))
}
