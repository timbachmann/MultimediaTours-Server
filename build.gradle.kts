val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    id("org.openapi.generator") version "7.6.0"
}

group = "de.timbachmann"
version = "0.0.1"

application {
    mainClass.set("de.timbachmann.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // MongoDB
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")

    // Koin
    implementation("io.insert-koin:koin-ktor:3.5.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")

    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.apache.commons:commons-lang3:3.14.0")

    //vitrivr
    implementation("org.vitrivr:cottontaildb-proto:0.15.0")
    implementation("io.grpc:grpc-all:1.51.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    // Tests
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}

openApiGenerate {
    inputSpec.set("$rootDir/resources/openapi/openapi-specs.json")
    generatorName.set("kotlin")
}

sourceSets {
    main {
        java {
            srcDir("${buildDir}/generate-resources/main/src")
        }
    }
}

tasks.compileKotlin {
    dependsOn("openApiGenerate")
}
