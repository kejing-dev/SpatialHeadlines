package com.headline.news

import com.pico.spatial.ui.foundation.dsl.DefaultWindowContainer
import com.pico.spatial.ui.foundation.dsl.WindowContainer
import com.pico.spatial.ui.foundation.dsl.WindowContainerSize
import com.pico.spatial.ui.foundation.dsl.Form
import com.pico.spatial.ui.foundation.dsl.Placement
import com.pico.spatial.ui.platform.resize.ContainerResizeType
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
import com.headline.news.ui.headlines.HeadlineScreen
import com.headline.news.ui.headlines.SettingsScreen
import com.headline.news.ui.theme.HeadlineTheme

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultWindowContainer {
            HeadlineTheme {
                HeadlineScreen()
            }
        }
        WindowContainer(
            id = "headline-settings",
            form = Form.Planar,
            defaultSize = WindowContainerSize(width = 800.dp, height = 840.dp),
            resizeType = ContainerResizeType.ContentSize,
            enableMaterialBackground = false,
            // Open fully in view even before the user moves the strip overhead.
            placement = { Placement.none() },
        ) {
            HeadlineTheme {
                SettingsScreen(Modifier.windowConstraints(width = 800.dp, height = 840.dp))
            }
        }
    }
