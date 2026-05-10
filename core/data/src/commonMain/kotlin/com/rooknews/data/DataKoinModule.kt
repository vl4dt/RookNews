package com.rooknews.data

import com.rooknews.data.database.ArticleDatabase
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.network.KtorClientFactory
import com.rooknews.data.repository.SqlDelightArticleRepository
import com.rooknews.data.repository.DefaultFeedRepository
import com.rooknews.data.repository.DefaultPreferencesRepository
import com.rooknews.data.repository.DefaultBriefingRepository
import com.rooknews.data.rss.RssFetcher
import com.rooknews.data.rss.RssParser
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.FeedRepository
import com.rooknews.domain.repository.PreferencesRepository
import com.rooknews.domain.repository.BriefingRepository
import org.koin.dsl.module

val dataModule = module {
    // Infrastructure
    single { KtorClientFactory.createConfigured() }
    single<ArticleDatabase> { DatabaseProvider.provideDatabase() }
    single { RssFetcher(get()) }
    single { RssParser() }

    // Repositories
    single<ArticleRepository> { SqlDelightArticleRepository() }
    single<FeedRepository> { DefaultFeedRepository() }
    single<PreferencesRepository> { DefaultPreferencesRepository() }
    single<BriefingRepository> { DefaultBriefingRepository() }
}
