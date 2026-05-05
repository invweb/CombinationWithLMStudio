package com.application.combinationwithlmstudio.utils

object Constants {
//    The IP ADDRESS where LM Studio is running
//    [LM STUDIO SERVER]   LM Studio API
//    [LM STUDIO SERVER]    ->  GET  http://192.168.0.10:1234/api/v1/models
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/api/v1/chat
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/api/v1/models/load
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/api/v1/models/download
//    [LM STUDIO SERVER]    ->  GET http://192.168.0.10:1234/api/v1/models/download/status:job_id
//    [LM STUDIO SERVER]   OpenAI-compatible
//    [LM STUDIO SERVER]    ->  GET  http://192.168.0.10:1234/v1/models
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/v1/responses
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/v1/chat/completions
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/v1/completions
//    [LM STUDIO SERVER]    ->  POST http://192.168.0.10:1234/v1/embeddings
    const val BASE_URL: String = "http://192.168.0.10:1234"
    const val LOCAL_API_URL = "$BASE_URL/api/v1/chat"
    const val LOCAL_MODELS_URL = "$BASE_URL/api/v1/models"
}