package de.timbachmann.api.model.request

import de.timbachmann.api.model.entity.MultimediaObject
import de.timbachmann.api.model.entity.MultimediaObjectPosition
import de.timbachmann.api.model.entity.MultimediaType
import org.bson.types.ObjectId


data class MultimediaObjectRequest(
    val type: MultimediaType,
    val title: String,
    val date: String,
    val source: String,
    val position: MultimediaObjectPosition?,
    val data: String,
    val author: String
) {
    fun toMultimediaObject(): MultimediaObject {
        return MultimediaObject(
            id = ObjectId(),
            type = type,
            title = title,
            date = date,
            source = source,
            position = position,
            data = data,
            author = author
        )
    }
}