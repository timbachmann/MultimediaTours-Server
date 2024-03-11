package de.timbachmann.api.model.entity

import de.timbachmann.api.model.response.MultimediaObjectResponse
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class MultimediaObject(
    @BsonId
    val id: ObjectId,
    val type: MultimediaType,
    val title: String,
    val date: String,
    val source: String,
    val position: MultimediaObjectPosition?,
    val data: String,
    val author: String
){
    fun toResponse() = MultimediaObjectResponse(
        id = id.toString(),
        type = type,
        title = title,
        date = date,
        source = source,
        position = position,
        data = data,
        author = author
    )
}

data class MultimediaObjectPosition(
    val lat: Double,
    val lng: Double,
    val bearing: Int,
    val yaw: Float,
)