package com.rooknews.ai.models

import com.rooknews.domain.model.AiModelInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Manages on-device model lifecycle: download, storage, activation.
 * Uses Okio for KMP-compatible file I/O.
 * Download implementation uses a stub — real streaming download requires
 * platform-specific HTTP implementation.
 */
class ModelManager(
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {

    private val modelsDir: Path = "ai_models".toPath()
    private val _modelStates = mutableMapOf<String, AiModelInfo>()
    private var _activeModelId: String? = null

    val modelStates: List<AiModelInfo>
        get() = ModelCatalog.models.map { catalog ->
            _modelStates[catalog.id] ?: catalog.toAiModelInfo(
                downloaded = false,
                active = false,
                downloadProgress = 0.0
            )
        }

    val activeModelId: String?
        get() = _activeModelId

    init {
        scanLocalModels()
    }

    /**
     * Scan local storage for existing models and build state map.
     */
    private fun scanLocalModels() {
        ModelCatalog.models.forEach { catalog ->
            val modelPath = modelsDir / "${catalog.id}.gguf"
            val exists = try {
                fileSystem.exists(modelPath)
            } catch (_: Exception) { false }

            if (exists) {
                _modelStates[catalog.id] = catalog.toAiModelInfo(
                    downloaded = true,
                    active = _activeModelId == catalog.id,
                    downloadProgress = 1.0
                )
            }
        }
    }

    /**
     * Download a model with progress tracking.
     * Note: This is a stub implementation that simulates download progress.
     * A real implementation would use Ktor's streaming HTTP download.
     */
    suspend fun downloadModel(modelId: String): Flow<Double> = channelFlow {
        val catalog = ModelCatalog.getById(modelId) ?: run {
            send(0.0)
            return@channelFlow
        }

        // Simulate download progress (stub)
        for (progress in listOf(0.1, 0.25, 0.4, 0.55, 0.7, 0.85, 0.95, 1.0)) {
            kotlinx.coroutines.delay(100)
            _modelStates[modelId]?.let { current ->
                _modelStates[modelId] = current.copy(downloadProgress = progress)
            }
            send(progress)
        }

        // Mark as downloaded
        _modelStates[modelId] = catalog.toAiModelInfo(
            downloaded = true,
            active = _activeModelId == modelId,
            downloadProgress = 1.0
        )
    }

    /**
     * Activate a model (set as the active inference model).
     */
    suspend fun activateModel(modelId: String): Boolean {
        val catalog = ModelCatalog.getById(modelId) ?: return false

        val modelInfo = _modelStates[modelId] ?: return false
        if (!modelInfo.downloaded) return false

        // Deactivate all, activate this one
        _activeModelId = modelId
        _modelStates[modelId] = modelInfo.copy(active = true)

        // Deactivate others
        _modelStates.keys.forEach { id ->
            if (id != modelId && _modelStates.containsKey(id)) {
                val current = _modelStates[id]
                current?.let {
                    _modelStates[id] = it.copy(active = false)
                }
            }
        }

        return true
    }

    /**
     * Deactivate the currently active model.
     */
    suspend fun deactivateActiveModel() {
        _activeModelId?.let { id ->
            _modelStates[id]?.let { current ->
                _modelStates[id] = current.copy(active = false)
            }
            _activeModelId = null
        }
    }

    /**
     * Check if any model is ready for inference.
     */
    fun isModelReady(): Boolean = _activeModelId != null

    /**
     * Get all models as a list.
     */
    fun getAllModels(): List<AiModelInfo> = modelStates

    /**
     * Get download status for a specific model.
     */
    fun getDownloadStatus(modelId: String): Double = _modelStates[modelId]?.downloadProgress ?: 0.0

    private fun com.rooknews.domain.model.AiModelCatalog.toAiModelInfo(
        downloaded: Boolean,
        active: Boolean,
        downloadProgress: Double
    ): AiModelInfo {
        return AiModelInfo(
            id = this.id,
            name = this.name,
            version = "${this.params} (${this.quantization})",
            sizeBytes = this.sizeBytes,
            downloaded = downloaded,
            active = active,
            downloadProgress = downloadProgress,
            ggufUrl = this.ggufUrl
        )
    }
}
