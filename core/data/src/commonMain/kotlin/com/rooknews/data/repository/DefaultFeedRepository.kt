package com.rooknews.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rooknews.data.database.ArticleDatabase
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.database.toDomain
import com.rooknews.data.feed.FeedCatalog
import com.rooknews.domain.model.FeedSource
import com.rooknews.domain.repository.FeedRepository
import com.rooknews.domain.repository.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultFeedRepository : FeedRepository {

    private val database: ArticleDatabase by lazy { DatabaseProvider.provideDatabase() }
    private val queries get() = database.feedSourceQueries

    override fun getFeedsFlow(): Flow<List<FeedSource>> {
        return queries.selectAllFeeds().asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { row -> row.toDomain() } }
    }

    override suspend fun getFeedById(id: String): FeedSource? = withContext(Dispatchers.IO) {
        queries.selectFeedById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun addFeed(feed: FeedSource) = withContext(Dispatchers.IO) {
        queries.insertFeed(
            id = feed.id,
            title = feed.title,
            url = feed.url,
            feedUrl = feed.feedUrl,
            category = feed.category.name,
            region = feed.region,
            language = feed.language,
            enabled = if (feed.enabled) 1L else 0L,
            lastFetched = feed.lastFetched
        )
    }

    override suspend fun removeFeed(feedId: String) = withContext(Dispatchers.IO) {
        queries.deleteFeed(feedId)
    }

    override suspend fun toggleFeed(feedId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        queries.updateFeedEnabled(if (enabled) 1L else 0L, feedId)
    }

    override suspend fun syncFeeds(): SyncResult {
        return SyncResult(success = false, 0, 0)
    }

    override suspend fun getBuiltInCatalog(): List<FeedSource> {
        return FeedCatalog.feeds
    }

    suspend fun seedBuiltInFeeds() = withContext(Dispatchers.IO) {
        FeedCatalog.feeds.forEach { feed ->
            queries.insertFeed(
                id = feed.id,
                title = feed.title,
                url = feed.url,
                feedUrl = feed.feedUrl,
                category = feed.category.name,
                region = feed.region,
                language = feed.language,
                enabled = if (feed.enabled) 1L else 0L,
                lastFetched = feed.lastFetched
            )
        }
    }
}
