package com.headline.news.data.repository

import com.headline.news.domain.model.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URI
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory

data class NewsFeed(val url: String, val source: String, val hosts: Set<String>) {
    fun acceptsArticle(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host in hosts && uri.userInfo == null &&
            uri.path.length > 10 && !uri.path.contains("search", ignoreCase = true) &&
            when (uri.host) {
                "www.bbc.com", "www.bbc.co.uk" -> uri.path.contains("/articles/") || Regex("/news/[^/]+-\\d+$").containsMatchIn(uri.path)
                "www.theguardian.com" -> Regex("/\\d{4}/[a-z]{3}/\\d{2}/").containsMatchIn(uri.path)
                "www.dw.com" -> Regex("/a-\\d+$").containsMatchIn(uri.path)
                else -> false
            }
    }.getOrDefault(false)

    companion object {
        fun forSettings(settings: HeadlineSettings): NewsFeed = when (settings.source) {
            NewsSource.BBC -> NewsFeed("https://feeds.bbci.co.uk/news/${if (settings.region == NewsRegion.UnitedStates) "world/us_and_canada" else "uk"}/rss.xml", "BBC News", setOf("www.bbc.co.uk", "www.bbc.com"))
            NewsSource.Guardian -> NewsFeed("https://www.theguardian.com/${if (settings.region == NewsRegion.UnitedStates) "us-news" else "uk-news"}/rss", "The Guardian", setOf("www.theguardian.com"))
            NewsSource.BBCChinese -> NewsFeed("https://feeds.bbci.co.uk/zhongwen/simp/rss.xml", "BBC 中文", setOf("www.bbc.co.uk", "www.bbc.com"))
            NewsSource.DWChinese -> NewsFeed("https://rss.dw.com/rdf/rss-chi-all", "DW 中文", setOf("www.dw.com"))
        }
    }
}

/** RSS 2.0 and namespace-aware RSS 1.0. Only publisher article URLs survive parsing. */
class NewsFeedParser {
    fun parse(xml: String, feed: NewsFeed): List<Headline> {
        require(!xml.contains("<!DOCTYPE", ignoreCase = true) && !xml.contains("<!ENTITY", ignoreCase = true)) { "DTD is not supported" }
        val builder = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            isExpandEntityReferences = false
        }.newDocumentBuilder()
        builder.setEntityResolver { _, _ -> InputSource(StringReader("")) }
        val document = builder.parse(InputSource(StringReader(xml)))
        val items = document.getElementsByTagNameNS("*", "item")
        return (0 until items.length).mapNotNull { index ->
            val item = items.item(index) as Element
            fun field(name: String): String {
                val nodes = item.childNodes
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i)
                    if ((node.localName ?: node.nodeName) == name) return node.textContent.trim()
                }
                return ""
            }
            val title = field("title").replace(Regex("\\s+"), " ").trim()
            val url = field("link")
            if (title.isBlank() || !feed.acceptsArticle(url)) return@mapNotNull null
            val date = field("pubDate").ifBlank { field("date") }
            val published = runCatching { ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }
                .getOrElse { runCatching { Instant.parse(date).toEpochMilli() }.getOrDefault(0L) }
            Headline(url.substringBefore('?'), title, feed.source, url, published)
        }.distinctBy { it.id }.take(30)
    }
}
