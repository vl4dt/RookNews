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
}

enum class ArticleSort {
    NEWEST,
    OLDEST,
    RELEVANCE,
    TITLE
}
