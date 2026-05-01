package com.application.combinationwithlmstudio.data.model


data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Message(
    val text: String,
    val isUser: Boolean
)