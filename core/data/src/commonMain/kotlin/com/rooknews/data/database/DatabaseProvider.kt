package com.rooknews.data.database

/**
 * Provides the [ArticleDatabase] singleton.
 * Call [DatabaseProvider.init] during application startup.
 */
object DatabaseProvider {

    @Volatile
    internal var database: ArticleDatabase? = null

    fun provideDatabase(): ArticleDatabase {
        return database ?: synchronized(this) {
            database ?: run {
                val db = createDatabase()
                database = db
                db
            }
        }
    }
}

/**
 * Platform-specific database creation.
 */
internal expect fun createDatabase(): ArticleDatabase
