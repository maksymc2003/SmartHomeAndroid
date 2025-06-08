package com.example.smarthome

import okhttp3.OkHttpClient

object NetworkClient {
    val instance: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }
}