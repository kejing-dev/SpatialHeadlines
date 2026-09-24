package com.headline.news.ui.headlines

import com.headline.news.data.repository.*
import com.headline.news.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ScrollFeedTest {
    @Before fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun cleanup() { Dispatchers.resetMain() }
    private class Store : SettingsRepository {
        var settings = HeadlineSettings()
        override fun load() = settings
        override fun save(settings: HeadlineSettings) { this.settings = settings }
    }
    private fun article(i: Int) = Headline("id-$i", "Publisher headline $i", "BBC", "https://www.bbc.com/news/articles/$i")
    private inner class Feed : HeadlineRepository {
        var articles = (1..24).map(::article)
        var calls = 0
        var fail = false
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun headlines(settings: HeadlineSettings, forceRefresh: Boolean): FeedResult {
            calls++; gate?.await(); if (fail) error("network")
            return FeedResult(articles, 1234L)
        }
    }
    @Test fun allArticlesRemainScrollableRegardlessOfVisibleCount() {
        val feed=Feed(); val vm=HeadlineViewModel(feed, Store()); val original=vm.state.value.headlines
        for (mode in DisplayMode.entries) {
            vm.onEvent(HeadlineEvent.SetMode(mode))
            vm.onEvent(HeadlineEvent.SetCount(1))
            assertEquals(24, vm.state.value.headlines.size)
            assertEquals(original, vm.state.value.headlines)
        }
        assertEquals(1, feed.calls)
    }
    @Test fun refreshStagesNewStoriesUntilExplicitActionAndKeepsExactLinks() {
        val feed=Feed(); val vm=HeadlineViewModel(feed, Store()); val original=vm.state.value.headlines
        feed.articles=listOf(article(25),article(26))+original.reversed()
        vm.onEvent(HeadlineEvent.Refresh)
        assertEquals(original,vm.state.value.headlines); assertEquals(2,vm.state.value.pendingHeadlineCount)
        assertEquals(0,vm.state.value.scrollGeneration)
        vm.onEvent(HeadlineEvent.ViewLatest)
        assertEquals(feed.articles,vm.state.value.headlines); assertEquals(0,vm.state.value.pendingHeadlineCount)
        assertEquals(1,vm.state.value.scrollGeneration)
        assertEquals(article(25).articleUrl,vm.state.value.headlines.first().articleUrl)
    }
    @Test fun reachingEndRefreshesOnceAndRespectsRateLimitAndSavedToggle() {
        var now=0L; val feed=Feed(); val store=Store(); val vm=HeadlineViewModel(feed,store){now}
        vm.onEvent(HeadlineEvent.ReachedEnd); assertEquals(2,feed.calls)
        repeat(100) { vm.onEvent(HeadlineEvent.ReachedEnd) }; assertEquals(2,feed.calls)
        now=60_000; vm.onEvent(HeadlineEvent.ReachedEnd); assertEquals(3,feed.calls)
        vm.onEvent(HeadlineEvent.SetRefreshAtEnd(false)); assertFalse(store.settings.refreshAtEnd)
        now=120_000; vm.onEvent(HeadlineEvent.ReachedEnd); assertEquals(3,feed.calls)
        vm.onEvent(HeadlineEvent.Refresh); assertEquals(4,feed.calls)
    }
    @Test fun repeatedRefreshWhileFetchingDoesNotCancelOrDuplicateTheRequest() {
        val feed=Feed(); val vm=HeadlineViewModel(feed,Store())
        val gate=CompletableDeferred<Unit>(); feed.gate=gate
        vm.onEvent(HeadlineEvent.Refresh)
        repeat(20){vm.onEvent(HeadlineEvent.Refresh);vm.onEvent(HeadlineEvent.ReachedEnd)}
        assertEquals(2,feed.calls); assertTrue(vm.state.value.isLoading)
        gate.complete(Unit); assertFalse(vm.state.value.isLoading)
    }
    @Test fun sourceSwitchDiscardsPendingOldSourceAndResetsScroll() {
        val feed=Feed(); val vm=HeadlineViewModel(feed,Store())
        feed.articles=listOf(article(30)); vm.onEvent(HeadlineEvent.Refresh)
        assertEquals(1,vm.state.value.pendingHeadlineCount)
        feed.articles=listOf(article(90)); vm.onEvent(HeadlineEvent.SetSource(NewsSource.Guardian))
        assertEquals(listOf(article(90)),vm.state.value.headlines); assertEquals(0,vm.state.value.pendingHeadlineCount)
        vm.onEvent(HeadlineEvent.ViewLatest); assertEquals(listOf(article(90)),vm.state.value.headlines)
        assertEquals(1,vm.state.value.scrollGeneration)
    }
    @Test fun failureRetainsTheReadingSnapshotAndAlreadyReceivedNewArticles() {
        val feed=Feed(); val vm=HeadlineViewModel(feed,Store()); val original=vm.state.value.headlines
        feed.articles=listOf(article(30))+original; vm.onEvent(HeadlineEvent.Refresh)
        feed.fail=true; vm.onEvent(HeadlineEvent.Refresh)
        assertEquals(original,vm.state.value.headlines); assertEquals(1,vm.state.value.pendingHeadlineCount)
        assertTrue(vm.state.value.offline)
        vm.onEvent(HeadlineEvent.ViewLatest); assertEquals(article(30),vm.state.value.headlines.first())
    }
}
