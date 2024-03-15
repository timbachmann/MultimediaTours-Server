package de.timbachmann.api.model.request

import de.timbachmann.api.model.entity.User
import de.timbachmann.api.model.entity.UserSession
import org.bson.types.ObjectId
import java.util.*

data class UserRequest(
    val email: String,
    val password: String,
    val lastLogin: String,
    val activeSessions: List<UserSession>,
    val role: String
) {
    fun toUserObject(): User {
        return User(
            id = ObjectId(),
            email = email,
            password = password,
            lastLogin = Date(),
            activeSessions = listOf(),
            role = role
        )
    }
}
