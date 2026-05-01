package com.application.combinationwithlmstudio.data.model

data class ApiResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<Choice>,
    val created: Long? = null
) {
    data class Choice(
        val message: Message,
        val finishReason: String? = null
    )
}