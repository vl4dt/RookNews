package com.rooknews.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val feedSourceId: String,
    val title: String,
    val summary: String? = null,
    val bodyMarkdown: String? = null,
    val author: String? = null,
    val publishedAt: Long,
    val fetchedAt: Long,
    val imageUrl: String? = null,
    val originalUrl: String,
    val read: Boolean = false,
    val bookmarked: Boolean = false,
    val aiSummary: String? = null,
    val aiBiasScore: Double? = null,
    val aiTopics: List<String> = emptyList(),
    val relevanceScore: Double? = null
)
