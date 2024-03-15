package de.timbachmann.api.routes

import de.timbachmann.api.model.entity.User
import de.timbachmann.api.model.entity.UserSession
import de.timbachmann.api.model.request.LoginRequest
import de.timbachmann.api.model.request.LogoutRequest
import de.timbachmann.api.model.response.LoginResponse
import de.timbachmann.api.model.response.UserLoginResponse
import de.timbachmann.api.repository.interfaces.UserRepositoryInterface
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.lang3.RandomStringUtils
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

fun Route.userRouting() {

    val repository by inject<UserRepositoryInterface>()

    route("/users") {
        post {
            val user = call.receive<LoginRequest>()
            if (repository.findByEmail(email = user.email) !== null) {
                call.respond(status = HttpStatusCode.Conflict,
                    message = "User already exists"
                )
            }
            val passwordHash = repository.hashPassword(user.password)
            repository.insertOne(User(
                id = ObjectId(),
                email = user.email,
                password = passwordHash,
                lastLogin = Date(0),
                activeSessions = listOf(),
                role = "ADMIN"
            ))?.let {
                call.respond(hashMapOf("id" to it.toString()))
            } ?: call.respond(HttpStatusCode.InternalServerError, "Could not register User")

        }

        get {
            repository.getAll().let {
                call.respond(it.map{obj -> obj.toResponse()})
            }
        }

        delete("/{id?}") {
            val id = call.parameters["id"] ?: return@delete call.respondText(
                text = "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val delete: Long = repository.deleteById(ObjectId(id))
            if (delete == 1L) {
                return@delete call.respondText("User Deleted successfully", status = HttpStatusCode.OK)
            }
            return@delete call.respondText("User not found", status = HttpStatusCode.NotFound)
        }

        get("/{email?}") {
            val email = call.parameters["email"]
            if (email.isNullOrEmpty()) {
                return@get call.respondText(
                    text = "Missing email",
                    status = HttpStatusCode.BadRequest
                )
            }
            repository.findByEmail(email)?.let {
                call.respond(it.toResponse())
            } ?: call.respondText("No records found for id $email")
        }

        patch("/{id?}") {
            val id = call.parameters["id"] ?: return@patch call.respondText(
                text = "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val updated = repository.updateOne(ObjectId(id), call.receive())
            call.respondText(
                text = if (updated == 1L) "User updated successfully" else "User not found",
                status = if (updated == 1L) HttpStatusCode.OK else HttpStatusCode.NotFound
            )
        }

        patch("/{id?}/password") {
            val id = call.parameters["id"] ?: return@patch call.respondText(
                text = "Missing user id",
                status = HttpStatusCode.BadRequest
            )
            val updated = repository.updateOne(ObjectId(id), call.receive())
            call.respondText(
                text = if (updated == 1L) "User updated successfully" else "User not found",
                status = if (updated == 1L) HttpStatusCode.OK else HttpStatusCode.NotFound
            )
        }

        post("/auth/login") {
            val userDTO = call.receive<LoginRequest>()
            val email = userDTO.email
            val password = userDTO.password

            val user = repository.findByEmail(email)

            if (user === null) {
                call.respond(HttpStatusCode.NotFound, "User not found.")
            }

            if (!repository.checkPassword(password, user!!.password)) {
                call.respond(HttpStatusCode.BadRequest, "Password incorrect.")
            }

            val token = repository.createJwtToken(user.id.toString(), user.email)
            if (token.isNullOrEmpty()) {
                call.respond(HttpStatusCode.InternalServerError, "Token could not be generated.")
            }
            val csrf = RandomStringUtils.random(20, 0, 0, true, true, null, SecureRandom());

            var activeSessions = user.activeSessions.filter{ it.validTo > Date() }
            activeSessions = activeSessions.plus(UserSession(token = token!!, CSRF = csrf, validTo = Date.from(Instant.now().plus(7, ChronoUnit.DAYS))))

            repository.updateOne(user.id, User(
                user.id, user.email, user.password, Date(), activeSessions, user.role
            ))

            call.respond(HttpStatusCode.OK, LoginResponse(token!!, UserLoginResponse(user.id.toString(), user.email, csrf)))
        }

        post("/auth/logout") {
            val request = call.receive<LogoutRequest>()
            repository.findById(ObjectId(request.id))?.let {
                val accessToken = call.request.header(HttpHeaders.Authorization)?.split(" ")?.get(1)
                val activeSessions = it.activeSessions.filter{ session -> session.token !== accessToken }
                repository.updateOne(it.id, User(
                    it.id, it.email, it.password, it.lastLogin, activeSessions, it.role
                )).let {
                    call.respond(HttpStatusCode.OK)
                }
            } ?: call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }
}
