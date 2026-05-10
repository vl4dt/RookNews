package com.rooknews.ai.models

import com.rooknews.domain.model.AiCapability
import com.rooknews.domain.model.AiModelCatalog

/**
 * Catalog of available on-device models.
 * Models are GGUF files from HuggingFace (bartowski), quantized for mobile inference.
 *
 * Gemma 4 uses Per-Layer Embeddings (PLE) — "2B" and "4B" are *effective* parameters.
 * Actual file sizes are 2-3x larger than the effective param count suggests.
 *
 * Selection rationale:
 * - E2B (3.5 GB): Phone default. 128K context swallows full articles. Apache 2.0.
 * - E4B (5.4 GB): Tablet/desktop. Better quality, same context. Gated by RAM check.
 */
object ModelCatalog {

    private const val RAM_PHONE_MIN = 6_000_000_000L  // 6 GB
    private const val RAM_TABLET_MIN = 8_000_000_000L // 8 GB

    val models: List<AiModelCatalog> = listOf(
        AiModelCatalog(
            id = "gemma-4-e2b-it-q4",
            name = "Gemma 4 E2B IT",
            provider = "Google",
            params = "2.3B effective (5.1B total)",
            quantization = "Q4_K_M",
            sizeBytes = 3_460_000_000, // ~3.46 GB
            ggufUrl = "https://huggingface.co/bartowski/google_gemma-4-E2B-it-GGUF/resolve/main/gemma-4-E2B-it-Q4_K_M.gguf",
            description = "Mobile-first multimodal model. 128K context, text+image+audio. Default for phones.",
            contextWindowTokens = 131_072,
            capabilities = setOf(
                AiCapability.SUMMARIZATION,
                AiCapability.BIAS_ANALYSIS,
                AiCapability.TOPIC_CLASSIFICATION,
                AiCapability.BRIEFING_GENERATION
            ),
            minRamRequiredBytes = RAM_PHONE_MIN
        ),
        AiModelCatalog(
            id = "gemma-4-e4b-it-q4",
            name = "Gemma 4 E4B IT",
            provider = "Google",
            params = "4.5B effective (8B total)",
            quantization = "Q4_K_M",
            sizeBytes = 5_410_000_000, // ~5.41 GB
            ggufUrl = "https://huggingface.co/bartowski/google_gemma-4-E4B-it-GGUF/resolve/main/gemma-4-E4B-it-Q4_K_M.gguf",
            description = "Higher-quality multimodal model for tablets. 128K context, better reasoning.",
            contextWindowTokens = 131_072,
            capabilities = setOf(
                AiCapability.SUMMARIZATION,
                AiCapability.BIAS_ANALYSIS,
                AiCapability.TOPIC_CLASSIFICATION,
                AiCapability.BRIEFING_GENERATION,
                AiCapability.DEEP_RESEARCH
            ),
            minRamRequiredBytes = RAM_TABLET_MIN
        )
    )

    fun getById(id: String): AiModelCatalog? = models.find { it.id == id }

    /**
     * Returns models that fit on a device with the given RAM.
     */
    fun availableForDevice(totalRamBytes: Long): List<AiModelCatalog> {
        return models.filter { it.minRamRequiredBytes <= totalRamBytes }
    }
}
