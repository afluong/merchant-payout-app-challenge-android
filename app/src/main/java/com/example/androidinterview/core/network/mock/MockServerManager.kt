package com.example.androidinterview.core.network.mock

import okhttp3.mockwebserver.MockWebServer

/**
 * Starts a local OkHttp MockWebServer that intercepts all API calls.
 * Mirrors the MSW (Mock Service Worker) setup in the React Native version of this challenge.
 *
 * Usage: call MockServerManager.baseUrl as your Retrofit/OkHttp base URL.
 */
object MockServerManager {

    private val server = MockWebServer()

    val baseUrl: String
        get() = server.url("/").toString()

    fun start() {
        server.dispatcher = MockDispatcher()
        server.start()
    }

    fun shutdown() {
        runCatching { server.shutdown() }
    }
}
