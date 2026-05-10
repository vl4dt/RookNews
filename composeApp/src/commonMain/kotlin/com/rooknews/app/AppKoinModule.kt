package com.rooknews.app

import com.rooknews.ai.aiModule
import com.rooknews.data.dataModule
import org.koin.core.module.Module

object AppKoinModule {
    val koinModules: List<Module> = listOf(
        dataModule,
        aiModule
    )
}
