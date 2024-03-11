package de.timbachmann.model.entity

import de.timbachmann.model.response.TourResponse
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Tour(
    @BsonId
    val id: ObjectId,
    val title: String,
    val source: String,
    val multimediaObjects: List<ObjectId>,
    val author: String
){
    fun toResponse() = TourResponse(
        id = id.toString(),
        title = title,
        source = source,
        multimediaObjects = multimediaObjects.map{ it.toString() },
        author = author
    )
}