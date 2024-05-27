package de.timbachmann.plugins

import io.grpc.ManagedChannelBuilder
import org.vitrivr.cottontail.client.SimpleClient
import org.vitrivr.cottontail.client.language.dml.Insert
import org.vitrivr.cottontail.core.values.StringValue

var client: SimpleClient? = null

fun configureCottontail() {
    val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 1865).usePlaintext().build()
    client = SimpleClient(channel)
}

fun exportTagsToCottontail(id: String, tags: List<String>) {
    val localClient = client
    localClient?.let {
        val txId = localClient.begin()

        try {
            tags.forEach { tag ->
                localClient.insert(
                    Insert("warren.cineast.cineast_tags")
                        .value("id", StringValue("i_$id"))
                        .value("name", StringValue(tag))
                        .value("description", StringValue(tag))
                )
            }
            localClient.commit(txId)
        } catch (e: Throwable) {
            localClient.rollback(txId)
            e.printStackTrace()
        }
    }
}