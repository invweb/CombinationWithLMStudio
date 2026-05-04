package com.application.combinationwithlmstudio.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.combinationwithlmstudio.data.model.ChatState
import com.application.combinationwithlmstudio.data.model.Message
import com.application.combinationwithlmstudio.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ChatViewModel : ViewModel() {
    private val _state = mutableStateOf(ChatState())
    val state: MutableState<ChatState> = _state
//    val state: MutableState<ChatState> = _state.asStateFlow()

    private val client = OkHttpClient()

    fun sendMessage(userMessage: String) {
        // Adding the user's message
        _state.value = _state.value.copy(
            messages = _state.value.messages + Message(userMessage, true),
            isLoading = true,
            error = null
        )

        viewModelScope.launch(Dispatchers.IO) {
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
                          "model": "google/gemma-4-e4b",
                          "messages": [
                            {"role": "system", "input": "Ты полезный ассистент."},
                            {"content": "Отвечай кратко и по делу, используй русский язык."},
                            {
                              "role": "user",
                              "content": "Что такое авиация? Дай определение в одном предложении."
                            }
                          ],
                          "temperature": 0.7,
                          "max_tokens": 100,
                          "top_p": 0.9,
                          "stream": false
                        }
        """.trimIndent()


        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(Constants.LOCAL_API_URL)
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body.string() ?: "Пустой ответ"
                // Parse the JSON to extract the response text
                parseResponse(responseBody)
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun parseResponse(jsonResponse: String): String {
        return try {
            // Simplified parsing — use the JSON library in real code
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
