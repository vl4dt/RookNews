package com.rooknews.data.database

import com.rooknews.domain.model.Article
import com.rooknews.domain.model.Briefing
import com.rooknews.domain.model.BriefingType
import com.rooknews.domain.model.FeedCategory
import com.rooknews.domain.model.FeedSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Map a SQLDelight [Article] row to a domain [Article] model.
 */
fun com.rooknews.data.database.Article.toDomain(): Article {
    return Article(
        id = id,
        feedSourceId = feed_source_id,
        title = title,
        summary = summary,
        bodyMarkdown = body_markdown,
        author = author,
        publishedAt = published_at,
        fetchedAt = fetched_at,
        imageUrl = image_url,
        originalUrl = original_url,
        read = read == 1L,
        bookmarked = bookmarked == 1L,
        aiSummary = ai_summary,
        aiBiasScore = ai_bias_score,
        aiTopics = parseJsonArray(ai_topics),
        relevanceScore = relevance_score
    )
}

/**
 * Map a SQLDelight [SelectArticlesByRelevance] row to a domain [Article] model.
 * (Same shape as Article but with non-null relevance_score.)
 */
fun com.rooknews.data.database.SelectArticlesByRelevance.toDomain(): Article {
    return Article(
        id = id,
        feedSourceId = feed_source_id,
        title = title,
        summary = summary,
        bodyMarkdown = body_markdown,
        author = author,
        publishedAt = published_at,
        fetchedAt = fetched_at,
        imageUrl = image_url,
        originalUrl = original_url,
        read = read == 1L,
        bookmarked = bookmarked == 1L,
        aiSummary = ai_summary,
        aiBiasScore = ai_bias_score,
        aiTopics = parseJsonArray(ai_topics),
        relevanceScore = relevance_score
    )
}

/**
 * Map a SQLDelight [Feed_source] row to a domain [FeedSource] model.
 */
fun com.rooknews.data.database.Feed_source.toDomain(): FeedSource {
    return FeedSource(
        id = id,
        title = title,
        url = url,
        feedUrl = feed_url,
        category = FeedCategory.valueOf(category),
        region = region,
        language = language,
        enabled = enabled == 1L,
        lastFetched = last_fetched
    )
}

/**
 * Map a domain [FeedSource] to values for SQLDelight insert.
 */
fun FeedSource.toSqlInsertArgs() {
    // Called via FeedSourceQueries.insertFeed
}

/**
 * Map a SQLDelight [Briefing] row to a domain [Briefing] model.
 */
fun com.rooknews.data.database.Briefing.toDomain(): Briefing {
    return Briefing(
        id = id,
        title = title,
        contentMarkdown = content_markdown,
        generatedAt = generated_at,
        type = BriefingType.valueOf(type),
        articleIds = parseJsonArray(article_ids),
        readingTimeMinutes = reading_time_minutes.toInt()
    )
}

// -- JSON helpers for serializing/deserializing lists in TEXT columns --

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

internal fun parseJsonArray(raw: String): List<String> {
    return try {
        json.parseToJsonElement(raw).jsonArray.mapNotNull { it.jsonPrimitive.content }
    } catch (_: Exception) {
        emptyList()
    }
}

internal fun serializeToJsonArray(list: List<String>): String {
    return "[${list.joinToString(", ") { "\"$it\"" }}]"
}
