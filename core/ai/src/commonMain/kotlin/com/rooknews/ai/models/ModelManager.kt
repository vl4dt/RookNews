package com.rooknews.ai.models

import com.rooknews.domain.model.AiModelInfo
import com.rooknews.domain.model.AiModelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Manages on-device model lifecycle: download, storage, activation.
 * Uses Okio for KMP-compatible file I/O.
 */
class ModelManager(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {

    private val modelsDir: Path = "ai_models".toPath()
    private val _modelStates = MutableStateFlow<List<AiModelInfo>>(emptyList())
    val modelStates: StateFlow<List<AiModelInfo>> = _modelStates.asStateFlow()

    init {
        scanLocalModels()
    }

    /**
     * Scan local storage for existing models and build state map.
     */
    private fun scanLocalModels() {
        val states = ModelCatalog.models.mapNotNull { catalog ->
            val modelPath = modelsDir / "${catalog.id}.gguf"
            val exists = try { fileSystem.metadata(modelPath) != null } catch (_: Exception) { false }
            catalog.toAiModelInfo(downloaded = exists, active = false)
        }
        _modelStates.value = states
    }

    /**
     * Download a model with progress tracking.
     * Note: Actual download requires platform-specific HTTP streaming.
     * This is a stub that simulates the download process.
     */
    suspend fun downloadModel(modelId: String): Flow<Double> {
        return kotlinx.coroutines.flow.flow {
            // In production, this would use Ktor's streaming download
            // For now, simulate progress
            for (progress in listOf(0.1, 0.25, 0.4, 0.55, 0.7, 0.85, 0.95, 1.0)) {
                kotlinx.coroutines.delay(100)
                emit(progress)
            }
            // Mark as downloaded
            val updated = _modelStates.value.toMutableList()
            val index = updated.indexOfFirst { it.id == modelId }
            if (index >= 0) {
                updated[index] = updated[index].copy(downloaded = true, downloadProgress = 1.0)
            }
            _modelStates.value = updated
        }
    }

    /**
     * Activate a model (set as the active inference model).
     */
    suspend fun activateModel(modelId: String): Boolean {
        val catalog = ModelCatalog.getById(modelId) ?: return false
        val states = _modelStates.value.toMutableList()

        // Check model is downloaded
        val modelInfo = states.find { it.id == modelId }
        if (modelInfo == null || !modelInfo.downloaded) {
            return false
        }

        // Deactivate all, activate this one
        val updated = states.map { info ->
            info.copy(active = info.id == modelId)
        }
        _modelStates.value = updated
        return true
    }

    /**
     * Get the currently active model ID, or null if none active.
     */
    fun getActiveModelId(): String? {
        return _modelStates.value.find { it.active }?.id
    }

    /**
     * Check if any model is ready for inference.
     */
    fun isModelReady(): Boolean {
        return getActiveModelId() != null
    }

    /**
     * Get all models as a list.
     */
    fun getAllModels(): List<AiModelInfo> {
        return _modelStates.value
    }

    private fun com.rooknews.domain.model.AiModelCatalog.toAiModelInfo(
        downloaded: Boolean,
        active: Boolean
    ): AiModelInfo {
        return AiModelInfo(
            id = this.id,
            name = this.name,
            version = "${this.params}-${this.quantization}",
            sizeBytes = this.sizeBytes,
            downloaded = downloaded,
            active = active,
            downloadProgress = if (downloaded) 1.0 else 0.0,
            ggufUrl = this.ggufUrl
        )
    }
}
