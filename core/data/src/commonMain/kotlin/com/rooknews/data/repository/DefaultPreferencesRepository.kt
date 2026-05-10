package com.rooknews.data.repository

import com.rooknews.domain.model.UserPreferences
import com.rooknews.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory preferences repository backed by StateFlow.
 * Persists to Okio file storage on platform-specific init.
 */
class DefaultPreferencesRepository : PreferencesRepository {

    private val _preferences = MutableStateFlow(UserPreferences())
    override val preferencesFlow: Flow<UserPreferences> = _preferences.asStateFlow()

    override suspend fun getPreferences(): UserPreferences {
        return _preferences.value
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        _preferences.value = preferences
        // TODO: persist to Okio file storage
    }
}
