package com.rooknews.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedSource(
    val id: String,
    val title: String,
    val url: String,
    val feedUrl: String,
    val category: FeedCategory,
    val region: String? = null,
    val language: String = "en",
    val enabled: Boolean = true,
    val lastFetched: Long? = null
)

enum class FeedCategory(val displayName: String) {
    WORLD("World"),
    POLITICS("Politics"),
    TECHNOLOGY("Technology"),
    SCIENCE("Science"),
    BUSINESS("Business"),
    HEALTH("Health"),
    SPORTS("Sports"),
    CULTURE("Culture"),
    LOCAL("Local")
}
