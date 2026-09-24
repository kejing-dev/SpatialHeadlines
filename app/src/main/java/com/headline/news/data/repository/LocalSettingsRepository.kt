package com.headline.news.data.repository

import android.content.SharedPreferences
import com.headline.news.domain.model.*

class LocalSettingsRepository(private val preferences: SharedPreferences) : SettingsRepository {
    override fun load() = HeadlineSettings(
        count = preferences.getInt("count", 3).coerceIn(1, 3),
        width = WidgetWidth.entries.firstOrNull { it.name == preferences.getString("width", null) }
            ?: WidgetWidth.Standard,
        edition = ContentEdition.entries.firstOrNull { it.name == preferences.getString("edition", null) } ?: ContentEdition.Overseas,
        region = NewsRegion.entries.firstOrNull { it.name == preferences.getString("region", null) } ?: NewsRegion.UnitedStates,
        source = NewsSource.entries.firstOrNull { it.name == preferences.getString("source", null) } ?: NewsSource.BBC,
        mode = DisplayMode.entries.firstOrNull { it.name == preferences.getString("mode", null) } ?: DisplayMode.Overhead,
        trendingCount = preferences.getInt("trendingCount", 6).coerceIn(1, 6),
        refreshAtEnd = preferences.getBoolean("refreshAtEnd", true),
    )

    override fun save(settings: HeadlineSettings) {
        preferences.edit().putInt("count", settings.count).putString("width", settings.width.name)
            .putString("edition", settings.edition.name).putString("region", settings.region.name)
            .putString("source", settings.source.name).putString("mode", settings.mode.name)
            .putInt("trendingCount", settings.trendingCount).putBoolean("refreshAtEnd", settings.refreshAtEnd).apply()
    }
}
