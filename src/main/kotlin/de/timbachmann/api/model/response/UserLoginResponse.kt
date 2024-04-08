package de.timbachmann.api.model.response

data class UserLoginResponse(
    val id: String,
    val email: String,
    val CSRF: String,
    val role: String,
)