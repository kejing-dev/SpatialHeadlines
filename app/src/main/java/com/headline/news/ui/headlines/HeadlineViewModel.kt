package com.headline.news.ui.headlines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.headline.news.data.repository.HeadlineRepository
import com.headline.news.data.repository.SettingsRepository
import com.headline.news.domain.model.*
import com.headline.news.domain.usecase.ConfigureFeedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HeadlineViewModel(
    private val repository: HeadlineRepository,
    private val settingsRepository: SettingsRepository,
    private val elapsedMillis: () -> Long = { System.nanoTime() / 1_000_000 },
) : ViewModel() {
    private val configure = ConfigureFeedUseCase()
    private val initialSettings = configure(settingsRepository.load())
    private val mutableState = MutableStateFlow(HeadlineUiState(settings = initialSettings, sources = configure.sources(initialSettings)))
    val state = mutableState.asStateFlow()
    private var pendingArticles: List<Headline>? = null
    private var loadJob: Job? = null
    private var requestId = 0
    private var lastRefreshAttempt: Long? = null

    init { refresh(false) }

    fun onEvent(event: HeadlineEvent) {
        when (event) {
            is HeadlineEvent.SetCount -> save(state.value.settings.withVisibleCount(event.count))
            is HeadlineEvent.AdjustCount -> save(state.value.settings.withVisibleCount(state.value.settings.visibleCount + event.delta.coerceIn(-1, 1)))
            is HeadlineEvent.SetMode -> save(state.value.settings.copy(mode = event.mode))
            is HeadlineEvent.SetWidth -> save(state.value.settings.copy(width = event.width))
            is HeadlineEvent.SetEdition -> save(configure.edition(state.value.settings, event.edition))
            is HeadlineEvent.SetRegion -> save(configure.region(state.value.settings, event.region))
            is HeadlineEvent.SetSource -> save(configure(state.value.settings.copy(source = event.source)))
            is HeadlineEvent.SetRefreshAtEnd -> save(state.value.settings.copy(refreshAtEnd = event.enabled))
            HeadlineEvent.Refresh -> if (!state.value.isLoading) refresh(true)
            HeadlineEvent.ReachedEnd -> {
                val now = elapsedMillis()
                if (state.value.settings.refreshAtEnd && !state.value.isLoading &&
                    (lastRefreshAttempt == null || now - lastRefreshAttempt!! >= 60_000)) refresh(true)
            }
            HeadlineEvent.ViewLatest -> pendingArticles?.let { latest ->
                mutableState.update { it.copy(headlines = latest, pendingHeadlineCount = 0,
                    scrollGeneration = it.scrollGeneration + 1, refreshStatus = null) }
                pendingArticles = null
            }
            HeadlineEvent.Reset -> save(HeadlineSettings())
            HeadlineEvent.BrowserUnavailable -> mutableState.update { it.copy(message = "No browser is available. Install a browser to open this article.") }
            HeadlineEvent.DismissMessage -> mutableState.update { it.copy(message = null) }
        }
    }

    private fun save(settings: HeadlineSettings) {
        val previous = state.value.settings
        val changedFeed = settings.source != previous.source || settings.region != previous.region || settings.edition != previous.edition
        settingsRepository.save(settings)
        if (changedFeed) {
            pendingArticles = null
            lastRefreshAttempt = null
        }
        mutableState.update {
            it.copy(settings = settings, sources = configure.sources(settings),
                headlines = if (changedFeed) emptyList() else it.headlines,
                pendingHeadlineCount = if (changedFeed) 0 else it.pendingHeadlineCount,
                scrollGeneration = it.scrollGeneration + if (changedFeed) 1 else 0,
                offline = if (changedFeed) false else it.offline,
                fetchedAt = if (changedFeed) 0 else it.fetchedAt,
                refreshStatus = if (changedFeed) null else it.refreshStatus)
        }
        if (changedFeed) refresh(false)
    }

    private fun refresh(force: Boolean) {
        loadJob?.cancel()
        val id = ++requestId
        val requested = state.value.settings
        if (force) lastRefreshAttempt = elapsedMillis()
        mutableState.update { it.copy(isLoading = true, loadError = null, refreshStatus = null) }
        loadJob = viewModelScope.launch {
            try {
                val result = repository.headlines(requested, force)
                if (id != requestId) return@launch
                val latest = result.headlines.distinctBy { it.id }
                val current = state.value.headlines
                val newCount = latest.count { article -> current.none { it.id == article.id } }
                // Keep the complete, stable reading snapshot until the user explicitly asks for new stories.
                // This also protects the item under a controller ray during a background request.
                if (!result.offline && current.isNotEmpty()) pendingArticles = latest.takeIf { newCount > 0 }
                mutableState.update {
                    it.copy(headlines = if (current.isEmpty()) latest else current,
                        isLoading = false, offline = result.offline, fetchedAt = result.fetchedAt,
                        pendingHeadlineCount = if (current.isEmpty()) 0 else if (result.offline) it.pendingHeadlineCount else newCount,
                        loadError = if (latest.isEmpty() && current.isEmpty()) "No articles available from this source." else null,
                        refreshStatus = when {
                            result.offline -> "Couldn't refresh · Showing saved stories"
                            force && newCount == 0 -> "You're up to date"
                            force && current.isNotEmpty() -> "Updated just now"
                            else -> null
                        })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (id == requestId) mutableState.update {
                    it.copy(isLoading = false, offline = it.headlines.isNotEmpty(),
                        loadError = if (it.headlines.isEmpty()) "Unable to load news. Check your connection and retry." else null,
                        refreshStatus = if (it.headlines.isNotEmpty()) "Couldn't refresh · Showing saved stories" else null)
                }
            }
        }
    }
}
