package com.headline.news.domain.usecase
import com.headline.news.domain.model.*
import org.junit.Assert.*
import org.junit.Test
class ConfigureFeedUseCaseTest {
    private val configure = ConfigureFeedUseCase()
    @Test fun repairsSourceFromAnotherEdition() { assertEquals(NewsSource.BBCChinese, configure(HeadlineSettings(edition=ContentEdition.Chinese)).source) }
    @Test fun switchingRegionKeepsSupportedPublisher() { assertEquals(NewsSource.Guardian, configure.region(HeadlineSettings(source=NewsSource.Guardian), NewsRegion.UnitedKingdom).source) }
    @Test fun switchingEditionPreservesLayout() {
        val result = configure.edition(HeadlineSettings(2, WidgetWidth.Wide), ContentEdition.Chinese)
        assertEquals(2, result.count); assertEquals(WidgetWidth.Wide, result.width)
        assertEquals(listOf(NewsSource.BBCChinese, NewsSource.DWChinese), configure.sources(result))
    }
}
