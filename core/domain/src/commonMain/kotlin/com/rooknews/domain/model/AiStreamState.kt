package com.rooknews.domain.model

import kotlinx.serialization.Serializable

/**
 * Sealed state for AI token-streaming UI.
 * Used by ViewModels to expose streaming inference progress to Compose.
 */
sealed interface AiStreamState {
    object Idle : AiStreamState
    object Loading : AiStreamState
    data class Streaming(val token: String, val fullText: String) : AiStreamState
    data class Complete(val output: String, val processingTimeMs: Long) : AiStreamState
    data class Error(val message: String) : AiStreamState
}

@Serializable
data class AiModelCatalog(
    val id: String,
    val name: String,
    val provider: String,
    val params: String,
    val quantization: String,
    val sizeBytes: Long,
    val ggufUrl: String,
    val description: String,
    val contextWindowTokens: Int = 8192,
    val capabilities: Set<AiCapability> = setOf(AiCapability.SUMMARIZATION),
    val minRamRequiredBytes: Long = 0L
)

enum class AiCapability {
    SUMMARIZATION,
    BIAS_ANALYSIS,
    TOPIC_CLASSIFICATION,
    BRIEFING_GENERATION,
    DEEP_RESEARCH
}

enum class AiModelStatus {
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    ACTIVE,
    ERROR
}
