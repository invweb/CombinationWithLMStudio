package com.application.combinationwithlmstudio.ui.viewmodel
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.combinationwithlmstudio.data.model.ChatState
import com.application.combinationwithlmstudio.data.model.Message
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// Замените IP на адрес вашего ПК в локальной сети
private const val LOCAL_API_URL = "http://192.168.1.10:1234/v1/chat/completions"

class ChatViewModel : ViewModel() {
    private val _state = mutableStateOf(ChatState())
    val state: Unit = _state.asStateFlow()

    private val client = OkHttpClient()

    fun sendMessage(userMessage: String) {
        // Добавляем сообщение пользователя
        _state.value = _state.value.copy(
            messages = _state.value.messages + Message(userMessage, true),
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val response = getLlmResponse(userMessage)
            _state.value = _state.value.copy(
                messages = _state.value.messages + Message(response, false),
                isLoading = false
            )
        }
    }

    private suspend fun getLlmResponse(prompt: String): String {
        val jsonBody = """
            {
              "model": "MODEL_NAME",
              "messages": [
                {"role": "system", "content": "Ты полезный ассистент."},
                {"role": "user", "content": "$prompt"}
              ],
              "temperature": 0.7,
              "stream": false
            }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(LOCAL_API_URL)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body.string() ?: "Пустой ответ"
                // Парсим JSON, чтобы извлечь текст ответа
                parseResponse(responseBody)
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            // Упрощённый парсинг — в реальном коде используйте JSON‑библиотеку
            val start = jsonResponse.indexOf("\"content\":\"") + 10
            val end = jsonResponse.indexOf("\"", start)
            if (start in 1..<end) {
                jsonResponse.substring(start, end)
            } else {
                "Не удалось извлечь ответ"
            }
        } catch (e: Exception) {
            "Ошибка парсинга: ${e.message}"
        }
    }
}

private fun MutableState<ChatState>.asStateFlow() {
    TODO("Not yet implemented")
}
