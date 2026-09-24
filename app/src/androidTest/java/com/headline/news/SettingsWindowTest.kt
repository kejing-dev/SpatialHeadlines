package com.headline.news

import android.content.Intent
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SettingsWindowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private fun find(label: String): AccessibilityNodeInfo? {
        automation.clearCache()
        fun visit(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null) return null
            if (node.text?.toString() == label || node.contentDescription?.toString() == label || node.stateDescription?.toString() == label) return node
            for (i in 0 until node.childCount) visit(node.getChild(i))?.let { return it }
            return null
        }
        return visit(automation.rootInActiveWindow)
    }
    private fun awaitLabel(label: String): AccessibilityNodeInfo {
        val deadline = SystemClock.uptimeMillis() + 15000
        while (SystemClock.uptimeMillis() < deadline) {
            find(label)?.let { return it }
            SystemClock.sleep(200)
        }
        error("Missing UI label: $label")
    }
    private fun button(label: String): AccessibilityNodeInfo {
        var node = awaitLabel(label)
        while (!node.isClickable && node.isEnabled && !node.isSelected && !node.isChecked) node = node.parent ?: error("Not clickable: $label")
        return node
    }
    private fun awaitCount(limit: Int): Int {
        val deadline = SystemClock.uptimeMillis() + 15000
        while (SystemClock.uptimeMillis() < deadline) {
            for (count in 1..limit) if (find("$count / $limit") != null) return count
            SystemClock.sleep(200)
        }
        error("Missing count for mode with limit $limit")
    }
    private fun click(label: String) {
        val target = button(label)
        if (!target.isClickable && (target.isSelected || target.isChecked)) return
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "QA click: $label\n") })
        var delivered = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        repeat(2) {
            if (!delivered) { SystemClock.sleep(800); delivered = button(label).performAction(AccessibilityNodeInfo.ACTION_CLICK) }
        }
        assertTrue("Click $label", delivered)
        SystemClock.sleep(700)
    }
    private fun screenshot(name: String) {
        SystemClock.sleep(1000)
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "qa").apply { mkdirs() }
        // UiAutomation.takeScreenshot misses PICO's compositor; screencap captures the spatial room.
        ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand("screencap -p")).use { input ->
            File(folder, "$name.png").outputStream().use { input.copyTo(it) }
        }
    }
    @After fun captureEndState() {
        screenshot("test-end")
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "qa")
        fun describe(n: AccessibilityNodeInfo?, depth: Int): String {
            if (n == null) return ""
            val here = " ".repeat(depth) + "text=${n.text} description=${n.contentDescription} state=${n.stateDescription} clickable=${n.isClickable} selected=${n.isSelected} checked=${n.isChecked} enabled=${n.isEnabled} actions=${n.actionList}\n"
            return here + (0 until n.childCount).joinToString("") { describe(n.getChild(it), depth + 1) }
        }
        File(folder, "test-end-tree.txt").writeText(describe(automation.rootInActiveWindow, 0))
    }
    @Test(timeout = 90000) fun captureReviewState() {
        val context = instrumentation.targetContext
        val scene = InstrumentationRegistry.getArguments().getString("scene", "settings")
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Settings")
        click("English")
        if (find("United Kingdom") != null) { click("United Kingdom"); click("United States") }
        if (find("The Guardian") != null) { click("The Guardian"); click("BBC News") }
        click(if (scene == "trending") "Trending" else "Overhead")
        if (scene != "trending") click("Standard")
        val limit = if (scene == "trending") 6 else 3
        var count = awaitCount(limit)
        while (count < limit) {
            click("More headlines")
            count++
            awaitLabel("$count / $limit")
        }
        if (scene == "all") {
            screenshot("final-settings")
            click("Done"); awaitLabel("News list"); screenshot("final-overhead")
            click("Settings"); click("Trending")
            var rows = awaitCount(6)
            while (rows < 6) { click("More headlines"); rows++; awaitLabel("$rows / 6") }
            click("Done"); screenshot("final-trending")
            click("Settings"); click("Overhead"); click("Done")
            return
        }
        if (scene != "settings") click("Done")
        SystemClock.sleep(8000)
        screenshot("review-$scene")
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "REVIEW_READY $scene\n") })
        SystemClock.sleep(15000)
    }
    @Test(timeout = 45000) fun opensTopicInInstalledBrowser() {
        val context = instrumentation.targetContext
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Open article in browser")
        val deadline = SystemClock.uptimeMillis() + 15000
        while (SystemClock.uptimeMillis() < deadline) {
            automation.clearCache()
            if (automation.rootInActiveWindow?.packageName?.toString() == "com.picoxr.browser") {
                screenshot("browser-opened")
                return
            }
            SystemClock.sleep(300)
        }
        error("Browser did not receive focus after Open topic")
    }
    @Test(timeout = 180000) fun twoModesCountsSettingsAndRepeatedTransitions() {
        val context = instrumentation.targetContext
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Settings")
        click("Overhead")
        click("Standard")
        click("English")
        // Persisted settings may come from an earlier emulator session.
        if (find("United Kingdom") != null) { click("United Kingdom"); click("United States") }
        if (find("The Guardian") != null) { click("The Guardian"); click("BBC News") }
        click("Narrow")
        screenshot("settings-narrow")
        click("Done")
        screenshot("overhead-narrow")
        click("Settings")
        click("Wide")
        click("Done")
        screenshot("overhead-wide")
        click("Settings")
        click("Standard")
        repeat(2) { if (button("Fewer headlines").isEnabled) click("Fewer headlines") }
        awaitLabel("1 / 3")
        click("Done")
        awaitLabel("1 / 3")
        assertFalse(button("Fewer headlines").isEnabled)
        screenshot("overhead-1")
        click("More headlines")
        awaitLabel("2 / 3")
        click("More headlines")
        awaitLabel("3 / 3")
        assertFalse(button("More headlines").isEnabled)
        screenshot("overhead-3")
        click("Fewer headlines")
        awaitLabel("2 / 3")
        click("Settings")
        click("Trending")
        var trendingCount = awaitCount(6)
        while (trendingCount < 6) { click("More headlines"); trendingCount++; awaitLabel("$trendingCount / 6") }
        awaitLabel("6 / 6")
        screenshot("settings-trending")
        click("Done")
        awaitLabel("6 / 6")
        assertFalse(button("More headlines").isEnabled)
        awaitLabel("6")
        screenshot("trending-6")
        click("Fewer headlines")
        awaitLabel("5 / 6")
        assertFalse(find("6")?.isVisibleToUser == true)
        screenshot("trending-5")
        click("Settings")
        click("Overhead")
        awaitLabel("2 / 3")
        click("Trending")
        awaitLabel("5 / 6")
        click("United States")
        click("United Kingdom")
        click("BBC News")
        click("The Guardian")
        awaitLabel("The Guardian")
        // Repeated mode/window transitions exercise shared state and window lifecycle.
        repeat(4) {
            click("Overhead"); awaitLabel("2 / 3")
            click("Trending"); awaitLabel("5 / 6")
            click("Done"); awaitLabel("5 / 6")
            click("Settings"); awaitLabel("Headline settings")
        }
        click("Chinese")
        awaitLabel("BBC 中文")
        assertNull(find("News region"))
        click("BBC 中文")
        click("DW 中文")
        awaitLabel("DW 中文")
        click("English")
        click("United Kingdom")
        click("United States")
        awaitLabel("BBC News")
        click("Overhead")
        click("More headlines")
        awaitLabel("3 / 3")
        screenshot("settings-overhead")
        click("Done")
        awaitLabel("3 / 3")
        assertNull(find("Headline settings"))
        assertNull(find("Top stories · Demo data"))
        // Opening is a separate action; full headline labels have no click action.
        assertTrue(button("Open article in browser").stateDescription.toString().startsWith("https://www.bbc."))
        screenshot("final-overhead")

    }
    private fun visibleArticleUrls(): List<String> {
        val list = awaitLabel("News list")
        fun visit(n: AccessibilityNodeInfo?): List<String> {
            if (n == null) return emptyList()
            // Spatial IconButton exposes its label on a child and the URL/action on its parent.
            val own = if (n.isVisibleToUser && n.isClickable && n.stateDescription?.toString()?.startsWith("https://") == true)
                listOf(n.stateDescription.toString()) else emptyList()
            return own + (0 until n.childCount).flatMap { visit(n.getChild(it)) }
        }
        return visit(list).distinct().also { assertTrue("Visible article actions", it.isNotEmpty()) }
    }

    @Test(timeout = 150000) fun reachingEndRefreshesWithoutMovingTheList() {
        val context = instrumentation.targetContext
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Settings"); click("English"); click("Overhead"); click("Done")
        awaitLabel("News list"); awaitLabel("Refresh news")
        val settings = com.headline.news.data.repository.LocalSettingsRepository(context.getSharedPreferences("headline", 0)).load()
        val cache = File(context.filesDir, "news-feeds/${settings.source}-${settings.region}.xml")
        val fetchedBefore = cache.lastModified()
        val articles = com.headline.news.data.repository.NewsFeedParser().parse(cache.readText(),
            com.headline.news.data.repository.NewsFeed.forSettings(settings))
        val lastArticle = articles.last().articleUrl
        // No manual refresh in this process: the first arrival at the end must trigger a real fetch.
        for (attempt in 0 until 40) {
            if (lastArticle in visibleArticleUrls()) break
            assertTrue(awaitLabel("News list").performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
            // PICO's animated scroll temporarily changes its accessibility actions; wait for the viewport.
            SystemClock.sleep(1500)
        }
        val end = visibleArticleUrls()
        assertTrue("Reached the publisher's final article", lastArticle in end)
        val deadline = SystemClock.uptimeMillis() + 20000
        while (SystemClock.uptimeMillis() < deadline && cache.lastModified() <= fetchedBefore) SystemClock.sleep(150)
        assertTrue("Reaching the end fetches and saves a fresh publisher response", cache.lastModified() > fetchedBefore)
        awaitLabel("Refresh news")
        assertEquals("End refresh preserves the article", end, visibleArticleUrls())
        screenshot("v14-auto-refresh-at-end")
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "V14_END_REFRESH_QA_PASSED\n") })
    }

    @Test(timeout = 180000) fun manualScrollToolbarRefreshAndPositionRetention() {
        val context = instrumentation.targetContext
        context.startActivity(Intent().setClassName(context, "com.headline.news.platform.LaunchActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        click("Settings"); click("English"); click("Overhead"); click("Standard")
        if (find("United Kingdom") != null) { click("United Kingdom"); click("United States") }
        if (find("The Guardian") != null) { click("The Guardian"); click("BBC News") }
        var count = awaitCount(3)
        while (count < 3) { click("More headlines"); count++; awaitLabel("$count / 3") }
        awaitLabel("Manual"); awaitLabel("Refresh at end")
        screenshot("v14-settings"); click("Done")
        awaitLabel("News list"); awaitLabel("Refresh news")
        val initial = visibleArticleUrls()
        assertNull(find("SCROLL TO READ")); assertNull(find("SCROLL FOR MORE"))
        screenshot("v14-overhead-top")
        assertTrue("Manual semantic scroll", awaitLabel("News list").performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
        SystemClock.sleep(1500)
        val scrolled = visibleArticleUrls()
        assertNotEquals("Can read beyond the initial three articles", initial.first(), scrolled.first())
        screenshot("v14-overhead-scrolled")
        SystemClock.sleep(9000)
        assertEquals("No automatic scrolling", scrolled, visibleArticleUrls())
        click("Fewer headlines"); awaitLabel("2 / 3")
        assertEquals("Minus preserves current article", scrolled.first(), visibleArticleUrls().first())
        click("More headlines"); awaitLabel("3 / 3")
        assertEquals("Plus preserves current article", scrolled.first(), visibleArticleUrls().first())
        click("Refresh news"); awaitLabel("Refresh news")
        assertEquals("Refresh preserves current article", scrolled.first(), visibleArticleUrls().first())
        screenshot("v14-refreshed")
        click("Settings"); click("Done")
        assertEquals("Closing settings preserves current article", scrolled.first(), visibleArticleUrls().first())
        click("Settings"); click("Trending")
        count = awaitCount(6)
        while (count < 6) { click("More headlines"); count++; awaitLabel("$count / 6") }
        click("Done"); awaitLabel("6 / 6"); awaitLabel("Latest news")
        screenshot("v14-trending")
        val trending = visibleArticleUrls()
        assertTrue(awaitLabel("News list").performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
        SystemClock.sleep(1500)
        assertNotEquals("Trending scrolls beyond six", trending.first(), visibleArticleUrls().first())
        screenshot("v14-trending-scrolled")
        // Scroll to the end. The end-trigger must finish without repeated loading or viewport jumps.
        for (i in 0 until 20) {
            if (!awaitLabel("News list").performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) break
            SystemClock.sleep(550)
        }
        SystemClock.sleep(1500); awaitLabel("Refresh news")
        val end = visibleArticleUrls()
        SystemClock.sleep(3000); assertEquals(end, visibleArticleUrls())
        screenshot("v14-end-refreshed")
        instrumentation.sendStatus(2, android.os.Bundle().apply { putString("stream", "V14_MANUAL_SCROLL_QA_PASSED\n") })
    }

}
