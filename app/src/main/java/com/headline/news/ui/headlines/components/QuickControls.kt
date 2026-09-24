package com.headline.news.ui.headlines.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headline.news.R
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.foundation.hover.SpatialHoverEffectGroup

@Composable
fun NewsToolbar(count: Int, limit: Int, loading: Boolean, onAdjust: (Int) -> Unit,
    onSettings: () -> Unit, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val group = remember { SpatialHoverEffectGroup.obtain() }
    WidgetSurface(group, Modifier.width(384.dp).height(72.dp).then(modifier), cornerRadius = 36.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$count visible", color = PicoTheme.colorScheme.labelSecondary, fontSize = 16.sp,
                modifier = Modifier.weight(1f).padding(start = 8.dp).semantics { stateDescription = "$count / $limit" })
            ControlButton(R.drawable.ic_minus, "Fewer headlines", count > 1) { onAdjust(-1) }
            ControlButton(R.drawable.ic_plus, "More headlines", count < limit) { onAdjust(1) }
            ControlButton(R.drawable.ic_settings, "Settings", onClick = onSettings)
            if (loading) Box(Modifier.size(48.dp).semantics { contentDescription = "Refreshing news" }, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else ControlButton(R.drawable.ic_refresh, "Refresh news", onClick = onRefresh)
        }
    }
}

@Composable
fun QuickControls(count: Int, limit: Int, onAdjust: (Int) -> Unit, onSettings: (() -> Unit)? = null, showCount: Boolean = true, countBetween: Boolean = false) {
    val currentAdjust by rememberUpdatedState(onAdjust)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (showCount && !countBetween) Text("$count / $limit", color = PicoTheme.colorScheme.labelSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 4.dp))
        ControlButton(R.drawable.ic_minus, "Fewer headlines", count > 1) { currentAdjust(-1) }
        if (showCount && countBetween) Text("$count / $limit", color = PicoTheme.colorScheme.labelSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp))
        ControlButton(R.drawable.ic_plus, "More headlines", count < limit) { currentAdjust(1) }
        if (onSettings != null) ControlButton(R.drawable.ic_settings, "Settings", onClick = onSettings)
    }
}

@Composable
fun ControlButton(icon: Int, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp),
        size = IconButtonDefaults.Small,
        colors = IconButtonDefaults.iconButtonColors(containerColor = PicoTheme.colorScheme.fillSecondary, contentColor = PicoTheme.colorScheme.labelPrimary)) {
        Icon(painterResource(icon), contentDescription = label, modifier = Modifier.size(24.dp))
    }
}

/** A separate action target; the headline itself never launches another app. */
@Composable
fun OpenTopicButton(articleUrl: String, onClick: () -> Unit) {
    val currentOpen by rememberUpdatedState(onClick)
    IconButton(onClick = { currentOpen() }, modifier = Modifier.size(48.dp).semantics { contentDescription = "Open article in browser"; stateDescription = articleUrl },
        size = IconButtonDefaults.Small,
        colors = IconButtonDefaults.iconButtonColors(containerColor = PicoTheme.colorScheme.fillSecondary, contentColor = PicoTheme.colorScheme.labelPrimary)) {
        Text("↗", color = PicoTheme.colorScheme.labelPrimary, fontSize = 26.sp)
    }
}
