package com.micahsoftdotexe.dreamingofclocks

import android.app.Application
import com.micahsoftdotexe.dreamingofclocks.di.AppContainer

class DreamingOfClocksApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
