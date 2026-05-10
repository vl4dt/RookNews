package com.rooknews.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiModelInfo(
    val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val downloaded: Boolean = false,
    val active: Boolean = false,
    val downloadProgress: Double = 0.0,
    val ggufUrl: String? = null
)

@Serializable
data class AiProcessingResult(
    val success: Boolean,
    val output: String? = null,
    val errorMessage: String? = null,
    val processingTimeMs: Long = 0,
    val provider: AiProvider = AiProvider.LOCAL
)

enum class AiProvider(val displayName: String) {
    LOCAL("On-Device"),
    CLOUD("Cloud")
}
