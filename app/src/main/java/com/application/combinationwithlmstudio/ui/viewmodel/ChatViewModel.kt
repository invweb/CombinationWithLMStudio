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
//        http://192.168.0.10:1234/api/v1/models - GET - list of available models
//        http://192.168.0.10:1234/api/v1/chat - POST -

//        val jsonBody = """
//                        {
//                          "model": "google/gemma-4-e4b",
//                          "input": "You are a useful assistant.",
//                          "messages": [
//                            {
//                                "role": "system",
//                                "input": "You are a useful assistant."
//                            },
//                            {
//                                "role": "system",
//                                "input": "Answer briefly and to the point."
//                            },
//                            {
//                                "role": "user",
//                                "input": "What is aviation? Define it in one sentence."
//                            }
//                          ],
//                          "temperature": 0.7,
//                          "top_p": 0.9,
//                          "stream": false
//                        }
//        """.trimIndent()

        val jsonBody = """
                        {
                          "model": "google/gemma-4-e4b",
                          "input": "You're a useful assistant. What is aviation? Define it in one sentence."
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
            "Error: ${e.message}"
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
                "Couldn't extract the response"
            }
        } catch (e: Exception) {
            "Parsing error: ${e.message}"
        }
    }
}
