package com.rooknews.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Briefing(
    val id: String,
    val title: String,
    val contentMarkdown: String,
    val generatedAt: Long,
    val type: BriefingType,
    val articleIds: List<String> = emptyList(),
    val readingTimeMinutes: Int = 0
)

enum class BriefingType(val displayName: String) {
    MORNING("Morning Briefing"),
    EVENING("Evening Briefing"),
    TOPIC("Topic Briefing"),
    CUSTOM("Custom Briefing")
}
