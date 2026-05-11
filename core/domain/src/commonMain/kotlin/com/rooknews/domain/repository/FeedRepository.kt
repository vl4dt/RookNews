package com.rooknews.domain.repository

import com.rooknews.domain.model.FeedSource
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeedsFlow(): Flow<List<FeedSource>>
    suspend fun getFeedById(id: String): FeedSource?
    suspend fun addFeed(feed: FeedSource)
    suspend fun removeFeed(feedId: String)
    suspend fun toggleFeed(feedId: String, enabled: Boolean)
    suspend fun updateFeedLastFetched(feedId: String, timestamp: Long)
    suspend fun syncFeeds(): SyncResult
    suspend fun getBuiltInCatalog(): List<FeedSource>
}

data class SyncResult(
    val success: Boolean,
    val articlesFetched: Int,
    val feedsSynced: Int,
    val errors: List<String> = emptyList()
)
