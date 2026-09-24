package com.headline.news.ui.headlines.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.hover.*
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

/** Hover is evaluated in PICO's compositor, not through Android mouse events. */
@Composable
internal fun WidgetSurface(group: SpatialHoverEffectGroup, modifier: Modifier, cornerRadius: Dp = 28.dp, content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(modifier.clip(shape).backgroundMaterial(true, Material.Thickest)
        .border(1.dp, PicoTheme.colorScheme.dividerLine, shape)
        .spatialHoverEffectGroup(group)
        .spatialHoverEffect { animation(tween(durationMillis = 180)) { alpha(if (it.isActive) 1f else .72f) } },
        content = content,
    )
}

/** Space stays reserved so revealing controls cannot cover or reflow headlines. */
internal fun Modifier.revealOnHover(group: SpatialHoverEffectGroup): Modifier =
    spatialHoverEffectGroup(group.behavior(SpatialHoverEffectGroup.Behavior.Responder))
        .spatialHoverEffect { animation(tween(durationMillis = 160, delayMillis = if (it.isActive) 0 else 300)) { alpha(if (it.isActive) 1f else 0f) } }
