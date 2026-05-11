package com.rooknews.app.screens.briefings

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.Briefing
import com.rooknews.domain.model.BriefingType
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private fun generateId(): String = "${System.currentTimeMillis()}-${(1..6).map { "0123456789abcdef".random() }.joinToString("")}"

@Composable
fun rememberBriefingsViewModel(
    briefingRepository: BriefingRepository = koinInject(),
    aiRepository: AiRepository = koinInject(),
    preferredProvider: AiProvider = AiProvider.LOCAL
): BriefingsViewModel {
    val vm = remember { BriefingsViewModel(briefingRepository, aiRepository, preferredProvider) }
    LaunchedEffect(Unit) { vm.loadBriefings() }
    return vm
}

class BriefingsViewModel(
    private val briefingRepository: BriefingRepository,
    private val aiRepository: AiRepository,
    private val preferredProvider: AiProvider = AiProvider.LOCAL
) : ViewModel() {
    var briefings by mutableStateOf<List<Briefing>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadBriefings()
    }

    fun loadBriefings() {
        viewModelScope.launch {
            isLoading = true
            error = null
            briefingRepository.getBriefingsFlow()
                .catch { e ->
                    error = e.message ?: "Failed to load briefings"
                    isLoading = false
                }
                .collect { list ->
                    briefings = list.sortedByDescending { it.generatedAt }
                    isLoading = false
                    error = null
                }
        }
    }

    fun onDeleteBriefing(id: String) {
        viewModelScope.launch { briefingRepository.deleteBriefing(id) }
    }

    fun onGenerateBriefing(topic: String? = null) {
        isGenerating = true
        error = null
        viewModelScope.launch {
            try {
                val aiResult = aiRepository.generateBriefing(topic, preferredProvider)
                if (aiResult.success && aiResult.output != null) {
                    val briefing = Briefing(
                        id = generateId(),
                        title = topic ?: "Daily Briefing",
                        contentMarkdown = aiResult.output!!,
                        generatedAt = System.currentTimeMillis(),
                        type = if (topic != null) BriefingType.TOPIC else BriefingType.MORNING,
                        readingTimeMinutes = estimateReadingTime(aiResult.output!!)
                    )
                    briefingRepository.insertBriefing(briefing)
                    loadBriefings() // Reload to show new briefing
                } else {
                    error = aiResult.errorMessage ?: "Briefing generation failed"
                }
            } catch (e: Exception) {
                error = e.message ?: "Briefing generation failed"
            } finally {
                isGenerating = false
            }
        }
    }

    private fun estimateReadingTime(text: String): Int {
        val words = text.split(Regex("\\s+")).size
        return kotlin.math.max(1, words / 200)
    }
}
