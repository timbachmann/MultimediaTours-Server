package de.timbachmann.api.repository.interfaces

import com.mongodb.client.result.UpdateResult
import de.timbachmann.api.model.entity.MultimediaObject
import org.bson.BsonObjectId
import org.bson.BsonValue
import org.bson.types.ObjectId

interface MultimediaObjectRepositoryInterface {
    suspend fun insertOne(multimediaObject: MultimediaObject): BsonObjectId?
    suspend fun deleteById(objectId: ObjectId): Long
    suspend fun findById(objectId: ObjectId): MultimediaObject?
    suspend fun getAll(): List<MultimediaObject>
    suspend fun updateOne(objectId: ObjectId, multimediaObject: MultimediaObject): UpdateResult
}