package com.rooknews.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val preferredTopics: List<String> = emptyList(),
    val excludedTopics: List<String> = emptyList(),
    val preferredSources: List<String> = emptyList(),
    val readingMode: ReadingMode = ReadingMode.LIGHT,
    val briefingEnabled: Boolean = true,
    val briefingTime: String = "07:00",
    val syncIntervalMinutes: Int = 30,
    val maxArticlesPerFeed: Int = 50,
    val language: String = "en",
    val region: String? = null
)

enum class ReadingMode(val displayName: String) {
    LIGHT("Light"),
    SEPIA("Sepia"),
    DARK("Dark")
}

@Serializable
data class SubscriptionState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val expiresAt: Long? = null,
    val lastChecked: Long = 0
)

enum class SubscriptionTier {
    FREE,
    PRO
}
