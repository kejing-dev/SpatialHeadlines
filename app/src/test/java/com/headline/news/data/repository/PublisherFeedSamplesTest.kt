package com.headline.news.data.repository

import com.headline.news.domain.model.*
import org.junit.Test
import org.junit.Assert.*

class PublisherFeedSamplesTest {
    @Test fun allSixPublisherSnapshotsParseIntoDirectArticles() {
        val configurations = mapOf(
            "bbc-us" to HeadlineSettings(), "bbc-uk" to HeadlineSettings(region=NewsRegion.UnitedKingdom),
            "guardian-us" to HeadlineSettings(source=NewsSource.Guardian), "guardian-uk" to HeadlineSettings(source=NewsSource.Guardian, region=NewsRegion.UnitedKingdom),
            "bbc-chinese" to HeadlineSettings(source=NewsSource.BBCChinese), "dw-chinese" to HeadlineSettings(source=NewsSource.DWChinese),
        )
        for ((name, settings) in configurations) {
            val xml = javaClass.getResourceAsStream("/feeds/$name.xml")!!.bufferedReader().use { it.readText() }
            val feed = NewsFeed.forSettings(settings)
            val articles = NewsFeedParser().parse(xml, feed)
            assertTrue("$name count ${articles.size}", articles.size >= 6)
            assertTrue(name, articles.all { feed.acceptsArticle(it.articleUrl) && it.publishedAt > 0 })
        }
    }
}
