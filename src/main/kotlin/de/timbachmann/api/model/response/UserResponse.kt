package de.timbachmann.api.model.response


data class UserResponse(
    val id: String,
    val email: String,
    val password: String,
    val lastLogin: String,
    val activeSessions: List<UserSessionResponse>,
    val role: String
)

data class UserSessionResponse(
    val token: String,
    val CSRF: String,
    val validTo: String
)