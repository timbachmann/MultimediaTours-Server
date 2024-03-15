package de.timbachmann.api.model.response

data class LoginResponse(
    val token: String,
    val user: UserLoginResponse
)