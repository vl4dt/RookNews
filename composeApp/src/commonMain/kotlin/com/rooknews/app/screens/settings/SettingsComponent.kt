package com.rooknews.app.screens.settings

import androidx.compose.runtime.*
import com.rooknews.domain.model.ReadingMode
import com.rooknews.domain.model.UserPreferences
import com.rooknews.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberSettingsViewModel(
    repository: PreferencesRepository = koinInject()
): SettingsViewModel {
    val vm = remember { SettingsViewModel(repository) }
    LaunchedEffect(Unit) { vm.loadPreferences() }
    return vm
}

class SettingsViewModel(
    private val repository: PreferencesRepository
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())

    var preferences by mutableStateOf(UserPreferences())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun loadPreferences() {
        scope.launch {
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
        scope.launch { repository.updatePreferences(updated) }
    }

    fun onBriefingEnabled(enabled: Boolean) {
        val updated = preferences.copy(briefingEnabled = enabled)
        preferences = updated
        scope.launch { repository.updatePreferences(updated) }
    }
}
