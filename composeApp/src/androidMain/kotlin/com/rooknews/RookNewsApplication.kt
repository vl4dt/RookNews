package com.rooknews

import android.app.Application
import com.rooknews.app.AppKoinModule
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.database.setApplicationContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RookNewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize SQLDelight database
        DatabaseProvider.setApplicationContext(this)

        startKoin {
            androidLogger()
            androidContext(this@RookNewsApplication)
            modules(AppKoinModule.koinModules)
        }
    }
}
