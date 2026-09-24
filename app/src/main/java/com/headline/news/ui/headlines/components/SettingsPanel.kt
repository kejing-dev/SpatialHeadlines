package com.headline.news.ui.headlines.components

import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headline.news.R
import com.headline.news.ui.theme.headlineFont
import com.headline.news.domain.model.*
import com.pico.spatial.ui.design.*
import com.pico.spatial.ui.design.menu.Menu
import com.pico.spatial.ui.design.menu.MenuItem
import com.pico.spatial.ui.foundation.material.backgroundMaterial
import com.pico.spatial.ui.platform.Material

@Composable
fun SettingsPanel(
    settings: HeadlineSettings,
    onRefreshAtEndChange: (Boolean) -> Unit,
    isLoading: Boolean,
    feedStatus: String,
    onRefresh: () -> Unit,
    sources: List<NewsSource>,
    onAdjustCount: (Int) -> Unit,
    onModeChange: (DisplayMode) -> Unit,
    onWidthChange: (WidgetWidth) -> Unit,
    onEditionChange: (ContentEdition) -> Unit,
    onRegionChange: (NewsRegion) -> Unit,
    onSourceChange: (NewsSource) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier.clip(shape).backgroundMaterial(true, Material.Thickest)
            .border(1.dp, PicoTheme.colorScheme.dividerLine, shape).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Headline settings", modifier = Modifier.weight(1f),
                color = PicoTheme.colorScheme.labelPrimary, style = PicoTheme.typography.titleLarge,
                fontSize = 28.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss, size = IconButtonDefaults.Small, modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = PicoTheme.colorScheme.fillSecondary, contentColor = PicoTheme.colorScheme.labelPrimary)) {
                Icon(painterResource(R.drawable.ic_close), "Close settings", modifier = Modifier.size(24.dp), tint = PicoTheme.colorScheme.labelPrimary)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingRow("Layout") {
            SegmentControl(Modifier.width(360.dp).height(48.dp)) {
                DisplayMode.entries.forEach { mode ->
                    SegmentItem(selected = settings.mode == mode, onClick = { onModeChange(mode) }, modifier = Modifier.weight(1f), title = {
                        // design-style: inherited-content-color SegmentItem
                        Text(mode.label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    })
                }
            }
        }
        if (settings.mode == DisplayMode.Overhead) SettingRow("Widget width") {
            SegmentControl(Modifier.width(360.dp).height(48.dp)) {
                WidgetWidth.entries.forEach { width ->
                    SegmentItem(selected = settings.width == width, onClick = { onWidthChange(width) }, modifier = Modifier.weight(1f), title = {
                        // design-style: inherited-content-color SegmentItem
                        Text(width.label, fontSize = 16.sp)
                    })
                }
            }
        } else SettingRow("Position") {
            Text("Move with window handle", color = PicoTheme.colorScheme.labelSecondary, fontSize = 16.sp)
        }
        SettingRow("Visible headlines") {
            Box(Modifier.width(360.dp), contentAlignment = Alignment.Center) {
                QuickControls(settings.visibleCount, settings.mode.limit, onAdjustCount, countBetween = true)
            }
        }
        SettingRow("Scrolling") {
            Text("Manual", color = PicoTheme.colorScheme.labelSecondary, fontSize = 18.sp)
        }
        SettingRow("Refresh at end") {
            Switch(checked = settings.refreshAtEnd, onCheckedChange = onRefreshAtEndChange,
                modifier = Modifier.semantics { contentDescription = "Refresh at end" })
        }
        SettingRow("Language") {
            SegmentControl(Modifier.width(360.dp).height(48.dp)) {
                ContentEdition.entries.forEach { edition ->
                    SegmentItem(selected = settings.edition == edition, onClick = { onEditionChange(edition) }, modifier = Modifier.weight(1f), title = {
                        // design-style: inherited-content-color SegmentItem
                        Text(edition.label, fontSize = 16.sp)
                    })
                }
            }
        }
        if (settings.edition == ContentEdition.Overseas) {
            SettingRow("News region") {
                ChoiceDropdown(settings.region.label, NewsRegion.entries, { it.label }, onRegionChange)
            }
        }
        SettingRow("News source") {
            ChoiceDropdown(settings.source.label, sources, { it.label }, onSourceChange)
        }
        }
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(feedStatus, modifier = Modifier.weight(1f).padding(end = 12.dp), color = PicoTheme.colorScheme.labelSecondary, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 2)
            Button(onClick = onRefresh, enabled = !isLoading, modifier = Modifier.width(164.dp).height(48.dp), colors = ButtonDefaults.buttonColors(
                containerColor = PicoTheme.colorScheme.fillSecondary, contentColor = PicoTheme.colorScheme.labelPrimary,
            )) {
                // design-style: inherited-content-color Button
                Text(if (isLoading) "Loading…" else "Refresh news", fontSize = 16.sp)
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onDismiss, modifier = Modifier.width(152.dp).height(48.dp), colors = ButtonDefaults.buttonColors(
                containerColor = PicoTheme.colorScheme.fillPrimary, contentColor = PicoTheme.colorScheme.labelPrimaryLight,
            )) {
                // design-style: inherited-content-color Button
                Text("Done", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.width(250.dp), color = PicoTheme.colorScheme.labelPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Box(Modifier.width(360.dp)) { content() }
    }
}

@Composable
private fun <T> ChoiceDropdown(label: String, options: List<T>, title: (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }, size = ButtonDefaults.Small, modifier = Modifier.width(360.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PicoTheme.colorScheme.fillSecondary, contentColor = PicoTheme.colorScheme.labelPrimary)) {
            // design-style: inherited-content-color Button
            Text(label, fontFamily = headlineFont(label), fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(R.drawable.ic_dropdown), contentDescription = null, modifier = Modifier.size(16.dp))
        }
        if (expanded) {
            Menu(onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    MenuItem(title = {
                        // design-style: inherited-content-color MenuItem
                        Text(title(option), fontFamily = headlineFont(title(option)))
                    }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}
