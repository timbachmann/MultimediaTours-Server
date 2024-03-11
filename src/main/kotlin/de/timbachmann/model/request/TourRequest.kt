package de.timbachmann.model.request

import de.timbachmann.model.entity.Tour
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