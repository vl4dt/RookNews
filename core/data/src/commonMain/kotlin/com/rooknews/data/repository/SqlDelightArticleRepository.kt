package com.rooknews.data.repository

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rooknews.data.database.Article as SqlArticle
import com.rooknews.data.database.ArticleDatabase
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.database.SelectArticlesByRelevance
import com.rooknews.data.database.toDomain
import com.rooknews.data.database.serializeToJsonArray
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.ArticleSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightArticleRepository : ArticleRepository {

    private val database: ArticleDatabase by lazy { DatabaseProvider.provideDatabase() }
    private val queries get() = database.articleQueries

    override fun getArticlesFlow(
        feedSourceIds: List<String>?,
        unreadOnly: Boolean,
        bookmarkedOnly: Boolean,
        sortBy: ArticleSort
    ): Flow<List<Article>> {

        val ctx = Dispatchers.IO

        val flow: Flow<List<Article>> = when {
            unreadOnly && bookmarkedOnly -> queries.selectUnreadArticles()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SqlArticle).toDomain() } }
            unreadOnly -> queries.selectUnreadArticles()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SqlArticle).toDomain() } }
            bookmarkedOnly -> queries.selectBookmarkedArticles()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SqlArticle).toDomain() } }
            sortBy == ArticleSort.RELEVANCE -> queries.selectArticlesByRelevance()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SelectArticlesByRelevance).toDomain() } }
            sortBy == ArticleSort.TITLE -> queries.selectArticlesByTitle()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SqlArticle).toDomain() } }
            sortBy == ArticleSort.OLDEST -> queries.selectAllArticles()
                .asFlow().mapToList(ctx).map { rows -> rows.reversed().map { (it as SqlArticle).toDomain() } }
            else -> queries.selectAllArticles()
                .asFlow().mapToList(ctx).map { rows -> rows.map { (it as SqlArticle).toDomain() } }
        }

        return flow
    }

    override suspend fun getArticleById(id: String): Article? = withContext(Dispatchers.IO) {
        queries.selectArticleById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun markAsRead(articleId: String, read: Boolean) = withContext(Dispatchers.IO) {
        queries.updateArticleRead(if (read) 1L else 0L, articleId)
    }

    override suspend fun toggleBookmark(articleId: String) = withContext(Dispatchers.IO) {
        val current = queries.selectArticleById(articleId).executeAsOneOrNull() ?: return@withContext
        queries.updateArticleBookmarked(if (current.bookmarked == 1L) 0L else 1L, articleId)
    }

    override suspend fun searchArticles(query: String): List<Article> = withContext(Dispatchers.IO) {
        queries.searchArticles(query).executeAsList().map { it.toDomain() }
    }

    override suspend fun clearOldArticles(beforeDate: Long) = withContext(Dispatchers.IO) {
        queries.deleteOldArticles(beforeDate)
    }

    suspend fun insertArticle(article: Article) = withContext(Dispatchers.IO) {
        queries.insertArticle(
            id = article.id,
            feedSourceId = article.feedSourceId,
            title = article.title,
            summary = article.summary,
            bodyMarkdown = article.bodyMarkdown,
            author = article.author,
            publishedAt = article.publishedAt,
            fetchedAt = article.fetchedAt,
            imageUrl = article.imageUrl,
            originalUrl = article.originalUrl,
            read = if (article.read) 1L else 0L,
            bookmarked = if (article.bookmarked) 1L else 0L,
            aiSummary = article.aiSummary,
            aiBiasScore = article.aiBiasScore,
            aiTopics = serializeToJsonArray(article.aiTopics),
            relevanceScore = article.relevanceScore
        )
    }

    suspend fun insertArticles(articles: List<Article>) = withContext(Dispatchers.IO) {
        articles.forEach { insertArticle(it) }
    }
}
