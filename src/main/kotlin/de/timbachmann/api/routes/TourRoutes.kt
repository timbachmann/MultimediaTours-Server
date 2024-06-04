package de.timbachmann.api.routes

import de.timbachmann.api.model.request.GenerateRequest
import de.timbachmann.api.model.request.TourRequest
import de.timbachmann.api.repository.interfaces.TourRepositoryInterface
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.bson.types.ObjectId
import org.koin.ktor.ext.inject
import org.openapitools.client.apis.SegmentsApi
import org.openapitools.client.models.QueryTerm
import org.openapitools.client.models.SimilarityQuery

fun Route.tourRouting() {

    val repository by inject<TourRepositoryInterface>()

    authenticate {
        route("/tours") {
            post {
                val newTour = call.receive<TourRequest>()
                val insertedId = repository.insertOne(newTour.toTourObject())
                call.respond(HttpStatusCode.Created, "Created tour with id $insertedId")
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

    route("/tours") {
        get {
            repository.getAll().let {
                call.respond(it.map{obj -> obj.toResponse()})
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
            } ?: call.respondText("No records found for id $id")
        }

        post("/generate") {
            val generateRequest = call.receive<GenerateRequest>()
            val searchQuery = generateRequest.searchQuery

            val queryTermEmbedding = QueryTerm(listOf("visualtexcoembedding"), QueryTerm.Type.TEXT, searchQuery)
            val queryTermTags = QueryTerm(listOf("tags"), QueryTerm.Type.TEXT, searchQuery.encodeBase64())
            val similarityQuery = SimilarityQuery(listOf(queryTermEmbedding, queryTermTags))
            val segmentsApi = SegmentsApi("http://localhost:4567")
            val similarityQueryResultBatch = segmentsApi.findSegmentSimilar(similarityQuery)

            repository.getAll().let {
                call.respond(it.map{obj -> obj.toResponse()}[0])
            }
        }
    }
}
