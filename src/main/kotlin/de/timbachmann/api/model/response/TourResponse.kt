package de.timbachmann.api.model.response


data class TourResponse(
    val id: String,
    val title: String,
    val source: String,
    val multimediaObjects: List<String>,
    val author: String,
    val tags: List<String>,
    val generated: Boolean
)