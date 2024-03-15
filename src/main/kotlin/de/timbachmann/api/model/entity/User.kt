package de.timbachmann.api.model.entity

import de.timbachmann.api.model.response.UserResponse
import de.timbachmann.api.model.response.UserSessionResponse
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*

data class User(
    @BsonId
    val id: ObjectId,
    val email: String,
    val password: String,
    val lastLogin: Date,
    val activeSessions: List<UserSession>,
    val role: String
) {
    fun toResponse() = UserResponse(
        id = id.toString(),
        email = email,
        password = password,
        lastLogin = lastLogin.toString(),
        activeSessions = activeSessions.map { it.toResponse() },
        role = role
    )
}

data class UserSession(
    val token: String,
    val CSRF: String,
    val validTo: Date
) {
    fun toResponse() = UserSessionResponse(
        token = token,
        CSRF = CSRF,
        validTo = validTo.toString(),
    )
}