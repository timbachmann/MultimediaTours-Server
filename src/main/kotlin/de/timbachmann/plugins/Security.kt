package de.timbachmann.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import de.timbachmann.api.model.entity.User
import de.timbachmann.api.repository.interfaces.UserRepositoryInterface
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {
    val userRepository by inject<UserRepositoryInterface>()

    val jwtSecret = environment.config.property("ktor.jwt.secret").getString()
    val jwtIssuer = environment.config.property("ktor.jwt.issuer").getString()
    val jwtRealm = environment.config.property("ktor.jwt.realm").getString()

    val jwtVerifier: JWTVerifier =
        JWT.require(Algorithm.HMAC256(jwtSecret))
            .withIssuer(jwtIssuer)
            .build()

    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(jwtVerifier)
            validate { credential ->
                val username: String? = credential.payload.getClaim("email").asString()
                val foundUser: User? = userRepository.findByEmail(username!!)
                return@validate foundUser?.let {
                    JWTPrincipal(credential.payload)
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
