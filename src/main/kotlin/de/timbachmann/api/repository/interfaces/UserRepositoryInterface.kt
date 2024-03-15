package de.timbachmann.api.repository.interfaces

import de.timbachmann.api.model.entity.User
import org.bson.BsonValue
import org.bson.types.ObjectId

interface UserRepositoryInterface {

    suspend fun createJwtToken(userId: String, email: String): String?
    suspend fun insertOne(user: User): BsonValue?
    suspend fun deleteById(objectId: ObjectId): Long
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: ObjectId): User?
    suspend fun checkPassword(password: String, hash: String): Boolean
    suspend fun hashPassword(password: String): String
    suspend fun getAll(): List<User>
    suspend fun updateOne(objectId: ObjectId, user: User): Long
}