package com.headline.news.domain.usecase
import com.headline.news.domain.model.Headline
import org.junit.Assert.*
import org.junit.Test
class VisibleHeadlinesUseCaseTest {
    private val select = VisibleHeadlinesUseCase()
    private val news = (1..6).map { Headline("$it", "Story $it", "Publisher", "https://publisher.example/articles/$it") }
    @Test fun preservesTitleUrlPairsAndOrder() { assertEquals(news.take(2), select(news, 2)) }
    @Test fun boundsCountToSupportedRange() { assertEquals(1, select(news, -1).size); assertEquals(3, select(news, 100).size) }
    @Test fun neverInventsNewsToFillRequestedCount() { assertTrue(select(emptyList(), 3).isEmpty()); assertEquals(2, select(news.take(2), 6, 6).size) }
}
