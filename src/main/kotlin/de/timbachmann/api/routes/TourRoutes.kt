package de.timbachmann.api.routes

import de.timbachmann.api.model.request.GenerateRequest
import de.timbachmann.api.model.request.TourRequest
import de.timbachmann.api.repository.interfaces.TourRepositoryInterface
import de.timbachmann.plugins.normalize
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
            val searchQueryWords = Regex("[^A-Za-z ]").replace(searchQuery, "").split(" ")

            val queryTermEmbedding = QueryTerm(listOf("visualtextcoembedding"), QueryTerm.Type.TEXT, searchQuery)
            val jsonListOfTags = "[${searchQueryWords.map { "{'id':'${it}'}" }.joinToString(",")}]"
            val queryTermTags = QueryTerm(listOf("tags"), QueryTerm.Type.TAG,
                "data:application/json;base64,${jsonListOfTags.encodeBase64()}")
            val similarityQuery = SimilarityQuery(listOf(queryTermTags, queryTermEmbedding))

            val segmentsApi = SegmentsApi("http://localhost:4567")
            val similarityQueryResultBatch = segmentsApi.findSegmentSimilar(similarityQuery)
            print(similarityQueryResultBatch)
            val ids = mutableListOf<String>()
            var max = 1.0
            var min = 0.0

            similarityQueryResultBatch.results?.let {
                it[1].content?.let { c ->
                    val values = c.map { e -> e.value ?: 0.0 }
                    max = values.maxOrNull() ?: 1.0
                    min = values.minOrNull() ?: 0.0
                }
            }

            similarityQueryResultBatch.results?.forEach { result ->
                result.content?.let { content ->
                    content.forEach { pair ->
                        pair.key?.split("_")?.let { key ->
                            pair.value?.let { value ->
                                if (value.toInt() == 1) {
                                    ids.add(key[1])
                                } else {
                                    val normalizedValue = normalize(value, min, max)
                                    if (normalizedValue >= 0.75) {
                                        ids.add(key[1])
                                    }
                                }
                            }
                        }
                    }
                }
            }
            println(ids)
            val multimediaObjects = ids.distinct()
            val newTour = TourRequest(title = searchQuery, source= "Generated by vitrivr",
                author = "Generated by vitrivr", tags = searchQueryWords, multimediaObjects = multimediaObjects)
            val finalTour = newTour.toTourObjectGenerated()
            repository.insertOne(finalTour)

            call.respond(HttpStatusCode.Created, finalTour.toResponse())
        }
    }
}
