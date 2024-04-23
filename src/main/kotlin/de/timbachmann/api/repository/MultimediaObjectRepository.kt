package de.timbachmann.api.repository

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.timbachmann.api.model.entity.MultimediaObject
import de.timbachmann.api.model.entity.User
import de.timbachmann.api.repository.interfaces.MultimediaObjectRepositoryInterface
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.BsonValue
import org.bson.types.ObjectId

class MultimediaObjectRepository(private val mongoDatabase: MongoDatabase) : MultimediaObjectRepositoryInterface {

    companion object {
        const val MULTIMEDIAOBJECT_COLLECTION = "multimediaObject"
    }

    override suspend fun insertOne(multimediaObject: MultimediaObject): BsonValue? {
        try {
            val result = mongoDatabase.getCollection<MultimediaObject>(MULTIMEDIAOBJECT_COLLECTION).insertOne(
                multimediaObject
            )
            return result.insertedId
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
        }
        return null
    }

    override suspend fun deleteById(objectId: ObjectId): Long {
        try {
            val result = mongoDatabase.getCollection<MultimediaObject>(MULTIMEDIAOBJECT_COLLECTION).deleteOne(Filters.eq("_id", objectId))
            return result.deletedCount
        } catch (e: MongoException) {
            System.err.println("Unable to delete due to an error: $e")
        }
        return 0
    }

    override suspend fun getAll(): List<MultimediaObject> =
        mongoDatabase.getCollection<MultimediaObject>(MULTIMEDIAOBJECT_COLLECTION).withDocumentClass<MultimediaObject>()
            .find().toList()

    override suspend fun findById(objectId: ObjectId): MultimediaObject? =
        mongoDatabase.getCollection<MultimediaObject>(MULTIMEDIAOBJECT_COLLECTION).withDocumentClass<MultimediaObject>()
            .find(Filters.eq("_id", objectId)).firstOrNull()

    override suspend fun updateOne(objectId: ObjectId, multimediaObject: MultimediaObject): UpdateResult {
        val query = Filters.eq("_id", objectId)
        val updates = Updates.combine(
            Updates.set(MultimediaObject::type.name, multimediaObject.type),
            Updates.set(MultimediaObject::title.name, multimediaObject.title),
            Updates.set(MultimediaObject::date.name, multimediaObject.date),
            Updates.set(MultimediaObject::source.name, multimediaObject.source),
            if (multimediaObject.position !== null) { Updates.set(MultimediaObject::position.name, multimediaObject.position) }
            else { Updates.unset(MultimediaObject::position.name) },
            Updates.set(MultimediaObject::data.name, multimediaObject.data),
            Updates.set(MultimediaObject::author.name, multimediaObject.author)
        )
        val options = UpdateOptions().upsert(true)
        val result =
            mongoDatabase.getCollection<MultimediaObject>(MULTIMEDIAOBJECT_COLLECTION)
                .updateOne(query, updates, options)

        return result
    }
}