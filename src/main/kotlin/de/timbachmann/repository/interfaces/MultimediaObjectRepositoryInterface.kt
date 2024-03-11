package de.timbachmann.repository.interfaces

import de.timbachmann.model.entity.MultimediaObject
import org.bson.BsonValue
import org.bson.types.ObjectId

interface MultimediaObjectRepositoryInterface {
    suspend fun insertOne(multimediaObject: MultimediaObject): BsonValue?
    suspend fun deleteById(objectId: ObjectId): Long
    suspend fun findById(objectId: ObjectId): MultimediaObject?
    suspend fun updateOne(objectId: ObjectId, multimediaObject: MultimediaObject): Long
}