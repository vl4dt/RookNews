package com.rooknews.data.database

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Holds the Android application context for database initialization.
 */
@Volatile
private var applicationContext: Context? = null

/**
 * Set the Android application context. Call once from Application.onCreate().
 */
fun DatabaseProvider.setApplicationContext(context: Context) {
    applicationContext = context
}

/**
 * Android-specific database creation using stored context.
 */
internal actual fun createDatabase(): ArticleDatabase {
    val ctx = applicationContext
        ?: throw IllegalStateException("Call DatabaseProvider.setApplicationContext(context) before accessing the database on Android.")
    val driver = AndroidSqliteDriver(ArticleDatabase.Schema, ctx, "rooknews.db")
    return ArticleDatabase(driver)
}
