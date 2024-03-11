package de.timbachmann.plugins

import com.mongodb.kotlin.client.coroutine.MongoClient
import de.timbachmann.repository.MultimediaObjectRepository
import de.timbachmann.repository.TourRepository
import de.timbachmann.repository.interfaces.MultimediaObjectRepositoryInterface
import de.timbachmann.repository.interfaces.TourRepositoryInterface
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDatabase() {
    install(Koin) {
        modules(
            module {
                single<MultimediaObjectRepositoryInterface> { MultimediaObjectRepository(get()) }
                single<TourRepositoryInterface> { TourRepository(get()) }
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