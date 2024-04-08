package de.timbachmann.api.routes

import de.timbachmann.api.model.entity.Tour
import de.timbachmann.api.model.request.TourRequest
import de.timbachmann.api.repository.interfaces.TourRepositoryInterface
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject

fun Route.tourRouting() {

    val repository by inject<TourRepositoryInterface>()

    authenticate {
        route("/tours") {
            post {
                val newTour = call.receive<TourRequest>()
                val insertedId = repository.insertOne(newTour.toTourObject())
                call.respond(HttpStatusCode.Created, "Created tour with id $insertedId")
            }

            get {
                repository.getAll().let {
                    call.respond(it.map{obj -> obj.toResponse()})
                }
            }

            delete("/{id?}") {
                val id = call.parameters["id"] ?: return@delete call.respondText(
                    text = "Missing tour id",
                    status = HttpStatusCode.BadRequest
                )
                val delete: Long = repository.deleteById(ObjectId(id))
                if (delete == 1L) {
                    return@delete call.respondText("Tour Deleted successfully", status = HttpStatusCode.OK)
                }
                return@delete call.respondText("Tour not found", status = HttpStatusCode.NotFound)
            }

            get("/{id?}") {
                val id = call.parameters["id"]
                if (id.isNullOrEmpty()) {
                    return@get call.respondText(
                        text = "Missing id",
                        status = HttpStatusCode.BadRequest
                    )
                }
                repository.findById(ObjectId(id))?.let {
                    call.respond(it.toResponse())
                } ?: call.respondText("No records found for id $id")
            }

            put("/{id?}") {
                val id = call.parameters["id"] ?: return@put call.respondText(
                    text = "Missing tour id",
                    status = HttpStatusCode.BadRequest
                )
                val tourRequest = call.receive<TourRequest>()
                val updated = repository.updateOne(ObjectId(id), tourRequest.toTourObject())
                call.respondText(
                    text = if (updated.wasAcknowledged()) "Tour updated successfully" else updated.toString(),
                    status = if (updated.wasAcknowledged()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
                )
            }
        }
    }
}
