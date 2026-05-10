package com.rooknews.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rooknews.data.database.ArticleDatabase
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.database.toDomain
import com.rooknews.data.database.serializeToJsonArray
import com.rooknews.domain.model.Briefing as DomainBriefing
import com.rooknews.domain.repository.BriefingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultBriefingRepository : BriefingRepository {

    private val database: ArticleDatabase by lazy { DatabaseProvider.provideDatabase() }
    private val queries get() = database.briefingQueries

    override fun getBriefingsFlow(): Flow<List<DomainBriefing>> {
        return queries.selectAllBriefings().asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { row -> row.toDomain() } }
    }

    override suspend fun getBriefingById(id: String): DomainBriefing? = withContext(Dispatchers.IO) {
        queries.selectBriefingById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun deleteBriefing(id: String) = withContext(Dispatchers.IO) {
        queries.deleteBriefing(id)
    }

    override suspend fun insertBriefing(briefing: DomainBriefing) = withContext(Dispatchers.IO) {
        queries.insertBriefing(
            id = briefing.id,
            title = briefing.title,
            contentMarkdown = briefing.contentMarkdown,
            generatedAt = briefing.generatedAt,
            type = briefing.type.name,
            articleIds = serializeToJsonArray(briefing.articleIds),
            readingTimeMinutes = briefing.readingTimeMinutes.toLong()
        )
    }
}
