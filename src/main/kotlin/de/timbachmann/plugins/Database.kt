package de.timbachmann.plugins

import com.mongodb.kotlin.client.coroutine.MongoClient
import de.timbachmann.api.repository.MultimediaObjectRepository
import de.timbachmann.api.repository.TourRepository
import de.timbachmann.api.repository.UserRepository
import de.timbachmann.api.repository.interfaces.MultimediaObjectRepositoryInterface
import de.timbachmann.api.repository.interfaces.TourRepositoryInterface
import de.timbachmann.api.repository.interfaces.UserRepositoryInterface
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDatabase() {
    install(Koin) {
        modules(
            module {
                single<MultimediaObjectRepositoryInterface> { MultimediaObjectRepository(get()) }
                single<TourRepositoryInterface> { TourRepository(get()) }
                single<UserRepositoryInterface> { UserRepository(get(), application = this@configureDatabase) }
            },
            module {
                single {
                    MongoClient.create(
                        environment.config.propertyOrNull("ktor.mongo.uri")?.getString()
                            ?: throw RuntimeException("Failed to access MongoDB URI.")
                    )
                }
                single {
                    get<MongoClient>().getDatabase(
                        environment.config.property("ktor.mongo.database").getString()
                    )
                }
            }
            )
    }
}