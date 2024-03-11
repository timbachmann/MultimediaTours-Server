package de.timbachmann.model.request

import de.timbachmann.model.entity.MultimediaObject
import de.timbachmann.model.entity.MultimediaObjectPosition
import de.timbachmann.model.entity.MultimediaType
import org.bson.types.ObjectId


data class MultimediaObjectRequest(
    val id: String,
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