package com.headline.news.data.repository

import com.headline.news.domain.model.*

interface HeadlineRepository {
    suspend fun headlines(settings: HeadlineSettings, forceRefresh: Boolean = false): FeedResult
}
data class FeedResult(val headlines: List<Headline>, val fetchedAt: Long, val offline: Boolean = false)
interface SettingsRepository {
    fun load(): HeadlineSettings
    fun save(settings: HeadlineSettings)
}
