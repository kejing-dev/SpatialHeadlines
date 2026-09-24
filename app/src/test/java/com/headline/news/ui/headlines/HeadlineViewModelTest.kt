package com.headline.news.ui.headlines

import com.headline.news.data.repository.*
import com.headline.news.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class HeadlineViewModelTest {
    @Before fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun teardown() { Dispatchers.resetMain() }
    private class MemorySettings(var value: HeadlineSettings = HeadlineSettings()) : SettingsRepository {
        override fun load() = value
        override fun save(settings: HeadlineSettings) { value = settings }
    }
    private class FixtureRepository : HeadlineRepository {
        var calls = 0
        var fail = false
        override suspend fun headlines(settings: HeadlineSettings, forceRefresh: Boolean): FeedResult {
            calls++
            if (fail) error("Offline")
            return FeedResult((1..6).map { Headline("${settings.source}-$it", "Article $it", settings.source.label,
                "https://www.bbc.com/news/articles/test$it", 1000L) }, 2000L)
        }
    }
    @Test fun modesRememberCountsAcrossRelaunch() {
        val store = MemorySettings(); val vm = HeadlineViewModel(FixtureRepository(), store)
        vm.onEvent(HeadlineEvent.SetCount(1)); vm.onEvent(HeadlineEvent.SetMode(DisplayMode.Trending))
        assertEquals(6, vm.state.value.headlines.size)
        vm.onEvent(HeadlineEvent.AdjustCount(-1)); vm.onEvent(HeadlineEvent.SetMode(DisplayMode.Overhead))
        assertEquals(1, vm.state.value.settings.visibleCount)
        vm.onEvent(HeadlineEvent.SetMode(DisplayMode.Trending))
        assertEquals(5, HeadlineViewModel(FixtureRepository(), store).state.value.settings.visibleCount)
    }
    @Test fun rapidAdjustmentsAndLayoutChangesDoNotRefetchOrReorder() {
        val repo = FixtureRepository(); val vm = HeadlineViewModel(repo, MemorySettings())
        val first = vm.state.value.headlines.first()
        for (mode in DisplayMode.entries) {
            vm.onEvent(HeadlineEvent.SetMode(mode))
            repeat(100) { vm.onEvent(HeadlineEvent.AdjustCount(1)) }
            assertEquals(mode.limit, vm.state.value.settings.visibleCount)
            repeat(100) { vm.onEvent(HeadlineEvent.AdjustCount(-1)) }
            assertEquals(first, vm.state.value.headlines.first()); assertEquals(6, vm.state.value.headlines.size)
        }
        vm.onEvent(HeadlineEvent.SetWidth(WidgetWidth.Wide))
        assertEquals(1, repo.calls)
    }
    @Test fun corruptCountsAreNormalized() {
        val vm = HeadlineViewModel(FixtureRepository(), MemorySettings(HeadlineSettings(count=-3, trendingCount=100, mode=DisplayMode.Trending)))
        assertEquals(6, vm.state.value.headlines.size)
        vm.onEvent(HeadlineEvent.SetMode(DisplayMode.Overhead)); assertEquals(1, vm.state.value.settings.visibleCount)
    }
    @Test fun feedFailureHasRetryAndNoInventedHeadlines() {
        val repo = FixtureRepository().apply { fail = true }; val vm = HeadlineViewModel(repo, MemorySettings())
        assertTrue(vm.state.value.headlines.isEmpty()); assertNotNull(vm.state.value.loadError); assertFalse(vm.state.value.isLoading)
        repo.fail = false; vm.onEvent(HeadlineEvent.Refresh)
        assertEquals(6, vm.state.value.headlines.size); assertNull(vm.state.value.loadError)
    }
    @Test fun failedRefreshPreservesArticleUrlAndTitle() {
        val repo = FixtureRepository(); val vm = HeadlineViewModel(repo, MemorySettings()); val before = vm.state.value.headlines
        repo.fail = true; vm.onEvent(HeadlineEvent.Refresh)
        assertEquals(before, vm.state.value.headlines); assertTrue(vm.state.value.offline)
    }
    @Test fun changingSourceFetchesAndClearsOldSourceOnFailure() {
        val repo = FixtureRepository(); val vm = HeadlineViewModel(repo, MemorySettings())
        repo.fail = true; vm.onEvent(HeadlineEvent.SetSource(NewsSource.Guardian))
        assertTrue(vm.state.value.headlines.isEmpty()); assertEquals(2, repo.calls)
        repo.fail = false; vm.onEvent(HeadlineEvent.Refresh)
        assertTrue(vm.state.value.headlines.all { it.source == "The Guardian" })
    }
    @Test fun slowerPreviousSourceCannotOverwriteNewSource() = runTest {
        val old = CompletableDeferred<FeedResult>(); val newer = CompletableDeferred<FeedResult>()
        val repository = object : HeadlineRepository {
            override suspend fun headlines(settings: HeadlineSettings, forceRefresh: Boolean): FeedResult =
                withContext(NonCancellable) { if (settings.source == NewsSource.BBC) old.await() else newer.await() }
        }
        val vm = HeadlineViewModel(repository, MemorySettings())
        vm.onEvent(HeadlineEvent.SetSource(NewsSource.Guardian))
        newer.complete(FeedResult(listOf(Headline("new", "New source", "The Guardian", "https://www.theguardian.com/world/2026/sep/24/story")), 1))
        old.complete(FeedResult(listOf(Headline("old", "Old source", "BBC News", "https://www.bbc.com/news/articles/old")), 1))
        assertEquals("new", vm.state.value.headlines.single().id)
    }
    @Test fun editionAndRegionUseValidSources() {
        val store = MemorySettings(); val vm = HeadlineViewModel(FixtureRepository(), store)
        vm.onEvent(HeadlineEvent.SetEdition(ContentEdition.Chinese)); assertEquals(NewsSource.BBCChinese, store.value.source)
        vm.onEvent(HeadlineEvent.SetSource(NewsSource.DWChinese)); assertEquals("DW 中文", vm.state.value.headlines.first().source)
        vm.onEvent(HeadlineEvent.SetEdition(ContentEdition.Overseas)); assertEquals(NewsSource.BBC, store.value.source)
        vm.onEvent(HeadlineEvent.SetSource(NewsSource.Guardian)); vm.onEvent(HeadlineEvent.SetRegion(NewsRegion.UnitedKingdom))
        assertEquals(NewsSource.Guardian, store.value.source)
    }
    @Test fun browserFeedbackIsDismissible() {
        val vm = HeadlineViewModel(FixtureRepository(), MemorySettings())
        vm.onEvent(HeadlineEvent.BrowserUnavailable); assertNotNull(vm.state.value.message)
        vm.onEvent(HeadlineEvent.DismissMessage); assertNull(vm.state.value.message)
    }
}
