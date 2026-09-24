package com.headline.news.ui.headlines

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.*
import com.headline.news.ui.headlines.components.NewsToolbar
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.headline.news.data.repository.LiveHeadlineRepository
import com.headline.news.ui.headlines.components.FeedStatusPanel
import com.headline.news.data.repository.LocalSettingsRepository
import com.headline.news.domain.model.Headline
import com.headline.news.domain.model.DisplayMode
import com.headline.news.ui.headlines.components.HeadlineWidget
import com.headline.news.ui.headlines.components.SettingsPanel
import com.headline.news.ui.headlines.components.TrendingWidget
import com.pico.spatial.ui.platform.containers.LocalSpatialNavigator
import com.pico.spatial.ui.platform.containers.WindowContainerScope

@Composable
private fun sharedHeadlineViewModel(): HeadlineViewModel {
    val application = LocalContext.current.applicationContext
    val factory = remember(application) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HeadlineViewModel(
                LiveHeadlineRepository(java.io.File(application.filesDir, "news-feeds")),
                LocalSettingsRepository(application.getSharedPreferences("headline", Context.MODE_PRIVATE)),
            ) as T
        }
    }
    return viewModel(viewModelStoreOwner = application as ViewModelStoreOwner, factory = factory)
}

@Composable
fun WindowContainerScope.HeadlineScreen() {
    val context = LocalContext.current
    val navigator = LocalSpatialNavigator.current
    val vm = sharedHeadlineViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val browserAvailable = remember(context) {
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bbc.com")).resolveActivity(context.packageManager) != null
    }
    HeadlineContent(
        onRefresh = { vm.onEvent(HeadlineEvent.Refresh) },
        onReachedEnd = { vm.onEvent(HeadlineEvent.ReachedEnd) },
        onViewLatest = { vm.onEvent(HeadlineEvent.ViewLatest) },
        browserAvailable = browserAvailable,
        state = state,
        onOpenSettings = { navigator.openWindowContainer("headline-settings", tag = "settings") },
        onAdjust = { vm.onEvent(HeadlineEvent.AdjustCount(it)) },
        onOpenTopic = { headline ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(headline.articleUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) {
                vm.onEvent(HeadlineEvent.BrowserUnavailable)
                android.widget.Toast.makeText(context, vm.state.value.message, android.widget.Toast.LENGTH_LONG).show()
            }
        },
        modifier = Modifier.windowConstraints(
            width = (if (state.settings.mode == DisplayMode.Trending) 480 else state.settings.width.dp).dp,
            height = (newsPanelHeight(state) + 24 + 72 + 48).dp,
        ),
    )
}

private fun newsPanelHeight(state: HeadlineUiState): Int {
    if (state.headlines.isEmpty()) return 180
    val rows = minOf(state.settings.visibleCount, state.headlines.size).coerceAtLeast(1)
    return if (state.settings.mode == DisplayMode.Trending) 72 + 88 * rows else 16 + 64 * rows
}

@Composable
internal fun HeadlineContent(
    state: HeadlineUiState,
    browserAvailable: Boolean,
    onRefresh: () -> Unit,
    onReachedEnd: () -> Unit,
    onViewLatest: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTopic: (Headline) -> Unit,
    onAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentReachedEnd by rememberUpdatedState(onReachedEnd)
    val currentState by rememberUpdatedState(state)
    LaunchedEffect(state.scrollGeneration) { listState.scrollToItem(0) }
    LaunchedEffect(listState, state.scrollGeneration) {
        var consumedEnd = false
        snapshotFlow { Triple(listState.isScrollInProgress, listState.canScrollForward, listState.canScrollBackward) }
            .distinctUntilChanged().collect { (scrolling, forward, backward) ->
                if (forward) consumedEnd = false
                if (scrolling && !forward && backward && !consumedEnd &&
                    currentState.headlines.size > currentState.settings.visibleCount) {
                    consumedEnd = true
                    currentReachedEnd()
                }
            }
    }
    Column(modifier, horizontalAlignment = Alignment.End) {
        val panel = Modifier.fillMaxWidth().height(newsPanelHeight(state).dp)
        if (state.headlines.isEmpty()) FeedStatusPanel(state.isLoading, state.loadError, onRefresh, panel)
        else if (state.settings.mode == DisplayMode.Trending)
            TrendingWidget(state.headlines, listState, onOpenTopic, browserAvailable, panel)
        else HeadlineWidget(state.headlines, listState, onOpenTopic, browserAvailable, panel)
        Spacer(Modifier.height(24.dp))
        NewsToolbar(state.settings.visibleCount, state.settings.mode.limit, state.isLoading, onAdjust, onOpenSettings, onRefresh)
        // Feedback has its own reserved area; no scroll hint or article-count footer (Figma comment #8).
        Box(Modifier.height(48.dp).widthIn(max = 480.dp), contentAlignment = Alignment.CenterEnd) {
            if (state.pendingHeadlineCount > 0) Button(onClick = onViewLatest, size = ButtonDefaults.Small) {
                // design-style: inherited-content-color Button
                Text("${state.pendingHeadlineCount} new stories · View latest", fontSize = 15.sp)
            } else {
                var statusVisible by remember { mutableStateOf(false) }
                LaunchedEffect(state.refreshStatus, state.isLoading) {
                    statusVisible = state.refreshStatus != null
                    if (!state.offline) { kotlinx.coroutines.delay(5000); statusVisible = false }
                }
                if (statusVisible) Text(state.refreshStatus.orEmpty(), color = PicoTheme.colorScheme.labelSecondary,
                    fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp), maxLines = 2)
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val vm = sharedHeadlineViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val navigator = LocalSpatialNavigator.current
    SettingsPanel(
        settings = state.settings,
        onRefreshAtEndChange = { vm.onEvent(HeadlineEvent.SetRefreshAtEnd(it)) },
        isLoading = state.isLoading,
        feedStatus = when {
            state.isLoading -> "Loading news…"
            state.loadError != null -> state.loadError!!
            state.offline -> "Offline · saved articles"
            state.fetchedAt > 0 -> "Updated " + java.time.Instant.ofEpochMilli(state.fetchedAt).atZone(java.time.ZoneId.systemDefault()).toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            else -> "Not updated yet"
        },
        onRefresh = { vm.onEvent(HeadlineEvent.Refresh) },
        sources = state.sources,
        onAdjustCount = { vm.onEvent(HeadlineEvent.AdjustCount(it)) },
        onModeChange = { vm.onEvent(HeadlineEvent.SetMode(it)) },
        onWidthChange = { vm.onEvent(HeadlineEvent.SetWidth(it)) },
        onEditionChange = { vm.onEvent(HeadlineEvent.SetEdition(it)) },
        onRegionChange = { vm.onEvent(HeadlineEvent.SetRegion(it)) },
        onSourceChange = { vm.onEvent(HeadlineEvent.SetSource(it)) },
        onDismiss = { navigator.closeWindowContainer() },
        modifier = modifier,
    )
}
