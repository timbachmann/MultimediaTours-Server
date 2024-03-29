package de.timbachmann.api.routes


import de.timbachmann.api.model.request.MultimediaObjectRequest
import de.timbachmann.api.repository.interfaces.MultimediaObjectRepositoryInterface
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject

fun Route.multimediaObjectRouting() {
    val repository by inject<MultimediaObjectRepositoryInterface>()
    authenticate {
        route("/multimedia-objects") {
            post {
                val newObject = call.receive<MultimediaObjectRequest>()
                val insertedId = repository.insertOne(newObject.toMultimediaObject())
                call.respond(HttpStatusCode.Created, "Created multimediaObject with id $insertedId")
            }

            get {
                repository.getAll().let {
                    call.respond(it.map{obj -> obj.toResponse()})
                }
            }

            delete("/{id?}") {
                val id = call.parameters["id"] ?: return@delete call.respondText(
                    text = "Missing multimediaObject id",
                    status = HttpStatusCode.BadRequest
                )
                val delete: Long = repository.deleteById(ObjectId(id))
                if (delete == 1L) {
                    return@delete call.respondText("MultimediaObject Deleted successfully", status = HttpStatusCode.OK)
                }
                return@delete call.respondText("MultimediaObject not found", status = HttpStatusCode.NotFound)
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

            patch("/{id?}") {
                val id = call.parameters["id"] ?: return@patch call.respondText(
                    text = "Missing multimediaObject id",
                    status = HttpStatusCode.BadRequest
                )
                val updated = repository.updateOne(ObjectId(id), call.receive())
                call.respondText(
                    text = if (updated == 1L) "MultimediaObject updated successfully" else "MultimediaObject not found",
                    status = if (updated == 1L) HttpStatusCode.OK else HttpStatusCode.NotFound
                )
            }
        }
    }
}