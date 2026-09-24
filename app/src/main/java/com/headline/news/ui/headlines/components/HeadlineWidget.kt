package com.headline.news.ui.headlines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headline.news.domain.model.Headline
import com.headline.news.ui.theme.HeadlineColors
import com.headline.news.ui.theme.headlineFont
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.hover.*

@Composable
fun HeadlineWidget(headlines: List<Headline>, listState: LazyListState, onOpenTopic: (Headline) -> Unit,
    browserAvailable: Boolean, modifier: Modifier = Modifier) {
    val group = remember { SpatialHoverEffectGroup.obtain() }
    WidgetSurface(group, modifier) {
        ArticleList(headlines, listState, onOpenTopic, browserAvailable, trending = false,
            modifier = Modifier.fillMaxSize().padding(8.dp))
    }
}

@Composable
internal fun ArticleList(headlines: List<Headline>, listState: LazyListState, onOpenTopic: (Headline) -> Unit,
    browserAvailable: Boolean, trending: Boolean, modifier: Modifier = Modifier) {
    Box(modifier) {
    // Keep the native scroll indicator outside the article action targets.
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 20.dp).semantics { contentDescription = "News list" }) {
        itemsIndexed(headlines, key = { _, article -> article.id }) { index, headline ->
            val rowGroup = remember(headline.id) { SpatialHoverEffectGroup.obtain() }
            Row(Modifier.fillMaxWidth().height(if (trending) 88.dp else 64.dp)
                .clip(RoundedCornerShape(20.dp)).spatialHoverEffectGroup(rowGroup)
                .spatialHoverEffect().padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text((index + 1).toString(), color = if (trending && index < 3) HeadlineColors.Hot else PicoTheme.colorScheme.labelSecondary,
                    fontSize = 22.sp, modifier = Modifier.width(32.dp))
                Text(headline.title, color = PicoTheme.colorScheme.labelPrimary,
                    fontSize = if (trending) 20.sp else 24.sp, lineHeight = 28.sp, fontFamily = headlineFont(headline.title),
                    maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                // Always reserve the action slot; revealing it never covers or shifts a title.
                if (browserAvailable) Box(Modifier.revealOnHover(rowGroup)) {
                    OpenTopicButton(headline.articleUrl) { onOpenTopic(headline) }
                }
            }
        }
    }
    ScrollIndicator(state = listState)
    }
}
