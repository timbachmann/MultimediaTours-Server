package de.timbachmann.repository

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.timbachmann.model.entity.MultimediaObject
import de.timbachmann.model.entity.Tour
import de.timbachmann.repository.interfaces.TourRepositoryInterface
import kotlinx.coroutines.flow.firstOrNull
import org.bson.BsonValue
import org.bson.types.ObjectId

class TourRepository(private val mongoDatabase: MongoDatabase) : TourRepositoryInterface {

    companion object {
        const val TOUR_COLLECTION = "tour"
    }

    override suspend fun insertOne(tour: Tour): BsonValue? {
        try {
            val result = mongoDatabase.getCollection<Tour>(TOUR_COLLECTION).insertOne(
                tour
            )
            return result.insertedId
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
        }
        return null
    }

    override suspend fun deleteById(objectId: ObjectId): Long {
        try {
            val result = mongoDatabase.getCollection<MultimediaObject>(TOUR_COLLECTION).deleteOne(Filters.eq("_id", objectId))
            return result.deletedCount
        } catch (e: MongoException) {
            System.err.println("Unable to delete due to an error: $e")
        }
        return 0
    }

    override suspend fun findById(objectId: ObjectId): Tour? =
        mongoDatabase.getCollection<Tour>(TOUR_COLLECTION).withDocumentClass<Tour>()
            .find(Filters.eq("_id", objectId)).firstOrNull()

    override suspend fun updateOne(objectId: ObjectId, tour: Tour): Long {
        try {
            val query = Filters.eq("_id", objectId)
            val updates = Updates.combine(
                Updates.set(Tour::title.name, tour.title),
                Updates.set(Tour::source.name, tour.source),
                Updates.set(Tour::multimediaObjects.name, tour.multimediaObjects),
                Updates.set(Tour::author.name, tour.author)
            )
            val options = UpdateOptions().upsert(true)
            val result =
                mongoDatabase.getCollection<Tour>(TOUR_COLLECTION)
                    .updateOne(query, updates, options)

            return result.modifiedCount
        } catch (e: MongoException) {
            System.err.println("Unable to update due to an error: $e")
        }
        return 0
    }
}