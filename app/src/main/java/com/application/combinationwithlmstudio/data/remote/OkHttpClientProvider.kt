package com.application.combinationwithlmstudio.data.remote

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpClientProvider {

    fun createClient(
        connectTimeout: Long = 30,
        readTimeout: Long = 60,
        writeTimeout: Long = 60,
        enableLogging: Boolean = false
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            // Включаем кэширование соединений
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            // Автоматически обрабатываем перенаправления
            .followRedirects(true)
            // Автоматически следуем за редиректами в HTTP‑запросах
            .followSslRedirects(true)

        // Добавляем логирующий перехватчик, если включено логирование
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }
}