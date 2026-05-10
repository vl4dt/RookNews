package com.rooknews.app.screens.settings

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.ReadingMode
import com.rooknews.domain.model.UserPreferences
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberSettingsViewModel(
    repository: PreferencesRepository = koinInject(),
    articleRepository: ArticleRepository = koinInject()
): SettingsViewModel {
    val vm = remember { SettingsViewModel(repository, articleRepository) }
    LaunchedEffect(Unit) { vm.loadPreferences() }
    return vm
}

class SettingsViewModel(
    private val repository: PreferencesRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {
    var preferences by mutableStateOf(UserPreferences())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        loadPreferences()
    }

    fun loadPreferences() {
        viewModelScope.launch {
            isLoading = true
            error = null
            repository.preferencesFlow
                .catch { e ->
                    error = e.message ?: "Failed to load preferences"
                    isLoading = false
                }
                .collect { prefs ->
                    preferences = prefs
                    isLoading = false
                    error = null
                }
        }
    }

    fun onReadingModeChanged(mode: ReadingMode) {
        val updated = preferences.copy(readingMode = mode)
        preferences = updated
        viewModelScope.launch { repository.updatePreferences(updated) }
    }

    fun onBriefingEnabled(enabled: Boolean) {
        val updated = preferences.copy(briefingEnabled = enabled)
        preferences = updated
        viewModelScope.launch { repository.updatePreferences(updated) }
    }

    fun onCloudApiKeyChanged(key: String) {
        val updated = preferences.copy(cloudApiKey = key)
        preferences = updated
        viewModelScope.launch { repository.updatePreferences(updated) }
    }

    fun onPreferredProviderChanged(provider: AiProvider) {
        val updated = preferences.copy(preferredProvider = provider)
        preferences = updated
        viewModelScope.launch { repository.updatePreferences(updated) }
    }

    fun onArticleRetentionChanged(days: Int) {
        val updated = preferences.copy(articleRetentionDays = days)
        preferences = updated
        viewModelScope.launch { repository.updatePreferences(updated) }
    }

    fun onResetCache() {
        viewModelScope.launch {
            articleRepository.deleteUnbookmarkedArticles()
        }
    }
}
