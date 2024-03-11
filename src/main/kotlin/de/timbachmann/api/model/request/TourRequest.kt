package de.timbachmann.api.model.request

import de.timbachmann.api.model.entity.Tour
import org.bson.types.ObjectId

data class TourRequest(
    val id: String,
    val title: String,
    val source: String,
    val multimediaObjects: List<String>,
    val author: String
) {
    fun toTourObject(): Tour {
        return Tour(
            id = ObjectId(),
            title = title,
            source = source,
            multimediaObjects = multimediaObjects.map { ObjectId(it) },
            author = author
        )
    }
}