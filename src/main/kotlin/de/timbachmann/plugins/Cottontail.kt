package de.timbachmann.plugins

import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.basics.predicate.And
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.values.FloatValue
import org.vitrivr.cottontail.core.values.StringValue

var client: SimpleClient? = null

fun configureCottontail() {
    val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 1865).usePlaintext().build()
    client = SimpleClient(channel)
}

fun exportTagsToCottontail(id: String, tags: List<String>) {
    val localClient = client
    localClient?.let {
        try {
            tags.forEach { tag ->
                val query = Query("cineast.cineast_tags").select("*").where(Compare("id", "=", tag))
                val response = localClient.query(query)

                if (response.columns.isEmpty()) {
                    localClient.insert(
                        Insert("cineast.cineast_tags")
                            .value("id", StringValue(tag))
                            .value("name", StringValue(tag))
                            .value("description", StringValue(""))
                    )
                }

                val queryObject = Query("cineast.features_segmenttags").select("*").where(And(Compare("id", "=", "i_$id"), Compare("tagid", "=", tag)))
                val responseObject = localClient.query(queryObject)

                if (responseObject.columns.isEmpty()) {
                    localClient.insert(
                        Insert("cineast.features_segmenttags")
                            .value("id", StringValue("i_$id"))
                            .value("tagid", StringValue(tag))
                            .value("score", FloatValue(1.0))
                    )
                }
            }


        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}