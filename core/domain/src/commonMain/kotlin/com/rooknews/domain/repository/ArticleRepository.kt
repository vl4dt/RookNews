package com.rooknews.domain.repository

import com.rooknews.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getArticlesFlow(
        feedSourceIds: List<String>? = null,
        unreadOnly: Boolean = false,
        bookmarkedOnly: Boolean = false,
        sortBy: ArticleSort = ArticleSort.NEWEST
    ): Flow<List<Article>>

    suspend fun getArticleById(id: String): Article?
    suspend fun markAsRead(articleId: String, read: Boolean)
    suspend fun toggleBookmark(articleId: String)
    suspend fun searchArticles(query: String): List<Article>
    suspend fun clearOldArticles(beforeDate: Long)

    /**
     * Insert a single article. Used during RSS sync.
     */
    suspend fun insertArticle(article: Article)

    /**
     * Insert multiple articles. Used during RSS sync.
     */
    suspend fun insertArticles(articles: List<Article>)

    /**
     * Clear all articles from the database. Used for cache reset.
     */
    suspend fun clearAllArticles()

    /**
     * Delete unbookmarked articles older than the given timestamp.
     * Used for automatic retention cleanup.
     */
    suspend fun deleteUnbookmarkedOldArticles(beforeDate: Long)

    /**
     * Delete all non-bookmarked articles. Used for cache reset.
     */
    suspend fun deleteUnbookmarkedArticles()
}

enum class ArticleSort {
    NEWEST,
    OLDEST,
    RELEVANCE,
    TITLE
}
