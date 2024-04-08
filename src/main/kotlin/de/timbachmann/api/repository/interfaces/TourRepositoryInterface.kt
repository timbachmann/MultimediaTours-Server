package de.timbachmann.api.repository.interfaces

import com.mongodb.client.result.UpdateResult
import de.timbachmann.api.model.entity.Tour
import org.bson.BsonValue
import org.bson.types.ObjectId

interface TourRepositoryInterface {
    suspend fun insertOne(tour: Tour): BsonValue?
    suspend fun deleteById(objectId: ObjectId): Long
    suspend fun findById(objectId: ObjectId): Tour?
    suspend fun getAll(): List<Tour>
    suspend fun updateOne(objectId: ObjectId, tour: Tour): UpdateResult
}