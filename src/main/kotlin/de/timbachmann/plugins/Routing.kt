package de.timbachmann.plugins

import de.timbachmann.routes.multimediaObjectRouting
import de.timbachmann.routes.tourRouting
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
    }
}
