package com.headline.news.ui.headlines.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headline.news.domain.model.Headline
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.hover.SpatialHoverEffectGroup

@Composable
fun TrendingWidget(headlines: List<Headline>, listState: LazyListState, onOpenTopic: (Headline) -> Unit,
    browserAvailable: Boolean, modifier: Modifier = Modifier) {
    val group = remember { SpatialHoverEffectGroup.obtain() }
    WidgetSurface(group, modifier) {
        Column(Modifier.fillMaxSize().padding(8.dp)) {
            Text("Latest news", modifier = Modifier.height(56.dp).padding(start = 16.dp, top = 12.dp),
                color = PicoTheme.colorScheme.labelPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            ArticleList(headlines, listState, onOpenTopic, browserAvailable, trending = true, modifier = Modifier.weight(1f))
        }
    }
}
