package com.application.combinationwithlmstudio.data.remote

import com.application.combinationwithlmstudio.data.model.ApiResponse
import com.application.combinationwithlmstudio.data.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class LlmApiService(
    private val client: OkHttpClient,
    private val baseUrl: String = "http://192.168.1.10:1234"
) {

    suspend fun getChatResponse(
        userMessage: String,
        modelName: String = "MODEL_NAME",
        temperature: Double = 0.7
    ): Result<ApiResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = createRequestBody(userMessage, modelName, temperature)
            val request = createRequest(requestBody)

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP error ${response.code}: ${response.message}")
                }

                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response body")

                // Парсим JSON в модель ApiResponse
                val apiResponse = parseResponse(responseBody)
                Result.success(apiResponse)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createRequestBody(
        userMessage: String,
        modelName: String,
        temperature: Double
    ): String {
        return """
            {
              "model": "$modelName",
              "messages": [
                {"role": "system", "content": "Ты полезный ассистент."},
                {"role": "user", "content": "$userMessage"}
              ],
              "temperature": $temperature,
              "stream": false,
              "max_tokens": 1000
            }
        """.trimIndent()
    }

    private fun createRequest(requestBody: String): Request {
        val requestBodyJson = requestBody.toRequestBody("application/json".toMediaTypeOrNull())

        return Request.Builder()
            .url("$baseUrl/v1/chat/completions")
            .post(requestBodyJson)
            .build()
    }

    private fun parseResponse(jsonResponse: String): ApiResponse {
        return try {
            // Используем kotlinx.serialization или Gson для надёжного парсинга
            // Ниже — упрощённая реализация для демонстрации

            val contentStart = jsonResponse.indexOf("\"content\":\"") + 10
            if (contentStart <= 10) {
                throw IOException("Не удалось найти поле 'content' в ответе")
            }

            val contentEnd = jsonResponse.indexOf("\"", contentStart)
            if (contentEnd <= contentStart) {
                throw IOException("Некорректный формат поля 'content'")
            }

            val content = jsonResponse.substring(contentStart, contentEnd)

            // Извлекаем ID чата, если есть
            val id = extractField(jsonResponse, "id")
            // Извлекаем модель
            val model = extractField(jsonResponse, "model")

            ApiResponse(
                id = id,
                model = model,
                choices = listOf(
                    ApiResponse.Choice(
                        message = Message(content, false),
                        finishReason = extractField(jsonResponse, "finish_reason")
                    )
                ),
                created = extractLongField(jsonResponse, "created")
            )
        } catch (e: Exception) {
            throw IOException("Ошибка парсинга JSON: ${e.message}", e)
        }
    }

    private fun extractField(json: String, fieldName: String): String? {
        val pattern = "\"$fieldName\":\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)
    }

    private fun extractLongField(json: String, fieldName: String): Long? {
        val pattern = "\"$fieldName\":(\\d+)".toRegex()
        return pattern.find(json)?.groupValues?.getOrNull(1)?.toLongOrNull()
    }
}