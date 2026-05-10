package com.rooknews.domain.repository

import com.rooknews.domain.model.Briefing
import com.rooknews.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun getPreferences(): UserPreferences
    suspend fun updatePreferences(preferences: UserPreferences)
}

interface BriefingRepository {
    fun getBriefingsFlow(): Flow<List<Briefing>>
    suspend fun getBriefingById(id: String): Briefing?
    suspend fun insertBriefing(briefing: Briefing)
    suspend fun deleteBriefing(id: String)
}
