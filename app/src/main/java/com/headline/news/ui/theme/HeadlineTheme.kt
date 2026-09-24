package com.headline.news.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.headline.news.R
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.systemColorScheme

val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
)

val NotoSansSC = FontFamily(Font(R.font.noto_sans_sc_regular, FontWeight.Normal))

fun headlineFont(text: String) = if (text.any { it in '\u3400'..'\u9fff' }) NotoSansSC else NotoSans

/** Exact app-owned palette from Figma S21. The room and editorial labels are not app UI. */
object HeadlineColors {
    val Ink = Color(0xFF202426) // design-style: fixed-figma-color V2 20:1173
    val SecondaryInk = Color(0xFF637074) // design-style: fixed-figma-color V2 20:1173
    val Hot = Color(0xFFD95043) // design-style: fixed-figma-color V2 20:1172
    val Warm = Color(0xFFC67526) // design-style: fixed-figma-color V2 20:1172
    val Border = Color(0x4DFFFFFF) // design-style: fixed-figma-color S21 2:5772
    val ButtonFill = Color(0xFFEEF1EF) // design-style: fixed-figma-color V2 20:1173
    // Native Material.Thickest reproduces the design's material blend and blur in the compositor.
    val MaterialBase = Color(0xCCBBBBBB) // design-style: fixed-figma-color S21 2:5772
    val MaterialDodge = Color(0xFF2E2E2E) // design-style: fixed-figma-color S21 2:5772
    val ButtonDodge = Color(0x7A323232) // design-style: fixed-figma-color S21 2:5772
}

@Composable
fun HeadlineTheme(content: @Composable () -> Unit) {
    val system = systemColorScheme(LocalContext.current)
    val colors = system.copy(
        fillPrimary = Color(0xFF282828), // design-style: fixed-figma-color S11 Done button
        fillSecondary = HeadlineColors.ButtonFill,
        fillTertiary = system.fillTertiary,
        fillLight = system.fillLight,
        labelPrimary = HeadlineColors.Ink,
        labelPrimaryLight = Color(0xFFFFFFFF), // design-style: fixed-figma-color S21 2:5772
        labelSecondary = HeadlineColors.SecondaryInk,
        labelTertiary = system.labelTertiary,
        labelQuaternary = system.labelQuaternary,
        lightenHover = system.lightenHover,
        lightenPressed = system.lightenPressed,
        error = system.error,
        alert = system.alert,
        passable = system.passable,
        interaction = system.interaction,
        dividerLine = HeadlineColors.Border,
    )
    val base = PicoTheme.typography
    val typography = base.copy(
        bodyLarge = base.bodyLarge.copy(fontFamily = NotoSans),
        bodyLargeMultiline = base.bodyLargeMultiline.copy(fontFamily = NotoSans),
        bodyMedium = base.bodyMedium.copy(fontFamily = NotoSans, fontSize = 24.sp, lineHeight = 36.sp, letterSpacing = 0.3.sp, fontWeight = FontWeight.Medium),
        bodyMediumMultiline = base.bodyMediumMultiline.copy(fontFamily = NotoSans),
        bodySmall = base.bodySmall.copy(fontFamily = NotoSans),
        bodyTiny = base.bodyTiny.copy(fontFamily = NotoSans),
        displayLarge = base.displayLarge.copy(fontFamily = NotoSans),
        displayMedium = base.displayMedium.copy(fontFamily = NotoSans),
        displaySmall = base.displaySmall.copy(fontFamily = NotoSans),
        headlineLarge = base.headlineLarge.copy(fontFamily = NotoSans),
        headlineMedium = base.headlineMedium.copy(fontFamily = NotoSans),
        headlineSmall = base.headlineSmall.copy(fontFamily = NotoSans),
        titleLarge = base.titleLarge.copy(fontFamily = NotoSans),
        titleMedium = base.titleMedium.copy(fontFamily = NotoSans),
        titleSmall = base.titleSmall.copy(fontFamily = NotoSans),
        labelLarge = base.labelLarge.copy(fontFamily = NotoSans),
        labelMedium = base.labelMedium.copy(fontFamily = NotoSans),
        labelSmall = base.labelSmall.copy(fontFamily = NotoSans),
    )
    PicoTheme(colorScheme = colors, typography = typography, content = content)
}
