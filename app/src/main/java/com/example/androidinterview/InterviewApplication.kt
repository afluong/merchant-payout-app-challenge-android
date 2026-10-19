package com.example.androidinterview

import android.app.Application
import com.example.androidinterview.core.network.mock.MockServerManager
import dagger.hilt.android.HiltAndroidApp
import kotlin.concurrent.thread

@HiltAndroidApp
class InterviewApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        thread(name = "mock-server-init") { MockServerManager.start() }
    }
}

