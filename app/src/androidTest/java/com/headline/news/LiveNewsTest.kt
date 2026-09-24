package com.headline.news

import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.headline.news.data.repository.*
import com.headline.news.domain.model.*
import com.headline.news.domain.usecase.ConfigureFeedUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LiveNewsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val automation get() = instrumentation.uiAutomation
    private fun find(label: String): AccessibilityNodeInfo? {
        automation.clearCache()
        fun visit(n: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (n == null) return null
            if (n.text?.toString() == label || n.contentDescription?.toString() == label) return n
            for (i in 0 until n.childCount) visit(n.getChild(i))?.let { return it }
            return null
        }
        return visit(automation.rootInActiveWindow)
    }
    private fun awaitLabel(label: String): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 40000
        while (SystemClock.uptimeMillis() < deadline) { find(label)?.let { return it }; SystemClock.sleep(250) }
        error("Missing $label")
    }
    private fun shell(command: String): String = ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).bufferedReader().use { it.readText() }
    private fun screenshot(name: String) {
        val folder = File(context.getExternalFilesDir(null), "qa-live").apply { mkdirs() }
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("screencap -p")).use { input -> File(folder, "$name.png").outputStream().use { input.copyTo(it) } }
    }
    @Test(timeout = 150000) fun allSourcesReturnRealPublisherArticles() = runBlocking {
        // Exercise networking in the app's normal foreground lifecycle, as on the headset.
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        awaitLabel("Settings")
        val repository = LiveHeadlineRepository(File(context.cacheDir, "qa-feeds"))
        val configs = listOf(
            HeadlineSettings(), HeadlineSettings(region=NewsRegion.UnitedKingdom),
            HeadlineSettings(source=NewsSource.Guardian), HeadlineSettings(source=NewsSource.Guardian, region=NewsRegion.UnitedKingdom),
            HeadlineSettings(edition=ContentEdition.Chinese, source=NewsSource.BBCChinese),
            HeadlineSettings(edition=ContentEdition.Chinese, source=NewsSource.DWChinese),
        )
        for (settings in configs) {
            val feed = NewsFeed.forSettings(settings)
            val result = repository.headlines(settings, true)
            assertFalse("Must fetch live: ${feed.url}", result.offline)
            assertTrue("At least six articles: ${feed.url}", result.headlines.size >= 6)
            assertTrue(result.headlines.all { feed.acceptsArticle(it.articleUrl) && it.title.isNotBlank() && it.publishedAt > 0 })
            instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "LIVE_FEED ${feed.url}: ${result.headlines.size} direct articles\n") })
        }
    }
    @Test(timeout = 90000) fun visibleHeadlineOpensItsExactArticle() = runBlocking {
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        var button = awaitLabel("Open article in browser")
        while (!button.isClickable) button = button.parent ?: error("Article action has no clickable parent")
        val url = button.stateDescription.toString()
        val settings = ConfigureFeedUseCase()(LocalSettingsRepository(context.getSharedPreferences("headline", 0)).load())
        val result = LiveHeadlineRepository(File(context.filesDir, "news-feeds")).headlines(settings)
        val article = result.headlines.first { it.articleUrl == url }
        assertNotNull("Visible title belongs to link", awaitLabel(article.title))
        assertTrue(NewsFeed.forSettings(settings).acceptsArticle(url))
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "ARTICLE_TITLE ${article.title}\nARTICLE_URL $url\n") })
        SystemClock.sleep(5000); screenshot("headline")
        assertTrue(button.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        val deadline = SystemClock.uptimeMillis() + 20000
        while (SystemClock.uptimeMillis() < deadline) {
            automation.clearCache()
            if (automation.rootInActiveWindow?.packageName?.toString() == "com.picoxr.browser") break
            SystemClock.sleep(250)
        }
        assertEquals("com.picoxr.browser", automation.rootInActiveWindow?.packageName?.toString())
        val activities = shell("dumpsys activity activities")
        assertTrue("Browser receives the exact displayed article URL", activities.contains(url))
        File(context.getExternalFilesDir(null), "qa-live/article-target.txt").writeText("${article.title}\n$url\n")
        SystemClock.sleep(10000); screenshot("article-browser")
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "ARTICLE_BROWSER_READY\n") })
        SystemClock.sleep(10000)
    }
}
