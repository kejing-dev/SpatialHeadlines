package com.headline.news.domain.model

data class Headline(val id: String, val title: String, val source: String, val articleUrl: String,
    val publishedAt: Long = 0L) {
    val metadata: String get() = if (publishedAt > 0) "$source · ${java.time.Instant.ofEpochMilli(publishedAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate()}" else source
}

enum class WidgetWidth(val dp: Int, val label: String) {
    Compact(800, "Narrow"), Standard(1000, "Standard"), Wide(1200, "Wide")
}

enum class DisplayMode(val label: String, val limit: Int) { Overhead("Overhead", 3), Trending("Trending", 6) }

enum class ContentEdition(val label: String) { Chinese("Chinese"), Overseas("English") }
enum class NewsRegion(val label: String) { UnitedStates("United States"), UnitedKingdom("United Kingdom") }
enum class NewsSource(val label: String) {
    BBC("BBC News"), Guardian("The Guardian"), BBCChinese("BBC 中文"), DWChinese("DW 中文")
}

data class HeadlineSettings(
    val count: Int = 3,
    val width: WidgetWidth = WidgetWidth.Standard,
    val edition: ContentEdition = ContentEdition.Overseas,
    val region: NewsRegion = NewsRegion.UnitedStates,
    val source: NewsSource = NewsSource.BBC,
    val mode: DisplayMode = DisplayMode.Overhead,
    val trendingCount: Int = 6,
    val refreshAtEnd: Boolean = true,
) {
    val visibleCount: Int get() = if (mode == DisplayMode.Overhead) count else trendingCount
    fun withVisibleCount(value: Int) = if (mode == DisplayMode.Overhead) copy(count = value.coerceIn(1, 3))
        else copy(trendingCount = value.coerceIn(1, 6))
}
