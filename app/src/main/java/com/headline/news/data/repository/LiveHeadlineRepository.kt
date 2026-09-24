package com.headline.news.data.repository

import com.headline.news.domain.model.HeadlineSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Fetch off the UI thread; disk snapshots contain the unmodified publisher feed. */
class LiveHeadlineRepository(
    private val cacheDirectory: File,
    private val download: (String) -> String = ::downloadFeed,
    private val now: () -> Long = System::currentTimeMillis,
) : HeadlineRepository {
    private val parser = NewsFeedParser()
    override suspend fun headlines(settings: HeadlineSettings, forceRefresh: Boolean): FeedResult = withContext(Dispatchers.IO) {
        val feed = NewsFeed.forSettings(settings)
        val file = File(cacheDirectory, "${settings.source}-${settings.region}.xml")
        val cached = runCatching {
            val items = parser.parse(file.readText(), feed)
            require(items.isNotEmpty())
            FeedResult(items, file.lastModified())
        }.getOrNull()
        if (!forceRefresh && cached != null && now() - cached.fetchedAt in 0 until 300_000L) return@withContext cached
        try {
            val xml = download(feed.url)
            val items = parser.parse(xml, feed)
            if (items.isEmpty()) throw IOException("Feed has no article links")
            val fetchedAt = now()
            runCatching {
                cacheDirectory.mkdirs()
                val temporary = File.createTempFile("feed-", ".tmp", cacheDirectory)
                try {
                    temporary.writeText(xml)
                    if (!temporary.renameTo(file)) throw IOException("Cannot save feed")
                    file.setLastModified(fetchedAt)
                } finally { temporary.delete() }
            }
            FeedResult(items, fetchedAt)
        } catch (failure: Exception) {
            cached?.copy(offline = true) ?: throw IOException("Could not load ${feed.source}", failure)
        }
    }
}

private fun downloadFeed(address: String): String {
    var url = URL(address)
    repeat(4) {
        require(url.protocol == "https") { "HTTPS required" }
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "HeadlineNews/1.4.1 (PICO Spatial RSS reader)")
            connection.setRequestProperty("Accept", "application/rss+xml, application/rdf+xml, application/xml, text/xml")
            val status = connection.responseCode
            if (status in listOf(301, 302, 303, 307, 308)) {
                url = URL(url, connection.getHeaderField("Location") ?: throw IOException("Missing redirect"))
            } else {
                if (status != 200) throw IOException("HTTP $status")
                val bytes = connection.inputStream.use { it.readNBytes(3 * 1024 * 1024 + 1) }
                if (bytes.size > 3 * 1024 * 1024) throw IOException("Feed is too large")
                return bytes.toString(Charsets.UTF_8)
            }
        } finally { connection.disconnect() }
    }
    throw IOException("Too many redirects")
}
