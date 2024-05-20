package de.timbachmann.api.routes


import de.timbachmann.api.model.entity.MultimediaType
import de.timbachmann.api.model.request.MultimediaObjectRequest
import de.timbachmann.api.repository.interfaces.MultimediaObjectRepositoryInterface
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject
import java.io.File

fun Route.multimediaObjectRouting() {
    val repository by inject<MultimediaObjectRepositoryInterface>()
    authenticate {
        route("/multimedia-objects") {
            post {
                val newObject = call.receive<MultimediaObjectRequest>()
                val insertedId = repository.insertOne(newObject.toMultimediaObject())
                call.respond(HttpStatusCode.Created, "Created multimediaObject with id $insertedId")
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

            put("/{id?}") {
                val id = call.parameters["id"] ?: return@put call.respondText(
                    text = "Missing multimediaObject id",
                    status = HttpStatusCode.BadRequest
                )
                val updated = repository.updateOne(ObjectId(id), call.receive())
                call.respondText(
                    text = if (updated.wasAcknowledged()) "MultimediaObject updated successfully" else updated.toString(),
                    status = if (updated.wasAcknowledged()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
                )
            }

            post("/upload/{id?}") {
                val id = call.parameters["id"] ?: return@post call.respondText(
                    text = "Missing multimediaObject id",
                    status = HttpStatusCode.BadRequest
                )
                var path: String? = null
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart {
                    when (it) {
                        is PartData.FileItem -> {
                            var fileName = it.originalFileName as String
                            fileName = id + fileName.substringAfterLast(".")
                            val bytes = it.streamProvider().readBytes()
                            path = "./multimedia/$fileName"
                            val file = path?.let { it1 -> File(it1) }
                            file?.writeBytes(bytes)
                        }

                        else -> {}
                    }
                    it.dispose()
                }

                path?.let {
                    call.respondText(it, status = HttpStatusCode.OK)
                } ?: call.respondText("Could not upload file!", status =  HttpStatusCode.InternalServerError)
            }
        }
    }

    route("/multimedia-objects") {
        get {
            repository.getAll().let {
                call.respond(it.map { obj -> obj.toResponse() })
            }
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
            } ?: call.respondText("No records found for id $id", status = HttpStatusCode.NotFound)
        }

        get("/file/{id?}") {
            val id = call.parameters["id"]
            if (id.isNullOrEmpty()) {
                return@get call.respondText(
                    text = "Missing id",
                    status = HttpStatusCode.BadRequest
                )
            }
            val multimediaObject = repository.findById(ObjectId(id))
            if (multimediaObject?.type !== MultimediaType.TEXT) {
                val path = multimediaObject?.data
                path?.let {
                    val file = File(it)
                    call.response.header(HttpHeaders.ContentDisposition, "attachment; filename=\"${file.name}\"")
                    call.respondFile(file)
                }
            } else {
                call.respondText("File not found or object is Type TEXT.", status = HttpStatusCode.NotFound)
            }
        }
    }
}