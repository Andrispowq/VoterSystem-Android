package com.akmeczo.votersystem.server.requests

data class VoteChoiceRequest(
    val name: String,
    val description: String?
)
