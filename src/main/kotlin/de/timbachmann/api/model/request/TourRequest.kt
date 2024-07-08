package de.timbachmann.api.model.request

import de.timbachmann.api.model.entity.Tour
import org.bson.types.ObjectId

data class TourRequest(
    val title: String,
    val source: String,
    val multimediaObjects: List<String>,
    val author: String,
    val tags: List<String>
) {
    fun toTourObject(): Tour {
        return Tour(
            id = ObjectId(),
            title = title,
            source = source,
            multimediaObjects = multimediaObjects.map { ObjectId(it) },
            author = author,
            tags = tags,
            generated = false
        )
    }

    fun toTourObjectGenerated(): Tour {
        return Tour(
            id = ObjectId(),
            title = title,
            source = source,
            multimediaObjects = multimediaObjects.map { ObjectId(it) },
            author = author,
            tags = tags,
            generated = true
        )
    }
}