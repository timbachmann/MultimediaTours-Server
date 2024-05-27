package de.timbachmann

import de.timbachmann.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*


fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() {

    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureDatabase()
    configureRouting()
    configureCottontail()
}