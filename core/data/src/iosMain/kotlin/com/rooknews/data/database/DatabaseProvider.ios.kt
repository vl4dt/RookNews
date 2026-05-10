package com.rooknews.data.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS-specific database creation — no extra init needed.
 */
internal actual fun createDatabase(): ArticleDatabase {
    val driver = NativeSqliteDriver(ArticleDatabase.Schema)
    return ArticleDatabase(driver)
}
