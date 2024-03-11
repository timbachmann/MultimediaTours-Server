package de.timbachmann.model.response

import de.timbachmann.model.entity.MultimediaObjectPosition
import de.timbachmann.model.entity.MultimediaType


data class MultimediaObjectResponse(
    val id: String,
    val type: MultimediaType,
    val title: String,
    val date: String,
    val source: String,
    val position: MultimediaObjectPosition?,
    val data: String,
    val author: String
)