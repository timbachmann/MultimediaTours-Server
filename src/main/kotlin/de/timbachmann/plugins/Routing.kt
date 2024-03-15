package de.timbachmann.plugins

import de.timbachmann.api.routes.multimediaObjectRouting
import de.timbachmann.api.routes.tourRouting
import de.timbachmann.api.routes.userRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            call.respondRedirect("/swagger-ui")
        }
        multimediaObjectRouting()
        tourRouting()
        userRouting()
    }
}
