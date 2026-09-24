package com.headline.news.ui.headlines.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headline.news.R
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.hover.SpatialHoverEffectGroup

@Composable
fun FeedStatusPanel(loading: Boolean, error: String?, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val group = remember { SpatialHoverEffectGroup.obtain() }
    WidgetSurface(group, modifier) {
        Row(Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (loading) "Loading news…" else "News unavailable", color = PicoTheme.colorScheme.labelPrimary, fontSize = 22.sp)
                Text(if (loading) "Getting articles from your selected source" else error ?: "No articles available.",
                    color = PicoTheme.colorScheme.labelSecondary, fontSize = 14.sp, lineHeight = 20.sp)
            }
            if (!loading) Button(onClick = onRetry) {
                // design-style: inherited-content-color Button
                Text("Retry")
            }
        }
    }
}
