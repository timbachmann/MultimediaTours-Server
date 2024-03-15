package de.timbachmann.api.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import de.timbachmann.api.model.entity.MultimediaObject
import de.timbachmann.api.model.entity.User
import de.timbachmann.api.repository.interfaces.UserRepositoryInterface
import io.ktor.server.application.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.BsonValue
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class UserRepository(private val mongoDatabase: MongoDatabase, private val application: Application) : UserRepositoryInterface {

    companion object {
        const val USER_COLLECTION = "user"
    }

    override suspend fun createJwtToken(userId: String, email: String): String? {
        return JWT.create()
                .withIssuer(application.environment.config.property("ktor.jwt.issuer").getString())
                .withClaim("email", email)
                .withClaim("id", userId)
                .withExpiresAt(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .sign(Algorithm.HMAC256(application.environment.config.property("ktor.jwt.secret").getString()))
    }

    override suspend fun insertOne(user: User): BsonValue? {
        try {
            val result = mongoDatabase.getCollection<User>(USER_COLLECTION).insertOne(
                user
            )
            return result.insertedId
        } catch (e: MongoException) {
            System.err.println("Unable to insert due to an error: $e")
        }
        return null
    }

    override suspend fun deleteById(objectId: ObjectId): Long {
        try {
            val result = mongoDatabase.getCollection<MultimediaObject>(USER_COLLECTION).deleteOne(Filters.eq("_id", objectId))
            return result.deletedCount
        } catch (e: MongoException) {
            System.err.println("Unable to delete due to an error: $e")
        }
        return 0
    }

    override suspend fun getAll(): List<User> =
        mongoDatabase.getCollection<User>(USER_COLLECTION).withDocumentClass<User>()
            .find().toList()

    override suspend fun findByEmail(email: String): User? =
        mongoDatabase.getCollection<User>(USER_COLLECTION).withDocumentClass<User>()
            .find(Filters.eq("email", email)).firstOrNull()

    override suspend fun findById(id: ObjectId): User? =
        mongoDatabase.getCollection<User>(USER_COLLECTION).withDocumentClass<User>()
            .find(Filters.eq("_id", id)).firstOrNull()

    override suspend fun checkPassword(password: String, hash: String): Boolean = BCrypt.checkpw(password, hash)

    override suspend fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt());

    override suspend fun updateOne(objectId: ObjectId, user: User): Long {
        try {
            val query = Filters.eq("_id", objectId)
            val updates = Updates.combine(
                Updates.set(User::email.name, user.email),
                Updates.set(User::password.name, user.password),
                Updates.set(User::lastLogin.name, user.lastLogin),
                Updates.set(User::activeSessions.name, user.activeSessions),
                Updates.set(User::role.name, user.role)
            )
            val options = UpdateOptions().upsert(true)
            val result =
                mongoDatabase.getCollection<User>(USER_COLLECTION)
                    .updateOne(query, updates, options)

            return result.modifiedCount
        } catch (e: MongoException) {
            System.err.println("Unable to update due to an error: $e")
        }
        return 0
    }
}