package com.headline.news.data.repository

import com.headline.news.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import java.nio.file.Files

class NewsFeedTest {
    private val settings = HeadlineSettings()
    private val feed = NewsFeed.forSettings(settings)
    private val xml = """<rss><channel><item><title><![CDATA[One & two]]></title><link>https://www.bbc.com/news/articles/story1?at_medium=RSS</link><pubDate>Wed, 23 Sep 2026 11:00:09 GMT</pubDate></item><item><title>Second story</title><link>https://www.bbc.com/news/articles/story2</link></item></channel></rss>"""
    @Test fun keepsPublisherTitleAndExactArticleLinkTogether() {
        val items = NewsFeedParser().parse(xml, feed)
        assertEquals("One & two", items[0].title)
        assertEquals("https://www.bbc.com/news/articles/story1?at_medium=RSS", items[0].articleUrl)
        assertEquals("Second story", items[1].title)
        assertTrue(items[0].publishedAt > 0)
    }
    @Test fun rejectsSearchHomeExternalAndUnsafeLinks() {
        for (url in listOf("https://news.google.com/search?q=story", "https://www.bbc.com/search?q=story", "https://www.bbc.com/news", "javascript:alert(1)", "http://www.bbc.com/news/articles/story", "https://www.bbc.com.evil.example/news/articles/story")) assertFalse(url, feed.acceptsArticle(url))
    }
    @Test fun malformedAndDuplicateItemsCannotProduceWrongTargets() {
        val items = NewsFeedParser().parse(xml.replace("</channel>", "<item><title>Duplicate</title><link>https://www.bbc.com/news/articles/story1?x=1</link></item><item><title>Missing link</title></item><item><link>https://www.bbc.com/news/articles/empty</link></item></channel>"), feed)
        assertEquals(2, items.size)
    }
    @Test fun readsNamespacedRdfFeed() {
        val rdf = """<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns="http://purl.org/rss/1.0/" xmlns:dc="http://purl.org/dc/elements/1.1/"><item><title>中文标题</title><link>https://www.dw.com/zh/story/a-123456</link><dc:date>2026-09-23T10:00:00Z</dc:date></item></rdf:RDF>"""
        val item = NewsFeedParser().parse(rdf, NewsFeed.forSettings(HeadlineSettings(source=NewsSource.DWChinese))).single()
        assertEquals("中文标题", item.title); assertTrue(item.publishedAt > 0)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsExternalEntityDeclarations() {
        NewsFeedParser().parse("<!DOCTYPE rss [<!ENTITY x SYSTEM 'file:///secret'>]><rss/>", feed)
    }
    @Test fun repositoryUsesFreshCacheAndStaleOfflineFallback() = runBlocking {
        val dir = Files.createTempDirectory("headline-test").toFile()
        try {
            var calls = 0; var fail = false; var time = 2_000_000L
            val repo = LiveHeadlineRepository(dir, { calls++; if (fail) error("offline"); xml }, { time })
            val live = repo.headlines(settings); assertFalse(live.offline)
            assertEquals(live.headlines, repo.headlines(settings).headlines); assertEquals(1, calls)
            fail = true; time += 600_000
            val cached = repo.headlines(settings); assertTrue(cached.offline)
            assertEquals(live.headlines, cached.headlines); assertEquals(live.fetchedAt, cached.fetchedAt)
        } finally { dir.deleteRecursively() }
    }
    @Test fun emptyServerFeedCannotReplaceValidCache() = runBlocking {
        val dir = Files.createTempDirectory("headline-test").toFile()
        try {
            var body = xml; val repo = LiveHeadlineRepository(dir, { body })
            val initial = repo.headlines(settings); body = "<rss><channel/></rss>"
            val result = repo.headlines(settings, true)
            assertTrue(result.offline); assertEquals(initial.headlines, result.headlines)
        } finally { dir.deleteRecursively() }
    }
    @Test fun differentSourceNeverReceivesAnotherSourcesCache() = runBlocking {
        val dir = Files.createTempDirectory("headline-test").toFile()
        try {
            LiveHeadlineRepository(dir, { xml }).headlines(settings)
            val result = runCatching { LiveHeadlineRepository(dir, { error("offline") }).headlines(settings.copy(source=NewsSource.Guardian)) }
            assertTrue(result.isFailure)
        } finally { dir.deleteRecursively() }
    }
}
