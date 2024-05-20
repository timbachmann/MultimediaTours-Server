package de.timbachmann.api.model.entity

import de.timbachmann.api.model.response.TourResponse
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Tour(
    @BsonId
    val id: ObjectId,
    val title: String,
    val source: String,
    val multimediaObjects: List<ObjectId>,
    val tags: List<String>,
    val author: String,
    val generated: Boolean
){
    fun toResponse() = TourResponse(
        id = id.toString(),
        title = title,
        source = source,
        multimediaObjects = multimediaObjects.map { it.toString() },
        author = author,
        tags = tags,
        generated = generated
    )
}