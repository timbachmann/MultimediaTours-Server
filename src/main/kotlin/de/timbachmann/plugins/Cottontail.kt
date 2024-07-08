package de.timbachmann.plugins

import de.timbachmann.api.model.entity.MultimediaType
import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.And
import org.vitrivr.cottontail.client.language.basics.predicate.Expression
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dql.Query

var client: SimpleClient? = null

fun configureCottontail() {
    val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 1865).usePlaintext().build()
    client = SimpleClient(channel)
}

fun exportTagsToCottontail(id: String, tags: List<String>, type: MultimediaType) {
    val localClient = client
    localClient?.let {
        try {
            tags.forEach { tag ->
                val query = Query("cineast.cineast_tags").select("*").where(Expression("id", "=", tag))
                val response = localClient.query(query)

                if (!response.hasNext()) {
                    localClient.insert(
                        Insert("cineast.cineast_tags")
                            .value("id", tag)
                            .value("name", tag)
                            .value("description", "")
                    )
                }

                val prefix = when(type) {
                    MultimediaType.AUDIO -> "a"
                    MultimediaType.VIDEO -> "v"
                    MultimediaType.IMAGE -> "i"
                    else -> null
                }

                val queryObject = Query("cineast.features_segmenttags").select("*").where(And(Expression("id", "=", "${prefix}_$id"), Expression("tagid", "=", tag)))
                val responseObject = localClient.query(queryObject)

                if (!responseObject.hasNext()) {
                    localClient.insert(
                        Insert("cineast.features_segmenttags")
                            .value("id", "${prefix}_$id")
                            .value("tagid", tag)
                            .value("score", 1.0)
                    )
                }
            }


        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}